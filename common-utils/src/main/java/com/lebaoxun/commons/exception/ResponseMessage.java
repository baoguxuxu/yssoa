package com.lebaoxun.commons.exception;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ResponseMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3814041435546894811L;

	private String errmsg;
	
	private String errcode;
	
	private Object data;
	
	public static ResponseMessage ok(){
		return new ResponseMessage();
	}
	
	public ResponseMessage put(String key,Object value){
		Map<String,Object> model = new HashMap<String,Object>();
		model.put(key, value);
		this.data = model;
		return this;
	} 
	
	public static ResponseMessage ok(Object data){
		return new ResponseMessage(data);
	}
	
	public static ResponseMessage error(String errcode, String errmsg) {
		return error(errcode, errmsg, null);
	}
	
	public static ResponseMessage error(String errcode, String errmsg,Object data) {
		ResponseMessage r = new ResponseMessage();
		r.setErrcode(errcode);
		r.setErrmsg(errmsg);
		r.setData(data);
		return r;
	}
	
	public ResponseMessage() {
		this.errcode = "0";
		this.errmsg = "ok";
	}
	
	public ResponseMessage(Object data) {
		this();
		this.data = data;
	}
	
	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public String getErrcode() {
		return errcode;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}
}
