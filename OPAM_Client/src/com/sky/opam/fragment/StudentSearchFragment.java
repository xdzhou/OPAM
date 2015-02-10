package com.sky.opam.fragment;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.loic.common.LibApplication;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.adapter.EtudiantListAdapter;
import com.sky.opam.model.Student;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;
import com.sky.opam.service.IntHttpService.asyncSearchEtudiantByNameReponse;

public class StudentSearchFragment extends OpamFragment
{
	private static final String TAG = StudentSearchFragment.class.getSimpleName();
	
	private EditText searchEditText;
	private Button searchButton;
	private EtudiantListAdapter adapter;
	private asyncSearchEtudiantByNameReponse searchEtudiantCallback;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView =  inflater.inflate(R.layout.etudiant_search_fragment, container, false);
		searchEditText = (EditText) rootView.findViewById(R.id.search_text);
		searchButton = (Button) rootView.findViewById(R.id.search_button);
		searchButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				String searchText = searchEditText.getText().toString();
				if(searchText.length() == 0)
				{
					ToastUtils.show("Input search name");
				}
				else if(getHttpService() != null)
				{
					//getHttpService().asyncSearchEtudiantByName(searchText, searchEtudiantCallback);
					getHttpService().asyncLogin("he_huilo", "pancakes", null);
				}
				else {
					ToastUtils.show("httpService is null");
				}
			}
		});
		ListView resultListView = (ListView) rootView.findViewById(R.id.search_resulte);
		adapter = new EtudiantListAdapter(getActivity());
		resultListView.setAdapter(adapter);
		
		initCallback();
		
		return rootView;
	}
	
	private void initCallback()
	{
		searchEtudiantCallback = new asyncSearchEtudiantByNameReponse() 
		{
			@Override
			public void onAsyncSearchEtudiantByNameReponse(HttpServiceErrorEnum errorEnum, final List<Student> results) 
			{
				if(errorEnum == errorEnum.OkError && results != null && getActivity() != null)
				{
					getActivity().runOnUiThread(new Runnable() 
					{
						@Override
						public void run() 
						{
							if(adapter != null)
							{
								adapter.clear();
								adapter.addAll(results);
								adapter.notifyDataSetChanged();
							}
						}
					});
					
				}
				else 
				{
					Log.e(TAG, "errorEnum : "+errorEnum.getDescription());
					ToastUtils.show(errorEnum.getDescription());
				}
			}
		};
	}
	
	private void removeCallback()
	{
		searchEtudiantCallback = null;
	}

	@Override
	protected void onHttpServiceReady() 
	{
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		removeCallback();
	}
}
