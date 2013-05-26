package com.sky.opam.abstractActivity;

import com.sky.opam.model.TranObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class BaseChatActivity extends Activity {

	private BroadcastReceiver MsgReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			TranObject msg = (TranObject) intent.getSerializableExtra("msg");
			if (msg != null) {				
				OnGetMessage(msg);
			} else {

			}
		}
	};


	public abstract void OnGetMessage(TranObject msg);

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.sky.serverSide");
		registerReceiver(MsgReceiver, intentFilter);

	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(MsgReceiver);
	}
}
