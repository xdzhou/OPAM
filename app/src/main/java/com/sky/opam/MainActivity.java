package com.sky.opam;

import android.os.Bundle;
import android.os.Environment;

import com.loic.common.dynamicLoad.PluginApk;
import com.loic.common.dynamicLoad.PluginManager;
import com.loic.common.fragManage.GcActivity;
import com.loic.common.fragManage.GcFragment;
import com.loic.common.fragManage.MenuManager;
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
}
