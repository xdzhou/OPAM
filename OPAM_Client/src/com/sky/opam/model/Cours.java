package com.sky.opam.model;

public class Cours {
	public String NumEve="";
	public String dateSrc="";
	
	public String login="";
	public String name="";
	public String type="";
	public String position="";
	public String debut;
	public String fin;
	public String auteur="";
	public String formateur="";	
	public String apprenants="";
	public String groupe="";
	public String salle="";

	public Cours(){
	}
	
	public Cours(String NumEve, String dateSrc){
		this.NumEve = NumEve;
		this.dateSrc = dateSrc;
	}

	@Override
	public String toString() {
		return "Cours [name=" + name + ", type=" + type + ", debut=" + debut
				+ ", fin=" + fin + ", auteur=" + auteur + ", formateur="
				+ formateur + ", salle=" + salle + ", group=" + groupe + "]";
	}



}
