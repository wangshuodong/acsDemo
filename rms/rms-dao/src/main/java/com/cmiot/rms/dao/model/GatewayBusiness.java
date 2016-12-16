package com.cmiot.rms.dao.model;

import java.util.Date;

public class GatewayBusiness {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.id
     *
     * @mbggenerated
     */
    private String id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.gateway_uuid
     *
     * @mbggenerated
     */
    private String gatewayUuid;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.business_code
     *
     * @mbggenerated
     */
    private String businessCode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.business_name
     *
     * @mbggenerated
     */
    private String businessName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.parameter_list
     *
     * @mbggenerated
     */
    private String parameterList;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.business_statu
     *
     * @mbggenerated
     */
    private String businessStatu;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.areacode
     *
     * @mbggenerated
     */
    private String areacode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.bandwidth
     *
     * @mbggenerated
     */
    private Integer bandwidth;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.create_time
     *
     * @mbggenerated
     */
    private Integer createTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.adsl_type
     *
     * @mbggenerated
     */
    private String businessType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.gateway_password
     *
     * @mbggenerated
     */
    private String gatewayPassword;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.adsl_password
     *
     * @mbggenerated
     */
    private String adslPassword;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.adsl_account
     *
     * @mbggenerated
     */
    private String adslAccount;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.order_no
     *
     * @mbggenerated
     */
    private String orderNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.update_time
     *
     * @mbggenerated
     */
    private Integer updateTime;

    /**
     *随boss工单传过来的业务编码
     */
    private String businessCodeBoss;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_gateway_business.fail_count
     *
     * @mbggenerated
     */
    private Integer failCount;


    private String provcode;

    private String useridBoss;

    private Date ordertimeBoss;

    private String devicetype;

    private String usernameBoss;

    private String useraddressBoss;

    private String contactpersonBoss;

    private String contactmannerBoss;

    private String vlanid;

    private String laninterface;

    private String ipaddress;

    private String subnetmask;

    private String defaultgateway;

    private String portSip;

    private String uriSip;

    private String defaultconnectionservice;

