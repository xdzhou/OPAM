package com.sky.opam;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import com.loic.common.LibApplication;
import com.loic.common.fragManage.GcFragment;
import com.loic.common.fragManage.MultiFragmentManager;
import com.sky.opam.service.IntHttpService;

public class OpamFragment extends GcFragment 
{
	private static final String TAG = OpamFragment.class.getSimpleName();
	private IntHttpService intHttpService;
	
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
	
	@Override
	public void onStop() 
	{
		super.onStop();
		if(serviceConnection != null)
		{
			LibApplication.getAppContext().unbindService(serviceConnection);
			Log.d(TAG, "unbind INT http service ...");
		}
	}

	protected void onHttpServiceReady()
	{
		
	}
}
