package com.sky.opam;

import com.loic.common.fragManage.GcActivity;
import com.loic.common.fragManage.GcFragment;
import com.loic.common.fragManage.MenuCreater;
import com.sky.opam.fragment.AgendaViewFragment;
import com.sky.opam.fragment.StudentSearchFragment;

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
		.appendMenuElement(getString(R.string.OA0000), AgendaViewFragment.class, R.drawable.logo)
		.appendMenuElement(getString(R.string.OA0001), StudentSearchFragment.class, R.drawable.logo)
		.appendMenuElement(getString(R.string.OA0002), AgendaViewFragment.class, R.drawable.logo)
		.appendMenuElement(getString(R.string.OA0003), AgendaViewFragment.class, R.drawable.logo);
	}
}
