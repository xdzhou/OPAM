package com.sky.opam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.sky.opam.model.ClassInfo;
import com.sky.opam.model.ClassType;
import com.sky.opam.model.Room;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.GoogleCalendarAPI;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;
import com.sky.opam.view.ColorPickerAdapter;
import com.sky.opam.view.RangeSeekBar;
import com.sky.opam.view.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.TimePicker;

public class ClassInfoEditActivity extends ActionBarActivity
{
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
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.classinfo_edit_activity);
		init();
		setActionBar();
	}
	
	private void init()
	{
		worker = new DBworker(this);
		myApp = (MyApp)getApplication();
		
		long classId = getIntent().getExtras().getLong("classId");
		String minTime = getIntent().getExtras().getString("minTime");
		String maxTime = getIntent().getExtras().getString("maxTime");
		
		if(classId == -1)
		{
			classInfo = ClassInfo.getCustomedClass(myApp.getLogin());
			classInfo.weekOfYear = getIntent().getExtras().getInt("weekOfYear");
			classInfo.dayOfWeek = getIntent().getExtras().getInt("dayOfWeek");
		}
		else 
		{
			classInfo = worker.getClassInfo(classId);
			if(minTime == null){
				String[] minMaxTime = Tool.getVocationTime(myApp, classInfo);
				minTime = minMaxTime[0];
				maxTime = minMaxTime[1];
			}
		}
		bgColor = classInfo.bgColor;
		nameEditText = (EditText)findViewById(R.id.NameEditText);
		nameEditText.setText(classInfo.name);
		teacherEditText = (EditText)findViewById(R.id.teacherEditText);
		teacherEditText.setText(classInfo.teacher);
		groupEditText = (EditText)findViewById(R.id.groupEditText);
		groupEditText.setText(classInfo.groupe);
		
		startTimeTV = (TextView) findViewById(R.id.startTimeTV);
		startTimeTV.setId(0);
		startTimeTV.setText(minTime);
		startTimeTV.setOnClickListener(timePickListener);
		
		endTimeTV = (TextView)findViewById(R.id.endTimeTV);
		endTimeTV.setId(1);
		endTimeTV.setText(maxTime);
		endTimeTV.setOnClickListener(timePickListener);
		
		//rangeSeekBar
		LinearLayout classEditLayout = (LinearLayout)findViewById(R.id.classEditLayout);
		try 
		{
			Date minDate = simpleFormat.parse(minTime);
			Date maxDate = simpleFormat.parse(maxTime);
			rangeSeekBar = new RangeSeekBar<Long>(minDate.getTime(), maxDate.getTime(), this);
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
		classEditLayout.addView(rangeSeekBar, 2);
		
		if(classInfo.id != -1)
		{
			try 
			{
				rangeSeekBar.setSelectedMinValue(simpleFormat.parse(classInfo.startTime).getTime());
				rangeSeekBar.setSelectedMaxValue(simpleFormat.parse(classInfo.endTime).getTime());
				startTimeTV.setText(classInfo.startTime);
				endTimeTV.setText(classInfo.endTime);
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}		
		}
		rangeSeekBar.setNotifyWhileDragging(true);
		rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Long>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,Long minValue, Long maxValue) 
			{
				Date date1 = new Date(minValue);
				Date date2 = new Date(maxValue);
				startTimeTV.setText(simpleFormat.format(date1));
				endTimeTV.setText(simpleFormat.format(date2));
			}		
		});
		
		typeSpinner = (Spinner)findViewById(R.id.typeSpinner);
		AddAdapter typeAdapter = new AddAdapter(this);
		int typeSelecteID = 0;
		List<ClassType> type_list  = worker.getAllClassType();
		for(int i=0; i<type_list.size(); i++)
		{
			ClassType type = type_list.get(i);
			if(type.name.equals(classInfo.classType.name)) typeSelecteID = i;
			typeAdapter.add(type.name);
		}
		typeAdapter.add(getResources().getString(R.string.add)+" "+getResources().getString(R.string.type));
		typeSpinner.setAdapter(typeAdapter);
		typeSpinner.setSelection(typeSelecteID);
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) 
			{			
				if(parent.getAdapter().getCount()-1 == position){
					getTextEntreBuilder((AddAdapter)parent.getAdapter(), 0).show();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		roomSpinner = (Spinner)findViewById(R.id.roomSpinner);
		AddAdapter roomAdapter = new AddAdapter(this);
		int roomSelecteID = 0;
		List<Room> room_list = worker.getAllRoom();
		for(int i=0; i<room_list.size(); i++)
		{
			Room room = room_list.get(i);
			if(classInfo.room!=null && room.name.equals(classInfo.room.name)) roomSelecteID = i;
			roomAdapter.add(room.name);
		}
		roomAdapter.add(getResources().getString(R.string.add)+" "+getResources().getString(R.string.room));
		roomSpinner.setAdapter(roomAdapter);
		roomSpinner.setSelection(roomSelecteID);
		roomSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) 
			{			
				if(parent.getAdapter().getCount()-1 == position){
					getTextEntreBuilder((AddAdapter)parent.getAdapter(), 1).show();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
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
	
	private void setActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			setResult(MyApp.Refresh);
			finish();
			return true;
		}else {
        	return super.onKeyDown(keyCode, event);
	    }
	}
	
	@Override  
    public boolean onCreateOptionsMenu(Menu menu) 
	{ 
        MenuItemCompat.setShowAsAction(menu.add("cancel").setIcon(android.R.drawable.ic_menu_close_clear_cancel), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menu.add("save").setIcon(android.R.drawable.ic_menu_save), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        return true;  
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem menu) 
	{
		if(menu.getTitle().equals("cancel"))
		{
			setResult(MyApp.Refresh);
			finish();
		}
		else if (menu.getTitle().equals("save")) 
		{
			String name = nameEditText.getText().toString();
			if(name==null || name.equals("")) 
				Tool.showInfo(this, "input the class name, please!");
			else 
			{
				classInfo.name = nameEditText.getText().toString();
				classInfo.startTime = startTimeTV.getText().toString();
				classInfo.endTime = endTimeTV.getText().toString();
				classInfo.teacher = teacherEditText.getText().toString();
				classInfo.groupe = groupEditText.getText().toString();
				classInfo.classType = worker.getClassType(((AddAdapter)typeSpinner.getAdapter()).getItem((int) typeSpinner.getSelectedItemId()));
				classInfo.room = worker.getRoom(((AddAdapter)roomSpinner.getAdapter()).getItem((int) roomSpinner.getSelectedItemId()));
				classInfo.bgColor = bgColor;
				GoogleCalendarAPI googleCalendarAPI = new GoogleCalendarAPI(ClassInfoEditActivity.this);
				if(classInfo.id == -1) worker.addClassInfo(classInfo);				
				else worker.updateClassInfo(classInfo, googleCalendarAPI);
				setResult(MyApp.Refresh);
				finish();
			}		
		}
		return super.onOptionsItemSelected(menu);
	}
	
	class AddAdapter extends ArrayAdapter<String>
	{
		public AddAdapter(Context context) 
		{
			super(context, R.layout.date_dropdown_spinner_layout);
		}
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) 
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.date_dropdown_spinner_layout, null);
			TextView tv = (TextView)convertView.findViewById(R.id.text1);
			String itemString = getItem(position);
			tv.setText(itemString);
			if(position == getCount()-1 )
			{
				//tv.setBackgroundColor(Color.GRAY);
				tv.setBackgroundResource(R.drawable.important_bg);
			}
			return convertView;
		}
	}
	
	private AlertDialog.Builder getTextEntreBuilder(final AddAdapter adapter, final int flag)
	{
		final View viewDia = LayoutInflater.from(ClassInfoEditActivity.this).inflate(R.layout.alert_dialog_text_entry, null);
		final EditText newInfoET = (EditText) viewDia.findViewById(R.id.et);
		TextView tView = (TextView)viewDia.findViewById(R.id.new_info);
		if(flag==0) tView.setText(getResources().getString(R.string.type)+" :");
		else tView.setText(getResources().getString(R.string.room)+" :");
		AlertDialog.Builder builder = new AlertDialog.Builder(ClassInfoEditActivity.this);
		builder.setIcon(android.R.drawable.ic_menu_edit);
		if(flag==0) builder.setTitle(getResources().getString(R.string.add)+" "+getResources().getString(R.string.type));
		else builder.setTitle(getResources().getString(R.string.add)+" "+getResources().getString(R.string.room));
		builder.setView(viewDia);
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if(flag == 0) typeSpinner.setSelection(0);
				else roomSpinner.setSelection(0);	
			}			
		});
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				String info = newInfoET.getText().toString();
				if(info.equals("")) 
				{
					Tool.showInfo(ClassInfoEditActivity.this, " NULL ");
					if(flag == 0) typeSpinner.setSelection(0);
					else roomSpinner.setSelection(0);
				}
				else 
				{							
					adapter.insert(info, 0);
					adapter.notifyDataSetChanged();
					if(flag == 0) 
					{
						worker.addGetClassType(info);
						typeSpinner.setSelection(0);
					}
					else 
					{
						worker.addGetRoom(info);
						roomSpinner.setSelection(0);
					}
					
				}
			}				
		});
		return builder;
	}
	
	private View.OnClickListener timePickListener = new View.OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			final TextView tView = (TextView) v;
			String[] hm = tView.getText().toString().split(":");
			int hour = Integer.parseInt(hm[0]);
			int min = Integer.parseInt(hm[1]);
			TimePickerDialog timePickerDialog = new TimePickerDialog(ClassInfoEditActivity.this, new OnTimeSetListener(){
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute)
				{
					String timeSelect = hourOfDay+":"+minute;
					try 
					{
						if(tView.getId() == 0)
						{
							rangeSeekBar.setSelectedMinValue(simpleFormat.parse(timeSelect).getTime());
							startTimeTV.setText(simpleFormat.format(rangeSeekBar.getSelectedMinValue()));
						}
						else 
						{
							rangeSeekBar.setSelectedMaxValue(simpleFormat.parse(timeSelect).getTime());
							endTimeTV.setText(simpleFormat.format(rangeSeekBar.getSelectedMaxValue()));
						}
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}			
				}
			}, hour, min, true);
			timePickerDialog.show();
		}
	};
}
