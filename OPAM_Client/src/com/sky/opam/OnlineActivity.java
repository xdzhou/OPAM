package com.sky.opam;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sky.opam.abstractActivity.BaseChatActivity;
import com.sky.opam.model.MsgSaved;
import com.sky.opam.model.TranObject;
import com.sky.opam.model.TranObjectType;

public class OnlineActivity extends BaseChatActivity {
	ListView friendsListView;
	MyApplication application;
	UserListAdapter adapter;
	boolean NetWorkFlag = true;
	ProgressBar pb;
	ImageView progress_image;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.online); 
		
		if(!isNetworkAvailable()){
			showInfo("Network is unable!");
			finish();
			NetWorkFlag = false;
		}	
		Intent service = new Intent(this, WebViewService.class);
		startService(service);		
		application = (MyApplication) this.getApplication();
		
		((TextView)findViewById(R.id.user_name)).setText(application.getUserName());
		pb = (ProgressBar) findViewById(R.id.progressBar1);			
		progress_image = (ImageView)findViewById(R.id.progress_image);
		
		if(application.isFirstOnLine()){
			pb.setVisibility(View.VISIBLE);	
			progress_image.setVisibility(View.GONE);
			application.setFirstOnLine(false);
		}else {
			progress_image.setVisibility(View.VISIBLE);	
			pb.setVisibility(View.GONE);
		}
		
		
		progress_image.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(application.isOnlineMode()){
					Intent broadCast = new Intent();
					broadCast.setAction("com.sky.clientSide");
					TranObject tranObject = new TranObject();
					tranObject.setType(TranObjectType.UserListRequire);
					broadCast.putExtra("msg", tranObject);
					sendBroadcast(broadCast);
					System.out.println("require the list of users!");
					pb.setVisibility(View.VISIBLE);
					v.setVisibility(View.GONE);
					handler.postDelayed(runnable, 9000);
				}else {
					showInfo("!Offline Mode!");
				}
				
			}
		});
		friendsListView = (ListView) findViewById(R.id.user_list);
		adapter = new UserListAdapter(this, application.getUserList(),application.getMsgUnReadMap());
		friendsListView.setAdapter(adapter);
		

    }	
	
	@Override
	protected void onRestart() {
		if(NetWorkFlag)adapter.notifyDataSetChanged();
		super.onRestart();
	}
	@Override
	protected void onResume() {
		if(NetWorkFlag)adapter.notifyDataSetChanged();
		super.onResume();
	}


	@Override
	public void OnGetMessage(TranObject msg) {
		//showInfo(msg.getContenu());
		if(msg.getType()==TranObjectType.Notification){
			String info = msg.getContenu();
			if(info.equals("Friends List update")){
				adapter.notifyDataSetChanged();
				pb.setVisibility(View.GONE);
				progress_image.setVisibility(View.VISIBLE);
				handler.removeCallbacks(runnable);
				showInfo(info);
				System.out.println("user list:"+application.getUserList());
			}else  {
				showInfo(msg.getContenu());			
			}
		}else if(msg.getType()==TranObjectType.UserOnLine || msg.getType()==TranObjectType.UserOutLine){		
			adapter.notifyDataSetChanged();
		}
		else if(msg.getType()==TranObjectType.MsgReceived){
			showInfo(IDgetName(msg.getInterlocutor())+":"+msg.getContenu());
			application.increaseNumUnread(msg.getInterlocutor());
			adapter.notifyDataSetChanged();
		}
		
	}
	
	@SuppressLint("SimpleDateFormat")
	private class UserListAdapter extends BaseAdapter{
		private List<String> friendsList;
		protected LayoutInflater mInflater;
		
		public UserListAdapter(Context context, List<String> fl, Map<String, Integer> new_num){
			super();
			friendsList=fl;
			this.mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			return friendsList.size();
		}
		@Override
		public Object getItem(int arg0) {
			return arg0;
		}
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = null;
			if(convertView == null){
				view = mInflater.inflate(R.layout.user_item, null);
			}else{
				view = convertView;
			}	
			ImageView tx = (ImageView) view.findViewById(R.id.recent_userhead);
			if(position==0){				
				tx.setImageDrawable(getResources().getDrawable(R.drawable.simsimi_logo));
			}else {
				tx.setImageDrawable(getResources().getDrawable(R.drawable.logo));
			}
			TextView tName = (TextView) view.findViewById(R.id.recent_name);
			TextView tTime = (TextView) view.findViewById(R.id.recent_time);
			TextView tMsg = (TextView) view.findViewById(R.id.recent_msg);
			TextView tNum = (TextView) view.findViewById(R.id.recent_num);
			int num_msg_unread = application.getNumUnread(friendsList.get(position));
			tNum.setText( (num_msg_unread==0)? "": num_msg_unread+"" );
			
			String[] names = new String(friendsList.get(position)).split("\\*");
			tName.setText(names[1]);
			List<MsgSaved> ms = application.getMsgSaved(friendsList.get(position));			
			if(ms.size()!=0){
				int last_num = ms.size()-1;
				SimpleDateFormat df = new SimpleDateFormat("HH:mm");			
				tTime.setText(df.format(ms.get(last_num).getDate()));
				tMsg.setText(ms.get(last_num).getMsg());
				//if(new_num[position]>0) tNum.setText(new_num[position]);
			}
			
			view.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					application.ClearNumUnread(friendsList.get(position));
					Intent intent = new Intent();
			        intent.setClass(OnlineActivity.this, ChatActivity.class);
			        Bundle bundle = new Bundle();  
			        bundle.putString("interlocutor", friendsList.get(position));
			        intent.putExtras(bundle);
			        startActivity(intent); 
				}
			});
			return view;
		}
		
	}
	
	//////////////////////////////////////////other fonction/////////////////////////////////
	
	private void showInfo(String msg){
		Toast.makeText(OnlineActivity.this, msg, Toast.LENGTH_SHORT).show();        
    }
	private boolean isNetworkAvailable() {
		ConnectivityManager mgr = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = mgr.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}
	
	private String IDgetName(String userID){
		System.out.println(userID);
		String[] names = new String(userID).split("\\*");
		return names[1];
	}
	
	Handler handler = new Handler();
	Runnable runnable = new Runnable() {	
		@Override
		public void run() {
			if(pb.isShown()){
				pb.setVisibility(View.GONE);
				progress_image.setVisibility(View.VISIBLE);
				showInfo("Sorry, but we have some problem to get user list!");
				handler.removeCallbacks(runnable);
			}			
		}
	};
	
		
}
