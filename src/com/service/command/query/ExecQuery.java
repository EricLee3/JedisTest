package com.service.command.query;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;


import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;

import com.service.command.connection.DataBaseManager;

import com.service.command.log.Logger;

/**
 * QueryRunner를 이용한 Execute-SQL Class
 * 페이징을 위한 List를 위해 queryForList 메소드 추
 * @author sylyu
 *
 */
public class ExecQuery {
	/**
	 * params 는 String[][]로 가져가면 된다.
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int[] batch(String sql, String[][] params ) {
		int[] result = null;
		QueryRunner qr = null;
		try{
			qr = new QueryRunner();
			result = qr.batch(sql, params);
		} catch (Exception e) {
			Logger.error(e);
		}
		return result;
	}
	
	/**
	 * params 는 String[][]로 가져가면 된다.
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int[] batch(Connection conn, String sql, String[][] params ) {
		int[] result = null;
		QueryRunner qr = null;
		try{
			qr = new QueryRunner();
			result = qr.batch(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
		}
		return result;
	}
	
	/**
	 * executeQuery for INSERT & UPDATE 
	 * @param sql
	 * @return
	 */
	public static int update(String sql) {
		debugQuery(sql, null);
		
		int result = 0;		
		QueryRunner qr = null;
		try {
			qr = new QueryRunner();
			result = qr.update(sql);
		} catch (Exception e) {
			Logger.error(e);
		}
		return result;
	}
	
	/**
	 * executeQuery for INSERT & UPDATE
	 * @param conn
	 * @param sql
	 * @return
	 */
	public static int update(Connection conn, String sql) {
		debugQuery(sql, null);
		
		int result = 0;		
		QueryRunner qr = null;
		try {
			qr = new QueryRunner();
			result = qr.update(conn, sql);
		} catch (Exception e) {
			Logger.error(e);
		}		
		return result;
	}
	
	/**
	 * executeQuery for INSERT & UPDATE
	 * @param sql
	 * @param param
	 * @return
	 */
	public static int update(String sql, ArrayList params) {
		debugQuery(sql, params);
		
		int result = 0;		
		QueryRunner qr = null;
		try {
			qr = new QueryRunner();
			result = qr.update(sql, params.toArray());
		} catch (Exception e) {
			Logger.error(e);
		}		
		return result;
	}
	
	/**
	 * executeQuery for INSERT & UPDATE
	 * @param conn
	 * @param sql
	 * @param param
	 * @return
	 */
	public static int update(Connection conn, String sql, ArrayList params) {
		debugQuery(sql, params);
		
		int result = 0;		
		QueryRunner qr = null;
		try {
			qr = new QueryRunner();
			result = qr.update(conn, sql, params.toArray());
		} catch (Exception e) {
			Logger.error(e);
		}		
		return result;
	}
	
	/**
	 * 일반적인 Query 실행
	 * @param sql
	 * @param rsh
	 * @return
	 */
	public static Object query(String sql, ResultSetHandler rsh){
		debugQuery(sql, null);
		
		Object obj = null;
		QueryRunner qr = null;
		try {
			qr = new QueryRunner();
			obj = qr.query(sql, rsh);	
		} catch (Exception e) {
			Logger.error(e);
		}		
		return obj;
	}
	
	/**
	 * 일반적인 Query 실행
	 * @param sql
	 * @param param
	 * @param rsh
	 * @return
	 */
	public static Object query(String sql, ArrayList params, ResultSetHandler rsh){
		debugQuery(sql, params);
		
		Object obj = null;
		QueryRunner qr = null;
		try {
			qr = new QueryRunner();
			obj = qr.query(sql, params.toArray(), rsh);	
		} catch (Exception e) {
			Logger.error(e);
		}		
		return obj;
	}
	
	/**
	 * 일반적인 Query 실행
	 * @param conn
	 * @param sql
	 * @param param
	 * @param rsh
	 * @return
	 */
	public static Object query(Connection conn, String sql, ArrayList params, ResultSetHandler rsh){
		debugQuery(sql, params);
		
		Object obj = null;
		QueryRunner qr = null;
		try {
			qr = new QueryRunner();
			if (params != null && params.size() >0 ) {
				obj = qr.query(conn, sql, params.toArray(), rsh);
			} else {
				obj = qr.query(conn, sql, rsh);
			}
		} catch (Exception e) {
			Logger.error(e);
		}		
		return obj;
	}
	
	
	
