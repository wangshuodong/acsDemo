package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GateWayBatchSetTaskInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/5/19.
 */
public interface GateWayBatchSetTaskInfoMapper extends BaseMapper<GateWayBatchSetTaskInfo>{

    int deleteByGateWayBatchSetTaskInfofoId(String id);

    int insert(GateWayBatchSetTaskInfo deviceTaskInfo);

    List<GateWayBatchSetTaskInfo> selectByParm(Map<String, Object> map);

    List<GateWayBatchSetTaskInfo> selectTasByGateWay(Map<String, Object> map);
}
