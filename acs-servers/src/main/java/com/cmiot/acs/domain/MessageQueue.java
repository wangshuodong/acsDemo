package com.cmiot.acs.domain;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by ZJL on 2016/11/12.
 */
public class MessageQueue {
    private boolean running = false;

    private Queue<Message> requestQueue;

    public MessageQueue(ConcurrentLinkedQueue<Message> concurrentLinkedQueue) {
        this.requestQueue = concurrentLinkedQueue;
    }

    public Queue<Message> getRequestQueue() {
        return this.requestQueue;
    }

    public void setRequestQueue(Queue<Message> requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void clear() {
        this.requestQueue.clear();
        this.requestQueue = null;
    }

    /**
     * 由于内部实现是链表。每次获取都会导致性能问题
     *
     * @return
     */
    @Deprecated
    public int size() {
        return this.requestQueue != null ? this.requestQueue.size() : 0;
    }

    public boolean isEmpty() {
        return this.requestQueue != null ? this.requestQueue.isEmpty() : true;
    }

    public boolean add(Message request) {
        return this.requestQueue.add(request);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return this.running;
    }
}