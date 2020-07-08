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
    	window = new JFrame("����ͷ");

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
        comboBox_camera.setModel(new DefaultComboBoxModel(camNameArray));       //  ���ѡ��ֵ
        JLabel lb_camera = new JLabel("����ͷѡ��");
	    seclectPanel.add( lb_camera );
	    seclectPanel.add(  comboBox_camera  );
	    bt_fps = new JButton("�����趨֡��");
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
            public void windowClosing(WindowEvent e)//�ر�ʱ�Ĳ�����windowClosed�ǹرպ�Ĳ���
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
        
        System.out.println( " ͼƬ�����·����  " + savePath );  
        System.out.println( " URL��ַ��  " + cemeraServerURL );  
        
        
        IPAddress = cemeraServerURL.split(":")[0];
        port = cemeraServerURL.split(":")[1];
        
        init();
        creatSavePath();    // ȷ���������  �޷�д���ļ��Ĵ��� 
    }
    private void initView() {
    	
    }
    
    public void creatSavePath( ) {
        String todayDate = getDate();
//        System.out.println(" ����   " +  todayDate + "    " + todayDataDir);
        if ( ! todayDataDir.contains(todayDate) ) { // ������ڶԲ���

            todayDataDir = creatDateDir( savePath ); // �����ļ���  �����ؽ��챣���ļ���·��
        }
    }
    public void addListener() {
    	comboBox_camera.addActionListener(new ActionListener() {  // �����������Ӧ����
			@Override 
			public void actionPerformed(ActionEvent e) {
//				System.out.println("�����");
				int cameraID = comboBox_camera.getSelectedIndex();
				
				webcam.close();
				
//				Webcam temp =  camList.get(cameraID);
				webcam = camList.get(cameraID);
//				System.out.println( webcam.getName() );
				webcam.setViewSize(WebcamResolution.VGA.getSize());
				webcam.open();  
				
				WebcamPanel newCamPanel = new WebcamPanel(webcam); 
//				System.out.println( " �Ƿ���� " + newCamPanel.getWebcam().getName() );
//				System.out.println( "shi fou da kai   " + webcam.isOpen() );
				
				newCamPanel.setFPSLimited(true);
				newCamPanel.setFPSLimit( cameraFPS );
//				newCamPanel.setFPSDisplayed(true);
//				newCamPanel.setDisplayDebugInfo(true);
				newCamPanel.setImageSizeDisplayed(true);
				newCamPanel.setMirrored(true);
				
				window.getContentPane().remove(camPanel);
				window.add(newCamPanel, BorderLayout.CENTER);
				
				camPanel = newCamPanel;   // �������
				window.getContentPane().validate ();
				window.getContentPane().repaint();
		        window.pack();

			}
		});
    	
    	
    	bt_fps.addActionListener(new ActionListener() {   //  �趨֡������Ӧ����
	    	@Override 
			public void actionPerformed(ActionEvent e) {
	    		double newFPS = Double.parseDouble(   tf_fps.getText( ) );
	    		cameraFPS = newFPS;
	    		
	    		WebcamPanel newCamPanel = new WebcamPanel(webcam); 
//				System.out.println( " �Ƿ���� " + newCamPanel.getWebcam().getName() );
//				System.out.println( "shi fou da kai   " + webcam.isOpen() );
				
				newCamPanel.setFPSLimited(true);
				newCamPanel.setFPSLimit( cameraFPS );
				newCamPanel.setImageSizeDisplayed(true);
				newCamPanel.setMirrored(true);
				
				window.getContentPane().remove(camPanel);
				window.add(newCamPanel, BorderLayout.CENTER);
				
				camPanel = newCamPanel;   // �������
				window.getContentPane().validate ();
				window.getContentPane().repaint();
		        window.pack();
	    		
	    	}
	    });
    }


    

public  String getSavePath( String configFilePath ) {   // �������ͼ��Ĵ���·��
	
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

        } else {    // ������ļ��� �����ļ���
        	System.out.println("�����ļ���");
            file.mkdir();
        }
    } else {
//        System.out.println("dir not exists, create it ...");
    	System.out.println("�����ļ���");
        file.mkdir();
    }
    return todayDataDir;
}



}



class TcpAsyncServer {

    /*�����˿�*/
    int port = 10012;
    /*��������С*/
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    /*������ض���*/
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
    /*����*/
    public void Start() throws Exception {
        /*��ʼ��һ��Selector*/
        selector = Selector.open();
        /*��ͨ��*/
        channel = ServerSocketChannel.open();
        /*������ģʽ*/
        channel.configureBlocking(false);
        /*����IP*/
        //InetAddress ip = InetAddress.getByName("127.0.0.1");
        InetAddress ip = InetAddress.getLocalHost();
        System.out.println("������ip��ַ��" +ip.toString());
        /*��IP�Ͷ˿�*/
        InetSocketAddress address = new InetSocketAddress(ip, port);
        socket = channel.socket();
        socket.bind(address);
        /*��������*/
        System.out.println("TCP��������ʼ����...");
        Listen();
    }

    /*ֹͣ*/
    public void Stop() throws Exception {
        channel.close();
        selector.close();
    }

    /*����*/
    public void Listen() throws Exception {
        /*ע������¼�*/
        channel.register(selector,SelectionKey.OP_ACCEPT);
        /*����ѭ��*/
        while (true) {
            selector.select();
            /*��ѯ�¼�*/
            Iterator iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key =  (SelectionKey)iter.next();
                iter.remove();
                /*�¼����ദ��*/
                if (key.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    System.out.println("���ն�������:"+ sc.getRemoteAddress());
                }
                else if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel)key.channel();
                    int recvCount = sc.read(buffer);
                    if (recvCount > 0) {
                        byte[] arr = buffer.array();
                        String recieveData =  new String(arr);
                        //6923878310115.jpg
//                        System.out.println(sc.getRemoteAddress() + "��������: "+  recieveData );

                        String fileName = recieveData.split("\\.")[0];                  
                        
                        // �������ճ���
                        //String filePath = "C:\\Users\\alf\\Pictures\\test\\" + "����_"  + fileName ;
                        
                        String filePath = cs.todayDataDir +"/" +  "����_"  + fileName ;
                        System.out.println("�ļ�����λ����" + filePath );

//                        System.out.println( "д��λ��: "+  filePath    );
                        
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
