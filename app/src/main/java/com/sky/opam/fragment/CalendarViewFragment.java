package com.sky.opam.fragment;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loic.common.graphic.AgendaView;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.tool.Tool;
import com.sky.opam.view.AgendaViewPage;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class CalendarViewFragment extends OpamFragment
{
    private AgendaViewPage mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView =  inflater.inflate(R.layout.agenda_view_fragment, container, false);
        mViewPager = (AgendaViewPage) rootView.findViewById(R.id.agenda_view_pager);
        mViewPager.setAdapter(new AgendaViewPageAdapter(2012, 10));
        mViewPager.setCurrentItem(AgendaViewPageAdapter.CENTER_POSITION, false);
        return rootView;
    }

    /**
     *
     */
    private class AgendaViewPageAdapter extends PagerAdapter
    {
        public static final int CENTER_POSITION = 50;
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
            AgendaView agendaView = new AgendaView(container.getContext());
            agendaView.setStartHour(7);
            agendaView.setEndHour(19);
            agendaView.initCalendar(year, month, false);
            agendaView.askForEvents();

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
                return new AgendaView(getContext());
            }
            else
            {
                return mAgendaViewCachs.poll();
            }
        }

        private Pair<Integer, Integer> getYearMonthFor(int position)
        {
            int newYear, newMonth;
            int deltaMonth = Math.abs(CENTER_POSITION - position);
            int deltaYear = deltaMonth / 12;
            deltaMonth %= 12;
            newYear = position < CENTER_POSITION ? centerYear - deltaYear : centerYear + deltaYear;
            if(centerMonth < deltaMonth)

        }
    }
}
