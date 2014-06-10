package com.sky.opam.fragment;

import java.util.HashMap;
import java.util.List;

import com.sky.opam.AgendaTabActivity;
import com.sky.opam.DayViewActivity;
import com.sky.opam.LoginActivity;
import com.sky.opam.R;
import com.sky.opam.model.Cours;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;
import com.sky.opam.view.ClassInfoClickListener;
import com.sky.opam.view.DayTabClassView;
import com.sky.opam.view.TimeLineView;

import android.R.color;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class WeekAgenda_Fragment extends Fragment{
	private int numWeek;
	private int startTime = 8;
	private int endTime = 19;	
	private float time_distance ;
	
	private float day_view_width ;
	private int title_hight ;
	private MyApp myApp;
	private final String[] tab_title = {"Mon","Tue","Wen","Thi","Fri"};
	private HashMap<Integer, List<Cours>> dateMap = new HashMap<Integer, List<Cours>>();
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Bundle b = bundle;
		if ((bundle == null)) b = getArguments();
		startTime = b.getInt("startTime");
		endTime = b.getInt("endTime");
		numWeek = b.getInt("numWeek");
		time_distance = b.getFloat("time_distance");
		
		myApp = (MyApp)getActivity().getApplication();
		Resources res = getActivity().getResources();
		title_hight = Tool.dip2px(getActivity(),res.getInteger(R.integer.title_hight));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Context context = getActivity();
		LinearLayout group_view = new LinearLayout(context);
		group_view.setOrientation(LinearLayout.VERTICAL);
		//
		LinearLayout tabLayout = new LinearLayout(context);
		tabLayout.setOrientation(LinearLayout.HORIZONTAL);
		TimeLineView tiView = new TimeLineView(context);
		tiView.setTimeDistance(time_distance);
		tiView.setStartTime(startTime);
		tiView.setEndTime(endTime);
		float tiView_width = tiView.getViewWidth();
		day_view_width = (Tool.getScreenWidth(getActivity()) - tiView_width)/5; 
		TextView tv = new TextView(context);
		tv.setGravity(Gravity.CENTER);
		tv.setWidth((int)tiView_width);
		tv.setHeight(title_hight);
		tabLayout.addView(tv);
		for(int i=0; i<5; i++){
			tv = new TextView(context);
			tv.setWidth((int)day_view_width);
			tv.setHeight(title_hight);
			tv.setText(tab_title[i]);
			tv.setGravity(Gravity.CENTER);
			tv.setOnClickListener(tabClickListener);
			tabLayout.addView(tv);
		}

		ScrollView agendaLayout = new ScrollView(context);
		LinearLayout innerLayout = new LinearLayout(context);
		agendaLayout.addView(innerLayout);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.addView(tiView);
		for(int i=0; i<5; i++){
			DayTabClassView dView = new DayTabClassView(context);
        	dView.setViewWidth(day_view_width);
        	dView.setTimeDistance(time_distance);
        	dView.setStartTime(startTime);
        	dView.setEndTime(endTime);
        	dView.setClickListener(classInfoClickListener);
        	dView.addClass(dateMap.get(i+1));
        	if(myApp.getCurrentWeekNum()==numWeek && Tool.getDayOfWeek()==i+1) dView.setBackgroundColor(Color.LTGRAY);
        	innerLayout.addView(dView);
		}
		
		group_view.addView(tabLayout);
		group_view.addView(agendaLayout);
		return group_view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("startTime", startTime);
		outState.putInt("endTime", endTime);
		outState.putInt("numWeek", numWeek);
		outState.putFloat("time_distance", time_distance);
	}
	
	ClassInfoClickListener classInfoClickListener = new ClassInfoClickListener() {		
		@Override
		public void onTouchEvent(View v, MotionEvent e, Cours c) {
			showClassInfo(c);
		}
	};
	
	View.OnClickListener tabClickListener = new View.OnClickListener() {	
		@Override
		public void onClick(View v) {
			if(v instanceof TextView){
				System.out.println("OO<<>>OO");
				TextView tv = (TextView) v;
				Intent intent = new Intent();
	            intent.setClass(getActivity(), DayViewActivity.class);
	            Bundle bundle = new Bundle();
	            bundle.putInt("numWeek", numWeek);
	            int dayOfWeek = 0;
				if(tv.getText().equals(tab_title[0])){
					dayOfWeek = 1;
				}else if (tv.getText().equals(tab_title[1])) {
					dayOfWeek = 2;
				}else if (tv.getText().equals(tab_title[2])) {
					dayOfWeek = 3;
				}else if (tv.getText().equals(tab_title[3])) {
					dayOfWeek = 4;
				}else {
					dayOfWeek = 5;
				}
				bundle.putInt("dayOfWeek", dayOfWeek);
				intent.putExtras(bundle);
	            startActivityForResult(intent, 0);
			}
		}
	};
	
	private void showClassInfo(Cours c) {
		final Dialog dlg = new Dialog(getActivity(), R.style.MyDialog);
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
	
	public void setData(int position, List<Cours> data){
		if(position>=1 && position<=5){
			dateMap.put(position, data);
		}
	}
}
