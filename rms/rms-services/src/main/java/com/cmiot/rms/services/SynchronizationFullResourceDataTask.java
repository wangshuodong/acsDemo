package com.cmiot.rms.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.cmiot.rms.common.ftp.FtpBusiness;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cmiot.rms.common.ftp.FtpClientUtil;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.SynDataDao;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.derivedclass.GatewayBean;

public class SynchronizationFullResourceDataTask {

	private static Logger logger = LoggerFactory.getLogger(SynchronizationFullResourceDataTask.class);

	@Value("${pboss.province.code}")
	String areaSimpleCode;

	@Value("${file.server.url}")
	String ftpUrl;

	@Value("${dsSyn.url}")
	String URL;

	@Value("${dsSyn.username}")
	String UNAME;

	@Value("${dsSyn.password}")
	String PWD;

	@Value("${ftpserver.localSaveFolder}")
	String curlocalSaveFolder;

	SimpleDateFormat dfToday = new SimpleDateFormat("yyyyMMdd");// 设置日期格式
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");// 设置日期格式
	SynDataDao queryDao = new SynDataDao();

	FtpBusiness ftpBusiness = new FtpBusiness();

	public void synchronizationData() {
		logger.info("Start with the full amount of resource data synchronization [" + DateTools.getTimeString() + "]");
		List<String> filePathList = new ArrayList<String>();
		try {
			logger.info("reqData " + DateTools.getCurrentTimeMillis());
			List<GatewayBean> gateWayBeanList = queryDao.queryList4Page(this.getConnection());
			logger.info("resData " + DateTools.getCurrentTimeMillis());
			if (!sendGatwayFileToFtpServer(gateWayBeanList, filePathList)) {
				logger.info("synchronization gatewayinfo to ftpserver falied!");
			} else {
				logger.info("synchronization gatewayinfo to ftpserver successed!");
			}
		} catch (SQLException e) {
			logger.info("SQL Exception:" + exceptionInfo(e));
		}
		try {
			logger.info("reqData " + DateTools.getCurrentTimeMillis());
			List<BoxInfo> boxBeanlist = queryDao.selectBoxInfo(this.getConnection());
			logger.info("resData " + DateTools.getCurrentTimeMillis());
			if (!sendBoxFileToFtpServer(boxBeanlist, filePathList)) {
				logger.info("synchronization boxinfo to ftpserver falied!");
			} else {
				logger.info("synchronization boxinfo to ftpserver successed!");
			}
		} catch (SQLException e) {
			logger.info("SQL Exception:" + exceptionInfo(e));
		}
//		File filedel = new File(File.separator + areaSimpleCode + File.separator);
		try {
			if(!filePathList.isEmpty() && filePathList.size() > 0){
				logger.info("开始删除同步全量数据时创建的临时文件和文件夹{}", filePathList);
				for(int i=0;i<filePathList.size();i++){
					File filedel = new File(filePathList.get(i));
					if(filedel != null && filedel.exists() && filedel.getParentFile().exists()) deletefile(filedel.getParent());
				}
			}
			
		} catch (Exception e) {
			logger.info("synchronizationData Exception:" + exceptionInfo(e));
		}
		logger.info("Complete the full amount of resource data synchronization [" + DateTools.getTimeString() + "]");
	}

