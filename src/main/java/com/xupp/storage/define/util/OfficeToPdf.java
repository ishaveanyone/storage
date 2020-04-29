package com.xupp.storage.define.util;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;


/**
 * @author Administrator 需要把动态库：jacob-1.18-x64.dll 单元测试需要手动放入至测试路径下
 */
public class OfficeToPdf {

	private static Logger log = LoggerFactory.getLogger(OfficeToPdf.class);

	private static final int wdFormatPDF = 17;
	private static final int xlTypePDF = 0;
	private static final int ppSaveAsPDF = 32;
	
	static {
		String libName;
		String jacobPath;
		// if (props.getProperty("os.name").toUpperCase().indexOf("WINDOWS") !=
		// -1){}
		// 判断使用的jre是1.6还是1.7
//		if (props.getProperty("java.home").contains("jre7")
//				|| props.getProperty("java.home").contains("jre8")) {
//			if (props.getProperty("os.arch").contains("64"))
				libName = "dll/jacob/jacob-1.18-x64.dll";// jdk1.7
//			else
//				libName = "dll/jacob/jacob-1.18-x86.dll";// jdk1.7
//		} else {
//			if (props.getProperty("os.arch").contains("64"))
//				libName = "dll/jacob/jacob-1.18-M2-x64.dll";// jdk1.6
//			else
//				libName = "dll/jacob/jacob-1.18_M2-x86.dll";// jdk1.6
//		}
//		
//		if (systemUtil.getProjectPhysicalPath() != null
//				&& !systemUtil.getProjectPhysicalPath().equals("")) {
//			jacobPath = systemUtil.getProjectPhysicalPath() + "WEB-INF"
//					+ File.separator + "classes" + File.separator + "com"
//					+ File.separator + "jacob" + File.separator + libName;
//		} else {
			/*jacobPath = OfficeToPdf.class.getClassLoader().getResource("").getPath().replace("%20", " ")
					  + "com/dist/dgpserver/common/jacob" + File.separator + libName;*/
			//jacobPath = "C:/jacob/"+libName;
//			jacobPath = System.getProperty("user.dir") + "\\dgp-server-common\\src\\main\\java\\com\\dist\\dgpserver\\common\\jacob\\" + libName;
			jacobPath = OfficeToPdf.class.getClassLoader().getResource("").getPath().replace("%20", " ")
					  + libName;
		log.info("jacob path>>>>>>>>>>>>>>>>>>>>>" + jacobPath);
		System.setProperty("jacob.dll.path", jacobPath);
	}

	/**
	 * 转换pdf
	 * @param inputFile
	 * @param pdfFile
	 * @return
	 */
	public static boolean convert2PDF(String inputFile, String pdfFile) {
		String suffix = getFileSufix(inputFile);
		String newInputFile = "";
		String newPdfFile = "";
		// 如果文件路径中含有空格，则需要先替换掉
		if (inputFile.contains(" ")) {
			newInputFile = inputFile.replace(" ", "");
			newPdfFile = pdfFile.replace(" ", "");
			File dir = new File(newInputFile.substring(0,newInputFile.lastIndexOf(File.separator)));
			if (!dir.isDirectory())
				dir.mkdirs();
			SystemUtil.copyFile(inputFile, newInputFile);
			File file = new File(newInputFile);
			if (!file.exists()) {
				System.out.println("文件不存在！");
				log.error("文件不存在！");
				return false;
			}
			if (suffix.equals("pdf")) {
				System.out.println("PDF not need to convert!");
				log.error("PDF not need to convert!");
				return false;
			}
			if (suffix.equals("doc") || suffix.equals("docx")
					|| suffix.equals("txt")) {
				boolean flag = word2PDF(newInputFile, newPdfFile);
				new File(newInputFile).delete();
				if (flag)
					SystemUtil.copyFile(newPdfFile, pdfFile);
				new File(newPdfFile).delete();
				return flag;
			} else if (suffix.equals("ppt") || suffix.equals("pptx")) {
				boolean flag = ppt2PDF(newInputFile, newPdfFile);
				new File(newInputFile).delete();
				if (flag)
					SystemUtil.copyFile(newPdfFile, pdfFile);
				new File(newPdfFile).delete();
				return flag;
			} else if (suffix.equals("xls") || suffix.equals("xlsx")) {
				boolean flag = excel2PDF(newInputFile, newPdfFile);
				new File(newInputFile).delete();
				if (flag)
					SystemUtil.copyFile(newPdfFile, pdfFile);
				new File(newPdfFile).delete();
				return flag;
			} else {
				System.out.println("文件格式不支持转换!");
				log.error("文件格式不支持转换!");
				return false;
			}
		} else {
			File file = new File(inputFile);
			if (!file.exists()) {
				System.out.println("文件不存在！");
				log.error("文件不存在！");
				return false;
			}
			if (suffix.equals("pdf")) {
				System.out.println("PDF not need to convert!");
				log.error("PDF not need to convert!");
				return false;
			}
			if (suffix.equals("doc") || suffix.equals("docx")
					|| suffix.equals("txt")) {
				return word2PDF(inputFile, pdfFile);
			} else if (suffix.equals("ppt") || suffix.equals("pptx")) {
				return ppt2PDF(inputFile, pdfFile);
			} else if (suffix.equals("xls") || suffix.equals("xlsx")) {
				return excel2PDF(inputFile, pdfFile);
			} else {
				System.out.println("文件格式不支持转换!");
				log.error("文件格式不支持转换!");
				return false;
			}
		}
	}
	
