package com.service.entity;

public class ServiceShoplinkerInfo {
	
	String shoplinker_order_id  = ""; //샵링커 주문번호

	String mall_order_id  = ""; //쇼핑몰 주문번호
	String mall_name  = ""; //쇼핑몰 명
	String baesong_status  = ""; //배송상태(발주확인)
	String order_name  = ""; //주문자명
	String order_tel  = ""; //주문자 전화번호
	String order_cel  = ""; //주문자 핸드폰번호
	String order_email  = "";  //주문자 이메일주소
	String receive  = "";//수취인명
	String receive_tel  = ""; //수취인 전화번호
	String receive_cel  = ""; //수취인 핸드폰 번호
	String receive_zipcode  = ""; //수취인 우편번호
	String receive_addr  = ""; //수취인 주소
	String baesong_type  = ""; //배송비결제방식(무료,착불,착불 선결제)
	String baesong_bi  = ""; //배송비
	String delivery_msg  = ""; //배송메세지(방문 수령이나 해외주문일 경우 msg 에 노출) ex:고객 요청 or 해외 배송 주문
	String order_product_id  = ""; //주문상품번호
	String shoplinker_product_id  = ""; //샵링커 상품번호
	String partner_product_id  = ""; //고객사상품코드
	String product_name  = ""; //상품명
	String quantity  = ""; //총 주문 수량
	String order_price  = ""; //총 주문 금액
	String sale_price  = ""; //판매 단가
	String supply_price  = ""; //공급가
	String sku  = ""; //옵션명
	String orderdate  = ""; //주문일자
	String order_reg_date  = ""; //주문수집일자
	String clame_status  = ""; //클레임상태(클레임 수집에도 입력 요망)
	String clame_memo  = ""; //클레임 메모
	String clame_date  = ""; //클레임 날짜
	String recv_gb  = "";  //주문 / 클레임 구분
	String call_seq  = ""; // call 차수
	String seq  = ""; // call 차수 순번
	String cocd  = ""; //사업부
	String inuser  = ""; //입력자
	String intime  = ""; //입력시간
	String result  = ""; //결과
	String id  = ""; //샵링커 주문번호(ship_id 와 매핑)
	String message  = ""; //결과 메세지

	
	
	
	public void setShoplinker_order_id(String shoplinker_order_id) {
		this.shoplinker_order_id = shoplinker_order_id;
	}
	public void setMall_order_id(String mall_order_id) {
		this.mall_order_id = mall_order_id;
	}
	public void setMall_name(String mall_name) {
		this.mall_name = mall_name;
	}
	public void setBaesong_status(String baesong_status) {
		this.baesong_status = baesong_status;
	}
	public void setOrder_name(String order_name) {
		this.order_name = order_name;
	}
	public void setOrder_tel(String order_tel) {
		this.order_tel = order_tel;
	}
	public void setOrder_cel(String order_cel) {
		this.order_cel = order_cel;
	}
	public void setOrder_email(String order_email) {
		this.order_email = order_email;
	}
	public void setReceive(String receive) {
		this.receive = receive;
	}
	public void setReceive_tel(String receive_tel) {
		this.receive_tel = receive_tel;
	}
	public void setReceive_cel(String receive_cel) {
		this.receive_cel = receive_cel;
	}
	public void setReceive_zipcode(String receive_zipcode) {
		this.receive_zipcode = receive_zipcode;
	}
	public void setReceive_addr(String receive_addr) {
		this.receive_addr = receive_addr;
	}
	public void setBaesong_type(String baesong_type) {
		this.baesong_type = baesong_type;
	}
	public void setBaesong_bi(String baesong_bi) {
		this.baesong_bi = baesong_bi;
	}
	public void setDelivery_msg(String delivery_msg) {
		this.delivery_msg = delivery_msg;
	}
	public void setOrder_product_id(String order_product_id) {
		this.order_product_id = order_product_id;
	}
	public void setShoplinker_product_id(String shoplinker_product_id) {
		this.shoplinker_product_id = shoplinker_product_id;
	}
	public void setPartner_product_id(String partner_product_id) {
		this.partner_product_id = partner_product_id;
	}
	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	public void setOrder_price(String order_price) {
		this.order_price = order_price;
	}
	public void setSale_price(String sale_price) {
		this.sale_price = sale_price;
	}
	public void setSupply_price(String supply_price) {
		this.supply_price = supply_price;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public void setOrderdate(String orderdate) {
		this.orderdate = orderdate;
	}
	public void setOrder_reg_date(String order_reg_date) {
		this.order_reg_date = order_reg_date;
	}
	public void setClame_status(String clame_status) {
		this.clame_status = clame_status;
	}
	public void setClame_memo(String clame_memo) {
		this.clame_memo = clame_memo;
	}
	public void setClame_date(String clame_date) {
		this.clame_date = clame_date;
	}
	public void setRecv_gb(String recv_gb) {
		this.recv_gb = recv_gb;
	}
	public void setCall_seq(String call_seq) {
		this.call_seq = call_seq;
	}
	public void setSeq(String seq) {
		this.seq = seq;
	}
	public void setCocd(String cocd) {
		this.cocd = cocd;
	}
	public void setInuser(String inuser) {
		this.inuser = inuser;
	}
	public void setIntime(String intime) {
		this.intime = intime;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
	public String getShoplinker_order_id() {
		return shoplinker_order_id;
	}
	public String getMall_order_id() {
		return mall_order_id;
	}
	public String getMall_name() {
		return mall_name;
	}
	public String getBaesong_status() {
		return baesong_status;
	}
	public String getOrder_name() {
		return order_name;
	}
	public String getOrder_tel() {
		return order_tel;
	}
	public String getOrder_cel() {
		return order_cel;
	}
	public String getOrder_email() {
		return order_email;
	}
	public String getReceive() {
		return receive;
	}
	public String getReceive_tel() {
		return receive_tel;
	}
	public String getReceive_cel() {
		return receive_cel;
	}
	public String getReceive_zipcode() {
		return receive_zipcode;
	}
	public String getReceive_addr() {
		return receive_addr;
	}
	public String getBaesong_type() {
		return baesong_type;
	}
	public String getBaesong_bi() {
		return baesong_bi;
	}
	public String getDelivery_msg() {
		return delivery_msg;
	}
	public String getOrder_product_id() {
		return order_product_id;
	}
	public String getShoplinker_product_id() {
		return shoplinker_product_id;
	}
	public String getPartner_product_id() {
		return partner_product_id;
	}
	public String getProduct_name() {
		return product_name;
	}
	public String getQuantity() {
		return quantity;
	}
	public String getOrder_price() {
		return order_price;
	}
	public String getSale_price() {
		return sale_price;
	}
	public String getSupply_price() {
		return supply_price;
	}
	public String getSku() {
		return sku;
	}
	public String getOrderdate() {
		return orderdate;
	}
	public String getOrder_reg_date() {
		return order_reg_date;
	}
	public String getClame_status() {
		return clame_status;
	}
	public String getClame_memo() {
		return clame_memo;
	}
	public String getClame_date() {
		return clame_date;
	}
	public String getRecv_gb() {
		return recv_gb;
	}
	public String getCall_seq() {
		return call_seq;
	}
	public String getSeq() {
		return seq;
	}
	public String getCocd() {
		return cocd;
	}
	public String getInuser() {
		return inuser;
	}
	public String getIntime() {
		return intime;
	}
	public String getResult() {
		return result;
	}
	public String getId() {
		return id;
	}
	public String getMessage() {
		return message;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}
