package com.sky.opam.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.loic.common.Chiffrement;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.model.User;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;
import com.sky.opam.service.IntHttpService.asyncLoginReponse;
import com.sky.opam.tool.DBworker;

public class LoginFragment extends OpamFragment implements asyncLoginReponse
{
	private static final String TAG = LoginFragment.class.getSimpleName();
	
	private AutoCompleteTextView loginTextView;
    private EditText passwordEditText;
    private Button enterButton;
    private ProgressDialog progressDialog;
    private DBworker worker;
    
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		worker = DBworker.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView =  inflater.inflate(R.layout.login_fragment, container, false);
		loginTextView = (AutoCompleteTextView) rootView.findViewById(R.id.txtID);
		passwordEditText = (EditText) rootView.findViewById(R.id.txtMDP);
		enterButton = (Button) rootView.findViewById(R.id.btnVAD);

		User defaultUser = worker.getDefaultUser();
		if(defaultUser != null)
			loginTextView.setText(defaultUser.login);
		
		enterButton.getBackground().setAlpha(150);
		enterButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				String login = loginTextView.getText().toString();
				String password = passwordEditText.getText().toString();
				if(login.length() == 0)
				{
					ToastUtils.show(getString(R.string.OA1004));
				}
				else if(password.length() == 0)
				{
					ToastUtils.show(getString(R.string.OA1005));
				}
				else
				{
					User currentUser = worker.getUser(login);
					if(currentUser != null)
					{
						if(currentUser.password.equals(Chiffrement.encrypt(password, IntHttpService.ENCRPT_KEY)))
							showAgenda();
						else
							passwordResetAlart();
					}
					else if(getHttpService() != null)
					{
						showProgressDialog();
						getHttpService().asyncLogin(login, password, LoginFragment.this);
					}
				}
			}
		});
		
		getGcActivity().getSupportActionBar().hide();
		getGcActivity().setLeftMenuEnable(false);
		
		return rootView;
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		getGcActivity().getSupportActionBar().show();
		getGcActivity().setLeftMenuEnable(true);
	}

	private void showAgenda()
	{
		getOpenMFM().setProfileAvatar(loginTextView.getText().toString());
		Bundle data = new Bundle();
		data.putString(AgendaViewFragment.BUNDLE_LOGIN_KEY, loginTextView.getText().toString());
		getMultiFragmentManager().showGcFragment(AgendaViewFragment.class, true, data);
	}
	
	private void passwordResetAlart()
	{
    	new AlertDialog.Builder(getActivity())
        .setTitle("Password Error")
        .setMessage("Password incorrect. If you have modified your password online these days, Please click retry!")
        .setPositiveButton(R.string.OA1008, new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int which) 
            { 
            	DBworker.getInstance().delUser(loginTextView.getText().toString());
            }
         })
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int which) 
            { 
                dialog.dismiss();
            }
         })
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show();
    }
	
	private void showProgressDialog()
	{
		if(progressDialog == null)
		{
			progressDialog = new ProgressDialog(getActivity(), 0);
	        progressDialog.setCancelable(false);
	        progressDialog.setButton("cancel", new DialogInterface.OnClickListener() 
	        {
	            public void onClick(DialogInterface dialog, int i) 
	            {
	                dialog.cancel();
	            }
	        });
	        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() 
	        {
	            public void onCancel(DialogInterface dialog) 
	            {
	                dialog.dismiss();
	            }
	        });
	        progressDialog.setIcon(android.R.drawable.ic_popup_sync);
	        progressDialog.setTitle(R.string.OA1007);
	        progressDialog.setMessage(getString(R.string.OA1006));
		}
		progressDialog.show();
	}
	
	private void closeProgressDialog()
	{
		if(progressDialog != null)
			progressDialog.dismiss();
	}

	@Override
	protected void onHttpServiceReady() 
	{
		Log.d(TAG, "onCasServiceReady...");
	}

	@Override
	public void onAsyncLoginReponse(String login, final HttpServiceErrorEnum errorEnum) 
	{
		closeProgressDialog();
		if(errorEnum == HttpServiceErrorEnum.OkError && isAdded())
		{
			worker.setDefaultUser(login);
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					showAgenda();
				}
			});
		}
		else 
		{
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					ToastUtils.show(errorEnum.getDescription());
				}
			});
			
			Log.e(TAG, "onAgendaLoaded with error : "+errorEnum.toString());
		}
	}
	
	@Override
	public boolean onBackPressed()
	{
		getActivity().finish();
		return true;
	}
}
