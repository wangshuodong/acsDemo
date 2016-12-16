package com.cmiot.rms.common.cache;

import java.io.Serializable;

/**
 *
 * Created by panmingguo on 2016/5/5.
 */
public class TemporaryObject implements Serializable {
    String requestId;

    public TemporaryObject(String requestId)
    {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
