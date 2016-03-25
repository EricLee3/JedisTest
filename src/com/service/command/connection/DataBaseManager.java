package com.service.command.connection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;


import org.apache.commons.dbutils.DbUtils;
import com.service.command.log.Logger;


/**
 * @author sylyu
 * @version 1.0
 */
public class DataBaseManager{
	public DataBaseManager() {
		
	}
	
	
  	/**
	 * Log4j Object.
	 */
	public synchronized static Connection getConnection(String dbmode) throws SQLException,Exception {
		ConnectInfo connectInfo = null;
		Connection con = null;
		
		try {
			connectInfo = new ConnectInfo();
			
			Class.forName(connectInfo.getDbDriver()).newInstance();
			//cube 테스트 계정
			if(dbmode.equals("iseccube")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("iseccube");
				connectInfo.setDbPassword("iseccube1234");
			}
			//나머지는 큐브   운영 서버 
			/*
			else if(dbmode.equals("wckcube")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("wckcube");
				connectInfo.setDbPassword("wckcube");
			}else if(dbmode.equals("slvcube")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("slvcube");
				connectInfo.setDbPassword("slvcube1234");
			}else if(dbmode.equals("disysmrm")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("disysmrm");
				connectInfo.setDbPassword("disysmrm1234");
			}else if(dbmode.equals("mofcube")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("mofcube");
				connectInfo.setDbPassword("mofcube1234");
			}else if(dbmode.equals("shoplinker")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("shoplinker");
				connectInfo.setDbPassword("shoplinker1234");
			}else if(dbmode.equals("jnscube")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("JNSCUBE");
				connectInfo.setDbPassword("JNSCUBE1234");
			}else if(dbmode.equals("rabcube")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("RABCUBE");
				connectInfo.setDbPassword("RABCUBE1234");
			}else if(dbmode.equals("soucube")) {
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("SOUCUBE");
				connectInfo.setDbPassword("SOUCUBE1234");
			}else if(dbmode.equals("redcube")) {
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("REDCUBE");
				connectInfo.setDbPassword("REDCUBE1234");
			} else if(dbmode.equals("nowcube")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("NOWCUBE");
				connectInfo.setDbPassword("NOWCUBE1234");
			}else if(dbmode.equals("favcube")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.24:1521:CBUTF8");
				connectInfo.setDbAccount("favcube");
				connectInfo.setDbPassword("!237101fav@");
			}
			*/
			//wms 개발
			else if(dbmode.equals("wmsapi")){
				connectInfo.setDbConnect("jdbc:oracle:thin:@220.117.243.55:1521:WMS");
				connectInfo.setDbAccount("WMS_USER");
				connectInfo.setDbPassword("WMS_USER");
			}
			
			con = java.sql.DriverManager.getConnection(connectInfo.getDbConnect(), connectInfo.getDbAccount(), connectInfo.getDbPassword());
			Logger.debug("DB Connect : "+dbmode+" Driver");
			connectInfo = null;
		} catch (Exception e) {	
			Logger.error(e);
		}
		return con;
	}
	
	
	/**
	 * Stored Procedure를 실행하기위해 prepareCall합니다.
	 *
	 * @param procedure
	 * @return boolean
	 */
	public static void prepareCall(Connection conn, String procedure) throws SQLException
	{
		@SuppressWarnings("unused")
		CallableStatement stmt = null;
		
		try
		{
			//Logger.info("call procedure:"+procedure);
			stmt = conn.prepareCall(procedure);
			stmt.execute();
		}
		catch (SQLException ex)
		{
			Logger.error(ex);
			throw ex;
		}
	}
 
	
	public static void close(Connection conn, String dbmode) {
		try {
			Logger.debug("DB Connect out : "+dbmode+" Driver");
			conn.setAutoCommit(true);
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}
