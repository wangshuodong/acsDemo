package com.cmiot.acs.control;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Tr069控制流程管理类
 *
 * @author zjialin
 * @date 2016-2-14
 */
public class ACSProcessControlManager {
    private static ACSProcessControlManager pcManager = null;
    private ConcurrentHashMap<String, ACSProcessControl> processControlMap;

    private ACSProcessControlManager() {
        processControlMap = new ConcurrentHashMap<>();
    }

    public synchronized static ACSProcessControlManager getInstance() {
        if (pcManager == null) {
            pcManager = new ACSProcessControlManager();
        }
        return pcManager;
    }

    public boolean addProcessControl(ACSProcessControl pcl) {
        if (pcl == null) {
            return false;
        }
        processControlMap.put(pcl.getCpeId(), pcl);
        return true;
    }

    public ACSProcessControl removeProcessControl(String _cpeId) {
        return processControlMap.remove(_cpeId);
    }

    public ACSProcessControl getProcessControl(String _cpeId) {
        return processControlMap.get(_cpeId);
    }

}
