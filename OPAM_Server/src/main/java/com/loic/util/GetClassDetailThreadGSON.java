package com.loic.util;

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

import com.loic.model.ClassInfo;

public class GetClassDetailThreadGSON implements Runnable {
	private ClassInfo classInfo = null;
	private String host;
	private HttpClient client;
	
	public GetClassDetailThreadGSON(ClassInfo c, String host, HttpClient client){
		classInfo = c;
		this.host = host;
		this.client = client;
	}
	
	@Override
	public void run(){
		String html;
		try {
			html = getCoursDetail(classInfo);
			chargerCours(classInfo, html);
		} catch (FailException e) {
			System.out.println("GetClassDetailThread cause exception:");
		}
		
	}
	
	private String getCoursDetail(ClassInfo c) throws FailException{
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
	
	private void chargerCours(ClassInfo c, String rspHtml) throws FailException{			
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
			
			c.classType.name = matcher.group(2);
			c.classType.name = c.classType.name.replace("\\", "");
			if(c.classType.name.contains("Examen")){
				 if(page.contains("Controle Final 2")){
					 c.classType.name+=" CF2";
				 }else {
					 c.classType.name+=" CF1";
				}
			 }
			if(c.classType.name.contains("Point de Rencontre")) c.classType.name="Point de Rencontre";
			c.startTime = matcher.group(5);
			c.endTime = matcher.group(6);
			setNumWeekDay(c, c.dateSrc+" "+ c.startTime);
			
			c.auteur=matcher.group(7);
			//c.auteur="";
			c.teacher = (offset==1) ? "" :matcher.group(8);

			//c.formateur="";
			c.name=matcher.group(10-offset);
			c.name=c.name.replace("\\", "");
			//Caractères spéciaux HTML
			c.name=c.name.replace("&amp;", "et");  
			c.groupe=matcher.group(11-offset);
			if(c.groupe.contains("Gp-EI1-G")){
				c.students = "all the students of 1st year";
			}else if (c.groupe.contains("Gp-EI2-G")) {
				c.students = "all the students of 2nd year";
			}else if (c.groupe.contains("Gp-EI3-G")) {
				c.students = "all the students of 3rd year";
			}else {
				c.students=matcher.group(9-offset);
			}
			
			c.room.name = matcher.group(12-offset);			
			if(c.room.name.equals("")){
				c.room.name = getSalleFromTitle( matcher.group(1) );
			}else{
				//可能group有好几组，此情况下，除第一个group外，其他都在salle里
				String[] groupsalle = c.room.name.split("__");
				int len = groupsalle.length;
				if(groupsalle[len-1].startsWith("Gp")){
					for(int i=0; i<len; i++) c.groupe+=("__"+groupsalle[i]);
					c.room.name = getSalleFromTitle( matcher.group(1) );
				}else {
					for(int i=0; i<len-1; i++) c.groupe+=("__"+groupsalle[i]);
					c.room.name = groupsalle[len-1];
				}
			}
			if(c.room.name.endsWith("__")){
				c.room.name=c.room.name.substring(0, c.room.name.length()-2);
			}
			c.room.name = c.room.name.split(" ")[0];
		}	
	}
	
	private void setNumWeekDay(ClassInfo c , String sdata) throws FailException{
		SimpleDateFormat sdf=new  SimpleDateFormat("yyyyMMdd HH:mm");
		Date date;
		try {
			date = sdf.parse(sdata);
			sdf.applyPattern("w");
			c.weekOfYear = Integer.parseInt(sdf.format(date));
		} catch (ParseException e) {
			throw new FailException("Format of date isn't correct:");
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		c.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	private String sansAccent(String s) {	 
        String strTemp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(strTemp).replaceAll("");
    }
	
	private String getSalleFromTitle(String msg){
		String[] rooms = msg.split("-");
		String lastMsg = rooms[rooms.length-1]; //获取salle可能存在的地方的string
		
		Pattern pt = Pattern.compile("(salle|en|Amphi) ([^_]+)",Pattern.CASE_INSENSITIVE); //不区分大小写//可能存在关键字salle, en, Amphi
		Matcher match_salle = pt.matcher(lastMsg);
		
		if(match_salle.find()) return match_salle.group(2);
		else return "";
	}

}
