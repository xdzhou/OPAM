package com.sky.opam;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.sky.opam.R;
import com.sky.opam.model.Cours;
import com.sky.opam.model.DataCompo;
import com.sky.opam.model.User;
import com.sky.opam.task.AgendaDownloadTask;
import com.sky.opam.tool.Chiffrement;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.PullXMLReader;
import com.sky.opam.tool.Tool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.Toast;

public class LoginActivity extends Activity {
        private AutoCompleteTextView tfID = null;
        private EditText tfMDP = null;
        private String login;
        private String password;
        private Context context;

        DBworker worker;
        User u;

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.main);
                //context = getApplicationContext();
                context = this;
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

                User fUser = worker.defaultUser();
                if (!fUser.getLogin().equals("")) {
                    u = fUser;
                    login = fUser.getLogin();
                    if (Tool.getNumWeek() == u.getThisweek()) {
                            AgendaShow();
                    } else {
                            askForUpdate();
                    }
                }

                tfID.setText(fUser.getLogin());
                if (!fUser.getPasswoed().equals("")) {
                    tfMDP.setText(Chiffrement.decrypt(fUser.getPasswoed(), "OPAM"));
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
                            u = worker.findUser(login);
                            if (u == null) {
                                    downloadCharge();
                            } else {
                                worker.updateDefaultUser(u.getLogin());
                                String mdp = Chiffrement.decrypt(u.getPasswoed(),"OPAM");
                                if (!mdp.equals(password)) {
                                    Tool.showInfo(context,"Password incorrect.");
                                } else {
                                    if (Tool.getNumWeek() == u.getThisweek()) {
                                        AgendaShow();
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
                if (requestCode == 0 && resultCode == 22) {
                        finishActivity(requestCode);
                        downloadCharge();
                } else if (resultCode == 1) {
                        finish();
                }
        }

        // download the course
        private void downloadCharge() {
            if (Tool.isNetworkAvailable(context)) {
                AgendaDownloadTask agendaDownloadTask = new AgendaDownloadTask(context, worker, new AgendaHandler());
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
                                AgendaShow();
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

        private void AgendaShow() {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, ClassTableActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("login", login);
            bundle.putString("numweek", "" + u.getThisweek());
            intent.putExtras(bundle);
            startActivityForResult(intent, 0);
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
				if(msg.what == 10){
					u = worker.findUser(login);
					AgendaShow();
				}else {
					Bundle b = msg.getData();
					String errorMsg = b.getString("error");
					Tool.showInfo(context, errorMsg);
				}
				
			}
        	
        }
}