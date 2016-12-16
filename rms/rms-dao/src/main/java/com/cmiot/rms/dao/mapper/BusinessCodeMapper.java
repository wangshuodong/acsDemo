package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.BusinessCode;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface BusinessCodeMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_business_code
     *
     * @mbggenerated
     */
    @Delete({
        "delete from t_business_code",
        "where business_code_boss = #{businessCodeBoss,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String businessCodeBoss);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_business_code
     *
     * @mbggenerated
     */
    @Insert({
        "insert into t_business_code (business_code_boss)",
        "values (#{businessCodeBoss,jdbcType=VARCHAR})"
    })
    int insert(BusinessCode record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_business_code
     *
     * @mbggenerated
     */
    int insertSelective(BusinessCode record);

    List<BusinessCode> seletAll();
}