    private String wanInterface;


    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.id
     *
     * @return the value of t_gateway_business.id
     *
     * @mbggenerated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.id
     *
     * @param id the value for t_gateway_business.id
     *
     * @mbggenerated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.gateway_uuid
     *
     * @return the value of t_gateway_business.gateway_uuid
     *
     * @mbggenerated
     */
    public String getGatewayUuid() {
        return gatewayUuid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.gateway_uuid
     *
     * @param gatewayUuid the value for t_gateway_business.gateway_uuid
     *
     * @mbggenerated
     */
    public void setGatewayUuid(String gatewayUuid) {
        this.gatewayUuid = gatewayUuid == null ? null : gatewayUuid.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.business_code
     *
     * @return the value of t_gateway_business.business_code
     *
     * @mbggenerated
     */
    public String getBusinessCode() {
        return businessCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.business_code
     *
     * @param businessCode the value for t_gateway_business.business_code
     *
     * @mbggenerated
     */
    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode == null ? null : businessCode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.business_name
     *
     * @return the value of t_gateway_business.business_name
     *
     * @mbggenerated
     */
    public String getBusinessName() {
        return businessName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.business_name
     *
     * @param businessName the value for t_gateway_business.business_name
     *
     * @mbggenerated
     */
    public void setBusinessName(String businessName) {
        this.businessName = businessName == null ? null : businessName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.parameter_list
     *
     * @return the value of t_gateway_business.parameter_list
     *
     * @mbggenerated
     */
    public String getParameterList() {
        return parameterList;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.parameter_list
     *
     * @param parameterList the value for t_gateway_business.parameter_list
     *
     * @mbggenerated
     */
    public void setParameterList(String parameterList) {
        this.parameterList = parameterList == null ? null : parameterList.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.business_statu
     *
     * @return the value of t_gateway_business.business_statu
     *
     * @mbggenerated
     */
    public String getBusinessStatu() {
        return businessStatu;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.business_statu
     *
     * @param businessStatu the value for t_gateway_business.business_statu
     *
     * @mbggenerated
     */
    public void setBusinessStatu(String businessStatu) {
        this.businessStatu = businessStatu == null ? null : businessStatu.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.areacode
     *
     * @return the value of t_gateway_business.areacode
     *
     * @mbggenerated
     */
    public String getAreacode() {
        return areacode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.areacode
     *
     * @param areacode the value for t_gateway_business.areacode
     *
     * @mbggenerated
     */
    public void setAreacode(String areacode) {
        this.areacode = areacode == null ? null : areacode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.bandwidth
     *
     * @return the value of t_gateway_business.bandwidth
     *
     * @mbggenerated
     */
    public Integer getBandwidth() {
        return bandwidth;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.bandwidth
     *
     * @param bandwidth the value for t_gateway_business.bandwidth
     *
     * @mbggenerated
     */
    public void setBandwidth(Integer bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.create_time
     *
     * @return the value of t_gateway_business.create_time
     *
     * @mbggenerated
     */
    public Integer getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.create_time
     *
     * @param createTime the value for t_gateway_business.create_time
     *
     * @mbggenerated
     */
    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

  
    public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	/**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.gateway_password
     *
     * @return the value of t_gateway_business.gateway_password
     *
     * @mbggenerated
     */
    public String getGatewayPassword() {
        return gatewayPassword;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.gateway_password
     *
     * @param gatewayPassword the value for t_gateway_business.gateway_password
     *
     * @mbggenerated
     */
    public void setGatewayPassword(String gatewayPassword) {
        this.gatewayPassword = gatewayPassword == null ? null : gatewayPassword.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.adsl_password
     *
     * @return the value of t_gateway_business.adsl_password
     *
     * @mbggenerated
     */
    public String getAdslPassword() {
        return adslPassword;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.adsl_password
     *
     * @param adslPassword the value for t_gateway_business.adsl_password
     *
     * @mbggenerated
     */
    public void setAdslPassword(String adslPassword) {
        this.adslPassword = adslPassword == null ? null : adslPassword.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.adsl_account
     *
     * @return the value of t_gateway_business.adsl_account
     *
     * @mbggenerated
     */
    public String getAdslAccount() {
        return adslAccount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.adsl_account
     *
     * @param adslAccount the value for t_gateway_business.adsl_account
     *
     * @mbggenerated
     */
    public void setAdslAccount(String adslAccount) {
        this.adslAccount = adslAccount == null ? null : adslAccount.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.order_no
     *
     * @return the value of t_gateway_business.order_no
     *
     * @mbggenerated
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.order_no
     *
     * @param orderNo the value for t_gateway_business.order_no
     *
     * @mbggenerated
     */
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo == null ? null : orderNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.update_time
     *
     * @return the value of t_gateway_business.update_time
     *
     * @mbggenerated
     */
    public Integer getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.update_time
     *
     * @param updateTime the value for t_gateway_business.update_time
     *
     * @mbggenerated
     */
    public void setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
    }

    public String getBusinessCodeBoss() {
        return businessCodeBoss;
    }

    public void setBusinessCodeBoss(String businessCodeBoss) {
        this.businessCodeBoss = businessCodeBoss;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_gateway_business.fail_count
     *
     * @return the value of t_gateway_business.fail_count
     *
     * @mbggenerated
     */
    public Integer getFailCount() {
        return failCount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_gateway_business.fail_count
     *
     * @param failCount the value for t_gateway_business.fail_count
     *
     * @mbggenerated
     */
    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

	public String getProvcode() {
		return provcode;
	}

	public void setProvcode(String provcode) {
		this.provcode = provcode;
	}

	public String getUseridBoss() {
		return useridBoss;
	}

	public void setUseridBoss(String useridBoss) {
		this.useridBoss = useridBoss;
	}

	public Date getOrdertimeBoss() {
		return ordertimeBoss;
	}

	public void setOrdertimeBoss(Date ordertimeBoss) {
		this.ordertimeBoss = ordertimeBoss;
	}

	public String getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(String devicetype) {
		this.devicetype = devicetype;
	}

	public String getUsernameBoss() {
		return usernameBoss;
	}

	public void setUsernameBoss(String usernameBoss) {
		this.usernameBoss = usernameBoss;
	}

	public String getUseraddressBoss() {
		return useraddressBoss;
	}

	public void setUseraddressBoss(String useraddressBoss) {
		this.useraddressBoss = useraddressBoss;
	}

	public String getContactpersonBoss() {
		return contactpersonBoss;
	}

	public void setContactpersonBoss(String contactpersonBoss) {
		this.contactpersonBoss = contactpersonBoss;
	}

	public String getContactmannerBoss() {
		return contactmannerBoss;
	}

	public void setContactmannerBoss(String contactmannerBoss) {
		this.contactmannerBoss = contactmannerBoss;
	}

	public String getVlanid() {
		return vlanid;
	}

	public void setVlanid(String vlanid) {
		this.vlanid = vlanid;
	}

	public String getLaninterface() {
		return laninterface;
	}

	public void setLaninterface(String laninterface) {
		this.laninterface = laninterface;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getSubnetmask() {
		return subnetmask;
	}

	public void setSubnetmask(String subnetmask) {
		this.subnetmask = subnetmask;
	}

	public String getDefaultgateway() {
		return defaultgateway;
	}

	public void setDefaultgateway(String defaultgateway) {
		this.defaultgateway = defaultgateway;
	}

	public String getPortSip() {
		return portSip;
	}

	public void setPortSip(String portSip) {
		this.portSip = portSip;
	}

	public String getUriSip() {
		return uriSip;
	}

	public void setUriSip(String uriSip) {
		this.uriSip = uriSip;
	}

	public String getDefaultconnectionservice() {
		return defaultconnectionservice;
	}

	public void setDefaultconnectionservice(String defaultconnectionservice) {
		this.defaultconnectionservice = defaultconnectionservice;
	}

	public String getWanInterface() {
		return wanInterface;
	}

	public void setWanInterface(String wanInterface) {
		this.wanInterface = wanInterface;
	}

}