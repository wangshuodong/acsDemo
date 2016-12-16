/**
 * 
 */
package com.cmiot.rms.dao.mapper;

import java.util.List;

import com.cmiot.rms.dao.model.DatabaseRecord;

/**
 * @author heping
 *
 */
public interface DatabaseMapper {
	int insert(DatabaseRecord databaseRecord);
	
	int update(DatabaseRecord databaseRecord);
	
	List<DatabaseRecord> query(DatabaseRecord databaseRecord);
}
