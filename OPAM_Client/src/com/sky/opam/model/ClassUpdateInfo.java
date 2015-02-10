package com.sky.opam.model;

import java.util.Date;

import com.loic.common.sqliteTool.Column;
import com.loic.common.sqliteTool.Model;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;

@Model
public class ClassUpdateInfo
{
	public int year;
	public int month;
	@Column(length = 10)
	public String login;
	
	public Date lastUpdateDate;
	public int classNumber;
	public boolean isSuccess;
	public HttpServiceErrorEnum errorEnum;
}
