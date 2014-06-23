package com.sky.opam.fragment;

import com.sky.opam.AccountActivity;
import com.sky.opam.AppConfig;
import com.sky.opam.DayViewActivity;
import com.sky.opam.R;
import com.sky.opam.WeekViewActivity;
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
public class Menu_Fragment extends ListFragment{
	private String login;
	private String user_name;
	
	public Menu_Fragment() {
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MyApp myApp = (MyApp)getActivity().getApplication();
		login = myApp.getLogin();
		user_name = new DBworker(getActivity()).getUser(login).getName();
		
		MenuAdapter adapter = new MenuAdapter(getActivity());
		adapter.add(new MenuItemContent(user_name, android.R.drawable.sym_def_app_icon));
		adapter.add(new MenuItemContent(getResources().getString(R.string.today_class), android.R.drawable.ic_menu_compass));
		//adapter.add(new MenuItemContent("Course Info", android.R.drawable.ic_menu_view));
		adapter.add(new MenuItemContent(getResources().getString(R.string.update), android.R.drawable.ic_menu_rotate));
		adapter.add(new MenuItemContent(getResources().getString(R.string.account), android.R.drawable.ic_menu_myplaces));
		adapter.add(new MenuItemContent(getResources().getString(R.string.setting), android.R.drawable.ic_menu_manage));
		adapter.add(new MenuItemContent(getResources().getString(R.string.exit), android.R.drawable.ic_menu_close_clear_cancel));
		
		setListAdapter(adapter);
	}
	
	private class MenuItemContent {
		public String tag;
		public int iconRes;
		public MenuItemContent(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}
	
	public class MenuAdapter extends ArrayAdapter<MenuItemContent> {
		public MenuAdapter(Context context) {
			super(context, 0);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				if(position == 0) convertView = LayoutInflater.from(getContext()).inflate(R.layout.tab_profile_item_view, null);
				else convertView = LayoutInflater.from(getContext()).inflate(R.layout.tab_item_view, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.imageview);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.textview);
			title.setText(getItem(position).tag);
			
			if(position == 0) new DownloadImageTask(icon).execute("http://trombi.it-sudparis.eu/photo.php?uid="+login+"&h=80&w=80");
				
			return convertView;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(position == 1){
			Intent intent = new Intent();
	        intent.setClass(getActivity(), DayViewActivity.class);
	        Bundle bundle = new Bundle();
	        bundle.putInt("numWeek", Tool.getNumWeek());
	        bundle.putInt("dayOfWeek", Tool.getDayOfWeek());
	        intent.putExtras(bundle);      
	        startActivityForResult(intent, MyApp.rsqCode);
		}else if (position == 2) {
			getActivity().setResult(MyApp.Update);
			getActivity().finish();
		}else if (position == 3) {
			Intent intent = new Intent();
			intent.setClass(getActivity(), AccountActivity.class);
			getActivity().startActivityForResult(intent, MyApp.rsqCode);
		}else if (position == 4) {
			Intent intent = new Intent();
			intent.setClass(getActivity(), AppConfig.class);
			getActivity().startActivityForResult(intent, MyApp.rsqCode);
		}else if (position == 5){
			getActivity().setResult(MyApp.Exit);
			getActivity().finish();
		}
	}
}
