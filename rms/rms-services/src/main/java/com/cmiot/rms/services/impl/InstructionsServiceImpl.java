package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.InstructionsStatusEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.dao.mapper.InstructionsInfoMapper;
import com.cmiot.rms.dao.model.InformInfo;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangzhen on 2016/2/2.
 */
@Service("instructionsService")
public class InstructionsServiceImpl implements InstructionsService {

    private static Logger logger = LoggerFactory.getLogger(InstructionsServiceImpl.class);

    @Autowired
    private InstructionsInfoMapper instructionsInfoMapper;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Value("${instructionSaveTimeOut}")
    int instructionTimeOut;


    @Override
    public InstructionsInfoWithBLOBs queryInstructions(InstructionsInfoWithBLOBs infoWithBLOBs) {
        return instructionsInfoMapper.queryInstructions(infoWithBLOBs);
    }

    @Override
    public int updateInstructionsInfo(InstructionsInfoWithBLOBs instructionsInfo) {
        return instructionsInfoMapper.updateByPrimaryKeySelective(instructionsInfo);
    }

    @Override
    public void addInstructionsInfo(InstructionsInfoWithBLOBs ins) {
        // 连接 Redis 服务
        Map<String, String> map = new HashMap<>();
        // 系统时间
        String time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
        // 指令对应的网关唯一标示
        map.put("cpeIdentity", ins.getCpeIdentity() == null ? "" : ins.getCpeIdentity());
        // 指令下发内容
        map.put("instructionsBeforeContent", ins.getInstructionsBeforeContent());
        // 指令下发对象名称
        map.put("instructionsBeforeClassname", ins.getInstructionsBeforeClassname() == null ? "" : ins.getInstructionsBeforeClassname());
        // 设置指令状态为发送中
        map.put("instructionsState", InstructionsStatusEnum.STATUS_0.code().toString());
        // 创建时间
        map.put("instructionsRoleCreateTime", time);
        // 修改时间
        map.put("instructionsRoleModifyTime", time);
        // 设置诊断状态默认为诊断中
        map.put("diagnosticsState", InstructionsStatusEnum.STATUS_0.code().toString());

        logger.info("redis写入 指令id:{} 网关id:{} ", ins.getInstructionsId(), ins.getCpeIdentity());

        // 写入redis
        redisClientTemplate.hmset(ins.getInstructionsId(), map);

        //设置超时时间
        redisClientTemplate.expire(ins.getInstructionsId(), instructionTimeOut);
    }

    @Override
    public Map<String, String> getInstructionsInfo(String id) {
        Map<String, String> map = new HashMap<>();
        map.put("requestId", id);
        map.put("cpeIdentity", redisClientTemplate.hmget(id, "cpeIdentity").get(0));
        map.put("status", redisClientTemplate.hmget(id, "instructionsState").get(0));
        map.put("json", redisClientTemplate.hmget(id, "instructionsAfterContent").get(0));
        map.put("beforeContent", redisClientTemplate.hmget(id, "instructionsBeforeContent").get(0));
        map.put("diagnosticsState", redisClientTemplate.hmget(id, "diagnosticsState").get(0));
        logger.info("redis取指令 id:{} status:{} josn:{}", id, map.get("status"), map.get("json"));
        return map;
    }

    @Override
    public Map<String, Object> updateInstructionsInfo(String instructionsId, JSONObject afterContent, int status, String afterClassname, String description) {
        Map<String, Object> result = new HashMap<>();
        //查询下发指令信息
        // 判断是否存在RequestId
        boolean isExist = redisClientTemplate.exists(instructionsId);
        if (!isExist) {
            // RequestId不存在
            result.put(Constant.CODE, RespCodeEnum.RC_1004.code());
            result.put(Constant.MESSAGE, "RequestId不存在");
            logger.info(JSON.toJSONString(result));
            return result;
        }

        Map<String, String> map = new HashMap<>();

        // 系统时间
        String time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
        // 添加字段 接收指令信息
        String instructionsAfterContent = JSON.toJSONString(afterContent);
        redisClientTemplate.hset(instructionsId, "instructionsAfterContent", instructionsAfterContent);
        // 接收指令对象名称
        redisClientTemplate.hset(instructionsId, "instructionsAfterClassname", afterClassname);
        // 覆盖之前的值
        redisClientTemplate.hset(instructionsId, "instructionsRoleModifyTime", time);
        // 修改状态
        redisClientTemplate.hset(instructionsId, "instructionsState", String.valueOf(status));

        logger.info("redis更新指令 id:{} status:{} josn:{}", instructionsId, status, instructionsAfterContent);

        // 日志记录
//        LogBackRecord.logBackBean(LogMarkEnum.LOG_MARK_PORT.code(), JSON.toJSONString(afterContent), redisClientTemplate.hmget(instructionsId, "instructionsBeforeContent")+"", null, null, null, null, "addObject", afterClassname, null, null);
        // 记录接口调用日志
        LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(afterContent), instructionsId, "", description,LogTypeEnum.LOG_TYPE_SYSTEM.description());
        result.put(Constant.CODE, RespCodeEnum.RC_1000.code());
        result.put(Constant.MESSAGE, "成功");
        return result;
    }

    @Override
    public Map<String, Object> updateInstructionsInfo(String instructionsId, int diagnosticsState, InformInfo informInfo) {
        logger.info("redis更新指令 id:{} diagnosticsState:{}", instructionsId, diagnosticsState);
        Map<String, Object> result = new HashMap<>();
        // 修改诊断状态状态
        redisClientTemplate.hset(instructionsId, "diagnosticsState", String.valueOf(diagnosticsState));
        // 添加字段 接收指令信息
        String instructionsAfterContent = JSON.toJSONString(informInfo);
        redisClientTemplate.hset(instructionsId, "instructionsAfterContent", instructionsAfterContent);
        result.put(Constant.CODE, RespCodeEnum.RC_1000.code());
        result.put(Constant.MESSAGE, "成功");
        return result;
    }

    @Override
    public void updateInstructionsInfo(String gatewaySerialnumber, String code) {
        logger.info("修改重启、出厂操作指令：gatewaySerialnumber"+gatewaySerialnumber+",code："+code);
        // 释放redis中写入的SN数据的锁
        redisClientTemplate.del(gatewaySerialnumber);
//        redisClientTemplate.set(gatewaySerialnumber,code);
    }

    /**
     * 获取指令中的值
     *
     * @param id
     * @param key
     * @return
     */
    @Override
    public String getBeforeContent(String id, String key) {
        String content = redisClientTemplate.hmget(id, "instructionsBeforeContent").get(0);
        JSONObject object = JSONObject.parseObject(content);
        if(null != object && null != object.get(key))
        {
            return object.get(key).toString();
        }
        return "";
    }

    /**
     * 获取错误指令结果
     *
     * @param id
     * @return
     */
    @Override
    public int getFaultCode(String id) {
        String className = redisClientTemplate.hmget(id, "instructionsAfterClassname").get(0);
        if(className.equals("Fault"))
        {
            String content = redisClientTemplate.hmget(id, "instructionsAfterContent").get(0);
            JSONObject contentObj = JSONObject.parseObject(content);
            JSONObject fault = (JSONObject)contentObj.get("faultStruct");
            return fault.getInteger("faultCode");
        }
        return 1;
    }
}
