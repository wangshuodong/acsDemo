package com.cmiot.rms.dao.model;

import com.iot.common.file.poi.ExcelVOAttribute;

import java.util.List;

public class GateWayDetail  extends BaseBean{

	private String hardwareAblityUuid;

	private Integer hardwareAblityLanCount;

	private Integer hardwareAblityUsbCount;

	private String hardwareAblitySupportWifi;


	private String hardwareAblityWifiLoc;


	private Integer hardwareAblityWifiCount;


	private String hardwareAblityWifiSize;

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	private String hardwareAblitySupportWifi24ghz;

	private String hardwareAblitySupportWifi58ghz;


	private String hardwareAblityIpv4v6;

	private String gatewayInfoUuid;

	private String gatewayInfoType;

	@ExcelVOAttribute(name="D", column = "D" )
	private String gatewayInfoModel;

	@ExcelVOAttribute(name="E", column = "E" )
	private String gatewayInfoVersion;

	@ExcelVOAttribute(name="A", column = "A" )
	private String gatewayInfoSerialnumber;

	@ExcelVOAttribute(name="C", column = "C" )
	private String gatewayInfoFactory;

	@ExcelVOAttribute(name="F", column = "F" )
	private String gatewayInfoHardwareVersion;

	@ExcelVOAttribute(name="B", column = "B" )
	private String gatewayInfoStatus;

	@ExcelVOAttribute(name="G", column = "G" )
	private String gatewayInfoIpaddress;

	@ExcelVOAttribute(name="H", column = "H" )
	private String gatewayInfoMacaddress;

	private Integer gatewayInfoJoinTime;

	private Integer gatewayInfoLastConnTime;


	private String gatewayConnectionrequesturl;
	
	private String osgi;
	
	private String jvm;
	
	private String gatewayPassword;

	private String gatewayName;

	private String gatewayConnectionrequestPassword;

	private String oui;



	public String getGatewayConnectionrequestPassword() {
		return gatewayConnectionrequestPassword;
	}

	public void setGatewayConnectionrequestPassword(String gatewayConnectionrequestPassword) {
		this.gatewayConnectionrequestPassword = gatewayConnectionrequestPassword;
	}

	private List<DeviceInfo> deviceInfoList;

	public List<DeviceInfo> getDeviceInfoList() {
		return deviceInfoList;
	}

	public void setDeviceInfoList(List<DeviceInfo> deviceInfoList) {
		this.deviceInfoList = deviceInfoList;
	}

	public String getGatewayFamilyPassword() {
		return gatewayFamilyPassword;
	}

	public void setGatewayFamilyPassword(String gatewayFamilyPassword) {
		this.gatewayFamilyPassword = gatewayFamilyPassword;
	}

	public String getGatewayDigestAccount() {
		return gatewayDigestAccount;
	}

	public void setGatewayDigestAccount(String gatewayDigestAccount) {
		this.gatewayDigestAccount = gatewayDigestAccount;
	}

	public String getGatewayDigestPassword() {
		return gatewayDigestPassword;
	}

	public void setGatewayDigestPassword(String gatewayDigestPassword) {
		this.gatewayDigestPassword = gatewayDigestPassword;
	}

	public String getGatewayFamilyAccount() {
		return gatewayFamilyAccount;
	}

	public void setGatewayFamilyAccount(String gatewayFamilyAccount) {
		this.gatewayFamilyAccount = gatewayFamilyAccount;
	}

	private String gatewayDigestAccount;  //digest帐号

	private String gatewayDigestPassword; //digest密码

	private String gatewayFamilyAccount;

	private String gatewayFamilyPassword;

	private String flowrate;

	public String getFlowrate() {
		return flowrate;
	}

	public void setFlowrate(String flowrate) {
		this.flowrate = flowrate;
	}

	public String getGatewayConnectionrequestUsername() {
		return gatewayConnectionrequestUsername;
	}

	public void setGatewayConnectionrequestUsername(String gatewayConnectionrequestUsername) {
		this.gatewayConnectionrequestUsername = gatewayConnectionrequestUsername;
	}

	private String gatewayConnectionrequestUsername;  //管理平台连接网关的用户名

	public String getHardwareAblityUuid() {
		return hardwareAblityUuid;
	}

	public void setHardwareAblityUuid(String hardwareAblityUuid) {
		this.hardwareAblityUuid = hardwareAblityUuid;
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
		this.hardwareAblityWifiLoc = hardwareAblityWifiLoc;
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
		this.hardwareAblityWifiSize = hardwareAblityWifiSize;
	}

	public String getHardwareAblitySupportWifi24ghz() {
		return hardwareAblitySupportWifi24ghz;
	}

	public void setHardwareAblitySupportWifi24ghz(
			String hardwareAblitySupportWifi24ghz) {
		this.hardwareAblitySupportWifi24ghz = hardwareAblitySupportWifi24ghz;
	}

	public String getHardwareAblitySupportWifi58ghz() {
		return hardwareAblitySupportWifi58ghz;
	}

	public void setHardwareAblitySupportWifi58ghz(
			String hardwareAblitySupportWifi58ghz) {
		this.hardwareAblitySupportWifi58ghz = hardwareAblitySupportWifi58ghz;
	}

	public String getHardwareAblityIpv4v6() {
		return hardwareAblityIpv4v6;
	}

	public void setHardwareAblityIpv4v6(String hardwareAblityIpv4v6) {
		this.hardwareAblityIpv4v6 = hardwareAblityIpv4v6;
	}

	public String getGatewayInfoUuid() {
		return gatewayInfoUuid;
	}

	public void setGatewayInfoUuid(String gatewayInfoUuid) {
		this.gatewayInfoUuid = gatewayInfoUuid;
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

	public String getGatewayInfoHardwareVersion() {
		return gatewayInfoHardwareVersion;
	}

	public void setGatewayInfoHardwareVersion(String gatewayInfoHardwareVersion) {
		this.gatewayInfoHardwareVersion = gatewayInfoHardwareVersion;
	}

	public String getGatewayInfoStatus() {
		return gatewayInfoStatus;
	}

	public void setGatewayInfoStatus(String gatewayInfoStatus) {
		this.gatewayInfoStatus = gatewayInfoStatus;
	}

	public String getGatewayInfoIpaddress() {
		return gatewayInfoIpaddress;
	}

	public void setGatewayInfoIpaddress(String gatewayInfoIpaddress) {
		this.gatewayInfoIpaddress = gatewayInfoIpaddress;
	}

	public String getGatewayInfoMacaddress() {
		return gatewayInfoMacaddress;
	}

	public void setGatewayInfoMacaddress(String gatewayInfoMacaddress) {
		this.gatewayInfoMacaddress = gatewayInfoMacaddress;
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

	public String getGatewayConnectionrequesturl() {
		return gatewayConnectionrequesturl;
	}

	public void setGatewayConnectionrequesturl(String gatewayConnectionrequesturl) {
		this.gatewayConnectionrequesturl = gatewayConnectionrequesturl;
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

	public String getGatewayPassword() {
		return gatewayPassword;
	}

	public void setGatewayPassword(String gatewayPassword) {
		this.gatewayPassword = gatewayPassword;
	}

	public String getOui() {
		return oui;
	}

	public void setOui(String oui) {
		this.oui = oui;
	}

	
}