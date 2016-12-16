package com.cmiot.rms.dao;

import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.derivedclass.GatewayBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SynDataDao {

    private static Logger logger = LoggerFactory.getLogger(SynDataDao.class);

    public List<GatewayBean> queryList4Page(Connection conn) throws SQLException
    {
        List<GatewayBean> gatewayBeanList = new ArrayList<GatewayBean>();
        // 获得数据库连接
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT a.GATEWAY_MACADDRESS, a.gateway_factory AS GATEWAY_FACTORY, " + "a.gateway_model AS DEVICE_MODEL, a.GATEWAY_UUID, a.GATEWAY_TYPE, a.GATEWAY_MODEL,"
                + " a.GATEWAY_NAME, a.GATEWAY_VERSION, a.GATEWAY_SERIALNUMBER, (select code from t_manufacturer_info where id = a.new_factory_code) AS GATEWAY_FACTORY_CODE, "
                + "a.GATEWAY_MEMO, a.GATEWAY_HARDWARE_VERSION, a.GATEWAY_JOIN_TIME, a.GATEWAY_LAST_CONN_TIME, "
                + "a.GATEWAY_ADSL_ACCOUNT, a.GATEWAY_STATUS, a.GATEWAY_ADDRESSINGTYPE, a.GATEWAY_IPADDRESS,"
                + " a.GATEWAY_SUBNETMASK, a.GATEWAY_DEFAULTGATEWAY, a.GATEWAY_DNSSERVERS, a.GATEWAY_DHCPOPTIONNUMBEROFENTRIES," + " a.GATEWAY_URL, a.GATEWAY_CONNECTIONREQUESTURL, "
                + "a.GATEWAY_AREA_ID, a.GATEWAY_DEVICE_UUID, a.GATEWAY_FIRMWARE_UUID, a.GATEWAY_EXTERNALIPADDRESS, "
                + "a.GATEWAY_USER_PHONE, a.GATEWAY_USER_NAME, a.GATEWAY_USER_ADDRESS, a.GATEWAY_PASSWORD, a.GATEWAY_DIGEST_ACCOUNT,"
                + " a.GATEWAY_DIGEST_PASSWORD, a.GATEWAY_FAMILY_ACCOUNT, a.GATEWAY_FAMILY_PASSWORD, a.log_switch_status ");
        // 添加获取VOIP用户账号
        sb.append(",  (		SELECT			CASE		WHEN b.business_type = 8 THEN			''		ELSE			b.parameter_list		END parameterList		FROM			"
                + "t_gateway_business b		WHERE			(				b.business_type = 1				OR b.business_type = 9				OR b.business_type = 8			)		"
                + "AND b.business_statu = 1		AND (			lower(				SUBSTR(b.business_code_boss FROM 1 FOR 4)			) = LOWER('vOip')			OR lower(				"
                + "SUBSTR(b.business_code FROM 1 FOR 4)			) = LOWER('vOip')			OR lower(				SUBSTR(b.business_name FROM 1 FOR 4)			) = LOWER('vOip')		)		"
                + "AND b.gateway_password = a.GATEWAY_PASSWORD		ORDER BY			b.update_time DESC,			b.create_time DESC		LIMIT 1	) parameterList ");
        sb.append("FROM t_gateway_info a");
        logger.info(" sql: " + sb.toString());
        // 通过数据库的连接操作数据库，实现增删改查
        PreparedStatement ptmt = conn.prepareStatement(sb.toString());
        ResultSet rs = ptmt.executeQuery();
        GatewayBean gatewayBean = null;
        while (rs.next())
        {
            gatewayBean = new GatewayBean();
            gatewayBean.setGatewayAreaId(rs.getString("gateway_area_id"));
            gatewayBean.setGatewayPassword(rs.getString("gateway_password"));
            gatewayBean.setGatewaySerialnumber(rs.getString("gateway_serialnumber"));
            gatewayBean.setGatewayMacaddress(rs.getString("gateway_macaddress"));
            gatewayBean.setGatewayModel(rs.getString("gateway_model"));
            gatewayBean.setGatewayFactoryCode(rs.getString("gateway_factory_code"));
            gatewayBean.setGatewayVersion(rs.getString("gateway_version"));
            gatewayBean.setGatewayAdslAccount(rs.getString("gateway_adsl_account"));
            gatewayBean.setGatewayMemo(rs.getString("gateway_memo"));
            gatewayBean.setParameterList(rs.getString("parameterList"));
            gatewayBeanList.add(gatewayBean);
        }
        closeConnect(rs,ptmt,conn);
        return gatewayBeanList;
    }

    public List<BoxInfo> selectBoxInfo(Connection conn) throws SQLException
    {
        List<BoxInfo> boxinfoBeanList = new ArrayList<BoxInfo>();
        // 获得数据库连接
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT BOX_UUID, BOX_NAME, BOX_SERIALNUMBER, BOX_MACADDRESS, GATEWAY_UUID, BOX_TYPE, " +
                "BOX_MODEL, BOX_FACTORY_CODE, BOX_ONLINE, BOX_FIRMWARE_UUID, BOX_HARDWARE_VERSION, BOX_AREA_ID, " +
                "BOX_MEMO, BOX_IPADDRESS, BOX_URL, BOX_CONNECTIONREQUESTURL, BOX_CONN_TYPE, BOX_DIGEST_ACCOUNT, " +
                "BOX_DIGEST_PASSWORD, BOX_FAMILY_ACCOUNT, BOX_FAMILY_PASSWORD, BOX_FILE_URL, BOX_JOIN_TIME, BOX_LAST_CONN_TIME," +
                " BOX_STATUS, box_connect_account, box_connect_password FROM t_box_info");
        // 通过数据库的连接操作数据库，实现增删改查
        PreparedStatement ptmt = conn.prepareStatement(sb.toString());
        ResultSet rs = ptmt.executeQuery();
        BoxInfo boxInfo = null;
        while (rs.next())
        {
            boxInfo = new BoxInfo();
            boxInfo.setBoxAreaId(rs.getString("BOX_AREA_ID"));
            boxInfo.setBoxSerialnumber(rs.getString("BOX_SERIALNUMBER"));
            boxInfo.setBoxMacaddress(rs.getString("BOX_MACADDRESS"));
            boxInfo.setBoxType(rs.getString("BOX_TYPE"));
            boxInfo.setBoxModel(rs.getString("BOX_MODEL"));
            boxInfo.setBoxFactoryCode(rs.getString("BOX_FACTORY_CODE"));
            boxInfo.setBoxFirmwareUuid(rs.getString("BOX_FIRMWARE_UUID"));
            boxInfo.setBoxFirmwareUuid(rs.getString("BOX_FIRMWARE_UUID"));
            boxInfo.setBoxMemo(rs.getString("BOX_MEMO"));
            if(rs.getString("box_connect_account") != null && !"".equals(rs.getString("box_connect_account"))){
                boxInfo.setBoxConnectAccount(rs.getString("box_connect_account"));
            }
            boxinfoBeanList.add(boxInfo);
        }
        closeConnect(rs,ptmt,conn);
        return boxinfoBeanList;
    }

    public List<BoxFirmwareInfo> queryFirmware(Connection conn) throws SQLException
    {
        List<BoxFirmwareInfo> boxFirmwareInfoList = new ArrayList<BoxFirmwareInfo>();
        // 获得数据库连接
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT f.id id, f.firmware_version firmwareVersion FROM t_box_firmware_info f, t_box_device_info t, t_box_factory_info h WHERE f.device_id = t.id AND t.factory_code = h.factory_code");
        // 通过数据库的连接操作数据库，实现增删改查
        PreparedStatement ptmt = conn.prepareStatement(sb.toString());
        ResultSet rs = ptmt.executeQuery();
        BoxFirmwareInfo boxFirmwareInfo = null;
        while (rs.next())
        {
            boxFirmwareInfo = new BoxFirmwareInfo();
            boxFirmwareInfo.setFirmwareVersion(rs.getString("firmwareVersion"));
            boxFirmwareInfo.setId(rs.getString("id"));
            boxFirmwareInfoList.add(boxFirmwareInfo);
        }
        closeConnect(rs,ptmt,conn);
        return boxFirmwareInfoList;
    }

    public void closeConnect(ResultSet rs,PreparedStatement ptmt, Connection conn) throws SQLException {
        rs.close();
        ptmt.close();
        conn.close();
    }
}
