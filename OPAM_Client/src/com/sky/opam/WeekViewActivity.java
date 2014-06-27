package com.sky.opam;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.sky.opam.fragment.Menu_Fragment;
import com.sky.opam.fragment.WeekAgenda_Fragment;
import com.sky.opam.model.Config;
import com.sky.opam.model.VersionInfo;
import com.sky.opam.task.AgendaSyncTask;
import com.sky.opam.task.CheckAppVersionTask;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
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
        setContentView(R.layout.seul_fragment);

        myApp = (MyApp)getApplication();
        numWeek = getIntent().getExtras().getInt("numWeek");       
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
        
        //class sync task
        if(worker.getConfig(myApp.getLogin()).isAutoSync) new AgendaSyncTask(this).execute();
        //check new version Info
        if(worker.getAutoUpdateNotify(this)) new CheckAppVersionTask(this, new CheckUpdateHandler()).execute();
	}
	
	private class CheckUpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle b = msg.getData();
			String vInfo = b.getString("versionInfo");
			VersionInfo versionInfo = (VersionInfo)new Gson().fromJson(vInfo, VersionInfo.class);
			AlertDialog.Builder builder = Tool.showVersionInfoAndUpdate(WeekViewActivity.this, versionInfo);
			builder.show();
		} 	
    }
	
	private void setActionBar(){
		ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, R.layout.date_dropdown_spinner_layout,getData());
        actionBar.setListNavigationCallbacks(spinnerAdapter, new  ActionBar.OnNavigationListener() {			
			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				profile_menu.showContent();
				setWeekAgenda(numWeek+position);
				return true;
			}
		});
	}
	
	private List<String> getData(){      
        List<String> data = new ArrayList<String>();
        data.add(Tool.getDateViaNumWeek(numWeek, Calendar.MONDAY)+" - "+Tool.getDateViaNumWeek(numWeek, Calendar.FRIDAY));
        data.add(Tool.getDateViaNumWeek(numWeek+1, Calendar.MONDAY)+" - "+Tool.getDateViaNumWeek(numWeek+1, Calendar.FRIDAY));        
        return data;
    }
	
	private void setWeekAgenda(int weekN){
		Config currentUserConfig = worker.getConfig(myApp.getLogin());
		WeekAgenda_Fragment fragment = new WeekAgenda_Fragment();
        Bundle b = new Bundle();
        b.putInt("startTime", currentUserConfig.startTime);
		b.putInt("endTime", currentUserConfig.endTime);
		b.putInt("numWeek", weekN);
		float time_distance = Tool.dip2px(this,50);
		b.putFloat("time_distance", time_distance);
		fragment.setArguments(b);
		
		for(int i=0; i<5; i++) fragment.setData(i+Calendar.MONDAY, worker.getClassInfo(myApp.getLogin(), weekN, i+Calendar.MONDAY));
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.seul_fragement,fragment);
		ft.commit();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == myApp.Refresh) {
        	finish();
        	startActivityForResult(getIntent(), MyApp.rsqCode);
        } else if (resultCode == myApp.Exit) {
        	setResult(MyApp.Exit);
            finish();
        }
    }
	
	@Override  
    public boolean onCreateOptionsMenu(Menu menu) { 
        MenuItemCompat.setShowAsAction(menu.add("share").setIcon(android.R.drawable.ic_menu_share), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menu.add("menu").setIcon(android.R.drawable.ic_menu_sort_by_size), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
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
		}else if (menu.getTitle().equals("share")) {
			shareAgendaView();
		}
		return super.onOptionsItemSelected(menu);
	}
	
///////////////////////////////////////////////////////////////////////////////////////////
	private View getMenuView(int menu_fragment){
		View view = LayoutInflater.from(this).inflate(menu_fragment, null);
		Menu_Fragment fragment = new Menu_Fragment();
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.menu_fragement,fragment);
		ft.commit();
		return view;
	}
	
	long exitTime = 0;
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                	Tool.showInfo(this, "one more time to exit");
                    exitTime = System.currentTimeMillis();
                } else {
                    setResult(MyApp.Exit);
                    finish();
                }
            }
            return true;
	    }else if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	if (profile_menu.isMenuShowing()) {
				profile_menu.showContent();
			}else {
				profile_menu.showMenu();
			}
	    	return true;
		}else {
        	return super.onKeyDown(keyCode, event);
	    }
    }

	@Override
	protected void onRestart() {
		super.onRestart();
		profile_menu.showContent();
	}
	
	private void shareAgendaView(){
		WeekAgenda_Fragment fragment = (WeekAgenda_Fragment) getSupportFragmentManager().findFragmentById(R.id.seul_fragement);
		if(fragment.getNumClassInfo() == 0){
			Tool.showInfo(WeekViewActivity.this, getResources().getString(R.string.zero_course));
			return;
		}
		int fragmentNumWeek = fragment.getNumWeek();
		View view = fragment.getView();
		Bitmap bitmap = Tool.ViewToBitmap(view);
		
		String imgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "opam.jpg";
		File document = new File(imgPath);
		if(document.exists()) document.delete();
        try {  
            FileOutputStream fos = new FileOutputStream(document);  
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);// 把数据写入文件  
        } catch (Exception e) {  
            e.printStackTrace();  
        }
        
        setWeekAgenda(fragmentNumWeek); //reset agenda view
        
        Uri uri = Uri.fromFile(new File(imgPath));
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(getResources().getString(R.string.my_class_from));
		sBuilder.append(Tool.getDateViaNumWeek(numWeek, Calendar.MONDAY));
		sBuilder.append(getResources().getString(R.string.to));
		sBuilder.append(Tool.getDateViaNumWeek(numWeek, Calendar.FRIDAY));
		sBuilder.append(" (");
		sBuilder.append(getResources().getString(R.string.come_from));
		sBuilder.append(getResources().getString(R.string.app_name));
		sBuilder.append(" -https://play.google.com/store/apps/details?id=com.sky.opam");		
		sBuilder.append(").");
		intent.putExtra(Intent.EXTRA_TEXT, sBuilder.toString());//附带的说明信息  
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));  
        intent.setType("image/*");   //分享图片  
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_title)));
	}
}
