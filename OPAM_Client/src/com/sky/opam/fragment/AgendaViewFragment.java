package com.sky.opam.fragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
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
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.loic.common.LibApplication;
import com.loic.common.graphic.AgendaView;
import com.loic.common.graphic.AgendaView.AgendaEvent;
import com.loic.common.graphic.AgendaView.AgendaViewEventTouchListener;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.model.ClassUpdateInfo;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;
import com.sky.opam.tool.DBworker;

public class AgendaViewFragment extends OpamFragment implements OnClickListener, IntHttpService.asyncGetClassInfoReponse, AgendaViewEventTouchListener
{
	private static final String TAG = AgendaViewFragment.class.getSimpleName();
	
	private String login;
	private AgendaViewPageAdapter adapter;
	
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
			DateFormat df = new SimpleDateFormat("yyyyMMdd");	
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
	
	private void refreshAgenViewForDate(Date date)
	{
		//check whether the classes are loaded
		ClassUpdateInfo updateInfo = DBworker.getInstance().getUpdateInfo(login, date);
		if(updateInfo != null && updateInfo.isSuccess)
		{
			
		}
		else 
		{
			//show download class dialog
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
			Log.i(TAG, "instantiateItem for position : "+position);
			View pageView = null;
			switch (position) 
			{
			case 0:
				if(preMonthView == null)
				{
					preMonthView = LayoutInflater.from(container.getContext()).inflate(R.layout.agenda_view_fragment_week_class_info, null);
					Button chargeLoadBtn = (Button) preMonthView.findViewById(R.id.charge_load_btn);
					chargeLoadBtn.setText("PreBtn");
					//chargeLoadBtn.setBackgroundDrawable(new RoundRectDrawable());
					chargeLoadBtn.setOnClickListener(AgendaViewFragment.this);
				}
				pageView = preMonthView;
				break;
			case 1:
				agendaView = new AgendaView(container.getContext());
				agendaView.setEventTouchListener(AgendaViewFragment.this);
				initAgendaEvents();
				pageView = agendaView;
				break;
			case 2:
				if(nextMonthView == null)
				{
					nextMonthView = LayoutInflater.from(container.getContext()).inflate(R.layout.agenda_view_fragment_week_class_info, null);
					Button chargeLoadBtn = (Button) nextMonthView.findViewById(R.id.charge_load_btn);
					chargeLoadBtn.setText("NextBtn");
					//chargeLoadBtn.setBackgroundDrawable(new RoundRectDrawable());
					chargeLoadBtn.setOnClickListener(AgendaViewFragment.this);
				}
				pageView = nextMonthView;
				break;
			default:
				break;
			}
			
			if(pageView != null)
			{
				container.addView(pageView);
			}
			return pageView;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) 
		{
			Log.i(TAG, "destroyItem for position : "+position);
			container.removeView((View) object);
		}
		
		private void initAgendaEvents()
		{
			DBworker worker = DBworker.getInstance();
			List<Object> list = worker.retrieveDatas(ClassEvent.class, null);
			
			final List<AgendaEvent> events = new ArrayList<AgendaView.AgendaEvent>();
			for(Object object : list)
			{
				ClassEvent classEvent = (ClassEvent) object;
				System.out.println(classEvent.NumEve);
				
				AgendaEvent event = new AgendaEvent();
				//event.mId = Long.parseLong(classEvent.NumEve);
				event.mColor = classEvent.bgColor;
				event.mStartTime = classEvent.startTime;
				event.mEndTime = classEvent.endTime;
				event.mName = classEvent.name;

				events.add(event);
			}
			
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					getActivity().setTitle(agendaView.setYearMonth(2012, Calendar.FEBRUARY));
					agendaView.setEventList(events);
				}
			});
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		menu.add("search").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add("today").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	/******************************************************
	 *************** INT Http Service callback ************
	 ******************************************************/
	@Override
	public void onAsyncGetClassInfoReponse(HttpServiceErrorEnum errorEnum, Date searchDate, final List<ClassEvent> results) 
	{
		Log.i(TAG, "onAsyncGetClassInfoReponse with result size : "+(results == null ? 0 : results.size()));
		if(results != null)
		{
			DBworker worker = DBworker.getInstance();
			for(ClassEvent classEvent : results)
			{
				worker.insertData(classEvent);
			}
			
			adapter.initAgendaEvents();
		}
	}
	
	/******************************************************
	 ***************** Event Click callback ***************
	 ******************************************************/
	@Override
	public void onEventClicked(AgendaEvent event, RectF rect) 
	{
		final Dialog dlg = new Dialog(getActivity(), R.style.MyDialog);
		dlg.show();
		Window win = dlg.getWindow();
		win.setContentView(R.layout.cours_detail_dialog);

		((TextView) win.findViewById(R.id.className)).setText(event.mName);
//		((TextView) win.findViewById(R.id.classType)).setText(c.classType.name);
//		((TextView) win.findViewById(R.id.classTime)).setText(c.startTime + "--" + c.endTime);
//		((TextView) win.findViewById(R.id.classGroup)).setText(c.groupe.replace("__", "\n"));
//		if(c.room.name!=null || !c.room.name.equals("")) ((TextView) win.findViewById(R.id.classRoom)).setText(c.room.name.replace("__", "\n"));
//		if(c.teacher!=null || !c.teacher.equals("")) ((TextView) win.findViewById(R.id.classTeacher)).setText(c.teacher.replace("__", "\n"));

		Button button = (Button) win.findViewById(R.id.dialog_button_cancel);
		button.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				dlg.cancel();
			}
		});
	}

	@Override
	public void onEventLongPressed(AgendaEvent event, RectF rect) 
	{
		// TODO Auto-generated method stub
		
	}

}
