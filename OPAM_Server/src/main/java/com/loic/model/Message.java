package com.loic.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="Message")
public class Message{
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name="sender")
	private User sender;
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name="recevier")
	private User recevier;
	private String content;
	private Date time;	
	
	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getRecevier() {
		return recevier;
	}

	public void setRecevier(User recevier) {
		this.recevier = recevier;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public long getId() {
		return id;
	}  
}

