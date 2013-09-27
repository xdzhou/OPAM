package com.loic.agenda.outil;


//CAS : host 157.159.10.172
//INT : host 157.159.10.180

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.retep.nosockHttpClient.GAEConnectionManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.loic.agenda.model.Cours;

public class Essai {
	String lt;
	int status;
	public String rspHtml;
	final String host = "http://si-etudiants.tem-tsp.eu/";
	HttpClient client;
	HttpResponse response;
	HttpEntity entity;
	HttpGet httpGet;
	HttpPost httpPost;
	List<Cours> cours = new ArrayList<Cours>();
	Cours c;
	public String userName="";

	public Essai() { 
		ClientConnectionManager connectionManager = new GAEConnectionManager();  
		client = new InsecureHttpClientFactory().buildHttpClient(connectionManager);
		c = new Cours("E", "E");
	}
	
	public List<Cours> start(String id,String mdp) throws UnsupportedEncodingException {
		Boolean flag = true;
		try {
			GoToAgenda();
			if(!rspHtml.contains("Central Authentication Service")){
 				flag=false;
 			}
			while(flag){
				login(id,mdp);		
	 			redirection();
	 			if(!rspHtml.contains("Central Authentication Service")){
	 				flag=false;
	 			}
			} 		
 			getAgendaHtml();	
 			redirection();
 			userName=getUserName();
 			getTWCoursPara();
 			getNWCoursPara();
 			for(Cours c : cours){
 			//c=cours.get(0);//
 				getCoursDetail(c);
 				chargerCours(c);
 			}
		} catch (FailException e) {
			//we put the error message to the cours
			c.name="FailException";
			c.type=e.toString();
			c.type=c.type.replace("com.loic.agenda.outil.FailException:", "");
			c.position = "49_1";
			cours.clear();
			cours.add(c);
			return cours;
		} finally {
			client.getConnectionManager().shutdown();
		}
		return cours;
	}
	
	private void GoToAgenda() throws FailException{
		try {
			httpGet = new HttpGet(host);
			response = client.execute(httpGet);
			
			entity = response.getEntity();
			status = response.getStatusLine().getStatusCode();
			
			if(status==200){
				//rspHtml = EntityUtils.toString(entity);
				InputStream stream = entity.getContent();
				BufferedReader br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();
				httpGet.abort();
			}else{
				throw new FailException("Get host failed: not 200");
			}
		} catch (Exception e) {
			System.out.println("status:"+status+"  url:"+httpGet.getURI().toString());
			throw new FailException("GoToAgenda cause exception:"+e.toString());
		}
	}
	
