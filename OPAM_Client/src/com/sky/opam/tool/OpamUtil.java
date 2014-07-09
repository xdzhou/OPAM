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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Looper;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.Toast;

public class OpamUtil {
	
	public static AlertDialog.Builder showVersionInfo(Context context, VersionInfo versionInfo){
		StringBuilder msg = new StringBuilder();
		msg.append(versionInfo.vName+":\n\n");
		int num = 1;
		for(String s: versionInfo.features){
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
	
	public static AlertDialog.Builder showVersionInfoAndUpdate(final Context context, VersionInfo versionInfo){	
		AlertDialog.Builder builder = showVersionInfo(context, versionInfo);
		builder.setNegativeButton(R.string.no, null);
		builder.setPositiveButton(R.string.update_app, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
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
	
	public static boolean isFirstUseApp(Context context){
		Boolean isFirstIn = false;  
		SharedPreferences pref = context.getSharedPreferences("share", 0); 
		isFirstIn = pref.getBoolean("isFirstIn", true);
		return isFirstIn;
	}
	
	/**
     * 显示时间 hh:mm
     * 
     * @param hour
     *          小时值
     */
	public static String getTime(int hour){
		return (hour < 10) ? ("0" + hour + ":00"): (hour + ":00");
	}
}
