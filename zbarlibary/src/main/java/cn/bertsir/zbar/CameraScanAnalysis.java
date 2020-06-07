/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.bertsir.zbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.os.Environment;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.detector.Detector;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import android.app.Activity;

import cn.bertsir.zbar.Qr.Config;
import cn.bertsir.zbar.Qr.Image;
import cn.bertsir.zbar.Qr.ImageScanner;
import cn.bertsir.zbar.Qr.ScanResult;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.Qr.SymbolSet;
import cn.bertsir.zbar.utils.QRUtils;

/**
 */
class CameraScanAnalysis extends Activity  implements Camera.PreviewCallback {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ImageScanner mImageScanner;
    private Handler mHandler;
    private ScanCallback mCallback;
    private static final String TAG = "CameraScanAnalysis";

    private boolean allowAnalysis = true;
    private Image barcode;
    private int cropWidth;
    private int cropHeight;
    private Camera.Size size;
    private byte[] data;
    private Camera camera;
    private Context context;
    private long lastResultTime = 0;
    private final int CAMREA_RESQUSET = 1101;
    private ImageView imageView;

    private MultiFormatReader multiFormatReader = new MultiFormatReader();


    CameraScanAnalysis(Context context) {
        this.context = context;
        mImageScanner = new ImageScanner();
        if (Symbol.scanType == QrConfig.TYPE_QRCODE) {
            mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
            mImageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
        } else if (Symbol.scanType == QrConfig.TYPE_BARCODE) {
            mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
            mImageScanner.setConfig(Symbol.CODE128, Config.ENABLE, 1);
            mImageScanner.setConfig(Symbol.CODE39, Config.ENABLE, 1);
            mImageScanner.setConfig(Symbol.EAN13, Config.ENABLE, 1);
            mImageScanner.setConfig(Symbol.EAN8, Config.ENABLE, 1);
            mImageScanner.setConfig(Symbol.UPCA, Config.ENABLE, 1);
            mImageScanner.setConfig(Symbol.UPCE, Config.ENABLE, 1);
            mImageScanner.setConfig(Symbol.UPCE, Config.ENABLE, 1);
        } else if (Symbol.scanType == QrConfig.TYPE_ALL) {
            mImageScanner.setConfig(Symbol.NONE, Config.X_DENSITY, 3);
            mImageScanner.setConfig(Symbol.NONE, Config.Y_DENSITY, 3);
        } else if (Symbol.scanType == QrConfig.TYPE_CUSTOM) {
            mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
            mImageScanner.setConfig(Symbol.scanFormat, Config.ENABLE, 1);
        } else {
            mImageScanner.setConfig(Symbol.NONE, Config.X_DENSITY, 3);
            mImageScanner.setConfig(Symbol.NONE, Config.Y_DENSITY, 3);
        }

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (mCallback != null) mCallback.onScanResult((ScanResult) msg.obj);
            }
        };
    }

    void setScanCallback(ScanCallback callback) {
        this.mCallback = callback;
    }
    void nonStop() {
        this.allowAnalysis = false;
    }
    void nonStart() {
        this.allowAnalysis = true;
    }




    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        System.out.println("*********相机1"   );

        if (allowAnalysis) {
            allowAnalysis = false;
            this.data = data;
            this.camera = camera;

            size = camera.getParameters().getPreviewSize();
            barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            if (Symbol.is_only_scan_center) {
                //用于框中的自动拉伸和对识别数据的裁剪
                cropWidth = (int) (Symbol.cropWidth * (size.height / (float) Symbol.screenWidth));
                cropHeight = (int) (Symbol.cropHeight * (size.width / (float) Symbol.screenHeight));
                Symbol.cropX = size.width / 2 - cropHeight / 2;
                Symbol.cropY = size.height / 2 - cropWidth / 2;
                barcode.setCrop(Symbol.cropX, Symbol.cropY, cropHeight, cropWidth);
            }else {
                //用于全屏幕的自动拉升
                Symbol.cropX =0;
                Symbol.cropY = 0;
                cropWidth = size.width;
                cropHeight = size.height;
            }

            if(Symbol.looperScan  &&  (System.currentTimeMillis() - lastResultTime < Symbol.looperWaitTime)){
                allowAnalysis = true;
                return;
            }

            executorService.execute(mAnalysisTask);

        }
    }

    /**
     * 相机设置焦距
     */
    public void cameraZoom(Camera mCamera) {
//        System.out.println("*********相机2"   );
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (!parameters.isZoomSupported()) {
                return;
            }
            int maxZoom = parameters.getMaxZoom();
            if (maxZoom == 0) {
                return;
            }
            if (parameters.getZoom() + 10 > parameters.getMaxZoom()) {
                return;
            }
            parameters.setZoom(parameters.getZoom() + 10);
            mCamera.setParameters(parameters);
        }
    }


    private void  writePic2( ){
        Intent intent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, CAMREA_RESQUSET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMREA_RESQUSET && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
//            imageView.setImageBitmap(bitmap);

        }
    }




