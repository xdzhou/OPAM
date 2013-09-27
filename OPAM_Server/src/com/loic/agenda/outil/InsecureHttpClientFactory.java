package com.loic.agenda.outil;


import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * <p>Sample factory for building a HttpClient that configures a HttpClient 
 * instance to store cookies and to accept SSLcertificates without HostName validation.</p>
 * <p>You obviously should not use this class in production, but it may come handy when 
 * developing with internal Servers using self-signed certificates.</p>
 */
public class InsecureHttpClientFactory {
	HttpClient hc;
	
    public HttpClient buildHttpClient(ClientConnectionManager ccm) {	
		HttpParams httpParams = new BasicHttpParams();
		String userAgent = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11";
        HttpProtocolParams.setUserAgent(httpParams, userAgent);  
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
        
        hc = new DefaultHttpClient(ccm,httpParams);
        hc.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");
		//configureProxy();
		//configureCookieStore();
		configureSSLHandling();
		return hc;
	}
       
    private void configureProxy() {
            //HttpHost proxy = new HttpHost("emea-proxy-pool.eu.alcatel-lucent.com", 8000);
            HttpHost proxy = new HttpHost("155.132.8.50", 8000);
            hc.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

	private void configureCookieStore() {
		CookieStore cStore = new BasicCookieStore();
		((AbstractHttpClient) hc).setCookieStore(cStore);
	}

	private void configureSSLHandling() {		
		SSLContext ssc;
		try {
			ssc = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {return null;}
			};
			ssc.init(null, new TrustManager[]{tm}, new SecureRandom());  
	        SSLSocketFactory ssf = new SSLSocketFactory(ssc,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 
	        Scheme https = new Scheme("https", 443, ssf); 
	        SchemeRegistry sr = hc.getConnectionManager().getSchemeRegistry();
	        Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
 			sr.register(http);
 			sr.register(https);
		} catch (Exception e) {
			e.printStackTrace();
		}  
	}

}