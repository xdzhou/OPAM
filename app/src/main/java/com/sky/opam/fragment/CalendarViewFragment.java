package com.sky.opam.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loic.common.graphic.AgendaView;
import com.loic.common.utils.NetWorkUtils;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.model.ClassUpdateInfo;
import com.sky.opam.model.User;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.Tool;
import com.sky.opam.view.AgendaViewPage;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public class CalendarViewFragment extends OpamFragment implements AgendaView.AgendaViewEventTouchListener
{
    private static final String TAG = CalendarViewFragment.class.getSimpleName();

    private AgendaViewPage mViewPager;
    private DBworker worker = DBworker.getInstance();
    private User currentUser;
    private DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        currentUser = worker.getDefaultUser();
        if(currentUser == null)
        {
            getMultiFragmentManager().showGcFragment(LoginFragment.class, true, null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView =  inflater.inflate(R.layout.agenda_view_fragment, container, false);
        mViewPager = (AgendaViewPage) rootView.findViewById(R.id.agenda_view_pager);
        AgendaViewPageAdapter adapter = new AgendaViewPageAdapter(2012, 10);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(adapter);
        mViewPager.setCurrentItem(AgendaViewPageAdapter.CENTER_POSITION, false);


        return rootView;
    }

    @Override
    public void onEventClicked(AgendaView.AgendaEvent event, RectF rect)
    {

    }

    @Override
    public void onEventLongPressed(AgendaView.AgendaEvent event, RectF rect)
    {

    }

    @Override
    public List<AgendaView.AgendaEvent> onNeedNewEventList(int year, int month)
    {
        List<AgendaView.AgendaEvent> agendaEvents = null;
        ClassUpdateInfo updateInfo = worker.getUpdateInfo(currentUser.login, year, month);
        if(updateInfo != null && updateInfo.lastSuccessUpdateDate != null)
        {
            List<ClassEvent> events = worker.getClassEvents(currentUser.login, year, month);
            if(events != null)
            {
                agendaEvents = new ArrayList<>(events.size());
                for(ClassEvent classEvent : events)
                {
                    AgendaView.AgendaEvent agendaEvent = new AgendaView.AgendaEvent();
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
            //askForLoadCourse(year, month);
        }
        return agendaEvents;
    }

    private void askForLoadCourse(final int year, final int month)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.OA0000)
        .setMessage(getString(R.string.OA2022, getYearMonthText(year, month)))
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                hideDialog();
                prepareLoadCourse(year, month);
            }
         })
        .setNegativeButton(android.R.string.no, cancelDialogListener)
        .setIcon(android.R.drawable.ic_dialog_alert);
        showDialog(builder);
    }

    private boolean prepareLoadCourse(int year, int month)
    {
        boolean success = false;
        if(getHttpService() == null)
        {
            showDialog(R.string.OA2019, "Http Service isn't ready, please try later.", null);
        }
        else if (!NetWorkUtils.isNetworkAvailable())
        {
            showDialog(R.string.OA2019, R.string.OA0004, null);
        }
        else
        {
            ToastUtils.show("Loading ...");
            //getHttpService().asyncLoadClassInfo(year, month, searchCourseCallback);
            success = true;
        }
        return success;
    }

    private String getYearMonthText(int year, int month)
    {
        return year + " " + dfs.getMonths()[month];
    }

    /**
     * adapter
     */
    private class AgendaViewPageAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener
    {
        public static final int CENTER_POSITION = 10;
        private int centerYear, centerMonth;
        private Queue<AgendaView> mAgendaViewCachs;

        public AgendaViewPageAdapter(int year, int month)
        {
            super();
            this.centerYear = year;
            this.centerMonth = month;
            mAgendaViewCachs = new LinkedList<>();
        }

        @Override
        public int getCount()
        {
            return CENTER_POSITION * 2 + 1;
        }

        @Override
        public boolean isViewFromObject(View view, Object object)
        {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            AgendaView agendaView = obtien();
            Pair<Integer, Integer> ymPair = getYearMonthFor(position);
            agendaView.setEventTouchListener(CalendarViewFragment.this);
            if(position < mViewPager.getCurrentItem())
            {
                agendaView.refreshAgendaWithNewDate(ymPair.first, ymPair.second, AgendaView.LAST_DAY_OF_MONTH , false);
            }
            else
            {
                agendaView.refreshAgendaWithNewDate(ymPair.first, ymPair.second, false);
            }

            container.addView(agendaView);
            return agendaView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            AgendaView view = (AgendaView) object;
            container.removeView(view);
            mAgendaViewCachs.add(view);
        }

        private AgendaView obtien()
        {
            if(mAgendaViewCachs.isEmpty())
            {
                AgendaView agendaView = new AgendaView(getContext());
                agendaView.setStartHour(7);
                agendaView.setEndHour(19);
                return agendaView;
            }
            else
            {
                return mAgendaViewCachs.poll();
            }
        }

        private Pair<Integer, Integer> getYearMonthFor(int position)
        {
            int newYear = centerYear, newMonth = centerMonth;
            newMonth += (position - CENTER_POSITION);
            if(newMonth < Calendar.JANUARY || newMonth > Calendar.DECEMBER)
            {
                newYear += (newMonth / 12);
                newMonth = newMonth % 12;
                if(newMonth < 0)
                {
                    newYear --;
                    newMonth += 12;
                }
            }
            return Pair.create(newYear, newMonth);
        }

        @Override
        public void onPageSelected(int position)
        {
            Activity activity = getActivity();
            if(activity != null)
            {
                Pair<Integer, Integer> ymPair = getYearMonthFor(position);
                activity.setTitle(getYearMonthText(ymPair.first, ymPair.second));
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
            int position = mViewPager.getCurrentItem();

            if((position == 1 || position == getCount() - 2)  //check whether we are in first / last position
                    && state == ViewPager.SCROLL_STATE_IDLE)  // check current pager is in an idle, no animation in progress
            {
                Pair<Integer, Integer> ymPair = getYearMonthFor(position);
                //reset center year month info
                centerYear = ymPair.first;
                centerMonth = ymPair.second;

                mViewPager.setCurrentItem(CENTER_POSITION, false);
            }
        }
    }
}
