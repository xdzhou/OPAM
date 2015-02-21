package com.sky.opam.fragment;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.R.integer;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.loic.common.LibApplication;
import com.loic.common.graphic.AgendaView;
import com.loic.common.graphic.AgendaView.AgendaEvent;
import com.loic.common.graphic.AgendaView.AgendaViewEventTouchListener;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.model.ClassUpdateInfo;
import com.sky.opam.model.User;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;
import com.sky.opam.tool.DBworker;

public class AgendaViewFragment extends OpamFragment implements OnClickListener, IntHttpService.asyncGetClassInfoReponse, AgendaViewEventTouchListener
{
	private static final String TAG = AgendaViewFragment.class.getSimpleName();
	public static final String BUNDLE_LOGIN_KEY = "BUNDLE_LOGIN_KEY";
	
	private User currentUser;
	private AgendaViewPageAdapter adapter;
	private DBworker worker;
	
	private View classDetailInfoView;
	private View monthDetailInfoView;
	
	private DateFormat classDetailTimeFormat;
	private DateFormat localDateFormat;
	private DateFormatSymbols dfs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		worker = DBworker.getInstance();
		currentUser = worker.getUser(getArguments().getString(BUNDLE_LOGIN_KEY));
		
		classDetailTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
		localDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		dfs = new DateFormatSymbols(Locale.getDefault());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		super.onCreateView(inflater, container, savedInstanceState);
		getActivity().setTitle("Test Tile");
		
		ViewPager mViewPager = new AgendaViewPage(LibApplication.getAppContext());
		adapter = new AgendaViewPageAdapter();
		mViewPager.setAdapter(adapter);
		mViewPager.setCurrentItem(1);
		
