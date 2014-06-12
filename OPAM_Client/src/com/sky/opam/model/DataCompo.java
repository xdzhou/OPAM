package com.sky.opam.model;

import java.util.ArrayList;
import java.util.List;

public class DataCompo {
	private List<ClassInfo> cours = new ArrayList<ClassInfo>();
	private int id;
	private int numweek;
	private String username;

	public DataCompo() {

	}

	public DataCompo(List<ClassInfo> cours, int id, int numweek, String username) {
		this.cours = cours;
		this.id = id;
		this.numweek = numweek;
		this.username = username;
	}

	public List<ClassInfo> getCours() {
		return cours;
	}

	public void setCours(List<ClassInfo> cours) {
		this.cours = cours;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumweek() {
		return numweek;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setNumweek(int numweek) {
		this.numweek = numweek;
	}

}
