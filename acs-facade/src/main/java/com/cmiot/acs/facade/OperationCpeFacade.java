package com.cmiot.acs.facade;

import com.cmiot.acs.model.AbstractMethod;

import java.util.List;
import java.util.Map;

/**
 * Created by zjial on 2016/5/3.
 */
public interface OperationCpeFacade {
    /**
     * 发送Methods到CPE
     *
     * @param abstractMethodList
     * @return
     */
    Map<String, Object> doACSEMethods(List<AbstractMethod> abstractMethodList);
}
