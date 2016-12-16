package com.cmiot.rms.services.workorder;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.ParameterValueStructStr;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.*;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.workorder.impl.BusiOperation;
import com.tydic.inter.app.service.GatewayHandleService;
import com.tydic.service.PluginDeviceService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/8/29.
 */
public class ExcuteWorkOrderThread implements Runnable,Serializable {

    private static final long serialVersionUID = 1L;

    private GatewayInfo gatewayInfo;

    private GatewayBusinessMapper gatewayBusinessMapper;

    private WorkOrderTemplateInfoMapper workOrderTemplateInfoMapper;

    private BusiOperation busiOperation;

    private GatewayAdslAccountMapper gatewayAdslAccountMapper;

    private GatewayInfoMapper gatewayInfoMapper;

    private GatewayBusinessOpenDetailMapper gatewayBusinessOpenDetailMapper;

    private GatewayBusinessExecuteHistoryMapper gatewayBusinessExecuteHistoryMapper;

    private com.cmiot.ams.service.AreaService areaService;
    
    private com.tydic.service.PluginDeviceService pluginDeviceService;
    
    private GatewayHandleService gatewayHandleService;
    
    private  GatewayPasswordMapper gatewayPasswordMapper;
    
    private InstructionMethodService instructionMethodService;

    private Logger logger = LoggerFactory.getLogger(ExcuteWorkOrderThread.class);

