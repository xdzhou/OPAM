package com.sky.opam.fragment;

import java.io.File;

import com.sky.opam.AccountActivity;
import com.sky.opam.AppConfigActivity;
import com.sky.opam.R;
import com.sky.opam.task.DownloadImageTask;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.Tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
public class Menu_Fragment extends ListFragment
{
	private String login;
	private String user_name;
	
	public Menu_Fragment() {
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		//user_name = DBworker.getInstance().getUser(login).getName();
		
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
	
	private class MenuItemContent 
	{
		public String tag;
		public int iconRes;
		public MenuItemContent(String tag, int iconRes) 
		{
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}
	
	public class MenuAdapter extends ArrayAdapter<MenuItemContent> 
	{
		public MenuAdapter(Context context) 
		{
			super(context, 0);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
//			if(convertView != null) 
//				return convertView;
			if(position == 0) 
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.tab_profile_item_view, null);
			else 
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.tab_item_view, null);
			
			ImageView icon = (ImageView) convertView.findViewById(R.id.imageview);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.textview);
			title.setText(getItem(position).tag);
			
			if(position == 0)
			{
				String imgPath = getActivity().getFilesDir() + "/" + login +".jpg";
				if(new File(imgPath).exists())
				{
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
					icon.setImageBitmap(bitmap);
				}
			}				
			return convertView;			
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
//		if(position == 1){
//			Intent intent = new Intent();
//	        intent.setClass(getActivity(), DayViewActivity.class);
//	        Bundle bundle = new Bundle();
//	        bundle.putInt("numWeek", Tool.getNumWeek());
//	        bundle.putInt("dayOfWeek", Tool.getDayOfWeek());
//	        intent.putExtras(bundle);      
//	        startActivityForResult(intent, MyApp.rsqCode);
//		}else if (position == 2) {
//			getActivity().setResult(MyApp.Update);
//			getActivity().finish();
//		}else if (position == 3) {
//			Intent intent = new Intent();
//			intent.setClass(getActivity(), AccountActivity.class);
//			getActivity().startActivityForResult(intent, MyApp.rsqCode);
//		}else if (position == 4) {
//			Intent intent = new Intent();
//			intent.setClass(getActivity(), AppConfigActivity.class);
//			getActivity().startActivityForResult(intent, MyApp.rsqCode);
//		}else if (position == 5){
//			getActivity().setResult(MyApp.Exit);
//			getActivity().finish();
//		}
	}
}
