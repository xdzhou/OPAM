package com.sky.opam.fragment;

import java.util.List;

import com.sky.opam.DayViewActivity;
import com.sky.opam.R;
import com.sky.opam.model.User;
import com.sky.opam.task.DownloadImageTask;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class Account_Fragment extends ListFragment{
	private List<User> users;
	private MyApp myApp;
	private DBworker worker;
	
	public Account_Fragment() {	
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		myApp = (MyApp)getActivity().getApplication();
		String CurrentLogin = myApp.getLogin();
		worker = new DBworker(getActivity());
		users = worker.getAllUser();
		
		AccountAdapter adapter = new AccountAdapter(getActivity());
		for(int i=0; i<users.size(); i++){
			User u = users.get(i);
			adapter.add(new AccountItemContent(u.getLogin(), u.getName()));
		}
		adapter.add(new AccountItemContent(null, "add new account"));
		setListAdapter(adapter);
	}
	
	private class AccountItemContent {
		public String login;
		public String name;
		public AccountItemContent(String login, String name) {
			this.login = login; 
			this.name = name;
		}
	}
	
	public class AccountAdapter extends ArrayAdapter<AccountItemContent> {
		public AccountAdapter(Context context) {
			super(context, 0);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.tab_item_view, null);
			ImageView icon = (ImageView) convertView.findViewById(R.id.imageview);
			TextView title = (TextView) convertView.findViewById(R.id.textview);
			if(position == users.size()){
				icon.setImageResource(android.R.drawable.ic_menu_add);		
			}else {
				new DownloadImageTask(icon).execute("http://trombi.it-sudparis.eu/photo.php?uid="+getItem(position).login+"&h=55&w=55");						
			}
			title.setText(getItem(position).name);
			return convertView;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(position == users.size()){
			
		}else {
			User selectedUser = users.get(position);
			myApp.setLogin(selectedUser.getLogin());
			worker.setDefaultUser(selectedUser.getLogin());
			getActivity().setResult(MyApp.Refresh);
			getActivity().finish();
		}
	}
}
