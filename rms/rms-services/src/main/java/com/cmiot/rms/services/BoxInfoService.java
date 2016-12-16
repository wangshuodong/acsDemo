
package com.cmiot.rms.services;



import com.cmiot.acs.model.Inform;



import java.util.List;

import com.cmiot.rms.dao.model.BoxInfo;

/**
 * 
 * @author zhangchuan
 *
 */
public interface BoxInfoService {
	
	void insertSelective(BoxInfo record);


    void updateByPrimaryKeySelective(BoxInfo record);

    void updateInformBoxInfo(Inform informReq);
    
    List<BoxInfo> queryListByIds(List<String> boxIds);
    
    BoxInfo selectByPrimaryKey(String boxUuid);
    
    int updateBySnSelective(BoxInfo record);
}
