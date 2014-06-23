package com.sky.opam.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.Toast;

public class Tool {
	
	public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
	}
	
	public static void showInfo(Context context, String title, String msg) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
	}

	public static void showInfo(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static Bitmap convertViewToBitmap(View view, Activity activity){
		if(view==null) return null;
		Bitmap bitmap;
		int viewWidth;
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int numBitMap = view.getMeasuredHeight() / getScreenHeight(activity) +1;
		if(numBitMap == 1){
			view.layout(0, 0, view.getMeasuredWidth(),view.getMeasuredHeight());
			view.buildDrawingCache();
			Bitmap b1 = view.getDrawingCache();
			Bitmap newmap = Bitmap.createBitmap(b1.getWidth(), b1.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(newmap);
			canvas.drawColor(Color.WHITE);
		    canvas.drawBitmap(b1, 0, 0, null);
		    canvas.save(Canvas.ALL_SAVE_FLAG);  
		    canvas.restore();
		    bitmap = newmap;
		}else {
			view.layout(0, 0, view.getMeasuredWidth(),view.getMeasuredHeight()/2);
			view.buildDrawingCache();
			Bitmap b1 = Bitmap.createBitmap(view.getDrawingCache());
			view.destroyDrawingCache();
			view.setDrawingCacheEnabled(true);
			view.layout(0,view.getMeasuredHeight()/2, view.getMeasuredWidth(), view.getMeasuredHeight());
			view.buildDrawingCache();
			Bitmap b2 = view.getDrawingCache();
			bitmap = combineBitmap(b1, b2);
		}
		view.layout(0, 0, view.getMeasuredWidth(),view.getMeasuredHeight());

        return bitmap;
	} 
	
	public static Bitmap combineBitmap(Bitmap b1, Bitmap b2) {  
	    if (b1 == null) {  
	        return null;  
	    }  
	    int bgWidth = b1.getWidth();  
	    int bgHeight = b1.getHeight();
	    int fgHeight = b2.getHeight();  
	    Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight + fgHeight, Config.ARGB_8888);  
	    Canvas canvas = new Canvas(newmap);  
	    canvas.drawColor(Color.WHITE);
	    canvas.drawBitmap(b1, 0, 0, null);  
	    canvas.drawBitmap(b2, 0, bgHeight, null);  
	    canvas.save(Canvas.ALL_SAVE_FLAG);  
	    canvas.restore();  
	    return newmap;  
	} 
	
	public static int getNumWeek() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int xq = c.get(Calendar.DAY_OF_WEEK);
        if (xq == 1) {
                c.set(Calendar.DATE, c.get(Calendar.DATE) - 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("w");
        return Integer.parseInt(sdf.format(c.getTime()));
	}
	
	public static String getDateViaNumWeek(int numweek, int dayOfWeek) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.WEEK_OF_YEAR, numweek);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.WEEK_OF_YEAR, numweek);
		int num = cal.get(Calendar.MONTH) + 1;

		String date = (num < 10) ? "0" + num : "" + num;
		date += "/";
		num = cal.get(Calendar.DAY_OF_MONTH);
		date += (num < 10) ? "0" + num : "" + num;
		return date;
	}
	
	public static int getYear() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR);
	}
	
	public static int getDayOfWeek() {
		Calendar c = Calendar.getInstance();
		int xq = c.get(Calendar.DAY_OF_WEEK);
		if (xq == 1) {
			return 7;
		} else {
			return xq - 1;
		}
	}
	
	public static int dip2px(Context context, int dipValue) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	
	public static int getScreenWidth(Activity activity){
		WindowManager manager = activity.getWindowManager();
		Display display = manager.getDefaultDisplay();
		return display.getWidth();
	}
	
	public static int getScreenHeight(Activity activity){
		WindowManager manager = activity.getWindowManager();
		Display display = manager.getDefaultDisplay();
		return display.getHeight();
	}
	
	public static String getTime(int hour){
		return (hour < 10) ? ("0" + hour + ":00"): (hour + ":00");
	}
	
}
