package com.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;    
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node; 
import org.w3c.dom.NodeList;

import com.service.command.connection.DataBaseManager;
import com.service.command.util.CommonUtil;
import com.service.command.util.StringUtil;
import com.service.dao.ServiceDAO;
import com.service.entity.ServiceDataInfo;
import com.service.entity.ServiceLogInfo;
import com.service.entity.ServiceShoplinkerInfo;
import com.service.CubeApiCreateJSON;

import com.service.command.log.Logger;
//import com.sun.java_cup.internal.production;

/**
 * CubeService.java
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

public class CubeService {
	

	private static CubeService instance = new CubeService();

	public static CubeService getInstance() {
		return instance;
	}

	private CubeService() {
		
	}
	
	
	/**
	 * 샵링커 API SEND DATA 
	 * 
	 * @param dbmode 
	 * @param inuser
	 * @param call_api
	 * @return
	 * @throws Exception
	 * @throws SQLException
	 */
	public String getShopOrderSendData(String dbmode, String inuser, String call_api)  throws Exception, SQLException{
		Connection conn = null;
		
		String server = "";
		Element root = null;
		
		Map<String, String> map = null;
		List spList = null;
		
		String result = "";
		String message = "";
		String result_code="";
		String ship_id = "";  			//샵링커 주문번호
		String order_id = "";
		String expnm = "";
		String vdcd = "";

		int send_no = 0;
		String call_seq = "";
		
		
		try {
			ServiceDAO dao = new ServiceDAO();
			
			conn = DataBaseManager.getConnection(dbmode);
			
			if(call_api.equals("ShopSendDelivery")){
				spList = dao.getShopSendDeliveryList(conn);
				
				
				if(spList != null && spList.size() > 0){
					
					for(int i = 0 ; i<spList.size();i++){
						map =  (HashMap<String, String>) spList.get(i);
						
						order_id = StringUtil.checkNull(map.get("tempno"));
						expnm = StringUtil.checkNull( map.get("expnm"));
						vdcd = StringUtil.checkNull( map.get("vdcd"));
						send_no = i+1;
						Logger.debug("[VDCD]"+vdcd);
						
						server="http://apiweb.shoplinker.co.kr/ShoplinkerApi/Order/delivery.php?iteminfo_url=http://api.isehq.com:8080/shop_delivery_"+dbmode+".jsp?order_id="+order_id+"|"+send_no+"|"+expnm; // xmlURL
						//server.append("http://api.isehq.com:8080/shop_delivery_data.jsp"); // xmlURL
						Logger.debug("[server]"+server.toString());
						
						Document document = getDocument(server.toString());
						@SuppressWarnings("unused")
						NodeList noneList = document.getDocumentElement().getChildNodes();
						root = document.getDocumentElement();
						
						result = StringUtil.nullTo(root.getElementsByTagName("result").item(0).getFirstChild().getNodeValue(),"");
						message = StringUtil.nullTo(root.getElementsByTagName("message").item(0).getFirstChild().getNodeValue(),"");
						
						if(result.equals("true")){
							ship_id = StringUtil.nullTo(root.getElementsByTagName("id").item(0).getFirstChild().getNodeValue(),"");	
							result_code="000";
						}else{
							ship_id ="N/A";
							result_code="100";
						}
						
						Logger.debug("[ship_id]"+ship_id+"[result]"+result+"[message]"+message);
						
						// 샵링커는 24 iseccube로 우선 고정 modified by 20120822
						//dbmode="iseccube";
						setSendLog(dbmode, inuser, call_api, getApiName(call_api), vdcd, ship_id, "", "", "", "", result_code, message, "", "");
						//샵링커쪽send log 들어갈 수 있을것같아 미리 민들어 두었으나 사용하지 않고있음
						//setShopSendLog(dbmode, inuser,  ship_id,  result_code, message);
						
					}
				}
			}			
			
			result = null;
			message = null;
			server = null;
			root = null;
			ship_id = null;  			//출고번호
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}

		return result;
	}


	/**
	 * 샵링커 API RECV DATA 
	 * 
	 * @param dbmode
	 * @param inuser
	 * @param call_api
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public String getShopOrderRecvData(String dbmode, String inuser, String call_api) throws SQLException, Exception{
		
		String result = "";

		String error_code = "";
		String message = "";
		String id = "";
		
		StringBuffer server =  null;
		Document document = null;
		Element root = null;
		
		String call_dt = "";
		String call_seq = "";
		String shop_dbmode = "shoplinker";
		
		try {

			server = new StringBuffer();
			
			if(call_api.equals("ShopRecvOrder")){
				server.append("http://apiweb.shoplinker.co.kr/ShoplinkerApi/Order/orderlist.php?iteminfo_url=http://api.isehq.com:8080/shop_order_"+dbmode+".jsp"); // xmlURL
				//server.append("http://api.isehq.com:8080/shop_order_data.jsp"); // xmlURL	
				document = getShopDocument(server.toString()); 
			}else if(call_api.equals("ShopRecvClame")){
				server.append("http://apiweb.shoplinker.co.kr/ShoplinkerApi/Clame/Clame_Xml.php?iteminfo_url=http://api.isehq.com:8080/shop_clame_"+dbmode+".jsp"); // xmlURL
				document = getDocument(server.toString()); 
				//server.append("http://api.isehq.com:8080/shop_clame_data.jsp"); // xmlURL	
			}
						
			Logger.debug("[server]"+server.toString());
			
			NodeList noneList = document.getDocumentElement().getChildNodes(); 
			root = document.getDocumentElement();
			
			try{
				error_code = root.getElementsByTagName("result").item(0).getFirstChild().getNodeValue();
			}catch(Exception e){
				error_code = "";
			}

			Logger.debug("[error_code]"+error_code);
			
			if(error_code.equals("") || error_code == null){
				// 정상 수신 및 정상 주문 데이터가 있는 건은 error_code 가 넘어오지 않음
				// 그것으로 구분하여 주문 데이터를 생성한다.
				ArrayList<HashMap<String, Serializable>> list = new ArrayList<HashMap<String, Serializable>>();
				for (int i = 1; i < noneList.getLength(); i++) { 
					Node row = noneList.item(i); 
					NodeList oneDepthList = row.getChildNodes(); 
					
					if(row.getNodeType() == Node.ELEMENT_NODE){
						HashMap<String, Serializable> map = new HashMap<String, Serializable>();
						
						for (int a = 0; a < oneDepthList.getLength(); a++) {
							 Node oneNode = oneDepthList.item(a);
					         NodeList oNodeList = oneNode.getChildNodes();
					         
					         if(oneNode.getNodeType() == Node.ELEMENT_NODE){
					        	 map.put(oneNode.getNodeName(), oneNode.getTextContent());
					         }
						}
						
						list.add(map);
					}
				}
								
				ServiceShoplinkerInfo sInfo = new ServiceShoplinkerInfo();
				int seq = 1;
				
				//db 가 정해져 있음
				call_dt = CommonUtil.getCurrentDate();
				call_seq = getShopRecvCallSeq(shop_dbmode, call_dt);  //로그 차수
				Logger.debug("[call_seq]"+call_seq);
				Logger.debug("[list]"+list.size());
				if(list != null && list.size() != 0){
					
					Map ordMap = null;
					ArrayList<Map> pList = new ArrayList<Map>();
					
					for(int b=0; b<list.size();b++){
						ordMap = list.get(b);
						sInfo.setShoplinker_order_id(StringUtil.nullTo((String)ordMap.get("shoplinker_order_id"),"")); 
						sInfo.setMall_order_id(StringUtil.nullTo((String)ordMap.get("mall_order_id"),""));  
						sInfo.setMall_name(StringUtil.nullTo((String)ordMap.get("mall_name"),"")); 
						sInfo.setOrder_product_id(StringUtil.nullTo((String)ordMap.get("order_product_id"),""));
						sInfo.setShoplinker_product_id(StringUtil.nullTo((String)ordMap.get("shoplinker_product_id"),""));
						sInfo.setProduct_name(StringUtil.nullTo((String)ordMap.get("product_name"),""));
						sInfo.setQuantity(StringUtil.nullTo((String)ordMap.get("quantity"),"")); 
						sInfo.setOrder_price(StringUtil.nullTo((String)ordMap.get("order_price"),""));
						sInfo.setSku(StringUtil.nullTo((String)ordMap.get("sku"),""));
						
						
						if(call_api.equals("ShopRecvOrder")){
							sInfo.setBaesong_status(StringUtil.nullTo((String)ordMap.get("baesong_status"),"")); 
							sInfo.setOrder_name(StringUtil.nullTo((String)ordMap.get("order_name"),"")); 
							sInfo.setOrder_tel(StringUtil.nullTo((String)ordMap.get("order_tel"),"")); 
							sInfo.setOrder_cel(StringUtil.nullTo((String)ordMap.get("order_cel"),"")); 
							sInfo.setOrder_email(StringUtil.nullTo((String)ordMap.get("order_email"),"")); 
							sInfo.setReceive(StringUtil.nullTo((String)ordMap.get("receive"),"")); 
							sInfo.setReceive_tel(StringUtil.nullTo((String)ordMap.get("receive_tel"),"")); 
							sInfo.setReceive_cel(StringUtil.nullTo((String)ordMap.get("receive_cel"),"")); 
							sInfo.setReceive_zipcode(StringUtil.nullTo((String)ordMap.get("receive_zipcode"),"")); 
							sInfo.setReceive_addr(StringUtil.nullTo((String)ordMap.get("receive_addr"),"")); 
							sInfo.setBaesong_type(StringUtil.nullTo((String)ordMap.get("baesong_type"),"")); 
							sInfo.setBaesong_bi(StringUtil.nullTo((String)ordMap.get("baesong_bi"),"")); 
							sInfo.setDelivery_msg(StringUtil.nullTo((String)ordMap.get("delivery_msg"),"")); 
							sInfo.setPartner_product_id(StringUtil.nullTo((String)ordMap.get("partner_product_id"),"")); 
							sInfo.setSale_price(StringUtil.nullTo((String)ordMap.get("sale_price"),"")); 
							sInfo.setSupply_price(StringUtil.nullTo((String)ordMap.get("supply_price"),"")); 
							sInfo.setOrderdate(StringUtil.substring(StringUtil.nullTo((String)ordMap.get("orderdate"),""),0,8)); 
						}else if (call_api.equals("ShopRecvClame")){
							sInfo.setClame_status(StringUtil.nullTo((String)ordMap.get("clame_status"),"")); 
							sInfo.setClame_memo(StringUtil.nullTo((String)ordMap.get("clame_memo"),"")); 
							sInfo.setClame_date(StringUtil.substring(StringUtil.nullTo((String)ordMap.get("clame_date"),""),0,8));
						}
						
						sInfo.setOrder_reg_date(call_dt); 
						sInfo.setRecv_gb(getRecvGb(call_api)); 
						sInfo.setCall_seq(call_seq); 
						sInfo.setSeq(String.valueOf(seq)); 
						sInfo.setCocd(StringUtil.nullTo((String)ordMap.get("partner_info"),""));  //사업부 추가시 사용할 계획 
						sInfo.setInuser(inuser); 
						
						
						setShopRecvData(shop_dbmode, sInfo);
						seq++;
					}
				}
				
				callProcedure(dbmode, call_api, call_dt, call_seq, "", "", "", "", "", "","");
				
			}else{
				// 비정상 수신 및 결과 조회 데이터가 없는 경우 error_code 넘어옴
				// error log 작성을 위한 로직
				message = root.getElementsByTagName("product_id").item(0).getFirstChild().getNodeValue();
				if(message.equals("")){
					message = root.getElementsByTagName("message").item(0).getFirstChild().getNodeValue();
				}
				Logger.debug("[error_code]"+error_code+"[message]"+message);
			}

			
			
			 result = null;
			 error_code = null;
			 message = null;
			 server = null;
			 document = null;
			 root = null;
			 
		} catch (Exception e) {
			//setErrorRecvLog(dbmode, CommonUtil.getCurrentDate(), call_seq, e.toString());
		} finally {
			
		}

		return result;
	}
	

	private static int setShopRecvData(String dbmode,
			ServiceShoplinkerInfo sInfo) throws Exception {
		Connection conn = null;
		ServiceDAO dao = null;
		int result = 0;
		
		try { 
			Logger.debug("[dbmode]"+dbmode);
			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();

			result = dao.setShopRecvData(conn, sInfo);  //로그 차수
			
			if(result > 0){
				conn.commit();
			}else{
				conn.rollback();
			}
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}

		return result;
		
	}

	private String getShopRecvCallSeq(String dbmode, String call_dt) throws Exception {
		Connection conn = null;
		ServiceDAO dao = null;
		String call_seq = "";
		String result = "";
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();
			
			call_seq = dao.getShopRecvCallSeq(conn, call_dt);  //로그 차수
			
		
			if(!call_seq.equals("")){
				result = call_seq;
				conn.commit();
			}else{
				result = "";
				conn.rollback();
			}
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}

		return result;
	}

	// WIZWID API [SEND] 공통모듈
	@SuppressWarnings("unchecked")
	public String getOrderSendData(String dbmode, String inuser, String call_api, String connip, String transcd) throws SQLException, Exception {
		Connection conn = null;
		ServiceDAO dao = null;

		String result = "";
		String error_code = "";
		String message = "";
 
		StringBuffer server = null;
		Element root = null;

		List<Object> getOrderSendData = null;
		Map<String, String> vMap = null;
		
		String vendor_id = ""; 			//거래처코드
		String ship_id = "";  			//출고번호
		String apply_dt = "";  			//출고일
		String apply_time = ""; 		//출고시간
		String deli_company_id = ""; 	//배송사코드
		String bl_no = ""; 				//운송장번호
		String cancel_code = "";		//주문취소사유
		
		String call_dt = "";
		String call_seq = "";
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();

			getOrderSendData = dao.getOrderSendData(conn, call_api, call_dt, call_seq, transcd);
			
			if (getOrderSendData.size() > 0) {
				
				for (int v = 0; v < getOrderSendData.size(); v++) {
					vMap = (HashMap<String, String>) getOrderSendData.get(v);
					server = new StringBuffer();
					
					if (transcd.equals("10")) {			//WIZWID URL
						server.append("http://"+connip+"/API/handler/wizwid/kr/APIService-"+call_api);
					} else if (transcd.equals("20") || transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
						server.append(connip+"/Cube/"+call_api+".asp");	//w컨셉 url
					}
					
					if (call_api.equals("OrderConfirm")) {		//발주확인
						vendor_id = vMap.get("VENDOR_ID");
						ship_id = vMap.get("TEMPNO");
						
						server.append("?VENDOR_ID="+ vendor_id +"&SHIP_ID="+ ship_id);
						
					} else if(call_api.equals("DeliveryInsert")) {	//배송정보등록
						vendor_id = vMap.get("VENDOR_ID");
						ship_id = vMap.get("TEMPNO");
						apply_dt = vMap.get("OUTDT");
						apply_time = vMap.get("UPDTIME");
						bl_no= vMap.get("EXPNM");
						deli_company_id = vMap.get("EXPNO");
						
						server.append("?VENDOR_ID="+ vendor_id +"&SHIP_ID="+ ship_id +"&APPLY_DT="+ apply_dt +"&APPLY_TIME="+ apply_time +"&DELI_COMPANY_ID="+ deli_company_id +"&BL_NO="+ bl_no);
						
					} else if(call_api.equals("SoldOutCancel")) {	//제휴사출고지시취소처리
						vendor_id = vMap.get("VENDOR_ID");
						ship_id = vMap.get("TEMPNO");
						cancel_code = vMap.get("CANCEL_CODE");
						
						server.append("?VENDOR_ID="+ vendor_id +"&SHIP_ID="+ ship_id +"&CANCEL_CODE="+ cancel_code);
						
					} else if(call_api.equals("OrderReturnConfirm")) {	//반품정보확인
						vendor_id = vMap.get("VENDOR_ID");
						ship_id = vMap.get("TEMPNO");
						
						server.append("?VENDOR_ID="+ vendor_id +"&SHIP_ID="+ ship_id);
						
					} else if(call_api.equals("ReturnPickUpInsert")) {	//반품수거등록
						vendor_id = vMap.get("VENDOR_ID");
						ship_id = vMap.get("TEMPNO");
						apply_dt = vMap.get("APPLY_DT");
						apply_time = vMap.get("APPLY_TIME");
						bl_no= vMap.get("EXPNM");
						deli_company_id = vMap.get("EXPNO");
						
						server.append("?VENDOR_ID="+ vendor_id +"&SHIP_ID="+ ship_id +"&APPLY_DT="+ apply_dt +"&APPLY_TIME="+ apply_time +"&DELI_COMPANY_ID="+ deli_company_id +"&BL_NO="+ bl_no);
						
					} else if(call_api.equals("ReturnRefuse")) {	//반품취소처리
						vendor_id = vMap.get("VENDOR_ID");
						ship_id = vMap.get("TEMPNO");
						
						server.append("?VENDOR_ID="+ vendor_id +"&SHIP_ID="+ ship_id);
					}
					
					if (transcd.equals("10")) {			//WIZWID URL
						Logger.debug("[server]"+"http://"+connip+"/API/handler/wizwid/kr/APIService-"+call_api+server.toString());
					} else if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
						Logger.debug("[server]"+connip+"/Cube/"+call_api+".asp"+server.toString());	//w컨셉 url
					}
					
					Document document = getDocument(server.toString());
					@SuppressWarnings("unused")
					NodeList noneList = document.getDocumentElement().getChildNodes();
					root = document.getDocumentElement();

					error_code = root.getElementsByTagName("CODE").item(0).getFirstChild().getNodeValue();
					message = root.getElementsByTagName("MESSAGE").item(0).getFirstChild().getNodeValue();
					
					setSendLog(dbmode, inuser, call_api, getApiName(call_api), vendor_id, ship_id, apply_dt, apply_time, deli_company_id, bl_no, error_code, message, cancel_code, transcd);
					
				}
			} else {
				setSendLog(dbmode, inuser, call_api, getApiName(call_api), "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "100", "연동할 대상 정보가 없습니다.","00", transcd);
			}
			
			result = null;
			error_code = null;
			message = null;

			server = null;
			root = null;

			getOrderSendData = null;
			vMap = null;

			vendor_id = null; 			//거래처코드
			ship_id = null;  			//출고번호
			apply_dt = null;  			//출고일
			apply_time = null; 			//출고시간
			deli_company_id = null; 	//배송사코드
			bl_no = null; 				//운송장번호
			cancel_code = null;			//주문취소사유
			
		} finally {
			DataBaseManager.close(conn, dbmode);
			if(conn != null) conn.close();
		}

		return result;
	}
	
	
	
	
	
	
	
	// WIZWID API [RECV]공통 모듈
	@SuppressWarnings("unchecked")
	public  String getOrderRecvData(String dbmode, String inuser, String call_api, String connip, String transcd) throws SQLException, Exception {
		Connection conn = null;
		ServiceDAO dao = null;
		String result = "";

		String error_code = "";
		String message = "";

		String server = "";
		Document document = null;
		Element root = null;
		
		int log_result = 0;
		
		List<Object> vendorList = null;
		Map<String, String> vMap = null;
		String vendor_id = "";
		String sta_dt = "";
		String end_dt = "";
		String call_seq = "";
			
		try {

			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();
			
			
			vendorList = dao.getVendorList(conn,transcd);
			
			if(vendorList != null){
				for(int v=0; v<vendorList.size();v++){
					vMap = (HashMap)vendorList.get(v);
					
					vendor_id = (String)vMap.get("VENDOR_ID");
					sta_dt = (String)vMap.get("STA_DT");
					end_dt = (String)vMap.get("END_DT");
					
					if (transcd.equals("10")) {			//WIZWID URL
						server = "http://"+connip+"/API/handler/wizwid/kr/APIService-"+call_api+"?VENDOR_ID="+vendor_id+"&STA_DT="+sta_dt+"&END_DT="+end_dt; // xmlURL
					} else if (transcd.equals("20") || transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
						server = connip+"/Cube/"+call_api+".asp?VENDOR_ID="+vendor_id+"&STA_DT="+sta_dt+"&END_DT="+end_dt;	//w컨셉 url
					}
					Logger.debug("vendor_id: "+vendor_id);
					document = getDocument(server); 
					if(document == null) {
						continue;
					}
					Logger.debug("[server]"+server.toString());
					
					NodeList noneList = document.getDocumentElement().getChildNodes(); 
					root = document.getDocumentElement();
					
					error_code = root.getElementsByTagName("CODE").item(0).getFirstChild().getNodeValue();
					message =  root.getElementsByTagName("MESSAGE").item(0).getFirstChild().getNodeValue();

					ArrayList<HashMap<String, Serializable>> list = new ArrayList<HashMap<String, Serializable>>();
					
					for (int i = 1; i < noneList.getLength(); i++) { 
						Node row = noneList.item(i); 
						NodeList oneDepthList = row.getChildNodes(); 
						
						if(row.getNodeType() == Node.ELEMENT_NODE){
							HashMap<String, Serializable> map = new HashMap<String, Serializable>();
							
							for (int a = 0; a < oneDepthList.getLength(); a++) {
								
								Node oneNode = oneDepthList.item(a);
								NodeList oNodeList = oneNode.getChildNodes();
								
							    if(oneNode.getNodeType() == Node.ELEMENT_NODE){
							    	ArrayList<HashMap<String, String>> productList = new ArrayList<HashMap<String, String>>();
									if(oneNode.getNodeName().equals("PRODUCT")){
								        for(int pn=0; pn<oNodeList.getLength();pn++){
								        	HashMap<String, String> prdMap = new HashMap<String, String>();
								            if(oNodeList.item(pn).getNodeType() == Node.ELEMENT_NODE){
								                Node prd = oNodeList.item(pn);
								                NodeList prdList = prd.getChildNodes();
								                
								                if(prd.getNodeType() == Node.ELEMENT_NODE && prd.getNodeName().equals("PRD")){
								                	
								                	for(int k=0; k<prdList.getLength();k++){
									                    if(prdList.item(k).getNodeType() == Node.ELEMENT_NODE){
									                        prdMap.put(prdList.item(k).getNodeName(), prdList.item(k).getTextContent());
									                    }
									                }
									                
								                }
								                productList.add(prdMap);
								                map.put(oneNode.getNodeName(), productList); 
								            } 	
								        }
								        
								        
								    }else{
								        map.put(oneNode.getNodeName(), oneNode.getTextContent());						
								    }
								}	
							}
							
							list.add(map);
						}
					}
				
					call_seq = setRecvLog(dbmode, inuser, call_api, getApiName(call_api), vendor_id, sta_dt, end_dt, error_code, message, transcd);
					
					if(call_seq.equals("")){
						log_result = 0;
					}else{
						log_result = 1;
					}
					
					
					Map ordMap = null;
					ArrayList<Map> pList = new ArrayList<Map>();
					ServiceDataInfo dInfo  = new ServiceDataInfo();
					
					int seq = 0;
					if(log_result !=0 && list.size() != 0){
						for(int j=0; j<list.size();j++){
							ordMap = list.get(j);
							
							pList  = (ArrayList<Map>)ordMap.get("PRODUCT");
							Map pMap = null; 
							
							if(pList != null){
								
								
								for(int ps = 0; ps<pList.size();ps++){
									pMap = (HashMap)pList.get(ps);
									dInfo.setCall_dt(CommonUtil.getCurrentDate());
									dInfo.setCall_seq(call_seq);
									dInfo.setVendor_id(vendor_id);
									dInfo.setInuser(inuser);
									dInfo.setError_code("00");
									dInfo.setError_msg(message);
									dInfo.setSeq(String.valueOf(seq+1));
									dInfo.setRecv_gb(getRecvGb(call_api)); // 10.주문, 20.주문취소, 30.반품, 40.반품취소
									dInfo.setFirst_order_id(StringUtil.nullTo((String)ordMap.get("FIRST_ORDER_ID"), "")); 
									dInfo.setOrder_id(StringUtil.nullTo((String)ordMap.get("ORDER_ID"), ""));
									dInfo.setShip_id(StringUtil.nullTo((String)ordMap.get("SHIP_ID"), ""));         
									dInfo.setTrans_dt(StringUtil.nullTo((String)ordMap.get("TRANS_DT"), ""));   
									dInfo.setCancel_dt(StringUtil.nullTo((String)ordMap.get("CANCEL_DT"), ""));
									dInfo.setInstruct_dt(StringUtil.nullTo((String)ordMap.get("INSTRUCT_DT"), ""));     
									dInfo.setChange_gb(StringUtil.nullTo((String)ordMap.get("CHANGE_GB"), ""));
									dInfo.setShip_status(StringUtil.nullTo((String)ordMap.get("SHIP_STATUS"), ""));     
									dInfo.setReceipt_nm(StringUtil.nullTo((String)ordMap.get("RECEIPT_NM"), ""));      
									dInfo.setReceipt_tel(StringUtil.nullTo((String)ordMap.get("RECEIPT_TEL"), ""));     
									dInfo.setReceipt_hp(StringUtil.nullTo((String)ordMap.get("RECEIPT_HP"), ""));      
									dInfo.setReceipt_zipcode(StringUtil.nullTo((String)ordMap.get("RECEIPT_ZIPCODE"), "")); 
									dInfo.setReceipt_addr1(StringUtil.nullTo((String)ordMap.get("RECEIPT_ADDR1"), ""));   
									dInfo.setReceipt_addr2(StringUtil.nullTo((String)ordMap.get("RECEIPT_ADDR2"), ""));   
									dInfo.setCust_nm(StringUtil.nullTo((String)ordMap.get("CUST_NM"), ""));         
									dInfo.setCust_tel(StringUtil.nullTo((String)ordMap.get("CUST_TEL"), ""));        
									dInfo.setCust_hp(StringUtil.nullTo((String)ordMap.get("CUST_HP"), ""));         
									dInfo.setCust_zipcode(StringUtil.nullTo((String)ordMap.get("CUST_ZIPCODE"), ""));    
									dInfo.setCust_addr1(StringUtil.nullTo((String)ordMap.get("CUST_ADDR1"), ""));      
									dInfo.setCust_addr2(StringUtil.nullTo((String)ordMap.get("CUST_ADDR2"), ""));      
									dInfo.setDelivery_msg(StringUtil.nullTo((String)ordMap.get("DELIVERY_MSG"), ""));    
									dInfo.setOrder_seq(StringUtil.nullTo((String)pMap.get("ORDER_SEQ"), ""));       
									dInfo.setShip_seq(StringUtil.nullTo((String)pMap.get("SHIP_SEQ"), ""));        
									dInfo.setItem_cd(StringUtil.nullTo((String)pMap.get("ITEM_CD"), ""));         
									dInfo.setItem_nm(StringUtil.nullTo((String)pMap.get("ITEM_NM"), ""));         
									dInfo.setOption1(StringUtil.nullTo((String)pMap.get("OPTION1"), " "));         
									dInfo.setOption2(StringUtil.nullTo((String)pMap.get("OPTION2"), " "));         
									dInfo.setDeli_gb(StringUtil.nullTo((String)pMap.get("DELI_GB"), ""));
									dInfo.setRet_code(StringUtil.nullTo((String)pMap.get("RET_CODE"), ""));
									dInfo.setDeli_price(StringUtil.nullTo((String)pMap.get("DELI_PRICE"), "0"));
									dInfo.setSale_price(StringUtil.nullTo((String)pMap.get("SALE_PRICE"), "0"));
									dInfo.setOri_ship_id(StringUtil.nullTo((String)pMap.get("ORI_SHIP_ID"), ""));
									dInfo.setQty(StringUtil.nullTo((String)pMap.get("QTY"), "0"));
									dInfo.setCust_email("");
									dInfo.setClame_memo("");
									dInfo.setCube_item("");
									dInfo.setCocd("");
									dInfo.setWhcd("");
									dInfo.setOrderKey("");
									dInfo.setOrderSeqKey("");									
									dInfo.setShipKey("");
									dInfo.setVendorNm("");
									dao.setRecvData(conn, dInfo, transcd);
									seq++;
								}
							}
							
						}
						
						conn.commit();
						
						callProcedure(dbmode, call_api, CommonUtil.getCurrentDate(), call_seq, connip, "", "", transcd, "", "","");
						//DataBaseManager.prepareCall(conn, "{call P_RECV_INORDER('"+getCurrentDate()+"','"+call_seq+"')}");
						
					}else{
						conn.rollback();
					} 
					
					
				}
				
			}
			
			 dao = null;
			 result = null;
			 error_code = null;
			 message = null;
			 server = null;
			 document = null;
			 root = null;
			 log_result = 0;
			 vendorList = null;
			 vMap = null;
			  
		} catch (Exception e) {
			/**
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
			*/
			setErrorRecvLog(dbmode, CommonUtil.getCurrentDate(), call_seq, e.toString());
		} finally {
			DataBaseManager.close(conn, dbmode);
			if(conn != null) conn.close();
		}

		return result;
	}
	
	
	/**
	 * 에러 난 경우 에러를 DB에 INSERT
	 * @param dbmode
	 * @param call_dt
	 * @param call_seq
	 * @param error_msg
	 * @throws Exception
	 */
	private  void setErrorRecvLog(String dbmode, String call_dt, String call_seq, String error_msg)  throws Exception {
		Connection conn = null;
		ServiceDAO dao = null;
		int result = 0;
		try {

			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();

			
			ServiceLogInfo rlog = new ServiceLogInfo();
			
			rlog.setCall_dt(call_dt);
			rlog.setCall_seq(call_seq);
			
			
			int log_result = dao.setErrorRecvLog(conn, rlog, error_msg);
			
			if(log_result == 1){ 
				conn.commit();
			}else{
				conn.rollback();
			}
			
			conn.commit();
			
		} catch (Exception e) {
			
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e; 
			
		} finally {
			DataBaseManager.close(conn, dbmode);
			if(conn != null) conn.close();
		}
	}

	
	/**
	 * WIZWID API 최종 처리 후 프로시저 호출
	 * @param dbmode
	 * @param call_api
	 * @param call_dt
	 * @param call_seq
	 * @throws Exception
	 */
	//비교 : cubeDao.callProcedure(  dbmode,        command,CommonUtil.getCurrentDate(),call_seq,   rtOrderKey,      tranDt,        rtOrder_id,     transCD,    rtQrder_dt ,     org_code,         sell_code);
	public void callProcedure(String dbmode, String call_api, String call_dt, String call_seq, String connip, String tranDt, String tranSeq, String transcd, String cmID, String cmPassKey ,String scProcKey) throws Exception 
	{
		Connection conn = null;

		Logger.debug(" callProcedure() CALL ");
		try {
			CubeApiCreateJSON jsonDao  = CubeApiCreateJSON.getInstance();
			ScApiCreateREDIS  redisDao = ScApiCreateREDIS.getInstance();
			
			Logger.debug("call_api["+call_api+"]");	
			
			if(call_api.equals("OrderRetrieve")){
				conn = DataBaseManager.getConnection(dbmode);
				//발주정보조회
				
				Logger.debug("주문 연동 시작");	
				DataBaseManager.prepareCall(conn, "{call P_RECV_INORDER('"+call_dt+"','"+call_seq+"','"+transcd+"')}");
				Logger.debug("주문 연동 끝");
				
				if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
					//발주조회 등록 후 주문완료 정보 전송
					sendOrderAfterCheck(dbmode, "OrderRetrieveCheck", call_dt, call_seq, connip, transcd, cmID, cmPassKey);
				}
				
			} else if(call_api.equals("OrderCancelRetrieve")) {
				conn = DataBaseManager.getConnection(dbmode);
				//발주취소조회
				DataBaseManager.prepareCall(conn, "{call P_RECV_INORDER_CANCEL('"+call_dt+"','"+call_seq+"','"+transcd+"')}");
				
			} else if(call_api.equals("OrderReturnRetrieve")) {
				conn = DataBaseManager.getConnection(dbmode);
				//반품정보조회
				DataBaseManager.prepareCall(conn, "{call P_RECV_REFUND('"+call_dt+"','"+call_seq+"','"+transcd+"')}");
				
				if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
					//반품정보조회 후 반품 정보 전송
					sendOrderAfterCheck(dbmode, "OrderReturnRetrieveCheck", call_dt, call_seq, connip, transcd, cmID, cmPassKey);
				}
				
			} else if(call_api.equals("OrderReturnCancelRetrieve")) {
				conn = DataBaseManager.getConnection(dbmode);
				//반품취소조회
				DataBaseManager.prepareCall(conn, "{call P_RECV_REFUND_CANCEL('"+call_dt+"','"+call_seq+"','"+transcd+"')}");
				
			} else if(call_api.equals("ShopRecvOrder") || call_api.equals("ShopRecvClame")) {
				conn = DataBaseManager.getConnection("shoplinker");
				DataBaseManager.prepareCall(conn, "{call P_RECV_SPLIT('"+call_dt+"','"+call_seq+"','"+dbmode+"','"+call_api+"')}");
			}
			
			
			if (transcd.equals("30")) {	//망고KR 인 경우..
				if (call_api.equals("OrderRetrieve") || call_api.equals("OrderCancelRetrieve") || call_api.equals("OrderReturnRetrieve") || call_api.equals("OrderReturnCancelRetrieve")) {
					//발주조회, 발주취소조회, 반품정보조회, 반품취소조회 등록 후 정보 전송
					jsonDao.api_Auto_PO_Send(dbmode, call_api, call_dt, call_seq, tranDt, tranSeq, transcd, connip);
				}
			}
			Logger.debug("transcd["+transcd+"]");
			if (transcd.equals("40")) {	//SC 인 경우..
				if (call_api.equals("OrderRetrieve") || call_api.equals("OrderCancelRetrieve") || call_api.equals("OrderReturnRetrieve") || call_api.equals("OrderReturnCancelRetrieve")) {
					Logger.debug("처리결과 전송 CALL");
					//발주조회, 발주취소조회
					
					/* 기존 컬럼활용으로 실제항목의미랑 다르게 사용
					  dbmode   : db
					  call_api : 처리 프로세스
					  call_dt  : 처리날짜
					  call_seq : 처리순번
					  tranDt   : 전송일자 
					  transcd  : 연동대상코드(sc : 40 )
					  tranSeq  : orderId(주문번호)
					  cmID	   : orderDt(주문날짜)
					  connip   : orderHeaderKey (주문번호 키)
					  cmPassKey : orgCode(사업부코드)
					  scProcKey : sell_code(판매채널코드)
					*/
					
					redisDao.api_Auto_PO_Send(dbmode, call_api, call_dt, call_seq ,tranDt, transcd ,tranSeq ,cmID ,connip, cmPassKey ,scProcKey);
				}
			}		
			conn.commit();
			
		} catch (Exception e) {
			/**
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
			*/
			setErrorRecvLog(dbmode, CommonUtil.getCurrentDate(), call_seq, e.toString());
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}
	}
	
	/**
	 * WIZWID API 최종 처리 후 프로시저 호출2 - (ServiceDataInfo dInfo 추가 2015.03.06 하윤식)
	 * @param dbmode
	 * @param call_api
	 * @param call_dt
	 * @param call_seq
	 * @throws Exception
	 */
	public void callProcedure2(String dbmode, String call_api, String call_dt, String call_seq, String connip, String tranDt, String tranSeq, String transcd, String cmID, String cmPassKey ,String scProcKey, ServiceDataInfo	dInfo) throws Exception 
	{
		Connection conn = null;

		Logger.debug(" callProcedure() CALL ");
		try {
			CubeApiCreateJSON jsonDao  = CubeApiCreateJSON.getInstance();
			ScApiCreateREDIS  redisDao = ScApiCreateREDIS.getInstance();
			
			Logger.debug("call_api["+call_api+"]");	
			
			if(call_api.equals("OrderRetrieve")){
				conn = DataBaseManager.getConnection(dbmode);
				//발주정보조회
				
				Logger.debug("주문 연동 시작");	
				DataBaseManager.prepareCall(conn, "{call P_RECV_INORDER('"+call_dt+"','"+call_seq+"','"+transcd+"')}");
				Logger.debug("주문 연동 끝");
				
				if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
					//발주조회 등록 후 주문완료 정보 전송
					sendOrderAfterCheck(dbmode, "OrderRetrieveCheck", call_dt, call_seq, connip, transcd, cmID, cmPassKey);
				}
				
			} else if(call_api.equals("OrderCancelRetrieve")) {
				conn = DataBaseManager.getConnection(dbmode);
				//발주취소조회
				DataBaseManager.prepareCall(conn, "{call P_RECV_INORDER_CANCEL('"+call_dt+"','"+call_seq+"','"+transcd+"')}");
				
			} else if(call_api.equals("OrderReturnRetrieve")) {
				conn = DataBaseManager.getConnection(dbmode);
				//반품정보조회
				DataBaseManager.prepareCall(conn, "{call P_RECV_REFUND('"+call_dt+"','"+call_seq+"','"+transcd+"')}");
				
				if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
					//반품정보조회 후 반품 정보 전송
					sendOrderAfterCheck(dbmode, "OrderReturnRetrieveCheck", call_dt, call_seq, connip, transcd, cmID, cmPassKey);
				}
				
			} else if(call_api.equals("OrderReturnCancelRetrieve")) {
				conn = DataBaseManager.getConnection(dbmode);
				//반품취소조회
				DataBaseManager.prepareCall(conn, "{call P_RECV_REFUND_CANCEL('"+call_dt+"','"+call_seq+"','"+transcd+"')}");
				
			} else if(call_api.equals("ShopRecvOrder") || call_api.equals("ShopRecvClame")) {
				conn = DataBaseManager.getConnection("shoplinker");
				DataBaseManager.prepareCall(conn, "{call P_RECV_SPLIT('"+call_dt+"','"+call_seq+"','"+dbmode+"','"+call_api+"')}");
			}
			
			
			if (transcd.equals("30")) {	//망고KR 인 경우..
				if (call_api.equals("OrderRetrieve") || call_api.equals("OrderCancelRetrieve") || call_api.equals("OrderReturnRetrieve") || call_api.equals("OrderReturnCancelRetrieve")) {
					//발주조회, 발주취소조회, 반품정보조회, 반품취소조회 등록 후 정보 전송
					jsonDao.api_Auto_PO_Send(dbmode, call_api, call_dt, call_seq, tranDt, tranSeq, transcd, connip);
				}
			}
			Logger.debug("transcd["+transcd+"]");
			if (transcd.equals("40")) {	//SC 인 경우..
				if (call_api.equals("OrderRetrieve") || call_api.equals("OrderCancelRetrieve") || call_api.equals("OrderReturnRetrieve") || call_api.equals("OrderReturnCancelRetrieve")) {
					Logger.debug("처리결과 전송 CALL");
					//발주조회, 발주취소조회
					
					/* 기존 컬럼활용으로 실제항목의미랑 다르게 사용
					  dbmode   : db
					  call_api : 처리 프로세스
					  call_dt  : 처리날짜
					  call_seq : 처리순번
					  tranDt   : 전송일자 
					  transcd  : 연동대상코드(sc : 40 )
					  tranSeq  : orderId(주문번호)
					  cmID	   : orderDt(주문날짜)
					  connip   : orderHeaderKey (주문번호 키)
					  cmPassKey : orgCode(사업부코드)
					  scProcKey : sell_code(판매채널코드)
					*/
					
					redisDao.api_Auto_PO_Send2(dbmode, call_api, call_dt, call_seq ,tranDt, transcd ,tranSeq ,cmID ,connip, cmPassKey ,scProcKey, dInfo);
				}
			}		
			conn.commit();
			
		} catch (Exception e) {
			/**
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
			*/
			setErrorRecvLog(dbmode, CommonUtil.getCurrentDate(), call_seq, e.toString());
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}
	}
	

	/**
	 * xml 파싱 
	 * @param serverUrl
	 * @return
	 */
	public static  Document getDocument(String serverUrl){
		HttpClient httpClient = null;
		String readLine = null;
		BufferedReader br = null;
		StringBuffer sb = null;
		Document document = null;
		
		try {
			GetMethod get = new GetMethod(serverUrl);
			
			httpClient = new HttpClient();
			httpClient.executeMethod(get);
			
			
			br = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
			sb = new StringBuffer();
			org.xml.sax.InputSource is = null;
			
			while ((readLine = br.readLine()) != null) {
				if (readLine.trim().length() > 0) {
					sb.append(readLine + "\n");
					is = new org.xml.sax.InputSource(new StringReader(sb.toString()));
				}
			}
			
			if(is!=null) {
				is.setEncoding("euc-kr");
				br.close();
				DocumentBuilder _docBuilder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();			
				document = _docBuilder.parse(is);
			} else {
				return null;
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		
		return document;
	} 
	
	
	/**
	 * xml 파싱 
	 * @param serverUrl
	 * @return
	 */
	public static  Document getShopDocument(String serverUrl){
		HttpClient httpClient = null;
		String readLine = null;
		BufferedReader br = null;
		StringBuffer sb = null;
		Document document = null;
		
		try {
			GetMethod get = new GetMethod(serverUrl);
			
			httpClient = new HttpClient();
			httpClient.executeMethod(get);
			
			
			br = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
			sb = new StringBuffer();
			org.xml.sax.InputSource is = null;
			int i = 0;
			
			while ((readLine = br.readLine()) != null) {
				
				if (readLine.trim().length() > 0) {
					if(i == 0){
						sb.append("<?xml version='1.0' encoding='euc-kr' ?>"+"\n");
						sb.append("<Shoplinker>"+"\n");
						sb.append("<order>"+"\n");
					}else{
						sb.append(readLine + "\n");
					}
					//Logger.debug("[xml]"+sb.toString());
					
					is = new org.xml.sax.InputSource(new StringReader(sb.toString()));
				}
				
				i++;
			}
			
			is.setEncoding("euc-kr");
			br.close();
			
			DocumentBuilder _docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			document = _docBuilder.parse(is); 
			
		} catch (Exception e) {
			Logger.error(e);
		}
		
		return document;
	}
	
	
	/**
	 * [RECV]수신시 에러정보
	 * @param call_api
	 * @param vendor_id
	 * @param sta_dt
	 * @param end_dt
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public String setRecvLog(String dbmode, String inuser, String call_api,String call_api_nm, String vendor_id,
		String sta_dt, String end_dt, String error_code, String message, String transcd) throws SQLException, Exception {
		Connection conn = null;
		ServiceDAO dao = null;
		String call_seq = "";
		String result = "";
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();
			//날짜 파라미터로 추가 :by lee
			call_seq = dao.getRecvCallSeq(conn,transcd ,StringUtil.substring(sta_dt, 0, 8) );  //로그 차수
			
			ServiceLogInfo rlog = new ServiceLogInfo();
			
			rlog.setCall_dt(CommonUtil.getCurrentDate());
			rlog.setVendor_id(vendor_id);
			rlog.setCall_seq(call_seq);
			rlog.setCall_api(call_api);
			rlog.setCall_api_name(call_api_nm);
			rlog.setStart_dt(StringUtil.substring(sta_dt, 0, 8));
			rlog.setEnd_dt(StringUtil.substring(end_dt, 0, 8));
			rlog.setInuser(inuser);
			rlog.setResult_code(error_code);
			rlog.setResult_name(message);
			
			int log_result = dao.setRecvLog(conn, rlog, transcd);
			
			if(log_result == 1){
				result = call_seq;
				conn.commit();
			}else{
				result = "";
				conn.rollback();
			}
			
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}

		return result;
	}
	
	

	/**
	 * [SEND] 샵링커 송신시 결과정보
	 * @param call_api
	 * @param vendor_id
	 * @param sta_dt
	 * @param end_dt
	 * @return
	 * @throws SQLException
	 * @throws Exception
	
	
	public  String setShopSendLog(String dbmode, String inuser, String ship_id,  String result_code, String message) throws SQLException, Exception {
		Connection conn = null;
		ServiceDAO dao = null;
		
		String call_seq = "";
		String result = "";
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();

			
			call_seq = dao.getShopSendCallSeq(conn);  //로그 차수
			
			ServiceLogInfo slog = new ServiceLogInfo();
			
			
			slog.setCall_dt(CommonUtil.getCurrentDate());
			
			slog.setCall_seq(call_seq);
			slog.setShip_id(ship_id);
			slog.setInuser(inuser);
			slog.setResult_code(result_code);
			slog.setResult_name(message);
			
			
			int log_result = dao.setShopSendLog(conn, slog);
			
			if(log_result == 1){
				result = call_seq;
				conn.commit();
			}else{
				result = "";
				conn.rollback();
			}
			
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}

		return result;
	}
	*/
	
	/**
	 * [SEND]송신시 에러정보
	 * @param call_api
	 * @param vendor_id
	 * @param sta_dt
	 * @param end_dt
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */ 
	public static  String setSendLog(String dbmode, String inuser, String call_api,String call_api_nm, String vendor_id, String ship_id, String apply_dt, String apply_time, String deli_company_id, String bl_no, String error_code, String message, String cancel_code, String transcd) throws SQLException, Exception {
		Connection conn = null;
		ServiceDAO dao = null;
		
		String result = "";
		String call_seq = "";
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();

			call_seq = dao.getSendCallSeq(conn,transcd);  //로그 차수
			
			ServiceLogInfo slog = new ServiceLogInfo();
			
			slog.setCall_dt(CommonUtil.getCurrentDate());
			slog.setVendor_id(vendor_id);
			slog.setCall_seq(call_seq);
			slog.setCall_api(call_api);
			slog.setShip_id(ship_id);
			slog.setApply_dt(apply_dt);
			slog.setApply_time(apply_time);
			slog.setDeli_company_id(deli_company_id);
			slog.setBl_no(bl_no);
			slog.setCall_api_name(call_api_nm);
			slog.setInuser(inuser);
			slog.setResult_code(error_code);
			slog.setResult_name(message);
			slog.setCancel_code(cancel_code);
			
			int log_result = dao.setSendLog(conn, slog, transcd);
			
			if(log_result == 1){
				result = call_seq;
				conn.commit();
			}else{
				result = "";
				conn.rollback();
			}
			
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}

		return result;
	}
	
	/**
	 * [SEND]송신시 에러정보 (ServiceDataInfo dInfo 추가 2015.03.06 하윤식)
	 * @param call_api
	 * @param vendor_id
	 * @param sta_dt
	 * @param end_dt
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */ 
	public static  String setSendLog2(String dbmode, String inuser, String call_api,String call_api_nm, String vendor_id, String ship_id, String apply_dt, String apply_time, String deli_company_id, String bl_no, String error_code, String message, String cancel_code, String transcd, ServiceDataInfo dInfo) throws SQLException, Exception {
		Connection conn = null;
		ServiceDAO dao = null;
		
		String result = "";
		String call_seq = "";
		
		try {

			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();

			call_seq = dao.getSendCallSeq(conn,transcd);  //로그 차수
			
			ServiceLogInfo slog = new ServiceLogInfo();
			
			slog.setCall_dt(CommonUtil.getCurrentDate());
			slog.setVendor_id(vendor_id);
			slog.setCall_seq(call_seq);
			slog.setCall_api(call_api);
			//slog.setShip_id(ship_id);
			slog.setShip_id(dInfo.getShipKey()); //20150306 하윤식 추가
			slog.setApply_dt(apply_dt);
			slog.setApply_time(apply_time);
			slog.setDeli_company_id(deli_company_id);
			slog.setBl_no(bl_no);
			slog.setCall_api_name(call_api_nm);
			slog.setInuser(inuser);
			slog.setResult_code(error_code);
			slog.setResult_name(message);
			slog.setCancel_code(cancel_code);
			
			int log_result = dao.setSendLog(conn, slog, transcd);
			
			if(log_result == 1){
				result = call_seq;
				conn.commit();
			}else{
				result = "";
				conn.rollback();
			}
			
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException se) {
			}
			Logger.error(e);
			throw e;
		} finally {
			DataBaseManager.close(conn,dbmode);
			if(conn != null) conn.close();
		}

		return result;
	}
	
	
	public static String getApiName(String call_api){
		String result = "";
		
		if(call_api.equals("OrderRetrieve")){  		
			result = "발주조회";
		}else if(call_api.equals("OrderConfirm")){
			result = "발주확인";
		}else if(call_api.equals("DeliveryInsert")){
			result = "배송정보등록";
		}else if(call_api.equals("SoldOutCancel")){
			result = "출고지시취소처리";
		}else if(call_api.equals("OrderCancelRetrieve")){
			result = "취소정보조회";
		}else if(call_api.equals("OrderReturnRetrieve")){
			result = "반품정보조회";
		}else if(call_api.equals("OrderReturnConfirm")){
			result = "반품정보확인";
		}else if(call_api.equals("ReturnPickUpInsert")){
			result = "반품수거등록";
		}else if(call_api.equals("OrderReturnCancelRetrieve")){
			result = "반품취소정보조회";
		}else if(call_api.equals("ReturnRefuse")){
			result = "반품취소처리";
		}else if(call_api.equals("OrderRetrieveCheck")){
			result = "주문정보 확인";
		}else if(call_api.equals("OrderReturnRetrieveCheck")){
			result = "반품정보 확인";
		}else if(call_api.equals("ShopRecvOrder")){
			result = "주문수집";
		}else if(call_api.equals("ShopSendDelivery")){
			result = "송장전송";
		}else if(call_api.equals("ShopRecvClame")){
			result = "클레임수집";			
		}else if(call_api.equals("OrderRetrieveCancelCheck")){
			result = "주문취소정보확인";
		}else if(call_api.equals("OrderReturnCancelRetrieveCheck")){
			result = "반품취소정보확인";
		} else if(call_api.equals("ReturnPickUpInsert")) {
			result = "반품수거등록";
		} 
		return result;
	}
	
	

	
	/**
	 * WIZWID API RECV 구분
	 * @param call_api
	 * @return
	 */
	public String getRecvGb(String call_api){
		String result = "";
		
		if(call_api.equals("OrderRetrieve") || call_api.equals("ShopRecvOrder")){ // 주문
			result = "10";
		}else if(call_api.equals("OrderCancelRetrieve")){ // 주문취소
			result = "20";
		}else if(call_api.equals("OrderReturnRetrieve")){ // 반품
			result = "30";
		}else if(call_api.equals("OrderReturnCancelRetrieve")){ // 반품취소
			result = "40";
		}else if(call_api.equals("ShopRecvClame")){  // 주문취소, 반품
			result = "50";
		}
		return result;
	}
	
	
	/**
	public void main(String[] args) {
		try {
			//발주정보조회 테스트
			//getOrderRetrieve("OrderRetrieve", "138497", "20120222000000","20120222235959 ");
			//getShopOrderRecvData("iseccube","SYSTEM","ShopRecvOrder");
			getShopOrderRecvData("iseccube","SYSTEM","ShopRecvClame");
			//getShopOrderSendData("iseccube","SYSTEM","ShopSendDelivery");
			
			//발주정보확인 테스트
			//getOrderConfirm("OrderConfirm", "138497", "045426149");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	
	/**
	 * WCONCEPT 발주조회 등록 후 주문완료 정보 전송
	 * @param dbmode
	 * @param call_dt
	 * @param call_seq
	 * @throws Exception
	 */
	private  void sendOrderAfterCheck(String dbmode, String call_api, String call_dt, String call_seq, String connip, String transcd, String cmID, String cmPassKey)  throws Exception {
		Connection conn = null;
		ServiceDAO dao	= null;
		
		StringBuffer server = null;
		Element root 		= null;

		List<Object> getOrderSendData	= null;
		Map<String, String> vMap 		= null;
		
		String inuser 		= "";
		String result		= "";
		String error_code	= "";
		String message 		= "";
 		
		String vendor_id		= ""; 	//거래처코드
		String ship_id 			= "";  	//출고번호
		String apply_dt 		= "";  	//출고일
		String apply_time 		= ""; 	//출고시간
		String deli_company_id	= "";	//배송사코드
		String bl_no 			= ""; 	//운송장번호
		String cancel_code 		= "";	//주문취소사유
		
		if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
			inuser = "BATCH";
		} else if (transcd.equals("40")) {
			inuser = "CMAPI";
		}
		
		try {
			
			conn = DataBaseManager.getConnection(dbmode);
			dao = new ServiceDAO();

			getOrderSendData = dao.getOrderSendData(conn, call_api, call_dt, call_seq, transcd);
			
			if (getOrderSendData.size() > 0) {
				
				for (int v = 0; v < getOrderSendData.size(); v++) {
					vMap = (HashMap<String, String>) getOrderSendData.get(v);
					
					if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
					
						server = new StringBuffer();
						
						if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
							server.append(connip+"/Cube/"+call_api+".asp");	//w컨셉 url
						}
						
						if (call_api.equals("OrderRetrieveCheck")) {	//발주확인
							vendor_id = vMap.get("VENDOR_ID");
							ship_id = vMap.get("SHIP_ID");
							
							server.append("?VENDOR_ID="+ vendor_id +"&SHIP_ID="+ ship_id);
							
						} else if(call_api.equals("OrderReturnRetrieveCheck")) {	//반품정보확인
							vendor_id = vMap.get("VENDOR_ID");
							ship_id = vMap.get("SHIP_ID");
							
							server.append("?VENDOR_ID="+ vendor_id +"&SHIP_ID="+ ship_id);
						}
						
						
						if (transcd.equals("20")|| transcd.equals("50")) {	//WCONCEPT URL or Favinit URL [IOS favinit inserting 08-Mar-16]
							Logger.debug("[server]"+connip+"/Cube/"+call_api+".asp"+server.toString());	//w컨셉 url
						}
						
						Document document = getDocument(server.toString());
						@SuppressWarnings("unused")
						NodeList noneList = document.getDocumentElement().getChildNodes();
						root = document.getDocumentElement();
	
						error_code = root.getElementsByTagName("CODE").item(0).getFirstChild().getNodeValue();
						message = root.getElementsByTagName("MESSAGE").item(0).getFirstChild().getNodeValue();
						
					}
					/*	커넥트미(CM) API 연동 작업 부분 주석 처리.. 2014-03-06
					else if (transcd.equals("40")) {	//커넥트미(CM)
						BufferedReader br 		= null;
						String url 				= "";	//SEND URL
						String cmDataType 		= "json";
						String cmOrdCd 			= "";	//CM주문번호
						String cmOrdSeq			= "";	//CM주문순번
						String cmVersion 		= "v1";
						
						if (call_api.equals("OrderRetrieveCheck")) {				//주문목록 조회 확인(발주확인)
							url = connip +"/proc/order/orderConfirm.api";
						} else if (call_api.equals("OrderReturnRetrieveCheck")) {	//반품 확인 (반품정보확인) 
							url = connip +"/proc/order/returnConfirm.api";
						}
						
						PostMethod post = new PostMethod(url);
						
						vendor_id	= vMap.get("VENDOR_ID");
						cmOrdCd 	= vMap.get("SM_ORDER_ID");
						cmOrdSeq 	= vMap.get("POSEQ");
						
						ship_id 	= cmOrdCd + cmOrdSeq;
						
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
							String rtVendorId = (String) jobj.get("vendor_id");
							String rtCmOrdCd = (String) jobj.get("cm_order_code");
							String rtCmOrdSeq = (String) jobj.get("cm_order_seq");
						
							if (errorcd.equals("0000")) {
								error_code = "000";
							} else { 
								error_code = "100";
							}
							message	= errormsg;
						}
					}
					커넥트미(CM) API 연동 작업 부분 주석 처리.. 2014-03-06 */
					
					setSendLog(dbmode, inuser, call_api, getApiName(call_api), vendor_id, ship_id, apply_dt, apply_time, deli_company_id, bl_no, error_code, message, cancel_code, transcd);
					
				}
			} else {
				setSendLog(dbmode, inuser, call_api, getApiName(call_api), "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "100", "연동할 대상 정보가 없습니다.","00", transcd);
			}
			
			
			
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		} finally {
			DataBaseManager.close(conn, dbmode);
			if(conn != null) conn.close();
		}
	}
	
	
}
