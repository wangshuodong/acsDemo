package com.cmiot.rms.dao.vo;

import com.cmiot.rms.common.annotation.CorrectnessData;
import com.cmiot.rms.common.annotation.DBRepetitionData;
import com.cmiot.rms.common.annotation.ExcelRepetitionData;
import com.cmiot.rms.common.annotation.NotBlank;
import com.iot.common.file.poi.ExcelVOAttribute;

public class GateWayInfoExcelContent {

	@ExcelVOAttribute(name="A", column = "A" )
	@NotBlank(fieldName = "是否智能网关")
	private String isIntelligentGateway;

	//MAC地址
	@ExcelVOAttribute(name="B", column = "B" )
	@NotBlank(fieldName = "MAC地址")
	@ExcelRepetitionData(fieldName = "MAC地址")
	@DBRepetitionData(fieldName = "MAC地址", columnName = "mac")
	private String gatewayInfoMacaddress;

	//网关SN
	@ExcelVOAttribute(name="C", column = "C" )
	@NotBlank(fieldName = "网关SN")
	@ExcelRepetitionData(fieldName = "网关SN")
	@DBRepetitionData(fieldName = "网关SN", columnName = "sn")
	private String gatewayInfoSerialnumber;

	//SSID1出厂名称后四位 *BMS平台使用
	@ExcelVOAttribute(name="D", column = "D" )
	@NotBlank(fieldName = "SSID1出厂名称后四位")
	private String ssid;

	//SSID1初始密码 *BMS平台使用
	@ExcelVOAttribute(name="E", column = "E" )
	@NotBlank(fieldName = "SSID1初始密码")
	private String ssidpwd;

	//useradmin初始密码（5位）*BMS平台使用
	@ExcelVOAttribute(name="F", column = "F" )
	@NotBlank(fieldName = "useradmin初始密码")
	private String upwd;

	//网关型号
	@CorrectnessData(fieldName = "网关型号", columnName = "model")
	@NotBlank(fieldName = "网关型号")
	@ExcelVOAttribute(name="G", column = "G" )
	private String gatewayInfoModel;

	//网关厂家编码
	@ExcelVOAttribute(name="H", column = "H" )
	@NotBlank(fieldName = "网关厂家编码")
	@CorrectnessData(fieldName = "网关厂家编码", columnName = "oui")
	private String gatewayInfoFactoryCode;

	//硬件版本
	@ExcelVOAttribute(name="I", column = "I" )
	@NotBlank(fieldName = "硬件版本")
	private String gatewayInfoHardwareVersion;

	//软件版本
	@ExcelVOAttribute(name="J", column = "J" )
	@NotBlank(fieldName = "软件版本")
	private String gatewayInfoVersion;

	//OSGI版本
	@ExcelVOAttribute(name="K", column = "K" )
	private String osgi;

	//JVM版本
	@ExcelVOAttribute(name="L", column = "L" )
	private String jvm;

	//生产日期(按照年月日格式) * 一级平台使用
	@ExcelVOAttribute(name="M", column = "M" )
	private String manufactureDate;

	//到货日期(按照年月日格式) * 一级平台使用
	@ExcelVOAttribute(name="N", column = "N" )
	private String arrivalDate;

	//操作系统类型
	private String operSystemType;

	//操作系统版本
	private String operSystemVersion;

	//网关形态
	private String gatewayInfoForm;

	//网关PASSWORD
	private String gatewayPassword;

	//LAN口数量
	private Integer hardwareAblityLanCount;

	//USB口数量
	private Integer hardwareAblityUsbCount;

	//USB口数量
	private String hardwareAblitySupportWifi;

	//无线天线位置
	private String hardwareAblityWifiLoc;

	//无线天线数
	private Integer hardwareAblityWifiCount;

	//无线天线尺寸
	private String hardwareAblityWifiSize;

	//WIFI2.4GHz能力
	private String hardwareAblitySupportWifi24ghz;

	//WIFI5.8GHz能力
	private String hardwareAblitySupportWifi58ghz;

	//IPv4/IPv6支持能力
	private String hardwareAblityIpv4v6;



	private String hardwareAblityUuid;

	private String gatewayInfoUuid;

