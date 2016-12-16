package com.cmiot.rms.services;

import java.util.List;

import com.cmiot.rms.dao.model.Factory;


public interface FactoryService {
	
	List<Factory> queryList(Factory factory);


}