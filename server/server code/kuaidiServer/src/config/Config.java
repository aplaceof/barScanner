package config;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Config {

	private JFrame frame;
	private JTextField tf_file_server_port;
	
	JLabel lb_IP  ;
	JLabel lb_file_server_config ;
	JLabel lb_file_server_port ;
	JLabel lb_camera_server_port  ;
	JTextField tf_savepath  ;
	JButton bt_confirm  ;
	JButton bt_cancel  ;
	JTextField tf_camera_server_port ;
	JButton bt_choose_dir ;
	JLabel lb_save_path;
	JLabel lb_camera_server_config; 
	JLabel lb_IPAddress;
	
	
    String savePathFileName = "save_path_config.txt";
    String cameraServerFileName =  "camera_server_config.txt";
    String fileServerFileName =  "file_server_config.txt";
	String defaultPath  = "" ;
	String currentDir = "";  
    String savePath = "";
    
	String fileServerPort ;  
	String cameraServerPort ;  
	String old_fileServerPort ;  
	String IPAddress;
    String cemeraServerURL = "";
    String fileServerURL = "";
    String  tomcatConfigFilePath  = "";


	/**
	 * Launch the application.
	 */
	
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Config window = new Config();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void loadConfig() {
		
		 // 读取文件， 加载配置
    	String savePathFilePath = currentDir + "/config/" +  savePathFileName;   // 这个东西初始化的时候没用
    	savePath = getSavePath(savePathFilePath );
    	
    	String cameraServerFilePath = currentDir+ "/config/"  + cameraServerFileName;
        cemeraServerURL = getServerURL(cameraServerFilePath );
        String temp_IPAddress = cemeraServerURL.split(":")[0];
        cameraServerPort = cemeraServerURL.split(":")[1]; 
        
        String fileServerFilePath = currentDir + "/config/" + fileServerFileName;
        fileServerURL = getServerURL(  fileServerFilePath );
        fileServerPort = fileServerURL.split(":")[1]; 
        old_fileServerPort = fileServerPort;     // 为了寻找替换  tomcat server.xml中的端口号， 先把原来的端口号记录下来
        
        // 将设置显示在界面上
        tf_camera_server_port.setText( cameraServerPort  );
        tf_file_server_port.setText( fileServerPort  );
        tf_savepath.setText(savePath); 
        
        defaultPath = savePath;
        
        System.out.println( "图片保存的路径是  " + defaultPath );  
        System.out.println( "file server URL地址是  " + fileServerURL );  
        System.out.println( "camera server  url 地址是  " + cemeraServerURL );  
        
		
	}
	
	private void initConfigDir( ) {
		currentDir= System.getProperty("user.dir")  ;
//		defaultPath = currentDir;
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

	public  String getServerURL( String configFilePath ) {  // 读取服务器URL 地址
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
	
	public String chooseSavePath() {

		JFileChooser fileChooser = new JFileChooser( defaultPath );
		tf_savepath.setText( defaultPath  );   
		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		 String filePath = "";
		 int returnVal = fileChooser.showOpenDialog(fileChooser);
		 if(returnVal == JFileChooser.APPROVE_OPTION){ 
		     filePath= fileChooser.getSelectedFile().getAbsolutePath();//这个就是你选择的文件夹的路径       
      }  
		 return filePath;
	}

	/**
	 * Create the application.
	 */
	public Config() {
		initConfigDir();  
		IPAddress  =  getIpAddress();  
		
		System.out.println("ip地址是： " +  IPAddress );
		System.out.println("当前路径是： " +  currentDir );
		
		initialize();   // 初始化界面
		 // 初始化配置文件夹  
		
		loadConfig();    // 载入设置
		
//		initConfigDir();
		addListener();


	}
	
	/**   
	 * Initialize the contents of the frame.   
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		lb_IP = new JLabel("IP 地址:");
		lb_IP.setBounds(48, 10, 54, 15);
		frame.getContentPane().add(lb_IP);
		
		 lb_file_server_config = new JLabel("文件服务器设置:");
		lb_file_server_config.setBounds(10, 35, 143, 15);
		frame.getContentPane().add(lb_file_server_config);
		
		 lb_file_server_port = new JLabel("端口号:");
		lb_file_server_port.setBounds(48, 59, 59, 15);
		frame.getContentPane().add(lb_file_server_port);
		
		 lb_camera_server_config = new JLabel("照相服务器设置");
		lb_camera_server_config.setBounds(10, 84, 125, 15);
		frame.getContentPane().add(lb_camera_server_config);
		
		 lb_camera_server_port = new JLabel("端口号");
		lb_camera_server_port.setHorizontalAlignment(SwingConstants.LEFT);
		lb_camera_server_port.setBounds(50, 105, 71, 15);
		frame.getContentPane().add(lb_camera_server_port);
		
		lb_save_path = new JLabel("文件保存路径");
		lb_save_path.setHorizontalAlignment(SwingConstants.LEFT);
		lb_save_path.setBounds(10, 141, 92, 15);
		frame.getContentPane().add(lb_save_path);
		
		tf_savepath = new JTextField();
		tf_savepath.setBounds(113, 138, 174, 21);
		frame.getContentPane().add(tf_savepath);
		tf_savepath.setColumns(10);
		tf_savepath.setText( defaultPath );
		
		
		bt_confirm = new JButton("确认");
		bt_confirm.setBounds(42, 202, 93, 23);
		frame.getContentPane().add(bt_confirm);
		
		bt_cancel = new JButton("退出");
		bt_cancel.setBounds(212, 202, 93, 23);
		frame.getContentPane().add(bt_cancel);
		
		JLabel lb_IPAddress = new JLabel("192.168.0.100");
		lb_IPAddress.setText(  this.IPAddress );
		lb_IPAddress.setFont(new Font("宋体", Font.PLAIN, 15));
		lb_IPAddress.setBounds(137, 10, 150, 15);
		frame.getContentPane().add(lb_IPAddress);
		
		tf_camera_server_port = new JTextField();
		tf_camera_server_port.setBounds(147, 102, 66, 21);
		frame.getContentPane().add(tf_camera_server_port);
		tf_camera_server_port.setColumns(10);
		
		tf_file_server_port = new JTextField();
		tf_file_server_port.setColumns(10);
		tf_file_server_port.setBounds(147, 56, 66, 21);
		frame.getContentPane().add(tf_file_server_port);
		
		bt_choose_dir = new JButton("选择");
		bt_choose_dir.setBounds(297, 137, 93, 23);
		frame.getContentPane().add(bt_choose_dir);
	}
	
	

    private boolean isMate( String originStr, String lineStr) {
    	String newLine = lineStr.trim();
    	
    	if(  newLine.startsWith("<Connector port=")  && newLine.endsWith( "protocol=\"HTTP/1.1\"")  ) {
    			return true;
    	}
    	return false;
    }
    private void replaceText(String  filePath,String origin ,String target) {//修改
        BufferedReader br=null;
        PrintWriter pw=null;
        StringBuffer buff=new StringBuffer();//临时容器!zhi
        File file=new File(filePath);//文件 
        String lineEnd=System.getProperty("line.separator");//平台换行!
        
        try {
            br = new BufferedReader(new FileReader(file));
            for(String lineStr=br.readLine(); lineStr!=null; lineStr=br.readLine()) {
                if( isMate( origin, lineStr )   ) {     // 如果条件匹配， 那么进行替换
//                	lineStr = lineStr.replace(origin,  target  );
            		String patt = "\"[0-9]*\"";
            		Pattern r = Pattern.compile(patt);
            		Matcher m = r.matcher(lineStr);
            		String newStr = m.replaceAll("\"" + target + "\"");
            		lineStr = newStr;
                }
                buff.append(  lineStr+lineEnd  );
            }
            
            pw=new PrintWriter(new FileWriter(file),true);
            pw.println(buff);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(br!=null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(pw!=null) 
            	pw.flush();
                pw.close();
        }
    }
	public void addListener( ) {
		bt_choose_dir.addActionListener(new ActionListener() {
			@Override 
			public void actionPerformed(ActionEvent e) {
				savePath = chooseSavePath();  
				if ( savePath.equals( "") ) {
					
				} else {
					tf_savepath.setText(   savePath );   
					defaultPath = savePath;
				}
				
			}
			
		});
		
		
		bt_confirm.addActionListener(new ActionListener() {
			@Override 
			public void actionPerformed(ActionEvent e) {
				fileServerPort = tf_file_server_port.getText();  
				cameraServerPort = tf_camera_server_port.getText();  
				String fileServerConfig = "fileServer#" + IPAddress + ":" + fileServerPort;
				String cameraServerConfig = "cameraServe#" + IPAddress + ":" + cameraServerPort;
				
				String picSaveConfig = "picSavePath#" + tf_savepath.getText();
						
				writeConfig(fileServerConfig,   currentDir + "/config/" + fileServerFileName );
				writeConfig(cameraServerConfig,   currentDir + "/config/" + cameraServerFileName );
				writeConfig(picSaveConfig,   currentDir + "/config/" + savePathFileName );
				// 替换 tomcat的端口号配置
//				cameraServerPort
				tomcatConfigFilePath = currentDir + "/tomcat/tomcat/conf/" + "server.xml" ;
				replaceText(tomcatConfigFilePath, new String( old_fileServerPort),   new String ( fileServerPort )  );
				old_fileServerPort = fileServerPort;
			}
			
		});
		
		bt_cancel.addActionListener(new ActionListener() {
			@Override 
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
			
		});
		
	}
	

	
	private static String getIpAddress() {
		      Set<String> ipList = new HashSet<>();
		      try {
		         Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		         InetAddress ip = null;
		         while (allNetInterfaces.hasMoreElements()) {
		            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
		            //排除虚拟接口和没有启动运行的接口
		            if (netInterface.isVirtual() || !netInterface.isUp()) {
		               continue;
		            } else {
		               Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
		               while (addresses.hasMoreElements()) {
		                  ip = addresses.nextElement();
		                  if (ip != null && (ip instanceof Inet4Address || ip instanceof Inet6Address)) {
		                     ipList.add(ip.getHostAddress());
		                  }
		               }
		            }
		         }
		      } catch (Exception e) {
		         e.printStackTrace();
		      }
		    
		    String res = "";
			for (String str : ipList) {
				if ( str.startsWith( "192") ) {
					res = str;
				}
			}
		      return res;
		   }

	public void writeConfig( String configStr, String configFilePath ) {
		File configFile = new File( configFilePath );
	    FileWriter fw = null;
	    try {
	        fw = new FileWriter(configFile);
	        BufferedWriter  bw = new BufferedWriter( fw );
	        bw.write( configStr   );
	        bw.newLine();
	        bw.flush();
	        bw.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
