package com.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.CallableStatement;
import java.sql.Connection;
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

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.service.command.connection.DataBaseManager;
import com.service.command.log.Logger;
import com.service.command.util.CommonUtil;
import com.service.command.util.StringUtil;
import com.service.dao.ServiceDAO;
import com.service.entity.ServiceDataInfo;
import com.service.entity.StoreRejectDataInfo;
/**
 * cube <> sc Redis 연동
 * @author 하윤식
 * @since 2015.03.17
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *     수정일     수정자          수정내용
 *  -----------  --------    ---------------------------
 *   2015.03.17   하윤식          최초 생성 
 *
 * </pre>
 */

public class ScApiCreateREDIS {
	
	private static ScApiCreateREDIS instance = new ScApiCreateREDIS();
	
	/* 연동 REDIS KEY */
	private static String  SEND_PRODUCT_KEY 	= ":product:C2S"; 		// 상품 등록/수정 송신
	private static String  RECV_PRODUCT_KEY 	= ":product:S2C"; 		// 상품 등록/수정 결과 수신
	private static String  SEND_INVENTORY_KEY 	= ":inventory:C2S"; 	// 재고 송신
	private static String  RECV_INVENTORY_KEY 	= ":inventory:S2C"; 	// 재고 처리결과 수신	
	private static String  RECV_ORDER 			= ":order:update:S2C"; 	// 출고의뢰 수신
	private static String  SEND_ORDER 			= ":order:update:C2S"; 	// 출고의뢰 결과 송신
	private static String  RECV_ORDER_RETURN 	= ":return:update:S2C"; // 반품/반품취소 수신
	private static String  SEND_ORDER_RETURN 	= ":return:update:C2S"; // 반품/반품취소 결과 송신		
	private static String  ORDER_RETURN_ERROR 	= ":return:error"; 		// 반품의뢰 에러 
	private static String  ORDER_ERROR 			= ":order:error"; 		// 출고의뢰 에러 	
	private static String  SEND_DELIVERY 		= ":order:update:C2S"; 	// 출고확정 송신
	private static String  ORDER_RETURN_CONFIRM = ":return:update:C2S"; 	// 출고확정 송신
	private static String  ORDER_STORE_REJECT	= ":order:store:reject:C2S"; // 매장출고거부 수신( CUBE -> SC)

	/* REDIS DB IP 운영서버 */   	
	//private static String  RED_IP	= "220.117.243.18";
	
	/* REDIS DB IP 테스트서버 */
//	private static String  RED_IP 	= "1.214.91.21";

	/*망내 테스트*/
	private static String  RED_IP 	= "192.168.10.66";
	
	
	
	private static int DB_INDEX 	= 1;
	private static int PORT    		= 6379;

	
	/* 연동상태코드 */
	private static String  RECV_ORDER_STATUS 				= "3200"; 		// 주문 요청상태
	private static String  SEND_ORDER_STATUS 				= "3202"; 		// 주문 요청결과상태	
	private static String  RECV_ORDER_CANCE_STATUS 			= "9000"; 		// 주문취소 요청상태
	private static String  SEND_ORDER_CANCE_STATUS 			= "9002"; 		// 주문취소 요청상태
	private static String  SEND_DELIVERY_STATUS 			= "3700"; 		// 출고확정
	
	private static String  RECV_ORDER_RETURN_STATUS 		= "3200"; 		// 반품 요청상태
	private static String  RECV_ORDER_RETURN_CANCE_STATUS 	= "9000"; 		// 반품취소 요청상태
	private static String  SEND_ORDER_RETURN_STATUS 		= "3202"; 		// 반품 요청결과상태		
	private static String  SEND_ORDER_RETURN_CANCE_STATUS 	= "9002"; 		// 반품취소 요청상태
	
	public static ScApiCreateREDIS getInstance()
	{
		return instance;
	}

	private ScApiCreateREDIS() {
		
	}
	
	/**
	 * 판매처코드 조회
	 * @param dbmode
	 * @param transCD
	 * @return
	 * @throws IOException
	 */
	public List<Object> GetVendorList(String dbmode, String transCD) throws IOException {
		String methodName ="com.service.ScApiCreateREDIS.GetVendorList()";
		Logger.debug(methodName);

		/*  JDBC Connection 변수 선언  */		
		Connection 			conn	= null;
		
		/* PreparedStatement 선언 */		
		PreparedStatement	pstmt	= null;
		PreparedStatement	pstmt2	= null;
		
		/* ResultSet 선언 */		
		ResultSet			rs		= null;
		ResultSet			rs2		= null;
		
		/*  StringBuffer 선언  */		
		StringBuffer   	sqlBuffer  	= new StringBuffer(500);	//쿼리문
		StringBuffer   	sqlBuffer2  = new StringBuffer(500);	//카운트 쿼리문
		
		// should do this to evade warning [IOS 22-Jan-16]
		//HashMap<String, String> hm = new HashMap<String, String>();
		HashMap hm = new HashMap();
		List	vendorList 	= new ArrayList();
		
		int cnt 	= 0;
		
		try
		{
			conn = DataBaseManager.getConnection(dbmode);
			
			sqlBuffer.append("SELECT     RETC  AS COCD                	");  
			sqlBuffer.append("         , REFCD AS VDCD					");         
			sqlBuffer.append("FROM     TBB150							");
			sqlBuffer.append("WHERE    REFTP =  'ZY' 					");
			sqlBuffer.append("AND      REFCD <> '0000'					");
			sqlBuffer.append("AND      CD4   =  '"+ transCD +"' 		");
			sqlBuffer.append("GROUP BY RETC, REFCD						");
			pstmt = conn.prepareStatement(sqlBuffer.toString());
			rs = pstmt.executeQuery();
			
			sqlBuffer2.append("SELECT  COUNT(1) AS CNT					");
			sqlBuffer2.append("FROM     TBB150							");
			sqlBuffer2.append("WHERE    REFTP =  'ZY' 					");
			sqlBuffer2.append("AND      REFCD <> '0000'					");
			sqlBuffer2.append("AND      CD4   =  '"+ transCD +"' 		");
			sqlBuffer2.append("GROUP BY RETC, REFCD						");
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
					hm.put("COCD", rs.getString("COCD"));
					hm.put("VDCD", rs.getString("VDCD"));
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
	
	/**
	 * @ 상품 등록/수정 Sterling 송신  CUBE -> SC
	 * @작성자 PYO
	 * @생성일 2014-10
	 * @param dbmode
	 * @param transCD
	 * @return sendMessage
	 * @throws SQLException
	 * @throws IOException
	 * @throws JedisConnectionException
	 */
	public String api_SendProductData(String dbmode, String transCD) throws SQLException, IOException, JedisConnectionException
	{
		// 로그를 찍기 위한 변수 선언
		String methodName ="com.service.ScApiCreateREDIS.api_SendProductData()";
		Logger.debug(methodName);
		
		/*  JDBC Connection 변수 선언  */
		Connection 		conn		= null;
		
		/* PreparedStatement 선언 */
		PreparedStatement	pstmt0		= null; //        쿼리문 실행
		PreparedStatement	pstmt1		= null; //     주 쿼리문 실행
		PreparedStatement	pstmt2		= null; //   서브 쿼리문 실행
		PreparedStatement	pstmt3		= null; // 카운트 쿼리문 실행

		/* ResultSet 선언 */
		ResultSet			rs0			= null;
		ResultSet			rs1			= null;
		ResultSet			rs2			= null;
		ResultSet			rs3			= null;
				
		/*  StringBuffer 선언  */
		StringBuffer	sqlBuffer0  = new StringBuffer(1000);	// 		   쿼리문
		StringBuffer	sqlBuffer1  = new StringBuffer(1000);	//      주 쿼리문
		StringBuffer   	sqlBuffer2  = new StringBuffer(500);	//    서브 쿼리문	
		StringBuffer   	sqlBuffer3  = new StringBuffer(500);	//  카운트 쿼리문	
		StringBuffer   	sqlBuffer4  = new StringBuffer(500);	//처리결과 메세지
		
		/*  Redis 선언  */
		Jedis 			jedis   	= null;
		/*
		JedisPool 		jedisPool	= null;
		JedisPoolConfig jedisConfig = null;
		*/
		
		/* String 변수선언  */
		String 	sendMessage = null;		
	    
	    /* 숫자형 선언 */
		int successCnt 	= 0;
		
		try {

			conn =	DataBaseManager.getConnection(dbmode);		
			conn.setAutoCommit(false);

			Logger.debug("0. Sterling API 전송을위한 SQL 작성 시작");
			
			/* 0. Sterling API 전송을위한 SQL 작성 시작*/
			sqlBuffer0.append("SELECT   RETC AS COCD							\n");	
			sqlBuffer0.append("       , CD4  AS VDCD							\n");			
			sqlBuffer0.append("  FROM TBB150					    			\n");	
			sqlBuffer0.append(" WHERE REFTP = 'ZY'								\n");	
			sqlBuffer0.append("   AND REFCD <> '0000'							\n");	
			sqlBuffer0.append("   AND CD4   = '"+ transCD +"'					\n");	
			sqlBuffer0.append("   GROUP BY RETC, CD4						    \n");
			
			/* 0-1. 주 쿼리문*/
			sqlBuffer1.append("SELECT    MAX(A.COCD)      AS COCD				\n");	
			sqlBuffer1.append("        , A.PRODINC        AS PRODINC			\n");			
			sqlBuffer1.append("        , MAX(A.PNAME)     AS PNAME				\n");	
			sqlBuffer1.append("        , MAX(A.BRAND_ID)  AS BRAND_ID			\n");	
			sqlBuffer1.append("        , MAX(A.BRAND_NM)  AS BRAND_NM			\n");	
			sqlBuffer1.append("        , MAX(A.FIPRI)     AS FIPRI				\n");	
			sqlBuffer1.append("        , MAX(A.TRAN_DATE) AS TRAN_DATE			\n");
			sqlBuffer1.append("        , MAX(A.TRAN_SEQ)  AS TRAN_SEQ			\n");			
			sqlBuffer1.append("        , MAX(A.GOODS_CODE)    AS GOODS_CODE		\n");
			sqlBuffer1.append("        , MAX(A.GOODS_DETAIL)  AS GOODS_DETAIL	\n");
			sqlBuffer1.append("        , MAX(A.GOODS_URL)     AS GOODS_URL		\n");
			sqlBuffer1.append("        , MAX(A.SEX ) 		  AS SEX			\n");
			sqlBuffer1.append("        , MAX(A.SEASON)  	  AS SEASON			\n");
			sqlBuffer1.append("        , MAX(A.GROUP_DESC)    AS GROUP_DESC		\n");			
			sqlBuffer1.append("  FROM TBP050_TRANSFER A ,						\n");				
			sqlBuffer1.append("      (	SELECT   BAR_CODE					    \n");	
			sqlBuffer1.append("                , MAX(TRAN_DATE) AS TRAN_DATE 	\n");
			sqlBuffer1.append("                , MAX(TRAN_SEQ)  AS TRAN_SEQ 	\n");
			sqlBuffer1.append("          FROM TBP050_TRANSFER   				\n");			
			sqlBuffer1.append("          WHERE STATUS  IN ('00', '99')   		\n");	
			sqlBuffer1.append("          AND COCD 		= ?  					\n");	
			sqlBuffer1.append("          AND SHOP_ID 	= ?  					\n");			
			sqlBuffer1.append("          GROUP BY BAR_CODE ) B  				\n");			
			sqlBuffer1.append("  WHERE A.TRAN_DATE = B.TRAN_DATE 				\n");
			sqlBuffer1.append("  AND   A.TRAN_SEQ  = B.TRAN_SEQ 				\n");
			sqlBuffer1.append("  AND A.BAR_CODE  = B.BAR_CODE 					\n");		
			sqlBuffer1.append("  AND A.COCD      = ? 							\n");
			sqlBuffer1.append("  AND A.SHOP_ID   = ? 							\n");
			sqlBuffer1.append("  GROUP BY A.PRODINC								\n");
			sqlBuffer1.append("  ORDER BY TRAN_DATE, TRAN_SEQ					\n");
			
			/* 0-2. 서브 쿼리문*/
			sqlBuffer2.append("SELECT   ITEM_COLOR                              \n");
			sqlBuffer2.append("        ,ITEM_SIZE                               \n");
			sqlBuffer2.append("        ,BAR_CODE                                \n");
			sqlBuffer2.append("FROM    TBP050_TRANSFER                          \n");
			sqlBuffer2.append("WHERE   TRAN_DATE = ?                            \n");
			sqlBuffer2.append("AND     TRAN_SEQ  = ?                            \n");
			sqlBuffer2.append("AND     COCD      = ?                            \n");
			sqlBuffer2.append("AND     SHOP_ID   = ?                            \n");			
			sqlBuffer2.append("AND     PRODINC	 = ?                            \n");
			sqlBuffer2.append("ORDER BY BAR_CODE                                \n");
			
			/* 0-3. 카운트 쿼리문*/
			sqlBuffer3.append("SELECT    COUNT(1) AS CNT					    \n");				
			sqlBuffer3.append("  FROM TBP050_TRANSFER A ,					    \n");				
			sqlBuffer3.append("      (	SELECT   BAR_CODE					    \n");	
			sqlBuffer3.append("                , MAX(TRAN_DATE) AS TRAN_DATE 	\n");
			sqlBuffer3.append("                , MAX(TRAN_SEQ)  AS TRAN_SEQ 	\n");
			sqlBuffer3.append("          FROM TBP050_TRANSFER   				\n");			
			sqlBuffer3.append("          WHERE STATUS  IN ('00', '99')   		\n");	
			sqlBuffer3.append("          AND COCD 		= ?  					\n");	
			sqlBuffer3.append("          AND SHOP_ID 	= ?  					\n");			
			sqlBuffer3.append("          GROUP BY BAR_CODE ) B  				\n");			
			sqlBuffer3.append("  WHERE A.TRAN_DATE = B.TRAN_DATE 				\n");
			sqlBuffer3.append("  AND   A.TRAN_SEQ  = B.TRAN_SEQ 				\n");
			sqlBuffer3.append("  AND A.BAR_CODE  = B.BAR_CODE 					\n");		
			sqlBuffer3.append("  AND A.COCD      = ? 							\n");
			sqlBuffer3.append("  AND A.SHOP_ID   = ? 							\n");
			sqlBuffer3.append("  GROUP BY A.PRODINC								\n");
			
			/* 0. Sterling API 전송을위한 SQL 작성 끝*/
			Logger.debug("0. Sterling API 전송을위한 SQL 작성 끝");
			
			pstmt0 = conn.prepareStatement(sqlBuffer0.toString()); 			
			pstmt1 = conn.prepareStatement(sqlBuffer1.toString()); 
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString()); 
			pstmt3 = conn.prepareStatement(sqlBuffer3.toString()); 			
			
			rs0 = pstmt0.executeQuery();

			/* 1. API CUBE 상품 전송데이터 Count 시작 */
			Logger.debug("[1. API CUBE 상품 전송데이터 조회 시작]");	
			while(rs0.next()){
				
				int count 		= 0;
				int errCnt 		= 0;
				int cnt 		= 0;
				
				String cocd = StringUtil.nullTo(rs0.getString("COCD"),"");
				String vdcd = StringUtil.nullTo(rs0.getString("VDCD"),"");
				
				Logger.debug("[COCD["+StringUtil.nullTo(rs0.getString("COCD"),"")+"]");
				Logger.debug("[VDCD["+StringUtil.nullTo(rs0.getString("VDCD"),"")+"]");
				
				pstmt3.setString(1, cocd);
				pstmt3.setString(2, vdcd);
				pstmt3.setString(3, cocd);
				pstmt3.setString(4, vdcd);
				
				rs3 = pstmt3.executeQuery();
				if(rs3.next()){
					cnt = rs3.getInt("CNT");
				}
				
				if(cnt > 0){ //전송 DATA 있을때..

					pstmt1.setString(1, cocd);
					pstmt1.setString(2, vdcd);
					pstmt1.setString(3, cocd);
					pstmt1.setString(4, vdcd);
					
					rs1 = pstmt1.executeQuery();
					
					JSONObject 	jsonObject 		= new JSONObject();
					JSONArray 	prodincArray 	= new JSONArray();
					
					// 품목 리시트조회
					while(rs1.next()){
							
						JSONObject prodList = new JSONObject();
					
						/* 2. Sterling API 전송을위한 JSON_DATA 생성 시작 */
						Logger.debug("[2. Sterling API 전송을위한 JSON_DATA 생성 시작]");					
						Logger.debug("[org_code["+StringUtil.nullTo(rs1.getString("COCD"),"")+"]");
						Logger.debug("[prodinc["+StringUtil.nullTo(rs1.getString("PRODINC"),"")+"]");
						Logger.debug("[pname["+StringUtil.nullTo(rs1.getString("PNAME"),"")+"]");
						Logger.debug("[brand_id["+StringUtil.nullTo(rs1.getString("BRAND_ID"),"")+"]");
						Logger.debug("[brand_name["+StringUtil.nullTo(rs1.getString("BRAND_NM"),"")+"]");
						Logger.debug("[sale_price["+StringUtil.nullTo(rs1.getString("FIPRI"),"")+"]");
						Logger.debug("[TRAN_DATE["+StringUtil.nullTo(rs1.getString("TRAN_DATE"),"")+"]");
						Logger.debug("[TRAN_SEQ["+StringUtil.nullTo(rs1.getString("TRAN_SEQ"),"")+"]");						
						Logger.debug("[GOODS_CODE["+StringUtil.nullTo(rs1.getString("GOODS_CODE"),"")+"]");
						Logger.debug("[GOODS_DETAIL["+StringUtil.nullTo(rs1.getString("GOODS_DETAIL"),"")+"]");						
						Logger.debug("[GOODS_URL["+StringUtil.nullTo(rs1.getString("GOODS_URL"),"")+"]");						
						Logger.debug("[SEX["+StringUtil.nullTo(rs1.getString("SEX"),"")+"]");
						Logger.debug("[SEASON["+StringUtil.nullTo(rs1.getString("SEASON"),"")+"]");
						Logger.debug("[GROUP_DESC["+StringUtil.nullTo(rs1.getString("GROUP_DESC"),"")+"]");
						
						
						prodList.put("tran_date",StringUtil.nullTo(rs1.getString("TRAN_DATE"),""));			// 1.[Parameter]사업부코드
						prodList.put("tran_seq",StringUtil.nullTo(rs1.getString("TRAN_SEQ"),""));			// 2.[Parameter]사업부코드						
						prodList.put("org_code",StringUtil.nullTo(rs1.getString("COCD"),""));				// 3.[Parameter]사업부코드
						prodList.put("prodinc",StringUtil.nullTo(rs1.getString("PRODINC"),""));				// 4.[Parameter]스타일코드
						prodList.put("pname",StringUtil.nullTo(rs1.getString("PNAME"),""));					// 5.[Parameter]상품명					
						prodList.put("brand_id",StringUtil.nullTo(rs1.getString("BRAND_ID"),""));			// 6.[Parameter]브랜드ID
						prodList.put("brand_name",StringUtil.nullTo(rs1.getString("BRAND_NM"),""));			// 7.[Parameter]브래드명
						prodList.put("sale_price",StringUtil.nullTo(rs1.getString("FIPRI"),""));			// 8.[Parameter]최초판매가	
						prodList.put("goods_code",StringUtil.nullTo(rs1.getString("GOODS_CODE"),""));		// 9.[Parameter]							
						prodList.put("goods_detail",StringUtil.nullTo(rs1.getString("GOODS_DETAIL"),""));	// 10.[Parameter]	
						prodList.put("goods_url",StringUtil.nullTo(rs1.getString("GOODS_URL"),""));			// 11.[Parameter]	
						prodList.put("sex",StringUtil.nullTo(rs1.getString("SEX"),""));						// 12.[Parameter]	
						prodList.put("season",StringUtil.nullTo(rs1.getString("SEASON"),""));				// 13.[Parameter]	
						//prodList.put("group_desc",StringUtil.nullTo(rs1.getString("GROUP_DESC"),""));		// 14.[Parameter]	
						prodList.put("group_text",StringUtil.nullTo(rs1.getString("GROUP_DESC"),""));		// 14.[Parameter] (2015.2.9 group_desc -> group_text로 수정)
						
						// 바코드 정보 가져오기..
						pstmt2.setString(1, StringUtil.nullTo(rs1.getString("TRAN_DATE"),""));
						pstmt2.setString(2, StringUtil.nullTo(rs1.getString("TRAN_SEQ"),""));
						pstmt2.setString(3, cocd);
						pstmt2.setString(4, vdcd);						
						pstmt2.setString(5, StringUtil.nullTo(rs1.getString("PRODINC"),""));
						
						rs2 = pstmt2.executeQuery();
						JSONArray cellOpt = new JSONArray();
						
						// 'optioninfo' creation [IOS 24-MAR-16]
						while (rs2.next()){
						
							JSONObject itemOption = new JSONObject();

							Logger.debug("[2-1. Sterling API 전송을위한 BAR_CODE JSON_DATA 생성 시작]");							
							Logger.debug("[ITEM_COLOR["+StringUtil.nullTo(rs2.getString("ITEM_COLOR"),"")+"]");
							Logger.debug("[ITEM_SIZE["+StringUtil.nullTo(rs2.getString("ITEM_SIZE"),"")+"]");
							Logger.debug("[BAR_CODE["+StringUtil.nullTo(rs2.getString("BAR_CODE"),"")+"]");
	
							itemOption.put("item_color", StringUtil.nullTo(rs2.getString("ITEM_COLOR"),""));	// 15.[Parameter]컬러명
							itemOption.put("item_size", StringUtil.nullTo(rs2.getString("ITEM_SIZE"),""));		// 16.[Parameter]사이즈명
							itemOption.put("bar_code", StringUtil.nullTo(rs2.getString("BAR_CODE"),""));		// 17.[Parameter]상품바코드
							
							cellOpt.add(itemOption);
							prodList.put("optioninfo",cellOpt);
						}										
						prodincArray.add(prodList);
						
						Logger.debug("[2. Sterling API 전송을위한 JSON_DATA 생성 끝]");
						/* 2. Sterling API 전송을위한 JSON_DATA 생성 끝 */	
						
						count++;		// 사업부별 성공 카운트
						successCnt++;	// 전체 성공 카운트					
					}
					jsonObject.put("list", prodincArray);
					Logger.debug("[송신데이터["+jsonObject.toString()+"]");
					
					/* 3. Redis Connection 시작 */
					Logger.debug("[3. Redis Connection 시작]");
					
					/* 3-1. Redis connection pool 생성 */				
					/*
					jedisPool 	= new JedisPool(new JedisPoolConfig(), redIp , port ,12000); 					
					jedis 		= jedisPool.getResource();
					*/
					jedis = new Jedis(RED_IP, PORT , 12000);
					
					Logger.debug("redis ip:  " + RED_IP);
					Logger.debug("redis port: " + PORT);
					
					jedis.connect();
					jedis.select(DB_INDEX);
	
					Logger.debug("[SEND_KEY]"+cocd+SEND_PRODUCT_KEY);
					
					/* 3-2 Steling OMS 전송할 상품정보 SET */				
					/* SET */  				 
					jedis.lpush(cocd+SEND_PRODUCT_KEY, jsonObject.toString());
											
					Logger.debug("[3. Redis Connection 끝]");				
					// 3. Redis Connection 끝
					
				}else{
					
					errCnt++;	// 사업부별 실패 카운트
				}
				
				sqlBuffer4.append("사업부["+cocd+"] 정상:"+count+"/ 실패:"+errCnt+"  "); 
			}
			
			if(successCnt > 0){
				sendMessage = "SUCCESS !!!!! ["+sqlBuffer4.toString()+"]";
			}else{
				sendMessage = "NO DATA !!!!! [ 송신할 상품정보가 존재하지 않습니다. ]";
			}

		} catch(SQLException e) {
			
			conn.rollback();			
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());			
			
			sendMessage = "FAIL!["+e.toString()+"]";			
		
		} catch(JedisConnectionException e) {

			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			/* Redis connection 제거*/
			//if( jedisPool!= null )jedisPool.returnBrokenResource(jedis); jedisPool = null;
			
			sendMessage = "FAIL!!["+e.toString()+"]";
			
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			sendMessage = "FAIL!!!["+e.toString()+"]";
		
		}finally {
			try 
			{
				conn.setAutoCommit(true);
				
				if( rs0 !=null ) try{ rs0.close(); rs0 = null; }catch(Exception e){}finally{rs0 = null;}
				if( rs1 !=null ) try{ rs1.close(); rs1 = null; }catch(Exception e){}finally{rs1 = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}
				
				if(pstmt0  != null ) try{ pstmt0.close(); pstmt0 = null; }catch(Exception e){}finally{pstmt0 = null;}
				if(pstmt1  != null ) try{ pstmt1.close(); pstmt1 = null; }catch(Exception e){}finally{pstmt1 = null;}				
				if(pstmt2  != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				
				DataBaseManager.close(conn, dbmode);				
				if(conn	!= null ) try{ conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}		
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
				//if(jedisPool!= null ) try{ jedisPool.destroy(); jedisPool = null; }catch(Exception e){}finally{jedisPool = null;}				
				
			} 
		    catch (Exception e) 
		    {

		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());						
				sendMessage = "FAIL!!!!["+e.toString()+"]";
		    }
		}

		return sendMessage;
	}

