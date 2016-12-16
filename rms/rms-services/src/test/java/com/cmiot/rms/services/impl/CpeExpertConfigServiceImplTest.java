package com.cmiot.rms.services.impl;

import com.cmiot.rms.services.CpeExpertConfigService;
import com.cmiot.rms.services.GatewaySpeedTrafficInfoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;


/**
 * @Description
 * @Author Henry
 * @Date 16/5/10
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
        "file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class CpeExpertConfigServiceImplTest {

    @Autowired
    private CpeExpertConfigService cpeExpertConfigService;

    @Test
    public void setHgReboot() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "Set");
        params.put("ID", 123);
        params.put("CmdType", "SET_HG_REBOOT");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
        p.put("MAC", "00:0E:F4:E0:06:07");
        params.put("Parameter", p);

        Map<String, Object> r = cpeExpertConfigService.setHgReboot(params);
        assertEquals(0, r.get("Result"));
    }

    @Test
    public void setHgRecover() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "Set");
        params.put("ID", 123);
        params.put("CmdType", "SET_HG_RECOVER");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
        p.put("MAC", "14:14:4B:20:87:34");
        params.put("Parameter", p);

        Map<String, Object> r = cpeExpertConfigService.setHgRecover(params);
        assertEquals(0, r.get("Result"));
    }

    @Test
    public void setHgServiceManage() throws Exception {
        Map params = new HashMap<String, Object>();
        params.put("RPCMethod", "GET");
        params.put("ID", 123);
        params.put("CmdType", "GET_HG_TIME_DURATION");
        params.put("SequenceId", "EFAB9000");

        Map p = new HashMap<String, Object>();
//        p.put("MAC", "00:0E:F4:E0:06:07"); //宏成
        p.put("MAC", "14:14:4B:19:87:34"); //星网
        p.put("FtpEnable","1");
        params.put("Parameter", p);

        Map<String, Object> r = cpeExpertConfigService.setHgServiceManage(params);
        assertEquals(0, r.get("Result"));
    }
}
