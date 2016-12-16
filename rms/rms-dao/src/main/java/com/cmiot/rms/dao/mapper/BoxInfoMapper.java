package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.derivedclass.BoxBean;

public interface BoxInfoMapper {

	int deleteByPrimaryKey(String boxUuid);

	int insert(BoxInfo record);

	int insertSelective(BoxInfo record);

	BoxInfo selectByPrimaryKey(String boxUuid);

	List<BoxInfo> selectBoxInfo(BoxInfo record);

	int updateByPrimaryKeySelective(BoxInfo record);

	int updateBySnSelective(BoxInfo record);

	int updateByPrimaryKey(BoxInfo record);

	List<BoxInfo> queryListByIds(List<String> boxIds);

	List<BoxBean> queryBoxListForPage(BoxInfo boxInfo);

	List<BoxInfo> queryList4Page(Map<String, Object> map);

	int queryList4PageCount(Map<String, Object> map);

	void batchInsertBoxInfo(List<BoxInfo> allDatas);

	BoxInfo selectGatewayInfo(BoxInfo boxInfo);

	List<BoxInfo> selectBoxInfoNoRe(BoxInfo record);

	int selectBoxInfoCount(BoxInfo record);
}