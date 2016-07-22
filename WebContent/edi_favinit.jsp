<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="com.service.CubeService" %>
<%@ page import="com.service.command.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>
<%@ page import="org.apache.commons.httpclient.methods.PostMethod" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpException" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>FAVINIT & CUBE API 연동</title>
</head>
<body>
<%
	CubeService cs	= CubeService.getInstance();
	 
	String command = StringUtil.nullTo(request.getParameter("command"),"N/C");
	String dbmode = StringUtil.nullTo(request.getParameter("dbmode"),"");
	String inuser = StringUtil.nullTo(request.getParameter("inuser"),"SYSTEM");
	
	String transCD = "50";	//큐브 구분코드 (10:wizwid, 20:wck, 30:mangoKR, 50:favinit)
	String Connip = ""; 	//w컨셉 ip

	Connip = "http://prs.favinit.com";		//리얼
	//Connip = "http://testprs.favinit.com";	//테스트
	
	if (dbmode.equals("") || dbmode == null) {
		out.print("DB명이 올바르지않습니다.");
	} else {
		if (command.equals("SendProductData")) {  // 상품 등록/수정 송신 [IOS 19.Jul.16]
			cs.getSendProductData(dbmode,inuser, command, Connip, transCD);	
		} else if(command.equals("RecvProductData")) {  // 상품 등록결과 수신 [IOS 19.Jul.16]
			cs.getRecvProductData(dbmode,inuser, command, Connip, transCD);
		} else if (command.equals("SendItemStock")) {  // 상품 재고 송신 [IOS 19.Jul.16]
			cs.getSendItemStock(dbmode,inuser, command, Connip, transCD);		
		} else if (command.equals("RecvItemStock")) {  // 상품 재고 처리결과 수신 [IOS 19.Jul.16]
			cs.getRecvItemStock(dbmode,inuser, command, Connip, transCD);
		} else if (command.equals("OrderRetrieve")) {						//발주조회
			cs.getOrderRecvData(dbmode, inuser, command, Connip, transCD);  	
		} else if (command.equals("OrderConfirm")) {				//발주확인
			cs.getOrderSendData(dbmode, inuser, command,Connip, transCD);		//cs.sendOrderAfterCheck 에서 처리..
		} else if (command.equals("DeliveryInsert")) {				//배송정보등록
			cs.getOrderSendData(dbmode, inuser, command,Connip, transCD); 
		} else if (command.equals("SoldOutCancel")) {				//제휴사출고지시취소처리
			cs.getOrderSendData(dbmode, inuser, command,Connip, transCD);
		} else if (command.equals("OrderCancelRetrieve")) {			//취소정보조회
			cs.getOrderRecvData(dbmode, inuser, command, Connip, transCD);  	
		} else if (command.equals("OrderReturnRetrieve")) {			//반품정보조회
			cs.getOrderRecvData(dbmode, inuser, command, Connip, transCD);  	
		} else if (command.equals("OrderReturnConfirm")) {			//반품정보확인
			cs.getOrderSendData(dbmode, inuser, command,Connip, transCD);		//cs.sendOrderAfterCheck 에서 처리..
		} else if (command.equals("ReturnPickUpInsert")) {			//반품수거등록
			cs.getOrderSendData(dbmode, inuser, command,Connip, transCD);
		} else if (command.equals("OrderReturnCancelRetrieve")) {	//반품취소정보조회 
			cs.getOrderRecvData(dbmode, inuser, command, Connip, transCD);  	
		} else if (command.equals("ReturnRefuse")) {				//반품 취소 처리
			cs.getOrderSendData(dbmode, inuser, command,Connip, transCD);
		} else {
			cs.setRecvLog(dbmode, inuser, command, command, "N/A", CommonUtil.getCurrentDate(), CommonUtil.getCurrentDate(), "500", "It's wrong cammand!", transCD);
		}
	}
	
%>
</body>
</html>