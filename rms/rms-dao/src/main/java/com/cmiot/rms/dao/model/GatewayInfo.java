package com.cmiot.rms.dao.model;

import com.iot.common.file.poi.ExcelVOAttribute;

public class GatewayInfo extends BaseBean{

	private String gatewayUuid;
	@ExcelVOAttribute(name="V", column = "V" )
	private String gatewayType;

	@ExcelVOAttribute(name="D", column = "D" )
	private String gatewayModel;

	private String gatewayName;

	@ExcelVOAttribute(name="E", column = "E" )
	private String gatewayVersion;

	@ExcelVOAttribute(name="A", column = "A" )
	private String gatewaySerialnumber;

	@ExcelVOAttribute(name="C", column = "C" )
	private String gatewayFactory;

	private String gatewayFactoryCode;

	private String gatewayMemo;

	@ExcelVOAttribute(name="F", column = "F" )
	private String gatewayHardwareVersion;

	private Integer gatewayJoinTime;

	private Integer gatewayLastConnTime;

	private String gatewayAdslAccount;

	@ExcelVOAttribute(name="B", column = "B" )
	private String gatewayStatus;

	@ExcelVOAttribute(name="G", column = "G" )
	private String gatewayIpaddress;

	@ExcelVOAttribute(name="H", column = "H" )
	private String gatewayMacaddress;

	private String gatewayConnectionrequesturl;

	private String gatewayAreaId;
	
	private String gatewayAreaName;

	private String gatewayDeviceUuid;
	
	private String deviceModel;
	
	private String makeMerchantsId;  //制造商ID
	
	private String gatewayPassword;
	
	private String gatewayDigestAccount;  //digest帐号
	
	private String gatewayDigestPassword; //digest密码

	private String oui; //oui

	private String newFactoryCode; //new_factory_code

	private String osgi;
	
	private String jvm;
	
	private String gatewayFamilyAccount;
	
	private String gatewayFamilyPassword;
	
	private String gatewayConnectionrequestUsername;  //管理平台连接网关的用户名
	
	private String gatewayConnectionrequestPassword; //管理平台连接网关的密码

	private String gatewayFirmwareUuid;

	private String gatewayExternalIPaddress;

	private Integer backupFileMaxNumber;

	private Integer logSwitchStatus;
	
	private String flowRate;  //流量
	
	private String businessStatus; //工单业务状态

	private String businessCode;//业务类型

	//SSID1出厂名称后四位 *BMS平台使用
	private String ssidInitLastnumber;

	//SSID1初始密码 *BMS平台使用
	private String ssidInitPwd;

	//useradmin初始密码 *BMS平台使用
	private String uadminInitPwd;

	public String getUadminInitPwd() {
		return uadminInitPwd;
	}

	public void setUadminInitPwd(String uadminInitPwd) {
		this.uadminInitPwd = uadminInitPwd;
	}
	public String getSsidInitPwd() {
		return ssidInitPwd;
	}

	public void setSsidInitPwd(String ssidInitPwd) {
		this.ssidInitPwd = ssidInitPwd;
	}

	public String getSsidInitLastnumber() {
		return ssidInitLastnumber;
	}

	public void setSsidInitLastnumber(String ssidInitLastnumber) {
		this.ssidInitLastnumber = ssidInitLastnumber;
	}

	public String getBusinessCode() {
		return businessCode;
	}

	public void setBusinessCode(String businessCode) {
		this.businessCode = businessCode;
	}

	public String getGatewayExternalIPaddress() {
		return gatewayExternalIPaddress;
	}

	public void setGatewayExternalIPaddress(String gatewayExternalIPaddress) {
		this.gatewayExternalIPaddress = gatewayExternalIPaddress;
	}

	public String getGatewayUuid() {
		return gatewayUuid;
	}

	public void setGatewayUuid(String gatewayUuid) {
		this.gatewayUuid = gatewayUuid == null ? null : gatewayUuid.trim();
	}

	public String getGatewayType() {
		return gatewayType;
	}

	public void setGatewayType(String gatewayType) {
		this.gatewayType = gatewayType == null ? null : gatewayType.trim();
	}

	public String getGatewayModel() {
		return gatewayModel;
	}

	public void setGatewayModel(String gatewayModel) {
		this.gatewayModel = gatewayModel == null ? null : gatewayModel.trim();
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName == null ? null : gatewayName.trim();
	}

	public String getGatewayVersion() {
		return gatewayVersion;
	}

	public void setGatewayVersion(String gatewayVersion) {
		this.gatewayVersion = gatewayVersion == null ? null : gatewayVersion.trim();
	}

	public String getGatewaySerialnumber() {
		return gatewaySerialnumber;
	}

	public void setGatewaySerialnumber(String gatewaySerialnumber) {
		this.gatewaySerialnumber = gatewaySerialnumber == null ? null : gatewaySerialnumber.trim();
	}

	public String getGatewayFactory() {
		return gatewayFactory;
	}

	public void setGatewayFactory(String gatewayFactory) {
		this.gatewayFactory = gatewayFactory == null ? null : gatewayFactory.trim();
	}

	public String getOui() {
		return oui;
	}

	public void setOui(String oui) {
		this.oui = oui;
	}

	public String getGatewayFactoryCode() {
		return gatewayFactoryCode;
	}

	public void setGatewayFactoryCode(String gatewayFactoryCode) {
		this.gatewayFactoryCode = gatewayFactoryCode == null ? null : gatewayFactoryCode.trim();
	}

	public String getGatewayMemo() {
		return gatewayMemo;
	}

	public void setGatewayMemo(String gatewayMemo) {
		this.gatewayMemo = gatewayMemo == null ? null : gatewayMemo.trim();
	}

