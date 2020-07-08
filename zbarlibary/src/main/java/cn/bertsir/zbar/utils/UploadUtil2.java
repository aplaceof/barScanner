//package cn.bertsir.zbar.utils;
//
//import android.os.AsyncTask;
//import android.os.Environment;
//import android.preference.PreferenceActivity;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.loopj.android.http.AsyncHttpClient;
//import com.loopj.android.http.AsyncHttpResponseHandler;
//import com.loopj.android.http.RequestParams;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import org.apache.http;
//import org.json.JSONObject;
//
///**
// *
// * 上传工具类
// * @author spring sky
// * Email:vipa1888@163.com
// * QQ:840950105
// * MyName:石明政
// */
//public class UploadUtil2 extends AsyncTask<String, Integer, String> {
//    public UploadUtil2() {
//            }
////    private static final String TAG = "uploadFile";
////    private static final int TIME_OUT = 10*1000;   //超时时间
////    private static final String CHARSET = "utf-8"; //设置编码
//    /**
//     * android上传文件到服务器
//     * @return  返回响应的内容
//*/
//
//
//
//
//    protected void onPostExecute(String result) {
////        MainActivity.this.mTvProgress.setText(result);
//    }
//
//    protected void onPreExecute() {
////        MainActivity.this.mTvProgress.setText("loading...");
//    }
//
//    protected void onProgressUpdate(Integer... values) {
////        MainActivity.this.mPgBar.setProgress(values[0]);
////        MainActivity.this.mTvProgress.setText("loading..." + values[0] + "%");
//    }
//
//
//
//    public void uploadFile(ArrayList<String> sendFilesPath) {
//        if (sendFilesPath.size() == 0)
//            return ;
//
//        String strUploadFile = mstrIP + mstrUploadFile;
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.setURLEncodingEnabled(false);
//
//        RequestParams params = new RequestParams();
////        params.put("user_name", mstrUser);
////        params.put("token", mstrCheckPass);
//        params.put("dir_parent", "@sys");
//        //批量上传
//        for (int i = 0; i < sendFilesPath.size(); i++) {
//            File myFile = new File(sendFilesPath.get(i));
//            try {
//                params.put(myFile.getName(), myFile);
//            } catch (FileNotFoundException e1) {
//                continue;
//            }
//        }
//
//        client.setTimeout(10000);
//        client.post(strUploadFile, params, new AsyncHttpResponseHandler() {
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers,
//                                  byte[] responseBody, Throwable arg3) {
//                Log.i("Show", "upload failed");
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers,
//                                  byte[] responseBody) {
//                String responseData = new String();
//                responseData = new String(responseBody);
//                try {
//                    JSONObject jsonObject = new JSONObject(responseData);
//                    int status = jsonObject.getInt("status");
//                    if (status == 1) {
//                        Log.i("Show", "upload 1");
//                    }
//                } catch (Exception e) {
//                }
//            }
//
//            @Override
//            public void onProgress(int bytesWritten, int totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//                int count = (int) ((bytesWritten * 1.0 / totalSize) * 100);
//                // 上传进度显示
////                progress.setProgress(count);
//                Log.e("上传 Progress>>>>>", bytesWritten + " / " + totalSize);
//            }
//
//            @Override
//            public void onRetry(int retryNo) {
//                super.onRetry(retryNo);
//                // 返回重试次数
//            }
//        });
//    }
//
//
//    public void uploadFile2(String url , String filePath ) {
//        //服务器端地址
////        String url = "http://192.168.0.101:8080/second/UploadServlet";
//        //手机端要上传的文件，首先要保存你手机上存在该文件
//
//
//        AsyncHttpClient httpClient = new AsyncHttpClient();
//
//        RequestParams param = new RequestParams();
//        try {
//            param.put("file", new File(filePath));
//            param.put("content", "picture");
//
//            httpClient.post(url, param, new AsyncHttpResponseHandler() {
//                @Override
//                public void onStart() {
//                    super.onStart();
//
////                    uploadInfo.setText("正在上传...");
//                    System.out.println("正在上传...");
//
//                }
//
//                @Override
//                public void onSuccess(String arg0) {
//                    super.onSuccess(arg0);
//
//                    Log.i("ck", "success>" + arg0);
//
//                    if (arg0.equals("success")) {
//                        System.out.println(  "上传成功！"  );
////                        Toast.makeText(MainActivity.this, "上传成功！", 1000).show();
//                    }
//
////                    uploadInfo.setText(arg0);
//                }
//
//                @Override
//                public void onFailure(Throwable arg0, String arg1) {
//                    super.onFailure(arg0, arg1);
//
////                    uploadInfo.setText("上传失败！");
//                    System.out.println(  "上传失败！"  );
//                }
//            });
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            System.out.println("上传文件不存在！");
////            Toast.makeText(MainActivity.this, "上传文件不存在！", 1000).show();
//        }
//    }
//
//
//    @Override
//    protected String doInBackground(String... params) {
//        String filePath = params[0];
//        String uploadUrl = params[1];
//        String end = "\r\n";
//        String twoHyphens = "--";
//        String boundary = "******";
//
//        try {
//            URL url = new URL(uploadUrl);
//            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
//            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setUseCaches(false);
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setConnectTimeout(6000);
//            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
//            httpURLConnection.setRequestProperty("Charset", "UTF-8");
//            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
//            dos.writeBytes(twoHyphens + boundary + end);
//            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath.substring(filePath.lastIndexOf("/") + 1) + "\"" + end);
//            dos.writeBytes(end);
//            FileInputStream fis = new FileInputStream(filePath);
//            long total = (long)fis.available();
//            String totalstr = String.valueOf(total);
//            Log.d("文件大小", totalstr);
//            byte[] buffer = new byte[8192];
////            int countx = false;
//            int length = 0;
//
//            int count;
//            while((count = fis.read(buffer)) != -1) {
//                dos.write(buffer, 0, count);
//                length += count;
//                this.publishProgress(new Integer[]{(int)((float)length / (float)total * 100.0F)});
//            }
//
//            fis.close();
//            dos.writeBytes(end);
//            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
//            dos.flush();
//            InputStream is = httpURLConnection.getInputStream();
//            InputStreamReader isr = new InputStreamReader(is, "utf-8");
//            BufferedReader br = new BufferedReader(isr);
//            String result = br.readLine();
//            dos.close();
//            is.close();
//            return "上传成功";
//        } catch (Exception var21) {
//            var21.printStackTrace();
//            return "上传失败";
//        }
//    }
//
//
////    public static void main( String [] args) {
////    	String path = "/Users/alf/Downloads/1.jpg";
////    	File file = new File(path); //这里的path就是那个地址的全局变量
////    	String RequestURL  = "http://localhost:8080/second/UploadShipServlet";
////
////    	String result = UploadUtil.uploadFile(file, RequestURL);
////    	System.out.print("得到的回应是：" +result );
////    }
//
//
//
//
//}
