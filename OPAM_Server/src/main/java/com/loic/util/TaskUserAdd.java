package com.loic.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.loic.clientModel.ClassInfoClient;
import com.loic.config.Config;
import com.loic.dao.UserDAO;
import com.loic.model.User;

public class TaskUserAdd implements Runnable{
	private List<ClassInfoClient> classInfos;
	private UserDAO userDAO;
	private HttpClient client;
	
	public TaskUserAdd(List<ClassInfoClient> classInfos, UserDAO userDAO, HttpClient client){
		this.classInfos = classInfos;
		this.userDAO = userDAO;
		this.client = client;
	}
	
	@Override
	public void run() {
		for(ClassInfoClient c : classInfos){
			String[] studentNames = c.students.split("_");
			for(String name : studentNames){
				User u = userDAO.findByName(name);
				if(u==null){
					String nom="", prenom="";
					String[] names = name.split(" ");
					for(String n : names){
						if(isAcronym(n)) nom += (n+"_");
						else prenom += (n+"_");
					}
					nom = nom.substring(0, nom.length()-1);
					prenom = prenom.substring(0, prenom.length()-1);
					
					searchUser(nom);
					searchUser(prenom);
				}
			}
		}
	}
	
	private void searchUser(String name){
		try {
			HttpPost httpPost = new HttpPost(Config.TROMBI_HOST);
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("user", name));
			data.add(new BasicNameValuePair("submit", "Rechercher"));
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			HttpResponse response = client.execute(httpPost);
			
			String rspHtml = EntityUtils.toString(response.getEntity());
			Document doc = Jsoup.parse(rspHtml);
			
			Elements elements = doc.getElementsByClass("TINT");
			elements.addAll(doc.getElementsByClass("INTM"));
			for(Element ele : elements){
				User user = new User();
				user.setName(standarniserNomPrenom(ele.getElementsByClass("ldapNom").get(0).text()));
				
				String email = ele.getElementsByTag("p").get(0).text();
				email = email.replace("Email :", "").trim();
				email = email.replace("[AT]", "@");
				user.setEmail(email);
				
				String login = ele.getElementsByTag("img").get(0).attr("src");
				StringBuilder sb = new StringBuilder();
				for(int i=15; i<login.length(); i++){
					if(login.charAt(i)!='&'){
						sb.append(login.charAt(i));
					}else {
						break;
					}
				}
				login = sb.toString();
				user.setLogin(login);
				if(ele.text().contains("SudParis")) user.setSchool("TSP");
				else user.setSchool("TEM");
				
				saveUser(user);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void saveUser(User user){
		User u = userDAO.findByLogin(user.getLogin());
		if(u == null){
			userDAO.save(user);
		}else {
			if(!u.equals(user)){
				u.setEmail(user.getEmail());
				u.setSchool(user.getSchool());
				userDAO.update(u);
			}
		}
	}
	
	//全是大写字母就 true
	private boolean isAcronym(String word){
		for(int i = 0; i < word.length(); i++){
			char c = word.charAt(i);
			if (Character.isLowerCase(c)){
				return false;
			}
		}
		return true;
	}
	
	private String standarniserNomPrenom(String name){
		String nom="", prenom="";
		String[] names = name.split(" ");
		for(String n : names){
			if(isAcronym(n)) nom += (n+" ");
			else prenom += (n+" ");
		}
		nom = nom.substring(0, nom.length()-1);
		prenom = prenom.substring(0, prenom.length()-1);
		return nom+" "+prenom;
	}

}