	/**
	 * SQL의 TotalCount 계산
	 * @param sql
	 * @param param
	 * @return
	 */
	public static int queryForCount(String dbmode, String sql, ArrayList params) {
		debugQuery(sql, params);
		Connection conn = null;
		int result = 0;
		try {
			conn = DataBaseManager.getConnection(dbmode);
			result = queryForCount(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			try {
				DbUtils.close(conn);
			} catch (Exception e) {}
		}
		
		return result;
		
	}
	
	/**
	 * SQL의 TotalCount 계산
	 * @param conn
	 * @param sql
	 * @param param
	 * @return
	 */
	public static int queryForCount(Connection conn, String sql, ArrayList params) {
		
		BigDecimal result = null;
		Map map = null;
		
		ResultSetHandler rsh = null;
		QueryRunner qr= null;
		try {			
			sql = 	" SELECT count(*) cnt FROM ( " + sql + ") AAA";
			
			debugQuery(sql, params);
			
			rsh = new MapHandler();
			qr = new QueryRunner();
			if (params != null && params.size() > 0) {
				map = (Map)qr.query(conn, sql, params.toArray(), rsh);
			} else {
				map = (Map)qr.query(conn, sql, rsh);
			}
			if (map != null && map.size() > 0 ) result = (BigDecimal)map.get("cnt");			
		} catch (Exception e) {
			Logger.error(e);
		}
		return result.intValue();
	}
	
	/**
	 * SQL의 최대값 계산
	 * @param conn
	 * @param sql
	 * @param param
	 * @return
	 */
	public static int queryForMaxCount(Connection conn, String sql, ArrayList params) {
		BigDecimal result = null;
		Map map = null;
		ResultSetHandler rsh = null;
		QueryRunner qr= null;
		try {			
			
			debugQuery(sql, params);
			
			rsh = new MapHandler();
			qr = new QueryRunner();
			if (params != null && params.size() > 0) {
				map = (Map)qr.query(conn, sql, params.toArray(), rsh);
			} else {
				map = (Map)qr.query(conn, sql, rsh);
			}
			if (map != null && map.size() > 0 ) result = (BigDecimal)map.get("cnt");			
		} catch (Exception e) {
			Logger.error(e);
		}
		return result.intValue();
	}
	
	
	
	public static int queryForSequence(String dbmode, String seq) {
		int result = 0;
		Connection conn = null;
		
		try {
			conn = DataBaseManager.getConnection(dbmode);
			result = queryForSequence(conn, seq);
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
		return result;
	}
	
	public static int queryForSequence(Connection conn, String seq) {
		BigDecimal result = null;
		
		Map map = null;
		String sql = "";
		ResultSetHandler rsh = null;
		QueryRunner qr= null;
		try {
			sql = "SELECT " + seq + " as seq FROM DUAL";
			
			debugQuery(sql, null);
			
			rsh = new MapHandler();
			qr = new QueryRunner();
			map = (Map)qr.query(conn, sql, rsh);
			if (map != null && map.size() > 0 ) result = (BigDecimal)map.get("seq");
		} catch (Exception e) {
			Logger.error(e);
		}
		
		return result.intValue();
	}
	
	/**
	 * 디버그
	 * @param strQuery
	 * @param arrList
	 */
	private static void debugQuery(String strQuery, ArrayList arrList) {
		
		StringBuffer msg = new StringBuffer();
		
		msg.append(" Query: ");
		msg.append(strQuery);
		msg.append(" Parameters: ");
		
		if (arrList == null) {
			msg.append("[]");
		} else {
			msg.append(arrList);
		}
		msg.append("\n");
		Logger.debug(msg.toString());
		
	}
	
	
	
	/**
	 * String을 LONG에 INSERT위한 메소드.
	 * @param conn
	 * @param sql
	 * @param clobName
	 * @param content
	 * @throws Exception
	 */
	public static void writeForLong(Connection conn, String sql, String content) throws Exception {
		PreparedStatement pstmt = null;
		try {
			Reader bis = new StringReader(content);
			int size = content.length();
			pstmt = conn.prepareStatement(sql);			
			pstmt.setCharacterStream(1, bis, size);			
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (pstmt != null) pstmt.close();
			} catch (SQLException se) {}
		}
	}
	
	/**
	 * Clob의 내용을 String으로 Return
	 * @param clob
	 * @return
	 * @throws Exception
	 */
	public static String stringToClob(Clob clob) throws Exception {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			if (clob != null) {
				br = new BufferedReader(clob.getCharacterStream());
		       char [] buf    = new char[1024];
		       int length = 0;	  
		       while ((length = br.read(buf, 0, 1024)) != -1 ) {
		    	   sb.append(buf, 0, length);
		       }
			}	       
	       
		} catch (Exception e) {
			throw e;
		} finally {
			try {if (br != null) br.close();} catch (Exception e) {}
		}
		return StringUtils.defaultString(sb.toString(),""); 
	}

}
