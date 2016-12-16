package com.cmiot.rms.common.ftp;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.poi.util.StringUtil;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ftp连接管理(使用apache commons-net-1.4.1 lib)
 * </p>
 */
public class FtpClientUtil {

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(FtpClientUtil.class);

	private FTPClient ftpClient = null;
	private String server;
	private int port;
	private String userName;
	private String userPassword;

	public FtpClientUtil(String server, int port, String userName, String userPassword) {
		this.server = server;
		this.port = port;
		this.userName = userName;
		this.userPassword = userPassword;
		open();
	}

	/**
	 * 链接到服务器
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean open() {
		if (ftpClient != null && ftpClient.isConnected()) {
			return true;
		}
		try {
			ftpClient = new FTPClient();
			// 连接
			ftpClient.connect(this.server, this.port);
			ftpClient.login(this.userName, this.userPassword);
			// 检测连接是否成功
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				this.close();
				System.err.println("FTP server refused connection.");
				System.exit(1);
			}
			/*
			 * logger.info("open FTP success:" + this.server+";port:"+this.port
			 * + ";name:" + this.userName + ";pwd:" + this.userPassword);
			 */
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置上传模式.binally
			// or ascii
			return true;
		} catch (Exception ex) {
			// 关闭
			this.close();
			ex.printStackTrace();
			logger.info(exceptionInfo(ex));
			return false;
		}

	}

	public boolean cd(String dir) throws IOException {
		if (ftpClient.changeWorkingDirectory(dir)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取目录下所有的文件名称
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	private FTPFile[] getFileList(String filePath) throws IOException {
		FTPFile[] list = ftpClient.listFiles();
		return list;

	}

	/**
	 * 循环将设置工作目录
	 */
	public boolean changeDir(String ftpPath) {
		if (!ftpClient.isConnected()) {
			return false;
		}
		try {
			// 将路径中的斜杠统一
			char[] chars = ftpPath.toCharArray();
			StringBuffer sbStr = new StringBuffer(256);
			for (int i = 0; i < chars.length; i++) {

				if ('\\' == chars[i]) {
					sbStr.append('/');
				} else {
					sbStr.append(chars[i]);
				}
			}
			ftpPath = sbStr.toString();
			// System.out.println("ftpPath"+ftpPath);

			if (ftpPath.indexOf('/') == -1) {
				// 只有一层目录
				// System.out.println("change"+ftpPath);
				ftpClient.changeWorkingDirectory(new String(ftpPath.getBytes(), "iso-8859-1"));
			} else {
				// 多层目录循环创建
				String[] paths = ftpPath.split("/");
				// String pathTemp = "";
				for (int i = 0; i < paths.length; i++) {
					// System.out.println("change "+paths[i]);
					ftpClient.changeWorkingDirectory(new String(paths[i].getBytes(), "iso-8859-1"));
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 循环创建目录，并且创建完目录后，设置工作目录为当前创建的目录下
	 */
	public boolean mkDir(String ftpPath) {
		boolean ccateFtpPathStatus = false;
		if (!ftpClient.isConnected()) {
			return false;
		}
		try {

			// 将路径中的斜杠统一
			char[] chars = ftpPath.toCharArray();
			StringBuffer sbStr = new StringBuffer(256);
			for (int i = 0; i < chars.length; i++) {

				if ('\\' == chars[i]) {
					sbStr.append('/');
				} else {
					sbStr.append(chars[i]);
				}
			}
			ftpPath = sbStr.toString();
			/* logger.info("ftpPath" + ftpPath); */

			if (ftpPath.indexOf('/') == -1) {
				// 只有一层目录
				if (StringUtils.isNotBlank(ftpPath)) {
					// ftpClient.enterLocalPassiveMode();
					ccateFtpPathStatus = ftpClient.makeDirectory(new String(ftpPath.getBytes("GBK"), "iso-8859-1"));
					ccateFtpPathStatus = ftpClient
							.changeWorkingDirectory(new String(ftpPath.getBytes("GBK"), "iso-8859-1"));
				}
			} else {
				// 多层目录循环创建
				String[] paths = ftpPath.split("/");
				if (null != paths && paths.length > 0) {
					// String pathTemp = "";
					for (int i = 0; i < paths.length; i++) {
						if (StringUtils.isNotBlank(paths[i])) {
							// ftpClient.enterLocalPassiveMode();
							ccateFtpPathStatus = ftpClient
									.makeDirectory(new String(paths[i].getBytes("GBK"), "iso-8859-1"));
							ccateFtpPathStatus = ftpClient
									.changeWorkingDirectory(new String(paths[i].getBytes("GBK"), "iso-8859-1"));
						}
					}
				}
			}
			return ccateFtpPathStatus;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 上传文件到FTP服务器
	 *
	 * @param ftpFileName
	 *            上传后的文件名
	 * @param ftpDirectory
	 *            FTP目录如:/path1/pathb2/,如果目录不存在回自动创建目录
	 * @throws Exception
	 */
	public boolean put(String localDirectoryAndFileName, String ftpFileName, String ftpDirectory) {
		if (!ftpClient.isConnected()) {
			return false;
		}
		boolean flag = false;
		if (ftpClient != null) {
			File srcFile = new File(localDirectoryAndFileName);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(srcFile);
				// 创建目录
				// this.cd("/");
				this.mkDir(ftpDirectory);
				ftpClient.setBufferSize(1024);
				ftpClient.setControlEncoding("GBK");
				// 设置文件类型（二进制）
				ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
				// 上传
				flag = ftpClient.storeFile(new String(ftpFileName.getBytes("GBK"), "iso-8859-1"), fis);
			} catch (Exception e) {
				this.close();
				e.printStackTrace();
				logger.info(exceptionInfo(e));
				return false;
			} finally {
				IOUtils.closeQuietly(fis);
			}
		}
		logger.info("success put file " + localDirectoryAndFileName + " to " + ftpDirectory + ftpFileName);
		return flag;
	}

	/**
	 * 从FTP服务器上下载文件并返回下载文件长度
	 *
	 * @param ftpDirectoryAndFileName
	 * @param localDirectoryAndFileName
	 * @return
	 * @throws Exception
	 */
	public long get(String ftpDirectoryAndFileName, String localDirectoryAndFileName) {

		long result = 0;
		if (!ftpClient.isConnected()) {
			return 0;
		}
		ftpClient.enterLocalPassiveMode(); // Use passive mode as default
		// because most of us are behind
		// firewalls these days.
		try {
			// 将路径中的斜杠统一
			char[] chars = ftpDirectoryAndFileName.toCharArray();
			StringBuffer sbStr = new StringBuffer(256);
			for (int i = 0; i < chars.length; i++) {

				if ('\\' == chars[i]) {
					sbStr.append('/');
				} else {
					sbStr.append(chars[i]);
				}
			}
			ftpDirectoryAndFileName = sbStr.toString();
			String filePath = ftpDirectoryAndFileName.substring(0, ftpDirectoryAndFileName.lastIndexOf("/"));
			String fileName = ftpDirectoryAndFileName.substring(ftpDirectoryAndFileName.lastIndexOf("/") + 1);
			this.changeDir(filePath);
			ftpClient.retrieveFile(new String(fileName.getBytes(), "iso-8859-1"),
					new FileOutputStream(localDirectoryAndFileName)); // download
			// file
			System.out.print(ftpClient.getReplyString()); // check result

		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Success get file" + ftpDirectoryAndFileName + " from " + localDirectoryAndFileName);
		return result;
	}

	/**
	 * 返回FTP目录下的文件列表
	 *
	 * @param ftpDirectory
	 * @return
	 */
	public List getFileNameList(String ftpDirectory) {
		List list = new ArrayList();
		return list;
	}

	/**
	 * 删除FTP上的文件
	 *
	 * @param ftpDirAndFileName
	 */
	public boolean deleteFile(String ftpDirAndFileName) {
		if (!ftpClient.isConnected()) {
			return false;
		}
		// Todo
		return true;
	}

	/**
	 * 删除FTP目录
	 *
	 * @param ftpDirectory
	 */
	public boolean deleteDirectory(String ftpDirectory) {
		if (!ftpClient.isConnected()) {
			return false;
		}
		// ToDo
		return true;
	}

	/**
	 * 关闭链接
	 */
	public void close() {
		try {
			if (ftpClient != null && ftpClient.isConnected())
				ftpClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * logger.info("Close Server Success :"+this.server+";port:"+this.port);
		 */
	}

	public FTPClient getFtpClient() {
		return ftpClient;
	}

	public void setFtpClient(FTPClient ftpClient) {

		this.ftpClient = ftpClient;
	}

	public String exceptionInfo(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString().toLowerCase();
	}

}
