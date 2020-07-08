package cn.bertsir.zbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
//import android.graphics.Camera;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;

import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


import cn.bertsir.zbar.utils.UploadUtil;


import cn.bertsir.zbar.Qr.ScanResult;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.utils.GetPathFromUri;
import cn.bertsir.zbar.utils.QRUtils;
import cn.bertsir.zbar.utils.UploadUtil;
//import cn.bertsir.zbar.utils.UploadUtil2;
import cn.bertsir.zbar.view.ScanView;
import cn.bertsir.zbar.utils.TcpRequest;
import cn.bertsir.zbar.view.VerticalSeekBar;

public class QRActivity extends Activity implements View.OnClickListener, SensorEventListener {

    private CameraPreview cp;
    private SoundPool soundPool;
    private ScanView sv;
    private ImageView mo_scanner_back;
    private ImageView iv_flash;
    private ImageView iv_album;
    private static final String TAG = "QRActivity";
    private TextView textDialog;
    private TextView tv_title;
    private FrameLayout fl_title;
    private TextView tv_des;

    private QrConfig options;
    static final int REQUEST_IMAGE_GET = 1;
    static final int REQUEST_PHOTO_CUT = 2;
    public static final int RESULT_CANCELED = 401;
    public final float AUTOLIGHTMIN = 10F;
    private Uri uricropFile;
    private String cropTempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cropQr.jpg";
    private VerticalSeekBar vsb_zoom;
    private AlertDialog progressDialog;
    private float oldDist = 1f;
    private String  IPAdress = "";
    private String  port = "";
    private int  picQuality;
    private static   String requestURL = "";
    private int finishSoundID = 0;
    private int errorSoundID = 0;
    private Context mContext;
    private Set<String> picSet ;
    private long  pre_scan_time;
    private long  new_scan_time;


    private static    String  picFileName = "" ;
    private static    String  picFileMarker = "" ;
    //用于检测光线
    private SensorManager sensorManager;
    private Sensor sensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        Log.i("zBarLibary", "version: "+BuildConfig.VERSION_NAME);
        options = (QrConfig) getIntent().getExtras().get(QrConfig.EXTRA_THIS_CONFIG);
        mContext = this;
        initParm();
        setContentView(R.layout.activity_qr);
        initView();
    }
