package com.simple.base.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

/**
 * 时间日期工具类
 */
public class DateTimeUtil {

	private static final Logger logger = Logger.getLogger(DateTimeUtil.class);

	public static final long MILLISECOND = 1;

	public static final long SECOND = 1000 * MILLISECOND;

	public static final long MINUTE = 60 * SECOND;

	public static final long HOUR = 60 * MINUTE;

	public static final long DAY = 24 * HOUR;

	public static final long WEEK = 7 * DAY;

	public static final int MINUTE_SECOND = 60;

	public static final int HOUR_SECOND = 60 * MINUTE_SECOND;

	public static final int DAY_SECOND = 24 * HOUR_SECOND;

	public static final int WEEK_SECOND = 7 * DAY_SECOND;

	/** 日期格式：yyyy-MM-dd HH:mm:ss.SSS */
	public static final String YMD_HMSSS = "yyyy-MM-dd HH:mm:ss.SSS";
	/** 日期格式：yyyyMMddHHmmssSSS */
	public static final String YMDHMSSS = "yyyyMMddHHmmssSSS";
	/** 日期格式：yyyy-MM-dd HH:mm:ss */
	public static final String YMD_HMS = "yyyy-MM-dd HH:mm:ss";
	/** 日期格式：yyyy-MM-dd HH:mm */
	public static final String YMD_HM = "yyyy-MM-dd HH:mm";
	/** 日期格式：yyyyMMddHHmmss */
	public static final String YMDHMS = "yyyyMMddHHmmss";
	/** 日期格式：yyyy-MM-dd */
	public static final String YMD = "yyyy-MM-dd";
	/** 时间格式：HH:mm:ss */
	public static final String HMS = "HH:mm:ss";

	private static final String DEFAULT_TYPE = YMD_HMS;

	private static final ConcurrentMap<String, SimpleDateFormat> map = CollectionUtil.newConcurrentMap();

	private static SimpleDateFormat getFmt(String type) {
		SimpleDateFormat fmt = map.get(type);
		if (fmt == null) {
			fmt = new SimpleDateFormat(type);
			SimpleDateFormat tmp = map.putIfAbsent(type, fmt);
			if (tmp != null) {
				fmt = tmp;
			}
		}
		return fmt;
	}

	/**
	 * 格式化日期时间
	 * 
	 * @param date
	 *            日期时间对象
	 * @param format
	 *            格式化类型
	 * @return 格式化后的字符串
	 */
	public static String format(Date date, String format) {
		if (date == null) {
			return "null";
		}
		if (StringUtil.isEmpty(format)) {
			format = DEFAULT_TYPE;
		}
		String s;
		synchronized (format.intern()) {
			s = getFmt(format).format(date);
		}
		return s;
	}

	/**
	 * 以默认形式yyyy-MM-dd HH:mm:ss格式化日期时间
	 * 
	 * @param date
	 *            日期时间对象
	 * @return 格式化后的字符串
	 */
	public static String format(Date date) {
		return format(date, DEFAULT_TYPE);
	}

	/**
	 * 格式化日期时间
	 * 
	 * @param date
	 *            日期时间的毫秒数
	 * @param format
	 *            格式化类型
	 * @return 格式化后的字符串
	 */
	public static String format(long date, String format) {
		return format(new Date(date), format);
	}

	/**
	 * 以默认形式yyyy-MM-dd HH:mm:ss格式化日期时间
	 * 
	 * @param date
	 *            日期时间的毫秒数
	 * @return 格式化后的字符串
	 */
	public static String format(long date) {
		return format(new Date(date), DEFAULT_TYPE);
	}

