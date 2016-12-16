
package com.cmiot.acs.control;


import com.cmiot.acs.model.Inform;

/**
 * @author zjialin
 * @version 1.0.0
 * @history<br/> ver    date       author desc
 * 1.0.0  2016-01-28 zjialin    created<br/>
 * <p/>
 * @since 1.0.0
 */
public abstract class DefaultInformHandleAdpter implements IInformHandle {

    private ACSProcessControl acsProcessControl;

    @Override
    public ACSProcessControl getAcsProcessControl() {
        return acsProcessControl;
    }

    @Override
    public void setAcsProcessControl(ACSProcessControl acsProcessControl) {
        this.acsProcessControl = acsProcessControl;
    }

    @Override
    public void diagnosticsCompleteEvent(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void kickedEvent(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void periodicEvent(Inform inform) {
        // TODO Auto-generated method stub

    }

}
