package com.cmiot.rms.services.boxValidator.parse.impl;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

import com.cmiot.rms.common.annotation.NotBlank;
import com.cmiot.rms.services.boxValidator.parse.IBoxValidator;
import com.cmiot.rms.services.boxValidator.result.BoxValidateResult;

/**
 * 非空校验
 */
public class BoxNotBlankValidator implements IBoxValidator {

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
				System.out.println("Excel中的数据非空校验解析异常");
			}
			if (f.isAnnotationPresent(NotBlank.class)) {
				NotBlank notBlank = f.getAnnotation(NotBlank.class);
				if (null == value || StringUtils.isBlank(value.toString())) {
					result.setMessage("导入失败,第" + row + "条数据错误," + notBlank.fieldName() + "不能为空，请按照下载的导入模版填入全部字段后再重新导入!");
					return result;
				}
			}

		}
		return result;
	}

	@Override
	public void afterValidate() {
	}
}
