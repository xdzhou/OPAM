package com.sky.opam.task;

import java.util.List;

import com.sky.opam.model.ClassInfo;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.GoogleCalendarAPI;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;

import android.content.Context;
import android.os.AsyncTask;

public class AgendaSyncTask extends AsyncTask<Void, Void, Integer>{
	private DBworker worker;
	private Context context;
	private MyApp myApp;
	private GoogleCalendarAPI googleCalendarAPI;
	
	public AgendaSyncTask(Context context) {
		this.context = context;
		worker = new DBworker(context);
		myApp = (MyApp) context.getApplicationContext();
		googleCalendarAPI = new GoogleCalendarAPI(context);
	}

	@Override
	protected Integer doInBackground(Void... params) {
		List<ClassInfo> list = worker.getUnsyncClassInfo(myApp.getLogin());
		for(ClassInfo c : list){
			long eventId = googleCalendarAPI.addCourse2Calendar(c);
			worker.setClassSynced(c.id, eventId);
		}
		return list.size();
	}

	@Override
	protected void onPostExecute(Integer result) {
		if(result!=0){
			Tool.showInfo(context, result+ " course have been synced");
		}
		worker = null;
	}
	
	
	
}
