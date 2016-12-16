package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayQueue;
import com.cmiot.rms.dao.model.derivedclass.GatewayBean;

import java.util.List;
import java.util.Map;

public interface GatewayInfoMapper  {
	int deleteByPrimaryKey(String gatewayInfoUuid);

	int insert(GatewayInfo record);

	int insertSelective(GatewayInfo record);

	GatewayInfo selectByPrimaryKey(String gatewayInfoUuid);
	
	List<GatewayBean> queryList4Page(GatewayInfo gatewayInfo);

	List<GatewayBean> queryList4PageNoArea(GatewayInfo gatewayInfo);

	List<GatewayInfo> queryGateWayInfoListPage(GatewayInfo gatewayInfo);

	int updateByPrimaryKeySelective(GatewayInfo record);

	int updateByPrimaryKey(GatewayInfo record);

	List<GatewayInfo> queryList(GatewayInfo gatewayInfo);
	
	long queryCount(Map<String, Object> map);
	
	List<GatewayInfo> queryPage(Map<String, Object> map);

	List<GatewayInfo> queryByDeviceArea(GatewayInfo gatewayInfo);

	GatewayInfo selectGatewayInfo(GatewayInfo gatewayInfo);

	List<GatewayInfo> queryListByIds(List<String> ids);

	/**
	 * 功能:查询网关设备厂商
	 */
	List<GatewayInfo> getDeviceFactory();

	List<GatewayInfo> queryGatewayInfoList(GatewayInfo gatewayInfo);

	List<GatewayInfo> queryListPage(GatewayInfo gatewayInfo);

	int updateStatus(GatewayInfo gatewayInfo);

	List<Map<String, Object>> queryGatewayAlarmInfo(String[] codes);

	int updateGatewayFirmwareInfo(GatewayInfo record);

	void batchInsertGatewayInfo(List<GatewayInfo> allDatas);

	void batchInsertGatewayInfoNew (List<GatewayInfo> allDatas);

	/**
	 * 更新备份文件数量限制
	 * @param para
	 * @return
     */
	int updateBackupFileMaxNumber( Map<String, Object> para);

	/**
	 * 更新网关日志开关状态
	 * @param para
	 * @return
     */
	int updateLogSwitchStatus(Map<String, Object> para);


	/**
	 * 查询Digest用户密码
	 * @param gatewayDigestAccount
	 * @return
     */
	List<Map<String, Object>> queryDigestPassword(String gatewayDigestAccount);

	List<Map<String, Object>> queryGatewayListByAreas(Map<String, Object> areaIds);

	void updateGatewayAreaIdByPassword(GatewayInfo gatewayInfo);
	
	List<Map<String, Object>> queryGatewayFirmVersionAndFlowrateByAreaIds(Map<String, Object> areaIds);

	int updateGatewayByMac(GatewayInfo gatewayInfo);
	
	List<GatewayInfo> queryGatewayListById(List<GatewayQueue> list);

	int unBindPasswordAndAdslAccount(GatewayInfo gatewayInfo);
	//通过adsl账号修改业务状态
	int updateGatewayBusinessStatusByAdsl(GatewayInfo gatewayInfo);

	int countSelectGatewayInfoByCode(String gatewayFactoryCode);

	Map<String, Object> queryManufacturerCodeByFactoryCode(
			String gatewayFactoryCode);

	List<GatewayInfo> selectGatewayInfoList(GatewayInfo gatewayInfo);

	List<GatewayInfo> selectGatewayInfoListByWorkOrder(GatewayBusiness gatewayBusiness);


	int clearPasswordAndAdslAccount(GatewayInfo gatewayInfo);

	void updateGatewayFactoryCode(Map<String, Object> par);

	List<GatewayInfo> selectByPppoeAccounts(List<String> pppoeAccountList);

	int selectGatewayCount(GatewayInfo gatewayInfo);

	List<Map<String, Object>> selectByMac(List<String> macList);
	
	Integer queryGatewayCountByAreaId(String areaId);
	
	Integer queryGatewayCountByAreaList(List<String> areaIds);
	
	List<Map<String,Object>> selectDeviceOrOuiOrFirmwareInfo(Map<String, Object> sql);
}
