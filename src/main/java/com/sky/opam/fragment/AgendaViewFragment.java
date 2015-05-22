package com.sky.opam.fragment;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.loic.common.LibApplication;
import com.loic.common.graphic.AgendaView;
import com.loic.common.graphic.AgendaView.AgendaEvent;
import com.loic.common.graphic.AgendaView.AgendaViewEventTouchListener;
import com.loic.common.utils.NetWorkUtils;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.model.ClassUpdateInfo;
import com.sky.opam.model.User;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;
import com.sky.opam.service.IntHttpService.asyncGetClassInfoReponse;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.Tool;

public class AgendaViewFragment extends OpamFragment implements AgendaViewEventTouchListener
{
	private static final String TAG = AgendaViewFragment.class.getSimpleName();
	public static final String BUNDLE_LOGIN_KEY = "BUNDLE_LOGIN_KEY";
	private static final String Share_Preference_Key = "AgendaViewFragment_Share_Preference_Key";
	private static final String BUNDLE_Agenda_Year_KEY = "BUNDLE_Agenda_Year_KEY";
	private static final String BUNDLE_Agenda_Month_KEY = "BUNDLE_Agenda_Month_KEY";
	
	private User currentUser;
	private AgendaViewPageAdapter adapter;
	private DBworker worker;
	private BroadcastReceiver coursLoadedReceiver;
	
	private ViewPager mViewPager;
	
	//private View classDetailInfoView;
	//private View monthDetailInfoView;
	
	private DateFormat classDetailTimeFormat;
	private DateFormat localDateTimeFormat;
	private DateFormat localDateFormat;
	private DateFormatSymbols dfs;
	
	@Override
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		if(currentUser != null)
		{
			outState.putString(BUNDLE_LOGIN_KEY, currentUser.login);
		}
		if(adapter != null)
		{
			int[] yearMonth = adapter.agendaView.getAgendaYearMonth();
			outState.putInt(BUNDLE_Agenda_Year_KEY, yearMonth[0]);
			outState.putInt(BUNDLE_Agenda_Month_KEY, yearMonth[1]);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		worker = DBworker.getInstance();
		String login = null;
		if(getArguments() != null)
		{
			login = getArguments().getString(BUNDLE_LOGIN_KEY);
		}
		if(login == null && savedInstanceState != null)
		{
			login = savedInstanceState.getString(BUNDLE_LOGIN_KEY);
		}
		
		currentUser = login != null ? worker.getUser(login) : worker.getDefaultUser();
		
		classDetailTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
		localDateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		localDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		dfs = new DateFormatSymbols(Locale.getDefault());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		mViewPager = new AgendaViewPage(LibApplication.getAppContext());
		
		int year = -1, month = -1;
		//get year month info from savedInstanceState
		if(savedInstanceState != null)
		{
			year = savedInstanceState.getInt(BUNDLE_Agenda_Year_KEY, -1);
			month = savedInstanceState.getInt(BUNDLE_Agenda_Month_KEY, -1);
		}
		//get year month info from sharedPreference
		if(year == -1 || month == -1)
		{
			SharedPreferences sp = LibApplication.getAppContext().getSharedPreferences(Share_Preference_Key, Context.MODE_PRIVATE);
			year = sp.getInt(BUNDLE_Agenda_Year_KEY, -1);
			month = sp.getInt(BUNDLE_Agenda_Month_KEY, -1);
		}
		//get current year month info
		if(year == -1 || month == -1)
		{
			int[] yearMonth = Tool.getCurrentYearMonth();
			year = yearMonth[0];
			month = yearMonth[1];
		}
		
		adapter = new AgendaViewPageAdapter(year, month);
		
		mViewPager.setAdapter(adapter);
		mViewPager.setCurrentItem(1);
		
		setHasOptionsMenu(true);

		getActivity().setTitle(year + " " + dfs.getMonths()[month]);

		return mViewPager;
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		saveSharePreference();
	}

	@Override
	protected void onHttpServiceReady() 
	{
		
	}
	