	/**
	 * 상품등록 처리결과 수신 SC -> CUBE
	 * @param dbmode
	 * @param vendorID
	 * @param transCD
	 * @return 
	 * @throws SQLException
	 * @throws IOException
	 * @throws JedisConnectionException
	 * @throws JSONException
	 */
	public String api_RecvProductData(String dbmode, String transCD) throws SQLException, IOException, JedisConnectionException, JSONException
	{
		// 로그를 찍기 위한 변수 선언
		String methodName ="com.service.ScApiCreateREDIS.api_RecvProductData()";
		Logger.debug(methodName);

		/* JDBC Connection 변수 선언 */
		Connection 		conn		= null;
		
		/* PreparedStatement 선언 */
		PreparedStatement	pstmt0		= null; // 쿼리문 실행
		PreparedStatement	pstmt1		= null; // 주 쿼리문 실행

		/* ResultSet 선언 */
		ResultSet			rs0			= null;

		/* StringBuffer 선언*/		
		StringBuffer	sqlBuffer0  = new StringBuffer(1000);	// 주 쿼리문
		StringBuffer	sqlBuffer1  = new StringBuffer(1000);	// 주 쿼리문
		StringBuffer	sqlBuffer2  = new StringBuffer(1000);	// 주 쿼리문
		
		/* Redis 선언 */
		Jedis 			jedis   	= null;
		
		/* String 변수선언  */	
		String 	sendMessage = null;		
	    //String  succStr 	= null;
	    
	    /* 숫자형 선언 */
		int successCnt 	= 0;
		
		try {
			
			conn =	DataBaseManager.getConnection(dbmode);		
			conn.setAutoCommit(false);

			Logger.debug("0. Sterling 상품 수신후 UPDATE SQL 작성 시작");
			
			sqlBuffer0.append("SELECT   RETC AS COCD											\n");	
			sqlBuffer0.append("       , CD4  AS VDCD											\n");			
			sqlBuffer0.append("  FROM TBB150					    							\n");	
			sqlBuffer0.append(" WHERE REFTP = 'ZY'												\n");	
			sqlBuffer0.append("   AND REFCD <> '0000'											\n");	
			sqlBuffer0.append("   AND CD4   = '"+ transCD +"'						    		\n");	
			sqlBuffer0.append("   GROUP BY RETC, CD4						    				\n");
			
			/* 0. Sterling 상품 수신후 UPDATE SQL 작성 시작*/
			/* 0-1. 주 쿼리문*/
			sqlBuffer1.append("UPDATE  TBP050_TRANSFER                                          \n");
			sqlBuffer1.append("     SET STATUS     = ?                                          \n");
			sqlBuffer1.append("        ,STATUS_MSG = ?                                          \n");
			sqlBuffer1.append("        ,UPD_ID 	= 'SCAPI'                              			\n");
			sqlBuffer1.append("        ,UPD_DT 	= TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS')       	\n");
			sqlBuffer1.append("WHERE STATUS  IN ('00', '99')          							\n");
			sqlBuffer1.append("AND COCD      =  ?                     							\n");
			sqlBuffer1.append("AND PRODINC   =  ?              									\n");
			sqlBuffer1.append("and TRAN_DATE||LPAD(TRAN_SEQ,4,'0')   <=  ?||LPAD(?,4,'0') 		\n");			
			
			/* 0-2. 서브 쿼리문*/
			/* 0-3. 카운트 쿼리문*/
			
			/* 0. Sterling 상품 수신후 UPDATE SQL 작성 끝*/
			Logger.debug("0. Sterling 상품 수신후 UPDATE SQL 작성  끝");
						
			/* 1. Redis connection 생성 */				
			Logger.debug("1. Redis connection 생성 시작");
			
			Logger.debug("Reids ip: " + RED_IP);
			Logger.debug("Reids port: " + PORT);
			
			jedis = new Jedis(RED_IP, PORT , 12000);
			jedis.connect();
			jedis.select(DB_INDEX);			
			
			Logger.debug("1. Redis connection 생성 끝");
			/* 1. Redis connection 끝 */
			
			pstmt0 = conn.prepareStatement(sqlBuffer0.toString()); // 주쿼리문		
			rs0 = pstmt0.executeQuery();
			
			while(rs0.next()){

				String cocd = StringUtil.nullTo(rs0.getString("COCD"),"");
				
			    int count 	= 0;
			    int errcnt 	= 0;
				int redisCnt 	= jedis.llen(cocd+RECV_PRODUCT_KEY ).intValue();
								
				Logger.debug("[COCD["+StringUtil.nullTo(rs0.getString("COCD"),"")+"]");		// 사업부코드
				Logger.debug("[VDCD["+StringUtil.nullTo(rs0.getString("VDCD"),"")+"]");		// SHOP_ID				
				Logger.debug("상품수신-REDIS_KEY["+cocd+RECV_PRODUCT_KEY+"]");
				Logger.debug("상품수신-REDIS_COUNT["+redisCnt+"]");				
				
				/* 2. Sterling 수신데이터 처리 시작 */	
				Logger.debug("2. Sterling 수신데이터 처리 시작");
				if(redisCnt > 0){
					
					String tranDate		= "";	// 전송날짜
					String tranSeq		= "";	// 전송순번					
					String org_code		= "";	// 사업부코드
					String prodinc		= "";	// 스타일코드
					String statuscd 	= "";	// 처리상태				    
					
					for (int j = 0; j < redisCnt; j++){
						
						String  jsonString 	= StringUtil.nullTo(jedis.rpop(cocd+RECV_PRODUCT_KEY),"");
						String 	jsonData 	= URLDecoder.decode(jsonString,"UTF-8");
						Logger.debug("SC API 상품처리결과 DATA["+jsonString+"]");	
						
						JSONObject 	json 		= JSONObject.fromObject(jsonData);
						JSONArray 	prodArray 	= json.getJSONArray("list");				
		
						/* 2-1. Sterling 수신데이터 파싱 시작*/	
						Logger.debug("2-1. Sterling 수신데이터 파싱 시작");
						Logger.debug(cocd+"_size["+prodArray.size()+"]");
						
						for (int i = 0; i < prodArray.size(); i++){
							
							JSONObject prodList = prodArray.getJSONObject(i);
	
							tranDate 	= StringUtil.nullTo(prodList.getString("tran_date"),"");	// 1.[Parameter] 전송날짜
							tranSeq 	= StringUtil.nullTo(prodList.getString("tran_seq"),"");		// 2.[Parameter] 전송순번						
							org_code 	= StringUtil.nullTo(prodList.getString("org_code"),"");		// 3.[Parameter] 사업부코드
							prodinc 	= StringUtil.nullTo(prodList.getString("prodinc"),"");		// 4.[Parameter] 스타일코드
							statuscd 	= StringUtil.nullTo(prodList.getString("statuscd"),""); 	// 5.[Parameter] 처리상태
		
							Logger.debug("tranDate["+tranDate+"]");
							Logger.debug("tranSeq["+tranSeq+"]");
							Logger.debug("org_code["+org_code+"]");
							Logger.debug("prodinc["+prodinc+"]");
							Logger.debug("statuscd["+statuscd+"]");
							
							//품목 전송 결과 업데이트..
							if (pstmt1 != null) { pstmt1.close(); pstmt1 = null; }
							pstmt1 = conn.prepareStatement(sqlBuffer1.toString()); //주 쿼리문	
		
							pstmt1.setString(1, statuscd);
							if(statuscd.equals("01")){
								pstmt1.setString(2, "API 상품등록 성공");
							}else{
								pstmt1.setString(2, "API 상품등록 실패");							
							}
							pstmt1.setString(3, org_code);
							pstmt1.setString(4, prodinc);
							pstmt1.setString(5, tranDate);
							pstmt1.setString(6, tranSeq);
							pstmt1.executeUpdate();							
													
						}
						Logger.debug("2-1. Sterling 수신데이터 파싱 끝");
						/* 2-1. Sterling 수신데이터 파싱 끝*/
						
						count++;		// 사업부 별 처리 카운트
						successCnt++;	// 전체 처리 카운트
						
					} // END-FOR(REDIS COUNT
				
				}else{
					errcnt++;	// 사업부별 실패 카운트
				}
				
				sqlBuffer2.append(" 사업부["+cocd+"]정상:"+count+"/ 실패:"+errcnt);				
				Logger.debug("2. Sterling 수신데이터 처리 끝");
				/* 2. Sterling 수신데이터 처리 끝 */
			}
			
			if(successCnt > 0){
				sendMessage = " SUCCESS !!!!! ["+sqlBuffer2+"]";
			}else{
				sendMessage = " NO DATA !!!!! [ 상품등록 처리결과 수신대상이 존재하지 않습니다. ]";				
			}
			
		} catch(SQLException e) {
			
			conn.rollback();			
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());			
			
			sendMessage = "FAIL!["+e.toString()+"]";			
		
		} catch(JedisConnectionException e) {

			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			sendMessage = "FAIL!!["+e.toString()+"]";
			
		} catch(Exception e) {
			
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			sendMessage = "FAIL!!!["+e.toString()+"]";
		
		}
		finally {
			try 
			{
				conn.setAutoCommit(true);	
				if( rs0 !=null ) try{ rs0.close(); rs0 = null; }catch(Exception e){}finally{rs0 = null;}
				
				if(pstmt0  != null ) try{ pstmt0.close(); pstmt0 = null; }catch(Exception e){}finally{pstmt0 = null;}
				if(pstmt1  != null ) try{ pstmt1.close(); pstmt1 = null; }catch(Exception e){}finally{pstmt1 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if(conn	!= null ) try{ conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}		
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
			} 
		    catch (Exception e) 
		    {

		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());						
				sendMessage = "FAIL!!!!["+e.toString()+"]";
		    }
		}

