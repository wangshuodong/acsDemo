package com.cmiot.rms.services;


import com.cmiot.rms.dao.model.Area;

import java.util.List;


/**
 * Created by wangzhen on 2016/1/20.
 */
public interface AreaService {
    Area queryArea(Area record);

	List<Area> queryList();
	
	public List queryAreaTree(String level) throws Exception;
}
