package com.sky.opam;

import com.sky.opam.model.ClassInfo;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.AndroidUtil;
import com.sky.opam.tool.TimeUtil;
import com.sky.opam.view.DayClassView;
import com.sky.opam.view.DayTabClassView.ClassInfoClickListener;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DayViewActivity extends ActionBarActivity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);
        
        int numWeek = (Integer) getIntent().getExtras().get("numWeek");
        int dayOfWeek = (Integer) getIntent().getExtras().get("dayOfWeek");
        
        MyApp myApp = (MyApp)getApplication();
        String login = myApp.getLogin();
        DBworker worker = new DBworker(this);
        
        DayClassView dayClassView = new DayClassView(this);
		dayClassView.setSW(AndroidUtil.getScreenWidth(this));
		dayClassView.setCours(worker.getClassInfo(login, numWeek, dayOfWeek));
		dayClassView.setClickListener(new ClassInfoClickListener() {
			@Override
			public void onTouchEvent(View v, MotionEvent e, ClassInfo c) {
				showClassInfo(c);
			}
		});
		ScrollView scrollView = new ScrollView(this);
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		TextView tView = new TextView(this);
		tView.setGravity(Gravity.CENTER);
		tView.setTextSize(AndroidUtil.dip2px(this, 15));
		int padding = AndroidUtil.dip2px(this, 5);
		tView.setPadding(padding, padding, padding, padding);
		tView.setText(TimeUtil.getYear()+"/"+TimeUtil.getDateViaNumWeek(numWeek, dayOfWeek));
		linearLayout.addView(tView);
		linearLayout.addView(dayClassView);
		scrollView.addView(linearLayout);
		setContentView(scrollView);
	}
	
	private void showClassInfo(ClassInfo c) {
		final Dialog dlg = new Dialog(this, R.style.MyDialog);
		dlg.show();
		Window win = dlg.getWindow();
		win.setContentView(R.layout.cours_detail_dialog);

		((TextView) win.findViewById(R.id.className)).setText(c.name);
		((TextView) win.findViewById(R.id.classType)).setText(c.classType.name);
		((TextView) win.findViewById(R.id.classTime)).setText(c.startTime + "--" + c.endTime);
		((TextView) win.findViewById(R.id.classGroup)).setText(c.groupe.replace("__", "\n"));
		if(c.room.name!=null || !c.room.name.equals("")) ((TextView) win.findViewById(R.id.classRoom)).setText(c.room.name.replace("__", "\n"));
		if(c.teacher!=null || !c.teacher.equals("")) ((TextView) win.findViewById(R.id.classTeacher)).setText(c.teacher.replace("__", "\n"));

		Button button = (Button) win.findViewById(R.id.dialog_button_cancel);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.cancel();
			}
		});
	}
}
