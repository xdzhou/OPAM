package com.loic.util;

//CAS : host 157.159.10.172
//INT : host 157.159.10.180

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.loic.clientModel.ClassInfoClient;
import com.loic.config.Config;

public class TaskAgendaLoad {
	private HttpClient client;
	private ClassInfoClient error_class_info; // a error msg saved to this object
	private List<ClassInfoClient> classInfos = new ArrayList<ClassInfoClient>();
	private String userName ;
	
	String lt;
	public String rspHtml;
	HttpResponse response;

	public TaskAgendaLoad(){ 
		error_class_info = new ClassInfoClient("E", "E");
		try {
			client = HtmlUtil.getHTTPSclient();
		} catch (FailException e) {
			error_class_info.name="FailException";
			error_class_info.students=e.getMessage();
			classInfos.clear();
			classInfos.add(error_class_info);
		}
	}
	
	public void Test(String id,String mdp) throws FailException, InterruptedException{
		Boolean flag = true;
		GoToAgenda();
		fillLoginInfo(id,mdp);
		redirection();
		getAgendaHtml();	
		redirection();
		//getTWCoursPara();
		getNWCoursPara();
		//System.out.println(rspHtml);
		Thread[] threads = new Thread[classInfos.size()];
			for(int i=0; i<threads.length; i++){
 			threads[i] = new Thread(new TaskClassDetailLoad(classInfos.get(i), Config.SI_ETUDIENT_HOST, client));
 			threads[i].start();
			}
			for(int i = 0; i < threads.length; i++){
				threads[i].join();
			}
	}
	
	public List<ClassInfoClient> start(String id,String mdp) {
		if(classInfos.size() == 1) return classInfos;
		
		Boolean flag = true;
		try {
			GoToAgenda();
			if(!rspHtml.contains("Central Authentication Service")){
 				flag = false;
 			}
			while(flag){
				fillLoginInfo(id,mdp);		
	 			redirection();
	 			if(!rspHtml.contains("Central Authentication Service")){
	 				flag=false;
	 			}
			} 		
 			getAgendaHtml();	
 			redirection();
 			getTWCoursPara();
 			getNWCoursPara();
 			
 			Thread[] threads = new Thread[classInfos.size()];
 			for(int i=0; i<threads.length; i++){
	 			threads[i] = new Thread(new TaskClassDetailLoad(classInfos.get(i), Config.SI_ETUDIENT_HOST, client));
	 			threads[i].start();
 			}
 			for(int i = 0; i < threads.length; i++){
 				threads[i].join();
 			}
 				  
		} catch (Exception e) {
			//we put the error message to the cours
			error_class_info.name="FailException";
			error_class_info.students=e.getMessage();
			classInfos.clear();
			classInfos.add(error_class_info);
			return classInfos;
		} finally {
			client.getConnectionManager().shutdown();
		}
		return classInfos;
	}
	
	private void GoToAgenda() throws FailException{
		try {
			HttpGet httpGet = new HttpGet(Config.SI_ETUDIENT_HOST);
			response = client.execute(httpGet);
			
			HttpEntity entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();
			
			if(status==200){
				rspHtml = EntityUtils.toString(entity);
				httpGet.abort();
			}else{
				throw new FailException("Get host failed: not 200");
			}
		} catch (Exception e) {
			System.out.println("GoToAgenda Exception: "+e.getMessage());
			throw new FailException("GoToAgenda cause exception:"+e.getMessage());
		}
	}
	
