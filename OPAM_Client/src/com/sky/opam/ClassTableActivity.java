package com.sky.opam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

import com.sky.opam.adapter.FragementClassAdapter;
import com.sky.opam.model.Cours;
import com.sky.opam.outil.DBworker;
import com.viewpagerindicator.TitlePageIndicator;

public class ClassTableActivity extends FragmentActivity {
	String login;
	int numweek;
	TextView tv;
	ImageView indi_week_btn;
	ViewPager mPager;
	int todayPosition;
	PopupWindow popMenu;
	DBworker worker;
	boolean isShowTW = true;
	Map<String, Float[]> classTypeMap = new HashMap<String, Float[]>();

	public static final int SNAP_VELOCITY = 200; // slide vitess
	private int screenWidth; // widthe of the screen
	private int leftEdge; // max left side of the menu
	private int rightEdge = 0; // max rignt side of the menu
	private int menuPadding = 140;
	private View content;
	private View menu;
	private LinearLayout.LayoutParams menuParams;
	private boolean isMenuVisible = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_page);

		iniContent();
		initValues();
		iniMenu();
	}

	private void iniContent() {
		login = (String) getIntent().getExtras().get("login");
		numweek = Integer.parseInt((String) getIntent().getExtras().get("numweek"));
		worker = new DBworker(getApplicationContext());
		String username = worker.findUser(login).getUsename();
		tv = (TextView) findViewById(R.id.top_title);
		tv.setText(username);
		tv = (TextView) findViewById(R.id.top_numweek);
		tv.setText(getTopNumWeek(numweek));
		indi_week_btn = (ImageView) findViewById(R.id.top_btn_flag);
		indi_week_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isShowTW) {
					tv.setText(getTopNumWeek(numweek + 1));
					indi_week_btn.setImageDrawable(getResources().getDrawable(R.drawable.icon_previous));
					mPager.setCurrentItem(5);
					isShowTW = false;
				} else {
					tv.setText(getTopNumWeek(numweek));
					indi_week_btn.setImageDrawable(getResources().getDrawable(R.drawable.icon_next));
					mPager.setCurrentItem(todayPosition);
					isShowTW = true;
				}
			}
		});

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
					tv.setText(getTopNumWeek(numweek));
					indi_week_btn.setImageDrawable(getResources()
							.getDrawable(R.drawable.icon_next));
				}
				if (arg0 == 5) {
					isShowTW = false;
					tv.setText(getTopNumWeek(numweek + 1));
					indi_week_btn.setImageDrawable(getResources()
							.getDrawable(R.drawable.icon_previous));
				}
			}
		});

		// the menu list button
		ImageView update_button = (ImageView) findViewById(R.id.top_btn_left);
		update_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isMenuVisible) {
					new ScrollTask().execute(-30);
				} else {
					new ScrollTask().execute(30);
				}
			}
		});
	}

	private void initValues() {
		WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		screenWidth = window.getDefaultDisplay().getWidth();
		content = findViewById(R.id.content);
		menu = findViewById(R.id.menu);
		menuParams = (LinearLayout.LayoutParams) menu.getLayoutParams();
		menuPadding = screenWidth / 2;
		// 将menu的宽度设置为屏幕宽度减去menuPadding test
		menuParams.width = screenWidth - menuPadding;
		// 左边缘的值赋值为menu宽度的负数
		leftEdge = -menuParams.width;
		// menu的leftMargin设置为左边缘的值，这样初始化时menu就变为不可见
		menuParams.leftMargin = leftEdge;
		// 将content的宽度设置为屏幕宽度
		content.getLayoutParams().width = screenWidth;
	}

	private void iniMenu() {
		SimpleAdapter adapter = new SimpleAdapter(this, getData(),
				R.layout.menu_list_item, new String[] { "menu_icon",
						"menu_title" }, new int[] { R.id.menu_icon,
						R.id.menu_title });
		ListView menulist = (ListView) findViewById(R.id.menu_list);
		menulist.setAdapter(adapter);

		CheckBox cbox = (CheckBox) findViewById(R.id.flag_sync_calendar);
		boolean sync_flag = worker.isCalendarSynced(login);
		if (sync_flag)
			new SyncCalendarTask().execute();
		cbox.setChecked(sync_flag);
		cbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				worker.setCalendarSynced(login, isChecked);
				if (isChecked)
					new SyncCalendarTask().execute();
			}

		});

		menulist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				new ScrollTask().execute(-30);
				if (position == 0) {
					mPager.setCurrentItem(todayPosition);
				} else if (position == 1) {
					genererStaticInfo();
					iniPopMenu();
				} else if (position == 2) {
					setResult(22);
					finish();
				} else if (position == 3) {
					finish();
				} else {
					setResult(1);
					finish();
				}
			}
		});
	}

	@SuppressLint("SimpleDateFormat")
	private void genererStaticInfo() {
		if (classTypeMap.containsKey("total")) {
			return;
		}
		List<Cours> ClassList = worker.findClass(login);
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		Date fin = null, debut = null;
		int indiWeek = 0;
		float dure = 0;
		Float[] totalMin = new Float[2];
		totalMin[0] = totalMin[1] = (float) 0.0;

		for (int i = 0; i < ClassList.size(); i++) {
			String type = getClassType(ClassList.get(i).type);
			String[] nw = ClassList.get(i).position.split("_");
			if (nw[0].equals("" + numweek))
				indiWeek = 0;
			else
				indiWeek = 1;
		//System.out.println(indiWeek);
			if (!ClassList.get(i).debut.equals("")) {
				try {
					debut = formatter.parse(ClassList.get(i).debut);
					fin = formatter.parse(ClassList.get(i).fin);
					dure = (float) ((fin.getTime() - debut.getTime()) / 1000 / 60 / 60.0);
					totalMin[indiWeek] += dure;
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (classTypeMap.containsKey(type)) {
				Float[] tf = classTypeMap.get(type);
				tf[indiWeek] = dure + tf[indiWeek];
				classTypeMap.put(type, tf);
			} else {
				Float[] tf = new Float[] { (float) 0, (float) 0 };
				tf[indiWeek] = dure;
				classTypeMap.put(type, tf);
			}
		}
		classTypeMap.put("total", totalMin);
	}

	private String getClassType(String type) {
		if (type.equals("Examen CF2")) {
			return "CF2";
		} else if (type.equals("Examen CF1")) {
			return "CF1";
		} else if (type.contains("Point de Rencontre")) {
			return "Pt de Rencontre";
		} else {
			return type;
		}
	}

	private void iniPopMenu() {
		if (popMenu == null) {
			System.out.println("got it 1");
			View popupWindow_view = getLayoutInflater().inflate(R.layout.pop,null, false);
			TableLayout tableLayout = (TableLayout) popupWindow_view.findViewById(R.id.poptl);
			TextView pop_title = (TextView) popupWindow_view.findViewById(R.id.pop_title);
			pop_title.setText("Course Info");
			Iterator<String> itor = classTypeMap.keySet().iterator();

			tableLayout.addView(getRow("", true));
			while (itor.hasNext()) {
				String type = itor.next();
				if (type.equals("total")) {
					continue;
				}
				tableLayout.addView(getRow(type, false));
			}
			// TODO: add a line horisontal
			tableLayout.addView(getRow("total", false));

			popMenu = new PopupWindow(popupWindow_view,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
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
			popMenu.showAtLocation(this.findViewById(R.id.top_title),
					Gravity.CENTER, 0, 0);
		} else {
			System.out.println("got it 2");
			popMenu.dismiss();
			popMenu = null;
		}
	}

	private TableRow getRow(String type, boolean flag) {
		TableRow row = new TableRow(this);
		row.setGravity(Gravity.CENTER);
		TextView tx = new TextView(this);
		if (flag) {
			row.addView(new TextView(this));
			tx.setPadding(8, 0, 0, 0);
			tx.setTextSize(20);
			tx.setTextColor(Color.rgb(216, 18, 241));
			tx.setText(getWeekName(numweek));
			row.addView(tx);
			tx = new TextView(this);
			tx.setPadding(8, 0, 0, 0);
			tx.setTextSize(20);
			tx.setTextColor(Color.rgb(216, 18, 241));
			tx.setText(getWeekName(numweek + 1));
			row.addView(tx);
			return row;
		}
		// row.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_select));
		tx.setPadding(0, 0, 8, 0);
		tx.setTextColor(Color.rgb(1, 0, 0));
		tx.setText(type);
		tx.setTextSize(20);
		if (type.equals("total"))
			tx.getPaint().setFakeBoldText(true);
		row.addView(tx);

		tx = new TextView(this);
		tx.setPadding(8, 0, 0, 0);
		tx.setText(classTypeMap.get(type)[0] + "h");
		tx.setTextSize(20);
		tx.setTextColor(Color.rgb(69, 18, 241));
		if (type.equals("total")) {
			tx.setTextColor(Color.rgb(216, 18, 241));
			tx.getPaint().setFakeBoldText(true);
		}
		row.addView(tx);

		tx = new TextView(this);
		tx.setPadding(8, 0, 0, 0);
		tx.setText(classTypeMap.get(type)[1] + "h");
		tx.setTextSize(20);
		tx.setTextColor(Color.rgb(69, 18, 241));
		if (type.equals("total")) {
			tx.setTextColor(Color.rgb(216, 18, 241));
			tx.getPaint().setFakeBoldText(true);
			row.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.tab_select));
		}
		row.addView(tx);
		return row;
	}

	private String getWeekName(int num) {
		if (num == 1) {
			return "1st";
		} else if (num == 2) {
			return "2nd";
		} else {
			return num + "th";
		}
	}

	// async task to show and hide menu
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
				}
				if (leftMargin < leftEdge) {
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

	class SyncCalendarTask extends AsyncTask<Integer, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			return worker.syncCalendar(ClassTableActivity.this, login);
		}

		@Override
		protected void onPostExecute(Boolean synced) {
			String info;
			if (synced) info = "Calender Synchronized !";
			else info = "can't sync google calender !";
			Toast.makeText(getApplicationContext(),info, Toast.LENGTH_SHORT).show();
		}
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
		map.put("menu_icon", R.drawable.icon_account);
		map.put("menu_title", "account");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("menu_icon", R.drawable.icon_exit);
		map.put("menu_title", "exit");
		list.add(map);

		return list;
	}

	// ////////////////////////////////////////other
	// function//////////////////////////////////////
	private String getTopNumWeek(int numweek) {
		String top_numweek = "";
		if (numweek == 1) {
			top_numweek = "1st";
		} else if (numweek == 2) {
			top_numweek = "2nd";
		} else {
			top_numweek = numweek + "th";
		}
		return "course of the " + top_numweek + " week";
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// catch the back event
	long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				if ((System.currentTimeMillis() - exitTime) > 2000) {
					Toast.makeText(getApplicationContext(),
							"one more time to exit", Toast.LENGTH_SHORT).show();
					exitTime = System.currentTimeMillis();
				} else {
					setResult(1);
					finish();
				}
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (isMenuVisible) {
					new ScrollTask().execute(-30);
				} else {
					new ScrollTask().execute(30);
				}
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
