package com.sky.opam;

import java.io.File;

import android.content.Intent;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.loic.common.LibApplication;
import com.loic.common.fragManage.GcActivity;
import com.loic.common.fragManage.MultiFragmentManager;
import com.sky.opam.fragment.AgendaViewFragment;
import com.sky.opam.fragment.LoginFragment;
import com.sky.opam.model.User;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.SharePreferenceUtils;

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
        if(defaultUser != null && SharePreferenceUtils.isUserLogined())
        {
            Bundle data = new Bundle();
            data.putString(AgendaViewFragment.BUNDLE_LOGIN_KEY, defaultUser.login);
            showGcFragment(AgendaViewFragment.class, true, data);
        }
        else
        {
            showGcFragment(LoginFragment.class, true, null);
        }
    }

    public boolean onMainMenuSelected(MenuItem menuItem)
    {
        Class<? extends OpamFragment> fragToSHowClass = null;
        switch (menuItem.getItemId())
        {
            case R.id.menu_agenda_login:
                fragToSHowClass = LoginFragment.class;
                break;
            case R.id.menu_agenda_logout:
                fragToSHowClass = LoginFragment.class;
                SharePreferenceUtils.setLoginState(false);
                break;
            case R.id.menu_agenda_calendar:
                fragToSHowClass = AgendaViewFragment.class;
                break;
        }
        if(fragToSHowClass != null)
        {
            showGcFragment(fragToSHowClass, true, null);
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Intent intent = new Intent(LibApplication.getAppContext(), IntHttpService.class);
        boolean success = LibApplication.getAppContext().stopService(intent);
        Log.d(TAG, "stop INT http service ... " + success);
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
