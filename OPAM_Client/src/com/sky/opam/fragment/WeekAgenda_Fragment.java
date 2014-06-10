package com.sky.opam.fragment;

import com.sky.opam.R;
import com.sky.opam.model.Cours;
import com.sky.opam.tool.Tool;
import com.sky.opam.view.DayClassView;
import com.sky.opam.view.TimeLineView;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class WeekAgenda_Fragment extends Fragment{
	private int startTime = 8;
	private int endTime = 19;	
	private float time_distance ;
	
	private float day_view_width ;
	private int title_hight ;
	private String[] tab_title = {"Mon","Tue","Wen","Thi","Fri"};
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Bundle b = bundle;
		if ((bundle == null)) b = getArguments();
		startTime = b.getInt("startTime");
		endTime = b.getInt("endTime");
		time_distance = b.getFloat("time_distance");
		
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
			tabLayout.addView(tv);
		}
		Cours c = new Cours();
		c.debut = "09:45";
		c.fin = "11:15";
		c.name = "ASR:TTADSDSF:<FS:<F:SFL:SFLTTTTTTTTTTTTTTTTTTTTQQQQQQQQQQQQQQQQ";
		ScrollView agendaLayout = new ScrollView(context);
		LinearLayout innerLayout = new LinearLayout(context);
		agendaLayout.addView(innerLayout);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.addView(tiView);
		for(int i=0; i<5; i++){
			DayClassView dView = new DayClassView(context);
        	dView.setViewWidth(day_view_width);
        	dView.setTimeDistance(time_distance);
        	dView.setStartTime(startTime);
        	dView.setEndTime(endTime);
        	dView.addClass(c);
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
		outState.putFloat("time_distance", time_distance);
	}

}
