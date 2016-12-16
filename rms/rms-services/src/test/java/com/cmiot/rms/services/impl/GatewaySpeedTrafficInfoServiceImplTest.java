package com.cmiot.rms.services.impl;

import com.cmiot.rms.services.GatewaySpeedTrafficInfoService;

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
public class GatewaySpeedTrafficInfoServiceImplTest {

    @Autowired
    private GatewaySpeedTrafficInfoService gatewaySpeedTrafficInfoService;

    @Test
    public void setLanDeviceSpeedTest() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "GET");
        params.put("ID", 123);
        params.put("CmdType", "SET_LAN_DEVICE_SPEED_TEST");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
        p.put("MAC", "14:14:4B:19:87:35");
        p.put("Enable", "1");
        params.put("Parameter", p);

        Map<String, Object> r = gatewaySpeedTrafficInfoService.setLanDeviceSpeedTest(params);
        assertEquals(0, r.get("Result"));
    }

    @Test
    public void getLanDeviceTrafficStatus() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "GET");
        params.put("ID", 123);
        params.put("CmdType", "GET_LAN_DEVICE_TRAFFIC_STATUS");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
//        p.put("MAC", "14:14:4B:19:87:34");
        p.put("MAC","00:C0:02:86:87:8A");//杭研
        p.put("DeviceMAC","54:ee:75:3a:2c:87");
        params.put("Parameter", p);

        Map<String, Object> r = gatewaySpeedTrafficInfoService.getLanDeviceTrafficStatus(params);
        assertEquals(0, r.get("Result"));
    }

    @Test
    public void setLanSpeedReportPolicy() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "Set");
        params.put("ID", 123);
        params.put("CmdType", "GET_HG_TIME_DURATION");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
//        p.put("MAC", "00:0E:F4:E0:06:07"); //宏成
//        p.put("MAC", "14:14:4B:20:87:34"); //星网
        p.put("MAC","2e:a1:ff:72:90:d4");//杭研
        p.put("Enable","1");
        p.put("Time","120");
        List<String> macs = new ArrayList<String>();
        macs.add("f4:e3:fb:9c:83:33");
        p.put("DeviceMACS",macs);
        params.put("Parameter", p);

        Map<String, Object> r = gatewaySpeedTrafficInfoService.setLanSpeedReportPolicy(params);
        assertEquals(0, r.get("Result"));
    }

    @Test
    public void getLanSpeedReportPolicy() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "GET");
        params.put("ID", 123);
        params.put("CmdType", "GET_LAN_SPEED_REPORT_POLICY");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
//        "InternetGatewayDevice.LANDevice.1.Hosts.Host.1.MACAddress" -> "80:be:05:50:55:7d"
//        p.put("MAC", "00:0E:F4:E0:06:07"); //宏成
//        p.put("MAC", "14:14:4B:20:87:34"); //星网
        p.put("MAC","00:C0:02:86:87:8A");//杭研

        params.put("Parameter", p);
        Long starTIime = System.currentTimeMillis();
        Map<String, Object> r = gatewaySpeedTrafficInfoService.getLanSpeedReportPolicy(params);
        Long endTime = System.currentTimeMillis();
        System.out.println("总的耗时时间：" + (endTime - starTIime));
        assertEquals(0, r.get("Result"));
    }
}
