package com.cmiot.rms.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.SetParameterAttributesStruct;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxDeviceInfoMapper;
import com.cmiot.rms.dao.mapper.BoxFactoryInfoMapper;
import com.cmiot.rms.dao.mapper.BoxFirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.mapper.DeviceInfoMapper;
import com.cmiot.rms.dao.mapper.FirmwareInfoMapper;
import com.cmiot.rms.dao.model.BoxDetail;
import com.cmiot.rms.dao.model.BoxDeviceInfo;
import com.cmiot.rms.dao.model.BoxFactoryInfo;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.dao.model.HardwareAblity;
import com.cmiot.rms.dao.model.derivedclass.BoxBean;
import com.cmiot.rms.dao.vo.BoxExcelContent;
import com.cmiot.rms.services.BoxFirmwareInfoService;
import com.cmiot.rms.services.BoxInfoService;
import com.cmiot.rms.services.BoxManageService;
import com.cmiot.rms.services.FirmwareInfoService;
import com.cmiot.rms.services.HardwareAblityService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.boxManager.instruction.BoxInstructionMethodService;
import com.cmiot.rms.services.boxManager.instruction.BoxInvokeInsService;
import com.cmiot.rms.services.boxValidator.BoxValidatorManagement;
import com.cmiot.rms.services.boxValidator.result.BoxValidateResult;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.iot.common.file.poi.ExcelUtil;

/**
 * Created by admin on 2016/6/8.
 */
@Service
public class BoxManageServiceImpl implements BoxManageService {

	private Logger logger = LoggerFactory.getLogger(BoxManageServiceImpl.class);

	@Autowired
	private BoxInfoService boxInfoService;

	@Autowired
	private RedisClientTemplate redisClientTemplate;

	@Autowired
	private LogManagerService logManagerService;

	@Resource
	private HardwareAblityService hardwareAblityService;

	@Autowired
	AreaService areaService;

	@Autowired
	FirmwareInfoService firmwareInfoService;

	@Autowired
	private DeviceInfoMapper deviceInfoMapper;

	@Autowired
	FirmwareInfoMapper firmwareInfoMapper;

	@Autowired
	BoxInvokeInsService boxInvokeInsService;

	@Autowired
	BoxFactoryInfoMapper boxFactoryInfoMapper;

	@Autowired
	BoxFirmwareInfoService bxofirmwareInfoService;

	@Value("${rebootTimeOutBox}")
	int rebootTimeOutBox;

	@Value("${factoryResetTimeOutBox}")
	int factoryResetTimeOutBox;

	@Autowired
	private BoxInfoMapper boxInfoMapper;

	@Autowired
	private BoxFirmwareInfoMapper boxFirmwareInfoMapper;

	@Autowired
	private InstructionsService instructionsService;

	@Autowired
	BoxInstructionMethodService boxInstructionMethodService;

	@Autowired
	BoxDeviceInfoMapper boxDeviceInfoMapper;

	private List<Map<String, Object>> deviceInfoList; // 记录查询出的结果集

	private List<Map<String, Object>> nonDeviceInfoList; // 记录查询过，但是没有的网关设备信息
	
    @Value("${ftpserver.localSaveFolder}")
    String excelSaveFolder;
    
	 @Autowired
	 BoxValidatorManagement boxValidatorManagement;

	/**
	 * 验证数据
	 * @param listBooks
	 * @return
	 */
	private String validatorManagementCheck(List<BoxExcelContent> listBooks) {
		for (int i = 0, inv = listBooks.size(); i < inv; i++) {
			BoxValidateResult result = boxValidatorManagement.validate(i + 1, listBooks.get(i));
			if (!result.isValid()) return result.getMessage();
		}
		return null;
	}

	@Override
	public Map<String, Object> importBox(Map<String, Object> parameter) {
		logger.info("机顶盒导入请求参数:{}", parameter);
		try {
			String filePath = (null == parameter.get("filePath") || StringUtils.isBlank(parameter.get("filePath").toString())) ? null : parameter.get("filePath").toString();
			if (StringUtils.isBlank(filePath)) return reutrnMap(RespCodeEnum.RC_1.code(), "获取文件上传路径失败", false);
			URL website = new URL(filePath);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			String[] excelSaveFolderPath = excelSaveFolder.split("/");
			StringBuffer pathSb = new StringBuffer();
			if (excelSaveFolderPath.length > 0) {
				for (int l = 0; l < excelSaveFolderPath.length; l++) {
					if (l == 0) {
						pathSb.append(File.separator + excelSaveFolderPath[l] + File.separator);
					} else {
						pathSb.append(excelSaveFolderPath[l] + File.separator);
					}
				}
			} else {
				pathSb.append(File.separator + excelSaveFolder + File.separator);
			}
			String filelocalPath = pathSb + "boxInfo" + DateTools.getCurrentTimeMillis() + ".xls";
			createFolder(pathSb.toString());
			logger.info(" filelocalPath " + filelocalPath);
			FileOutputStream fos = new FileOutputStream(filelocalPath);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			rbc.close();
			fos.close();
			FileInputStream fis = new FileInputStream(filelocalPath);
			ExcelUtil<BoxExcelContent> util = new ExcelUtil<BoxExcelContent>(BoxExcelContent.class);
			List<BoxExcelContent> listBooks = util.importExcel(null, fis);
			if (null == listBooks || listBooks.size() <= 0) return reutrnMap(RespCodeEnum.RC_1.code(), "上传失败,请确认上传文件类型和模板一致!", false);
			String errorMsg = validatorManagementCheck(listBooks);
			if (StringUtils.isNotBlank(errorMsg)) return reutrnMap(RespCodeEnum.RC_1.code(), errorMsg, false);
			boolean flag = insertOrUpdateGateway(listBooks);// 所有验证通过，存储数据
			if (!flag) return reutrnMap(RespCodeEnum.RC_1.code(), "导入机顶盒失败,在插入数据时失败!", false);
			// 记录日志
			Map<String, Object> parameterLog = new HashMap<>();
			// 操作的数据内容
			parameterLog.put("content", JSON.toJSONString(listBooks));
			// 登录用户名称
			parameterLog.put("userName", parameter.get("userName"));
			// 类目ID(菜单ID)
			parameterLog.put("categoryMenu", CategoryEnum.BOX_MANAGER_SERVICE.name());
			// 具体的操作
			parameterLog.put("operation", "导入机顶盒信息");
			// 角色名称
			parameterLog.put("roleName", parameter.get("roleName"));
			// 类目名称
			parameterLog.put("categoryMenuName", CategoryEnum.BOX_MANAGER_SERVICE.description());
			// 日志类别
			parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
			logManagerService.recordOperationLog(parameterLog);
			return reutrnMap(RespCodeEnum.RC_0.code(), "机顶盒导入成功", true);
		} catch (DuplicateKeyException de) {
			logger.info("importBox DuplicateKey Exception ", de);
			return reutrnMap(RespCodeEnum.RC_1.code(), "导入机顶盒失败,数据唯一字段重复", false);
		} catch (Exception e) {
			logger.info("importBox Exception ", e);
			return reutrnMap(RespCodeEnum.RC_1.code(), "导入机顶盒失败,系统内部错误", false);
		}

		// Map<String, Object> resultMap = new HashMap<>();
		// String str = JSON.toJSONString(parameter.get("boxExcels"));
		//
		// /*
		// * String str =String.valueOf(parameter.get("boxExcels"));
		// * FtpBusiness getfile = new FtpBusiness();
		// * File f = getfile.downloadFromUrl(str,"\\");
		// * logger.info(" f " + f);
		// * FileInputStream fis = null;
		// * try {
		// * fis = new FileInputStream(f);
		// * } catch (FileNotFoundException e) {
		// * e.printStackTrace();
		// * }
		// * ExcelUtil util = new ExcelUtil(BoxExcelContent.class);
		// * logger.info(util.toString());
		// * List listBooks = util.importExcel("网关导入信息", fis);
		// * logger.info(String.valueOf(listBooks.size()));
		// */
		//
		// logger.info("文件上传传入参数：" + str);
		// List<BoxExcelContent> listBooks;
		// try {
		// listBooks = JSON.parseArray(str, BoxExcelContent.class);
		// } catch (Exception e) {
		// logger.info(exceptionInfo(e));
		// String msg = "导入机顶盒失败,请确认上传的字段类型是否正确！";
		// logger.info(msg);
		// return reutrnMap(RespCodeEnum.RC_ERROR.code(), msg, false);
		// }
		//
		// try {
		// if (listBooks != null) {
		// if (listBooks == null || listBooks.size() < 1) {
		// String msg = "导入机顶盒失败 请确认上传文件类型和模板一致!";
		// logger.info(msg);
		// return reutrnMap(RespCodeEnum.RC_ERROR.code(), msg, false);
		// } else {
		// Map<String, Object> retmap = checkContent(listBooks);
		// if (retmap == null || retmap.isEmpty()) {// excel表格是否填寫完整
		// Map<String, Object> errorContentMap = checkGatewayMac(listBooks);
		// if (errorContentMap.isEmpty()) {
		// errorContentMap = checkFireware(listBooks);
		// if (errorContentMap.isEmpty()) {
		// // 所有验证通过，存储数据
		// boolean flag = insertOrUpdateGateway(listBooks);
		// if (!flag) {
		// String msg = "导入机顶盒失败,在插入数据时失败!";
		// return reutrnMap(RespCodeEnum.RC_1.code(), msg.toString(), false);
		// }
		// Map<String, Object> parameterLog = new HashMap<>();
		// // 操作的数据内容
		// parameterLog.put("content", JSON.toJSONString(listBooks));
		// // 登录用户名称
		// parameterLog.put("userName", parameter.get("userName"));
		// // 类目ID(菜单ID)
		// parameterLog.put("categoryMenu", CategoryEnum.BOX_MANAGER_SERVICE.name());
		// // 具体的操作
		// parameterLog.put("operation", "导入机顶盒信息");
		// // 角色名称
		// parameterLog.put("roleName", parameter.get("roleName"));
		// // 类目名称
		// parameterLog.put("categoryMenuName", CategoryEnum.BOX_MANAGER_SERVICE.description());
		// // 日志类别
		// parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
		// logManagerService.recordOperationLog(parameterLog);
		// return reutrnMap(RespCodeEnum.RC_0.code(), "", true);
		// } else {
		// String msg = "导入机顶盒失败,第" + errorContentMap.get("index") + "条数据错误,请在固件管理模块录入厂商编码:" + ((BoxExcelContent) errorContentMap.get("gateway")).getBoxInfoFactoryCode() + "机顶盒型号:"
		// + ((BoxExcelContent) errorContentMap.get("gateway")).getBoxInfoModel() + " 固件版本:" + ((BoxExcelContent) errorContentMap.get("gateway")).getBoxInfoVersion() + ",再次上传！";
		// logger.info(msg);
		// return reutrnMap(RespCodeEnum.RC_1.code(), msg.toString(), false);
		// }
		// } else {
		//
		// StringBuffer msg = new StringBuffer();
		// msg.append("导入机顶盒失败,第" + errorContentMap.get("index") + "条数据错误,已存在 ");
		// if (errorContentMap.get("macFlag").equals(1)) {
		// msg.append("MAC地址: " + ((BoxExcelContent) errorContentMap.get("gateway")).getBoxInfoMacaddress());
		// }
		// if (errorContentMap.get("snFlag").equals(1)) {
		// msg.append("网关SN: " + ((BoxExcelContent) errorContentMap.get("gateway")).getBoxInfoSerialnumber());
		// }
		// msg.append(",请修改后再次上传！");
		// logger.info(msg.toString());
		// return reutrnMap(RespCodeEnum.RC_1.code(), msg.toString(), false);
		// }
		//
		// } else {
		// String msg = "导入机顶盒失败,第" + retmap.get("index") + "条数据错误," + retmap.get("filed") + "不能为空，请按照下载的导入模版填入全部字段后再上传！";
		// logger.info(msg);
		// return reutrnMap(RespCodeEnum.RC_1.code(), msg.toString(), false);
		// }
		// }
		// } else {
		// String msg = "导入机顶盒失败,文件上传为空";
		// logger.info(msg);
		// return reutrnMap(RespCodeEnum.RC_1.code(), msg.toString(), false);
		// }
		// } catch (DuplicateKeyException de) {
		// logger.info(exceptionInfo(de));
		// logger.info(" 导入机顶盒失败,错误信息 " + de.toString());
		// return reutrnMap(RespCodeEnum.RC_1.code(), "导入机顶盒失败,数据唯一字段重复", false);
		// } catch (Exception e) {
		// logger.info(exceptionInfo(e));
		// logger.info(" 导入机顶盒失败,错误信息 " + e.toString());
		// return reutrnMap(RespCodeEnum.RC_1.code(), "导入机顶盒失败,系统内部错误", false);
		// }
	}
	