//    private void  writePic(Image image ,String resStr ) {
//        byte [] pic_data = image.getData();
//        int width=  image.width;
//        int height=  image.height;
//
////        System.out.println("*********图片"  + pic_data  );
//        System.out.println("*********图片"  + pic_data.length  );
//        System.out.println("********图片"  + image.width  + "    "  + image.height  );
//
//
//        Bitmap res_pic =  decodeToBitMap(pic_data, camera );
//
//        String savePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DCIM/Camera";
//
//        File folder = new File(savePath);
//        if (!folder.exists()) // 如果文件夹不存在则创建
//        {
//            folder.mkdir();
//
//        }
//        long dataTake = System.currentTimeMillis();
////        String filename = savePath + File.separator +"IMG_"+ dataTake +"_" +"_"+ ".txt";
//        String filename =  "IMG_"+ resStr + "_"+ ".JPEG";
//        System.out.println("*********写出"  + filename  );
//
//        saveBitmap2file( res_pic, savePath, filename);
////        saveFile(  filename, "ksajdflkjasdkdjflksdj");
//    }



    public Bitmap decodeToBitMap(byte[] data, Camera camera ) {
        if(data==null ){
            return null;
        }

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        try {
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
            if (image != null) {

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0,  image.getWidth(),  image.getHeight()),
                        100, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(
                        stream.toByteArray(), 0, stream.size());
                stream.close();
                return bmp;
            }
        } catch (Exception ex) {

        }
        return null;
    }




    public static void saveBitmap2file(Bitmap bmp,  String savePath, String fileName) {


//        if (Environment.getExternalStorageState().equals(
//                Environment.MEDIA_MOUNTED)) {
//            savePath = SD_PATH;
//        } else {
//            System.out.println( "保存失败！");
//            return ;
//        }
        File filePic = new File(savePath + File.separator  + fileName);
        try {
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            //Toast.makeText(context, "保存成功,位置:" + filePic.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    private Runnable mAnalysisTask = new Runnable() {
        @Override
        public void run() {
//            System.out.println("*********相机3"   );

            if (Symbol.is_auto_zoom && Symbol.scanType == QrConfig.TYPE_QRCODE
                    && QRUtils.getInstance().isScreenOriatationPortrait(context)) {

                if(Symbol.is_only_scan_center){
                    if (Symbol.cropX == 0 || Symbol.cropY == 0 || cropWidth == 0 || cropHeight == 0) {
                        return;
                    }
                }

                LuminanceSource source = new PlanarYUVLuminanceSource(data, size.width,
                        size.height, Symbol.cropX, Symbol.cropY, cropWidth, cropHeight, true);
                if (source != null) {
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    DetectorResult detectorResult = null;
                    try {
                        detectorResult = new Detector(bitmap.getBlackMatrix()).detect();

                        ResultPoint[] p = detectorResult.getPoints();
                        //计算扫描框中的二维码的宽度，两点间距离公式
                        float point1X = p[0].getX();
                        float point1Y = p[0].getY();
                        float point2X = p[1].getX();
                        float point2Y = p[1].getY();
                        int len = (int) Math.sqrt(Math.abs(point1X - point2X) * Math.abs(point1X - point2X) + Math.abs(point1Y - point2Y) * Math.abs(point1Y - point2Y));
                        int minZoomLen = 10;
                        if (len < cropWidth / 4 && len > minZoomLen) {
                            cameraZoom(camera);
                        }

                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            int result = mImageScanner.scanImage(barcode);




            String resultStr = null;
            int resultType = -1;
            System.out.println("*********扫码结果resint" + result  );

            if (result != 0) {
                SymbolSet symSet = mImageScanner.getResults();
                for (Symbol sym : symSet){
                    resultStr = sym.getData();
                    resultType= sym.getType();
                }

            }
            System.out.println("*********扫码结果resultStr" + resultStr  );
            System.out.println("*********扫码结果resultType" + resultType  );

            if (!TextUtils.isEmpty(resultStr)) {    //  扫码成功， 理论上来说在这里直接保存图片就行了, 但是这里保存的图片的质量非常低
//                writePic(  barcode, resultStr );
//                writePic2();

                ScanResult scanResult = new ScanResult();
                scanResult.setContent(resultStr);
                scanResult.setType(resultType == Symbol.QRCODE ? ScanResult.CODE_QR : ScanResult.CODE_BAR);
                Message message = mHandler.obtainMessage();
                message.obj = scanResult;
                message.sendToTarget();
                lastResultTime = System.currentTimeMillis();
                if (Symbol.looperScan) {
                    allowAnalysis = true;
                }


            } else {
                if (Symbol.doubleEngine) {
                    decode(data, size.width, size.height);
                } else {
                    allowAnalysis = true;
                }
            }
        }
    };


    /**
     * zxing解码
     *
     * @param data
     * @param width
     * @param height
     */
    private void decode(byte[] data, int width, int height) {
        System.out.println("*********相机4"   );
        Result rawResult = null;
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;
        data = rotatedData;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0,
                0, width, height, true);
        ;
        Hashtable<DecodeHintType, Object> scanOption = new Hashtable<>();
        scanOption.put(DecodeHintType.CHARACTER_SET, "utf-8");
        Collection<Reader> readers = new ArrayList<>();
        readers.add((new QRCodeReader()));
        scanOption.put(DecodeHintType.POSSIBLE_FORMATS,readers);
        multiFormatReader.setHints(scanOption);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap( new HybridBinarizer(source) );
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
                String resultStr = rawResult.toString();
                BarcodeFormat resultFormat = rawResult.getBarcodeFormat();
                if (!TextUtils.isEmpty(resultStr)) {
                    ScanResult scanResult = new ScanResult();
                    scanResult.setContent(resultStr);
                    scanResult.setType(resultFormat == BarcodeFormat.QR_CODE ? ScanResult.CODE_QR : ScanResult.CODE_BAR);
                    Message message = mHandler.obtainMessage();
                    message.obj = scanResult;
                    message.sendToTarget();
                    lastResultTime = System.currentTimeMillis();
                    if (Symbol.looperScan) {
                        allowAnalysis = true;
                    }
                } else allowAnalysis = true;
            } catch (ReaderException re) {
                allowAnalysis = true;
                //Log.i("解码异常",re.toString());
            } finally {
                multiFormatReader.reset();
            }
        } else {
            allowAnalysis = true;
        }

    }


}