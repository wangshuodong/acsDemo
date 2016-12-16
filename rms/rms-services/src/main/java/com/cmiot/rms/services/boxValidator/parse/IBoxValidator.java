package com.cmiot.rms.services.boxValidator.parse;

import com.cmiot.rms.services.boxValidator.result.BoxValidateResult;

/**
 * 校验器接口
 */
public interface IBoxValidator {

	/**
	 * 校验前准备工作
	 */
	void beforeValidate();

	/**
	 * 按照规则校验对象，并且把校验结果保存在ValidateResult中返回
	 * @param row
	 *            当前数据的行号
	 * @param t
	 *            需要验证的实体对象
	 * @return
	 */
	<T> BoxValidateResult boxValidate(int row, T t);

	/**
	 * 校验后清除工作
	 */
	void afterValidate();
}
