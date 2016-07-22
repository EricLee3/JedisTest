package com.service;

public class trimStrDate {
	String date;
	trimStrDate(String date)  {
		this.date = date;
		elimSpace();
	}
	
	public void dateSetter(String date)  {
		this.date = date;
	}
	private void elimSpace()  {
		date = date.replaceAll("\\s", "");
	}
	public String elimColonandMinus()  {
		return (date = date.replaceAll("[-:]", ""));
	}
	public String shrinkDate()  {
		elimSpace();
		elimColonandMinus();
		return date.substring(0,8);
	}
	
//	public static void main(String[] args)  {
//		String tdate = "2016-04-14 07:04:47";
//		trimStrDate aDate = new trimStrDate(tdate);
//		aDate.elimSpace();
//		tdate = aDate.elimColonandMinus();
//		tdate = aDate.shrinkDate();
//	}
}
