package com.cmiot.acs.domain;

import com.cmiot.acs.ServerSetting;
import com.cmiot.acs.common.SignDigestUtil;
import com.cmiot.acs.common.SoapMethodUtil;
import com.cmiot.acs.control.ACSProcessControl;
import com.cmiot.acs.control.ACSProcessControlManager;
import com.cmiot.acs.control.InformParse;
import com.cmiot.acs.control.ResponseParse;
import com.cmiot.acs.control.impl.InformHandleImpl;
import com.cmiot.acs.control.impl.ResponseHandleImpl;
import com.cmiot.acs.domain.cache.SpringRedisUtil;
import com.cmiot.acs.domain.code.VerifyMethod;
import com.cmiot.acs.domain.lock.ProcessLock;
import com.cmiot.acs.integration.AbIntegration;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Inform;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 信息工作者
 * Created by ZJL on 2016/11/14.
 */
public class MessageWorker extends VerifyMethod implements Runnable {
    public Logger logger = LoggerFactory.getLogger(MessageWorker.class);
    private InformParse informParse = new InformParse(new InformHandleImpl());
    private final ChannelHandlerContext context;
    private final MessageQueue messageQueue;
    private Message message;

    public MessageWorker(ChannelHandlerContext context, MessageQueue messageQueue) {
        messageQueue.setRunning(true);
        this.context = context;
        this.messageQueue = messageQueue;
    }


