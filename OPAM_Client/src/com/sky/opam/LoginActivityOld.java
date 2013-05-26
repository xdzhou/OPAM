package com.sky.opam;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
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
import org.apache.http.util.VersionInfo;

import com.sky.opam.R;
import com.sky.opam.model.Cours;
import com.sky.opam.model.DataCompo;
import com.sky.opam.model.User;
import com.sky.opam.outil.Chiffrement;
import com.sky.opam.outil.DBworker;
import com.sky.opam.outil.Navigateur;
import com.sky.opam.outil.PullXMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

public class LoginActivityOld extends Activity {
	//private final static String TAG="OPAMActivity";
	private Button monBtn = null;
	private AutoCompleteTextView tfID = null;
	private EditText tfMDP = null;
    private String login;
    private String password;
    
	String rspXML;
	List<Cours> cours = new ArrayList<Cours>();
	DataCompo dataCompo;
	DBworker worker;
	User u ;
	ProgressDialog dialog;
	Thread mThreadLoadApps;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        worker = new DBworker(getApplicationContext());
 
        monBtn=(Button)findViewById(R.id.btnVAD);
        monBtn.getBackground().setAlpha(150);
        tfID=(AutoCompleteTextView)findViewById(R.id.txtID);
        String[] countries = new String[]{"afghanistan","albania","algeria","american aamoa","andorra"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, countries);
        tfID.setAdapter(adapter);
        //tfID.getBackground().setAlpha(150);
        tfMDP=(EditText)findViewById(R.id.txtMDP);
        tfMDP.getBackground().setAlpha(150);
        
        User fUser = worker.defaultUser();
        if(!fUser.getLogin().equals("")){
        	u=fUser;
        	login=fUser.getLogin();
        	if(getNumWeek()==u.getThisweek()){
				AgendaShow();
			}else{
				askForUpdate();
			}
        }
        
