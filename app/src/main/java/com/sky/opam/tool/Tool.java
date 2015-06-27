package com.sky.opam.tool;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.loic.common.LibApplication;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;

public class Tool 
{
    public static String getTrombiPhotoURL(String login, int size)
    {
        return "http://trombi.tem-tsp.eu/photo.php?uid="+login+"&h="+size+"&w="+size;
    }
    
    public static int[] getPreviousMonth(int year, int month)
    {
        int[] retVal = new int[] {year, month};
        if(month == Calendar.JANUARY)
        {
            retVal[0] = year - 1;
            retVal[1] = Calendar.DECEMBER;
        }
        else 
        {
            retVal[1] = month - 1;
        }
        return retVal;
    }
    
    public static int[] getNextMonth(int year, int month)
    {
        int[] retVal = new int[] {year, month};
        if(month == Calendar.DECEMBER)
        {
            retVal[0] = year + 1;
            retVal[1] = Calendar.JANUARY;
        }
        else 
        {
            retVal[1] = month + 1;
        }
        return retVal;
    }
    
    public static int[] getCurrentYearMonth()
    {
        int[] retVal = new int[2];
        Calendar calendar = Calendar.getInstance();
        retVal[0] = calendar.get(Calendar.YEAR);
        retVal[1] = calendar.get(Calendar.MONTH);
        return retVal;
    }
    
    public static int[] getYearMonthForDate(Date date)
    {
        int[] retVal = new int[2];
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        retVal[0] = calendar.get(Calendar.YEAR);
        retVal[1] = calendar.get(Calendar.MONTH);
        return retVal;
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
     * 把 dip 数值转换为 px（像素值）
     * 
     * @param context
     *          环境，一般为activity
     * @param dipValue    
     *             dip 数值 
     */
    public static int dip2px(Context context, int dipValue) 
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
    

    
    /**
     * 得到当前应用的 版本名 Version Name
     *
     */
    public static String getVersionName()
    {
        String version_name = null;
        try {
            Context context = LibApplication.getAppContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version_name = pInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version_name;
    }
    
    public static int getVersionCode()
    {
        int version_code = 0;
        try {
            Context context = LibApplication.getAppContext();
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
    
    
    public static boolean isFirstUseApp(Context context)
    {
        Boolean isFirstIn = false;  
        SharedPreferences pref = context.getSharedPreferences("share", 0); 
        isFirstIn = pref.getBoolean("isFirstIn", true);
        return isFirstIn;
    }
}
