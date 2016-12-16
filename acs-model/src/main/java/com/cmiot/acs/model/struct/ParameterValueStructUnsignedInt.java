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
public final class ParameterValueStructUnsignedInt extends ParameterValueStruct<Long> {
    private static final long serialVersionUID = -1252168441231306256L;

    public ParameterValueStructUnsignedInt(String name, Long value) {
        super(name, value, Type_UnsignedInt);
    }
}