    public ExcuteWorkOrderThread(GatewayInfo gatewayInfo){
        this.gatewayInfo = gatewayInfo;
        this.gatewayBusinessMapper = (GatewayBusinessMapper) SpringApplicationContextHolder.getSpringBean("gatewayBusinessMapper");
        this.workOrderTemplateInfoMapper = (WorkOrderTemplateInfoMapper) SpringApplicationContextHolder.getSpringBean("workOrderTemplateInfoMapper");
        this.busiOperation = (BusiOperation) SpringApplicationContextHolder.getSpringBean("busiOperation");
        this.gatewayAdslAccountMapper = (GatewayAdslAccountMapper) SpringApplicationContextHolder.getSpringBean("gatewayAdslAccountMapper");
        this.gatewayInfoMapper = (GatewayInfoMapper) SpringApplicationContextHolder.getSpringBean("gatewayInfoMapper");
        this.gatewayBusinessOpenDetailMapper = (GatewayBusinessOpenDetailMapper) SpringApplicationContextHolder.getSpringBean("gatewayBusinessOpenDetailMapper");
        this.gatewayBusinessExecuteHistoryMapper = (GatewayBusinessExecuteHistoryMapper) SpringApplicationContextHolder.getSpringBean("gatewayBusinessExecuteHistoryMapper");
        this.areaService = (AreaService)SpringApplicationContextHolder.getSpringBean("amsAreaService");
        pluginDeviceService = (PluginDeviceService)SpringApplicationContextHolder.getSpringBean("pluginDeviceService");
        this.gatewayHandleService = (GatewayHandleService)SpringApplicationContextHolder.getSpringBean("gatewayHandleService");
        this.gatewayPasswordMapper = (GatewayPasswordMapper)SpringApplicationContextHolder.getSpringBean("gatewayPasswordMapper");
        instructionMethodService = (InstructionMethodService)SpringApplicationContextHolder.getSpringBean("instructionMethodService");
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,readOnly=false)
    public void run() {
        try {
            logger.info("start invoke excuteWorkOrder,北向接口触发");
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("gatewayPassword", gatewayInfo.getGatewayPassword());
            List<String> statusList = new ArrayList<>();
            statusList.add("0");//未开通
            paramMap.put("businessStatuList", statusList);
            //查询有效的工单，工单按照创建时间正序排序
            List<GatewayBusiness> listBusiness = gatewayBusinessMapper.selectBusinessList(paramMap);
            
            if (listBusiness == null||listBusiness.isEmpty()) {
            	logger.info("该网关没有有效工单,gateWayPassword:{}", gatewayInfo.getGatewayPassword());
            	return;
			}
            
            //把所有工单改为执行中
            List<String> ids = new ArrayList<>();
            for(GatewayBusiness gatewayBusiness:listBusiness){
                ids.add(gatewayBusiness.getId());
            }
            
            Map<String, Object> para = new HashMap<>();
            para.put("businessStatu", "3");
            para.put("ids", ids);
            gatewayBusinessMapper.batchUpdateStatus(para);
            
            //把wband工单放到第一位执行
            GatewayBusiness wbandGatewayBusiness = null;
            int wbandLocation = 0;
            boolean isHaveWband = false;
            if (listBusiness.size()>1) {
            	for(int m = 0;m<listBusiness.size();m++){
                	//宽带新装
                    if(listBusiness.get(m).getBusinessCode().startsWith("wband") &&"1".equals(listBusiness.get(m).getBusinessType())){
                        wbandGatewayBusiness = listBusiness.get(m);
                        wbandLocation = m;
                        isHaveWband = true;
                        break;
                    }
                }
                if(isHaveWband) {
                    listBusiness.remove(wbandLocation);
                    listBusiness.add(0, wbandGatewayBusiness);
                }
			}
            
            //遍历执行工单
            //下发业务条数
            boolean sendNumResult = setOneCommand(gatewayInfo.getGatewayMacaddress(), "InternetGatewayDevice.X_CMCC_UserInfo.ServiceNum", String.valueOf(listBusiness.size()));
            if (!sendNumResult) {
            	logger.info("下发工单条数到网关password失败:{}", gatewayInfo.getGatewayPassword());
            	return;
			}
            for (GatewayBusiness gb : listBusiness) {
                //查询模板信息
                Map<String,Object> paMap = new HashMap<>();
                paMap.put("businessCode",gb.getBusinessCode());
                paMap.put("gatewayMacaddress",gatewayInfo.getGatewayMacaddress());
                //先查询匹配固件，硬件，生产商，设备型号的模板，如果没有则查询默认模板
                Map<?, ?> selectResultMap = workOrderTemplateInfoMapper.selectByBusinessCode(paMap);
                if(null == selectResultMap){
                    selectResultMap = workOrderTemplateInfoMapper.selectDefaultTemplate(paMap);
                }
                
                String template_message = (String) selectResultMap.get("template_message");
                if (null == selectResultMap||StringUtils.isEmpty(template_message)) {
                    logger.info("工单的模板类容为空,gateWayPassword:{},businessCode:{}", gatewayInfo.getGatewayPassword(), gb.getBusinessCode());
                    continue;
				}
                
                Map<String, String> resultMap = new HashMap<>();
                String parameterList = gb.getParameterList();
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                if (StringUtils.isNotEmpty(parameterList)) {
                	parameterMap = JSON.parseObject(parameterList, Map.class);
				}
                parameterMap.put("gateWayMac", gatewayInfo.getGatewayMacaddress());
                gatewayBusinessMapper.updateByPrimayKey(gb);

                resultMap = busiOperation.excute(template_message, parameterMap);
//              resultMap.put("MSG_CODE", "0");
                GatewayBusinessExecuteHistory gheh = new GatewayBusinessExecuteHistory();
                gheh.setOrderNo(gb.getOrderNo());
                if ("0".equals(resultMap.get("MSG_CODE"))) {
                	gheh.setExecuteStatus(1);
                	//新装成功
                	if ("1".equals(gb.getBusinessType())||"9".equals(gb.getBusinessType())||"6".equals(gb.getBusinessType())) {
						//宽带新装成功  wband wband_xxx_z
                		if (gb.getBusinessCode().startsWith("wband")) {
                			afterWbandInstall(gb);
						}
                		
                		//宽带、VOIP、OTTTV新装成功后公共处理
                		gb.setBusinessStatu("1");
                		gatewayBusinessMapper.updateByPrimayKey(gb);
                		if ("1".equals(gb.getBusinessType())) {
                			updateBusinessDetaiStatus(gb, gatewayInfo.getGatewayUuid(), "1");
						}
					}
                	//拆机成功
                	else if ("8".equals(gb.getBusinessType())) {
						//宽带拆机成功后处理  wband_xxx_c 安徽拆机接口不走模板
                		if (gb.getBusinessCode().startsWith("wband")) {
                			afterWbandDisassemble(gb);
						}
                		//在VOIP和OTT拆机后处理
                		else {
    						afterDisassemble(gb);
						}
					}
                    
                } else {
                	//执行失败
                    gheh.setExecuteStatus(2);
                    businessExeFail(gb);
                }
                
                gheh.setExecuteTime(DateTools.getCurrentSecondTime());
                //写操作记录
                gatewayBusinessExecuteHistoryMapper.insert(gheh);
                
            }
            
        }catch (Exception e){
            logger.error("执行工单报错,Exception:{}", e);
        }
    }
    
  
    /**
     * @param gatewayBusiness
     * wband新装成功后处理
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,readOnly=false)
    private void afterWbandInstall(GatewayBusiness gb){
    	//更新网关的宽带账号
        String pppoeAccount = gb.getAdslAccount();
        String AreaCode = gb.getAreacode();
        Integer area = null;
        if(!StringUtils.isEmpty(AreaCode)){
            area = areaService.findIdByCode(AreaCode);
        }
        GatewayInfo updateGatewayInfo = new GatewayInfo();
        updateGatewayInfo.setGatewayUuid(gatewayInfo.getGatewayUuid());
    	updateGatewayInfo.setBusinessStatus("1");
        updateGatewayInfo.setGatewayAdslAccount(pppoeAccount);
        if (null != area) {
        	updateGatewayInfo.setGatewayAreaId(area + "");
		}
        gatewayInfoMapper.updateByPrimaryKeySelective(updateGatewayInfo);
        
        try {
            GatewayAdslAccount adslAccount = new GatewayAdslAccount();
            adslAccount.setAdslAccount(pppoeAccount == null ? "" : pppoeAccount.toString());
            adslAccount.setCreateTime(Integer.valueOf((System.currentTimeMillis() + "").substring(0, 10)));
            adslAccount.setGatewayMAC(gatewayInfo.getGatewayMacaddress());
            String adslUuid = UniqueUtil.uuid();
            adslAccount.setId(adslUuid);
            if (null != area) {
                adslAccount.setAreaId(area);
            }
            gatewayAdslAccountMapper.insert(adslAccount);
        }catch (Exception e){
            logger.error("gatewayAdslAccountMapper.insert error,e:{}" ,e);
        }
        
        if(null != area) {
            try {
				//通知BMS
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("gatewayInfoMacaddress", gatewayInfo.getGatewayMacaddress());
				map.put("gatewayInfoAreaId", area + "");
				Map<String,Object> bmsResult = pluginDeviceService.updatePluginLeadGatewayData(map);
				logger.info("更新区域通知BMS结果,GatewayMacaddress:{},执行结果:{},结果描述:{}", gatewayInfo.getGatewayMacaddress(), bmsResult.get("resultCode"), bmsResult.get("resultMsg"));
			} catch (Exception e) {
				logger.error("更新区域通知BMS异常,e:{}" ,e);
			}
        }
    }
    
    
    /**
     * @param gatewayBusiness
     * Wband拆机成功后处理
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,readOnly=false)
    private void afterWbandDisassemble(GatewayBusiness gb){
    	//删除t_gateway_password的记录
		int m =gatewayPasswordMapper.deleteByPrimaryKey(gb.getGatewayPassword());
        logger.info(" invoke broadBandUnsubcribe删除t_gateway_password的记录,m:{},GatewayPassword:{}",m,gb.getGatewayPassword());
        
        //将该LOID对应的工单都置为 作废  根据password或宽带账号
        gb.setBusinessStatu("4");
        gatewayBusinessMapper.updateStatusByPassword(gb);
        
        //所有业务置为未开通
    	GatewayBusinessOpenDetail gatewayBusinessOpenDetail = new GatewayBusinessOpenDetail();
        gatewayBusinessOpenDetail.setGatewayUuid(gatewayInfo.getGatewayUuid());
        gatewayBusinessOpenDetail.setOpenStatus("0");
        int n = gatewayBusinessOpenDetailMapper.updateByGatewayUuid(gatewayBusinessOpenDetail);
        logger.info("invoke newInstallation，更新工单开通工单明细结果,n:{}", n);
    	
        //解绑网关
    	GatewayInfo updateGatewayInfo = new GatewayInfo();
        updateGatewayInfo.setGatewayUuid(gatewayInfo.getGatewayUuid());
        int k = gatewayInfoMapper.unBindPasswordAndAdslAccount(updateGatewayInfo);
        logger.info(" invoke broadBandUnsubcribe,解除password和adsl与网关的绑定关系,k:{}",k);
        
        //通知BMS，调用一级平台销户接口
        //1.用户恢复出厂设置   2.首次认证   3.固件升级    4.工单拆机恢复出厂设置 ，5销户
       try {
		Map<String, Object> bmsResult =  gatewayHandleService.factoryNotify(gatewayInfo.getGatewayMacaddress(), "4", false);
		logger.info("拆机通知BMS结果,GatewayMacaddress:{},执行结果:{},结果描述:{}", gatewayInfo.getGatewayMacaddress(), bmsResult.get("resultCode"), bmsResult.get("resultMsg"));
		logger.info("拆机通知BMS结果,由于不需要等待BMS的处理，所以这里想当时异步处理，所以上述执行结果为空");
		} catch (Exception e) {
			//logger.error("拆机通知BMS异常", e);
		}
    }
    
    
    
    
    /**
     * @param gatewayBusiness
     * VOIP和OTT拆机成功后处理
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,readOnly=false)
    private void afterDisassemble(GatewayBusiness gatewayBusiness){
        
        updateBusinessDetaiStatus(gatewayBusiness, gatewayInfo.getGatewayUuid(), "0");
        
        //根据password和业务账号  将该业务对应的工单改为 作废
        gatewayBusiness.setBusinessStatu("4");
        gatewayBusinessMapper.updateStatusByPwdAndBusiness(gatewayBusiness);
    }
    
    /**
     * @param gatewayBusiness
     * 业务详情状态更新
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,readOnly=false)
    private void updateBusinessDetaiStatus(GatewayBusiness gatewayBusiness,String gatewayUuid,String status){
    	
    	GatewayBusinessOpenDetail gatewayBusinessOpenDetailParam = new GatewayBusinessOpenDetail();
    	String code = gatewayBusiness.getBusinessCodeBoss();
    	//集团PBOSS拆机
        if (gatewayBusiness.getBusinessCodeBoss().indexOf("_C")>-1) {
        	code = gatewayBusiness.getBusinessCodeBoss().substring(0, gatewayBusiness.getBusinessCodeBoss().length()-1)+"Z";
		}
        gatewayBusinessOpenDetailParam.setBusinessCodeBoss(code);
        gatewayBusinessOpenDetailParam.setGatewayUuid(gatewayUuid);
        GatewayBusinessOpenDetail gatewayBusinessOpenDetailResult = gatewayBusinessOpenDetailMapper.selectByParam(gatewayBusinessOpenDetailParam);
        if (gatewayBusinessOpenDetailResult == null) {
            GatewayBusinessOpenDetail gatewayBusinessOpenDetail = new GatewayBusinessOpenDetail();
            gatewayBusinessOpenDetail.setBusinessCodeBoss(gatewayBusiness.getBusinessCodeBoss());
            gatewayBusinessOpenDetail.setOrderNo(gatewayBusiness.getOrderNo());
            gatewayBusinessOpenDetail.setGatewayUuid(gatewayUuid);
            gatewayBusinessOpenDetail.setOpenStatus(status);
            gatewayBusinessOpenDetail.setId(UniqueUtil.uuid());
            int n = gatewayBusinessOpenDetailMapper.insert(gatewayBusinessOpenDetail);
            logger.info("invoke newInstallation，写入工单开通工单明细结果,n:{},gatewayBusinessOpenDetail:{}", n, gatewayBusinessOpenDetail);
        } else {
            if (!status.equals(gatewayBusinessOpenDetailResult.getOpenStatus())) {
                gatewayBusinessOpenDetailResult.setOpenStatus(status);
                int n = gatewayBusinessOpenDetailMapper.updateByPrimaryKey(gatewayBusinessOpenDetailResult);
                logger.info("invoke newInstallation，更新工单开通工单明细结果,n:{},gatewayBusinessOpenDetailResult:{}", n, gatewayBusinessOpenDetailResult);
            }
        }
        
    }
    
    
    /**工单执行失败 修改工单状态
     * @param gb
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,readOnly=false)
    private void businessExeFail(GatewayBusiness gb){
    	//执行失败
        gb.setBusinessStatu("2");
        if (null == gb.getFailCount()) {
            gb.setFailCount(1);
        } else {
            gb.setFailCount(gb.getFailCount() + 1);
        }
        gatewayBusinessMapper.updateByPrimayKey(gb);
    }
    
    
    /**
	* @param mac
	* @param command
	* @param value
	* @return
	* 下发一个指令
	*/
	private boolean setOneCommand(String mac,String command,String value){
	
		List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
		ParameterValueStructStr pvs = new ParameterValueStructStr(command, value);
		list.add(pvs);
		
		boolean isSuccess = instructionMethodService.setParameterValue(mac, list);
		logger.debug("设置参数：{},值：{},结果：{}", command,value,isSuccess);
		
		return isSuccess;
	}
}