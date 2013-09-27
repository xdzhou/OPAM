package com.sky.opam.outil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class SimsimiAPI {
	HttpClient client;
	CookieStore cookieStore;
	HttpContext localContext;
	HttpGet httpGet;
	HttpResponse response;
	HttpEntity entity;

	public SimsimiAPI() {
		client = new DefaultHttpClient();
		// set cookie store
		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		// other set
		client.getParams().setParameter(
				HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");
	}

	public void Start() {
		httpGet = new HttpGet("http://www.simsimi.com/talk.htm");
		setHeader();
		try {
			response = client.execute(httpGet, localContext);
			httpGet.abort();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		client.getConnectionManager().shutdown();
	}

	public String getSimsimiRsp(String msg, String ln) {
		try {
			msg = URLEncoder.encode(msg, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		System.out.println("http://www.simsimi.com/func/req?msg=" + msg
				+ "&lc=" + ln);
		httpGet = new HttpGet("http://www.simsimi.com/func/req?msg=" + msg
				+ "&lc=" + ln);
		setHeader();
		try {
			response = client.execute(httpGet, localContext);
			entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();
			if (status == 200) {
				JSONObject json = new JSONObject(EntityUtils.toString(entity));
				String s = json.getString("response");
				return s;
			} else {
				return "Not 200, failed";
			}
			// httpGet.abort();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			return "I have no response!";
		}
		return "sorry, There is a error!!!";
	}

	private void setHeader() {
		httpGet.setHeader("Referer", "http://www.simsimi.com/talk.htm");
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 5.1; rv:20.0) Gecko/20100101 Firefox/20.0");
	}

}
