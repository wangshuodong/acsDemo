package com.cmiot.rms.common.constant;

/**
 * 诊断参数
 * Created by wangzhen on 2016/4/18.
 */
public class ConstantDiagnose {

	// ===========================GPON 上行状态和统计父级节点==============================//
	public static final String WANDEVICE = "InternetGatewayDevice.WANDevice.";

	// ===========================LAN口获信息节点==============================//
	public static final String LANDEVICE = "InternetGatewayDevice.LANDevice.";

	// ===========================CPU 和 内存占用比==============================//
	/**
	 * CPU占用比
	 */
	public static final String CPU_USAGE = "InternetGatewayDevice.DeviceInfo.ProcessStatus.CPUUsage";
	/**
	 * 内存占用比
	 */
	public static final String RAM_USAGE = "InternetGatewayDevice.DeviceInfo.X_CMCC_RAMUsage";
	/**
	 * 总内存大小，单位Kbytes
	 */
	public static final String RAM_TOTAL = "InternetGatewayDevice.DeviceInfo.MemoryStatus.Total";
	/**
	 * 可用内存大小，单位Kbytes
	 */
	public static final String RAM_FREE = "InternetGatewayDevice.DeviceInfo.MemoryStatus.Free";

	// ===========================拨号检查结果==============================//
	/**
	 * 仿真结果
	 */
	public static final String EMULATOR_RESULT = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.Result";

	// ===========================DHCP检查结果==============================//
	/**
	 * 仿真获取的IP地址
	 */
	public static final String IPOEDIAGNOSTICS_LOCALADDRESS = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.LocalAddress";
	/**
	 * 模拟用户的MAC
	 */
	public static final String IPOEDIAGNOSTICS_USERMAC = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.UserMac";
	/**
	 * 仿真状态
	 */
	public static final String IPOEDIAGNOSTICS_DiagnosticsState = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.DiagnosticsState";

	// ===========================PING检查结果==============================//
	/**
	 * Ping测试的主机名或主机地址
	 */
	public static final String IPPINGDIAGNOSTICS_HOST = "InternetGatewayDevice.IPPingDiagnostics.Host";
	/**
	 * 测试过程Ping包个数
	 */
	public static final String IPPINGDIAGNOSTICS_NUMBEROFREPETITIONS = "InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions";

	public static final String IPPINGDIAGNOSTICS_TIMEOUT = "InternetGatewayDevice.IPPingDiagnostics.Timeout";
	/**
	 * 测试过程Ping包返回时延限制（单位：ms）
	 */
	public static final String IPPINGDIAGNOSTICS_DATABLOCKSIZE = "InternetGatewayDevice.IPPingDiagnostics.DataBlockSize";
	/**
	 * 测试过程Ping包成功个数
	 */
	public static final String IPPINGDIAGNOSTICS_SUCCESSCOUNT = "InternetGatewayDevice.IPPingDiagnostics.SuccessCount";
	/**
	 * 测试过程Ping包失败个数
	 */
	public static final String IPPINGDIAGNOSTICS_FAILURECOUNT = "InternetGatewayDevice.IPPingDiagnostics.FailureCount";
	/**
	 *
	 */
	public static final String IPPINGDIAGNOSTICS_AVERAGERESPONSETIME = "InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime";
	/**
	 * 测试过程Ping包平均返回时延（单位ms）
	 */
	public static final String IPPINGDIAGNOSTICS_MINIMUMRESPONSETIME = "InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime";
	/**
	 * 测试过程Ping包最大返回时延（单位ms）
	 */
	public static final String IPPINGDIAGNOSTICS_MAXIMUMRESPONSETIME = "InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime";
	/**
	 * WAN连接（TR069节点全路径）只针对路由WAN
	 */
	public static final String IPPINGDIAGNOSTICS_INTERFACE = "InternetGatewayDevice.IPPingDiagnostics.Interface";
	/**
	 * 诊断状态
	 */
	public static final String IPPINGDIAGNOSTICS_DIAGNOSTICSSTATE = "InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState";
	/**
	 * 维护账号管理
	 */
	public static final String TELECOMACCOUNT_PASSWORD = "InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Password";
	/**
	 * 联网功能参数模型 : LAN口
	 */
	public static final String LANETHERNETINTERFACECONFIG = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
	/**
	 * 联网功能参数模型 : WLAN口
	 */
	public static final String WLANCONFIGURATION = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.";
	/**
	 * 联网功能参数模型 : VoIp
	 */
	public static final String VoIp = "InternetGatewayDevice.Services.VoiceService.";
	/**
	 * 告警查询
	 */
	public static final String ALARM_NUMBER = "InternetGatewayDevice.DeviceInfo.X_CMCC_Alarm.AlarmNumber";
	/**
	 * 认证PASSWORD
	 */
	public static final String USERINFO_PASSWORD = "InternetGatewayDevice.X_CMCC_UserInfo.Password";

