package com.cmiot.rms.services;

import java.util.Map;

/**
 * 网关配置文件管理接口
 * Created by panmingguo on 2016/6/4.
 */
public interface GatewayBackupFileService {

    /**
     * 网关数据备份
     * @param parameter
     * @return
     */
    Map<String,Object> backup(Map<String, Object> parameter);

    /**
     * 网关数据恢复
     * @param parameter
     * @return
     */
    Map<String,Object> restore(Map<String, Object> parameter);


    /**
     * 查询备份文件列表
     * @param parameter
     * @return
     */
    Map<String,Object> queryBackupFileList(Map<String, Object> parameter);

    /**
     * 设置网关备份文件数量限制
     * @param parameter
     * @return
     */
    Map<String,Object> saveFileNumberLimit(Map<String, Object> parameter);


    /**
     * 文件服务器在文件上传完成后回调该接口更新文件大小、名称、状态
     * @param parameter
     * @return
     */
    Map<String, Object> updateBackupFileInfo (Map<String, Object> parameter);

    /**
     * 删除备份文件
     * @param parameter
     * @return
     */
    Map<String, Object> deleteBackupFile(Map<String, Object> parameter);

}
