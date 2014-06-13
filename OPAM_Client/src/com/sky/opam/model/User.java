package com.sky.opam.model;

public class User {
	private String login = "";
	private String passwoed = "";
	private String name = "";
	private int numWeekUpdated;

	public User() {
	}

	public User(String login, String passwoed, String name, int numWeekUpdated) {
		this.login = login;
		this.passwoed = passwoed;
		this.name = name;
		this.numWeekUpdated = numWeekUpdated;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPasswoed() {
		return passwoed;
	}

	public void setPasswoed(String passwoed) {
		this.passwoed = passwoed;
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
