package com.sky.opam;

import com.sky.opam.fragment.WeekAgenda_Fragment;
import com.sky.opam.model.Cours;
import com.sky.opam.tool.Tool;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class TestActivity extends FragmentActivity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        
        WeekAgenda_Fragment fragment = new WeekAgenda_Fragment();
        Bundle b = new Bundle();
        b.putInt("startTime", 8);
		b.putInt("endTime", 19);
		float time_distance = Tool.dip2px(this,50);
		b.putFloat("time_distance", time_distance);
		fragment.setArguments(b);
		
		
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.agenda_fragement,fragment);
		ft.commit();
        
	}
}
