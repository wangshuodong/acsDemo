package com.cmiot.rms.common.utils;

/**
 * IP4地址比较工具类
 * Created by panmingguo on 2016/5/17.
 */
public class IpV4Util {

    /**
     * 判断Ip是否在某一个IP段中
     * @param startIp
     * @param endIp
     * @param sourceIp
     * @return
     */
    public static Boolean checkIpV4(String startIp,String endIp, String sourceIp)
    {
        if(((compareIpV4s(sourceIp, startIp)) >= 0) && (compareIpV4s(sourceIp, endIp) <= 0))
        {
            return true;
        }

        return false;
    }

    /**
     * 比较两个ip地址，如果两个都是合法地址，则1代表ip1大于ip2，-1代表ip1小于ip2,0代表相等；
     * 如果有其一不是合法地址，如ip2不是合法地址，则ip1大于ip2，返回1，反之返回-1；两个都是非法地址时，则返回0；
     * 注意此处的ip地址指的是如“192.168.1.1”地址，并不包括mask
     * @param ip1
     * @param ip2
     * @return
     */
    public static int compareIpV4s(String ip1,String ip2)
    {
        int result = 0;
        int ipValue1 = getIpV4Value(ip1); // 获取ip1的32bit值
        int ipValue2 = getIpV4Value(ip2); // 获取ip2的32bit值
        if(ipValue1 > ipValue2)
        {
            result =  1;
        }
        else if(ipValue1 < ipValue2)
        {
            result = -1;
        }
        else if(ipValue1 == ipValue2)
        {
            result = -0;
        }
        return result;
    }

    /**
     * 将Ip地址转换为int
     * @param ipOrMask
     * @return
     */
    public static int getIpV4Value(String ipOrMask)
    {
        byte[] addr = getIpV4Bytes(ipOrMask);
        int address1  = addr[3] & 0xFF;
        address1 |= ((addr[2] << 8) & 0xFF00);
        address1 |= ((addr[1] << 16) & 0xFF0000);
        address1 |= ((addr[0] << 24) & 0xFF000000);
        return address1;
    }


    /**
     *将IP地址转换为byte
     * @param ipOrMask
     * @return
     */
    public static byte[] getIpV4Bytes(String ipOrMask)
    {
        try
        {
            String[] addrs = ipOrMask.split("\\.");
            int length = addrs.length;
            byte[] addr = new byte[length];
            for (int index = 0; index < length; index++)
            {
                addr[index] = (byte) (Integer.parseInt(addrs[index]) & 0xff);
            }
            return addr;
        }
        catch (Exception e)
        {
        }
        return new byte[4];
    }
}
