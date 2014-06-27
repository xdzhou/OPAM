package com.sky.opam.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.sky.opam.R;
import com.sky.opam.model.ClassInfo;
import com.sky.opam.model.User;
import com.sky.opam.model.UserClassPackage;
import com.sky.opam.tool.Chiffrement;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.FailException;
import com.sky.opam.tool.GoogleCalendarAPI;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class AgendaDownloadTask extends AsyncTask<String, Void, String>{
	private ProgressDialog pdialog;
	private DBworker worker;
	private Handler handler;
	private GoogleCalendarAPI calendarAPI;
	
	public AgendaDownloadTask(Context context,Handler handler) {
		this.worker = new DBworker(context);
		this.handler = handler;
		calendarAPI = new GoogleCalendarAPI(context);
        pdialog = new ProgressDialog(context, 0);
        pdialog.setCancelable(true);
        pdialog.setButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        pdialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                dialog = null;
                cancel(true);
            }
        });
        pdialog.setIcon(android.R.drawable.ic_popup_sync);
        pdialog.setTitle(R.string.downloading);
        pdialog.setMessage(context.getResources().getString(R.string.download_class_msg));
	}
	
	@Override
    protected void onPreExecute() {
        pdialog.show();
    }
	
	@Override
    protected void onCancelled() {
		super.onCancelled();
    }

	@Override
	protected String doInBackground(String... params) {
		String login = params[0];
		String password = params[1];
		String msg = null;
		try {
			String agendaJson = getAgendaJSON(login, password);
			Gson gson = new Gson();
			UserClassPackage mypackage = (UserClassPackage) gson.fromJson(agendaJson, UserClassPackage.class);
			if(mypackage.getClassInfos().size()==1 && mypackage.getClassInfos().get(0).name.equals("FailException")){
				msg = mypackage.getClassInfos().get(0).students;
			}else {
	        	User u = worker.getUser(login);
	        	if(u == null){
	        		u = new User(login, Chiffrement.encrypt(password,"OPAM"), mypackage.getUser().getName(), mypackage.getUser().getNumWeekUpdated());
	        		worker.addUser(u);
	        	}else {
	        		u.setNumWeekUpdated(mypackage.getUser().getNumWeekUpdated());
	                worker.updateUser(u);
				}	        	
                //worker.setDefaultUser(login);
                worker.delDownloadClassInfo(login, calendarAPI);
                List<ClassInfo> cours = mypackage.getClassInfos();
                for (ClassInfo c : cours) {
                    c.login = login;
                    c.bgColor = "#999999"; //default color
                    long id = worker.addGetRoom(c.room);
                    if(id==-1) c.room = null;
                    else c.room.id = id;
                    id = worker.addGetClassType(c.classType);
                    if(id==-1) c.classType = null;
                    else c.classType.id = id;
                    worker.addClassInfo(c);
                }
	        }
		} catch (FailException e) {
			msg = e.getMessage();
		}	
		return msg;
	}
	
	@Override
    protected void onPostExecute(String result) {
		worker = null;
        if (pdialog != null && pdialog.isShowing()) {
            pdialog.dismiss();
            pdialog = null;
        }
        if (result == null) {
            handler.sendEmptyMessage(R.integer.OK);
        } else {
        	Message msg = new Message();
        	Bundle b = new Bundle();// 存放数据
        	b.putString("error", result);
        	msg.setData(b);
        	handler.sendMessage(msg);
        }
    }
	
	private String getAgendaJSON(String login, String password) throws FailException{
		String agendaJson;
		HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost( "http://openopam-loic.rhcloud.com/agendaopamjson");
        List<NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicNameValuePair("username", login));
        data.add(new BasicNameValuePair("password", Chiffrement.encrypt(password, "OPAM")));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
            HttpResponse response = client.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                agendaJson = EntityUtils.toString(entity);
                httpPost.abort();
                return agendaJson;
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