	private void login(String id, String mdp) throws FailException{
		try {
			String postUrl = chercherValue("action");
			if(!postUrl.startsWith("http")){
				postUrl="https://cas.tem-tsp.eu"+postUrl;
			}
			httpPost = new HttpPost(postUrl);
			//
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("username", id));
			data.add(new BasicNameValuePair("password", mdp));
			data.add(new BasicNameValuePair("warn", "true"));
			data.add(new BasicNameValuePair("lt", chercherValue("lt")));
			data.add(new BasicNameValuePair("_eventId", "submit"));
			data.add(new BasicNameValuePair("submit", "LOGIN"));
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			response = client.execute(httpPost);
			status = response.getStatusLine().getStatusCode();
			
			
			if(status==302){
				entity = response.getEntity();
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
					url=host+url;
				}
				httpGet = new HttpGet(url);
				response = client.execute(httpGet);
				entity = response.getEntity();
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
			httpGet = new HttpGet(host+"OpDotnet/commun/Login/aspxtoasp.aspx?");
			response = client.execute(httpGet);
			entity = response.getEntity();
			//rspHtml = EntityUtils.toString(entity);
			InputStream stream = entity.getContent();
			BufferedReader br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
			rspHtml="";
			for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
			stream.close();
			
			httpGet.abort();
			
			httpPost = new HttpPost(host+"/commun/aspxtoasp.asp");
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
			data.add(new BasicNameValuePair("session_IdUser", chercherValue("session_IdUser")));
			data.add(new BasicNameValuePair("session_Utilisateur", chercherValue("session_Utilisateur")));
			data.add(new BasicNameValuePair("session_FeuilleCss", chercherValue("session_FeuilleCss")));
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			response = client.execute(httpPost);
			status = response.getStatusLine().getStatusCode();
			
			
			if(status==302){
				entity = response.getEntity();
				//rspHtml = EntityUtils.toString(entity);
				stream = entity.getContent();
				br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();								
				
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
	
	private String chercherValue(String name) throws FailException{
		Pattern pattern = Pattern.compile("name=\""+name+"\" value=\"([^\"]+)\"");		
		if(name.equals("action")){
			pattern = Pattern.compile("action=\"([^\"]+)\"");
		}
		Matcher matcher = pattern.matcher(rspHtml);
		if(matcher.find()){
			  return matcher.group(1);
		}else {
			System.out.println("chercherValue failed: "+name);
			System.out.println("1"+rspHtml);
			throw new FailException("chercherValue failed: no value found");
		}
	}
	
	private String getUserName() throws FailException{
		Pattern pattern = Pattern.compile("Agenda de l'utilisateur ([^ ]+) ([^ ]+) ");
		Matcher matcher = pattern.matcher(rspHtml);
		if(matcher.find()){
			return matcher.group(1)+" "+matcher.group(2);
		}else {
			throw new FailException("user name not found:");
		}
		
	}
	
	private void getTWCoursPara(){		
		Pattern pattern = Pattern.compile("onmouseover=\"DetEve\\(\'([0-9]+)\',\'([^']+)\',\'([0-9]+)\'\\)");
		Matcher matcher = pattern.matcher(rspHtml);
		while(matcher.find()){
			 Cours c = new Cours(matcher.group(1), matcher.group(3));
			 cours.add(c);
		}
		
	}
	
	private void getNWCoursPara() throws FailException{
		Pattern pattern = Pattern.compile("onclick=\"NavDat\\(\'([0-9]+)\'\\)");
		Matcher matcher = pattern.matcher(rspHtml);
		matcher.find();
		matcher.find();
		String NumDat = matcher.group(1);
		try {
			httpPost = new HttpPost(host+"/Eplug/Agenda/Agenda.asp");
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("NumDat", NumDat));
			data.add(new BasicNameValuePair("DebHor", chercherValue("DebHor")));
			data.add(new BasicNameValuePair("FinHor", chercherValue("FinHor")));
			data.add(new BasicNameValuePair("ValGra", chercherValue("ValGra")));			
			data.add(new BasicNameValuePair("NomCal", chercherValue("NomCal")));
			data.add(new BasicNameValuePair("NumLng", chercherValue("NumLng")));
			data.add(new BasicNameValuePair("FromAnn", chercherValue("FromAnn")));
			data.add(new BasicNameValuePair("MLG_BOX21", "Etes vous sur de vouloir supprimer les évènements cochés ?"));
			data.add(new BasicNameValuePair("MLG_BOX22", "Etes vous sur de vouloir supprimer cette occurence ?"));
			data.add(new BasicNameValuePair("MLG_BOX23", "Etes vous sur de vouloir supprimer cet évènement ?"));
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			response = client.execute(httpPost);
			status = response.getStatusLine().getStatusCode();
			if(status==200){
				entity = response.getEntity();
				//rspHtml = EntityUtils.toString(entity);
				InputStream stream = entity.getContent();
				BufferedReader br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();
				
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
	
	private void getCoursDetail(Cours c) throws FailException{
		httpGet = new HttpGet(host+"Eplug/Agenda/Eve-Det.asp?NumEve="+c.NumEve+"&DatSrc="+c.dateSrc);
		try {
			response = client.execute(httpGet);		
			status = response.getStatusLine().getStatusCode();
			if(status==200){
				entity = response.getEntity();
				//rspHtml = EntityUtils.toString(entity);
				InputStream stream = entity.getContent();
				BufferedReader br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();	
				
				httpGet.abort();
			}else{
				System.out.println("GetCoursDetail failed: status "+status);
				throw new FailException("GetCoursDetail failed: status "+status);
			}		
		} catch (Exception e) {
			System.out.println("getCoursDetail cause exception:");
			throw new FailException("getCoursDetail cause exception:"+e.toString());
		}
		
	}
	
	private void chargerCours(Cours c) throws FailException, UnsupportedEncodingException{
		String titreExamen;
		if(rspHtml.contains("Contrôle Final 2")){
			titreExamen="CF2";
		}else if (rspHtml.contains("Contrôle Final 1")) {
			titreExamen="CF1";
		}else {
			titreExamen="";
		}
		Pattern pattern = Pattern.compile("<B>Type<\\\\/B> : (.+)<B>Etat<\\\\/B> : (.+)<B>Début<\\\\/B> : (.+)<B>Fin<\\\\/B> : (.+)<B>Auteur<\\\\/B> : (.+)<B>Apprenants<\\\\/B> : (.+)<B>Projets<\\\\/B> : (.+)<B>Groupes de personnes<\\\\/B> : (.+)");
		Matcher matcher = pattern.matcher(rspHtml);
		
//		String str = rspHtml.replaceAll("<[^>]+>", "&");
//		System.out.println(str);
		
		if(matcher.find()){	
			 Pattern p = Pattern.compile("<(.+?)>", Pattern.DOTALL);
			 Pattern p1 = Pattern.compile("_([^_]+)_");
			 Pattern p3 = Pattern.compile(">([^<]+)<\\\\");
			 String resulta="";

			 Matcher m = p.matcher(matcher.group(1));
			 c.type=m.replaceAll("").trim();
			 c.type=sansAccent(c.type);
			 c.type=c.type.replace(" ", " ");	 
			 c.type=c.type.replace("\\", "");
			 if(c.type.contains("Examen")){
				 c.type+=" "+titreExamen;
			 }
			 
			 m = p.matcher(matcher.group(3));
			 String debut = m.replaceAll("");
			 debut=debut.replace(" ", "");
			 c.debut=c.dateSrc+" "+debut;
			 
			 //calculer position
			 SimpleDateFormat sdf=new  SimpleDateFormat("yyyyMMdd HH:mm");
			 try {
				Date date = sdf.parse(c.debut);
				sdf.applyPattern("w");
				c.position+=sdf.format(date);
				c.position+="_";
				c.position+=getDayWeek(date);
			} catch (ParseException e) {
				System.out.println(rspHtml);
				throw new FailException("Format of date isn't correct:");
			}
			 
			 m = p.matcher(matcher.group(4));
			 String fin = m.replaceAll("");
			 fin=fin.replace(" ", "");
			 c.fin=c.dateSrc+" "+fin;
			 
			 String mp = matcher.group(5);
			 mp=mp.replace(" : ", "");
			 Matcher m1 = p3.matcher(mp);
			 Boolean flag=false;
			 while(m1.find()){
				 if(flag) resulta+="_";
				 if(!m1.group(1).contains("Formateur")){
					 resulta+=m1.group(1);
				 }else {
					break;
				}				 
				 flag=true;
			 }
			 c.auteur=resulta.substring(0,resulta.length()-1);
			 c.auteur=sansAccent(c.auteur);
			 c.auteur=c.auteur.replace(" ", " ");
			 
			 resulta="";
			 flag=false;
			 while(m1.find()){
				 if(flag) resulta+="_";
				 resulta+=m1.group(1);				 				 
				 flag=true;
			 }
			 c.formateur=resulta;
			 c.formateur=sansAccent(c.formateur);
			 c.formateur=c.formateur.replace(" ", " ");
			 resulta="";
			 
			 if(c.type.contains("TP") || c.type.contains("TD") || c.type.contains("Cours Integre")){
				 m = p.matcher(matcher.group(6));
				 mp = m.replaceAll("_");
				 m1 = p1.matcher(mp);
				 flag=false;
				 while(m1.find()){
					 if(flag) resulta+="_";
					 resulta+=m1.group(1);
					 flag=true;
				 }
				 c.apprenants=resulta;
				 c.apprenants=sansAccent(c.apprenants);
				 c.apprenants=c.apprenants.replace(" ", " ");
				 resulta="";
			 }else{
				 c.apprenants="all the students";
			 }

			 m = p.matcher(matcher.group(7));
			 c.name=m.replaceAll("");
			 c.name=sansAccent(c.name);
			 c.name=c.name.replace(" ", " ");
			 c.name=c.name.replace("\\", "");

			 Pattern p2 = Pattern.compile(">([^<]+)<\\\\/A>");
			 m=p2.matcher(matcher.group(8));
			 if(m.find()){
				 c.group=m.group(1);
			 }
			 flag=false;
			 while(m.find()){
				 if(flag) c.salle+="_";
				 c.salle+=m.group(1);
				 flag=true;
			 }
			 c.salle=c.salle.replace(" ", " ");
			 
			if(c.name.equals("") || c.type.equals("")|| c.auteur.equals("")|| c.group.equals("")){
				System.out.println(rspHtml);
				throw new FailException("Charge of cours failed:");
			}
			 
		}else {
//			str = rspHtml.replaceAll("<[^>]+>", "");
//			System.out.println(str);
		}
	}
	
	private int getDayWeek(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int xq = c.get(Calendar.DAY_OF_WEEK);
		if(xq==1){
			return 7;
		}else {
			return xq-1;
		}
	}
	
	private String sansAccent(String s) {	 
        String strTemp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(strTemp).replaceAll("");
  }

}