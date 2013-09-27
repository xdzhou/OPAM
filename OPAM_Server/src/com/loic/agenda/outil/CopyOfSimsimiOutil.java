package com.loic.agenda.outil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
//Simsimi: 54.225.163.43
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

public class CopyOfSimsimiOutil {
	HttpClient client;
	CookieStore cookieStore;
	HttpContext localContext;
	HttpGet httpGet;
	HttpResponse response;
	HttpEntity entity;

	public CopyOfSimsimiOutil(){ 
		client = new DefaultHttpClient();
		// set proxy
		HttpHost proxy = new HttpHost("155.132.8.50", 8000);
		client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		// set cookie store
		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		//other set
		client.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");		
	}
	
	public String getSimsimiRsp(String msg){
		httpGet = new HttpGet("http://www.simsimi.com/talk.htm");
		setHeader();
		try {
			response = client.execute(httpGet,localContext);
			httpGet.abort();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//ShowCookies();
		
		httpGet = new HttpGet("http://www.simsimi.com/func/req?msg="+msg+"&lc=en");
		setHeader();
		//setGAcookies();
		try {
			response = client.execute(httpGet,localContext);
			entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();
			if(status==200){
				//System.out.println(EntityUtils.toString(entity));
				JSONParser parser = new JSONParser();
				ContainerFactory containerFactory = new ContainerFactory(){
				    public List creatArrayContainer() {
				      return new LinkedList();
				    }
				    public Map createObjectContainer() {
				      return new LinkedHashMap();
				    }                    
				  };
				  Map map = (Map)parser.parse(EntityUtils.toString(entity), containerFactory);
				  //System.out.println(map.get("response"));
				  return (String) map.get("response");
			}else {
				return "Not 200, failed";
			}	
			//httpGet.abort();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		} finally{
			client.getConnectionManager().shutdown();
		}
		//ShowCookies();		
		return "end";
		
	}
	
	private void setHeader(){
		httpGet.setHeader("Referer", "http://www.simsimi.com/talk.htm");
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:20.0) Gecko/20100101 Firefox/20.0");
	}
	
	private void setGAcookies(){
		BasicClientCookie cookie = new BasicClientCookie("__utma", "119922954.1468906681.1366894641.1366976937.1366976937.6");
		cookie.setDomain(".simsimi.com");
		cookie.setPath("/");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.YEAR, c.get(Calendar.YEAR)+2);
		cookie.setExpiryDate(c.getTime());
		cookieStore.addCookie(cookie);
		
		String b = "119922954.4.9."+new Date().getTime();
		cookie = new BasicClientCookie("__utmb", b);
		cookie.setDomain(".simsimi.com");
		cookie.setPath("/");
		c.setTime(new Date());
		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE)+30);
		cookie.setExpiryDate(c.getTime());
		cookieStore.addCookie(cookie);
		
		cookie = new BasicClientCookie("__utmc", "119922954");
		cookie.setDomain(".simsimi.com");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		
		cookie = new BasicClientCookie("__utmz", "119922954.1366894642.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
		cookie.setDomain(".simsimi.com");
		cookie.setPath("/");
		c.setTime(new Date());
		c.set(Calendar.MONTH, c.get(Calendar.MONTH)+6);
		cookie.setExpiryDate(c.getTime());
		cookieStore.addCookie(cookie);
	}
	
	private void ShowCookies(){
		for(Cookie c : cookieStore.getCookies()){
			//System.out.println("Name:"+c.getName()+"  value:"+c.getValue()+" expire:"+c.getExpiryDate().toString());
			System.out.println(c);
		}
	}
	
	
	
}
