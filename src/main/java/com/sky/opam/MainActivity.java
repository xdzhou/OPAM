package com.sky.opam;

import com.loic.common.fragManage.GcActivity;
import com.loic.common.fragManage.GcFragment;
import com.loic.common.fragManage.MenuCreater;
import com.sky.opam.fragment.AgendaViewFragment;
import com.sky.opam.fragment.SettingFragment;
import com.sky.opam.fragment.TrombiFragment;

public class MainActivity extends GcActivity 
{
	@Override
	protected Class<? extends GcFragment> getInitCenterFragment() 
	{
		return OpamMFM.class;
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
}
