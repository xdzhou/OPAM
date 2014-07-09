package com.sky.opam;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

public class TestActivity extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        
        AccountManager am = AccountManager.get(this); 
        for(Account a : am.getAccounts()){
        	System.out.println(a.type+" -- "+a.name);
        }
        
	}
	
	private class OnTokenAcquired implements AccountManagerCallback<Bundle> {  
	    @Override  
	    public void run(AccountManagerFuture<Bundle> result) {  
	        // Get the result of the operation from the AccountManagerFuture.  
	        Bundle bundle;
			try {
				bundle = result.getResult();
				String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
				System.out.println("Token: "+token);	
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  
	    }  
	} 
	
	private class OnError implements Callback{
		@Override
		public boolean handleMessage(Message msg) {
			System.out.println(msg.getData().toString());
			return false;
		}
		
	}

}
