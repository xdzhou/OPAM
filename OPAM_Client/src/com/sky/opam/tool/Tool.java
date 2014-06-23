package com.sky.opam.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
	
	public static Bitmap ViewToBitmap(View view){
		if(view==null) return null;
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
                     MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		//Define a bitmap with the same size as the view		
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(bitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) bgDrawable.draw(canvas);
        else canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        return bitmap;
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