    public void createFolder(String filePath) {
        File f=new File(filePath);
        /*f.setWritable(true, false);*/
        logger.info(" filePath " + filePath);
        if(!f.exists()){
            boolean op = f.mkdirs();
            if(op){
            	logger.info( " create filePath " + filePath +" success!");
            }else{
            	logger.info( " mkdirs " + filePath +" failed!");
            }
        }else{
        	logger.info( " filePath " + filePath +" is exists!");
        }
    }

	@Override
	public Map<String, Object> queryList4Page(Map<String, Object> parameter) {
		logger.info("start invoke queryList4Page parameter:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String uid = (String) parameter.get("uid");
			if (StringUtils.isEmpty(uid)) {
				retMap.put("resultCode", 10000);
				retMap.put("resultMessage", "查询错误");
				logger.info("invoke queryList4Page parameter error,uid can'be null,retMap:{}", retMap);
				return retMap;
			}
			String boxAreaId = (String) parameter.get("boxAreaId");
			List<String> areaLisStrs = new ArrayList<>();
			if (!StringUtils.isEmpty(boxAreaId)) {
				List<Area> listArea = areaService.findChildArea(Integer.valueOf(boxAreaId));
				for (Area area : listArea) {
					areaLisStrs.add(area.getId() + "");
				}
			} else {
				List<Area> listArea = areaService.findAreaByAdmin(uid);
				for (Area area : listArea) {
					areaLisStrs.add(area.getId() + "");
				}
			}
			Map<String, Object> parmMap = getBoxInfoParam(parameter);
			parmMap.put("boxAreaId", areaLisStrs);
			List<BoxInfo> list = boxInfoMapper.queryList4Page(parmMap);
			int count = boxInfoMapper.queryList4PageCount(parmMap);
			retMap.put("resultCode", 0);
			retMap.put("resultMessage", "成功");
			retMap.put("page", parameter.get("page") == null ? 1 : Integer.valueOf(parameter.get("page") + ""));
			retMap.put("pageSize", parameter.get("pageSize") == null ? 10 : Integer.valueOf(parameter.get("pageSize") + ""));
			retMap.put("total", count);
			retMap.put("data", list);
		} catch (Exception e) {
			logger.error("分页查询机顶盒信息接口错误", e);
			retMap.put("resultCode", 10005);
			retMap.put("resultMessage", "查询错误");
		}
		logger.info("end invoke queryList4Page parameter:{}", retMap);
		return retMap;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
	public Map<String, Object> queryBoxListForPage(Map<String, Object> parameter) {
		BoxInfo boxInfo = new BoxInfo();
		Map<String, Object> backMap = new HashMap<String, Object>();
		try {
			if (parameter.get("boxSerialnumber") != null && !"".equals(parameter.get("boxSerialnumber"))) {
				boxInfo.setBoxSerialnumber(parameter.get("boxSerialnumber").toString().trim());
			}
			// 生产商编号
			if (parameter.get("factoryName") != null && !"".equals(parameter.get("factoryName").toString().trim())) {
				boxInfo.setBoxFactoryCode(parameter.get("factoryName").toString().trim());
			}
			if (parameter.get("uId") != null && !"".equals(parameter.get("uId").toString().trim())) {
				List<com.cmiot.ams.domain.Area> userAreaList = areaService.findAreaByAdmin((String) parameter.get("uId"));
				if (userAreaList != null && userAreaList.size() > 0) {
					// 区域编号
					if (parameter.get("areaCode") != null && !"".equals(parameter.get("areaCode").toString().trim())) {
						List<com.cmiot.ams.domain.Area> areaList = areaService.findChildArea(Integer.parseInt((String) parameter.get("areaCode")));
						if (areaList != null && areaList.size() > 0) {
							StringBuffer sb = new StringBuffer();
							sb.append("(");
							for (int j = 0; j < userAreaList.size(); j++) {
								for (int i = 0; i < areaList.size(); i++) {
									if (userAreaList.get(j).getId().equals(areaList.get(i).getId())) {
										sb.append("," + areaList.get(i).getId());
										logger.info(" userAreaList " + userAreaList.get(j).getId() + " ~~areaList~~ " + areaList.get(i).getId());
									}
								}
							}
							sb.append(")");
							String par = sb.toString().replaceFirst(",", "");
							boxInfo.setBoxAreaId(par);
						}
					} else {
						StringBuffer sbNoArea = new StringBuffer();
						sbNoArea.append("(");
						for (int j = 0; j < userAreaList.size(); j++) {
							sbNoArea.append("," + userAreaList.get(j).getId());
						}
						sbNoArea.append(")");
						String par = sbNoArea.toString().replaceFirst(",", "");
						boxInfo.setBoxAreaId(par);
					}
				}
			}
			// 用户地址
			if (parameter.get("boxMacaddress") != null && !"".equals(parameter.get("boxMacaddress").toString().trim())) {
				boxInfo.setBoxMacaddress(parameter.get("boxMacaddress").toString().trim());
			}
			// 设备型号
			if (parameter.get("boxModel") != null && !"".equals(parameter.get("boxModel").toString().trim())) {
				logger.info("  BoxDeviceUuid  " + parameter.get("boxModel"));
				Map<String, Object> boxDevice = boxDeviceInfoMapper.selectByPrimaryKey(String.valueOf(parameter.get("boxModel")));
				if (null == boxDevice) {
					logger.info(" 查无: " + parameter.get("boxModel") + " 的设备型号!");
				} else {
					boxInfo.setBoxModel((String) boxDevice.get("boxModel"));
					logger.info(" 查寻: " + parameter.get("boxModel") + " 的设备型号,为: " + (String) boxDevice.get("boxModel"));
				}
			}
			// 固件版本
			if (parameter.get("boxVersion") != null && !"".equals(parameter.get("boxVersion").toString().trim())) {
				boxInfo.setBoxFirmwareUuid(parameter.get("boxVersion").toString().trim());
			}
			int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
			int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;
			PageHelper.startPage(page, pageSize);
			List<BoxBean> list = boxInfoMapper.queryBoxListForPage(boxInfo);
			if (null != list && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					BoxBean boxBean = list.get(i);
					try {
						if (boxBean.getBoxAreaId() == null || boxBean.getBoxAreaId().equals("")) {
							logger.info("BoxAreaId is null!");
						} else {
							com.cmiot.ams.domain.Area area = areaService.findAreaById(Integer.parseInt(boxBean.getBoxAreaId()));
							if (null != area) {
								list.get(i).setAreaName(area.getName());
							}
						}
						/*
						 * if(boxBean.getBoxFirmwareUuid()==null||boxBean.getBoxFirmwareUuid().equals("")){
						 * logger.info("BoxFirmwareUuid is null!");
						 * }else{
						 * BoxFirmwareInfo boxFirmwareInfo = boxFirmwareInfoMapper.selectByPrimaryKey(boxBean.getBoxFirmwareUuid());
						 * if(null != boxFirmwareInfo){
						 * list.get(i).setBoxVersion(boxFirmwareInfo.getFirmwareVersion());
						 * }else{
						 * list.get(i).setBoxVersion("");
						 * }
						 * }
						 *//*
							 * if(boxBean.getBoxFactoryCode()==null||boxBean.getBoxFactoryCode().equals("")){
							 * logger.info("getBoxFactoryCode is null!");
							 * }else{
							 * BoxFactoryInfo boxFactoryInfo = new BoxFactoryInfo();
							 * boxFactoryInfo.setFactoryCode(boxBean.getBoxFactoryCode());
							 * List<BoxFactoryInfo> factoryList = boxFactoryInfoMapper.queryList(boxFactoryInfo);
							 * if(null != factoryList){
							 * list.get(i).setBoxFactoryName(factoryList.get(0).getFactoryName());
							 * }
							 * }
							 */
						// 1未注册，2.在线 3.离线
						if (boxBean.getBoxStatus() == null || "".equals(boxBean.getBoxStatus())) {
							list.get(i).setBoxStatus("1");
							logger.info(i + " BoxStatus is null!");
						} else {
							if (boxBean.getBoxStatus().equals("2")) {
								if (boxBean.getBoxOnline() == null || boxBean.getBoxOnline().equals("")) {
									list.get(i).setBoxStatus("3");
									// 修改库中状态为离线
									BoxBean record = new BoxBean();
									record.setBoxUuid(boxBean.getBoxUuid());
									record.setBoxOnline(0);
									boxInfoMapper.updateByPrimaryKeySelective(record);
									logger.info(i + " BoxOnline is null!");
								} else {
									logger.info(i + " BoxOnline is " + boxBean.getBoxOnline());
									logger.info(i + "in redis BoxOnline is " + redisClientTemplate.get(Constant.BOX_ONLINE + boxBean.getBoxSerialnumber()));
									if (boxBean.getBoxOnline() == 1) {// 1：在线
										// 数据库中是在线，需要核对redis中状态是否为在线
										if ("1".equals(redisClientTemplate.get(Constant.BOX_ONLINE + boxBean.getBoxSerialnumber()))) {
											list.get(i).setBoxStatus("2");
										} else {
											list.get(i).setBoxStatus("3");
											// 修改库中状态为离线
											BoxBean record = new BoxBean();
											record.setBoxUuid(boxBean.getBoxUuid());
											record.setBoxOnline(0);
											boxInfoMapper.updateByPrimaryKeySelective(record);
										}

									}
									if (boxBean.getBoxOnline() == 0) {// 0：离线
										list.get(i).setBoxStatus("3");
									}
								}
							} else {
								list.get(i).setBoxStatus(boxBean.getBoxStatus());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			backMap.put("page", page);
			backMap.put("pageSize", pageSize);
			backMap.put("total", ((Page) list).getTotal());
			backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			backMap.put(Constant.MESSAGE, "机顶盒管理分页查询成功!");
			backMap.put(Constant.DATA, JSON.toJSON(list));
			return backMap;
		} catch (Exception e) {
			logger.info(exceptionInfo(e));
			backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			backMap.put(Constant.MESSAGE, "机顶盒管理分页查询异常!" + e.toString());
			backMap.put(Constant.DATA, null);
			return backMap;
		}
	}

	/**
	 * 小方法分离
	 * @param parameter
	 * @return
	 */
	private Map<String, Object> getBoxInfoParam(Map<String, Object> parameter) {
		Map<String, Object> paramMap = new HashMap<>();
		int page = parameter.get("page") == null ? 1 : Integer.valueOf(parameter.get("page") + "");
		int pageSize = parameter.get("pageSize") == null ? 10 : Integer.valueOf(parameter.get("pageSize") + "");
		String boxName = (String) parameter.get("boxName");
		String boxSerialnumber = (String) parameter.get("boxSerialnumber");
		String boxMacaddress = (String) parameter.get("boxMacaddress");
		String gatewayUuid = (String) parameter.get("gatewayUuid");
		String boxType = (String) parameter.get("boxType");
		String boxModel = (String) parameter.get("boxModel");
		String boxFactoryCode = (String) parameter.get("boxFactoryCode");
		String boxStatus = (String) parameter.get("boxStatus");
		paramMap.put("start", (page - 1) * pageSize);
		paramMap.put("end", page * pageSize);
		if (!StringUtils.isEmpty(boxName)) {
			paramMap.put("boxName", boxName);
		}
		if (!StringUtils.isEmpty(boxSerialnumber)) {
			paramMap.put("boxSerialnumber", boxSerialnumber);
		}
		if (!StringUtils.isEmpty(boxMacaddress)) {
			paramMap.put("boxMacaddress", boxMacaddress);
		}
		if (!StringUtils.isEmpty(gatewayUuid)) {
			paramMap.put("gatewayUuid", gatewayUuid);
		}
		if (!StringUtils.isEmpty(boxType)) {
			paramMap.put("boxType", boxType);
		}
		if (!StringUtils.isEmpty(boxModel)) {
			paramMap.put("boxModel", boxModel);
		}
		if (!StringUtils.isEmpty(boxFactoryCode)) {
			paramMap.put("boxFactoryCode", boxFactoryCode);
		}
		if (!StringUtils.isEmpty(boxStatus)) {
			paramMap.put("boxStatus", boxStatus);
		}
		return paramMap;
	}

	@Override
	public Map<String, Object> updateBoxInfo(Map<String, Object> parameter) {
		return null;
	}

	@Override
	public Map<String, Object> rebootBox(Map<String, Object> parameter) {
		logger.info("start invoke rebootBox parameter:{}", parameter);
		Map<String, Object> retmap = new HashMap<>();
		List<String> boxIds = (List<String>) parameter.get("boxIds");
		List<String> boxSnList = new ArrayList<>();
		if (boxIds == null || boxIds.isEmpty()) {
			logger.info("重启指令下发失败：boxIds参数为空");
			;
			retmap.put(Constant.CODE, RespCodeEnum.RC_1002.code());
			retmap.put(Constant.MESSAGE, "重启指令下发失败：boxIds参数为空");
			retmap.put(Constant.DATA, false);
			return retmap;
		}
		try {
			List<BoxInfo> bis = boxInfoService.queryListByIds(boxIds);
			for (BoxInfo boxInfo : bis) {
				String boxId = boxInfo.getBoxUuid();
				String serialNumber = "R-F-" + boxInfo.getBoxSerialnumber();// 添加前缀作为重启和恢复出厂在redis中的唯一key
				String str = redisClientTemplate.set(serialNumber, RebootEnum.STATUS_0.code(), "NX", "EX", rebootTimeOutBox);
				logger.info("机顶盒重启在redis添加key为:" + serialNumber + "的锁,返回的状态为:" + str);
				if (null == str) {
					boxIds.remove(boxId);
				} else {
					boxSnList.add(serialNumber);
				}
			}
			if (boxIds.size() == 0) {
				logger.info("机顶盒正在重启中，无需重复操作");
				retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retmap.put(Constant.MESSAGE, "机顶盒正在重启中，无需重复操作");
				retmap.put(Constant.DATA, true);
				return retmap;
			}
			Map<String, Object> map = new HashMap<>();
			// 批量下发指令给机顶盒
			map.put("boxIds", boxIds);
			map.put("methodName", "Reboot");
			Map<String, Object> result = boxInvokeInsService.executeBatch(map);
			if (result == null) {
				retmap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
				retmap.put(Constant.MESSAGE, "重启指令下发失败");
				retmap.put(Constant.DATA, false);
			} else {
				retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retmap.put(Constant.MESSAGE, "成功");
				retmap.put(Constant.DATA, true);
			}
			Map<String, Object> parameterLog = new HashMap<>();
			// 操作的数据内容
			parameterLog.put("content", JSON.toJSONString(boxIds));
			// 登录用户名称
			parameterLog.put("userName", parameter.get("userName"));
			// 类目ID(菜单ID)
			parameterLog.put("categoryMenu", CategoryEnum.BOX_MANAGER_SERVICE.name());
			// 具体的操作
			parameterLog.put("operation", "机顶盒重启");
			// 角色名称
			parameterLog.put("roleName", parameter.get("roleName"));
			// 类目名称
			parameterLog.put("categoryMenuName", CategoryEnum.BOX_MANAGER_SERVICE.description());
			// 日志类别
			parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
			logManagerService.recordOperationLog(parameterLog);
		} catch (Exception e) {
			logger.error("机顶盒重启指令下发网络连接异常", e.getMessage(), e.getCause());
			// 释放写入redis中的SN数据
			for (String str : boxSnList) {
				// 抛异常的时候释放锁
				redisClientTemplate.del(str);
			}

			retmap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retmap.put(Constant.MESSAGE, "重启指令下发网络连接异常");
			retmap.put(Constant.DATA, false);
		}
		logger.info("end invoke rebootBox result:{}", retmap);
		return retmap;

	}

	@Override
	public Map<String, Object> factoryResetBox(Map<String, Object> parameter) {
		logger.info("start invoke factoryResetBox parameter:{}", parameter);
		Map<String, Object> retmap = new HashMap<>();
		List<String> boxIds = (List<String>) parameter.get("boxIds");
		List<String> boxSnList = new ArrayList<>();
		if (boxIds == null || boxIds.isEmpty()) {
			logger.info("恢复出厂指令下发失败：boxIds参数为空");
			;
			retmap.put(Constant.CODE, RespCodeEnum.RC_1002.code());
			retmap.put(Constant.MESSAGE, "恢复出厂指令下发失败：boxIds参数为空");
			retmap.put(Constant.DATA, false);
			return retmap;
		}
		try {
			List<BoxInfo> bis = boxInfoService.queryListByIds(boxIds);
			for (BoxInfo boxInfo : bis) {
				String boxId = boxInfo.getBoxUuid();
				String serialNumber = "R-F-" + boxInfo.getBoxSerialnumber();// 添加前缀作为重启和恢复出厂在redis中的唯一key
				String str = redisClientTemplate.set(serialNumber, RebootEnum.STATUS_1.code(), "NX", "EX", factoryResetTimeOutBox);
				logger.info("机顶盒恢复出厂在redis添加key为:" + serialNumber + "的锁,返回的状态为:" + str);
				if (null == str) {
					boxIds.remove(boxId);
				} else {
					boxSnList.add(serialNumber);
				}
			}
			if (boxIds.size() == 0) {
				logger.info("机顶盒正在恢复出厂中，无需重复操作");
				retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retmap.put(Constant.MESSAGE, "机顶盒正在恢复出厂中，无需重复操作");
				retmap.put(Constant.DATA, true);
				return retmap;
			}
			Map<String, Object> map = new HashMap<>();
			// 批量下发指令给机顶盒
			map.put("boxIds", boxIds);
			map.put("methodName", "FactoryReset");
			Map<String, Object> result = boxInvokeInsService.executeBatch(map);
			if (result == null) {
				retmap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
				retmap.put(Constant.MESSAGE, "恢复出厂指令下发失败");
				retmap.put(Constant.DATA, false);
			} else {
				retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retmap.put(Constant.MESSAGE, "成功");
				retmap.put(Constant.DATA, true);
			}
			Map<String, Object> parameterLog = new HashMap<>();
			// 操作的数据内容
			parameterLog.put("content", JSON.toJSONString(boxIds));
			// 登录用户名称
			parameterLog.put("userName", parameter.get("userName"));
			// 类目ID(菜单ID)
			parameterLog.put("categoryMenu", CategoryEnum.BOX_MANAGER_SERVICE.name());
			// 具体的操作
			parameterLog.put("operation", "机顶盒恢复出厂设置");
			// 角色名称
			parameterLog.put("roleName", parameter.get("roleName"));
			// 类目名称
			parameterLog.put("categoryMenuName", CategoryEnum.BOX_MANAGER_SERVICE.description());
			// 日志类别
			parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
			logManagerService.recordOperationLog(parameterLog);
		} catch (Exception e) {
			logger.error("机顶盒恢复出厂指令下发网络连接异常", e.getMessage(), e.getCause());
			// 释放写入redis中的SN数据
			for (String str : boxSnList) {
				// 抛异常的时候释放锁
				redisClientTemplate.del(str);
			}

			retmap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retmap.put(Constant.MESSAGE, "恢复出厂指令下发网络连接异常");
			retmap.put(Constant.DATA, false);
		}
		logger.info("end invoke factoryResetBox result:{}", retmap);
		return retmap;
	}

	@Override
	public Map<String, Object> modFamilyAccountPwd(Map<String, Object> parameter) {
		logger.info("start invoke modFamilyAccountPwd parameter:{}", parameter);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String boxUuid = parameter.get("boxId") == null ? null : parameter.get("boxId").toString();
		if (boxUuid == null || "".equals(boxUuid)) {
			logger.info("修改机顶盒维护账号失败,boxId为空");
			returnMap.put(Constant.CODE, 10000);
			returnMap.put(Constant.MESSAGE, "boxId不能为空");
			returnMap.put(Constant.DATA, false);
			return returnMap;
		}
		BoxInfo boxInfo = boxInfoService.selectByPrimaryKey(boxUuid);
		String boxMacaddress = "";
		if (boxInfo != null) {
			boxMacaddress = boxInfo.getBoxMacaddress();
		}
		String username = parameter.get("account") == null ? null : parameter.get("account").toString();
		String password = parameter.get("password") == null ? null : parameter.get("password").toString();

		if (boxMacaddress == null || "".equals(boxMacaddress)) {
			logger.info("修改机顶盒维护账号失败,机顶盒MAC地址为空");
			returnMap.put(Constant.CODE, 10000);
			returnMap.put(Constant.MESSAGE, "机顶盒MAC地址不能为空");
			returnMap.put(Constant.DATA, false);
			return returnMap;
		}
		if (username == null || "".equals(username)) {
			logger.info("修改机顶盒维护账号失败,account为空");
			returnMap.put(Constant.CODE, 10000);
			returnMap.put(Constant.MESSAGE, "account不能为空");
			returnMap.put(Constant.DATA, false);
			return returnMap;
		}
		if (password == null || "".equals(password)) {
			logger.info("修改机顶盒维护账号失败,password为空");
			returnMap.put(Constant.CODE, 10000);
			returnMap.put(Constant.MESSAGE, "password不能为空");
			returnMap.put(Constant.DATA, false);
			return returnMap;
		}

		// 下发参数值给机顶盒修改，成功后更新数据库,返回处理结果
		String node = "InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount";
		List<ParameterValueStruct> paramList = new ArrayList<>();
		/*
		 * ParameterValueStruct<String> enable = new ParameterValueStruct<String>();
		 * enable.setName(node + ".Enable");
		 * enable.setValue("TRUE");
		 * enable.setReadWrite(true);
		 * enable.setValueType("boolean");
		 */
		ParameterValueStruct<String> uName = new ParameterValueStruct<String>();
		uName.setName(node + ".Username");
		uName.setValue(username);
		uName.setReadWrite(true);
		uName.setValueType("string");

		ParameterValueStruct<String> pwd = new ParameterValueStruct<String>();
		pwd.setName(node + ".Password");
		pwd.setValue(password);
		pwd.setReadWrite(true);
		pwd.setValueType("string");
		paramList.add(uName);
		paramList.add(pwd);
		try {
			if (boxInstructionMethodService.setParameterValue(boxMacaddress, paramList)) {
				BoxInfo bi = new BoxInfo();
				bi.setBoxUuid(boxInfo.getBoxUuid());
				bi.setBoxFamilyAccount(username);
				bi.setBoxFamilyPassword(password);
				if (parameter.get("roleName").equals("root")) {
					bi.setBoxAreaId(String.valueOf(parameter.get("areaId")));
				}
				boxInfoService.updateByPrimaryKeySelective(bi);
				returnMap.put(Constant.CODE, 0);
				returnMap.put(Constant.MESSAGE, "成功请求修改设备维护账号和密码");
				returnMap.put(Constant.DATA, true);
				return returnMap;
			} else {
				returnMap.put(Constant.CODE, -1);
				returnMap.put(Constant.MESSAGE, "下发指令修改账号密码失败");
				returnMap.put(Constant.DATA, false);
				return returnMap;
			}
		} catch (Exception e) {
			logger.error("修改机顶盒维护账号失败：", e);
			returnMap.put(Constant.CODE, -1);
			returnMap.put(Constant.MESSAGE, "下发指令修改失败,服务器内部错误");
			returnMap.put(Constant.DATA, false);
			return returnMap;
		}

	}

	// 检查excel表格是否填写完整
	private Map<String, Object> checkContent(List<BoxExcelContent> listBooks) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		for (int i = 0; i < listBooks.size(); i++) {
			BoxExcelContent gateWayExcelContent = listBooks.get(i);
			StringBuffer data = new StringBuffer("获取导入第" + i + "条数据----->");
			data.append("FactoryCode: ").append(gateWayExcelContent.getBoxInfoFactoryCode()).append("|");
			data.append("HardwareVersion: ").append(gateWayExcelContent.getBoxInfoHardwareVersion()).append("|");
			data.append("Macaddress: ").append(gateWayExcelContent.getBoxInfoMacaddress()).append("|");
			data.append("Model: ").append(gateWayExcelContent.getBoxInfoModel()).append("|");
			data.append("Version: ").append(gateWayExcelContent.getBoxInfoVersion()).append("|");
			data.append("SN: ").append(gateWayExcelContent.getBoxInfoSerialnumber());
			logger.info(data.toString());
			if (StringUtils.isEmpty(gateWayExcelContent.getBoxInfoFactoryCode()) || StringUtils.isEmpty(gateWayExcelContent.getBoxInfoHardwareVersion()) || StringUtils.isEmpty(gateWayExcelContent.getBoxInfoMacaddress())
					|| StringUtils.isEmpty(gateWayExcelContent.getBoxInfoModel()) || StringUtils.isEmpty(gateWayExcelContent.getBoxInfoVersion()) || StringUtils.isEmpty(gateWayExcelContent.getBoxInfoSerialnumber())) {
				if (StringUtils.isEmpty(gateWayExcelContent.getBoxInfoFactoryCode())) {
					retMap.put("index", i + 1);
					retMap.put("filed", "机顶盒厂家编码");
					return retMap;
				}
				if (StringUtils.isEmpty(gateWayExcelContent.getBoxInfoHardwareVersion())) {
					retMap.put("index", i + 1);
					retMap.put("filed", "硬件版本");
					return retMap;
				}
				if (StringUtils.isEmpty(gateWayExcelContent.getBoxInfoMacaddress())) {
					retMap.put("index", i + 1);
					retMap.put("filed", "MAC地址");
					return retMap;
				}
				if (StringUtils.isEmpty(gateWayExcelContent.getBoxInfoModel())) {
					retMap.put("index", i + 1);
					retMap.put("filed", "机顶盒型号");
					return retMap;
				}
				if (StringUtils.isEmpty(gateWayExcelContent.getBoxInfoVersion())) {
					retMap.put("index", i + 1);
					retMap.put("filed", "固件版本");
					return retMap;
				}
				if (StringUtils.isEmpty(gateWayExcelContent.getBoxInfoSerialnumber())) {
					retMap.put("index", i + 1);
					retMap.put("filed", "终端SN");
					return retMap;
				}
				if (StringUtils.isEmpty(gateWayExcelContent.getBoxInfoType())) {
					retMap.put("index", i + 1);
					retMap.put("filed", "机顶盒类型");
					return retMap;
				}
			}

		}
		return retMap;
	}

	// 根据厂商编码和设备类型查找设备
	private Map<String, Object> checkDevice(List<BoxExcelContent> listBooks) {
		Map<String, Object> retMap = new HashMap<String, Object>();

		for (int i = 0; i < listBooks.size(); i++) {
			BoxExcelContent boxExcelContent = listBooks.get(i);
			DeviceInfo deviceInfo = getDeviceInfo(boxExcelContent.getBoxInfoFactoryCode(), boxExcelContent.getBoxInfoModel());
			if (deviceInfo == null) {
				retMap.put("index", i + 1);
				retMap.put("gateway", boxExcelContent);
				return retMap;
			}
		}
		return retMap;
	}

	private DeviceInfo getDeviceInfo(String code, String model) {
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setDeviceFactory(code);
		deviceInfo.setDeviceModel(model);
		List<DeviceInfo> deviceInfoList = deviceInfoMapper.searchDeviceModel(deviceInfo);
		if (deviceInfoList.size() < 1) {
			return null;
		}
		return deviceInfoList.get(0);
	}

	// 根据设备查找固件 与网关的固件版本进行对比
	private Map<String, Object> checkFireware(List<BoxExcelContent> listBooks) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		for (int i = 0; i < listBooks.size(); i++) {
			Map<String, Object> params = new HashMap<String, Object>();
			BoxExcelContent boxExcelContent = listBooks.get(i);
			params.put("factoryCode", boxExcelContent.getBoxInfoFactoryCode());
			params.put("firmwareVersion", boxExcelContent.getBoxInfoVersion());
			params.put("boxModel", boxExcelContent.getBoxInfoModel());
			logger.info("checkFireware map param {}", params.toString());
			Map<String, Object> map = bxofirmwareInfoService.searchBoxFirmwareId(params);
			if (map != null && Integer.parseInt(map.get("resultCode").toString()) == 0) {
				if (map.get("data") != null) {
					List<BoxFirmwareInfo> infoList = (List<BoxFirmwareInfo>) map.get("data");
					if (infoList.isEmpty()) {
						retMap.put("index", i + 1);
						retMap.put("gateway", boxExcelContent);
						return retMap;
					}
				} else {
					retMap.put("index", i + 1);
					retMap.put("gateway", boxExcelContent);
					return retMap;
				}
			} else {
				retMap.put("index", i + 1);
				retMap.put("gateway", boxExcelContent);
				return retMap;
			}
		}
		return retMap;
	}

	private Map<String, Object> checkGatewayMac(List<BoxExcelContent> listBooks) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		for (int i = 0; i < listBooks.size(); i++) {
			BoxExcelContent boxExcelContent = (BoxExcelContent) listBooks.get(i);
			List<BoxInfo> boxInfoList = boxInfoMapper.selectBoxInfo(null);
			boolean checkExist = false;
			int macFlag = 0, snFlag = 0; // 密码是否已存在 0 不存在 1已存在
			logger.info(" GatewayMac: " + boxExcelContent.getBoxInfoMacaddress() + " GatewaySn: " + boxExcelContent.getBoxInfoSerialnumber());
			for (BoxInfo boxInfo : boxInfoList) {
				if (boxInfo.getBoxMacaddress().equals(boxExcelContent.getBoxInfoMacaddress())) {
					checkExist = true;
					macFlag = 1;
					break;
				}
				if (boxInfo.getBoxSerialnumber().equals(boxExcelContent.getBoxInfoSerialnumber())) {
					checkExist = true;
					snFlag = 1;
					break;
				}
			}
			if (checkExist) {
				retMap.put("index", Integer.valueOf(i + 1));
				retMap.put("gateway", boxExcelContent);
				retMap.put("macFlag", macFlag);
				retMap.put("snFlag", snFlag);
				return retMap;
			}
		}
		return retMap;
	}

	private boolean insertOrUpdateGateway(List<BoxExcelContent> listBooks) {
		List<BoxInfo> allDatas = new ArrayList<BoxInfo>();
		boolean flag = true;
		for (int k = 0; k < listBooks.size(); k++) {

			BoxExcelContent boxExcelContent = listBooks.get(k);
			BoxInfo boxInfo = new BoxInfo();
			// boxInfo.setBoxMacaddress(boxExcelContent.getBoxInfoMacaddress());
			// boxInfo = boxInfoMapper.selectGatewayInfo(boxInfo);

			// if (boxInfo == null) {
			// 不存在此网关 插入
			boxInfo = new BoxInfo();
			boxInfo.setBoxUuid(UniqueUtil.uuid());
			boxInfo.setBoxName("");
			boxInfo.setBoxSerialnumber(boxExcelContent.getBoxInfoSerialnumber());
			boxInfo.setBoxMacaddress(boxExcelContent.getBoxInfoMacaddress());
			boxInfo.setBoxType(boxExcelContent.getBoxInfoType());
			boxInfo.setBoxModel(boxExcelContent.getBoxInfoModel());
			boxInfo.setBoxFactoryCode(boxExcelContent.getBoxInfoFactoryCode());

			BoxDeviceInfo deviceInfo = new BoxDeviceInfo();
			deviceInfo.setBoxModel(boxExcelContent.getBoxInfoModel());
			deviceInfo.setFactoryCode(boxExcelContent.getBoxInfoFactory());
			List<Map<String, Object>> boxDeviceInfoList = boxDeviceInfoMapper.selectBoxDeviceInfo(deviceInfo);
			String deviceId = "";
			if (boxDeviceInfoList.size() > 0) {
				Map<String, Object> boxDeviceMap = boxDeviceInfoList.get(0);
				deviceId = String.valueOf(boxDeviceMap.get("id"));
			}
			logger.info(" DeviceId: " + deviceId);
			BoxFirmwareInfo firm = new BoxFirmwareInfo();
			firm.setDeviceId(deviceId);
			firm.setFirmwareVersion(boxExcelContent.getBoxInfoVersion());
			List<BoxFirmwareInfo> fireList = boxFirmwareInfoMapper.queryFirmwareInfo(firm);
			if (fireList.size() > 0) {
				boxInfo.setBoxFirmwareUuid(fireList.get(0).getId());
				logger.info(" BoxFirmwareUuid: " + fireList.get(0).getId());
			} else {
				logger.info("query firmwareUuid faild,param deviceId " + deviceId + "  Version " + boxExcelContent.getBoxInfoVersion() + "  Model " + boxExcelContent.getBoxInfoModel());
				flag = false;
				return flag;
			}

			boxInfo.setBoxHardwareVersion(boxExcelContent.getBoxInfoHardwareVersion());
			if (boxExcelContent.getBoxInfoAreaId() == null || "".equals(boxExcelContent.getBoxInfoAreaId())) {
				boxInfo.setBoxAreaId("");
			} else {
				boxInfo.setBoxAreaId(boxExcelContent.getBoxInfoAreaId());
			}
			boxInfo.setBoxMemo("");
			if (boxExcelContent.getBoxInfoIpaddress() == null || "".equals(boxExcelContent.getBoxInfoIpaddress())) {
				boxInfo.setBoxIpaddress("");
			} else {
				boxInfo.setBoxIpaddress(boxExcelContent.getBoxInfoIpaddress());
			}
			boxInfo.setBoxUrl("");
			boxInfo.setBoxConnectionrequesturl("");
			boxInfo.setBoxConnType("");
			boxInfo.setBoxDigestAccount("");
			boxInfo.setBoxDigestPassword("");
			boxInfo.setBoxFamilyAccount("");
			boxInfo.setBoxFamilyPassword("");
			boxInfo.setBoxFileUrl("");
			boxInfo.setBoxJoinTime(0);
			boxInfo.setBoxLastConnTime(0);
			boxInfo.setBoxStatus("1");
			allDatas.add(boxInfo);
			// } else {
			// // 已经导入的网关暂时不做处理
			// flag = false;
			// return flag;
			// }
		}
		try {
			if (flag) {
				// 批量导入数据
				boxInfoMapper.batchInsertBoxInfo(allDatas);
			}
		} catch (Exception e) {
			logger.info(exceptionInfo(e));
			flag = false;
			return flag;
		}
		return flag;
	}

	@Override
	public Map<String, Object> getParameterNames(Map<String, Object> parameter) {
		logger.info("======下发指令参数--->parameter:{}", parameter);
		Map<String, Object> backMap = new HashMap<String, Object>();
		String id = parameter.get("id").toString();
		if (StringUtils.isEmpty(id)) {
			backMap.put(Constant.CODE, 10000);
			backMap.put(Constant.MESSAGE, "id不能为空");
			backMap.put(Constant.DATA, null);
			return backMap;
		}
		String path = parameter.get("path").toString();
		if (StringUtils.isEmpty(path)) {
			backMap.put(Constant.CODE, 10000);
			backMap.put(Constant.MESSAGE, "path不能为空");
			backMap.put(Constant.DATA, null);
			return backMap;
		}
		BoxInfo boxInfo = boxInfoMapper.selectByPrimaryKey(id);
		if (boxInfo == null) {
			logger.info("获取机顶盒属性下发指令失败！机顶盒不存在，id:{}", id);
			backMap.put(Constant.CODE, -1);
			backMap.put(Constant.MESSAGE, "获取机顶盒属性下发指令失败");
			backMap.put(Constant.DATA, null);
			return backMap;
		}
		parameter.put("methodName", "GetParameterNames");
		// 查发指令
		Map<String, Object> result = null;
		try {
			boolean nextLevel = true;
			if ("false".equalsIgnoreCase(parameter.get("nextLevel").toString())) {
				nextLevel = false;
			}
			result = (Map<String, Object>) getParameterNames(id, path, nextLevel);
			if (result == null || result.get("requestId") == null || "".equals(result.get("requestId").toString())) {
				logger.info("获取机顶盒属性下发指令失败！");
				backMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
				backMap.put(Constant.MESSAGE, "获取机顶盒属性下发指令失败");
				backMap.put(Constant.DATA, null);
				return backMap;
			}
		} catch (Exception e) {
			logger.error("获取机顶盒属性下发指令出错！", e);
			backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			backMap.put(Constant.MESSAGE, "获取机顶盒属性下发指令出错");
			backMap.put(Constant.DATA, null);

			return backMap;
		}
		// 定时获取返回数据
		try {
			String requestId = (String) result.get("requestId");
			List list = getParameterNamePolling(requestId);
			backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			backMap.put(Constant.MESSAGE, "获取机顶盒属性功能");
			Object obj = toZTreeMap(list);
			backMap.put(Constant.DATA, obj);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取机顶盒属性出错", e);
			backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			backMap.put(Constant.MESSAGE, "获取机顶盒属性出错");
			backMap.put(Constant.DATA, null);
		}
		logger.info("end invoke getParameterNames,backMap:{}", backMap);
		return backMap;
	}

	@Override
	public Map<String, Object> getParameterValues(Map<String, Object> parameter) {
		Map<String, Object> backMap = new HashMap<String, Object>();
		logger.info("======下发指令参数--->,parameter:{}", parameter);
		// 查发指令
		Map<String, Object> result = null;
		try {
			String id = parameter.get("id").toString();
			String path = parameter.get("path").toString();
			if (StringUtils.isEmpty(id)) {
				backMap.put(Constant.CODE, 10000);
				backMap.put(Constant.MESSAGE, "id不能为空");
				backMap.put(Constant.DATA, null);
				return backMap;
			}
			if (StringUtils.isEmpty(path)) {
				backMap.put(Constant.CODE, 10000);
				backMap.put(Constant.MESSAGE, "path不能为空");
				backMap.put(Constant.DATA, null);
				return backMap;
			}
			BoxInfo boxInfo = boxInfoMapper.selectByPrimaryKey(id);
			if (boxInfo == null) {
				logger.info("获取机顶盒属性参数下发指令失败！机顶盒不存在，id:{}", id);
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "获取机顶盒属性参数下发指令失败");
				backMap.put(Constant.DATA, null);
				return backMap;
			}
			List<String> list = new ArrayList<String>();
			String[] paths = path.split(",");
			for (String ph : paths) {
				list.add(ph);
			}
			result = (Map<String, Object>) getParameterValues(id, list);

			if (result == null || result.get("requestId") == null || "".equals(result.get("requestId").toString())) {
				logger.info("获取机顶盒属性参数下发指令失败！");
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "获取机顶盒属性参数下发指令失败");
				backMap.put(Constant.DATA, null);

				return backMap;
			}
		} catch (Exception e) {
			logger.info("获取机顶盒属性参数下发指令出错！", e);
			backMap.put(Constant.CODE, 1);
			backMap.put(Constant.MESSAGE, "获取机顶盒属性参数下发指令出错");
			backMap.put(Constant.DATA, null);

			return backMap;
		}
		// 定时获取返回数据
		try {
			String requestId = (String) result.get("requestId");
			List<JSONObject> list = getParameterValuePolling(requestId);
			backMap.put(Constant.CODE, 0);
			backMap.put(Constant.MESSAGE, "获取机顶盒属性参数功能");
			// 把返回结果传化为Map结构
			Object obj = toParameterValueList(list);
			backMap.put(Constant.DATA, obj);
		} catch (Exception e) {
			logger.error("获取机顶盒属性参数出错", e);
			backMap.put(Constant.CODE, 1);
			backMap.put(Constant.MESSAGE, "获取机顶盒属性参数出错");
			backMap.put(Constant.DATA, null);
		}
		logger.info("end invoke getParameterValues,backMap:{}", backMap);
		return backMap;
	}

