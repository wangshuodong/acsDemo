package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.BatchSetTask;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/5/17.
 */
public interface BatchSetTaskMapper extends BaseMapper<BatchSetTask>  {

    int deleteByPrimaryKey(String id);

    int insert(BatchSetTask batchSetTask);

    BatchSetTask selectByPrimaryKey(String id);

    List<BatchSetTask> selectByParm(Map<String, Object> map);

    int selectCountByParm(Map<String, Object> map);

    int updateByPrimaryKey(BatchSetTask batchSetTask);
}
