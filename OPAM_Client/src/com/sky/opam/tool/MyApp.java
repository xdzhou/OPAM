package com.sky.opam.tool;

import android.app.Application;

public class MyApp extends Application{
	private String login;
	private int currentWeekNum;
	public static final int rsqCode = 11;
	public static final int Exit = 0xde;
	public static final int Update = 0xaa;
	public static final int Refresh = 22;
	
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public int getCurrentWeekNum() {
		return currentWeekNum;
	}
	public void setCurrentWeekNum(int currentWeekNum) {
		this.currentWeekNum = currentWeekNum;
	}
	
}
