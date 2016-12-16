package com.cmiot.rms.dao;

import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/4/12.
 */
public interface BaseDao {
    /**
     * 根据sql查询返回List对象
     * @param sql
     * @param args
     * @return
     */
    List<Map<String, Object>> queryForMap(String sql, Object[] args);

    /**
     * 查询总数
     * @param sql
     * @param args
     * @return
     */
    Long queryTotal(String sql, Object[] args);

}
