package com.cmiot.rms.services;

import com.cmiot.acs.model.Inform;
import com.cmiot.rms.common.page.PageBean;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayQueue;
import com.cmiot.rms.dao.model.HardwareAblity;

import java.util.List;
import java.util.Map;

/**
 * Created by fuwanhong on 2016/1/25.
 */
public interface GatewayInfoService {

    List<GatewayInfo> queryList(GatewayInfo gatewayInfo) ;
    
    List<GatewayInfo> queryGatewayInfoList(GatewayInfo gatewayInfo);

    List<GatewayInfo> queryByDeviceArea(GatewayInfo gatewayInfo);
    
    public Map queryList4Page(int page,int pageSize,GatewayInfo gatewayInfo);
    
    PageBean<GatewayInfo> queryListPage(PageBean<GatewayInfo> page);
    
    void addGatewayInfo(GatewayInfo gatewayInfo);

    void addSelectiveGatewayInfo(GatewayInfo gatewayInfo);

    void updateGatewayInfo(GatewayInfo gatewayInfo);

    void updateSelectGatewayInfo(GatewayInfo gatewayInfo);
    
    void delGatewayInfo(String gatewayInfoUuid);

    GatewayInfo  selectByUuid(String gatewayInfoUuid);


    GatewayInfo selectGatewayInfo(GatewayInfo gatewayInfo);

    List<GatewayInfo> queryListByIds(List<String> ids);
    
    List<GatewayInfo> getDeviceFactory();

    void updateInformGatewayInfo(Inform informReq);

    void updateGatewayFirmwareInfo(GatewayInfo record);

	void batchInsertGatewayInfo(List<GatewayInfo> allDatas);

    int insertSelective(GatewayInfo info);

     /**
     * 更新备份文件数量限制
     * @param gatewayIds 多个逗号隔开
     * @param maxNumber
     * @return
     */
     void updateBackupFileMaxNumber(String gatewayIds, int maxNumber);

    /**
     * 更新网关日志开关状态
     * @param gatewayId
     * @param status
     */
     void updateLogSwitchStatus(String gatewayId, int status);


    /**
     * 查询digest账号密码
     * @param gatewayDigestAccount
     * @return
     */
    List<String> queryDigestPassword(String gatewayDigestAccount);
    
    /**
     * 根据区域IN查询网关列表
     * */
    List<Map<String, Object>> queryGatewayListByAreas(Map<String, Object> param);
    
    /**
     *  根据网关PASSWORD更新 区域ID
     * */
    
	void updateGatewayAreaIdByPassword(GatewayInfo gatewayInfo);

	List<Map<String, Object>> queryGatewayFirmVersionAndFlowrateByAreaIds(Map<String, Object> params);
	
	
	/**
	 * 网关批量导入调用
	 * @param gatewayList
	 * @param hardwareList
	 * @param queueList
	 * @throws Exception
	 */
	void addbatchGatewayHardwareQueue(List<GatewayInfo> gatewayList, List<HardwareAblity> hardwareList,List<GatewayQueue> queueList) throws Exception;

	Map<String, Object> queryManufacturerCodeByFactoryCode(
			String gatewayFactoryCode);

    /**
     * 清楚网关宽带账号和密码
     * @param gatewayInfo
     */
    void clearPasswordAndAdslAccount(GatewayInfo gatewayInfo);

}
