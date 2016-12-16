package com.cmiot.rms.common.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DateTools {

    /* 时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /* 时间格式：yyyy-MM-dd */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /* 时间格式：yyyyMMdd HH:mm:ss */
    public static final String DATE_FORMAT_03 = "yyyyMMdd HH:mm:ss";

    /* 时间格式：yyyyMMdd */
    public static final String DATE_FORMAT_04 = "yyyyMMdd";

    /* 时间格式：yyyyMM */
    public static final String DATE_FORMAT_YYYYMM = "yyyyMM";

    /* 时间格式：YYYYMMDDHHMMSS */
    public static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    /* 时间格式：YYYY-MM-DD HH:MM */
    public static final String YYYY_MM_DD_HH_MM = "yyy-MM-dd HH:mm";

    public static final String YYYY_MM_DD_HHMM = "yyy-MM-dd-HH-mm";

    public static final String HH_MM_SS = "HH:mm:ss";

    /**
     * 日期格式化
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(Date date, String pattern) {
        String returnValue = "";
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            returnValue = df.format(date);
        }
        return (returnValue);
    }

    /**
     * 日期格式化
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(String date, String pattern) throws Exception {
        String returnValue = "";
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            returnValue = df.format(Long.parseLong(date));
        }
        return (returnValue);
    }

    /**
     * 日期格式化
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(Date date, String pattern, Locale locale) {
        String returnValue = "";
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern, locale);
            returnValue = df.format(date);
        }
        return (returnValue);
    }

    /**
     * 将字符串类型的日期转化成Date类型
     *
     * @param strDate
     * @param pattern
     * @return
     */
    public static Date parse(String strDate, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = df.parse(strDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字符串类型的日期转化成UTC时间格式
     * 精确到grade等级，如grade=1精确到毫秒，grade=1000精确到秒
     *
     * @param strDate
     * @param pattern
     * @param grade
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String parseTime(String strDate, String pattern, long grade) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = df.parse(strDate);
            long time = date.getTime() / grade;
            return time + "";
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * 获取时间戳
     */
    public static String getTimeString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
        Calendar calendar = Calendar.getInstance();
        return df.format(calendar.getTime());
    }

    /**
     * 获取日期年份
     *
     * @param date 日期
     * @return
     */
    public static String getYear(Date date) {
        return format(date, "yyyy-MM-dd HH:mm:ss").substring(0, 4);
    }

    /**
     * 按默认格式的字符串距离今天的天数
     *
     * @param date 日期字符串
     * @return
     */
    public static int countDays(String date) {
        long t = Calendar.getInstance().getTime().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(parse(date, "yyyy-MM-dd HH:mm:ss"));
        long t1 = c.getTime().getTime();
        return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
    }

    /**
     * 按用户格式字符串距离今天的天数
     *
     * @param date   日期字符串
     * @param format 日期格式
     * @return
     */
    public static int countDays(String date, String format) {
        long t = Calendar.getInstance().getTime().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(parse(date, format));
        long t1 = c.getTime().getTime();
        return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
    }

    /**
     * 获取某天凌晨时间
     * <一句话功能简述>
     * <功能详细描述>
     *
     * @param date
     * @return Date [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static Date getMorning(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTime();
    }

    /**
     * 获取当前时间的时间戳，精确到毫秒
     * <功能详细描述>
     *
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String getCurrentTimeMillis() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    /**
     * 获取多少天之后的日期
     * <功能详细描述>
     *
     * @param startDate
     * @param days
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String getAfterDate(String startDate, int days) {
        Date date;
        Calendar cal = Calendar.getInstance();
        try {
            date = (new SimpleDateFormat("yyyy-MM-dd")).parse(startDate);

            cal.setTime(date);
            cal.add(Calendar.DATE, days);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return (new SimpleDateFormat("yyyy-MM-dd")).format(cal.getTime());
    }

    /**
     * 日期比较
     * <功能详细描述>
     *
     * @param startDate
     * @param endDate
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static boolean compareDate(String startDate, String endDate,
                                      String format) {
        Date newDate = strDateToDate(startDate, format);
        Date newDate2 = strDateToDate(endDate, format);
        return compareDate(newDate, newDate2);
    }

    /**
     * 将字符转换为日期类型
     * <功能详细描述>
     *
     * @param strDate
     * @param sourceFormat
     * @return Date [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static Date strDateToDate(String strDate, String sourceFormat) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(sourceFormat);
        Date date = null;
        try {
            date = dateFormat.parse(strDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 判断时间大小
     * <功能详细描述>
     *
     * @param date1
     * @param date2
     * @return boolean [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static boolean compareDate(Date date1, Date date2) {
        if (date1.compareTo(date2) > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前时间
     * <功能详细描述>
     *
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String now() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    /**
     * 获取几分钟后
     * <功能详细描述>
     *
     * @param date
     * @param minute
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String afterMinute(String date, int minute) {
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MINUTE, minute);
        Date tasktime = cale.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(tasktime);
    }

    public static Date getAfterMonthStart(Date date, int months) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.MONTH, months);
        System.out.println(DateTools.format(calendar.getTime(),
                "YYYY-MM-dd HH:mm:ss"));
        return calendar.getTime();
    }

    public static Date getAfterMonth(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);

        System.out.println(DateTools.format(calendar.getTime(),
                "YYYY-MM-dd HH:mm:ss"));

        return calendar.getTime();
    }

    public static Date getAfterDay(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);

        return calendar.getTime();
    }

    public static String format(Long expireTime, String pattern) {
        try {
            if (expireTime != null) {
                return format(new Date(expireTime), pattern);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Date转换为Timestamp
     *
     * @param date
     * @param format
     * @return
     */
    public static Timestamp getDateToTimestamp(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String time = "";
        time = sdf.format(date);
        Timestamp ts = Timestamp.valueOf(time);

        return ts;
    }

    /**
     * String 转换 Timestamp
     *
     * @param str
     * @return
     */
    public static Timestamp getStrToTimestamp(String str) {
        Date date = strDateToDate(str, YYYY_MM_DD_HH_MM_SS);
        Timestamp ts = DateTools.getDateToTimestamp(date, YYYY_MM_DD_HH_MM_SS);
        return ts;
    }

    /**
     * Timestamp 转 String
     */
    public static String timestampToString(Timestamp timestamp) {
        return timestampToString(timestamp, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * Timestamp 转 String
     */
    public static String timestampToString(Timestamp timestamp, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(timestamp);
    }


    public static String timestampToHuman(int stamp) {
        Date date = new Date();
        date.setTime((long) stamp * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    /**
     * 计算时间差
     *
     * @param time1 时间1
     * @param time2 时间2
     * @return 相差的月份
     */
    public static int isCompareDate(String time1, String time2) {
        int month = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date1 = sdf.parse(time1);
            Date date2 = sdf.parse(time2);
            long time = date1.getTime() - date2.getTime();
            month = (int) (time / 1000 / 3600 / 24 / 30);
        } catch (Exception e) {
            month = 0;
        }
        return month;
    }

    /**
     * 获取当前时间:2016-04-23
     *
     * @return
     */
    public static String getCurrentDay() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD);
        return df.format(cal.getTime());
    }

    /**
     * 获取传入时间的下一天时间
     * @param day
     * @return
     */
    public static String getNextDay(String day)
    {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(day);

            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.add(calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return formatter.format(calendar.getTime());

        } catch (Exception e) {
            return"";
        }
    }

    /**
     * 获取当日到周日的日期列表
     *
     * @return
     */
    public static List<String> getDayList() {
        Calendar cal = Calendar.getInstance();

        //年
        int year = cal.get(Calendar.YEAR);

        //年
        int month = cal.get(Calendar.MONTH) + 1;

        //天
        int day = cal.get(Calendar.DATE);


        //获取当月总天数
        int days = cal.getActualMaximum(Calendar.DATE);

        //获取本周日
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        int sunday = cal.get(Calendar.DATE);

        if (sunday < day) {
            sunday = sunday + days;
        }

        List<String> dayList = new ArrayList<>();
        String strMonth = null;
        String strDay = null;
        for (int i = day; i <= sunday; i++) {
            if (i > days) {
                strMonth = (month + 1) > 10 ? ((month + 1) + "") : ("0" + (month + 1));
                strDay = (i % days) > 10 ? ((i % days) + "") : ("0" + (i % days));
            } else {
                strMonth = month > 10 ? (month + "") : ("0" + month);
                strDay = (i > 10) ? (i + "") : ("0" + i);
            }

            dayList.add(year + "-" + strMonth + "-" + strDay);
        }

        return dayList;
    }

    /***
     * 获取当前时间，精确到秒
     * @return
     */
    public static int getCurrentSecondTime()
    {
        long timeMillis = System.currentTimeMillis();
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        int currentTime = (int) timeSeconds;

        return currentTime;
    }

    /**
     * 时间转换
     *
     * @param date
     * @return
     */
    public static int timeStrToStamp(String date) {
        int time = 100;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parsedDate = dateFormat.parse(date);
            time = (int) (parsedDate.getTime() / 1000L);
        } catch (Exception e) {
        }
        return time;
    }
    
    /**
     * 获取系统当前时间戳，格式：yyyyMMddHHmmss
     * @return
     */
    public static String getTimeStamp()
    {
    	SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_YYYYMMDDHHMMSS);
		return df.format(new Date());
    }
}
