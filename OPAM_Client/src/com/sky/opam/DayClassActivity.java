package com.sky.opam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sky.opam.R;
import com.sky.opam.outil.DBworker;

public class DayClassActivity extends ListActivity  {
	Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String login = (String) getIntent().getExtras().get("login");
		String flag = (String) getIntent().getExtras().get("flag");
		
		DBworker worker = new DBworker(getApplicationContext());
		cursor = worker.findClass(login, flag);
		if(cursor.getCount()==0){	
			
			//setContentView(myLayout);
		}else {
			SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.cours_detail,
	                new String[]{"name","type","time","group","salle"},
	                new int[]{R.id.className,R.id.classType,R.id.classTime,R.id.classGroup,R.id.classRoom});
	        setListAdapter(adapter);
		}
		cursor.close();      
	}
	
	private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm"); 
 
        Map<String, Object> map;
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
        	map = new HashMap<String, Object>();
            map.put("name", cursor.getString(0));
            map.put("type", "Type:   "+cursor.getString(1));
            map.put("time", "Time:   "+cursor.getString(2)+"--"+cursor.getString(3)); 
            map.put("group", "Group:   "+cursor.getString(4));
            map.put("salle", "Room:   "+cursor.getString(5));
            list.add(map);
        }
        return list;
    }

	private void showInfo(String msg){
		Toast.makeText(DayClassActivity.this, msg, Toast.LENGTH_SHORT).show();        
    }
}
