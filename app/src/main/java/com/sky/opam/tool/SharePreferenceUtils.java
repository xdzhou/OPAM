package com.sky.opam.tool;

import android.content.Context;

import com.loic.common.LibApplication;

public class SharePreferenceUtils
{
    public static final String LOGIN_STATE_PREFERENCE = "LOGIN_STATE_PREFERENCE";

    public static final String Login_State = "Login_State";
    public static final String Remenber_Me_State = "Remenber_Me_State";

    public static boolean isUserLogined()
    {
        return LibApplication.getAppContext().getSharedPreferences(LOGIN_STATE_PREFERENCE, Context.MODE_PRIVATE).getBoolean(Login_State, false);
    }

    public static void setLoginState(boolean isLogined)
    {
        LibApplication.getAppContext().getSharedPreferences(LOGIN_STATE_PREFERENCE, Context.MODE_PRIVATE).edit().putBoolean(Login_State, isLogined).commit();
    }

    public static boolean isRememberMe()
    {
        return LibApplication.getAppContext().getSharedPreferences(LOGIN_STATE_PREFERENCE, Context.MODE_PRIVATE).getBoolean(Remenber_Me_State, true);
    }

    public static void setRememberMeState(boolean isRemember)
    {
        LibApplication.getAppContext().getSharedPreferences(LOGIN_STATE_PREFERENCE, Context.MODE_PRIVATE).edit().putBoolean(Remenber_Me_State, isRemember).commit();
    }
}