	@Override
	public Map<String, Object> setParameterValues(Map<String, Object> parameter) {
		Map<String, Object> backMap = new HashMap<String, Object>();
		logger.info("======下发指令参数--->，parameter:{}", parameter);
		boolean result = false;
		// 查发指令
		try {
			String id = parameter.get("id").toString();
			List listParam = (List) parameter.get("listS");
			String listS = JSONObject.toJSONString(listParam);
			if (StringUtils.isEmpty(id)) {
				backMap.put(Constant.CODE, 10000);
				backMap.put(Constant.MESSAGE, "id不能为空");
				backMap.put(Constant.DATA, null);
				return backMap;
			}
			if (StringUtils.isEmpty(listS)) {
				backMap.put(Constant.CODE, 10000);
				backMap.put(Constant.MESSAGE, "listS不能为空");
				backMap.put(Constant.DATA, null);
				return backMap;
			}
			BoxInfo boxInfo = boxInfoMapper.selectByPrimaryKey(id);
			if (boxInfo == null) {
				logger.info("设置机顶盒属性参数下发指令失败！机顶盒不存在，boxUuid:{}", id);
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "设置机顶盒属性参数下发指令失败");
				backMap.put(Constant.DATA, null);
				return backMap;
			}
			List<ParameterValueStruct> list = new ArrayList<>();
			try {
				list = JSON.parseArray(listS, ParameterValueStruct.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			result = boxInstructionMethodService.setParameterValue(boxInfo.getBoxMacaddress(), list);
		} catch (Exception e) {
			logger.info("设置机顶盒属性参数下发指令出错！", e);
			backMap.put(Constant.CODE, 0);
			backMap.put(Constant.MESSAGE, "设置机顶盒属性参数下发指令出错");
			backMap.put(Constant.DATA, null);

			return backMap;
		}
		// 定时获取返回数据
		try {
			Map<String, Object> parameterLog = new HashMap<>();
			// 操作的数据内容
			parameterLog.put("content", JSON.toJSONString(parameter));
			// 登录用户名称
			parameterLog.put("userName", parameter.get("userName"));
			// 类目ID(菜单ID)
			parameterLog.put("categoryMenu", CategoryEnum.BOX_MANAGER_SERVICE.name());
			// 具体的操作
			parameterLog.put("operation", "机顶盒参数设置");
			// 角色名称
			parameterLog.put("roleName", parameter.get("roleName"));
			// 类目名称
			parameterLog.put("categoryMenuName", CategoryEnum.BOX_MANAGER_SERVICE.description());
			// 日志类别
			parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
			logManagerService.recordOperationLog(parameterLog);
			if (!result) {
				logger.info("设置机顶盒属性参数下发指令失败！");
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "设置机顶盒属性参数下发指令失败");
				backMap.put(Constant.DATA, null);
			} else {
				backMap.put(Constant.CODE, 0);
				backMap.put(Constant.MESSAGE, "机顶盒属性参数设置成功");
				backMap.put(Constant.DATA, null);
			}
		} catch (Exception e) {
			logger.error("设置机顶盒属性参数出错", e);
			backMap.put(Constant.CODE, 1);
			backMap.put(Constant.MESSAGE, "设置机顶盒属性参数出错");
			backMap.put(Constant.DATA, null);
		}
		logger.info("end invoke setParameterValues,backMap:{}", backMap);
		return backMap;
	}

