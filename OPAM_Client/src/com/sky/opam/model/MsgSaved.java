package com.sky.opam.model;

import java.util.Date;

public class MsgSaved {

	String from;
	String to;
	Date date;
	String msg;
	
	public MsgSaved(){
		
	}
	
	public MsgSaved(String from, String to, Date date, String msg){
		this.from = from;
		this.to = to;
		this.date = date;
		this.msg = msg;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	

}
