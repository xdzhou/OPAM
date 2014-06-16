package com.sky.opam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sky.opam.R.id;
import com.sky.opam.R.integer;
import com.sky.opam.model.ClassInfo;
import com.sky.opam.model.ClassType;
import com.sky.opam.model.Room;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;
import com.sky.opam.view.ColorPickerAdapter;
import com.sky.opam.view.RangeSeekBar;
import com.sky.opam.view.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class ClassInfoEditActivity extends ActionBarActivity{
	private DBworker worker;
	private MyApp myApp;
	private String bgColor;
	private ClassInfo classInfo;
	private EditText nameEditText;
	private EditText teacherEditText;
	private EditText groupEditText;
	private TextView startTimeTV;
	private TextView endTimeTV;
	private Spinner typeSpinner;
	private Spinner roomSpinner;
	private GridView colorGridView;
	private RangeSeekBar<Long> rangeSeekBar;
	private SimpleDateFormat  simpleFormat = new SimpleDateFormat("HH:mm");
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.classinfo_edit_activity);
		init();
		setActionBar();
	}
	
	private void init(){
		worker = new DBworker(this);
		myApp = (MyApp)getApplication();
		
		long classId = getIntent().getExtras().getLong("classId");
		String minTime = getIntent().getExtras().getString("minTime");
		String maxTime = getIntent().getExtras().getString("maxTime");	
		
		if(classId == -1){
			classInfo = ClassInfo.getCustomedClass(myApp.getLogin());
			classInfo.weekOfYear = getIntent().getExtras().getInt("weekOfYear");
			classInfo.dayOfWeek = getIntent().getExtras().getInt("dayOfWeek");
		}else {
			classInfo = worker.getClassInfo(classId);
		}
		bgColor = classInfo.bgColor;
		nameEditText = (EditText)findViewById(R.id.NameEditText);
		nameEditText.setText(classInfo.name);
		teacherEditText = (EditText)findViewById(R.id.teacherEditText);
		teacherEditText.setText(classInfo.teacher);
		groupEditText = (EditText)findViewById(R.id.groupEditText);
		groupEditText.setText(classInfo.groupe);
		
		startTimeTV = (TextView) findViewById(R.id.startTimeTV);
		startTimeTV.setText(minTime);
		
		endTimeTV = (TextView)findViewById(R.id.endTimeTV);
		endTimeTV.setText(maxTime);
		
		//rangeSeekBar
		LinearLayout classEditLayout = (LinearLayout)findViewById(R.id.classEditLayout);
		try {
			Date minDate = simpleFormat.parse(minTime);
			Date maxDate = simpleFormat.parse(maxTime);
			rangeSeekBar = new RangeSeekBar<Long>(minDate.getTime(), maxDate.getTime(), this);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		classEditLayout.addView(rangeSeekBar, 2);
		
		if(classInfo.id != -1){
			try {
				rangeSeekBar.setSelectedMinValue(simpleFormat.parse(classInfo.startTime).getTime());
				rangeSeekBar.setSelectedMaxValue(simpleFormat.parse(classInfo.endTime).getTime());
				startTimeTV.setText(classInfo.startTime);
				endTimeTV.setText(classInfo.endTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}		
		}
		rangeSeekBar.setNotifyWhileDragging(true);
		rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Long>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,Long minValue, Long maxValue) {
				Date date1 = new Date(minValue);
				Date date2 = new Date(maxValue);
				startTimeTV.setText(simpleFormat.format(date1));
				endTimeTV.setText(simpleFormat.format(date2));
			}		
		});
		
		typeSpinner = (Spinner)findViewById(R.id.typeSpinner);
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		int typeSelecteID = 0;
		List<ClassType> type_list  = worker.getAllClassType();
		for(int i=0; i<type_list.size(); i++){
			ClassType type = type_list.get(i);
			if(type.name.equals(classInfo.classType.name)) typeSelecteID = i;
			typeAdapter.add(type.name);
		}
		typeSpinner.setAdapter(typeAdapter);
		typeSpinner.setSelection(typeSelecteID);
		
		roomSpinner = (Spinner)findViewById(R.id.roomSpinner);
		ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		int roomSelecteID = 0;
		List<Room> room_list = worker.getAllRoom();
		for(int i=0; i<room_list.size(); i++){
			Room room = room_list.get(i);
			if(room.name.equals(classInfo.room.name)) roomSelecteID = i;
			roomAdapter.add(room.name);
		}
		roomSpinner.setAdapter(roomAdapter);
		roomSpinner.setSelection(roomSelecteID);
		
		colorGridView = (GridView)findViewById(R.id.colorGridView);
		final ColorPickerAdapter adapter = new ColorPickerAdapter(ClassInfoEditActivity.this);
		colorGridView.setAdapter(adapter);
		//adapter.initColor(classInfo.bgColor);
		colorGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				adapter.reselectColor((ImageView)view);
				bgColor = adapter.getBgColor(position);
			}
		});
	}
	
	private void setActionBar(){
		ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
	}
	
	@Override  
    public boolean onCreateOptionsMenu(Menu menu) { 
        MenuItemCompat.setShowAsAction(menu.add("cancel").setIcon(android.R.drawable.ic_menu_close_clear_cancel), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menu.add("save").setIcon(android.R.drawable.ic_menu_save), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        return true;  
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem menu) {
		if(menu.getTitle().equals("cancel")){
			setResult(MyApp.Refresh);
			finish();
		}else if (menu.getTitle().equals("save")) {
			String name = nameEditText.getText().toString();
			if(name==null || name.equals("")) Tool.showInfo(this, "input the class name, please!");
			else {
				classInfo.name = nameEditText.getText().toString();
				classInfo.startTime = startTimeTV.getText().toString();
				classInfo.endTime = endTimeTV.getText().toString();
				classInfo.teacher = teacherEditText.getText().toString();
				classInfo.groupe = groupEditText.getText().toString();
				classInfo.classType = worker.getClassType(((ArrayAdapter<String>)typeSpinner.getAdapter()).getItem((int) typeSpinner.getSelectedItemId()));
				classInfo.room = worker.getRoom(((ArrayAdapter<String>)roomSpinner.getAdapter()).getItem((int) roomSpinner.getSelectedItemId()));
				classInfo.bgColor = bgColor;
				if(classInfo.id == -1) worker.addClassInfo(classInfo);
				else worker.updateClassInfo(classInfo);
				setResult(MyApp.Refresh);
				finish();
			}		
		}
		return super.onOptionsItemSelected(menu);
	}

}
