package com.sky.opam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

import com.sky.opam.R;
import com.sky.opam.abstractActivity.NotChatActivity;
import com.sky.opam.model.Cours;
import com.sky.opam.outil.DBworker;

public class AgendaActivity extends NotChatActivity implements OnTouchListener{
	String login;
	String numweek;
	TabHost monTH;
	int currentTabIndi;
	PopupWindow popMenu;
	DBworker worker;
	Map<String, Float> classTypeMap = new HashMap<String, Float>();

	public static final int SNAP_VELOCITY = 200; //slide vitess
	private int screenWidth; // widthe of the screen
	private int leftEdge; // max left side of the menu
	private int rightEdge = 0; // max rignt side of the menu
	private int menuPadding = 140;
	private View content;
	private View menu;
	private LinearLayout.LayoutParams menuParams;
	private boolean isMenuVisible=false;
	private float xDown;
	private float xMove;
	private float xUp;
	private VelocityTracker mVelocityTracker;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.agenda);       
        
        iniContent();
        initValues();
        //content.setOnTouchListener(this);
        menu.setOnTouchListener(this);
        iniMenu();
        
    }
	
	private void iniContent(){
		login = (String) getIntent().getExtras().get("login");
        numweek = (String) getIntent().getExtras().get("numweek");
        worker = new DBworker(getApplicationContext());
        String username = worker.findUser(login).getUsename();
        TextView tv = (TextView) findViewById(R.id.top_title);
        tv.setText(username);
        tv = (TextView) findViewById(R.id.top_numweek);
        String top_numweek;
        if(numweek.equals("1")){
        	top_numweek="1st";
        }else if (numweek.equals("2")) {
        	top_numweek="2nd";
		}else {
			top_numweek=numweek+"th";
		}
        top_numweek="course of the "+top_numweek+" week";
        tv.setText(top_numweek);

        Intent[] intents = new Intent [5];
        
        intents[0] = new Intent().setClass(AgendaActivity.this, DynamicClassActivity.class);
        Bundle b = new Bundle();  
        b.putString("login", login);
        b.putString("flag", numweek+"_1");
        b.putString("isToday", "0");
        intents[0].putExtras(b);
        
        intents[1] = new Intent().setClass(AgendaActivity.this, DynamicClassActivity.class);
        b.putString("flag", numweek+"_2"); 
        intents[1].putExtras(b);
        
        intents[2] = new Intent().setClass(AgendaActivity.this, DynamicClassActivity.class);
        b.putString("flag", numweek+"_3"); 
        intents[2].putExtras(b);
        
        intents[3] = new Intent().setClass(AgendaActivity.this, DynamicClassActivity.class);
        b.putString("flag", numweek+"_4"); 
        intents[3].putExtras(b);
        
        intents[4] = new Intent().setClass(AgendaActivity.this, DynamicClassActivity.class);
        b.putString("flag", numweek+"_5"); 
        intents[4].putExtras(b);
        
        monTH = getTabHost();
        
        int xq = getDayWeek();
        if(xq<6){
        	b.putString("flag", numweek+"_"+xq);
        	b.putString("isToday", "1");
        	intents[xq-1].putExtras(b);
        }
        
        //monTH.setup();
        TabSpec spec = monTH.newTabSpec("onglet1");
        spec.setIndicator(getTabDate(Calendar.MONDAY),getResources().getDrawable(R.drawable.lun_select));
        spec.setContent(intents[0]);
        monTH.addTab(spec);
        monTH.addTab(monTH.newTabSpec("onglet2").setIndicator(getTabDate(Calendar.TUESDAY),getResources().getDrawable(R.drawable.mar_select)).setContent(intents[1]));
        monTH.addTab(monTH.newTabSpec("onglet3").setIndicator(getTabDate(Calendar.WEDNESDAY),getResources().getDrawable(R.drawable.mer_select)).setContent(intents[2]));
        monTH.addTab(monTH.newTabSpec("onglet4").setIndicator(getTabDate(Calendar.THURSDAY),getResources().getDrawable(R.drawable.jeu_select)).setContent(intents[3]));
        monTH.addTab(monTH.newTabSpec("onglet5").setIndicator(getTabDate(Calendar.FRIDAY),getResources().getDrawable(R.drawable.ven_select)).setContent(intents[4]));  
        currentTabIndi= (xq>5)?4:xq-1;
        monTH.setCurrentTab(currentTabIndi);      
        
        // the menu list button
        ImageView update_button = (ImageView) findViewById(R.id.top_btn_left);
        update_button.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(isMenuVisible){
	    			new ScrollTask().execute(-30);
	    		}else {
	    			new ScrollTask().execute(30);
				}
			}
		});
        
        //the next week button
        ImageView next_button = (ImageView) findViewById(R.id.top_btn_right);
        next_button.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
		        intent.setClass(AgendaActivity.this, AgendaNextWeekActivity.class);
		        Bundle bundle = new Bundle();  
		        bundle.putString("login", login); 
		        bundle.putString("numweek", numweek);
		        intent.putExtras(bundle);
		        startActivityForResult(intent, 0);
			}
		});
	}	
	
	//set the background of all the tabs
	private void setColorToAllTab(){
		for (int i = 0; i < monTH.getTabWidget().getChildCount(); i++) {
        	monTH.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_select));
        }
	}
	
	@Override
	public void showInfo(String msg){
		Toast.makeText(AgendaActivity.this, msg, Toast.LENGTH_SHORT).show();        
    }
	
	private int getDayWeek(){
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int xq = c.get(Calendar.DAY_OF_WEEK);
		if(xq==1){
			return 7;
		}else {
			return xq-1;
		}
	}
	
	private String getTabDate(int dayOfWeek){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int nw = Integer.parseInt(numweek);
	  	cal.set(Calendar.WEEK_OF_YEAR,nw);
	  	cal.set(Calendar.DAY_OF_WEEK,dayOfWeek);
	  	cal.set(Calendar.WEEK_OF_YEAR,nw);
	  	String date="";
	  	date += cal.get(Calendar.DAY_OF_MONTH);
	  	date += "/";
	  	int month = cal.get(Calendar.MONTH)+1;
	  	date += month;
	  	return date;
	}	
	
	//reponse to the update request
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==0 && resultCode!=0){
			if(resultCode==11){
				backToToday(); return;
			}
			if(resultCode==5){
				Intent intent = new Intent();
		        intent.setClass(AgendaActivity.this, OnlineActivity.class);
		        startActivity(intent);
				return;
			}
			setResult(resultCode);
	        finish();   
		}
	}
	
	//catch the back event
	long exitTime = 0;
	@Override 
	public boolean dispatchKeyEvent(KeyEvent event) {		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){ 
			if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
				if((System.currentTimeMillis()-exitTime) > 2000){  
		            Toast.makeText(getApplicationContext(), "one more time to exit", Toast.LENGTH_SHORT).show();                                
		            exitTime = System.currentTimeMillis(); 
		        } else {
		        	setResult(1);
		            finish();
		        }
			}
			 return true;   
	    }else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
	    	if (event.getAction() == KeyEvent.ACTION_DOWN) {
	    		if(isMenuVisible){
	    			new ScrollTask().execute(-30);
	    		}else {
	    			new ScrollTask().execute(30);
				}
	    	}
	    	return true;
		}else {
	    	return super.dispatchKeyEvent(event);  	
		}
	    
	}
	
	private void iniPopMenu(){
		if(popMenu==null){
			System.out.println("got it 1");
			View popupWindow_view = getLayoutInflater().inflate(R.layout.pop, null,false);
			TableLayout tableLayout=(TableLayout) popupWindow_view.findViewById(R.id.poptl);
			TextView pop_title = (TextView) popupWindow_view.findViewById(R.id.pop_title);
			String title;
			if(numweek.equals("1")){
				title="course info (1st week)";
			}else if (numweek.equals("2")) {
				title="course info (2nd week)";
			}else {
				title="course info ("+numweek+"th week)";
			}
			pop_title.setText(title);
			Iterator<String> itor = classTypeMap.keySet().iterator();
			
			while(itor.hasNext()){
				String type = itor.next();
				if(type.equals("total")){
					continue;
				}
				tableLayout.addView(getRow(type, classTypeMap.get(type)));
			}
			//TODO: add a line horisontal
			tableLayout.addView(getRow("total", classTypeMap.get("total")));

			popMenu = new PopupWindow(popupWindow_view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
			popMenu.setAnimationStyle(R.style.AnimationFade);
			popupWindow_view.setOnTouchListener(new View.OnTouchListener() {
				@Override   
				public boolean onTouch(View v, MotionEvent event) {    
				if (popMenu != null && popMenu.isShowing()) {   
					popMenu.dismiss();   
					popMenu = null;   
				}   
				return false;   
				} 
			});
			popMenu.setOutsideTouchable(false);
			popMenu.update();
			popMenu.showAtLocation(this.findViewById(R.id.top_title), Gravity.CENTER, 0, 0);
		}else {
			System.out.println("got it 2");
			popMenu.dismiss();
			popMenu = null;
		}
	}
	
	private TableRow getRow(String type, Float value){
		TableRow row = new TableRow(this);
		//row.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_select));
		row.setGravity(Gravity.CENTER);
		TextView tx = new TextView(this);
		tx.setPadding(0, 0, 8, 0);
		tx.setTextColor(Color.rgb(1, 0, 0));
		tx.setText(type);tx.setTextSize(20);
		if(type.equals("total")){
			 tx.getPaint().setFakeBoldText(true);
		}
		row.addView(tx);
		tx = new TextView(this);
		tx.setPadding(8, 0, 0, 0);
		tx.setText(classTypeMap.get(type)+" h");tx.setTextSize(20);
		tx.setTextColor(Color.rgb(69, 18, 241));
		if(type.equals("total")){
			tx.setTextColor(Color.rgb(216, 18, 241));
			tx.getPaint().setFakeBoldText(true);
			row.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_select));
		}
		row.addView(tx);
		return row;
	}