	/**
	 * 解析日期时间
	 * 
	 * @param str
	 *            日期时间字符串
	 * @param format
	 *            格式化类型
	 * @return 日期时间对象
	 */
	public static Date parse(String str, String format) {
		if (StringUtil.isEmpty(str)) {
			return null;
		}
		if (StringUtil.isEmpty(format)) {
			format = DEFAULT_TYPE;
		}
		try {
			synchronized (format.intern()) {
				return getFmt(format).parse(str);
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 解析日期时间
	 * 
	 * @param str
	 *            日期时间字符串
	 * @return 日期时间对象
	 */
	public static Date parse(String str) {
		return parse(str, DEFAULT_TYPE);
	}

	/**
	 * 获取当期日期时间对象
	 * 
	 * @return 日期时间对象
	 */
	public static Date getNow() {
		return new Date();
	}

	/**
	 * 根据日历的规则，为给定的日历字段添加或减去指定的时间量
	 * 
	 * @param from
	 *            原始日期时间
	 * @param timeType
	 *            Calendar中定义的日历字段值
	 * @param amount
	 *            为字段添加的日期或时间量,可以为负值
	 * @see java.util.Calendar
	 * @return 新的日期时间对象
	 */
	public static Date getDateTimeOffset(Date from, int timeType, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(from);
		c.add(timeType, amount);
		return c.getTime();
	}

	/**
	 * 返回指定时间的某天之后或之前的时间
	 * 
	 * @param from
	 *            原始日期时间
	 * @param days
	 *            天数，可以为负值
	 * @return 新的日期时间对象
	 */
	public static Date getDateOffset(Date from, int days) {
		return getDateTimeOffset(from, Calendar.DAY_OF_MONTH, days);
	}

	/**
	 * 返回指定时间的某月之后或之前的时间
	 * 
	 * @param from
	 *            原始日期时间
	 * @param months
	 *            月数，可以为负值
	 * @return 新的日期时间对象
	 */
	public static Date getMonthOffset(Date from, int months) {
		return getDateTimeOffset(from, Calendar.MONTH, months);
	}

	/**
	 * 返回指定时间的某天之后或之前的时间
	 * 
	 * @param from
	 *            原始日期时间
	 * @param hours
	 *            小时数，可以为负值
	 * @return 新的日期时间对象
	 */
	public static Date getHourOffset(Date from, int hours) {
		return getDateTimeOffset(from, Calendar.HOUR_OF_DAY, hours);
	}

	/**
	 * 判断时间是否在两个时间之间
	 * 
	 * @param time
	 *            需要判断的时间
	 * @param start
	 *            起始时间
	 * @param end
	 *            终止时间
	 * @return 如果在两个时间之间则返回true，否则false
	 */
	public static boolean timeIsIn(Date time, Date start, Date end) {
		return time.after(start) && time.before(end);
	}

	/**
	 * 判断现在是否在两个时间之间
	 * 
	 * @param start
	 *            起始时间
	 * @param end
	 *            终止时间
	 * @return 如果在两个时间之间则返回true，否则false
	 */
	public static boolean nowIsIn(Date start, Date end) {
		return timeIsIn(new Date(), start, end);
	}

	/**
	 * 判断一个年份是否闰年(格里高利历)的简单实现.1582年以后的世纪年必须是400的倍数才是闰年,1582年以前则不论.
	 * 
	 * @param year
	 *            年份
	 * @return 如果是闰年则返回true，否则返回false
	 */
	public static boolean isLeapYear(int year) {
		if ((year & 3) != 0) {
			return false;
		}
		if (year > 1582) {
			return (year % 25 != 0) || (year % 16 == 0); // Gregorian
		}
		if (year < 1582) {
			return true; // Julian
		}
		return false;
	}

	/**
	 * 判断两个日期是否是同一年的同一个月
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @return 满足条件则返回true，否则返回false
	 */
	public static boolean isSameMonth(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		if (cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) {
			return false;
		}
		return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}

	/**
	 * 返回某个日期到月底剩余的天数
	 * 
	 * @param date
	 *            指定的日期
	 * @return 到月底的剩余天数（包含当天）
	 */
	public static int getLeftDaysOfCurMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int leftdays = cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH) + 1;
		return leftdays;
	}

	/**
	 * 获取当前月份的显示（1~12）
	 * 
	 * @return
	 */
	public static int getCurMonth() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MONTH) + 1;
	}

	/**
	 * 获取当前是哪年
	 * 
	 * @return
	 */
	public static int getCurYear() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR);
	}

	/**
	 * 获取今天是几号
	 * 
	 * @return
	 */
	public static int getDayOfCurMonth() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DATE);
	}

	/**
	 * 获取指定某年的这个月有多少天
	 * 
	 * @param year
	 * @param month
	 *            1~12
	 * @return
	 */
	public static int getDayNumsOfMonth(int year, int month) {
		Calendar c = Calendar.getInstance();
		c.set(year, month - 1, 1);
		return c.getActualMaximum(Calendar.DATE);
	}

	/**
	 * 获取当前这个月有多少天
	 * 
	 * @return
	 */
	public static int getDayNumsOfCurMonth() {
		return getDayNumsOfMonth(getCurYear(), getCurMonth());
	}

	public static int currentTimeSeconds() {
		long timeMillis = System.currentTimeMillis();
		return (int) (timeMillis / SECOND);
	}

	public static void main(String[] args) {
		System.out.println(currentTimeSeconds());
	}

}