        tfID.setText(fUser.getLogin());
        if(!fUser.getPasswoed().equals("")){
        	tfMDP.setText(Chiffrement.decrypt(fUser.getPasswoed(), "OPAM"));
        }
        
        
        monBtn.setOnClickListener(new android.view.View.OnClickListener() {			
			public void onClick(View v) {
				
				login = tfID.getText().toString();
				password = tfMDP.getText().toString();

				if(login.length()==0) Toast.makeText(LoginActivityOld.this, "Tapez votre identifiant,SVP!", Toast.LENGTH_SHORT).show();
				else if(password.length()==0) Toast.makeText(LoginActivityOld.this, "Tapez votre mot de passe,SVP!", Toast.LENGTH_SHORT).show();
				else {				        
			        u = worker.findUser(login);
			        if(u==null){
			        	downloadCharge();
			        }else{
			        	worker.updateDefaultUser(u.getLogin());
			        	String mdp = Chiffrement.decrypt(u.getPasswoed(), "OPAM");
			        	if(!mdp.equals(password)){
			        		showInfo("Password incorrect.");
			        	}else {
							if(getNumWeek()==u.getThisweek()){
								AgendaShow();
							}else{
								askForUpdate();
							}
							
						}
			        }
				}		        
			}
		});   
        //animation en debut
        Resources res = this.getResources();
        TransitionDrawable transition = (TransitionDrawable) res.getDrawable(R.drawable.expand_collapse);
        ImageView image = (ImageView) findViewById(R.id.bgID);
        image.setImageDrawable(transition);
        transition.startTransition(5000);       
    }  
	
	//reponse au le update request
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==0 && resultCode==22){		
			downloadCharge();
		}else if (resultCode==1) {
			finish();
		}
	}

	// mise a jour des cours
	private void downloadCharge() {
		dialog = new ProgressDialog(this);  
        dialog.setIcon(R.drawable.download_icon);  
        dialog.setTitle("downloading the data");  
        dialog.setMessage("Please wait while loading the list of class...");  
        dialog.setCancelable(false);  
        dialog.show();
        
      //un thread qui s'ocupe au mis à jour
        mThreadLoadApps = new Thread(){ 
    		@Override  
            public void run() {
    			String msgData = "";
    			String resulta = getCours();
    			if(resulta.equals("OK")){			
    				try {
    					dataCompo = PullXMLReader.readXML(new ByteArrayInputStream(rspXML.getBytes("UTF-8")));
    					if (dataCompo.getId()==2) {
        					msgData = dataCompo.getCours().get(0).type;
        				}else {
        					if(u==null){
        						u = new User(login,Chiffrement.encrypt(password, "OPAM"),dataCompo.getUsername());
        						worker.addUser(u);
        					}
        					u.setThisweek(dataCompo.getNumweek());
        					worker.updateUser(u);
        					worker.updateDefaultUser(login);
        					worker.delAllCours(login);
        					cours = dataCompo.getCours();
        					for(Cours c : cours){
        						c.login=login;
        						worker.addCours(c);
        					}
        					msgData = "No Probleme";
        				}
    				} catch (UnsupportedEncodingException e) {
    					msgData = e.toString();
    				}
    								
    			}else {
    				msgData = resulta;
				}
    			Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("data", msgData);
                msg.setData(b);	
                handler.sendMessage(msg);
    		}
    	};
    	mThreadLoadApps.start();
	}
	
	//use the httpclient for download the xml
	private String getCours(){
		login = tfID.getText().toString();
		password = tfMDP.getText().toString();
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://intagenda.appspot.com/agendaopam");
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("username", tfID.getText().toString()));
		//System.out.println("Hello:"+mdpS);
		data.add(new BasicNameValuePair("password", Chiffrement.encrypt(password, "OPAM")));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			HttpResponse response = client.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			if(status==200){
				HttpEntity entity = response.getEntity();
				rspXML = EntityUtils.toString(entity);
				httpPost.abort();
				return "OK";
			}else{
				return "Can't connect to the server, status:"+status+" recevied.";
			}
		} catch (Exception e) {
			return e.toString();			
		}finally{
			client.getConnectionManager().shutdown();
		}
	}

	private void showInfo(String title, String msg){
        new AlertDialog.Builder(this)
        .setTitle(title).setMessage(msg)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
        .show();        
    }
	
	private void showInfo(String msg){
		Toast.makeText(LoginActivityOld.this, msg, Toast.LENGTH_SHORT).show();        
    }
	
	//ask for update the base de donnée if it's too old
	private void askForUpdate(){		
        new AlertDialog.Builder(this)
        .setTitle("update").setMessage("Your calendar is a litter old, do you want update ?")
        .setPositiveButton("Yes", onclick)
        .setNegativeButton("No", onclick)
        .show();  
    }
	
	DialogInterface.OnClickListener onclick = new DialogInterface.OnClickListener() {  
        @Override  
        public void onClick(DialogInterface dialog, int which) {  
            switch (which) {  
                case Dialog.BUTTON_NEGATIVE:  
                	AgendaShow();
                    break;  
                case Dialog.BUTTON_NEUTRAL:  
                    showInfo("unknow choise...");
                    break;  
                case Dialog.BUTTON_POSITIVE:  
                	downloadCharge();
                    break;  
            }  
        }  
    };
    
    private Handler handler = new Handler(){  
        @Override  
        public void handleMessage(Message msg) {   
            super.handleMessage(msg);
            Bundle b = msg.getData();
            String data = b.getString("data");
            if(dialog!=null && dialog.isShowing()){
        		dialog.dismiss();
        		dialog=null;
        		if(mThreadLoadApps!=null){
        			mThreadLoadApps.interrupt();
        		}	
        	}
            if(data.equals("No Probleme")){
            	AgendaShow();
            }else{
            	showInfo("error",data);
            }
        }  
    };
    
    private void AgendaShow(){
        Intent intent = new Intent();
        intent.setClass(LoginActivityOld.this, AgendaActivity.class);
        Bundle bundle = new Bundle();  
        bundle.putString("login", login); 
        bundle.putString("numweek", ""+u.getThisweek());
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
        
    }
    
	private int getNumWeek(){
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int xq = c.get(Calendar.DAY_OF_WEEK);
		if(xq==1){
			c.set(Calendar.DATE, c.get(Calendar.DATE)-1);
		}
		SimpleDateFormat sdf = new  SimpleDateFormat();
		sdf.applyPattern("w");
		return Integer.parseInt(sdf.format(c.getTime()));
	}

	
	long exitTime = 0;
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "on more time", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();  
	        } else {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);  	
	}
	
}