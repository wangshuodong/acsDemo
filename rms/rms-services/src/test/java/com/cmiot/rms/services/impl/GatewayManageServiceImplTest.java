package com.cmiot.rms.services.impl;

import com.cmiot.rms.services.GatewayManageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Henry
 * @Date 16/6/11
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
        "file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class GatewayManageServiceImplTest {

    @Autowired
    private GatewayManageService manageService;

    @Test
    public void testQueryObjectInfo() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", "1");
        params.put("pathName", "InternetGatewayDevice.DeviceInfo.SerialNumber");

        Map<String, Object> r = manageService.queryObjectInfo(params);
        System.out.println(r);
    }

    @Test
    public void testModObjectInfo() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", "1");

        List<Map<String, Object>> lists = new ArrayList<>();

        Map<String, Object> attribute1 = new HashMap<>();
        attribute1.put("Name", "InternetGatewayDevice.DeviceInfo.SerialNumber");
        attribute1.put("Notification", 3);
        List<String> accsessList = new ArrayList<>();
        accsessList.add("Subscriber");
        attribute1.put("AccessList", accsessList);

        Map<String, Object> attribute2 = new HashMap<>();
        attribute2.put("Name", "InternetGatewayDevice.DeviceInfo.ProductClass");
        attribute2.put("Notification", 2);
        List<String> accsessList2 = new ArrayList<>();
        accsessList2.add("Subscriber");
        attribute2.put("AccessList", accsessList2);

        lists.add(attribute1);
        lists.add(attribute2);

        params.put("listS", lists);

        Map<String, Object> r = manageService.modObjectInfo(params);
        System.out.println(r);
    }

    @Test
    public void queryGatewayUpAndDownSpeed()throws Exception{
        Map<String, Object> params = new HashMap<>();
        params.put("id", "0");
        Map<String, Object> r = manageService.queryGatewayUpAndDownSpeed(params);
        System.out.println(r);
    }

    @Test
    public void unbindGatewayTest()throws Exception{
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("gatewayUuid", "00b478c39aeb42a182151db095285e0a");
        manageService.unbindGateway(parameter);
    }

}