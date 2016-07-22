package com.service.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.service.command.query.ExecQuery;
import com.service.command.util.StringUtil;
import com.service.entity.ServiceDataInfo;
import com.service.entity.ServiceLogInfo;
import com.service.entity.ServiceShoplinkerInfo;

import com.service.command.log.Logger;

/**
 * ServiceDAO.java
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
public class ServiceDAO extends ServiceQuery{
	
	private static ServiceDAO instance = new ServiceDAO();
	
	public static ServiceDAO getInstance()
	{
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public String getRecvCallSeq(Connection conn, String transcd, String orderDt) throws Exception {
		
		String result = "";
		String sql = "";
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		Map<Object, String> map = null; 
		
		try {
			sql = getSql("ServiceDAO.getRecvCallSeq","","",transcd);
			params = new ArrayList<Object>();	
			//params.add(orderDt);
			
			rsh = new MapHandler();
			map = (Map<Object, String>)ExecQuery.query(conn, sql, params, rsh);		
			result = map.get("CALL_SEQ"); 
			
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	public String getSendCallSeq(Connection conn, String transcd) throws Exception {
		
		String result = "";
		String sql = "";
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		Map<Object, String> map = null; 
		
		try {
			sql = getSql("ServiceDAO.getSendCallSeq","","",transcd);
			params = new ArrayList<Object>();	
			
			rsh = new MapHandler();
			map = (Map<Object, String>)ExecQuery.query(conn, sql, params, rsh);		
			result = map.get("CALL_SEQ"); 
			Logger.debug(result);
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		
		return result;
	}


	
	@SuppressWarnings("unchecked")
	public  List<Object> getOrderSendData(Connection conn, String call_api, String call_dt, String call_seq, String transcd) throws Exception {
		
		String sql = null;
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		List<Object> result = null;
		
		try {
			
			if(call_api.equals("OrderConfirm")){
				//발주확인
				sql = getSql("ServiceDAO.OrderConfirm",call_dt,call_seq,transcd);
			}else if(call_api.equals("DeliveryInsert")){
				//배송정보등록
				if(transcd.equals("40")){
					sql = getSql("ServiceDAO.sterlingDeliveryInsert",call_dt,call_seq,transcd);
				}else{
					sql = getSql("ServiceDAO.DeliveryInsert",call_dt,call_seq,transcd);					
				}
			}else if(call_api.equals("SoldOutCancel")){
				//제휴사출고지시취소처리
				sql = getSql("ServiceDAO.SoldOutCancel",call_dt,call_seq,transcd);
			}else if(call_api.equals("OrderReturnConfirm")){
				//반품정보확인
				sql = getSql("ServiceDAO.OrderReturnConfirm",call_dt,call_seq,transcd);
			}else if(call_api.equals("ReturnPickUpInsert")){
				//반품수거등록
				sql = getSql("ServiceDAO.ReturnPickUpInsert",call_dt,call_seq,transcd);
			}else if(call_api.equals("ReturnRefuse")){
				//반품취소처리
				sql = getSql("ServiceDAO.ReturnRefuse",call_dt,call_seq,transcd);
			}else if(call_api.equals("OrderRetrieveCheck")){
				//발주조회 등록 후 주문완료 정보 확인
				sql = getSql("ServiceDAO.OrderRetrieveCheck",call_dt,call_seq,transcd);
			}else if(call_api.equals("OrderReturnRetrieveCheck")){
				//반품정보조회 후 반품 정보 전송
				sql = getSql("ServiceDAO.OrderReturnRetrieveCheck",call_dt,call_seq,transcd);
			}else if(call_api.equals("DeliveryHeaderInsert")){
				//반품정보조회 후 반품 정보 전송
				sql = getSql("ServiceDAO.DeliveryHeaderInsert",call_dt,call_seq,transcd);
			}else if(call_api.equals("DeliveryDetailInsert")){
				//반품정보조회 후 반품 정보 전송
				sql = getSql("ServiceDAO.DeliveryDetailInsert",call_dt,call_seq,transcd);
			}else if(call_api.equals("ORCHeaderInsert")){
				//반품확정
				sql = getSql("ServiceDAO.ORCHeaderInsert",call_dt,call_seq,transcd);
			}else if(call_api.equals("ORCDetailInsert")){
				//반품확정
				sql = getSql("ServiceDAO.ORCDetailInsert",call_dt,call_seq,transcd);
			}
			
			rsh = new MapListHandler(); 
			params = new ArrayList<Object>();
			
			result = (List<Object>)ExecQuery.query(conn, sql, params, rsh);
			
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		
		return result;
	}
	

	
	
	@SuppressWarnings("unchecked")
	public List<Object> getVendorList(Connection conn, String transcd) throws Exception {
		
		String sql = null;
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		List<Object> result = null;
		
		
		try {
			sql = getSql("ServiceDAO.getVendorList","","",transcd);
			rsh = new MapListHandler();
			params = new ArrayList<Object>();
			
			result = (List<Object>)ExecQuery.query(conn, sql, params, rsh);
			 
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		
		
		return result;
	}


	public int setRecvLog(Connection conn, ServiceLogInfo rlog, String transcd) throws Exception {
		String sql = "";
		ArrayList<String> params = null;
		int result = 0;
		try {
			
			sql = getSql("ServiceDAO.setRecvLog","","",transcd);
			params = new ArrayList<String>(); 
			
			params.add(rlog.getCall_dt());
			params.add(rlog.getCall_seq()); 
			params.add(rlog.getCall_api());
			params.add(rlog.getCall_api_name());
			params.add(rlog.getVendor_id());
			params.add(rlog.getStart_dt());
			params.add(rlog.getEnd_dt());
			params.add(rlog.getResult_code());
			params.add(rlog.getResult_name());
			params.add(rlog.getInuser());
			
			
			result = ExecQuery.update(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}	
		
		return result;
	}

	public int setSendLog(Connection conn, ServiceLogInfo slog, String transcd) throws Exception {
		String sql = "";
		ArrayList<String> params = null;
		int result = 0;
		try {
			
			/**
			 INSERT INTO iseccube.api_send_log
			            (call_dt, call_seq, call_api, call_api_name, vendor_id, ship_id,
			             apply_dt, apply_time, deli_company_id, bl_no, result_code,
			             result_name, inuser, intime
			            )
			     VALUES (TO_CHAR (SYSDATE, 'YYYYMMDD'), '0001', 'OrderConfirm', '발주정보확인', '138497', '045426149',
			             '', '', '', '', '100',
			             ' 에러발생', 'SYSTEM', TO_CHAR (SYSDATE, 'YYYYMMDDHH24MISS')
			            )                                                                
			*/
			
			
			
			sql = getSql("ServiceDAO.setSendLog","","",transcd);
			params = new ArrayList<String>(); 
			
			
			Logger.debug("getCall_dt"+slog.getCall_dt());
			Logger.debug("getCall_seq"+slog.getCall_seq());
			Logger.debug("getCall_api"+slog.getCall_api());
			Logger.debug("getCall_api_name"+slog.getCall_api_name());
			Logger.debug("getVendor_id"+slog.getVendor_id());
			Logger.debug("getShip_id"+slog.getShip_id());
			Logger.debug("getApply_dt"+slog.getApply_dt());
			Logger.debug("getApply_time"+slog.getApply_time());
			Logger.debug("getDeli_company_id"+slog.getDeli_company_id());
			Logger.debug("getBl_no"+slog.getBl_no());
			Logger.debug("getResult_code"+slog.getResult_code());
			Logger.debug("getResult_name"+slog.getResult_name());
			Logger.debug("getInuser"+slog.getInuser());
			Logger.debug("getCancel_code"+slog.getCancel_code());
			
			params.add(slog.getCall_dt());
			params.add(slog.getCall_seq()); 
			params.add(slog.getCall_api());
			params.add(slog.getCall_api_name());
			params.add(slog.getVendor_id());
			params.add(slog.getShip_id());
			params.add(slog.getApply_dt());
			params.add(slog.getApply_time());
			params.add(slog.getDeli_company_id());
			params.add(slog.getBl_no());
			params.add(slog.getResult_code());
			params.add(slog.getResult_name());
			params.add(slog.getInuser());
			params.add(slog.getCancel_code());

			result = ExecQuery.update(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}	
		
		return result;
	}
	

	public int setRecvData(Connection conn, ServiceDataInfo dataInfo, String transcd) throws Exception {
		String sql = "";
		ArrayList<String> params = null;
		int result = 0;
		try {
			
			sql = getSql("ServiceDAO.setRecvData","","",transcd);
			params = new ArrayList<String>();
			
			params.add(dataInfo.getCall_dt());
			params.add(dataInfo.getCall_seq());
			params.add(dataInfo.getSeq());
			params.add(dataInfo.getRecv_gb());
			params.add(dataInfo.getFirst_order_id());
			params.add(dataInfo.getOrder_id());
			params.add(dataInfo.getShip_id());
			params.add(dataInfo.getTrans_dt());
			params.add(dataInfo.getVendor_id());
			params.add(dataInfo.getInstruct_dt());
			params.add(dataInfo.getCancel_dt());
			params.add(dataInfo.getChange_gb());
			params.add(dataInfo.getShip_status());
			params.add(dataInfo.getReceipt_nm());
			params.add(dataInfo.getReceipt_tel());
			params.add(dataInfo.getReceipt_hp());
			params.add(dataInfo.getReceipt_zipcode());
			params.add(dataInfo.getReceipt_addr1());
			params.add(dataInfo.getReceipt_addr2());
			params.add(dataInfo.getCust_nm());
			params.add(dataInfo.getCust_tel());
			params.add(dataInfo.getCust_hp());
			params.add(dataInfo.getCust_zipcode());
			params.add(dataInfo.getCust_addr1());
			params.add(dataInfo.getCust_addr2());
			params.add(dataInfo.getDelivery_msg());
			params.add(dataInfo.getOrder_seq());
			params.add(dataInfo.getShip_seq());
			params.add(dataInfo.getItem_cd());
			params.add(dataInfo.getItem_nm());
			params.add(dataInfo.getOption1());
			params.add(dataInfo.getOption2());
			params.add(dataInfo.getSale_price());
			params.add(dataInfo.getDeli_price());
			params.add(dataInfo.getQty());
			params.add(dataInfo.getDeli_gb());
			params.add(dataInfo.getError_code());
			params.add(dataInfo.getError_msg());
			params.add(dataInfo.getInuser());
			params.add(dataInfo.getOri_ship_id());
			params.add(dataInfo.getRet_code());
			params.add(dataInfo.getCust_email());
			params.add(dataInfo.getClame_memo());
			params.add(dataInfo.getCube_item());			
			params.add(dataInfo.getCocd());			
			params.add(dataInfo.getWhcd());
			params.add(dataInfo.getOrderKey());
			params.add(dataInfo.getOrderSeqKey());
			params.add(dataInfo.getShipKey());
			params.add(dataInfo.getVendorNm());
			//2015.08.30 by lee
			params.add(dataInfo.getNodeType());
			// inserted [IOS 26-Jan-26]
			params.add(dataInfo.getVendor_Pono());
			
			result = ExecQuery.update(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}	
		
		return result;
	}
	
	//반품 추가(하윤식 2015.02.25)
	public int setRecvReturnData(Connection conn, ServiceDataInfo dataInfo, String transcd) throws Exception {
		String sql = "";
		ArrayList<String> params = null;
		int result = 0;
		try {
			
			sql = getSql("ServiceDAO.setRecvReturnData","","",transcd);
			params = new ArrayList<String>();
			
			params.add(dataInfo.getCall_dt());
			params.add(dataInfo.getCall_seq());
			params.add(dataInfo.getSeq());
			params.add(dataInfo.getRecv_gb());
			params.add(dataInfo.getFirst_order_id());
			params.add(dataInfo.getOrder_id());
			params.add(dataInfo.getShip_id());
			params.add(dataInfo.getTrans_dt());
			params.add(dataInfo.getVendor_id());
			params.add(dataInfo.getInstruct_dt());
			params.add(dataInfo.getChange_gb());
			params.add(dataInfo.getShip_status());
			params.add(dataInfo.getReceipt_nm());
			params.add(dataInfo.getReceipt_tel());
			params.add(dataInfo.getReceipt_hp());
			params.add(dataInfo.getReceipt_zipcode());
			params.add(dataInfo.getReceipt_addr1());
			params.add(dataInfo.getReceipt_addr2());
			params.add(dataInfo.getCust_nm());
			params.add(dataInfo.getCust_tel());
			params.add(dataInfo.getCust_hp());
			params.add(dataInfo.getCust_zipcode());
			params.add(dataInfo.getCust_addr1());
			params.add(dataInfo.getCust_addr2());
			params.add(dataInfo.getDelivery_msg());
			params.add(dataInfo.getOrder_seq());
			params.add(dataInfo.getShip_seq()); 
			params.add(dataInfo.getItem_cd());
			params.add(dataInfo.getItem_nm());
			params.add(dataInfo.getOption1());
			params.add(dataInfo.getOption2());
			params.add(dataInfo.getSale_price()); 
			params.add(dataInfo.getDeli_price());
			params.add(dataInfo.getQty()); 
			params.add(dataInfo.getDeli_gb());
			params.add(dataInfo.getError_code());
			params.add(dataInfo.getError_msg());
			params.add(dataInfo.getInuser());
			params.add(dataInfo.getRet_code());
			params.add(dataInfo.getCust_email());
			params.add(dataInfo.getClame_memo());
			params.add(dataInfo.getCube_item());			
			params.add(dataInfo.getCocd());
			params.add(dataInfo.getWhcd());
			params.add(dataInfo.getOrderKey());
			params.add(dataInfo.getOrderSeqKey()); 
			params.add(dataInfo.getShipKey());
			params.add(dataInfo.getVendorNm());
			params.add(dataInfo.getRet_desc());             // 반품사유상세 추가(2015.02.25 하윤식)          
			params.add(dataInfo.getItem_status());          // 상품등급     추가(2015.02.25 하윤식)
			params.add(dataInfo.getOri_ship_id()); 		 	// 원주문라인키 추가(2015.04.11)
			//params.add(dataInfo.getOrderLineNo_org());      // 원주문순번   추가(2015.02.25 하윤식)
			
			
			result = ExecQuery.update(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}	
		
		return result;
	}
	
	//반품취소 추가(하윤식 2015.03.12)
	public int setRecvReturnCancelData(Connection conn, ServiceDataInfo dataInfo, String transcd) throws Exception {
		String sql = "";
		ArrayList<String> params = null;
		int result = 0;
		try {
			
			sql = getSql("ServiceDAO.setRecvReturnCancelData","","",transcd);
			params = new ArrayList<String>();
			
			params.add(dataInfo.getCall_dt());
			params.add(dataInfo.getCall_seq());
			params.add(dataInfo.getSeq());
			params.add(dataInfo.getRecv_gb());
			params.add(dataInfo.getFirst_order_id());
			params.add(dataInfo.getOrder_id());
			params.add(dataInfo.getShipKey());
			params.add(dataInfo.getTrans_dt());
			params.add(dataInfo.getVendor_id());
			params.add(dataInfo.getCancel_dt());
			params.add(dataInfo.getChange_gb());
			params.add(dataInfo.getShip_status());
			params.add(dataInfo.getReceipt_nm());
			params.add(dataInfo.getReceipt_tel());
			params.add(dataInfo.getReceipt_hp());
			params.add(dataInfo.getReceipt_zipcode());
			params.add(dataInfo.getReceipt_addr1());
			params.add(dataInfo.getReceipt_addr2());
			params.add(dataInfo.getCust_nm());
			params.add(dataInfo.getCust_tel());
			params.add(dataInfo.getCust_hp());
			params.add(dataInfo.getCust_zipcode());
			params.add(dataInfo.getCust_addr1());
			params.add(dataInfo.getCust_addr2());
			params.add(dataInfo.getDelivery_msg());
			params.add(dataInfo.getOrder_seq());
			params.add(dataInfo.getShip_seq()); 
			params.add(dataInfo.getItem_cd());
			params.add(dataInfo.getItem_nm());
			params.add(dataInfo.getOption1());
			params.add(dataInfo.getOption2());
			params.add(dataInfo.getSale_price()); 
			params.add(dataInfo.getDeli_price());
			params.add(dataInfo.getQty()); 
			params.add(dataInfo.getDeli_gb());
			params.add(dataInfo.getError_code());
			params.add(dataInfo.getError_msg());
			params.add(dataInfo.getInuser());
			params.add(dataInfo.getRet_code());
			params.add(dataInfo.getCust_email());
			params.add(dataInfo.getClame_memo());
			params.add(dataInfo.getCube_item());			
			params.add(dataInfo.getCocd());
			params.add(dataInfo.getWhcd());
			params.add(dataInfo.getOrderKey());
			params.add(dataInfo.getOrderSeqKey()); 
			params.add(dataInfo.getShipKey());
			params.add(dataInfo.getVendorNm());
			params.add(dataInfo.getRet_desc());             // 반품사유상세 추가(2015.02.25 하윤식)          
			params.add(dataInfo.getItem_status());          // 상품등급     추가(2015.02.25 하윤식)
			params.add(dataInfo.getOrderReleaseKey_org());  // 원주문확정키 추가(2015.02.25 하윤식)
			params.add(dataInfo.getOrderLineNo_org());      // 원주문순번   추가(2015.02.25 하윤식)
			
			
			result = ExecQuery.update(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}	
		
		return result;
	}

	public int setErrorRecvLog(Connection conn, ServiceLogInfo rlog, String error_msg) throws Exception {
		String sql = "";
		ArrayList<String> params = null;
		int result = 0;
		try {
			
			sql = getSql("ServiceDAO.setErrorRecvLog","","","");
			params = new ArrayList<String>(); 
			
			params.add(error_msg);
			params.add(rlog.getCall_dt());
			params.add(rlog.getCall_seq());
			
			result = ExecQuery.update(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}	
		
		return result;
	}
	
	/**
	 * 샵링커 송장전송 대상 리스트
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getShopSendDeliveryData(Connection conn, String order_id, String expnm) throws Exception {
		
		String sql = null;
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		List<Object> result = null;
		
		try {
			sql = getSql("ServiceDAO.getShopSendDeliveryData","","","");
			rsh = new MapListHandler();
			params = new ArrayList<Object>();
			params.add(order_id);
			params.add(expnm);
			
			Logger.debug("[order_id]"+order_id+"[expnm]"+expnm);
			
			result = (List<Object>)ExecQuery.query(conn, sql, params, rsh);
			 
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		
		
		return result;
	}
	
	
	
	/**
	 * 샵링커 송장전송 대상 리스트
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getShopSendDeliveryList(Connection conn) throws Exception {
		
		String sql = null;
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		List<Object> result = null;
		
		
		try {
			sql = getSql("ServiceDAO.ShopSendDeliveryList","","","");;
			rsh = new MapListHandler();
			params = new ArrayList<Object>();
			
			result = (List<Object>)ExecQuery.query(conn, sql, params, rsh);
			 
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		
		
		return result;
	}
	
	
	/**
	 * 샵링커 주문전송 대상 리스트
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getShopRecvOrderList(Connection conn) throws Exception {
		
		String sql = null;
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		List<Object> result = null;
		
		
		try {
			sql = getSql("ServiceDAO.ShopRecvOrderList","","","");;
			rsh = new MapListHandler();
			params = new ArrayList<Object>();
			
			result = (List<Object>)ExecQuery.query(conn, sql, params, rsh);
			 
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		
		
		return result;
	}
	
	/**
	 * 샵링커 클레임 대상 리스트
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getShopRecvClameList(Connection conn) throws Exception {
		
		String sql = null;
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		List<Object> result = null;
		
		
		try {
			sql = getSql("ServiceDAO.ShopRecvClameList","","","");;
			rsh = new MapListHandler();
			params = new ArrayList<Object>();
			
			result = (List<Object>)ExecQuery.query(conn, sql, params, rsh);
			 
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		
		
		return result;
	}

	public String getShopRecvCallSeq(Connection conn, String call_dt) throws Exception {
		String result = "";
		String sql = "";
		ArrayList<Object> params = null;
		ResultSetHandler rsh = null;
		Map<Object, String> map = null; 
		
		try {
			sql = getSql("ServiceDAO.getShopRecvCallSeq","","","");
			params = new ArrayList<Object>();	
			params.add(call_dt);
			
			rsh = new MapHandler();
			map = (Map<Object, String>)ExecQuery.query(conn, sql, params, rsh);		
			result = map.get("CALL_SEQ"); 
			
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}
		return result;
		
	}

	public int setShopRecvData(Connection conn, ServiceShoplinkerInfo sInfo) throws Exception {
		String sql = "";
		ArrayList<String> params = null;
		int result = 0;
		try {
			
			sql = getSql("ServiceDAO.setShopRecvData","","","");
			params = new ArrayList<String>();
			
			
			params.add(sInfo.getShoplinker_order_id());
			params.add(sInfo.getMall_order_id());
			params.add(sInfo.getMall_name());
			params.add(sInfo.getBaesong_status());
			params.add(sInfo.getOrder_name());
			params.add(sInfo.getOrder_tel());
			params.add(sInfo.getOrder_cel());
			params.add(sInfo.getOrder_email());
			params.add(sInfo.getReceive());
			params.add(sInfo.getReceive_tel());
			params.add(sInfo.getReceive_cel());
			params.add(sInfo.getReceive_zipcode());
			params.add(sInfo.getReceive_addr());
			params.add(sInfo.getBaesong_type());
			params.add(sInfo.getBaesong_bi());
			params.add(sInfo.getDelivery_msg());
			params.add(sInfo.getOrder_product_id());
			params.add(sInfo.getShoplinker_product_id());
			params.add(sInfo.getPartner_product_id());
			params.add(sInfo.getProduct_name());
			params.add(sInfo.getQuantity());
			params.add(sInfo.getOrder_price());
			params.add(sInfo.getSale_price());
			params.add(sInfo.getSupply_price());
			params.add(sInfo.getSku());
			params.add(sInfo.getOrderdate());
			params.add(sInfo.getOrder_reg_date());
			params.add(sInfo.getClame_status());
			params.add(sInfo.getClame_memo());
			params.add(sInfo.getClame_date());
			params.add(sInfo.getRecv_gb());
			params.add(sInfo.getCall_seq());
			params.add(sInfo.getSeq());
			params.add(sInfo.getCocd());
			params.add(sInfo.getInuser());
			
			result = ExecQuery.update(conn, sql, params);
			
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}	
		
		return result;
	}

	/**
	public int setShopSendLog(Connection conn, ServiceLogInfo slog) throws Exception {
		String sql = "";
		ArrayList<String> params = null;
		int result = 0;
		try {

			sql = getSql("ServiceDAO.setShopSendLog");
			params = new ArrayList<String>(); 
			
			params.add(slog.getResult_code());
			params.add(slog.getShip_id());
			params.add(slog.getResult_name());
			params.add(slog.getCall_dt());
			params.add(slog.getCall_seq()); 
			params.add(slog.getInuser());
			

			result = ExecQuery.update(conn, sql, params);
		} catch (Exception e) {
			Logger.error(e);
			throw e;
		}	
		
		return result;
	}
	*/

}
