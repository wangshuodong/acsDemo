package com.cmiot.rms.services;

import java.util.Map;

public interface DiagnoseThresholdValueService {

	/**
	 * 查询诊断阈值
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> queryDiagnoseThresholdValue(Map<String, Object> parameter);

	/**
	 * 保存或更新阈值
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> saveDiagnoseThresholdValue(Map<String, Object> parameter);

}
