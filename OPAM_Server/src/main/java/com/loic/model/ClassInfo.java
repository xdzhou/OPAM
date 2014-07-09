package com.loic.model;

public class ClassInfo{
	public transient String NumEve = "";
	public transient String dateSrc = "";
			
	public long id;
	public String login;
	public String name;
	public ClassType classType = new ClassType();
	public int weekOfYear;
	public int dayOfWeek;
	public String startTime;
	public String endTime;
	public String auteur;
	public String teacher;
	public String students;
	public String groupe;
	public Room room = new Room();
	public String color;
	public long eventId = 0;

	public ClassInfo() {
		login=name=auteur=teacher=students=groupe="";
	}
	
	public ClassInfo(String NumEve, String dateSrc){
		this.NumEve = NumEve;
		this.dateSrc = dateSrc;
	}

	@Override
	public String toString() {
		return "ClassInfo [id=" + id + ", login=" + login + ", name=" + name
				+ ", weekOfYear=" + weekOfYear + ", dayOfWeek=" + dayOfWeek
				+ ", startTime=" + startTime + ", endTime=" + endTime
				+ ", eventId=" + eventId + "]";
	}	
	
}
