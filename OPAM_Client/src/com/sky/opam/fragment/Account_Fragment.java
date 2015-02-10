package com.sky.opam.fragment;

import java.io.File;
import java.util.List;

import com.sky.opam.R;
import com.sky.opam.model.User;
import com.sky.opam.task.DownloadImageTask;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.Tool;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class Account_Fragment extends ListFragment
{
	private List<User> users;
	private DBworker worker;
	private String newLogin;
	private int LastLoginPosition = 0;
	
	public Account_Fragment() 
	{
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		worker = DBworker.getInstance();
		//users = worker.getAllUser();
		
//		adapter = new AccountAdapter(getActivity());
//		chargeAdaper();
//		setListAdapter(adapter);
	}
	/*
	private void chargeAdaper(){
		for(int i=0; i<users.size(); i++){
			User u = users.get(i);
			//adapter.add(new AccountItemContent(u.getLogin(), u.getName()));
			LastLoginPosition ++;
		}
		adapter.add(new AccountItemContent(null, getResources().getString(R.string.add_new_account)));
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
		private int imageSize = Tool.dip2px(getContext(), 60);
		
		public AccountAdapter(Context context) {
			super(context, 0);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_item_view, null);
			ImageView icon = (ImageView) convertView.findViewById(R.id.icon_view);
			icon.getLayoutParams().height = icon.getLayoutParams().width = imageSize;
			TextView title = (TextView) convertView.findViewById(R.id.name_view);
			if(position == users.size())
			{
				icon.setImageResource(android.R.drawable.ic_menu_add);		
			}
			else
			{
				//new DownloadImageTask(icon).execute("http://trombi.it-sudparis.eu/photo.php?uid="+getItem(position).login+"&h=55&w=55");
				String imgPath = getActivity().getFilesDir() + "/" + getItem(position).login +".jpg";
				if(new File(imgPath).exists())
				{
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
					icon.setImageBitmap(bitmap);
				}
				if(!users.get(position).getLogin().equals("myApp.getLogin()")){
					ImageView del_icon = (ImageView) convertView.findViewById(R.id.del_view);
					del_icon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
					del_icon.setOnClickListener(new delAccountListener(position));
				}
			}
			title.setText(getItem(position).name);
			return convertView;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(position == users.size()){
			AlertDialog.Builder login_dialog = new AlertDialog.Builder(getActivity());
			final View viewDia = LayoutInflater.from(getActivity()).inflate(R.layout.login_dialog, null);
			final EditText loginET = (EditText) viewDia.findViewById(R.id.txtID);
			final EditText pwET = (EditText) viewDia.findViewById(R.id.txtMDP);
			login_dialog.setTitle(getResources().getString(R.string.add_new_account));
			login_dialog.setIcon(android.R.drawable.ic_menu_edit);
			login_dialog.setView(viewDia);
			login_dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {		
				@Override
				public void onClick(DialogInterface dialog, int which) {
					newLogin = loginET.getText().toString();
					String password = pwET.getText().toString();
//                    if (newLogin.length() == 0)
//                        Tool.showInfo(getActivity(),getResources().getString(R.string.login_null_alert));
//                    else if (password.length() == 0)
//                    	Tool.showInfo(getActivity(),getResources().getString(R.string.pw_null_alert));
//                    else if (worker.isLoginExist(newLogin))
//                    	Tool.showInfo(getActivity(),"Login Exist !");
//                    else {
//                    	dialog.dismiss();
//    					AgendaDownloadTask agendaDownloadTask = new AgendaDownloadTask(getActivity(), new AgendaHandler());
//    		            agendaDownloadTask.execute(newLogin,password);
//                    }				
				}
			});
			login_dialog.show();
		}else {
			User selectedUser = users.get(position);
			//myApp.setLogin(selectedUser.getLogin());
			worker.setDefaultUser(selectedUser.getLogin());
			//getActivity().setResult(MyApp.Refresh);
			getActivity().finish();
		}
	}
	
	private class AgendaHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == R.integer.OK){
				User u = worker.getUser(newLogin);
				users.add(u);
				adapter.insert(new AccountItemContent(u.getLogin(), u.getName()), LastLoginPosition);
				LastLoginPosition ++;
				adapter.notifyDataSetChanged();
			}else {
				Bundle b = msg.getData();
				String errorMsg = b.getString("error");
				//Tool.showInfo(getActivity(), errorMsg);
			}		
		} 	
    }
	
	class delAccountListener implements View.OnClickListener{
		private int position;
		public delAccountListener(int p){
			position = p;
		}
		@Override
		public void onClick(View v) {
			AlertDialog.Builder del_dialog = new AlertDialog.Builder(getActivity());
			del_dialog.setTitle(getResources().getString(R.string.account_del));
			del_dialog.setIcon(android.R.drawable.ic_menu_info_details);
			del_dialog.setMessage(getResources().getString(R.string.account_del_alert)+": "+users.get(position).getName()+" ?");
			del_dialog.setNegativeButton(getResources().getString(R.string.no), null);
			del_dialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					worker.delUser(users.get(position).getLogin(), true);
					users.remove(position);
					LastLoginPosition--;
					adapter.clear();
					chargeAdaper();
					adapter.notifyDataSetChanged();
				}	
			});
			del_dialog.show();
		}	
	}
	*/
}
