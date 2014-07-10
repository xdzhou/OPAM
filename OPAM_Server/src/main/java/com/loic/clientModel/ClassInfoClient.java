package com.loic.clientModel;

public class ClassInfoClient{
	public transient String NumEve = "";
	public transient String dateSrc = "";
			
	public long id;
	public String login;
	public String name;
	public ClassTypeClient classType = new ClassTypeClient();
	public int weekOfYear;
	public int dayOfWeek;
	public String startTime;
	public String endTime;
	public String auteur;
	public String teacher;
	public String students;
	public String groupe;
	public RoomClient room = new RoomClient();
	public String color;
	public long eventId = 0;

	public ClassInfoClient() {
		login=name=auteur=teacher=students=groupe="";
	}
	
	public ClassInfoClient(String NumEve, String dateSrc){
		this.NumEve = NumEve;
		this.dateSrc = dateSrc;
	}

	@Override
	public String toString() {
		return "ClassInfoClient [NumEve=" + NumEve + ", dateSrc=" + dateSrc
				+ ", login=" + login + ", name=" + name + ", classType="
				+ classType + ", weekOfYear=" + weekOfYear + ", dayOfWeek="
				+ dayOfWeek + ", startTime=" + startTime + ", endTime="
				+ endTime + ", auteur=" + auteur + ", teacher=" + teacher
				+ ", students=" + students + ", groupe=" + groupe + ", room="
				+ room + "]";
	}
	
	
}
