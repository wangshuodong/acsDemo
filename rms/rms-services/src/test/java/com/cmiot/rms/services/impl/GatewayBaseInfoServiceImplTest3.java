package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.Inform;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.BoxManageService;
import com.cmiot.rms.services.GatewayBaseInfoService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewayManageService;
import com.cmiot.rms.services.WirelessNetworkSettingService;
import com.cmiot.rms.services.template.RedisClientTemplate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;






@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
        "file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class GatewayBaseInfoServiceImplTest3 {

    @Autowired
    private WirelessNetworkSettingService wirelessNetworkSettingService;

    @Autowired
    private GatewayBaseInfoService gatewayBaseInfoService;

    @Autowired
    private GatewayManageService gatewayManageService;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Autowired
    BoxManageService boxManageService;

    @Test
    public void testDeviceInfoAndStatus() throws Exception {//请求无反应
             Map<String ,Object> map = new HashMap<>();
             map.put("RPCMethod", "Get");
             map.put("ID", "1004012010");
             map.put("CmdType", "GET_HG_INFO_ALL");
             map.put("SequenceId", "3DDE8324");
             Map<String ,Object> parameterMap = new HashMap<>();
             parameterMap.put("MAC", "00:0E:F4:E0:06:07");
             map.put("Parameter", parameterMap);
             System.out.println(gatewayBaseInfoService.getDeviceInfoAndStatus(map));
    }
    
    
    @Test
    public void testOpenWPS() throws Exception {//OK
             Map<String ,Object> openWPSmap = new HashMap<>();
             openWPSmap.put("RPCMethod", "Get");
             openWPSmap.put("ID", "1004012010");
             openWPSmap.put("CmdType", "SET_WIFI_WPS_ON");
             openWPSmap.put("SequenceId", "3DDE8324");
             Map<String ,Object> openWPSparameterMap = new HashMap<>();
             openWPSparameterMap.put("MAC", "14:14:4B:19:87:34");
             openWPSparameterMap.put("SSIDIndex","2");
             openWPSmap.put("Parameter", openWPSparameterMap);

             System.out.println(wirelessNetworkSettingService.openWPS(openWPSmap));
    }
    
    @Test
    public void testCloseWPS() throws Exception {//OK
             Map<String ,Object> closeWPSmap = new HashMap<>();
             closeWPSmap.put("RPCMethod", "Get");
             closeWPSmap.put("ID", "1004012010");
             closeWPSmap.put("CmdType", "SET_WIFI_WPS_OFF");
             closeWPSmap.put("SequenceId", "3DDE8324");
             Map<String ,Object> closeWPSparameterMap = new HashMap<>();
             closeWPSparameterMap.put("MAC", "14:14:4B:19:87:34");
             closeWPSparameterMap.put("SSIDIndex","2");
             closeWPSmap.put("Parameter", closeWPSparameterMap);
             System.out.println(wirelessNetworkSettingService.closeWPS(closeWPSmap));

    }

    @Test
    public void testGetWPSCurrentStatus() throws Exception {//返回的两个字段都没有
             Map<String ,Object> closeWPSmap = new HashMap<>();
             closeWPSmap.put("RPCMethod", "Get");
             closeWPSmap.put("ID", "1004012010");
             closeWPSmap.put("CmdType", "GET_WIFI_WPS_STATUS");
             closeWPSmap.put("SequenceId", "3DDE8324");
             Map<String ,Object> closeWPSparameterMap = new HashMap<>();
             closeWPSparameterMap.put("MAC", "14:14:4B:19:87:34");
             closeWPSmap.put("Parameter", closeWPSparameterMap);
             System.out.println(wirelessNetworkSettingService.getWPSCurrentStatus(closeWPSmap));
    }

    @Test
    public void testGetInternetConInformation() throws Exception {//IPv6的字段都无法返回
        Map<String ,Object> closeWPSmap = new HashMap<>();
        closeWPSmap.put("RPCMethod", "Get");
        closeWPSmap.put("ID", "1004012010");
        closeWPSmap.put("CmdType", "GET_WAN_INFO");
        closeWPSmap.put("SequenceId", "3DDE8324");
        Map<String ,Object> closeWPSparameterMap = new HashMap<>();
        closeWPSparameterMap.put("MAC", "00:0E:F4:E0:06:07");
        closeWPSmap.put("Parameter", closeWPSparameterMap);
        System.out.println(wirelessNetworkSettingService.getInternetConInformation(closeWPSmap));
    }

    @Test
    public void testGetSSIDINFO() throws Exception {//OK ~SSIDindex判断需要修改
        Map<String ,Object> closeWPSmap = new HashMap<>();
        closeWPSmap.put("RPCMethod", "Get");
        closeWPSmap.put("ID", "1004012010");
        closeWPSmap.put("CmdType", "GET_WIFI_SSID_INFO");
        closeWPSmap.put("SequenceId", "3DDE8324");
        Map<String ,Object> closeWPSparameterMap = new HashMap<>();
        closeWPSparameterMap.put("MAC", "14:14:4B:198734");
        closeWPSparameterMap.put("SSIDIndex", "10");
        closeWPSmap.put("Parameter", closeWPSparameterMap);
        System.out.println(wirelessNetworkSettingService.getSSIDInfo(closeWPSmap));
    }
    @Test
    public void testGetWifiSSIDInfo() throws Exception {//OK ~SSIDindex判断需要修改
    	Map<String ,Object> closeWPSmap = new HashMap<>();
    	closeWPSmap.put("RPCMethod", "Get");
    	closeWPSmap.put("ID", "1004012010");
    	closeWPSmap.put("CmdType", "GET_WIFI_SSID_INFO");
    	closeWPSmap.put("SequenceId", "3DDE8324");
    	Map<String ,Object> closeWPSparameterMap = new HashMap<>();
    	closeWPSparameterMap.put("MAC", "1C25E1000030");
    	closeWPSparameterMap.put("SSIDIndex", "0");
    	closeWPSmap.put("Parameter", closeWPSparameterMap);
    	System.out.println(wirelessNetworkSettingService.getWifiSSIDInfo(closeWPSmap));
    }
    //000EF4E00607  00C00286878A 14144B198734 14144B208734
    @Test
    public void testsetSSIDINFO() throws Exception {//OK ~SSIDindex判断需要修改
        Map<String ,Object> closeWPSmap = new HashMap<>();
        closeWPSmap.put("RPCMethod", "Get");
        closeWPSmap.put("ID", "1004012010");
        closeWPSmap.put("CmdType", "SET_WIFI_SSID_INFO");
        closeWPSmap.put("SequenceId", "58A15856");
        Map<String ,Object> closeWPSparameterMap = new HashMap<>();
        closeWPSparameterMap.put("MAC", "00C00286878A");
        closeWPSparameterMap.put("SSIDIndex", "0");

        closeWPSparameterMap.put("SSID", "ssid");
        closeWPSparameterMap.put("PWD", "83aa400af464c76d");
        closeWPSparameterMap.put("ENCRYPT", "1");
        closeWPSparameterMap.put("PowerLevel", "80");
        closeWPSparameterMap.put("Channel", "0");
        closeWPSparameterMap.put("Guest", "1");
        closeWPSparameterMap.put("Hidden", "1");
        closeWPSparameterMap.put("Enable", "1");
        closeWPSmap.put("Parameter", closeWPSparameterMap);
        System.out.println(wirelessNetworkSettingService.setSSIDConfiguration(closeWPSmap));
    }
    /*{
        "RPCMethod": "Set",
            "ID": 123,
            "CmdType": "SET_WIFI_SSID_INFO",
            "SequenceId": "58A15856",
            "Parameter": {
        "MAC": "00:C0:02:86:87:8A",
                "SSIDIndex": "0",
                "SSID": "ssid",
                "PWD": "83aa400af464c76d",
                "ENCRYPT": "1",
                "PowerLevel": "80",
                "Channel": "0",
                "Guest": "1",
                "Hidden": "1",
                "Enable": "1"
    }
    }*/

    @Test
    public void testgetPPPDailUpStatus() throws Exception { //OK ~WANStatus\DialReason\WANStatus1\DialReason1参数字段返回结果没有
        Map<String ,Object> closeWPSmap = new HashMap<>();
        closeWPSmap.put("RPCMethod", "Get");
        closeWPSmap.put("ID", "1004012010");
        closeWPSmap.put("CmdType", "GET_WIFI_SSID_INFO");
        closeWPSmap.put("SequenceId", "3DDE8324");
        Map<String ,Object> closeWPSparameterMap = new HashMap<>();
        closeWPSparameterMap.put("MAC", "00:0E:F4:E0:06:07");
        closeWPSparameterMap.put("SSIDIndex", "1");
        closeWPSmap.put("Parameter", closeWPSparameterMap);
        System.out.println(wirelessNetworkSettingService.getPPPDailUpStatus(closeWPSmap));
    }

    @Test
    public void testGetArea() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
       map.put("id", "1");
        System.out.println(gatewayManageService.queryGatewayBaseInfo(map));
    }

    @Test
    public void testModifyUinfo() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        map.put("id", "3");
        map.put("username", "1");
        map.put("password", "1");
        map.put("roleName","root");
        map.put("areaId","2");
        System.out.println(gatewayManageService.modFamilyAccountPwd(map));

    }

    @Test
    public void testAddObjectInfo() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        map.put("id", "3");
        map.put("pathName", "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor. MonitorConfig");
        System.out.println(gatewayManageService.addObjectInfo(map));

    }

    @Test
    public void testDeleteObjectInfo() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        map.put("id", "3");
        map.put("pathName", "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor. MonitorConfig");
        System.out.println(gatewayManageService.deleteObjectInfo(map));

    }

    @Test
    public void testQueryList4Page() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        /*map.put("areaCode", "11");*/
        map.put("uId","54724e5378586a49785872657968336b59747950795855534a595a366a484f48366553763336356b4d2f3246494d585868396a5662673d3d");
