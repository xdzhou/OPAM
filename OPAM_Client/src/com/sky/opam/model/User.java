package com.sky.opam.model;

public class User {
	private String login = "";
	private String passwoed = "";
	private String usename = "";
	private int thisweek;
	// if 1, this user is the default user
	private int defaultUser = 0;

	public User() {
		super();
	}

	public User(String login, String passwoed, String username) {
		super();
		this.login = login;
		this.passwoed = passwoed;
		this.usename = username;
	}

	public User(String login, String passwoed, String username,
			int defaultUser, int thisweek) {
		super();
		this.login = login;
		this.passwoed = passwoed;
		this.usename = username;
		this.thisweek = thisweek;
		this.defaultUser = defaultUser;
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

	public int getThisweek() {
		return thisweek;
	}

	public void setThisweek(int thisweek) {
		this.thisweek = thisweek;
	}

	public String getUsename() {
		return usename;
	}

	public void setUsename(String usename) {
		this.usename = usename;
	}

	public int getDefaultUser() {
		return defaultUser;
	}

	public void setDefaultUser(int defaultUser) {
		this.defaultUser = defaultUser;
	}

	@Override
	public String toString() {
		return "User [login=" + login + ", passwoed=" + passwoed + ", usename="
				+ usename + ", thisweek=" + thisweek + ", defaultUser="
				+ defaultUser + "]";
	}

}
