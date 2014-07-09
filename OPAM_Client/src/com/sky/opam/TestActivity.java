package com.sky.opam;

import com.sky.opam.tool.AndroidUtil;
import com.sky.opam.tool.StringUtils;

import cn.jpush.android.api.InstrumentedActivity;
import cn.jpush.android.api.JPushInterface;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class TestActivity extends Activity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        
        MessageReceiver receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(MESSAGE_RECEIVED_ACTION);
		System.out.println("REGISTER");
        registerReceiver(receiver, filter);
	}
	
	public static final String MESSAGE_RECEIVED_ACTION = "NEW_MSG";
	public class MessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
              //System.out.println(intent.getStringExtra("msg"));
              AndroidUtil.showInfo(context, intent.getStringExtra("msg"));
			}
		}
	}

}
