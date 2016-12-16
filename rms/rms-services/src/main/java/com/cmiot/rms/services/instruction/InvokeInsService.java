package com.cmiot.rms.services.instruction;


import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/17
 */
public interface InvokeInsService {

    Map<String, Object> executeOne(Map<String, Object> map);

    Map<String, Object> executeBatch(Map<String, Object> map);
}
