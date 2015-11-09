package com.sky.opam;

import android.os.Bundle;
import android.view.MenuItem;

import com.loic.common.fragManage.MultiFragmentManager;
import com.sky.opam.fragment.AgendaViewFragment;
import com.sky.opam.fragment.LoginFragment;
import com.sky.opam.model.User;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.SharePreferenceUtils;

public class OpamMFM extends MultiFragmentManager 
{
    private static final String TAG = OpamMFM.class.getSimpleName();

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
}