	private void saveSharePreference()
	{
		if(adapter != null)
		{
			SharedPreferences sp = LibApplication.getAppContext().getSharedPreferences(Share_Preference_Key, Context.MODE_PRIVATE);
			int[] yearMonth = adapter.agendaView.getAgendaYearMonth();
			sp.edit().putInt(BUNDLE_Agenda_Year_KEY, yearMonth[0]).putInt(BUNDLE_Agenda_Month_KEY, yearMonth[1]).apply();
		}
	}
	
	private void refreshAgendaView(Date date, boolean forceLoad)
	{
		if(adapter != null && adapter.agendaView != null && isAdded())
		{
			//refresh agenda view
			final String actionTitle = adapter.agendaView.refreshAgendaWithNewDate(date, forceLoad);

			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					mViewPager.setCurrentItem(1);
					//refresh left month info
					int [] preYearMonth = adapter.agendaView.getPreviousYearMonth();
					fillMonthDetailInfo(adapter.preMonthView, preYearMonth[0], preYearMonth[1]);
					//refresh right month info
					int [] nextYearMonth = adapter.agendaView.getNextYearMonth();
					fillMonthDetailInfo(adapter.nextMonthView, nextYearMonth[0], nextYearMonth[1]);
					
					getActivity().setTitle(actionTitle);
				}
			});
		}
	}
	
	private void refreshAgendaView(int year, int month, boolean forceLoad)
	{
		if(adapter != null && adapter.agendaView != null && !isDetached())
		{
			//refresh agenda view
			final String actionTitle = adapter.agendaView.refreshAgendaWithNewDate(year, month, forceLoad);
			
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					mViewPager.setCurrentItem(1);
					//refresh left month info
					int [] preYearMonth = adapter.agendaView.getPreviousYearMonth();
					fillMonthDetailInfo(adapter.preMonthView, preYearMonth[0], preYearMonth[1]);
					//refresh right month info
					int [] nextYearMonth = adapter.agendaView.getNextYearMonth();
					fillMonthDetailInfo(adapter.nextMonthView, nextYearMonth[0], nextYearMonth[1]);
					getActivity().setTitle(actionTitle);
				}
			});
		}
	}
	
	/******************************************************
	 ******************* pageView adapter *****************
	 ******************************************************/
	private class AgendaViewPage extends ViewPager
	{
		public AgendaViewPage(Context context)
		{
			super(context);
		}
		
		@Override
		protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) 
		{
			if(v instanceof AgendaView)
			{
				return ((AgendaView) v).canScrollHorizontal(-dx);
			}
			return super.canScroll(v, checkV, dx, x, y);
		}
	}
	
	private class AgendaViewPageAdapter extends PagerAdapter
	{
		private View preMonthView;
		private View nextMonthView;
		private AgendaView agendaView;
		
		private int year, month;
		
		public AgendaViewPageAdapter(int year, int month) 
		{
			super();
			this.year = year;
			this.month = month;
		}

		@Override
		public int getCount() 
		{
			return 3;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) 
		{
			return view.equals(object);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) 
		{
			View pageView = null;
			switch (position) 
			{
			case 0:
				if(preMonthView == null)
				{
					int[] preMonth = Tool.getPreviousMonth(year, month);
					preMonthView = createMonthDetailPageView(preMonth[0], preMonth[1], false);
				}
				pageView = preMonthView;
				break;
			case 1:
				agendaView = new AgendaView(container.getContext());
				agendaView.setStartHour(currentUser.agendaStartHour);
				agendaView.setEndHour(currentUser.agendaEndHour);
				agendaView.initCalendar(year, month, false);
				agendaView.setEventTouchListener(AgendaViewFragment.this);
				agendaView.askForEvents();
				pageView = agendaView;
				break;
			case 2:
				if(nextMonthView == null)
				{
					int[] nextMonth = Tool.getNextMonth(year, month);
					nextMonthView = createMonthDetailPageView(nextMonth[0], nextMonth[1], true);
				}
				pageView = nextMonthView;
				break;
			default:
				Log.e(TAG, "ERROR, get view for position : "+position);
				pageView = new View(container.getContext());
				break;
			}
			
			container.addView(pageView);
			return pageView;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) 
		{
			container.removeView((View) object);
		}
		
		public void tryUpdatePreNextMonthClassInfo(Date date)
		{
			if(date != null)
			{
				int[] ym = Tool.getYearMonthForDate(date);
				int[] preYM = agendaView.getPreviousYearMonth();
				int[] nextYM = agendaView.getNextYearMonth();
				if(ym[0] == preYM[0] && ym[1] == preYM[1])
				{
					fillMonthDetailInfo(preMonthView, preYM[0], preYM[1]);
				}
				else if (ym[0] == nextYM[0] && ym[1] == nextYM[1]) 
				{
					fillMonthDetailInfo(nextMonthView, nextYM[0], nextYM[1]);
				}
			}
		}
	}
	
	private View createMonthDetailPageView(int year, int month, boolean isForNextMonth)
	{
		View monthDetailView = View.inflate(LibApplication.getAppContext(), R.layout.agenda_fragment_month_page_view_layout, null);
		((ImageView) monthDetailView.findViewById(R.id.profile_avatar)).setImageDrawable(getOpenMFM().getAvatarRoundDrawable());
		((TextView) monthDetailView.findViewById(R.id.profile_name)).setText(currentUser.name);
		if(isForNextMonth)
		{
			((TextView) monthDetailView.findViewById(R.id.month_detail_month_type)).setText(getString(R.string.OA2018));
		}
		
		fillMonthDetailInfo(monthDetailView, year, month);

		return monthDetailView;
	}
	
	private void fillMonthDetailInfo(View monthDetailView, final int year, final int month)
	{
		((TextView)monthDetailView.findViewById(R.id.month_detail_pre_next_month)).setText(year+" "+dfs.getMonths()[month]);
		ClassUpdateInfo updateInfo = worker.getUpdateInfo(currentUser.login, year, month);
		
		String text = "-";
		if(updateInfo != null && updateInfo.lastSuccessUpdateDate != null)
		{
			text = Integer.toString(updateInfo.classNumber);
		}
		((TextView)monthDetailView.findViewById(R.id.month_detail_class_num)).setText(text);
		
		text = "-";
		if(updateInfo != null && updateInfo.lastSuccessUpdateDate != null)
		{
			text = localDateTimeFormat.format(updateInfo.lastSuccessUpdateDate);
		}
		((TextView)monthDetailView.findViewById(R.id.month_detail_successs_update)).setText(text);
		
		text = "-";
		if(updateInfo != null && updateInfo.lastFailUpdateDate != null)
		{
			text = localDateTimeFormat.format(updateInfo.lastFailUpdateDate);
		}
		((TextView)monthDetailView.findViewById(R.id.month_detail_failed_update)).setText(text);
		
		text = "-";
		if(updateInfo != null && updateInfo.errorEnum != null)
		{
			text = updateInfo.errorEnum.getDescription();
		}
		((TextView)monthDetailView.findViewById(R.id.month_detail_failed_reason)).setText(text);
		
		Button updateBtn = ((Button) monthDetailView.findViewById(R.id.month_detail_update));
		Button chargeBtn = ((Button) monthDetailView.findViewById(R.id.month_detail_charge));
		if(updateBtn != null && chargeBtn != null)
		{
			updateBtn.setEnabled(true);
			updateBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) 
				{
					if(prepareLoadCourse(year, month))
					{
						v.setEnabled(false);
					}
				}
			});
			
			chargeBtn.setEnabled(true);
			if(updateInfo == null || updateInfo.lastSuccessUpdateDate == null)
			{
				chargeBtn.setEnabled(false);
			}
			else
				chargeBtn.setOnClickListener(new View.OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						refreshAgendaView(year, month, false);
					}
				});
		}
	}
	
	private boolean prepareLoadCourse(int year, int month)
	{
		boolean success = false;
		if(getHttpService() == null)
		{
			showErrorDialog(getString(R.string.OA2019), "Http Service isn't ready, please try later.");
		}
		else if (!NetWorkUtils.isNetworkAvailable()) 
		{
			showErrorDialog(getString(R.string.OA2019), getString(R.string.OA0004));
		}
		else 
		{
			ToastUtils.show("Loading ...");
			getHttpService().asyncLoadClassInfo(year, month, searchCourseCallback);
			success = true;
		}
		return success;
	}
	
	private void showErrorDialog(String title, String msg)
	{
		createDialogBuilderWithCancel(title, msg).withDialogColor("#FFE74C3C").withEffect(Effectstype.RotateBottom).show();
	}
	
	@Override
	protected void onCoursLoaded (Intent intent)
	{
		if(intent != null)
		{
			int enumIndex = intent.getIntExtra(IntHttpService.CoursLoaded_Error_Enum_Index_Info, -1);
			long time = intent.getLongExtra(IntHttpService.CoursLoaded_Date_Info, -1);
			int classSize = intent.getIntExtra(IntHttpService.CoursLoaded_Cours_Size_Info, -1);
			
			if(enumIndex != -1 && time != -1 && classSize != -1)
			{
				onCoursLoaded(HttpServiceErrorEnum.values()[enumIndex], new Date(time), classSize);
			}
		}
	}
	
	private void onCoursLoaded(final HttpServiceErrorEnum errorEnum, final Date searchDate, final int classSize)
	{
		if(isAdded() && ! isHidden() && searchDate != null && errorEnum != null && getActivity() != null)
		{
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					adapter.tryUpdatePreNextMonthClassInfo(searchDate);
					if(errorEnum == HttpServiceErrorEnum.OkError)
					{
						createDialogBuilderWithCancel(getString(R.string.OA0000), getString(R.string.OA2020, getYearMonthText(searchDate), classSize))
						.withButton2Text(getString(android.R.string.ok)).setButton2Click(new View.OnClickListener() 
						{
							@Override
							public void onClick(View v) 
							{
								hideDialog();
								refreshAgendaView(searchDate, true);
							}
						}).show();
					}
					else 
					{
						createDialogBuilderWithCancel(getString(R.string.OA0000), getString(R.string.OA2021, getYearMonthText(searchDate), errorEnum.getDescription()))
						.withDialogColor("#FFE74C3C").withEffect(Effectstype.RotateBottom).show();
					}
				}
			});
		}
	}
	
	/******************************************************
	 ********************** option menu *******************
	 ******************************************************/
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		MenuItem searchMI = menu.add(R.string.OA2024).setIcon(android.R.drawable.ic_menu_search).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		searchMI.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				Calendar c = Calendar.getInstance();
	            int year = c.get(Calendar.YEAR);
	            int month = c.get(Calendar.MONTH);
	            int day = c.get(Calendar.DAY_OF_MONTH);
	            DatePickerDialog datePickerDialog = new DatePickerDialog(AgendaViewFragment.this.getActivity(), new DatePickerDialog.OnDateSetListener() 
				{
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
					{
						ClassUpdateInfo updateInfo = worker.getUpdateInfo(currentUser.login, year, monthOfYear);
						if(updateInfo != null && updateInfo.lastSuccessUpdateDate != null)
						{
							refreshAgendaView(year, monthOfYear, false);
						}
						else
						{
							askForLoadCourse(year, monthOfYear);
						}
					}
				}, year, month, day);
	            datePickerDialog.setTitle(getString(R.string.OA2008));
	            datePickerDialog.show();
				return false;
			}
		});
		
		MenuItem todayMI = menu.add(R.string.OA2023).setIcon(android.R.drawable.ic_menu_today).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		todayMI.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				refreshAgendaView(new Date(), false);
				return false;
			}
		});
		
		MenuItem monthInfoMI = menu.add(R.string.OA2007).setIcon(android.R.drawable.ic_menu_info_details).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		monthInfoMI.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				View monthDetailInfoView = View.inflate(LibApplication.getAppContext(), R.layout.agenda_fragment_month_detail_info, null);
				
				int[] yearMonth = adapter.agendaView.getAgendaYearMonth();
				fillMonthDetailInfo(monthDetailInfoView, yearMonth[0], yearMonth[1]);
				((TextView)monthDetailInfoView.findViewById(R.id.month_detail_month_type)).setText(R.string.OA2025);
				
				createDialogBuilderWithCancel(getString(R.string.OA2007), null)
				.setCustomView(monthDetailInfoView).show();
				return false;
			}
		});
	}
	
	/******************************************************
	 *************** INT Http Service callback ************
	 ******************************************************/
	private String getYearMonthText(Date date)
	{
		int[] yearMonth = Tool.getYearMonthForDate(date);
		return getYearMonthText(yearMonth[0], yearMonth[1]);
	}
	
	private String getYearMonthText(int year, int month)
	{
		return year + " " + dfs.getMonths()[month];
	}
	
	private asyncGetClassInfoReponse searchCourseCallback = new asyncGetClassInfoReponse() 
	{
		@Override
		public void onAsyncGetClassInfoReponse(HttpServiceErrorEnum errorEnum, Date searchDate, List<ClassEvent> results) 
		{
			onCoursLoaded(errorEnum, searchDate, results == null ? 0 : results.size());
		}
	};
	
	/******************************************************
	 ***************** Agenda Event callback **************
	 ******************************************************/
	private void askForLoadCourse(final int year, final int month)
	{
		createDialogBuilderWithCancel(getString(R.string.OA0000), getString(R.string.OA2022, getYearMonthText(year, month)))
		.withButton1Text(getString(android.R.string.no)).withButton2Text(getString(android.R.string.yes)).setButton2Click(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				hideDialog();
				prepareLoadCourse(year, month);
			}
		}).show();
	}
	
	@Override
	public List<AgendaEvent> onNeedNewEventList(int year, int month)
	{
		List<AgendaEvent> agendaEvents = null;
		ClassUpdateInfo updateInfo = worker.getUpdateInfo(currentUser.login, year, month);
		if(updateInfo != null && updateInfo.lastSuccessUpdateDate != null)
		{
			List<ClassEvent> events = worker.getClassEvents(currentUser.login, year, month);
			if(events != null)
			{
				agendaEvents = new ArrayList<AgendaView.AgendaEvent>(events.size());
				for(ClassEvent classEvent : events)
				{
					AgendaEvent agendaEvent = new AgendaEvent();
					agendaEvent.mName = classEvent.name;
					agendaEvent.mId = classEvent.NumEve;
					agendaEvent.mStartTime = classEvent.startTime;
					agendaEvent.mEndTime = classEvent.endTime;
					agendaEvent.mColor = classEvent.bgColor;
					agendaEvents.add(agendaEvent);
				}
			}
		}
		else
		{
			//show class info update Dialog
			askForLoadCourse(year, month);
		}
		return agendaEvents;
	}
	
	@Override
	public void onEventClicked(AgendaEvent event, RectF rect) 
	{
		View classDetailInfoView = View.inflate(LibApplication.getAppContext(), R.layout.agenda_fragment_class_detail_info, null);

		ClassEvent classEvent = worker.getClassEvent(currentUser.login, event.mId);
		if(classEvent != null)
		{
			View.OnClickListener editBtnListener = new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					//getOpenMFM().showGcFragment(CourseEditFragment.class, false, null);
					ToastUtils.show("Coming soon ...");
				}
			};

			((TextView)classDetailInfoView.findViewById(R.id.classType)).setText(classEvent.type == null ? "" : classEvent.type.replace(IntHttpService.getSpecialSpace(), ""));
			((TextView)classDetailInfoView.findViewById(R.id.classTime)).setText(classDetailTimeFormat.format(classEvent.startTime).concat(" - ").concat(classDetailTimeFormat.format(classEvent.endTime)).concat(" "+localDateFormat.format(classEvent.endTime)));
			((TextView)classDetailInfoView.findViewById(R.id.classGroup)).setText(classEvent.groupe == null ? "" : classEvent.groupe.replace("__", "\n"));
			((TextView)classDetailInfoView.findViewById(R.id.classRoom)).setText(classEvent.room == null ? "" : classEvent.room.replace("__", "\n"));
			((TextView)classDetailInfoView.findViewById(R.id.classTeacher)).setText(classEvent.teacher == null ? "" : classEvent.teacher.replace("__", "\n"));
			
			createDialogBuilderWithCancel(event.mName, null)
			.setCustomView(classDetailInfoView)
			.withButton1Text(getString(R.string.OA2017))
			.setButton1Click(editBtnListener).show();
		}
	}

	@Override
	public void onEventLongPressed(AgendaEvent event, RectF rect) 
	{
		
	}
	
	@Override
	protected int getInitDialogBackgroundColor()
	{
		return Color.parseColor("#33b5e5");
	}
}
