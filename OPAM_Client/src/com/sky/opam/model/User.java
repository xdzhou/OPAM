package com.sky.opam.model;

import com.loic.common.sqliteTool.Column;
import com.loic.common.sqliteTool.ID;
import com.loic.common.sqliteTool.Model;

@Model
public class User
{
	@ID
	@Column(length = 10)
	public String login;
	@Column(length = 50)
	public String password;
	@Column(length = 50)
	public String name;
	
	public boolean isDefaultUser;
	public boolean isAutoSyncEvent;
	public boolean isAutoConnect;
	
	public int agendaStartHour;
	public int agendaEndHour;

	public User() 
	{
	}

	public User(String login, String password, String name) 
	{
		this.login = login;
		this.password = password;
		this.name = name;
		
		isAutoSyncEvent = isAutoConnect = true;
		isDefaultUser = false;
		agendaStartHour = 7;
		agendaEndHour = 19;
	}
}
