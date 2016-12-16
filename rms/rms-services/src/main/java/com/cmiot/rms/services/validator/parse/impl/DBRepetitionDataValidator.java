package com.cmiot.rms.services.validator.parse.impl;

import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.common.annotation.DBRepetitionData;
import com.cmiot.rms.services.validator.parse.IValidator;
import com.cmiot.rms.services.validator.result.ValidateResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;

/**
 * 是否与DB中的数据重复校验
 * Created by panmingguo on 2016/11/15.
 */
public class DBRepetitionDataValidator implements IValidator {

    @Autowired
    GatewayInfoMapper gatewayInfoMapper;

    /**
     * 校验前准备工作
     */
    @Override
    public void beforeValidate() {

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
        for(Field f : fields) {
            f.setAccessible(true);
            Object value = null;
            try {
                value = f.get(t);
            } catch (Exception e) {
                System.out.println("解析异常");
            }

            if(f.isAnnotationPresent(DBRepetitionData.class))
            {
                DBRepetitionData dbRepetitionData = f.getAnnotation(DBRepetitionData.class);
                GatewayInfo serchInfo = new GatewayInfo();
                if(dbRepetitionData.columnName().equals("mac"))
                {
                    serchInfo.setGatewayMacaddress(value.toString());
                }
                else if(dbRepetitionData.columnName().equals("sn"))
                {
                    serchInfo.setGatewaySerialnumber(value.toString());
                }

                int count = gatewayInfoMapper.selectGatewayCount(serchInfo);
                if(count > 0)
                {
                    result.setMessage("导入失败,第"+ row +"条数据的" + dbRepetitionData.fieldName() +"已经存在，请修改后重新导入!");
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * 校验后清除工作
     */
    @Override
    public void afterValidate() {

    }
}