	private void fillLoginInfo(String id, String mdp) throws FailException{
		try {
			String postUrl = HtmlUtil.findAttributValueByID(rspHtml,"fm1","action");
			if(!postUrl.startsWith("http")){
				postUrl=Config.CAS_HOST+postUrl;
			}
			HttpPost httpPost = new HttpPost(postUrl);
			//
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("username", id));
			data.add(new BasicNameValuePair("password", mdp));
			data.add(new BasicNameValuePair("warn", "true"));
			data.add(new BasicNameValuePair("lt", HtmlUtil.findInputValue(rspHtml, "lt")));
			data.add(new BasicNameValuePair("_eventId", "submit"));
			data.add(new BasicNameValuePair("submit", "LOGIN"));
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			response = client.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();			
			
			if(status==302){
				HttpEntity entity = response.getEntity();
				//rspHtml = EntityUtils.toString(entity);
				InputStream stream = entity.getContent();
				BufferedReader br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();
				
				httpPost.abort();
				String url = response.getFirstHeader("Location").getValue();
				//userid=User.insert(id, mdp);
			}else{
				System.out.println("Authentification failed:");
				throw new FailException("Authentification failed:"+id+"/"+mdp);
			}			
		} catch (Exception e) {
			System.out.println("login cause exception:"+e.toString());
			throw new FailException("login cause exception:"+e.toString());
		}
	}
	
	private void redirection() throws FailException{
		try {
			String url = response.getFirstHeader("Location").getValue();
			if(url == null){
				throw new FailException("redirection failed: no url");
			}else{
				if(!url.startsWith("http")){
					url=Config.SI_ETUDIENT_HOST+url;
				}
				HttpGet httpGet = new HttpGet(url);
				response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				//rspHtml = EntityUtils.toString(entity);
				InputStream stream = entity.getContent();
				BufferedReader br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();
				
				httpGet.abort();
			}			
		} catch (Exception e) {
			System.out.println("redirection cause exception:");
			throw new FailException("redirection cause exception:"+e.toString());
		}
	}
	
	private void getAgendaHtml() throws FailException {
		try {
			HttpGet httpGet = new HttpGet(Config.SI_ETUDIENT_HOST+"OpDotnet/commun/Login/aspxtoasp.aspx?");
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			rspHtml = EntityUtils.toString(entity);		
			httpGet.abort();
			
			HttpPost httpPost = new HttpPost(Config.SI_ETUDIENT_HOST+"/commun/aspxtoasp.asp");
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("url", "/Eplug/Agenda/Agenda.asp?IdApplication=190"));
			data.add(new BasicNameValuePair("TypeAcces", "Utilisateur"));
			data.add(new BasicNameValuePair("__IdAppliSource", ""));
			data.add(new BasicNameValuePair("session_Culture", "fr-FR"));
			data.add(new BasicNameValuePair("session_AccesPublic", "0"));
			data.add(new BasicNameValuePair("session_IdLangue", "1"));
			data.add(new BasicNameValuePair("session_IdCommunaute", "2"));
			data.add(new BasicNameValuePair("session_DataLangue", "http://157.159.10.180/dataop/langue"));			
			data.add(new BasicNameValuePair("session_DataOp", "/DataOp/2/"));
			data.add(new BasicNameValuePair("session_Espaces", "Espaces"));
			data.add(new BasicNameValuePair("session_IdUser", HtmlUtil.findAttributValueByID(rspHtml, "session_IdUser", "value")));
			String session_Utilisateur = HtmlUtil.findAttributValueByID(rspHtml, "session_Utilisateur", "value");
			String[] temp = session_Utilisateur.split("-");
			userName = temp[temp.length-1];
			userName.replace("Titre:", "");
			userName = userName.trim();
			data.add(new BasicNameValuePair("session_Utilisateur", session_Utilisateur));
			data.add(new BasicNameValuePair("session_FeuilleCss", HtmlUtil.findAttributValueByID(rspHtml, "session_FeuilleCss", "value")));
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			response = client.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();

			if(status==302){
				entity = response.getEntity();
				rspHtml = EntityUtils.toString(entity);							
				httpPost.abort();
			}else{
				System.out.println("getAgendaHtml failed: not 302");
				throw new FailException("getAgendaHtml failed: not 302");
			}			
		} catch (Exception e) {
			System.out.println("getAgendaHtml cause exception:");
			throw new FailException("getAgendaHtml cause exception:"+e.toString());
		}
	}
	
	private void getTWCoursPara(){
		Document doc = Jsoup.parse(rspHtml);
		Elements elements = doc.getElementsByAttribute("onmouseover");
		Pattern pattern = Pattern.compile("[0-9]+");
		for(Element ele : elements){
			Matcher matcher = pattern.matcher(ele.attr("onmouseover"));
			ClassInfoClient c = new ClassInfoClient();
			if(matcher.find()){
				c.NumEve = matcher.group();
			}
			if(matcher.find()){
				c.dateSrc = matcher.group();
			}
			classInfos.add(c);
		}
		
	}
	
	private void getNWCoursPara() throws FailException{
		try {
//			Calendar c = TimeUtil.getCalendarParis();
//			c.add(Calendar.WEEK_OF_YEAR, 1);
//			DateFormat df = new SimpleDateFormat("yyyyMMdd");
//			String NumDat = df.format(c.getTime());
			String NumDat = "20140606";
			
			HttpPost httpPost = new HttpPost(Config.SI_ETUDIENT_HOST+"/Eplug/Agenda/Agenda.asp");
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("NumDat", NumDat));	
			
			data.add(new BasicNameValuePair("DebHor", HtmlUtil.findInputValue(rspHtml, "DebHor")));
			data.add(new BasicNameValuePair("FinHor", HtmlUtil.findInputValue(rspHtml, "FinHor")));
			data.add(new BasicNameValuePair("ValGra", HtmlUtil.findInputValue(rspHtml, "ValGra")));			
			data.add(new BasicNameValuePair("NomCal", HtmlUtil.findInputValue(rspHtml, "NomCal")));
			data.add(new BasicNameValuePair("NumLng", HtmlUtil.findInputValue(rspHtml, "NumLng")));
			data.add(new BasicNameValuePair("FromAnn", HtmlUtil.findInputValue(rspHtml, "FromAnn")));
			data.add(new BasicNameValuePair("MLG_BOX21", HtmlUtil.findInputValue(rspHtml, "MLG_BOX21")));
			data.add(new BasicNameValuePair("MLG_BOX22", HtmlUtil.findInputValue(rspHtml, "MLG_BOX22")));
			data.add(new BasicNameValuePair("MLG_BOX23", HtmlUtil.findInputValue(rspHtml, "MLG_BOX23")));					
			
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			response = client.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			if(status==200){
				HttpEntity entity = response.getEntity();
				rspHtml = EntityUtils.toString(entity);				
				httpPost.abort();
			}else{
				System.out.println("getAgendaHtml failed: not 302");
				throw new FailException("getAgendaHtml failed: not 302");
			}			
		} catch (Exception e) {
			System.out.println("cause exception:");
			throw new FailException("cause exception:"+e.toString());
		}
		//redirection();
		getTWCoursPara();
	}
}
