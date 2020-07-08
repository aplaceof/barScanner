package camera;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.Socket;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.security.x509.IPAddressName;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


@WebServlet(name = "MyServlet")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;
    private String path;
    String todayDataDir = "";
    private static int defaultBufferSize = 1024* 1024*15;

    String configDir = "";
    String savePathFileName =  "save_path_config.txt";
    String cameraServerFileName =  "camera_server_config.txt";
    String fileServerFileName =  "file_server_config.txt";

    String savePath = "";
    String cemeraServerURL = "";
    String servletServerURL = "";

    String cameraServerIPAddress = "";
    String cameraServerport = "";
    String servletServerIPAddress = "";
    String servletServerport = "";
    String pathRouter = "/../../../config/";


    public MyServlet() {
        super();
        String temp = System.getProperty("user.dir");
        configDir = temp + pathRouter;
        System.out.println( "设置文件所在的路径是" + configDir );

        savePath = getSavePath(configDir + "/" +  savePathFileName );
        System.out.println( " 保存的路径是" + savePath );

        cemeraServerURL = getCameraServerURL(configDir + "/" +   cameraServerFileName );
        cameraServerIPAddress = cemeraServerURL.split(":")[0];
        cameraServerport = cemeraServerURL.split(":")[1];

        servletServerURL = getServletURL( configDir + "/" +   fileServerFileName );
        servletServerIPAddress = servletServerURL.split(":")[0];
        servletServerport = servletServerURL.split(":")[1];

//        Login mylogin=new Login();
//        mylogin.InitUI();

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("接收到get 请求");

        response.setHeader("content-type", "text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().append("Served at: ").append(request.getContextPath());
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
//        out.println("<h1>" + "  快递服务器 " + "</h1>");
//        out.println("<h1>" + "  快递服务器 " + "</h1>");

//		this.doPost(request, response);
    }
    protected void doPost2(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        System.out.println("接收到get 请求");
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("接收到post 请求");

        response.getWriter().append("Served at: ").append(request.getContextPath());
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");



        String temp_fileName  =  request.getHeader("picname");
//        System.out.println(" 文件名字是" + temp_fileName );

        new Thread(new Runnable() {
            @Override
            public void run() {
                TcpConnect( cameraServerIPAddress, cameraServerport, temp_fileName );
            }
        }).start();

		PrintWriter out = response.getWriter();

        // 创建文件项目工厂对象
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // 设置文件上传路径
//        String dataSave_dir = "/Users/alf/Downloads/kuaidi/";

        // 获取系统默认的临时文件保存路径，该路径为Tomcat根目录下的temp文件夹
        String temp = System.getProperty("java.io.tmpdir");
        // 设置缓冲区大小为 1M
        factory.setSizeThreshold(1024 * 1024 *50 );
        // 设置临时文件夹为temp
        factory.setRepository(new File(temp));
        factory.setSizeThreshold( 1024 * 1024 *10 );
        // 用工厂实例化上传组件,ServletFileUpload 用来解析文件上传请求
        ServletFileUpload servletFileUpload  = new ServletFileUpload(factory);

        String fileName_bk  = "";
        // 解析结果放在List中
        try {
            List<FileItem> list = servletFileUpload.parseRequest(request);

            for (FileItem item : list) {
                String name = item.getFieldName();
                InputStream is = item.getInputStream();
//                System.out.println("item的名字是   " + item);
//                System.out.println("item   的field name 是:   " + name );

                if (name.contains("content")) {
                    System.out.println(inputStream2String(is));
                } else if (name.contains("image")) {
//                    System.out.println("包含 IMG 字符");
                    try {
//                        path = dataSave_dir+"/"+item.getName() ;
                        final String fileName   = item.getName();
                        fileName_bk = fileName;

//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                TcpConnect( cameraServerIPAddress, cameraServerport, temp_fileName );
//                            }
//                        }).start();

                        new Thread( new Runnable(){
                            @Override
                            public void run() {
                                try {
                                    inputStream2FileBase64(is, savePath,  fileName);    // 使用base 64 是成功的
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        ).start();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println( " 数据流 接收完毕， 文件名字是"+  temp_fileName );
			out.write( "success");  //这里我把服务端成功后，返回给客户端的是上传成功后路径
        } catch (FileUploadException e) {
            e.printStackTrace();
            System.out.println("failure");
//			out.write("failure");
        }

//		out.flush();
//		out.close();
    }

    // 流转化成字符串
    public  String inputStream2String(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = -1;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString();
    }

    public byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while((len = inStream.read(buffer)) != -1){
            outStream.write(buffer, 0, len);
        }
        byte [] res = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return res;
    }
    public  String getSavePath( String configFilePath ) {
        File configFile = new File( configFilePath );
        FileReader fr = null;
        String configStr = "";
        try {
            fr = new FileReader(configFile);
            BufferedReader  bis = new BufferedReader( fr );

            configStr =  bis.readLine();
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String config[] = configStr.split("#");
        String dataDir = config[1];
        return dataDir;
    }

    public  String getCameraServerURL( String configFilePath ) {
        File configFile = new File( configFilePath );
        FileReader fr = null;
        String URLStr = "";
        try {
            fr = new FileReader(configFile);
            BufferedReader  bis = new BufferedReader( fr );

            URLStr =  bis.readLine();
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String config[] = URLStr.split("#");
        String URL = config[1];
        return URL;
    }
    public  String getServletURL( String configFilePath ) {
        File configFile = new File( configFilePath );
        FileReader fr = null;
        String URLStr = "";
        try {
            fr = new FileReader(configFile);
            BufferedReader  bis = new BufferedReader( fr );

            URLStr =  bis.readLine();
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String config[] = URLStr.split("#");
        String URL = config[1];
        return URL;
    }

    private String  getDate() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = formatter.format(date);
        return dateStr;
    }

    private void TcpConnect( String IPAdress, String port,  String fileName  ) {
        Socket s= null;
        try {
            s = new Socket(IPAdress, Integer.parseInt(port));

            BufferedWriter out=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            out.write(fileName);
            out.flush();
            s.shutdownOutput();
            BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));

            String str=in.readLine();
            System.out.println("从camera server 返回的内容是" + str);
            s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public String creatDateDir( String dataDir ) {

        String todayDataDir = dataDir + "/" + this.getDate();

        File file = new File(todayDataDir);
        file.mkdir();
        if (file.exists()) {

            if (file.isDirectory()) {

            } else {    // 如果是文件， 不是文件夹
                System.out.println("创建文件夹");
                file.mkdir();
            }
        } else {
//            System.out.println("dir not exists, create it ...");
            System.out.println("创建文件夹");
            file.mkdir();
        }
        return todayDataDir;
    }
    // 流转化成文件
    public void inputStream2FileBase64(InputStream inputSteam, String dataDir, String fileName ) throws Exception {

        String todayDate = getDate();
//        System.out.println(" 分析   " +  todayDate + "    " + todayDataDir);
        if ( ! todayDataDir.contains(todayDate) ) { // 如果日期对不上

            todayDataDir = creatDateDir( dataDir ); // 创建文件夹  并返回今天保存文件的路径
        }

        String picSavePath  = todayDataDir + "/" + "快递_" + fileName;
        File picFile = new File( picSavePath );
        FileOutputStream fos = new FileOutputStream(picFile);

        byte picData[]  = readStream( inputSteam );             // 读取http 数据流 ， 进行解码
//        System.out.println( "传入文件长度" + picData.length );
        String picStr = new String( picData );
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] decoderBytes = decoder.decodeBuffer(picStr);

        long start = System.currentTimeMillis();

        fos.write( decoderBytes );
        fos.flush();
        fos.close();

        long end = System.currentTimeMillis();
        long delta = end - start;
//        System.out.println("写文件的执行时间是" + delta);
    }

}