//    @Override
//    protected void onDestroy() {
//
//    }


    /**
     * 获得用户设置
     */


    /**
     * 初始化参数
     */
    private void initParm() {
        switch (options.getSCREEN_ORIENTATION()) {
            case QrConfig.SCREEN_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case QrConfig.SCREEN_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case QrConfig.SCREEN_SENSOR:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
        Symbol.scanType = options.getScan_type();
        Symbol.scanFormat = options.getCustombarcodeformat();
        Symbol.is_only_scan_center = options.isOnly_center();
        Symbol.is_auto_zoom = options.isAuto_zoom();
        Symbol.doubleEngine = options.isDouble_engine();
        Symbol.looperScan = options.isLoop_scan();
        Symbol.looperWaitTime = options.getLoop_wait_time();
        Symbol.screenWidth = QRUtils.getInstance().getScreenWidth(this);
        Symbol.screenHeight = QRUtils.getInstance().getScreenHeight(this);
        // 服务器相关设置
        Symbol.upload = options.isUpload();
        IPAdress = options.getIPAdress();
        picQuality = options.getPicQuality();
        port = options.getPort();

//        requestURL = "http://192.168.0.100:8080/firstWeb_war_exploded/demo";

        requestURL =  "http://" + IPAdress +":" +port + "/" +options.getServerPath();
        System.out.println("得到的 url 是" + requestURL );
        if (options.isAuto_light()) {
            getSensorManager();
        }
        picSet = new HashSet<String>();
        new_scan_time = 0;
        pre_scan_time = 0;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        cp = (CameraPreview) findViewById(R.id.cp);
        //bi~
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        finishSoundID = soundPool.load(this, options.getDing_path(), 1);
        errorSoundID = soundPool.load(this,  R.raw.error, 1);
//        soundPool.load(this, options.getDing_path(), 1);

        sv = (ScanView) findViewById(R.id.sv);
        sv.setType(options.getScan_view_type());
//        System.out.println(  "用的时候， 扫码类型是" +  options.getScan_view_type( ) );
//        sv.setType( 2 );

        mo_scanner_back = (ImageView) findViewById(R.id.mo_scanner_back);
        mo_scanner_back.setOnClickListener(this);
        mo_scanner_back.setImageResource(options.getBackImgRes());

        iv_flash = (ImageView) findViewById(R.id.iv_flash);
        iv_flash.setOnClickListener(this);
        iv_flash.setImageResource(options.getLightImageRes());


        iv_album = (ImageView) findViewById(R.id.iv_album);
        iv_album.setOnClickListener(this);
        iv_album.setImageResource(options.getAblumImageRes());

        tv_title = (TextView) findViewById(R.id.tv_title);
        fl_title = (FrameLayout) findViewById(R.id.fl_title);
        tv_des = (TextView) findViewById(R.id.tv_des);

        vsb_zoom = (VerticalSeekBar) findViewById(R.id.vsb_zoom);

        iv_album.setVisibility(options.isShow_light() ? View.VISIBLE : View.GONE);
        fl_title.setVisibility(options.isShow_title() ? View.VISIBLE : View.GONE);
        iv_flash.setVisibility(options.isShow_light() ? View.VISIBLE : View.GONE);
        iv_album.setVisibility(options.isShow_album() ? View.VISIBLE : View.GONE);
        tv_des.setVisibility(options.isShow_des() ? View.VISIBLE : View.GONE);
        vsb_zoom.setVisibility(options.isShow_zoom() ? View.VISIBLE : View.GONE);

        tv_des.setText(options.getDes_text());
        tv_title.setText(options.getTitle_text());
        fl_title.setBackgroundColor(options.getTITLE_BACKGROUND_COLOR());
        tv_title.setTextColor(options.getTITLE_TEXT_COLOR());

        sv.setCornerColor(options.getCORNER_COLOR());
        sv.setLineSpeed(options.getLine_speed());
        sv.setLineColor(options.getLINE_COLOR());
        sv.setScanLineStyle(options.getLine_style());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setSeekBarColor(vsb_zoom, options.getCORNER_COLOR());
        }
        vsb_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cp.setZoom((progress / 100f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    /**
     * 获取光线传感器
     */
    public void getSensorManager() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setSeekBarColor(SeekBar seekBar, int color) {
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * 识别结果回调
     */




    private static Bitmap AddTimeWatermark(Bitmap mBitmap, String result) {
        //获取原始图片与水印图片的宽与高
        int mBitmapWidth = mBitmap.getWidth();
        int mBitmapHeight = mBitmap.getHeight();

        Bitmap mNewBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mNewBitmap);
        //向位图中开始画入MBitmap原始图片
        mCanvas.drawBitmap(mBitmap,0,0,null);
        //添加文字
        Paint mPaint = new Paint();
        String date_str = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss EEEE").format(new Date());
        String  timeStr  = "拍摄时间:  "  +  date_str;
        String  barcodeStr   = "条形码: "  +  result ;
        //String mFormat = TingUtils.getTime()+"\n"+" 纬度:"+GpsService.latitude+"  经度:"+GpsService.longitude;
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(300);
        //水印的位置坐标
//        mCanvas.drawText(mFormat, (mBitmapWidth * 1) / 10,(mBitmapHeight*14)/15,mPaint);
        mCanvas.drawText(timeStr,   400,400 ,mPaint);
        mCanvas.drawText(barcodeStr,   400,800 ,mPaint);
        mCanvas.save(Canvas.ALL_SAVE_FLAG);
        mCanvas.restore();
        return mNewBitmap;
    }

    private String uploadPic2(String filePath, String uploadUrl ) {
        System.out.println("进入异步线程");

        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setChunkedStreamingMode(51200);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(6*1000);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection
                    .getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\""
                    + filePath.substring(filePath.lastIndexOf("/") + 1)
                    + "\"" + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(filePath);
            long total = fis.available();
            String totalstr = String.valueOf(total);
            Log.d("文件大小", totalstr);
            byte[] buffer = new byte[1024*10]; // 8k
            int count = 0;
            int length = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
                length += count;
//                    publishProgress((int) ((length / (float) total) * 100));
                //为了演示进度,休眠500毫秒
                //Thread.sleep(500);
            }

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            fis.close();
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            @SuppressWarnings("unused")
            String result = br.readLine();
            dos.close();
            is.close();
            System.out.println("上传成功");
            return "上传成功";

        }catch (Exception e) {
            e.printStackTrace();
            return "上传失败";
        }

    }
    private String uploadPic(String filePath, String uploadUrl ) {
        System.out.println("进入异步线程");

        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setChunkedStreamingMode(51200);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(6*1000);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);


            DataOutputStream dos = new DataOutputStream(httpURLConnection
                    .getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\""
                    + filePath.substring(filePath.lastIndexOf("/") + 1)
                    + "\"" + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(filePath);
            long total = fis.available();
            String totalstr = String.valueOf(total);
            Log.d("文件大小", totalstr);
            byte[] buffer = new byte[1024*10]; // 8k
            int count = 0;
            int length = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
                length += count;
//                    publishProgress((int) ((length / (float) total) * 100));
                //为了演示进度,休眠500毫秒
                //Thread.sleep(500);
            }

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            fis.close();
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            @SuppressWarnings("unused")
            String result = br.readLine();
            dos.close();
            is.close();
            System.out.println("上传成功");
            return "上传成功";

        }catch (Exception e) {
            e.printStackTrace();
            return "上传失败";
        }

    }

    private static String bitmapToBase64(Bitmap bitmap, int picQuality) {  // 压缩会显著减少要传输的数据
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {

                Matrix matrix = new Matrix();
                matrix.setScale(0.5f, 0.5f);
                Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap = bm;

                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, picQuality, baos);
                byte[] bitmapBytes = baos.toByteArray();
//                System.out.println("压缩长度是" + bitmapBytes. length );


                baos.flush();
                baos.close();


                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }




    private class MyTask extends  AsyncTask<byte[], Integer , String >{
        @Override
        protected void onPreExecute(  ) {

//            mTvProgress.setText("loading...");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            mPgBar.setProgress(values[0]);
//            mTvProgress.setText("loading..." + values[0] + "%");
        }

        @Override
        protected void onPostExecute(String result) {
            if (result ==  "上传失败" ){
                soundPool.play(errorSoundID, 1, 1, 0, 0, 1);
            }
        }
        @Override
        protected String doInBackground(byte[] ... params) {
//            System.out.println("进入异步线程");
//            System.out.println("请求的URL地址是： " + requestURL );
            int compressPicQuality = picQuality;
            byte data [] = params[0];
            String uploadUrl = requestURL;
            String fileName = picFileName;
            String picMarker = picFileMarker;


            Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length );
            final Bitmap mTimeWatermark = AddTimeWatermark(mBitmap,  picMarker);    //时间水印

            String picData = bitmapToBase64(mTimeWatermark, compressPicQuality);
            String end = "\r\n";
            String twoHyphens = "--";
            String boundary = "******";
            try {
                URL url = new URL(uploadUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setChunkedStreamingMode(51200);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(6*1000);
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("Charset", "UTF-8");
                httpURLConnection.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                httpURLConnection.setRequestProperty("picname", picFileName );

                DataOutputStream dos = new DataOutputStream(httpURLConnection
                        .getOutputStream());
                dos.writeBytes(twoHyphens + boundary + end);
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\""
                        + fileName  + "\"" + end);
                dos.writeBytes(end);


                byte[] buffer = new byte[8192]; // 8k
                int count = 0;
                int length = 0;
                dos.write( picData.getBytes()  );

                dos.writeBytes(end);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
                dos.flush();

                InputStream is;
                int code = httpURLConnection.getResponseCode();
                if (code == 200) {
                    is = httpURLConnection.getInputStream(); // 得到网络返回的正确输入流
//                    System.out.println("得到正确的返回流");
                } else {
                    is = httpURLConnection.getErrorStream(); // 得到网络返回的错误输入流
//                    System.out.println("得到正确的返回流");
                }
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String result = br.readLine();

//                System.out.println("服务器的返回结果是  " + result );
                dos.close();
                br.close();

                if (  result.contains( "success"  )  ) {
//                    System.out.println("上传成功");
                    return "上传成功";
                }


            }catch (Exception e) {
                e.printStackTrace();
                return "上传失败";
            }
            return "上传失败";
        }



        protected String doInBackground_bk(byte[] ... params) {
            System.out.println("进入异步线程");
            int compressPicQuality = picQuality;
            byte data [] = params[0];
            String uploadUrl = requestURL;
            String fileName = picFileName;
            String picMarker = picFileMarker;

            Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length );
            final Bitmap mTimeWatermark = AddTimeWatermark(mBitmap,  picMarker);    //时间水印



            String picData = bitmapToBase64(mTimeWatermark, compressPicQuality);
            String end = "\r\n";
            String twoHyphens = "--";
            String boundary = "******";
            try {
                URL url = new URL(uploadUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setChunkedStreamingMode(51200);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(6*1000);
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("Charset", "UTF-8");
                httpURLConnection.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(httpURLConnection
                        .getOutputStream());
                dos.writeBytes(twoHyphens + boundary + end);
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\""
                        + fileName  + "\"" + end);
                dos.writeBytes(end);


                byte[] buffer = new byte[8192]; // 8k
                int count = 0;
                int length = 0;
                dos.write( picData.getBytes()  );

                dos.writeBytes(end);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
                dos.flush();

                InputStream is;
                int code = httpURLConnection.getResponseCode();
                if (code == 200) {
                    is = httpURLConnection.getInputStream(); // 得到网络返回的正确输入流
                    System.out.println("得到正确的返回流");
                } else {
                    is = httpURLConnection.getErrorStream(); // 得到网络返回的错误输入流
                    System.out.println("得到正确的返回流");
                }
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String result = br.readLine();

                dos.close();
                br.close();
                System.out.println("上传成功");
                return "上传成功";

            }catch (Exception e) {
                e.printStackTrace();
                return "上传失败";
            }
        }



    }  // MyTask 结束


    public void saveToLocal( String picFilePath, byte data[] ) {

        File pictureFile = new File( picFilePath );
        try {
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }
            File mFile = pictureFile ;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length );
            Bitmap mTimeWatermark = AddTimeWatermark(bitmap,  picFileMarker);    //时间水印


            Matrix matrix = new Matrix();    //采样
            matrix.setScale(0.5f, 0.5f);
            Bitmap bm = Bitmap.createBitmap(mTimeWatermark, 0, 0, mTimeWatermark.getWidth(), mTimeWatermark.getHeight(), matrix, true);
            mTimeWatermark = bm;


            BufferedOutputStream mOutputStream = new BufferedOutputStream(new FileOutputStream(mFile));
            // 将图片压缩到流中
            mTimeWatermark.compress(Bitmap.CompressFormat.JPEG, options.picQuality,  mOutputStream);      //时间水印
            mOutputStream.flush();
            mOutputStream.close();
