package com.cmiot.rms.services;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import com.cmiot.rms.dao.model.GatewayInfo;

/**
 * 升级任务发服务类（内部使用，主要在事件触发升级时使用）
 * Created by panmingguo on 2016/5/18.
 */
public interface FirmwareUpgradeTaskInnerService {

    /**
     * 网关上报Inform信息时，判断是否配置相关事件的升级任务，如有，执行升级任务
     * 目前存在4种事件可以配置升级任务：1:初始安装第一次启动时 2：周期心跳上报时 3：设备重新启动时 4：参数改变
     *
     * @param gatewayInfo
     * @param event
     */
    Boolean executeUpgradeTask(GatewayInfo gatewayInfo, UpgradeTaskEventEnum event);


    /**
     * 获取升级任务指令
     * @param gatewayInfo
     * @return
     */
    AbstractMethod getUpgradeInstruction(GatewayInfo gatewayInfo);
}
