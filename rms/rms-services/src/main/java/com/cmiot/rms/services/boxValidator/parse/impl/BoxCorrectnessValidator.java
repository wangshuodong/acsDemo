package com.cmiot.rms.services.boxValidator.parse.impl;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cmiot.rms.common.annotation.CorrectnessData;
import com.cmiot.rms.dao.mapper.BoxFirmwareInfoMapper;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import com.cmiot.rms.services.boxValidator.parse.IBoxValidator;
import com.cmiot.rms.services.boxValidator.result.BoxValidateResult;

/**
 * 数据正确性校验
 * 根据目前机顶盒业务导入数据中机顶盒厂家编码,机顶盒型号对应业务表t_box_factory_info与t_box_device_info最后要确认t_box_firmware_info
 */
public class BoxCorrectnessValidator implements IBoxValidator {

	@Autowired
	BoxFirmwareInfoMapper boxFirmwareInfoMapper;

	// 缓存网关设备型号
	private List<BoxFirmwareInfo> boxFirmwareInfoList;

	/**
	 * 校验前准备工作
	 */
	@Override
	public void beforeValidate() {
		boxFirmwareInfoList = boxFirmwareInfoMapper.selectByDeviceId();
	}

	/**
	 * 按照规则校验对象，并且把校验结果保存在ValidateResult中返回
	 * @param row
	 *            当前数据的行号
	 * @param t
	 *            需要验证的实体对象
	 * @return
	 */
	@Override
	public <T> BoxValidateResult boxValidate(int row, T t) {
		BoxValidateResult result = new BoxValidateResult();
		Field[] fields = t.getClass().getDeclaredFields();
		// 用于错误提示
		String modelPrompt = "";
		String fcPrompt = "";
		String vsPrompt = "";

		// 网关型号,厂家编码和版本值
		String model = "", fc = "", vs = "";

		// 保存设备ID
		Field deviceIdField = null;
		for (Field f : fields) {
			if (f.isAnnotationPresent(CorrectnessData.class)) {
				f.setAccessible(true);
				Object value = null;
				try {
					value = f.get(t);
				} catch (Exception e) {
					System.out.println("数据正确性校验解析异常");
				}

				CorrectnessData correctnessData = f.getAnnotation(CorrectnessData.class);
				if (correctnessData.columnName().equals("model")) {
					model = value.toString();
					modelPrompt = correctnessData.fieldName();
				} else if (correctnessData.columnName().equals("fc")) {
					fc = value.toString();
					fcPrompt = correctnessData.fieldName();
				} else if (correctnessData.columnName().equals("vs")) {
					vs = value.toString();
					vsPrompt = correctnessData.fieldName();
				}
			}

			if (f.getName().equals("deviceInfoUuid")) {
				deviceIdField = f;
			}

		}

		if (null == boxFirmwareInfoList) {
			boxFirmwareInfoList = boxFirmwareInfoMapper.selectByDeviceId();
		}

		boolean isPass = false;
		for (BoxFirmwareInfo info : boxFirmwareInfoList) {
			if (model.equals(info.getBoxModel()) && fc.equals(info.getFactoryCode()) && vs.equals(info.getFirmwareVersion())) {
				isPass = true;
				// 匹配到设备型号，将设备Id保存，便于后面插入数据库使用，避免后续再次查询
				if (null != deviceIdField) {
					try {
						deviceIdField.setAccessible(true);
						deviceIdField.set(t, info.getId());
					} catch (Exception e) {

					}

				}
				break;
			}
		}

		if (!isPass) {
			result.setMessage("导入失败,第" + row + "条数据错误," + modelPrompt + "," + fcPrompt + "和" + vsPrompt + "之前的对应关系不存在, 请修改后重新导入!");
			return result;
		}

		return result;
	}

	@Override
	public void afterValidate() {
		if (null != boxFirmwareInfoList) {
			boxFirmwareInfoList.clear();
		}
	}
}
