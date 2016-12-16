package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.common.page.PageBean;

/**
 * @Author xukai
 * Date 2016/1/25
 */
public interface BaseMapper<T> {
    PageBean<T> queryList4Page(PageBean<T> pagerBean);
}
