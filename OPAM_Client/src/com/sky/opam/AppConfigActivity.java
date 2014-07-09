package com.sky.opam;

import com.sky.opam.model.Config;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Util;
import com.sky.opam.view.RangeSeekBar;
import com.sky.opam.view.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AppConfigActivity extends ActionBarActivity{
	private MyApp myApp;
	private DBworker worker;
	private Config config;
	
	private TextView startTimeTV;
	private TextView endTimeTV;
	private ToggleButton autoSyncTB;
	private ToggleButton autoLoginTB;
	private ToggleButton autoUpdateNotifyTB;
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
        startTimeTV.setText(Util.getTime(config.startTime));
        endTimeTV = (TextView)findViewById(R.id.endTimeTV);
        endTimeTV.setText(Util.getTime(config.endTime));
        TextView VersionTV = (TextView)findViewById(R.id.versionTV);
        String version = Util.getVersionName(this);
        if(version != null) VersionTV.setText(version);
        
        autoSyncTB = (ToggleButton)findViewById(R.id.autoSyncTB);
        autoSyncTB.setChecked(config.isAutoSync);
        autoLoginTB = (ToggleButton)findViewById(R.id.autoLogin);
        autoLoginTB.setChecked(worker.getAutoLogin(this));
        autoUpdateNotifyTB = (ToggleButton)findViewById(R.id.autoUpdateNotify);
        autoUpdateNotifyTB.setChecked(worker.getAutoUpdateNotify(this));
        
        rangeSeekBar = new RangeSeekBar<Integer>(0, 24, this);
        rangeSeekBar.setNotifyWhileDragging(true);
        rangeSeekBar.setSelectedMinValue(config.startTime);
		rangeSeekBar.setSelectedMaxValue(config.endTime);
        root.addView(rangeSeekBar, 1);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,Integer minValue, Integer maxValue) {
				startTimeTV.setText(Util.getTime(minValue));
				endTimeTV.setText(Util.getTime(maxValue));
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
			worker.setAutoLogin(AppConfigActivity.this, autoLoginTB.isChecked());
			worker.setAutoUpdateNotify(AppConfigActivity.this, autoUpdateNotifyTB.isChecked());
			setResult(MyApp.Refresh);
			finish();
		}
		return super.onOptionsItemSelected(menu);
	}
}
