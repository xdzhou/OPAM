package com.loic.agenda.model;

public class Cours {
	public String NumEve="";
	public String dateSrc="";
	public String name="";
	public String type="";
	public String debut="debut";
	public String fin="fin";
	public String auteur="";
	public String formateur="";
	public String salle="";
	public String apprenants="";
	public String group="";
	public String position="";

	public Cours(String NumEve, String dateSrc){
		this.NumEve = NumEve;
		this.dateSrc = dateSrc;
	}

	@Override
	public String toString() {
		return "Cours [name=" + name + ", type=" + type + ", debut=" + debut
				+ ", fin=" + fin + ", auteur=" + auteur + ", formateur="
				+ formateur + ", salle=" + salle + ", group=" + group + "]";
	}



}
