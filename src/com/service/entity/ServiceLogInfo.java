package com.service.entity;

public class ServiceLogInfo {

	//recv_log
	String start_dt         = null;    	//조회시작일      
	String end_dt           = null;		//조회종료일      
	
	//send_log
	String ship_id          = null;  	//출고/회수번호
	String apply_dt         = null;  	//출고일자     
	String apply_time       = null;  	//출고시간     
	String deli_company_id  = null;  	//배송사코드   
	String bl_no            = null;  	//송장번호 
	
	//공통
	String call_dt          = null;    	//API Call 일자   
	String call_seq         = null;    	//API Call 차수   
	String call_api         = null;    	//API ID          
	String call_api_name    = null;    	//API 명          
	String vendor_id        = null;    	//거래처코드      
	String result_code      = null;    	//결과코드        
	String result_name      = null;    	//결과메시지      
	String inuser           = "SYSTEM";	//입력자          
	String intime           = null;    	//입력시간   
	String cancel_code 		= null; 	//주문취소사유
	
	
	String result = null;  		//샵링커 결과
	String id = null; 			//샵링커 id
	String message = null; 		//샵링커 메세지
	
	
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCancel_code() {
		return cancel_code;
	}
	public void setCancel_code(String cancel_code) {
		this.cancel_code = cancel_code;
	}
	public String getShip_id() {
		return ship_id;
	}
	public void setShip_id(String ship_id) {
		this.ship_id = ship_id;
	}
	public String getApply_dt() {
		return apply_dt;
	}
	public void setApply_dt(String apply_dt) {
		this.apply_dt = apply_dt;
	}
	public String getApply_time() {
		return apply_time;
	}
	public void setApply_time(String apply_time) {
		this.apply_time = apply_time;
	}
	public String getDeli_company_id() {
		return deli_company_id;
	}
	public void setDeli_company_id(String deli_company_id) {
		this.deli_company_id = deli_company_id;
	}
	public String getBl_no() {
		return bl_no;
	}
	public void setBl_no(String bl_no) {
		this.bl_no = bl_no;
	}
	
	public String getCall_dt() {
		return call_dt;
	}
	public void setCall_dt(String call_dt) {
		this.call_dt = call_dt;
	}
	public String getCall_seq() {
		return call_seq;
	}
	public void setCall_seq(String call_seq) {
		this.call_seq = call_seq;
	}
	public String getCall_api() {
		return call_api;
	}
	public void setCall_api(String call_api) {
		this.call_api = call_api;
	}
	public String getCall_api_name() {
		return call_api_name;
	}
	public void setCall_api_name(String call_api_name) {
		this.call_api_name = call_api_name;
	}
	public String getVendor_id() {
		return vendor_id;
	}
	public void setVendor_id(String vendor_id) {
		this.vendor_id = vendor_id;
	}
	public String getStart_dt() {
		return start_dt;
	}
	public void setStart_dt(String start_dt) {
		this.start_dt = start_dt;
	}
	public String getEnd_dt() {
		return end_dt;
	}
	public void setEnd_dt(String end_dt) {
		this.end_dt = end_dt;
	}
	public String getResult_code() {
		return result_code;
	}
	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}
	public String getResult_name() {
		return result_name;
	}
	public void setResult_name(String result_name) {
		this.result_name = result_name;
	}
	public String getInuser() {
		return inuser;
	}
	public void setInuser(String inuser) {
		this.inuser = inuser;
	}
	public String getIntime() {
		return intime;
	}
	public void setIntime(String intime) {
		this.intime = intime;
	}
	
}
