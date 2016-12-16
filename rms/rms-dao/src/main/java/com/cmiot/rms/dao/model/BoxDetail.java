package com.cmiot.rms.dao.model;



public class BoxDetail extends BaseBean {

	private String hardwareAblityUuid;

	private Integer hardwareAblityLanCount;

	private Integer hardwareAblityUsbCount;

	private String hardwareAblitySupportWifi;

	private String hardwareAblityWifiLoc;

	private Integer hardwareAblityWifiCount;

	private String hardwareAblityWifiSize;

	private String hardwareAblitySupportWifi24ghz;

	private String hardwareAblitySupportWifi58ghz;

	private String hardwareAblityIpv4v6;

	private String boxInfoUuid;

	private String boxInfoType;

	private String boxInfoModel;

	private String boxInfoVersion;

	private String boxInfoSerialnumber;

	private String boxInfoFactory;

	private String boxInfoHardwareVersion;

	private String boxInfoStatus;

	private String boxInfoIpaddress;

	private String boxInfoMacaddress;

	private Integer boxInfoJoinTime;

	private Integer boxInfoLastConnTime;

	private String boxConnectionrequesturl;

	private String boxDigestAccount;  //digest帐号

	private String boxDigestPassword; //digest密码

	private String boxFamilyAccount;

	private String boxFamilyPassword;

	public String getBoxConnType() {
		return boxConnType;
	}

	public void setBoxConnType(String boxConnType) {
		this.boxConnType = boxConnType;
	}

	public String getBoxFileUrl() {
		return boxFileUrl;
	}

	public void setBoxFileUrl(String boxFileUrl) {
		this.boxFileUrl = boxFileUrl;
	}

	public String getBoxUrl() {
		return boxUrl;
	}

	public void setBoxUrl(String boxUrl) {
		this.boxUrl = boxUrl;
	}

	private String boxUrl;

	private String boxConnType;

	private String boxFileUrl;

	public String getBoxConnectionrequesturl() {
		return boxConnectionrequesturl;
	}

	public void setBoxConnectionrequesturl(String boxConnectionrequesturl) {
		this.boxConnectionrequesturl = boxConnectionrequesturl;
	}

	public String getBoxDigestAccount() {
		return boxDigestAccount;
	}

	public void setBoxDigestAccount(String boxDigestAccount) {
		this.boxDigestAccount = boxDigestAccount;
	}

	public String getBoxDigestPassword() {
		return boxDigestPassword;
	}

	public void setBoxDigestPassword(String boxDigestPassword) {
		this.boxDigestPassword = boxDigestPassword;
	}

	public String getBoxFamilyAccount() {
		return boxFamilyAccount;
	}

	public void setBoxFamilyAccount(String boxFamilyAccount) {
		this.boxFamilyAccount = boxFamilyAccount;
	}

	public String getBoxFamilyPassword() {
		return boxFamilyPassword;
	}

	public void setBoxFamilyPassword(String boxFamilyPassword) {
		this.boxFamilyPassword = boxFamilyPassword;
	}

	public String getBoxInfoFactory() {
		return boxInfoFactory;
	}

	public void setBoxInfoFactory(String boxInfoFactory) {
		this.boxInfoFactory = boxInfoFactory;
	}

	public String getBoxInfoHardwareVersion() {
		return boxInfoHardwareVersion;
	}

	public void setBoxInfoHardwareVersion(String boxInfoHardwareVersion) {
		this.boxInfoHardwareVersion = boxInfoHardwareVersion;
	}

	public String getBoxInfoIpaddress() {
		return boxInfoIpaddress;
	}

	public void setBoxInfoIpaddress(String boxInfoIpaddress) {
		this.boxInfoIpaddress = boxInfoIpaddress;
	}

	public Integer getBoxInfoJoinTime() {
		return boxInfoJoinTime;
	}

	public void setBoxInfoJoinTime(Integer boxInfoJoinTime) {
		this.boxInfoJoinTime = boxInfoJoinTime;
	}

	public Integer getBoxInfoLastConnTime() {
		return boxInfoLastConnTime;
	}

	public void setBoxInfoLastConnTime(Integer boxInfoLastConnTime) {
		this.boxInfoLastConnTime = boxInfoLastConnTime;
	}

