package com.sky.opam;

import com.sky.opam.R;
import com.sky.opam.model.User;
import com.sky.opam.task.AgendaDownloadTask;
import com.sky.opam.tool.Chiffrement;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.MyApp;
import com.sky.opam.tool.Tool;

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
    private User u;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("LoginActivity created");
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.login_activity);
            context = this;
            myApp = (MyApp) getApplication();
            myApp.setCurrentWeekNum(Tool.getNumWeek());
            worker = new DBworker(context);

            Button monBtn = (Button) findViewById(R.id.btnVAD);
            monBtn.getBackground().setAlpha(150);
            tfID = (AutoCompleteTextView) findViewById(R.id.txtID);
            String[] countries = new String[] { "afghanistan", "albania","algeria", "american aamoa", "andorra" };
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, countries);
            tfID.setAdapter(adapter);
            // tfID.getBackground().setAlpha(150);
            tfMDP = (EditText) findViewById(R.id.txtMDP);
            tfMDP.getBackground().setAlpha(150);

            User fUser = worker.getDefaultUser();
            if (fUser != null) {
                u = fUser;
                login = fUser.getLogin();
                myApp.setLogin(login);  
                tfID.setText(fUser.getLogin());
                tfMDP.setText(Chiffrement.decrypt(fUser.getPasswoed(), "OPAM"));
                if (myApp.getCurrentWeekNum() == u.getNumWeekUpdated()) {
                    WeekAgendaShow(myApp.getCurrentWeekNum());
                } else {
                    askForUpdate();
                }
            }

            monBtn.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(View v) {
                    login = tfID.getText().toString();
                    password = tfMDP.getText().toString();

                    if (login.length() == 0)
                        Tool.showInfo(context,"input your login, please!");
                    else if (password.length() == 0)
                    	Tool.showInfo(context,"input your password, please!");
                    else {
                        u = worker.getUser(login);
                        myApp.setLogin(login);
                        if (u == null) {
                                downloadCharge();
                        } else {
                            worker.setDefaultUser(u.getLogin());
                            String mdp = Chiffrement.decrypt(u.getPasswoed(),"OPAM");
                            if (!mdp.equals(password)) {
                                Tool.showInfo(context,"Password incorrect.");
                            } else {
                                if (myApp.getCurrentWeekNum() == u.getNumWeekUpdated()) {
                                    WeekAgendaShow(myApp.getCurrentWeekNum());
                                } else {
                                    askForUpdate();
                                }
                            }
                        }
                    }
                }
            });
            // animation en debut
            Resources res = this.getResources();
            TransitionDrawable transition = (TransitionDrawable) res.getDrawable(R.drawable.expand_collapse);
            ImageView image = (ImageView) findViewById(R.id.bgID);
            image.setImageDrawable(transition);
            transition.startTransition(5000);
    }

    // reponse au le update request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	System.out.println("Login "+requestCode+" "+ resultCode);
        if (resultCode == myApp.Update) {
            finishActivity(requestCode);
            downloadCharge();
        } else if (resultCode == myApp.Exit || resultCode == 0) {
            finish();
        }
    }

    // download the course
    private void downloadCharge() {
        if (Tool.isNetworkAvailable(context)) {
        	login = tfID.getText().toString();
        	if(login.equals(myApp.getLogin())){
        		password = tfMDP.getText().toString();
        	}else {
				login = myApp.getLogin();
				password = Chiffrement.decrypt(worker.getUser(login).getPasswoed(), "OPAM");
			}
            
            AgendaDownloadTask agendaDownloadTask = new AgendaDownloadTask(context, new AgendaHandler());
            agendaDownloadTask.execute(login,password);
        } else {
            Tool.showInfo(context, "Network is not available;");
        }
    }

    // ask for update the base de donnée if it's too old
    private void askForUpdate() {
        new AlertDialog.Builder(this)
            .setTitle("update")
            .setMessage("Your courses are a bit outdated, do you want to update ?")
            .setPositiveButton("Yes", onclick)
            .setNegativeButton("No", onclick).show();
    }

    DialogInterface.OnClickListener onclick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                WeekAgendaShow(u.getNumWeekUpdated());
                break;
            case Dialog.BUTTON_NEUTRAL:
                Tool.showInfo(context,"unknow choise...");
                break;
            case Dialog.BUTTON_POSITIVE:
                downloadCharge();
                break;
            }
        }
    };

    private void WeekAgendaShow(int numWeek) {
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
                Tool.showInfo(context, "on more time to exit");
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
				//Tool.showInfo(context, "Data updated !");
				u = worker.getUser(login);
				WeekAgendaShow(u.getNumWeekUpdated());
			}else {
				Bundle b = msg.getData();
				String errorMsg = b.getString("error");
				Tool.showInfo(context, errorMsg);
			}		
		} 	
    }
}