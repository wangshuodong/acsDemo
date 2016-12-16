/**
 * 
 */
package com.cmiot.rms.dao.mapper;

import java.util.List;

import com.cmiot.rms.dao.model.derivedclass.GatewayBusinessBean;

/**
 * @author heping
 *
 */
public interface GatewayBusinessBeanMapper {
	List<GatewayBusinessBean> queryList4Page(GatewayBusinessBean bean);
	
	GatewayBusinessBean queryBusinessDetail(String orderNo);
}
