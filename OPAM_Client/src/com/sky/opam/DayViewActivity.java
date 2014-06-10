package com.sky.opam;

import com.sky.opam.fragment.WeekAgenda_Fragment;
import com.sky.opam.model.Cours;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;
import com.sky.opam.view.DayClassView;
import com.sky.opam.view.ClassInfoClickListener;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class DayViewActivity extends FragmentActivity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        int numWeek = (Integer) getIntent().getExtras().get("numWeek");
        int dayOfWeek = (Integer) getIntent().getExtras().get("dayOfWeek");
        MyApp myApp = (MyApp)getApplication();
        String login = myApp.getLogin();
        DBworker worker = new DBworker(this);
        
        DayClassView dayClassView = new DayClassView(this);
		dayClassView.setSW(Tool.getScreenWidth(this));
		dayClassView.setCours(worker.findClassInfo(login, numWeek, dayOfWeek));
		dayClassView.setClickListener(new ClassInfoClickListener() {
			@Override
			public void onTouchEvent(View v, MotionEvent e, Cours c) {
				showClassInfo(c);
			}
		});
		setContentView(dayClassView);
	}
	
	private void showClassInfo(Cours c) {
		final Dialog dlg = new Dialog(this, R.style.MyDialog);
		dlg.show();
		Window win = dlg.getWindow();
		win.setContentView(R.layout.cours_detail_pop);

		((TextView) win.findViewById(R.id.className)).setText(c.name);
		((TextView) win.findViewById(R.id.classType)).setText(c.type);
		((TextView) win.findViewById(R.id.classTime)).setText(c.debut + "--" + c.fin);
		((TextView) win.findViewById(R.id.classGroup)).setText(c.groupe.replace("__", "\n"));
		if(c.salle!=null || !c.salle.equals("")) ((TextView) win.findViewById(R.id.classRoom)).setText(c.salle.replace("__", "\n"));
		if(c.formateur!=null || !c.formateur.equals("")) ((TextView) win.findViewById(R.id.classTeacher)).setText(c.formateur.replace("__", "\n"));

		Button button = (Button) win.findViewById(R.id.dialog_button_cancel);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.cancel();
			}
		});
	}
}
