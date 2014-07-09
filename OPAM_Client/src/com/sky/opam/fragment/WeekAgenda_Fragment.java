package com.sky.opam.fragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.sky.opam.ClassInfoEditActivity;
import com.sky.opam.DayViewActivity;
import com.sky.opam.R;
import com.sky.opam.model.ClassInfo;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Util;
import com.sky.opam.view.DayTabClassView;
import com.sky.opam.view.DayTabClassView.ClassInfoClickListener;
import com.sky.opam.view.DayTabClassView.DayViewLongPressListener;
import com.sky.opam.view.TimeLineView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
	private int numClassInfo = 0;
	private int startTime = 8;
	private int endTime = 19;	
	private float time_distance ;
	
	private float day_view_width ;
	private int title_hight ;
	private MyApp myApp;
	private DBworker worker;
	private String[] tab_title = {"MO ","TU ","WE ","TH ","FR "};
	private HashMap<Integer, List<ClassInfo>> dateMap = new HashMap<Integer, List<ClassInfo>>();
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Bundle b = bundle;
		if ((bundle == null)) b = getArguments();
		startTime = b.getInt("startTime");
		endTime = b.getInt("endTime");
		numWeek = b.getInt("numWeek");
		time_distance = b.getFloat("time_distance");
		worker = new DBworker(getActivity());
		
		myApp = (MyApp)getActivity().getApplication();
		Resources res = getActivity().getResources();
		title_hight = Util.dip2px(getActivity(),res.getInteger(R.integer.title_hight));
		
		for(int i=0; i<tab_title.length; i++){
			tab_title[i] += Util.getDateViaNumWeek(numWeek, i+Calendar.MONDAY).substring(3, 5);
		}

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
		day_view_width = (Util.getScreenWidth(getActivity()) - tiView_width)/5; 
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
			dView.setId(i+Calendar.MONDAY);
        	dView.setViewWidth(day_view_width);
        	dView.setTimeDistance(time_distance);
        	dView.setStartTime(startTime);
        	dView.setEndTime(endTime);
        	dView.setMyLongPressListener(dayViewLongPressListener);
        	dView.setClickListener(classInfoClickListener);
        	dView.addClass(dateMap.get(i+Calendar.MONDAY));
        	if(myApp.getCurrentWeekNum() == numWeek && Util.getDayOfWeek()==i+Calendar.MONDAY) dView.setBackgroundColor(Color.LTGRAY);
        	innerLayout.addView(dView);
		}
		
		group_view.addView(tabLayout);
		group_view.addView(agendaLayout);
		//group_view.buildDrawingCache();
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
	
	DayViewLongPressListener dayViewLongPressListener = new DayViewLongPressListener() {		
		@Override
		public void onLongPressEvent(DayTabClassView v, ClassInfo c, String vocationStartTime,String vocationEndTime) {
			if(c == null){
				createEventDialog(v, vocationStartTime, vocationEndTime);
			}else {
				EventEditDialog(v, c, vocationStartTime, vocationEndTime);
			}
		}
	};
	
	ClassInfoClickListener classInfoClickListener = new ClassInfoClickListener() {		
		@Override
		public void onTouchEvent(View v, MotionEvent e, ClassInfo c) {
			showClassInfo(c);
		}
	};
	
	View.OnClickListener tabClickListener = new View.OnClickListener() {	
		@Override
		public void onClick(View v) {
			if(v instanceof TextView){
				TextView tv = (TextView) v;
				Intent intent = new Intent();
	            intent.setClass(getActivity(), DayViewActivity.class);
	            Bundle bundle = new Bundle();
	            bundle.putInt("numWeek", numWeek);
	            int dayOfWeek;
				if(tv.getText().equals(tab_title[0])){
					dayOfWeek = Calendar.MONDAY;
				}else if (tv.getText().equals(tab_title[1])) {
					dayOfWeek = Calendar.TUESDAY;
				}else if (tv.getText().equals(tab_title[2])) {
					dayOfWeek = Calendar.WEDNESDAY;
				}else if (tv.getText().equals(tab_title[3])) {
					dayOfWeek = Calendar.THURSDAY;
				}else {
					dayOfWeek = Calendar.FRIDAY;
				}
				bundle.putInt("dayOfWeek", dayOfWeek);
				intent.putExtras(bundle);
	            startActivityForResult(intent, 0);
			}
		}
	};
	
	private void showClassInfo(ClassInfo c) {
		final Dialog dlg = new Dialog(getActivity(), R.style.MyDialog);
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
	
	public void setData(int position, List<ClassInfo> data){
		if(position>=Calendar.MONDAY && position<=Calendar.FRIDAY){
			dateMap.put(position, data);
			numClassInfo += data.size();
		}
	}
	/*
	 * 
	 */
	private void createEventDialog(final DayTabClassView v, final String vocationStartTime, final String vocationEndTime){
		String[] mItems = {getResources().getString(R.string.add_class)};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle(getResources().getString(R.string.free_time)+": "+vocationStartTime+" - "+vocationEndTime);
		builder.setOnCancelListener(new OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				v.cancelSelectDraw();
			}
		});
		builder.setItems(mItems, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
		        intent.setClass(getActivity(), ClassInfoEditActivity.class);
		        Bundle bundle = new Bundle();
		        bundle.putLong("classId", -1);
		        bundle.putString("minTime", vocationStartTime);
		        bundle.putString("maxTime", vocationEndTime);
		        bundle.putInt("weekOfYear", numWeek);
		        bundle.putInt("dayOfWeek", v.getId());
		        intent.putExtras(bundle);
		        getActivity().startActivityForResult(intent, MyApp.rsqCode);
			}
		});
		builder.create().show();
	}
	
	private void EventEditDialog(final DayTabClassView v, final ClassInfo c, final String vocationStartTime, final String vocationEndTime){
		String[] mItems = {getResources().getString(R.string.edit_class), getResources().getString(R.string.remove_class)};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle(getResources().getString(R.string.class_time)+": "+c.startTime+" - "+c.endTime);
		builder.setOnCancelListener(new OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				v.cancelSelectDraw();
			}
		});
		builder.setItems(mItems, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==0){
					Intent intent = new Intent();
			        intent.setClass(getActivity(), ClassInfoEditActivity.class);
			        Bundle bundle = new Bundle();
			        bundle.putLong("classId", c.id);
			        bundle.putString("minTime", vocationStartTime);
			        bundle.putString("maxTime", vocationEndTime);
			        intent.putExtras(bundle);
			        getActivity().startActivityForResult(intent, MyApp.rsqCode);
				}else{
					worker.delClassInfo(c.id);
					v.removeClass(c);
					numClassInfo--;
					dialog.cancel();
				}			
			}
		});
		builder.create().show();
	}

	public int getNumWeek() {
		return numWeek;
	}
	public int getNumClassInfo() {
		return numClassInfo;
	}
}
