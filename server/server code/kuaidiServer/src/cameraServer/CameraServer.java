package cameraServer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;

public class CameraServer {

    private static int    num    = 0;
    String todayDataDir = "";
    
    //String configFilePath = "C://Users/alf/Pictures/test/" + "save_path_config.txt";
//    String configDir = "C://Users/alf/Pictures/test/";
    String savePathFileName = "save_path_config.txt";
    String cameraServerFileName =  "camera_server_config.txt";
    String savePath = "";
    String cemeraServerURL = "";
    String IPAddress = "";
    String port = "";
    String currentDir = "";
    
    
    JComboBox comboBox_camera;
    JFrame window;
    Webcam webcam ;
    List<Webcam> camList;
    WebcamPanel camPanel ;
    private  double cameraFPS = 2;
    JTextField tf_fps ;
    JButton bt_fps;
    
    
    

    public static void main(String[] args) throws IOException
    {
    	
    	

    	CameraServer  cs = null;
		cs = new  CameraServer();
    	cs.window.setVisible(true);
    	cs.serverStart(cs.webcam, cs);        
    	
    }
    public void serverStart( Webcam cam, CameraServer cs) {
        TcpAsyncServer tcpServer = new TcpAsyncServer( cam,  cs );
        try {
			tcpServer.Start();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    

    public void init() {
    	
        webcam = Webcam.getDefault();
    	window = new JFrame("摄像头");

		if (webcam != null) {
			System.out.println("Webcam: " + webcam.getName());
		} else {
			System.out.println("No webcam detected");
		}
		

    	camList = Webcam.getWebcams();
    	String camNameArray[] = new String[ camList.size() ];
    	for ( int i = 0; i< camList.size(); i++ ) {
    		Webcam temp_cam  = camList.get(i);
    		String temp_cam_name = temp_cam.getName();
    		camNameArray[i] = temp_cam_name;
    	}
    	

    	GridLayout layout = new GridLayout(2, 2) ; 
        JPanel seclectPanel = new JPanel(layout);
        
		comboBox_camera = new JComboBox();
        comboBox_camera.setModel(new DefaultComboBoxModel(camNameArray));       //  添加选项值
        JLabel lb_camera = new JLabel("摄像头选择：");
	    seclectPanel.add( lb_camera );
	    seclectPanel.add(  comboBox_camera  );
	    bt_fps = new JButton("重新设定帧数");
	    tf_fps = new JTextField("2");                   
	    
	    seclectPanel.add( bt_fps );
	    seclectPanel.add( tf_fps );
	    
	    
	    
	    
	    
	    window.add(seclectPanel, BorderLayout.NORTH );  
	    
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        //Webcam.setDriver(new NativeWebcamDriver());
        
        camPanel = new WebcamPanel(webcam);
        camPanel.setFPSLimited(true);
        camPanel.setFPSLimit(cameraFPS);
//        camPanel.setFPSDisplayed(true);
//        camPanel.setDisplayDebugInfo(true)
        camPanel.setImageSizeDisplayed(true);
        camPanel.setMirrored(true);
        
	    
        window.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e)//关闭时的操作，windowClosed是关闭后的操作
            {
                webcam.close();
                window.dispose();
                System.exit(0);
            }
        });   
        
        window.add(camPanel, BorderLayout.CENTER);
        
        window.setResizable(true);
        
