package com.cmiot.rms.services.report;


import com.cmiot.acs.model.Inform;

/**
 * CPE上报Inform处理类
 * Created by panmingguo on 2016/5/10.
 */
public interface InformHandle {
    /**
     * "0 BOOTSTRAP" 表明会话发起原因是CPE首次安装或ACS的URL发生变化。
     */
    void bootStrapEvent(Inform inform);

    /**
     * "1 BOOT" 表明会话发起原因是CPE加电或重置，包括系统首次启动，以及因任何原因而引起的重启，包括使用Reboot方法。
     *
     * @param inform
     */
    void bootEvent(Inform inform);

    /**
     * "2 PERIODIC" 表明会话发起原因是定期的Inform引起。
     *
     * @param inform
     */
    void periodicEvent(Inform inform);

    /**
     * "3 SCHEDULED" 表明会话发起原因是调用了ScheduleInform方法。
     *
     * @param inform
     */
    void scheduleEvent(Inform inform);


    /**
     * "4 VALUE CHANGE" 表明会话发起原因是一个或多个参数值的变化。该参数值包括在Inform方法的调用中。例如CPE分配了新的IP地址。
     *
     * @param inform
     */
    void valueChangeEvent(Inform inform);

    /**
     * "6 CONNECTION REQUEST" 表明会话发起原因是3.2节中定义的源自服务器的Connection Request
     *
     * @param inform
     */
    void connectionRequestEvent(Inform inform);

    /**
     * "7 TRANSFER COMPLETE" 表明会话的发起是为了表明以前请求的下载或上载（不管是否成功）已经结束，在此会话中将要调用一次或多次TransferComplete方法。
     *
     * @param inform
     */
    void transferCompleteEvent(Inform inform);

    /**
     * "8 DIAGNOSTICS COMPLETE" 当完成由ACS发起的诊断测试结束后，重新与ACS建立连接时使用。如DSL环路诊断（见附录B）。
     *
     * @param inform
     */
    void diagnosticsCompleteEvent(Inform inform);

    /**
     * "X CMCC MONITOR"硬件版本、软件版本、终端回连URL，省级数字家庭管理平台之前配置的需要监控的参数。
     */
    void xCmccMonitor(Inform inform);

    /**
     * "X CMCC BIND
     */
    void xCmccBind(Inform inform);
}
