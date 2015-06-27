package com.sky.opam;

import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.loic.common.LibApplication;
import com.loic.common.fragManage.GcFragment;
import com.loic.common.fragManage.MultiFragmentManager;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;
import com.sky.opam.tool.Tool;

public class OpamFragment extends GcFragment 
{
    private static final String TAG = OpamFragment.class.getSimpleName();
    private IntHttpService intHttpService;
    private BroadcastReceiver coursLoadedReceiver;
    
    private ServiceConnection serviceConnection = new ServiceConnection() 
    {
        @Override
        public void onServiceDisconnected(ComponentName name) 
        {
            intHttpService = null;
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) 
        {
            Log.d(TAG, "onServiceConnected : INT http service ...");
            intHttpService = ((IntHttpService.LocalBinder)service).getService();
            onHttpServiceReady();
        }
    };
    
    @Override
    public void onResume() 
    {
        super.onResume();
        Intent intent = new Intent(LibApplication.getAppContext(), IntHttpService.class);
        LibApplication.getAppContext().startService(intent);
        boolean flag = LibApplication.getAppContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        Log.d(TAG, "bind INT http service ... "+flag);
    }

    public OpamMFM getOpenMFM()
    {
        MultiFragmentManager mfm = getMultiFragmentManager();
        if(mfm != null && mfm instanceof OpamMFM)
            return (OpamMFM) mfm;
        else
            return null;
    }
    
    public IntHttpService getHttpService() 
    {
        IntHttpService service = null;
        synchronized (this) 
        {
            service = intHttpService;
        }
        return service;
    }
    
    private void registerBroadCast() 
    {
        if(coursLoadedReceiver == null)
        {
            coursLoadedReceiver = new BroadcastReceiver() 
            {
                @Override
                public void onReceive(Context context, Intent intent) 
                {
                    onCoursLoaded (intent);
                }
            };
            
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(coursLoadedReceiver, new IntentFilter(IntHttpService.CoursLoadedBroadCaset));
        }
    }
    
    private void unregisterBroadCast()
    {
        if(coursLoadedReceiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(coursLoadedReceiver);
            coursLoadedReceiver = null;
        }
    }
    
    @Override
    public void onStart() 
    {
        super.onStart();
        registerBroadCast();
    }
    
    @Override
    public void onStop() 
    {
        super.onStop();
        if(serviceConnection != null)
        {
            LibApplication.getAppContext().unbindService(serviceConnection);
            Log.d(TAG, "unbind INT http service ...");
        }
        unregisterBroadCast();
    }

    protected void onHttpServiceReady()
    {
        
    }
    
    protected void onCoursLoaded (Intent intent)
    {
        if(intent != null && isAdded() && !isHidden())
        {
            int enumIndex = intent.getIntExtra(IntHttpService.CoursLoaded_Error_Enum_Index_Info, -1);
            long time = intent.getLongExtra(IntHttpService.CoursLoaded_Date_Info, -1);
            int classSize = intent.getIntExtra(IntHttpService.CoursLoaded_Cours_Size_Info, -1);
            
            if(enumIndex != -1 && time != -1 && classSize != -1)
            {
                Date searchDate = new Date(time);
                String toastInfo;
                IntHttpService.HttpServiceErrorEnum errorEnum = IntHttpService.HttpServiceErrorEnum.values()[enumIndex];
                if(errorEnum == IntHttpService.HttpServiceErrorEnum.OkError)
                {
                    toastInfo = getString(R.string.OA2020, getYearMonthText(searchDate), classSize);
                } else
                {
                    toastInfo = getString(R.string.OA2021, getYearMonthText(searchDate), errorEnum.getDescription());
                }
                ToastUtils.show(toastInfo);
            }
        }
    }
    
    private String getYearMonthText(Date date)
    {
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        int[] yearMonth = Tool.getYearMonthForDate(date);
        return yearMonth[0] + " " + dfs.getMonths()[yearMonth[1]];
    }
}
