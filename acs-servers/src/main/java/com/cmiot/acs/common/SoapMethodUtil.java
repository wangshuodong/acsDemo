package com.cmiot.acs.common;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.SoapMessageModel;

/**
 * SoapMethod 工具类
 * Created by ZJL on 2016/8/25.
 */
public class SoapMethodUtil {

    /**
     * 将Soap转换为Method
     *
     * @param method
     * @return
     */
    public static StringBuilder methodToSoap(AbstractMethod method) {
        StringBuilder builder = null;
        try {
            if (method != null) {
                SoapMessageModel soapMessageModel = new SoapMessageModel();
                method.addAllSoap(soapMessageModel);
                builder = new StringBuilder(AbstractMethod.getMsgToString(soapMessageModel));
            }
        } catch (Exception ignored) {

        }
        return builder;
    }


    /**
     * 将Method转换为Soap
     *
     * @param builder
     * @return
     */
    private final static String TOP_BOX = ">Device.";

    public static AbstractMethod soapToMethod(StringBuilder builder) {
        AbstractMethod method = null;
        try {
            if (builder != null && builder.length() > 0) {
                SoapMessageModel soapMsg = AbstractMethod.getStringToMsg(builder.toString());
                String classPath = AbstractMethod.METHOD_CLASS_PATH + AbstractMethod.getRequestName(soapMsg);
                method = (AbstractMethod) Class.forName(classPath).newInstance();
                method.parse(soapMsg);
                if (method instanceof Inform) {
                    if (builder.toString().contains(TOP_BOX)) {
                        method.setDeviceType(2L);//标识为机顶盒
                    }
                }
            }
        } catch (Exception ignored) {

        }
        return method;
    }
}
