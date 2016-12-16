package com.cmiot.acs.dispatcher;

import com.cmiot.acs.domain.Message;
import com.cmiot.acs.domain.MessageQueue;
import com.cmiot.acs.domain.MessageWorker;
import com.cmiot.acs.utils.ExceptionUtils;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * 流程调度器
 * Created by zjial on 2016/5/25.
 */
public class HandlerDispatcher implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HandlerDispatcher.class);
    private final ConcurrentMap<ChannelHandlerContext, MessageQueue> sessionMsgQ;
    private Executor messageExecutor;
    private boolean running;
    private long sleepTimeMS;
    private int sleepTimeNS;

    public HandlerDispatcher() {
        this.sessionMsgQ = new ConcurrentHashMap<>();
        this.running = true;
        this.sleepTimeMS = 200L;
        this.sleepTimeNS = 0;
    }

    public void setMessageExecutor(Executor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }


    public void setSleepTimeMS(long sleepTimeMS) {
        this.sleepTimeMS = sleepTimeMS;
    }


    public void setSleepTimeNS(int sleepTimeNS) {
        this.sleepTimeNS = sleepTimeNS;
    }


    public void addMessageQueue(ChannelHandlerContext context, MessageQueue messageQueue) {
        this.sessionMsgQ.put(context, messageQueue);
    }


    public void removeMessageQueue(ChannelHandlerContext context) {
        MessageQueue queue = this.sessionMsgQ.remove(context);
        if (queue != null)
            queue.clear();
    }


    public MessageQueue getMessageQueue(ChannelHandlerContext context) {
        return this.sessionMsgQ.get(context);
    }


    public void addMessage(ChannelHandlerContext context, Message message) {
        try {
            // 通过Hash算法将其放入相应的队列中
            MessageQueue messageQueue = this.getMessageQueue(context);
            if (messageQueue == null) {
                messageQueue = new MessageQueue(new ConcurrentLinkedQueue<>());
                messageQueue.add(message);
                this.addMessageQueue(context, messageQueue);
            } else {
                messageQueue.add(message);
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }


    public void run() {
        while (this.running) {
            try {
                //处理一个时间片中每个网关的所有请求，一个网关一个线程
                for (Map.Entry<ChannelHandlerContext, MessageQueue> entry : sessionMsgQ.entrySet()) {
                    ChannelHandlerContext context = entry.getKey();
                    MessageQueue messageQueue = entry.getValue();
                    if ((messageQueue != null) && (!messageQueue.isEmpty()) && (!messageQueue.isRunning())) {
                        MessageWorker messageWorker = new MessageWorker(context, messageQueue);
                        this.messageExecutor.execute(messageWorker);
                    }
                }
            } catch (Exception e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
            try {
                Thread.sleep(this.sleepTimeMS, this.sleepTimeNS);
            } catch (InterruptedException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

}
