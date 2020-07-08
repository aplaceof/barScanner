package camera;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
//http://192.168.0.100:8080/firstWeb_war_exploded/demo

@WebServlet("/UploadShipServlet")
public class UploadShipServlet extends HttpServlet {
	private static final long serialVersionUID = 3L;
	private String path;

	public UploadShipServlet() {
		super();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("接收到get 请求");  
		response.getWriter().append("Served at: ").append(request.getContextPath());
//		this.doPost(request, response);
	}
//	protected void doPost2(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//		System.out.println("接收到get 请求");
//		response.getWriter().append("Served at: ").append(request.getContextPath());
//	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println("接收到post 请求");
		System.out.println("接收到post 请求");
		System.out.println("接收到post 请求");
		System.out.println("接收到post 请求");
		System.out.println("接收到post 请求");
		System.out.println("接收到post 请求");
		System.out.println("接收到post 请求");

		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");


		Enumeration<?> enum1 = request.getHeaderNames();
		while (enum1.hasMoreElements()) {
			String key = (String) enum1.nextElement();
			if( key.equals("picname")) {
				String value = request.getHeader(key);
				System.out.println(key + "\t" + value);
			}

		}
//		PrintWriter out = response.getWriter();
		
		// 创建文件项目工厂对象
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// 设置文件上传路径
		String upload = "/Users/alf/Downloads/";

		//		// 获取系统默认的临时文件保存路径，该路径为Tomcat根目录下的temp文件夹
		String temp = System.getProperty("java.io.tmpdir");
		// 设置缓冲区大小为 1M
		factory.setSizeThreshold(1024 * 1024);
		// 设置临时文件夹为temp
		factory.setRepository(new File(temp));
		// 用工厂实例化上传组件,ServletFileUpload 用来解析文件上传请求
		ServletFileUpload servletFileUpload  = new ServletFileUpload(factory);

//		path = upload+"/"+item.getName();

//		System.out.println("包含 IMG 字符");
		// 解析结果放在List中
		try {
			List<FileItem> list = servletFileUpload.parseRequest(request);

			for (FileItem item : list) {
				String name = item.getFieldName();
				InputStream is = item.getInputStream();

				if (name.contains("content")) {
					System.out.println(inputStream2String(is));
				} else if (name.contains("image")) {
					System.out.println("包含 IMG 字符");
					try {
						path = upload+"/"+item.getName();
						System.out.println("文件保存路径" + path  );
						inputStream2File(is, path);
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println( "文件是 文件的名字是"+  path );
//			out.write(path);  //这里我把服务端成功后，返回给客户端的是上传成功后路径
		} catch (FileUploadException e) {
			e.printStackTrace();  
			System.out.println("failure");     
//			out.write("failure");          
		}

//		out.flush();
//		out.close();
	}

	// 流转化成字符串
	public static String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);  
		}
		return baos.toString();  
	}

	// 流转化成文件
	public static void inputStream2File(InputStream is, String savePath) throws Exception {
		System.out.println("文件保存路径为:" + savePath);
		File file = new File(savePath);
		InputStream inputSteam = is;
		BufferedInputStream fis = new BufferedInputStream(inputSteam);
		FileOutputStream fos = new FileOutputStream(file);
		int f;
		while ((f = fis.read()) != -1) {
			fos.write(f);
		}
		fos.flush();
		fos.close();
		fis.close();
		inputSteam.close();

	}
	
	
}