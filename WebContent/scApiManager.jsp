<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="com.service.ScApiCreateREDIS" %>
<%@ page import="com.service.command.util.*" %> 
<%@ page import="java.util.*" %> 
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>
<%@ page import="org.apache.commons.httpclient.methods.PostMethod" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpException" %>

<%
	ScApiCreateREDIS redisDAO	= ScApiCreateREDIS.getInstance();

	String dbmode	= StringUtil.nullTo(request.getParameter("dbmode"),"");
	String command	= StringUtil.nullTo(request.getParameter("command"),"");
	System.out.println("############ : dbmode=" + dbmode);
	System.out.println("############ : command=  " + command);
	
	
	String transCD = "40";	
	//큐브 구분코드 ( 10:wizwid, 20:wconcept, 30:mangoKR , 40:Magento )
	
	String resultMessage = null;
	//거부용
	String resultMessage2 = "";

	
	if (dbmode.equals("") || dbmode == null) {		
		resultMessage = "DB명이 올바르지않습니다.";
	} else {
		
		// 상품 등록/수정 송신
		if (command.equals("SendProductData")) {
			resultMessage = redisDAO.api_SendProductData(dbmode,transCD);	

		// 상품 등록결과 수신
		}else if(command.equals("RecvProductData")) {
		
			resultMessage = redisDAO.api_RecvProductData(dbmode,transCD);
		
		// 상품 재고 송신
		}else if (command.equals("SendItemStock")) {
			
			resultMessage = redisDAO.api_Auto_SendItemStock(dbmode,transCD);		
		
		// 상품 재고 처리결과 수신	
		}else if (command.equals("RecvItemStock")) {

			resultMessage = redisDAO.api_Auto_RecvItemStock(dbmode,transCD);
			
		// 주문 / 주문취소	
		} else if (command.equals("OrderProcess")) {
			resultMessage = redisDAO.api_Auto_PO(dbmode,command,transCD);
			
		// 반품 / 반품취소	
		} else if (command.equals("OrderReturnProcess")) {
		
			resultMessage = redisDAO.api_Auto_ReturnPO(dbmode,command,transCD);
		// 출고확정
		} else if (command.equals("DeliveryInsert")) {
			/*매장 거부: 출고확정 정보전송 전 매장 거부에 대한 정보가 넘어가 함 20150904 
			api_Auto_StoreReject 의 REDIS KEY 도 화정 정보 송신 키로 수정함 
			command 가 매장거부일 겨우 StoreCancel 이여야 함
			BY LEE */
			//String command_storeReject = "StoreCancel";
			//resultMessage2 = redisDAO.api_Auto_StoreReject(dbmode,command_storeReject,transCD);
			resultMessage = redisDAO.api_Delivery_Send(dbmode,command,transCD);
		} else if (command.equals("ReturnPickUpInsert")) {
		
			resultMessage = redisDAO.api_Auto_ReturnConfirm(dbmode,command,transCD);
		} 
		
		/*		
		else if (command.equals("Call")) {
					
			resultMessage = redisDAO.api_Auto_PO_Send(dbmode,"OrderCancelRetrieve","20141028","0352","20141028124019","40","100000285","20141028","20141028092000213475","80","ASPB");			
		
		}
		*/
		else if(command.equals("SendProductDataRedMarker")) {									// 상품 등록 송신 RedMarker
				resultMessage = redisDAO.api_SendProductData_RedMarker(dbmode,transCD);	
		}
		else if (command.equals("SendItemStockRedMarker")) {									// 상품 재고 송신 RedMarker
			resultMessage = redisDAO.api_Auto_SendItemStock_RedMarker(dbmode,transCD);		
		} else if (command.equals("SoldOutCancel") || command.equals("ReturnRefuse")){ 			// 품절취소, 반품거부 2016.04.15
			resultMessage = redisDAO.api_Auto_SoldOutRefuse_Cancel(dbmode,command,transCD);
		}
		//매장 직출 거부 20154.08.28  by lee
		else if(command.equals("StoreCancel")){
			resultMessage = redisDAO.api_Auto_StoreReject(dbmode,command,transCD);
		}
		else{
			
			resultMessage = "MODE FAIL!!";
		}
				

	}
	
%>
<%= resultMessage2 + resultMessage%>