package com.cmiot.acs.common;

import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by ZJL on 2016/11/14.
 */
public class CommonUtil {
    public static String PATH = null;

    /**
     * 生成GID
     *
     * @return
     */
    public static String getGid() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssS");
        return sdf.format(new Date(System.currentTimeMillis())) + RandomStringUtils.randomNumeric(9);
    }

    /**
     * 获取外部的资源文件
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static String getFilePath(String fileName) {
        if (PATH == null) {
            String myPath = CommonUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            PATH = new File(myPath).getParent() + File.separator;
        }
        return PATH + fileName;
    }

    /**
     * 判断resultCode是否为0
     *
     * @param map
     * @return
     */
    public static boolean judgeResultCode(Map<String, Object> map) {
        String code = String.valueOf(map.get("resultCode"));
        return "0".equals(code) ? true : false;
    }
}
