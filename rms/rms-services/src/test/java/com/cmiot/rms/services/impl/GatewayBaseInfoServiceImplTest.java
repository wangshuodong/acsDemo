package com.cmiot.rms.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @Description
 * @Author Henry
 * @Date 16/5/10
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
        "file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class GatewayBaseInfoServiceImplTest {

    @Autowired
    private GatewayBaseInfoServiceImpl baseInfoService;

    @Test
    public void testGetHgResourceUsage() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "GET");
        params.put("ID", 123);
        params.put("CmdType", "GET_HG_RESOURCE_USAGE");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
        p.put("MAC", "1C25E10123451");
        params.put("Parameter", p);

        Map<String, Object> r = baseInfoService.getHgResourceUsage(params);
        assertEquals(0, r.get("Result"));

        p.put("MAC", "mac not exists");
        params.put("Parameter", p);
        r = baseInfoService.getHgResourceUsage(params);
        assertEquals(-102, r.get("Result"));
    }

    @Test
    public void testGetHgSystemInfo() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "GET");
        params.put("ID", 123);
        params.put("CmdType", "GET_HG_SYSTEM_INFO");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
        p.put("MAC", "1C25E1012345");
        params.put("Parameter", p);

        Map<String, Object> r = baseInfoService.getHgSystemInfo(params);
        assertEquals(0, r.get("Result"));
    }

    @Test
    public void testGetHgTimeDuration() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "GET");
        params.put("ID", 123);
        params.put("CmdType", "GET_HG_TIME_DURATION");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
        p.put("MAC", "14144B198734");

        params.put("Parameter", p);

        Map<String, Object> r = baseInfoService.getHgTimeDuration(params);
        assertEquals(0, r.get("Result"));
    }

    @Test
    public void testGetWLANPrefix() {
        List<String> prefixes = baseInfoService.getWLANPrefix("1C25E1012345");
        System.out.println(prefixes);
    }

}
