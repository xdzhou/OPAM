package com.sky.opam.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.sky.opam.R;
import com.sky.opam.model.VersionInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;

public class Tool 
{
	public static String getTrombiPhotoURL(String login, int size)
	{
		return "http://trombi.tem-tsp.eu/photo.php?uid="+login+"&h="+size+"&w="+size;
	}
	
	public static void showInfo(Context context, String title, String msg) 
	{
        new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
            .setPositiveButton(R.string.ok, null).show();
	}
	
	
	/**
     * 把一个view转换为Bitmap，可用于屏幕截图
     * 
     * @param view
     *          需要转换的view
     */
	public static Bitmap ViewToBitmap(View view)
	{
		if(view==null) 
			return null;
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
	
	/**
     * 得到当前的周数（一年中的第几周）
     * 
     */
	public static int getNumWeek() 
	{
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int xq = c.get(Calendar.DAY_OF_WEEK);
        if (xq == 1) 
        {
                c.set(Calendar.DATE, c.get(Calendar.DATE) - 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("w");
        return Integer.parseInt(sdf.format(c.getTime()));
	}
	
	/**
     * 得到日期（月/日）
     * 
     * @param numweek
     *          得到当前的周数
     * @param dayOfWeek
     * 			星期几
     */
	public static String getDateViaNumWeek(int numweek, int dayOfWeek) 
	{
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
	
	/**
     * 得到公元多少年
     * 
     */
	public static int getYear() 
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR);
	}
	
	/**
     * 星期几
     * 
     */
	public static int getDayOfWeek() 
	{
		Calendar c = Calendar.getInstance();
		int xq = c.get(Calendar.DAY_OF_WEEK);
		return xq;
	}
	
	/**
     * 把 dip 数值转换为 px（像素值）
     * 
     * @param context
     *          环境，一般为activity
     * @param dipValue    
     * 			dip 数值 
     */
	public static int dip2px(Context context, int dipValue) 
	{
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	
	/**
     * 得到手机屏幕的宽度
     * 
     * @param activity
     *          环境，为activity
     */
	public static int getScreenWidth(Activity activity)
	{
		WindowManager manager = activity.getWindowManager();
		Display display = manager.getDefaultDisplay();
		return display.getWidth();
	}
	
	/**
     * 得到手机屏幕的高度
     * 
     * @param activity
     *          环境，为activity
     */
	public static int getScreenHeight(Activity activity)
	{
		WindowManager manager = activity.getWindowManager();
		Display display = manager.getDefaultDisplay();
		return display.getHeight();
	}
	
	/**
     * 显示时间 hh:mm
     * 
     * @param hour
     *          小时值
     */
	public static String getTime(int hour)
	{
		return (hour < 10) ? ("0" + hour + ":00"): (hour + ":00");
	}
	
	/**
     * 得到当前应用的 版本名 Version Name
     * 
     * @param context
     *          环境，一般为activity
     */
	public static String getVersionName(Context context)
	{
		String version_name = null;
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version_name = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version_name;
	}
	
	/**
     * 得到当前应用的 版本号 Version Code
     * 
     * @param context
     *          环境，一般为activity
     */
	public static int getVersionCode(Context context)
	{
		int version_code = 0;
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version_code = pInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version_code;
	}
	
	/**
     * 得到当前的语言
     * 
     */
	public static String getLocalLanguage()
	{
		//return context.getResources().getConfiguration().locale.getDisplayLanguage();
		return Locale.getDefault().getLanguage();
	}
	
	public static AlertDialog.Builder showVersionInfo(Context context, VersionInfo versionInfo)
	{
		StringBuilder msg = new StringBuilder();
		msg.append(versionInfo.vName+":\n\n");
		int num = 1;
		for(String s: versionInfo.features)
		{
			msg.append(num+": ");
			msg.append(s+"\n\n");
			num++;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setMessage(msg.toString());
		builder.setTitle(R.string.new_version_feature);
		builder.setPositiveButton(R.string.ok, null);
		return builder;
	}
	
	public static AlertDialog.Builder showVersionInfoAndUpdate(final Context context, VersionInfo versionInfo)
	{	
		AlertDialog.Builder builder = showVersionInfo(context, versionInfo);
		builder.setNegativeButton(R.string.no, null);
		builder.setPositiveButton(R.string.update_app, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
				try {
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
				} catch (android.content.ActivityNotFoundException anfe) {
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
				}
			}		
		});
		return builder;
	}
	
	public static boolean isFirstUseApp(Context context)
	{
		Boolean isFirstIn = false;  
		SharedPreferences pref = context.getSharedPreferences("share", 0); 
		isFirstIn = pref.getBoolean("isFirstIn", true);
		return isFirstIn;
	}
}
