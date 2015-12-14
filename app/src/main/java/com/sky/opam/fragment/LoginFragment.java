package com.sky.opam.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.loic.common.Chiffrement;
import com.loic.common.utils.AndroidUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.OpamMFM;
import com.sky.opam.R;
import com.sky.opam.model.User;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.tool.DBworker;
import com.sky.opam.tool.SharePreferenceUtils;

import rx.Subscriber;

public class LoginFragment extends OpamFragment
{
    private static final String TAG = LoginFragment.class.getSimpleName();
    
    private AutoCompleteTextView loginTextView;
    private EditText passwordEditText;
    private Switch rememberMeSwitch;
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
        Button enterButton = (Button) rootView.findViewById(R.id.btnVAD);

        rememberMeSwitch = (Switch) rootView.findViewById(R.id.switch_remember_me);
        if(SharePreferenceUtils.isRememberMe())
        {
            rememberMeSwitch.setChecked(true);
            User defaultUser = worker.getDefaultUser();
            if(defaultUser != null)
            {
                loginTextView.setText(defaultUser.login);
                passwordEditText.setText(Chiffrement.decrypt(defaultUser.password, IntHttpService.ENCRPT_KEY));
            }
        }
        else
        {
            rememberMeSwitch.setChecked(false);
        }

        enterButton.getBackground().setAlpha(150);
        enterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AndroidUtils.closeSoftKeyboard(getActivity());
                String login = loginTextView.getText().toString();
                String password = passwordEditText.getText().toString();
                if (TextUtils.isEmpty(login))
                {
                    loginTextView.setError(getString(R.string.OA1004));
                }
                else if (TextUtils.isEmpty(password))
                {
                    passwordEditText.setError(getString(R.string.OA1005));
                }
                else
                {
                    User currentUser = worker.getUser(login);
                    if (currentUser != null)
                    {
                        if (currentUser.password.equals(Chiffrement.encrypt(password, IntHttpService.ENCRPT_KEY)))
                        {
                            showAgenda();
                        }
                        else
                        {
                            passwordResetAlert();
                        }
                    }
                    else
                    {
                        requestLogin(login, password);
                    }
                }
            }
        });
        
        getGcActivity().setActionBarVisible(false);
        
        return rootView;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        SharePreferenceUtils.setRememberMeState(rememberMeSwitch.isChecked());
    }

    @Override
    public void onDestroy() 
    {
        super.onDestroy();
        getGcActivity().setActionBarVisible(true);
    }

    private void requestLogin(final String login, String password)
    {
        final IntHttpService httpService = getHttpService();
        if(httpService == null)
        {
            Log.e(TAG, "requestLogin : IntHttpService is null ...");
            return;
        }
        httpService.requestLogin(login, password).subscribe(new Subscriber<User>()
        {
            @Override
            public void onStart()
            {
                showProgressDialog();
            }

            @Override
            public void onCompleted()
            {
                closeProgressDialog();
            }

            @Override
            public void onError(Throwable e)
            {
                closeProgressDialog();
                Log.e(TAG, "requestLogin error : "+e);
            }

            @Override
            public void onNext(User user)
            {
                worker.insertData(user);
                worker.setDefaultUser(login);
                if (isAdded())
                {
                    showAgenda();
                }
            }
        });
    }

    private void showAgenda()
    {
        SharePreferenceUtils.setLoginState(true);
        OpamMFM mfm = getOpenMFM();
        if(mfm != null && mfm.isAdded())
        {
            Bundle data = new Bundle();
            data.putString(AgendaViewFragment.BUNDLE_LOGIN_KEY, loginTextView.getText().toString());
            mfm.showGcFragment(AgendaViewFragment.class, true, data);
        }
    }
    
    private void passwordResetAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Password Error")
        .setMessage("Password incorrect. If you have modified your password online (E-Campus) these days, Please click retry!")
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
        .setIcon(android.R.drawable.ic_dialog_alert);
        showDialog(builder);
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
        {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onHttpServiceReady() 
    {
        Log.d(TAG, "onCasServiceReady...");
    }
}
