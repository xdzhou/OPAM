package com.sky.opam.tool;

import cn.jpush.android.api.JPushInterface;
import android.app.Application;
import android.util.Log;

public class MyApp extends Application{
	private String login;
	private int currentWeekNum;
	public static final int rsqCode = 11;
	public static final int Exit = 0xde;
	public static final int Update = 0xaa;
	public static final int Refresh = 22;
	
	@Override
    public void onCreate() { 
         super.onCreate();         
         JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
         JPushInterface.init(this);     		// 初始化 JPush
    }
	
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
