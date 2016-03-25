package com.service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;

import com.service.command.connection.DataBaseManager;
import com.service.command.log.Logger;
import com.service.command.util.CommonUtil;
import com.service.command.util.StringUtil;
import com.service.dao.ServiceDAO;
import com.service.entity.ServiceDataInfo;
import com.service.CubeService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;




public class CubeApiCreateJSON {
	
	private static CubeApiCreateJSON instance = new CubeApiCreateJSON();

	public static CubeApiCreateJSON getInstance() {
		return instance;
	}
   
	private CubeApiCreateJSON() {
		
	} 
	
	public List<Object> GetVendorList(String dbmode, String transCD) throws IOException {
		String methodName ="com.service.CubeApiCreateJSON.GetVendorList()";
		Logger.debug(methodName);
		
		Connection 			conn	= null;
		PreparedStatement	pstmt	= null;
		PreparedStatement	pstmt2	= null;
		ResultSet			rs		= null;
		ResultSet			rs2		= null;
		StringBuffer   	sqlBuffer  	= new StringBuffer(500);	//쿼리문
		StringBuffer   	sqlBuffer2  = new StringBuffer(500);	//카운트 쿼리문
		
		HashMap hm 			= new HashMap();
		List	vendorList 	= new ArrayList();
		
		int cnt 	= 0;
		
		try
		{ 
			conn = DataBaseManager.getConnection(dbmode);
			
			sqlBuffer.append("SELECT  TO_CHAR(REFCD) AS VENDOR_ID,                 	");  
			sqlBuffer.append("        TO_CHAR(SYSDATE-8,'YYYYMMDD') AS STA_DT,		");         
			sqlBuffer.append("        TO_CHAR(SYSDATE,'YYYYMMDD') AS END_DT			");
			sqlBuffer.append("FROM    TBB150										");
			sqlBuffer.append("WHERE   REFTP =  'ZY' 								");
			sqlBuffer.append("AND     REFCD <> '0000'								");
			sqlBuffer.append("AND     CD4   =  '"+ transCD +"' 						");
			pstmt = conn.prepareStatement(sqlBuffer.toString());
			rs = pstmt.executeQuery();
			
			sqlBuffer2.append("SELECT  COUNT(1) AS CNT								");
			sqlBuffer2.append("FROM    TBB150										");
			sqlBuffer2.append("WHERE   REFTP =  'ZY' 								");
			sqlBuffer2.append("AND     REFCD <> '0000'								");
			sqlBuffer2.append("AND     CD4   =  '"+ transCD +"' 					");
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
			rs2 = pstmt2.executeQuery();
			
			
			if(rs2.next())
			{
				cnt = rs2.getInt("CNT");
			}
			
			if(cnt > 0)
			{
				while(rs.next())
				{
					hm = new HashMap();
					hm.put("VENDOR_ID", rs.getString("VENDOR_ID"));
					hm.put("STA_DT", rs.getString("STA_DT"));
					hm.put("END_DT", rs.getString("END_DT"));
					vendorList.add(hm);
				}
			}
			else {
				vendorList = null;
			}
			
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		} finally {
			try
			{
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
			}
			catch (Exception e)
			{
				Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			}
		}
		
		return vendorList;
	}
	
	
	public String api_Auto_Item(String dbmode, String command, String vendorID, String transCD, String sendDomain) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.api_Auto_Item()";
		Logger.debug(methodName);
		
		Connection 			conn		= null;
		
		PreparedStatement	pstmt		= null;
		PreparedStatement	pstmt2		= null;
		PreparedStatement	pstmt3		= null;
		PreparedStatement	pstmt4		= null;
		PreparedStatement	pstmt5		= null;
		
		ResultSet			rs			= null;
		ResultSet			rs2			= null;
		ResultSet			rs3			= null;
		
		StringBuffer   		sqlBuffer  	= new StringBuffer(1000);	//주 쿼리문
		StringBuffer   		sqlBuffer2  = new StringBuffer(500);	//서브 쿼리문
		StringBuffer   		sqlBuffer3  = new StringBuffer(500);	//카운트 쿼리문
		StringBuffer   		sqlBuffer4  = new StringBuffer(500);	//UPDATE 쿼리문
		StringBuffer   		sqlBuffer5  = new StringBuffer(500);	//UPDATE 쿼리문
		
		int cnt 	= 0;
		String 	sendMessage 	= null;
		
		BufferedReader br 		= null;
		String url = sendDomain +"/malladmin/join/pubApiManager.jsp";	//SEND URL
		PostMethod post = new PostMethod(url);
		
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			
			
			sqlBuffer.append("SELECT  MAX(A.TRAN_DATE)      AS TRAN_DATE						\n");
			sqlBuffer.append("        , MAX(A.TRAN_SEQ)     AS TRAN_SEQ                     	\n");
			sqlBuffer.append("        , MAX(A.SEQ)          AS SEQ                          	\n");
			sqlBuffer.append("        , MAX(A.STORAGE_ID)   AS STORAGE_ID                   	\n");
			sqlBuffer.append("        , A.PRODINC                                           	\n");
			sqlBuffer.append("        , MAX(A.PNAME)        AS PNAME                        	\n");
			sqlBuffer.append("        , MAX(A.WEIGHT)       AS WEIGHT                       	\n");
			sqlBuffer.append("        , MAX(A.WEIGHT_UNIT)  AS WEIGHT_UNIT                  	\n");
			sqlBuffer.append("        , MAX(A.ASSORT_GB)    AS ASSORT_GB                    	\n");
			sqlBuffer.append("        , MAX(A.NONSALE_YN)   AS NONSALE_YN                   	\n");
			sqlBuffer.append("        , MAX(A.SHORTAGE_YN)  AS SHORTAGE_YN                  	\n");
			sqlBuffer.append("        , MAX(A.RES_UNIT)     AS RES_UNIT                     	\n");
			sqlBuffer.append("        , MAX(A.TAX_GB)       AS TAX_GB                       	\n");
			sqlBuffer.append("        , MAX(A.PURL)         AS PURL                         	\n");
			sqlBuffer.append("        , MAX(A.STORY)        AS STORY                        	\n");
			sqlBuffer.append("        , MAX(A.BRAND_ID)     AS BRAND_ID                     	\n");
			sqlBuffer.append("        , MAX(A.CATEGORY_ID)  AS CATEGORY_ID                  	\n");
			sqlBuffer.append("        , MAX(A.NATION)       AS NATION                       	\n");
			sqlBuffer.append("        , MAX(A.LOCAL_PRICE)  AS LOCAL_PRICE                  	\n");
			sqlBuffer.append("        , MAX(A.LOCAL_SALE)   AS LOCAL_SALE            			\n");
			sqlBuffer.append("        , MAX(A.DELI_PRICE)   AS DELI_PRICE                   	\n");
			sqlBuffer.append("        , MAX(A.ESTI_PRICE)   AS ESTI_PRICE                   	\n");
			sqlBuffer.append("        , MAX(A.MARGIN_GB)    AS MARGIN_GB                    	\n");
			sqlBuffer.append("        , MAX(A.SALE_PRICE)   AS SALE_PRICE                   	\n");
			sqlBuffer.append("        , MAX(A.USER_ID)      AS USER_ID                      	\n");
			sqlBuffer.append("        , A.VENDOR_ID                                         	\n");
			sqlBuffer.append("        , MAX(A.CARD_FEE)     AS CARD_FEE                     	\n");
			sqlBuffer.append("        , MAX(C.COCD)         AS SUPPLY_ID                       	\n");
			sqlBuffer.append("FROM    TBP050_TRANSFER A,                                    	\n");
			sqlBuffer.append("        (                                                     	\n");
			sqlBuffer.append("            SELECT  BAR_CODE                                  	\n");
			sqlBuffer.append("                    , VENDOR_ID                               	\n");
			sqlBuffer.append("                    , MAX(TRAN_DATE) AS TRAN_DATE             	\n");
			sqlBuffer.append("                    , MAX(TRAN_SEQ)  AS TRAN_SEQ              	\n");
			sqlBuffer.append("            FROM    TBP050_TRANSFER                           	\n");
			sqlBuffer.append("            WHERE   STATUS IN ('00', '99')                    	\n");
			sqlBuffer.append("            GROUP BY BAR_CODE, VENDOR_ID                      	\n");
			sqlBuffer.append("        )   B,                                                	\n");
			sqlBuffer.append("        (                                                     	\n");
			sqlBuffer.append("            SELECT  REFCD AS VENDOR_ID                        	\n");
			sqlBuffer.append("                    , CD1   AS SHOP_ID                        	\n");
			sqlBuffer.append("                    , RETC  AS COCD                           	\n");
			sqlBuffer.append("            FROM    TBB150                                    	\n");
			sqlBuffer.append("            WHERE   REFTP = 'ZY'                              	\n");
			sqlBuffer.append("            AND     REFCD <> '0000'                           	\n");
			sqlBuffer.append("            AND     CD4   = '"+ transCD +"'                   	\n");
			sqlBuffer.append("        ) C                                                   	\n");
			sqlBuffer.append("WHERE  A.TRAN_DATE = B.TRAN_DATE                              	\n");
			sqlBuffer.append("AND    A.TRAN_SEQ  = B.TRAN_SEQ                               	\n");
			sqlBuffer.append("AND    A.BAR_CODE  = B.BAR_CODE                               	\n");
			sqlBuffer.append("AND    A.VENDOR_ID = C.VENDOR_ID                              	\n");
			sqlBuffer.append("AND    A.SHOP_ID   = C.SHOP_ID                                	\n");
			sqlBuffer.append("GROUP BY A.VENDOR_ID, A.PRODINC                               	\n");
			sqlBuffer.append("ORDER BY A.VENDOR_ID, A.PRODINC                               	\n");

			pstmt = conn.prepareStatement(sqlBuffer.toString());
			
			
			sqlBuffer2.append("SELECT  ITEM_COLOR                                    			\n");
			sqlBuffer2.append("        ,ITEM_SIZE                                               \n");
			sqlBuffer2.append("        ,BAR_CODE                                                \n");
			sqlBuffer2.append("        ,ORDER_LMT_YN                                            \n");
			sqlBuffer2.append("        ,ORDER_LMT_CNT                                           \n");
			sqlBuffer2.append("FROM    TBP050_TRANSFER                                 			\n");
			sqlBuffer2.append("WHERE   TRAN_DATE = ?                                   			\n");
			sqlBuffer2.append("AND     TRAN_SEQ  = ?                                          	\n");
			sqlBuffer2.append("AND     PRODINC	 = ?                                    		\n");
			sqlBuffer2.append("ORDER BY TRAN_DATE, TRAN_SEQ, SEQ                                \n");
			
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
			
			
			sqlBuffer3.append("SELECT  COUNT(1) AS CNT											\n");
			sqlBuffer3.append("FROM    TBP050_TRANSFER A,                                    	\n");
			sqlBuffer3.append("        (                                                     	\n");
			sqlBuffer3.append("            SELECT  BAR_CODE                                  	\n");
			sqlBuffer3.append("                    , VENDOR_ID                               	\n");
			sqlBuffer3.append("                    , MAX(TRAN_DATE) AS TRAN_DATE             	\n");
			sqlBuffer3.append("                    , MAX(TRAN_SEQ)  AS TRAN_SEQ              	\n");
			sqlBuffer3.append("            FROM    TBP050_TRANSFER                           	\n");
			sqlBuffer3.append("            WHERE   STATUS IN ('00', '99')                    	\n");
			sqlBuffer3.append("            GROUP BY BAR_CODE, VENDOR_ID                      	\n");
			sqlBuffer3.append("        )   B,                                                	\n");
			sqlBuffer3.append("        (                                                     	\n");
			sqlBuffer3.append("            SELECT  REFCD AS VENDOR_ID                        	\n");
			sqlBuffer3.append("                    , CD1   AS SHOP_ID                        	\n");
			sqlBuffer3.append("                    , RETC  AS COCD                           	\n");
			sqlBuffer3.append("            FROM    TBB150                                    	\n");
			sqlBuffer3.append("            WHERE   REFTP = 'ZY'                              	\n");
			sqlBuffer3.append("            AND     REFCD <> '0000'                           	\n");
			sqlBuffer3.append("            AND     CD4   = '"+ transCD +"'                      \n");
			sqlBuffer3.append("        ) C                                                   	\n");
			sqlBuffer3.append("WHERE  A.TRAN_DATE = B.TRAN_DATE                              	\n");
			sqlBuffer3.append("AND    A.TRAN_SEQ  = B.TRAN_SEQ                               	\n");
			sqlBuffer3.append("AND    A.BAR_CODE  = B.BAR_CODE                               	\n");
			sqlBuffer3.append("AND    A.VENDOR_ID = C.VENDOR_ID                              	\n");
			sqlBuffer3.append("AND    A.SHOP_ID   = C.SHOP_ID                                	\n");
			
			pstmt3 = conn.prepareStatement(sqlBuffer3.toString());
			
			
			sqlBuffer4.append("UPDATE  TBP050_TRANSFER                                          			                                                               	\n");
			sqlBuffer4.append("    SET ASSORT_ID   = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,ITEM_ID    = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,COLOR_SEQ  = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,SIZE_SEQ   = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,STATUS     = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,STATUS_MSG = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,UPD_ID 	= 'MangoAPI'                              				                                                                    \n");
			sqlBuffer4.append("        ,UPD_DT 	= TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS')       			                                                                    \n");
			sqlBuffer4.append("WHERE   (TRAN_DATE, TRAN_SEQ, SEQ, PRODINC, BAR_CODE, VENDOR_ID)            			                                                        \n");
			sqlBuffer4.append("    IN  (                                                        			                                                                \n");
			sqlBuffer4.append("            SELECT  M1.TRAN_DATE, M1.TRAN_SEQ, M1.SEQ, M1.PRODINC, M1.BAR_CODE, M1.VENDOR_ID	                                                \n");
			sqlBuffer4.append("            FROM    TBP050_TRANSFER M1                            			                                                                \n");
			sqlBuffer4.append("                    ,(                                                                                                                       \n");
			sqlBuffer4.append("                        SELECT  A.BAR_CODE                                                                                                   \n");
			sqlBuffer4.append("                                ,A.TRAN_DATE                                                                                                 \n");
			sqlBuffer4.append("                                ,(SELECT MAX(TRAN_SEQ) FROM TBP050_TRANSFER WHERE TRAN_DATE=A.TRAN_DATE AND BAR_CODE=A.BAR_CODE AND VENDOR_ID=A.VENDOR_ID) AS TRAN_SEQ	\n");
			sqlBuffer4.append("                        FROM    (                                                                                                            \n");
			sqlBuffer4.append("                                    SELECT  BAR_CODE                                                                                         \n");
			sqlBuffer4.append("                                      	   ,VENDOR_ID                                                                                       \n");
			sqlBuffer4.append("                                            ,MAX(TRAN_DATE) AS TRAN_DATE                                                                     \n");
			sqlBuffer4.append("                                    FROM    TBP050_TRANSFER                                                                                  \n");
			sqlBuffer4.append("                                    WHERE   STATUS IN ('00', '99')                                                                           \n");
			sqlBuffer4.append("                                    GROUP BY BAR_CODE, VENDOR_ID                                                                             \n");
			sqlBuffer4.append("                                )   A                                                                                                        \n");
			sqlBuffer4.append("                    )   M2                                        			                                                                \n");
			sqlBuffer4.append("            WHERE   M1.TRAN_DATE = M2.TRAN_DATE                    			                                                                \n");
			sqlBuffer4.append("            AND     M1.TRAN_SEQ  = M2.TRAN_SEQ                     			                                                                \n");
			sqlBuffer4.append("            AND     M1.BAR_CODE  = M2.BAR_CODE                     			                                                                \n");
			sqlBuffer4.append("            AND     M1.PRODINC 	= ?                             			                                                                \n");
			sqlBuffer4.append("            AND     M1.BAR_CODE  = ?                             			                                                                \n");
			sqlBuffer4.append("            AND     M1.VENDOR_ID = ?                             			                                                                \n");
			sqlBuffer4.append("        )                                                                                                                                    \n");

