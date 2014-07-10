package com.loic.util;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class HtmlUtil {
	
	public static String findAttributValueByID(String html, String elementID, String attribuName){
		Document doc = Jsoup.parse(html);
		Element content = doc.getElementById(elementID);
		return content.attr(attribuName);
	}
	
	public static String findAttributValueByTAG(String html, String tag, String keyName, String attribuName){
		Document doc = Jsoup.parse(html);
		Elements tags = doc.getElementsByTag(tag);
		for (Element link : tags) {
			if(keyName.equals(link.attr("name"))){
				return link.attr(attribuName);
			}
		}
		return null;
	}
	
	public static String findInputValue(String html, String keyName){
		return findAttributValueByTAG(html, "input", keyName, "value");
	}
	
	public static HttpClient getHTTPSclient() throws FailException {
		try {
			HttpClient client = null;
			SSLContext ssc = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
	            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
	            public X509Certificate[] getAcceptedIssuers() {return null;}
			};
			ssc.init(null, new TrustManager[]{tm}, new SecureRandom());  
	        SSLSocketFactory ssf = new SSLSocketFactory(ssc,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 
			
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			schemeRegistry.register(new Scheme("https", 443, ssf));

			ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		    client = new DefaultHttpClient(cm);
		    return client;		
		} catch (Exception e) {
			throw new FailException(e.getMessage());
		}
	}
}
