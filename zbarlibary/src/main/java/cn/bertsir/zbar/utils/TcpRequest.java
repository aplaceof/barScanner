package cn.bertsir.zbar.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;


//发送端的代码如下：
public class TcpRequest extends Thread {
    Socket s;
    String remote_server;

    public TcpRequest(){
        remote_server = "192.168.0.101";
//        try {
//            s=  new Socket(remote_server,10010);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    public TcpRequest(String remote_server  ){
        remote_server = "192.168.0.101";
    }
    public void init( ) {
        remote_server = "192.168.0.101";
        try {
            s=  new Socket(remote_server,10010);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//     new Thread(new TT()).start();  //开启发送端的线程

    public OutputStream getTcpStream( ) {
        try {
            return s.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void run( ) {
        try {
//            remote_server = "192.168.0.101";
//            s=  new Socket(remote_server,10010);

//创建一个socket绑定的端口和地址为：10001，服务器。
            OutputStream outS = s.getOutputStream();
//获取到输出流
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outS));

            String line= "askdfjlkadsjfjsdalfjlkasdjflkjasldjf";
            bw.write(line);


        bw.flush();
//            while((line=br.readLine())!=null){
//                bw.write(line);
//                bw.flush();
////将内容写到控制台
//            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

}