//            System.out.println("图片的质量是：" +  options.picQuality );
            Uri uri = Uri.fromFile(pictureFile);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }



    public void takePicture(final ImageView view,  Camera camera, final String picName){
        picFileMarker = picName;
        picFileName  =  picName + ".jpg";

        camera.takePicture(null,null,new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
//                System.out.println("*********写回位置图片*********"     );
                final String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/kuaidi/";
                File pic_dir = new File( path );
                final String picFilePath =  path + File.separator + "快递_" + picName  + ".jpg"  ;
                final byte picData []  =   data;

                new_scan_time   = System.currentTimeMillis();
                long scan_delta =( new_scan_time - pre_scan_time) /1000;


                if( picSet.contains( picName ) &&  scan_delta < 6  ) { // 如果已经检测过了, 而且上一次检测这张快递在5秒钟之内


                } else {  // 如果没检测过

                    if (!pic_dir.exists() ) {
                        try {
                            //按照指定的路径创建文件夹
                            pic_dir.mkdirs();

                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }

                    if( options.isUpload()  ){  //  如果上传到服务器
                        MyTask mTask = new MyTask();
                        mTask.execute( data );

                    } else {   // 保存到本地

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                saveToLocal(picFilePath, picData );
                            }
                        }).start();
                    }


                }


                pre_scan_time = new_scan_time;  // 记录检测时间
                picSet.add( picName ); // 将已经检测到的 快递 加入到集合当中
                camera.startPreview();

            }
        });

    }


