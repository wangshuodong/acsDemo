package com.cmiot.rms.services.boxManager.instruction;


import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/17
 */
public interface BoxInvokeInsService {

    Map<String, Object> executeOne(Map<String, Object> map);

    Map<String, Object> executeBatch(Map<String, Object> map);
}
