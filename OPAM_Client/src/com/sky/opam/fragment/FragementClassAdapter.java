package com.sky.opam.fragment;

import java.util.Calendar;

import android.R.integer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragementClassAdapter extends FragmentPagerAdapter {
	private int numweek;
	private String login;

	public FragementClassAdapter(FragmentManager fm, String login, int numweek) {
		super(fm);
		this.numweek = numweek;
		this.login = login;
	}

	@Override
	public Fragment getItem(int position) {
		if (position < 5)
			return ClassFragment.newInstance(login, numweek + "_"
					+ (position + 1), (position + 1) == getDayWeek(), true);
		else
			return ClassFragment.newInstance(login, (numweek + 1) + "_"
					+ (position + 1 - 5), false, false);

	}

	@Override
	public int getCount() {
		return 10;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		// return getTabDate(position+Calendar.MONDAY);
		String title = null;
		if (position < 5)
			title = getTabDate(numweek, position + Calendar.MONDAY);
		else
			title = getTabDate(numweek + 1, position - 5 + Calendar.MONDAY);

		switch (position) {
		case 0:
		case 5:
			title += "-Mon";
			break;
		case 1:
		case 6:
			title += "-Tue";
			break;
		case 2:
		case 7:
			title += "-Wen";
			break;
		case 3:
		case 8:
			title += "-Thi";
			break;
		default:
			title += "-Fri";
			break;
		}
		return title;
	}

	//////////////////////////////////////////other function///////////////////////////////////////
	private String getTabDate(int numweek, int dayOfWeek) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.WEEK_OF_YEAR, numweek);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.WEEK_OF_YEAR, numweek);
		int num = cal.get(Calendar.MONTH) + 1;

		String date = (num < 10) ? "0" + num : "" + num;
		date += "/";
		num = cal.get(Calendar.DAY_OF_MONTH);
		date += (num < 10) ? "0" + num : "" + num;

		return date;
	}

	private int getDayWeek() {
		Calendar c = Calendar.getInstance();
		int xq = c.get(Calendar.DAY_OF_WEEK);
		if (xq == 1) {
			return 7;
		} else {
			return xq - 1;
		}
	}

	public int getTodayPosition() {
		int xq = getDayWeek();
		if (xq < 6)
			return getDayWeek() - 1;
		else
			return 4;
	}

}
