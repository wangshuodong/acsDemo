package com.cmiot.rms.services;

import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import org.springframework.stereotype.Service;

/**
 * 批量设置触发接口
 * 触发条件1、初始安装第一次启动时触发；2、周期心跳上报时触发；3、设备重新启动时触发
 * Created by weilei on 2016/5/19.
 */
@Service
public interface BatchSetTaskTrrigerService {
    /**
     *
     * @param isHaveUpTask 是否有升级任务
     * @param sn 设备sn
     * @param oui 设备生产厂商编码
     * @param event  event 触发事件 1、0 BOOTSTRAP 2、1 BOOT 3、2 PERIODIC
     */
    void batchSetTaskTrriger(Boolean isHaveUpTask,String sn, String oui, UpgradeTaskEventEnum event);
}
