package com.sky.opam.fragment;

import java.util.List;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.loic.common.utils.AndroidUtils;
import com.loic.common.utils.NetWorkUtils;
import com.loic.common.utils.ToastUtils;
import com.sky.opam.OpamFragment;
import com.sky.opam.R;
import com.sky.opam.adapter.EtudiantListAdapter;
import com.sky.opam.model.Student;
import com.sky.opam.service.IntHttpService;
import com.sky.opam.service.IntHttpService.HttpServiceErrorEnum;
import com.sky.opam.service.IntHttpService.asyncSearchStudentByNameListener;

public class TrombiFragment extends OpamFragment implements asyncSearchStudentByNameListener
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
                AndroidUtils.closeSoftKeyboard(getActivity());
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
                    getHttpService().asyncSearchStudentByName(searchText, schoolParam[schoolSpinner.getSelectedItemPosition()], gradeParam[gradeSpinner.getSelectedItemPosition()], TrombiFragment.this);
                }
            }
        });
        ListView resultListView = (ListView) rootView.findViewById(R.id.search_resulte);
        listAdapter = new EtudiantListAdapter(getActivity());
        resultListView.setAdapter(listAdapter);
        
        return rootView;
    }

    @Override
    public void onStop() 
    {
        IntHttpService service = getHttpService();
        if(service != null)
        {
            service.cancelAsyncSearchStudentByNameRequest();
        }
        super.onStop();
    }

    @Override
    public void onAsyncSearchStudentByName(final HttpServiceErrorEnum errorEnum, final List<Student> results)
    {
        if(errorEnum == errorEnum.OkError && results != null && isAdded())
        {
            getActivity().runOnUiThread(new Runnable() 
            {
                @Override
                public void run() 
                {
                    Snackbar.make(getView(), getString(R.string.OA3002) + " : " + results.size(), Snackbar.LENGTH_SHORT).show();
                    if(listAdapter != null)
                    {
                        listAdapter.clear();
                        listAdapter.addAll(results);
                        listAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        else if(errorEnum != errorEnum.OkError)
        {
            showDialog("Trombi", "Error : "+errorEnum.getDescription(), null);
        }
    }
}
