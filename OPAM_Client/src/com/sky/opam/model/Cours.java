package com.sky.opam.model;

public class Cours {
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
		return "Cours [login=" + login + ", name=" + name + ", type=" + type
				+ ", position=" + position + ", debut=" + debut + ", fin="
				+ fin + ", auteur=" + auteur + ", formateur=" + formateur
				+ ", apprenants=" + apprenants + ", groupe=" + groupe
				+ ", salle=" + salle + "]";
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
