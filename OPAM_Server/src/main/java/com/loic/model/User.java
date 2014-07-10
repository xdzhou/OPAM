package com.loic.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="User")
public class User {
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
	@Column(length=20)
	private String login;
	@Column(length=50)
    private String name;
	private String email;
    private int numWeekUpdated;
    
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNumWeekUpdated() {
		return numWeekUpdated;
	}
	public void setNumWeekUpdated(int numWeekUpdated) {
		this.numWeekUpdated = numWeekUpdated;
	}
	public long getId() {
		return id;
	}	    
	
}
