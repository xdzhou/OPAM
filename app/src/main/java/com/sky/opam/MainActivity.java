package com.sky.opam;

import android.os.Bundle;
import android.os.Environment;

import com.loic.common.LibApplication;
import com.loic.common.dynamicLoad.PluginApk;
import com.loic.common.dynamicLoad.PluginManager;
import com.loic.common.fragManage.GcActivity;
import com.loic.common.fragManage.GcFragment;
import com.loic.common.fragManage.MenuCreater;
import com.sky.opam.fragment.AgendaViewFragment;
import com.sky.opam.fragment.SettingFragment;
import com.sky.opam.fragment.TrombiFragment;

import java.io.File;

public class MainActivity extends GcActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		PluginManager pluginManager = PluginManager.getInstance();
		File pluginFile = new File(Environment.getExternalStorageDirectory(), "plugin");
		PluginApk pluginApk = pluginManager.getPluginApk(pluginFile.listFiles()[0].getAbsolutePath());

		setCenterFragment(pluginApk.getPluginFragment());
	}

	@Override
	protected Class<? extends GcFragment> getInitCenterFragment() 
	{
		return null;
	}

	@Override
	protected void initMenuItem(MenuCreater menuCreater) 
	{
		menuCreater.appendAutoAppMenuSection()
		.appendMenuElement(getString(R.string.OA0000), AgendaViewFragment.class, android.R.drawable.ic_menu_agenda)
		.appendMenuElement(getString(R.string.OA0001), TrombiFragment.class, android.R.drawable.ic_menu_search)
		.appendMenuElement(getString(R.string.OA0002), SettingFragment.class, android.R.drawable.ic_menu_manage)
		.appendMenuElement(getString(R.string.OA0003), null, android.R.drawable.ic_menu_close_clear_cancel);
	}

	private Class<? extends GcFragment> getPluginFragClass()
	{
		PluginManager pluginManager = PluginManager.getInstance();
		File pluginFile = new File(Environment.getExternalStorageDirectory(), "plugin");
		PluginApk pluginApk = pluginManager.getPluginApk(pluginFile.listFiles()[0].getAbsolutePath());
		return  pluginApk.getPluginFragmentClass();
	}
}
