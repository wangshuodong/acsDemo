package com.cmiot.rms.services.impl;

import com.cmiot.rms.services.GatewayBatchSetTaskInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
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
public class GatewayBatchSetTaskInterfaceImplTest {

    @Autowired
    private GatewayBatchSetTaskInterface gatewayBatchSetTaskInterface;

    @Test
    public void queryBatchSetTaskPage() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("page", 1);
        params.put("pageSize", 5);
        params.put("areaCode", "49");
        params.put("parmName", "");
        Map<String, Object> r = gatewayBatchSetTaskInterface.queryBatchSetTaskPage(params);
        assertEquals(0, r.get("resultCode"));
    }

    @Test
    public void addBatchSetTask() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("areaCode", "49");
        params.put("trrigerEvent", "1,3");

        List<Map<String,Object>> list = new ArrayList<>();
        Map p = new HashMap<String, Object>();
        p.put("parmName", "InternetGatewayDevice.LANDevice.1.Hosts.Host.11.MACAddress");
        p.put("parmValue", "sdfasdfasfdsdsdfasdfasff");
        p.put("parmType", "string");
        p.put("parmLength", 75);
        p.put("parmWriteable", "1");
        list.add(p);
        params.put("taskDetail", list);

        Map<String, Object> r = gatewayBatchSetTaskInterface.addBatchSetTask(params);
        assertEquals(0, r.get("resultCode"));
    }

    @Test
    public void updateBatchSetTask() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("trrigerEvent", "1,2");
        params.put("id", "428fd6806e4b45f9b68a100b93074703");
        List<Map<String,Object>> list = new ArrayList<>();
        Map p = new HashMap<String, Object>();
        p.put("parmName", "InternetGatewayDevice.LANDevice.1.Hosts.Host.17.MACAddress");
        p.put("parmValue", "9875");
        p.put("parmType", "string");
        p.put("parmLength", 50);
        p.put("parmWriteable", "1");
        list.add(p);
        params.put("taskDetail", list);

        Map<String, Object> r = gatewayBatchSetTaskInterface.updateBatchSetTask(params);
        assertEquals(0, r.get("resultCode"));
    }

    @Test
    public void deleteBatchSetTask() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("trrigerEvent", "1,2");
        params.put("id", "428fd6806e4b45f9b68a100b93074703");
        List<Map<String,Object>> list = new ArrayList<>();
        Map p = new HashMap<String, Object>();
        p.put("parmName", "InternetGatewayDevice.LANDevice.1.Hosts.Host.17.MACAddress");
        p.put("parmValue", "9875");
        p.put("parmType", "string");
        p.put("parmLength", 50);
        p.put("parmWriteable", "1");
        list.add(p);
        params.put("taskDetail", list);

        Map<String, Object> r = gatewayBatchSetTaskInterface.deleteBatchSetTask(params);
        assertEquals(0, r.get("resultCode"));
    }
}
