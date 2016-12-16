package com.cmiot.rms.services;

import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.rms.dao.model.InformInfo;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;

import java.util.Map;

/**
 * 指令信息处理
 * Created by wangzhen on 2016/2/2.
 */
public interface InstructionsService {

    /**
     * 根据不同的条件查询信息  适合任何条件查询的通用方法
     *
     * @param infoWithBLOBs
     * @return
     */
    InstructionsInfoWithBLOBs queryInstructions(InstructionsInfoWithBLOBs infoWithBLOBs);

    /**
     * 修改下发指令信息
     *
     * @param infoWithBLOBs
     * @return
     */
    int updateInstructionsInfo(InstructionsInfoWithBLOBs infoWithBLOBs);

    void addInstructionsInfo(InstructionsInfoWithBLOBs ins);

    Map<String, String> getInstructionsInfo(String id);

    /**
     * 修改指令公用方法
     *
     * @param instructionsId 请求的RequestId
     * @param afterContent   请求的detail
     * @param status         返回的状态码，默认是0
     * @param afterClassname 对象名称
     * @param description    接口描述
     */
    Map<String, Object> updateInstructionsInfo(String instructionsId, JSONObject afterContent, int status, String afterClassname, String description);

    /**
     * 诊断信息更新
     *
     * @param instructionsId
     * @param diagnosticsState
     * @param informInfo
     * @return
     */
    Map<String, Object> updateInstructionsInfo(String instructionsId, int diagnosticsState, InformInfo informInfo);

    /**
     * 处理重启 恢复出厂操作
     * @param gatewaySerialnumber
     * @param code
     */
    void updateInstructionsInfo(String gatewaySerialnumber, String code);


    /**
     * 获取指令中的值
     * @param id
     * @param key
     * @return
     */
    String getBeforeContent(String id, String key);

    /**
     * 获取错误指令结果
     * @param id
     * @return
     */
    int getFaultCode(String id);
}