	/**
	 * 获取参数名称公共方法
	 *
	 * @param id
	 * @param path
	 * @param nextLevel
	 * @return
	 */
	public Object getParameterNames(String id, String path, boolean nextLevel) throws Exception {
		logger.info("传入getParameterNames参数" + id + " " + path + " " + nextLevel);
		Map<String, Object> map = new HashMap<>();
		map.put("boxUuid", id);
		map.put("methodName", "GetParameterNames");
		map.put("parameterPath", path);
		map.put("nextLevel", nextLevel);
		Map<String, Object> result = boxInvokeInsService.executeOne(map);
		return result;
	}

	/**
	 * 功能:根据requestId循环获取网关属性
	 *
	 * @param requestId
	 * @return
	 */
	public List<JSONObject> getParameterNamePolling(String requestId) {
		Map<String, String> map = null;
		for (int i = 0; i < Constant.COUNT_CYCLE; i++) {
			map = instructionsService.getInstructionsInfo(requestId);
			String status = map.get("status");
			if ("1".equalsIgnoreCase(status)) {
				break;// 跳出当前for循环
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (map != null && !map.isEmpty()) {
			if ("1".equalsIgnoreCase(map.get("status").toString())) {
				String json = map.get("json");
				JSONObject jsonObject = JSON.parseObject(json);
				List<JSONObject> list = (List<JSONObject>) jsonObject.get("parameterList");
				return list;
			}
		}
		return null;
	}

	/**
	 * 功能:把返回的网关属性传化为ztree节点treeMap的形式
	 */
	public List<Map<String, Object>> toZTreeMap(List<JSONObject> list) {
		List backList = new ArrayList();
		if (list != null && list.size() > 0) {
			for (JSONObject jsonObject : list) {
				if (jsonObject.getString("name").endsWith(".")) {
					Map map = new HashMap();
					map.put("isParent", true);
					String name = jsonObject.getString("name");
					String path = name.substring(0, name.lastIndexOf("."));
					String nodeName = path.substring(path.lastIndexOf(".") + 1, path.length());
					map.put("name", nodeName);
					map.put("readWrite", jsonObject.getString("writable"));
					backList.add(map);
				} else {
					Map map = new HashMap();
					map.put("isParent", false);

					String path = jsonObject.getString("name");
					String nodeName = path.substring(path.lastIndexOf(".") + 1, path.length());
					map.put("name", nodeName);
					map.put("readWrite", jsonObject.getString("writable"));
					backList.add(map);
				}
			}
		}
		return backList;
	}

	/**
	 * 获取参数值
	 *
	 * @param id
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public Object getParameterValues(String id, List<String> list) throws Exception {
		logger.info("传入getParameterNames参数:" + id + " " + list);
		Map<String, Object> map = new HashMap<>();
		map.put("boxUuid", id);
		map.put("methodName", "GetParameterValues");
		map.put("parameterList", list);
		Map<String, Object> result = boxInvokeInsService.executeOne(map);
		return result;
	}

	/**
	 * 功能:根据requestId循环获取网关属性参数
	 *
	 * @param requestId
	 * @return
	 */
	public List<JSONObject> getParameterValuePolling(String requestId) {
		Map<String, String> map = null;
		for (int i = 0; i < Constant.COUNT_CYCLE; i++) {
			map = instructionsService.getInstructionsInfo(requestId);
			String status = map.get("status");
			if ("1".equalsIgnoreCase(status)) {
				break;// 跳出当前for循环
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (map != null && !map.isEmpty()) {
			if ("1".equalsIgnoreCase(map.get("status").toString())) {
				String json = map.get("json");
				JSONObject jsonObject = JSON.parseObject(json);
				JSONObject jsonParameterList = (JSONObject) jsonObject.get("parameterList");
				if (jsonParameterList != null && jsonParameterList.size() > 0) {
					List<JSONObject> list = (List<JSONObject>) jsonParameterList.get("parameterValueStructs");
					return list;
				}
			}
		}
		return null;
	}

	/**
	 * 功能:把返回的网关属性参数转化为list
	 */
	public List<Map<String, Object>> toParameterValueList(List<JSONObject> list) {
		List backList = new ArrayList();
		if (list != null && list.size() > 0) {
			for (JSONObject jsonObject : list) {
				if (jsonObject != null) {
					Map map = new HashMap();
					map.put("valueType", jsonObject.getString("valueType"));
					map.put("name", jsonObject.getString("name"));
					map.put("readWrite", jsonObject.get("readWrite"));
					map.put("value", jsonObject.get("value"));

					backList.add(map);
				}
			}
		}
		return backList;
	}

	@Override
	public Map<String, Object> queryBoxBaseInfo(Map<String, Object> parameter) {
		Map<String, Object> retmap = new HashMap<>();
		String boxUuid = parameter.get("boxId") == null ? null : parameter.get("boxId").toString();
		if (boxUuid == null || "".equals(boxUuid)) {
			retmap.put(Constant.CODE, 10000);
			retmap.put(Constant.MESSAGE, "boxUuid不能为空");
			return retmap;
		}
		BoxInfo boxInfo = new BoxInfo();
		boxInfo.setBoxUuid(boxUuid);
		BoxInfo data = boxInfoMapper.selectGatewayInfo(boxInfo);
		if (data != null) {
			String areaId = data.getBoxAreaId();
			String areaName = "";
			try {
				com.cmiot.ams.domain.Area area = areaService.findAreaById(Integer.parseInt(areaId));
				areaName = area.getName();
			} catch (Exception e) {
			}
			String account = data.getBoxFamilyAccount();
			String password = data.getBoxFamilyPassword();

			String boxMacaddress = data.getBoxMacaddress();
			Map<String, Object> dataMap = new HashMap<>();

			dataMap.put("areaId", areaId);
			dataMap.put("areaName", areaName);
			dataMap.put("account", account);
			dataMap.put("password", password);
			dataMap.put("boxMacaddress", boxMacaddress);
			retmap.put(Constant.CODE, 0);
			retmap.put(Constant.MESSAGE, "返回机顶盒信息成功");
			retmap.put(Constant.DATA, dataMap);
			return retmap;
		} else {
			retmap.put(Constant.CODE, -1);
			retmap.put(Constant.MESSAGE, "无机顶盒信息返回");
			return retmap;
		}
	}

	@Override
	public Map<String, Object> queryBoxDetail(Map<String, Object> map) {
		Map<String, Object> backMap = new HashMap<String, Object>();
		try {
			BoxDetail boxDetail = new BoxDetail();
			String bxoUuid = map.get("boxUuid").toString();

			BoxInfo boxInfo = boxInfoService.selectByPrimaryKey(bxoUuid);

			HardwareAblity hardwareAblity = hardwareAblityService.selectByGatewayInfoUuid(boxInfo.getGatewayUuid());

			if (hardwareAblity != null) {
				boxDetail.setHardwareAblityUuid(hardwareAblity.getHardwareAblityUuid());
				boxDetail.setHardwareAblityLanCount(hardwareAblity.getHardwareAblityLanCount());
				boxDetail.setHardwareAblityUsbCount(hardwareAblity.getHardwareAblityUsbCount());
				if (hardwareAblity.getHardwareAblitySupportWifi()) {
					boxDetail.setHardwareAblitySupportWifi("是");
				} else {
					boxDetail.setHardwareAblitySupportWifi("否");
				}
				boxDetail.setHardwareAblityWifiLoc(hardwareAblity.getHardwareAblityWifiLoc());
				boxDetail.setHardwareAblityWifiCount(hardwareAblity.getHardwareAblityWifiCount());
				boxDetail.setHardwareAblityWifiSize(hardwareAblity.getHardwareAblityWifiSize());
				boxDetail.setHardwareAblitySupportWifi24ghz(hardwareAblity.getHardwareAblitySupportWifi24ghz());
				boxDetail.setHardwareAblitySupportWifi58ghz(hardwareAblity.getHardwareAblitySupportWifi58ghz());
				boxDetail.setHardwareAblityIpv4v6(hardwareAblity.getHardwareAblityIpv4v6());
			}

			if (boxInfo != null) {
				boxDetail.setBoxInfoUuid(boxInfo.getGatewayUuid());
				boxDetail.setBoxInfoType(boxInfo.getBoxType());
				boxDetail.setBoxInfoModel(boxInfo.getBoxModel());

				if (boxInfo.getBoxFirmwareUuid() == null || boxInfo.getBoxFirmwareUuid().equals("")) {
					logger.info("BoxFirmwareUuid is null!");
				} else {
					BoxFirmwareInfo boxFirmwareInfo = boxFirmwareInfoMapper.selectByPrimaryKey(boxInfo.getBoxFirmwareUuid());
					if (null != boxFirmwareInfo) {
						boxDetail.setBoxInfoVersion(boxFirmwareInfo.getFirmwareVersion());
					} else {
						boxDetail.setBoxInfoVersion("");
					}
				}

				boxDetail.setBoxInfoSerialnumber(boxInfo.getBoxSerialnumber());
				boxDetail.setBoxInfoHardwareVersion(boxInfo.getBoxHardwareVersion());

				// 1未注册，2.在线 3.离线
				if (boxInfo.getBoxStatus() == null || "".equals(boxInfo.getBoxStatus())) {
					boxDetail.setBoxInfoStatus("1");
					logger.info(" BoxStatus is null!");
				} else {
					if (boxInfo.getBoxStatus().equals("2")) {
						if (boxInfo.getBoxOnline() == null || boxInfo.getBoxOnline().equals("")) {
							boxDetail.setBoxInfoStatus("3");
							// 修改库中状态为离线
							BoxBean record = new BoxBean();
							record.setBoxUuid(boxInfo.getBoxUuid());
							record.setBoxOnline(0);
							boxInfoMapper.updateByPrimaryKeySelective(record);
							logger.info(" BoxOnline is null!");
						} else {
							logger.info(" BoxOnline is " + boxInfo.getBoxOnline());
							if (boxInfo.getBoxOnline() == 1) {// 1：在线
								// 数据库中是在线，需要核对redis中状态是否为在线
								if ("1".equals(redisClientTemplate.get(Constant.BOX_ONLINE + boxInfo.getBoxSerialnumber()))) {
									boxDetail.setBoxInfoStatus("2");
								} else {
									boxDetail.setBoxInfoStatus("3");
									// 修改库中状态为离线
									BoxBean record = new BoxBean();
									record.setBoxUuid(boxInfo.getBoxUuid());
									record.setBoxOnline(0);
									boxInfoMapper.updateByPrimaryKeySelective(record);
								}

							}
							if (boxInfo.getBoxOnline() == 0) {// 0：离线
								boxDetail.setBoxInfoStatus("3");
							}
						}
					} else {
						boxDetail.setBoxInfoStatus(boxInfo.getBoxStatus());
					}
				}
				boxDetail.setBoxInfoIpaddress(boxInfo.getBoxIpaddress());
				boxDetail.setBoxInfoMacaddress(boxInfo.getBoxMacaddress());
				boxDetail.setBoxInfoJoinTime(boxInfo.getBoxJoinTime());
				boxDetail.setBoxInfoLastConnTime(boxInfo.getBoxLastConnTime());
				boxDetail.setBoxUrl(boxInfo.getBoxUrl());
				boxDetail.setBoxConnType(boxInfo.getBoxConnType());
				boxDetail.setBoxFileUrl(boxInfo.getBoxFileUrl());
				boxDetail.setBoxConnectionrequesturl(boxInfo.getBoxConnectionrequesturl());
				boxDetail.setBoxFamilyAccount(boxInfo.getBoxFamilyAccount());
				boxDetail.setBoxFamilyPassword(boxInfo.getBoxFamilyPassword());
				boxDetail.setBoxDigestAccount(boxInfo.getBoxDigestAccount());
				boxDetail.setBoxDigestPassword(boxInfo.getBoxDigestPassword());
			}
			BoxFactoryInfo factory = new BoxFactoryInfo();
			List<BoxFactoryInfo> factoryList = boxFactoryInfoMapper.queryList(factory);
			boxDetail.setBoxInfoFactory("");
			for (BoxFactoryInfo factoryMap2 : factoryList) {
				if (factoryMap2.getFactoryCode().equals(boxInfo.getBoxFactoryCode())) {
					boxDetail.setBoxInfoFactory(factoryMap2.getFactoryName());
					break;
				}
			}
			backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			backMap.put(Constant.MESSAGE, "机顶盒详情查询功能");
			backMap.put(Constant.DATA, JSON.toJSON(boxDetail));
			return backMap;
		} catch (Exception e) {
			logger.info(exceptionInfo(e));
			backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			backMap.put(Constant.MESSAGE, "机顶盒详情查询出错");
			backMap.put(Constant.DATA, null);
			return backMap;
		}
	}

	@Override
	public Map<String, Object> getAttribute(Map<String, Object> map) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String boxUuid = map.get("id") == null ? null : map.get("id").toString();
		String objectName = map.get("pathName") == null ? null : map.get("pathName").toString();
		if (StringUtils.isEmpty(boxUuid)) {
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "id不能为空");
			return returnMap;
		}
		if (StringUtils.isEmpty(objectName)) {
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "pathName不能为空");
			return returnMap;
		}
		BoxInfo boxInfo = boxInfoService.selectByPrimaryKey(boxUuid);
		String boxMacaddress = "";
		if (boxInfo != null) {
			boxMacaddress = boxInfo.getBoxMacaddress();
		}

		if (StringUtils.isEmpty(boxMacaddress)) {
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "机顶盒MAC不存在");
			return returnMap;
		}
		List<String> attrList = new ArrayList<>();
		attrList.add(0, objectName);
		try {
			Map<String, Object> resultData = boxInstructionMethodService.getParameterAttributes(boxMacaddress, attrList);
			logger.info(" 查询节点属性指令返回值: " + resultData);
			if (resultData == null || resultData.isEmpty()) {
				returnMap.put("resultCode", -1);
				returnMap.put(Constant.MESSAGE, "查询节点属性失败，机顶盒返回 : " + resultData);
				return returnMap;
			} else {
				List dataList = new ArrayList<>();
				for (Map.Entry entry : resultData.entrySet()) {
					Map<String, Object> dataMap = new HashMap<>();
					dataMap.put("Name", entry.getKey());
					Map valueMap = (Map) entry.getValue();
					if (null != valueMap) {
						dataMap.put("Notification", valueMap.get("Notification"));
						String[] array = (String[]) valueMap.get("AccessList");
						List<String> accessList = new ArrayList<>();
						for (String a : array) {
							accessList.add(a);
						}
						dataMap.put("AccessList", accessList);
					}
					dataList.add(dataMap);
				}
				returnMap.put("resultCode", 0);
				returnMap.put(Constant.MESSAGE, "成功查询节点属性");
				returnMap.put("data", dataList);
				return returnMap;
			}
		} catch (Exception e) {
			logger.error("查询机顶盒节点属性失败，e:{}", e);
			returnMap.put("resultCode", -1);
			returnMap.put(Constant.MESSAGE, "查询节点属性失败");
			return returnMap;
		}
	}

