package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.BatchSetTaskDetail;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/5/17.
 */
public interface BatchSetTaskDetailMapper extends BaseMapper<BatchSetTaskDetail>{

    int deleteByBatchSetTaskId(String id);

    int insert(BatchSetTaskDetail batchSetTaskDetail);

    List<BatchSetTaskDetail> selectByParm(Map<String, Object> map);
}
