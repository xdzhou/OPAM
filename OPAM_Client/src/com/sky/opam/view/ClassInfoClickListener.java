package com.sky.opam.view;

import com.sky.opam.model.ClassInfo;

import android.view.MotionEvent;
import android.view.View;

public interface ClassInfoClickListener {
	public void onTouchEvent(View v, MotionEvent e, ClassInfo c);
}
