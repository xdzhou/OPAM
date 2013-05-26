package com.sky.opam;

import java.util.List;

import com.sky.opam.model.Cours;
import com.sky.opam.outil.DBworker;
import com.sky.opam.widget.ClassView;
import com.sky.opam.widget.MyViewclickListener;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DynamicClassActivity extends Activity  {
	LinearLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dynamic_class); 
		
		String login = (String) getIntent().getExtras().get("login");
		String flag = (String) getIntent().getExtras().get("flag");
		String isToday = (String) getIntent().getExtras().get("isToday");
		
		ClassView myview = (ClassView)findViewById(R.id.myview);
		
		WindowManager manager = getWindowManager();
        Display display = manager.getDefaultDisplay();
        myview.setSW(display.getWidth());
        //myview.setSH(display.getHeight());
        if(isToday.equals("1")){
        	myview.setIstoday(true);
        }

        DBworker worker = new DBworker(getApplicationContext());
        final List<Cours> cours = worker.trouverCours(login, flag);
		myview.setCours(cours);
		myview.setClickListener(new MyViewclickListener() {		
			@Override
			public void onTouchEvent(View v, MotionEvent event, int i) {
				if(v.getId()==R.id.myview){
					Cours c = cours.get(i);
					showClassInfo(c);
				}
			}
		});
		//showInfo("login:"+login+" flag:"+flag);
	}
	private void showClassInfo(Cours c){
		final Dialog dlg = new Dialog(DynamicClassActivity.this, R.style.MyDialog);   
		dlg.show(); 
		Window win = dlg.getWindow();
		win.setContentView(R.layout.cours_detail);		
	
		TextView name = (TextView)win.findViewById(R.id.className);
		TextView type = (TextView)win.findViewById(R.id.classType);
		TextView time = (TextView)win.findViewById(R.id.classTime);
		TextView group = (TextView)win.findViewById(R.id.classGroup);
		TextView room = (TextView)win.findViewById(R.id.classRoom);
		name.setText(c.name);
		type.setText("Type:   "+c.type);
		time.setText("Time:   "+c.debut+"--"+c.fin);
		group.setText("Group:   "+c.groupe);
		room.setText("Room:   "+c.salle);
              
		Button button = (Button) win.findViewById(R.id.dialog_button_cancel);
		button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				dlg.cancel();
			}
		});
    }
}