	private String gatewayInfoType;

	private String gatewayInfoName;

	private String gatewayInfoFactory;

	private String gatewayInfoMemo;

	private Integer gatewayInfoJoinTime;

	private Integer gatewayInfoLastConnTime;

	private String gatewayInfoAdslAccount;

	private String gatewayInfoStatus;

	private String gatewayInfoAddressingtype;

	private String gatewayInfoIpaddress;

	private String gatewayInfoSubnetmask;

	private String gatewayInfoDefaultgateway;

	private String gatewayInfoDnsservers;

	private Integer gatewayInfoDhcpoptionnumberofentries;

	private String gatewayInfoUrl;

	private String gatewayInfoConnectionrequesturl;

	private String gatewayInfoAreaId;

	private String deviceInfoUuid;

	public String getIsIntelligentGateway() {
		return isIntelligentGateway;
	}

	public void setIsIntelligentGateway(String isIntelligentGateway) {
		this.isIntelligentGateway = isIntelligentGateway;
	}

	public String getGateWayfrimwareUUID() {
		return gateWayfrimwareUUID;
	}

	public void setGateWayfrimwareUUID(String gateWayfrimwareUUID) {
		this.gateWayfrimwareUUID = gateWayfrimwareUUID;
	}

	private String gateWayfrimwareUUID;


	public String getHardwareAblityUuid() {
		return hardwareAblityUuid;
	}

	public void setHardwareAblityUuid(String hardwareAblityUuid) {
		this.hardwareAblityUuid = hardwareAblityUuid == null ? null : hardwareAblityUuid.trim();
	}

	public Integer getHardwareAblityLanCount() {
		return hardwareAblityLanCount;
	}

	public void setHardwareAblityLanCount(Integer hardwareAblityLanCount) {
		this.hardwareAblityLanCount = hardwareAblityLanCount;
	}

	public Integer getHardwareAblityUsbCount() {
		return hardwareAblityUsbCount;
	}

	public void setHardwareAblityUsbCount(Integer hardwareAblityUsbCount) {
		this.hardwareAblityUsbCount = hardwareAblityUsbCount;
	}

	public String getHardwareAblitySupportWifi() {
		return hardwareAblitySupportWifi;
	}

	public void setHardwareAblitySupportWifi(String hardwareAblitySupportWifi) {
		this.hardwareAblitySupportWifi = hardwareAblitySupportWifi;
	}

	public String getHardwareAblityWifiLoc() {
		return hardwareAblityWifiLoc;
	}

	public void setHardwareAblityWifiLoc(String hardwareAblityWifiLoc) {
		this.hardwareAblityWifiLoc = hardwareAblityWifiLoc == null ? null : hardwareAblityWifiLoc.trim();
	}

	public Integer getHardwareAblityWifiCount() {
		return hardwareAblityWifiCount;
	}

	public void setHardwareAblityWifiCount(Integer hardwareAblityWifiCount) {
		this.hardwareAblityWifiCount = hardwareAblityWifiCount;
	}

	public String getHardwareAblityWifiSize() {
		return hardwareAblityWifiSize;
	}

	public void setHardwareAblityWifiSize(String hardwareAblityWifiSize) {
		this.hardwareAblityWifiSize = hardwareAblityWifiSize == null ? null : hardwareAblityWifiSize.trim();
	}

	public String getHardwareAblitySupportWifi24ghz() {
		return hardwareAblitySupportWifi24ghz;
	}

	public void setHardwareAblitySupportWifi24ghz(String hardwareAblitySupportWifi24ghz) {
		this.hardwareAblitySupportWifi24ghz = hardwareAblitySupportWifi24ghz == null ? null : hardwareAblitySupportWifi24ghz.trim();
	}

	public String getHardwareAblitySupportWifi58ghz() {
		return hardwareAblitySupportWifi58ghz;
	}

	public void setHardwareAblitySupportWifi58ghz(String hardwareAblitySupportWifi58ghz) {
		this.hardwareAblitySupportWifi58ghz = hardwareAblitySupportWifi58ghz == null ? null : hardwareAblitySupportWifi58ghz.trim();
	}

	public String getHardwareAblityIpv4v6() {
		return hardwareAblityIpv4v6;
	}