/*        map.put("factoryName","CIOT");
        map.put("makeMerchantsId","1");*/
        System.out.println("  ~~testQueryList4Page~~  " + gatewayManageService.queryList4Page(map));
    }

    @Test
    public void testQueryGatewayDetail() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        map.put("gatewayUuid", "1");
        System.out.println("  ~~testQueryGatewayDetail~~  " + gatewayManageService.queryGatewayDetail(map));
    }

    @Test
    public void testgetHgResourceUsage() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        map.put("MAC", "14144B198734");
        Map<String ,Object> Parameter = new HashMap<>();
        Parameter.put("Parameter",map);
        System.out.println("  ~~testgetHgResourceUsage~~  " + gatewayBaseInfoService.getHgResourceUsage(Parameter));
    }

    @Test
    public void testqueryObjectInfo() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        map.put("id", "3ae9426decd9495e9408cd8d4e2000f7");
        map.put("pathName", "InternetGatewayDevice.LANInterfaces.WLANConfiguration");
        System.out.println("  ~~testgetHgResourceUsage~~  " + gatewayManageService.queryObjectInfo(map));
    }

    @Test
    public void testReboot() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        List<String> rebootList =new ArrayList<>();
        rebootList.add(0,"1c6adb5f085142dc83f97a4d91b4cd85");
        map.put("gatewayIds",rebootList);
        System.out.println("  ~~testReboot~~  " + gatewayManageService.reboot(map));
    }

    @Test
    public void testRedis() throws Exception {
        System.out.println("  ~~status~~  " + redisClientTemplate.get("0HGRKW29L082LO"));
    }



    @Test
    public void testqueryBoxListForPage() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        map.put("areaCode", "53");
        map.put("uId","admin");
        /*map.put("page","2");*/
        System.out.println("  ~~queryBoxListForPage~~  " + boxManageService.queryBoxListForPage(map));
    }

    @Test
    public void testQueryBoxDetail() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        map.put("boxUuid", "321");
        System.out.println("  ~~queryBoxDetail~~  " + boxManageService.queryBoxDetail(map));
    }

    @Test
    public void testImportGatewayInfo() throws Exception {//请求无反应
        Map<String ,Object> map = new HashMap<>();
        String fp;
       /* fp = "http://172.19.0.53:8099/fileserver/upload/file/2016/08/04/网关导入模板.xls";*/
        /*fp = "http://183.230.40.192:8088/fileserver/upload/file/2016/08/08/importExcel.xls";*/
        fp = "ftp://app:app_!QAZxsw2@172.19.10.8/2016/08/04/importExcel.xls";
        map.put("filePath", fp);
        System.out.println("  ~~testImportGatewayInfo~~  " + gatewayManageService.ImportGatewayInfo(map));
    }

}
