package com.cmiot.rms.dao.model;

import java.util.Date;

public class BoxDiagnoseLog {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_box_diagnose_log.id
     *
     * @mbggenerated
     */
    private String id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_box_diagnose_log.box_macaddress
     *
     * @mbggenerated
     */
    private String boxMacaddress;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_box_diagnose_log.diagnose_operator
     *
     * @mbggenerated
     */
    private String diagnoseOperator;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_box_diagnose_log.diagnose_type
     *
     * @mbggenerated
     */
    private Integer diagnoseType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_box_diagnose_log.diagnose_time
     *
     * @mbggenerated
     */
    private Date diagnoseTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_box_diagnose_log.id
     *
     * @return the value of t_box_diagnose_log.id
     *
     * @mbggenerated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_box_diagnose_log.id
     *
     * @param id the value for t_box_diagnose_log.id
     *
     * @mbggenerated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_box_diagnose_log.box_macaddress
     *
     * @return the value of t_box_diagnose_log.box_macaddress
     *
     * @mbggenerated
     */
    public String getBoxMacaddress() {
        return boxMacaddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_box_diagnose_log.box_macaddress
     *
     * @param boxMacaddress the value for t_box_diagnose_log.box_macaddress
     *
     * @mbggenerated
     */
    public void setBoxMacaddress(String boxMacaddress) {
        this.boxMacaddress = boxMacaddress == null ? null : boxMacaddress.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_box_diagnose_log.diagnose_operator
     *
     * @return the value of t_box_diagnose_log.diagnose_operator
     *
     * @mbggenerated
     */
    public String getDiagnoseOperator() {
        return diagnoseOperator;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_box_diagnose_log.diagnose_operator
     *
     * @param diagnoseOperator the value for t_box_diagnose_log.diagnose_operator
     *
     * @mbggenerated
     */
    public void setDiagnoseOperator(String diagnoseOperator) {
        this.diagnoseOperator = diagnoseOperator == null ? null : diagnoseOperator.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_box_diagnose_log.diagnose_type
     *
     * @return the value of t_box_diagnose_log.diagnose_type
     *
     * @mbggenerated
     */
    public Integer getDiagnoseType() {
        return diagnoseType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_box_diagnose_log.diagnose_type
     *
     * @param diagnoseType the value for t_box_diagnose_log.diagnose_type
     *
     * @mbggenerated
     */
    public void setDiagnoseType(Integer diagnoseType) {
        this.diagnoseType = diagnoseType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_box_diagnose_log.diagnose_time
     *
     * @return the value of t_box_diagnose_log.diagnose_time
     *
     * @mbggenerated
     */
    public Date getDiagnoseTime() {
        return diagnoseTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_box_diagnose_log.diagnose_time
     *
     * @param diagnoseTime the value for t_box_diagnose_log.diagnose_time
     *
     * @mbggenerated
     */
    public void setDiagnoseTime(Date diagnoseTime) {
        this.diagnoseTime = diagnoseTime;
    }
}