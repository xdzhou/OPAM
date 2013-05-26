package com.sky.opam.outil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import android.util.Xml;

import com.sky.opam.model.Cours;
import com.sky.opam.model.DataCompo;

public class PullXMLReader {
	public static DataCompo readXML(InputStream inStream){
		XmlPullParser parser = Xml.newPullParser();
		int id = -1;
		int numweek = -1;
		String username = "";
		try {
			parser.setInput(inStream, "UTF-8");
			int eventType = parser.getEventType();
			Cours c = null;			
			List<Cours> cours = null;
			//SimpleDateFormat sdf=new  SimpleDateFormat("yyyyMMdd HH:mm");
			
			while(eventType!=XmlPullParser.END_DOCUMENT){
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					cours = new ArrayList<Cours>();
					break;
				case XmlPullParser.START_TAG:
					String name = parser.getName();
					if(name.equalsIgnoreCase("list")){
						id = Integer.parseInt(parser.getAttributeValue(0));
						numweek = Integer.parseInt(parser.getAttributeValue(1));
						username = parser.getAttributeValue(2);
						System.out.println("username: "+username);
					}else if(name.equalsIgnoreCase("cour")){
						c = new Cours();
					}else if (c!=null) {
						if(name.equalsIgnoreCase("name")){
							c.name = parser.nextText();
						}else if (name.equalsIgnoreCase("type")) {
							c.type = parser.nextText();
						}else if (name.equalsIgnoreCase("debut")) {
							String s = parser.nextText();
							if(!s.equals("debut")){
								c.debut = s.substring(9, s.length());
							}	
						}else if (name.equalsIgnoreCase("position")) {
							c.position = parser.nextText();
						}else if (name.equalsIgnoreCase("auteur")) {
							c.auteur = parser.nextText();
						}else if (name.equalsIgnoreCase("formateur")) {
							c.formateur = parser.nextText();
						}else if (name.equalsIgnoreCase("fin")) {
							String s = parser.nextText();
							if(!s.equals("fin")){
								c.fin = s.substring(9, s.length());
							}
						}else if (name.equalsIgnoreCase("apprenant")) {
							c.apprenants = parser.nextText();
						}else if (name.equalsIgnoreCase("group")) {
							c.groupe = parser.nextText();
						}else {
							c.salle = parser.nextText();
						}
					}
					break;
				case XmlPullParser.END_TAG:
					if(parser.getName().equalsIgnoreCase("cour") && c!=null){
						if(!c.name.equals("")){
							if(c.salle.equals("")){c.salle="no room special";}
							cours.add(c);
						}
						c=null;
					}
					break;
				default:
					break;
				}
				eventType = parser.next();
			}
			inStream.close();
			return new DataCompo(cours, id, numweek,username);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
