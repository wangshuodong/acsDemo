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
public final class ParameterValueStructObject extends ParameterValueStruct<Object> {
    private static final long serialVersionUID = 8230721944738939224L;

    public ParameterValueStructObject(String name, Object value) {
        super(name, value, Type_Object);
    }
}
