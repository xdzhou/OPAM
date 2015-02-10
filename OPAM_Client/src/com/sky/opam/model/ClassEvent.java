package com.sky.opam.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.loic.common.sqliteTool.Column;
import com.loic.common.sqliteTool.ID;
import com.loic.common.sqliteTool.Model;

@Model
public class ClassEvent implements Comparable<ClassEvent>
{
	public static final  DateFormat dtf = new SimpleDateFormat("yyyyMMddHH:mm");
	
	@ID
	public long NumEve; //event id in INT server, used as ID
	@Column(length = 10)
	public String login;
	@Column(length = 30)
	public String name;
	@Column(length = 30)
	public String type;
	public Date startTime;
	public Date endTime;
	@Column(length = 30)
	public String auteur;
	@Column(length = 50)
	public String teacher;
	@Column(length = 255)
	public String students;
	@Column(length = 50)
	public String groupe;
	@Column(length = 30)
	public String room;
	public int bgColor;
	public long eventId;
	
	public ClassEvent()
	{
	}
	
	public static Comparator<ClassEvent> timeComparator = new Comparator<ClassEvent>() 
	{
		@Override
		public int compare(ClassEvent first, ClassEvent second) 
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
			String FinDuMonde = "20121221";
			Date t2 = null, t1 = null;
			try 
			{
				t1 = sdf.parse(FinDuMonde + " " + first.startTime);
				t2 = sdf.parse(FinDuMonde + " " + second.startTime);
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}		
			return (int) ((t1.getTime() - t2.getTime())/1000);
		}
	};


	@Override
	public String toString() 
	{
		return "ClassInfo [name=" + name + ", type=" + type + ", startTime="
				+ startTime + ", endTime=" + endTime + ", auteur=" + auteur
				+ ", teacher=" + teacher + ", groupe=" + groupe + ", room="
				+ room + "]";
	}

	public static ClassEvent getCustomedClass(String login)
	{
		ClassEvent c = new ClassEvent();
		c.auteur = login;
		return c;
	}

	public String getCalendarTitle() 
	{
		if (name != null && name.contains("Point de Rencontre"))
			return name;
		else if(type != null && name != null)
			return type + " - " + name;
		else
			return name;
	}

	public String getCalendarDescription() 
	{
		StringBuilder s = new StringBuilder();
		if (!teacher.equals("")) 
			s.append("Teacher : ").append(teacher).append(" ");
		s.append("\nStudent : ").append(students);
		return s.toString();
	}

	@Override
	public int compareTo(ClassEvent another) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		String FinDuMonde = "20121221";
		Date t2 = null, t1 = null;
		try {
			t1 = sdf.parse(FinDuMonde + " " + this.startTime);
			t2 = sdf.parse(FinDuMonde + " " + another.endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}		
		return (int) ((t1.getTime() - t2.getTime())/1000);
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (NumEve ^ (NumEve >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassEvent other = (ClassEvent) obj;
		if (NumEve != other.NumEve)
			return false;
		return true;
	}

}
