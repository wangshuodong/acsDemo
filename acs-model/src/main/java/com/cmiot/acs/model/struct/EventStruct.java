package com.cmiot.acs.model.struct;

import java.io.Serializable;

/**
 * @author zjialin
 *         inform - event - eventstruct中的
 */
public class EventStruct implements Serializable {
    private static final long serialVersionUID = -5828885806819628244L;
    // 如果因定义commandKey而产生的inform，则必须要赋值，否则默认是空字符串；引起的该值改变的条件：scheduleInform,reboot,download,upload
    private String commandKey;
    private String evenCode;

    public EventStruct() {

    }

    public EventStruct(String evenCode) {
        this.evenCode = evenCode;
        this.commandKey = "";
    }

    public EventStruct(String evenCode, String commandKey) {
        this.evenCode = evenCode;
        this.commandKey = commandKey;
    }

    public String getCommandKey() {
        return commandKey;
    }

    /**
     * @param commandKey 如果因定义commandKey而产生的inform，则必须要赋值，否则默认是空字符串；<br>
     *                   引起的该值改变的条件：scheduleInform,reboot,download,upload
     */
    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }

    public String getEvenCode() {
        return evenCode;
    }

    public void setEvenCode(String evenCode) {
        this.evenCode = evenCode;
    }

    @Override
    public String toString() {
        return "EventCode:" + getEvenCode() + " CommandKey:" + getCommandKey();
    }
}
