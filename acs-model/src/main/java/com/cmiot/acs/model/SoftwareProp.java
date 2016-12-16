package com.cmiot.acs.model;

import java.io.Serializable;

/**
 * @author zjialin
 */
public class SoftwareProp implements Serializable {
    private static final long serialVersionUID = 1L;
    //硬件
    private String hardware;
    //版本
    private String version;

    /**
     * @see Object#equals(Object)
     */
    public SoftwareProp() {
    }

    public SoftwareProp(String h, String v) {
        hardware = h;
        version = v;
    }

    public boolean equals(Object otherOb) {
        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof SoftwareProp)) {
            return false;
        }
        SoftwareProp other = (SoftwareProp) otherOb;
        return ((hardware == null ? other.hardware == null : hardware.equals(other.hardware)) && (version == null ? other.version == null : version.equals(other.version)));
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return ((hardware == null ? 0 : hardware.hashCode()) ^ (version == null ? 0 : version.hashCode()));
    }

}
