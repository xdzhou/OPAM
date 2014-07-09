package com.sky.opam;
 
 import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

import com.sky.opam.R;
 
 public class ChatActivity extends Activity{
 	private String interlocutor;
 	private String fName;

 	private ChatListAdapter chatListAdapter;
 	private boolean FriendIsOnline = true;
 	PopupWindow popupWindow = null;
 	TextView ln=null;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         //setContentView(R.layout.chat);
         
         interlocutor = (String) getIntent().getExtras().get("interlocutor");
         if(interlocutor.equals("opam*Simsimi")){
         	MsgSaved msg = new MsgSaved(interlocutor,application.getUserID() , new Date(), "Hi");
 			application.saveMsg(msg);
 			ln = (TextView) findViewById(R.id.ln);
 			ln.setText("ch");
 	        ln.setOnClickListener(popClick);
         }
         
         
         fName = IDgetName(interlocutor);
         ((TextView)findViewById(R.id.chat_name)).setText(fName);
         final EditText tv = (EditText) findViewById(R.id.chat_editmessage);
         Button sendButton = (Button) findViewById(R.id.chat_send);
         ListView chatlist = (ListView) findViewById(R.id.chatlist);
         chatListAdapter = new ChatListAdapter(this, application.getMsgSaved(interlocutor));
         chatlist.setAdapter(chatListAdapter);
         //send button
         sendButton.setOnClickListener(new View.OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				if(!tv.getText().toString().equals("")){
 					if(FriendIsOnline){
 						if(interlocutor.equals("opam*Simsimi")){
 							SimsimiTask task = new SimsimiTask();
 							task.execute(tv.getText().toString(), ln.getText().toString());
 						}else {
 							Intent broadCast = new Intent();
 							broadCast.setAction("com.sky.clientSide");
 							TranObject tranObject = new TranObject();
 							tranObject.setInterlocutor(interlocutor);
 							tranObject.setType(TranObjectType.MsgToSend);
 							tranObject.setContenu(tv.getText().toString());
 							broadCast.putExtra("msg", tranObject);
 							sendBroadcast(broadCast);
 							System.out.println("require send msg: To:"interlocutor" content:"tranObject.getContenu());
 						}
 						MsgSaved msg = new MsgSaved(application.getUserID(), interlocutor, new Date(), tv.getText().toString());
 						application.saveMsg(msg);
 						chatListAdapter.notifyDataSetChanged();
 						tv.setText("");
 					}else {
 						showInfo("Your friend is offline, she/he can't see your message.");
 					}
 				}else {
 					showInfo("write something...");
 				}
 			}
 		});
         // back button
         ImageView backButton = (ImageView) findViewById(R.id.chat_back);
         backButton.setOnClickListener(new View.OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				if(simsimiAPI!=null){simsimiAPI.stop();}
 				finish();
 			}
 		});
       
         
     }
 
 	@Override
 	public void OnGetMessage(TranObject msg) {
 		if(msg.getType()==TranObjectType.MsgReceived ){		
 			if(msg.getInterlocutor().equals(interlocutor)){
 				chatListAdapter.notifyDataSetChanged();
 			}else {
 				showInfo(IDgetName(msg.getInterlocutor())":"msg.getContenu());
 				application.increaseNumUnread(msg.getInterlocutor());
 			}
 		}else if (msg.getType()==TranObjectType.UserOnLine) {
 			showInfo("User online:"msg.getContenu());
 			if(msg.getContenu().equals(fName)){
 				FriendIsOnline=true;
 			}
 		}else if (msg.getType()==TranObjectType.UserOutLine) {
 			showInfo("User offline:"msg.getContenu());
 			if(msg.getContenu().equals(fName)){
 				FriendIsOnline=false;
 			}
 		}else if (msg.getType()==TranObjectType.Notification) {
 			showInfo(msg.getContenu());
 		}else {
 			
 		}
 	}
 	
 	@Override
 	protected void onRestart() {
 		chatListAdapter.notifyDataSetChanged();
 		super.onRestart();
 	}
 	@Override
 	protected void onResume() {
 		chatListAdapter.notifyDataSetChanged();
 		super.onResume();
 	}
 
 	@Override 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {		
 		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
 	        if(simsimiAPI!=null){simsimiAPI.stop();}
 	    }
 	    return super.onKeyDown(keyCode, event);  	
 	}
 	/////////////////////////////////////////adaptor///////////////////////////////////////////////
 	@SuppressLint("SimpleDateFormat")
 	private class ChatListAdapter extends BaseAdapter{
 		protected List<MsgSaved> msgList;
 		//protected Context context;
 		protected LayoutInflater mInflater;
 		
 		public ChatListAdapter(Context context, List<MsgSaved> msgList){
 			super();
 			this.mInflater = LayoutInflater.from(context);
 			//this.context = context;
 			this.msgList = msgList;
 		}
 		@Override
 		public int getCount() {
 			return msgList.size();
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
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if(msgList.get(position).getFrom().equals(interlocutor)){
 				convertView = mInflater.inflate(R.layout.chat_item_left, null);
 				if(interlocutor.equals("opam*Simsimi")){
 					ImageView tx = (ImageView) convertView.findViewById(R.id.iv_userhead);
 					tx.setImageDrawable(getResources().getDrawable(R.drawable.simsimi_logo));
 				}
 			}else {
 				convertView = mInflater.inflate(R.layout.chat_item_right, null);
 			}
 			TextView show_name = (TextView) convertView.findViewById(R.id.tv_username);
 			TextView show_time = (TextView) convertView.findViewById(R.id.tv_sendtime);
 			TextView show_message = (TextView) convertView.findViewById(R.id.tv_chatcontent);
 			
 			if(msgList.get(position).getFrom().equals(interlocutor)){
 				show_name.setText(fName);
 			}else {
 				show_name.setText(application.getUserName());
 			}
 			
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");			
 			show_time.setText(df.format(msgList.get(position).getDate()));
 			show_message.setText(msgList.get(position).getMsg());
 			
 			return convertView;
 		}
 		
 	}
 	////////////////////////////////////////////Task////////////////////////////////////////
 	class SimsimiTask extends AsyncTask<String, Integer, String>{		
 		@Override
 		protected String doInBackground(String... msg) {
 			return getSimsimiMsg(msg[0], msg[1]);
 		}
 		@Override
 		protected void onPostExecute(String result) {
 			MsgSaved msg = new MsgSaved(interlocutor, application.getUserID() , new Date(), result);
 			application.saveMsg(msg);
 			chatListAdapter.notifyDataSetChanged();
 			super.onPostExecute(result);
 		}			
 	}
 	//////////////////////////////////////////////lanugage pop win/////////////////////////////////////
 	OnClickListener popClick = new OnClickListener() {   	    
 		@Override   
 		public void onClick(View v) {
 			if(popupWindow==null){
 				getPopupWindow(v); 
 			}
 			if(popupWindow.isShowing()){
 				popupWindow.dismiss();
 			}else{
 				popupWindow.showAsDropDown(v); 
 			}
 			 
 		}   
 	};
 	
 	private void getPopupWindow(View v){
 		View ln_list_view = getLayoutInflater().inflate(R.layout.chat_language_pop, null,false);
 		ListView ln_list = (ListView) ln_list_view.findViewById(R.id.ln_list);
 		SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.chat_language_item, new String[]{"ln_name"}, new int[]{R.id.ln_item});
 		//ln_list.setAdapter(new ArrayAdapter<String>(this, R.layout.chat_language_item,getData()));
 		ln_list.setAdapter(adapter);
 		final String[] ln_name_list = {"ch","zh","en","fr","ko","ph","he","th","id","ja","ms","vn","ar","ru","de","nl","pt","it","es","sv","da","nb","tr","kh","ml","pl","fi","hu","hi","ro","el","ta","bg","hr","lt","af","pa","cs","uk","sk","rs","ca","te","eu","cy"};
 		ln_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
 				ln.setText(ln_name_list[arg2]);
 				popupWindow.dismiss();
 			}
 		});
 		popupWindow = new PopupWindow(ln_list_view,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
 		ln_list_view.setOnTouchListener(new View.OnTouchListener() {
 			@Override   
 			public boolean onTouch(View v, MotionEvent event) {    
 			if (popupWindow != null && popupWindow.isShowing()) {   
 				popupWindow.dismiss();   
 			}   
 			return false;   
 			} 
 		});		 
 		popupWindow.setOutsideTouchable(false);
 		popupWindow.update();
 		
 		//popupWindow.showAtLocation(this.findViewById(R.id.ln), Gravity.RIGHT, 0, 0);
 	}
 	private List<Map<String, Object>> getData(){ 
 		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();		 
         Map<String, Object> map;
         
         map = new HashMap<String, Object>(); map.put("ln_name","中文简体");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","中文繁體");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","English");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Français");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","한국어");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Filipino");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Hebrew");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Thai");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Bahasa Indonesia");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","日本語");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Bahasa Melayu");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","tieng viet");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","العربية");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Русский");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Deutsch");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Nederlands");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Português");  list.add(map);       
         map = new HashMap<String, Object>(); map.put("ln_name","Italiano");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Español");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Svenska");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Dansk");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Norsk (Bokmål)");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Türkçe");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Khmer");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Malayalam");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Polski");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Suomi");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","magyar");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Hindi");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Română");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Ελληνικά");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Tamil");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Български");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Hrvatski");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Lietuvių");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Afrikaans");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Punjabi");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","čeština");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","українець");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Slovenčina");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Српски");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","catala");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Telugu");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Euskara");  list.add(map);
         map = new HashMap<String, Object>(); map.put("ln_name","Cymraeg");  list.add(map);   
         return list;
     }
 	////////////////////////////////////////other function////////////////////////////////////////////
 	private void showInfo(String msg){
 		Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();        
     }
 	
 	private String IDgetName(String userID){
 		System.out.println(userID);
 		String[] names = new String(userID).split("\\*");
 		return names[1];
 	}
 	
 	private String getSimsimiMsg(String question, String ln){
 		if(simsimiAPI==null){
 			simsimiAPI = new SimsimiAPI();
 			simsimiAPI.Start();
 		}
 		return simsimiAPI.getSimsimiRsp(question, ln);
 	}
 
 }