package com.loic.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
	
	public static Calendar getCalendarParis(){
		Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        c.setFirstDayOfWeek(Calendar.MONDAY);
        return c;
	}
	/**
     * 得到当前的周数（一年中的第几周）
     * 
     */
	public static int getNumWeek() {
        Calendar c = getCalendarParis();
        return c.get(Calendar.WEEK_OF_YEAR);
	}
	
	public static int getNumWeek(Date date) {
		Calendar c = getCalendarParis();
		c.setTime(date);
        return c.get(Calendar.WEEK_OF_YEAR);
	}
	
	/**
     * 得到日期（月/日）
     * 
     */
	public static String getDateViaNumWeek(int numweek, int dayOfWeek) {
		Calendar cal = getCalendarParis();
		cal.set(Calendar.WEEK_OF_YEAR, numweek);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.WEEK_OF_YEAR, numweek);
		int num = cal.get(Calendar.MONTH) + 1;

		String date = (num < 10) ? "0" + num : "" + num;
		date += "/";
		num = cal.get(Calendar.DAY_OF_MONTH);
		date += (num < 10) ? "0" + num : "" + num;
		return date;
	}
	
	/**
     * 得到公元多少年
     */
	public static int getYear() {
		Calendar cal = getCalendarParis();
		return cal.get(Calendar.YEAR);
	}
	
	/**
     * 星期几
     */
	public static int getDayOfWeek() {
		Calendar c = getCalendarParis();
		int xq = c.get(Calendar.DAY_OF_WEEK);
		return xq;
	}
	
	public static int getDayOfWeek(Date date) {
		Calendar c = getCalendarParis();
		c.setTime(date);
		int xq = c.get(Calendar.DAY_OF_WEEK);
		return xq;
	}
}