        window.pack();
        //window.setVisible(true);
        addListener();

    
    }
    
    
    public CameraServer() {   
    	
        currentDir = System.getProperty("user.dir")  ;
    	
    	String savePathFilePath = currentDir + "/config/" + savePathFileName;
    	savePath = getSavePath(savePathFilePath );
    	String cameraServerFilePath = currentDir + "/config/" + cameraServerFileName;
        cemeraServerURL = getCameraServerURL(cameraServerFilePath );
        
        System.out.println( " 图片保存的路径是  " + savePath );  
        System.out.println( " URL地址是  " + cemeraServerURL );  
        
        
        IPAddress = cemeraServerURL.split(":")[0];
        port = cemeraServerURL.split(":")[1];
        
        init();
        creatSavePath();    // 确保不会出现  无法写入文件的错误 
    }
    private void initView() {
    	
    }
    
    public void creatSavePath( ) {
        String todayDate = getDate();
//        System.out.println(" 分析   " +  todayDate + "    " + todayDataDir);
        if ( ! todayDataDir.contains(todayDate) ) { // 如果日期对不上

            todayDataDir = creatDateDir( savePath ); // 创建文件夹  并返回今天保存文件的路径
        }
    }
    public void addListener() {
    	comboBox_camera.addActionListener(new ActionListener() {  // 更换相机的相应函数
			@Override 
			public void actionPerformed(ActionEvent e) {
//				System.out.println("点击了");
				int cameraID = comboBox_camera.getSelectedIndex();
				
				webcam.close();
				
//				Webcam temp =  camList.get(cameraID);
				webcam = camList.get(cameraID);
//				System.out.println( webcam.getName() );
				webcam.setViewSize(WebcamResolution.VGA.getSize());
				webcam.open();  
				
				WebcamPanel newCamPanel = new WebcamPanel(webcam); 
//				System.out.println( " 是否更改 " + newCamPanel.getWebcam().getName() );
//				System.out.println( "shi fou da kai   " + webcam.isOpen() );
				
				newCamPanel.setFPSLimited(true);
				newCamPanel.setFPSLimit( cameraFPS );
//				newCamPanel.setFPSDisplayed(true);
//				newCamPanel.setDisplayDebugInfo(true);
				newCamPanel.setImageSizeDisplayed(true);
				newCamPanel.setMirrored(true);
				
				window.getContentPane().remove(camPanel);
				window.add(newCamPanel, BorderLayout.CENTER);
				
				camPanel = newCamPanel;   // 更新组件
				window.getContentPane().validate ();
				window.getContentPane().repaint();
		        window.pack();

			}
		});
    	
    	
    	bt_fps.addActionListener(new ActionListener() {   //  设定帧数的相应函数
	    	@Override 
			public void actionPerformed(ActionEvent e) {
	    		double newFPS = Double.parseDouble(   tf_fps.getText( ) );
	    		cameraFPS = newFPS;
	    		
	    		WebcamPanel newCamPanel = new WebcamPanel(webcam); 
//				System.out.println( " 是否更改 " + newCamPanel.getWebcam().getName() );
//				System.out.println( "shi fou da kai   " + webcam.isOpen() );
				
				newCamPanel.setFPSLimited(true);
				newCamPanel.setFPSLimit( cameraFPS );
				newCamPanel.setImageSizeDisplayed(true);
				newCamPanel.setMirrored(true);
				
				window.getContentPane().remove(camPanel);
				window.add(newCamPanel, BorderLayout.CENTER);
				
				camPanel = newCamPanel;   // 更新组件
				window.getContentPane().validate ();
				window.getContentPane().repaint();
		        window.pack();
	    		
	    	}
	    });
    }


    

public  String getSavePath( String configFilePath ) {   // 获得拍摄图像的储存路径
	
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


private String  getDate() {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    String dateStr = formatter.format(date);
    return dateStr;
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
//        System.out.println("dir not exists, create it ...");
    	System.out.println("创建文件夹");
        file.mkdir();
    }
    return todayDataDir;
}



}



class TcpAsyncServer {

    /*监听端口*/
    int port = 10012;
    /*缓冲区大小*/
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    /*其它相关定义*/
    Selector selector;
    ServerSocketChannel channel;
    ServerSocket socket;
    Webcam  webcam;
    
    CameraServer cs;
    
    public TcpAsyncServer ( Webcam  webcam,  CameraServer cs  ) {
    	this.cs = cs;
    	this.webcam = webcam;
    	this.port = Integer.parseInt( cs.port );
    }
    /*启动*/
    public void Start() throws Exception {
        /*初始化一个Selector*/
        selector = Selector.open();
        /*打开通道*/
        channel = ServerSocketChannel.open();
        /*非阻塞模式*/
        channel.configureBlocking(false);
        /*本机IP*/
        //InetAddress ip = InetAddress.getByName("127.0.0.1");
        InetAddress ip = InetAddress.getLocalHost();
        System.out.println("服务器ip地址：" +ip.toString());
        /*绑定IP和端口*/
        InetSocketAddress address = new InetSocketAddress(ip, port);
        socket = channel.socket();
        socket.bind(address);
        /*启动监听*/
        System.out.println("TCP服务器开始监听...");
        Listen();
    }

    /*停止*/
    public void Stop() throws Exception {
        channel.close();
        selector.close();
    }

    /*监听*/
    public void Listen() throws Exception {
        /*注册接收事件*/
        channel.register(selector,SelectionKey.OP_ACCEPT);
        /*无限循环*/
        while (true) {
            selector.select();
            /*轮询事件*/
            Iterator iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key =  (SelectionKey)iter.next();
                iter.remove();
                /*事件分类处理*/
                if (key.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    System.out.println("新终端已连接:"+ sc.getRemoteAddress());
                }
                else if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel)key.channel();
                    int recvCount = sc.read(buffer);
                    if (recvCount > 0) {
                        byte[] arr = buffer.array();
                        String recieveData =  new String(arr);
                        //6923878310115.jpg
//                        System.out.println(sc.getRemoteAddress() + "发来数据: "+  recieveData );

                        String fileName = recieveData.split("\\.")[0];                  
                        
                        // 调用拍照程序
                        //String filePath = "C:\\Users\\alf\\Pictures\\test\\" + "人像_"  + fileName ;
                        
                        String filePath = cs.todayDataDir +"/" +  "人像_"  + fileName ;
                        System.out.println("文件保存位置是" + filePath );

//                        System.out.println( "写回位置: "+  filePath    );
                        
//                        WebcamUtils.capture(webcam, filePath, ImageUtils.FORMAT_JPG);
                        new Thread( new Runnable() {
                        	public void run() {
                        		WebcamUtils.capture(webcam, filePath, ImageUtils.FORMAT_JPG);
                        	}
                        }).start();
     
                        buffer.flip();
                    }
                    else {
                        sc.close();
                    }
                    buffer.clear();
                }

                else {

                }
            }

        }

    }
}
