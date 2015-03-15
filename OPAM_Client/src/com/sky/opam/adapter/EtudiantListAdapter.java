package com.sky.opam.adapter;

import com.loic.common.LibApplication;
import com.loic.common.manager.LoadImgManager;
import com.loic.common.manager.LoadImgManager.onDownloadImgReadyListener;
import com.sky.opam.R;
import com.sky.opam.model.Student;
import com.sky.opam.tool.Tool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EtudiantListAdapter extends ArrayAdapter<Student> implements onDownloadImgReadyListener
{
	private static final String TAG = EtudiantListAdapter.class.getSimpleName();
	
	private LoadImgManager loadImgManager;
	private int expectSize;
	
	public EtudiantListAdapter(Context context) 
	{
		this(context, 0);
	}

	public EtudiantListAdapter(Context context, int textViewResourceId) 
	{
		super(context, textViewResourceId);
		init();
	}
	
	private void init()
	{
		loadImgManager = LoadImgManager.getInstance();
		expectSize = LibApplication.getAppContext().getResources().getDimensionPixelSize(R.dimen.etudiant_search_list_item_height);
		loadImgManager.registerListener(this);
	}
	
	private static class ViewHolder
	{
		ImageView profil;
		TextView nameTextView;
		TextView schoolTextView;
		TextView gradeTextView;
		TextView emailTextView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder viewHolder = null;
		Student etudiant = getItem(position);
		if(convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.etudiant_list_item_view, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.profil = (ImageView) convertView.findViewById(R.id.etudiant_profil);
			viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.etudiant_name);
			viewHolder.schoolTextView = (TextView) convertView.findViewById(R.id.etudiant_school);
			viewHolder.gradeTextView = (TextView) convertView.findViewById(R.id.etudiant_grade);
			viewHolder.emailTextView = (TextView) convertView.findViewById(R.id.etudiant_email);
			convertView.setTag(viewHolder);
		}
		else 
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.nameTextView.setText(etudiant.name);
		viewHolder.schoolTextView.setText(etudiant.school.getAbbreviation());
		viewHolder.gradeTextView.setText(etudiant.grade);
		if(etudiant.email != null)
		{
			viewHolder.emailTextView.setPaintFlags(viewHolder.emailTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			viewHolder.emailTextView.setText(etudiant.email);
			viewHolder.emailTextView.setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					TextView tView = (TextView) v;
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.putExtra(Intent.EXTRA_EMAIL, new String[]{tView.getText().toString()});
					intent.setType("message/rfc822");
					
					EtudiantListAdapter.this.getContext().startActivity(Intent.createChooser(intent, "Send Email"));
				}
			});
		}
		viewHolder.profil.setImageBitmap(null);
		
		if(etudiant.login != null)
		{
			String imageUrl = Tool.getTrombiPhotoURL(etudiant.login, 80);
			Bitmap bitmap = loadImgManager.getBitmapByUrl(imageUrl, expectSize, expectSize);
			if(bitmap != null)
				viewHolder.profil.setImageBitmap(bitmap);
		}
		
		return convertView;
	}

	@Override
	public boolean onDownloadImgReady(String url, Bitmap bitmap) 
	{
		if(bitmap != null)
		{
			notifyDataSetChanged();
		}
		return true;
	}
}