	public String getBoxInfoMacaddress() {
		return boxInfoMacaddress;
	}

	public void setBoxInfoMacaddress(String boxInfoMacaddress) {
		this.boxInfoMacaddress = boxInfoMacaddress;
	}

	public String getBoxInfoModel() {
		return boxInfoModel;
	}

	public void setBoxInfoModel(String boxInfoModel) {
		this.boxInfoModel = boxInfoModel;
	}

	public String getBoxInfoSerialnumber() {
		return boxInfoSerialnumber;
	}

	public void setBoxInfoSerialnumber(String boxInfoSerialnumber) {
		this.boxInfoSerialnumber = boxInfoSerialnumber;
	}

	public String getBoxInfoStatus() {
		return boxInfoStatus;
	}

	public void setBoxInfoStatus(String boxInfoStatus) {
		this.boxInfoStatus = boxInfoStatus;
	}

	public String getBoxInfoType() {
		return boxInfoType;
	}

	public void setBoxInfoType(String boxInfoType) {
		this.boxInfoType = boxInfoType;
	}

	public String getBoxInfoUuid() {
		return boxInfoUuid;
	}

	public void setBoxInfoUuid(String boxInfoUuid) {
		this.boxInfoUuid = boxInfoUuid;
	}

	public String getBoxInfoVersion() {
		return boxInfoVersion;
	}

	public void setBoxInfoVersion(String boxInfoVersion) {
		this.boxInfoVersion = boxInfoVersion;
	}

	public String getHardwareAblityIpv4v6() {
		return hardwareAblityIpv4v6;
	}

	public void setHardwareAblityIpv4v6(String hardwareAblityIpv4v6) {
		this.hardwareAblityIpv4v6 = hardwareAblityIpv4v6;
	}

	public Integer getHardwareAblityLanCount() {
		return hardwareAblityLanCount;
	}

	public void setHardwareAblityLanCount(Integer hardwareAblityLanCount) {
		this.hardwareAblityLanCount = hardwareAblityLanCount;
	}

	public String getHardwareAblitySupportWifi24ghz() {
		return hardwareAblitySupportWifi24ghz;
	}

	public void setHardwareAblitySupportWifi24ghz(String hardwareAblitySupportWifi24ghz) {
		this.hardwareAblitySupportWifi24ghz = hardwareAblitySupportWifi24ghz;
	}

	public String getHardwareAblitySupportWifi58ghz() {
		return hardwareAblitySupportWifi58ghz;
	}

	public void setHardwareAblitySupportWifi58ghz(String hardwareAblitySupportWifi58ghz) {
		this.hardwareAblitySupportWifi58ghz = hardwareAblitySupportWifi58ghz;
	}

	public String getHardwareAblitySupportWifi() {
		return hardwareAblitySupportWifi;
	}

	public void setHardwareAblitySupportWifi(String hardwareAblitySupportWifi) {
		this.hardwareAblitySupportWifi = hardwareAblitySupportWifi;
	}

	public Integer getHardwareAblityUsbCount() {
		return hardwareAblityUsbCount;
	}

	public void setHardwareAblityUsbCount(Integer hardwareAblityUsbCount) {
		this.hardwareAblityUsbCount = hardwareAblityUsbCount;
	}

	public String getHardwareAblityUuid() {
		return hardwareAblityUuid;
	}

	public void setHardwareAblityUuid(String hardwareAblityUuid) {
		this.hardwareAblityUuid = hardwareAblityUuid;
	}

	public Integer getHardwareAblityWifiCount() {
		return hardwareAblityWifiCount;
	}

	public void setHardwareAblityWifiCount(Integer hardwareAblityWifiCount) {
		this.hardwareAblityWifiCount = hardwareAblityWifiCount;
	}

	public String getHardwareAblityWifiLoc() {
		return hardwareAblityWifiLoc;
	}

	public void setHardwareAblityWifiLoc(String hardwareAblityWifiLoc) {
		this.hardwareAblityWifiLoc = hardwareAblityWifiLoc;
	}

	public String getHardwareAblityWifiSize() {
		return hardwareAblityWifiSize;
	}

	public void setHardwareAblityWifiSize(String hardwareAblityWifiSize) {
		this.hardwareAblityWifiSize = hardwareAblityWifiSize;
	}
}