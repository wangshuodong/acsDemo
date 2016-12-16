package com.cmiot.rms.services.boxValidator.result;

public class BoxValidateResult {

	/**
	 * 校验是否通过
	 */
	private Boolean isValid = true;

	/**
	 * 错误信息
	 */
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.isValid = false;
		this.message = message;
	}

	/**
	 * 校验是否通过
	 * @return true 通过
	 *         false 未通过
	 */
	public Boolean isValid() {
		return this.isValid;
	}
}
