package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GatewayBackupFileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GatewayBackupFileInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(GatewayBackupFileInfo record);

    int insertSelective(GatewayBackupFileInfo record);

    GatewayBackupFileInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GatewayBackupFileInfo record);

    int updateByPrimaryKey(GatewayBackupFileInfo record);

    /**
     * 根据网关Id查询备份文件数量
     * @param gatewayId
     * @return
     */
    int selectCountByGatewayId(@Param("gatewayId") String gatewayId);

    /**
     * 根据网关Id查询正在备份的数量，用于判断当前网关是否正在备份中
     * @param gatewayId
     * @return
     */
    int selectBackupCount(@Param("gatewayId") String gatewayId);

    /**
     * 根据网关Id查询其备份文件列表
     * @param gatewayId
     * @return
     */
    List<GatewayBackupFileInfo> selectListByGatewayId(@Param("gatewayId")String gatewayId);

    /**
     * 查询正在处理中的备份任务
     * @return
     */
    List<GatewayBackupFileInfo> selectProcessingList();
}