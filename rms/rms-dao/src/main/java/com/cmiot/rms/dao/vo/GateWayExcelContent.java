package com.cmiot.rms.dao.vo;

import com.iot.common.file.poi.ExcelVOAttribute;

public class GateWayExcelContent {

	private String hardwareAblityUuid;

	@ExcelVOAttribute(name="I", column = "I" )
	private Integer hardwareAblityLanCount;

	@ExcelVOAttribute(name="J", column = "J" )
	private Integer hardwareAblityUsbCount;

	@ExcelVOAttribute(name="K", column = "K" )
	private String hardwareAblitySupportWifi;

	@ExcelVOAttribute(name="L", column = "L" )
	private String hardwareAblityWifiLoc;

	@ExcelVOAttribute(name="M", column = "M" )
	private Integer hardwareAblityWifiCount;

	@ExcelVOAttribute(name="N", column = "N" )
	private String hardwareAblityWifiSize;

	@ExcelVOAttribute(name="O", column = "O" )
	private String hardwareAblitySupportWifi24ghz;

	@ExcelVOAttribute(name="P", column = "P" )
	private String hardwareAblitySupportWifi58ghz;

	@ExcelVOAttribute(name="Q", column = "Q" )
	private String hardwareAblityIpv4v6;

	private String gatewayInfoUuid;


	private String gatewayInfoType;

	@ExcelVOAttribute(name="D", column = "D" )
	private String gatewayInfoModel;

//	@ExcelVOAttribute(name="T", column = "T" )
	private String gatewayInfoName;

	@ExcelVOAttribute(name="E", column = "E" )
	private String gatewayInfoVersion;

	@ExcelVOAttribute(name="A", column = "A" )
	private String gatewayInfoSerialnumber;

//	@ExcelVOAttribute(name="C", column = "C" )
	private String gatewayInfoFactory;

	@ExcelVOAttribute(name="C", column = "C" )
//	@ExcelVOAttribute(name="R", column = "R" )
	private String gatewayInfoFactoryCode;

	private String gatewayInfoMemo;

	@ExcelVOAttribute(name="F", column = "F" )
	private String gatewayInfoHardwareVersion;

	private Integer gatewayInfoJoinTime;

	private Integer gatewayInfoLastConnTime;

	private String gatewayInfoAdslAccount;

	@ExcelVOAttribute(name="B", column = "B" )
	private String gatewayInfoStatus;

	private String gatewayInfoAddressingtype;

	@ExcelVOAttribute(name="G", column = "G" )
	private String gatewayInfoIpaddress;

	private String gatewayInfoSubnetmask;

	private String gatewayInfoDefaultgateway;

	private String gatewayInfoDnsservers;

	@ExcelVOAttribute(name="H", column = "H" )
	private String gatewayInfoMacaddress;

	private Integer gatewayInfoDhcpoptionnumberofentries;

	private String gatewayInfoUrl;

	private String gatewayInfoConnectionrequesturl;

	@ExcelVOAttribute(name="R", column = "R" )
//	@ExcelVOAttribute(name="S", column = "S" )
	private String gatewayInfoAreaId;

	private String deviceInfoUuid;
	
	@ExcelVOAttribute(name="U", column = "U" )
	private String gatewayPassword;
	
	
	private String osgi;
	
	private String jvm;
 
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


}