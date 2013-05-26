package com.sky.opam;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sky.opam.model.MsgSaved;
import com.sky.opam.model.TranObject;
import com.sky.opam.model.TranObjectType;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.webkit.CacheManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewService extends Service {
	private MyApplication application;
	WebView webView;
	String userID;
	int errorCount=0;

	private BroadcastReceiver clientReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			TranObject msg = (TranObject) intent.getSerializableExtra("msg");
			if(msg.getType()==TranObjectType.MsgToSend){
				System.out.println("receive sendMsg broadcast");
				webView.loadUrl("javascript:sendMessage('"+msg.getInterlocutor()+"','"+msg.getContenu()+"')");
			}else if(msg.getType()==TranObjectType.UserListRequire){
				System.out.println("receive userList broadcast");
				webView.loadUrl("javascript:GetUserList()");				
			}else if (msg.getType()==TranObjectType.ServiceStop) {
				System.out.println("receive serviceStop broadcast");
				webView.loadUrl("javascript:userOffline()");
				new Timer().schedule(serviceDistroy, 2000);
			}else {
				System.out.println("receive unknown broadcast");
			}
		}
	};

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		System.out.println("service start");
		
		application = (MyApplication) this.getApplicationContext();
		userID = application.getUserID();
		application.addUser("opam*Simsimi");
		
		webViewIni();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.sky.clientSide");
		registerReceiver(clientReceiver, filter);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
	}

	@Override
	public void onDestroy() {				
		if(application.isOnlineMode()){application.clear();}
		webViewClear();	
		unregisterReceiver(clientReceiver);
		System.out.println("cleared!!");
		super.onDestroy();		
	}
	
///////////////////////////////////////webview action///////////////////////////////////
	@SuppressLint("SetJavaScriptEnabled")
	private void webViewIni(){
		webView = new WebView(this);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); 
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebClient());
		//webView.setWebChromeClient(new MyChromeClient());
		webView.addJavascriptInterface(new JSInterface(), "android");	
		webView.loadUrl("http://opamonline.appspot.com/");
	}
	
	class MyWebClient extends WebViewClient{
		@Override
		public void onPageFinished(WebView view, String url) {			
			webView.loadUrl("javascript:requestToken('"+userID+"')");
			super.onPageFinished(view, url);
		}
	}
	
	final class JSInterface{
		JSInterface(){}		
		public void showJSinfo(String info){
			System.out.println(info);
			
			Intent broadCast = new Intent();
			broadCast.setAction("com.sky.serverSide");
			
			TranObject tranObject = new TranObject();
			try {
				JSONObject json = new JSONObject(info);
				String infoType = json.getString("type");
				if(infoType.equals("Notif")){
					tranObject.setType(TranObjectType.Notification);
					tranObject.setContenu(json.getString("Content"));
				}
				else if (infoType.equals("UserList")) {
					application.getUserList().clear();
					application.addUser("opam*Simsimi");
					JSONArray jArray = json.getJSONArray("list");
					for(int i=0;i<jArray.length();i++){
						application.addUser(jArray.getString(i));
					}
					tranObject.setType(TranObjectType.Notification);
					tranObject.setContenu("Friends List update");
				}
				else if (infoType.equals("UserOnline")) {
					application.addUser(json.getString("friendID"));
					tranObject.setType(TranObjectType.UserOnLine);
					tranObject.setContenu(IDgetName(json.getString("friendID")));
				}
				else if (infoType.equals("UserOffline")) {
					application.detUser(json.getString("friendID"));
					tranObject.setType(TranObjectType.UserOutLine);
					tranObject.setContenu(IDgetName(json.getString("friendID")));
				}
				else if (infoType.equals("MSG")) {
					MsgSaved ms = new MsgSaved();
					ms.setFrom(json.getString("from"));
					ms.setTo(userID);
					ms.setDate(new Date());
					ms.setMsg(json.getString("msg"));
					application.saveMsg(ms);					
					tranObject.setType(TranObjectType.MsgReceived);
					tranObject.setInterlocutor(ms.getFrom());
					tranObject.setContenu(ms.getMsg());
				}else if (infoType.equals("Error")) {
					if(!json.getString("Content").contains("deleting the user")){
						errorCount++;
						if(errorCount>=3){
							application.offlineMode();
							tranObject.setType(TranObjectType.Notification);
							tranObject.setContenu("Friends List update");
							webView.loadUrl("javascript:userOffline()");
							new Timer().schedule(serviceDistroy, 2000);
						}else {
							tranObject.setType(TranObjectType.Notification);
							tranObject.setContenu(json.getString("Error Found! Reconnect in 5 seconds."));
							webView.loadUrl("javascript:userOffline()");
							new Timer().schedule(reloadTask, 5000);
						}						
					}
					
				}
				else {
					
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			broadCast.putExtra("msg", tranObject);
			sendBroadcast(broadCast);

		}
		
	}
	///////////////////////////////other function///////////////////////////////////
	TimerTask serviceDistroy = new TimerTask() {	
		@Override
		public void run() {
			stopSelf();
		}
	};
	
	TimerTask reloadTask = new TimerTask() {	
		@Override
		public void run() {
			webView.reload();
		}
	};
	
	private String IDgetName(String userID){
		String[] names = new String(userID).split("\\*");
		return names[1];
	}
	
	private void webViewClear(){
		webView.clearCache(true);
		webView.clearHistory();
		webView.clearFormData();
		webView.destroy();
		File cacheFile = CacheManager.getCacheFileBaseDir().getParentFile();
		for(File f : cacheFile.listFiles()){
			deleteFile(f);
		}		
		this.deleteDatabase("webview.db");
		this.deleteDatabase("webview.db-shm");
		this.deleteDatabase("webview.db-wal");
		this.deleteDatabase("webviewCookiesChromium.db");
		this.deleteDatabase("webviewCookiesChromiumPrivate.db");
	}
	private void deleteFile(File file){
		if(file.isFile()){
			file.delete();
		}else {
			for(File item: file.listFiles()){
				deleteFile(item);
			}
			file.delete();
		}
	}
}
