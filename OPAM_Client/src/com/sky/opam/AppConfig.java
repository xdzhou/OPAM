package com.sky.opam;

import java.util.Date;

import com.sky.opam.model.Config;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;
import com.sky.opam.view.RangeSeekBar;
import com.sky.opam.view.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AppConfig extends ActionBarActivity{
	private MyApp myApp;
	private DBworker worker;
	private Config config;
	
	private TextView startTimeTV;
	private TextView endTimeTV;
	private ToggleButton autoSyncTB;
	private RangeSeekBar<Integer> rangeSeekBar;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (MyApp)getApplication();
        worker = new DBworker(this);
        config = worker.getConfig(myApp.getLogin());
        setContentView(R.layout.config_activity);
        
        LinearLayout root = (LinearLayout)findViewById(R.id.root_layout);
        startTimeTV = (TextView)findViewById(R.id.startTimeTV);
        startTimeTV.setText(Tool.getTime(config.startTime));
        endTimeTV = (TextView)findViewById(R.id.endTimeTV);
        endTimeTV.setText(Tool.getTime(config.endTime));
        TextView VersionTV = (TextView)findViewById(R.id.versionTV);
        String version = null;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        if(version!=null) VersionTV.setText(version);
        
        autoSyncTB = (ToggleButton)findViewById(R.id.autoSyncTB);
        autoSyncTB.setChecked(config.isAutoSync);
        
        rangeSeekBar = new RangeSeekBar<Integer>(0, 24, this);
        rangeSeekBar.setNotifyWhileDragging(true);
        rangeSeekBar.setSelectedMinValue(config.startTime);
		rangeSeekBar.setSelectedMaxValue(config.endTime);
        root.addView(rangeSeekBar, 1);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,Integer minValue, Integer maxValue) {
				startTimeTV.setText(Tool.getTime(minValue));
				endTimeTV.setText(Tool.getTime(maxValue));
			}		
		});
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
			finish();
		}else if (menu.getTitle().equals("save")) {
			config.startTime = rangeSeekBar.getSelectedMinValue();
			config.endTime = rangeSeekBar.getSelectedMaxValue();
			config.isAutoSync = autoSyncTB.isChecked();
			worker.updateConfig(config);
			setResult(MyApp.Refresh);
			finish();
		}
		return super.onOptionsItemSelected(menu);
	}
}