		return sendMessage;
	}

	/**
	 * 상품 재고수량 송신  CUBE  ->  SC
	 * @param dbmode
	 * @param transCD
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws JedisConnectionException
	 * @throws JSONException
	 */
	public String api_Auto_SendItemStock(String dbmode, String transCD) throws SQLException, IOException, JedisConnectionException, JSONException
	{
		String methodName ="com.service.ScApiCreateREDIS.api_Auto_SendItemStock";
		Logger.debug(methodName);
		
		/* JDBC Connection 변수 선언 */
		Connection 		conn		= null;
		
		/* PreparedStatement 선언 */
		PreparedStatement	pstmt0		= null; //    		쿼리문 실행
		PreparedStatement	pstmt1		= null; //     주 	쿼리문 실행
		PreparedStatement	pstmt2		= null; // 카운트 	쿼리문 실행
		CallableStatement 	cstmt    	= null;		

		/* ResultSet 선언 */
		ResultSet			rs0			= null;
		ResultSet			rs1			= null;
		ResultSet			rs2			= null;		
				
		/* StringBuffer 선언*/		
		StringBuffer	sqlBuffer0  = new StringBuffer(1000);	// 사업부 	쿼리문
		StringBuffer	sqlBuffer1  = new StringBuffer(1000);	// 주 		쿼리문
		StringBuffer	sqlBuffer2  = new StringBuffer(1000);	// 카운트 	쿼리문
		StringBuffer   	sqlBuffer3  = new StringBuffer(500);	// 처리결과 메세지
		
		/* Redis 선언 */
		Jedis 			jedis   	= null;
		
		/* String 변수선언  */	
		String 	sendMessage = null;		
	    
	    /* 숫자형 선언 */
		int successCnt 	= 0;
				
		try {

			conn = DataBaseManager.getConnection(dbmode);
		
			/* 0. Sterling 재고 송신 SQL 시작 */	
			Logger.debug("0. Sterling 재고 송신 SQL 시작");
			
			sqlBuffer0.append("SELECT   RETC AS COCD				\n");	
			sqlBuffer0.append("       , CD4  AS VDCD				\n");			
			sqlBuffer0.append("  FROM TBB150					    \n");	
			sqlBuffer0.append(" WHERE REFTP = 'ZY'					\n");	
			sqlBuffer0.append("   AND REFCD <> '0000'				\n");	
			sqlBuffer0.append("   AND CD4   = '"+ transCD +"'		\n");	
			sqlBuffer0.append("   GROUP BY RETC, CD4				\n");
						
			sqlBuffer1.append("SELECT   A.TRAN_DATE AS TRAN_DATE    \n");
			sqlBuffer1.append("        ,A.TRAN_SEQ  AS TRAN_SEQ     \n");
			sqlBuffer1.append("        ,A.WHCD    	AS WHCD        	\n");			
			sqlBuffer1.append("        ,A.BARCODE   AS BARCODE      \n");
			sqlBuffer1.append("        ,A.STOCK  	AS  STOCK       \n");
			sqlBuffer1.append("FROM    TBD260 A             		\n");
			sqlBuffer1.append(" , ( SELECT  WHCD							\n");
			sqlBuffer1.append("           , BARCODE       					\n");
			sqlBuffer1.append("           , MAX(TRAN_DATE) AS TRAN_DATE     \n");
			sqlBuffer1.append("           , MAX(TRAN_SEQ)  AS TRAN_SEQ    	\n");
			sqlBuffer1.append("        FROM TBD260      	 				\n");			
			sqlBuffer1.append("          WHERE STATUS   <> '01'     	 	\n");			
			sqlBuffer1.append("           AND VENDOR_ID = ?      	 		\n");			
			sqlBuffer1.append("           AND COCD      = ?      	 		\n");
			sqlBuffer1.append("           GROUP BY WHCD, BARCODE    	 	\n");			
			sqlBuffer1.append("      ) B    	 							\n");			
			sqlBuffer1.append("      , ( SELECT BAR_CODE AS BARCODE    	 	\n");
			sqlBuffer1.append("        FROM TBP050_TRANSFER   	 			\n");
			sqlBuffer1.append("    WHERE STATUS = '01'  	 				\n");
			sqlBuffer1.append("     GROUP BY BAR_CODE   	 				\n");
			sqlBuffer1.append("      ) C  	 								\n");									
			sqlBuffer1.append("   WHERE A.TRAN_DATE = B.TRAN_DATE    	 	\n");
			sqlBuffer1.append("      AND A.TRAN_SEQ  = B.TRAN_SEQ    	 	\n");
			sqlBuffer1.append("      AND A.WHCD      = B.WHCD    	 		\n");
			sqlBuffer1.append("      AND A.BARCODE   = B.BARCODE   	 		\n");
			sqlBuffer1.append("      AND A.BARCODE   = C.BARCODE   	 		\n");			
			sqlBuffer1.append("      AND A.VENDOR_ID = ?   	 				\n");
			sqlBuffer1.append("      AND A.COCD      = ?  	 				\n");
			sqlBuffer1.append("      ORDER BY 1,2,3,4 ASC   				\n");
			
			sqlBuffer2.append("SELECT  COUNT(1) AS CNT    					\n");
			sqlBuffer2.append("FROM    TBD260 A             				\n");
			sqlBuffer2.append(" , ( SELECT  WHCD							\n");
			sqlBuffer2.append("           , BARCODE       					\n");
			sqlBuffer2.append("           , MAX(TRAN_DATE) AS TRAN_DATE     \n");
			sqlBuffer2.append("           , MAX(TRAN_SEQ)  AS TRAN_SEQ    	\n");
			sqlBuffer2.append("        FROM TBD260      	 				\n");			
			sqlBuffer2.append("          WHERE STATUS   <> '01'     	 	\n");			
			sqlBuffer2.append("           AND VENDOR_ID = ?      	 		\n");			
			sqlBuffer2.append("           AND COCD      = ?      	 		\n");
			sqlBuffer2.append("           GROUP BY WHCD, BARCODE    	 	\n");			
			sqlBuffer2.append("      ) B    	 							\n");			
			sqlBuffer2.append("      , ( SELECT BAR_CODE AS BARCODE    	 	\n");
			sqlBuffer2.append("        FROM TBP050_TRANSFER   	 			\n");
			sqlBuffer2.append("    WHERE STATUS = '01'  	 				\n");
			sqlBuffer2.append("     GROUP BY BAR_CODE   	 				\n");
			sqlBuffer2.append("      ) C  	 								\n");									
			sqlBuffer2.append("   WHERE A.TRAN_DATE = B.TRAN_DATE    	 	\n");
			sqlBuffer2.append("      AND A.TRAN_SEQ  = B.TRAN_SEQ    	 	\n");
			sqlBuffer2.append("      AND A.WHCD      = B.WHCD    	 		\n");
			sqlBuffer2.append("      AND A.BARCODE   = B.BARCODE   	 		\n");
			sqlBuffer2.append("      AND A.BARCODE   = C.BARCODE   	 		\n");			
			sqlBuffer2.append("      AND A.VENDOR_ID = ?   	 				\n");
			sqlBuffer2.append("      AND A.COCD      = ?  	 				\n");
			
			pstmt0 = conn.prepareStatement(sqlBuffer0.toString());	// 사업부 쿼리 	
			pstmt1 = conn.prepareStatement(sqlBuffer1.toString());	//     주 쿼리
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());  // 카운트 쿼리 
				
			Logger.debug("0. Sterling 재고 송신 SQL 끝");
			/* 0. Sterling 재고 송신 SQL 끝 */

    			
			rs0 = pstmt0.executeQuery();
			
			// 사업부 조회
			while(rs0.next()){
				
				int cnt 	= 0;    				
				int count 	= 0; // 성공카운트	    				
				int errCnt 	= 0; // 실패카운트
				
				Logger.debug("[COCD["+StringUtil.nullTo(rs0.getString("COCD"),"")+"]");		// 사업부코드
				String cocd = StringUtil.nullTo(rs0.getString("COCD"),"");    				
				
				/* 1. Sterling 재고 프로시져 시작 */
				Logger.debug("1. Sterling 재고 프로시져 시작");
				
				if (cstmt != null) { cstmt.close(); cstmt = null; }				
				cstmt = conn.prepareCall("{call P_SEND_STOCK(?, ?, ?, ?, ?, ?)}");
				cstmt.registerOutParameter(1, Types.CHAR);
	        	cstmt.registerOutParameter(2, Types.CHAR);
	        	cstmt.registerOutParameter(3, Types.CHAR);
	        	cstmt.registerOutParameter(4, Types.INTEGER);
	        	cstmt.setString(5, transCD);
	        	cstmt.setString(6, cocd);	        	
	        	
	        	cstmt.executeUpdate();
	        	
	        	String errcode 	= cstmt.getString(1);
	        	String errmsg 	= cstmt.getString(2);	
	            String tranDt  	= cstmt.getString(3);
	            int    tranSeq 	= cstmt.getInt(4);

	    		Logger.debug("errcode["+errcode+"]");
	    		Logger.debug("errmsg["+errmsg+"]");
	    		Logger.debug("tranDt["+tranDt+"]");
	    		Logger.debug("tranSeq["+tranSeq+"]");

				Logger.debug("1. Sterling 재고 프로시져 끝");
				/* 1. Sterling 재고 프로시져 끝 */    		

	    		if (errcode.equals("00")) {
        	
    				pstmt2.setString(1, transCD);
					pstmt2.setString(2, cocd);
    				pstmt2.setString(3, transCD);
					pstmt2.setString(4, cocd);
					
	            	rs2 = pstmt2.executeQuery();
	    			
	    			if(rs2.next())
	    			{
	    				cnt = rs2.getInt("CNT");
	    			}
	            	
	    			//전송 DATA 있을때..
	    			if(cnt > 0){
	    				
	    				/* 2. Sterling 재고송신 JSON형식 API항목 정의 시작 */
	    				Logger.debug("2. Sterling 재고송신 JSON형식 API항목 정의 시작");
	    				
	    				pstmt1.setString(1, transCD);
						pstmt1.setString(2, cocd);
	    				pstmt1.setString(3, transCD);
						pstmt1.setString(4, cocd);
						
	    				rs1 = pstmt1.executeQuery();
	    				    				
	    				JSONObject jsonObject = new JSONObject();
	    				JSONArray cell = new JSONArray();
	    				
	    				while(rs1.next())
	    				{
	    					JSONObject asrrotList = new JSONObject();

	    					asrrotList.put("tran_date",StringUtil.nullTo(rs1.getString("TRAN_DATE"),""));	// 전송날짜
	    					asrrotList.put("tran_seq",StringUtil.nullTo(rs1.getString("TRAN_SEQ"),""));		// 전송순번	    					
	    					asrrotList.put("org_code",cocd);												// 사업부코드	    					
	    					asrrotList.put("ship_node",StringUtil.nullTo(rs1.getString("WHCD"),""));		// 창고코드
	    					asrrotList.put("bar_code",StringUtil.nullTo(rs1.getString("BARCODE"),""));		// 상품코드
	    					asrrotList.put("qty",StringUtil.nullTo(rs1.getString("STOCK"),""));				// 수량
	    					asrrotList.put("uom","EACH");													// 측정단위 ( SC 고정값 )
	    					
	        				Logger.debug("tran_date["+StringUtil.nullTo(rs1.getString("TRAN_DATE"),"")+"]");	    					
	        				Logger.debug("tran_seq["+StringUtil.nullTo(rs1.getString("TRAN_SEQ"),"")+"]");
	    					Logger.debug("ship_node["+StringUtil.nullTo(rs1.getString("WHCD"),"")+"]");
	    					Logger.debug("bar_code["+StringUtil.nullTo(rs1.getString("BARCODE"),"")+"]");
	        				Logger.debug("qty["+StringUtil.nullTo(rs1.getString("STOCK"),"")+"]");
	        				
	    					cell.add(asrrotList);
	    					
	    					count++;
	    					successCnt++;
	    				}
	    				
	    				jsonObject.put("list", cell);
	    				
	    				/* 2. Sterling 재고송신 JSON형식 API항목 정의 끝 */
	    				Logger.debug("2. Sterling 재고송신 JSON형식 API항목 정의 끝");
	    				
						/* 3. Redis Connection 시작 */
						Logger.debug("[3. Redis Connection 시작]");
						
						jedis = new Jedis(RED_IP, PORT , 12000);
						jedis.connect();
						jedis.select(DB_INDEX);
	
						Logger.debug("[재고SEND_KEY]"+cocd+SEND_INVENTORY_KEY);
						
						/* 3-1 Steling OMS 전송할 재고정보 SET */				
						/* SET */  				 
						jedis.lpush(cocd+SEND_INVENTORY_KEY, jsonObject.toString());
											
						Logger.debug("[3. Redis Connection 끝]");				
						// 3. Redis Connection 끝
						
	    			}else{ 	//전송 DATA 없을때.. 
	    				errCnt++;
	    			}
	    			
	    			if(errCnt > 0){
	    				
	    				sqlBuffer3.append(" 사업부["+cocd+"] 전송할 재고정보가 없습니다.");
	    			}else{
	    				
		    			sqlBuffer3.append(" 사업부["+cocd+"] 정상:"+count+"건");	    				
		    		}
	 
	    		}else{
	    			
	    			sqlBuffer3.append(" 사업부["+cocd+"]"+ errmsg);

	    		}	
			}
			
    		if(successCnt > 0){
				sendMessage = "SUCCESS !!!!! ["+sqlBuffer3.toString()+"]";
    		}else{
    			sendMessage = "NO DATA !!!!! [ 송신할 재고정보가 존재하지 않습니다. ]";
    		}   
    		
			
		} catch(SQLException e) {
			
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage = "FAIL!["+e.toString()+"]";	
			
		} catch(JedisConnectionException e) {

			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			sendMessage = "FAIL!!["+e.toString()+"]";			
		
		} catch(Exception e) {
			
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage = "FAIL!!!["+e.toString()+"]";	
			
		} finally {
			
			try 
		    {
				
				if( rs0 !=null ) try{ rs0.close(); rs0 = null; }catch(Exception e){}finally{rs0 = null;}
				if( rs1 !=null ) try{ rs1.close(); rs1 = null; }catch(Exception e){}finally{rs1 = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}

				if( pstmt0 != null ) try{ pstmt0.close(); pstmt0 = null; }catch(Exception e){}finally{pstmt0 = null;}
				if( pstmt1 != null ) try{ pstmt1.close(); pstmt1 = null; }catch(Exception e){}finally{pstmt1 = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				if( cstmt != null ) try{ cstmt.close(); cstmt = null; }catch(Exception e){}finally{cstmt = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}				
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}				
		
		return sendMessage;
	}
		
	/**
	 * 재고 처리결과 수신 SC -> CUBE
	 * @param dbmode
	 * @param vendorID
	 * @param transCD
	 * @return 
	 * @throws SQLException
	 * @throws IOException
	 * @throws JedisConnectionException
	 * @throws JSONException
	 */
	public String api_Auto_RecvItemStock(String dbmode, String transCD) throws SQLException, IOException, JedisConnectionException, JSONException
	{
		// 로그를 찍기 위한 변수 선언
		String methodName ="com.service.ScApiCreateREDIS.api_Auto_RecvItemStock()";
		Logger.debug(methodName);

		/* JDBC Connection 변수 선언 */
		Connection 		conn		= null;
		
		/* PreparedStatement 선언 */
		PreparedStatement	pstmt0		= null; // 쿼리문 	실행
		PreparedStatement	pstmt1		= null; // 주쿼리문 실행

		/* ResultSet 선언 */
		ResultSet			rs0			= null;

		/* StringBuffer 선언*/		
		StringBuffer	sqlBuffer0  = new StringBuffer(1000);	// 사업부 쿼리문
		StringBuffer	sqlBuffer1  = new StringBuffer(1000);	// 주 	  쿼리문
		StringBuffer	sqlBuffer2  = new StringBuffer(500);	// 결과메세지 
		
		/* Redis 선언 */
		Jedis 			jedis   	= null;
		
		/* String 변수선언  */	
		String 	sendMessage = null;		
	    //String  succStr 	= null;
	    
	    /* 숫자형 선언 */
		int successCnt 	= 0;
		
		try {
			
			conn =	DataBaseManager.getConnection(dbmode);		
			conn.setAutoCommit(false);

			Logger.debug("0. Sterling 재고 수신후 UPDATE SQL 작성 시작");
			
			sqlBuffer0.append("SELECT   RETC AS COCD											\n");	
			sqlBuffer0.append("       , CD4  AS VDCD											\n");			
			sqlBuffer0.append("  FROM TBB150					    							\n");	
			sqlBuffer0.append(" WHERE REFTP = 'ZY'												\n");	
			sqlBuffer0.append("   AND REFCD <> '0000'											\n");	
			sqlBuffer0.append("   AND CD4   = '"+ transCD +"'						    		\n");	
			sqlBuffer0.append("   GROUP BY RETC, CD4						    				\n");
			
			/* 0. Sterling 상품 수신후 UPDATE SQL 작성 시작*/
			/* 0-1. 주 쿼리문*/
			sqlBuffer1.append("UPDATE  TBD260              	\n");
			sqlBuffer1.append("    SET STATUS 	   = ?      \n");
			sqlBuffer1.append("        ,STATUS_MSG = ?      \n");
			sqlBuffer1.append("        ,UPDUSER 	= 'SCAPI'                              			\n");
			sqlBuffer1.append("        ,UPDTIME 	= TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS')       	\n");			
			sqlBuffer1.append("WHERE   TRAN_DATE <= ?        \n");
			sqlBuffer1.append("AND     TRAN_SEQ	 <= ?        \n");
			sqlBuffer1.append("AND     BARCODE 	 = ?        \n");
			sqlBuffer1.append("AND     COCD 	 = ?        \n");		
			sqlBuffer1.append("AND     WHCD 	 = ?        \n");
			sqlBuffer1.append("AND     STATUS IN ('00', '05') \n");	
			
			/* 0-2. 서브 쿼리문*/
			/* 0-3. 카운트 쿼리문*/
			
			/* 0. Sterling 상품 수신후 UPDATE SQL 작성 끝*/
			Logger.debug("0. Sterling 재고 수신후 UPDATE SQL 작성  끝");
						
			/* 1. Redis connection 생성 */				
			Logger.debug("1. Redis connection 생성 시작");
			
			jedis = new Jedis(RED_IP, PORT , 12000);
			jedis.connect();
			jedis.select(DB_INDEX);			
			
			Logger.debug("1. Redis connection 생성 끝");
			/* 1. Redis connection 끝 */
			
			pstmt0 = conn.prepareStatement(sqlBuffer0.toString()); // 주쿼리문		
			rs0 = pstmt0.executeQuery();
			
			while(rs0.next()){

				String cocd = StringUtil.nullTo(rs0.getString("COCD"),"");

			    int count 		= 0;
			    int errcnt 		= 0;
				int redisCnt 	= jedis.llen(cocd+RECV_INVENTORY_KEY ).intValue();
				
				Logger.debug("[COCD["+StringUtil.nullTo(rs0.getString("COCD"),"")+"]");		// 사업부코드
				Logger.debug("[VDCD["+StringUtil.nullTo(rs0.getString("VDCD"),"")+"]");		// SHOP_ID
				
				Logger.debug("재고수신-REDIS_KEY["+cocd+RECV_INVENTORY_KEY+"]");
				Logger.debug("재고수신-REDIS_COUNT["+redisCnt +"]");					
				Logger.debug("2. Sterling 재고 처리결과 수신데이터 처리 시작");
				
				/* 2. Sterling 수신데이터 처리 시작 */				
				if(redisCnt > 0){

					for (int j = 0; j < redisCnt; j++){
						
						String  jsonString = StringUtil.nullTo(jedis.rpop(cocd+RECV_INVENTORY_KEY),"");				
						Logger.debug("SC API 재고처리결과 DATA["+jsonString+"]");
						
						String org_code		= "";	// 사업부코드
						String barcode		= "";	// 바코드코드
						String shipNode		= "";	// 창고코드
						String statuscd 	= "";	// 처리상태
						String tranDate 	= "";	// 전송날짜
						String tranSeq 		= "";	// 순번
						
						String jsonData = URLDecoder.decode(jsonString,"UTF-8");
						
						JSONObject 	json 		= JSONObject.fromObject(jsonData);
						JSONArray 	prodArray 	= json.getJSONArray("list");				
		
						/* 2-1. Sterling 수신데이터 파싱 시작*/	
						Logger.debug("2-1. Sterling 수신데이터 파싱 시작");
						Logger.debug(cocd+"_size["+prodArray.size()+"]");
						
						for (int i = 0; i < prodArray.size(); i++){
							
							JSONObject prodList = prodArray.getJSONObject(i);
							
							tranDate 	= StringUtil.nullTo(prodList.getString("tran_date"),""); // 1.[Parameter] 전송날짜
							tranSeq 	= StringUtil.nullTo(prodList.getString("tran_seq"),"");  // 2.[Parameter] 전송순번
							org_code 	= StringUtil.nullTo(prodList.getString("org_code"),"");	 // 3.[Parameter] 사업부코드
							shipNode 	= StringUtil.nullTo(prodList.getString("ship_node"),""); // 4.[Parameter] 창고코드						
							barcode 	= StringUtil.nullTo(prodList.getString("bar_code"),"");	 // 5.[Parameter] 스타일코드
							statuscd 	= StringUtil.nullTo(prodList.getString("statuscd"),"");  // 6.[Parameter] 처리상태
	
							Logger.debug("tranDate["+tranDate+"]");
							Logger.debug("tranSeq["+tranSeq+"]");						
							Logger.debug("org_code["+org_code+"]");
							Logger.debug("shipNode["+shipNode+"]");
							Logger.debug("barcode["+barcode+"]");
							Logger.debug("statuscd["+statuscd+"]");
							
							//품목 전송 결과 업데이트..
							if (pstmt1 != null) { pstmt1.close(); pstmt1 = null; }
							pstmt1 = conn.prepareStatement(sqlBuffer1.toString()); //주 쿼리문	
		
							pstmt1.setString(1, statuscd);
							if(statuscd.equals("01")){
								pstmt1.setString(2, "API 재고등록 성공");
							}else{
								pstmt1.setString(2, "API 재고등록 실패");							
							}
							
							pstmt1.setString(3, tranDate);	// 전송날짜
							pstmt1.setString(4, tranSeq);	// 전송순번						
							pstmt1.setString(5, barcode);   // 바코드
							pstmt1.setString(6, org_code);	// 사업부코드
							pstmt1.setString(7, shipNode);	// 창고코드
							
							pstmt1.executeUpdate();						
													
						}
						Logger.debug("2-1. Sterling 수신데이터 파싱 끝");
						/* 2-1. Sterling 수신데이터 파싱 끝*/
						
						count++;		// 사업부 별 처리 카운트
						successCnt++;	// 전체 처리 카운트
					}
				}else{
					errcnt++;	// 사업부별 실패 카운트
				}
				
    			if(errcnt > 0){
    				
    				sqlBuffer2.append(" 사업부["+cocd+"] 재고처리결과 수신대상이없습니다.");
    			}else{
    				
	    			sqlBuffer2.append(" 사업부["+cocd+"] 정상:"+count+"건");	    				
	    		}
				
				Logger.debug("2. Sterling 재고 처리결과 수신데이터 처리 끝");
				/* 2. Sterling 수신데이터 처리 끝 */
			}
			
			if(successCnt > 0){
				sendMessage = " SUCCESS !!!!! ["+sqlBuffer2+"]";
			}else{
				sendMessage = " NO DATA !!!!! [ 재고처리결과 수신대상이 존재하지 않습니다. ]";				
			}
			
		} catch(SQLException e) {
			
			conn.rollback();			
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());			
			
			sendMessage = "FAIL!["+e.toString()+"]";			
		
		} catch(JedisConnectionException e) {

			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			sendMessage = "FAIL!!["+e.toString()+"]";
			
		} catch(Exception e) {
			
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			sendMessage = "FAIL!!!["+e.toString()+"]";
		
		}
		finally {
			try 
			{
				conn.setAutoCommit(true);	
				if( rs0 !=null ) try{ rs0.close(); rs0 = null; }catch(Exception e){}finally{rs0 = null;}
				
				if(pstmt0  != null ) try{ pstmt0.close(); pstmt0 = null; }catch(Exception e){}finally{pstmt0 = null;}
				if(pstmt1  != null ) try{ pstmt1.close(); pstmt1 = null; }catch(Exception e){}finally{pstmt1 = null;}
				
				DataBaseManager.close(conn, dbmode);
				if(conn	!= null ) try{ conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}		
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
			} 
		    catch (Exception e) 
		    {

		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());						
				sendMessage = "FAIL!!!!["+e.toString()+"]";
		    }
		}

		return sendMessage;
	}
	
	/**
	 * 주문정보 수신 ( 출고의뢰 수신 )
	 * @param dbmode
	 * @param command
	 * @param vendorID
	 * @param transCD
	 * @param sendDomain
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws JedisConnectionException
	 * @throws JSONException
	 */
	public String api_Auto_PO(String dbmode, String processCmd,String transCD) throws SQLException, IOException, JedisConnectionException, JSONException
	{
		String methodName ="com.service.ScApiCreateREDIS.api_Auto_PO()";
		Logger.debug(methodName);
		
		/* JDBC Connection 변수 선언 */		
		Connection 		conn	= null;
		
		/* Redis 선언 */
		Jedis 			jedis   = null;

		/* PreparedStatement 선언 */
		PreparedStatement	pstmt		= null;
		
		List<Object> vendorList 	= null;
		HashMap		 getHm	= new HashMap();
		
		StringBuffer	resultBuffer  	= new StringBuffer(500);	// 결과메세지 
		StringBuffer   	sqlBuffer  		= new StringBuffer(500);	// 서브쿼리문
		
		/* String 변수선언  */			
		String call_seq 	= "";		
		String sendMessage 	= null;
		String succStr 		= "";
		String processNm	= "";

		int	   succCnt		= 0;
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO 	dao = new ServiceDAO();
			
			sqlBuffer.append("UPDATE  API_RECV_LOG				");
			sqlBuffer.append("		   SET  RESULT_CODE  = ? 	");
			sqlBuffer.append("			   ,RESULT_NAME  = ?	");
			sqlBuffer.append("WHERE   CALL_DT  = ?				");
			sqlBuffer.append("AND     CALL_SEQ = ? 				");
			pstmt = conn.prepareStatement(sqlBuffer.toString());
						
			/* RedKEY 조회*/
			vendorList = GetVendorList(dbmode,transCD);
			
			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
			
			Date nowDay = new Date();
			String toDay = sdFormat.format(nowDay);
			
			Logger.debug("vendorList["+vendorList.size()+"]");			
			if (vendorList != null) {
				
				/* 1. Redis connection 생성 */				
				Logger.debug("1. Redis connection 생성 시작");
				
				jedis = new Jedis(RED_IP, PORT , 12000);
				jedis.connect();
				jedis.select(DB_INDEX);			
								
				Logger.debug("1. Redis connection 생성 끝");
				/* 1. Redis connection 끝 */
					
				
				for (int i = 0; i < vendorList.size(); i++) {
					
					getHm = (HashMap)vendorList.get(i);
					
					String org_code 	= StringUtil.nullTo((String)getHm.get("COCD"),"");
					String sell_code    = StringUtil.nullTo((String)getHm.get("VDCD"),"");					
					
					int    orgCnt		= 0;
					int    orgErrCnt	= 0;
					int    redisCnt 	= jedis.llen(org_code+":"+sell_code+RECV_ORDER ).intValue();
					
					Logger.debug("org_code["+org_code+"]");					
					Logger.debug("sell_code["+sell_code+"]");
					Logger.debug("OrderProcess-REDIS_KEY["+org_code+":"+sell_code+RECV_ORDER+"]");									
					Logger.debug("OrderProcess-REDIS_COUNT["+redisCnt+"]");	
					
					Logger.debug("2. Sterling OrderProcess 수신데이터 처리 시작");
					if(redisCnt > 0){
		
						String status			= "";
						String tranDt			= "";						
						String rtCocd			= "";		// 신규
						String rtVendorId		= "";
						String rtShipNode		= "";		// 신규
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
						String rtDeliPrice		= "0";
						String rtQty			= "";
						String rtShipID			= "";
						String rtCancelDt		= "";
						String rtShipStatus 	= "";
						String rtDeliGb 		= "";
						String rtRetCode 		= "";
						String rtOriShipId 		= "";
						String rtClameMemo		= "";
						String rtCubeItem		= "";
						String rtShipKey		= "";		// 신규
						String rtOrderSeqKey	= "";		// 신규						
						String rtOrderKey		= "";		// 신규
						String rtQrder_dt		= "";
						String rtOrder_id		= "";
						String rtVendorNm		= "";		
						//추가 20150828 BY LEE : 매장 직출 기능 추가에 따른 추가
						String rtNodeType		= ""; 
						String strVendor_Pono	= "";		// inserted for PONO [IOS 26-Jan-26]
						
						for (int v = 0; v < redisCnt; v++) {

							String command			= "";							
							String  jsonString = StringUtil.nullTo(jedis.rpop(org_code+":"+sell_code+RECV_ORDER),"");
							Logger.debug("SC API OrderProcess 수신 DATA["+jsonString+"]");					
							
							int    insertErrCnt = 0;

							ServiceDataInfo 	dInfo  	= new ServiceDataInfo();
							
							String jsonData  = URLDecoder.decode(jsonString,"UTF-8");
							
							JSONObject 	jobj = JSONObject.fromObject(jsonData);						
							JSONArray jarray = jobj.getJSONArray("list");
								
							Logger.debug("Sterling OrderProcess 마스터정보 시작");
							Logger.debug("status["+StringUtil.nullTo((String) jobj.get("status"),"")+"]");
							Logger.debug("tranDt["+StringUtil.nullTo((String) jobj.get("tranDt"),"")+"]");
							Logger.debug("orderHeaderKey["+StringUtil.nullTo((String) jobj.get("orderHeaderKey"),"")+"]");
							Logger.debug("orderDt["+StringUtil.nullTo((String) jobj.get("orderDt"),"")+"]");						
							Logger.debug("orderId["+StringUtil.nullTo((String) jobj.get("orderId"),"")+"]");
							Logger.debug("Sterling OrderProcess 마스터정보 끝");	

							rtQrder_dt 	= StringUtil.nullTo((String) jobj.get("orderDt"),"");			// 주문일자
							rtOrder_id	= StringUtil.nullTo((String) jobj.get("orderId"),"");			// 주문번호
							rtOrderKey 	= StringUtil.nullTo((String) jobj.get("orderHeaderKey"),"");	// 전송일자
							status 		= StringUtil.nullTo((String) jobj.get("status"),"");			// 주문상태
							tranDt 		= StringUtil.nullTo((String) jobj.get("tranDt"),"");			// 전송일자
							
							
							if(status.equals(RECV_ORDER_STATUS)){	/* 출고의뢰일때 사용하는 항목 */								
								Logger.debug("vendor_id["+StringUtil.nullTo((String) jobj.get("vendor_id"),"")+"]");
								rtVendorNm  = StringUtil.nullTo((String) jobj.get("vendor_id"),"");
								
								// inserted for Vendor PONO [IOS 26-Jan-16]
								strVendor_Pono = StringUtil.nullTo((String) jobj.get("channelOrderNo"), "");
							}
							
							Logger.debug("처리상태["+status+"]");
							
							if(status.equals(RECV_ORDER_STATUS)){
								command = "OrderRetrieve";			// 주문(출고의뢰) 
							}else if(status.equals(RECV_ORDER_CANCE_STATUS)){
								command = "OrderCancelRetrieve";	// 주문취소
								 		   
							}else{
								command = "NOT PROCESS STATUS";
							}
							
							processNm = cubeDao.getApiName(command);
							
							Logger.debug("처리업무구분["+command+"]");
							if(command.equals("OrderRetrieve") || command.equals("OrderCancelRetrieve")){
							
	
								call_seq = cubeDao.setRecvLog(dbmode, "SCAPI", command, cubeDao.getApiName(command), sell_code, toDay, toDay, "000", "처리중!!!!!", transCD);
								Logger.debug("call_dt["+CommonUtil.getCurrentDate()+"]");
								Logger.debug("call_seq["+call_seq+"]");							
								
								for (int j = 0; j < jarray.size(); j++)
								{
									JSONObject rtList = jarray.getJSONObject(j);
									
									Logger.debug("Sterling OrderProcess 주문확정라인 시작");								
									Logger.debug("org_code["+StringUtil.nullTo(rtList.getString("org_code"),"")+"]");
									Logger.debug("sell_code["+StringUtil.nullTo(rtList.getString("sell_code"),"")+"]");
									Logger.debug("ship_node["+StringUtil.nullTo(rtList.getString("ship_node"),"")+"]");
									Logger.debug("orderDt["+StringUtil.nullTo(rtList.getString("orderDt"),"")+"]");
									Logger.debug("orderId["+StringUtil.nullTo(rtList.getString("orderId"),"")+"]");
									Logger.debug("orderLineNo["+StringUtil.zeroPutStr(3,StringUtil.nullTo(rtList.getString("orderLineNo"),""))+"]");
									Logger.debug("orderLineKey["+StringUtil.nullTo(rtList.getString("orderLineKey"),"")+"]");
									Logger.debug("orderReleaseKey["+StringUtil.nullTo(rtList.getString("orderReleaseKey"),"")+"]");	
									
									if(status.equals(RECV_ORDER_STATUS)){	/* 출고의뢰일때 사용하는 항목 */									
										Logger.debug("receiptNm["+StringUtil.nullTo(rtList.getString("receiptNm"),"")+"]");
										Logger.debug("receiptTel["+StringUtil.nullTo(rtList.getString("receiptTel"),"")+"]");
										Logger.debug("receiptHp["+StringUtil.nullTo(rtList.getString("receiptHp"),"")+"]");
										Logger.debug("receiptAddr1["+StringUtil.nullTo(rtList.getString("receiptAddr1"),"")+"]");
										Logger.debug("receiptAddr2["+StringUtil.nullTo(rtList.getString("receiptAddr2"),"")+"]");
										Logger.debug("receiptZipcode["+StringUtil.nullTo(rtList.getString("receiptZipcode"),"")+"]");
										Logger.debug("custNm["+StringUtil.nullTo(rtList.getString("custNm"),"")+"]");
										Logger.debug("custTel["+StringUtil.nullTo(rtList.getString("custTel"),"")+"]");
										Logger.debug("custHp["+StringUtil.nullTo(rtList.getString("custHp"),"")+"]");	
										Logger.debug("deliveryMsg["+StringUtil.nullTo(rtList.getString("deliveryMsg"),"")+"]");
									}
									
									Logger.debug("itemId["+StringUtil.nullTo(rtList.getString("itemId"),"")+"]");
									Logger.debug("itemNm["+StringUtil.nullTo(rtList.getString("itemNm"),"")+"]");
									Logger.debug("qty["+StringUtil.nullTo(rtList.getString("qty"),"")+"]");
									Logger.debug("salePrice["+StringUtil.nullTo(rtList.getString("salePrice"),"")+"]");

									Logger.debug("Sterling OrderProcess 주문확정라인 끝");						
									
									rtCocd				= StringUtil.nullTo(rtList.getString("org_code"),"");		// 사업부코드						
									rtVendorId			= StringUtil.nullTo(rtList.getString("sell_code"),"");		// 판매채널코드							
									rtShipNode			= StringUtil.nullTo(rtList.getString("ship_node"),"");		// 창고코드	
									
									// 주문일자
									if (command.equals("OrderRetrieve") || command.equals("OrderReturnRetrieve")) {						//주문,반품 정보..
										rtOrderDt		= StringUtil.nullTo(rtList.getString("orderDt"),"");
									} else if (command.equals("OrderCancelRetrieve") || command.equals("OrderReturnCancelRetrieve")) {	//주문취소,반품취소 정보..
										rtCancelDt		= StringUtil.nullTo(rtList.getString("orderDt"),"");
									}
								
									rtOrderId			= StringUtil.nullTo(rtList.getString("orderId"),"");			// 주문번호
									rtOrderSeq			= StringUtil.zeroPutStr(3,StringUtil.nullTo(rtList.getString("orderLineNo"),""));		// 주문순번
									rtOrderSeqKey		= StringUtil.nullTo(rtList.getString("orderLineKey"),"");		// 주문순번키
									rtShipKey			= StringUtil.nullTo(rtList.getString("orderReleaseKey"),"");	// 주문확정키
									
									if(status.equals(RECV_ORDER_STATUS)){								
										/* 수취인정보*/
										rtReceiptNm			= StringUtil.nullTo(rtList.getString("receiptNm"),"");		// 수취인명							
										rtReceiptTel		= StringUtil.nullTo(rtList.getString("receiptTel"),"");		// 수취인전화							
										rtReceiptHp			= StringUtil.nullTo(rtList.getString("receiptHp"),"");		// 수취인휴대폰
										rtReceiptAddr1		= StringUtil.nullTo(rtList.getString("receiptAddr1"),"");		// 수취인주소1					
										rtReceiptAddr2		= StringUtil.nullTo(rtList.getString("receiptAddr2"),"");		// 수취인주소1					
										rtReceiptZipcode	= StringUtil.nullTo(rtList.getString("receiptZipcode"),"");	// 수취인우편번호													
										
										/* 주문자정보*/							
										rtCustNm			= StringUtil.nullTo(rtList.getString("custNm"),"");			// 주문자정보
										rtCustTel			= StringUtil.nullTo(rtList.getString("custTel"),"");			// 주문자전화번호							
										rtCustHp			= StringUtil.nullTo(rtList.getString("custHp"),"");			// 주문자휴대폰							
		
										rtDeliveryMsg		= StringUtil.nullTo(rtList.getString("deliveryMsg"),"");	// 배송메세지
									}
									/* 상품정보*/
									rtItemCd			= StringUtil.nullTo(rtList.getString("itemId"),"");			// 상품코드
									rtItemNm			= StringUtil.nullTo(rtList.getString("itemNm"),"");			// 상품명					
									rtQty				= StringUtil.nullTo(rtList.getString("qty"),"0");			// 수량
									rtSalePrice			= StringUtil.nullTo(rtList.getString("salePrice"),"0");		// 개별판매가격
									rtNodeType			= StringUtil.nullTo(rtList.getString("nodeType"),"");		// 노드유형
	
								
									dInfo.setCall_dt(CommonUtil.getCurrentDate());
									dInfo.setCall_seq(call_seq);
									dInfo.setInuser("SCAPI");
									dInfo.setError_code("00");
									dInfo.setError_msg("SUCCESS");
									dInfo.setSeq(String.valueOf(j+1));
									dInfo.setRecv_gb(cubeDao.getRecvGb(command)); // 10.주문, 20.주문취소, 30.반품, 40.반품취소
									dInfo.setOrderKey(rtOrderKey);									
									dInfo.setTrans_dt(tranDt); 
									dInfo.setCocd(rtCocd);									
									dInfo.setVendor_id(rtVendorId);
									dInfo.setWhcd(rtShipNode);
									dInfo.setInstruct_dt(rtOrderDt);
									dInfo.setCancel_dt(rtCancelDt);																	
									dInfo.setFirst_order_id(rtOrderId); 
									dInfo.setOrder_id(rtOrderId);
									dInfo.setOrder_seq(rtOrderSeq);  
									dInfo.setShip_seq(rtOrderSeq);
									dInfo.setOrderSeqKey(rtOrderSeqKey);								
									dInfo.setShip_id(rtShipKey);  
									dInfo.setShipKey(rtShipKey);																
									dInfo.setChange_gb(rtChangeGb);
									dInfo.setShip_status(rtShipStatus); 
									dInfo.setReceipt_nm(rtReceiptNm);  
									dInfo.setReceipt_tel(rtReceiptTel);
									dInfo.setReceipt_hp(rtReceiptHp);
									dInfo.setReceipt_addr1(rtReceiptAddr1);   
									dInfo.setReceipt_addr2(rtReceiptAddr2);  								
									dInfo.setReceipt_zipcode(rtReceiptZipcode); 
									dInfo.setCust_nm(rtCustNm);   
									dInfo.setCust_tel(rtCustTel);  
									dInfo.setCust_hp(rtCustHp);  
									dInfo.setCust_zipcode(rtCustZipcode);    
									dInfo.setCust_addr1(rtCustAddr1);      
									dInfo.setCust_addr2(rtCustAddr2);  															
									dInfo.setDelivery_msg(rtDeliveryMsg);
									dInfo.setItem_cd(rtItemCd);   
									dInfo.setItem_nm(rtItemNm);  								
									dInfo.setQty(rtQty);
									dInfo.setOption1(rtOption1);         
									dInfo.setOption2(rtOption2);         
									dInfo.setDeli_gb(rtDeliGb);								
									dInfo.setRet_code(rtRetCode);
									dInfo.setDeli_price(rtDeliPrice);
									dInfo.setSale_price(rtSalePrice);								
									dInfo.setOri_ship_id(rtOriShipId);
									dInfo.setCust_email(rtCustEmail);
									dInfo.setClame_memo(rtClameMemo);
									dInfo.setCube_item(rtCubeItem);
									dInfo.setVendorNm(rtVendorNm);
									//2015.08.31  by lee
									dInfo.setNodeType(rtNodeType);
									dInfo.setVendor_Pono(strVendor_Pono);	// inserted [IOS 26-Jan-16]
									
									/* 2-1. API_RECV_DATA INSERT 요청 시작 */
									Logger.debug("2-1. API_RECV_DATA INSERT 요청 시작");
									
									int result = dao.setRecvData(conn, dInfo, transCD);
	
									Logger.debug("result["+result+"]");
									Logger.debug("2-1. API_RECV_DATA INSERT 요청 끝");
									/* 2-1. API_RECV_DATA INSERT 요청 끝 */
									
									if(result == 0){
										insertErrCnt++;
										break;
									}									
									
								}
	
								/* 2-2. callProcedure 요청 시작 */
								Logger.debug("2-2. callProcedure 요청 시작");
								Logger.debug("insertErrCnt["+insertErrCnt+"]");	
								
								// 정상
								if(insertErrCnt == 0){
										
									Logger.debug("API_RECV_DATA INSERT 정상");
	
									//전송대상 BARCODE 전송 결과 업데이트..
									if (pstmt != null) { pstmt.close(); pstmt = null; }
									pstmt = conn.prepareStatement(sqlBuffer.toString());
									
									pstmt.setString(1, "000");
									pstmt.setString(2, "SUCCESS!");
									pstmt.setString(3, CommonUtil.getCurrentDate());
									pstmt.setString(4, call_seq);
	
									pstmt.executeUpdate();
									

									Logger.debug(" 프로시져 콜 시작");
									//resultBuffer.append("{"+processNm+"}사업부["+org_code+":"+sell_code+"]SUCESS!");							
									//cubeDao.callProcedure(dbmode, command, CommonUtil.getCurrentDate(), call_seq, rtOrderKey, tranDt, rtOrder_id, transCD, rtQrder_dt, org_code, sell_code);
									cubeDao.callProcedure2(dbmode, command, CommonUtil.getCurrentDate(), call_seq, rtOrderKey, tranDt, rtOrder_id, transCD, rtQrder_dt, org_code, sell_code, dInfo);
									Logger.debug(" 프로시져 콜 끝");
									
									orgCnt++; // 사업부별 정상카운트
									succCnt++;
									
								}else{ // API_RECV_DATA TABLE INSERT 오류시!!!!!
	
									Logger.debug("API_RECV_DATA INSERT 실패");
									//resultBuffer.append("{"+processNm+"}사업부["+org_code+":"+sell_code+"]API_RECV_DATA INSERT ERROR!["+rtOrderId+"]");
									
									//전송대상 BARCODE 전송 결과 업데이트..
									if (pstmt != null) { pstmt.close(); pstmt = null; }
									pstmt = conn.prepareStatement(sqlBuffer.toString());
									
									pstmt.setString(1, "100");
									pstmt.setString(2, "API_RECV_DATA INSERT ERROR![주문번호:"+rtOrderId+"]");
									pstmt.setString(3, CommonUtil.getCurrentDate());
									pstmt.setString(4, call_seq);
	
									pstmt.executeUpdate();
									
									Logger.debug("2-3. 주문연동처리시 에러처리");
									jedis.lpush(org_code+":"+sell_code+ORDER_ERROR,jsonString);
									orgErrCnt++;
								}
								
								Logger.debug("2-2. callProcedure 요청 끝");
								/* 2-2. callProcedure 요청 끝 */
								
							}else{
															
								//resultBuffer.append("사업부["+org_code+":"+sell_code+"] 주문/주문취소 프로세스 진행상태가 아닙니다. ");
								cubeDao.setRecvLog(dbmode, "scAPI", command, "주문번호["+rtOrder_id+"]가 처리가능한상태["+status+"]가 아닙니다", sell_code, toDay, toDay, "100", "FAIL!(처리상태오류)", transCD);
	
								Logger.debug("2-3. 주문연동처리시 처리상태오류");
								jedis.lpush(org_code+":"+sell_code+ORDER_ERROR,jsonString);
								
								orgErrCnt++;
							}							
							
						} // end-for (REDIS)
						
						resultBuffer.append("사업부["+org_code+":"+sell_code+"]처리("+orgCnt+")건/실패("+orgErrCnt+")건  ");						
						
					} else {
						
						resultBuffer.append(" 사업부["+org_code+":"+sell_code+"]NO DATA! 주문내역없음.  ");
						succStr = "조회된데이터가 없습니다.";
						cubeDao.setRecvLog(dbmode, "SCAPI", processCmd, "발주요청",sell_code, toDay, toDay, "100", succStr, transCD);
					}
					
					
					Logger.debug("2. Sterling OrderProcess 수신데이터 처리 끝");
					/* 2. Sterling 출고의로 수신데이터 처리 끝 */
				}
												
				if(succCnt > 0){
					succStr = "SUCCES!";
				}else{
					succStr = "FAIL!";
				}
				

			} else {
				succStr = "FAIL!.";
				resultBuffer.append("NO DATA!! 조회된 사업부가 없습니다.");		
			}
			
			sendMessage = succStr+resultBuffer.toString();
			
		} catch(SQLException e) {
			
			conn.rollback();			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
			
		} catch(JedisConnectionException e) {

			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());			
			sendMessage = "FAIL!!["+e.toString()+"]";
					
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();

			
		} finally {
			
			try 
		    {
				conn.setAutoCommit(true);	
				
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
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
	
	/**
	 * 출고의뢰 결과 송신
	 * @param dbmode
	 * @param command
	 * @param call_dt
	 * @param call_seq
	 * @param tranDt
	 * @param transCD
	 * @param orderId
	 * @param orderHeaderKey
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws JSONException
	 */  //redisDao.api_Auto_PO_Send     (dbmode,        call_api,       call_dt,        call_seq ,       tranDt,        transcd ,       tranSeq ,        cmID ,          connip,                cmPassKey ,      scProcKey);
	public String api_Auto_PO_Send(String dbmode, String command, String call_dt, String call_seq, String tranDt, String transCD, String orderId , String orderDt, String orderHeaderKey, String orgCode , String sell_Code) 
	        throws SQLException, IOException, JedisConnectionException, JSONException
	{
		String methodName ="com.service.ScApiCreateREDIS.api_Auto_PO_Send()";
		Logger.debug(methodName);
		
		/* JDBC Connection 변수 선언 */			
		Connection 			conn		= null;
		
		/* PreparedStatement 선언 */		
		PreparedStatement	pstmt1		= null;
		
		/* ResultSet 선언 */		
		ResultSet			rs			= null;
		
		/* Redis 선언 */
		Jedis 				jedis   	= null;
		
		/* StringBuffer 선언 */		
		StringBuffer   		sqlBuffer1  = new StringBuffer(1000);	//주 쿼리문
		
		/* String 변수선언 */		
		String 	sendMessage 	= null;
		String 	status 			= "";
		String 	sendCommand 	= "";		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();

			
			/* 0. Redis 출고의로 전송 SQL 생성 */	
			sqlBuffer1.append("SELECT   WHCD                   			\n");
			sqlBuffer1.append("        ,ORDER_SEQ                       \n");
			sqlBuffer1.append("        ,ORDERSEQ_KEY					\n");
			sqlBuffer1.append("        ,SHIP_KEY						\n");			
			sqlBuffer1.append("        ,ITEM_CD							\n");		
			sqlBuffer1.append("        ,ERROR_CODE						\n");
			sqlBuffer1.append("        ,ERROR_MSG						\n");
			sqlBuffer1.append("        ,RENO							\n");
			sqlBuffer1.append("        ,QTY								\n");
			sqlBuffer1.append("FROM    API_RECV_DATA					\n");
			sqlBuffer1.append("WHERE   CALL_DT  = ?						\n");
			sqlBuffer1.append("AND     CALL_SEQ = ?						\n");
			sqlBuffer1.append("AND     RECV_GB  = ?						\n");
			sqlBuffer1.append("AND     TRANS_STATUS = ?					\n");

			
			Logger.debug("----- 출고의뢰결과 조회조건 -----");			
			Logger.debug("call_dt["+call_dt+"]");
			Logger.debug("call_seq["+call_seq+"]");
			Logger.debug("command["+cubeDao.getRecvGb(command)+"]");
			Logger.debug("transCD["+transCD+"]");
			Logger.debug("----- -------------------------- -----");
			
			pstmt1 = conn.prepareStatement(sqlBuffer1.toString());

			pstmt1.setString(1, call_dt);
			pstmt1.setString(2, call_seq);
			pstmt1.setString(3, cubeDao.getRecvGb(command));
			pstmt1.setString(4, transCD);
			rs = pstmt1.executeQuery();
			
			// 주문
			if(command.equals("OrderRetrieve")){
				
				status 		= SEND_ORDER_STATUS;
				sendCommand = "OrderRetrieveCheck";
			
			// 주문취소
			}else if(command.equals("OrderCancelRetrieve")){
				
				status 		= SEND_ORDER_CANCE_STATUS;
				sendCommand = "OrderRetrieveCancelCheck";
				
			// 반품
			}else if(command.equals("OrderReturnRetrieve")){
				
				status 		= SEND_ORDER_RETURN_STATUS;
				sendCommand = "OrderReturnRetrieveCheck";
				
			// 반품취소	
			}else if(command.equals("OrderReturnCancelRetrieve")){
				
				status 		= SEND_ORDER_RETURN_CANCE_STATUS;
				sendCommand = "OrderReturnCancelRetrieveCheck";
				
			}
			
			JSONObject jsonObject = new JSONObject();
			JSONArray cell = new JSONArray();

			Logger.debug("----- 출고의뢰결과 송신 헤더정보 -----");			
			Logger.debug("org_code["+orgCode+"]");
			Logger.debug("sell_code["+sell_Code+"]");
			Logger.debug("orderDt["+orderDt+"]");
			Logger.debug("orderId["+orderId+"]");
			Logger.debug("orderHeaderKey["+orderHeaderKey+"]");
			Logger.debug("tranDt["+tranDt+"]");
			Logger.debug("----- -------------------------- -----");
			
			jsonObject.put("org_code", orgCode);
			jsonObject.put("sell_code", sell_Code);
			jsonObject.put("orderDt", orderDt);
			jsonObject.put("orderId", orderId);
			jsonObject.put("orderHeaderKey", orderHeaderKey);
			jsonObject.put("tranDt", tranDt);			
			jsonObject.put("status", status);

			
			while(rs.next())
			{
				JSONObject asrrotList = new JSONObject();

				Logger.debug("----- 출고의뢰결과 송신 디데일 정보 -----");	
				Logger.debug("ship_node["+StringUtil.nullTo(rs.getString("WHCD"),"")+"]");				
				Logger.debug("orderLineNo["+StringUtil.nullTo(rs.getString("ORDER_SEQ"),"")+"]");
				Logger.debug("orderLineKey["+StringUtil.nullTo(rs.getString("ORDERSEQ_KEY"),"")+"]");
				Logger.debug("orderReleaseKey["+StringUtil.nullTo(rs.getString("SHIP_KEY"),"")+"]");
				Logger.debug("itemId["+StringUtil.nullTo(rs.getString("ITEM_CD"),"")+"]");				
				Logger.debug("statuscd["+StringUtil.nullTo(rs.getString("ERROR_CODE"),"")+"]");		
				Logger.debug("statusMsg["+StringUtil.nullTo(rs.getString("ERROR_MSG"),"")+"]");	
				Logger.debug("shipmentNo["+StringUtil.nullTo(rs.getString("RENO"),"")+"]");	
				Logger.debug("qty["+StringUtil.nullTo(rs.getString("QTY"),"")+"]");	
				Logger.debug("----- -------------------------- -----");	
				
				asrrotList.put("ship_node", StringUtil.nullTo(rs.getString("WHCD"),""));
				asrrotList.put("orderLineNo", StringUtil.nullTo(rs.getString("ORDER_SEQ"),""));
				asrrotList.put("orderLineKey", StringUtil.nullTo(rs.getString("ORDERSEQ_KEY"),""));
				asrrotList.put("orderReleaseKey", StringUtil.nullTo(rs.getString("SHIP_KEY"),""));
				asrrotList.put("shipmentNo", StringUtil.nullTo(rs.getString("RENO"),""));
				asrrotList.put("itemId", StringUtil.nullTo(rs.getString("ITEM_CD"),""));
				asrrotList.put("uom", "EACH");
				asrrotList.put("qty", StringUtil.nullTo(rs.getString("QTY"),""));
				asrrotList.put("statuscd", StringUtil.nullTo(rs.getString("ERROR_CODE"),""));
				asrrotList.put("statusMsg", StringUtil.nullTo(rs.getString("ERROR_MSG"),""));
				
				cell.add(asrrotList);
			}
		
			jsonObject.put("list", cell);
			
			/* 1. Redis connection 생성 */				
			Logger.debug("1. Redis connection 생성 시작");
			
			jedis = new Jedis(RED_IP, PORT , 12000);
			jedis.connect();
			jedis.select(DB_INDEX);			
			
			// 주문 / 주문취소
			if(command.equals("OrderRetrieve") || command.equals("OrderCancelRetrieve")){
				
				Logger.debug("[출고의뢰결과SEND_KEY]"+orgCode+":"+sell_Code+SEND_ORDER);
				/* 3-1 Steling OMS 전송할 재고정보 SET */				
				/* SET */  				 
				jedis.lpush(orgCode+":"+sell_Code+SEND_ORDER, jsonObject.toString());
				
			// 반품 / 반품취소
			}else if(command.equals("OrderReturnRetrieve") || command.equals("OrderReturnCancelRetrieve") ){
				
				Logger.debug("[반품의뢰결과SEND_KEY]"+orgCode+":"+sell_Code+SEND_ORDER_RETURN);				
				/* 3-1 Steling OMS 전송할 재고정보 SET */				
				/* SET */  				 
				jedis.lpush(orgCode+":"+sell_Code+SEND_ORDER_RETURN, jsonObject.toString());
				
			}
						
			Logger.debug("1. Redis connection 생성 끝");
			/* 1. Redis connection 끝 */
			
			CubeService.setSendLog(dbmode, "SCAPI", sendCommand, CubeService.getApiName(sendCommand), sell_Code, orderId, "N/A", "N/A", "N/A", "N/A", "000", "SUCCESS!", "00", transCD);
			
		} catch(SQLException e) {
			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
			
		} catch(JedisConnectionException e) {

			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());			
			sendMessage = "FAIL!!["+e.toString()+"]";			
			
		} catch(Exception e) {
			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}			
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {

			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( pstmt1 != null ) try{ pstmt1.close(); pstmt1 = null; }catch(Exception e){}finally{pstmt1 = null;}
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
				
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
	
	/**
	 * 출고의뢰 결과 송신2 (ServiceDataInfo dInfo 추가 2015.03.06 하윤식)
	 * @param dbmode
	 * @param command
	 * @param call_dt
	 * @param call_seq
	 * @param tranDt
	 * @param transCD
	 * @param orderId
	 * @param orderHeaderKey
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws JSONException
	 */ 
	public String api_Auto_PO_Send2(String dbmode, String command, String call_dt, String call_seq, String tranDt, String transCD, String orderId, String orderDt, String orderHeaderKey, String orgCode, String sell_Code, ServiceDataInfo dInfo) 
	        throws SQLException, IOException, JedisConnectionException, JSONException
	{
		String methodName ="com.service.ScApiCreateREDIS.api_Auto_PO_Send()";
		Logger.debug(methodName);
		
		/* JDBC Connection 변수 선언 */			
		Connection 			conn		= null;
		
		/* PreparedStatement 선언 */		
		PreparedStatement	pstmt1		= null;
		
		/* ResultSet 선언 */		
		ResultSet			rs			= null;
		
		/* Redis 선언 */
		Jedis 				jedis   	= null;
		
		/* StringBuffer 선언 */		
		StringBuffer   		sqlBuffer1  = new StringBuffer(1000);	//주 쿼리문
		
		/* String 변수선언 */		
		String 	sendMessage 	= null;
		String 	status 			= "";
		String 	sendCommand 	= "";		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();

			
			/* 0. Redis 출고의로 전송 SQL 생성 */	
			sqlBuffer1.append("SELECT   WHCD                   			\n");
			sqlBuffer1.append("        ,ORDER_SEQ                       \n");
			sqlBuffer1.append("        ,ORDERSEQ_KEY					\n");
			sqlBuffer1.append("        ,SHIP_KEY						\n");			
			sqlBuffer1.append("        ,ITEM_CD							\n");		
			sqlBuffer1.append("        ,ERROR_CODE						\n");
			sqlBuffer1.append("        ,ERROR_MSG						\n");
			sqlBuffer1.append("        ,RENO							\n");
			sqlBuffer1.append("        ,QTY								\n");
			sqlBuffer1.append("FROM    API_RECV_DATA					\n");
			sqlBuffer1.append("WHERE   CALL_DT  = ?						\n");
			sqlBuffer1.append("AND     CALL_SEQ = ?						\n");
			sqlBuffer1.append("AND     RECV_GB  = ?						\n");
			sqlBuffer1.append("AND     TRANS_STATUS = ?					\n");

			
			Logger.debug("----- 출고의뢰결과 조회조건 -----");			
			Logger.debug("call_dt["+call_dt+"]");
			Logger.debug("call_seq["+call_seq+"]");
			Logger.debug("command["+cubeDao.getRecvGb(command)+"]");
			Logger.debug("transCD["+transCD+"]");
			Logger.debug("----- -------------------------- -----");
			
			pstmt1 = conn.prepareStatement(sqlBuffer1.toString());

			pstmt1.setString(1, call_dt);
			pstmt1.setString(2, call_seq);
			pstmt1.setString(3, cubeDao.getRecvGb(command));
			pstmt1.setString(4, transCD);
			rs = pstmt1.executeQuery();
			
			// 주문
			if(command.equals("OrderRetrieve")){
				
				status 		= SEND_ORDER_STATUS;
				sendCommand = "OrderRetrieveCheck";
			
			// 주문취소
			}else if(command.equals("OrderCancelRetrieve")){
				
				status 		= SEND_ORDER_CANCE_STATUS;
				sendCommand = "OrderRetrieveCancelCheck";
				
			// 반품
			}else if(command.equals("OrderReturnRetrieve")){
				
				status 		= SEND_ORDER_RETURN_STATUS;
				sendCommand = "OrderReturnRetrieveCheck";
				
			// 반품취소	
			}else if(command.equals("OrderReturnCancelRetrieve")){
				
				status 		= SEND_ORDER_RETURN_CANCE_STATUS;
				sendCommand = "OrderReturnCancelRetrieveCheck";
				
			}
			
			JSONObject jsonObject = new JSONObject();
			JSONArray cell = new JSONArray();

			Logger.debug("----- 출고의뢰결과 송신 헤더정보 -----");			
			Logger.debug("org_code["+orgCode+"]");
			Logger.debug("sell_code["+sell_Code+"]");
			Logger.debug("orderDt["+orderDt+"]");
			Logger.debug("orderId["+orderId+"]");
			Logger.debug("orderHeaderKey["+orderHeaderKey+"]");
			Logger.debug("tranDt["+tranDt+"]");
			Logger.debug("----- -------------------------- -----");
			
			jsonObject.put("org_code", orgCode);
			jsonObject.put("sell_code", sell_Code);
			jsonObject.put("orderDt", orderDt);
			jsonObject.put("orderId", orderId);
			jsonObject.put("orderHeaderKey", orderHeaderKey);
			jsonObject.put("tranDt", tranDt);			
			jsonObject.put("status", status);

			
			while(rs.next())
			{
				JSONObject asrrotList = new JSONObject();

				Logger.debug("----- 출고의뢰결과 송신 디데일 정보 -----");	
				Logger.debug("ship_node["+StringUtil.nullTo(rs.getString("WHCD"),"")+"]");				
				Logger.debug("orderLineNo["+StringUtil.nullTo(rs.getString("ORDER_SEQ"),"")+"]");
				Logger.debug("orderLineKey["+StringUtil.nullTo(rs.getString("ORDERSEQ_KEY"),"")+"]");
				Logger.debug("orderReleaseKey["+StringUtil.nullTo(rs.getString("SHIP_KEY"),"")+"]");
				Logger.debug("itemId["+StringUtil.nullTo(rs.getString("ITEM_CD"),"")+"]");				
				Logger.debug("statuscd["+StringUtil.nullTo(rs.getString("ERROR_CODE"),"")+"]");		
				Logger.debug("statusMsg["+StringUtil.nullTo(rs.getString("ERROR_MSG"),"")+"]");	
				Logger.debug("shipmentNo["+StringUtil.nullTo(rs.getString("RENO"),"")+"]");	
				Logger.debug("qty["+StringUtil.nullTo(rs.getString("QTY"),"")+"]");	
				Logger.debug("----- -------------------------- -----");	
				
				asrrotList.put("ship_node", StringUtil.nullTo(rs.getString("WHCD"),""));
				asrrotList.put("orderLineNo", StringUtil.nullTo(rs.getString("ORDER_SEQ"),""));
				asrrotList.put("orderLineKey", StringUtil.nullTo(rs.getString("ORDERSEQ_KEY"),""));
				asrrotList.put("orderReleaseKey", StringUtil.nullTo(rs.getString("SHIP_KEY"),""));
				asrrotList.put("shipmentNo", StringUtil.nullTo(rs.getString("RENO"),""));
				asrrotList.put("itemId", StringUtil.nullTo(rs.getString("ITEM_CD"),""));
				asrrotList.put("uom", "EACH");
				asrrotList.put("qty", StringUtil.nullTo(rs.getString("QTY"),""));
				asrrotList.put("statuscd", StringUtil.nullTo(rs.getString("ERROR_CODE"),""));
				asrrotList.put("statusMsg", StringUtil.nullTo(rs.getString("ERROR_MSG"),""));
				
				cell.add(asrrotList);
			}
		
			jsonObject.put("list", cell);
			
			/* 1. Redis connection 생성 */				
			Logger.debug("1. Redis connection 생성 시작");
			
			jedis = new Jedis(RED_IP, PORT , 12000);
			jedis.connect();
			jedis.select(DB_INDEX);			
			
			// 주문 / 주문취소
			if(command.equals("OrderRetrieve") || command.equals("OrderCancelRetrieve")){
				
				Logger.debug("[출고의뢰결과SEND_KEY]"+orgCode+":"+sell_Code+SEND_ORDER);
				/* 3-1 Steling OMS 전송할 재고정보 SET */				
				/* SET */  				 
				jedis.lpush(orgCode+":"+sell_Code+SEND_ORDER, jsonObject.toString());
				
			// 반품 / 반품취소
			}else if(command.equals("OrderReturnRetrieve") || command.equals("OrderReturnCancelRetrieve") ){
				
				Logger.debug("[반품의뢰결과SEND_KEY]"+orgCode+":"+sell_Code+SEND_ORDER_RETURN);				
				/* 3-1 Steling OMS 전송할 재고정보 SET */				
				/* SET */  				 
				jedis.lpush(orgCode+":"+sell_Code+SEND_ORDER_RETURN, jsonObject.toString());
				
			}
			
			Logger.debug("return json data ==>" + jsonObject.toString());
			Logger.debug("1. Redis connection 생성 끝");
			/* 1. Redis connection 끝 */
			
			//cubeDao.setSendLog(dbmode, "SCAPI", sendCommand, cubeDao.getApiName(sendCommand), sell_Code, orderId, "N/A", "N/A", "N/A", "N/A", "000", "SUCCESS!", "00", transCD);
			cubeDao.setSendLog2(dbmode, "SCAPI", sendCommand, cubeDao.getApiName(sendCommand), sell_Code, orderId, "N/A", "N/A", "N/A", "N/A", "000", "SUCCESS!", "00", transCD, dInfo);
			
		} catch(SQLException e) {
			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
			
		} catch(JedisConnectionException e) {

			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());			
			sendMessage = "FAIL!!["+e.toString()+"]";			
			
		} catch(Exception e) {
			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}			
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {

			
			try 
		    {
				if( rs !=null ) try{ rs.close(); rs = null; }catch(Exception e){}finally{rs = null;}
				if( pstmt1 != null ) try{ pstmt1.close(); pstmt1 = null; }catch(Exception e){}finally{pstmt1 = null;}
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
				
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

	/**
	 * 출고확정 송신
	 * @param dbmode
	 * @param command
	 * @param transCD
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws JedisConnectionException
	 * @throws JSONException
	 */
	public String api_Delivery_Send(String dbmode, String command, String transCD) throws SQLException, IOException, JedisConnectionException, JSONException
	{
		String methodName ="com.service.ScApiCreateREDIS.api_Delivery_Send()";
		Logger.debug(methodName);
		
		/* JDBC Connection 변수 선언 */			
		Connection 			conn		= null;
		
		/* PreparedStatement 선언 */				
		/* ResultSet 선언 */		
		
		/* Redis 선언 */
		Jedis 				jedis   	= null;
		
		/* List , HashMap 등 */
		List<Object> vendorList 		= null;
		HashMap		 getHm				= new HashMap();
		
		/* StringBuffer 선언 */		
		StringBuffer	resultBuffer  	= new StringBuffer(500);	// 결과메세지 
		
		/* String 변수선언 */		
		String 	sendMessage 	= null;
		String 	call_dt 		= "";
		String 	call_seq 		= "";
		String 	call_header 	= "DeliveryHeaderInsert";
		String 	call_detail 	= "DeliveryDetailInsert";
		
		int totalCnt = 0;
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO 	dao = new ServiceDAO();

			vendorList = GetVendorList(dbmode,transCD);
			Logger.debug("vendorList["+vendorList.size()+"]");
			
			if (vendorList != null) {			
								
				/* 1. Redis connection 생성 */				
				Logger.debug("0. Redis connection 생성 시작");
				
				jedis = new Jedis(RED_IP, PORT , 12000);
				jedis.connect();
				jedis.select(DB_INDEX);			
					
			
				Logger.debug("0. Redis connection 생성 끝");
				/* 1. Redis connection 끝 */
								
				for (int i = 0; i < vendorList.size(); i++) {
					
					getHm = (HashMap)vendorList.get(i);
					
					String org_code 	= StringUtil.nullTo((String)getHm.get("COCD"),"");
					String sell_code    = StringUtil.nullTo((String)getHm.get("VDCD"),"");
					
					int orgCnt = 0;
					int errCnt = 0;

					List<Object> getOrderSendData 	= null;	
					Map<String, String> vMap 		= null;					
					getOrderSendData = dao.getOrderSendData(conn, call_header, org_code, sell_code, transCD);
			
					Logger.debug("1. 출고확정 송신 처리 시작");			
					Logger.debug("출고확정 송신건수["+getOrderSendData.size()+"]");
					
					if (getOrderSendData.size() > 0) {						
						
						for (int v = 0; v < getOrderSendData.size(); v++) {
		
							Logger.debug("1-1. 출고확정 송신데이터 HEARD 파싱 시작");										
							
							JSONObject jsonObject = new JSONObject();
							JSONArray cell = new JSONArray();
							
							String cocd 			= ""; 		// 사업부코드
							String vendorId 		= "";  		// 판매채널코드
							String pono 			= "";  		// 주문번호			
							String orderHeaderKey 	= "";  		// 주문번호키
							
														
							vMap = (HashMap<String, String>) getOrderSendData.get(v);
		
							Logger.debug("COCD["+StringUtil.nullTo(vMap.get("COCD"),"")+"]");
							Logger.debug("VENDOR_ID["+StringUtil.nullTo(vMap.get("VENDOR_ID"),"")+"]");
							Logger.debug("PONO["+StringUtil.nullTo(vMap.get("PONO"),"")+"]");
							Logger.debug("ORDERHEADERKEY["+StringUtil.nullTo(vMap.get("ORDERHEADERKEY"),"")+"]");
							
							cocd 				= StringUtil.nullTo(vMap.get("COCD"),"");
							vendorId 			= StringUtil.nullTo(vMap.get("VENDOR_ID"),"");
							pono 				= StringUtil.nullTo(vMap.get("PONO"),"");
							orderHeaderKey 		= StringUtil.nullTo(vMap.get("ORDERHEADERKEY"),"");
							
							jsonObject.put("org_code", cocd);
							jsonObject.put("sell_code", vendorId);
							jsonObject.put("orderId", pono);
							jsonObject.put("orderHeaderKey", orderHeaderKey);
							jsonObject.put("tranDt", CommonUtil.getCurrentDate());			
							jsonObject.put("status", SEND_DELIVERY_STATUS);
							
							
							List<Object> getOrderDetailData 	= null;	
							Map<String, String> jMap 		= null;					
							getOrderDetailData = dao.getOrderSendData(conn, call_detail, org_code, sell_code, pono);
							
							for (int j = 0; j < getOrderDetailData.size(); j++) {
								
								Logger.debug("1-2. 출고확정 송신데이터 DETAIL 파싱 시작");									
								
								JSONObject asrrotList = new JSONObject();							
								
								String whcd 			= ""; 		// 창고코드
								String orderSeq 		= ""; 		// 주문순번
								String orderLineKey	 	= ""; 		// 주문순번키
								String orderReleaseKey 	= "";		// 주문확정키
								String reno	 			= "";		// 출고의뢰번호
								String barCode		 	= "";		// 바코드
								String outDt 			= "";		// 출고일자
								String outTime 			= "";		// 출고시간
								String expNm 			= "";		//
								String expNo 			= "";		//
								
								jMap = (HashMap<String, String>) getOrderDetailData.get(j);
								
								Logger.debug("WHCD["+StringUtil.nullTo(jMap.get("WHCD"),"")+"]");
								Logger.debug("ORDER_SEQ["+StringUtil.nullTo(String.valueOf(jMap.get("ORDER_SEQ")),"")+"]");
								Logger.debug("ORDERLINEKEY["+StringUtil.nullTo(jMap.get("ORDERLINEKEY"),"")+"]");
								Logger.debug("ORDERRELEASEKEY["+StringUtil.nullTo(jMap.get("ORDERRELEASEKEY"),"")+"]");
								Logger.debug("RENO["+StringUtil.nullTo(jMap.get("RENO"),"")+"]");
								Logger.debug("OUTDT["+StringUtil.nullTo(jMap.get("OUTDT"),"")+"]");
								Logger.debug("UPDTIME["+StringUtil.nullTo(jMap.get("UPDTIME"),"")+"]");
								Logger.debug("OUTDT["+StringUtil.nullTo(jMap.get("OUTDT"),"")+"]");
								Logger.debug("EXPNM["+StringUtil.nullTo(jMap.get("EXPNM"),"")+"]");
								Logger.debug("EXPNO["+StringUtil.nullTo(jMap.get("EXPNO"),"")+"]");
								
								whcd				= StringUtil.nullTo(jMap.get("WHCD"),"");
								orderSeq 			= StringUtil.nullTo(String.valueOf(jMap.get("ORDER_SEQ")),"");
								orderLineKey 		= StringUtil.nullTo(jMap.get("ORDERLINEKEY"),"");
								orderReleaseKey 	= StringUtil.nullTo(jMap.get("ORDERRELEASEKEY"),"");
								reno 				= StringUtil.nullTo(jMap.get("RENO"),"");
								outDt 				= StringUtil.nullTo(jMap.get("OUTDT"),"");
								outTime 			= StringUtil.nullTo(jMap.get("OUTTIME"),"");
								expNm 				= StringUtil.nullTo(jMap.get("EXPNM"),"");
								expNo 				= StringUtil.nullTo(jMap.get("EXPNO"),"");
								
								asrrotList.put("ship_node", whcd);
								asrrotList.put("orderLineNo", orderSeq);
								asrrotList.put("orderLineKey", orderLineKey);
								asrrotList.put("orderReleaseKey", orderReleaseKey);
								asrrotList.put("shipmentNo", reno);
								asrrotList.put("expnm", expNm);
								asrrotList.put("expNo", expNo);
								asrrotList.put("outDt", outDt);
								asrrotList.put("outTime", outTime);	
								
								cell.add(asrrotList);
								
								Logger.debug("1-2.1. 출고확정 송신 SEND_LOG 시작");
								CubeService.setSendLog(dbmode, "SCAPI", command, CubeService.getApiName(command), vendorId, orderReleaseKey, "N/A", "N/A", "N/A", "N/A", "000", "SUCCESS!", "00", transCD);

								Logger.debug("1-2.1. 출고확정 송신 SEND_LOG 끝");
								
								Logger.debug("1-2. 출고확정 송신데이터 DETAIL 파싱 끝");
							}

							jsonObject.put("list", cell);													
							
							Logger.debug("[출고확정SEND_KEY]"+org_code+":"+sell_code+SEND_DELIVERY);							
							
							Logger.debug("1-3. 출고확정 송신 REDIS 시작");	
							jedis.lpush(org_code+":"+sell_code+SEND_DELIVERY, jsonObject.toString());			
							Logger.debug("1-3. 출고확정 송신 REDIS 끝");							
														
							orgCnt++;
							totalCnt++;
							Logger.debug("1-1. 출고확정 송신데이터 HEARD 파싱 끝");							
						}						
						
					} else {
						CubeService.setSendLog(dbmode, "SCAPI", command, CubeService.getApiName(command), sell_code, "N/A", "N/A", "N/A", "N/A", "N/A", "100", "연동할 대상 정보가 없습니다.","00", transCD);
						errCnt++;
					}
					
					resultBuffer.append("사업부["+org_code+":"+sell_code+"]처리("+orgCnt+")건/실패("+errCnt+")건  ");	
				}
			}			
			Logger.debug("1. 출고확정 송신 처리 끝");		
			
			if(totalCnt > 0){

				sendMessage = "SUCCESS!"+resultBuffer.toString();
			
			}else{
				sendMessage = "FAIL! SEND NO DATA...즐거운 하루 되세요^^";				
			}

			
		} catch(SQLException e) {
			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
			
		} catch(JedisConnectionException e) {

			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());			
			sendMessage = "FAIL!!["+e.toString()+"]";			
			
		} catch(Exception e) {
			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}			
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {

			try 
		    {
				conn.setAutoCommit(true);	
				
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
				
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
	
	/** 반품확정 
	 * @param dbmode
	 * @param command
	 * @param transCD
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws JedisConnectionException
	 * @throws JSONException
	 */
	
	public String api_Auto_ReturnConfirm(String dbmode, String command, String transCD) throws SQLException, IOException, JedisConnectionException, JSONException
	{
		String methodName ="com.service.ScApiCreateREDIS.api_Delivery_Send()";
		Logger.debug(methodName);
		
		/* JDBC Connection 변수 선언 */			
		Connection 			conn		= null;
		
		/* PreparedStatement 선언 */				
		/* ResultSet 선언 */		
		
		/* Redis 선언 */
		Jedis 				jedis   	= null;
		
		/* List , HashMap 등 */
		List<Object> vendorList 		= null;
		HashMap		 getHm				= new HashMap();
		
		/* StringBuffer 선언 */		
		StringBuffer	resultBuffer  	= new StringBuffer(500);	// 결과메세지 
		
		/* String 변수선언 */		
		String 	sendMessage 	= null;
		String 	call_header 	= "ORCHeaderInsert"; // ORC: OrderReturnConfirm
		String 	call_detail 	= "ORCDetailInsert";
		
		int totalCnt = 0;
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO 	dao = new ServiceDAO();

			vendorList = GetVendorList(dbmode,transCD);
			Logger.debug("vendorList["+vendorList.size()+"]");
			
			if (vendorList != null) {			
								
				/* 1. Redis connection 생성 */				
				Logger.debug("0. Redis connection 생성 시작");
				
				jedis = new Jedis(RED_IP, PORT , 12000);
				jedis.connect();
				jedis.select(DB_INDEX);			
					
			
				Logger.debug("0. Redis connection 생성 끝");
				/* 1. Redis connection 끝 */
								
				for (int i = 0; i < vendorList.size(); i++) {
					
					getHm = (HashMap)vendorList.get(i);
					
					String org_code 	= StringUtil.nullTo((String)getHm.get("COCD"),"");
					String sell_code    = StringUtil.nullTo((String)getHm.get("VDCD"),"");
					
					int orgCnt = 0;
					int errCnt = 0;

					List<Object> getOrderSendData 	= null;	
					Map<String, String> vMap 		= null;					
					getOrderSendData = dao.getOrderSendData(conn, call_header, org_code, sell_code, transCD);
			
					Logger.debug("1. 반품확정 송신 처리 시작");			
					Logger.debug("반품확정 송신건수["+getOrderSendData.size()+"]");
					
					if (getOrderSendData.size() > 0) {						
						
						for (int v = 0; v < getOrderSendData.size(); v++) {
		
							Logger.debug("1-1. 반품확정 송신데이터 HEARD 파싱 시작");										
							
							JSONObject jsonObject = new JSONObject();
							JSONArray cell = new JSONArray();
							
							String cocd 			= ""; 		// 사업부코드
							String vendorId 		= "";  		// 판매채널코드
							String pono 			= "";  		// 주문번호			
							String orderHeaderKey 	= "";  		// 주문번호키
							
														
							vMap = (HashMap<String, String>) getOrderSendData.get(v);
		
							Logger.debug("COCD["+StringUtil.nullTo(vMap.get("COCD"),"")+"]");
							Logger.debug("VENDOR_ID["+StringUtil.nullTo(vMap.get("VENDOR_ID"),"")+"]");
							Logger.debug("PONO["+StringUtil.nullTo(vMap.get("PONO"),"")+"]");
							Logger.debug("ORDERHEADERKEY["+StringUtil.nullTo(vMap.get("ORDERHEADERKEY"),"")+"]");
							
							cocd 				= StringUtil.nullTo(vMap.get("COCD"),"");
							vendorId 			= StringUtil.nullTo(vMap.get("VENDOR_ID"),"");
							pono 				= StringUtil.nullTo(vMap.get("PONO"),"");
							orderHeaderKey 		= StringUtil.nullTo(vMap.get("ORDERHEADERKEY"),"");
							
							jsonObject.put("org_code", cocd);
							jsonObject.put("sell_code", vendorId);
							jsonObject.put("orderId", pono);
							jsonObject.put("orderHeaderKey", orderHeaderKey);
							jsonObject.put("tranDt", CommonUtil.getCurrentDate());			
							jsonObject.put("status", SEND_DELIVERY_STATUS);
							
							
							List<Object> getOrderDetailData 	= null;	
							Map<String, String> jMap 		= null;					
							getOrderDetailData = dao.getOrderSendData(conn, call_detail, org_code, sell_code, pono);
							
							for (int j = 0; j < getOrderDetailData.size(); j++) {
								
								Logger.debug("1-2. 반품확정 송신데이터 DETAIL 파싱 시작");									
								
								JSONObject asrrotList = new JSONObject();							
								
								String whcd 			= ""; 		// 창고코드
								String orderSeq 		= ""; 		// 주문순번
								String orderLineKey	 	= ""; 		// 주문순번키
								String orderReleaseKey 	= "";		// 주문확정키
								String reno	 			= "";		// 출고의뢰번호
								String barCode		 	= "";		// 바코드
								String outDt 			= "";		// 출고일자
								String outTime 			= "";		// 출고시간
								String expNm 			= "";		//
								String expNo 			= "";		//
								
								jMap = (HashMap<String, String>) getOrderDetailData.get(j);
								
								Logger.debug("WHCD["+StringUtil.nullTo(jMap.get("WHCD"),"")+"]");
								Logger.debug("ORDER_SEQ["+StringUtil.nullTo(String.valueOf(jMap.get("ORDER_SEQ")),"")+"]");
								Logger.debug("ORDERLINEKEY["+StringUtil.nullTo(jMap.get("ORDERLINEKEY"),"")+"]");
								Logger.debug("ORDERRELEASEKEY["+StringUtil.nullTo(jMap.get("ORDERRELEASEKEY"),"")+"]");
								Logger.debug("RENO["+StringUtil.nullTo(jMap.get("RENO"),"")+"]");
								Logger.debug("OUTDT["+StringUtil.nullTo(jMap.get("OUTDT"),"")+"]");
								Logger.debug("UPDTIME["+StringUtil.nullTo(jMap.get("UPDTIME"),"")+"]");
								Logger.debug("OUTDT["+StringUtil.nullTo(jMap.get("OUTDT"),"")+"]");
								Logger.debug("EXPNM["+StringUtil.nullTo(jMap.get("EXPNM"),"")+"]");
								Logger.debug("EXPNO["+StringUtil.nullTo(jMap.get("EXPNO"),"")+"]");
								
								whcd				= StringUtil.nullTo(jMap.get("WHCD"),"");
								orderSeq 			= StringUtil.nullTo(String.valueOf(jMap.get("ORDER_SEQ")),"");
								orderLineKey 		= StringUtil.nullTo(jMap.get("ORDERLINEKEY"),"");
								orderReleaseKey 	= StringUtil.nullTo(jMap.get("ORDERRELEASEKEY"),"");
								reno 				= StringUtil.nullTo(jMap.get("RENO"),"");
								outDt 				= StringUtil.nullTo(jMap.get("OUTDT"),"");
								outTime 			= StringUtil.nullTo(jMap.get("OUTTIME"),"");
								expNm 				= StringUtil.nullTo(jMap.get("EXPNM"),"");
								expNo 				= StringUtil.nullTo(jMap.get("EXPNO"),"");
								
								asrrotList.put("ship_node", whcd);
								asrrotList.put("orderLineNo", orderSeq);
								asrrotList.put("orderLineKey", orderLineKey);
								asrrotList.put("orderReleaseKey", orderReleaseKey);
								asrrotList.put("shipmentNo", reno);
								asrrotList.put("expnm", expNm);
								asrrotList.put("expNo", expNo);
								asrrotList.put("outDt", outDt);
								asrrotList.put("outTime", outTime);	
								
								cell.add(asrrotList);
								
								Logger.debug("1-2.1. 반품확정 송신 SEND_LOG 시작");
								cubeDao.setSendLog(dbmode, "SCAPI", command, cubeDao.getApiName(command), vendorId, orderReleaseKey, "N/A", "N/A", "N/A", "N/A", "000", "SUCCESS!", "00", transCD);

								Logger.debug("1-2.1. 반품확정 송신 SEND_LOG 끝");
								
								Logger.debug("1-2. 반품확정 송신데이터 DETAIL 파싱 끝");
							}

							jsonObject.put("list", cell);													
							
							Logger.debug("[반품확정SEND_KEY]"+org_code+":"+sell_code+ORDER_RETURN_CONFIRM);							
							
							Logger.debug("1-3. 반품확정 송신 REDIS 시작");	
							Logger.debug("JsonString : " + jsonObject.toString());
							jedis.lpush(org_code+":"+sell_code+ORDER_RETURN_CONFIRM, jsonObject.toString());			
							Logger.debug("1-3. 반품확정 송신 REDIS 끝");							
														
							orgCnt++;
							totalCnt++;
							Logger.debug("1-1. 반품확정 송신데이터 HEARD 파싱 끝");							
						}						
						
					} else {
						cubeDao.setSendLog(dbmode, "SCAPI", command, cubeDao.getApiName(command), sell_code, "N/A", "N/A", "N/A", "N/A", "N/A", "100", "연동할 대상 정보가 없습니다.","00", transCD);
						errCnt++;
					}
					
					resultBuffer.append("사업부["+org_code+":"+sell_code+"]처리("+orgCnt+")건/실패("+errCnt+")건  ");	
				}
			}			
			Logger.debug("1. 반품확정 송신 처리 끝");		
			
			if(totalCnt > 0){

				sendMessage = "SUCCESS!"+resultBuffer.toString();
			
			}else{
				sendMessage = "FAIL! SEND NO DATA...즐거운 하루 되세요^^";				
			}

			
		} catch(SQLException e) {
			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
			
		} catch(JedisConnectionException e) {

			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());			
			sendMessage = "FAIL!!["+e.toString()+"]";			
			
		} catch(Exception e) {
			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}			
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
		} finally {

			try 
		    {
				conn.setAutoCommit(true);	
				
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
				
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
	
	/**
	 * 반품 / 반품취소
	 * @param dbmode
	 * @param processCmd
	 * @param transCD
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws JedisConnectionException
	 * @throws JSONException
	 */
	public String api_Auto_ReturnPO(String dbmode, String processCmd, String transCD) throws SQLException, IOException, JedisConnectionException, JSONException
	{
		String methodName ="com.service.ScApiCreateREDIS.api_Auto_ReturnPO()";
		Logger.debug(methodName);
		
		/* JDBC Connection 변수 선언 */		
		Connection 		conn	= null;
		
		/* Redis 선언 */
		Jedis 			jedis   = null;

		/* PreparedStatement 선언 */
		PreparedStatement	pstmt	= null;
		
		List<Object> vendorList 	= null;
		HashMap		 getHm	= new HashMap();
		
		StringBuffer	resultBuffer  	= new StringBuffer(500);	// 결과메세지 
		StringBuffer   	sqlBuffer  		= new StringBuffer(500);	// 서브쿼리문
		
		/* String 변수선언  */			
		String call_seq 	= "";		
		String sendMessage 	= null;
		String succStr 		= "";
		String processNm	= "";

		int	   succCnt		= 0;
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			CubeService cubeDao = CubeService.getInstance();
			ServiceDAO 	dao = new ServiceDAO();
			
			sqlBuffer.append("UPDATE  API_RECV_LOG				");
			sqlBuffer.append("		   SET  RESULT_CODE  = ? 	");
			sqlBuffer.append("			   ,RESULT_NAME  = ?	");
			sqlBuffer.append("WHERE   CALL_DT  = ?				");
			sqlBuffer.append("AND     CALL_SEQ = ? 				");
			pstmt = conn.prepareStatement(sqlBuffer.toString());
						
			/* RedKEY 조회*/
			vendorList = GetVendorList(dbmode,transCD);
			
			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
			
			Date nowDay = new Date();
			String toDay = sdFormat.format(nowDay);
			
			Logger.debug("vendorList["+vendorList.size()+"]");			
			if (vendorList != null) {
				
				/* 1. Redis connection 생성 */				
				Logger.debug("1. Redis connection 생성 시작");
				
				jedis = new Jedis(RED_IP, PORT , 12000);
				jedis.connect();
				jedis.select(DB_INDEX);			
								
				Logger.debug("1. Redis connection 생성 끝");
				/* 1. Redis connection 끝 */ 
					
				
				for (int i = 0; i < vendorList.size(); i++) {
					
					getHm = (HashMap)vendorList.get(i);
					
					String org_code 	= StringUtil.nullTo((String)getHm.get("COCD"),"");
					String sell_code    = StringUtil.nullTo((String)getHm.get("VDCD"),"");					
					
					int    orgCnt		= 0;
					int    orgErrCnt	= 0;
					int    redisCnt 	= jedis.llen(org_code+":"+sell_code+RECV_ORDER_RETURN ).intValue();
					
					Logger.debug("org_code["+org_code+"]");					
					Logger.debug("sell_code["+sell_code+"]");
					Logger.debug("OrderReturnProcess-REDIS_KEY["+org_code+":"+sell_code+RECV_ORDER_RETURN+"]");									
					Logger.debug("OrderReturnProcess-REDIS_COUNT["+redisCnt+"]");	
					
					Logger.debug("2. Sterling OrderReturnProcess 수신데이터 처리 시작");
					if(redisCnt > 0){
		
						String status			= "";
						String tranDt			= "";						
						String rtCocd			= "";		// 신규
						String rtVendorId		= "";
						String rtShipNode		= "";		// 신규
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
						String rtDeliPrice		= "0";
						String rtQty			= "";
						String rtShipID			= "";
						String rtCancelDt		= "";
						String rtShipStatus 	= "";
						String rtDeliGb 		= "";
						String rtRetCode 		= "";
						String rtOriShipId 		= "";
						String rtClameMemo		= "";
						String rtCubeItem		= "";
						String rtShipKey		= "";		// 신규
						String rtOrderSeqKey	= "";		// 신규						
						String rtOrderKey		= "";		// 신규
						String rtQrder_dt		= "";
						String rtOrder_id		= "";
						String rtVendorNm		= "";
						String rtRetDesc        = "";	    // 반품사유상세 추가(2015.02.25 하윤식)
						String rtItemStatus     = "";	    // 상품등급     추가(2015.02.25 하윤식)
						String rtOrderLineNo_org     = "";	// 원주문순번   추가(2015.02.25 하윤식)
						String rtOrderReleaseKey_org = "";  // 원주문확정키 추가(2015.02.25 하윤식)
						
						for (int v = 0; v < redisCnt; v++) {

							String command			= "";							
							String  jsonString = StringUtil.nullTo(jedis.rpop(org_code+":"+sell_code+RECV_ORDER_RETURN),"");
							Logger.debug(jsonString);
							Logger.debug("553.SC API OrderReturnProcess 수신 DATA["+jsonString+"]");					
							
							int    insertErrCnt = 0;

							ServiceDataInfo 	dInfo  	= new ServiceDataInfo();
							
							String jsonData  = URLDecoder.decode(jsonString,"UTF-8");
							if(jsonData.length() < 1) {
								
								
							} 
							
							JSONObject 	jobj = JSONObject.fromObject(jsonData);						
							JSONArray jarray = jobj.getJSONArray("list");
								
							Logger.debug("Sterling OrderReturnProcess 마스터정보 시작");
							Logger.debug("status["+StringUtil.nullTo((String) jobj.get("status"),"")+"]");
							Logger.debug("tranDt["+StringUtil.nullTo((String) jobj.get("tranDt"),"")+"]");
							Logger.debug("orderHeaderKey["+StringUtil.nullTo((String) jobj.get("orderHeaderKey"),"")+"]");
							Logger.debug("orderDt["+StringUtil.nullTo((String) jobj.get("orderDt"),"")+"]");						
							Logger.debug("orderId["+StringUtil.nullTo((String) jobj.get("orderId"),"")+"]");
							Logger.debug("Sterling OrderReturnProcess 마스터정보 끝");	

							rtQrder_dt 	= StringUtil.nullTo((String) jobj.get("orderDt"),"");			// 주문일자
							rtOrder_id	= StringUtil.nullTo((String) jobj.get("orderId"),"");			// 주문번호
							rtOrderKey 	= StringUtil.nullTo((String) jobj.get("orderHeaderKey"),"");	// 전송일자
							status 		= StringUtil.nullTo((String) jobj.get("status"),"");			// 주문상태
							tranDt 		= StringUtil.nullTo((String) jobj.get("tranDt"),"");			// 전송일자

							
							Logger.debug("처리상태["+status+"]");
							
							if(status.equals(RECV_ORDER_RETURN_STATUS)){
								command = "OrderReturnRetrieve";			// 반품주문 
							}else if(status.equals(RECV_ORDER_RETURN_CANCE_STATUS)){
								command = "OrderReturnCancelRetrieve";	// 반품주문 취소
								 		   
							}else{
								command = "NOT PROCESS STATUS";
							}
							
							processNm = cubeDao.getApiName(command);
							
							Logger.debug("처리업무구분["+command+"]");
							if(command.equals("OrderReturnRetrieve") || command.equals("OrderReturnCancelRetrieve")){
	
								call_seq = cubeDao.setRecvLog(dbmode, "SCAPI", command, cubeDao.getApiName(command), sell_code, toDay, toDay, "000", "처리중!!!!!", transCD);
								Logger.debug("call_dt["+CommonUtil.getCurrentDate()+"]");
								Logger.debug("call_seq["+call_seq+"]");							
								
								for (int j = 0; j < jarray.size(); j++)
								{
									JSONObject rtList = jarray.getJSONObject(j);
									
									Logger.debug("Sterling OrderReturnProcess 주문확정라인 시작");								
									Logger.debug("org_code["+StringUtil.nullTo(rtList.getString("org_code"),"")+"]");
									Logger.debug("sell_code["+StringUtil.nullTo(rtList.getString("sell_code"),"")+"]");
									Logger.debug("ship_node["+StringUtil.nullTo(rtList.getString("ship_node"),"")+"]");
									Logger.debug("orderDt["+StringUtil.nullTo(rtList.getString("orderDt"),"")+"]");
									Logger.debug("orderId["+StringUtil.nullTo(rtList.getString("orderId"),"")+"]");
									Logger.debug("orderLineNo["+StringUtil.zeroPutStr(3,StringUtil.nullTo(rtList.getString("orderLineNo"),""))+"]");
									Logger.debug("orderLineKey["+StringUtil.nullTo(rtList.getString("orderLineKey"),"")+"]");
									Logger.debug("orderReleaseKey["+StringUtil.nullTo(rtList.getString("orderReleaseKey"),"")+"]");	
									
									if(status.equals(RECV_ORDER_RETURN_STATUS)){									
										Logger.debug("receiptNm["+StringUtil.nullTo(rtList.getString("receiptNm"),"")+"]");
										Logger.debug("receiptTel["+StringUtil.nullTo(rtList.getString("receiptTel"),"")+"]");
										Logger.debug("receiptHp["+StringUtil.nullTo(rtList.getString("receiptHp"),"")+"]");
										Logger.debug("receiptAddr1["+StringUtil.nullTo(rtList.getString("receiptAddr1"),"")+"]");
										Logger.debug("receiptAddr2["+StringUtil.nullTo(rtList.getString("receiptAddr2"),"")+"]");
										Logger.debug("receiptZipcode["+StringUtil.nullTo(rtList.getString("receiptZipcode"),"")+"]");
										Logger.debug("custNm["+StringUtil.nullTo(rtList.getString("custNm"),"")+"]");
										Logger.debug("custTel["+StringUtil.nullTo(rtList.getString("custTel"),"")+"]");
										Logger.debug("custHp["+StringUtil.nullTo(rtList.getString("custHp"),"")+"]");	
										Logger.debug("deliveryMsg["+StringUtil.nullTo(rtList.getString("deliveryMsg"),"")+"]");
									}
									
									Logger.debug("itemId["+StringUtil.nullTo(rtList.getString("itemId"),"")+"]");
									Logger.debug("itemNm["+StringUtil.nullTo(rtList.getString("itemNm"),"")+"]");
									Logger.debug("qty["+StringUtil.nullTo(rtList.getString("qty"),"")+"]");
									Logger.debug("salePrice["+StringUtil.nullTo(rtList.getString("salePrice"),"")+"]");
									Logger.debug("returnDesc["+StringUtil.nullTo(rtList.getString("returnDesc"),"")+"]");                   // 반품사유상세 추가(2015.02.25 하윤식)
									Logger.debug("itemStatus["+StringUtil.nullTo(rtList.getString("itemStatus"),"")+"]");                   // 상품등급     추가(2015.02.25 하윤식)
									Logger.debug("orderLineNo_org["+StringUtil.nullTo(rtList.getString("orderLineNo_org"),"")+"]");         // 원주문순번   추가(2015.02.25 하윤식)
									Logger.debug("orderReleaseKey_org["+StringUtil.nullTo(rtList.getString("orderReleaseKey_org"),"")+"]"); // 원주문확정키 추가(2015.02.25 하윤식)

									Logger.debug("Sterling OrderReturnProcess 주문확정라인 끝");						
									
									rtCocd				= StringUtil.nullTo(rtList.getString("org_code"),"");		// 사업부코드				
									Logger.debug("********************************************** 1");
									rtVendorId			= StringUtil.nullTo(rtList.getString("sell_code"),"");		// 판매채널코드
									Logger.debug("********************************************** 2");
									rtShipNode			= StringUtil.nullTo(rtList.getString("ship_node"),"");		// 창고코드
									Logger.debug("********************************************** 3");
									
									// 주문일자
									if (command.equals("OrderReturnRetrieve")) {					//반품 정보..
										Logger.debug("********************************************** 4");
										rtOrderDt		= StringUtil.nullTo(rtList.getString("orderDt"),"");
									} else if (command.equals("OrderReturnCancelRetrieve")) {	       	//반품취소 정보..
										Logger.debug("********************************************** 5");
										rtCancelDt		= StringUtil.nullTo(rtList.getString("orderDt"),"");
									}
									Logger.debug("********************************************** 6");
									rtFirstOrderId		= StringUtil.nullTo(rtList.getString("orderId"),"");			// 주문번호
									Logger.debug("********************************************** 7");
									rtOrderId			= StringUtil.nullTo(rtList.getString("orderId"),"");			// 주문번호
									Logger.debug("********************************************** 8");
									rtOrderSeq			= StringUtil.zeroPutStr(3,StringUtil.nullTo(rtList.getString("orderLineNo"),""));		// 주문순번
									Logger.debug("********************************************** 9");
									rtOrderSeqKey		= StringUtil.nullTo(rtList.getString("orderLineKey"),"");		// 주문순번키
									Logger.debug("********************************************** 10");
									rtShipKey			= StringUtil.nullTo(rtList.getString("orderReleaseKey"),"");	// 주문확정키
									Logger.debug("********************************************** 11");
									
									if(status.equals(RECV_ORDER_RETURN_STATUS)){
										Logger.debug("********************************************** 12");
										/* 수취인정보*/
										rtReceiptNm			= StringUtil.nullTo(rtList.getString("receiptNm"),"");		// 수취인명
										Logger.debug("********************************************** 13");
										rtReceiptTel		= StringUtil.nullTo(rtList.getString("receiptTel"),"");		// 수취인전화
										Logger.debug("********************************************** 14");
										rtReceiptHp			= StringUtil.nullTo(rtList.getString("receiptHp"),"");		// 수취인휴대폰
										Logger.debug("********************************************** 15");
										rtReceiptAddr1		= StringUtil.nullTo(rtList.getString("receiptAddr1"),"");	// 수취인주소1
										Logger.debug("********************************************** 16");
										rtReceiptAddr2		= StringUtil.nullTo(rtList.getString("receiptAddr2"),"");	// 수취인주소1
										Logger.debug("********************************************** 17");
										rtReceiptZipcode	= StringUtil.nullTo(rtList.getString("receiptZipcode"),"");	// 수취인우편번호
										Logger.debug("********************************************** 18");
										
										/* 주문자정보*/							
										rtCustNm			= StringUtil.nullTo(rtList.getString("custNm"),"");			// 주문자정보
										Logger.debug("********************************************** 19");
										rtCustTel			= StringUtil.nullTo(rtList.getString("custTel"),"");		// 주문자전화번호
										Logger.debug("********************************************** 20");
										rtCustHp			= StringUtil.nullTo(rtList.getString("custHp"),"");			// 주문자휴대폰
										Logger.debug("********************************************** 21");
		
										rtDeliveryMsg		= StringUtil.nullTo(rtList.getString("deliveryMsg"),"");	// 배송메세지
										Logger.debug("********************************************** 22");
									}
									/* 상품정보*/
									rtItemCd			= StringUtil.nullTo(rtList.getString("itemId"),"");			// 상품코드
									Logger.debug("********************************************** 23");
									rtItemNm			= StringUtil.nullTo(rtList.getString("itemNm"),"");			// 상품명
									Logger.debug("********************************************** 24");
									rtQty				= StringUtil.nullTo(rtList.getString("qty"),"0");			// 수량
									Logger.debug("********************************************** 25");
									rtSalePrice			= StringUtil.nullTo(rtList.getString("salePrice"),"0");		// 개별판매가격
									Logger.debug("********************************************** 26");
	
									rtRetDesc             = StringUtil.nullTo(rtList.getString("returnDesc"),"");            // 반품사유상세 추가(2015.02.25 하윤식)
									Logger.debug("********************************************** 27");
									rtItemStatus          = StringUtil.nullTo(rtList.getString("itemStatus"),"");            // 상품등급     추가(2015.02.25 하윤식)
									Logger.debug("********************************************** 28");
			                        rtOrderLineNo_org     = StringUtil.nullTo(rtList.getString("orderLineNo_org"),"");       // 원주문순번   추가(2015.02.25 하윤식)
			                        Logger.debug("********************************************** 29");
			                        rtOrderReleaseKey_org = StringUtil.nullTo(rtList.getString("orderReleaseKey_org"),"");   // 원주문확정키 추가(2015.02.25 하윤식)
			                        Logger.debug("********************************************** 30");
								
									dInfo.setCall_dt(CommonUtil.getCurrentDate());
									Logger.debug("********************************************** 31");
									dInfo.setCall_seq(call_seq);
									Logger.debug("********************************************** 32");
									dInfo.setInuser("SCAPI");
									Logger.debug("********************************************** 33");
									dInfo.setError_code("00");
									Logger.debug("********************************************** 34");
									dInfo.setError_msg("SUCCESS");
									Logger.debug("********************************************** 35");
									dInfo.setSeq(String.valueOf(j+1));
									Logger.debug("********************************************** 36");
									dInfo.setRecv_gb(cubeDao.getRecvGb(command)); // 10.주문, 20.주문취소, 30.반품, 40.반품취소
									Logger.debug("********************************************** 37");
									dInfo.setOrderKey(rtOrderKey);
									Logger.debug("********************************************** 38");
									dInfo.setTrans_dt(tranDt); 
									Logger.debug("********************************************** 39");
									dInfo.setCocd(rtCocd);									
									Logger.debug("********************************************** 40");
									dInfo.setVendor_id(rtVendorId);
									Logger.debug("********************************************** 41");
									dInfo.setWhcd(rtShipNode);
									Logger.debug("********************************************** 42");
									dInfo.setInstruct_dt(rtOrderDt);
									Logger.debug("********************************************** 43");
									dInfo.setCancel_dt(rtCancelDt);
									Logger.debug("********************************************** 44");
									dInfo.setFirst_order_id(rtFirstOrderId); 
									Logger.debug("********************************************** 45");
									dInfo.setOrder_id(rtOrderId);
									Logger.debug("********************************************** 46");
									dInfo.setOrder_seq(rtOrderSeq);  
									Logger.debug("********************************************** 47");
									dInfo.setShip_seq(rtOrderSeq);
									Logger.debug("********************************************** 48");
									dInfo.setOrderSeqKey(rtOrderSeqKey);								
									Logger.debug("********************************************** 49");
									dInfo.setShip_id(rtOrderId);  
									Logger.debug("********************************************** 50");
									dInfo.setShipKey(rtShipKey);										
									Logger.debug("********************************************** 51");
									dInfo.setChange_gb(rtChangeGb);
									Logger.debug("********************************************** 52");
									dInfo.setShip_status(rtShipStatus); 
									Logger.debug("********************************************** 53");
									dInfo.setReceipt_nm(rtReceiptNm);  
									Logger.debug("********************************************** 54");
									dInfo.setReceipt_tel(rtReceiptTel);
									Logger.debug("********************************************** 55");
									dInfo.setReceipt_hp(rtReceiptHp);
									Logger.debug("********************************************** 56");
									dInfo.setReceipt_addr1(rtReceiptAddr1);   
									Logger.debug("********************************************** 57");
									dInfo.setReceipt_addr2(rtReceiptAddr2);  							
									Logger.debug("********************************************** 58");
									dInfo.setReceipt_zipcode(rtReceiptZipcode); 
									Logger.debug("********************************************** 59");
									dInfo.setCust_nm(rtCustNm);   
									Logger.debug("********************************************** 60");
									dInfo.setCust_tel(rtCustTel);  
									Logger.debug("********************************************** 61");
									dInfo.setCust_hp(rtCustHp);  
									Logger.debug("********************************************** 62");
									dInfo.setCust_zipcode(rtCustZipcode);    
									Logger.debug("********************************************** 63");
									dInfo.setCust_addr1(rtCustAddr1);      
									Logger.debug("********************************************** 64");
									dInfo.setCust_addr2(rtCustAddr2);  									
									Logger.debug("********************************************** 65");
									dInfo.setDelivery_msg(rtDeliveryMsg);
									Logger.debug("********************************************** 66");
									dInfo.setItem_cd(rtItemCd);   
									Logger.debug("********************************************** 67");
									dInfo.setItem_nm(rtItemNm);  								
									Logger.debug("********************************************** 68");
									dInfo.setQty(rtQty);
									Logger.debug("********************************************** 69");
									dInfo.setOption1(rtOption1);         
									Logger.debug("********************************************** 70");
									dInfo.setOption2(rtOption2);         
									Logger.debug("********************************************** 71");
									dInfo.setDeli_gb(rtDeliGb);								
									Logger.debug("********************************************** 72");
									dInfo.setRet_code(rtRetCode);
									Logger.debug("********************************************** 73");
									dInfo.setDeli_price(rtDeliPrice);
									Logger.debug("********************************************** 74");
									dInfo.setSale_price(rtSalePrice);								
									Logger.debug("********************************************** 75");
									dInfo.setOri_ship_id(rtOriShipId);
									Logger.debug("********************************************** 76");
									dInfo.setCust_email(rtCustEmail);
									Logger.debug("********************************************** 77");
									dInfo.setClame_memo(rtClameMemo);
									Logger.debug("********************************************** 78");
									dInfo.setCube_item(rtCubeItem);
									Logger.debug("********************************************** 79");
									dInfo.setVendorNm(rtVendorNm);
									Logger.debug("********************************************** 80");
									dInfo.setRet_desc(rtRetDesc);                        // 반품사유상세 추가(2015.02.25 하윤식)
									Logger.debug("********************************************** 81");
									dInfo.setItem_status(rtItemStatus);                  // 상품등급     추가(2015.02.25 하윤식)
									Logger.debug("********************************************** 82");
									dInfo.setOrderLineNo_org(rtOrderLineNo_org);         // 원주문순번   추가(2015.02.25 하윤식)
									Logger.debug("********************************************** 83");
									dInfo.setOrderReleaseKey_org(rtOrderReleaseKey_org); // 원주문확정키 추가(2015.02.25 하윤식)
									Logger.debug("********************************************** 84");
									
									/* 2-1. API_RECV_DATA INSERT 요청 시작 */
									Logger.debug("2-1. API_RECV_DATA INSERT 요청 시작");
									
									//int result = dao.setRecvData(conn, dInfo, transCD);
									
									int result = 0;
									if(status.equals(RECV_ORDER_RETURN_STATUS)){
										// 반품
										result = dao.setRecvReturnData(conn, dInfo, transCD); // 하윤식수정(2015.02.25) 
									}else if(status.equals(RECV_ORDER_RETURN_CANCE_STATUS)){
										// 반품취소
										result = dao.setRecvReturnCancelData(conn, dInfo, transCD); // 하윤식수정(2015.03.12) 		   
									}
	
									Logger.debug("result["+result+"]");
									Logger.debug("2-1. API_RECV_DATA INSERT 요청 끝");
									/* 2-1. API_RECV_DATA INSERT 요청 끝 */
									
									if(result == 0){
										insertErrCnt++;
										break;
									}									
									
								}
	
								/* 2-2. callProcedure 요청 시작 */
								Logger.debug("2-2. callProcedure 요청 시작");
								Logger.debug("insertErrCnt["+insertErrCnt+"]");	
								
								// 정상
								if(insertErrCnt == 0){
										
									Logger.debug("API_RECV_DATA INSERT 정상");
	
									//전송대상 BARCODE 전송 결과 업데이트..
									if (pstmt != null) { pstmt.close(); pstmt = null; }
									pstmt = conn.prepareStatement(sqlBuffer.toString());
									
									pstmt.setString(1, "000");
									pstmt.setString(2, "SUCCESS!");
									pstmt.setString(3, CommonUtil.getCurrentDate());
									pstmt.setString(4, call_seq);
	
									pstmt.executeUpdate();
									

									Logger.debug(" 프로시져 콜 시작");							
									//cubeDao.callProcedure(dbmode, command, CommonUtil.getCurrentDate(), call_seq, rtOrderKey, tranDt, rtOrder_id, transCD, rtQrder_dt, org_code, sell_code);
									cubeDao.callProcedure2(dbmode, command, CommonUtil.getCurrentDate(), call_seq, rtOrderKey, tranDt, rtOrder_id, transCD, rtQrder_dt, org_code, sell_code, dInfo);
									Logger.debug(" 프로시져 콜 끝");
									
									orgCnt++; // 사업부별 정상카운트
									succCnt++;
									
								}else{ // API_RECV_DATA TABLE INSERT 오류시!!!!!
	
									Logger.debug("API_RECV_DATA INSERT 실패");
									
									//전송대상 BARCODE 전송 결과 업데이트..
									if (pstmt != null) { pstmt.close(); pstmt = null; }
									pstmt = conn.prepareStatement(sqlBuffer.toString());
									
									pstmt.setString(1, "100");
									pstmt.setString(2, "API_RECV_DATA INSERT ERROR![주문번호:"+rtOrderId+"]");
									pstmt.setString(3, CommonUtil.getCurrentDate());
									pstmt.setString(4, call_seq);
	
									pstmt.executeUpdate();
									
									Logger.debug("2-3. 반품연동처리시 에러처리");
									jedis.lpush(org_code+":"+sell_code+ORDER_RETURN_ERROR,jsonString);
									orgErrCnt++;
								}
								
								Logger.debug("2-2. callProcedure 요청 끝");
								/* 2-2. callProcedure 요청 끝 */
								
							}else{
															
								cubeDao.setRecvLog(dbmode, "scAPI", command, "주문번호["+rtOrder_id+"]가 처리가능한상태["+status+"]가 아닙니다", sell_code, toDay, toDay, "100", "FAIL!(처리상태오류)", transCD);
	
								Logger.debug("2-3. 반품연동처리시 처리상태오류");
								jedis.lpush(org_code+":"+sell_code+ORDER_ERROR,jsonString);
								
								orgErrCnt++;
							}							
							
						} // end-for (REDIS)
						
						resultBuffer.append("사업부["+org_code+":"+sell_code+"]처리("+orgCnt+")건/실패("+orgErrCnt+")건  ");						
						
					} else {
						
						resultBuffer.append(" 사업부["+org_code+":"+sell_code+"]NO DATA! 주문내역없음.  ");
						succStr = "조회된데이터가 없습니다.";
						cubeDao.setRecvLog(dbmode, "SCAPI", processCmd, "반품/반품취소요청",sell_code, toDay, toDay, "100", succStr, transCD);
					}
					
					
					Logger.debug("2. Sterling OrderReturnProcess 수신데이터 처리 끝");
					/* 2. Sterling 출고의로 수신데이터 처리 끝 */
				}
												
				if(succCnt > 0){
					succStr = "SUCCES!";
				}else{
					succStr = "FAIL!";
				}
				

			} else {
				succStr = "FAIL!.";
				resultBuffer.append("NO DATA!! 조회된 사업부가 없습니다.");		
			}
			
			sendMessage = succStr+resultBuffer.toString();
			
		} catch(SQLException e) {
			e.printStackTrace();
			conn.rollback();			
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage	= "Error : "+ e.toString();
			
		} catch(JedisConnectionException e) {
			e.printStackTrace();
			if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e1){}finally{jedis = null;}
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());			
			sendMessage = "FAIL!!["+e.toString()+"]";
					
		} catch(Exception e) {
			e.printStackTrace();
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage	= "Error : "+ e.toString();

			
		} finally {
			
			try 
		    {
				conn.setAutoCommit(true);	
				
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } 
		    catch (Exception e) 
		    {
		    	e.printStackTrace();
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}
		
		return sendMessage;
		
	}
	
	public String api_SendProductData_RedMarker(String dbmode, String transCD) throws SQLException, IOException, JedisConnectionException
	{
		// 로그를 찍기 위한 변수 선언
		String methodName ="com.service.ScApiCreateREDIS.api_SendProductData_RedMarker()";
		Logger.debug(methodName);
		
		/*  JDBC Connection 변수 선언  */
		Connection 		conn		= null;
		
		/* PreparedStatement 선언 */
		PreparedStatement	pstmt0		= null; //        쿼리문 실행
		PreparedStatement	pstmt1		= null; //     주 쿼리문 실행
		PreparedStatement	pstmt2		= null; //   서브 쿼리문 실행
		PreparedStatement	pstmt3		= null; // 카운트 쿼리문 실행

		/* ResultSet 선언 */
		ResultSet			rs0			= null;
		ResultSet			rs1			= null;
		ResultSet			rs2			= null;
		ResultSet			rs3			= null;
				
		/*  StringBuffer 선언  */
		StringBuffer	sqlBuffer0  = new StringBuffer(1000);	// 		   쿼리문
		StringBuffer	sqlBuffer1  = new StringBuffer(1000);	//      주 쿼리문
		StringBuffer   	sqlBuffer2  = new StringBuffer(500);	//    서브 쿼리문	
		StringBuffer   	sqlBuffer3  = new StringBuffer(500);	//  카운트 쿼리문	
		StringBuffer   	sqlBuffer4  = new StringBuffer(500);	//처리결과 메세지
		
		/*  Redis 선언  */
		Jedis 			jedis   	= null;
		/*
		JedisPool 		jedisPool	= null;
		JedisPoolConfig jedisConfig = null;
		*/
		
		/* String 변수선언  */
		String 	sendMessage = null;		
	    
	    /* 숫자형 선언 */
		int successCnt 	= 0;
		
		try {

			conn =	DataBaseManager.getConnection(dbmode);		
			conn.setAutoCommit(false);

			Logger.debug("0. Sterling API 전송을위한 SQL 작성 시작");
			
			/* 0. Sterling API 전송을위한 SQL 작성 시작*/
			sqlBuffer0.append("SELECT   RETC AS COCD							\n");	
			sqlBuffer0.append("       , CD4  AS VDCD							\n");			
			sqlBuffer0.append("  FROM TBB150					    			\n");	
			sqlBuffer0.append(" WHERE REFTP = 'ZY'								\n");	
			sqlBuffer0.append("   AND REFCD <> '0000'							\n");	
			sqlBuffer0.append("   AND CD4   = '"+ transCD +"'					\n");	
			sqlBuffer0.append("   GROUP BY RETC, CD4						    \n");
			
			/* 0-1. 주 쿼리문*/
			sqlBuffer1.append("SELECT  COCD ,PRODINC ,PNAME ,BRAND_ID ,BRAND_NM											        \n");	
			sqlBuffer1.append("       ,FIPRI ,TRAN_DATE	,TRAN_SEQ ,GOODS_CODE ,GOODS_DETAIL ,GOODS_URL ,SEX ,SEASON, GROUP_DESC	\n");	
			sqlBuffer1.append(" FROM ( SELECT    MAX(A.COCD)      AS COCD		\n");	
			sqlBuffer1.append("        , A.PRODINC        AS PRODINC			\n");			
			sqlBuffer1.append("        , MAX(A.PNAME)     AS PNAME				\n");	
			sqlBuffer1.append("        , MAX(A.BRAND_ID)  AS BRAND_ID			\n");	
			sqlBuffer1.append("        , MAX(A.BRAND_NM)  AS BRAND_NM			\n");	
			sqlBuffer1.append("        , MAX(A.FIPRI)     AS FIPRI				\n");	
			sqlBuffer1.append("        , MAX(A.TRAN_DATE) AS TRAN_DATE			\n");
			sqlBuffer1.append("        , MAX(A.TRAN_SEQ)  AS TRAN_SEQ			\n");			
			sqlBuffer1.append("        , MAX(A.GOODS_CODE)    AS GOODS_CODE		\n");
			sqlBuffer1.append("        , MAX(A.GOODS_DETAIL)  AS GOODS_DETAIL	\n");
			sqlBuffer1.append("        , MAX(A.GOODS_URL)     AS GOODS_URL		\n");
			sqlBuffer1.append("        , MAX(A.SEX ) 		  AS SEX			\n");
			sqlBuffer1.append("        , MAX(A.SEASON)  	  AS SEASON			\n");
			sqlBuffer1.append("        , MAX(A.GROUP_DESC)    AS GROUP_DESC		\n");				
			sqlBuffer1.append("  FROM TBP050_TRANSFER A ,						\n");				
			sqlBuffer1.append("      (	SELECT   BAR_CODE					    \n");	
			sqlBuffer1.append("                , MAX(TRAN_DATE) AS TRAN_DATE 	\n");
			sqlBuffer1.append("                , MAX(TRAN_SEQ)  AS TRAN_SEQ 	\n");
			sqlBuffer1.append("          FROM TBP050_TRANSFER   				\n");			
			sqlBuffer1.append("          WHERE STATUS  IN ('00', '99')   		\n");	
			sqlBuffer1.append("          AND COCD 		= ?  					\n");	
			sqlBuffer1.append("          AND SHOP_ID 	= ?  					\n");			
			sqlBuffer1.append("          GROUP BY BAR_CODE ) B  				\n");			
			sqlBuffer1.append("  WHERE A.TRAN_DATE = B.TRAN_DATE 				\n");
			sqlBuffer1.append("  AND   A.TRAN_SEQ  = B.TRAN_SEQ 				\n");
			sqlBuffer1.append("  AND A.BAR_CODE  = B.BAR_CODE 					\n");		
			sqlBuffer1.append("  AND A.COCD      = ? 							\n");
			sqlBuffer1.append("  AND A.SHOP_ID   = ? 							\n");
			sqlBuffer1.append("  GROUP BY A.PRODINC								\n");
			sqlBuffer1.append("  ORDER BY TRAN_DATE, TRAN_SEQ ASC				\n");
			sqlBuffer1.append("                                    ) X			\n");
			sqlBuffer1.append("  WHERE ROWNUM <= 100 							\n");
			
			/* 0-2. 서브 쿼리문*/
			sqlBuffer2.append("SELECT   ITEM_COLOR                              \n");
			sqlBuffer2.append("        ,ITEM_SIZE                               \n");
			sqlBuffer2.append("        ,BAR_CODE                                \n");
			sqlBuffer2.append("FROM    TBP050_TRANSFER                          \n");
			sqlBuffer2.append("WHERE   TRAN_DATE = ?                            \n");
			sqlBuffer2.append("AND     TRAN_SEQ  = ?                            \n");
			sqlBuffer2.append("AND     COCD      = ?                            \n");
			sqlBuffer2.append("AND     SHOP_ID   = ?                            \n");			
			sqlBuffer2.append("AND     PRODINC	 = ?                            \n");
			sqlBuffer2.append("ORDER BY BAR_CODE                                \n");
			
			/* 0-3. 카운트 쿼리문*/
			sqlBuffer3.append("SELECT    COUNT(1) AS CNT					    \n");				
			sqlBuffer3.append("  FROM TBP050_TRANSFER A ,					    \n");				
			sqlBuffer3.append("      (	SELECT   BAR_CODE					    \n");	
			sqlBuffer3.append("                , MAX(TRAN_DATE) AS TRAN_DATE 	\n");
			sqlBuffer3.append("                , MAX(TRAN_SEQ)  AS TRAN_SEQ 	\n");
			sqlBuffer3.append("          FROM TBP050_TRANSFER   				\n");			
			sqlBuffer3.append("          WHERE STATUS  IN ('00', '99')   		\n");	
			sqlBuffer3.append("          AND COCD 		= ?  					\n");	
			sqlBuffer3.append("          AND SHOP_ID 	= ?  					\n");			
			sqlBuffer3.append("          GROUP BY BAR_CODE ) B  				\n");			
			sqlBuffer3.append("  WHERE A.TRAN_DATE = B.TRAN_DATE 				\n");
			sqlBuffer3.append("  AND   A.TRAN_SEQ  = B.TRAN_SEQ 				\n");
			sqlBuffer3.append("  AND A.BAR_CODE  = B.BAR_CODE 					\n");		
			sqlBuffer3.append("  AND A.COCD      = ? 							\n");
			sqlBuffer3.append("  AND A.SHOP_ID   = ? 							\n");
			sqlBuffer3.append("  GROUP BY A.PRODINC								\n");
			
			/* 0. Sterling API 전송을위한 SQL 작성 끝*/
			Logger.debug("0. Sterling API 전송을위한 SQL 작성 끝");
			
			pstmt0 = conn.prepareStatement(sqlBuffer0.toString()); 			
			pstmt1 = conn.prepareStatement(sqlBuffer1.toString()); 
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString()); 
			pstmt3 = conn.prepareStatement(sqlBuffer3.toString()); 			
			
			rs0 = pstmt0.executeQuery();

			/* 1. API CUBE 상품 전송데이터 Count 시작 */
			Logger.debug("[1. API CUBE 상품 전송데이터 조회 시작]");	
			while(rs0.next()){
				
				int count 		= 0;
				int errCnt 		= 0;
				int cnt 		= 0;
				
				String cocd = StringUtil.nullTo(rs0.getString("COCD"),"");
				String vdcd = StringUtil.nullTo(rs0.getString("VDCD"),"");
				
				Logger.debug("[COCD["+StringUtil.nullTo(rs0.getString("COCD"),"")+"]");
				Logger.debug("[VDCD["+StringUtil.nullTo(rs0.getString("VDCD"),"")+"]");
				
				pstmt3.setString(1, cocd);
				pstmt3.setString(2, vdcd);
				pstmt3.setString(3, cocd);
				pstmt3.setString(4, vdcd);
				
				rs3 = pstmt3.executeQuery();
				if(rs3.next()){
					cnt = rs3.getInt("CNT");
				}
				
				if(cnt > 0){ //전송 DATA 있을때..

					pstmt1.setString(1, cocd);
					pstmt1.setString(2, vdcd);
					pstmt1.setString(3, cocd);
					pstmt1.setString(4, vdcd);
					
					rs1 = pstmt1.executeQuery();
					
					JSONObject 	jsonObject 		= new JSONObject();
					JSONArray 	prodincArray 	= new JSONArray();
					
					// 품목 리시트조회
					while(rs1.next()){
							
						JSONObject prodList = new JSONObject();
					
						/* 2. Sterling API 전송을위한 JSON_DATA 생성 시작 */
						Logger.debug("[2. Sterling API 전송을위한 JSON_DATA 생성 시작]");					
						Logger.debug("[org_code["+StringUtil.nullTo(rs1.getString("COCD"),"")+"]");
						Logger.debug("[prodinc["+StringUtil.nullTo(rs1.getString("PRODINC"),"")+"]");
						Logger.debug("[pname["+StringUtil.nullTo(rs1.getString("PNAME"),"")+"]");
						Logger.debug("[brand_id["+StringUtil.nullTo(rs1.getString("BRAND_ID"),"")+"]");
						Logger.debug("[brand_name["+StringUtil.nullTo(rs1.getString("BRAND_NM"),"")+"]");
						Logger.debug("[sale_price["+StringUtil.nullTo(rs1.getString("FIPRI"),"")+"]");
						Logger.debug("[TRAN_DATE["+StringUtil.nullTo(rs1.getString("TRAN_DATE"),"")+"]");
						Logger.debug("[TRAN_SEQ["+StringUtil.nullTo(rs1.getString("TRAN_SEQ"),"")+"]");						
						Logger.debug("[GOODS_CODE["+StringUtil.nullTo(rs1.getString("GOODS_CODE"),"")+"]");
						Logger.debug("[GOODS_DETAIL["+StringUtil.nullTo(rs1.getString("GOODS_DETAIL"),"")+"]");						
						Logger.debug("[GOODS_URL["+StringUtil.nullTo(rs1.getString("GOODS_URL"),"")+"]");						
						Logger.debug("[SEX["+StringUtil.nullTo(rs1.getString("SEX"),"")+"]");
						Logger.debug("[SEASON["+StringUtil.nullTo(rs1.getString("SEASON"),"")+"]");
						Logger.debug("[GROUP_DESC["+StringUtil.nullTo(rs1.getString("GROUP_DESC"),"")+"]");
						
						
						prodList.put("tran_date",StringUtil.nullTo(rs1.getString("TRAN_DATE"),""));			// 1.[Parameter]사업부코드
						prodList.put("tran_seq",StringUtil.nullTo(rs1.getString("TRAN_SEQ"),""));			// 2.[Parameter]사업부코드						
						prodList.put("org_code",StringUtil.nullTo(rs1.getString("COCD"),""));				// 3.[Parameter]사업부코드
						prodList.put("prodinc",StringUtil.nullTo(rs1.getString("PRODINC"),""));				// 4.[Parameter]스타일코드
						prodList.put("pname",StringUtil.nullTo(rs1.getString("PNAME"),""));					// 5.[Parameter]상품명					
						prodList.put("brand_id",StringUtil.nullTo(rs1.getString("BRAND_ID"),""));			// 6.[Parameter]브랜드ID
						prodList.put("brand_name",StringUtil.nullTo(rs1.getString("BRAND_NM"),""));			// 7.[Parameter]브래드명
						prodList.put("sale_price",StringUtil.nullTo(rs1.getString("FIPRI"),""));			// 8.[Parameter]최초판매가	
						prodList.put("goods_code",StringUtil.nullTo(rs1.getString("GOODS_CODE"),""));		// 9.[Parameter]							
						prodList.put("goods_detail",StringUtil.nullTo(rs1.getString("GOODS_DETAIL"),""));	// 10.[Parameter]	
						prodList.put("goods_url",StringUtil.nullTo(rs1.getString("GOODS_URL"),""));			// 11.[Parameter]	
						prodList.put("sex",StringUtil.nullTo(rs1.getString("SEX"),""));						// 12.[Parameter]	
						prodList.put("season",StringUtil.nullTo(rs1.getString("SEASON"),""));				// 13.[Parameter]	
						//prodList.put("group_desc",StringUtil.nullTo(rs1.getString("GROUP_DESC"),""));		// 14.[Parameter]
						prodList.put("group_text",StringUtil.nullTo(rs1.getString("GROUP_DESC"),""));		// 14.[Parameter] (2015.2.16 group_desc -> group_text로 수정)
						
						// 바코드 정보 가져오기..
						pstmt2.setString(1, StringUtil.nullTo(rs1.getString("TRAN_DATE"),""));
						pstmt2.setString(2, StringUtil.nullTo(rs1.getString("TRAN_SEQ"),""));
						pstmt2.setString(3, cocd);
						pstmt2.setString(4, vdcd);						
						pstmt2.setString(5, StringUtil.nullTo(rs1.getString("PRODINC"),""));
						
						rs2 = pstmt2.executeQuery();
						JSONArray cellOpt = new JSONArray();
						
						while (rs2.next()){
						
							JSONObject itemOption = new JSONObject();

							Logger.debug("[2-1. Sterling API 전송을위한 BAR_CODE JSON_DATA 생성 시작]");							
							Logger.debug("[ITEM_COLOR["+StringUtil.nullTo(rs2.getString("ITEM_COLOR"),"")+"]");
							Logger.debug("[ITEM_SIZE["+StringUtil.nullTo(rs2.getString("ITEM_SIZE"),"")+"]");
							Logger.debug("[BAR_CODE["+StringUtil.nullTo(rs2.getString("BAR_CODE"),"")+"]");
	
							itemOption.put("item_color", StringUtil.nullTo(rs2.getString("ITEM_COLOR"),""));	// 15.[Parameter]컬러명
							itemOption.put("item_size", StringUtil.nullTo(rs2.getString("ITEM_SIZE"),""));		// 16.[Parameter]사이즈명
							itemOption.put("bar_code", StringUtil.nullTo(rs2.getString("BAR_CODE"),""));		// 17.[Parameter]상품바코드
							
							cellOpt.add(itemOption);
							prodList.put("optioninfo",cellOpt);
						}										
						prodincArray.add(prodList);
						
						Logger.debug("[2. Sterling API 전송을위한 JSON_DATA 생성 끝]");
						/* 2. Sterling API 전송을위한 JSON_DATA 생성 끝 */	
						
						count++;		// 사업부별 성공 카운트
						successCnt++;	// 전체 성공 카운트					
					}
					jsonObject.put("list", prodincArray);
					Logger.debug("[송신데이터["+jsonObject.toString()+"]");
					
					/* 3. Redis Connection 시작 */
					Logger.debug("[3. Redis Connection 시작]");
					
					/* 3-1. Redis connection pool 생성 */				
					/*
					jedisPool 	= new JedisPool(new JedisPoolConfig(), redIp , port ,12000); 					
					jedis 		= jedisPool.getResource();
					*/
					jedis = new Jedis(RED_IP, PORT , 12000);
					jedis.connect();
					jedis.select(DB_INDEX);
	
					Logger.debug("[SEND_KEY]"+cocd+SEND_PRODUCT_KEY);
					
					/* 3-2 Steling OMS 전송할 상품정보 SET */				
					/* SET */  				 
					jedis.lpush(cocd+SEND_PRODUCT_KEY, jsonObject.toString());
											
					Logger.debug("[3. Redis Connection 끝]");				
					// 3. Redis Connection 끝
					
				}else{
					
					errCnt++;	// 사업부별 실패 카운트
				}
				
				sqlBuffer4.append("사업부["+cocd+"] 정상:"+count+"/ 실패:"+errCnt+"  "); 
			}
			
			if(successCnt > 0){
				sendMessage = "SUCCESS !!!!! ["+sqlBuffer4.toString()+"]";
			}else{
				sendMessage = "NO DATA !!!!! [ 송신할 상품정보가 존재하지 않습니다. ]";
			}

		} catch(SQLException e) {
			
			conn.rollback();			
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());			
			
			sendMessage = "FAIL!["+e.toString()+"]";			
		
		} catch(JedisConnectionException e) {

			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			/* Redis connection 제거*/
			//if( jedisPool!= null )jedisPool.returnBrokenResource(jedis); jedisPool = null;
			
			sendMessage = "FAIL!!["+e.toString()+"]";
			
		} catch(Exception e) {
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			sendMessage = "FAIL!!!["+e.toString()+"]";
		
		}finally {
			try 
			{
				conn.setAutoCommit(true);
				
				if( rs0 !=null ) try{ rs0.close(); rs0 = null; }catch(Exception e){}finally{rs0 = null;}
				if( rs1 !=null ) try{ rs1.close(); rs1 = null; }catch(Exception e){}finally{rs1 = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}
				
				if(pstmt0  != null ) try{ pstmt0.close(); pstmt0 = null; }catch(Exception e){}finally{pstmt0 = null;}
				if(pstmt1  != null ) try{ pstmt1.close(); pstmt1 = null; }catch(Exception e){}finally{pstmt1 = null;}				
				if(pstmt2  != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				
				DataBaseManager.close(conn, dbmode);				
				if(conn	!= null ) try{ conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}		
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
				//if(jedisPool!= null ) try{ jedisPool.destroy(); jedisPool = null; }catch(Exception e){}finally{jedisPool = null;}				
				
			} 
		    catch (Exception e) 
		    {

		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());						
				sendMessage = "FAIL!!!!["+e.toString()+"]";
		    }
		}

		return sendMessage;
	}
	
	public String api_Auto_SendItemStock_RedMarker(String dbmode, String transCD) throws SQLException, IOException, JedisConnectionException, JSONException
	{
		String methodName ="com.service.ScApiCreateREDIS.api_Auto_SendItemStock_RedMarker()";
		Logger.debug(methodName);
		
		/* JDBC Connection 변수 선언 */
		Connection 		conn		= null;
		
		/* PreparedStatement 선언 */
		PreparedStatement	pstmt0		= null; //    		쿼리문 실행
		PreparedStatement	pstmt1		= null; //     주 	쿼리문 실행
		PreparedStatement	pstmt2		= null; // 카운트 	쿼리문 실행
		PreparedStatement	pstmt3		= null; // 전송결과	쿼리문 실행 (2015.02.12 하윤식추가)
		CallableStatement 	cstmt    	= null;		

		/* ResultSet 선언 */
		ResultSet			rs0			= null;
		ResultSet			rs1			= null;
		ResultSet			rs2			= null;		
				
		/* StringBuffer 선언*/		
		StringBuffer	sqlBuffer0  = new StringBuffer(1000);	// 사업부 	쿼리문
		StringBuffer	sqlBuffer1  = new StringBuffer(1000);	// 주 		쿼리문
		StringBuffer	sqlBuffer2  = new StringBuffer(1000);	// 카운트 	쿼리문
		StringBuffer	sqlBuffer3  = new StringBuffer(1000);	// 전송결과	쿼리문 (2015.02.12 하윤식추가)
		StringBuffer   	sqlBuffer4  = new StringBuffer(500);	// 처리결과 메세지
		
		/* Redis 선언 */
		Jedis 			jedis   	= null;
		
		/* String 변수선언  */	
		String 	sendMessage = null;		
	    
	    /* 숫자형 선언 */
		int successCnt 	= 0;
				
		try {

			conn = DataBaseManager.getConnection(dbmode);
		
			/* 0. Sterling 재고 송신 SQL 시작 */	
			Logger.debug("0. Sterling 재고 송신 SQL 시작");
			
			sqlBuffer0.append("SELECT   RETC AS COCD				\n");	
			sqlBuffer0.append("       , CD4  AS VDCD				\n");			
			sqlBuffer0.append("  FROM TBB150					    \n");	
			sqlBuffer0.append(" WHERE REFTP = 'ZY'					\n");	
			sqlBuffer0.append("   AND REFCD <> '0000'				\n");	
			sqlBuffer0.append("   AND CD4   = '"+ transCD +"'		\n");	
			sqlBuffer0.append("   GROUP BY RETC, CD4				\n");
			
			sqlBuffer1.append("SELECT  TRAN_DATE    				\n");
			sqlBuffer1.append("        ,TRAN_SEQ     				\n");
			sqlBuffer1.append("        ,WHCD        				\n");			
			sqlBuffer1.append("        ,BARCODE      				\n");
			sqlBuffer1.append("        ,STOCK       				\n");
			sqlBuffer1.append(" FROM (    							\n");			
			sqlBuffer1.append("SELECT   A.TRAN_DATE AS TRAN_DATE    \n");
			sqlBuffer1.append("        ,A.TRAN_SEQ  AS TRAN_SEQ     \n");
			sqlBuffer1.append("        ,A.WHCD    	AS WHCD        	\n");			
			sqlBuffer1.append("        ,A.BARCODE   AS BARCODE      \n");
			sqlBuffer1.append("        ,A.STOCK  	AS  STOCK       \n");
			sqlBuffer1.append("FROM    TBD260 A             		\n");
			sqlBuffer1.append(" , ( SELECT  WHCD							\n");
			sqlBuffer1.append("           , BARCODE       					\n");
			sqlBuffer1.append("           , MAX(TRAN_DATE) AS TRAN_DATE     \n");
			sqlBuffer1.append("           , MAX(TRAN_SEQ)  AS TRAN_SEQ    	\n");
			sqlBuffer1.append("        FROM TBD260      	 				\n");			
			//sqlBuffer1.append("          WHERE STATUS   <> '01'     	 	\n");
			sqlBuffer1.append("          WHERE STATUS NOT IN ('01','05') 	\n"); //2015.02.12 하윤식수정
			sqlBuffer1.append("          AND TRAN_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD') \n");
			sqlBuffer1.append("           AND VENDOR_ID = ?      	 		\n");			
			sqlBuffer1.append("           AND COCD      = ?      	 		\n");
			sqlBuffer1.append("           GROUP BY WHCD, BARCODE    	 	\n");			
			sqlBuffer1.append("      ) B    	 							\n");			
			sqlBuffer1.append("      , ( SELECT BAR_CODE AS BARCODE    	 	\n");
			sqlBuffer1.append("        FROM TBP050_TRANSFER   	 			\n");
			sqlBuffer1.append("    WHERE STATUS = '01'  	 				\n");
			sqlBuffer1.append("     GROUP BY BAR_CODE   	 				\n");
			sqlBuffer1.append("      ) C  	 								\n");									
			sqlBuffer1.append("   WHERE A.TRAN_DATE = B.TRAN_DATE    	 	\n");
			sqlBuffer1.append("      AND A.TRAN_SEQ  = B.TRAN_SEQ    	 	\n");
			sqlBuffer1.append("      AND A.WHCD      = B.WHCD    	 		\n");
			sqlBuffer1.append("      AND A.BARCODE   = B.BARCODE   	 		\n");
			sqlBuffer1.append("      AND A.BARCODE   = C.BARCODE   	 		\n");			
			sqlBuffer1.append("      AND A.VENDOR_ID = ?   	 				\n");
			sqlBuffer1.append("      AND A.COCD      = ?  	 				\n");
			sqlBuffer1.append("      ORDER BY 1,2,3,4 ASC  	) XX 			\n");
			sqlBuffer1.append("   WHERE  ROWNUM     <= 1000   	 			\n");
			
			sqlBuffer2.append("SELECT  COUNT(1) AS CNT                      \n");
			sqlBuffer2.append("FROM    TBD260 A             		        \n");
			sqlBuffer2.append(" , ( SELECT  WHCD							\n");
			sqlBuffer2.append("           , BARCODE       					\n");
			sqlBuffer2.append("           , MAX(TRAN_DATE) AS TRAN_DATE     \n");
			sqlBuffer2.append("           , MAX(TRAN_SEQ)  AS TRAN_SEQ    	\n");
			sqlBuffer2.append("        FROM TBD260      	 				\n");			
			//sqlBuffer2.append("          WHERE STATUS   <> '01'     	 	\n");
			sqlBuffer2.append("          WHERE STATUS NOT IN ('01','05')	\n"); //2015.02.12 하윤식수정
			sqlBuffer2.append("           AND VENDOR_ID = ?      	 		\n");			
			sqlBuffer2.append("           AND COCD      = ?      	 		\n");
			sqlBuffer2.append("           GROUP BY WHCD, BARCODE    	 	\n");			
			sqlBuffer2.append("      ) B    	 							\n");
			sqlBuffer2.append("      , ( SELECT BAR_CODE AS BARCODE    	 	\n");
			sqlBuffer2.append("        FROM TBP050_TRANSFER   	 			\n");
			sqlBuffer2.append("    WHERE STATUS = '01'  	 				\n");
			sqlBuffer2.append("     GROUP BY BAR_CODE   	 				\n");
			sqlBuffer2.append("      ) C  	 								\n");									
			sqlBuffer2.append("   WHERE A.TRAN_DATE = B.TRAN_DATE    	 	\n");
			sqlBuffer2.append("      AND A.TRAN_SEQ  = B.TRAN_SEQ    	 	\n");
			sqlBuffer2.append("      AND A.WHCD      = B.WHCD    	 		\n");
			sqlBuffer2.append("      AND A.BARCODE   = B.BARCODE   	 		\n");
			sqlBuffer2.append("      AND A.BARCODE   = C.BARCODE   	 		\n");			
			sqlBuffer2.append("      AND A.VENDOR_ID = ?   	 				\n");
			sqlBuffer2.append("      AND A.COCD      = ?  	 				\n");
			
			//전송완료일 경우 status컬럼을 '05'로 update (2015.02.12 하윤식추가)
			sqlBuffer3.append("MERGE INTO TBD260 A                                                 \n");
			sqlBuffer3.append("USING ( SELECT TRAN_DATE                                            \n");
			sqlBuffer3.append("             , TRAN_SEQ                                             \n");
			sqlBuffer3.append("             , WHCD                                                 \n");
			sqlBuffer3.append("             , BARCODE                                              \n");
			sqlBuffer3.append("             , STOCK                                                \n");
			sqlBuffer3.append("          FROM ( SELECT A.TRAN_DATE AS TRAN_DATE                    \n");
			sqlBuffer3.append("                      , A.TRAN_SEQ  AS TRAN_SEQ                     \n");
			sqlBuffer3.append("                      , A.WHCD      AS WHCD                         \n");
			sqlBuffer3.append("                      , A.BARCODE   AS BARCODE                      \n");
			sqlBuffer3.append("                      , A.STOCK     AS  STOCK                       \n");
			sqlBuffer3.append("                   FROM TBD260 A                                    \n");
			sqlBuffer3.append("                      , ( SELECT WHCD                               \n");
			sqlBuffer3.append("                               , BARCODE                            \n");
			sqlBuffer3.append("                               , MAX(TRAN_DATE) AS TRAN_DATE        \n");
			sqlBuffer3.append("                               , MAX(TRAN_SEQ)  AS TRAN_SEQ         \n");
			sqlBuffer3.append("                            FROM TBD260                             \n");
			sqlBuffer3.append("                           WHERE STATUS    NOT IN ('01','05')       \n");
			sqlBuffer3.append("          AND TRAN_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD') \n");
			sqlBuffer3.append("                             AND VENDOR_ID = ?                      \n");
			sqlBuffer3.append("                             AND COCD      = ?                      \n");
			sqlBuffer3.append("                           GROUP BY WHCD, BARCODE                   \n");
			sqlBuffer3.append("                       ) B                                          \n");
			sqlBuffer3.append("                     , ( SELECT BAR_CODE AS BARCODE                 \n");
			sqlBuffer3.append("                           FROM TBP050_TRANSFER                     \n");
			sqlBuffer3.append("                          WHERE STATUS = '01'                       \n");
			sqlBuffer3.append("                          GROUP BY BAR_CODE                         \n");
			sqlBuffer3.append("                       ) C                                          \n");
			sqlBuffer3.append("                 WHERE A.TRAN_DATE = B.TRAN_DATE                    \n");
			sqlBuffer3.append("                   AND A.TRAN_SEQ  = B.TRAN_SEQ                     \n");
			sqlBuffer3.append("                   AND A.WHCD      = B.WHCD                         \n");
			sqlBuffer3.append("                   AND A.BARCODE   = B.BARCODE                      \n");
			sqlBuffer3.append("                   AND A.BARCODE   = C.BARCODE                      \n");
			sqlBuffer3.append("                   AND A.VENDOR_ID = ?                              \n");
			sqlBuffer3.append("                   AND A.COCD      = ?                              \n");
			sqlBuffer3.append("                 ORDER BY 1,2,3,4 ASC                               \n");
			sqlBuffer3.append("              ) XX                                                  \n");
			sqlBuffer3.append("          WHERE ROWNUM <= 1000                                       \n");
			sqlBuffer3.append("      ) Z                                                           \n");
			sqlBuffer3.append("ON (   A.TRAN_DATE = Z.TRAN_DATE                                    \n");
			sqlBuffer3.append("   AND A.TRAN_SEQ  = Z.TRAN_SEQ                                     \n");
			sqlBuffer3.append("   AND A.WHCD      = Z.WHCD                                         \n");
			sqlBuffer3.append("   AND A.BARCODE   = Z.BARCODE                                      \n");
			sqlBuffer3.append("   )                                                                \n");
			sqlBuffer3.append("WHEN MATCHED THEN                                                   \n");
			sqlBuffer3.append("UPDATE                                                              \n");
			sqlBuffer3.append("   SET STATUS     = '05'                                            \n");
			sqlBuffer3.append("     , STATUS_MSG = 'SCAPI전송'                                     \n");
			sqlBuffer3.append("     , UPDUSER    = 'SCAPI'                                         \n");
			sqlBuffer3.append("     , UPDTIME    = TO_CHAR(SYSDATE, 'YYMMDDHH24MISS')              \n");
			
			pstmt0 = conn.prepareStatement(sqlBuffer0.toString());	// 사업부   쿼리 	
			pstmt1 = conn.prepareStatement(sqlBuffer1.toString());	//     주   쿼리
			pstmt2 = conn.prepareStatement(sqlBuffer2.toString());  // 카운트   쿼리 
			pstmt3 = conn.prepareStatement(sqlBuffer3.toString());  // 전송결과	쿼리 
				
			Logger.debug("0. Sterling 재고 송신 SQL 끝");
			/* 0. Sterling 재고 송신 SQL 끝 */

    			
			rs0 = pstmt0.executeQuery();
			
			// 사업부 조회
			while(rs0.next()){
				
				int cnt 	= 0;    				
				int count 	= 0; // 성공카운트	    				
				int errCnt 	= 0; // 실패카운트
				
				Logger.debug("[COCD["+StringUtil.nullTo(rs0.getString("COCD"),"")+"]");		// 사업부코드
				String cocd = StringUtil.nullTo(rs0.getString("COCD"),"");    				
				
				/* 1. Sterling 재고 프로시져 시작 */
				Logger.debug("1. Sterling 재고 프로시져 시작");
				
				if (cstmt != null) { cstmt.close(); cstmt = null; }				
				cstmt = conn.prepareCall("{call P_SEND_STOCK(?, ?, ?, ?, ?, ?)}");
				cstmt.registerOutParameter(1, Types.CHAR);
	        	cstmt.registerOutParameter(2, Types.CHAR);
	        	cstmt.registerOutParameter(3, Types.CHAR);
	        	cstmt.registerOutParameter(4, Types.INTEGER);
	        	cstmt.setString(5, transCD);
	        	cstmt.setString(6, cocd);	        	
	        	
	        	cstmt.executeUpdate();
	        	
	        	String errcode 	= cstmt.getString(1);
	        	String errmsg 	= cstmt.getString(2);	
	            String tranDt  	= cstmt.getString(3);
	            int    tranSeq 	= cstmt.getInt(4);

	    		Logger.debug("errcode["+errcode+"]");
	    		Logger.debug("errmsg["+errmsg+"]");
	    		Logger.debug("tranDt["+tranDt+"]");
	    		Logger.debug("tranSeq["+tranSeq+"]");

				Logger.debug("1. Sterling 재고 프로시져 끝");
				/* 1. Sterling 재고 프로시져 끝 */    		

	    		if (errcode.equals("00")) {
        	
	            	pstmt2.setString(1, transCD);
					pstmt2.setString(2, cocd);
					pstmt2.setString(3, transCD);
					pstmt2.setString(4, cocd);
					
	            	rs2 = pstmt2.executeQuery();
	    			
	    			if(rs2.next())
	    			{
	    				cnt = rs2.getInt("CNT");
	    			}
	            	
	    			//전송 DATA 있을때..
	    			if(cnt > 0){
	    				
	    				/* 2. Sterling 재고송신 JSON형식 API항목 정의 시작 */
	    				Logger.debug("2. Sterling 재고송신 JSON형식 API항목 정의 시작");
	    				
		            	pstmt1.setString(1, transCD);
						pstmt1.setString(2, cocd);
						pstmt1.setString(3, transCD);
						pstmt1.setString(4, cocd);
	    				
	    				rs1 = pstmt1.executeQuery();
	    				    				
	    				JSONObject jsonObject = new JSONObject();
	    				JSONArray cell = new JSONArray();
	    				
	    				while(rs1.next())
	    				{
	    					JSONObject asrrotList = new JSONObject();

	    					asrrotList.put("tran_date",StringUtil.nullTo(rs1.getString("TRAN_DATE"),""));	// 전송날짜
	    					asrrotList.put("tran_seq",StringUtil.nullTo(rs1.getString("TRAN_SEQ"),""));		// 전송순번	    					
	    					asrrotList.put("org_code",cocd);												// 사업부코드	    					
	    					asrrotList.put("ship_node",StringUtil.nullTo(rs1.getString("WHCD"),""));		// 창고코드
	    					asrrotList.put("bar_code",StringUtil.nullTo(rs1.getString("BARCODE"),""));		// 상품코드
	    					asrrotList.put("qty",StringUtil.nullTo(rs1.getString("STOCK"),""));				// 수량
	    					asrrotList.put("uom","EACH");													// 측정단위 ( SC 고정값 )
	    					
	        				Logger.debug("tran_date["+StringUtil.nullTo(rs1.getString("TRAN_DATE"),"")+"]");	    					
	        				Logger.debug("tran_seq["+StringUtil.nullTo(rs1.getString("TRAN_SEQ"),"")+"]");
	    					Logger.debug("ship_node["+StringUtil.nullTo(rs1.getString("WHCD"),"")+"]");
	    					Logger.debug("bar_code["+StringUtil.nullTo(rs1.getString("BARCODE"),"")+"]");
	        				Logger.debug("qty["+StringUtil.nullTo(rs1.getString("STOCK"),"")+"]");
	        				
	    					cell.add(asrrotList);
	    					
	    					count++;
	    					successCnt++;
	    				}
	    				
	    				jsonObject.put("list", cell);
	    				
	    				/* 2. Sterling 재고송신 JSON형식 API항목 정의 끝 */
	    				Logger.debug("2. Sterling 재고송신 JSON형식 API항목 정의 끝");
	    				
						/* 3. Redis Connection 시작 */
						Logger.debug("[3. Redis Connection 시작]");
						
						jedis = new Jedis(RED_IP, PORT , 12000);
						jedis.connect();
						jedis.select(DB_INDEX);
	
						Logger.debug("[재고SEND_KEY]"+cocd+SEND_INVENTORY_KEY);
						
						/* 3-1 Steling OMS 전송할 재고정보 SET */				
						/* SET */  				 
						jedis.lpush(cocd+SEND_INVENTORY_KEY, jsonObject.toString());
											
						Logger.debug("[3. Redis Connection 끝]");				
						// 3. Redis Connection 끝
						
						// 4. 전송결과 status:05 update 시작 (2015.02.12 하윤식추가)
						Logger.debug("[4. 전송결과 status:05 update 시작]");
						pstmt3.setString(1, transCD);
						pstmt3.setString(2, cocd);
						pstmt3.setString(3, transCD);
						pstmt3.setString(4, cocd);
						pstmt3.executeUpdate();
						Logger.debug("[4. 전송결과 status:05 update 끝]");
						// 4. 전송결과 status:05 update 끝
						
	    			}else{ 	//전송 DATA 없을때.. 
	    				errCnt++;
	    			}
	    			
	    			if(errCnt > 0){
	    				
	    				sqlBuffer4.append(" 사업부["+cocd+"] 전송할 재고정보가 없습니다.");
	    			}else{
	    				
		    			sqlBuffer4.append(" 사업부["+cocd+"] 정상:"+count+"건");	    				
		    		}
	 
	    		}else{
	    			
	    			sqlBuffer4.append(" 사업부["+cocd+"]"+ errmsg);

	    		}	
			}
			
    		if(successCnt > 0){
				sendMessage = "SUCCESS !!!!! ["+sqlBuffer4.toString()+"]";
    		}else{
    			sendMessage = "NO DATA !!!!! [ 송신할 재고정보가 존재하지 않습니다. ]";
    		}   
    		
			
		} catch(SQLException e) {
			
			Logger.debug("###Error###:"+ methodName +" Error sql:"+ e.toString());
			sendMessage = "FAIL!["+e.toString()+"]";	
			
		} catch(JedisConnectionException e) {

			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			
			sendMessage = "FAIL!!["+e.toString()+"]";			
		
		} catch(Exception e) {
			
			Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
			sendMessage = "FAIL!!!["+e.toString()+"]";	
			
		} finally {
			
			try 
		    {
				
				if( rs0 !=null ) try{ rs0.close(); rs0 = null; }catch(Exception e){}finally{rs0 = null;}
				if( rs1 !=null ) try{ rs1.close(); rs1 = null; }catch(Exception e){}finally{rs1 = null;}
				if( rs2 !=null ) try{ rs2.close(); rs2 = null; }catch(Exception e){}finally{rs2 = null;}

				if( pstmt0 != null ) try{ pstmt0.close(); pstmt0 = null; }catch(Exception e){}finally{pstmt0 = null;}
				if( pstmt1 != null ) try{ pstmt1.close(); pstmt1 = null; }catch(Exception e){}finally{pstmt1 = null;}
				if( pstmt2 != null ) try{ pstmt2.close(); pstmt2 = null; }catch(Exception e){}finally{pstmt2 = null;}
				if( pstmt3 != null ) try{ pstmt3.close(); pstmt3 = null; }catch(Exception e){}finally{pstmt3 = null;} //2015.02.12 하윤식추가
				if( cstmt != null ) try{ cstmt.close(); cstmt = null; }catch(Exception e){}finally{cstmt = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}				
		    } 
		    catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}				
		
		return sendMessage;
	}	
	
	
	/**
	 * 매장 직출 기능 FROM CUBE TO STERLING
	 * @param dbmode
	 * @param command
	 * @param transCD
	 * @return
	 */
	public String api_Auto_StoreReject (String dbmode, String command,String transCD){
		
		String methodName ="com.service.ScApiCreateREDIS.api_Auto_StoreReject()";
		Logger.debug(methodName);
		
		Connection 		conn = null;
		Jedis 			jedis   = null;
		PreparedStatement	pstmt		= null;
		PreparedStatement	pstmtDetail		= null;
		String commandNm ="매장출고 거부";
		String redisData ="";
		String resultMessage = "";

		StringBuffer msg = new StringBuffer();
		try {
			/* JDBC Connection 변수 선언 */		
			conn	= DataBaseManager.getConnection(dbmode);
			//공통 로그 생성용
			CubeService cubeDao = CubeService.getInstance();
			
			List<Object> vendorList 	= null;
			HashMap		 getHm	= new HashMap();
			
			String redisStatus ="1310";
				
			/* SELLER 조직 정보  조회 : 95 ASPB, 45 REDM, 95 SDFY 등*/
			vendorList = GetVendorList(dbmode,transCD);
			Logger.debug("vendorList["+vendorList.size()+"]");	
			
			for(int i = 0; i < vendorList.size(); i++){
				
				StringBuffer	headerBuffer  		= new StringBuffer(500);	// header 
				
				getHm = (HashMap)vendorList.get(i);
				
				String org_code 	= StringUtil.nullTo((String)getHm.get("COCD"),""); // 코드  45,90..
				String sell_code    = StringUtil.nullTo((String)getHm.get("VDCD"),"");	//조직 명	ASPB, SDRY.. 
				
				//
				String redisKey =org_code + ":"+ sell_code  + SEND_DELIVERY;
				
				Logger.debug("org_code["+org_code+ "]");	
				Logger.debug("sell_code["+sell_code+ "]");
				Logger.debug("redis key ["+redisKey+ "]");	
				
				
				headerBuffer.append(" SELECT A.COCD                                AS COCD                                           ");
				headerBuffer.append("     , B.REFCD                               AS VENDOR_ID					     ");
				headerBuffer.append("     , A.PODT                                AS PODT					     ");
				headerBuffer.append("     , SUBSTR(A.PONO, 1, LENGTH(A.PONO) - 2) AS PONO					     ");
				headerBuffer.append("     , A.ORDERHEADERKEY                      AS ORDERHEADERKEY	 			     ");
				headerBuffer.append("  FROM TBD03C A										     ");
				headerBuffer.append("     , TBB150 B										     ");
				headerBuffer.append("     , TBD026 C										     ");
				headerBuffer.append(" WHERE A.VDCD   = B.CD1									     ");
				headerBuffer.append("   AND A.CD22   = B.REFCD									     ");
				headerBuffer.append("   AND A.PODT   = C.PODT									     ");
				headerBuffer.append("   AND A.TEMPNO = C.TEMPNO									     ");
				headerBuffer.append("   AND A.POSEQ  = C.POSEQ									     ");
				headerBuffer.append("   AND A.VDCD   = C.RVDCD									     ");
				headerBuffer.append("   AND B.REFTP  = 'ZY'									     ");
				headerBuffer.append("   AND B.CD4    = ?								     ");
				headerBuffer.append("   AND B.REFCD  = ?									     ");
				headerBuffer.append("   AND A.CBGU   = '1'									     ");
				headerBuffer.append("   AND A.GUBUN  = '1'									     ");
				headerBuffer.append("   AND A.COCD   = ?									     ");
				headerBuffer.append("   AND A.PODT   BETWEEN TO_CHAR(SYSDATE -25, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')	     ");
				headerBuffer.append("   AND A.POSEQ  < 90001									     ");
				headerBuffer.append("   AND C.REQYN  = 'C'									     ");
				headerBuffer.append("   AND NOT EXISTS ( SELECT 1								     ");
				headerBuffer.append("					  FROM API_SEND_LOG Z					     ");
				headerBuffer.append("					 WHERE B.REFCD       = Z.VENDOR_ID			     ");
				headerBuffer.append("					   AND A.TEMPNO      = Z.SHIP_ID			     ");
				headerBuffer.append("					   AND Z.CALL_DT    >= TO_CHAR(SYSDATE -30, 'YYYYMMDD')	     ");
				headerBuffer.append("					   AND Z.RESULT_CODE = '000'				     ");
				headerBuffer.append("					   AND Z.CALL_API    = 'StoreCancel'			     ");
				headerBuffer.append("				  )								     ");
				headerBuffer.append("   GROUP BY A.COCD, B.REFCD, A.PODT, A.PONO, A.ORDERHEADERKEY				     ");
				
				
				pstmt = conn.prepareStatement(headerBuffer.toString());
				pstmt.setString(1, transCD);
				pstmt.setString(2, sell_code);
				pstmt.setString(3, org_code);
				
				System.out.println("org_cdoe" + org_code);
				System.out.println("sell_code" + sell_code);
				System.out.println("transCD" + transCD);
				
				ResultSet	headResult = pstmt.executeQuery();
				
				//헤더 정보 조회
				while(headResult.next()){
					JSONObject header = new JSONObject();
					
					StringBuffer   	detailBuffer  		= new StringBuffer(500);	// detail
					
					header.put("org_code", org_code);
					header.put("sell_code", sell_code);
					
					System.out.println( StringUtil.nullTo(headResult.getString("PODT"),""));
					System.out.println( StringUtil.nullTo(headResult.getString("PONO"),""));
					
					header.put("orderDt", StringUtil.nullTo(headResult.getString("PODT"),""));
					header.put("orderId", StringUtil.nullTo(headResult.getString("PONO"),""));
					
					
					header.put("orderHeaderKey", StringUtil.nullTo(headResult.getString("ORDERHEADERKEY"),""));
					header.put("tranDt", CommonUtil.getCurrentDate());
					header.put("status", redisStatus);
					
					String asPont = headResult.getString("PODT");
					String asPono = headResult.getString("PONO");
					String coCd   = headResult.getString("COCD");
					
					detailBuffer.append(" SELECT C.SVDCD      			AS SHIPNODE                             ");   
					detailBuffer.append("     , A.POSEQ 				AS 	ORDERLINENO							 ");
					detailBuffer.append("     , A.ORDERLINEKEY			AS 	ORDERLINEKEY						 ");
					detailBuffer.append("     , A.ORDERRELEASEKEY		AS 	ORDERRELEASEKEY						 ");
					detailBuffer.append("     , A.BARCODE				AS ITEMCD							      ");
					detailBuffer.append("     , 'EACH' 					AS UOM										");
					detailBuffer.append("     ,  A.QTY					AS QTY						      ");
					detailBuffer.append("     , '80'					AS STATUS							      ");
					detailBuffer.append("     , A.CD1					AS STATUS_MSG					      ");
					detailBuffer.append("     , A.TEMPNO  				AS SHIPID					      ");
					detailBuffer.append("  FROM TBD03C A											      ");
					detailBuffer.append("     , TBB150 B											      ");
					detailBuffer.append("     , TBD026 C											      ");
					detailBuffer.append(" WHERE A.VDCD   = B.CD1										      ");
					detailBuffer.append("   AND A.CD22   = B.REFCD										      ");
					detailBuffer.append("   AND A.PODT   = C.PODT										      ");
					detailBuffer.append("   AND A.TEMPNO = C.TEMPNO										      ");
					detailBuffer.append("   AND A.POSEQ  = C.POSEQ										      ");
					detailBuffer.append("   AND A.VDCD   = C.RVDCD										      ");
					detailBuffer.append("   AND B.REFTP  = 'ZY'										      ");
					detailBuffer.append("   AND B.CD4    = ?									      ");
					detailBuffer.append("   AND B.REFCD  = ?										      ");
					detailBuffer.append("   AND A.CBGU   = '1'										      ");
					detailBuffer.append("   AND A.GUBUN  = '1'										      ");
					detailBuffer.append("   AND A.COCD   = ?										      ");
					detailBuffer.append("   AND A.PODT   BETWEEN TO_CHAR(SYSDATE -25, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')		      ");
					detailBuffer.append("   AND A.POSEQ  < 90001										      ");
					detailBuffer.append("   AND C.REQYN  = 'C'										      ");
					detailBuffer.append("   AND A.PODT   = ?										      ");
					detailBuffer.append("   AND A.PONO   = ?||'-C'									      ");
					detailBuffer.append("   AND NOT EXISTS ( SELECT 1									      ");
					detailBuffer.append("					  FROM API_SEND_LOG Z						      ");
					detailBuffer.append("					 WHERE B.REFCD       = Z.VENDOR_ID				      ");
					detailBuffer.append("					   AND A.TEMPNO      = Z.SHIP_ID				      ");
					detailBuffer.append("					   AND Z.CALL_DT    >= TO_CHAR(SYSDATE -30, 'YYYYMMDD')		      ");
					detailBuffer.append("					   AND Z.RESULT_CODE = '000'					      ");
					detailBuffer.append("					   AND Z.CALL_API    = 'StoreCancel'				      ");
					detailBuffer.append("				  )									      ");
					
					pstmtDetail = conn.prepareStatement(detailBuffer.toString());
					pstmtDetail.setString(1, transCD);
					pstmtDetail.setString(2, sell_code);
					pstmtDetail.setString(3, coCd);
					pstmtDetail.setString(4, asPont);
					pstmtDetail.setString(5, asPono);
					
					System.out.println("org_cdoe" + org_code);
					System.out.println("sell_code" + sell_code);
					System.out.println("coCd" + coCd);
					System.out.println("asPont" + asPont);
					System.out.println("asPono" + asPono);
					
					ResultSet detailSet = pstmtDetail.executeQuery();
					
    				JSONArray cell = new JSONArray();
					
					while(detailSet.next()){
						JSONObject detail = new JSONObject();
						
						detail.put("ship_node", StringUtil.nullTo(detailSet.getString("SHIPNODE"),""));
						detail.put("orderLineNo", StringUtil.nullTo(detailSet.getString("ORDERLINENO"),""));
						detail.put("orderLineKey", StringUtil.nullTo(detailSet.getString("ORDERLINEKEY"),""));
						detail.put("orderReleaseKey", StringUtil.nullTo(detailSet.getString("ORDERRELEASEKEY"),""));
						detail.put("itemId", StringUtil.nullTo(detailSet.getString("ITEMCD"),""));
						detail.put("uom", StringUtil.nullTo(detailSet.getString("UOM"),""));
						detail.put("qty", StringUtil.nullTo(detailSet.getString("QTY"),""));
						detail.put("statuscd", StringUtil.nullTo(detailSet.getString("STATUS"),""));
						detail.put("statusMsg", StringUtil.nullTo(detailSet.getString("STATUS_MSG"),""));
						cell.add(detail);
						//CUBE 로그테이블에 이력 생성
						String shipId = StringUtil.nullTo(detailSet.getString("SHIPID"),"");
						CubeService.setSendLog(dbmode, "SCAPI", command, commandNm, sell_code,shipId, "N/A", "N/A", "N/A", "N/A", "000", "SUCCESS","00", transCD);
					}
					header.put("list", cell);
					redisData = header.toString();
					Logger.debug("redis data: " +redisData);
					//결과
					msg.append(redisData);
				//redis 연결, write
					if (vendorList != null && !redisData.equals( "{}")) {
						Logger.debug("1. Redis connection 생성 시작");
						jedis = new Jedis(RED_IP, PORT , 12000);
						jedis.connect();
						//고정 값 : 1
						jedis.select(DB_INDEX);	
						jedis.lpush(redisKey, redisData);
						Logger.debug("1. Redis connection 생성 끝");
					}
				}
				resultMessage = "성공적으로 전송했습니다: 전송전문:" + msg.toString();
			}
	
		} catch(SQLException e) {
			Logger.debug(methodName + "SQL EXCEPTION 발생 ");	
			resultMessage= "SQL EXCEPTION 발생 ";
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
		} catch(JedisConnectionException e) {
			Logger.debug(methodName + "Jedis connection 에러: IP: " + RED_IP + " PORT : " + PORT);
			resultMessage= "Jedis connection 에러: IP: " + RED_IP + " PORT : " + PORT;
		} catch(Exception e) {
			Logger.debug("EXCEPTION : " + e);
			resultMessage="에러발생";
		} finally {
			
			try {
				conn.setAutoCommit(true);	
				
				if(jedis!= null ) try{ jedis.disconnect(); jedis = null; }catch(Exception e){}finally{jedis = null;}
				if( pstmt != null ) try{ pstmt.close(); pstmt = null; }catch(Exception e){}finally{pstmt = null;}
				if( pstmtDetail != null ) try{ pstmtDetail.close(); pstmtDetail = null; }catch(Exception e){}finally{pstmtDetail = null;}
				
				DataBaseManager.close(conn, dbmode);
				if( conn!= null ) try{conn.close(); conn = null; }catch(Exception e){}finally{conn = null;}
		    } catch (Exception e) 
		    {
		    	Logger.debug("###Error###:"+ methodName +" Error :"+ e.toString());
		    }
		}		
		return resultMessage;
	}
}
