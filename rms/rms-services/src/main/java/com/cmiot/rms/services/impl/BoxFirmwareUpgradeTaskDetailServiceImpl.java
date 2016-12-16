package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxFirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.dao.model.BoxFirmwareUpgradeTaskDetail;
import com.cmiot.rms.services.BoxFirmwareUpgradeTaskDetailService;
import com.github.pagehelper.PageHelper;

/**
 * Created by fuwanhong on 2016/1/25.
 */
@Service("boxFirmwareUpgradeTaskDetailService")
public class BoxFirmwareUpgradeTaskDetailServiceImpl implements BoxFirmwareUpgradeTaskDetailService {

	@Autowired
	BoxFirmwareUpgradeTaskDetailMapper boxFirmwareUpgradeTaskDetailMapper;

	private static Logger logger = LoggerFactory.getLogger(BoxFirmwareUpgradeTaskDetailServiceImpl.class);

	@Autowired
	private AreaService amsAreaService;

	/**
	 * 根据升级任务ID和任务详情状态查询任务详情
	 * 
	 * @param parameter
	 * @return
	 */
	@Override
	public List<Map<String, Object>> queryListByIdAndStatus(Map<String, Object> parameter) {
		String upgradeTaskId = null != parameter.get("upgradeTaskId") ? parameter.get("upgradeTaskId").toString() : null;
		String type = null != parameter.get("type") ? parameter.get("type").toString() : null;
		int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
		int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

		PageHelper.startPage(page, pageSize);
		Map<String, String> para = new HashMap<>();
		para.put("upgradeTaskId", upgradeTaskId);
		para.put("type", type);
		List<Map<String, Object>> resultList = null;// boxFirmwareUpgradeTaskDetailMapper.queryListByIdAndStatus(para);

		// 通过ams查询区域名称
		if (resultList.size() > 0) {
			List<Integer> areaIds = new ArrayList<>();
			int areaId;
			for (Map<String, Object> task : resultList) {
				areaId = intConversion(task.get("area_id"));
				if (areaId != 0) {
					areaIds.add(areaId);
				}
			}

			List<Area> areas = amsAreaService.findAreasByIds(areaIds);
			if (null != areas && areas.size() > 0) {
				for (Map<String, Object> task : resultList) {
					areaId = intConversion(task.get("area_id"));
					for (Area area : areas) {
						if (area.getId() == areaId) {
							task.put("area_name", area.getName());
							break;
						}
					}
				}
			}

		}

		return resultList;
	}

	/**
	 * 添加升级任务详情
	 * 
	 * @param firmwareUpgradeTaskDetail
	 */
	// @Override
	// public void addFirmwareUpgradeTaskDetail(BoxFirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail) {
	// firmwareUpgradeTaskDetail.setId(UniqueUtil.uuid());
	// boxFirmwareUpgradeTaskDetailMapper.insert(firmwareUpgradeTaskDetail);
	// }

	/**
	 * 查询升级任务详情总数
	 * 
	 * @param upgradeTaskId
	 * @param status
	 * @return
	 */
	@Override
	public int searchTaskDetailCount(String upgradeTaskId, String status) {
		Map<String, String> map = new HashMap<>();
		map.put("upgradeTaskId", upgradeTaskId);
		map.put("status", status);
		return boxFirmwareUpgradeTaskDetailMapper.searchTaskDetailCount(map);
	}

	/**
	 * 查询非升级成功的总数
	 * 
	 * @param firmwareId
	 * @return
	 */
	// @Override
	// public int searchNoSuccessCount(String firmwareId) {
	// return boxFirmwareUpgradeTaskDetailMapper.searchNoSuccessCount(firmwareId);
	// }

	/**
	 * 根据网关ID和任务ID更新任务状态
	 * 
	 * @param record
	 */
	// @Override
	// public void updateTaskDetailStatus(BoxFirmwareUpgradeTaskDetail record) {
	// boxFirmwareUpgradeTaskDetailMapper.updateTaskDetailStatus(record);
	// }

	/**
	 * 查询升级任务详情列表
	 *
	 * @param firmwareUpgradeTaskDetail
	 * @return
	 */
	// @Override
	// public List<BoxFirmwareUpgradeTaskDetail> queryList(BoxFirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail) {
	// return boxFirmwareUpgradeTaskDetailMapper.queryList(firmwareUpgradeTaskDetail);
	// }

	/**
	 * 根据网关ID查询该网关是否存在立即升级的任务
	 *
	 * @param gatewayId
	 * @return
	 */
	// @Override
	// public BoxFirmwareUpgradeTaskDetail searchLatelyImmediatelyDetail(String gatewayId) {
	// List<BoxFirmwareUpgradeTaskDetail> details = boxFirmwareUpgradeTaskDetailMapper.searchLatelyImmediatelyDetail(gatewayId);
	// if (null != details && details.size() > 0) {
	// return details.get(0);
	// }
	// return null;
	// }

	/**
	 * 批量插入
	 *
	 * @param detailList
	 */
	@Override
	public void batchInsert(List<BoxFirmwareUpgradeTaskDetail> detailList) {
		boxFirmwareUpgradeTaskDetailMapper.batchInsert(detailList);
	}

	/**
	 * 整数类型转换
	 * 
	 * @return
	 */
	private Integer intConversion(Object obj) {
		try {
			if (null != obj) {
				return Integer.valueOf(obj.toString());
			}
		} catch (Exception e) {
			return 0;
		}

		return 0;
	}
}