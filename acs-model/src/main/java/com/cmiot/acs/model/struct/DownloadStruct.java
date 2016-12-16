package com.cmiot.acs.model.struct;

import java.io.Serializable;

/**
 * Created by zjialin on 2016/3/3.
 */
public class DownloadStruct implements Serializable {
    private static final long serialVersionUID = 8922200910696171480L;
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DownloadStruct{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