	public void setHardwareAblityIpv4v6(String hardwareAblityIpv4v6) {
		this.hardwareAblityIpv4v6 = hardwareAblityIpv4v6 == null ? null : hardwareAblityIpv4v6.trim();
	}

	public String getGatewayInfoUuid() {
		return gatewayInfoUuid;
	}

	public void setGatewayInfoUuid(String gatewayInfoUuid) {
		this.gatewayInfoUuid = gatewayInfoUuid == null ? null : gatewayInfoUuid.trim();
	}

	public String getGatewayInfoType() {
		return gatewayInfoType;
	}

	public void setGatewayInfoType(String gatewayInfoType) {
		this.gatewayInfoType = gatewayInfoType;
	}

	public String getGatewayInfoModel() {
		return gatewayInfoModel;
	}

	public void setGatewayInfoModel(String gatewayInfoModel) {
		this.gatewayInfoModel = gatewayInfoModel;
	}

	public String getGatewayInfoName() {
		return gatewayInfoName;
	}

	public void setGatewayInfoName(String gatewayInfoName) {
		this.gatewayInfoName = gatewayInfoName;
	}

	public String getGatewayInfoVersion() {
		return gatewayInfoVersion;
	}

	public void setGatewayInfoVersion(String gatewayInfoVersion) {
		this.gatewayInfoVersion = gatewayInfoVersion;
	}

	public String getGatewayInfoSerialnumber() {
		return gatewayInfoSerialnumber;
	}

	public void setGatewayInfoSerialnumber(String gatewayInfoSerialnumber) {
		this.gatewayInfoSerialnumber = gatewayInfoSerialnumber;
	}

	public String getGatewayInfoFactory() {
		return gatewayInfoFactory;
	}

	public void setGatewayInfoFactory(String gatewayInfoFactory) {
		this.gatewayInfoFactory = gatewayInfoFactory;
	}

	public String getGatewayInfoFactoryCode() {
		return gatewayInfoFactoryCode;
	}

	public void setGatewayInfoFactoryCode(String gatewayInfoFactoryCode) {
		this.gatewayInfoFactoryCode = gatewayInfoFactoryCode;
	}

	public String getGatewayInfoMemo() {
		return gatewayInfoMemo;
	}

	public void setGatewayInfoMemo(String gatewayInfoMemo) {
		this.gatewayInfoMemo = gatewayInfoMemo;
	}

	public String getGatewayInfoHardwareVersion() {
		return gatewayInfoHardwareVersion;
	}

	public void setGatewayInfoHardwareVersion(String gatewayInfoHardwareVersion) {
		this.gatewayInfoHardwareVersion = gatewayInfoHardwareVersion;
	}

	public Integer getGatewayInfoJoinTime() {
		return gatewayInfoJoinTime;
	}

	public void setGatewayInfoJoinTime(Integer gatewayInfoJoinTime) {
		this.gatewayInfoJoinTime = gatewayInfoJoinTime;
	}

	public Integer getGatewayInfoLastConnTime() {
		return gatewayInfoLastConnTime;
	}

	public void setGatewayInfoLastConnTime(Integer gatewayInfoLastConnTime) {
		this.gatewayInfoLastConnTime = gatewayInfoLastConnTime;
	}

	public String getGatewayInfoAdslAccount() {
		return gatewayInfoAdslAccount;
	}

	public void setGatewayInfoAdslAccount(String gatewayInfoAdslAccount) {
		this.gatewayInfoAdslAccount = gatewayInfoAdslAccount;
	}

	public String getGatewayInfoStatus() {
		return gatewayInfoStatus;
	}

	public void setGatewayInfoStatus(String gatewayInfoStatus) {
		this.gatewayInfoStatus = gatewayInfoStatus;
	}

	public String getGatewayInfoAddressingtype() {
		return gatewayInfoAddressingtype;
	}

	public void setGatewayInfoAddressingtype(String gatewayInfoAddressingtype) {
		this.gatewayInfoAddressingtype = gatewayInfoAddressingtype;
	}

	public String getGatewayInfoIpaddress() {
		return gatewayInfoIpaddress;
	}

