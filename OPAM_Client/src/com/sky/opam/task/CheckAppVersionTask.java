package com.sky.opam.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.sky.opam.model.VersionInfo;
import com.sky.opam.tool.FailException;
import com.sky.opam.tool.Tool;
import android.content.Context;
import android.os.AsyncTask;

public class CheckAppVersionTask extends AsyncTask<Void, Void, Void>{
	private Context context;
	
	public CheckAppVersionTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			int currentVersionCode = getCurrentVersionCode();
			if(currentVersionCode > Tool.getVersionCode(context)){
				VersionInfo versionInfo = getVersionInfo();
				Tool.showVersionInfo(context, versionInfo);
			}
		}catch (FailException e) {
			Tool.showInfo(context, e.getMessage());
		}
		return null;
	}
	
	private int getCurrentVersionCode() throws FailException{
		int versionCode = 0;
		HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet( "http://openopam-loic.rhcloud.com/agendaopamjson");
        try {
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                versionCode = Integer.parseInt(EntityUtils.toString(entity));
                httpGet.abort();
                return versionCode;
            } else {
            	throw new FailException("Can't connect to the server, status:" + status+ " recevied.");
            }
        } catch (UnsupportedEncodingException e) {
        	throw new FailException(e.getMessage());
		} catch (ClientProtocolException e) {
			throw new FailException(e.getMessage());
		} catch (IOException e) {
			throw new FailException(e.getMessage());
		} finally {
            client.getConnectionManager().shutdown();
        }
	}
	
	private VersionInfo getVersionInfo() throws FailException{
		HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet( "http://openopam-loic.rhcloud.com/agendaopamjson");
        try {
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                String resulta = EntityUtils.toString(entity);
                httpGet.abort();
                Gson gson = new Gson();
                return (VersionInfo) gson.fromJson(resulta, VersionInfo.class);
            } else {
            	throw new FailException("Can't connect to the server, status:" + status+ " recevied.");
            }
        } catch (UnsupportedEncodingException e) {
        	throw new FailException(e.getMessage());
		} catch (ClientProtocolException e) {
			throw new FailException(e.getMessage());
		} catch (IOException e) {
			throw new FailException(e.getMessage());
		} finally {
            client.getConnectionManager().shutdown();
        }
	}
	
}
