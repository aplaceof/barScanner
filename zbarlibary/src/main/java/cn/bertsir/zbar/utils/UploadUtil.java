package cn.bertsir.zbar.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * 上传工具类
 * @author spring sky
 * Email:vipa1888@163.com
 * QQ:840950105
 * MyName:石明政
 */
public class UploadUtil extends AsyncTask<String, Integer, String> {
    public UploadUtil() {
    }
//    private static final String TAG = "uploadFile";
//    private static final int TIME_OUT = 10*1000;   //超时时间
//    private static final String CHARSET = "utf-8"; //设置编码
    /**
     * android上传文件到服务器
     * @param file  需要上传的文件
     * @param RequestURL  请求的rul
     * @return  返回响应的内容
     */




    protected void onPostExecute(String result) {
//        MainActivity.this.mTvProgress.setText(result);
    }

    protected void onPreExecute() {
//        MainActivity.this.mTvProgress.setText("loading...");
    }

    protected void onProgressUpdate(Integer... values) {
//        MainActivity.this.mPgBar.setProgress(values[0]);
//        MainActivity.this.mTvProgress.setText("loading..." + values[0] + "%");
    }

    protected String doInBackground(String... params) {
        String filePath = params[0];
        String uploadUrl = params[1];
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";

        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(6000);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath.substring(filePath.lastIndexOf("/") + 1) + "\"" + end);
            dos.writeBytes(end);
            FileInputStream fis = new FileInputStream(filePath);
            long total = (long)fis.available();
            String totalstr = String.valueOf(total);
            Log.d("文件大小", totalstr);
            byte[] buffer = new byte[8192];
//            int countx = false;
            int length = 0;

            int count;
            while((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
                length += count;
                this.publishProgress(new Integer[]{(int)((float)length / (float)total * 100.0F)});
            }

            fis.close();
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            dos.close();
            is.close();
            return "上传成功";
        } catch (Exception var21) {
            var21.printStackTrace();
            return "上传失败";
        }
    }


//    public static void main( String [] args) {
//    	String path = "/Users/alf/Downloads/1.jpg";
//    	File file = new File(path); //这里的path就是那个地址的全局变量
//    	String RequestURL  = "http://localhost:8080/second/UploadShipServlet";
//
//    	String result = UploadUtil.uploadFile(file, RequestURL);
//    	System.out.print("得到的回应是：" +result );
//    }




}
