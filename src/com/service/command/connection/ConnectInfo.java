package com.service.command.connection;

import java.io.*;



public class ConnectInfo {
	private String dbMode;
	
	private String dbDriver;
	private String dbConnect;
	private String dbAccount;
	private String dbPassword;
	private int maxActive;
	
	private String datasourceName;
	
	public String getDbAccount() {
		return dbAccount;
	}
	public void setDbAccount(String dbAccount) {
		this.dbAccount = dbAccount;
	}
	public String getDbConnect() {
		return dbConnect;
	}
	public void setDbConnect(String dbConnect) {
		this.dbConnect = dbConnect;
	}
	public String getDbDriver() {
		return dbDriver;
	}
	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}
	public String getDbPassword() {
		return dbPassword;
	}
	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
	public int getMaxActive() {
		return maxActive;
	}
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}		
	public String getDatasourceName() {
		return datasourceName;
	}
	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}	
	public String getDbMode() {
		return dbMode;
	}
	public void setDbMode(String dbMode) {
		this.dbMode = dbMode;
	}
	public ConnectInfo() throws IOException {
		dbMode = "1";
		dbDriver = "oracle.jdbc.driver.OracleDriver";
		//dbConnect = "jdbc:oracle:thin:@220.117.243.57:1521:WIZEMS";
		//dbConnect = "jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8";
		//dbAccount = "iseccube";
		//dbPassword = "iseccube1234";
		
		// test ISECCube	// [IOS 21-JAN-16]
		dbConnect = "jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8";
		dbAccount = "iseccube";
		dbPassword = "iseccube1234";

		maxActive = 100;
		
	}
}
