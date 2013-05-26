package com.sky.opam.abstractActivity;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sky.opam.model.TranObject;
import com.sky.opam.model.TranObjectType;

public abstract class NotChatActivity extends TabActivity{
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

	public void OnGetMessage(TranObject msg) {
		if(msg.getType()==TranObjectType.MsgReceived ){
			showInfo(IDgetName(msg.getInterlocutor())+" : "+msg.getContenu());
			increaseNum(msg.getInterlocutor());
		}else if (msg.getType()==TranObjectType.UserOnLine) {
			showInfo("User online : "+msg.getContenu());
		}else if (msg.getType()==TranObjectType.UserOutLine) {
			showInfo("User offline : "+msg.getContenu());
		}else if (msg.getType()==TranObjectType.Notification) {
			showInfo(msg.getContenu());
		}else {
			
		}
	}
	
	public abstract void showInfo(String msg);
	public abstract void increaseNum(String userID);
	
	////////////////////////////////////////////////////
	private String IDgetName(String userID){
		System.out.println(userID);
		String[] names = new String(userID).split("\\*");
		return names[1];
	}

}
