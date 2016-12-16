package com.cmiot.rms.services.boxValidator.parse.impl;

import java.lang.reflect.Field;

import org.springframework.beans.factory.annotation.Autowired;

import com.cmiot.rms.common.annotation.DBRepetitionData;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.boxValidator.parse.IBoxValidator;
import com.cmiot.rms.services.boxValidator.result.BoxValidateResult;

/**
 * 是否与DB中的数据重复校验
 */
public class BoxDBRepetitionDataValidator implements IBoxValidator {

	@Autowired
	BoxInfoMapper boxInfoMapper;

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
				System.out.println("是否与DB中的数据重复校验解析异常");
			}
			if (f.isAnnotationPresent(DBRepetitionData.class)) {
				DBRepetitionData dbRepetitionData = f.getAnnotation(DBRepetitionData.class);
				BoxInfo serchInfo = new BoxInfo();
				if (dbRepetitionData.columnName().equals("mac")) {
					serchInfo.setBoxMacaddress(value.toString());
				} else if (dbRepetitionData.columnName().equals("sn")) {
					serchInfo.setBoxSerialnumber(value.toString());
				}
				int count = boxInfoMapper.selectBoxInfoCount(serchInfo);
				if (count > 0) {
					result.setMessage("导入失败,第" + row + "条数据的" + dbRepetitionData.fieldName() + "已经存在，请修改后重新导入!");
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
