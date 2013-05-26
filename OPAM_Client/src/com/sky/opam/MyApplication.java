package com.sky.opam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sky.opam.model.MsgSaved;

import android.R.bool;
import android.app.Application;


public class MyApplication extends Application {
	private static final int MsgSaveMax = 30;
	private String userID="";
	private String userName;
	private boolean FirstOnLine = true;
	private boolean OnlineMode = true;
	private List<String> userList = new ArrayList<String>();
	private Map<String, Integer> msgUnReadMap = new HashMap<String, Integer>();
	private Map<String, List<MsgSaved>> msgMap = new HashMap<String, List<MsgSaved>>();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public List<String> getUserList() {
		return userList;
	}

	public Map<String, List<MsgSaved>> getMsgMap() {
		return msgMap;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}	

	public Map<String, Integer> getMsgUnReadMap() {
		return msgUnReadMap;
	}
	

	public boolean isFirstOnLine() {
		return FirstOnLine;
	}

	public void setFirstOnLine(boolean firstOnLine) {
		FirstOnLine = firstOnLine;
	}
	
	public boolean isOnlineMode() {
		return OnlineMode;
	}

	public void setOnlineMode(boolean onlineMode) {
		OnlineMode = onlineMode;
	}

	/////////////////////////////////////////////////////////////////////////
	public void addUser(String user){
		if(user!=null && user.contains("*") && !user.equals(userID)){
			if(!userList.contains(user)){
				userList.add(user);
			}
			if(!msgMap.containsKey(user)){
				List<MsgSaved> list = new ArrayList<MsgSaved>();
				msgMap.put(user, list);
			}
		}
		
	}
	
	public void detUser(String user){
		if(userList.contains(user)){
			userList.remove(user);
		}
	}
	
	public void saveMsg(MsgSaved msg){
		String mapID = msg.getFrom();
		if(mapID.equals(userID)){
			mapID=msg.getTo();
		}
		List<MsgSaved> list;
		if(msgMap.containsKey(mapID)){
			list=msgMap.get(mapID);
		}else {
			list = new ArrayList<MsgSaved>();
			msgMap.put(mapID, list);
		}
		while(list.size()>=MsgSaveMax){
			list.remove(0);
		}
		list.add(msg);
		msgMap.put(mapID, list);
	}
	
	public List<MsgSaved> getMsgSaved(String friendID){
		if (msgMap.containsKey(friendID)) {
			return msgMap.get(friendID);
		}else {
			return null;
		}
	}
	
	public int getNumUnread(String userID){
		if(!msgUnReadMap.containsKey(userID)){
			return 0;
		}else {
			return msgUnReadMap.get(userID);
		}
	}
	
	public void increaseNumUnread(String userID){
		if(!msgUnReadMap.containsKey(userID)){
			msgUnReadMap.put(userID, 1);
		}else {
			int num = msgUnReadMap.get(userID);
			msgUnReadMap.put(userID, num+1);
		}
	}
	
	public void ClearNumUnread(String userID){
		msgUnReadMap.remove(userID);
	}
	
	///////////////////other function//////////////////
	public void clear(){
		userList.clear();
		msgMap.clear();
		msgUnReadMap.clear();
		FirstOnLine=true;
	}
	
	public void offlineMode(){
		userList.clear();		
		msgUnReadMap.clear();
		addUser("opam*Simsimi");
		for(String key : msgMap.keySet()){
			if(!key.equals("opam*Simsimi")){
				msgMap.remove(key);
			}
		}
		OnlineMode = false;
	}
	
}
