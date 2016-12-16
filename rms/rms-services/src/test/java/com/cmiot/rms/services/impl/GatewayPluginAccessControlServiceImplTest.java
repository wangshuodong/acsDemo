package com.cmiot.rms.services.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cmiot.rms.services.GatewayPluginAccessControlService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml", "file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml" })
public class GatewayPluginAccessControlServiceImplTest {

	@Autowired
	GatewayPluginAccessControlService gatewayPluginAccessControlService;

	@Test
	public void addPluginByNume() {
		Map<String, Object> p = new HashMap<String, Object>();// 参数对象
		p.put("MAC", "1C25E10001F8");

		List<Map<String, Object>> capabilitySet = new ArrayList<Map<String, Object>>();// 插件集合

		List<String> apiList = new ArrayList<String>();// API集
//		apiList.add("getWLANSSIDInfo");
//		apiList.add("getWLANSSIDStats");
//		apiList.add("getWLANWPSStatus");
//		apiList.add("getWLANWPSPtatus");
//		apiList.add("getWLANWPSMtatus");

		Map<String, Object> plugin = new HashMap<String, Object>();// 插件名称与 API对象
//		plugin.put("DUName", "WLANSSID");
		plugin.put("DUName", "WLANSSIDNOTTEST");
		plugin.put("apiList", apiList);

		capabilitySet.add(plugin);

		p.put("capabilitySet", capabilitySet);
		p = gatewayPluginAccessControlService.addPluginByNume(p);
		assertEquals(0, p.get("Result"));
	}

	@Test
	public void queryCapabilitySet() {
		// Map<String, Object> p = new HashMap<String, Object>();
		// // p.put("MAC", "000EF4E00607");// 宏成
		// p.put("MAC", "14144B198734"); // 星网
		// // p.put("MAC", "00C00286878A"); // 杭研
		// Map<String, Object> r = gatewayPluginAccessControlService.queryPluginCapabilitySet(p);
		// assertEquals(0, r.get("Result"));
	}

	@Test
	public void queryPluginApiList() {
		// Map<String, Object> p = new HashMap<String, Object>();
		// // p.put("MAC", "000EF4E00607");// 宏成
		// p.put("MAC", "14144B198734"); // 星网
		// // p.put("MAC", "00C00286878A"); // 杭研
		// Map<String, Object> r = gatewayPluginAccessControlService.queryPluginApiList(p);
		// assertEquals(0, r.get("Result"));
		/**
		 * 返回数据:
		 * {
		 * data=[
		 * {
		 * duName=WlanQueryService,
		 * duUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.9.DUName,
		 * apiList=[
		 * {
		 * aipUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.9.API.3.Name,
		 * aipName=getWLANWPSStatus
		 * },
		 * {
		 * aipUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.9.API.1.Name,
		 * aipName=getWLANSSIDInfo
		 * },
		 * {
		 * aipUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.9.API.2.Name,
		 * aipName=getWLANSSIDStats
		 * }
		 * ]
		 * },
		 * {
		 * duName=WlanQueryService,
		 * duUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.8.DUName,
		 * apiList=[
		 * ]
		 * },
		 * {
		 * duName=WlanQueryService,
		 * duUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.7.DUName,
		 * apiList=[
		 * {
		 * aipUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.7.API.1.Name,
		 * aipName=getWLANSSIDInfo
		 * },
		 * {
		 * aipUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.7.API.3.Name,
		 * aipName=getWLANWPSStatus
		 * },
		 * {
		 * aipUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.7.API.2.Name,
		 * aipName=getWLANSSIDStats
		 * }
		 * ]
		 * },
		 * {
		 * duName=WlanQueryService,
		 * duUrl=InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.2.DUName,
		 * apiList=[
		 * ]
		 * }
		 * ],
		 * resultCode=0,
		 * resultMsg=成功
		 * }
		 */
	}

	// @Test
	// public void addGatewayPlugin() {
	// Map<String, Object> p = new HashMap<String, Object>();
	// // p.put("MAC", "000EF4E00607");// 宏成
	// // p.put("MAC", "14144B198734"); // 星网
	// p.put("MAC", "14144B198734"); // 杭研
	//
	// List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	//
	// // List<String> freeApiList = new ArrayList<String>();// 自由添加时的API集
	// // freeApiList.add("getWLANSSIDInfo");
	// // freeApiList.add("getWLANSSIDStats");
	// // freeApiList.add("getWLANWPSStatus");
	//
	// List<Map<String, Object>> nodeApiList = new ArrayList<Map<String, Object>>();// 节点添加时的API集
	// Map<String, Object> nodemap = new HashMap<String, Object>();
	// nodemap.put("apiUrl", "");// api url
	// nodemap.put("apiName", "getWLANSSIDInfo");// api name
	// nodeApiList.add(nodemap);
	//
	// Map<String, Object> nodemap1 = new HashMap<String, Object>();
	// nodemap1.put("apiUrl", "");// api url
	// nodemap1.put("apiName", "getWLANSSIDStats");// api name
	// nodeApiList.add(nodemap1);
	//
	// Map<String, Object> nodemap2 = new HashMap<String, Object>();
	// nodemap2.put("apiUrl", "");// api url
	// nodemap2.put("apiName", "getWLANWPSStatus");// api name
	// nodeApiList.add(nodemap2);
	//
	// Map<String, Object> map = new HashMap<String, Object>();
	// map.put("duName", "WlanQueryService");// 服务类名
	// map.put("nodeApiList", nodeApiList);// nodeAPI集
	// // map.put("freeApiList", freeApiList);// freeAPI集
	// map.put("duUrl", "");// 服务类名URL
	// list.add(map);
	//
	// p.put("addType", "node");// 添加方式node:节点添加,free:网关自动创建节点添加
	// // p.put("addType", "free");
	// p.put("capabilitySet", list);
	// Map<String, Object> r = gatewayPluginAccessControlService.addPluginApi(p);
	// assertEquals(0, r.get("Result"));
	// /**
	// * {data=, resultCode=0, resultMsg=批量网关设置服务类名成功,批量网关设置API成功}
	// */
	// }

}
