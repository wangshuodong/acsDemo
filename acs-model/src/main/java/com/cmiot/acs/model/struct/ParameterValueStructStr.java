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
public final class ParameterValueStructStr extends ParameterValueStruct<String> {
    private static final long serialVersionUID = 8383736136511239687L;

    public ParameterValueStructStr(String name, String value) {
        super(name, value, Type_String);
    }
}
