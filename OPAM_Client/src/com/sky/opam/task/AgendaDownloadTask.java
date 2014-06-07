package com.sky.opam.task;

import java.io.ByteArrayInputStream;
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

import com.sky.opam.R;
import com.sky.opam.model.Cours;
import com.sky.opam.model.DataCompo;
import com.sky.opam.model.User;
import com.sky.opam.tool.Chiffrement;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.FailException;
import com.sky.opam.tool.PullXMLReader;

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
	private Context context;
	private Handler handler;
	
	public AgendaDownloadTask(Context context, DBworker worker,Handler handler) {
		this.worker = worker;
		this.context = context;
		this.handler = handler;
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
        pdialog.setIcon(R.drawable.download_icon);
        pdialog.setTitle("downloading the data");
        pdialog.setMessage("Please wait while loading the list of class...");
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
			String agendaXML = getAgendaXML(login, password);
			DataCompo dataCompo = PullXMLReader.readXML(new ByteArrayInputStream(agendaXML.getBytes("UTF-8")));
			if (dataCompo.getId() == 2) {
                msg = dataCompo.getCours().get(0).type;
	        } else {
	        	User u = worker.findUser(login);
	        	if(u==null){
	        		u = new User(login, Chiffrement.encrypt(password,"OPAM"), dataCompo.getUsername());
	        		worker.addUser(u);
	        	}
	        	u.setThisweek(dataCompo.getNumweek());
                worker.updateUser(u);
                worker.updateDefaultUser(login);
                worker.delAllCours(login);
                worker.delAllEventID(context, login);
                List<Cours> cours = dataCompo.getCours();
                for (Cours c : cours) {
                        c.login = login;
                        worker.addCours(c);
                }
	        }
		} catch (FailException e) {
			msg = e.getMessage();
		} catch (UnsupportedEncodingException e) {
			msg = e.getMessage();
		}		
		return msg;
	}
	
	@Override
    protected void onPostExecute(String result) {
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
	
	private String getAgendaXML(String login, String password) throws FailException{
		String agendaXML;
		HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost( "http://openopam-loic.rhcloud.com/agendaopam");
        List<NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicNameValuePair("username", login));
        data.add(new BasicNameValuePair("password", Chiffrement.encrypt(password, "OPAM")));
        try {
                httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
                HttpResponse response = client.execute(httpPost);
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    agendaXML = EntityUtils.toString(entity);
                    httpPost.abort();
                    return agendaXML;
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
