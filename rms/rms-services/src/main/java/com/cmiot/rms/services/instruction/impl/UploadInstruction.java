package com.cmiot.rms.services.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Upload;
import com.cmiot.rms.services.instruction.AbstractInstruction;

import java.util.Map;

/**
 * Created by panmingguo on 2016/6/6.
 */
public class UploadInstruction extends AbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {
        Upload upload = new Upload();
        upload.setCommandKey(getString(map.get("commandKey")));
        upload.setFileType(getString(map.get("fileType")));
        upload.setUrl(getString(map.get("url")));
        upload.setUserName(getString(map.get("userName")));
        upload.setPassWord(getString(map.get("passWord")));
        upload.setDelaySeconds(getInt(map.get("delaySeconds")));

        return upload;
    }
}
