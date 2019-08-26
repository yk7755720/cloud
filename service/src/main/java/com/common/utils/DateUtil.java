package com.common.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_TIME_PATTERN1 = "yyyyMMddHHmmss";
	public static final String DATE_TIME_PATTERN2 = "MMDDHHMMSS";
	public static final String MINUTE_PATTERN = "yyyy-MM-dd HH:mm";
	public static final String HOUR_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_PATTERN = "yyyy-MM-dd";
	public static final String DATE_PATTERN1 = "yyyyMMdd";
	public static final String MONTH_PATTERN = "yyyy-MM";
	public static final String YEAR_PATTERN = "yyyy";
	public static final String MINUTE_ONLY_PATTERN = "mm";
	public static final String HOUR_ONLY_PATTERN = "HH";

	public static String getCurrentDateTime() {
		SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_PATTERN1);// 设置日期格式
		return df.format(new Date());// new Date()为获取当前系统时间
	}
	
	public static String getCurrentDate() {
		SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN1);// 设置日期格式
		return df.format(new Date());// new Date()为获取当前系统时间
	}
	
	public static String getCurrentDate(String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);// 设置日期格式
		return df.format(new Date());// new Date()为获取当前系统时间
	}
	
	public static int getCurrentYear(){
	 	Calendar cale = Calendar.getInstance();  
        return cale.get(Calendar.YEAR);  
	}
	
	public static int getCurrentMonth(){
	 	Calendar cale = Calendar.getInstance();  
        return cale.get(Calendar.MONTH) + 1;  
	}

	public static int getCurrentDay(){
	 	Calendar cale = Calendar.getInstance();  
        return cale.get(Calendar.DATE);
	}

	public static Date DateFormat(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String format = sdf.format(date);
		ParsePosition pos=new ParsePosition(8);
		Date parse = sdf.parse(format, pos);
		return parse;
	}
	public static int dateBetween(String before, String after) {
		SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_PATTERN1);
		int sec = 0;
		try {
			Date dt1 = df.parse(before);
			Date dt2 = df.parse(after);
			long t1 = dt1.getTime();
			long t2 = dt2.getTime();
			sec = (int) (t2 - t1) / 1000;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return sec;
	}
	
	/**
	 * @param date
	 * @param day
	 *            想要获取的日期与传入日期的差值 比如想要获取传入日期前四天的日期 day=-4即可
	 * @return
	 */
	public static Date getSomeDay(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, day);
		return calendar.getTime();
	}
}