	// ===========================系统信息==============================//
	/**
	 * 系统上电时间，单位为秒
	 */
	public static final String SYSTEM_UP_TIME = "InternetGatewayDevice.DeviceInfo.UpTime";
	/**
	 * PON注册上线时间，单位为秒
	 */
	public static final String PON_UP_TIME = "InternetGatewayDevice.DeviceInfo.X_CMCC_PONUpTime";
	/**
	 * 网关命名，方便用户自己识别设备。
	 */
	public static final String GATEWAY_ALIAS = "InternetGatewayDevice.DeviceInfo.X_CMCC_CustomiseName";
	/**
	 * CPU型号,主芯片厂商及主芯片完整型号
	 */
	public static final String CPU_CLASS = "InternetGatewayDevice.DeviceInfo.X_CMCC_CPUClass";

	// ============================基本参数==========================//
	/**
	 * Digest 账号
	 */
	public static final String MANAGEMENT_SERVER_USERNAME = "InternetGatewayDevice.ManagementServer.Username";
	/**
	 * Digest 密码
	 */
	public static final String MANAGEMENT_SERVER_PASSWORD = "InternetGatewayDevice.ManagementServer.Password";
	/**
	 * 网关登录 账号
	 */
	public static final String DEVICE_INFO_ACCOUNT_USERNAME = "InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Username";
	/**
	 * 网关登录 密码
	 */
	public static final String DEVICE_INFO_ACCOUNT_PASSWORD = "InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Password";

	// ============================DHCP仿真参数==========================//
	/**
	 * 仿真状态
	 */
	public static final String DHCP_DIAGNOSTICSSTATE = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.DiagnosticsState";

	/**
	 * WAN连接（TR069节点全路径）针对桥接和路由WAN均适用
	 */
	public static final String DHCP_WANINTERFACE = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.WANInterface";

	/**
	 * 模拟用户的MAC
	 */
	public static final String DHCP_USERMAC = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.UserMac";

	/**
	 * DHCP连接成功后需要Ping的目的IP
	 */
	public static final String DHCP_PINGDESTIPADDRESS = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.PingDestIPAddress";

	/**
	 * 测试过程Ping包个数
	 */
	public static final String DHCP_PINGNUMBEROFREPETITIONS = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.PingNumberOfRepetitions";

	/**
	 * 测试过程Ping包返回时延限制
	 */
	public static final String DHCP_TIMEOUT = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.Timeout";

	/**
	 * 仿真结果
	 */
	public static final String DHCP_RESULT = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.Result";
	/**
	 * 仿真获取的IP地址
	 */
	public static final String DHCP_LOCALADDRESS = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.LocalAddress";
	/**
	 * 默认网关地址
	 */
	public static final String DHCP_DEFAULTGATEWAY = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.DefaultGateway";
	/**
	 * 测试过程Ping包成功个数
	 */
	public static final String DHCP_SUCCESSCOUNT = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.SuccessCount";
	/**
	 * 测试过程Ping包失败个数
	 */
	public static final String DHCP_FAILURECOUNT = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.FailureCount";
	/**
	 * 测试过程Ping包平均返回时延（单位：ms）
	 */
	public static final String DHCP_AVERAGERESPONSETIME = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.AverageResponseTime";
	/**
	 * 测试过程Ping包最小返回时延（单位：ms）
	 */
	public static final String DHCP_MINIMUMRESPONSETIME = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.MinimumResponseTime";
	/**
	 * 测试过程Ping包最大返回时延（单位：ms）
	 */
	public static final String DHCP_MAXIMUMRESPONSETIME = "InternetGatewayDevice.X_CMCC_IPoEDiagnostics.MaximumResponseTime";

	// ============================PPPoE仿真参数==========================//
	/**
	 * 诊断状态
	 */
	public static final String PPPOE_DIAGNOSTICSSTATE = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.DiagnosticsState";
	/**
	 * PPPoE用户名
	 */
	public static final String PPPOE_USERNAME = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.Username";
	/**
	 * PPPoE密码
	 */
	public static final String PPPOE_PASSWORD = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.Password";
	/**
	 * WAN连接（TR069节点全路径）针对桥接和路由WAN均适用
	 */
	public static final String PPPOE_WANINTERFACE = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.WANInterface";
	/**
	 * 认证模式
	 */
	public static final String PPPOE_PPPAUTHENTICATIONPROTOCOL = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.PPPAuthenticationProtocol";
	/**
	 * 重试次数
	 */
	public static final String PPPOE_RETRYTIMES = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.RetryTimes";
	/**
	 * 诊断结果
	 */
	public static final String PPPOE_RESULT = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.Result";
	/**
	 * 会话ID
	 */
	public static final String PPPOE_PPPSESSIONID = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.PPPSessionID";
	/**
	 * IP地址
	 */
	public static final String PPPOE_EXTERNALIPADDRESS = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.ExternalIPAddress";
	/**
	 * 默认网关
	 */
	public static final String PPPOE_DEFAULTGATEWAY = "InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.DefaultGateway";

