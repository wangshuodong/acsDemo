package com.cmiot.rms.common.enums;

public enum ServiceListEnum {
	TR069("tro69"),
	INTERNET("internet"),
	VOIP("voip"),
	OTHER("other"),;
	
	private String description;
	
	ServiceListEnum(String description){
		this.description = description;
	}
	
	public static String getName(String name){
		for(ServiceListEnum sle : ServiceListEnum.values()){
			if(name.equals(sle.name())){
				return sle.name();
			}
		}
		return null;
	}

}
