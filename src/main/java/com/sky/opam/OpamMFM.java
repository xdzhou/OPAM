package com.sky.opam;

import java.io.File;

import android.content.Intent;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.os.Bundle;
import android.util.Log;

import com.loic.common.LibApplication;
import com.loic.common.fragManage.MultiFragmentManager;
import com.loic.common.fragManage.MenuElementItem;
import com.loic.common.manager.LoadImgManager;
import com.sky.opam.fragment.AgendaViewFragment;
import com.sky.opam.fragment.LoginFragment;
import com.sky.opam.model.User;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.tool.DBworker;

public class OpamMFM extends MultiFragmentManager 
{
	private static final String TAG = OpamMFM.class.getSimpleName();
	
	private RoundedBitmapDrawable avatarRoundDrawable;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		DBworker dBworker = DBworker.getInstance();
		User defaultUser = dBworker.getDefaultUser();
		if(defaultUser != null && defaultUser.isAutoConnect)
		{
			//setProfileAvatar(defaultUser.login);
			Bundle data = new Bundle();
			data.putString(AgendaViewFragment.BUNDLE_LOGIN_KEY, defaultUser.login);
			showGcFragment(AgendaViewFragment.class, true, data);
			//showGcFragment(LoginFragment.class, true, null);
		}
		else 
		{
			//showGcFragment(AgendaViewFragment.class, true, null);
			showGcFragment(LoginFragment.class, true, null);
		}
	}
	
	@Override
	public boolean onBackPressed() 
	{
		boolean consumed = super.onBackPressed();
		if(!consumed && fragmentClassInShowing != null && !fragmentClassInShowing.isAssignableFrom(AgendaViewFragment.class))
		{
			this.showGcFragment(AgendaViewFragment.class, true, null);
			consumed = true;
		}
		return consumed;
	}

	@Override
	public boolean onOpenElement(MenuElementItem menuElementItem, int position)
    {
		if(menuElementItem.fragmentClass != null)
		{
			showGcFragment(menuElementItem.fragmentClass, true, null);
		}
		else
		{
			getActivity().finish();
		}
		return true;
    }

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		Intent intent = new Intent(LibApplication.getAppContext(), IntHttpService.class);
		boolean success = LibApplication.getAppContext().stopService(intent);
		Log.d(TAG, "stop INT http service ... "+success);
		LoadImgManager.getInstance().dispose();
	}
	
	public void setProfileAvatar(String login)
	{
		String profilePath = IntHttpService.getUserProfileFilePath(login);
		if(profilePath != null && new File(profilePath).exists())
		{
			avatarRoundDrawable = RoundedBitmapDrawableFactory.create(LibApplication.getAppContext().getResources(), profilePath);
			if(avatarRoundDrawable != null)
			{
				avatarRoundDrawable.setAntiAlias(true);
				avatarRoundDrawable.setCornerRadius(avatarRoundDrawable.getBitmap().getWidth());
				getGcActivity().getSupportActionBar().setIcon(avatarRoundDrawable);
			}
		}
	}

	public RoundedBitmapDrawable getAvatarRoundDrawable() 
	{
		return avatarRoundDrawable;
	}
	
	
}
