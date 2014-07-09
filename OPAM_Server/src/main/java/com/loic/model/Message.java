package com.loic.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Message")
public class Message implements Comparable<Message> {
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	private User sender;
	private User recevier;
	private String content;
	private Date time;
	
	@Override
	public int compareTo(Message o) {
		if (null == o) return -1;
        else return this.time.compareTo(o.time);
	}

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

