package com.service.dao;

import com.service.command.log.Logger;

/**
 * ServiceQuery.java
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

public class ServiceQuery {
	/**
	 * sql getter
	 * @param key
	 * @return
	 */
	public String getSql(String key, String call_dt, String call_seq, String transcd) {
		String query = "";
		
		if(key.equals("ServiceDAO.getRecvCallSeq")){
			//수신대상 차수
			query = getRecvCallSeq();
		}else if(key.equals("ServiceDAO.getSendCallSeq")){
			//송신대상 차수
			query = getSendCallSeq();
		}else if(key.equals("ServiceDAO.setRecvLog")){
			//수신 대상 로그 등록
			query = setRecvLog();
		}else if(key.equals("ServiceDAO.setRecvData")){
			//수신 대상 데이터 등록
			query = setRecvData(transcd);
		}else if(key.equals("ServiceDAO.setRecvReturnData")){  
			//반품수신 대상 데이터 등록 (하윤식 2015.02.25)
			query = setRecvReturnData(transcd);
		}else if(key.equals("ServiceDAO.setRecvReturnCancelData")){  
			//반품수신 대상 데이터 등록 (하윤식 2015.03.12)
			query = setRecvReturnCancelData(transcd);	
		}else if(key.equals("ServiceDAO.setSendLog")){
			//송신 대상 로그 등록
			query = setSendLog();
		}else if(key.equals("ServiceDAO.getVendorList")){
			//벤더, 조회일자 조회
			query = getVendorList(transcd);
		}else if(key.equals("ServiceDAO.OrderConfirm")){
			//발주확인 대상 조회
			query = OrderConfirm(transcd);
		}else if(key.equals("ServiceDAO.DeliveryInsert")){
			//배송정보등록
			query = DeliveryInsert(transcd);
		}else if(key.equals("ServiceDAO.SoldOutCancel")){
			//제휴사출고지시취소처리
			query = SoldOutCancel(transcd);
		}else if(key.equals("ServiceDAO.OrderReturnConfirm")){
			//반품정보확인
			query = OrderReturnConfirm(transcd);
		}else if(key.equals("ServiceDAO.ReturnPickUpInsert")){
			//반품수거등록
			query = ReturnPickUpInsert(transcd);
		}else if(key.equals("ServiceDAO.ReturnRefuse")){
			//반품취소처리
			query = ReturnRefuse(transcd);
		}else if(key.equals("ServiceDAO.OrderRetrieveCheck")){
			//발주조회 등록 후 주문완료 정보 확인
			query = OrderRetrieveCheck(call_dt,call_seq,transcd);
		}else if(key.equals("ServiceDAO.OrderReturnRetrieveCheck")){
			//반품정보조회 후 반품 정보 전송
			query = OrderReturnRetrieveCheck(call_dt,call_seq,transcd);
		}else if(key.equals("ServiceDAO.setErrorRecvLog")){
			//RECV 에러발생시 
			query = setErrorRecvLog();
		}else if(key.equals("ServiceDAO.ShopRecvOrderList")){
			//RECV 주문수집 
			query = getShopRecvOrderList();
		}else if(key.equals("ServiceDAO.ShopSendDeliveryList")){
			//SEND 송장전송 
			query = getShopSendDeliveryList();
		}else if(key.equals("ServiceDAO.ShopRecvClameList")){
			//RECV 클레임수집
			query = getShopRecvClameList();
		}else if(key.equals("ServiceDAO.getShopRecvCallSeq")){
			//RECV 샵링커 call_seq 
			query = getShopRecvCallSeq();
		}else if(key.equals("ServiceDAO.setShopRecvData")){
			//수신 대상 데이터 등록
			query = setShopRecvData();
		}else if(key.equals("ServiceDAO.getShopSendDeliveryData")){
			//SEND 송장전송 주문건별 조회
			query = getShopSendDeliveryData();
		}else if(key.equals("ServiceDAO.setShopSendLog")){
			//SEND 샵링커 send log
			//query = setShopSendLog();
		}else if(key.equals("ServiceDAO.DeliveryHeaderInsert")){
			// Sterling 출고확정
			query = deliveryHeaderInsert(call_dt,call_seq,transcd);
		}else if(key.equals("ServiceDAO.DeliveryDetailInsert")){
			// Sterling 출고확정
			query = deliveryDetailInsert(call_dt,call_seq,transcd);
		} else if(key.equals("ServiceDAO.ORCHeaderInsert")) {
			query = orConfirmHeaderInsert(call_dt,call_seq,transcd);
		} else if(key.equals("ServiceDAO.ORCDetailInsert")) {
			query = orConfirmDetailInsert(call_dt,call_seq,transcd);
		}
		
		return query;
	}
	 
	
	/**
	private String setShopSendLog() {
		StringBuffer qry = new StringBuffer();
		
		qry.append("  INSERT INTO ISECCUBE.SHOPINVOICE_LOG ( ");
		qry.append("     RESULT, ID, MESSAGE,  ");
		qry.append("     CALL_DT, CALL_SEQ, INUSER,  ");
		qry.append("     INTIME)  ");
		qry.append("  VALUES ( ?, ?, ?, ");
		qry.append("      ?, ?, ?, to_char(sysdate, 'YYYYMMDDHH24MISS' )) ");
		
		return qry.toString();
	}
    */
	
	private String setShopRecvData() {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" INSERT INTO SHOPLINKER.SHOPINPUT_LOG (                      ");
		qry.append("    shoplinker_order_id, mall_order_id, mall_name,           ");
		qry.append("    baesong_status, order_name, order_tel,                   ");
		qry.append("    order_cel, order_email, receive,                         ");
		qry.append("    receive_tel, receive_cel, receive_zipcode,               ");
		qry.append("    receive_addr, baesong_type, baesong_bi,                  ");
		qry.append("   delivery_msg, order_product_id, shoplinker_product_id,    "); 
		qry.append("   partner_product_id, product_name, quantity,               ");
		qry.append("   order_price, sale_price, supply_price,                    ");
		qry.append("   sku, orderdate, order_reg_date,                           ");
		qry.append("   clame_status, clame_memo, clame_date,                     ");
		qry.append("   recv_gb, call_seq, seq,                                   ");
		qry.append("   cocd, inuser, intime)                                     ");
		qry.append(" VALUES ( ?, ?, ?,                                            ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, ?,                                                 ");
		qry.append("    ?, ?, to_char(sysdate, 'YYYYMMDDHH24MISS' ))             ");
		
		return qry.toString();
	}



	//recv 샵링커 수신 차수
	private String getShopRecvCallSeq() {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" SELECT /*+ INDEX_DESC(ORDER_REG_DATE,CALL_SEQ) */						 				 ");
		qry.append("        LTRIM(TO_CHAR(TO_NUMBER(NVL(MAX(CALL_SEQ), 9000)) + 1, '0000')) as CALL_SEQ ");
		qry.append("   FROM SHOPLINKER.SHOPINPUT_LOG 															 ");
		qry.append("  WHERE ORDER_REG_DATE = ?											 			 ");
		
		
		return qry.toString();
	}


	//RECV 클레임수집
	private String getShopRecvClameList() {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" SELECT CD1                                                   ");
		qry.append("      , CD2                                                   ");
		qry.append("      , TO_CHAR(SYSDATE-8,'YYYYMMDDHH24MISS') AS STA_DT       ");
		qry.append("      , TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS')   AS END_DT       ");
		qry.append("   FROM TBB150                                                ");
		qry.append("  WHERE REFTP = 'ZW'                                          ");
		qry.append("    AND REFCD <> '0000'                                       ");
		
		return qry.toString();
	}

	//SEND 송장전송
	private String getShopSendDeliveryList() {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" SELECT D.CD1                                                                             ");
		qry.append("      , A.TEMPNO                                                                          ");
		qry.append("      , B.REFNM                                                                           ");
		qry.append("      , A.EXPNM                                                                           ");
		qry.append("      , A.VDCD                                                                            ");
		qry.append("   FROM TBD03C A                                                                          ");
		qry.append("      , TBB150 B                                                                          ");
		qry.append("      , ( SELECT DISTINCT CD1                                                             ");
		qry.append("            FROM TBB150                                                                   ");
		qry.append("           WHERE REFTP = 'ZW'                                                             ");
		qry.append("             AND REFCD <> '0000'                                                          ");
		qry.append("        ) D                                                                               ");
		qry.append("      , TBB050 E                                                                          ");
		qry.append("  WHERE NVL(A.EXPNO, '09') = B.REFCD                                                      ");
		qry.append("    AND A.VDCD  = E.VDCD                                                                  ");
		qry.append("    AND B.REFTP = 'ZV'                                                                    ");
		qry.append("    AND B.REFCD <> '0000'                                                                 ");
		qry.append("    AND A.CBGU  = '1'                                                                     ");
		qry.append("    AND A.GUBUN = '1'                                                                     ");
		qry.append("    AND A.POYN  = 'Y'                                                                     ");
		qry.append("    AND A.OUTDT BETWEEN TO_CHAR(SYSDATE-7, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')   ");
		qry.append("    AND E.CD5 IS NOT NULL                                                                 ");
		qry.append("    AND NOT EXISTS ( SELECT 1                                                             ");
		qry.append("                       FROM API_SEND_LOG C                                                ");
		qry.append("                      WHERE A.TEMPNO = C.SHIP_ID                                          ");
		qry.append("                        AND C.RESULT_CODE = '000'       )                                  ");

		
		
		return qry.toString();
	}

	//SEND 주문건별 송장전송 데이터
	private String getShopSendDeliveryData() {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" SELECT D.CD1                                                                             ");
		qry.append("      , A.TEMPNO                                                                          ");
		qry.append("      , B.REFNM                                                                           ");
		qry.append("      , A.EXPNM                                                                           ");
		qry.append("      , A.VDCD                                                                            ");
		qry.append("   FROM TBD03C A                                                                          ");
		qry.append("      , TBB150 B                                                                          ");
		qry.append("      , ( SELECT DISTINCT CD1                                                             ");
		qry.append("            FROM TBB150                                                                   ");
		qry.append("           WHERE REFTP = 'ZW'                                                             ");
		qry.append("             AND REFCD <> '0000'                                                          ");
		qry.append("        ) D                                                                               ");
		qry.append("      , TBB050 E                                                                          ");
		qry.append("  WHERE NVL(A.EXPNO, '09') = B.REFCD                                                      ");
		qry.append("    AND A.VDCD  = E.VDCD                                                                  ");
		qry.append("    AND B.REFTP = 'ZV'                                                                    ");
		qry.append("    AND B.REFCD <> '0000'                                                                 ");
		qry.append("    AND A.CBGU  = '1'                                                                     ");
		qry.append("    AND A.GUBUN = '1'                                                                     ");
		qry.append("    AND A.POYN  = 'Y'                                                                     ");
		qry.append("    AND A.OUTDT BETWEEN TO_CHAR(SYSDATE-7, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')   ");
		qry.append("    AND E.CD5 IS NOT NULL                                                                 ");
		qry.append("    AND NOT EXISTS ( SELECT 1                                                             ");
		qry.append("                       FROM API_SEND_LOG C                                                ");
		qry.append("                      WHERE A.TEMPNO = C.SHIP_ID                                          ");
		qry.append("                        AND C.RESULT_CODE = '000' )                                        ");
		qry.append("    AND A.TEMPNO = ?                                                                        ");
		qry.append("    AND A.EXPNM = ?                                                                        ");
		
		return qry.toString();
	}

	 
	//RECV 주문수집
	private String getShopRecvOrderList() {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" SELECT CD1                                                 ");
		qry.append("      , CD2                                                 ");
		qry.append("      , CD3                                                 ");
		qry.append("      , CD4                                                 "); 
		qry.append("      , TO_CHAR(SYSDATE-15,'YYYYMMDDHH24MISS') AS STA_DT     ");
		qry.append("      , TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS')   AS END_DT     ");
		qry.append("   FROM TBB150                                              ");
		qry.append("  WHERE REFTP = 'ZW'                                        ");
		qry.append("    AND REFCD <> '0000'                                     ");
		
		return qry.toString();
	}


	private String setErrorRecvLog() {
		//RECV 에러발생시 UPDATE
		StringBuffer qry = new StringBuffer();

		qry.append(" UPDATE API_SEND_LOG                                           ");
		qry.append("    SET RESULT_CODE = '100'     							   ");
		qry.append("    ,RESULT_NAME = ?    				   ");
		qry.append("     WHERE  CALL_DT       = ?                   ");
		qry.append("     AND    CALL_SEQ      = ?               ");

		return qry.toString();
	}

	//발주확인 대상 조회
	private String OrderConfirm(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT DISTINCT																				");
		qry.append("       B.REFCD          AS VENDOR_ID                                                        ");
		qry.append("     , SUBSTR(A.TEMPNO, 1,LENGTH(A.TEMPNO) -3) AS TEMPNO                                    ");
		qry.append("  FROM TBD03C A                                                                             ");
		qry.append("     , TBB150 B                                                                             ");
		qry.append(" WHERE A.VDCD     = B.CD1                                                                   ");
		qry.append("   AND A.CD22     = B.REFCD                                                                 ");
		qry.append("   AND B.REFTP    = 'ZY'                                                                    ");
		qry.append("   AND B.CD4      = '"+ transcd +"'                                                         ");
		qry.append("   AND A.CBGU     = '1'                                                                     ");
		qry.append("   AND A.GUBUN    = '1'                                                                     ");
		qry.append("   AND A.REDT BETWEEN TO_CHAR(SYSDATE -7, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')      ");
		qry.append("   AND A.PODT    >= '20120404'                                                              ");
		qry.append("   AND A.POSEQ    < 90001                                                                   ");
		qry.append("   AND NOT EXISTS ( SELECT 1                                                                ");
		qry.append("                      FROM API_SEND_LOG C                                                   ");
		qry.append("                     WHERE SUBSTR(A.TEMPNO, 1,LENGTH(A.TEMPNO) -3) = C.SHIP_ID              ");
		qry.append("                       AND C.RESULT_CODE = '000'                                            ");
		qry.append("                       AND C.CALL_API    = 'OrderConfirm'                                   ");
		qry.append("                  )                                                                         ");
		 
		return qry.toString();
	}

	//배송정보등록
	private String DeliveryInsert(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT DISTINCT                                                                                                             	"); 
		qry.append("	   B.REFCD                                  AS VENDOR_ID                                                                    "); 
		qry.append("	 , SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) AS TEMPNO                                                                       "); 
		qry.append("	 , A.PONO                                                                        											"); 
		qry.append("	 , TRIM(TO_CHAR(A.POSEQ, '000')) 			AS ORDER_SEQ                                                                    "); 
		qry.append("	 , A.OUTDT                                  AS OUTDT                                                                        "); 
		qry.append("	 , SUBSTR(A.UPDTIME, 9, 6)                  AS UPDTIME                                                                      "); 
		qry.append("	 , CASE A.COCD WHEN '95' THEN DECODE(NVL(A.EXPNM,''), '', SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) || '*', A.EXPNM)         "); 
		qry.append("	   ELSE A.EXPNM END                         AS EXPNM                                                                        "); 
		qry.append("	 , CASE A.COCD WHEN '95' THEN DECODE(NVL(A.EXPNO,''), '', '04', DECODE(A.EXPNO,'00','99',A.EXPNO))                          "); 
		qry.append("	   ELSE DECODE(A.EXPNO,'00','99',A.EXPNO) END AS EXPNO                                                                      "); 
		qry.append("  FROM TBD03C A                                                                                                                 "); 
		qry.append("	 , TBB150 B                                                                                                                 "); 
		qry.append(" WHERE A.VDCD  = B.CD1                                                                                                          "); 
		qry.append("   AND A.CD22  = B.REFCD                                                                                                        "); 
		qry.append("   AND B.REFTP = 'ZY'                                                                                                           "); 
		qry.append("   AND B.CD4   = '"+ transcd +"'                                                                                                "); 
		qry.append("   AND A.CBGU  = '1'                                                                                                            "); 
		qry.append("   AND A.GUBUN = '1'                                                                                                            "); 
		qry.append("   AND A.OUTDT BETWEEN TO_CHAR(SYSDATE -7, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')                                         ");
// [IOS 16-FEB-16] 큐브 요청에 의거 -20일로 변경했었음, 원 코드는 -7
//		qry.append("   AND A.OUTDT BETWEEN TO_CHAR(SYSDATE -20, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')                                         ");
		qry.append("   AND A.PODT >= DECODE('"+ transcd +"', '20', '20131203', '20120404')                                                          "); 
		qry.append("   AND A.POSEQ < 90001                                                                                                          "); 
		qry.append("   AND NOT EXISTS ( SELECT 1                                                                                                    "); 
		qry.append("					  FROM API_SEND_LOG C                                                                                       "); 
		qry.append("					 WHERE B.REFCD       = C.VENDOR_ID                                                                          "); 
		qry.append("					   AND SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) = C.SHIP_ID                                                 "); 
		qry.append("					   AND C.RESULT_CODE = '000'                                                                                "); 
		qry.append("					   AND C.CALL_API    = 'DeliveryInsert'                                                                     "); 
		qry.append("				  )                                                                                                             "); 
		qry.append("                                                                                                                                ");
		
		return qry.toString();
	}
	
	//제휴사출고지시취소처리
	private String SoldOutCancel(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT DISTINCT                                                                                                                 ");
		qry.append("	   B.REFCD                                  AS VENDOR_ID                                                                    ");
		qry.append("	 , SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) AS TEMPNO                                                                       ");
		qry.append("	 , SUBSTR(A.PONO, 1, LENGTH(A.PONO) -2) 	AS PONO                                                                        	");
		qry.append("	 , DECODE(NVL(A.CD1, '3Z'),'단순변심','31','품절','33','3Z') AS CANCEL_CODE                                                 ");
		qry.append("	 , TRIM(TO_CHAR(A.POSEQ, '000')) AS ORDER_SEQ                                                                               ");
		qry.append("	 , SUBSTR(A.UPDTIME, 1,8)  AS CANCEL_DT                                                                                     ");
		qry.append("	 , NVL(a.CD1,'')  AS  CANCEL_NM                                                                                     ");		
		qry.append("  FROM TBD03C A                                                                                                                 ");
		qry.append("	 , TBB150 B                                                                                                                 ");
		qry.append(" WHERE A.VDCD  = B.CD1                                                                                                          ");
		qry.append("   AND B.REFTP = 'ZY'                                                                                                           ");
		qry.append("   AND A.CBGU  = '1'                                                                                                            ");
		qry.append("   AND A.GUBUN = '1'                                                                                                            ");
		qry.append("   AND SUBSTR(A.UPDTIME, 1,8) BETWEEN TO_CHAR(SYSDATE -7, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')                          ");
		qry.append("   AND A.PODT >= DECODE('"+ transcd +"', '20', '20131203', '20120404')                                                          ");
		qry.append("   AND A.POSEQ < 90001                                                                                                          ");
		qry.append("   AND A.CD22  = B.REFCD                                                                                                        ");
		qry.append("   AND A.PONO  LIKE '%-C'                                                                                                       ");
		qry.append("   AND A.IMPCD = 'N'                                                                                                            ");
		qry.append("   AND A.CD1  <> 'API취소'                                                                                                      ");
		qry.append("   AND B.CD4   = '"+ transcd +"'                                                                                                ");
		qry.append("   AND NOT EXISTS ( SELECT 1                                                                                                    ");
		qry.append("					  FROM API_SEND_LOG C                                                                                       ");
		qry.append("					 WHERE B.REFCD       = C.VENDOR_ID                                                                          ");
		qry.append("					   AND DECODE('"+ transcd +"', '30', A.TEMPNO, SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3)) = C.SHIP_ID		");
		qry.append("					   AND C.RESULT_CODE = '000'                                                                                ");
		qry.append("					   AND C.CALL_API    = 'SoldOutCancel'                                                                      ");
		qry.append("				  )                                                                                                             ");
		qry.append("                                                                                                                                ");
		
		return qry.toString();
	}
	
	//반품정보확인
	private String OrderReturnConfirm(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT DISTINCT                                     								  	");
		qry.append("       B.REFCD          AS VENDOR_ID                                                  	");
		qry.append("     , SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) AS TEMPNO                             	");
		qry.append("  FROM TBD03C A                                                                       	");
		qry.append("     , TBB150 B                                                                       	");
		qry.append(" WHERE A.VDCD  = B.CD1                                                                	");
		qry.append("   AND A.CD22  = B.REFCD                                                              	");
		qry.append("   AND B.CD4   = '"+ transcd +"'                                                      	");
		qry.append("   AND B.REFTP = 'ZY'                                                                 	");
		qry.append("   AND A.CBGU  = '2'                                                                  	");
		qry.append("   AND A.GUBUN = '1'                                                                  	");
		qry.append("   AND A.REDT  BETWEEN to_char(sysdate-7,'YYYYMMDD') AND to_char(sysdate,'YYYYMMDD')  	");
		qry.append("   AND A.PODT >= '20120404'                                                           	");
		qry.append("   AND A.POSEQ < 90001                                                                  ");
		qry.append("   AND NOT EXISTS ( SELECT 1                                                          	");
		qry.append("                      FROM API_SEND_LOG C                                             	");
		qry.append("                     WHERE SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) = C.SHIP_ID       	");
		qry.append("                       AND B.REFCD       = C.VENDOR_ID                                	");
		qry.append("                       AND C.RESULT_CODE = '000'                                      	");
		qry.append("                       AND CALL_API      = 'OrderReturnConfirm'                       	");
		qry.append("                  )                                                                   	");	
		
		return qry.toString();
	}
	
	//반품수거등록
	private String ReturnPickUpInsert(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT DISTINCT                                                                                                                  ");
		qry.append("       B.REFCD                 AS VENDOR_ID                                                                                      ");
		qry.append("     , SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3)       AS TEMPNO                                                                  ");
		qry.append("     , A.PONO                                                                  													 ");
		qry.append("     , TRIM(TO_CHAR(A.POSEQ, '000')) AS ORDER_SEQ                                                                  				 ");
		qry.append("     , A.OUTDT                 AS APPLY_DT                                                                                       ");
		qry.append("     , SUBSTR(A.UPDTIME, 9, 6) AS APPLY_TIME                                                                                     ");
		qry.append("     , A.EXPNM                 AS EXPNM                                                                                          ");
		qry.append("     , NVL(DECODE(a.expno, '00', '99', a.expno),'99') AS expno                                                                   ");
		qry.append("  FROM TBD03C A                                                                                                                  ");
		qry.append("     , TBB150 B                                                                                                                  ");
		qry.append(" WHERE A.VDCD  = B.CD1                                                                                                           ");
		qry.append("   AND A.CD22  = B.REFCD                                                                                                         ");
		qry.append("   AND B.CD4   = '"+ transcd +"'                                                                                                 ");
		qry.append("   AND B.REFTP = 'ZY'                                                                                                            ");
		qry.append("   AND A.CBGU  = '2'                                                                                                             ");
		qry.append("   AND A.GUBUN = '1'                                                                                                             ");
		qry.append("   AND A.OUTDT BETWEEN to_char(sysdate-7,'YYYYMMDD') AND to_char(sysdate,'YYYYMMDD')                                             ");
		qry.append("   AND A.PODT >= DECODE('"+ transcd +"', '20', '20131203', '20120404')                                                           ");
		qry.append("   AND A.POSEQ < 90001                                                                                                           ");
		qry.append("   AND NOT EXISTS ( SELECT 1                                                                                                     ");
		qry.append("                      FROM API_SEND_LOG C                                                                                        ");
		qry.append("                     WHERE SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) = C.SHIP_ID                                                  ");
		qry.append("                       AND B.REFCD       = C.VENDOR_ID                                                                           ");
		qry.append("                       AND C.RESULT_CODE = '000'                                                                                 ");
		qry.append("                       AND C.CALL_API    = 'ReturnPickUpInsert'                                                                  ");
		qry.append("                  )                                                                                                              ");
		
		return qry.toString();
	}
	
	//반품취소처리    
	private String ReturnRefuse(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT DISTINCT                                                                                                                  ");
		qry.append("       B.REFCD AS VENDOR_ID                                                                                                      ");
		qry.append("     , SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) AS TEMPNO                                                                        ");
		qry.append("     , SUBSTR(A.PONO, 1, LENGTH(A.PONO) -2) AS PONO                                                                        		 ");
		qry.append("     , TRIM(TO_CHAR(A.POSEQ, '000')) AS ORDER_SEQ                                                                                ");
		qry.append("     , SUBSTR(A.UPDTIME, 1,8)  AS CANCEL_DT                                                                                      ");
		qry.append("  FROM TBD03C A                                                                                                                  ");
		qry.append("     , TBB150 B                                                                                                                  ");
		qry.append(" WHERE A.VDCD  = B.CD1                                                                                                           ");
		qry.append("   AND A.CD22  = B.REFCD                                                                                                         ");
		qry.append("   AND B.CD4   = '"+ transcd +"'                                                                                                 ");
		qry.append("   AND B.REFTP = 'ZY'                                                                                                            ");
		qry.append("   AND A.CBGU  = '2'                                                                                                             ");
		qry.append("   AND A.GUBUN = '1'                                                                                                             ");
		qry.append("   AND SUBSTR(A.UPDTIME, 1, 8) BETWEEN to_char(sysdate-7,'YYYYMMDD') AND to_char(sysdate,'YYYYMMDD')                             ");
		qry.append("   AND A.PODT >= DECODE('"+ transcd +"', '20', '20131203', '20120404')                                                           ");
		qry.append("   AND A.POSEQ < 90001                                                                                                           ");
		qry.append("   AND A.PONO  LIKE '%-D'                                                                                                        ");
		qry.append("   AND A.IMPCD = 'N'                                                                                                             ");
		qry.append("   AND A.CD1  <> 'API취소'                                                                                                       ");
		qry.append("   AND NOT EXISTS ( SELECT 1                                                                                                     ");
		qry.append("                      FROM API_SEND_LOG C                                                                                        ");
		qry.append("                     WHERE SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) = C.SHIP_ID                                                  ");
		qry.append("                       AND B.REFCD       = C.VENDOR_ID                                                                           ");
		qry.append("                       AND C.RESULT_CODE = '000'                                                                                 ");
		qry.append("                       AND C.CALL_API    = 'ReturnRefuse'                                                                        ");
		qry.append("                  )                                                                                                              ");	
		
		return qry.toString();
	}
	
	//발주조회 등록 후 주문완료 정보 확인
	private String OrderRetrieveCheck(String call_dt, String call_seq, String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT DISTINCT VENDOR_ID							");
		qry.append("       ,SHIP_ID										");
		qry.append("       ,PONO										");
		qry.append("       ,TRIM(TO_CHAR(POSEQ,'000')) AS POSEQ			");
		qry.append("FROM   API_RECV_DATA								");
		qry.append("WHERE  CALL_DT  	= '"+ call_dt +"'				");
		qry.append("AND    CALL_SEQ  	= '"+ call_seq +"'				");
		qry.append("AND	   RECV_GB		= '10'							");
		qry.append("AND    ERROR_CODE   = '01'							");
		qry.append("AND    TRANS_STATUS = '"+ transcd +"'				");
		
		return qry.toString();
	}
	
	//반품정보조회 후 반품 정보 전송
	private String OrderReturnRetrieveCheck(String call_dt, String call_seq, String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT DISTINCT VENDOR_ID							");
		qry.append("       ,SHIP_ID										");
		qry.append("       ,PONO										");
		qry.append("       ,TRIM(TO_CHAR(POSEQ,'000')) AS POSEQ			");
		qry.append("FROM   API_RECV_DATA								");
		qry.append("WHERE  CALL_DT  	= '"+ call_dt +"'				");
		qry.append("AND    CALL_SEQ  	= '"+ call_seq +"'				");
		qry.append("AND	   RECV_GB		= '30'							");
		qry.append("AND    ERROR_CODE   = '01'							");
		qry.append("AND    TRANS_STATUS = '"+ transcd +"'				");
		
		return qry.toString();
	}


	//벤더, 조회일자 조회
	private String getVendorList(String transcd) {
		StringBuffer qry = new StringBuffer();

		qry.append(" SELECT	REFCD AS VENDOR_ID,                         ");
		qry.append(" TO_CHAR(SYSDATE-25,'YYYYMMDD') AS STA_DT,  	        ");
		qry.append(" TO_CHAR(SYSDATE,'YYYYMMDD') AS END_DT	            ");
		qry.append("      FROM	TBB150								    ");
		qry.append("        WHERE	REFTP = 'ZY'				        ");
		qry.append("        AND REFCD <> '0000'                         ");
		qry.append("        AND CD4 = '"+transcd+"'                     ");
		
		return qry.toString();
	}

	//송신 대상 로그 등록
	public String setSendLog() {
		StringBuffer qry = new StringBuffer();

		qry.append(" INSERT INTO API_SEND_LOG                                           				");
		qry.append("       (CALL_DT, CALL_SEQ, CALL_API, CALL_API_NAME, VENDOR_ID, SHIP_ID,      		");
		qry.append("        APPLY_DT, APPLY_TIME, DELI_COMPANY_ID, BL_NO, RESULT_CODE,           		");
		qry.append("        RESULT_NAME, INUSER, INTIME, CANCEL_CODE									");
		qry.append("       )                                                                     		");
		qry.append("      VALUES ( ?, ?, ?, ?, ?, ?,                    								");
		qry.append("              ?, ?, ?, ?, ?,                                                 		");
		qry.append("              ?, ?, to_char(sysdate, 'YYYYMMDDHH24MISS' ), ?						");
		qry.append("             )                                                               		");

		Logger.debug(qry.toString());
		return qry.toString();
	}
	
	
	/*
	 * 2015.08.31 STORE_GB 추가 by lee
	 * VENDOR_PONO 추가 [IOS 26-JAN-16]
	 * */
	public String setRecvData(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" INSERT INTO API_RECV_DATA                                          			");  
		qry.append("        (CALL_DT, CALL_SEQ, SEQ, RECV_GB, FIRST_ORDER_ID, ORDER_ID,          	"); 
		qry.append("        SHIP_ID, TRANS_DT, VENDOR_ID, INSTRUCT_DT, CANCEL_DT, CHANGE_GB,     	"); 
		qry.append("        SHIP_STATUS, RECEIPT_NM, RECEIPT_TEL, RECEIPT_HP,                    	"); 
		qry.append("        RECEIPT_ZIPCODE, RECEIPT_ADDR1, RECEIPT_ADDR2, CUST_NM,              	"); 
		qry.append("        CUST_TEL, CUST_HP, CUST_ZIPCODE, CUST_ADDR1, CUST_ADDR2,             	"); 
		qry.append("        DELIVERY_MSG, ORDER_SEQ, SHIP_SEQ, ITEM_CD, ITEM_NM, OPTION1,        	"); 
		qry.append("        OPTION2, SALE_PRICE, DELI_PRICE, QTY, DELI_GB, ERROR_CODE,           	"); 
		qry.append("        ERROR_MSG, INUSER, INTIME, ORI_SHIP_ID , RET_CODE, TRANS_STATUS,	 	");
		qry.append("        CUST_EMAIL, CLAME_MEMO, CUBE_ITEM , COCD , WHCD	, ORDER_KEY, 			");
		qry.append("        ORDERSEQ_KEY, SHIP_KEY, VENDOR_NM , STORE_GB , VENDOR_PONO				");
		qry.append("             )                                                               	"); 
		qry.append("      VALUES ( ?, ?, ?, ?, ?, ?,            						         	"); 
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	"); 
		qry.append("              ?, ?, ?, ?,                                                    	"); 
		qry.append("              ?, ?, ?, ?,                                                    	"); 
		qry.append("              ?, ?, ?, ?, ?,                                                 	"); 
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	"); 
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	"); 
		qry.append("              ?, ?, to_char(sysdate, 'YYYYMMDDHH24MISS'), ?, ?, '"+transcd+"',	");
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	");
		qry.append("              ?, ?, ?, ?, ?					                                    ");
		qry.append("             )                                                               	"); 
		
		return qry.toString();
	}
	
	// 반품추가(하윤식 2015.02.25)
	public String setRecvReturnData(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" INSERT INTO API_RECV_DATA                                          			");  
		qry.append("        (CALL_DT, CALL_SEQ, SEQ, RECV_GB, FIRST_ORDER_ID, ORDER_ID,          	"); 
		qry.append("        SHIP_ID, TRANS_DT, VENDOR_ID, INSTRUCT_DT, CHANGE_GB,                   "); 
		qry.append("        SHIP_STATUS, RECEIPT_NM, RECEIPT_TEL, RECEIPT_HP,                    	"); 
		qry.append("        RECEIPT_ZIPCODE, RECEIPT_ADDR1, RECEIPT_ADDR2, CUST_NM,              	"); 
		qry.append("        CUST_TEL, CUST_HP, CUST_ZIPCODE, CUST_ADDR1, CUST_ADDR2,             	"); 
		qry.append("        DELIVERY_MSG, ORDER_SEQ, SHIP_SEQ, ITEM_CD, ITEM_NM, OPTION1,        	"); 
		qry.append("        OPTION2, SALE_PRICE, DELI_PRICE, QTY, DELI_GB, ERROR_CODE,           	"); 
		qry.append("        ERROR_MSG, INUSER, INTIME,  RET_CODE, TRANS_STATUS,	 	                ");
		qry.append("        CUST_EMAIL, CLAME_MEMO, CUBE_ITEM , COCD , WHCD	, ORDER_KEY, 			");
		qry.append("        ORDERSEQ_KEY, SHIP_KEY, VENDOR_NM , RET_DESC, ITEM_STATUS, ORI_SHIP_ID	"); 
		qry.append("             )                                                               	"); 
		qry.append("      VALUES ( ?, ?, ?, ?, ?, ?,            						         	"); 
		qry.append("              ?, ?, ?, ?, ?,                                                	"); 
		qry.append("              ?, ?, ?, ?,                                                    	"); 
		qry.append("              ?, ?, ?, ?,                                                    	"); 
		qry.append("              ?, ?, ?, ?, ?,                                                 	"); 
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	"); 
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	"); 
		qry.append("              ?, ?, to_char(sysdate, 'YYYYMMDDHH24MISS'),  ?, '"+transcd+"',	");
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	");
		qry.append("              ?, ?, ?, ?, ?, ?||LPAD(?, 3, '0')  	     			            ");
		qry.append("             )                                                               	"); 
		
		return qry.toString();
	}
	
	// 반품취소 추가(하윤식 2015.03.12)
	public String setRecvReturnCancelData(String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" INSERT INTO API_RECV_DATA                                          			");  
		qry.append("        (CALL_DT, CALL_SEQ, SEQ, RECV_GB, FIRST_ORDER_ID, ORDER_ID,          	"); 
		qry.append("        SHIP_ID, TRANS_DT, VENDOR_ID, CANCEL_DT, CHANGE_GB,                     "); 
		qry.append("        SHIP_STATUS, RECEIPT_NM, RECEIPT_TEL, RECEIPT_HP,                    	"); 
		qry.append("        RECEIPT_ZIPCODE, RECEIPT_ADDR1, RECEIPT_ADDR2, CUST_NM,              	"); 
		qry.append("        CUST_TEL, CUST_HP, CUST_ZIPCODE, CUST_ADDR1, CUST_ADDR2,             	"); 
		qry.append("        DELIVERY_MSG, ORDER_SEQ, SHIP_SEQ, ITEM_CD, ITEM_NM, OPTION1,        	"); 
		qry.append("        OPTION2, SALE_PRICE, DELI_PRICE, QTY, DELI_GB, ERROR_CODE,           	"); 
		qry.append("        ERROR_MSG, INUSER, INTIME,  RET_CODE, TRANS_STATUS,	 	                ");
		qry.append("        CUST_EMAIL, CLAME_MEMO, CUBE_ITEM , COCD , WHCD	, ORDER_KEY, 			");
		qry.append("        ORDERSEQ_KEY, SHIP_KEY, VENDOR_NM , RET_DESC, ITEM_STATUS, ORI_SHIP_ID	"); 
		qry.append("             )                                                               	"); 
		qry.append("      VALUES ( ?, ?, ?, ?, ?, ?,            						         	"); 
		qry.append("              ?, ?, ?, ?, ?,                                                	"); 
		qry.append("              ?, ?, ?, ?,                                                    	"); 
		qry.append("              ?, ?, ?, ?,                                                    	"); 
		qry.append("              ?, ?, ?, ?, ?,                                                 	"); 
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	"); 
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	"); 
		qry.append("              ?, ?, to_char(sysdate, 'YYYYMMDDHH24MISS'),  ?, '"+transcd+"',	");
		qry.append("              ?, ?, ?, ?, ?, ?,                                              	");
		qry.append("              ?, ?, ?, ?, ?, ?||LPAD(?, 3, '0')  	     			            ");
		qry.append("             )                                                               	"); 
		
		return qry.toString();
	}

	
	//수신 대상 로그 등록
	public String setRecvLog() {
		StringBuffer qry = new StringBuffer(); 
		
		qry.append(" INSERT INTO API_RECV_LOG (       		 ");
		qry.append("    CALL_DT, CALL_SEQ, CALL_API,             	 ");
		qry.append("    CALL_API_NAME, VENDOR_ID, START_DT,     	 ");
		qry.append("    END_DT, RESULT_CODE, RESULT_NAME,       	 "); 
		qry.append("    INUSER, INTIME)                         	 ");
		qry.append(" VALUES ( ? ,? ,? ,?   ");
		qry.append("     ,? , ?,?                               	 ");  
		qry.append("     , ?,? ,?                               	 "); 
		qry.append("     ,to_char(sysdate, 'YYYYMMDDHH24MISS')       ");
		qry.append("     )       ");
		
		return qry.toString();
	}
	
	//수신대상 차수
	public String getRecvCallSeq() {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" SELECT /*+ INDEX_DESC(CALL_DT,CALL_SEQ) */						 				 ");
		qry.append("        LTRIM(TO_CHAR(TO_NUMBER(NVL(MAX(CALL_SEQ), 0)) + 1, '0000')) as CALL_SEQ ");
		qry.append("   FROM API_RECV_LOG															 ");
		qry.append("  WHERE CALL_DT = TO_CHAR(SYSDATE,'YYYYMMDD') 						 			 ");
		//qry.append("  WHERE CALL_DT = ?						 			 ");
		
		
		return qry.toString();
	}
	
	
	//송신대상 차수
	public String getSendCallSeq() {
		StringBuffer qry = new StringBuffer();
		
		qry.append(" SELECT /*+ INDEX_DESC(CALL_DT,CALL_SEQ) */						 				 ");
		qry.append("        LTRIM(TO_CHAR(TO_NUMBER(NVL(MAX(CALL_SEQ), 0)) + 1, '0000')) as CALL_SEQ ");
		qry.append("   FROM API_SEND_LOG															 ");
		qry.append("  WHERE CALL_DT = TO_CHAR(SYSDATE,'YYYYMMDD') 						 			 ");
		
		return qry.toString();
	}
	
	
	// 출고확정 Header 정보
	private String deliveryHeaderInsert(String cocd, String vdcd, String transcd) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT  A.COCD  					  AS  COCD				                                                                "); 
		qry.append("	  , B.REFCD                       AS  VENDOR_ID                                                                    		"); 
		qry.append("	  , A.PONO                        AS  PONO                                               								"); 
		qry.append("	  , A.ORDERHEADERKEY              AS  ORDERHEADERKEY                                                          			"); 
		qry.append("  FROM TBD03C A                                                                                                             "); 
		qry.append("	 , TBB150 B                                                                                                             "); 
		qry.append(" WHERE A.VDCD  = B.CD1                                                                                                      "); 
		qry.append("   AND A.CD22  = B.REFCD                                                                                                    "); 
		qry.append("   AND B.REFTP = 'ZY'                                                                                                       ");
		qry.append("   AND B.CD4   = '"+ transcd +"'                                                                                            ");		
		qry.append("   AND B.REFCD   = '"+ vdcd +"'                                                                                            	"); 
		qry.append("   AND A.CBGU  = '1'                                                                                                        "); 
		qry.append("   AND A.GUBUN = '1'                                                                                                        "); 
		qry.append("   AND A.COCD   = '"+ cocd +"'                                                                                            	");		
		qry.append("   AND A.OUTDT BETWEEN TO_CHAR(SYSDATE -7, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')                                     "); 
		qry.append("   AND A.PODT >= '20141028'                                                     											"); 
		qry.append("   AND A.POSEQ < 90001                                                                                                      "); 
		qry.append("   AND NOT EXISTS ( SELECT 1                                                                                                "); 
		qry.append("					  FROM API_SEND_LOG C                                                                                   "); 
		qry.append("					 WHERE B.REFCD       = C.VENDOR_ID                                                                      "); 
		qry.append("					   AND SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) = C.SHIP_ID                                             "); 
		qry.append("					   AND C.CALL_DT >= TO_CHAR(SYSDATE -10, 'YYYYMMDD')                                           			");
		qry.append("					   AND C.RESULT_CODE = '000'                                                                            "); 
		qry.append("					   AND C.CALL_API    = 'DeliveryInsert'                                                                 "); 
		qry.append("				  )                                                                                                         "); 
		qry.append("   GROUP BY A.COCD, B.REFCD, A.PONO, A.ORDERHEADERKEY                                                                       ");
		
		return qry.toString();
	}

	// 출고확정 Header 정보
	private String deliveryDetailInsert(String cocd, String vdcd, String poNo) {
		StringBuffer qry = new StringBuffer();
		
		qry.append("SELECT  A.WHCD  					  AS  WHCD				                                                                "); 
		qry.append("	  , A.POSEQ 					  AS  ORDER_SEQ                                                                        	"); 
		qry.append("	  , A.ORDERLINEKEY                AS  ORDERLINEKEY                                                          			"); 
		qry.append("	  , A.ORDERRELEASEKEY        	  AS  ORDERRELEASEKEY																	"); 
		qry.append("	  , A.RENO                        AS  RENO                                                                     			");   
		qry.append("	  , SUBSTR(A.UPDTIME, 9, 6)       AS  UPDTIME                                                                  			");	
		qry.append("	  , A.EXPNO                       AS  EXPNO                                                              				");
		qry.append("	  , A.EXPNM 					  AS  EXPNM                                                              				"); 
		qry.append("	  , A.OUTDT                       AS  OUTDT                                                                  			");
		qry.append("	  , SUBSTR(UPDTIME, 9, 6)         AS  OUTTIME                                                                  			");		
		qry.append("  FROM TBD03C A                                                                                                             ");  
		qry.append(" WHERE A.PONO  = '"+poNo+"'                                                                                                 "); 
		qry.append(" AND   A.COCD  = '"+cocd+"'                                                                                                 "); 
		qry.append(" AND   A.CD22  = '"+vdcd+"'                                                                                                 ");  
		qry.append(" AND   A.POYN  = 'Y'                                                                                                 		");  
		qry.append(" AND NOT EXISTS ( SELECT 1                             		                                                                "); 
		qry.append("					  FROM API_SEND_LOG C                                                                                   "); 
		qry.append("					 WHERE A.CD22       = C.VENDOR_ID                                                                      "); 
		qry.append("					   AND SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) = C.SHIP_ID                                             "); 
		qry.append("					   AND C.CALL_DT >= TO_CHAR(SYSDATE -10, 'YYYYMMDD')                                          			");
		qry.append("					   AND C.RESULT_CODE = '000'                                                                             "); 
		qry.append("					   AND C.CALL_API    = 'DeliveryInsert'                                                                 "); 
		qry.append("				  )                                                                                                         ");		
		return qry.toString();
	}	
	
	// 출고확정 Header 정보
		private String orConfirmHeaderInsert(String cocd, String vdcd, String transcd) {
			StringBuffer qry = new StringBuffer();
			
			qry.append("SELECT  A.COCD  					  AS  COCD				                                                                "); 
			qry.append("	  , B.REFCD                       AS  VENDOR_ID                                                                    		"); 
			qry.append("	  , A.PONO                        AS  PONO                                               								"); 
			qry.append("	  , A.ORDERHEADERKEY              AS  ORDERHEADERKEY                                                          			"); 
			qry.append("  FROM TBD03C A                                                                                                             "); 
			qry.append("	 , TBB150 B                                                                                                             "); 
			qry.append(" WHERE A.VDCD  = B.CD1                                                                                                      "); 
			qry.append("   AND A.CD22  = B.REFCD                                                                                                    "); 
			qry.append("   AND B.REFTP = 'ZY'                                                                                                       ");
			qry.append("   AND B.CD4   = '"+ transcd +"'                                                                                            ");		
			qry.append("   AND B.REFCD   = '"+ vdcd +"'                                                                                            	"); 
			qry.append("   AND A.CBGU  = '2'                                                                                                        "); 
			qry.append("   AND A.GUBUN = '1'                                                                                                        "); 
			qry.append("   AND A.COCD   = '"+ cocd +"'                                                                                            	");		
			qry.append("   AND A.OUTDT BETWEEN TO_CHAR(SYSDATE -7, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')                                     "); 
			qry.append("   AND A.PODT >= '20141028'                                                     											"); 
			qry.append("   AND A.POSEQ < 90001                                                                                                      "); 
			qry.append("   AND NOT EXISTS ( SELECT 1                                                                                                "); 
			qry.append("					  FROM API_SEND_LOG C                                                                                   "); 
			qry.append("					 WHERE B.REFCD       = C.VENDOR_ID                                                                      "); 
			qry.append("					   AND SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) = C.SHIP_ID                                             "); 
			qry.append("					   AND C.CALL_DT >= TO_CHAR(SYSDATE -10, 'YYYYMMDD')                                           			");
			qry.append("					   AND C.RESULT_CODE = '000'                                                                            "); 
			qry.append("					   AND C.CALL_API    = 'ReturnPickUpInsert'                                                             "); 
			qry.append("				  )                                                                                                         "); 
			qry.append("   GROUP BY A.COCD, B.REFCD, A.PONO, A.ORDERHEADERKEY                                                                       ");
			
			return qry.toString();
		}

		// 출고확정 Header 정보
		private String orConfirmDetailInsert(String cocd, String vdcd, String poNo) {
			StringBuffer qry = new StringBuffer();
			
			qry.append("SELECT  A.WHCD  					  AS  WHCD				                                                                "); 
			qry.append("	  , A.POSEQ 					  AS  ORDER_SEQ                                                                        	"); 
			qry.append("	  , A.ORDERLINEKEY                AS  ORDERLINEKEY                                                          			"); 
			qry.append("	  , A.ORDERRELEASEKEY        	  AS  ORDERRELEASEKEY																	"); 
			qry.append("	  , A.RENO                        AS  RENO                                                                     			");   
			qry.append("	  , SUBSTR(A.UPDTIME, 9, 6)       AS  UPDTIME                                                                  			");	
			qry.append("	  , A.EXPNO                       AS  EXPNO                                                              				");
			qry.append("	  , A.EXPNM 					  AS  EXPNM                                                              				"); 
			qry.append("	  , A.OUTDT                       AS  OUTDT                                                                  			");
			qry.append("	  , SUBSTR(UPDTIME, 9, 6)         AS  OUTTIME                                                                  			");		
			qry.append("  FROM TBD03C A                                                                                                             ");  
			qry.append(" WHERE A.PONO  = '"+poNo+"'                                                                                                 "); 
			qry.append(" AND   A.COCD  = '"+cocd+"'                                                                                                 "); 
			qry.append(" AND   A.CD22  = '"+vdcd+"'                                                                                                 ");  
			qry.append(" AND   A.POYN  = 'Y'                                                                                                 		");  
			qry.append(" AND NOT EXISTS ( SELECT 1                             		                                                                "); 
			qry.append("					  FROM API_SEND_LOG C                                                                                   "); 
			qry.append("					 WHERE A.CD22       = C.VENDOR_ID                                                                       "); 
			qry.append("					   AND SUBSTR(A.TEMPNO, 1, LENGTH(A.TEMPNO) -3) = C.SHIP_ID                                             "); 
			qry.append("					   AND C.CALL_DT >= TO_CHAR(SYSDATE -10, 'YYYYMMDD')                                          			");
			qry.append("					   AND C.RESULT_CODE = '000'                                                                            "); 
			qry.append("					   AND C.CALL_API    = 'ReturnPickUpInsert'                                                             "); 
			qry.append("				  )                                                                                                         ");		
			return qry.toString();
		}	
}








