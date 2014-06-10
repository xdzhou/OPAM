package com.sky.opam.tool;

import android.app.Application;

public class MyApp extends Application{
	private String login;
	private int currentWeekNum;
	
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