		setHasOptionsMenu(true);
		return mViewPager;
	}

	@Override
	protected void onHttpServiceReady() 
	{
		
	}

	@Override
	public void onClick(View v) 
	{
		if(v.equals(adapter.preMonthView))
		{
			DateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.US);	
			Date date;
			try 
			{
				date = df.parse("20120202");
				//getHttpService().asyncGetClassInfo(date, this);
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}
		}
		else if (v.equals(adapter.nextMonthView)) 
		{
			
		}
	}
	
	private void refreshAgendaView(Date date)
	{
		if(adapter != null && adapter.agendaView != null && isAdded())
		{
			//refresh agenda view
			final String actionTitle = adapter.agendaView.refreshAgendaWithNewDate(date, true);
			//refresh left month info
			int [] preYearMonth = adapter.agendaView.getPreviousYearMonth();
			fillMonthDetailInfo(adapter.preMonthView, preYearMonth[0], preYearMonth[1]);
			//refresh right month info
			int [] nextYearMonth = adapter.agendaView.getNextYearMonth();
			fillMonthDetailInfo(adapter.nextMonthView, nextYearMonth[0], nextYearMonth[1]);
			
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					getActivity().setTitle(actionTitle);
				}
			});
		}
	}
	
	private void refreshAgendaView(int year, int month)
	{
		if(adapter != null && adapter.agendaView != null && isAdded())
		{
			//refresh agenda view
			final String actionTitle = adapter.agendaView.refreshAgendaWithNewDate(year, month, true);
			//refresh left month info
			int [] preYearMonth = adapter.agendaView.getPreviousYearMonth();
			fillMonthDetailInfo(adapter.preMonthView, preYearMonth[0], preYearMonth[1]);
			//refresh right month info
			int [] nextYearMonth = adapter.agendaView.getNextYearMonth();
			fillMonthDetailInfo(adapter.nextMonthView, nextYearMonth[0], nextYearMonth[1]);
			
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
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
					int year, month;
					Date date = new Date();
					if(date.getMonth() == Calendar.JANUARY)
					{
						year = date.getYear() + 1900 - 1;
						month = Calendar.DECEMBER;
					}
					else 
					{
						year = date.getYear() + 1900;
						month = date.getMonth() - 1;
					}
					
					preMonthView = createMonthDetailPageView(year, month, false);
				}
				pageView = preMonthView;
				break;
			case 1:
				agendaView = new AgendaView(container.getContext());
				agendaView.setEventTouchListener(AgendaViewFragment.this);
				pageView = agendaView;
				break;
			case 2:
				if(nextMonthView == null)
				{
					int year, month;
					Date date = new Date();
					if(date.getMonth() == Calendar.DECEMBER)
					{
						year = date.getYear() + 1900 + 1;
						month = Calendar.JANUARY;
					}
					else 
					{
						year = date.getYear() + 1900;
						month = date.getMonth() + 1;
					}
					
					nextMonthView = createMonthDetailPageView(year, month, true);
				}
				pageView = nextMonthView;
				break;
			default:
				break;
			}
			
			if(pageView != null)
				container.addView(pageView);
			
			return pageView;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) 
		{
			container.removeView((View) object);
		}
	}
	
	private View createMonthDetailPageView(int year, int month, boolean isForNextMonth)
	{
		View monthDetailView = View.inflate(LibApplication.getAppContext(), R.layout.agenda_fragment_month_page_view_layout, null);
		((ImageView) monthDetailView.findViewById(R.id.profile_avatar)).setImageDrawable(getOpenMFM().getAvatarRoundDrawable());
		((TextView) monthDetailView.findViewById(R.id.profile_name)).setText(currentUser.name);
		if(isForNextMonth)
			((TextView) monthDetailView.findViewById(R.id.month_detail_month_type)).setText("Next month");
		
		fillMonthDetailInfo(monthDetailView, year, month);

		return monthDetailView;
	}
	
	private void fillMonthDetailInfo(View monthDetailView,final int year, final int month)
	{
		((TextView)monthDetailView.findViewById(R.id.month_detail_pre_next_month)).setText(year+" "+dfs.getMonths()[month]);
		ClassUpdateInfo updateInfo = worker.getUpdateInfo(currentUser.login, year, month);
		
		String text = "?";
		if(updateInfo != null && updateInfo.lastSuccessUpdateDate != null)
			text = Integer.toString(updateInfo.classNumber);
		((TextView)monthDetailView.findViewById(R.id.month_detail_class_num)).setText(text);
		
		text = "?";
		if(updateInfo != null && updateInfo.lastSuccessUpdateDate != null)
			text = localDateFormat.format(updateInfo.lastSuccessUpdateDate);
		((TextView)monthDetailView.findViewById(R.id.month_detail_successs_update)).setText(text);
		
		text = "?";
		if(updateInfo != null && updateInfo.lastFailUpdateDate != null)
			text = localDateFormat.format(updateInfo.lastFailUpdateDate);
		((TextView)monthDetailView.findViewById(R.id.month_detail_failed_update)).setText(text);
		
		text = "?";
		if(updateInfo != null && updateInfo.errorEnum != null)
			text = updateInfo.errorEnum.toString();
		((TextView)monthDetailView.findViewById(R.id.month_detail_failed_reason)).setText(text);
		
		Button updateBtn = ((Button) monthDetailView.findViewById(R.id.month_detail_update));
		Button chargeBtn = ((Button) monthDetailView.findViewById(R.id.month_detail_charge));
		chargeBtn.setEnabled(true);
		if(updateInfo == null || updateInfo.lastSuccessUpdateDate == null)
			chargeBtn.setEnabled(false);
		else
			chargeBtn.setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					refreshAgendaView(year, month);
				}
			});
	}
	
	/******************************************************
	 ********************** option menu *******************
	 ******************************************************/
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		MenuItem searchMI = menu.add("search").setIcon(android.R.drawable.ic_menu_search).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		searchMI.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				Calendar c = Calendar.getInstance();
	            int year = c.get(Calendar.YEAR);
	            int month = c.get(Calendar.MONTH);
	            int day = c.get(Calendar.DAY_OF_MONTH);
				new DatePickerDialog(AgendaViewFragment.this.getActivity(), new DatePickerDialog.OnDateSetListener() 
				{
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
					{
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.MONTH, monthOfYear);
						cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						refreshAgendaView(cal.getTime());
					}
				}, year, month, day).show();
				return false;
			}
		});
		
		MenuItem todayMI = menu.add("today").setIcon(android.R.drawable.ic_menu_today).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		todayMI.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				refreshAgendaView(new Date());
				return false;
			}
		});
		
		MenuItem monthInfoMI = menu.add("info").setIcon(android.R.drawable.ic_menu_info_details).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		monthInfoMI.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				if(monthDetailInfoView == null)
					monthDetailInfoView = View.inflate(LibApplication.getAppContext(), R.layout.agenda_fragment_month_detail_info, null);
				
				int[] yearMonth = adapter.agendaView.getAgendaYearMonth();
				fillMonthDetailInfo(monthDetailInfoView, yearMonth[0], yearMonth[1]);
				
				createDialogBuilderWithCancel(item.getTitle().toString(), null)
				.setCustomView(monthDetailInfoView)
				.show();
				return false;
			}
		});
	}
	
	/******************************************************
	 *************** INT Http Service callback ************
	 ******************************************************/
	@Override
	public void onAsyncGetClassInfoReponse(HttpServiceErrorEnum errorEnum, Date searchDate, final List<ClassEvent> results) 
	{
		Log.i(TAG, "onAsyncGetClassInfoReponse with result size : "+(results == null ? 0 : results.size()));
		if(errorEnum == HttpServiceErrorEnum.OkError)
		{
			refreshAgendaView(searchDate);
		}
		else 
		{
			showDialog("Error", errorEnum.getDescription());
		}
	}
	
	/******************************************************
	 ***************** Agenda Event callback **************
	 ******************************************************/
	@Override
	public List<AgendaEvent> onNeedNewEventList(int year, int month)
	{
		List<AgendaEvent> agendaEvents = null;
		DBworker worker = DBworker.getInstance();
		ClassUpdateInfo updateInfo = worker.getUpdateInfo(currentUser.login, year, month);
		if(updateInfo != null && updateInfo.lastSuccessUpdateDate != null)
		{
			List<ClassEvent> events = worker.getClassEvents(currentUser.login, year, month);
			if(events != null)
			{
				agendaEvents = new ArrayList<AgendaView.AgendaEvent>();
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
			showDialog("Loading", year+" "+month);
			getHttpService().asyncLoadClassInfo(year, month, this);
		}
		return agendaEvents;
	}
	
	@Override
	public void onEventClicked(AgendaEvent event, RectF rect) 
	{
		if(classDetailInfoView == null)
			classDetailInfoView = View.inflate(LibApplication.getAppContext(), R.layout.agenda_fragment_class_detail_info, null);
		
		DBworker worker = DBworker.getInstance();
		ClassEvent classEvent = worker.getClassEvent(currentUser.login, event.mId);
		if(classEvent != null)
		{
			View.OnClickListener editBtnListener = new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					// TODO Auto-generated method stub
					
				}
			};

			((TextView)classDetailInfoView.findViewById(R.id.classType)).setText(classEvent.type == null ? "" : classEvent.type);
			((TextView)classDetailInfoView.findViewById(R.id.classTime)).setText(classDetailTimeFormat.format(classEvent.startTime).concat(" - ").concat(classDetailTimeFormat.format(classEvent.endTime)));
			((TextView)classDetailInfoView.findViewById(R.id.classGroup)).setText(classEvent.groupe == null ? "" : classEvent.groupe.replace("__", "\n"));
			((TextView)classDetailInfoView.findViewById(R.id.classRoom)).setText(classEvent.room == null ? "" : classEvent.room.replace("__", "\n"));
			((TextView)classDetailInfoView.findViewById(R.id.classTeacher)).setText(classEvent.teacher == null ? "" : classEvent.teacher.replace("__", "\n"));
			
			//hideDialog();
			
			createDialogBuilderWithCancel(event.mName, null)
			.setCustomView(classDetailInfoView)
			.withButton1Text(getString(R.string.edit))
			.setButton1Click(editBtnListener)
			.show();
		}
	}

	@Override
	public void onEventLongPressed(AgendaEvent event, RectF rect) 
	{
		
	}
	
	/******************************************************
	 ******************** private function ****************
	 ******************************************************/
	
}
