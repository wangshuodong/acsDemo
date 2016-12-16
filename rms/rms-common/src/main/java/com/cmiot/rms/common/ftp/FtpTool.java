package com.cmiot.rms.common.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FtpTool {

	public static void upload(String localFile) throws Exception {

		String server = "127.0.0.1";
		int port = 21;
		String user = "test";
		String pass = "test";
		String ftpFirmwareFileRootPath = "/firmware";

		FTPClient client = new FTPClient();
		FileInputStream fis = null;
		try {

			client.connect(server, port);
			client.login(user, pass);
			client.enterLocalPassiveMode();

			client.setFileType(FTP.BINARY_FILE_TYPE);

			String uploadName = "/tempxxxx.txt";
			if ("".equals(localFile)) {
				localFile = "E:/mobileno.txt";
			} else {

			}

			String ftpFilePath = ftpFirmwareFileRootPath+uploadName;
			fis = new FileInputStream(localFile);


			// Store file to server
			boolean done = client.storeFile(ftpFilePath, fis);
			client.logout();

			if (done) {
				System.out.println("The  file is uploaded successfully.");
			} else {
				throw new Exception("FTP文件上传失败");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				client.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		FtpTool.upload("");
	}
}
