package com.cmiot.rms.services.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cmiot.rms.dao.mapper.FactoryMapper;
import com.cmiot.rms.dao.model.Factory;
import com.cmiot.rms.services.FactoryService;


@Service("factoryService")
public class FactoryServiceImpl implements FactoryService {

    @Autowired
    private FactoryMapper factoryMapper;


    @Override
    public List<Factory> queryList(Factory factory) {
        return factoryMapper.queryList(factory);
    }
    

    

    
    


}
