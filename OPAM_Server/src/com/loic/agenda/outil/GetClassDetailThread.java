package com.loic.agenda.outil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.loic.agenda.model.Cours;

public class GetClassDetailThread implements Runnable {
	Cours cours = null;
	String host;
	HttpClient client;
	
	public GetClassDetailThread(Cours c, String host, HttpClient client){
		cours = c;
		this.host = host;
		this.client = client;
	}
	
	@Override
	public void run(){
		String html;
		try {
			html = getCoursDetail(cours);
			chargerCours(cours, html);
		} catch (FailException e) {
			System.out.println("GetClassDetailThread cause exception:");
		}
		
	}
	
	private String getCoursDetail(Cours c) throws FailException{
		HttpGet httpGet = new HttpGet(host+"Eplug/Agenda/Eve-Det.asp?NumEve="+c.NumEve+"&DatSrc="+c.dateSrc);
		try {
			HttpResponse response = client.execute(httpGet);		
			int status = response.getStatusLine().getStatusCode();
			if(status==200){
				HttpEntity entity = response.getEntity();
				//rspHtml = EntityUtils.toString(entity);
				InputStream stream = entity.getContent();
				BufferedReader br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				String rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();				
				httpGet.abort();
				System.out.println(rspHtml);
				return rspHtml;
			}else{
				System.out.println("GetCoursDetail failed: status "+status);
				throw new FailException("GetCoursDetail failed: status "+status);
			}		
		} catch (Exception e) {
			System.out.println("getCoursDetail cause exception:");
			throw new FailException("getCoursDetail cause exception:"+e.toString());
		}
		
	}
	
	private void chargerCours(Cours c, String rspHtml) throws FailException{			
		String page = rspHtml.replaceAll("<[^>]+>", "__");
		page = sansAccent(page);
		page = page.replace(" ", " ");
		page = page.replaceAll("_[ _:]+_", "__");	
		
		//System.out.println(page);
		
		Pattern pattern;
		if(page.contains("Formateur")){
			pattern = Pattern.compile("__([^_]+)__Type__ ([^_]+)__Etat__([^_]+)__Date\\(s\\)__([^_]+)__Debut__ : ([^_]+)__Fin__ : ([^_]+)__Auteur__(.+)__Formateur__(.+)__Apprenants__(.+)__Projets__([^_]+)__Groupes de personnes__([^_]+)__(.*)'\\);__");
		}else {
			pattern = Pattern.compile("__([^_]+)__Type__ ([^_]+)__Etat__([^_]+)__Date\\(s\\)__([^_]+)__Debut__ : ([^_]+)__Fin__ : ([^_]+)__Auteur__(.+)__Apprenants__(.+)__Projets__([^_]+)__Groupes de personnes__([^_]+)__(.*)'\\);__");
		}		
		Matcher matcher = pattern.matcher(page);
		if(matcher.find()){
			int offset=0;
			if(matcher.groupCount()==11){offset=1;}
			
			c.type=matcher.group(2);
			c.type=c.type.replace("\\", "");
			if(c.type.contains("Examen")){
				 if(page.contains("Controle Final 2")){
					 c.type+=" CF2";
				 }else {
					 c.type+=" CF1";
				}
			 }
			if(c.type.contains("Point de Rencontre")) c.type="Point de Rencontre";
			c.debut=c.dateSrc+" "+matcher.group(5);
			c.fin=c.dateSrc+" "+matcher.group(6);
			c.position=getPosition(c.debut);
			c.auteur=matcher.group(7);
			//c.auteur="";
			c.formateur = (offset==1) ? "" :matcher.group(8);

			//c.formateur="";
			c.name=matcher.group(10-offset);
			c.name=c.name.replace("\\", "");
			//Caractères spéciaux HTML
			c.name=c.name.replace("&amp;", "et");  
			c.group=matcher.group(11-offset);
			if(c.group.contains("Gp-EI1-G")){
				c.apprenants = "all the students of 1st year";
			}else if (c.group.contains("Gp-EI2-G")) {
				c.apprenants = "all the students of 2nd year";
			}else if (c.group.contains("Gp-EI3-G")) {
				c.apprenants = "all the students of 3rd year";
			}else {
				c.apprenants=matcher.group(9-offset);
			}
			
			c.salle = matcher.group(12-offset);			
			if(c.salle.equals("")){
				c.salle = getSalleFromTitle( matcher.group(1) );
			}else{
				//可能group有好几组，此情况下，除第一个group外，其他都在salle里
				String[] groupsalle = c.salle.split("__");
				int len = groupsalle.length;
				if(groupsalle[len-1].startsWith("Gp")){
					for(int i=0; i<len; i++) c.group+=("__"+groupsalle[i]);
					c.salle = getSalleFromTitle( matcher.group(1) );
				}else {
					for(int i=0; i<len-1; i++) c.group+=("__"+groupsalle[i]);
					c.salle = groupsalle[len-1];
				}
			}
			if(c.salle.endsWith("__")){
				c.salle=c.salle.substring(0, c.salle.length()-2);
			}
		}	
	}
	
	private String getPosition(String sdata) throws FailException{
		String position ;
		SimpleDateFormat sdf=new  SimpleDateFormat("yyyyMMdd HH:mm");
		Date date;
		try {
			date = sdf.parse(sdata);
			sdf.applyPattern("w");
			position=sdf.format(date);
			position+="_";
		} catch (ParseException e) {
			throw new FailException("Format of date isn't correct:");
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int xq = c.get(Calendar.DAY_OF_WEEK);
		if(xq==1){
			position+=7;
		}else {
			xq--;
			position+=xq;
		}
		return position;
	}
	
	private String sansAccent(String s) {	 
        String strTemp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(strTemp).replaceAll("");
    }
	
	private String getSalleFromTitle(String msg){
		String[] rooms = msg.split("-");
		String lastMsg = rooms[rooms.length-1]; //获取salle可能存在的地方的string
		
		Pattern pt = Pattern.compile("salle ([^_]+)",Pattern.CASE_INSENSITIVE); //不区分大小写//可能存在关键字salle
		Matcher match_salle = pt.matcher(lastMsg);
		
		if(match_salle.find()) return match_salle.group(1);
		else {
			pt = Pattern.compile("en ([^_]+)",Pattern.CASE_INSENSITIVE); //不区分大小写//可能存在关键字en
			match_salle = pt.matcher(lastMsg);
			if(match_salle.find()) return match_salle.group(1);
		}
		return "";
	}

}
