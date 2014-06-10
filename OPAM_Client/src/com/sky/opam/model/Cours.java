package com.sky.opam.model;

import java.io.Serializable;

public class Cours implements Serializable{

	private static final long serialVersionUID = 1L;
	public String login = "";
	public String name = "";
	public String type = "";
	public String position = "";
	public String debut;
	public String fin;
	public String auteur = "";
	public String formateur = "";
	public String apprenants = "";
	public String groupe = "";
	public String salle = "";

	public Cours() {
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

}
