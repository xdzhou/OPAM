package com.sky.opam.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.tool.DBworker;

public class CourseEditFragment extends OpamFragment
{
	private static final String TAG = CourseEditFragment.class.getSimpleName();
	public static final String Bundle_Course_NumEve_Key = "Bundle_Course_NumEve_Key";
	
	private DBworker worker;
	private ClassEvent course;
	
	@Override
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		outState.putLong(Bundle_Course_NumEve_Key, course.NumEve);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		worker = DBworker.getInstance();
		long courseId = -1;
		if(getArguments() != null)
			courseId = getArguments().getLong(Bundle_Course_NumEve_Key, -1);
		if(courseId == -1 && savedInstanceState != null)
			courseId = savedInstanceState.getLong(Bundle_Course_NumEve_Key, -1);
		if(courseId != -1)
			course = worker.getClassEvent(worker.getDefaultUser().login, courseId);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView =  inflater.inflate(R.layout.classinfo_edit_activity, container, false);
		return rootView;
	}
}
