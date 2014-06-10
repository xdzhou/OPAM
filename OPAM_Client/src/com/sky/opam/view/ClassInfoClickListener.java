package com.sky.opam.view;

import com.sky.opam.model.Cours;

import android.view.MotionEvent;
import android.view.View;

public interface ClassInfoClickListener {
	public void onTouchEvent(View v, MotionEvent e, Cours c);
}
