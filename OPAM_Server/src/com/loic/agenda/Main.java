package com.loic.agenda;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.loic.agenda.model.Cours;
import com.loic.agenda.outil.Chiffrement;
import com.loic.agenda.outil.Java2xml;
import com.loic.agenda.outil.NetAnt;
import com.loic.agenda.outil.NetAntMutiThreads;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException, KeyManagementException, NoSuchAlgorithmException{
		//System.out.println(Chiffrement.encrypt("whoami?", "OPAM"));
		
		NetAnt es = new NetAnt();
	    List<Cours> cours = es.start("zhou_xia", "whoami?");
	    String userName = es.userName;
	    Java2xml b = new Java2xml(cours, userName);
	    
	    System.out.print(b.getXML());
		
	}
	
}