////////////////////////////////////////side menu///////////////////////////////////////////////
	
	private void iniMenu(){
		SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.menu_list_item,
                new String[]{"menu_icon","menu_title"},
                new int[]{R.id.menu_icon,R.id.menu_title});
		ListView menulist = (ListView) menu;
		menulist.setAdapter(adapter);
		
		menulist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				new ScrollTask().execute(-30);
				if(arg2==0){
					backToToday();
				}else if (arg2==1) {
					genererStaticInfo();
					iniPopMenu();
				}else if (arg2==2) {
					setResult(22);
			        finish();
				}else if(arg2==3){
					Intent intent = new Intent();
			        intent.setClass(AgendaActivity.this, OnlineActivity.class);
			        startActivity(intent);
				}else if (arg2==4) {
					finish();
				}else {
					setResult(1);
			        finish();
				}
			}
		});
	}
	private void backToToday(){
		monTH.setCurrentTab(currentTabIndi);
        setColorToAllTab();
        monTH.getTabWidget().getChildAt(currentTabIndi).setBackgroundDrawable(getResources().getDrawable(R.drawable.new_bg));
	}
	private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("menu_icon", R.drawable.gototoday);
        map.put("menu_title", "go to today");
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put("menu_icon", R.drawable.staticinfo);
        map.put("menu_title", "course info");
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put("menu_icon", R.drawable.icon_update);
        map.put("menu_title", "update");
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put("menu_icon", R.drawable.icon_chat);
        map.put("menu_title", "chat");
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put("menu_icon", R.drawable.icon_account);
        map.put("menu_title", "account");
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put("menu_icon", R.drawable.icon_exit);
        map.put("menu_title", "exit");
        list.add(map);
         
        return list;
    }
	
	@SuppressLint("SimpleDateFormat")
	private void genererStaticInfo(){
		if(classTypeMap.containsKey("total")){
			return;
		}
		List<Cours> TWClassList = worker.findClass(login, Integer.parseInt(numweek));
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		Date fin = null,debut = null;
		float totalMin=0, dure=0;
		
		for(int i=0; i<TWClassList.size();i++){
			String type = getClassType(TWClassList.get(i).type);
			if(!TWClassList.get(i).debut.equals("")){
				try {
					debut = formatter.parse(TWClassList.get(i).debut);
					fin = formatter.parse(TWClassList.get(i).fin);
					dure = (float) ((fin.getTime()-debut.getTime())/1000/60/60.0);
					totalMin += dure;
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if(classTypeMap.containsKey(type)){
				classTypeMap.put(type, dure+classTypeMap.get(type));
			}else {
				classTypeMap.put(type, dure);
			}
		}
		classTypeMap.put("total", totalMin);
	}
	private String getClassType(String type){
		if(type.equals("Examen CF2")){
			return "CF2";
		}else if (type.equals("Examen CF1")) {
			return "CF1";
		}else if (type.contains("Point de Rencontre")) {
			return "Point de Rencontre";
		}else {
			return type;
		}
	}
	
	//initition the values
	private void initValues() {
		WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		screenWidth = window.getDefaultDisplay().getWidth();
		content = findViewById(R.id.content);
		menu = findViewById(R.id.menu);
		menuParams = (LinearLayout.LayoutParams) menu.getLayoutParams();
		menuPadding = screenWidth/2;
		// 将menu的宽度设置为屏幕宽度减去menuPadding
		menuParams.width = screenWidth - menuPadding;
		// 左边缘的值赋值为menu宽度的负数
		leftEdge = -menuParams.width;
		// menu的leftMargin设置为左边缘的值，这样初始化时menu就变为不可见
		menuParams.leftMargin = leftEdge;
		// 将content的宽度设置为屏幕宽度
		content.getLayoutParams().width = screenWidth;
	}
	
	//show and hide menu async task
	class ScrollTask extends AsyncTask<Integer, Integer, Integer> {
		@Override
		protected Integer doInBackground(Integer... speed) {
			int leftMargin = menuParams.leftMargin;
			// 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
			while (true) {
				leftMargin = leftMargin + speed[0];
				if (leftMargin > rightEdge) {
					leftMargin = rightEdge;
					break;
				}if (leftMargin < leftEdge) {
					leftMargin = leftEdge;
					break;
				}
				publishProgress(leftMargin);
				// 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
				sleep(20);
			}
			if (speed[0] > 0) {
				isMenuVisible = true;
			} else {
				isMenuVisible = false;
			}
			return leftMargin;
		}
		@Override
		protected void onProgressUpdate(Integer... leftMargin) {
			menuParams.leftMargin = leftMargin[0];
			menu.setLayoutParams(menuParams);
		}
		@Override
		protected void onPostExecute(Integer leftMargin) {
			menuParams.leftMargin = leftMargin;
			menu.setLayoutParams(menuParams);
		}
	}
	
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		createVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("activity down");
			xDown = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			// 手指移动时，对比按下时的横坐标，计算出移动的距离，来调整menu的leftMargin值，从而显示和隐藏menu
			xMove = event.getRawX();
			int distanceX = (int) (xMove - xDown);
			if (isMenuVisible) {
				menuParams.leftMargin = distanceX;
			} else {
				menuParams.leftMargin = leftEdge + distanceX;
			}
			if (menuParams.leftMargin < leftEdge) {
				menuParams.leftMargin = leftEdge;
			} else if (menuParams.leftMargin > rightEdge) {
				menuParams.leftMargin = rightEdge;
			}
			menu.setLayoutParams(menuParams);
			break;
		case MotionEvent.ACTION_UP:
			// 手指抬起时，进行判断当前手势的意图，从而决定是滚动到menu界面，还是滚动到content界面
			xUp = event.getRawX();
			if (wantToShowMenu()) {
				if (shouldScrollToMenu()) {
					scrollToMenu();
				} else {
					scrollToContent();
				}
			} else if (wantToShowContent()) {
				if (shouldScrollToContent()) {
					scrollToContent();
				} else {
					scrollToMenu();
				}
			}
			recycleVelocityTracker();
			break;
		}
		return false;
	}
	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}
	private boolean wantToShowContent() {
		return xUp - xDown < 0 && isMenuVisible;
	}

	private boolean wantToShowMenu() {
		return xUp - xDown > 0 && !isMenuVisible;
	}

	private boolean shouldScrollToMenu() {
		return xUp - xDown > screenWidth / 3 || getScrollVelocity() > SNAP_VELOCITY;
	}

	private boolean shouldScrollToContent() {
		return xDown - xUp + menuPadding > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	private void scrollToMenu() {
		new ScrollTask().execute(30);
	}

	private void scrollToContent() {
		new ScrollTask().execute(-30);
	}
	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}

	@Override
	public void increaseNum(String userID) {
		((MyApplication) this.getApplicationContext()).increaseNumUnread(userID);
	}

}
