package com.cmiot.rms.services.validator.parse.impl;

import com.cmiot.rms.common.annotation.CorrectnessData;
import com.cmiot.rms.dao.mapper.DeviceInfoMapper;
import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.services.validator.parse.IValidator;
import com.cmiot.rms.services.validator.result.ValidateResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 数据正确性校验
 * Created by panmingguo on 2016/11/16.
 */
public class CorrectnessValidator implements IValidator {

    @Autowired
    DeviceInfoMapper deviceInfoMapper;

    //缓存网关设备型号
    private List<DeviceInfo> deviceInfos;
    /**
     * 校验前准备工作
     */
    @Override
    public void beforeValidate() {
        deviceInfos = deviceInfoMapper.searchAllDeviceModel();
    }

    /**
     * 按照规则校验对象，并且把校验结果保存在ValidateResult中返回
     * @param row 当前数据的行号
     * @param t 需要验证的实体对象
     * @return
     */
    @Override
    public <T> ValidateResult validate(int row, T t)
    {
        ValidateResult result = new ValidateResult();
        Field [] fields = t.getClass().getDeclaredFields();

        //用于错误提示
        String modelPrompt = "";
        String ouiPrompt = "";

        //网关型号和OUI值
        String model = "";
        String oui = "";

        //保存设备ID
        Field deviceIdField = null;
        for(Field f : fields) {

            if(f.isAnnotationPresent(CorrectnessData.class))
            {
                f.setAccessible(true);
                Object value = null;
                try {
                    value = f.get(t);
                } catch (Exception e) {
                    System.out.println("解析异常");
                }

                CorrectnessData correctnessData = f.getAnnotation(CorrectnessData.class);
                if(correctnessData.columnName().equals("model"))
                {
                    model = value.toString();
                    modelPrompt = correctnessData.fieldName();
                }
                else if(correctnessData.columnName().equals("oui"))
                {
                    oui = value.toString();
                    ouiPrompt = correctnessData.fieldName();
                }
            }

            if(f.getName().equals("deviceInfoUuid"))
            {
                deviceIdField = f;
            }

        }

        if(null == deviceInfos)
        {
            deviceInfos = deviceInfoMapper.searchAllDeviceModel();
        }

        boolean isPass = false;
        for(DeviceInfo info : deviceInfos)
        {
            if(model.equals(info.getDeviceModel()) && oui.equals(info.getDeviceFactory()))
            {
                isPass = true;
                //匹配到设备型号，将设备Id保存，便于后面插入数据库使用，避免后续再次查询
                if(null != deviceIdField)
                {
                    try
                    {
                        deviceIdField.setAccessible(true);
                        deviceIdField.set(t, info.getId());
                    }
                    catch (Exception e)
                    {

                    }

                }
                break;
            }
        }

        if(!isPass)
        {
            result.setMessage("导入失败,第"+ row +"条数据错误," + modelPrompt + "和" + ouiPrompt + "之前的对应关系不存在, 请修改后重新导入!");
            return result;
        }

        return result;
    }

    /**
     * 校验后清除工作
     */
    @Override
    public void afterValidate() {

        if(null != deviceInfos)
        {
            deviceInfos.clear();
        }
    }
}
