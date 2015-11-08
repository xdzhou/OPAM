package com.sky.opam;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.loic.common.LibApplication;
import com.loic.common.fragManage.GcActivity;
import com.loic.common.fragManage.GcFragment;
import com.sky.opam.fragment.SettingFragment;
import com.sky.opam.fragment.TrombiFragment;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.SharePreferenceUtils;
import com.sky.opam.tool.Tool;
import com.squareup.picasso.Picasso;

public class MainActivity extends GcActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private  View menuHeaderView;

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
    protected void onInitMainMenu(NavigationView navigationMenu)
    {
        //set header view
        menuHeaderView = navigationMenu.inflateHeaderView(R.layout.menu_header);

        //set main menu
        getMenuInflater().inflate(R.menu.main_menu, navigationMenu.getMenu());
        
        onSharedPreferenceChanged(null, SharePreferenceUtils.Login_State);
    }

    @Override
    public void onMainMenuSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.menu_agenda_logout:
                SharePreferenceUtils.setLoginState(false);
            case R.id.menu_agenda_login:
            case R.id.menu_agenda_calendar:
                setCenterFragment(new OpamMFM());
                break;
            case R.id.menu_trombi:
                setCenterFragment(new TrombiFragment());
                break;
            case R.id.menu_setting:
                setCenterFragment(new SettingFragment());
                break;
            case R.id.menu_Contact:
                setCenterFragment(new GcFragment());
                break;
            case R.id.menu_about:
                setCenterFragment(new GcFragment());
                break;
        }
    }

    @Override
    protected @IdRes int getInitMenuId()
    {
        return SharePreferenceUtils.isUserLogined() ? R.id.menu_agenda_calendar : R.id.menu_agenda_logout;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equals(SharePreferenceUtils.Login_State))
        {
            Menu mainMenu = getNavigationMenu().getMenu();
            ImageView avatarImage = (ImageView) menuHeaderView.findViewById(R.id.photo);
            if(SharePreferenceUtils.isUserLogined())
            {
                mainMenu.findItem(R.id.menu_agenda_login).setVisible(false);
                mainMenu.findItem(R.id.menu_agenda_calendar).setVisible(true);
                mainMenu.findItem(R.id.menu_agenda_logout).setVisible(true);

                String url = Tool.getTrombiPhotoURL(DBworker.getInstance().getDefaultUser().login, 80);
                if(url != null)
                {
                    Picasso.with(LibApplication.getAppContext()).load(url).into(avatarImage);
                }
            }
            else
            {
                mainMenu.findItem(R.id.menu_agenda_login).setVisible(true);
                mainMenu.findItem(R.id.menu_agenda_logout).setVisible(false);
                mainMenu.findItem(R.id.menu_agenda_calendar).setVisible(false);
                avatarImage.setImageResource(-1);
            }
        }
    }
}
