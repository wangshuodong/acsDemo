package com.cmiot.rms.common.constant;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author xukai
 *         Date 2016/1/27
 */
public class Constant {
	public static final String SESSION_LOGIN_ACCOUNT = "session_login_account";

	public static final String SEPARATOR = "_";

	public static final String ID_SEPARATOR = "@";

	public static final String DEVICE_URL_PATH = "/system/device/deviceList.htm";

	public static final String GATEWAY_URL_PATH = "/system/device/gatewayList.htm";

	public static final String FIRM_URL_PATH = "/system/upgradeTask/firmwareUploadList.htm";

	public static final String UPTASK_URL_PATH = "/system/upgradeTask/upgradeTaskList.htm";

	public static final String GATEWAY_EXCEL_TEMPLATE_PATH = "/gatesTemplate/gatewayTemplate.xls";

	public static final String API_OUTER_BEAN = "com.cmiot.rms.api.outer.bean.";

	public static String EXTERNAL_IPADDRESS = "ExternalIPAddress";

	public static final String CODE = "resultCode";

	public static final String MESSAGE = "resultMsg";

	public static final String DATA = "data";

	public static final String TOTAL = "total";

	public static final String PAGE = "page";

	public static final String PAGESIZE = "pageSize";


	/**
	 * 循环休眠时间2秒
	 */
	public static final int SLEEP_TIME = 2000;
	/**
	 * 循环次数
	 */
	public static final int COUNT_CYCLE = 10;

	public static final String SERVICELIST_TR069 = "TR069";

	public static final String SERVICELIST_INTERNET = "INTERNET";

	public static final String SERVICELIST_VOIP = "VOIP";

	public static final String SERVICELIST_OTHER = "OTHER";

	/**
	 * 消息寄存器
	 */
	public static ConcurrentHashMap<String, Object> MEMORY_VALUE = new ConcurrentHashMap<>();

	/**
	 * 横向接口常量
	 */
	public static final String ID = "ID";
	public static final String CMDTYPE = "CmdType";
	public static final String SEQUENCEID = "SequenceId";
	public static final String PARAMETER = "Parameter";
	public static final String MAC = "MAC";
	public static final String DEVICEMAC = "DeviceMAC";
	public static final String DEVICEMACS = "DeviceMACS";
	public static final String RESULTDATA = "ResultData";
	public static final String RESULT = "Result";
	public static final String STATUS = "Status";
	public static final String FAILREASON = "FailReason";
	public static final String RPCMETHOD = "RPCMethod";
	public static final String FAILEDCODE = "-1";

	/**
	 * 工单北向接口常量
	 */
	public static final String PBOSS_RESULT = "Result";
	public static final String PBOSS_CMDTYPE = "CmdType";
	public static final String PBOSS_RESULTDATA = "ResultData";
	public static final String PBOSS_FAILREASON = "FailReason";
	public static final String PBOSS_PARAMETER = "Parameter";
	public static final int PBOSS_SUCCESS = 0;//成功
	public static final int PBOSS_FAIL = -1;//信息处理失败
	public static final int PBOSS_UNMATCH = -2;//逻辑不匹配
	public static final int PBOSS_VARIFICATION_FAIL = -3;//字段验证不通过
	public static final int PBOSS_PARAM_LOST = -4;//接口入参数据缺失
	
	//机顶盒在线状态key前缀
	public static final String BOX_ONLINE = "BOX-ONLINE-";

	public static final String ERROR_PARAMS = "errorParams";

	//新装
	public static final String OPERATIONTYPE_Z = "Z";
	//拆除
	public static final String OPERATIONTYPE_C = "C";
	//修改
	public static final String OPERATIONTYPE_X = "X";

	public static final int PBOSS_STANDARD_FAIL = 1;//信息处理失败
}
