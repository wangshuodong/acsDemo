package com.cmiot.rms.services.util;

import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.utils.DateTools;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by panmingguo on 2016/5/9.
 */
public class InstructionUtil {

    public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z" };


    /**
     * 通过正则表达式来获取实际带有{i}的节点(匹配多个)
     * @param nameList
     * @param regValueList
     * @param value
     */
    public static void getName(List<String> nameList, List<String> regValueList, String value)
    {
        for(String regValue : regValueList)
        {
            Pattern pattern = Pattern.compile(regValue);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                nameList.add(value);
                return;
            }
        }
    }

    /**
     * 通过正则表达式来获取实际带有{i}的节点（匹配单个）
     * @param nameList
     * @param regValue
     * @param value
     */
    public static void getName(List<String> nameList, String regValue, String value)
    {
        Pattern pattern = Pattern.compile(regValue);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            nameList.add(value);
            return;
        }
    }

    /**
     * 拼装返回值
     * @param parameter
     */
    public static Map<String, Object> getResultMap(Map<String, Object> parameter)
    {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        retMap.put(Constant.RESULT, 0);
        return retMap;
    }

    /**
     * 随机生成8位16进制数
     * @return
     */
    public static String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }

    /**
     * 随机生成8位16进制数
     * @return
     */
    public static String generate8HexString() {
        Random random = new Random();
        String s = "";
        for (int i = 0; i < 8; i++) {
            s += Integer.toHexString(random.nextInt(16));
        }
        return s;
    }

    /**
     * 随机生成16位16进制数
     * @return
     */
    public static String generate16Uuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 16; i++) {
            String str = uuid.substring(i * 2, i * 2 + 2);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }
}