//    // 照片回调
//    private final class MyPictureCallback implements Camera.PictureCallback {
//        // 照片生成后
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera) {
//            try {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                Matrix matrix = new Matrix();
//                matrix.setRotate(270);
//
////                Environment.getExternalStorageDirectory().getPath() + File.separator + "DCIM/Camera";
//                String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/camera";
//                File jpgFile = new File( path );
//                System.out.println("*********写回位置图片"  + path  );
//                if (!jpgFile.exists()) {
//                    jpgFile.mkdir();
//                }
//                File jpgFile1 = new File(jpgFile.getAbsoluteFile(), System.currentTimeMillis() + ".jpg");
//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                FileOutputStream fos = new FileOutputStream(jpgFile1);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
////                ToastUtils.show(getApplicationContext(), getString(R.string.save_success));
//                fos.close();
//                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri uri = Uri.fromFile(jpgFile1);
//                intent.setData(uri);
//                sendBroadcast(intent);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }finally {
//                if(Build.VERSION.SDK_INT>=24){
////                    reset();
//                }
////                isTakingPhoto = false;
//            }
//        }
//    }


    private ScanCallback resultCallback = new ScanCallback() {
        @Override
        public void onScanResult(ScanResult result) {


            Camera mCamera = cp.getCamera();

//            System.out.println("是否为空" + mCamera == null );
//            if (mCamera != null)
//                System.out.println("写回第二章图片"  );
//                mCamera.takePicture(null, null, new MyPictureCallback());

            final  String res_str = result.getContent();

            takePicture(null, mCamera, res_str );


            if (options.isPlay_sound()) {
                soundPool.play(finishSoundID, 1, 1, 0, 0, 1);
            }
            if (options.isShow_vibrator()) {
                QRUtils.getInstance().getVibrator(getApplicationContext());
            }

            if (cp != null) {
                cp.setFlash(false);
            }
            QrManager.getInstance().getResultCallback().onScanSuccess(result);
            if (!Symbol.looperScan) {
                finish();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_album) {
            fromAlbum();
        } else if (v.getId() == R.id.iv_flash) {
            if (cp != null) {
                cp.setFlash();
            }
        } else if (v.getId() == R.id.mo_scanner_back) {
            setResult(RESULT_CANCELED);//兼容混合开发
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (cp != null) {
            cp.setScanCallback(resultCallback);
            cp.start();
        }

        if (sensorManager != null) {
            //一般在Resume方法中注册
            /**
             * 第三个参数决定传感器信息更新速度
             * SensorManager.SENSOR_DELAY_NORMAL:一般
             * SENSOR_DELAY_FASTEST:最快
             * SENSOR_DELAY_GAME:比较快,适合游戏
             * SENSOR_DELAY_UI:慢
             */
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cp != null) {
            cp.stop();
        }
        if (sensorManager != null) {
            //解除注册
            sensorManager.unregisterListener(this, sensor);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (cp != null) {
            cp.setFlash(false);
            cp.stop();
        }

        soundPool.release();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_GET:
                    if (options.isNeed_crop()) {
                        cropPhoto(data.getData());
                    } else {
                        recognitionLocation(data.getData());
                    }
                    break;
                case Crop.REQUEST_CROP:
                    recognitionLocation(uricropFile);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 从相册选择
     */
    private void fromAlbum() {
        if (QRUtils.getInstance().isMIUI()) {//是否是小米设备,是的话用到弹窗选取入口的方法去选取
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(Intent.createChooser(intent, options.getOpen_album_text()), REQUEST_IMAGE_GET);
        } else {//直接跳到系统相册去选取
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT < 19) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
            } else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
            }
            startActivityForResult(Intent.createChooser(intent, options.getOpen_album_text()), REQUEST_IMAGE_GET);
        }
    }

    /**
     * 识别本地
     *
     * @param uri
     */
    private void recognitionLocation(Uri uri) {
//        System.out.println("*********活动1"   );
        final String imagePath = GetPathFromUri.getPath(this, uri);
        textDialog = showProgressDialog();
//        textDialog.setText("请稍后...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (TextUtils.isEmpty(imagePath)) {
                        Toast.makeText(getApplicationContext(), "获取图片失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //优先使用zbar识别一次二维码
                    final String qrcontent = QRUtils.getInstance().decodeQRcode(imagePath);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ScanResult scanResult = new ScanResult();
                            if (!TextUtils.isEmpty(qrcontent)) {
                                closeProgressDialog();
                                scanResult.setContent(qrcontent);
                                scanResult.setType(ScanResult.CODE_QR);
                                QrManager.getInstance().getResultCallback().onScanSuccess(scanResult);
                                QRUtils.getInstance().deleteTempFile(cropTempPath);//删除裁切的临时文件
                                finish();
                            } else {
                                //尝试用zxing再试一次识别二维码
                                final String qrcontent = QRUtils.getInstance().decodeQRcodeByZxing(imagePath);
                                if (!TextUtils.isEmpty(qrcontent)) {
                                    closeProgressDialog();
                                    scanResult.setContent(qrcontent);
                                    scanResult.setType(ScanResult.CODE_QR);
                                    QrManager.getInstance().getResultCallback().onScanSuccess(scanResult);
                                    QRUtils.getInstance().deleteTempFile(cropTempPath);//删除裁切的临时文件
                                    finish();
                                } else {
                                    //再试试是不是条形码
                                    try {
                                        String barcontent = QRUtils.getInstance().decodeBarcode(imagePath);
                                        if (!TextUtils.isEmpty(barcontent)) {
                                            closeProgressDialog();
                                            scanResult.setContent(barcontent);
                                            scanResult.setType(ScanResult.CODE_BAR);
                                            QrManager.getInstance().getResultCallback().onScanSuccess(scanResult);
                                            QRUtils.getInstance().deleteTempFile(cropTempPath);//删除裁切的临时文件
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "识别失败！", Toast.LENGTH_SHORT).show();
                                            closeProgressDialog();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), "识别异常！", Toast.LENGTH_SHORT).show();
                                        closeProgressDialog();
                                        e.printStackTrace();
                                    }

                                }

                            }
                        }
                    });


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "识别异常！", Toast.LENGTH_SHORT).show();
                    closeProgressDialog();
                }
            }
        }).start();
    }

    /**
     * 裁切照片
     *
     * @param uri
     */
    public void cropPhoto(Uri uri) {
        System.out.println("*********活动2"   );
        uricropFile = Uri.parse("file://" + "/" + cropTempPath);
        Crop.of(uri, uricropFile).asSquare().start(this);
    }


    public TextView showProgressDialog() {
        System.out.println("*********活动3"   );
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setCancelable(false);
        View view = View.inflate(this, R.layout.dialog_loading, null);
        builder.setView(view);
        ProgressBar pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        TextView tv_hint = (TextView) view.findViewById(R.id.tv_hint);
        if (Build.VERSION.SDK_INT >= 23) {
            pb_loading.setIndeterminateTintList(getColorStateList(R.color.dialog_pro_color));
        }
        progressDialog = builder.create();
        progressDialog.show();

        return tv_hint;
    }

    public void closeProgressDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("*********活动4"   );
        if (options.isFinger_zoom()) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = QRUtils.getInstance().getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 2) {
                        float newDist = QRUtils.getInstance().getFingerSpacing(event);
                        if (newDist > oldDist) {
                            cp.handleZoom(true);
                        } else if (newDist < oldDist) {
                            cp.handleZoom(false);
                        }
                        oldDist = newDist;
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float light = event.values[0];
        if (light < AUTOLIGHTMIN) {//暂定值
            if (cp.isPreviewStart()) {
                cp.setFlash(true);
                sensorManager.unregisterListener(this, sensor);
                sensor = null;
                sensorManager = null;
            }
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
