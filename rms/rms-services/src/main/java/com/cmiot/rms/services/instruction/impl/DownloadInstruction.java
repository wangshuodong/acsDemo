package com.cmiot.rms.services.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Download;
import com.cmiot.rms.services.instruction.AbstractInstruction;

import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/18
 */
public class DownloadInstruction extends AbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {
        Download download = new Download();
        download.setCommandKey(getString(map.get("commandKey")));
        download.setFileType(getString(map.get("fileType")));
        download.setUrl(getString(map.get("url")));
        download.setUserName(getString(map.get("userName")));
        download.setPassWord(getString(map.get("passWord")));
        download.setTargetFileName(getString(map.get("targetFileName")));
        download.setSuccessURL(getString(map.get("successURL")));
        download.setFailureURL(getString(map.get("failureURL")));

        long size = 0;
        try {
            Object obj = map.get("fileSize");
            if(null != obj)
            {
                size = Math.round(Double.valueOf(obj.toString()));
            }
        }catch (Exception e)
        {
            size = 0;
        }

        download.setFileSize(size);

        download.setDelaySeconds(getInt(map.get("delaySeconds")));
        return download;
    }
}
