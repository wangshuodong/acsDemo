package com.cmiot.rms.dao.vo;

import com.cmiot.rms.common.annotation.CorrectnessData;
import com.cmiot.rms.common.annotation.DBRepetitionData;
import com.cmiot.rms.common.annotation.ExcelRepetitionData;
import com.cmiot.rms.common.annotation.NotBlank;
import com.iot.common.file.poi.ExcelVOAttribute;

public class BoxExcelContent {

	@ExcelVOAttribute(name="A", column = "A" )
	@NotBlank(fieldName = "网关SN")
	@ExcelRepetitionData(fieldName = "网关SN")
	@DBRepetitionData(fieldName = "网关SN", columnName = "sn")
	private String boxInfoSerialnumber;

	@ExcelVOAttribute(name="B", column = "B" )
	@NotBlank(fieldName = "网关厂家编码")
	@CorrectnessData(fieldName = "网关厂家编码", columnName = "fc")
	private String boxInfoFactoryCode;

	@ExcelVOAttribute(name="C", column = "C" )
	@NotBlank(fieldName = "网关型号")
	@CorrectnessData(fieldName = "网关型号", columnName = "model")
	private String boxInfoModel;

	@ExcelVOAttribute(name="D", column = "D" )
	@NotBlank(fieldName = "机顶盒软件版本")
	@CorrectnessData(fieldName = "机顶盒软件版本", columnName = "vs")
	private String boxInfoVersion;

	@ExcelVOAttribute(name="E", column = "E" )
	private String boxInfoHardwareVersion;

	@ExcelVOAttribute(name="F", column = "F" )
	@NotBlank(fieldName = "MAC地址")
	@ExcelRepetitionData(fieldName = "MAC地址")
	@DBRepetitionData(fieldName = "MAC地址", columnName = "mac")
	private String boxInfoMacaddress;

	@ExcelVOAttribute(name="G", column = "G" )
	@NotBlank(fieldName = "机顶盒类型")
	@ExcelRepetitionData(fieldName = "机顶盒类型")
	private String boxInfoType;

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

	private String boxInfoName;

	private String boxInfoFactory;

	private String boxInfoMemo;

	private Integer boxInfoJoinTime;

	private Integer boxInfoLastConnTime;

	private String gatewayInfoAdslAccount;

	private String boxInfoStatus;

	private String boxInfoIpaddress;

	private String boxInfoUrl;

	private String boxInfoConnectionrequesturl;

	private String boxInfoAreaId;

	private String deviceInfoUuid;
	
	private String gatewayPassword;

	public String getBoxInfoAreaId() {
		return boxInfoAreaId;
	}

	public void setBoxInfoAreaId(String boxInfoAreaId) {
		this.boxInfoAreaId = boxInfoAreaId;
	}

	public String getBoxInfoConnectionrequesturl() {
		return boxInfoConnectionrequesturl;
	}

	public void setBoxInfoConnectionrequesturl(String boxInfoConnectionrequesturl) {
		this.boxInfoConnectionrequesturl = boxInfoConnectionrequesturl;
	}

	public String getBoxInfoFactory() {
		return boxInfoFactory;
	}

	public void setBoxInfoFactory(String boxInfoFactory) {
		this.boxInfoFactory = boxInfoFactory;
	}

	public String getBoxInfoFactoryCode() {
		return boxInfoFactoryCode;
	}

	public void setBoxInfoFactoryCode(String boxInfoFactoryCode) {
		this.boxInfoFactoryCode = boxInfoFactoryCode;
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

	public String getBoxInfoMemo() {
		return boxInfoMemo;
	}

	public void setBoxInfoMemo(String boxInfoMemo) {
		this.boxInfoMemo = boxInfoMemo;
	}

	public String getBoxInfoModel() {
		return boxInfoModel;
	}

	public void setBoxInfoModel(String boxInfoModel) {
		this.boxInfoModel = boxInfoModel;
	}

	public String getBoxInfoName() {
		return boxInfoName;
	}

	public void setBoxInfoName(String boxInfoName) {
		this.boxInfoName = boxInfoName;
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

	public String getBoxInfoUrl() {
		return boxInfoUrl;
	}

	public void setBoxInfoUrl(String boxInfoUrl) {
		this.boxInfoUrl = boxInfoUrl;
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

	public String getDeviceInfoUuid() {
		return deviceInfoUuid;
	}

	public void setDeviceInfoUuid(String deviceInfoUuid) {
		this.deviceInfoUuid = deviceInfoUuid;
	}

	public String getGatewayInfoAdslAccount() {
		return gatewayInfoAdslAccount;
	}

	public void setGatewayInfoAdslAccount(String gatewayInfoAdslAccount) {
		this.gatewayInfoAdslAccount = gatewayInfoAdslAccount;
	}

	public String getGatewayPassword() {
		return gatewayPassword;
	}

	public void setGatewayPassword(String gatewayPassword) {
		this.gatewayPassword = gatewayPassword;
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