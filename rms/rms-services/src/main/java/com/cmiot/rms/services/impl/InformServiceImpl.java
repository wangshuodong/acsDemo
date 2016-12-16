package com.cmiot.rms.services.impl;

import com.cmiot.rms.dao.model.InformInfo;
import com.cmiot.rms.dao.mapper.InformInfoMapper;
import com.cmiot.rms.services.InformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wangzhen on 2016/2/2.
 */
@Service("informService")
public class InformServiceImpl implements InformService {

    @Autowired
    private InformInfoMapper informInfoMapper;

    @Override
    public void addInformInfo(InformInfo informInfo) {
        informInfoMapper.insertSelective(informInfo);
    }
}
