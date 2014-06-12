package com.sky.opam;

import com.sky.opam.fragment.Account_Fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class AccountActivity extends FragmentActivity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		System.out.println("AccountActivity created");
		super.onCreate(savedInstanceState);
        setContentView(R.layout.seul_fragment);
        
        Account_Fragment fragment = new Account_Fragment();
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.agenda_fragement,fragment);
		ft.commit();
	}

}
