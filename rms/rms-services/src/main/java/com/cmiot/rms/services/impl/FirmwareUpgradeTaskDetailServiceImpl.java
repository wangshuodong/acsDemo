package com.cmiot.rms.services.impl;

import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.model.FirmwareUpgradeTaskDetail;
import com.cmiot.rms.dao.mapper.FirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.services.FirmwareUpgradeTaskDetailService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fuwanhong on 2016/1/25.
 */
@Service("firmwareUpgradeTaskDetailService")
public class FirmwareUpgradeTaskDetailServiceImpl implements FirmwareUpgradeTaskDetailService {

    @Autowired
    FirmwareUpgradeTaskDetailMapper firmwareUpgradeTaskDetailMapper;

	private static Logger logger = LoggerFactory.getLogger(FirmwareUpgradeTaskDetailServiceImpl.class);

    @Autowired
    private AreaService amsAreaService;
    /**
     * 根据升级任务ID和任务详情状态查询任务详情
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryListByIdAndStatus(Map<String, Object> parameter) {
        String upgradeTaskId = null != parameter.get("upgradeTaskId") ? parameter.get("upgradeTaskId").toString() : null;
        String type = null != parameter.get("type") ? parameter.get("type").toString() : null;
        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()):1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()):10;


        PageHelper.startPage(page, pageSize);
        Map<String, String> para = new HashMap<>();
        para.put("upgradeTaskId", upgradeTaskId);
        para.put("type", type);
        List<Map<String, Object>> resultList = firmwareUpgradeTaskDetailMapper.queryListByIdAndStatus(para);

		long total = ((Page)resultList).getTotal();

		Map<String, Object> ret = new HashMap<>();
		ret.put("total", total);

        //通过ams查询区域名称
        if(resultList.size() > 0)
        {
            List<Integer> areaIds = new ArrayList<>();
            int areaId;
            for(Map<String, Object> task : resultList)
            {
                areaId = intConversion(task.get("area_id"));
                if(areaId != 0)
                {
                    areaIds.add(areaId);
                }
            }

            List<Area> areas = amsAreaService.findAreasByIds(areaIds);
            if(null != areas && areas.size() > 0)
            {
                for(Map<String, Object> task : resultList)
                {
                    areaId = intConversion(task.get("area_id"));
                    for(Area area : areas)
                    {
                        if(area.getId() == areaId)
                        {
                            task.put("area_name", area.getName());
                            break;
                        }
                    }
                }
            }

		}
		ret.put("list", resultList);
		return ret;
	}

	/**
	 * 添加升级任务详情
	 *
	 * @param firmwareUpgradeTaskDetail
	 */
	@Override
	public void addFirmwareUpgradeTaskDetail(FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail) {
		firmwareUpgradeTaskDetail.setId(UniqueUtil.uuid());
		firmwareUpgradeTaskDetailMapper.insert(firmwareUpgradeTaskDetail);
	}

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
		return firmwareUpgradeTaskDetailMapper.searchTaskDetailCount(map);
	}

	/**
	 * 查询非升级成功的总数
	 *
	 * @param firmwareId
	 * @return
	 */
	@Override
	public int searchNoSuccessCount(String firmwareId) {
		return firmwareUpgradeTaskDetailMapper.searchNoSuccessCount(firmwareId);
	}

	/**
	 * 根据网关ID和任务ID更新任务状态
	 *
	 * @param record
	 */
	@Override
	public void updateTaskDetailStatus(FirmwareUpgradeTaskDetail record) {
		firmwareUpgradeTaskDetailMapper.updateTaskDetailStatus(record);
	}

	/**
	 * 查询升级任务详情列表
	 *
	 * @param firmwareUpgradeTaskDetail
	 * @return
	 */
	@Override
	public List<FirmwareUpgradeTaskDetail> queryList(FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail) {
		return firmwareUpgradeTaskDetailMapper.queryList(firmwareUpgradeTaskDetail);
	}

	/**
	 * 根据网关ID查询该网关是否存在立即升级的任务
	 *
	 * @param gatewayId
	 * @return
	 */
	@Override
	public FirmwareUpgradeTaskDetail searchLatelyImmediatelyDetail(String gatewayId) {
		List<FirmwareUpgradeTaskDetail> details = firmwareUpgradeTaskDetailMapper.searchLatelyImmediatelyDetail(gatewayId);
		if (null != details && details.size() > 0) {
			return details.get(0);
		}
		return null;
	}

    /**
     * 批量插入
     *
     * @param detailList
     */
    @Override
    public void batchInsert(List<FirmwareUpgradeTaskDetail> detailList) {
        firmwareUpgradeTaskDetailMapper.batchInsert(detailList);
    }

	/**
	 * 查询网关升级数，用于判断网关是否在升级中
	 *
	 * @param gatewayId
	 * @return
	 */
	@Override
	public int searchProcessingCount(String gatewayId) {
		return firmwareUpgradeTaskDetailMapper.searchProcessingCount(gatewayId);
	}

	/**
     * 整数类型转换
     * @return
     */
    private Integer intConversion(Object obj)
    {
        try
        {
            if(null != obj)
            {
                return Integer.valueOf(obj.toString());
            }
        }
        catch (Exception e)
        {
            return 0;
        }

        return 0;
    }
}