	@Override
	public Map<String, Object> setAttribute(Map<String, Object> map) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String boxUuid = map.get("id") == null ? null : map.get("id").toString();
		if (StringUtils.isEmpty(boxUuid)) {
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "id不能为空");
			return returnMap;
		}

		List<Map<String, Object>> detailList = (List<Map<String, Object>>) map.get("listS");
		if (detailList == null || detailList.size() == 0) {
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "listS不能为空");
			return returnMap;
		}
		BoxInfo boxInfo = boxInfoService.selectByPrimaryKey(boxUuid);
		String boxMacaddress = "";
		if (boxInfo != null) {
			boxMacaddress = boxInfo.getBoxMacaddress();
		}

		if (StringUtils.isEmpty(boxMacaddress)) {
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "机顶盒MAC不存在");
			return returnMap;
		}
		List<SetParameterAttributesStruct> setParamAttrList = new ArrayList<>();
		for (Map<String, Object> nodeValueMap : detailList) {
			SetParameterAttributesStruct setParam = new SetParameterAttributesStruct();
			List<String> accessListP = (List<String>) nodeValueMap.get("AccessList");
			logger.info(" accessListP is " + accessListP);
			if (accessListP == null && (nodeValueMap.get("Notification").equals("") || nodeValueMap.get("Notification") == null)) {
				returnMap.put("resultCode", -1);
				returnMap.put(Constant.MESSAGE, "修改节点属性指令失败 : Notification 或 AccessList 都为空值!");
				return returnMap;
			}
			if (accessListP != null) {
				setParam.setAccesslist(accessListP);
				setParam.setAccessListChange(true);
			}
			logger.info(" accessListP " + accessListP.size());
			setParam.setName(String.valueOf(nodeValueMap.get("pathName")));
			setParam.setNotification(Integer.parseInt(String.valueOf(nodeValueMap.get("Notification"))));
			setParam.setNotificationChange(true);
			setParamAttrList.add(setParam);
		}
		if (setParamAttrList.size() > 0) {
			for (int l = 0; l < setParamAttrList.size(); l++) {
				logger.info(" pathName " + setParamAttrList.get(l).getName());
				logger.info(" Notification " + setParamAttrList.get(l).getNotification());
				logger.info(" AccessList " + setParamAttrList.get(l).getAccessList());
			}
		} else {
			logger.info(" setParamAttrList is null! ");
		}

		try {
			Boolean opeResult = boxInstructionMethodService.SetParameterAttributes(boxMacaddress, setParamAttrList);
			logger.info(" 修改节点属性返回值: " + opeResult);
			if (!opeResult) {
				returnMap.put("resultCode", -1);
				returnMap.put(Constant.MESSAGE, "修改节点属性失败，机顶盒返回 : " + opeResult);
				return returnMap;
			} else {
				returnMap.put("resultCode", 0);
				returnMap.put(Constant.MESSAGE, "修改节点属性属性成功");
				returnMap.put("data", opeResult);
				return returnMap;
			}
		} catch (Exception e) {
			logger.error("修改机顶盒属性失败,e:{}", e);
			returnMap.put("resultCode", -1);
			returnMap.put(Constant.MESSAGE, "修改节点属性失败");
			return returnMap;
		}
	}

	public String exceptionInfo(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString().toLowerCase();
	}

	public Map<String, Object> reutrnMap(String resultCode, String resultMsg, boolean data) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put(Constant.CODE, resultCode);
		returnMap.put(Constant.MESSAGE, resultMsg);
		returnMap.put(Constant.DATA, Boolean.valueOf(data));
		return returnMap;
	}

}
