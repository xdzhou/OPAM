package com.sky.opam;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.sky.opam.fragment.FragementClassAdapter;
import com.sky.opam.fragment.Menu_Fragment;
import com.sky.opam.tool.DBworker;
import com.viewpagerindicator.TitlePageIndicator;

public class AgendaTabActivity extends ActionBarActivity {
	private SlidingMenu profile_menu;
	private ViewPager mPager;
	private String login;
	private int numweek;
	private DBworker worker;
	private boolean isShowTW = true;
	private int todayPosition;
	private MenuItem selectMenuItem;
	private static Interpolator interp = new Interpolator() {
		@Override
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t + 1.0f;
		}		
	};
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.agenda_tab);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        
        login = (String) getIntent().getExtras().get("login");
        numweek = Integer.parseInt((String) getIntent().getExtras().get("numweek"));
        worker = new DBworker(getApplicationContext());
        
        FragementClassAdapter mAdapter = new FragementClassAdapter(getSupportFragmentManager(), login, numweek);
        todayPosition = mAdapter.getTodayPosition();
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(todayPosition);
        
        TitlePageIndicator mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
            @Override
            public void onPageSelected(int arg0) {
                if (arg0 == 4) {
                    isShowTW = true;
                    selectMenuItem.setIcon(getResources().getDrawable(R.drawable.icon_next));
                }
                if (arg0 == 5) {
                    isShowTW = false;
                    selectMenuItem.setIcon(getResources().getDrawable(R.drawable.icon_previous));
                }
            }
        });
        
        profile_menu = new SlidingMenu(this);
        profile_menu.setMode(SlidingMenu.RIGHT);
        profile_menu.setShadowWidthRes(R.dimen.shadow_width);
        profile_menu.setShadowDrawable(R.drawable.shadow);
        profile_menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
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
	
	@Override  
    public boolean onCreateOptionsMenu(Menu menu) { 
		selectMenuItem = menu.add("select").setIcon(R.drawable.icon_next);
        MenuItemCompat.setShowAsAction(selectMenuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
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
			profile_menu.showContent();
			if (isShowTW) {
				menu.setIcon(getResources().getDrawable(R.drawable.icon_previous));
                mPager.setCurrentItem(5);
                isShowTW = false;
	        } else {
	        	menu.setIcon(getResources().getDrawable(R.drawable.icon_next));
	                mPager.setCurrentItem(todayPosition);
	                isShowTW = true;
	        }
		}
		return super.onOptionsItemSelected(menu);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	private View getMenuView(int menu_fragment){
		View view = LayoutInflater.from(this).inflate(menu_fragment, null);
		Menu_Fragment fragment = new Menu_Fragment(login, worker.findUser(login).getUsename());
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.menu_fragment,fragment);
		ft.commit();
		return view;
	}
}