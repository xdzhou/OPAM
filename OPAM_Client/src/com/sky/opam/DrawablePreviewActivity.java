package com.sky.opam;

import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;

import android.R.drawable;
import android.graphics.drawable.Drawable;
import android.widget.*;

import java.util.*;

import android.app.*;
import android.content.res.Resources.NotFoundException;

public class DrawablePreviewActivity extends ListActivity
{
    private static final String TAG = "DrawablePreviewActivity";
    private List<Map<String, Object>> drinfo = new ArrayList<Map<String, Object>>();

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle("Preview of R.drawable.*");
        
        setup("com.sky.opam");
        setup("android");
        
        setListAdapter(new SimpleAdapter(this,
                        drinfo,
                        R.layout.listitem,
                        new String[] { "drimg", "drname" },
                        new int[] { R.id.drimg, R.id.drname }));

    }
    
    private void setup(String className){
    	Class RClass;
		try {
			RClass = Class.forName(className+".R");
			Class[] subclasses = RClass.getDeclaredClasses();
	        Class RDrawable = null;

	        for(Class subclass : subclasses) {
	            if((className+".R.drawable").equals(subclass.getCanonicalName())) {
	                RDrawable = subclass;
	                break;
	            }
	        }

	        Field[] drawables = RDrawable.getFields();
	        for(Field dr : drawables) {
	            Map<String, Object> map = new HashMap<String, Object>();
	            Drawable img = getResources().getDrawable(dr.getInt(null));

	            map.put("drimg", dr.getInt(null));
	            map.put("drname", dr.getName());

	            drinfo.add(map);
	        }
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
    }
}
