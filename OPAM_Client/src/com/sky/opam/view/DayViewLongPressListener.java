package com.sky.opam.view;

import com.sky.opam.model.ClassInfo;

import android.view.MotionEvent;
import android.view.View;

public interface DayViewLongPressListener {
	public void onLongPressEvent(DayTabClassView v, ClassInfo c, String vocationStartTime, String vocationEndTime);
}
