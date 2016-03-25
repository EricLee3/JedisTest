package com.service.command.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CommonUtil {
	/**
	 * 현재 날짜를 리턴해 주는 유틸
	 * @return
	 */
	@SuppressWarnings("unused")
	public static String getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		// 날짜를 통신용 문자열로 변경       
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");       
		return formatter.format(cal.getTime());   
	}
	
	public static String getCurrentDeciDate() {
		Calendar cal = Calendar.getInstance();
		// 날짜를 통신용 문자열로 변경       
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");       
		return formatter.format(cal.getTime());   
	}
	
	/**
	 * 이전 날짜(8일전) 를 리턴해 주는 유틸
	 * @return
	 */
	public static String getPastDeciDate() {
		Calendar cal = Calendar.getInstance();
		// 날짜를 통신용 문자열로 변경       
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		cal.add(cal.DATE,-8);
		return formatter.format(cal.getTime());   
	}
}