    @Override
    public void run() {
        try {
            while (!messageQueue.isEmpty()) {
                message = messageQueue.getRequestQueue().poll();
                String gid = message.getGid();
                Inform laInform = message.getInform();
                StringBuilder body = message.getBody();
                HttpRequest request = message.getRequest();

                logger.info("【GID={}】CAE请求ACS报文：{}\n{}", gid, request, body);

                System.out.println("===========>" + ServerSetting.dubboUrl);

                // ======================================Digest Start==================================================
                if (ServerSetting.digestSwitch == null) {
                    ServerSetting.digestSwitch = queryDigestSwitch();
                }

                ACSProcessControl laAcsPcl = null;
                if (laInform != null) {
                    laAcsPcl = ACSProcessControlManager.getInstance().getProcessControl(laInform.getDeviceId().getCpeId());
                }

                if (laAcsPcl == null && ServerSetting.digestSwitch) {
                    String authorization = request.headers().get(HttpHeaderNames.AUTHORIZATION);
                    if (StringUtils.isBlank(authorization)) {
                        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
                        String wwwAut = SignDigestUtil.newWwwAuthenticate(RandomStringUtils.randomAlphanumeric(32));
                        response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, wwwAut);
                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                        logger.info("【GID={}】ACS响应CPE报文：{}", gid, response);
                        this.writeAndFlush(context, response);
                        return;
                    } else {
                        boolean digestStatus = this.verifyDigest(message.getInform(), message.getRequest());
                        if (!digestStatus) {
                            logger.error("【GID={}】Digest认证失败：{}", gid, authorization);
                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_ACCEPTABLE);
                            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
                            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                            logger.info("【GID={}】ACS响应CPE报文：{}", gid, response);
                            this.writeAndFlushClose(context, response);
                            return;
                        }
                    }
                }
                //=======================================Digest End======================================================

                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                if (laInform != null) {
                    String cpeId = laInform.getDeviceId().getCpeId();
                    ACSProcessControl acsPcl = ACSProcessControlManager.getInstance().getProcessControl(cpeId);
                    if (acsPcl == null) {
                        acsPcl = new ACSProcessControl(cpeId, AbstractMethod.URN_CWMP1_1, message.getInform().getMaxEnvelopes());
                        ACSProcessControlManager.getInstance().addProcessControl(acsPcl);
                        acsPcl.setResponseParse(new ResponseParse(new ResponseHandleImpl()));
                        //获取初始化的缓存指令
                        ProcessLock.put(cpeId, cpeId);
                        SpringRedisUtil.set(cpeId + "_URL", ServerSetting.dubboUrl);
                        List<AbstractMethod> methodList = SpringRedisUtil.getMethodList(cpeId);
                        if (methodList != null && methodList.size() > 0) {
                            acsPcl.addACSRequestMethod(methodList);
                        }
                    }


                    AbstractMethod responseMethod = SoapMethodUtil.soapToMethod(body);
                    boolean verifyInform = true;
                    if (responseMethod != null && responseMethod instanceof Inform) {
                        verifyInform = this.verifyInform(laInform);
                    }

                    if (verifyInform) {

                        LogBackup.backupLog(cpeId, new StringBuilder().append(request).append(body));
                        AbIntegration.reportInfo(laInform, responseMethod);

                        AbstractMethod requestMethod = null;
                        if (responseMethod == null) {
                            requestMethod = doParseACSMethods(laInform);
                        } else {
                            doParseCPEMethod(laInform, responseMethod);
                            if (verifyNoSpecialResponse(responseMethod)) {
                                requestMethod = doParseACSMethods(laInform);
                            } else {
                                return;
                            }
                        }

                        StringBuilder responseBuilder = SoapMethodUtil.methodToSoap(requestMethod);
                        if (responseBuilder != null && responseBuilder.length() > 0) {
                            response.content().writeBytes(responseBuilder.toString().getBytes());
                        }

                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                        logger.info("【GID={}】ACS响应CPE报文：{}\n{}", gid, response, responseBuilder);

                        if (requestMethod != null) {
                            this.writeAndFlush(context, response);
                        } else {
                            this.writeAndFlushClose(context, response);
                        }
                    } else {
                        response.setStatus(HttpResponseStatus.FORBIDDEN);
                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                        logger.info("【GID={}】Inform认证失败ACS响应CPE报文：{}", gid, response);
                        this.writeAndFlushClose(context, response);
                    }
                } else {
                    response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                    logger.info("【GID={}】Inform为空ACS响应CPE报文：{}", gid, response);
                    this.writeAndFlushClose(context, response);
                }
            }
        } catch (Exception e) {
            logger.error("【GID={}】系统异常关闭Channel：{}", message.getGid(), e);
            context.close();
        } finally {
            this.messageQueue.setRunning(false);
            this.message = null;
        }
    }


    /**
     * 解析CPE到ACS上报的数据模型做对应的处理
     *
     * @param laInform
     * @param method
     */

    private void doParseCPEMethod(Inform laInform, AbstractMethod method) throws Exception {
        String cpeId = laInform.getDeviceId().getCpeId();
        ACSProcessControl acsPcl = ACSProcessControlManager.getInstance().getProcessControl(cpeId);
        if (method instanceof Inform) {
            Inform inform = (Inform) method;
            if (acsPcl == null) {
                acsPcl = new ACSProcessControl(cpeId, AbstractMethod.URN_CWMP1_1, laInform.getMaxEnvelopes());
                ACSProcessControlManager.getInstance().addProcessControl(acsPcl);
                acsPcl.setResponseParse(new ResponseParse(new ResponseHandleImpl()));
            }
            // Inform处理
            informParse.getInformHandle().setAcsProcessControl(acsPcl); // 无论acsPcl是否为空都调用设置
            informParse.parseInform(inform);// 具体方法解析
            // Inform的回复
            acsPcl.setDeviceInform(inform); // 缓存Inform，以备使用
            acsPcl.revAndConfirmMethod(method); // 具体方法解析
        } else {
            acsPcl.revAndConfirmMethod(method);
        }
    }


    /**
     * 解析ACS到CP下发的数据模型做对应的处理
     *
     * @param laInform
     */
    private AbstractMethod doParseACSMethods(Inform laInform) throws Exception {
        AbstractMethod requestMethod = null;
        String cpeId = laInform.getDeviceId().getCpeId();
        ACSProcessControl acsPcl = ACSProcessControlManager.getInstance().getProcessControl(cpeId);
        if (acsPcl != null) {
            requestMethod = acsPcl.doACSEMethods();
            if (ServerSetting.waitTimeout != null) {
                if (requestMethod == null) {
                    String cpeIdLock = ProcessLock.get(cpeId);
                    if (StringUtils.isNotBlank(cpeIdLock)) {
                        synchronized (cpeIdLock) {
                            cpeIdLock.wait(ServerSetting.waitTimeout);
                        }
                        requestMethod = acsPcl.doACSEMethods();
                    }
                }
            }
        }
        return requestMethod;
    }


    private void writeAndFlush(ChannelHandlerContext context, FullHttpResponse response) {
        Inform laInform = message.getInform();
        if (laInform != null) {
            response.headers().add("sn", laInform.getDeviceId().getSerialNubmer());
        }
        context.writeAndFlush(response);
    }


    private void writeAndFlushClose(ChannelHandlerContext context, FullHttpResponse response) {
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


}
