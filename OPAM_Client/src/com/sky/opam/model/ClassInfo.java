package com.sky.opam.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClassInfo implements Serializable, Comparable<ClassInfo>{

	private static final long serialVersionUID = 1L;
	public String login;
	public String name;
	public String type;
	public String position;
	public String debut;
	public String fin;
	public String auteur;
	public String formateur;
	public String apprenants;
	public String groupe;
	public String salle;

	public ClassInfo() {
		login=name=type=position=auteur=formateur=apprenants=groupe="";
	}

	@Override
	public String toString() {
		return "Cours [" + "name=" + name + ", type=" + type
				+ ", position=" + position + ", debut=" + debut + ", fin="
				+ fin +"]";
	}

	public String getCalendarTitle() {
		if (name.contains("Point de Rencontre"))
			return name;
		else {
			return type + " - " + name;
		}
	}

	public String getCalendarDescription() {
		StringBuilder s = new StringBuilder();
		if (!formateur.equals("")) s.append("Teacher : ").append(formateur).append(" ");
		s.append("\nStudent : ").append(apprenants);
		return s.toString();
	}

	@Override
	public int compareTo(ClassInfo another) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		String FinDuMonde = "20121221";
		Date t2 = null, t1 = null;
		try {
			t1 = sdf.parse(FinDuMonde + " " + this.debut);
			t2 = sdf.parse(FinDuMonde + " " + another.debut);
		} catch (ParseException e) {
			e.printStackTrace();
		}		
		return (int) ((t1.getTime() - t2.getTime())/1000);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((debut == null) ? 0 : debut.hashCode());
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
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
		if (debut == null) {
			if (other.debut != null)
				return false;
		} else if (!debut.equals(other.debut))
			return false;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}
	
	

}
