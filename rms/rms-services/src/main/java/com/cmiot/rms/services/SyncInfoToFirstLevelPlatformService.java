package com.cmiot.rms.services;

import java.util.Map;

public interface SyncInfoToFirstLevelPlatformService {

	void report(String method ,Map<String, Object> reportMap);

}
