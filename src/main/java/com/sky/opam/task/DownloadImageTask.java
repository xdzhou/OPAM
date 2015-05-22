package com.sky.opam.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
	private Context context;
	private String login;

    public DownloadImageTask(Context context, String login) 
    {
        this.context = context;
        this.login = login;
    }
    
    @Override
    protected Bitmap doInBackground(String... urls) 
    {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try 
        {
        	String imgPath = context.getFilesDir() + "/" + login +".jpg";
    		File document = new File(imgPath);
    		if(!document.exists())
    		{
    			InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                FileOutputStream fos = new FileOutputStream(document);  
                mIcon11.compress(Bitmap.CompressFormat.JPEG, 100, fos);
    		}
            
        } 
        catch (Exception e) 
        {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }
}
