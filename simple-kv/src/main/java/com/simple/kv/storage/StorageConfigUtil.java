package com.simple.kv.storage;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class StorageConfigUtil {

	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	public static final int BJ_OFFSET = 8;
	
	private static Calendar getDailyExpiredCalendar(StorageConfig sc) {
		Calendar c = Calendar.getInstance(GMT, Locale.US);
		c.add(Calendar.HOUR_OF_DAY, BJ_OFFSET - sc.getDailyExpiredTime().first);
		c.add(Calendar.MINUTE, -sc.getDailyExpiredTime().second);
		return c;
	}
	
	public static int getExpiredWeekDay(StorageConfig sc) {
		return getDailyExpiredCalendar(sc).get(Calendar.DAY_OF_WEEK);
	}
	
	/**
	 * 对于按天过期的缓存配置，返回剩余秒数
	 * @param sc 配置
	 * @return
	 */
	public static int getRemainSecondForDailyExpired(StorageConfig sc) {
		Calendar c = getDailyExpiredCalendar(sc);
		long m1 = c.getTimeInMillis();
		c.add(Calendar.DATE, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		long m2 = c.getTimeInMillis();
		int remain = Math.max((int) ((m2 - m1) / 1000), 1);
		return remain;
	}
	
}
