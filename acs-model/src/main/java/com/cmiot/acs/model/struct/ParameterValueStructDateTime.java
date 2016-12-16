package com.cmiot.acs.model.struct;

/**
 * TODO Add class comment here<p/>
 *
 * @author zjialin
 * @version 1.0.0
 * @history<br/> ver date author desc
 * 1.0.0 2016-01-21 zjialin created<br/>
 * <p/>
 * @since 1.0.0
 */
public final class ParameterValueStructDateTime extends ParameterValueStruct<String> {
    private static final long serialVersionUID = 8383736136511239623L;

    /**
     * 表示为UTC（全球统一时间）
     * 2天3小时4分5秒将表示为0000-00-02T03:04:05
     *
     * @param name
     * @param value
     */
    public ParameterValueStructDateTime(String name, String value) {
        super(name, value, Type_DateTime);
    }
}
