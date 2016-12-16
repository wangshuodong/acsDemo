package com.cmiot.rms.dao.impl;

import com.cmiot.rms.dao.BaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/4/12.
 */
public class BaseDaoImpl implements BaseDao{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据sql查询返回List对象
     * @param sql
     * @param args
     * @return
     */
    @Override
    public List<Map<String, Object>> queryForMap(String sql, Object[] args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    /**
     * 查询总数
     * @param sql
     * @param args
     * @return
     */
    @Override
    public Long queryTotal(String sql, Object[] args) {
        return jdbcTemplate.queryForObject(sql, Long.class, args);
    }
}
