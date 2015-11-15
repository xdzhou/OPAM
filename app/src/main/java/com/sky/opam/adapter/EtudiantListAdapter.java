package com.sky.opam.adapter;

import com.loic.common.LibApplication;
import com.sky.opam.R;
import com.sky.opam.model.Student;
import com.sky.opam.tool.Tool;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class EtudiantListAdapter extends RecyclerView.Adapter<EtudiantListAdapter.ViewHolder>
{
    private static final String TAG = EtudiantListAdapter.class.getSimpleName();

    private List<Student> mData = Collections.emptyList();
    
    public EtudiantListAdapter()
    {
    }

    public void updateData(List<Student> list)
    {
        mData = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType)
    {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.etudiant_list_item_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(convertView);
        viewHolder.profile = (ImageView) convertView.findViewById(R.id.etudiant_profil);
        viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.etudiant_name);
        viewHolder.schoolTextView = (TextView) convertView.findViewById(R.id.etudiant_school);
        viewHolder.gradeTextView = (TextView) convertView.findViewById(R.id.etudiant_grade);
        viewHolder.emailTextView = (TextView) convertView.findViewById(R.id.etudiant_email);
        viewHolder.emailTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TextView tView = (TextView) v;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{tView.getText().toString()});
                intent.setType("message/rfc822");

                parent.getContext().startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position)
    {
        Student stu = mData.get(position);

        viewHolder.nameTextView.setText(stu.name);
        viewHolder.schoolTextView.setText(stu.school.getAbbreviation());
        viewHolder.gradeTextView.setText(stu.grade);
        if(! TextUtils.isEmpty(stu.email))
        {
            viewHolder.emailTextView.setPaintFlags(viewHolder.emailTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            viewHolder.emailTextView.setText(stu.email);
        }
        else
        {
            viewHolder.emailTextView.setText("");
        }
        viewHolder.profile.setImageBitmap(null);

        if(stu.login != null)
        {
            String imageUrl = Tool.getTrombiPhotoURL(stu.login, 80);
            Picasso.with(LibApplication.getContext()).load(imageUrl).into(viewHolder.profile);
        }
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView profile;
        TextView nameTextView;
        TextView schoolTextView;
        TextView gradeTextView;
        TextView emailTextView;

        public ViewHolder(View itemView)
        {
            super(itemView);
        }
    }
}
