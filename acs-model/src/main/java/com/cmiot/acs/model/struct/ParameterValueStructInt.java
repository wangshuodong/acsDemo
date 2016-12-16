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
public final class ParameterValueStructInt extends ParameterValueStruct<Integer> {
    private static final long serialVersionUID = 4753580287517867419L;

    public ParameterValueStructInt(String name, Integer value) {
        super(name, value, Type_Int);
    }
}