			pstmt4 = conn.prepareStatement(sqlBuffer4.toString());
			
			
			sqlBuffer5.append("UPDATE  TBP050_TRANSFER                                                                  	                                                                \n");
			sqlBuffer5.append("    SET STATUS  = ?                                                                                                                                          \n");
			sqlBuffer5.append("        ,UPD_ID = 'MangoAPI'                                                                	                                                                \n");
			sqlBuffer5.append("        ,UPD_DT = TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS')                                       	                                                                \n");
			sqlBuffer5.append("WHERE   (TRAN_DATE, TRAN_SEQ, SEQ, PRODINC, BAR_CODE, VENDOR_ID)                                                                                             \n");
			sqlBuffer5.append("    IN  (                                                                                                                                                    \n");
			sqlBuffer5.append("            SELECT                                                                                                                                           \n");
			sqlBuffer5.append("                    M1.TRAN_DATE, M1.TRAN_SEQ, M1.SEQ, M1.PRODINC, M1.BAR_CODE, M1.VENDOR_ID                                                                 \n");
			sqlBuffer5.append("            FROM    (                                                                                                                                        \n");
			sqlBuffer5.append("                        SELECT                                                                                                                               \n");
			sqlBuffer5.append("                                TRAN_DATE||LPAD(TRAN_SEQ,4,'0') AS WORK_DT               	                                                                \n");
			sqlBuffer5.append("                                ,TRAN_DATE                                                 	                                                                \n");
			sqlBuffer5.append("                                ,TRAN_SEQ                                                  	                                                                \n");
			sqlBuffer5.append("                                ,SEQ                                                       	                                                                \n");
			sqlBuffer5.append("                                ,PRODINC                                                   	                                                                \n");
			sqlBuffer5.append("                                ,BAR_CODE                                                  	                                                                \n");
			sqlBuffer5.append("                                ,STATUS                                                    	                                                                \n");
			sqlBuffer5.append("                                ,STATUS_MSG                                                	                                                                \n");
			sqlBuffer5.append("                                ,VENDOR_ID                                                	                                                                \n");
			sqlBuffer5.append("                        FROM    TBP050_TRANSFER                            			    	                                                                \n");
			sqlBuffer5.append("                        WHERE   STATUS IN ('00', '99')                           			                                                                \n");
			sqlBuffer5.append("                        AND     PRODINC 	 = ?                           						                                                                \n");
			sqlBuffer5.append("                        AND     BAR_CODE  = ?                                					                                                            \n");
			sqlBuffer5.append("                        AND     VENDOR_ID = ?                                					                                                            \n");
			sqlBuffer5.append("                    )   M1,                                                                                                                                  \n");
			sqlBuffer5.append("                    (                                                                                                                                        \n");
			sqlBuffer5.append("                        SELECT                                                                                                                               \n");
			sqlBuffer5.append("                                MAX(A.TRAN_DATE||LPAD(A.TRAN_SEQ,4,'0')) AS WORK_DT                                                                          \n");
			sqlBuffer5.append("                                ,A.PRODINC                                                                                                                   \n");
			sqlBuffer5.append("                                ,A.BAR_CODE	                                                                                                                \n");
			sqlBuffer5.append("                                ,A.VENDOR_ID	                                                                                                                \n");
			sqlBuffer5.append("                        FROM    TBP050_TRANSFER	A                            			                                                                    \n");
			sqlBuffer5.append("                                ,(                                                                                                                           \n");
			sqlBuffer5.append("                                    SELECT  C.BAR_CODE                                                                                                       \n");
			sqlBuffer5.append("                                            ,C.TRAN_DATE                                                                                                     \n");
			sqlBuffer5.append("                                            ,C.VENDOR_ID                                                                                                     \n");
			sqlBuffer5.append("                                            ,(SELECT MAX(TRAN_SEQ) FROM TBP050_TRANSFER WHERE TRAN_DATE=C.TRAN_DATE AND BAR_CODE=C.BAR_CODE AND VENDOR_ID=C.VENDOR_ID) AS TRAN_SEQ		\n");
			sqlBuffer5.append("                                    FROM    (                                                                                                                \n");
			sqlBuffer5.append("                                                SELECT  BAR_CODE                                                                                             \n");
			sqlBuffer5.append("                                                        ,VENDOR_ID                                                                         					\n");
			sqlBuffer5.append("                                                        ,MAX(TRAN_DATE) AS TRAN_DATE                                                                         \n");
			sqlBuffer5.append("                                                FROM    TBP050_TRANSFER                                                                                      \n");
			sqlBuffer5.append("                                                WHERE   STATUS = '01'                                                                                        \n");
			sqlBuffer5.append("                                                GROUP BY BAR_CODE, VENDOR_ID                                                                                 \n");
			sqlBuffer5.append("                                            )   C                                                                                                            \n");
			sqlBuffer5.append("                                )   B                                        			                                                                    \n");
			sqlBuffer5.append("                        WHERE   A.TRAN_DATE = B.TRAN_DATE                    			                                                                    \n");
			sqlBuffer5.append("                        AND     A.TRAN_SEQ  = B.TRAN_SEQ                     			                                                                    \n");
			sqlBuffer5.append("                        AND     A.BAR_CODE  = B.BAR_CODE                     			                                                                    \n");
			sqlBuffer5.append("                        AND     A.PRODINC   = ?                            					                                                                \n");
			sqlBuffer5.append("                        AND     A.BAR_CODE  = ?                                				                                                                \n");
			sqlBuffer5.append("                        AND     A.VENDOR_ID = ?                                				                                                                \n");
			sqlBuffer5.append("                        GROUP BY A.PRODINC, A.BAR_CODE, A.VENDOR_ID                                                                                          \n");
			sqlBuffer5.append("                    )   M2                                                                                                                                   \n");
			sqlBuffer5.append("            WHERE   M1.PRODINC   = M2.PRODINC                                                                                                                \n");
			sqlBuffer5.append("            AND     M1.BAR_CODE  = M2.BAR_CODE                                                                                                               \n");
			sqlBuffer5.append("            AND     M1.VENDOR_ID = M2.VENDOR_ID                                                                                                              \n");
			sqlBuffer5.append("            AND     M1.WORK_DT   < M2.WORK_DT                                                                                                                \n");
			sqlBuffer5.append("        )                                                                                                                                                    \n");
			
			pstmt5 = conn.prepareStatement(sqlBuffer5.toString());
			
			
			rs3 = pstmt3.executeQuery();
			
