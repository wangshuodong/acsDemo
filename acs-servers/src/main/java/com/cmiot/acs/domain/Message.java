package com.cmiot.acs.domain;

import com.cmiot.acs.model.Inform;
import io.netty.handler.codec.http.HttpRequest;

import java.io.Serializable;


/**
 * Created by ZJL on 2016/11/12.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -2609390318523103844L;

    private HttpRequest request;

    private Inform inform;

    private StringBuilder body;

    private String gid;

    public Message() {
    }

    public Message(HttpRequest request, Inform inform, StringBuilder body, String gid) {
        this.request = request;
        this.inform = inform;
        this.body = body;
        this.gid = gid;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public Inform getInform() {
        return inform;
    }

    public void setInform(Inform inform) {
        this.inform = inform;
    }

    public StringBuilder getBody() {
        return body;
    }

    public void setBody(StringBuilder body) {
        this.body = body;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }
}
