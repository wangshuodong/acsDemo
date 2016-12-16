package com.cmiot.rms.services;

import java.util.Map;


/**
 * zoujiang
 */
public interface BusinessCategoryService{

    public Map<String, Object> addBusinessCategory(Map<String, Object> params);
    
    public Map<String, Object> findBusinessCategory(Map<String, Object> params);
    
    public Map<String, Object> updateBusinessCategory(Map<String, Object> params);
    
    public Map<String, Object> deleteBusinessCategory(Map<String, Object> params);
    
    public Map<String, Object> queryBusinessCategory(Map<String, Object> params);
    /**
     * 新增业务编码（安徽定制）
     * @param params
     * @return
     */
    public Map<String, Object> addAHBusinessCategory(Map<String, Object> params);
    /**
     * 分页查询业务编码（安徽定制）
     * @param params
     * @return
     */
    public Map<String, Object> queryAHBusinessCategory(Map<String, Object> params);

    /**
     * 新增业务编码，适配一个业务编码对应多个业务模板
     * @param params
     * @return
     */
    public Map<String, Object> addBusinessCategoryNew(Map<String, Object> params);

    /**
     * 更新业务编码，适配一个业务编码对应多个业务模板
     * @param params
     * @return
     */
    public Map<String, Object> updateBusinessCategoryNew(Map<String, Object> params);

    /**
     * 查询业务编码，适配一个业务编码对应多个业务模板
     * @param params
     * @return
     */
    public Map<String, Object> queryBusinessCategoryNew(Map<String, Object> params);

    /**
     * 删除业务编码，适配一个业务编码对应多个业务模板
     * @param params
     * @return
     */
    public Map<String, Object> deleteBusinessCategoryNew(Map<String, Object> params);
}
