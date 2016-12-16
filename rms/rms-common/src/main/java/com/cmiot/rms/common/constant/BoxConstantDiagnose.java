package com.cmiot.rms.common.constant;

public class BoxConstantDiagnose {

	/*---------------------------------Ping诊断----------------------------------*/

	/**
	 * Ping诊断参数根节点
	 */
	private static String pingRootNode = "Device.LAN.IPPingDiagnostics.";

	/**
	 * 表示诊断数据的情况,如 果终端管理平台要求设置 这个值,则可以是: "None"
	 * "Requested"
	 * "Complete" "Error_CannotResolveHost Name"
	 * "Error_Internal" "Error_Other"
	 */
	public static final String PING_DIAGNOSTICSSTATE = pingRootNode + "DiagnosticsState";
	/**
	 * 用于 ping 诊断的主机名或 地址
	 */
	public static final String PING_HOST = pingRootNode + "Host";
	/**
	 * 在报告结果之前,ping 诊 断重复的次数
	 */
	public static final String PING_NUMBEROFREPETITIONS = pingRootNode + "NumberOfRepetitions";
	/**
	 * 用毫秒表示的 ping 诊断超 时时间
	 */
	public static final String PING_TIMEOUT = pingRootNode + "Timeout";
	/**
	 * 每个 ping 命令发送的数据 大小,以字节为单位,要 求固定大小为 32 字节
	 */
	public static final String PING_DATABLOCKSIZE = pingRootNode + "DataBlockSize";
	/**
	 * 测试包中用于 DiffServ 的 码点,默认值为 0
	 */
	public static final String PING_DSCP = pingRootNode + "DSCP";
	/**
	 * 最近的 ping 测试中成功 的次数
	 */
	public static final String PING_SUCCESSCOUNT = pingRootNode + "SuccessCount";
	/**
	 * 在最近的 ping 测试中失败 的次数
	 */
	public static final String PING_FAILURECOUNT = pingRootNode + "FailureCount";
	/**
	 * 以毫秒为单位的最近一次 ping 测试所有成功响应的 平均时间
	 */
	public static final String PING_AVERAGERESPONSETIME = pingRootNode + "AverageResponseTime";
	/**
	 * 以毫秒为单位的最近一次 ping 测试所有成功响应的 最短时间
	 */
	public static final String PING_MINIMUMRESPONSETIME = pingRootNode + "MinimumResponseTime";
	/**
	 * 以毫秒为单位的最近一次 ping 测试所有成功响应的 最长时间
	 */
	public static final String PING_MAXIMUMRESPONSETIME = pingRootNode + "MaximumResponseTime";

	/*---------------------------------TraceRoute诊断----------------------------------*/

	private static String traceRouteRootNode = "Device.LAN.TraceRouteDiagnostics.";

	/**
	 * 表示诊断数据的情况,如 果终端管理平台要求设置 这个值,则可以是: "None"
	 * "Requested"
	 * "Complete" "Error_CannotResolveHost Name" Error_MaxHopCountExcee ded
	 * "Error_Internal" "Error_Other"
	 */
	public static final String TRACEROUTE_DIAGNOSTICSSTATE = traceRouteRootNode + "DiagnosticsState";
	/**
	 * 用于 traceroute 诊断的主机 名或地址
	 */
	public static final String TRACEROUTE_HOST = traceRouteRootNode + "Host";
	/**
	 * 用毫秒表示的路由诊断超时时间
	 */
	public static final String TRACEROUTE_TIMEOUT = traceRouteRootNode + "Timeout";
	/**
	 * 每个路由命令发送的数据 大小,以字节为单位,要 求固定大小为 32 字节
	 */
	public static final String TRACEROUTE_DATABLOCKSIZE = traceRouteRootNode + "DataBlockSize";

	/**
	 * 发送的测试数据包的最大 跳数(最大 TTL 数),默认 为 30 跳
	 */
	public static final String TRACEROUTE_MAXHOPCOUNT = traceRouteRootNode + "MaxHopCount";
	/**
	 * 测试包中用于 DiffServ 的 码点,默认值为 0
	 */
	public static final String TRACEROUTE_DSCP = traceRouteRootNode + "DSCP";
	/**
	 * 以毫秒表示的最近一次路 由主机测试的响应时间, 如果无法决定具体路由, 则默认为 0
	 */
	public static final String TRACEROUTE_RESPONSETIME = traceRouteRootNode + "ResponseTime";
	/**
	 * 用于发现路由的跳数,如 果无法决定路由,则默认 为 0
	 */
	public static final String TRACEROUTE_NUMBEROFROUTEHOPS = traceRouteRootNode + "NumberOfRouteHops";

	/**
	 * 用于表示被发现的路由对象,如果路由无法达到,则这个对象没有实例
	 */
	public static final String TRACEROUTE_ROUTEHOPS = "Device.LAN.TraceRouteDiagnostics.RouteHops.";
}
