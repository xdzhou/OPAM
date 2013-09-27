package com.loic.agenda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.parser.ParseException;

import com.loic.agenda.model.Cours;
import com.loic.agenda.outil.NewEssai;

public class Main {

	public static void main(String[] args) throws ParseException, IOException{

		//System.out.println(Chiffrement.encrypt("whoami?", "OPAM"));
	    
	    File file = new File("class.txt");
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    String rspHtml = br.readLine();
	    
	    String page = rspHtml.replaceAll("<[^>]+>", "_");
	    page = sansAccent(page);
		page = page.replace(" ", " ");
		page = page.replaceAll("_[ _:]+_", "_");
		
		Pattern pattern;
		if(page.contains("Formateur")){
			pattern = Pattern.compile("_Type_ ([^_]+)_Etat_([^_]+)_Date\\(s\\)_([^_]+)_Debut_ : ([^_]+)_Fin_ : ([^_]+)_Auteur_(.+)_Formateur_(.+)_Apprenants_(.+)_Projets_([^_]+)_Groupes de personnes_([^_]+)_(.*)'\\);_");
		}else {
			pattern = Pattern.compile("_Type_ ([^_]+)_Etat_([^_]+)_Date\\(s\\)_([^_]+)_Debut_ : ([^_]+)_Fin_ : ([^_]+)_Auteur_(.+)_Apprenants_(.+)_Projets_([^_]+)_Groupes de personnes_([^_]+)_(.*)'\\);_");
		}
		
		Matcher matcher = pattern.matcher(page);
		if(matcher.find()){
			System.out.println(matcher.groupCount());
			for(int i=1 ; i<=11; i++){
				System.out.println(matcher.group(i));
			}
			
		}else {
			System.out.println("fail");
		}
		
		
	}
	
	private static String sansAccent(String s) {	 
        String strTemp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(strTemp).replaceAll("");
  }
	
}
