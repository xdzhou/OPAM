package com.sky.opam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loic.common.LibApplication;
import com.loic.common.fragManage.GcActivity;
import com.loic.common.fragManage.GcFragment;
import com.sky.opam.fragment.CalendarViewFragment;
import com.sky.opam.fragment.SettingFragment;
import com.sky.opam.fragment.TrombiFragment;
import com.sky.opam.model.User;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.SharePreferenceUtils;
import com.sky.opam.tool.Tool;
import com.squareup.picasso.Picasso;

public class MainActivity extends GcActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private ImageView mUserAvatar;
    private TextView mUserName;

    @Override
    protected void onStart()
    {
        super.onStart();
        getSharedPreferences(SharePreferenceUtils.LOGIN_STATE_PREFERENCE, Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        getSharedPreferences(SharePreferenceUtils.LOGIN_STATE_PREFERENCE, Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Intent intent = new Intent(LibApplication.getAppContext(), IntHttpService.class);
        LibApplication.getAppContext().stopService(intent);
    }

    @Override
    protected void onInitMainMenu(NavigationView navigationMenu)
    {
        //set header view
        View menuHeaderView = navigationMenu.inflateHeaderView(R.layout.menu_header);
        mUserAvatar = (ImageView) menuHeaderView.findViewById(R.id.user_avatar);
        mUserName = (TextView) menuHeaderView.findViewById(R.id.user_name);

        //set main menu
        getMenuInflater().inflate(R.menu.main_menu, navigationMenu.getMenu());

        onSharedPreferenceChanged(null, SharePreferenceUtils.Login_State);
    }

    @Override
    public Class<? extends GcFragment> getFragClassForMenuItem(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.menu_agenda_logout:
                SharePreferenceUtils.setLoginState(false);
            case R.id.menu_agenda_login:
            case R.id.menu_agenda_calendar:
                return OpamMFM.class;
            case R.id.menu_trombi:
                return TrombiFragment.class;
            case R.id.menu_setting:
                return SettingFragment.class;
            case R.id.menu_Contact:
                return GcFragment.class;
            case R.id.menu_about:
                return CalendarViewFragment.class;
        }
        return null;
    }

    @Override
    protected @IdRes int getInitMenuId()
    {
        return SharePreferenceUtils.isUserLogined() ? R.id.menu_agenda_calendar : R.id.menu_agenda_login;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equals(SharePreferenceUtils.Login_State))
        {
            Menu mainMenu = getNavigationMenu().getMenu();
            if(SharePreferenceUtils.isUserLogined())
            {
                mainMenu.findItem(R.id.menu_agenda_login).setVisible(false);
                mainMenu.findItem(R.id.menu_agenda_calendar).setVisible(true);
                mainMenu.findItem(R.id.menu_agenda_logout).setVisible(true);

                User loginUser = DBworker.getInstance().getDefaultUser();
                if(loginUser != null)
                {
                    Picasso.with(LibApplication.getAppContext()).load(Tool.getTrombiPhotoURL(loginUser.login, 80)).into(mUserAvatar);
                    mUserName.setText(loginUser.name);
                }
            }
            else
            {
                mainMenu.findItem(R.id.menu_agenda_login).setVisible(true);
                mainMenu.findItem(R.id.menu_agenda_logout).setVisible(false);
                mainMenu.findItem(R.id.menu_agenda_calendar).setVisible(false);
                mUserAvatar.setImageResource(-1);
                mUserName.setText("To Login");
            }
            refreshMainMenu();
        }
    }
}
