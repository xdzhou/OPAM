package com.sky.opam;

import java.util.ArrayList;
import java.util.List;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.sky.opam.fragment.Menu_Fragment;
import com.sky.opam.fragment.WeekAgenda_Fragment;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public class WeekViewActivity extends ActionBarActivity{
	private SlidingMenu profile_menu;
	private static Interpolator interp = new Interpolator() {
		@Override
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t + 1.0f;
		}		
	};
	private MyApp myApp;
	private DBworker worker;
	private int numWeek;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        myApp = (MyApp)getApplication();
        numWeek = (Integer) getIntent().getExtras().get("numWeek");       
        worker = new DBworker(this);
        
        setActionBar();  
        setWeekAgenda(numWeek);
		
		profile_menu = new SlidingMenu(this);
        profile_menu.setMode(SlidingMenu.RIGHT);
        profile_menu.setShadowWidthRes(R.dimen.shadow_width);
        profile_menu.setShadowDrawable(R.drawable.shadow);
        profile_menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        profile_menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        profile_menu.setFadeDegree(0.35f);
        profile_menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        profile_menu.setBehindCanvasTransformer(new CanvasTransformer() {
        	@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				canvas.translate(0, canvas.getHeight()*(1-interp.getInterpolation(percentOpen)));
			}			
		});
        profile_menu.setMenu(getMenuView(R.layout.menu_fragment));
	}
	
	private void setActionBar(){
		ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,getData());
        actionBar.setListNavigationCallbacks(spinnerAdapter, new  ActionBar.OnNavigationListener() {			
			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				setWeekAgenda(numWeek+position);
				return true;
			}
		});
	}
	
	private List<String> getData(){      
        List<String> data = new ArrayList<String>();
        data.add(numWeek+"th week");
        data.add((numWeek+1)+"th week");
        data.add((numWeek+2)+"th week");
        data.add((numWeek+3)+"th week");        
        return data;
    }
	
	private void setWeekAgenda(int weekN){
		WeekAgenda_Fragment fragment = new WeekAgenda_Fragment();
        Bundle b = new Bundle();
        b.putInt("startTime", 8);
		b.putInt("endTime", 19);
		b.putInt("numWeek", weekN);
		float time_distance = Tool.dip2px(this,50);
		b.putFloat("time_distance", time_distance);
		fragment.setArguments(b);
		
		for(int i=1; i<=5; i++) fragment.setData(i, worker.findClassInfo(myApp.getLogin(), weekN, i));
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.agenda_fragement,fragment);
		ft.commit();
	}
	
	@Override  
    public boolean onCreateOptionsMenu(Menu menu) { 
        //MenuItemCompat.setShowAsAction(menu.add("select").setIcon(R.drawable.icon_next), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menu.add("menu").setIcon(R.drawable.menu_list), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        return true;  
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem menu) {
		if(menu.getTitle().equals("menu")){
			if (profile_menu.isMenuShowing()) {
				profile_menu.showContent();
			}else {
				profile_menu.showMenu();
			}
		}else if (menu.getTitle().equals("select")) {
			
		}
		return super.onOptionsItemSelected(menu);
	}
	
///////////////////////////////////////////////////////////////////////////////////////////
	private View getMenuView(int menu_fragment){
		View view = LayoutInflater.from(this).inflate(menu_fragment, null);
		Menu_Fragment fragment = new Menu_Fragment();
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.menu_fragment,fragment);
		ft.commit();
		return view;
	}
}
