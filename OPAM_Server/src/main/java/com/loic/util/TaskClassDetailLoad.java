package com.loic.util;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.loic.clientModel.ClassInfoClient;

public class TaskClassDetailLoad implements Runnable {
	private ClassInfoClient classInfo = null;
	private String host;
	private HttpClient client;
	private String specialString;
	
	public TaskClassDetailLoad(ClassInfoClient c, String host, HttpClient client){
		classInfo = c;
		this.host = host;
		this.client = client;
		byte[] bytes = {-62, -96};
		try {
			specialString = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		String html;
		try {
			html = getCoursDetail(classInfo);
			//System.out.println(html);
			chargerCours(classInfo, html);
		} catch (Exception e) {
			System.out.println("GetClassDetailThread E: "+e.getMessage());
		}
		
	}
	
	private String getCoursDetail(ClassInfoClient c) throws FailException{
		HttpGet httpGet = new HttpGet(host+"Eplug/Agenda/Eve-Det.asp?NumEve="+c.NumEve+"&DatSrc="+c.dateSrc);
		try {
			HttpResponse response = client.execute(httpGet);		
			int status = response.getStatusLine().getStatusCode();
			if(status==200){
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity);
			}else{
				System.out.println("GetCoursDetail failed: status "+status);
				throw new FailException("GetCoursDetail failed: status "+status);
			}		
		} catch (Exception e) {
			System.out.println("getCoursDetail cause exception:");
			throw new FailException("getCoursDetail cause exception:"+e.getMessage());
		}
		
	}
	
	private void chargerCours(ClassInfoClient c, String rspHtml) throws Exception{		
		Document doc = Jsoup.parse(rspHtml);
		Elements scriptElements = doc.getElementsByTag("SCRIPT");
		String scriptContent = scriptElements.get(scriptElements.size()-1).data();
		scriptContent = scriptContent.replace("\\", "");
		doc = Jsoup.parse(scriptContent);
		Elements fontElements = doc.getElementsByTag("FONT");
		for(Element ele: fontElements){
			ele.append("_"); //给学生名字加上标记
		}
		Elements aElements = doc.getElementsByTag("A");
		for(Element ele: aElements){
			if(ele.attr("onclick").contains("VisOrg")) ele.append("_"); //给 Group 加上标记
		}
		Elements TDele = doc.getElementsByTag("TD");		
		String roomNameMayBe = null;
		for(int i=0; i< TDele.size(); i++){
			String s = getContentFromTDelement(TDele.get(i));
			if(!StringUtils.isEmpty(s)){			
				if(i==0) {
					String[] spliteString = s.split("-");
					c.name = spliteString[0].trim();
					//获取salle可能存在的地方的string
					roomNameMayBe = spliteString[spliteString.length-1].trim(); 
				}
				if(s.contains("Type")){
					c.classType.name = getContentFromTDelement(TDele.get(++i));
				}else if (s.contains("Date")) {
					String dateString = getContentFromTDelement(TDele.get(++i));
					SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
					Date date = dateFormatter.parse(dateString);				
					c.weekOfYear = TimeUtil.getNumWeek(date);
					c.dayOfWeek = TimeUtil.getDayOfWeek(date);
				}else if (s.contains("Début")) {
					c.startTime = s.replace("Début :", "").trim();
				}else if (s.contains("Fin")) {
					c.endTime = s.replace("Fin :", "").trim();
				}else if (s.contains("Auteur")) {
					String auteur = getContentFromTDelement(TDele.get(++i));
					if(auteur.endsWith("_")) auteur = auteur.substring(0, auteur.length()-1);
					c.auteur = auteur.replace("_ ", "_");
				}
				else if (s.contains("Formateur")) {
					String formateur = getContentFromTDelement(TDele.get(++i));
					if(formateur.endsWith("_")) formateur = formateur.substring(0, formateur.length()-1);
					c.teacher = formateur.replace("_ ", "_");
				}else if (s.contains("Apprenants")) {
					String students = getContentFromTDelement(TDele.get(++i));
					if(students.endsWith("_")) students = students.substring(0, students.length()-1);
					c.students = students.replace("_ ", "_");
				}else if (s.endsWith("_")) {
					c.groupe += getContentFromTDelement(TDele.get(i));
				}else {
					c.room.name = s.split(" ")[0];
				}
			}	
		}		
		if(c.groupe.endsWith("_")) c.groupe = c.groupe.substring(0,c.groupe.length()-1);
		if(StringUtils.isEmpty(c.room.name)) c.room.name = getSalleFromTitle(roomNameMayBe);		
		System.out.println(c);
	}
	
	private String getContentFromTDelement(Element e){
		String s = Jsoup.parse(e.html()).text();
		s = s.replace(specialString, " ").trim();
		return s;
	}
	
	private String getSalleFromTitle(String msg){	
		Pattern pt = Pattern.compile("(salle|en|Amphi) ([^_]+)",Pattern.CASE_INSENSITIVE); //不区分大小写//可能存在关键字salle, en, Amphi
		Matcher match_salle = pt.matcher(msg);	
		if(match_salle.find()) {
			return match_salle.group(2);
		}
		else return "";
	}

}
