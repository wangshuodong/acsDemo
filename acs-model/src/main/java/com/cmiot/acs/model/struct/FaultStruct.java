package com.cmiot.acs.model.struct;

import java.io.Serializable;

/**
 * 传输中的错误结构
 *
 * @author zjialin
 * @date 2016-2-7
 */
public class FaultStruct implements Serializable {
    private static final long serialVersionUID = -5306408862583246392L;

    private int faultCode;        // 错误码，无符号，9001,9002,9010,9011,9012
    private String faultString; // 错误码描述，faultCode = 0 时，faultString 为空

    public FaultStruct() {
        faultCode = 0;
        faultString = "";
    }

    public FaultStruct(int faultCode, String faultString) {
        setFault(faultCode, faultString);
    }

    /**
     * 错误码描述，faultCode = 0 时，faultString 为空
     *
     * @param faultCode
     * @param faultString
     */
    public void setFault(int faultCode, String faultString) {
        if (faultCode <= 0) {
            faultCode = 0;
            this.faultString = "";
        } else {
            this.faultCode = faultCode;
            this.faultString = faultString;
        }
    }

    public int getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(int faultCode) {
        this.faultCode = faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    @Override
    public String toString() {
        return "FaultCode:" + getFaultCode() + " FaultString:" + getFaultString();
    }
}
