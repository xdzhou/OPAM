package com.sky.opam.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loic.common.fragManage.GcFragment;
import com.sky.opam.R;

public class SettingFragment extends GcFragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView =  inflater.inflate(R.layout.config_activity, container, false);
		return rootView;
	}
}
