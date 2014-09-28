package com.sky.opam.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class ClassInfo implements Serializable, Comparable<ClassInfo>{
	private static final long serialVersionUID = 1L;
	
	public long id;
	public String login = "";
	public String name = "";
	public ClassType classType = new ClassType();
	public int weekOfYear;
	public int dayOfWeek;
	public String startTime = "";
	public String endTime = "";
	public String auteur = "";
	public String teacher = "";
	public String students = "";
	public String groupe = "";
	public Room room = new Room();
	public String bgColor = "#999999";
	public long eventId ;
	
	public static Comparator<ClassInfo> timeComparator = new Comparator<ClassInfo>() 
	{
		@Override
		public int compare(ClassInfo first, ClassInfo second) 
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
		return "ClassInfo [id=" + id + ", login=" + login + ", name=" + name
				+ ", weekOfYear=" + weekOfYear + ", dayOfWeek=" + dayOfWeek
				+ ", startTime=" + startTime + ", endTime=" + endTime
				+ ", eventId=" + eventId + "]";
	}

	public static ClassInfo getCustomedClass(String login)
	{
		ClassInfo c = new ClassInfo();
		c.id = -1;
		c.login = c.auteur = login;
		c.bgColor = "#999999";
		return c;
	}

	public String getCalendarTitle() 
	{
		if (name != null && name.contains("Point de Rencontre"))
			return name;
		else if(classType.name != null && name != null)
			return classType.name + " - " + name;
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
	public int compareTo(ClassInfo another) 
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassInfo other = (ClassInfo) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
