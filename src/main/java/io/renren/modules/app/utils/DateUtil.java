package io.renren.modules.app.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author changbindong
 * @date 5/3/23
 * @description
 */
public class DateUtil {

	public static Date getMonthStartTimeByDate(Date date) {
		long currentTime = date.getTime();
		String timeZone = "GMT+8:00";
		Calendar calendar = Calendar.getInstance();// 获取当前日期
		calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
		calendar.setTimeInMillis(currentTime);
		calendar.add(Calendar.YEAR, 0);
		calendar.add(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long timeInMillis = calendar.getTimeInMillis();
		return new Date(timeInMillis);
	}

	public static Date getMonthEndTimeByDate(Date date) {
		long currentTime = date.getTime();
		String timeZone = "GMT+8:00";
		Calendar calendar = Calendar.getInstance();// 获取当前日期
		Calendar calendar2 = Calendar.getInstance();// 获取当前日期
		calendar2.setTimeZone(TimeZone.getTimeZone(timeZone));
		calendar2.setTimeInMillis(currentTime);
		calendar2.add(Calendar.YEAR, 0);
		calendar2.add(Calendar.MONTH, 0);
		calendar2.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));// 获取当前月最后一天
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		long timeInMillis2 = calendar2.getTimeInMillis();
		return new Date(timeInMillis2);

	}
}
