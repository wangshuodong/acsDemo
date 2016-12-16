package com.cmiot.rms.common.enums;

public enum LogTypeEnum {
	LOG_TYPE_SYSTEM("SYSTEM","系统日志"),
	LOG_TYPE_SAFE("SAFE","安全日志"),
	LOG_TYPE_OPERATION("OPERATION","操作日志"),
	LOG_TYPE_ALARM("ALARM","告警日志"),
	;
	

	 /* 描述 */
    private String description;
    /*编码*/
    private String code;
	LogTypeEnum(String code, String description) {
		this.code = code;
        this.description = description;
    }


    public String description() {
        return description;
    }
    
    public String code(){
    	return code;
    }

    // 普通方法
    public static String getDescription(String code) {
        for (LogTypeEnum lte : LogTypeEnum.values()) {
            if (lte.code().equals(code)) {
                return lte.description() + "";
            }
        }
        return null;
    }
    public static String getCode(String description){
    	for(LogTypeEnum lte : LogTypeEnum.values()){
    		if(lte.description().equals(description)){
    			return lte.code() + "";
    		}
    	}
    	return null;
    }
}
