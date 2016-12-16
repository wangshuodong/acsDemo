package com.cmiot.acs.facadeimpl;

import com.cmiot.acs.ServerSetting;
import com.cmiot.acs.facade.OperationCpeFacade;
import com.cmiot.acs.model.AbstractMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行收到的下发指令
 * Created by zjial on 2016/5/4.
 */
public class OperationCpeFacadeImpl implements OperationCpeFacade {
    public static final Logger logger = LoggerFactory.getLogger(OperationCpeFacadeImpl.class);

    @Override
    public Map<String, Object> doACSEMethods(List<AbstractMethod> abstractMethodList) {
        if (abstractMethodList != null && abstractMethodList.size() == 1) {
            AbstractMethod method = abstractMethodList.get(0);
            logger.info("收到RMS指令,入参：{}", method);
            Map<String, Object> resultMap = SynchronousFacadeImpl.synchronous(method);
            logger.info("收到RMS指令,出参：{}", resultMap);
            return resultMap;
        } else {
            logger.info("收到RMS批量指令");
            Map<String, List<AbstractMethod>> groups = AcsCpeFacade.abstractMethodListGroup(abstractMethodList);
            for (List<AbstractMethod> methods : groups.values()) {
                ServerSetting.OPERATION_CPE_THREAD_POOL.execute(new AsynchronousFacadeImpl(methods));
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("resultCode", 0);
            resultMap.put("resultMessage", "批量成功，异步执行不可靠结果");
            logger.info("收到RMS批量指令,出参：{}", resultMap);
            return resultMap;
        }
    }

}


