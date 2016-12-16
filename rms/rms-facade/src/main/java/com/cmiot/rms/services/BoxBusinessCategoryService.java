package com.cmiot.rms.services;

import java.util.Map;

/**
 * box业务编码接口
 * @author zhangchuan
 *
 */
public interface BoxBusinessCategoryService {
	/**
     * 新增业务编码，适配一个业务编码对应多个业务模板
     * @param params
     * @return
     */
    public Map<String, Object> addBoxBusinessCategory(Map<String, Object> params);

    /**
     * 更新业务编码，适配一个业务编码对应多个业务模板
     * @param params
     * @return
     */
    public Map<String, Object> updateBoxBusinessCategory(Map<String, Object> params);

    /**
     * 查询业务编码，适配一个业务编码对应多个业务模板
     * @param params
     * @return
     */
    public Map<String, Object> queryBoxBusinessCategory(Map<String, Object> params);

    /**
     * 删除业务编码，适配一个业务编码对应多个业务模板
     * @param params
     * @return
     */
    public Map<String, Object> deleteBoxBusinessCategory(Map<String, Object> params);
    
    public Map<String, Object> findBoxBusinessCategory(Map<String, Object> params);
}
