package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;
import com.cmiot.rms.services.BoxInterfaceService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.boxManager.instruction.BoxAbstractInstruction;
import com.cmiot.rms.services.boxManager.instruction.impl.BoxSetParameterValuesInstruction;
import com.cmiot.rms.services.boxManager.outerservice.BoxInterfaceHandOut;
import com.cmiot.rms.services.boxManager.outerservice.BoxRequestMgrService;
import com.cmiot.rms.services.util.InstructionUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/6/14.
 */
@Service
public class BoxInterfaceServiceImpl implements BoxInterfaceService {

        private static Logger logger = LoggerFactory.getLogger(BoxInterfaceServiceImpl.class);

        @Autowired
        private BoxInterfaceHandOut boxInterfaceHandOut;

        @Autowired
        private BoxRequestMgrService boxRequestMgrService;

        @Autowired
        private BoxInfoMapper boxInfoMapper;

        @Autowired
        private InstructionsService instructionsService;

        @Value("${stun.server.ip}")
        String ip;

        @Value("${stun.server.port}")
        String port;

        /**
         * ACS上报接口
         *
         * @param abstractMethod
         * @return
         */
        @Override
        public Map<String, Object> reportInfo(AbstractMethod abstractMethod) {
        logger.info("Start invoke reportInfo:{}", abstractMethod);

        JSONObject jsonObj = JSON.parseObject(JSONObject.toJSONString(abstractMethod));
        Map<String, Object> retmap = (Map<String, Object>)boxInterfaceHandOut.handOut(jsonObj);

        logger.info("End invoke reportInfo:{}", retmap);
        return retmap;
    }

        /**
         * OUI-SN验证接口
         *
         * @param parameter
         * @return
         */
        @Override
        public Map<String, Object> checkOuiSn(Map<String, Object> parameter) {
        logger.info("Start invoke checkOuiSn:{}", parameter);
        boolean isSuccess = boxRequestMgrService.certification(parameter);
        Map<String, Object> retMap = new HashMap<>();
        if(isSuccess)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        }
        else
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.OUISN_CHECK_FAILED.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.OUISN_CHECK_FAILED.getResultMsg());
        }
        logger.info("End invoke checkOuiSn:{}", retMap);
        return retMap;
    }

    /**
     * 根据用户名查询digest密码
     *
     * @param userName
     * @return
     */
    @Override
    public List<String> queryDigestPassword(String userName) {
        logger.info("Start invoke queryDigestPassword:{}", userName);
        List<String> ret = boxRequestMgrService.queryDigestPassword(userName);
        logger.info("End invoke queryDigestPassword:{}", ret);
        return ret;
    }

    /**
     * 机顶盒首次上报ACS从RMS获取需要下发的指令
     *
     * @param parameter
     * @return
     */
    @Override
    public List<AbstractMethod> getbootStrapInstructions(Map<String, Object> parameter) {

        String oui = null != parameter.get("oui") ? parameter.get("oui").toString() : "";
        String sn = null != parameter.get("sn") ? parameter.get("sn").toString() : "";
        if(StringUtils.isEmpty(oui) || StringUtils.isEmpty("sn"))
        {
            return new ArrayList<>();
        }

        //1.获取机顶盒信息
        BoxInfo searchboxInfo = new BoxInfo();
        searchboxInfo.setBoxSerialnumber(sn);
        searchboxInfo.setBoxFactoryCode(oui);
        // 根据SN和OUI查询是否已经存在机顶盒信息
        List<BoxInfo> boxList = boxInfoMapper.selectBoxInfo(searchboxInfo);
        if (boxList == null || boxList.size() == 0) {
            return new ArrayList<>();
        }
        BoxInfo boxInfo = boxList.get(0);
        List<AbstractMethod> methodList = new ArrayList<>();

        //2.获取账号密码设置指令
        AbstractMethod accountMethod = getAccountInstruction(boxInfo);
        if(null != accountMethod)
        {
            methodList.add(accountMethod);
        }
        return methodList;
    }

    /**
     * 根据OUI—SN查询机顶盒的Digest的账号和密码
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryDigestAccAndPw(Map<String, Object> parameter) {
        logger.info("Start invoke queryDigestAccAndPw:{}", parameter);
        Map<String, Object> ret = boxRequestMgrService.queryDigestAccAndPw(parameter);
        logger.info("End invoke queryDigestAccAndPw:{}", ret);
        return ret;
    }

    /**
     * 获取账号密码设置指令
     * @param boxInfo
     * @return
     */
    private AbstractMethod getAccountInstruction(BoxInfo boxInfo) {

        // 首次连接的时候设置digest帐号和密码,帐号密码随机生成
        String account = "cpe" + InstructionUtil.generateShortUuid();
        String password = "cpe" + InstructionUtil.generateShortUuid();
        // 生成终端连接管理平台的帐号密码
        String connectAccount = "RMS" + InstructionUtil.generateShortUuid();
        String connectPassword = "RMS" + InstructionUtil.generateShortUuid();

        List<ParameterValueStruct> list = new ArrayList<>();
        ParameterValueStruct<String> struct = new ParameterValueStruct<>();
        struct.setName("Device.ManagementServer.ConnectionRequestUsername");
        struct.setValue(connectAccount);
        list.add(struct);
        ParameterValueStruct<String> passwordStruct = new ParameterValueStruct<>();
        passwordStruct.setName("Device.ManagementServer.ConnectionRequestPassword");
        passwordStruct.setValue(connectPassword);
        list.add(passwordStruct);

        ParameterValueStruct<String> connectAccountStruct = new ParameterValueStruct<>();
        connectAccountStruct.setName("Device.ManagementServer.Username");
        connectAccountStruct.setValue(account);
        list.add(connectAccountStruct);
        ParameterValueStruct<String> connectPasswordStruct = new ParameterValueStruct<String>();
        connectPasswordStruct.setName("Device.ManagementServer.Password");
        connectPasswordStruct.setValue(password);
        list.add(connectPasswordStruct);


        //下发指令设置STUN状态
        ParameterValueStruct<Boolean> STUNEnableStruct = new ParameterValueStruct<Boolean>();
        STUNEnableStruct.setName("Device.ManagementServer.STUNEnable");
        STUNEnableStruct.setValue(true);
        list.add(STUNEnableStruct);
        ParameterValueStruct<String> STUNServerAddressStruct = new ParameterValueStruct<String>();
        STUNServerAddressStruct.setName("Device.ManagementServer.STUNServerAddress");
        STUNServerAddressStruct.setValue(ip);
        list.add(STUNServerAddressStruct);
        ParameterValueStruct<Long> STUNServerPortStruct = new ParameterValueStruct<Long>();
        STUNServerPortStruct.setName("Device.ManagementServer.STUNServerPort");
        STUNServerPortStruct.setValue(Long.parseLong(port));
        list.add(STUNServerPortStruct);

        Map<String, Object> map = new HashMap<>();
        map.put("pvList", list);

        InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();

        //生成指令ID作为请求的requestId
        String insId = "BoxBootStrap_"+ InstructionUtil.generate16Uuid();
        is.setInstructionsId(insId);
        is.setCpeIdentity(boxInfo.getBoxUuid());
        map.put("requestId", insId);

        BoxAbstractInstruction ins = new BoxSetParameterValuesInstruction();
        AbstractMethod abstractMethod = ins.createIns(is, boxInfo, map);

        is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));
        instructionsService.addInstructionsInfo(is);

        return abstractMethod;
    }
}