	public String getGatewayHardwareVersion() {
		return gatewayHardwareVersion;
	}

	public void setGatewayHardwareVersion(String gatewayHardwareVersion) {
		this.gatewayHardwareVersion = gatewayHardwareVersion == null ? null : gatewayHardwareVersion.trim();
	}

	public Integer getGatewayJoinTime() {
		return gatewayJoinTime;
	}

	public void setGatewayJoinTime(Integer gatewayJoinTime) {
		this.gatewayJoinTime = gatewayJoinTime;
	}

	public Integer getGatewayLastConnTime() {
		return gatewayLastConnTime;
	}

	public void setGatewayLastConnTime(Integer gatewayLastConnTime) {
		this.gatewayLastConnTime = gatewayLastConnTime;
	}

	public String getGatewayAdslAccount() {
		return gatewayAdslAccount;
	}

	public void setGatewayAdslAccount(String gatewayAdslAccount) {
		this.gatewayAdslAccount = gatewayAdslAccount == null ? null : gatewayAdslAccount.trim();
	}

	public String getGatewayStatus() {
		return gatewayStatus;
	}

	public void setGatewayStatus(String gatewayStatus) {
		this.gatewayStatus = gatewayStatus == null ? null : gatewayStatus.trim();
	}

	public String getGatewayIpaddress() {
		return gatewayIpaddress;
	}

	public void setGatewayIpaddress(String gatewayIpaddress) {
		this.gatewayIpaddress = gatewayIpaddress == null ? null : gatewayIpaddress.trim();
	}

	public String getGatewayMacaddress() {
		return gatewayMacaddress;
	}

	public void setGatewayMacaddress(String gatewayMacaddress) {
		this.gatewayMacaddress = gatewayMacaddress == null ? null : gatewayMacaddress.trim();
	}

	public String getGatewayConnectionrequesturl() {
		return gatewayConnectionrequesturl;
	}

	public void setGatewayConnectionrequesturl(String gatewayConnectionrequesturl) {
		this.gatewayConnectionrequesturl = gatewayConnectionrequesturl == null ? null : gatewayConnectionrequesturl.trim();
	}

	public String getGatewayAreaId() {
		return gatewayAreaId;
	}

	public void setGatewayAreaId(String gatewayAreaId) {
		this.gatewayAreaId = gatewayAreaId == null ? null : gatewayAreaId.trim();
	}
	

	public String getGatewayAreaName() {
		return gatewayAreaName;
	}

	public void setGatewayAreaName(String gatewayAreaName) {
		this.gatewayAreaName = (gatewayAreaName==null ? "所有区域" : gatewayAreaName.trim());
	}

	public String getGatewayDeviceUuid() {
		return gatewayDeviceUuid;
	}

	public void setGatewayDeviceUuid(String gatewayDeviceUuid) {
		this.gatewayDeviceUuid = gatewayDeviceUuid;
	}

	public String getGatewayFirmwareUuid() {
		return gatewayFirmwareUuid;
	}

	public void setGatewayFirmwareUuid(String gatewayFirmwareUuid) {
		this.gatewayFirmwareUuid = gatewayFirmwareUuid;
	}


	public String getMakeMerchantsId() {
		return makeMerchantsId;
	}

	public void setMakeMerchantsId(String makeMerchantsId) {
		this.makeMerchantsId = makeMerchantsId;
	}

	public String getGatewayPassword() {
		return gatewayPassword;
	}

	public void setGatewayPassword(String gatewayPassword) {
		this.gatewayPassword = gatewayPassword;
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

	public String getNewFactoryCode() {
		return newFactoryCode;
	}

	public void setNewFactoryCode(String newFactoryCode) {
		this.newFactoryCode = newFactoryCode;
	}

	public String getGatewayFamilyAccount() {
		return gatewayFamilyAccount;
	}

	public void setGatewayFamilyAccount(String gatewayFamilyAccount) {
		this.gatewayFamilyAccount = gatewayFamilyAccount;
	}

	public String getGatewayFamilyPassword() {
		return gatewayFamilyPassword;
	}

	public void setGatewayFamilyPassword(String gatewayFamilyPassword) {
		this.gatewayFamilyPassword = gatewayFamilyPassword;
	}

	public Integer getBackupFileMaxNumber() {
		return backupFileMaxNumber;
	}

	public void setBackupFileMaxNumber(Integer backupFileMaxNumber) {
		this.backupFileMaxNumber = backupFileMaxNumber;
	}
    

	public GatewayInfo() {
		super();
	}

	public GatewayInfo(String gatewayUuid) {
		super();
		this.gatewayUuid = gatewayUuid;
	}

	public String getGatewayConnectionrequestUsername() {
		return gatewayConnectionrequestUsername;
	}

	public void setGatewayConnectionrequestUsername(
			String gatewayConnectionrequestUsername) {
		this.gatewayConnectionrequestUsername = gatewayConnectionrequestUsername;
	}

	public String getGatewayConnectionrequestPassword() {
		return gatewayConnectionrequestPassword;
	}

	public void setGatewayConnectionrequestPassword(
			String gatewayConnectionrequestPassword) {
		this.gatewayConnectionrequestPassword = gatewayConnectionrequestPassword;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public Integer getLogSwitchStatus() {
		return logSwitchStatus;
	}

	public void setLogSwitchStatus(Integer logSwitchStatus) {
		this.logSwitchStatus = logSwitchStatus;
	}

	public String getFlowRate() {
		return flowRate;
	}

	public void setFlowRate(String flowRate) {
		this.flowRate = flowRate;
	}

	public String getBusinessStatus() {
		return businessStatus;
	}

	public void setBusinessStatus(String businessStatus) {
		this.businessStatus = businessStatus;
	}
	
}