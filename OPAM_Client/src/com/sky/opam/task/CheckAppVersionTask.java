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

import com.sky.opam.tool.FailException;
import com.sky.opam.tool.AndroidUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class CheckAppVersionTask extends AsyncTask<Void, Void, String>{
	private Context context;
	private Handler handler;
	
	public CheckAppVersionTask(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			int lastVersionCode = getLastVersionCode();
			if(lastVersionCode > AndroidUtil.getVersionCode(context)){
				return getVersionInfo();
			}
		}catch (FailException e) {
			return null;
		}
		return null;
	}
	
	@Override
    protected void onPostExecute(String result) {
        if (result != null) {
        	Message msg = new Message();
        	Bundle b = new Bundle();// 存放数据
        	b.putString("versionInfo", result);
        	msg.setData(b);
        	handler.sendMessage(msg);
        }
    }
	
	private int getLastVersionCode() throws FailException{
		int versionCode = 0;
		HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet( "http://openopam-loic.rhcloud.com/agendaopamjson?para=code");
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
	
	private String getVersionInfo() throws FailException{
		String localLanguage = AndroidUtil.getLocalLanguage();
		HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet( "http://openopam-loic.rhcloud.com/agendaopamjson?para="+localLanguage);
        try {
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                String resulta = EntityUtils.toString(entity);
                httpGet.abort();
                return resulta;
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
