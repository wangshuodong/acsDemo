package com.cmiot.rms.common.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Created by panmingguo on 2016/6/21.
 */
public class StringLocalUtils {

    /**
     * 对象转换为字符串，当字符串为空串时，返回null
     * @param obj
     * @return
     */
    public static String ObjectToNull(Object obj)
    {
        String str = null != obj ? obj.toString() : null;
        return StringUtils.stripToNull(str);
    }
}