	public boolean deletefile(String delpath) throws Exception {
		/* logger.info(" deletefile ing..."); */
		try {
			File file = new File(delpath);
			// 当且仅当此抽象路径名表示的文件存在且 是一个目录时，返回 true
			if (!file.isDirectory()) {
				file.delete();
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					/* File delfile = new File(delpath + "\\" + filelist[i]); */
					File delfile = new File(delpath + File.separator + filelist[i]);
					if (!delfile.isDirectory()) {
						delfile.delete();
						/*
						 * logger.info(delfile.getAbsolutePath() +
						 * "delete file success");
						 */
					} else if (delfile.isDirectory()) {
						/* deletefile(delpath + "\\" + filelist[i]); */
						deletefile(delpath + File.separator + filelist[i]);
					}
				}
				/*
				 * logger.info(file.getAbsolutePath()+"delete folder success");
				 */
				file.delete();
			}
		} catch (FileNotFoundException e) {
			logger.info("deletefile() Exception:" + exceptionInfo(e));
		}
		/* logger.info(" deletefile end..."); */
		return true;
	}

	public boolean sendGatwayFileToFtpServer(List<GatewayBean> gateWayBeanList, List<String> targetFile) {
		boolean opeResult = false;
		String localSaveFolder = curlocalSaveFolder + System.currentTimeMillis() + File.separator;
		try {
			if (gateWayBeanList.size() > 0) {
				File file = null;
				Map<String, List<GatewayBean>> resultMap = new HashMap<String, List<GatewayBean>>(); // 最终要的结果
																										// 等到一个按factory分组的Map
				if (gateWayBeanList.size() > 0) {
					List<GatewayBean> dataList = gateWayBeanList;
					GatewayBean dataItem; // 数据库中查询到的每条记录
					for (int i = 0; i < dataList.size(); i++) {
						dataItem = dataList.get(i);
						if (resultMap.containsKey(dataItem.getGatewayFactoryCode())) {
							resultMap.get(dataItem.getGatewayFactoryCode()).add(dataItem);
						} else {
							List<GatewayBean> list = new ArrayList<GatewayBean>();
							list.add(dataItem);
							resultMap.put(dataItem.getGatewayFactoryCode(), list);
						}
					}
					for (Map.Entry<String, List<GatewayBean>> resultMapEntry : resultMap.entrySet()) {
						List<Map<String, String>> exportData = new ArrayList<Map<String, String>>();
						List<GatewayBean> gatewayBeanEntry = resultMapEntry.getValue();
						for (int i = 0; i < gatewayBeanEntry.size(); i++) {
							Map<String, String> row1 = new LinkedHashMap<String, String>();
							row1.put("1", String.valueOf(gatewayBeanEntry.get(i).getGatewayAreaId()));
							row1.put("2", String.valueOf(gatewayBeanEntry.get(i).getGatewayPassword()));
							//row1.put("3", String.valueOf(gatewayBeanEntry.get(i).getGatewaySerialnumber()));
							row1.put("3", String.valueOf(gatewayBeanEntry.get(i).getGatewayFactoryCode() +"-"+ gatewayBeanEntry.get(i).getGatewaySerialnumber()));
							row1.put("4", String.valueOf(gatewayBeanEntry.get(i).getGatewaySerialnumber()));
							row1.put("5", String.valueOf(gatewayBeanEntry.get(i).getGatewayMacaddress()));
							row1.put("6", String.valueOf(gatewayBeanEntry.get(i).getGatewayModel()));
							row1.put("7", String.valueOf(gatewayBeanEntry.get(i).getGatewayFactoryCode()));
							row1.put("8", String.valueOf(gatewayBeanEntry.get(i).getGatewayVersion()));
							row1.put("9", String.valueOf(0));
							row1.put("10", String.valueOf(gatewayBeanEntry.get(i).getGatewayAdslAccount()));
							String pl = "";
							pl = gatewayBeanEntry.get(i).getParameterList();
							if (StringUtils.isNotBlank(pl) && pl.indexOf("SIPUserName") > -1) {
								Map<String, String> userMap = JSON.parseObject(pl, new TypeReference<Map<String, String>>() {
								});
								if (null != userMap && userMap.size() > 0 && StringUtils.isNotBlank(userMap.get("SIPUserName"))) pl = userMap.get("SIPUserName");
							}
							row1.put("11", String.valueOf(pl));
							row1.put("12", String.valueOf(gatewayBeanEntry.get(i).getGatewayMemo()));
							exportData.add(row1);
						}
						/*
						 * AreaCode|BindCode|HguId|SN|HguMac|ProductClass|
						 * HguVendor|SoftwareVersion|WifiFlag|PPPOEUser|VOIPUser
						 * |Remark
						 */
						LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
						map.put("1", "AreaCode");
						map.put("2", "BindCode");
						map.put("3", "HguId");
						map.put("4", "SN");
						map.put("5", "HguMac");
						map.put("6", "ProductClass");
						map.put("7", "HguVendor");
						map.put("8", "SoftwareVersion");
						map.put("9", "WifiFlag");
						map.put("10", "PPPOEUser");
						map.put("11", "VOIPUser");
						map.put("12", "Remark");
						// *ftproot/GD/ZD/HW/DHMP/CM/20160611
						String ftpBasePath = areaSimpleCode + "|" + "ZD" + "|"
								+ resultMapEntry.getKey() + "|" + "DHMP" + "|" + "CM"
								+ "|" + String.valueOf(dfToday.format(new Date()));
						/*String truePath = makePath(ftpBasePath);*/
						/*FtpClientUtil ftpClinet = new FtpClientUtil(sysIp, sysPort, sysAccount, sysPassword);// 连接ftp准备*/
						// createFolder(truePath);
						// 创建FTP目录
						// ftpClinet.cd("/");
						// ftpClinet.mkDir(truePath);
						// 创建保存目录
						String fileName = resultMapEntry.getKey() + "gateway";
						// file = createCSVFile(exportData, map, truePath,
						// fileName);
						file = createCSVFile(exportData, map, localSaveFolder, fileName);
						targetFile.add(file.getPath());
						String zipFileName = "CM-CPE-HGU-00-V1.0.0-" + String.valueOf(df.format(new Date()));
						// 打包文件
						// fileToZip(truePath,truePath,zipFileName);
						fileToZip(localSaveFolder, localSaveFolder, zipFileName);
						//使用fileServer提交文件
						String fileServerPath = ftpUrl + "&path=" + ftpBasePath;
						String fileLocalName = localSaveFolder + zipFileName + ".zip";
						String ftpserverName = ftpBusiness.sendTofileServer(fileServerPath,fileLocalName);
						logger.info("ftpserverName " + ftpserverName);
						// ftpClinet.open(); // 我打开了链接
						/*ftpClinet.put(localSaveFolder + zipFileName + ".zip", zipFileName + ".zip", truePath);*/// 上传至ftp服务器
						// File sourceFile = new File(localSaveFolder);
						/*ftpClinet.close();*/
						// 删除文件
						/* delete(sourceFile); */
						
					}
					opeResult = true;
				} else {
					opeResult = false;
				}
			}
		} catch (Exception e) {
			logger.info(exceptionInfo(e));
		}
		return opeResult;
	}

	public boolean sendBoxFileToFtpServer(List<BoxInfo> boxBeanList, List<String> targetfile) {
		boolean opeResult = false;
		String localSaveFolder = curlocalSaveFolder + System.currentTimeMillis() + File.separator;
		try {
			/*FtpClientUtil ftpClinet = new FtpClientUtil(sysIp, sysPort, sysAccount, sysPassword);*/// 连接ftp准备
			if (boxBeanList.size() > 0) {
				File file = null;
				Map<String, List<BoxInfo>> resultMap = new HashMap<String, List<BoxInfo>>(); // 最终要的结果
																								// 等到一个按factory分组的Map
				List<BoxFirmwareInfo> boxFirmwareInfoList = queryDao.queryFirmware(this.getConnection());
				if (boxBeanList.size() > 0) {
					List<BoxInfo> dataList = boxBeanList;
					BoxInfo dataItem; // 数据库中查询到的每条记录
					for (int i = 0; i < dataList.size(); i++) {
						dataItem = dataList.get(i);
						if (resultMap.containsKey(dataItem.getBoxFactoryCode())) {
							resultMap.get(dataItem.getBoxFactoryCode()).add(dataItem);
						} else {
							List<BoxInfo> list = new ArrayList<BoxInfo>();
							list.add(dataItem);
							resultMap.put(dataItem.getBoxFactoryCode(), list);
						}
					}
					for (Map.Entry<String, List<BoxInfo>> resultMapEntry : resultMap.entrySet()) {
						List<Map<String, String>> exportData = new ArrayList<Map<String, String>>();
						List<BoxInfo> boxInfoBeanEntry = resultMapEntry.getValue();
						for (int i = 0; i < boxInfoBeanEntry.size(); i++) {
							Map<String, String> row1 = new LinkedHashMap<String, String>();
							row1.put("1", String.valueOf(boxInfoBeanEntry.get(i).getBoxAreaId()));
							row1.put("2", String.valueOf(boxInfoBeanEntry.get(i).getBoxFactoryCode()+"-"+boxInfoBeanEntry.get(i).getBoxSerialnumber()));
							row1.put("3", String.valueOf(0));
							row1.put("4", String.valueOf(boxInfoBeanEntry.get(i).getBoxMacaddress()));
							row1.put("5", String.valueOf(boxInfoBeanEntry.get(i).getBoxType()));
							row1.put("6", String.valueOf(boxInfoBeanEntry.get(i).getBoxModel()));
							row1.put("7", boxInfoBeanEntry.get(i).getBoxFactoryCode());
							row1.put("8", "");
							if (boxInfoBeanEntry.get(i).getBoxFirmwareUuid() != null || !"".equals(boxInfoBeanEntry.get(i).getBoxFirmwareUuid())) {
								if (boxFirmwareInfoList != null && boxFirmwareInfoList.size() > 0) {
									for (BoxFirmwareInfo boxFirmwareInfo : boxFirmwareInfoList) {
										if (!"".equals(boxFirmwareInfo.getId()) && boxFirmwareInfo.getId().equals(boxInfoBeanEntry.get(i).getBoxFirmwareUuid())) {
											row1.put("8", boxFirmwareInfo.getFirmwareVersion());
										}
									}
								}
							}
							if (boxInfoBeanEntry.get(i).getBoxConnectAccount() != null
									&& !"".equals(boxInfoBeanEntry.get(i).getBoxConnectAccount())) {
								row1.put("9", boxInfoBeanEntry.get(i).getBoxConnectAccount());
							} else {
								row1.put("9", "");
							}

							row1.put("10", String.valueOf(boxInfoBeanEntry.get(i).getBoxMemo()));
							exportData.add(row1);
						}
						/*
						 * AreaCode|StbId|HguId|StbMac|StbType|ProductClass|
						 * StbVendor|SoftwareVersion|UserID|Remark
						 */
						LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
						map.put("1", "AreaCode");
						map.put("2", "StbId");
						map.put("3", "HguId");
						map.put("4", "StbMac");
						map.put("5", "StbType");
						map.put("6", "ProductClass");
						map.put("7", "StbVendor");
						map.put("8", "SoftwareVersion");
						map.put("9", "UserID");
						map.put("10", "Remark");
						String ftpBasePath = areaSimpleCode + "|" + "ZD" + "|"
								+ resultMapEntry.getKey() + "|" + "DHMP" + "|" + "CM"
								+ "|" + String.valueOf(dfToday.format(new Date()));

						/*String truePath = makePath(ftpBasePath);*/

						// 创建FTP目录
						// ftpClinet.cd("/");
						// ftpClinet.mkDir(truePath);
						// 创建保存目录
						String fileName = resultMapEntry.getKey() + "box";
						file = createCSVFile(exportData, map, localSaveFolder, fileName);
						targetfile.add(file.getAbsolutePath());
						String zipFileName = "CM-CPE-STB-00-V1.0.0-" + String.valueOf(df.format(new Date()));
						// 打包文件
						fileToZip(localSaveFolder, localSaveFolder, zipFileName);
						//使用fileServer提交文件
						String fileServerPath = ftpUrl + "&path=" + ftpBasePath;
						String fileLocalName = localSaveFolder + zipFileName + ".zip";
						String ftpserverName = ftpBusiness.sendTofileServer(fileServerPath,fileLocalName);
						logger.info("ftpserverName " + ftpserverName);

						// ftpClinet.open(); // 我打开了链接
						/*ftpClinet.put(localSaveFolder + zipFileName + ".zip", zipFileName + ".zip", truePath);*/// 上传至ftp服务器
						// File sourceFile = new File(localSaveFolder);
						/*ftpClinet.close();*/
						// 删除文件
						/* delete(sourceFile); */
						
					}
					opeResult = true;
				} else {
					opeResult = false;
				}
			}
		} catch (Exception e) {
			logger.info(exceptionInfo(e));
		}
		return opeResult;

	}

	private String makePath(String ftpBasePath) {
		String[] ftpBasePathArr = ftpBasePath.split("/");
		StringBuffer pathSb = new StringBuffer();
		if (ftpBasePathArr.length > 0) {
			for (int l = 0; l < ftpBasePathArr.length; l++) {
				if (l == 0) {
					pathSb.append("|" + ftpBasePathArr[l] + "|");
				} else {
					pathSb.append(ftpBasePathArr[l] + "|");
				}
			}
		} else {
			pathSb.append("|" + ftpBasePath + "|");
		}
		return pathSb.toString();
	}

	/**
	 * 生成为CVS文件
	 * 
	 * @param exportData
	 *            源数据List
	 * 
	 *            csv文件的列表头map
	 * @param outPutPath
	 *            文件路径
	 * @param fileName
	 *            文件名称
	 * @return
	 */
	public File createCSVFile(List<Map<String, String>> exportData, LinkedHashMap<String, String> map, String outPutPath, String fileName) {
		File csvFile = null;
		BufferedWriter csvFileOutputStream = null;
		try {
			File file = new File(outPutPath);
			if (!file.exists()) {
				boolean it = file.mkdirs();
				if (!it)
					throw new Exception("创建[" + outPutPath + "]目录返回状态:" + it);
			}
			// 定义文件名格式并创建
			logger.info("同步到FTP时fileName:" + fileName + ";outPutPath:" + outPutPath);
			csvFile = File.createTempFile(fileName, ".csv", new File(outPutPath));
			/* logger.info("csvFile：" + csvFile); */
			// UTF-8使正确读取分隔符","
			csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"),
					1024);
			/* logger.info("csvFileOutputStream: " + csvFileOutputStream); */
			// 写入文件头部
			for (Iterator<Map.Entry<String, String>>  propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {
				Map.Entry<String, String> propertyEntry = propertyIterator.next();
				csvFileOutputStream
						.write(propertyEntry.getValue() != null ? propertyEntry.getValue() : "");
				if (propertyIterator.hasNext()) {
					csvFileOutputStream.write("|");
				}
			}
			csvFileOutputStream.newLine();
			// 写入文件内容
			for (Iterator<Map<String, String>> iterator = exportData.iterator(); iterator.hasNext();) {
				Map<String, String> row = iterator.next();
				for (Iterator<Map.Entry<String, String>> propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {
					Map.Entry<String, String> propertyEntry = propertyIterator.next();
					csvFileOutputStream.write(row.get(propertyEntry.getKey()));
					if (propertyIterator.hasNext()) {
						csvFileOutputStream.write("|");
					}
				}
				if (iterator.hasNext()) {
					csvFileOutputStream.newLine();
				}
			}
			csvFileOutputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(exceptionInfo(e));
		} finally {
			try {
				csvFileOutputStream.close();
			} catch (IOException e) {
				logger.info(exceptionInfo(e));
			}
		}
		return csvFile;
	}

	public void createFolder(String filePath) {
		File f = new File(filePath);
		/* f.setWritable(true, false); */
		/* logger.info(" filePath " + filePath); */
		if (!f.exists()) {
			boolean op = f.mkdirs();
			/*
			 * if(op){ logger.info( " create filePath " + filePath +" success!"
			 * ); }else{ logger.info( " mkdirs " + filePath +" failed!"); }
			 */
		} else {
			/* logger.info( " filePath " + filePath +" is exists!"); */
		}
	}

	/**
	 * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
	 * 
	 * @param sourceFilePath
	 *            :待压缩的文件路径
	 * @param zipFilePath
	 *            :压缩后存放路径
	 * @param fileName
	 *            :压缩后文件的名称
	 * @return
	 */
	public boolean fileToZip(String sourceFilePath, String zipFilePath, String fileName) {
		boolean flag = false;
		File sourceFile = new File(sourceFilePath);
		logger.info("sourceFile.getAbsolutePath:" + sourceFile.getAbsolutePath());
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		ZipOutputStream zos = null;

		if (sourceFile.exists() == false) {
			logger.info("待压缩的文件目录：" + sourceFilePath + "不存在.");
		} else {
			try {
				File zipFile = new File(zipFilePath + "/" + fileName + ".zip");
				if (zipFile.exists()) {
					logger.info(zipFilePath + "目录下存在名字为:" + fileName + ".zip" + "打包文件.");
				} else {
					File[] sourceFiles = sourceFile.listFiles();
					if (null == sourceFiles || sourceFiles.length < 1) {
						logger.info("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
					} else {
						fos = new FileOutputStream(zipFile);
						zos = new ZipOutputStream(new BufferedOutputStream(fos));
						byte[] bufs = new byte[1024 * 10];
						for (int i = 0; i < sourceFiles.length; i++) {
							// 创建ZIP实体，并添加进压缩包
							ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
							zos.putNextEntry(zipEntry);
							// 读取待压缩的文件并写进压缩包里
							fis = new FileInputStream(sourceFiles[i]);
							bis = new BufferedInputStream(fis, 1024 * 10);
							int read = 0;
							while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
								zos.write(bufs, 0, read);
							}
						}
						flag = true;
					}
				}
			} catch (FileNotFoundException e) {
				logger.info(exceptionInfo(e));
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IOException e) {
				logger.info(exceptionInfo(e));
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				// 关闭流
				try {
					if (null != bis)
						bis.close();
					if (null != zos)
						zos.close();
				} catch (IOException e) {
					logger.info(exceptionInfo(e));
					throw new RuntimeException(e);
				}
			}
		}
		return flag;
	}

	public String exceptionInfo(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString().toLowerCase();
	}

	private Connection conn = null;

	public Connection getConnection() {
		try {
			// 1.加载驱动程序
			Class.forName("com.mysql.jdbc.Driver");
			// 2.获得数据库的连接
			conn = DriverManager.getConnection(URL, UNAME, PWD);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.info(exceptionInfo(e));
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info(exceptionInfo(e));
		}
		return conn;
	}

	public static void delete(File file) throws IOException {

		if (file.isDirectory()) {

			// directory is empty, then delete it
			if (file.list().length == 0) {

				file.delete();
				System.out.println("Directory is deleted : " + file.getAbsolutePath());

			} else {

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
					System.out.println("Directory is deleted : " + file.getAbsolutePath());
				}
			}

		} else {
			// if file, then delete it
			boolean b = file.delete();
			System.out.println("File is deleted : " + file.getAbsolutePath() + "是否成功：" + b);
		}
	}

}
