package com.sky.opam.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.loic.common.LibApplication;
import com.loic.common.graphic.RangeSeekBar;
import com.loic.common.graphic.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.model.User;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.Tool;

public class SettingFragment extends OpamFragment
{
    private static final String TAG = SettingFragment.class.getSimpleName();
    
    private RangeSeekBar<Integer> rangeSeekBar;
    private ToggleButton autoSyncTB;
    private ToggleButton autoLoginTB;
    private ToggleButton autoUpdateNotifyTB;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        user = DBworker.getInstance().getDefaultUser();
        if(user == null)
        {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        getActivity().setTitle(getString(R.string.OA0002));
        View rootView =  inflater.inflate(R.layout.config_activity, container, false);
        
        final TextView startTimeTV = (TextView) rootView.findViewById(R.id.startTimeTV);
        startTimeTV.setText(getTime(user.agendaStartHour));
        final TextView endTimeTV = (TextView) rootView.findViewById(R.id.endTimeTV);
        endTimeTV.setText(getTime(user.agendaEndHour));
        
        rangeSeekBar = new RangeSeekBar<Integer>(0, 24, getActivity());
        rangeSeekBar.setNotifyWhileDragging(true);
        rangeSeekBar.setSelectedMinValue(7);
        rangeSeekBar.setSelectedMaxValue(21);
        LinearLayout root = (LinearLayout)rootView.findViewById(R.id.root_layout);
        root.addView(rangeSeekBar, 1);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() 
        {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,Integer minValue, Integer maxValue) 
            {
                startTimeTV.setText(getTime(minValue));
                endTimeTV.setText(getTime(maxValue));
            }        
        });
        rangeSeekBar.setEnabled(false);
        
        autoSyncTB = (ToggleButton) rootView.findViewById(R.id.autoSyncTB);
        autoSyncTB.setChecked(false);
        autoSyncTB.setEnabled(false);
        autoLoginTB = (ToggleButton) rootView.findViewById(R.id.autoLogin);
        autoLoginTB.setChecked(user.isAutoConnect);
        autoUpdateNotifyTB = (ToggleButton) rootView.findViewById(R.id.autoUpdateNotify);
        autoUpdateNotifyTB.setChecked(isAutoNotifi());
        
        TextView VersionTV = (TextView) rootView.findViewById(R.id.versionTV);
        String version = Tool.getVersionName();
        if(version != null) 
            VersionTV.setText(version);
        
        setHasOptionsMenu(true);
        
        return rootView;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        MenuItem saveMI = menu.add("save").setIcon(android.R.drawable.ic_menu_save).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        saveMI.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
        {
            @Override
            public boolean onMenuItemClick(MenuItem item) 
            {
                saveParams();
                return false;
            }
        });
    }
    
    private void saveParams()
    {
        if(user != null)
        {
            user.agendaStartHour = rangeSeekBar.getSelectedMinValue();
            user.agendaEndHour = rangeSeekBar.getSelectedMaxValue();
            user.isAutoSyncEvent = autoSyncTB.isChecked();
            user.isAutoConnect = autoLoginTB.isChecked();
            
            DBworker.getInstance().updateData(user);
            
            setAutoNotifi(autoUpdateNotifyTB.isChecked());
            
            ToastUtils.show("Parameter Saved !");
        }
    }
    
    private String getTime(int hour)
    {
        String h = hour < 10 ? "0"+hour : hour+"";
        return h+":00";
    }
    
    private boolean isAutoNotifi()
    {
        SharedPreferences sharedPref = LibApplication.getAppContext().getSharedPreferences(SettingFragment.class.getName(), Context.MODE_PRIVATE);
        return sharedPref.getBoolean("isAutoNotifi", true);
    }
    
    private void setAutoNotifi(boolean autoNotifi)
    {
        SharedPreferences sharedPref = LibApplication.getAppContext().getSharedPreferences(SettingFragment.class.getName(), Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean("isAutoNotifi", autoNotifi).apply();
    }
}
