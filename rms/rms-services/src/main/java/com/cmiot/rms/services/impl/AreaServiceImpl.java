package com.cmiot.rms.services.impl;

import com.cmiot.rms.dao.model.Area;
import com.cmiot.rms.dao.mapper.AreaMapper;
import com.cmiot.rms.services.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangzhen on 2016/1/20.
 */
@Service("areaService")
public class AreaServiceImpl implements AreaService {

    @Autowired
    private AreaMapper areaMapper;
    

    @Override
    public List<Area> queryList() {
        return areaMapper.queryList();
    }


	@Override
	public Area queryArea(Area record) {
		return areaMapper.queryArea(record);
	}
	
	/**
	 * 功能:查询
	 * @param level 表示节点的地区类型
	 * @return
	 */
	@Override
	public List queryAreaTree(String level) throws Exception {
		List<Area> arList=areaMapper.queryList();
		
		List list=createTreeLevel(arList, level);
		
		return list;
	}
	
	//递归处理list为MapTree格式
	public List createTreeLevel(List<Area> arList ,String level){
		List list=new ArrayList();
		for(int i=0;i<arList.size();i++){
			Area area=arList.get(i);
			if(area.getAreaType().equalsIgnoreCase(level)){
				Map mapTree=new ConcurrentHashMap();
				mapTree.put("id", area.getAreaId());
				mapTree.put("name", area.getAreaName());
				mapTree.put("pId", area.getAreaParentAreaId());
				List relist=createTree(arList, area.getAreaId());
				if(relist.size()>0 && !relist.isEmpty()){
					mapTree.put("children", relist);
				}
				list.add(mapTree);
			}
		}
		
		return list;
	}
	
	//递归处理list为MapTree格式
	public List createTree(List<Area> arList ,String AreaId){
		List list=new ArrayList();
		for(int i=0;i<arList.size();i++){
			Area area=arList.get(i);
			if(area.getAreaParentAreaId().equalsIgnoreCase(AreaId)){
				Map mapTree=new ConcurrentHashMap();
				mapTree.put("id", area.getAreaId());
				mapTree.put("name", area.getAreaName());
				mapTree.put("pId", area.getAreaParentAreaId());
				List reList=createTree(arList, area.getAreaId());
				if(reList.size()>0 && !list.isEmpty()){
					mapTree.put("children", reList);
				}
				list.add(mapTree);
			}
		}
		
		return list;
	}
    
    
}
