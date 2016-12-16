package com.cmiot.rms.dao.model;

import java.util.Date;

public class DiagnoseLog extends BaseBean {

	private static final long serialVersionUID = -3903742769353828582L;

	private Integer id;// 表ID

	private String gatewayMacaddress;// MAC地址

	private String diagnoseOperator;// 诊断操作者

	private Integer diagnoseType;//诊断类型1:获取路由器LAN口信息与已经无线连接信息;2:获取CUP与内存占用比例;3:网络诊断;4:网关Ping地址平均访问时延

	private Date diagnoseTime;// 诊断时间

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGatewayMacaddress() {
		return gatewayMacaddress;
	}

	public void setGatewayMacaddress(String gatewayMacaddress) {
		this.gatewayMacaddress = gatewayMacaddress == null ? null : gatewayMacaddress.trim();
	}

	public String getDiagnoseOperator() {
		return diagnoseOperator;
	}

	public void setDiagnoseOperator(String diagnoseOperator) {
		this.diagnoseOperator = diagnoseOperator == null ? null : diagnoseOperator.trim();
	}

	public Integer getDiagnoseType() {
		return diagnoseType;
	}

	public void setDiagnoseType(Integer diagnoseType) {
		this.diagnoseType = diagnoseType;
	}

	public Date getDiagnoseTime() {
		return diagnoseTime;
	}

	public void setDiagnoseTime(Date diagnoseTime) {
		this.diagnoseTime = diagnoseTime;
	}

}