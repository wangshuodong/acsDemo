package com.cmiot.rms.services.boxValidator;

import java.util.ArrayList;
import java.util.List;

import com.cmiot.rms.services.boxValidator.parse.IBoxValidator;
import com.cmiot.rms.services.boxValidator.result.BoxValidateResult;

/**
 * 校验器管理类，通过spring注解方式注入校验器
 */
public class BoxValidatorManagement {

	// 校验器列表，通过spring注入
	private List<IBoxValidator> boxValidatorList = new ArrayList<IBoxValidator>();

	public BoxValidatorManagement() {
	}

	/**
	 * 对传入的对象根据注册的校验依次进行校验
	 * @param row
	 * @param t
	 * @param <T>
	 * @return
	 */
	public <T> BoxValidateResult validate(int row, T t) {
		BoxValidateResult result = new BoxValidateResult();

		// 校验前准备工作
		for (IBoxValidator boxValidator : boxValidatorList) {
			boxValidator.beforeValidate();
		}

		try {
			for (IBoxValidator boxValidator : boxValidatorList) {
				result = boxValidator.boxValidate(row, t);
				if (!result.isValid()) {
					// 失败后清除工作
					afterValidate();
					return result;
				}
			}
		} finally {
			// 完成后清除工作
			afterValidate();
		}

		return result;
	}

	/**
	 * 校验后清除工作
	 */
	private void afterValidate() {
		for (IBoxValidator boxValidator : boxValidatorList) {
			boxValidator.afterValidate();
		}
	}

	public List<IBoxValidator> getBoxValidatorList() {
		return boxValidatorList;
	}

	public void setBoxValidatorList(List<IBoxValidator> boxValidatorList) {
		this.boxValidatorList = boxValidatorList;
	}

}
