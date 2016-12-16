package com.cmiot.rms.dao.mapper;

import java.util.List;

import com.cmiot.rms.dao.model.BoxWorkOrder;

public interface BoxWorkOrderMapper {
	List<BoxWorkOrder> queryList4Page(BoxWorkOrder boxWorkOrder);

	BoxWorkOrder queryBusinessDetail(String orderNo);
	
	int deleteBoxWorkOrder(String orderNo);
	
	int updateBoxWorkOrder(BoxWorkOrder boxWorkOrder);
}
