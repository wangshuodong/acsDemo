package com.cmiot.rms.services.boxValidator.parse.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.cmiot.rms.common.annotation.ExcelRepetitionData;
import com.cmiot.rms.services.boxValidator.parse.IBoxValidator;
import com.cmiot.rms.services.boxValidator.result.BoxValidateResult;

/**
 * Excel中的数据重复校验
 */
public class BoxExcelRepetitionDataValidator implements IBoxValidator {

	/**
	 * 缓存满足条件的字段值和行号，用于判断重复性
	 */
	private static Map<String, Integer> cache = new HashMap<>();

	@Override
	public void beforeValidate() {

	}

	@Override
	public <T> BoxValidateResult boxValidate(int row, T t) {
		BoxValidateResult result = new BoxValidateResult();
		Field[] fields = t.getClass().getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			Object value = null;
			try {
				value = f.get(t);
			} catch (Exception e) {
				System.out.println("Excel中的数据重复校验解析异常");
			}
			if (f.isAnnotationPresent(ExcelRepetitionData.class)) {
				ExcelRepetitionData excelRepetitionData = f.getAnnotation(ExcelRepetitionData.class);
				String key = f.getName() + "_" + value.toString();
				if (cache.containsKey(key)) {
					result.setMessage("导入失败,第" + row + "条数据的" + excelRepetitionData.fieldName() + "与第" + cache.get(key) + "条重复，请修改后重新导入!");
					return result;
				} else {
					cache.put(key, row);
				}
			}
		}
		return result;
	}

	@Override
	public void afterValidate() {
		cache.clear();
	}
}
