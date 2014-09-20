package com.sky.opam.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class User {
	@DatabaseField(id = true)
	private String login;
	@DatabaseField
	private String cryptePW;
	@DatabaseField
	private String name;
	@DatabaseField
	private int numWeekUpdated;

	public User() {
	}

	public User(String login, String cryptePW, String name, int numWeekUpdated) {
		this.login = login;
		this.cryptePW = cryptePW;
		this.name = name;
		this.numWeekUpdated = numWeekUpdated;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
	

	public String getCryptePW() {
		return cryptePW;
	}

	public void setCryptePW(String cryptePW) {
		this.cryptePW = cryptePW;
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
}