	public static boolean convert2PDF2(String inputFile, String pdfFile) {
//      System.out.println(System.getProperty("jacob.dll.path"));
      System.out.println("转换文件路径："+inputFile);
      String suffix = getFileSufix(inputFile);
      switch (suffix){
          case "ppt":
          case "pptx":
              return ppt2PDF(inputFile,pdfFile);
          case "doc":
          case "docx":
          case "txt":
              return word2PDF(inputFile,pdfFile);
          case "xls":
          case "xlsx":
              return excel2PDF(inputFile,pdfFile);
      }
      System.out.println("文件格式不支持转换");
      return false;
  }

	public static String getFileSufix(String fileName) {
		int splitIndex = fileName.lastIndexOf(".");
		return fileName.substring(splitIndex + 1);
	}

	public static boolean word2PDF(String inputFile, String pdfFile) {
		try {
			long startTime = System.currentTimeMillis();
			
			// 打开word应用程序
			ActiveXComponent app = new ActiveXComponent("Word.Application");
			// 设置word不可见
			app.setProperty("Visible", false);
			// 获得word中所有打开的文档,返回Documents对象
			Dispatch docs = app.getProperty("Documents").toDispatch();
			// 调用Documents对象中Open方法打开文档，并返回打开的文档对象Document
			Dispatch doc = Dispatch.call(docs, "Open", inputFile, false, true).toDispatch();
			Dispatch.call(doc, "ExportAsFixedFormat", pdfFile, wdFormatPDF // word保存为pdf格式宏，值为17
			);
			// 关闭文档
			Dispatch.call(doc, "Close", false);
			// 关闭word应用程序
			app.invoke("Quit", 0);
			long endTime = System.currentTimeMillis();
			log.info("word文件转换为pdf格式耗时/毫秒:"+(endTime-startTime));
			return true;
		} catch (Exception e) {
			System.out.println("word转PDF格式失败" + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {

		}
	}
	//尝试将文件流输出到  pdf文件  失败
	public static boolean word2PDF(InputStream inputStream, String pdfFile) {
		try {
			// 打开word应用程序
			ActiveXComponent app = new ActiveXComponent("Word.Application");
			// 设置word不可见
			app.setProperty("Visible", false);
			// 获得word中所有打开的文档,返回Documents对象
			Dispatch docs = app.getProperty("Documents").toDispatch();
			// 调用Documents对象中Open方法打开文档，并返回打开的文档对象Document
			Dispatch doc = Dispatch.call(docs, "Open", inputStream, false, true).toDispatch();
			// 调用Document对象的SaveAs方法，将文档保存为pdf格式
			/*
			 * Dispatch.call(doc, "SaveAs", pdfFile, wdFormatPDF
			 * //word保存为pdf格式宏，值为17 );
			 */
			Dispatch.call(doc, "ExportAsFixedFormat", pdfFile, wdFormatPDF // word保存为pdf格式宏，值为17
					);
			// 关闭文档
			Dispatch.call(doc, "Close", false);
			// 关闭word应用程序
			app.invoke("Quit", 0);
			return true;
		} catch (Exception e) {
			System.out.println("word转PDF格式失败" + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			
		}
	}

	public static boolean excel2PDF(String inputFile, String pdfFile) {
		try {
			long startTime = System.currentTimeMillis();
			ActiveXComponent app = new ActiveXComponent("Excel.Application");
			app.setProperty("Visible", false);
			Dispatch excels = app.getProperty("Workbooks").toDispatch();
			Dispatch excel = Dispatch.call(excels, "Open", inputFile, false,
					true).toDispatch();
			Dispatch.call(excel, "ExportAsFixedFormat", xlTypePDF, pdfFile);
			Dispatch.call(excel, "Close", false);
			app.invoke("Quit");
			long endTime = System.currentTimeMillis();
			log.info("xls文件转换为pdf格式 耗时/毫秒:"+(endTime-startTime));
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return false;
		}

	}

	public static boolean ppt2PDF(String inputFile, String pdfFile) {
		try {
			long startTime = System.currentTimeMillis();
			ActiveXComponent app = new ActiveXComponent(
					"PowerPoint.Application");
			// app.setProperty("Visible", msofalse);
			Dispatch ppts = app.getProperty("Presentations").toDispatch();

			Dispatch ppt = Dispatch.call(ppts, "Open", inputFile, true,// ReadOnly
					true,// Untitled指定文件是否有标题
					true// WithWindow指定文件是否可见
					).toDispatch();

			Dispatch.call(ppt, "SaveAs", pdfFile, ppSaveAsPDF);

			Dispatch.call(ppt, "Close");

			app.invoke("Quit");
			long endTime = System.currentTimeMillis();
			log.info("ppt文件转换为pdf格式 耗时/毫秒:"+(endTime-startTime));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//同word2PDF(String inputFile, String pdfFile)是一样的
	public static void wordToPDF(String sfileName, String toFileName){
        System.out.println("启动Word...");
        long start = System.currentTimeMillis();
        ActiveXComponent app = null;  
        Dispatch doc = null;  
        try {      
            app = new ActiveXComponent("Word.Application");      
            app.setProperty("Visible", new Variant(false));  
            Dispatch docs = app.getProperty("Documents").toDispatch();   
//          doc = Dispatch.call(docs,  "Open" , sourceFile).toDispatch();   
            doc = Dispatch.invoke(docs,"Open",Dispatch.Method,new Object[] {
               sfileName, new Variant(false),new Variant(true) }, new int[1]).toDispatch();               
            System.out.println("打开文档..." + sfileName);
            System.out.println("转换文档到PDF..." + toFileName);
            File tofile = new File(toFileName);
            if (tofile.exists()) {      
                tofile.delete();      
            }        
//          Dispatch.call(doc, "SaveAs",  destFile,  17);                    
            Dispatch.invoke(doc, "SaveAs", Dispatch.Method, new Object[] {
                toFileName, new Variant(17) }, new int[1]);    
            long end = System.currentTimeMillis();
            System.out.println("转换完成..用时：" + (end - start) + "ms.");
        } catch (Exception e) {
            e.printStackTrace();  
            System.out.println("========Error:文档转换失败：" + e.getMessage());
        } finally {  
            Dispatch.call(doc,"Close",false);  
            System.out.println("关闭文档");
            if (app != null)      
                app.invoke("Quit", new Variant[] {});      
            }  
          //如果没有这句话,winword.exe进程将不会关闭  
           ComThread.Release();     
    } 

	// 调用
//	@Description("只能用\\来表示路径")
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		OfficeToPdf o2p = new OfficeToPdf();

		String docPath = "H:/b.docx";
		String pdfPath = "H:/b.pdf";
		o2p.convert2PDF(docPath, pdfPath);
	}

}
