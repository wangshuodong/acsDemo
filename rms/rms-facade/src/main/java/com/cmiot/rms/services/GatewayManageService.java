package com.cmiot.rms.services;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 网关管理接口
 */
public interface GatewayManageService {
  
    /**
     * 功能:dubbo服务接口、网关分页查询功能
     * @param map 请求参数
     * @return
     */
    public Map<String,Object> queryList4Page(Map<String,Object> map) ;
    
    /**
     * 功能:dubbo服务接口、网关详情查询功能
     * @param map 请求参数
     * @return
     */
    public Map<String,Object> queryGatewayDetail(Map<String,Object> map) ;

    /**
     * 文件上传
     * @param parameter
     * @return
     */
    public Map<String, Object> uploadFile(Map<String, Object> parameter);

    /**
     * 重启
     * @param parameter
     * @return
     */
    public Map<String, Object> reboot(Map<String, Object> parameter);

    /**
     * 恢复出厂设置
     * @param parameter
     * @return
     */
    public Map<String, Object> factoryReset(Map<String, Object> parameter);

    /**
     * 5.6.6.修改网关用户管理员密码
     * @param parameter
     * @return
     */
    Map<String, Object> setHgAdminPwd(Map<String, Object> parameter);
    
    /**
     * 查询网关告警信息
     * */
    Map<String, Object> queryAlarmInfo(Map<String, Object> params);
    
    /**
     * 增加创建对象实例
     * */
    Map<String, Object> addObjectInfo(Map<String, Object> params);
    
    /**
     * 删除对象实例
     * */
    Map<String, Object> deleteObjectInfo(Map<String, Object> params);
    
    /**
     * 修改设备维护账号和密码
     * */
    Map<String, Object> modFamilyAccountPwd(Map<String, Object> params);

    /**
     * 查询区域
     * */
    Map<String, Object> queryGatewayBaseInfo(Map<String, Object> params);
    
    /**
     * 查询单个网关在线状态
     * @param params
     * @return
     */
    Map<String, Object> queryGatewayState(Map<String, Object> params);

    
    /**
     * WAN口状态查询，上下行连接速率
     * */
    Map<String, Object> getComplexData(Map<String, Object> params);
    /**
     * 查询pppoe状态和上网账号
     * @param params
     * @return
     */
    Map<String, Object> getPppoeAndAccount(Map<String, Object> params);


    /**
     * 查询对象节点相关属性
     * @param params
     * @return
     */
    Map<String, Object> queryObjectInfo(Map<String, Object> params);

    /**
     * 修改对象节点相关属性
     * @param params
     * @return
     */
    Map<String, Object> modObjectInfo(Map<String, Object> params);

    /**
     * 网关批量导入功能
     * @param parameter
     * @return
     */
    Map<String, Object> ImportGatewayInfo(Map<String, Object> parameter);
    
    /**
     * 同步网关在线状态
     * @param parameter
     * @return
     */
    Map<String, Object> syncGatewayOnlineStatus(Map<String, Object> parameter);

    /**
     * 业务类型查询
     * @param
     * @return
     */
    Map<String, Object> getBusinessCategory(Map<String, Object> parameter);

    /**
     * 设备类型查询
     * @param
     * @return
     */
    Map<String, Object> getDeviceInfoList(Map<String, Object> parameter);

    /**
     * 查询网关上下行速率
     * @param
     * @return
     */
    Map<String, Object> queryGatewayUpAndDownSpeed(Map<String, Object> parameter);

    /**
     * 查询网关上下行速率(通过网关mac查询)
     * @param
     * @return
     */
    Map<String, Object> queryGatewaySpeedByMac(Map<String, Object> parameter);

    /**
     * 查询网关类型配置项
     * @param
     * @return
     */
    Map<String, Object> queryGatewayTypeByConf();


    /**
     * 解绑网关
     * @param
     * @return
     */
    Map<String, Object> unbindGateway(Map<String, Object> parameter);


    /**
     * 根据网关mac查询网关信息
     * （bms使用）
     * @param parameter
     * @return
     */
    Map<String, Object> queryGatewayByMac(Map<String, Object> parameter);
    
    /**
     * 通过区域id查询拥有网关数量（提供给AMS）
     * @param areaId
     * @return
     */
    Integer queryGatewayCountByAreaId(String areaId);
    /**
     * 通过区域id列表查询拥有网关数量（提供给AMS）
     * @param areaId
     * @return
     */
    Integer queryGatewayCountByAreaList(List<String> areaIds);
}
