package com.sky.opam.model;

import java.io.Serializable;

public class TranObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TranObjectType type;
	private String interlocutor = "server";
	private String contenu;

	public TranObject() {
	}

	public TranObjectType getType() {
		return type;
	}
	
	public void setType(TranObjectType type) {
		this.type=type;
	}

	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}
	

	public String getInterlocutor() {
		return interlocutor;
	}

	public void setInterlocutor(String interlocutor) {
		this.interlocutor = interlocutor;
	}

	@Override
	public String toString() {
		return "TranObject [type=" + type + ", contenu=" + contenu + "]";
	}

	
}