	// ============================Traceroute诊断==========================//
	/**
	 * Traceroute诊断采用的协议类型：UDP;ICMP
	 */
	public static final String TRACEROUTE_MODE = "InternetGatewayDevice.TraceRouteDiagnostics.Mode";
	/**
	 * 诊断状态
	 */
	public static final String TRACEROUTE_DIAGNOSTICSSTATE = "InternetGatewayDevice.TraceRouteDiagnostics.DiagnosticsState";
	/**
	 * WAN连接（TR069节点全路径）只针对路由WAN
	 */
	public static final String TRACEROUTE_INTERFACE = "InternetGatewayDevice.TraceRouteDiagnostics.Interface";
	/**
	 * Traceroute测试的主机名或主机地址
	 */
	public static final String TRACEROUTE_HOST = "InternetGatewayDevice.TraceRouteDiagnostics.Host";
	/**
	 * 每跳重复次数
	 */
	public static final String TRACEROUTE_NUMBEROFTRIES = "InternetGatewayDevice.TraceRouteDiagnostics.NumberOfTries";
	/**
	 * 诊断超时时间(单位：ms)
	 */
	public static final String TRACEROUTE_TIMEOUT = "InternetGatewayDevice.TraceRouteDiagnostics.Timeout";
	/**
	 * 每个Traceroute包发送的数据块大小（单位：字节）
	 */
	public static final String TRACEROUTE_DATABLOCKSIZE = "InternetGatewayDevice.TraceRouteDiagnostics.DataBlockSize";
	/**
	 * 用来测试包的DSCP值
	 */
	public static final String TRACEROUTE_DSCP = "InternetGatewayDevice.TraceRouteDiagnostics.DSCP";
	/**
	 * 最大跳数
	 */
	public static final String TRACEROUTE_MAXHOPCOUNT = "InternetGatewayDevice.TraceRouteDiagnostics.MaxHopCount";
	/**
	 * 诊断响应时间(单位：ms)
	 */
	public static final String TRACEROUTE_RESPONSETIME = "InternetGatewayDevice.TraceRouteDiagnostics.ResponseTime";
	/**
	 * 实际探测到的总跳数
	 */
	public static final String TRACEROUTE_HOPSNUMBEROFENTRIES = "InternetGatewayDevice.TraceRouteDiagnostics.HopsNumberOfEntries";
	/**
	 * Traceroute诊断记录列表
	 */
	public static final String TRACEROUTE_OBJECT = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops.";

	// ============================HHTP诊断==========================//
	/**
	 * 诊断状态
	 */
	public static final String HTTP_DIAGNOSTICSSTATE = "InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState";
	/**
	 * WAN连接（TR069节点全路径）只针对路由WAN
	 */
	public static final String HTTP_INTERFACE = "InternetGatewayDevice.DownloadDiagnostics.Interface";
	/**
	 * 用于下载的URL
	 */
	public static final String HTTP_DOWNLOADURL = "InternetGatewayDevice.DownloadDiagnostics.DownloadURL";
	/**
	 * 用来测试包的DSCP值
	 */
	public static final String HTTP_DSCP = "InternetGatewayDevice.DownloadDiagnostics.DSCP";
	/**
	 * 用来测试包的优先级
	 */
	public static final String HTTP_ETHERNETPRIORITY = "InternetGatewayDevice.DownloadDiagnostics.EthernetPriority";
	/**
	 * 请求收到时间
	 */
	public static final String HTTP_ROMTIME = "InternetGatewayDevice.DownloadDiagnostics.ROMTime";
	/**
	 * 传输开始时间
	 */
	public static final String HTTP_BOMTIME = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
	/**
	 * 传输结束时间
	 */
	public static final String HTTP_EOMTIME = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
	/**
	 * 接收字节数，包括控制头
	 */
	public static final String HTTP_TESTBYTESRECEIVED = "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived";
	/**
	 * 接收字节数
	 */
	public static final String HTTP_TOTALBYTESRECEIVED = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
	/**
	 * TCP请求时间
	 */
	public static final String HTTP_TCPOPENREQUESTTIME = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
	/**
	 * TCP响应时间
	 */
	public static final String HTTP_TCPOPENRESPONSETIME = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";

	// ============================Voip诊断==========================//
	/**
	 * VOIP根节点
	 */
	public static final String VOIP_ROOT = "InternetGatewayDevice.Services.VoiceService.";

}
