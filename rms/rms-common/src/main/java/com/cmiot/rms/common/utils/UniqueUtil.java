package com.cmiot.rms.common.utils;

import java.util.UUID;

/**
 * 唯一性工具
 * Created by wangzhen on 2016/1/25.
 */
public class UniqueUtil {
    /**
     * 获取UUID
     */
    public static String uuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }
}
