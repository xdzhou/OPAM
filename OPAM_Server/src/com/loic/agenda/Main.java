package com.loic.agenda;

import com.loic.agenda.outil.Chiffrement;

public class Main {

	public static void main(String[] args){
		//System.out.println(Chiffrement.encrypt("whoami?", "OPAM"));
		
		String[] room = "Systèmes Hautes Performances (ASR 5) S12 en B313".split("-");
		System.out.println(room[room.length-1]);
		
	}
	
}
