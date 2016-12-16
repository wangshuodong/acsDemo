package com.cmiot.rms.services;

import java.util.Map;

/**
 * 公共数据查询类
 * 包括制造商查询，生产商查询和设备型号查询
 * Created by panmingguo on 2016/4/8.
 */
public interface CommonSearchService {

	/**
	 * 查询所有制造商
	 *
	 * @return
	 */
	Map<String, Object> queryAllManufacturer();

	/**
	 * 根据制造商ID查询生产商
	 *
	 * @param parameter
	 * @return
	 */
	Map<String, Object> queryForManufacturerId(Map<String, Object> parameter);

	/**
	 * 根据生产商编码查询设备型号
	 *
	 * @param parameter
	 * @return
	 */
	Map<String, Object> queryDeviceModel(Map<String, Object> parameter);

	/**
	 * 查询所有设备型号
	 *
	 * @return
	 */
	Map<String, Object> queryAllDeviceModel();

	/**
	 * 根据生产商ID查询OUI
	 *
	 * @return
	 */
	Map<String, Object> queryOuiByProductionId(Map<String, Object> parameter);

	/**
	 * 制造商列表查询
	 *
	 * @return
	 */
	Map<String, Object> queryMakeInfoList(Map<String, Object> parameter);

	/**
	 * 新增或更新制造商
	 *
	 * @return
	 */
	Map<String, Object> addOrUpdateMakeInfo(Map<String, Object> parameter);

	/**
	 * 删除制造商
	 *
	 * @return
	 */
	Map<String, Object> delMakeInfo(Map<String, Object> parameter);

	/**
	 * 生产商列表查询
	 *
	 * @return
	 */
	Map<String, Object> queryManufacturerList(Map<String, Object> parameter);

	/**
	 * 新增或更新生产商
	 *
	 * @return
	 */
	Map<String, Object> addOrUpdateManufacturerInfo(Map<String, Object> parameter);

	/**
	 * 删除生产商
	 *
	 * @return
	 */
	Map<String, Object> delManufacturerInfo(Map<String, Object> parameter);

	/**
	 * OUI列表查询
	 *
	 * @return
	 */
	Map<String, Object> queryOuiList(Map<String, Object> parameter);

	/**
	 * 新增或更新OUI
	 *
	 * @return
	 */
	Map<String, Object> addOrUpdateOuiInfo(Map<String, Object> parameter);

	/**
	 * 删除OUI
	 *
	 * @return
	 */
	Map<String, Object> delOuiInfo(Map<String, Object> parameter);
	
	/**
	 * 新增制造商
	 * @param parameter
	 * @return
	 */
	Map<String, Object> addMakeInfo(Map<String, Object> parameter);
	
	/**
	 * 更新制造商
	 * @param parameter
	 * @return
	 */
	Map<String, Object> updateMakeInfo(Map<String, Object> parameter);
	
	/**
	 * 新增生产商
	 * @param parameter
	 * @return
	 */
	Map<String, Object> addManufacturerInfo(Map<String, Object> parameter);
	
	/**
	 * 更新生产商
	 * @param parameter
	 * @return
	 */
	Map<String, Object> updateManufacturerInfo(Map<String, Object> parameter);
	
	/**
	 * 新增OUI
	 * @param parameter
	 * @return
	 */
	Map<String, Object> addOuiInfo(Map<String, Object> parameter);
	
	/**
	 * 更新OUI
	 * @param parameter
	 * @return
	 */
	Map<String, Object> updateOuiInfo(Map<String, Object> parameter);
}
