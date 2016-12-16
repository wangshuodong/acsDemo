package com.cmiot.rms.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxDeviceInfoMapper;
import com.cmiot.rms.dao.model.BoxDeviceInfo;
import com.cmiot.rms.services.BoxDeviceManageService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

@Service
public class BoxDeviceManageServiceImpl implements BoxDeviceManageService {

	@Autowired
	BoxDeviceInfoMapper boxDeviceInfoMapper;
	
	@Override
	public Map<String, Object> addDeviceInfo(Map<String, Object> parameter) {
		Map<String,Object> retmap = new HashMap<>();
		String factoryCode  = parameter.get("factoryCode")==null? "":parameter.get("factoryCode").toString();
		String boxModel  = parameter.get("boxModel")==null? "":parameter.get("boxModel").toString();
		String deviceName  = parameter.get("deviceName")==null? null:parameter.get("deviceName").toString();
		String remark  = parameter.get("remark")==null? "":parameter.get("remark").toString();
		if("".equals(factoryCode)){
			    retmap.put(Constant.CODE, -1);
	            retmap.put(Constant.MESSAGE, "factoryCode为空");
	            return retmap;
		}
		if("".equals(boxModel)){
			retmap.put(Constant.CODE, -1);
			retmap.put(Constant.MESSAGE, "boxModel为空");
			return retmap;
		}
		//验证该厂商该MODEL的信息是否已经存在
		BoxDeviceInfo deviceInfo = new BoxDeviceInfo();
		deviceInfo.setFactoryCode(factoryCode);
		deviceInfo.setBoxModel(boxModel);
	//	deviceInfo.setDeviceName(deviceName);
		try {
			List<Map<String, Object>> deviceList =  boxDeviceInfoMapper.selectBoxDeviceInfo(deviceInfo);
			if(deviceList != null && deviceList.size() > 0 ){
				retmap.put(Constant.CODE, -1);
				retmap.put(Constant.MESSAGE, "存在相同设备类型");
				return retmap;
			}
			deviceInfo.setId(UniqueUtil.uuid());
			deviceInfo.setDeviceName(deviceName);
			deviceInfo.setRemark(remark);
			boxDeviceInfoMapper.insert(deviceInfo);
			retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retmap.put(Constant.MESSAGE, "新增成功");
			return retmap;
		} catch (Exception e) {
			e.printStackTrace();
			retmap.put(Constant.CODE, -1);
			retmap.put(Constant.MESSAGE, "服务器内部错误");
			return retmap;
		}
	}

	@Override
	public Map<String, Object> queryDeviceInfoList(Map<String, Object> parameter) {
		Map<String,Object> retmap = new HashMap<>();
		String factoryCode  = parameter.get("factoryCode")==null? null:parameter.get("factoryCode").toString();
		String boxModel  = parameter.get("boxModel")==null? null:parameter.get("boxModel").toString();
		BoxDeviceInfo deviceInfo = new BoxDeviceInfo();
		deviceInfo.setFactoryCode(factoryCode);
		deviceInfo.setBoxModel(boxModel);
		int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;
        PageHelper.startPage(page, pageSize);
        List<Map<String, Object>> deviceList =  boxDeviceInfoMapper.selectBoxDeviceInfo(deviceInfo);
		
        retmap.put("page", page);
        retmap.put("pageSize", pageSize);
        retmap.put("total", ((Page) deviceList).getTotal());
        retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        retmap.put(Constant.MESSAGE, "机顶盒设备类型分页查询成功!");
        retmap.put(Constant.DATA, JSON.toJSON(deviceList));
        return retmap;
        
	}
	@Override
	public Map<String, Object> queryDeviceInfo(Map<String, Object> parameter) {
		Map<String,Object> retmap = new HashMap<>();
		String id  = parameter.get("id")==null? null:parameter.get("id").toString();
		if(id == null || "".equals(id)){
			retmap.put(Constant.CODE, -1);
            retmap.put(Constant.MESSAGE, "id为空");
            return retmap;
		}
		Map<String, Object> deviceInfo =  boxDeviceInfoMapper.selectByPrimaryKey(id);
		retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
		retmap.put(Constant.MESSAGE, "机顶盒设备类型查询成功!");
		retmap.put(Constant.DATA, JSON.toJSON(deviceInfo));
		return retmap;
		
	}

	@Override
	public Map<String, Object> updateDeviceInfo(Map<String, Object> parameter) {
		
		Map<String,Object> retmap = new HashMap<>();
		String id  = parameter.get("id")==null? "":parameter.get("id").toString();
		String factoryCode  = parameter.get("factoryCode")==null? "":parameter.get("factoryCode").toString();
		String boxModel  = parameter.get("boxModel")==null? "":parameter.get("boxModel").toString();
		String deviceName  = parameter.get("deviceName")==null? null:parameter.get("deviceName").toString();
		String remark  = parameter.get("remark")==null? "":parameter.get("remark").toString();
		
		if("".equals(id)){
			    retmap.put(Constant.CODE, -1);
	            retmap.put(Constant.MESSAGE, "id为空");
	            return retmap;
		}
		if("".equals(factoryCode)){
			retmap.put(Constant.CODE, -1);
			retmap.put(Constant.MESSAGE, "factoryCode为空");
			return retmap;
		}
		if("".equals(boxModel)){
			retmap.put(Constant.CODE, -1);
			retmap.put(Constant.MESSAGE, "boxModel为空");
			return retmap;
		}
		
		BoxDeviceInfo info = new BoxDeviceInfo();
		info.setFactoryCode(factoryCode);
		info.setBoxModel(boxModel);
	//	info.setDeviceName(deviceName);
		//验证该厂商该MODEL的信息是否已经存在
		List<Map<String, Object>> deviceList =  boxDeviceInfoMapper.selectBoxDeviceInfo(info);
		if(deviceList != null && deviceList.size() > 0){
			if(!id.equals(deviceList.get(0).get("id").toString())){
				retmap.put(Constant.CODE, -1);
				retmap.put(Constant.MESSAGE, "存在相同设备类型");
				return retmap;
			}
		}
		info.setId(id);
		info.setDeviceName(deviceName);
		info.setRemark(remark);
		boxDeviceInfoMapper.updateByPrimaryKey(info);
		
		retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        retmap.put(Constant.MESSAGE, "更新机顶盒设备类型成功");
        return retmap;
	}

	@Override
	public Map<String, Object> deleteDeviceInfo(Map<String, Object> parameter) {
		Map<String,Object> retmap = new HashMap<>();
		String ids  = parameter.get("ids")==null? "":parameter.get("ids").toString();
		if("".equals(ids)){
			    retmap.put(Constant.CODE, -1);
	            retmap.put(Constant.MESSAGE, "id为空");
	            return retmap;
		}
		for(String id :ids.split(",")){
			
			boxDeviceInfoMapper.deleteByPrimaryKey(id);
		}
		
		retmap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        retmap.put(Constant.MESSAGE, "删除机顶盒设备类型成功");
        return retmap;
	}

}
