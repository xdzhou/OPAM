package com.sky.opam.outil;


//CAS : host 157.159.10.172
//INT : host 157.159.10.180

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import com.sky.opam.model.Cours;

public class Navigateur {
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

	public Navigateur() { 
		HttpParams httpParams = new BasicHttpParams();
		String userAgent = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11";
        HttpProtocolParams.setUserAgent(httpParams, userAgent);  
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
        
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("https",SSLSocketFactory.getSocketFactory(), 443));
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        SingleClientConnManager mgr = new SingleClientConnManager(httpParams, schemeRegistry);
        
        client = new DefaultHttpClient(mgr,httpParams);
        //client = new DefaultHttpClient(httpParams);
        client.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");
        ((AbstractHttpClient) client).setCookieStore(new BasicCookieStore());
		//configureSSLHandling();
		
		c = new Cours("E", "E");
		System.out.println("config fini");
	}

	
	public List<Cours> start(String id,String mdp) {
		Boolean flag = true;
		try {
			GoToAgenda();			
			login(id,mdp);
			
//			if(!rspHtml.contains("Central Authentication Service")){
// 				flag=false;
// 			}
//			while(flag){
//				login(id,mdp);		
//	 			redirection();
//	 			if(!rspHtml.contains("Central Authentication Service")){
//	 				flag=false;
//	 			}
//			} 
//			
 			getAgendaHtml();
 			
// 			redirection();
 			getTWCoursPara();
// 			getNWCoursPara();
// 			for(Cours c : cours){
// 				getCoursDetail(c);
// 				chargerCours(c);
// 			}
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
			//System.out.println("status:"+status+"  url:"+httpGet.getURI().toString());
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
			if(status==200){
				entity = response.getEntity();
				//rspHtml = EntityUtils.toString(entity);
				InputStream stream = entity.getContent();
				BufferedReader br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();
				if(rspHtml.contains("Central Authentication Service")){
					System.out.println("Authentification failed:");
					throw new FailException("Authentification failed:"+id+"/"+mdp);
				}
				httpPost.abort();
				//userid=User.insert(id, mdp);
			}else{
				System.out.println("Status not 200:");
				throw new FailException("Status not 200:"+id+"/"+mdp);
			}			
		} catch (Exception e) {
			System.out.println("login cause exception:");
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
			data.add(new BasicNameValuePair("submit", "Poster"));
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			response = client.execute(httpPost);
			status = response.getStatusLine().getStatusCode();

			if(status==200){
				entity = response.getEntity();
				//rspHtml = EntityUtils.toString(entity);
				stream = entity.getContent();
				br =  new BufferedReader(new InputStreamReader(stream, "utf-8"));
				rspHtml="";
				for(String temp = br.readLine(); temp != null; rspHtml += temp ,temp = br.readLine()); 
				stream.close();
			
				httpPost.abort();
			}else{
				System.out.println("getAgendaHtml failed: not 200");
				throw new FailException("getAgendaHtml failed: not 200");
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
			System.out.println("chercherValue failed: no value found");
			throw new FailException("chercherValue failed: no value found");
		}
	}
	
	private void getTWCoursPara(){		
		Pattern pattern = Pattern.compile("onmouseover=\"DetEve\\(\'([0-9]+)\',\'([^']+)\',\'([0-9]+)\'\\)");
		Matcher matcher = pattern.matcher(rspHtml);
		while(matcher.find()){
			 Cours c = new Cours(matcher.group(1), matcher.group(3));
			 cours.add(c);
			 System.out.println(c.name);
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
				System.out.println(rspHtml);
				throw new FailException("getAgendaHtml failed: not 302");
			}			
		} catch (Exception e) {
			System.out.println(rspHtml);
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

//				byte[] body = EntityUtils.toByteArray(entity);
//				rspHtml = new String(body,Charset.forName("UTF-8"));
				
//				BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));  
//				String line = null;  
//				StringBuffer sb = new StringBuffer();
//				while ((line = reader.readLine()) != null) {  
//					sb.append(line); 
//				}
//				rspHtml = sb.toString();
				httpGet.abort();
			}else{
				System.out.println(rspHtml);
				throw new FailException("GetCoursDetail failed: not 200");
			}		
		} catch (Exception e) {
			System.out.println(rspHtml);
			throw new FailException("getCoursDetail cause exception:"+e.toString());
		}
		
	}
	
	private void chargerCours(Cours c) throws FailException{
		
		Pattern pattern = Pattern.compile("<B>Type<\\\\/B> : (.+)<B>Etat<\\\\/B> : (.+)<B>Début<\\\\/B> : (.+)<B>Fin<\\\\/B> : (.+)<B>Auteur<\\\\/B> : (.+)<B>Apprenants<\\\\/B> : (.+)<B>Projets<\\\\/B> : (.+)<B>Groupes de personnes<\\\\/B> : (.+)");
		Matcher matcher = pattern.matcher(rspHtml);
		
		if(matcher.find()){	
			 Pattern p = Pattern.compile("<(.+?)>", Pattern.DOTALL);
			 Pattern p1 = Pattern.compile("_([^_]+)_");
			 Pattern p3 = Pattern.compile(">([^<]+)<\\\\");
			 String resulta="";

			 Matcher m = p.matcher(matcher.group(1));
			 c.type=m.replaceAll("").trim();
			 c.type=c.type.replace("\\", "");
			 c.type=c.type.replace(" ", "");
			 c.type=c.type.replace("é", "e");
			 c.type=c.type.replace("è", "e");
			 c.type=c.type.replace("à", "a");
			 c.type=c.type.replace("ç", "c");
			 
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
//				sdf.applyPattern("E");
//				c.position+=sdf.format(date);
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
			 c.auteur=c.auteur.replace("é", "e");
			 c.auteur=c.auteur.replace("è", "e");
			 c.auteur=c.auteur.replace("à", "a");
			 c.auteur=c.auteur.replace("ç", "c");
			 
			 resulta="";
			 flag=false;
			 while(m1.find()){
				 if(flag) resulta+="_";
				 resulta+=m1.group(1);				 				 
				 flag=true;
			 }
			 c.formateur=resulta;	
			 resulta="";
			 
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
			 c.apprenants=c.apprenants.replace("\\", "");
			 c.apprenants=c.apprenants.replace(" ", " ");
			 resulta="";
			 
			 m = p.matcher(matcher.group(7));
			 c.name=m.replaceAll("");
			 c.name=c.name.replace("\\", "");
			 c.name=c.name.replace(" ", "");
			 c.name=c.name.replace("é", "e");
			 c.name=c.name.replace("è", "e");
			 c.name=c.name.replace("à", "a");
			 c.name=c.name.replace("ç", "c");
			 
			 Pattern p2 = Pattern.compile(">([^<]+)<\\\\/A>");
			 m=p2.matcher(matcher.group(8));
			 if(m.find()){
				 c.groupe=m.group(1);
			 }
			 flag=false;
			 while(m.find()){
				 if(flag) c.salle+="_";
				 c.salle+=m.group(1);
				 flag=true;
			 }
			 c.salle=c.salle.replace(" ", " ");
			 
			if(c.name.equals("") || c.type.equals("")|| c.auteur.equals("")|| c.groupe.equals("")){
				System.out.println(rspHtml);
				throw new FailException("Charge of cours failed:");
			}
			 
		}else {
			System.out.println(rspHtml);
			throw new FailException("matcher ERROR:");
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

}
