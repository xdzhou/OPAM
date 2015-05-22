package com.sky.opam.fragment;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.loic.common.manager.LoadImgManager;
import com.loic.common.utils.NetWorkUtils;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.adapter.EtudiantListAdapter;
import com.sky.opam.model.Student;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;
import com.sky.opam.service.IntHttpService.asyncSearchEtudiantByNameReponse;

public class TrombiFragment extends OpamFragment implements asyncSearchEtudiantByNameReponse
{
	private static final String TAG = TrombiFragment.class.getSimpleName();
	
	private EditText searchEditText;
	private Button searchButton;
	
	private String[] schoolParam = {null, "TINT", "INTM"};
	private String[] gradeParam = {null, "bac", "1", "2", "3", "MS", "MSc", "MBA"};
	
	private EtudiantListAdapter listAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		getActivity().setTitle(getString(R.string.OA0001));
		View rootView =  inflater.inflate(R.layout.etudiant_search_fragment, container, false);
		final Spinner schoolSpinner = (Spinner) rootView.findViewById(R.id.spinner_school);
		ArrayAdapter<CharSequence> schoolAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.school_array, android.R.layout.simple_spinner_item);
		schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		schoolSpinner.setAdapter(schoolAdapter);
		
		final Spinner gradeSpinner = (Spinner) rootView.findViewById(R.id.spinner_grade);
		ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.grade_array, android.R.layout.simple_spinner_item);
		gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gradeSpinner.setAdapter(gradeAdapter);
		
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
				else if (! NetWorkUtils.isNetworkAvailable()) 
				{
					ToastUtils.show(getString(R.string.OA0004));
				}
				else if(getHttpService() != null)
				{
					getHttpService().asyncSearchEtudiantByName(searchText, schoolParam[schoolSpinner.getSelectedItemPosition()], gradeParam[gradeSpinner.getSelectedItemPosition()], TrombiFragment.this);
				}
				else 
				{
					ToastUtils.show("httpService is null, retry later");
				}
			}
		});
		ListView resultListView = (ListView) rootView.findViewById(R.id.search_resulte);
		listAdapter = new EtudiantListAdapter(getActivity());
		resultListView.setAdapter(listAdapter);
		
		return rootView;
	}

	@Override
	protected void onHttpServiceReady() 
	{
	}

	@Override
	public void onStop() 
	{
		super.onStop();
		LoadImgManager.getInstance().removeListener(listAdapter);
	}

	@Override
	public void onAsyncSearchEtudiantByNameReponse(final HttpServiceErrorEnum errorEnum, final List<Student> results) 
	{
		if(errorEnum == errorEnum.OkError && results != null && getActivity() != null)
		{
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					ToastUtils.show(getString(R.string.OA3002)+" : " + results.size());
					if(listAdapter != null)
					{
						listAdapter.clear();
						listAdapter.addAll(results);
						listAdapter.notifyDataSetChanged();
					}
				}
			});
		}
		else if(errorEnum != errorEnum.OkError && getActivity() != null)
		{
			getActivity().runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					createDialogBuilderWithCancel("Trombi", "Error : "+errorEnum.getDescription()).show();
				}
			});
		}
	}
}
