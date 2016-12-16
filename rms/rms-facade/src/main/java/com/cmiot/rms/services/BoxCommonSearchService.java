package com.cmiot.rms.services;

import java.util.Map;

/**
 * 公共数据查询类
 * 包括生产商查询和设备型号查询
 * Created by panmingguo on 2016/6/15.
 */
public interface BoxCommonSearchService {
    /**
     * 查询生产商信息
     *
     * @return
     */
    Map<String, Object> queryFactoryInfo(Map<String, Object> parameter);

    /**
     * 根据生产商编码查询设备型号
     *
     * @param parameter
     * @return
     */
    Map<String, Object> queryBoxModel(Map<String, Object> parameter);
}