			if(rs3.next())
			{
				cnt = rs3.getInt("cnt");
			}
			
			
			//전송 DATA 있을때..
			if(cnt > 0)
			{
				
				rs = pstmt.executeQuery();
				
				
				JSONObject jsonObject = new JSONObject();
				JSONArray cell = new JSONArray();
				
				while(rs.next())
				{
					JSONObject asrrotList = new JSONObject();
					
					asrrotList.put("storage_id", rs.getString("STORAGE_ID"));
					asrrotList.put("prodinc", rs.getString("PRODINC"));
					asrrotList.put("pname", rs.getString("PNAME"));
					asrrotList.put("weight", rs.getString("WEIGHT"));
					asrrotList.put("weight_unit", rs.getString("WEIGHT_UNIT"));
					asrrotList.put("assort_gb", rs.getString("ASSORT_GB"));
					asrrotList.put("nonsale_yn", rs.getString("NONSALE_YN"));
					asrrotList.put("shortage_yn", rs.getString("SHORTAGE_YN"));
					asrrotList.put("res_unit", rs.getString("RES_UNIT"));
					asrrotList.put("tax_gb", rs.getString("TAX_GB"));
					
					if (rs.getString("PURL") == null || rs.getString("PURL").equals("")) {
						asrrotList.put("purl", ".");
					} else {
						asrrotList.put("purl", rs.getString("PURL"));
					}
					
					if (rs.getString("STORY") == null || rs.getString("STORY").equals("")) {
						asrrotList.put("story", ".");
					} else {
						asrrotList.put("story", rs.getString("STORY"));
					}
					
					asrrotList.put("brand_id", rs.getString("BRAND_ID"));
					asrrotList.put("category_id", rs.getString("CATEGORY_ID"));
					asrrotList.put("nation", rs.getString("NATION"));
					asrrotList.put("local_price", rs.getString("LOCAL_PRICE"));
					asrrotList.put("local_sale", rs.getString("LOCAL_SALE"));
					asrrotList.put("deli_price", rs.getString("DELI_PRICE"));
					asrrotList.put("esti_price", rs.getString("ESTI_PRICE"));
					asrrotList.put("margin_gb", rs.getString("MARGIN_GB"));
					asrrotList.put("sale_price", rs.getString("SALE_PRICE"));
					asrrotList.put("user_id", rs.getString("USER_ID"));
					asrrotList.put("vendor_id", rs.getString("VENDOR_ID"));
					asrrotList.put("card_fee", rs.getString("CARD_FEE"));
					
					
					//옵션 정보 가져오기..
					pstmt2.setString(1, rs.getString("TRAN_DATE"));
					pstmt2.setString(2, rs.getString("TRAN_SEQ"));
					pstmt2.setString(3, rs.getString("PRODINC"));
					
					rs2 = pstmt2.executeQuery();
					
					JSONArray cellOpt = new JSONArray();
					while (rs2.next()) 
					{
						JSONObject itemOption = new JSONObject();
						
						itemOption.put("item_color", rs2.getString("ITEM_COLOR"));
						itemOption.put("item_size", rs2.getString("ITEM_SIZE"));
						itemOption.put("bar_code", rs2.getString("BAR_CODE"));
						itemOption.put("order_lmt_yn", rs2.getString("ORDER_LMT_YN"));
						itemOption.put("order_lmt_cnt", rs2.getString("ORDER_LMT_CNT"));
						
						cellOpt.add(itemOption);
						asrrotList.put("optioninfo", cellOpt);
					}
					//옵션 정보 가져오기..
					
					cell.add(asrrotList);

				}
				
				jsonObject.put("list", cell);
				//Logger.debug("jsonStr="+jsonObject.toString());
				   
				
				int conTimeOUt 	= 120000;
				int soTimeOut 	= 120000;
				int idleTimeout = 120000;
				
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				HttpClient httpClient = new HttpClient(connectionManager);
				httpClient.getParams().setParameter("http.protocol.expect-continue", false);
				httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
				httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
				httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
				httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
				connectionManager.closeIdleConnections(idleTimeout);
				connectionManager.getParams().setMaxTotalConnections(100);
				
				
				//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
				post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.addParameter("joinID", vendorID);
				post.addParameter("command", command);
				post.addParameter("data", jsonObject.toString());
				
				Logger.debug("url["+url+"]");
				int resultCode = httpClient.executeMethod(post);
				Logger.debug("resultCode["+resultCode+"]");
				
				if (String.valueOf(resultCode).equals("200")) { 
					
					br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
					String retString = "";
					String line;
					
					while ((line = br.readLine()) != null) {
						retString += line;
					}
					br.close();
					
					String jsonString = URLDecoder.decode(retString,"UTF-8");
					
					JSONObject jobj = JSONObject.fromObject(jsonString);
					String errorcd = (String) jobj.get("errorcd");
					String errormsg = (String) jobj.get("errormsg");
					
					if (errorcd.equals("01")) {
						String succStr = "SUCCESS!!";
						
						JSONArray jarray = jobj.getJSONArray("list");
						
						String rtProdinc	= "";
						String rtBarcode  	= "";
						String rtAssortid  	= "";
						String rtItemid 	= "";
						String rtColorseq 	= "";
						String rtSizeseq 	= "";
						String rtStatuscd 	= "";
						String rtVendorID	= "";
						String rtMessage 	= "";
						
						for (int i = 0; i < jarray.size(); i++)
						{
							JSONObject rtList = jarray.getJSONObject(i);
							
							rtProdinc	= rtList.getString("prodinc");
							rtBarcode	= rtList.getString("barcode");
							rtAssortid 	= rtList.getString("assortid");
							rtItemid 	= rtList.getString("itemid");
							rtColorseq 	= rtList.getString("colorseq");
							rtSizeseq 	= rtList.getString("sizeseq");
							rtVendorID	= rtList.getString("vendor_id");
							rtStatuscd 	= rtList.getString("statuscd");
							rtMessage 	= rtList.getString("message");
							
							//전송대상 BARCODE 전송 결과 업데이트..
							if (pstmt4 != null) { pstmt4.close(); pstmt4 = null; }
							pstmt4 = conn.prepareStatement(sqlBuffer4.toString());
							
							pstmt4.setString(1, rtAssortid);
							pstmt4.setString(2, rtItemid);
							pstmt4.setString(3, rtColorseq);
							pstmt4.setString(4, rtSizeseq);
							pstmt4.setString(5, rtStatuscd);
							pstmt4.setString(6, rtMessage);
							pstmt4.setString(7, rtProdinc);
							pstmt4.setString(8, rtBarcode);
							pstmt4.setString(9, rtVendorID);
							
							pstmt4.executeUpdate();
							
							if (rtStatuscd.equals("01")) {
								//전송대상 BARCODE 전송 결과가 "01" 성공이면 이전 데이타도 성공처리 업데이트..
								if (pstmt5 != null) { pstmt5.close(); pstmt5 = null; }
								pstmt5 = conn.prepareStatement(sqlBuffer5.toString());
								
								pstmt5.setString(1, rtStatuscd);
								pstmt5.setString(2, rtProdinc);
								pstmt5.setString(3, rtBarcode);
								pstmt5.setString(4, rtVendorID);
								pstmt5.setString(5, rtProdinc);
								pstmt5.setString(6, rtBarcode);
								pstmt5.setString(7, rtVendorID);
								
								pstmt5.executeUpdate();
							}
							
							
							//Logger.debug("prodinc="+rtProdinc+",barcode="+rtBarcode+",assortid="+rtAssortid+",itemid="+rtItemid+",colorseq="+rtColorseq+",sizeseq="+rtSizeseq+",vendorid="+rtVendorID+",statuscd="+rtStatuscd+",message="+rtMessage);
						}
						
						sendMessage = succStr;
						
					} else {
						sendMessage = "ERROR : "+ errormsg;
					}
					
				} else {
					sendMessage = "ERROR : API Connection Fail!!";
				}
				
			}	
			else	//전송 DATA 없을때.. 
			{
				sendMessage = "NO DATA!! SUCCESS!!";
			}
			
			
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}
				if( rs3 !=null ) try{ rs3.close(); rs3 = null; }catch(Exception e){}finally{rs3 = null;}
				
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				if( pstmt3 != null ) try{ pstmt3.close(); pstmt3 = null; }catch(Exception e){}finally{pstmt3 = null;}
				if( pstmt4 != null ) try{ pstmt4.close(); pstmt4 = null; }catch(Exception e){}finally{pstmt4 = null;}
				if( pstmt5 != null ) try{ pstmt5.close(); pstmt5 = null; }catch(Exception e){}finally{pstmt5 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
	}
	
	
	public String api_Auto_PO(String dbmode, String command, String vendorID, String transCD, String sendDomain) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.api_Auto_PO()";
		Logger.debug(methodName);
		
		Connection 			conn	= null;
		
		List<Object> vendorList 	= null;
		HashMap		 getHm	= new HashMap();
		
		String vendor_id	= "";
		String call_seq 	= "";
		
		String sendMessage 	= null;
		String succStr 		= "";
		
		BufferedReader br 	= null;
		String url = sendDomain +"/malladmin/join/pubApiManager.jsp";	//SEND URL
		PostMethod post = new PostMethod(url);
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO 	dao = new ServiceDAO();
			
			vendorList = GetVendorList(dbmode,transCD);
			
			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
			Date nowDay = new Date();
			String toDay = sdFormat.format(nowDay);
			
			
			if (vendorList != null) {
				for (int i = 0; i < vendorList.size(); i++) {
					getHm = (HashMap)vendorList.get(i);
					vendor_id = (String)getHm.get("VENDOR_ID");
					
					int conTimeOUt 	= 120000;
					int soTimeOut 	= 120000;
					int idleTimeout = 120000;
					
					MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
					HttpClient httpClient = new HttpClient(connectionManager);
					httpClient.getParams().setParameter("http.protocol.expect-continue", false);
					httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
					httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
					httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
					httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);

					connectionManager.closeIdleConnections(idleTimeout);
					connectionManager.getParams().setMaxTotalConnections(100);
					
					//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
					post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
					post.addParameter("joinID", vendor_id);
					post.addParameter("command", command);
					
					int resultCode = httpClient.executeMethod(post);
					
					
					if (String.valueOf(resultCode).equals("200")) {
						
						br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
						String retString = "";
						String line;
						
						while ((line = br.readLine()) != null) {
							retString += line;
						}
						br.close();
						
						String jsonString = URLDecoder.decode(retString,"UTF-8");
						
						JSONObject jobj = JSONObject.fromObject(jsonString);
						String errorcd = (String) jobj.get("errorcd");
						String errormsg = (String) jobj.get("errormsg");
						String tranDt = (String) jobj.get("tranDt");
						String tranSeq = (String) jobj.get("tranSeq");
						
						if (errorcd.equals("01")) {
							
							String rtFirstOrderId	= "";
							String rtOrderId		= "";
							String rtOrderSeq		= "";
							String rtOrderDt		= "";
							String rtRelationNo		= "";
							String rtRelationSeq	= "";
							String rtRelationDt		= "";
							String rtChangeGb		= "";
							String rtReceiptNm		= "";
							String rtReceiptTel		= "";
							String rtReceiptHp		= "";
							String rtReceiptZipcode	= "";
							String rtReceiptAddr1	= "";
							String rtReceiptAddr2	= "";
							String rtCustNm			= "";
							String rtCustEmail		= "";
							String rtCustTel		= "";
							String rtCustHp			= "";
							String rtCustZipcode	= "";
							String rtCustAddr1		= "";
							String rtCustAddr2		= "";
							String rtDeliveryMsg	= "";
							String rtItemCd			= "";
							String rtItemNm			= "";
							String rtOption1		= "";
							String rtOption2		= "";
							String rtSalePrice		= "";
							String rtDeliPrice		= "";
							String rtQty			= "";
							String rtShipID			= "";
							String rtCancelDt		= "";
							String rtShipStatus 	= "";
							String rtShipSeq 		= "";
							String rtDeliGb 		= "";
							String rtRetCode 		= "";
							String rtOriShipId 		= "";
							String rtClameMemo		= "";
							String rtCubeItem		= "";
							
							
							SimpleDateFormat tdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
							String rtTransDt = tdFormat.format(nowDay);
							
							call_seq = cubeDao.setRecvLog(dbmode, "MangoAPI", command, cubeDao.getApiName(command), vendor_id, toDay, toDay, "000", errormsg, transCD);
							
							ServiceDataInfo dInfo  = new ServiceDataInfo();
							
							JSONArray jarray = jobj.getJSONArray("list");
							
							for (int j = 0; j < jarray.size(); j++)
							{
								JSONObject rtList = jarray.getJSONObject(j);
								
								rtFirstOrderId		= rtList.getString("relationNo");
								rtOrderId			= rtList.getString("orderId");
								rtOrderSeq			= rtList.getString("orderSeq");
								rtOrderDt			= rtList.getString("orderDt");
								rtRelationNo		= rtList.getString("relationNo");
								rtRelationSeq		= rtList.getString("relationSeq");
								
								if (command.equals("OrderRetrieve") || command.equals("OrderReturnRetrieve")) {						//주문,반품 정보..
									rtRelationDt	= rtList.getString("relationDt");
								} else if (command.equals("OrderCancelRetrieve") || command.equals("OrderReturnCancelRetrieve")) {	//주문취소,반품취소 정보..
									rtCancelDt		= rtList.getString("relationDt");
								}
								
								if (command.equals("OrderReturnRetrieve")) {	//반품 정보..
									rtOriShipId		= rtOrderId + rtOrderSeq;
									rtQty			= Integer.toString(-1 * Integer.parseInt(rtList.getString("qty")));	//반품 정보일 때 수량 마이너스(-) 로 넘기기..
								} else {
									rtQty			= rtList.getString("qty");
								}
								
								rtChangeGb			= rtList.getString("changeGb");
								rtReceiptNm			= rtList.getString("receiptNm");
								rtReceiptTel		= rtList.getString("receiptTel");
								rtReceiptHp			= rtList.getString("receiptHp");
								rtReceiptZipcode	= rtList.getString("receiptZipcode");
								rtReceiptAddr1		= rtList.getString("receiptAddr1");
								rtReceiptAddr2		= rtList.getString("receiptAddr2");
								rtCustNm			= rtList.getString("custNm");
								rtCustTel			= rtList.getString("custTel");
								rtCustHp			= rtList.getString("custHp");
								rtCustZipcode		= rtList.getString("custZipcode");
								rtCustAddr1			= rtList.getString("custAddr1");
								rtCustAddr2			= rtList.getString("custAddr2");
								rtDeliveryMsg		= rtList.getString("deliveryMsg");
								rtItemCd			= rtList.getString("itemCd");
								rtItemNm			= rtList.getString("itemNm");
								rtOption1			= rtList.getString("option1");
								rtOption2			= rtList.getString("option2");
								rtSalePrice			= rtList.getString("salePrice");
								rtDeliPrice			= rtList.getString("deliPrice");
								
								if (command.equals("OrderCancelRetrieve")) {	//주문취소 정보 일때..
									rtShipID		= rtList.getString("relationNo")+rtList.getString("relationSeq");
								} else {
									rtShipID		= rtList.getString("relationNo");
								}
								
								rtShipSeq 			= rtList.getString("relationSeq");
								
								
								dInfo.setCall_dt(CommonUtil.getCurrentDate());
								dInfo.setCall_seq(call_seq);
								dInfo.setVendor_id(vendor_id);
								dInfo.setInuser("MangoAPI");
								dInfo.setError_code("00");
								dInfo.setError_msg(errormsg);
								dInfo.setSeq(String.valueOf(j+1));
								dInfo.setRecv_gb(cubeDao.getRecvGb(command)); // 10.주문, 20.주문취소, 30.반품, 40.반품취소
								dInfo.setFirst_order_id(StringUtil.nullTo(rtFirstOrderId, "")); 
								dInfo.setOrder_id(StringUtil.nullTo(rtRelationNo, ""));
								dInfo.setShip_id(StringUtil.nullTo(rtShipID, ""));         
								dInfo.setTrans_dt(StringUtil.nullTo(rtTransDt, ""));   
								dInfo.setCancel_dt(StringUtil.nullTo(rtCancelDt, ""));
								dInfo.setInstruct_dt(StringUtil.nullTo(rtRelationDt, ""));
								dInfo.setChange_gb(StringUtil.nullTo(rtChangeGb, ""));
								dInfo.setShip_status(StringUtil.nullTo(rtShipStatus, ""));     
								dInfo.setReceipt_nm(StringUtil.nullTo(rtReceiptNm, ""));      
								dInfo.setReceipt_tel(StringUtil.nullTo(rtReceiptTel, ""));     
								dInfo.setReceipt_hp(StringUtil.nullTo(rtReceiptHp, ""));      
								dInfo.setReceipt_zipcode(StringUtil.nullTo(rtReceiptZipcode, "")); 
								dInfo.setReceipt_addr1(StringUtil.nullTo(rtReceiptAddr1, ""));   
								dInfo.setReceipt_addr2(StringUtil.nullTo(rtReceiptAddr2, ""));   
								dInfo.setCust_nm(StringUtil.nullTo(rtCustNm, ""));         
								dInfo.setCust_tel(StringUtil.nullTo(rtCustTel, ""));        
								dInfo.setCust_hp(StringUtil.nullTo(rtCustHp, ""));         
								dInfo.setCust_zipcode(StringUtil.nullTo(rtCustZipcode, ""));    
								dInfo.setCust_addr1(StringUtil.nullTo(rtCustAddr1, ""));      
								dInfo.setCust_addr2(StringUtil.nullTo(rtCustAddr2, ""));      
								dInfo.setDelivery_msg(StringUtil.nullTo(rtDeliveryMsg, ""));    
								dInfo.setOrder_seq(StringUtil.nullTo(rtRelationSeq, ""));       
								dInfo.setShip_seq(StringUtil.nullTo(rtShipSeq, ""));        
								dInfo.setItem_cd(StringUtil.nullTo(rtItemCd, ""));         
								dInfo.setItem_nm(StringUtil.nullTo(rtItemNm, ""));         
								dInfo.setOption1(StringUtil.nullTo(rtOption1, ""));         
								dInfo.setOption2(StringUtil.nullTo(rtOption2, ""));         
								dInfo.setDeli_gb(StringUtil.nullTo(rtDeliGb, ""));
								dInfo.setRet_code(StringUtil.nullTo(rtRetCode, ""));
								dInfo.setDeli_price(StringUtil.nullTo(rtDeliPrice, "0"));
								dInfo.setSale_price(StringUtil.nullTo(rtSalePrice, "0"));
								dInfo.setOri_ship_id(StringUtil.nullTo(rtOriShipId, ""));
								dInfo.setQty(StringUtil.nullTo(rtQty, "0"));
								dInfo.setCust_email(StringUtil.nullTo(rtCustEmail, ""));
								dInfo.setClame_memo(StringUtil.nullTo(rtClameMemo, ""));
								dInfo.setCube_item(StringUtil.nullTo(rtCubeItem, ""));
								dInfo.setCocd("");
								dInfo.setWhcd("");
								dInfo.setOrderKey("");
								dInfo.setOrderSeqKey("");
								dInfo.setShipKey("");								
								dInfo.setVendorNm("");	
								dao.setRecvData(conn, dInfo, transCD);
								
							}

							succStr = "SUCCESS!!";
							
							cubeDao.callProcedure(dbmode, command, CommonUtil.getCurrentDate(), call_seq, sendDomain, tranDt, tranSeq, transCD, "", "","");
							
						} else {
							succStr = "ERROR : "+ errormsg;
							
							cubeDao.setRecvLog(dbmode, "MangoAPI", command, cubeDao.getApiName(command), vendor_id, toDay, toDay, "100", succStr, transCD);
						}
						
					} else {
						succStr = "ERROR : API Connection Fail";
						
						cubeDao.setRecvLog(dbmode, "MangoAPI", command, cubeDao.getApiName(command), vendor_id, toDay, toDay, "100", succStr, transCD);
					}
					
					//parameter 삭제 초기화..
					post.removeParameter("joinID");
					post.removeParameter("command");
					
					post.releaseConnection();
				}
				
			} else {
				succStr = "NO DATA!! 조회 된 벤더ID 가 없습니다.";
			}
			
			sendMessage = succStr;
			
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
		
	}
	
	
	public String api_Auto_PO_Send(String dbmode, String command, String call_dt, String call_seq, String tranDt, String tranSeq, String transCD, String sendDomain) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.api_Auto_PO_Send()";
		Logger.debug(methodName);
		
		Connection 			conn		= null;
		
		PreparedStatement	pstmt		= null;
		ResultSet			rs			= null;
		StringBuffer   		sqlBuffer  	= new StringBuffer(1000);	//주 쿼리문
		
		String 	sendMessage 	= null;
		
		BufferedReader br 		= null;
		String url = sendDomain +"/malladmin/join/pubApiManager.jsp";	//SEND URL
		PostMethod post = new PostMethod(url);
		
		String commandNM = "";
		
		if (command.equals("OrderRetrieve")) {
			commandNM = "OrderProcGet";
		} else if (command.equals("OrderReturnRetrieve")) {
			commandNM = "OrderReturnProcGet";
		} else if (command.equals("OrderCancelRetrieve")) {
			commandNM = "OrderCancelProcGet";
		} else if (command.equals("OrderReturnCancelRetrieve")) {
			commandNM = "OrderReturnCancelProcGet";
		}
		
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
		
			sqlBuffer.append("SELECT  VENDOR_ID                   			\n");
			sqlBuffer.append("        ,ORDER_ID                             \n");
			sqlBuffer.append("        ,ORDER_SEQ							\n");
			sqlBuffer.append("        ,SHIP_ID								\n");
			sqlBuffer.append("        ,SHIP_SEQ								\n");
			sqlBuffer.append("        ,PODT									\n");			
			sqlBuffer.append("        ,PONO									\n");	
			sqlBuffer.append("        ,POSEQ								\n");
			sqlBuffer.append("        ,TEMPNO								\n");
			sqlBuffer.append("        ,ERROR_CODE							\n");
			sqlBuffer.append("        ,ERROR_MSG							\n");
			sqlBuffer.append("FROM    API_RECV_DATA							\n");
			sqlBuffer.append("WHERE   CALL_DT  = ?							\n");
			sqlBuffer.append("AND     CALL_SEQ = ?							\n");
			sqlBuffer.append("AND     RECV_GB  = ?							\n");
			sqlBuffer.append("AND     TRANS_STATUS = ?						\n");
			
			//ERROR_CODE IN ('01','02') W컨셉, CM
			
			pstmt = conn.prepareStatement(sqlBuffer.toString());
			
			//Logger.debug("call_dt="+call_dt);
			//Logger.debug("call_seq="+call_seq);
			//Logger.debug("getRecvGb="+cubeDao.getRecvGb(command));
			//Logger.debug("transCD="+transCD);
			//Logger.debug("tranDt="+tranDt);
			//Logger.debug("tranSeq="+tranSeq);
			
			pstmt.setString(1, call_dt);
			pstmt.setString(2, call_seq);
			pstmt.setString(3, cubeDao.getRecvGb(command));
			pstmt.setString(4, transCD);
			rs = pstmt.executeQuery();
			
			JSONObject jsonObject = new JSONObject();
			JSONArray cell = new JSONArray();
			
			jsonObject.put("tranDt", tranDt);
			jsonObject.put("tranSeq", tranSeq);
			
			while(rs.next())
			{
				JSONObject asrrotList = new JSONObject();
				
				asrrotList.put("vendorId", rs.getString("VENDOR_ID"));
				asrrotList.put("relationNo", rs.getString("ORDER_ID"));
				asrrotList.put("relationSeq", rs.getString("ORDER_SEQ"));
				asrrotList.put("shipID", StringUtil.nullTo(rs.getString("SHIP_ID"),""));
				asrrotList.put("shipSeq", StringUtil.nullTo(rs.getString("SHIP_SEQ"),""));
				asrrotList.put("poDt", StringUtil.nullTo(rs.getString("PODT"),""));
				asrrotList.put("poNo", StringUtil.nullTo(rs.getString("PONO"),""));
				asrrotList.put("poSeq", StringUtil.nullTo(rs.getString("POSEQ"),""));
				asrrotList.put("tempNo", StringUtil.nullTo(rs.getString("TEMPNO"),""));
				asrrotList.put("errorCode", rs.getString("ERROR_CODE"));
				asrrotList.put("errorMsg", rs.getString("ERROR_MSG"));
				
				cell.add(asrrotList);
			}
		
			jsonObject.put("list", cell);
			//Logger.debug("jsonStr="+jsonObject.toString());
			
			
			int conTimeOUt 	= 120000;
			int soTimeOut 	= 120000;
			int idleTimeout = 120000;
			
			MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
			HttpClient httpClient = new HttpClient(connectionManager);
			httpClient.getParams().setParameter("http.protocol.expect-continue", false);
			httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
			httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
			connectionManager.closeIdleConnections(idleTimeout);
			connectionManager.getParams().setMaxTotalConnections(100);
			
			
			//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
			post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
			post.addParameter("joinID", "");
			post.addParameter("command", commandNM);
			post.addParameter("data", jsonObject.toString());
			
			int resultCode = httpClient.executeMethod(post);
			
			
			if (String.valueOf(resultCode).equals("200")) {
				br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
				String retString = "";
				String line;
				
				while ((line = br.readLine()) != null) {
					retString += line;
				}
				br.close();
				
				String jsonString = URLDecoder.decode(retString,"UTF-8");
				
				JSONObject jobj = JSONObject.fromObject(jsonString);
				String errorcd = (String) jobj.get("errorcd");
				String errormsg = (String) jobj.get("errormsg");
				
				if (errorcd.equals("01")) {
					sendMessage = "SUCCESS!!";

				} else {
					sendMessage = "ERROR : "+ errormsg;
				}
				
			} else {	
				sendMessage = "ERROR : API Connection Fail!!";
			}
			
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
	}
	
	
	public String api_Auto_PO_Refuse(String dbmode, String command, String vendorID, String transCD, String sendDomain) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.api_Auto_PO_Refuse()";
		Logger.debug(methodName);
		
		Connection 			conn		= null;
		PreparedStatement	pstmt		= null;
		PreparedStatement	pstmt2		= null;
		ResultSet			rs			= null;
		StringBuffer   		sqlBuffer  	= new StringBuffer(500);	//서브 쿼리문
		StringBuffer   		sqlBuffer2  = new StringBuffer(500);	//UPDATE 쿼리문
		
		List<Object> getOrderSendData 	= null;
		Map<String, String> vMap 		= null;
		
		String 	sendMessage 	= null;
		
		String vendor_id 		= ""; 	//거래처코드
		String po_id 	 		= "";  	//출고번호
		String po_seq    		= "";  	//출고순번
		String cancel_dt   		= "";  	//취소일자
		
		String ship_id			= "";
		String apply_dt			= "";
		String apply_time		= "";
		String deli_company_id	= "";
		String bl_no			= "";
		String error_code		= "100";
		String message			= "연동처리 대기";
		String cancel_code		= "";
		String cancelNm			= "";
		String call_dt 	 		= "";
		String call_seq  		= "";
		
		BufferedReader br 	= null;
		String url = sendDomain +"/malladmin/join/pubApiManager.jsp";	//SEND URL
		PostMethod post = new PostMethod(url);
		
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO dao = new ServiceDAO();

			sqlBuffer.append("SELECT  /*+ INDEX_DESC(CALL_DT,CALL_SEQ) */						 				");
			sqlBuffer.append("		  LTRIM(TO_CHAR(TO_NUMBER(NVL(MAX(CALL_SEQ), 0)) + 1, '0000')) as CALL_SEQ 	");
			sqlBuffer.append("FROM 	  API_SEND_LOG															 	");
			sqlBuffer.append("WHERE   CALL_DT = TO_CHAR(SYSDATE,'YYYYMMDD') 						 			");
			pstmt = conn.prepareStatement(sqlBuffer.toString());
			
			sqlBuffer2.append("UPDATE  API_SEND_LOG					");
			sqlBuffer2.append("		   SET RESULT_CODE  = ? 		");
			sqlBuffer2.append("			   ,RESULT_NAME = ?			");
			sqlBuffer2.append("WHERE   CALL_DT  = ?					");
			sqlBuffer2.append("AND     CALL_SEQ = ? 				");
			sqlBuffer2.append("AND     VENDOR_ID = ? 				");
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
			
			
			call_dt = CommonUtil.getCurrentDate();
			
			getOrderSendData = dao.getOrderSendData(conn, command, call_dt, call_seq, transCD);
			
			if (getOrderSendData.size() > 0) {
				
				JSONObject jsonObject = new JSONObject();
				JSONArray cell = new JSONArray();
				
				for (int i = 0; i < getOrderSendData.size(); i++) {
					vMap = (HashMap<String, String>) getOrderSendData.get(i);
					
					vendor_id 	= vMap.get("VENDOR_ID");
					po_id 		= vMap.get("TEMPNO").trim();
					po_seq 		= vMap.get("ORDER_SEQ").trim();
					cancel_dt 	= vMap.get("CANCEL_DT");
					cancelNm 	= vMap.get("CANCEL_NM");					
					if (command.equals("SoldOutCancel")) {			//품절취소
						cancel_code = vMap.get("CANCEL_CODE");
					} else if (command.equals("ReturnRefuse")) {	//반품거부
						cancel_code = "";
					}
					
					
					ship_id = po_id + po_seq;
					
					
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						call_seq = rs.getString("CALL_SEQ");
					}
					
					rs.close();
					//Logger.debug("call_seq="+call_seq);
					
					JSONObject asrrotList = new JSONObject();
					
					asrrotList.put("vendorId", vendor_id);
					asrrotList.put("relationNo", po_id);
					asrrotList.put("relationSeq", po_seq);
					asrrotList.put("cancelDt", cancel_dt);
					asrrotList.put("cancelCd", cancel_code);
					asrrotList.put("CancelReason", cancelNm);
					asrrotList.put("callDt", call_dt);
					asrrotList.put("callSeq", call_seq);
					
					cell.add(asrrotList);
					
					cubeDao.setSendLog(dbmode, "MangoAPI", command, cubeDao.getApiName(command), vendor_id, ship_id, apply_dt, apply_time, deli_company_id, bl_no, error_code, message, cancel_code, transCD);
					
				}
				
				jsonObject.put("list", cell);
				//Logger.debug("jsonStr="+jsonObject.toString());
				
				
				int conTimeOUt 	= 120000;
				int soTimeOut 	= 120000;
				int idleTimeout = 120000;
				
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				HttpClient httpClient = new HttpClient(connectionManager);
				httpClient.getParams().setParameter("http.protocol.expect-continue", false);
				httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
				httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
				httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
				httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
				connectionManager.closeIdleConnections(idleTimeout);
				connectionManager.getParams().setMaxTotalConnections(100);
				
				
				//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
				post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.addParameter("joinID", vendorID);
				post.addParameter("command", command);
				post.addParameter("data", jsonObject.toString());
				
				int resultCode = httpClient.executeMethod(post);
				
				
				if (String.valueOf(resultCode).equals("200")) {
					
					br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
					String retString = "";
					String line;
					
					while ((line = br.readLine()) != null) {
						retString += line;
					}
					br.close();
					
					String jsonString = URLDecoder.decode(retString,"UTF-8");
					
					JSONObject jobj = JSONObject.fromObject(jsonString);
					String errorcd = (String) jobj.get("errorcd");
					String errormsg = (String) jobj.get("errormsg");
					
					if (errorcd.equals("01")) {
						String succStr = "SUCCESS!!";
						
						String rtVendorId	= "";
						String rtResultCd  	= "";
						String rtResultMsg  = "";
						String rtCallDt 	= "";
						String rtCallSeq 	= "";
						
						JSONArray jarray = jobj.getJSONArray("list");
						
						for (int i = 0; i < jarray.size(); i++)
						{
							JSONObject rtList = jarray.getJSONObject(i);
							
							
							rtVendorId	= rtList.getString("vendorId");
							rtResultCd	= rtList.getString("resultCode");
							rtResultMsg = rtList.getString("resultMsg");
							rtCallDt 	= rtList.getString("callDt");
							rtCallSeq 	= rtList.getString("callSeq");
							
							if (rtResultCd.equals("01")) {
								rtResultCd = "000";		//정상
							} else {
								rtResultCd = "100";		//에러
							}
							
							//전송대상 BARCODE 전송 결과 업데이트..
							if (pstmt2 != null) { pstmt2.close(); pstmt2 = null; }
							pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
							
							pstmt2.setString(1, rtResultCd);
							pstmt2.setString(2, rtResultMsg);
							pstmt2.setString(3, rtCallDt);
							pstmt2.setString(4, rtCallSeq);
							pstmt2.setString(5, rtVendorId);
							pstmt2.executeUpdate();
							
							
							//Logger.debug("vendorId="+rtVendorId+",resultCode="+rtResultCd+",resultMsg="+rtResultMsg+",callDt="+rtCallDt+",callSeq="+rtCallSeq);
						}
						
						sendMessage = succStr;
						
					} else {
						sendMessage = "ERROR : "+ errormsg;
					}
					
					
				} else {
					sendMessage = "ERROR : API Connection Fail!!";
					
					cubeDao.setSendLog(dbmode, "MangoAPI", command, cubeDao.getApiName(command), "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "100", sendMessage, "00", transCD);
				}
					
			} else {
				sendMessage = "연동할 대상 정보가 없습니다.";
				
				cubeDao.setSendLog(dbmode, "MangoAPI", command, cubeDao.getApiName(command), "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "100", sendMessage, "00", transCD);
			}
		
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
	}
	
	
	public String api_Auto_ItemStock(String dbmode, String command, String vendorID, String transCD, String sendDomain) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.api_Auto_ItemStock()";
		Logger.debug(methodName);
		
		Connection 			conn		= null;
		CallableStatement 	cstmt    	= null;
		PreparedStatement	pstmt		= null;
		PreparedStatement	pstmt2		= null;
		PreparedStatement	pstmt3		= null;
		PreparedStatement	pstmt4		= null;
		PreparedStatement	pstmt5		= null;
		ResultSet			rs			= null;
		ResultSet			rs2			= null;
		ResultSet			rs3			= null;
		StringBuffer   		sqlBuffer  	= new StringBuffer(500);	//쿼리문
		StringBuffer   		sqlBuffer2  = new StringBuffer(500);	//쿼리문
		StringBuffer   		sqlBuffer3  = new StringBuffer(500);	//쿼리문
		StringBuffer   		sqlBuffer4  = new StringBuffer(500);	//쿼리문
		
		
		int cnt 	= 0;
		String 	sendMessage 	= null;
		
		BufferedReader br 		= null;
		String url = sendDomain +"/malladmin/join/pubApiManager.jsp";	//SEND URL
		PostMethod post = new PostMethod(url);
		
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
		

			sqlBuffer.append("SELECT  VENDOR_ID           	\n");
			sqlBuffer.append("        ,BARCODE            	\n");
			sqlBuffer.append("        ,ASSORT_ID          	\n");
			sqlBuffer.append("        ,ITEM_ID            	\n");
			sqlBuffer.append("        ,STOCK              	\n");
			sqlBuffer.append("FROM    TBD260              	\n");
			sqlBuffer.append("WHERE   TRAN_DATE = ?			\n");
			sqlBuffer.append("AND     TRAN_SEQ  = ?        	\n");
			pstmt = conn.prepareStatement(sqlBuffer.toString());
			
			sqlBuffer2.append("SELECT  COUNT(1) AS CNT		\n");
			sqlBuffer2.append("FROM    TBD260              	\n");
			sqlBuffer2.append("WHERE   TRAN_DATE = ?		\n");
			sqlBuffer2.append("AND     TRAN_SEQ  = ?        \n");
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
	   
			sqlBuffer3.append("UPDATE  TBD260              	\n");
			sqlBuffer3.append("    SET STATUS 	   = ?      \n");
			sqlBuffer3.append("        ,STATUS_MSG = ?      \n");
			sqlBuffer3.append("WHERE   TRAN_DATE = ?        \n");
			sqlBuffer3.append("AND     TRAN_SEQ	 = ?        \n");
			sqlBuffer3.append("AND     BARCODE 	 = ?        \n");
			sqlBuffer3.append("AND     ASSORT_ID = ?        \n");
			sqlBuffer3.append("AND     ITEM_ID 	 = ?        \n");
			pstmt3 = conn.prepareStatement(sqlBuffer3.toString());
			
			sqlBuffer4.append("UPDATE  TBD260              	\n");
			sqlBuffer4.append("    SET STATUS 	   = ?      \n");
			sqlBuffer4.append("        ,STATUS_MSG = ?      \n");
			sqlBuffer4.append("WHERE   TRAN_DATE = ?        \n");
			sqlBuffer4.append("AND     TRAN_SEQ	 = ?        \n");
			pstmt4 = conn.prepareStatement(sqlBuffer4.toString());
			
			StringBuffer sqlBuffer5 = new StringBuffer();
			sqlBuffer5.append(" SELECT   RETC AS COCD				\n");	
			sqlBuffer5.append("       , CD4  AS VDCD				\n");			
			sqlBuffer5.append("  FROM TBB150					    \n");	
			sqlBuffer5.append(" WHERE REFTP = 'ZY'					\n");	
			sqlBuffer5.append("   AND REFCD <> '0000'				\n");	
			sqlBuffer5.append("   AND CD4   = '"+ transCD +"'		\n");	
			sqlBuffer5.append("   GROUP BY RETC, CD4				\n");
			
			pstmt5 = conn.prepareStatement(sqlBuffer5.toString());
			rs3 = pstmt5.executeQuery();
			
			while(rs3.next()) {
				Logger.debug("[COCD["+StringUtil.nullTo(rs3.getString("COCD"),"")+"]");		// 사업부코드
				String cocd = StringUtil.nullTo(rs3.getString("COCD"),""); 
				
				//상품등록 프로시저 실행..
				cstmt = conn.prepareCall("{call P_SEND_STOCK(?, ?, ?, ?, ?, ?)}");
				cstmt.registerOutParameter(1, Types.CHAR);
	        	cstmt.registerOutParameter(2, Types.CHAR);
	        	cstmt.registerOutParameter(3, Types.CHAR);
	        	cstmt.registerOutParameter(4, Types.INTEGER);
	        	cstmt.setString(5, transCD);
	        	cstmt.setString(6, cocd);
	        	
	        	cstmt.executeUpdate();
	        	
	        	String errcode = cstmt.getString(1);
	        	String errmsg = cstmt.getString(2);	
	            String tranDt  = cstmt.getString(3);
	            int    tranSeq = cstmt.getInt(4);
	            
	            //Logger.debug("errcode="+errcode);
	            //Logger.debug("errmsg="+errmsg);
	            //Logger.debug("tranDt="+tranDt);
	            //Logger.debug("tranSeq="+tranSeq);
	            
	            
	            if (errcode.equals("00")) {
	            	
	            	pstmt2.setString(1, tranDt);
					pstmt2.setInt(2, tranSeq);
	            	rs2 = pstmt2.executeQuery();
	    			
	    			if(rs2.next())
	    			{
	    				cnt = rs2.getInt("CNT");
	    			}
	            	
	    			//전송 DATA 있을때..
	    			if(cnt > 0)
	    			{
	    				pstmt.setString(1, tranDt);
	    				pstmt.setInt(2, tranSeq);
	    				rs = pstmt.executeQuery();
	    				
	    				
	    				JSONObject jsonObject = new JSONObject();
	    				JSONArray cell = new JSONArray();
	    				
	    				while(rs.next())
	    				{
	    					JSONObject asrrotList = new JSONObject();
	    					
	    					asrrotList.put("vendorId", rs.getString("VENDOR_ID"));
	    					asrrotList.put("bar_code", rs.getString("BARCODE"));
	    					asrrotList.put("assort_id", rs.getString("ASSORT_ID"));
	    					asrrotList.put("item_id", rs.getString("ITEM_ID"));
	    					asrrotList.put("stockQty", rs.getString("STOCK"));
	    						
	    					cell.add(asrrotList);
	    				}
	    				
	    				jsonObject.put("list", cell);
	    				//Logger.debug("jsonStr="+jsonObject.toString());
	    				
	    				
	    				int conTimeOUt 	= 120000;
	    				int soTimeOut 	= 120000;
	    				int idleTimeout = 120000;
	    				
	    				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	    				HttpClient httpClient = new HttpClient(connectionManager);
	    				httpClient.getParams().setParameter("http.protocol.expect-continue", false);
	    				httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
	    				httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
	    				httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
	    				httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
	    				connectionManager.closeIdleConnections(idleTimeout);
	    				connectionManager.getParams().setMaxTotalConnections(100);
	    				
	    				
	    				//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
	    				post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
	    				post.addParameter("joinID", "");
	    				post.addParameter("command", command);
	    				post.addParameter("data", jsonObject.toString());
	    				
	    				int resultCode = httpClient.executeMethod(post);
	    				
	    				
	    				if (String.valueOf(resultCode).equals("200")) {
	    					
	    					br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
	    					String retString = "";
	    					String line;
	    					
	    					while ((line = br.readLine()) != null) {
	    						retString += line;
	    					}
	    					br.close();
	    					
	    					String jsonString = URLDecoder.decode(retString,"UTF-8");
	    					
	    					JSONObject jobj = JSONObject.fromObject(jsonString);
	    					String errorcd = (String) jobj.get("errorcd");
	    					String errormsg = (String) jobj.get("errormsg");
	    					
	    					
	    					if (errorcd.equals("01")) {
	    						
	    						JSONArray jarray = jobj.getJSONArray("list");
	    						
	    						String rtBarcode  	= "";
	    						String rtAssortid  	= "";
	    						String rtItemid 	= "";
	    						String rtStatuscd 	= "";
	    						String rtMessage 	= "";
	    						
	    						for (int i = 0; i < jarray.size(); i++)
	    						{
	    							JSONObject rtList = jarray.getJSONObject(i);
	    							
	    							rtBarcode	= rtList.getString("barcode");
	    							rtAssortid 	= rtList.getString("assortid");
	    							rtItemid 	= rtList.getString("itemid");
	    							rtStatuscd 	= rtList.getString("statuscd");
	    							rtMessage 	= rtList.getString("message");
	    							
	    							//재고연동 전송 결과 업데이트..
	    							if (pstmt3 != null) { pstmt3.close(); pstmt3 = null; }
	    							pstmt3 = conn.prepareStatement(sqlBuffer3.toString());
	    							
	    							pstmt3.setString(1, rtStatuscd);
	    							pstmt3.setString(2, rtMessage);
	    							pstmt3.setString(3, tranDt);
	    							pstmt3.setInt(4, tranSeq);
	    							pstmt3.setString(5, rtBarcode);
	    							pstmt3.setString(6, rtAssortid);
	    							pstmt3.setString(7, rtItemid);
	    							pstmt3.executeUpdate();
	    						
	    							//Logger.debug("tranDt="+tranDt+",tranSeq="+tranSeq+",barcode="+rtBarcode+",assortid="+rtAssortid+",itemid="+rtItemid+",statuscd="+rtStatuscd+",message="+rtMessage);
	    						}
	    						
	    						sendMessage = "SUCCESS!!";
	    						
	    					} else {
	    						//연동 오류건 결과 내용 업데이트..
	    						pstmt4.setString(1, errorcd);
	    		            	pstmt4.setString(2, errormsg);
	    		            	pstmt4.setString(3, tranDt);
	    		            	pstmt4.setInt(4, tranSeq);
	    		            	pstmt4.executeUpdate();
	    						
	    						sendMessage = "ERROR : "+ errormsg;
	    					}
	    					
	    				} else {	
	    					sendMessage = "ERROR : API Connection Fail!!";
	    				}
	    				
	    			}
	    			else	//전송 DATA 없을때.. 
	    			{
	    				sendMessage = "NO DATA!! SUCCESS!!";
	    			}
	    			
	            } else {
	            	sendMessage	= "Error : "+ errmsg;
	            }
			}
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}
				if( rs3 !=null ) try{ rs3.close(); rs3 = null; }catch(Exception e){}finally{rs3 = null;}
				
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				if( pstmt3 != null ) try{ pstmt3.close(); pstmt3 = null; }catch(Exception e){}finally{pstmt3 = null;}
				if( pstmt4 != null ) try{ pstmt4.close(); pstmt4 = null; }catch(Exception e){}finally{pstmt4 = null;}
				if( pstmt5 != null ) try{ pstmt5.close(); pstmt5 = null; }catch(Exception e){}finally{pstmt5 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}	
			
		
		return sendMessage;
	}
	
	/*	커넥트미(CM) API 연동 작업 부분 주석 처리.. 2014-03-06
	public String CM_Api_Item(String dbmode, String command, String vendorID, String transCD, String sendDomain, String cmID, String cmPassKey) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.CM_Api_Item()";
		Logger.debug(methodName);
		
		Connection 			conn		= null;
		
		PreparedStatement	pstmt		= null;
		PreparedStatement	pstmt2		= null;
		PreparedStatement	pstmt3		= null;
		PreparedStatement	pstmt4		= null;
		PreparedStatement	pstmt5		= null;
		
		ResultSet			rs			= null;
		ResultSet			rs2			= null;
		ResultSet			rs3			= null;
		
		StringBuffer   		sqlBuffer  	= new StringBuffer(1000);	//주 쿼리문
		StringBuffer   		sqlBuffer2  = new StringBuffer(500);	//서브 쿼리문
		StringBuffer   		sqlBuffer3  = new StringBuffer(500);	//카운트 쿼리문
		StringBuffer   		sqlBuffer4  = new StringBuffer(500);	//UPDATE 쿼리문
		StringBuffer   		sqlBuffer5  = new StringBuffer(500);	//UPDATE 쿼리문
		
		int cnt 	= 0;
		String 	sendMessage 	= null;
		
		BufferedReader br 		= null;
		String url = sendDomain +"/proc/product/createProduct.api";	//SEND URL
		PostMethod post = new PostMethod(url);
		
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			
			
			sqlBuffer.append("SELECT  MAX(A.TRAN_DATE)      AS TRAN_DATE						\n");
			sqlBuffer.append("        , MAX(A.TRAN_SEQ)     AS TRAN_SEQ                     	\n");
			sqlBuffer.append("        , MAX(A.SEQ)          AS SEQ                          	\n");
			sqlBuffer.append("        , MAX(A.STORAGE_ID)   AS STORAGE_ID                   	\n");
			sqlBuffer.append("        , A.PRODINC                                           	\n");
			sqlBuffer.append("        , MAX(A.PNAME)        AS PNAME                        	\n");
			sqlBuffer.append("        , MAX(A.WEIGHT)       AS WEIGHT                       	\n");
			sqlBuffer.append("        , MAX(A.WEIGHT_UNIT)  AS WEIGHT_UNIT                  	\n");
			sqlBuffer.append("        , MAX(A.ASSORT_GB)    AS ASSORT_GB                    	\n");
			sqlBuffer.append("        , MAX(A.NONSALE_YN)   AS NONSALE_YN                   	\n");
			sqlBuffer.append("        , MAX(A.SHORTAGE_YN)  AS SHORTAGE_YN                  	\n");
			sqlBuffer.append("        , MAX(A.RES_UNIT)     AS RES_UNIT                     	\n");
			sqlBuffer.append("        , MAX(A.TAX_GB)       AS TAX_GB                       	\n");
			sqlBuffer.append("        , MAX(A.PURL)         AS PURL                         	\n");
			sqlBuffer.append("        , MAX(A.STORY)        AS STORY                        	\n");
			sqlBuffer.append("        , MAX(A.BRAND_ID)     AS BRAND_ID                     	\n");
			sqlBuffer.append("        , MAX(A.CATEGORY_ID)  AS CATEGORY_ID                  	\n");
			sqlBuffer.append("        , MAX(A.NATION)       AS NATION                       	\n");
			sqlBuffer.append("        , MAX(A.LOCAL_PRICE)  AS LOCAL_PRICE                  	\n");
			sqlBuffer.append("        , MAX(A.LOCAL_SALE)   AS LOCAL_SALE            			\n");
			sqlBuffer.append("        , MAX(A.DELI_PRICE)   AS DELI_PRICE                   	\n");
			sqlBuffer.append("        , MAX(A.ESTI_PRICE)   AS ESTI_PRICE                   	\n");
			sqlBuffer.append("        , MAX(A.MARGIN_GB)    AS MARGIN_GB                    	\n");
			sqlBuffer.append("        , MAX(A.SALE_PRICE)   AS SALE_PRICE                   	\n");
			sqlBuffer.append("        , MAX(A.USER_ID)      AS USER_ID                      	\n");
			sqlBuffer.append("        , A.VENDOR_ID                                         	\n");
			sqlBuffer.append("        , MAX(A.CARD_FEE)     AS CARD_FEE                     	\n");
			sqlBuffer.append("        , MAX(C.COCD)         AS SUPPLY_ID                       	\n");
			sqlBuffer.append("FROM    TBP050_TRANSFER A,                                    	\n");
			sqlBuffer.append("        (                                                     	\n");
			sqlBuffer.append("            SELECT  BAR_CODE                                  	\n");
			sqlBuffer.append("                    , VENDOR_ID                               	\n");
			sqlBuffer.append("                    , MAX(TRAN_DATE) AS TRAN_DATE             	\n");
			sqlBuffer.append("                    , MAX(TRAN_SEQ)  AS TRAN_SEQ              	\n");
			sqlBuffer.append("            FROM    TBP050_TRANSFER                           	\n");
			sqlBuffer.append("            WHERE   STATUS IN ('00', '99')                    	\n");
			sqlBuffer.append("            GROUP BY BAR_CODE, VENDOR_ID                      	\n");
			sqlBuffer.append("        )   B,                                                	\n");
			sqlBuffer.append("        (                                                     	\n");
			sqlBuffer.append("            SELECT  REFCD AS VENDOR_ID                        	\n");
			sqlBuffer.append("                    , CD1   AS SHOP_ID                        	\n");
			sqlBuffer.append("                    , RETC  AS COCD                           	\n");
			sqlBuffer.append("            FROM    TBB150                                    	\n");
			sqlBuffer.append("            WHERE   REFTP = 'ZY'                              	\n");
			sqlBuffer.append("            AND     REFCD <> '0000'                           	\n");
			sqlBuffer.append("            AND     CD4   = '"+ transCD +"'                   	\n");
			sqlBuffer.append("        ) C                                                   	\n");
			sqlBuffer.append("WHERE  A.TRAN_DATE = B.TRAN_DATE                              	\n");
			sqlBuffer.append("AND    A.TRAN_SEQ  = B.TRAN_SEQ                               	\n");
			sqlBuffer.append("AND    A.BAR_CODE  = B.BAR_CODE                               	\n");
			sqlBuffer.append("AND    A.VENDOR_ID = C.VENDOR_ID                              	\n");
			sqlBuffer.append("AND    A.SHOP_ID   = C.SHOP_ID                                	\n");
			sqlBuffer.append("GROUP BY A.VENDOR_ID, A.PRODINC                               	\n");
			sqlBuffer.append("ORDER BY A.VENDOR_ID, A.PRODINC                               	\n");

			pstmt = conn.prepareStatement(sqlBuffer.toString());
			
			
			sqlBuffer2.append("SELECT  ITEM_COLOR                                    			\n");
			sqlBuffer2.append("        ,ITEM_SIZE                                               \n");
			sqlBuffer2.append("        ,BAR_CODE                                                \n");
			sqlBuffer2.append("        ,ORDER_LMT_YN                                            \n");
			sqlBuffer2.append("        ,ORDER_LMT_CNT                                           \n");
			sqlBuffer2.append("FROM    TBP050_TRANSFER                                 			\n");
			sqlBuffer2.append("WHERE   TRAN_DATE = ?                                   			\n");
			sqlBuffer2.append("AND     TRAN_SEQ  = ?                                          	\n");
			sqlBuffer2.append("AND     PRODINC	 = ?                                    		\n");
			sqlBuffer2.append("ORDER BY TRAN_DATE, TRAN_SEQ, SEQ                                \n");
			
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
			
			
			sqlBuffer3.append("SELECT  COUNT(1) AS CNT											\n");
			sqlBuffer3.append("FROM    TBP050_TRANSFER A,                                    	\n");
			sqlBuffer3.append("        (                                                     	\n");
			sqlBuffer3.append("            SELECT  BAR_CODE                                  	\n");
			sqlBuffer3.append("                    , VENDOR_ID                               	\n");
			sqlBuffer3.append("                    , MAX(TRAN_DATE) AS TRAN_DATE             	\n");
			sqlBuffer3.append("                    , MAX(TRAN_SEQ)  AS TRAN_SEQ              	\n");
			sqlBuffer3.append("            FROM    TBP050_TRANSFER                           	\n");
			sqlBuffer3.append("            WHERE   STATUS IN ('00', '99')                    	\n");
			sqlBuffer3.append("            GROUP BY BAR_CODE, VENDOR_ID                      	\n");
			sqlBuffer3.append("        )   B,                                                	\n");
			sqlBuffer3.append("        (                                                     	\n");
			sqlBuffer3.append("            SELECT  REFCD AS VENDOR_ID                        	\n");
			sqlBuffer3.append("                    , CD1   AS SHOP_ID                        	\n");
			sqlBuffer3.append("                    , RETC  AS COCD                           	\n");
			sqlBuffer3.append("            FROM    TBB150                                    	\n");
			sqlBuffer3.append("            WHERE   REFTP = 'ZY'                              	\n");
			sqlBuffer3.append("            AND     REFCD <> '0000'                           	\n");
			sqlBuffer3.append("            AND     CD4   = '"+ transCD +"'                      \n");
			sqlBuffer3.append("        ) C                                                   	\n");
			sqlBuffer3.append("WHERE  A.TRAN_DATE = B.TRAN_DATE                              	\n");
			sqlBuffer3.append("AND    A.TRAN_SEQ  = B.TRAN_SEQ                               	\n");
			sqlBuffer3.append("AND    A.BAR_CODE  = B.BAR_CODE                               	\n");
			sqlBuffer3.append("AND    A.VENDOR_ID = C.VENDOR_ID                              	\n");
			sqlBuffer3.append("AND    A.SHOP_ID   = C.SHOP_ID                                	\n");
			
			pstmt3 = conn.prepareStatement(sqlBuffer3.toString());
			
			
			sqlBuffer4.append("UPDATE  TBP050_TRANSFER                                          			                                                               	\n");
			sqlBuffer4.append("    SET ASSORT_ID   = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,ITEM_ID    = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,STATUS     = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,STATUS_MSG = ?                                          			                                                                \n");
			sqlBuffer4.append("        ,UPD_ID 	= 'CMAPI'                              				                                                                    	\n");
			sqlBuffer4.append("        ,UPD_DT 	= TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS')       			                                                                    \n");
			sqlBuffer4.append("WHERE   (TRAN_DATE, TRAN_SEQ, SEQ, PRODINC, BAR_CODE, VENDOR_ID)            			                                                        \n");
			sqlBuffer4.append("    IN  (                                                        			                                                                \n");
			sqlBuffer4.append("            SELECT  M1.TRAN_DATE, M1.TRAN_SEQ, M1.SEQ, M1.PRODINC, M1.BAR_CODE, M1.VENDOR_ID	                                                \n");
			sqlBuffer4.append("            FROM    TBP050_TRANSFER M1                            			                                                                \n");
			sqlBuffer4.append("                    ,(                                                                                                                       \n");
			sqlBuffer4.append("                        SELECT  A.BAR_CODE                                                                                                   \n");
			sqlBuffer4.append("                                ,A.TRAN_DATE                                                                                                 \n");
			sqlBuffer4.append("                                ,(SELECT MAX(TRAN_SEQ) FROM TBP050_TRANSFER WHERE TRAN_DATE=A.TRAN_DATE AND BAR_CODE=A.BAR_CODE AND VENDOR_ID=A.VENDOR_ID) AS TRAN_SEQ	\n");
			sqlBuffer4.append("                        FROM    (                                                                                                            \n");
			sqlBuffer4.append("                                    SELECT  BAR_CODE                                                                                         \n");
			sqlBuffer4.append("                                      	   ,VENDOR_ID                                                                                       \n");
			sqlBuffer4.append("                                            ,MAX(TRAN_DATE) AS TRAN_DATE                                                                     \n");
			sqlBuffer4.append("                                    FROM    TBP050_TRANSFER                                                                                  \n");
			sqlBuffer4.append("                                    WHERE   STATUS IN ('00', '99')                                                                           \n");
			sqlBuffer4.append("                                    GROUP BY BAR_CODE, VENDOR_ID                                                                             \n");
			sqlBuffer4.append("                                )   A                                                                                                        \n");
			sqlBuffer4.append("                    )   M2                                        			                                                                \n");
			sqlBuffer4.append("            WHERE   M1.TRAN_DATE = M2.TRAN_DATE                    			                                                                \n");
			sqlBuffer4.append("            AND     M1.TRAN_SEQ  = M2.TRAN_SEQ                     			                                                                \n");
			sqlBuffer4.append("            AND     M1.BAR_CODE  = M2.BAR_CODE                     			                                                                \n");
			sqlBuffer4.append("            AND     M1.PRODINC 	= ?                             			                                                                \n");
			sqlBuffer4.append("            AND     M1.BAR_CODE  = ?                             			                                                                \n");
			sqlBuffer4.append("            AND     M1.VENDOR_ID = ?                             			                                                                \n");
			sqlBuffer4.append("        )                                                                                                                                    \n");

			pstmt4 = conn.prepareStatement(sqlBuffer4.toString());
			
			
			sqlBuffer5.append("UPDATE  TBP050_TRANSFER                                                                  	                                                                \n");
			sqlBuffer5.append("    SET STATUS  = ?                                                                                                                                          \n");
			sqlBuffer5.append("        ,UPD_ID = 'CMAPI'                                                                	                                                                \n");
			sqlBuffer5.append("        ,UPD_DT = TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS')                                       	                                                                \n");
			sqlBuffer5.append("WHERE   (TRAN_DATE, TRAN_SEQ, SEQ, PRODINC, BAR_CODE, VENDOR_ID)                                                                                             \n");
			sqlBuffer5.append("    IN  (                                                                                                                                                    \n");
			sqlBuffer5.append("            SELECT                                                                                                                                           \n");
			sqlBuffer5.append("                    M1.TRAN_DATE, M1.TRAN_SEQ, M1.SEQ, M1.PRODINC, M1.BAR_CODE, M1.VENDOR_ID                                                                 \n");
			sqlBuffer5.append("            FROM    (                                                                                                                                        \n");
			sqlBuffer5.append("                        SELECT                                                                                                                               \n");
			sqlBuffer5.append("                                TRAN_DATE||LPAD(TRAN_SEQ,4,'0') AS WORK_DT               	                                                                \n");
			sqlBuffer5.append("                                ,TRAN_DATE                                                 	                                                                \n");
			sqlBuffer5.append("                                ,TRAN_SEQ                                                  	                                                                \n");
			sqlBuffer5.append("                                ,SEQ                                                       	                                                                \n");
			sqlBuffer5.append("                                ,PRODINC                                                   	                                                                \n");
			sqlBuffer5.append("                                ,BAR_CODE                                                  	                                                                \n");
			sqlBuffer5.append("                                ,STATUS                                                    	                                                                \n");
			sqlBuffer5.append("                                ,STATUS_MSG                                                	                                                                \n");
			sqlBuffer5.append("                                ,VENDOR_ID                                                	                                                                \n");
			sqlBuffer5.append("                        FROM    TBP050_TRANSFER                            			    	                                                                \n");
			sqlBuffer5.append("                        WHERE   STATUS IN ('00', '99')                           			                                                                \n");
			sqlBuffer5.append("                        AND     PRODINC 	 = ?                           						                                                                \n");
			sqlBuffer5.append("                        AND     BAR_CODE  = ?                                					                                                            \n");
			sqlBuffer5.append("                        AND     VENDOR_ID = ?                                					                                                            \n");
			sqlBuffer5.append("                    )   M1,                                                                                                                                  \n");
			sqlBuffer5.append("                    (                                                                                                                                        \n");
			sqlBuffer5.append("                        SELECT                                                                                                                               \n");
			sqlBuffer5.append("                                MAX(A.TRAN_DATE||LPAD(A.TRAN_SEQ,4,'0')) AS WORK_DT                                                                          \n");
			sqlBuffer5.append("                                ,A.PRODINC                                                                                                                   \n");
			sqlBuffer5.append("                                ,A.BAR_CODE	                                                                                                                \n");
			sqlBuffer5.append("                                ,A.VENDOR_ID	                                                                                                                \n");
			sqlBuffer5.append("                        FROM    TBP050_TRANSFER	A                            			                                                                    \n");
			sqlBuffer5.append("                                ,(                                                                                                                           \n");
			sqlBuffer5.append("                                    SELECT  C.BAR_CODE                                                                                                       \n");
			sqlBuffer5.append("                                            ,C.TRAN_DATE                                                                                                     \n");
			sqlBuffer5.append("                                            ,C.VENDOR_ID                                                                                                     \n");
			sqlBuffer5.append("                                            ,(SELECT MAX(TRAN_SEQ) FROM TBP050_TRANSFER WHERE TRAN_DATE=C.TRAN_DATE AND BAR_CODE=C.BAR_CODE AND VENDOR_ID=C.VENDOR_ID) AS TRAN_SEQ		\n");
			sqlBuffer5.append("                                    FROM    (                                                                                                                \n");
			sqlBuffer5.append("                                                SELECT  BAR_CODE                                                                                             \n");
			sqlBuffer5.append("                                                        ,VENDOR_ID                                                                         					\n");
			sqlBuffer5.append("                                                        ,MAX(TRAN_DATE) AS TRAN_DATE                                                                         \n");
			sqlBuffer5.append("                                                FROM    TBP050_TRANSFER                                                                                      \n");
			sqlBuffer5.append("                                                WHERE   STATUS = '01'                                                                                        \n");
			sqlBuffer5.append("                                                GROUP BY BAR_CODE, VENDOR_ID                                                                                 \n");
			sqlBuffer5.append("                                            )   C                                                                                                            \n");
			sqlBuffer5.append("                                )   B                                        			                                                                    \n");
			sqlBuffer5.append("                        WHERE   A.TRAN_DATE = B.TRAN_DATE                    			                                                                    \n");
			sqlBuffer5.append("                        AND     A.TRAN_SEQ  = B.TRAN_SEQ                     			                                                                    \n");
			sqlBuffer5.append("                        AND     A.BAR_CODE  = B.BAR_CODE                     			                                                                    \n");
			sqlBuffer5.append("                        AND     A.PRODINC   = ?                            					                                                                \n");
			sqlBuffer5.append("                        AND     A.BAR_CODE  = ?                                				                                                                \n");
			sqlBuffer5.append("                        AND     A.VENDOR_ID = ?                                				                                                                \n");
			sqlBuffer5.append("                        GROUP BY A.PRODINC, A.BAR_CODE, A.VENDOR_ID                                                                                          \n");
			sqlBuffer5.append("                    )   M2                                                                                                                                   \n");
			sqlBuffer5.append("            WHERE   M1.PRODINC   = M2.PRODINC                                                                                                                \n");
			sqlBuffer5.append("            AND     M1.BAR_CODE  = M2.BAR_CODE                                                                                                               \n");
			sqlBuffer5.append("            AND     M1.VENDOR_ID = M2.VENDOR_ID                                                                                                              \n");
			sqlBuffer5.append("            AND     M1.WORK_DT   < M2.WORK_DT                                                                                                                \n");
			sqlBuffer5.append("        )                                                                                                                                                    \n");
			
			pstmt5 = conn.prepareStatement(sqlBuffer5.toString());
			
			
			rs3 = pstmt3.executeQuery();
			
			if(rs3.next())
			{
				cnt = rs3.getInt("cnt");
			}
			
			
			//전송 DATA 있을때..
			if(cnt > 0)
			{
				rs = pstmt.executeQuery();
				
				JSONObject jsonObject = new JSONObject();
				JSONArray cell = new JSONArray();
				
				while(rs.next())
				{
					JSONObject asrrotList = new JSONObject();
					
					asrrotList.put("storage_id", rs.getString("STORAGE_ID"));
					
					asrrotList.put("prodinc", rs.getString("PRODINC"));
					asrrotList.put("pname", rs.getString("PNAME"));
					asrrotList.put("weight", rs.getString("WEIGHT"));
					asrrotList.put("weight_unit", rs.getString("WEIGHT_UNIT"));
					asrrotList.put("assort_gb", rs.getString("ASSORT_GB"));
					asrrotList.put("nonsale_yn", rs.getString("NONSALE_YN"));
					asrrotList.put("shortage_yn", rs.getString("SHORTAGE_YN"));
					asrrotList.put("res_unit", rs.getString("RES_UNIT"));
					asrrotList.put("tax_gb", rs.getString("TAX_GB"));
					
					if (rs.getString("PURL") == null || rs.getString("PURL").equals("")) {
						asrrotList.put("purl", ".");
					} else {
						asrrotList.put("purl", rs.getString("PURL"));
					}
					
					if (rs.getString("STORY") == null || rs.getString("STORY").equals("")) {
						asrrotList.put("story", ".");
					} else {
						asrrotList.put("story", rs.getString("STORY"));
					}
					
					asrrotList.put("brand_id", rs.getString("BRAND_ID"));
					asrrotList.put("category_id", rs.getString("CATEGORY_ID"));
					asrrotList.put("nation", rs.getString("NATION"));
					asrrotList.put("local_price", rs.getString("LOCAL_PRICE"));
					asrrotList.put("local_sale", rs.getString("LOCAL_SALE"));
					asrrotList.put("deli_price", rs.getString("DELI_PRICE"));
					asrrotList.put("esti_price", rs.getString("ESTI_PRICE"));
					asrrotList.put("margin_gb", rs.getString("MARGIN_GB"));
					asrrotList.put("sale_price", rs.getString("SALE_PRICE"));
					asrrotList.put("user_id", rs.getString("USER_ID"));
					asrrotList.put("vendor_id", rs.getString("VENDOR_ID"));
					asrrotList.put("supply_id", rs.getString("SUPPLY_ID"));
					asrrotList.put("card_fee", rs.getString("CARD_FEE"));
					
					//옵션 정보 가져오기..
					pstmt2.setString(1, rs.getString("TRAN_DATE"));
					pstmt2.setString(2, rs.getString("TRAN_SEQ"));
					pstmt2.setString(3, rs.getString("PRODINC"));
					
					rs2 = pstmt2.executeQuery();
					
					JSONArray cellOpt = new JSONArray();
					while (rs2.next()) 
					{
						JSONObject itemOption = new JSONObject();
						
						itemOption.put("item_color", rs2.getString("ITEM_COLOR"));
						itemOption.put("item_size", rs2.getString("ITEM_SIZE"));
						itemOption.put("bar_code", rs2.getString("BAR_CODE"));
						itemOption.put("order_lmt_yn", rs2.getString("ORDER_LMT_YN"));
						itemOption.put("order_lmt_cnt", rs2.getString("ORDER_LMT_CNT"));
						
						cellOpt.add(itemOption);
						asrrotList.put("optioninfo", cellOpt);
					}
					//옵션 정보 가져오기..
					
					cell.add(asrrotList);

				}
				
				jsonObject.put("list", cell);
				//Logger.debug("jsonStr="+jsonObject.toString());
				
				
				int conTimeOUt 	= 120000;
				int soTimeOut 	= 120000;
				int idleTimeout = 120000;
				
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				HttpClient httpClient = new HttpClient(connectionManager);
				httpClient.getParams().setParameter("http.protocol.expect-continue", false);
				httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
				httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
				httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
				httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
				connectionManager.closeIdleConnections(idleTimeout);
				connectionManager.getParams().setMaxTotalConnections(100);
				
				
				//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
				post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.addParameter("id", cmID);
				post.addParameter("passkey", cmPassKey);
				post.addParameter("data", jsonObject.toString());
				
				int resultCode = httpClient.executeMethod(post);
				
				
				if (String.valueOf(resultCode).equals("200")) {
					
					br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
					String retString = "";
					String line;
					
					while ((line = br.readLine()) != null) {
						retString += line;
					}
					br.close();
					
					String jsonString = URLDecoder.decode(retString,"UTF-8");
					//Logger.debug("rtJson = "+jsonString);
					
					JSONObject jobj = JSONObject.fromObject(jsonString);
					String errorcd = (String) jobj.get("errorcd");
					String errormsg = (String) jobj.get("errormsg");
					
					if (errorcd.equals("01")) {
						String succStr = "SUCCESS!!";
						
						JSONArray jarray = jobj.getJSONArray("list");
						
						String rtProdinc		= "";
						String rtBarcode  		= "";
						String rtCMProdinc 		= "";
						String rtCMOptioninc	= "";
						String rtVendorID		= "";
						String rtVendorNM		= "";
						String rtStatuscd 		= "";
						String rtMessage 		= "";
						
						for (int i = 0; i < jarray.size(); i++)
						{
							JSONObject rtList = jarray.getJSONObject(i);
							
							rtProdinc		= rtList.getString("prodinc");
							rtBarcode		= rtList.getString("barcode");
							rtCMProdinc		= rtList.getString("cmprodinc")+"_";	//커넥트미(CM)인 경우 끝에 "_" 를 붙여준다.
							rtCMOptioninc	= rtList.getString("cmoptioninc");
							rtVendorID		= rtList.getString("vendor_id");
							rtVendorNM		= rtList.getString("vendor_name");
							rtStatuscd 		= rtList.getString("statuscd");
							rtMessage 		= rtList.getString("msg");
							
							//전송대상 BARCODE 전송 결과 업데이트..
							if (pstmt4 != null) { pstmt4.close(); pstmt4 = null; }
							pstmt4 = conn.prepareStatement(sqlBuffer4.toString());
							
							pstmt4.setString(1, rtCMProdinc);
							pstmt4.setString(2, rtCMOptioninc);
							pstmt4.setString(3, rtStatuscd);
							pstmt4.setString(4, rtMessage);
							pstmt4.setString(5, rtProdinc);
							pstmt4.setString(6, rtBarcode);
							pstmt4.setString(7, rtVendorID);
							
							pstmt4.executeUpdate();
							
							if (rtStatuscd.equals("01")) {
								//전송대상 BARCODE 전송 결과가 "01" 성공이면 이전 데이타도 성공처리 업데이트..
								if (pstmt5 != null) { pstmt5.close(); pstmt5 = null; }
								pstmt5 = conn.prepareStatement(sqlBuffer5.toString());
								
								pstmt5.setString(1, rtStatuscd);
								pstmt5.setString(2, rtProdinc);
								pstmt5.setString(3, rtBarcode);
								pstmt5.setString(4, rtVendorID);
								pstmt5.setString(5, rtProdinc);
								pstmt5.setString(6, rtBarcode);
								pstmt5.setString(7, rtVendorID);
								
								pstmt5.executeUpdate();
							}
							
							//Logger.debug("prodinc="+rtProdinc+",barcode="+rtBarcode+",cmprodinc="+rtCMProdinc+",cmoptioninc="+rtCMOptioninc+",vendor_id="+rtVendorID+",vendor_name="+rtVendorNM+",statuscd="+rtStatuscd+",msg="+rtMessage);
						}
						
						
						sendMessage = succStr;
						
					} else {
						sendMessage = "ERROR : "+ errormsg;
					}
				 		
				} else {
					sendMessage = "ERROR : API Connection Fail!!";
				}
				
			}	
			else	//전송 DATA 없을때.. 
			{
				sendMessage = "NO DATA!! SUCCESS!!";
			}
			
			
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}
				if( rs3 !=null ) try{ rs3.close(); rs3 = null; }catch(Exception e){}finally{rs3 = null;}
				
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				if( pstmt3 != null ) try{ pstmt3.close(); pstmt3 = null; }catch(Exception e){}finally{pstmt3 = null;}
				if( pstmt4 != null ) try{ pstmt4.close(); pstmt4 = null; }catch(Exception e){}finally{pstmt4 = null;}
				if( pstmt5 != null ) try{ pstmt5.close(); pstmt5 = null; }catch(Exception e){}finally{pstmt5 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
	}
	
	public String CM_Api_RecvData(String dbmode, String command, String vendorID, String transCD, String sendDomain, String cmID, String cmPassKey) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.CM_Api_RecvData()";
		Logger.debug(methodName);
		
		Connection 			conn	= null;
		
		List<Object> vendorList 	= null;
		HashMap		 getHm	= new HashMap();
		
		String vendor_id	= "";
		String call_seq 	= "";
		
		String sendMessage 	= null;
		String succStr 		= "";
		
		BufferedReader br 		= null;
		String url 				= "";	//SEND URL
		String cmDataType 		= "json";
		String cmOrdInqType 	= "2";	//주문조회타입(1:주문번호, 2:기간)
		String cmOrdCd 			= "";	//CM주문번호
		String cmOrdSeq			= "";	//CM주문순번
		String cmStartDate 		= "";	//주문조회 시작일
		String cmEndDate 		= "";	//주문조회 종료일
		String cmCollectState 	= "2";	//수집상태(1:수집, 2:미수집)
		String cmVersion 		= "v1";
		
		if (command.equals("OrderRetrieve")) {						//주문정보(주문조회)
			url = sendDomain +"/proc/order/findOrderList.api";
		} else if (command.equals("OrderReturnRetrieve")) {			//반품정보(반품조회)
			url = sendDomain +"/proc/order/returnOrderList.api";
		} else if (command.equals("OrderCancelRetrieve")) {			//주문취소정보(주문취소조회)
			url = sendDomain +"/proc/order/cancelOrderList.api";
		} else if (command.equals("OrderReturnCancelRetrieve")) {	//반품취소정보(반품취소조회)
			url = sendDomain +"/proc/order/findReturnCancel.api";
		}
		
		PostMethod post = new PostMethod(url);
		
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO 	dao = new ServiceDAO();
			
			vendorList = GetVendorList(dbmode,transCD);
			
			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
			Date nowDay = new Date();
			String toDay = sdFormat.format(nowDay);
			
			
			if (vendorList != null) {
				
				for (int i = 0; i < vendorList.size(); i++) {
					getHm = (HashMap)vendorList.get(i);
					
					vendor_id 	= (String)getHm.get("VENDOR_ID");
					//cmStartDate = (String)getHm.get("STA_DT");
					cmStartDate = "20140101";
					cmEndDate 	= (String)getHm.get("END_DT");
					

					int conTimeOUt 	= 120000;
					int soTimeOut 	= 120000;
					int idleTimeout = 120000;
					
					MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
					HttpClient httpClient = new HttpClient(connectionManager);
					httpClient.getParams().setParameter("http.protocol.expect-continue", false);
					httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
					httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
					httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
					httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
					connectionManager.closeIdleConnections(idleTimeout);
					connectionManager.getParams().setMaxTotalConnections(100);
					
					
					//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
					post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
					post.addParameter("id", cmID);
					post.addParameter("auth_code", cmPassKey);
					post.addParameter("data_type", cmDataType);
					post.addParameter("vendor_id", vendor_id);
					post.addParameter("order_inquiry_type", cmOrdInqType);
					post.addParameter("cm_order_code", cmOrdCd);
					post.addParameter("cm_order_seq", cmOrdSeq);
					post.addParameter("start_date", cmStartDate);
					post.addParameter("end_date", cmEndDate);
					post.addParameter("collect_state", cmCollectState);
					post.addParameter("version", cmVersion);
					
					int resultCode = httpClient.executeMethod(post);
					
					
					if (String.valueOf(resultCode).equals("200")) {
						
						br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
						String retString = "";
						String line;
						
						while ((line = br.readLine()) != null) {
							retString += line;
						}
						br.close();
						
						String jsonString = URLDecoder.decode(retString,"UTF-8");
						//Logger.debug("jsonString="+jsonString);
						
						JSONObject jobj = JSONObject.fromObject(jsonString);
						String errorcd = (String) jobj.get("result");
						String errormsg = (String) jobj.get("result_text");
						
						if (errorcd.equals("0000")) {
							
							String rtFirstOrderId	= "";
							String rtOrderId		= "";
							String rtOrderSeq		= "";
							String rtOrderDt		= "";
							String rtChangeGb		= "";
							String rtReceiptNm		= "";
							String rtReceiptTel		= "";
							String rtReceiptHp		= "";
							String rtReceiptZipcode	= "";
							String rtReceiptAddr1	= "";
							String rtReceiptAddr2	= "";
							String rtCustNm			= "";
							String rtCustEmail		= "";
							String rtCustTel		= "";
							String rtCustHp			= "";
							String rtCustZipcode	= "";
							String rtCustAddr1		= "";
							String rtCustAddr2		= "";
							String rtDeliveryMsg	= "";
							String rtItemCd			= "";
							String rtItemNm			= "";
							String rtOption1		= "";
							String rtOption2		= "";
							String rtSalePrice		= "";
							String rtDeliPrice		= "";
							String rtQty			= "";
							String rtShipID			= "";
							String rtCancelDt		= "";
							String rtShipStatus 	= "";
							String rtShipSeq 		= "";
							String rtDeliGb 		= "";
							String rtRetCode 		= "";
							String rtOriShipId 		= "";
							String rtVendorId		= "";
							String rtOrdDetailCode	= "";
							String rtClameMemo		= "";
							String rtCubeItem		= "";
							String rtDosCd			= "";
							String rtDosNm			= "";
							String rtSmOrdId		= "";
							
							SimpleDateFormat tdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
							String rtTransDt = tdFormat.format(nowDay);
							
							call_seq = cubeDao.setRecvLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), vendor_id, toDay, toDay, "000", errormsg, transCD);
							
							ServiceDataInfo dInfo  = new ServiceDataInfo();
							
							JSONArray jarray = jobj.getJSONArray("order_list");
							
							for (int j = 0; j < jarray.size(); j++)
							{
								JSONObject rtList = jarray.getJSONObject(j);
								
								
								if (command.equals("OrderRetrieve")) {	//주문..
									
									rtOrderId			= rtList.getString("dos_order_code");
									rtOrderSeq			= rtList.getString("cm_order_seq");
									rtDosCd				= rtList.getString("dos_code");
									rtDosNm				= rtList.getString("dos_name");
									rtSmOrdId			= rtList.getString("cm_order_code");
									
									if (rtList.getString("cm_ship_id").equals("")) {
										rtShipID		= rtOrderId + rtOrderSeq;
									} else {
										rtShipID		= rtList.getString("cm_ship_id");
									}
									
									rtChangeGb			= rtList.getString("exchange_yn");	//11:일반주문, 12:교환주문
									
									if (rtList.getString("dos_first_order_code").equals("")) {
										rtFirstOrderId	= rtList.getString("cm_order_code");
									} else {
										if (rtChangeGb.equals("11")) {
											rtFirstOrderId	= rtList.getString("cm_order_code");
										} else {
											rtFirstOrderId	= rtList.getString("dos_first_order_code");
										}
									}
									
									rtShipSeq 			= rtList.getString("cm_order_seq");
									
									rtOrderDt			= rtList.getString("order_datetime").substring(0,8);
									rtShipStatus		= rtList.getString("order_state");
									rtQty				= rtList.getString("quantity");
									rtSalePrice			= rtList.getString("order_price");
									rtItemCd			= rtList.getString("cm_product_code");
									rtCubeItem			= rtList.getString("cube_barcode");
									rtItemNm			= rtList.getString("product_name");
									rtCustNm			= rtList.getString("order_name");
									rtCustTel			= rtList.getString("order_tel");
									rtCustHp			= rtList.getString("order_cell");
									rtCustEmail			= rtList.getString("order_email");
									rtReceiptNm			= rtList.getString("receive_name");
									rtReceiptTel		= rtList.getString("receive_tel");
									rtReceiptHp			= rtList.getString("receive_cell");
									rtReceiptZipcode	= rtList.getString("receive_zipcode");
									rtReceiptAddr1		= rtList.getString("receive_addr");
									rtReceiptAddr2		= rtList.getString("receive_addr2");
									rtDeliveryMsg		= rtList.getString("delivery_msg");
									rtVendorId			= rtList.getString("vendor_id");
									
								} else if (command.equals("OrderReturnRetrieve")) {		//반품..
									rtFirstOrderId		= rtList.getString("cm_order_code");
									rtOrderId			= rtList.getString("exchange_order_code");
									rtOrderSeq			= rtList.getString("cm_order_seq");
									rtDosCd				= rtList.getString("dos_code");
									rtDosNm				= rtList.getString("dos_name");
									rtSmOrdId			= rtList.getString("dos_order_code");
									
									if (rtList.getString("cm_ship_id").equals("")) {
										rtShipID		= rtOrderId + rtOrderSeq;
									} else {
										rtShipID		= rtList.getString("cm_ship_id");
									}
									
									rtChangeGb			= rtList.getString("exchange_yn");
									rtOrderDt			= rtList.getString("claim_request_datetime").substring(0,8);
									rtQty				= "-1";
									rtClameMemo			= rtList.getString("return_detail_reason");
									rtRetCode			= rtList.getString("return_reason_code");
									rtItemCd			= rtList.getString("cm_product_code");
									rtCubeItem			= rtList.getString("cube_barcode");
									rtItemNm			= rtList.getString("product_name");
									rtCustNm			= rtList.getString("collect_name");
									rtCustTel			= rtList.getString("collect_tel1");
									rtCustHp			= rtList.getString("collect_tel2");
									rtCustAddr1			= rtList.getString("collect_addr");
									rtCustAddr2			= rtList.getString("collect_addr2");
									rtReceiptNm			= rtList.getString("collect_name");
									rtReceiptTel		= rtList.getString("collect_tel1");
									rtReceiptHp			= rtList.getString("collect_tel2");
									rtReceiptZipcode	= rtList.getString("collect_zipcode");
									rtReceiptAddr1		= rtList.getString("collect_addr");
									rtReceiptAddr2		= rtList.getString("collect_addr2");
									rtVendorId			= rtList.getString("vendor_id");
									
								} else if (command.equals("OrderCancelRetrieve")) {		//주문취소..
									rtFirstOrderId		= rtList.getString("cm_order_code");
									rtOrderId			= rtList.getString("cm_order_code");
									rtOrderSeq			= rtList.getString("cm_order_seq");
									rtDosCd				= rtList.getString("dos_code");
									rtDosNm				= rtList.getString("dos_name");
									rtSmOrdId			= rtList.getString("dos_order_code");
									
									if (rtList.getString("cm_ship_id").equals("")) {
										rtShipID		= rtOrderId + rtOrderSeq;
									} else {
										rtShipID		= rtList.getString("cm_ship_id");
									}
									
									rtClameMemo			= rtList.getString("cancel_detail_reason");
									rtRetCode			= rtList.getString("cancel_reason_code");
									rtCancelDt			= rtList.getString("claim_request_datetime").substring(0,8);
									rtItemCd			= rtList.getString("cm_product_code");
									rtCubeItem			= rtList.getString("cube_barcode");
									rtItemNm			= rtList.getString("product_name");
									rtVendorId			= rtList.getString("vendor_id");
									
								} else if (command.equals("OrderReturnCancelRetrieve")) {	//반품취소..
									rtFirstOrderId		= rtList.getString("cm_order_code");
									rtOrderId			= rtList.getString("cm_order_code");
									rtOrderSeq			= rtList.getString("cm_order_seq");
									rtDosCd				= rtList.getString("dos_code");
									rtDosNm				= rtList.getString("dos_name");
									rtSmOrdId			= rtList.getString("dos_order_code");
									
									if (rtList.getString("cm_ship_id").equals("")) {
										rtShipID		= rtOrderId + rtOrderSeq;
									} else {
										rtShipID		= rtList.getString("cm_ship_id");
									}
								
									rtChangeGb			= rtList.getString("exchange_yn");
									rtCancelDt			= rtList.getString("claim_request_datetime").substring(0,8);
									rtItemCd			= rtList.getString("cm_product_code");
									rtCubeItem			= rtList.getString("cube_barcode");
									rtItemNm			= rtList.getString("product_name");
									rtVendorId			= rtList.getString("vendor_id");
								}
								
								
								dInfo.setCall_dt(CommonUtil.getCurrentDate());
								dInfo.setCall_seq(call_seq);
								dInfo.setVendor_id(rtVendorId);
								dInfo.setInuser("CMAPI");
								dInfo.setError_code("00");
								dInfo.setError_msg(errormsg);
								dInfo.setSeq(String.valueOf(j+1));
								dInfo.setRecv_gb(cubeDao.getRecvGb(command)); // 10.주문, 20.주문취소, 30.반품, 40.반품취소
								dInfo.setFirst_order_id(StringUtil.nullTo(rtFirstOrderId, "")); 
								dInfo.setOrder_id(StringUtil.nullTo(rtOrderId, ""));
								dInfo.setShip_id(StringUtil.nullTo(rtShipID, ""));         
								dInfo.setTrans_dt(StringUtil.nullTo(rtTransDt, ""));   
								dInfo.setCancel_dt(StringUtil.nullTo(rtCancelDt, ""));
								dInfo.setInstruct_dt(StringUtil.nullTo(rtOrderDt, ""));
								dInfo.setChange_gb(StringUtil.nullTo(rtChangeGb, ""));
								dInfo.setShip_status(StringUtil.nullTo(rtShipStatus, ""));     
								dInfo.setReceipt_nm(StringUtil.nullTo(rtReceiptNm, ""));      
								dInfo.setReceipt_tel(StringUtil.nullTo(rtReceiptTel, ""));     
								dInfo.setReceipt_hp(StringUtil.nullTo(rtReceiptHp, ""));      
								dInfo.setReceipt_zipcode(StringUtil.nullTo(rtReceiptZipcode, "")); 
								dInfo.setReceipt_addr1(StringUtil.nullTo(rtReceiptAddr1, ""));   
								dInfo.setReceipt_addr2(StringUtil.nullTo(rtReceiptAddr2, ""));   
								dInfo.setCust_nm(StringUtil.nullTo(rtCustNm, ""));         
								dInfo.setCust_tel(StringUtil.nullTo(rtCustTel, ""));        
								dInfo.setCust_hp(StringUtil.nullTo(rtCustHp, ""));         
								dInfo.setCust_zipcode(StringUtil.nullTo(rtCustZipcode, ""));    
								dInfo.setCust_addr1(StringUtil.nullTo(rtCustAddr1, ""));      
								dInfo.setCust_addr2(StringUtil.nullTo(rtCustAddr2, ""));      
								dInfo.setDelivery_msg(StringUtil.nullTo(rtDeliveryMsg, ""));    
								dInfo.setOrder_seq(StringUtil.nullTo(rtOrderSeq, ""));       
								dInfo.setShip_seq(StringUtil.nullTo(rtShipSeq, ""));        
								dInfo.setItem_cd(StringUtil.nullTo(rtItemCd, ""));         
								dInfo.setItem_nm(StringUtil.nullTo(rtItemNm, ""));         
								dInfo.setOption1(StringUtil.nullTo(rtOption1, ""));         
								dInfo.setOption2(StringUtil.nullTo(rtOption2, ""));         
								dInfo.setDeli_gb(StringUtil.nullTo(rtDeliGb, ""));
								dInfo.setRet_code(StringUtil.nullTo(rtRetCode, ""));
								dInfo.setDeli_price(StringUtil.nullTo(rtDeliPrice, "0"));
								dInfo.setSale_price(StringUtil.nullTo(rtSalePrice, "0"));
								dInfo.setOri_ship_id(StringUtil.nullTo(rtOriShipId, ""));
								dInfo.setQty(StringUtil.nullTo(rtQty, "0"));
								dInfo.setCust_email(StringUtil.nullTo(rtCustEmail, ""));
								dInfo.setClame_memo(StringUtil.nullTo(rtClameMemo, ""));
								dInfo.setCube_item(StringUtil.nullTo(rtCubeItem, ""));
								//dInfo.setDos_code(StringUtil.nullTo(rtDosCd, ""));
								//dInfo.setDos_name(StringUtil.nullTo(rtDosNm, ""));
								//dInfo.setSm_order_id(StringUtil.nullTo(rtSmOrdId, ""));
								
								dao.setRecvData(conn, dInfo, transCD);
								
							}
	
							succStr = "SUCCESS!!";
							
							cubeDao.callProcedure(dbmode, command, CommonUtil.getCurrentDate(), call_seq, sendDomain, "", "", transCD, cmID, cmPassKey);
							
						} else {
							succStr = "ERROR : "+ errormsg;
							
							cubeDao.setRecvLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), vendor_id, toDay, toDay, "100", succStr, transCD);
						}
			
					} else {
						succStr = "ERROR : API Connection Fail";
						
						cubeDao.setRecvLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), vendor_id, toDay, toDay, "100", succStr, transCD);
					}
				
					
					//parameter 삭제 초기화..
					post.removeParameter("id");
					post.removeParameter("auth_code");
					post.removeParameter("data_type");
					post.removeParameter("vendor_id");
					post.removeParameter("order_inquiry_type");
					post.removeParameter("cm_order_code");
					post.removeParameter("cm_order_seq");
					post.removeParameter("start_date");
					post.removeParameter("end_date");
					post.removeParameter("collect_state");
					post.removeParameter("version");
					
					post.releaseConnection();
					
				}
				
			} else {
				succStr = "NO DATA!! 조회 된 벤더ID 가 없습니다.";
			}
			
			sendMessage = succStr;
			
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
		
	}
	
	public String CM_Api_Refuse(String dbmode, String command, String vendorID, String transCD, String sendDomain, String cmID, String cmPassKey) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.CM_Api_Refuse()";
		Logger.debug(methodName);
		
		Connection 			conn		= null;
		PreparedStatement	pstmt		= null;
		PreparedStatement	pstmt2		= null;
		ResultSet			rs			= null;
		StringBuffer   		sqlBuffer  	= new StringBuffer(500);	//서브 쿼리문
		StringBuffer   		sqlBuffer2  = new StringBuffer(500);	//UPDATE 쿼리문
		
		List<Object> getOrderSendData 	= null;
		Map<String, String> vMap 		= null;
		
		String 	sendMessage 	= null;
		
		String vendor_id 		= ""; 	//거래처코드
		String po_id 	 		= "";  	//출고번호
		String po_seq    		= "";  	//출고순번
		String cancel_dt   		= "";  	//취소일자
		
		String ship_id			= "";
		String apply_dt			= "";
		String apply_time		= "";
		String deli_company_id	= "";
		String bl_no			= "";
		String error_code		= "100";
		String message			= "연동처리 대기";
		String cancel_code		= "";
		
		String call_dt 	 		= "";
		String call_seq  		= "";
		
		BufferedReader br 		= null;
		String url 				= "";	//SEND URL
		String cmDataType 		= "json";
		String cmOrdDetailCode 	= "";	//주문상품코드
		String cmVersion 		= "v1";
		
		if (command.equals("SoldOutCancel")) {			//품절취소
			url = sendDomain +"/proc/order/soldoutCancel.api";
		} else if (command.equals("ReturnRefuse")) {	//반품거부
			url = sendDomain +"/proc/order/returnCancel.api";
		}
			
		PostMethod post = new PostMethod(url);
		
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO dao = new ServiceDAO();

			sqlBuffer.append("SELECT  /hint*+ INDEX_DESC(CALL_DT,CALL_SEQ) *hint/						 		");
			sqlBuffer.append("		  LTRIM(TO_CHAR(TO_NUMBER(NVL(MAX(CALL_SEQ), 0)) + 1, '0000')) as CALL_SEQ 	");
			sqlBuffer.append("FROM 	  API_SEND_LOG															 	");
			sqlBuffer.append("WHERE   CALL_DT = TO_CHAR(SYSDATE,'YYYYMMDD') 						 			");
			pstmt = conn.prepareStatement(sqlBuffer.toString());
			
			sqlBuffer2.append("UPDATE  API_SEND_LOG					");
			sqlBuffer2.append("		   SET RESULT_CODE  = ? 		");
			sqlBuffer2.append("			   ,RESULT_NAME = ?			");
			sqlBuffer2.append("WHERE   CALL_DT  = ?					");
			sqlBuffer2.append("AND     CALL_SEQ = ? 				");
			sqlBuffer2.append("AND     VENDOR_ID = ? 				");
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
			
			
			call_dt = CommonUtil.getCurrentDate();
			
			getOrderSendData = dao.getOrderSendData(conn, command, call_dt, call_seq, transCD);
			
			if (getOrderSendData.size() > 0) {
				
				JSONObject jsonObject = new JSONObject();
				JSONArray cell = new JSONArray();
				
				for (int i = 0; i < getOrderSendData.size(); i++) {
					vMap = (HashMap<String, String>) getOrderSendData.get(i);
					
					vendor_id 	= vMap.get("VENDOR_ID");
					po_id 		= vMap.get("PONO").trim();
					po_seq 		= vMap.get("ORDER_SEQ").trim();
					cancel_dt 	= vMap.get("CANCEL_DT");
					
					if (command.equals("SoldOutCancel")) {			//품절취소
						cancel_code = vMap.get("CANCEL_CODE");
					} else if (command.equals("ReturnRefuse")) {	//반품거부
						cancel_code = "";
					}
					
					ship_id = po_id + po_seq;
					
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						call_seq = rs.getString("CALL_SEQ");
					}
					rs.close();
					//Logger.debug("call_seq="+call_seq);
					
					JSONObject asrrotList = new JSONObject();
					
					asrrotList.put("vendor_id", vendor_id);
					asrrotList.put("call_date", call_dt);
					asrrotList.put("call_seq", call_seq);
					asrrotList.put("cm_order_code", po_id);
					asrrotList.put("cm_order_seq", po_seq);
					
					cell.add(asrrotList);
					
					cubeDao.setSendLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), vendor_id, ship_id, apply_dt, apply_time, deli_company_id, bl_no, error_code, message, cancel_code, transCD);
					
				}
				
				jsonObject.put("order_list", cell);
				//Logger.debug("jsonStr="+jsonObject.toString());
				
				
				int conTimeOUt 	= 120000;
				int soTimeOut 	= 120000;
				int idleTimeout = 120000;
				
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				HttpClient httpClient = new HttpClient(connectionManager);
				httpClient.getParams().setParameter("http.protocol.expect-continue", false);
				httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
				httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
				httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
				httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
				connectionManager.closeIdleConnections(idleTimeout);
				connectionManager.getParams().setMaxTotalConnections(100);
				
				
				//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
				post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.addParameter("id", cmID);
				post.addParameter("auth_code", cmPassKey);
				post.addParameter("data_type", cmDataType);
				post.addParameter("version", cmVersion);
				post.addParameter("json_data", jsonObject.toString());
				
				int resultCode = httpClient.executeMethod(post);
				
				
				if (String.valueOf(resultCode).equals("200")) {
					
					br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
					String retString = "";
					String line;
					
					while ((line = br.readLine()) != null) {
						retString += line;
					}
					br.close();
					
					String jsonString = URLDecoder.decode(retString,"UTF-8");
					
					JSONObject jobj = JSONObject.fromObject(jsonString);
					String errorcd = (String) jobj.get("result");
					String errormsg = (String) jobj.get("result_text");
					
					if (errorcd.equals("0000")) {
						String succStr = "SUCCESS!!";
						
						String rtVendorId	= "";
						String rtCallDt 	= "";
						String rtCallSeq 	= "";
						String rtCmOrdCd 	= "";
						String rtCmOrdSeq 	= "";
						String rtResultCd  	= "";
						String rtResultMsg  = "";
						
						JSONArray jarray = jobj.getJSONArray("order_list");
						
						for (int i = 0; i < jarray.size(); i++)
						{
							JSONObject rtList = jarray.getJSONObject(i);
							
							
							rtVendorId	= rtList.getString("vendor_id");
							rtCallDt 	= rtList.getString("call_date");
							rtCallSeq 	= rtList.getString("call_seq");
							rtCmOrdCd 	= rtList.getString("cm_order_code");
							rtCmOrdSeq 	= rtList.getString("cm_order_seq");
							rtResultCd	= rtList.getString("result_code");
							rtResultMsg = rtList.getString("result_message");
							
							if (rtResultCd.equals("0000")) {
								rtResultCd = "000";		//정상
							} else {
								rtResultCd = "100";		//에러
							}
							
							//전송대상 BARCODE 전송 결과 업데이트..
							if (pstmt2 != null) { pstmt2.close(); pstmt2 = null; }
							pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
							
							pstmt2.setString(1, rtResultCd);
							pstmt2.setString(2, rtResultMsg);
							pstmt2.setString(3, rtCallDt);
							pstmt2.setString(4, rtCallSeq);
							pstmt2.setString(5, rtVendorId);
							pstmt2.executeUpdate();
							
							
							//Logger.debug("vendorId="+rtVendorId+",resultCode="+rtResultCd+",resultMsg="+rtResultMsg+",callDt="+rtCallDt+",callSeq="+rtCallSeq);
						}
						
						sendMessage = succStr;
						
					} else {
						sendMessage = "ERROR : "+ errormsg;
					}
					
					
				} else {
					sendMessage = "ERROR : API Connection Fail!!";
					
					cubeDao.setSendLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "100", sendMessage, "00", transCD);
				}
					
			} else {
				sendMessage = "연동할 대상 정보가 없습니다.";
				
				cubeDao.setSendLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "100", sendMessage, "00", transCD);
			}
		
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
	}
	
	public String CM_Api_SendData(String dbmode, String command, String vendorID, String transCD, String sendDomain, String cmID, String cmPassKey) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.CM_Api_SendData()";
		Logger.debug(methodName);
		
		Connection 			conn		= null;
		
		List<Object> getOrderSendData 	= null;
		Map<String, String> vMap 		= null;
		
		String 	sendMessage 	= null;
		
		String error_code		= "";
		String message 			= "";
		
		String vendor_id 		= ""; 	//거래처코드
		String cancel_code		= "";
		String apply_dt			= "";
		String apply_time		= "";
		
		String call_dt 	 		= "";
		String call_seq  		= "";
		
		BufferedReader br 		= null;
		String url 				= "";	//SEND URL
		String cmDataType 		= "json";
		String cmOrdCd 			= "";	//CM주문번호
		String cmOrdSeq			= "";	//CM주문순번
		String cmApplyDate		= "";	//출고일
		String cmApplyTime		= "";	//출고시각
		String cmPickupDate		= "";	//회수일
		String cmPickupTime		= "";	//회수시각
		String cmDeliveryCode	= "";	//택배사코드
		String cmInvoiceNo		= "";	//송장번호
		String cmVersion 		= "v1";
		
		if (command.equals("DeliveryInsert")) {				//배송정보등록
			url = sendDomain +"/proc/order/updateInvoice.api";
		} else if (command.equals("ReturnPickUpInsert")) {	//반품수거등록(반품 완료 처리)
			url = sendDomain +"/proc/order/approveReturn.api";
		}
		
		PostMethod post = new PostMethod(url);
		
		
		try {
			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO dao = new ServiceDAO();

			getOrderSendData = dao.getOrderSendData(conn, command, call_dt, call_seq, transCD);
			
			if (getOrderSendData.size() > 0) {
				
				for (int i = 0; i < getOrderSendData.size(); i++) {
					vMap = (HashMap<String, String>) getOrderSendData.get(i);
					
					vendor_id 	= vMap.get("VENDOR_ID");
					cmOrdCd 	= vMap.get("PONO");
					cmOrdSeq 	= vMap.get("ORDER_SEQ");
					
					if (command.equals("DeliveryInsert")) {				//배송정보등록
						cmApplyDate = vMap.get("OUTDT");
						cmApplyTime = vMap.get("UPDTIME");
						
						apply_dt	= cmApplyDate;
						apply_time	= cmApplyTime;
						
					} else if (command.equals("ReturnPickUpInsert")) {	//반품수거등록
						cmPickupDate = vMap.get("APPLY_DT");
						cmPickupTime = vMap.get("APPLY_TIME");
						
						apply_dt	= cmPickupDate;
						apply_time	= cmPickupTime;
					}
					
					cmInvoiceNo= vMap.get("EXPNM");
					cmDeliveryCode = vMap.get("EXPNO");
					
					int conTimeOUt 	= 120000;
					int soTimeOut 	= 120000;
					int idleTimeout = 120000;
					
					MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
					HttpClient httpClient = new HttpClient(connectionManager);
					httpClient.getParams().setParameter("http.protocol.expect-continue", false);
					httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
					httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
					httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
					httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
					connectionManager.closeIdleConnections(idleTimeout);
					connectionManager.getParams().setMaxTotalConnections(100);
					
					
					post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
					post.addParameter("id", cmID);
					post.addParameter("auth_code", cmPassKey);
					post.addParameter("data_type", cmDataType);
					post.addParameter("vendor_id", vendor_id);
					post.addParameter("cm_order_code", cmOrdCd);
					post.addParameter("cm_order_seq", cmOrdSeq);
					
					if (command.equals("DeliveryInsert")) {				//배송정보등록
						post.addParameter("apply_date", apply_dt);
						post.addParameter("apply_time", apply_time);
					} else if (command.equals("ReturnPickUpInsert")) {	//반품수거등록
						post.addParameter("pickup_date", apply_dt);
						post.addParameter("pickup_time", apply_time);
					}
					
					post.addParameter("delivery_code", cmDeliveryCode);
					post.addParameter("invoice_no", cmInvoiceNo);
					post.addParameter("version", cmVersion);
					
					int resultCode = httpClient.executeMethod(post);
					
					
					if (String.valueOf(resultCode).equals("200")) {
						br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
						String retString = "";
						String line;
						
						while ((line = br.readLine()) != null) {
							retString += line;
						}
						br.close();
						
						String jsonString = URLDecoder.decode(retString,"UTF-8");
						
						JSONObject jobj = JSONObject.fromObject(jsonString);
						String errorcd = (String) jobj.get("result");
						String errormsg = (String) jobj.get("result_text");
						
						if (errorcd.equals("0000")) {
							String succStr = "SUCCESS!!";
							
							String rtVendorId	= "";
							String rtCmOrdCd 	= "";
							String rtCmOrdSeq 	= "";
							
							rtVendorId	= (String) jobj.get("vendor_id");
							rtCmOrdCd	= (String) jobj.get("cm_order_code");
							rtCmOrdSeq 	= (String) jobj.get("cm_order_seq");
							
							sendMessage = succStr;
							
						} else {
							sendMessage = "ERROR : "+ errormsg;
						}
						
						if (errorcd.equals("0000")) {
							error_code = "000";
						} else { 
							error_code = "100";
						}
						message	= errormsg;
						
						cubeDao.setSendLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), vendor_id, cmOrdCd+cmOrdSeq, apply_dt, apply_time, cmDeliveryCode, cmInvoiceNo, error_code, message, cancel_code, transCD);
						
					} else {
						sendMessage = "ERROR : API Connection Fail!!";
						
						cubeDao.setSendLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "100", sendMessage, "00", transCD);
					}
				
					
					//parameter 삭제 초기화..
					post.removeParameter("id");
					post.removeParameter("auth_code");
					post.removeParameter("data_type");
					post.removeParameter("vendor_id");
					post.removeParameter("cm_order_code");
					post.removeParameter("cm_order_seq");
					
					if (command.equals("DeliveryInsert")) {				//배송정보등록
						post.removeParameter("apply_date");
						post.removeParameter("apply_time");
					} else if (command.equals("ReturnPickUpInsert")) {	//반품수거등록
						post.removeParameter("pickup_date");
						post.removeParameter("pickup_time");
					}
					
					post.removeParameter("delivery_code");
					post.removeParameter("invoice_no");
					post.removeParameter("version");
					
					post.releaseConnection();
				}
			
			} else {
				sendMessage = "연동할 대상 정보가 없습니다.";
				
				cubeDao.setSendLog(dbmode, "CMAPI", command, cubeDao.getApiName(command), "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "100", sendMessage, "00", transCD);
			}
			
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
	}
	
	public String CM_Api_ItemStock(String dbmode, String command, String vendorID, String transCD, String sendDomain, String cmID, String cmPassKey) throws SQLException, IOException, JSONException
	{
		String methodName ="com.service.CubeApiCreateJSON.CM_Api_ItemStock()";
		Logger.debug(methodName);
		
		Connection 			conn		= null;
		CallableStatement 	cstmt    	= null;
		PreparedStatement	pstmt		= null;
		PreparedStatement	pstmt2		= null;
		PreparedStatement	pstmt3		= null;
		PreparedStatement	pstmt4		= null;
		ResultSet			rs			= null;
		ResultSet			rs2			= null;
		StringBuffer   		sqlBuffer  	= new StringBuffer(500);	//쿼리문
		StringBuffer   		sqlBuffer2  = new StringBuffer(500);	//쿼리문
		StringBuffer   		sqlBuffer3  = new StringBuffer(500);	//쿼리문
		StringBuffer   		sqlBuffer4  = new StringBuffer(500);	//쿼리문
		
		
		int cnt 	= 0;
		String 	sendMessage 	= null;
		
		BufferedReader br 		= null;
		String url = sendDomain +"/proc/product/productStock.api";	//SEND URL
		PostMethod post = new PostMethod(url);
		
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
		

			sqlBuffer.append("SELECT  VENDOR_ID           	\n");
			sqlBuffer.append("        ,BARCODE            	\n");
			sqlBuffer.append("        ,ASSORT_ID          	\n");
			sqlBuffer.append("        ,ITEM_ID            	\n");
			sqlBuffer.append("        ,STOCK              	\n");
			sqlBuffer.append("FROM    TBD260              	\n");
			sqlBuffer.append("WHERE   TRAN_DATE = ?			\n");
			sqlBuffer.append("AND     TRAN_SEQ  = ?        	\n");
			pstmt = conn.prepareStatement(sqlBuffer.toString());
			
			sqlBuffer2.append("SELECT  COUNT(1) AS CNT		\n");
			sqlBuffer2.append("FROM    TBD260              	\n");
			sqlBuffer2.append("WHERE   TRAN_DATE = ?		\n");
			sqlBuffer2.append("AND     TRAN_SEQ  = ?        \n");
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());
	   
			sqlBuffer3.append("UPDATE  TBD260              	\n");
			sqlBuffer3.append("    SET STATUS 	   = ?      \n");
			sqlBuffer3.append("        ,STATUS_MSG = ?      \n");
			sqlBuffer3.append("WHERE   TRAN_DATE = ?        \n");
			sqlBuffer3.append("AND     TRAN_SEQ	 = ?        \n");
			sqlBuffer3.append("AND     BARCODE 	 = ?        \n");
			sqlBuffer3.append("AND     ASSORT_ID = ?        \n");
			sqlBuffer3.append("AND     ITEM_ID 	 = ?        \n");
			pstmt3 = conn.prepareStatement(sqlBuffer3.toString());
			
			sqlBuffer4.append("UPDATE  TBD260              	\n");
			sqlBuffer4.append("    SET STATUS 	   = ?      \n");
			sqlBuffer4.append("        ,STATUS_MSG = ?      \n");
			sqlBuffer4.append("WHERE   TRAN_DATE = ?        \n");
			sqlBuffer4.append("AND     TRAN_SEQ	 = ?        \n");
			pstmt4 = conn.prepareStatement(sqlBuffer4.toString());
			
			
			
			//상품등록 프로시저 실행..
			cstmt = conn.prepareCall("{call P_SEND_STOCK(?, ?, ?, ?, ?)}");
			cstmt.registerOutParameter(1, Types.CHAR);
        	cstmt.registerOutParameter(2, Types.CHAR);
        	cstmt.registerOutParameter(3, Types.CHAR);
        	cstmt.registerOutParameter(4, Types.INTEGER);
        	cstmt.setString(5, transCD);
        	
        	cstmt.executeUpdate();
        	
        	String errcode = cstmt.getString(1);
        	String errmsg = cstmt.getString(2);	
            String tranDt  = cstmt.getString(3);
            int    tranSeq = cstmt.getInt(4);
            
            //Logger.debug("errcode="+errcode);
            //Logger.debug("errmsg="+errmsg);
            //Logger.debug("tranDt="+tranDt);
            //Logger.debug("tranSeq="+tranSeq);
            
            
            if (errcode.equals("00")) {
            	
            	pstmt2.setString(1, tranDt);
				pstmt2.setInt(2, tranSeq);
            	rs2 = pstmt2.executeQuery();
    			
    			if(rs2.next())
    			{
    				cnt = rs2.getInt("CNT");
    			}
            	
    			//전송 DATA 있을때..
    			if(cnt > 0)
    			{
    				pstmt.setString(1, tranDt);
    				pstmt.setInt(2, tranSeq);
    				rs = pstmt.executeQuery();
    				
    				
    				JSONObject jsonObject = new JSONObject();
    				JSONArray cell = new JSONArray();
    				
    				while(rs.next())
    				{
    					JSONObject asrrotList = new JSONObject();
    					
    					asrrotList.put("vendorId", rs.getString("VENDOR_ID"));
    					asrrotList.put("bar_code", rs.getString("BARCODE"));
    					asrrotList.put("cmprodinc", rs.getString("ASSORT_ID"));
    					asrrotList.put("cmoptioninc", rs.getString("ITEM_ID"));
    					asrrotList.put("stockQty", rs.getString("STOCK"));
    						
    					cell.add(asrrotList);
    				}
    				
    				jsonObject.put("list", cell);
    				//Logger.debug("jsonStr="+jsonObject.toString());
    				
    				
    				int conTimeOUt 	= 120000;
    				int soTimeOut 	= 120000;
    				int idleTimeout = 120000;
    				
    				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    				HttpClient httpClient = new HttpClient(connectionManager);
    				httpClient.getParams().setParameter("http.protocol.expect-continue", false);
    				httpClient.getParams().setParameter("http.connection.timeout", conTimeOUt);
    				httpClient.getParams().setParameter("http.socket.timeout", soTimeOut);
    				httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeOUt);
    				httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeOut);
    				connectionManager.closeIdleConnections(idleTimeout);
    				connectionManager.getParams().setMaxTotalConnections(100);
    				
    				
    				//post.setRequestHeader("Content-type", "application/json; charset=UTF-8");
    				post.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
    				post.addParameter("id", cmID);
    				post.addParameter("passkey", cmPassKey);
    				post.addParameter("data", jsonObject.toString());
    				
    				int resultCode = httpClient.executeMethod(post);
    				
    				
    				if (String.valueOf(resultCode).equals("200")) {
    					
    					br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
    					String retString = "";
    					String line;
    					
    					while ((line = br.readLine()) != null) {
    						retString += line;
    					}
    					br.close();
    					
    					String jsonString = URLDecoder.decode(retString,"UTF-8");
    					
    					JSONObject jobj = JSONObject.fromObject(jsonString);
    					String errorcd = (String) jobj.get("errorcd");
    					String errormsg = (String) jobj.get("errormsg");
    					
    					
    					if (errorcd.equals("01")) {
    						
    						JSONArray jarray = jobj.getJSONArray("list");
    						
    						String rtBarcode  		= "";
    						String rtCMProdinc  	= "";
    						String rtCMOptioninc	= "";
    						String rtStatuscd 		= "";
    						String rtMessage 		= "";
    						
    						for (int i = 0; i < jarray.size(); i++)
    						{
    							JSONObject rtList = jarray.getJSONObject(i);
    							
    							rtBarcode		= rtList.getString("barcode");
    							rtCMProdinc 	= rtList.getString("cmprodinc");
    							rtCMOptioninc 	= rtList.getString("cmoptioninc");
    							rtStatuscd 		= rtList.getString("statuscd");
    							rtMessage 		= rtList.getString("msg");
    							
    							//재고연동 전송 결과 업데이트..
    							if (pstmt3 != null) { pstmt3.close(); pstmt3 = null; }
    							pstmt3 = conn.prepareStatement(sqlBuffer3.toString());
    							
    							pstmt3.setString(1, rtStatuscd);
    							pstmt3.setString(2, rtMessage);
    							pstmt3.setString(3, tranDt);
    							pstmt3.setInt(4, tranSeq);
    							pstmt3.setString(5, rtBarcode);
    							pstmt3.setString(6, rtCMProdinc);
    							pstmt3.setString(7, rtCMOptioninc);
    							pstmt3.executeUpdate();
    						
    							//Logger.debug("tranDt="+tranDt+",tranSeq="+tranSeq+",barcode="+rtBarcode+",cmprodinc="+rtCMProdinc+",cmoptioninc="+rtCMOptioninc+",statuscd="+rtStatuscd+",message="+rtMessage);
    						}
    						
    						sendMessage = "SUCCESS!!";
    						
    					} else {
    						//연동 오류건 결과 내용 업데이트..
    						pstmt4.setString(1, errorcd);
    		            	pstmt4.setString(2, errormsg);
    		            	pstmt4.setString(3, tranDt);
    		            	pstmt4.setInt(4, tranSeq);
    		            	pstmt4.executeUpdate();
    						
    						sendMessage = "ERROR : "+ errormsg;
    					}
    					
    				} else {	
    					sendMessage = "ERROR : API Connection Fail!!";
    				}
    				
    			}
    			else	//전송 DATA 없을때.. 
    			{
    				sendMessage = "NO DATA!! SUCCESS!!";
    			}
    			
            } else {
            	sendMessage	= "Error : "+ errmsg;
            }
			
		} catch(SQLException e) {
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {
			post.releaseConnection();
			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}
				
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				if( pstmt3 != null ) try{ pstmt3.close(); pstmt3 = null; }catch(Exception e){}finally{pstmt3 = null;}
				if( pstmt4 != null ) try{ pstmt4.close(); pstmt4 = null; }catch(Exception e){}finally{pstmt4 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}	
		
		return sendMessage;
	}
	커넥트미(CM) API 연동 작업 부분 주석 처리.. 2014-03-06 */
	 
}
