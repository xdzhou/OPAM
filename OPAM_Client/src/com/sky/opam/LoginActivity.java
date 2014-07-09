package com.sky.opam;

import com.google.gson.Gson;
import com.sky.opam.R;
import com.sky.opam.model.User;
import com.sky.opam.model.VersionInfo;
import com.sky.opam.task.AgendaDownloadTask;
import com.sky.opam.tool.Chiffrement;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.AndroidUtil;
import com.sky.opam.tool.OpamUtil;
import com.sky.opam.tool.TimeUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class LoginActivity extends Activity {
    private AutoCompleteTextView tfID = null;
    private EditText tfMDP = null;
    private String login;
    private String password;
    private Context context;
    private MyApp myApp;

    private DBworker worker;
    private User currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_activity);
        context = this;
        myApp = (MyApp) getApplication();
        myApp.setCurrentWeekNum(TimeUtil.getNumWeek());
        worker = new DBworker(context);
        
        //First Use App, show Version Info
        boolean oldAutoLoginFlag = worker.getAutoLogin(context);
        boolean isFirstUse = OpamUtil.isFirstUseApp(context);
        if(isFirstUse){
        	worker.setAutoLogin(context, false);
            VersionInfo versionInfo = (VersionInfo) new Gson().fromJson(getResources().getString(R.string.version_10_info), VersionInfo.class);
            getSharedPreferences("share", 0).edit().putBoolean("isFirstIn", false).commit(); 
            AlertDialog.Builder builder = OpamUtil.showVersionInfo(context, versionInfo);
            builder.show();
        }       
        
        Button monBtn = (Button) findViewById(R.id.btnVAD);
        monBtn.getBackground().setAlpha(150);
        tfID = (AutoCompleteTextView) findViewById(R.id.txtID);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line);
        for(User tempUser : worker.getAllUser()){
        	adapter.add(tempUser.getLogin());
        }
        tfID.setThreshold(1);
        tfID.setAdapter(adapter);
        tfMDP = (EditText) findViewById(R.id.txtMDP);
        tfMDP.getBackground().setAlpha(150);

        User fUser = worker.getDefaultUser();
        if (fUser != null) {
            currentUser = fUser;
            login = fUser.getLogin();
            myApp.setLogin(login);  
            tfID.setText(fUser.getLogin());
            //tfMDP.setText(Chiffrement.decrypt(fUser.getPasswoed(), "OPAM"));         
            if(worker.getAutoLogin(context)){
            	if (myApp.getCurrentWeekNum() == currentUser.getNumWeekUpdated()) WeekAgendaShow(myApp.getCurrentWeekNum());
            	else askForUpdate();
            }
        }
        if(isFirstUse) worker.setAutoLogin(context, oldAutoLoginFlag);

        monBtn.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View v) {
                login = tfID.getText().toString();
                password = tfMDP.getText().toString();

                if (login.length() == 0)
                    AndroidUtil.showInfo(context,getResources().getString(R.string.login_null_alert));
                else if (password.length() == 0)
                	AndroidUtil.showInfo(context,getResources().getString(R.string.pw_null_alert));
                else {
                    currentUser = worker.getUser(login);
                    myApp.setLogin(login);
                    if (currentUser == null) {
                            downloadCharge();
                    } else {
                        worker.setDefaultUser(currentUser.getLogin());
                        String mdp = Chiffrement.decrypt(currentUser.getPasswoed(),"OPAM");
                        if (!mdp.equals(password)) {
                            AndroidUtil.showInfo(context,"Password incorrect.");
                        } else {
                            if (myApp.getCurrentWeekNum() == currentUser.getNumWeekUpdated()) {
                                WeekAgendaShow(myApp.getCurrentWeekNum());
                            } else {
                                askForUpdate();
                            }
                        }
                    }
                }
            }
        });

    }

    // reponse au le update request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MyApp.Update) {
            finishActivity(requestCode);
            downloadCharge();
        } else if (resultCode == MyApp.Exit || resultCode == 0) {
            finish();
        }
    }

    // download the course
    private void downloadCharge() {
        if (AndroidUtil.isNetworkAvailable(context)) {
        	login = tfID.getText().toString();
        	if(login.equals(myApp.getLogin())){
        		password = tfMDP.getText().toString();
        		//从weekViewActivity的更新命令 需要设置密码
        		if(password.equals("")) password = Chiffrement.decrypt(worker.getUser(login).getPasswoed(), "OPAM");
        	}else {
				login = myApp.getLogin();
				password = Chiffrement.decrypt(worker.getUser(login).getPasswoed(), "OPAM");
			}         
            AgendaDownloadTask agendaDownloadTask = new AgendaDownloadTask(context, new AgendaHandler());
            agendaDownloadTask.execute(login,password);
        } else {
            AndroidUtil.showInfo(context, getResources().getString(R.string.network_unavailable));
        }
    }

    // ask for update the base de donnée if it's too old
    private void askForUpdate() {
        new AlertDialog.Builder(this)
            .setTitle("update")
            .setMessage(R.string.update_classInfo_msg)
            .setPositiveButton(R.string.yes, onclick)
            .setNegativeButton(R.string.no, onclick).show();
    }

    DialogInterface.OnClickListener onclick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                WeekAgendaShow(currentUser.getNumWeekUpdated());
                break;
            case Dialog.BUTTON_NEUTRAL:
                AndroidUtil.showInfo(context,"unknow choise...");
                break;
            case Dialog.BUTTON_POSITIVE:
                downloadCharge();
                break;
            }
        }
    };

    private void WeekAgendaShow(int numWeek) {
    	worker.setDefaultUser(currentUser.getLogin());
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, WeekViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("numWeek", numWeek);
        intent.putExtras(bundle);
        startActivityForResult(intent, MyApp.rsqCode);
    }

    long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK&& event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                AndroidUtil.showInfo(context, "on more time to exit");
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private class AgendaHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == R.integer.OK){
				currentUser = worker.getUser(login);
				WeekAgendaShow(currentUser.getNumWeekUpdated());
			}else {
				Bundle b = msg.getData();
				String errorMsg = b.getString("error");
				AndroidUtil.showInfo(context, errorMsg);
			}		
		} 	
    }
}