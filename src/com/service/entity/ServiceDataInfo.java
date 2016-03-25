package com.service.entity;

/**
 * ServiceDataInfo.java
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
public class ServiceDataInfo{

	String call_dt              = null;    //API Call 일자             
	String call_seq             = null;    //API Call 차수             
	String seq                  = null;    //API Call 차수별 순번      
	String recv_gb              = null;    //API 구분                  
	String first_order_id       = null;    //원주문번호                
	String order_id             = null;    //주문번호                  
	String ship_id              = null;    //출고번호                  
	String trans_dt             = null;    //전송일자                  
	String vendor_id            = null;    //거래처                    
	String instruct_dt          = null;    //출고/회수지시일           
	String cancel_dt            = null;    //출고/회수지시취소일       
	String change_gb            = null;    //교환구분                  
	String ship_status          = null;    //상태                      
	String receipt_nm           = null;    //수취인명                  
	String receipt_tel          = null;    //수취인전화                
	String receipt_hp           = null;    //수취인 핸드폰             
	String receipt_zipcode      = null;    //수취인 우편번호           
	String receipt_addr1        = null;    //수취인주소1               
	String receipt_addr2        = null;    //수취인주소2               
	String cust_nm              = null;    //주문자명                  
	String cust_email           = null;    //주문자 이메일
	String cust_tel             = null;    //주문자 전화               
	String cust_hp              = null;    //주문자 핸드폰             
	String cust_zipcode         = null;    //주문자 우편번호           
	String cust_addr1           = null;    //주문자 주소1              
	String cust_addr2           = null;    //주문자 주소2              
	String delivery_msg         = null;    //배송메세지                
	String order_seq            = null;    //주문순번                  
	String ship_seq             = null;    //출고지시/회수지시순번     
	String item_cd              = null;    //상품코드                  
	String item_nm              = null;    //상품명                    
	String option1              = null;    //속성1                     
	String option2              = null;    //속성2                     
	String sale_price           = null;    //판매가                    
	String deli_price           = null;    //공급원가                  
	String qty                  = null;    //구매수량                  
	String deli_gb              = null;    //착불여부                  
	String error_code           = null;    //처리상태                  
	String error_msg            = null;    //처리결과메세지            
	String podt                 = null;    //입금일자(판매일자)        
	String pono                 = null;    //주문번호                  
	String poseq                = null;    //주문순번                          
	String tempno               = null;    //출고전표                  
	String inuser               = null;    //입력자                    
	String intime               = null;    //입력시간  
	String ori_ship_id			= null;    //원출고번호(출고번호||출고순번)
	String ret_code				= null;    //반품취소사유 
	String clame_memo			= null;    //취소,반품,클레임 사유
	String cube_item			= null;    //큐브아이템
	String cocd					= null;    //사업부코드
	String whcd					= null;    //창고코드
	String order_key			= null;    //SC 주문번호키
	String orderSeq_key			= null;    //SC 주문순번키
	String ship_key				= null;    //SC 주문확정키
	String vendorNm            	= null;    //거래처명
	String ret_desc             = null;    //반품사유상세 추가(2015.02.25 하윤식)
	String item_status          = null;    //상품등급     추가(2015.02.25 하윤식)
	String orderLineNo_org      = null;    //원주문순번   추가(2015.02.25 하윤식)
    String orderReleaseKey_org  = null;    //원주문확정키 추가(2015.02.25 하윤식)
    String nodeType				= null;	   //
    String strVendor_Pono		= null;	   //벤더번호 추가 [IOS 26-Jan-16] 
		
    public String getVendor_Pono()  {
    	return strVendor_Pono;
    }
    
    public void setVendor_Pono(String V_Pono)  {
    	this.strVendor_Pono = V_Pono;
    }
    
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	public String getRet_code() {
		return ret_code;
	}
	public void setRet_code(String ret_code) {
		this.ret_code = ret_code;
	}
	public String getClame_memo() {
		return clame_memo;
	}
	public void setClame_memo(String clame_memo) {
		this.clame_memo = clame_memo;
	}
	public String getOri_ship_id() {
		return ori_ship_id;
	}
	public void setOri_ship_id(String ori_ship_id) {
		this.ori_ship_id = ori_ship_id;
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
	public String getSeq() {
		return seq;
	}
	public void setSeq(String seq) {
		this.seq = seq;
	}
	public String getRecv_gb() {
		return recv_gb;
	}
	public void setRecv_gb(String recv_gb) {
		this.recv_gb = recv_gb;
	}
	public String getFirst_order_id() {
		return first_order_id;
	}
	public void setFirst_order_id(String first_order_id) {
		this.first_order_id = first_order_id;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getShip_id() {
		return ship_id;
	}
	public void setShip_id(String ship_id) {
		this.ship_id = ship_id;
	}
	public String getTrans_dt() {
		return trans_dt;
	}
	public void setTrans_dt(String trans_dt) {
		this.trans_dt = trans_dt;
	}
	public String getVendor_id() {
		return vendor_id;
	}
	public void setVendor_id(String vendor_id) {
		this.vendor_id = vendor_id;
	}
	public String getInstruct_dt() {
		return instruct_dt;
	}
	public void setInstruct_dt(String instruct_dt) {
		this.instruct_dt = instruct_dt;
	}
	public String getCancel_dt() {
		return cancel_dt;
	}
	public void setCancel_dt(String cancel_dt) {
		this.cancel_dt = cancel_dt;
	}
	public String getChange_gb() {
		return change_gb;
	}
	public void setChange_gb(String change_gb) {
		this.change_gb = change_gb;
	}
	public String getShip_status() {
		return ship_status;
	}
	public void setShip_status(String ship_status) {
		this.ship_status = ship_status;
	}
	public String getReceipt_nm() {
		return receipt_nm;
	}
	public void setReceipt_nm(String receipt_nm) {
		this.receipt_nm = receipt_nm;
	}
	public String getReceipt_tel() {
		return receipt_tel;
	}
	public void setReceipt_tel(String receipt_tel) {
		this.receipt_tel = receipt_tel;
	}
	public String getReceipt_hp() {
		return receipt_hp;
	}
	public void setReceipt_hp(String receipt_hp) {
		this.receipt_hp = receipt_hp;
	}
	public String getReceipt_zipcode() {
		return receipt_zipcode;
	}
	public void setReceipt_zipcode(String receipt_zipcode) {
		this.receipt_zipcode = receipt_zipcode;
	}
	public String getReceipt_addr1() {
		return receipt_addr1;
	}
	public void setReceipt_addr1(String receipt_addr1) {
		this.receipt_addr1 = receipt_addr1;
	}
	public String getReceipt_addr2() {
		return receipt_addr2;
	}
	public void setReceipt_addr2(String receipt_addr2) {
		this.receipt_addr2 = receipt_addr2;
	}
	public String getCust_nm() {
		return cust_nm;
	}
	public void setCust_nm(String cust_nm) {
		this.cust_nm = cust_nm;
	}
	public String getCust_email() {
		return cust_email;
	}
	public void setCust_email(String cust_email) {
		this.cust_email = cust_email;
	}
	public String getCust_tel() {
		return cust_tel;
	}
	public void setCust_tel(String cust_tel) {
		this.cust_tel = cust_tel;
	}
	public String getCust_hp() {
		return cust_hp;
	}
	public void setCust_hp(String cust_hp) {
		this.cust_hp = cust_hp;
	}
	public String getCust_zipcode() {
		return cust_zipcode;
	}
	public void setCust_zipcode(String cust_zipcode) {
		this.cust_zipcode = cust_zipcode;
	}
	public String getCust_addr1() {
		return cust_addr1;
	}
	public void setCust_addr1(String cust_addr1) {
		this.cust_addr1 = cust_addr1;
	}
	public String getCust_addr2() {
		return cust_addr2;
	}
	public void setCust_addr2(String cust_addr2) {
		this.cust_addr2 = cust_addr2;
	}
	public String getDelivery_msg() {
		return delivery_msg;
	}
	public void setDelivery_msg(String delivery_msg) {
		this.delivery_msg = delivery_msg;
	}
	public String getOrder_seq() {
		return order_seq;
	}
	public void setOrder_seq(String order_seq) {
		this.order_seq = order_seq;
	}
	public String getShip_seq() {
		return ship_seq;
	}
	public void setShip_seq(String ship_seq) {
		this.ship_seq = ship_seq;
	}
	public String getItem_cd() {
		return item_cd;
	}
	public void setItem_cd(String item_cd) {
		this.item_cd = item_cd;
	}
	public String getItem_nm() {
		return item_nm;
	}
	public void setItem_nm(String item_nm) {
		this.item_nm = item_nm;
	}
	public String getOption1() {
		return option1;
	}
	public void setOption1(String option1) {
		this.option1 = option1;
	}
	public String getOption2() {
		return option2;
	}
	public void setOption2(String option2) {
		this.option2 = option2;
	}
	public String getSale_price() {
		return sale_price;
	}
	public void setSale_price(String sale_price) {
		this.sale_price = sale_price;
	}
	public String getDeli_price() {
		return deli_price;
	}
	public void setDeli_price(String deli_price) {
		this.deli_price = deli_price;
	}
	public String getQty() {
		return qty;
	}
	public void setQty(String qty) {
		this.qty = qty;
	}
	public String getDeli_gb() {
		return deli_gb;
	}
	public void setDeli_gb(String deli_gb) {
		this.deli_gb = deli_gb;
	}
	public String getError_code() {
		return error_code;
	}
	public void setError_code(String error_code) {
		this.error_code = error_code;
	}
	public String getError_msg() {
		return error_msg;
	}
	public void setError_msg(String error_msg) {
		this.error_msg = error_msg;
	}
	public String getPodt() {
		return podt;
	}
	public void setPodt(String podt) {
		this.podt = podt;
	}
	public String getPono() {
		return pono;
	}
	public void setPono(String pono) {
		this.pono = pono;
	}
	public String getPoseq() {
		return poseq;
	}
	public void setPoseq(String poseq) {
		this.poseq = poseq;
	}
	public String getTempno() {
		return tempno;
	}
	public void setTempno(String tempno) {
		this.tempno = tempno;
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
	public String getCube_item() {
		return cube_item;
	}
	public void setCube_item(String cube_item) {
		this.cube_item = cube_item;
	}
	public String getCocd() {
		return cocd;
	}
	public void setCocd(String cocd) {
		this.cocd = cocd;
	}
	public String getWhcd() {
		return whcd;
	}
	public void setWhcd(String whcd) {
		this.whcd = whcd;
	}	
	public String getOrderKey() {
		return order_key;
	}
	public void setOrderKey(String order_key) {
		this.order_key = order_key;
	}	
	public String getOrderSeqKey() {
		return orderSeq_key;
	}
	public void setOrderSeqKey(String orderSeq_key) {
		this.orderSeq_key = orderSeq_key;
	}
	public String getShipKey() {
		return ship_key;
	}
	public void setShipKey(String ship_key) {
		this.ship_key = ship_key;
	}		
	public void setVendorNm(String vendorNm) {
		this.vendorNm = vendorNm;
	}
	public String getVendorNm() {
		return vendorNm;
	}
	public String getRet_desc() {
		return ret_desc;
	}
	public void setRet_desc(String ret_desc) {
		this.ret_desc = ret_desc;
	}
	public String getItem_status() {
		return item_status;
	}
	public void setItem_status(String item_status) {
		this.item_status = item_status;
	}
	public String getOrderLineNo_org() {
		return orderLineNo_org;
	}
	public void setOrderLineNo_org(String orderLineNo_org) {
		this.orderLineNo_org = orderLineNo_org;
	}
	public String getOrderReleaseKey_org() {
		return orderReleaseKey_org;
	}
	public void setOrderReleaseKey_org(String orderReleaseKey_org) {
		this.orderReleaseKey_org = orderReleaseKey_org;
	}
	
}