	public void setGatewayInfoIpaddress(String gatewayInfoIpaddress) {
		this.gatewayInfoIpaddress = gatewayInfoIpaddress;
	}

	public String getGatewayInfoSubnetmask() {
		return gatewayInfoSubnetmask;
	}

	public void setGatewayInfoSubnetmask(String gatewayInfoSubnetmask) {
		this.gatewayInfoSubnetmask = gatewayInfoSubnetmask;
	}

	public String getGatewayInfoDefaultgateway() {
		return gatewayInfoDefaultgateway;
	}

	public void setGatewayInfoDefaultgateway(String gatewayInfoDefaultgateway) {
		this.gatewayInfoDefaultgateway = gatewayInfoDefaultgateway;
	}

	public String getGatewayInfoDnsservers() {
		return gatewayInfoDnsservers;
	}

	public void setGatewayInfoDnsservers(String gatewayInfoDnsservers) {
		this.gatewayInfoDnsservers = gatewayInfoDnsservers;
	}

	public String getGatewayInfoMacaddress() {
		return gatewayInfoMacaddress;
	}

	public void setGatewayInfoMacaddress(String gatewayInfoMacaddress) {
		this.gatewayInfoMacaddress = gatewayInfoMacaddress;
	}

	public Integer getGatewayInfoDhcpoptionnumberofentries() {
		return gatewayInfoDhcpoptionnumberofentries;
	}

	public void setGatewayInfoDhcpoptionnumberofentries(
			Integer gatewayInfoDhcpoptionnumberofentries) {
		this.gatewayInfoDhcpoptionnumberofentries = gatewayInfoDhcpoptionnumberofentries;
	}

	public String getGatewayInfoUrl() {
		return gatewayInfoUrl;
	}

	public void setGatewayInfoUrl(String gatewayInfoUrl) {
		this.gatewayInfoUrl = gatewayInfoUrl;
	}

	public String getGatewayInfoConnectionrequesturl() {
		return gatewayInfoConnectionrequesturl;
	}

	public void setGatewayInfoConnectionrequesturl(
			String gatewayInfoConnectionrequesturl) {
		this.gatewayInfoConnectionrequesturl = gatewayInfoConnectionrequesturl;
	}

	public String getGatewayInfoAreaId() {
		return gatewayInfoAreaId;
	}

	public void setGatewayInfoAreaId(String gatewayInfoAreaId) {
		this.gatewayInfoAreaId = gatewayInfoAreaId;
	}

	public String getDeviceInfoUuid() {
		return deviceInfoUuid;
	}

	public void setDeviceInfoUuid(String deviceInfoUuid) {
		this.deviceInfoUuid = deviceInfoUuid;
	}

	public String getGatewayPassword() {
		return gatewayPassword;
	}

	public void setGatewayPassword(String gatewayPassword) {
		this.gatewayPassword = gatewayPassword;
	}

	public String getOsgi() {
		return osgi;
	}

	public void setOsgi(String osgi) {
		this.osgi = osgi;
	}

	public String getJvm() {
		return jvm;
	}

	public void setJvm(String jvm) {
		this.jvm = jvm;
	}

	public String getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(String arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public String getGatewayInfoForm() {
		return gatewayInfoForm;
	}

	public void setGatewayInfoForm(String gatewayInfoForm) {
		this.gatewayInfoForm = gatewayInfoForm;
	}

	public String getOperSystemType() {
		return operSystemType;
	}

	public void setOperSystemType(String operSystemType) {
		this.operSystemType = operSystemType;
	}

	public String getManufactureDate() {
		return manufactureDate;
	}

	public void setManufactureDate(String manufactureDate) {
		this.manufactureDate = manufactureDate;
	}

	public String getOperSystemVersion() {
		return operSystemVersion;
	}

	public void setOperSystemVersion(String operSystemVersion) {
		this.operSystemVersion = operSystemVersion;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getSsidpwd() {
		return ssidpwd;
	}

	public void setSsidpwd(String ssidpwd) {
		this.ssidpwd = ssidpwd;
	}

	public String getUpwd() {
		return upwd;
	}

	public void setUpwd(String upwd) {
		this.upwd = upwd;
	}
}