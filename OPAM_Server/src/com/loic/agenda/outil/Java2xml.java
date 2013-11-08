package com.loic.agenda.outil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.loic.agenda.model.Cours;

public class Java2xml {
	String xml="no data";

	public Java2xml(List<Cours> cours, String username) throws IOException {
		Element root =new Element("list");
		Document doc =new Document(root);
		//xml id = 1 : didn't find the class
		if(cours==null || cours.size()==0){
			root.setAttribute("id", "1");
		}
		//xml id = 2 : error found
		else if (cours.size()==1 && cours.get(0).name.equals("FailException")) {
			root.setAttribute("id", "2");
		}
		//xml id = 0 : normal
		else {
			root.setAttribute("id", "0");
		}
		
		root.setAttribute("numweek", ""+getNumWeek());
		root.setAttribute("username", username);
				
		for (Cours c : cours) {
			Element elements =new Element("cour");
			elements.addContent(new Element("name").setText(c.name));
			elements.addContent(new Element("type").setText(c.type));
			elements.addContent(new Element("position").setText(c.position));
			elements.addContent(new Element("debut").setText(c.debut.toString()));
			elements.addContent(new Element("fin").setText(c.fin.toString()));
			elements.addContent(new Element("auteur").setText(c.auteur));
			elements.addContent(new Element("formateur").setText(c.formateur));
			elements.addContent(new Element("apprenant").setText(c.apprenants));
			elements.addContent(new Element("group").setText(c.group));
			elements.addContent(new Element("salle").setText(c.salle));
			root.addContent(elements);
		}
		ByteArrayOutputStream byteRsp=new ByteArrayOutputStream(); 
		Format format = Format.getCompactFormat();   
	    format.setEncoding("UTF-8");   
	    format.setIndent("  ");
		XMLOutputter XMLOut = new XMLOutputter(format);
		XMLOut.output(doc, byteRsp); 
		xml=byteRsp.toString();
	}
	
	public String getXML(){
		return xml;
	}
	
	private int getNumWeek(){		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		int xq = c.get(Calendar.DAY_OF_WEEK);
		if(xq==1){
			c.set(Calendar.DATE, c.get(Calendar.DATE)-1);
		}
		SimpleDateFormat sdf = new  SimpleDateFormat();
		sdf.applyPattern("w");
		return Integer.parseInt(sdf.format(c.getTime()));

	}
}
