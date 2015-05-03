package com.sky.opam.tool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.loic.common.LibApplication;
import com.loic.common.sqliteTool.SqliteWorker;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.model.ClassUpdateInfo;
import com.sky.opam.model.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

public class DBworker extends SqliteWorker
{
	private static final String TAG = DBworker.class.getSimpleName();
	private static DBworker singleton;

    public static synchronized DBworker getInstance()
    {
    	if(singleton == null)
    	{
    		singleton = new DBworker(LibApplication.getAppContext());
    	}
    	
    	return singleton;
    }
    
    private DBworker(Context context) 
    {
    	super(new DBHelper(context));
    }
    
    private int[] getYearMonth(Date date)
    {
    	if(date != null)
    	{
    	    Calendar cal = Calendar.getInstance();
    	    cal.setTime(date);
    	    int year = cal.get(Calendar.YEAR);
    	    int month = cal.get(Calendar.MONTH);
    	    
    	    return new int[] {year, month};
    	}
    	return null;
    }
    
    /******************************************************
	 ******************** User Operation ******************
	 ******************************************************/
    public User getDefaultUser()
    {
    	Object object = retrieveAData(User.class, "isDefaultUser = 1");
    	if(object != null && object instanceof User)
    	{
    		return (User) object;
    	}
    	return null;
    }
    
    public void setDefaultUser(String login) 
    {
    	SQLiteDatabase db = helper.getWritableDatabase();
    	db.execSQL("update User set isDefaultUser = 0 where isDefaultUser = 1");
    	db.execSQL("update User set isDefaultUser = ? where login = ?",new Object[] { 1, login });
    	db.close();
    }
    
    public User getUser(String login)
    {
    	Object object = retrieveAData(User.class, "login = '"+login+"'");
    	if(object != null && object instanceof User)
    	{
    		return (User) object;
    	}
    	return null;
    }

    public void delUser(String login)
    {
    	if(login != null && !login.isEmpty())
    		deleteData(User.class, "login = '"+login+"'");
    }
    
    
    /******************************************************
	 ************** ClassUpdateInfo Operation *************
	 ******************************************************/
    public ClassUpdateInfo getUpdateInfo(String login, Date date)
    {
    	int[] values = getYearMonth(date);	
    	if(values != null)
    	{
    	    return getUpdateInfo(login, values[0], values[1]);
    	}
    	return null;
    }
    
    public ClassUpdateInfo getUpdateInfo(String login, int year, int month)
    {
    	if(login != null && !login.isEmpty() && year > 0 && month >= Calendar.JANUARY && month <= Calendar.DECEMBER)
    	{
    		Object object = retrieveAData(ClassUpdateInfo.class, "login = '"+login+"' AND year = "+year+" AND month = "+month);
        	if(object != null && object instanceof ClassUpdateInfo)
        		return (ClassUpdateInfo) object;
    	}
    	return null;
    }
    
    public void updateClassUpdateInfro(ClassUpdateInfo updateInfo)
    {
    	if(updateInfo != null && updateInfo.login != null)
    	{
    		updateData(updateInfo, "login = '"+updateInfo.login+"' AND year = "+updateInfo.year+" AND month = "+updateInfo.month);
    	}
    }
    
    /******************************************************
	 **************** ClassEvent Operation ****************
	 ******************************************************/
    public ClassEvent getClassEvent(String login, long numEve)
    {
    	Object object = retrieveAData(ClassEvent.class, "login = '"+login+"' AND NumEve = "+numEve);
    	if(object != null && object instanceof ClassEvent)
    	{
    		return (ClassEvent) object;
    	}
    	return null;
    }
    
    public List<ClassEvent> getClassEvents(String login, int year, int month)
    {
    	if(login != null && !login.isEmpty() && year > 0 && month >= Calendar.JANUARY && month <= Calendar.DECEMBER)
    	{
    		long startestTime, endestTime;
    		Calendar cal = Calendar.getInstance();
    		cal.set(Calendar.YEAR, year);
    		cal.set(Calendar.MONTH, month);
    		cal.set(Calendar.DAY_OF_MONTH, 1);
    		cal.set(Calendar.HOUR_OF_DAY, 1);
    		startestTime = cal.getTime().getTime();
    		
    		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    		cal.set(Calendar.HOUR_OF_DAY, 23);
    		endestTime = cal.getTime().getTime();
    		
    		List<Object> objects = retrieveDatas(ClassEvent.class, "login = '"+login+"' AND startTime > "+startestTime+" AND endTime < "+endestTime);
        	if(objects != null && !objects.isEmpty() && objects.get(0) instanceof ClassEvent)
        	{
        		List<ClassEvent> events = new ArrayList<ClassEvent>(objects.size());
        		for(Object o : objects)
        		{
        			events.add((ClassEvent)o);
        		}
        		return events;
        	}
    	}
    	return null;
    }
    
    public void deleteClassEvents (String login, Date date)
    {
    	int[] values = getYearMonth(date);	
    	if(values != null)
    	{
    	    deleteClassEvents(login, values[0], values[1]);
    	}
    }
    
    public void deleteClassEvents (String login, int year, int month)
    {
    	if(login != null && !login.isEmpty() && year > 0 && month >= Calendar.JANUARY && month <= Calendar.DECEMBER)
    	{
    		long startestTime, endestTime;
    		Calendar cal = Calendar.getInstance();
    		cal.set(Calendar.YEAR, year);
    		cal.set(Calendar.MONTH, month);
    		cal.set(Calendar.DAY_OF_MONTH, 1);
    		cal.set(Calendar.HOUR_OF_DAY, 1);
    		startestTime = cal.getTime().getTime();
    		
    		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    		cal.set(Calendar.HOUR_OF_DAY, 23);
    		endestTime = cal.getTime().getTime();
    		
    		deleteData(ClassEvent.class, "login = '"+login+"' AND startTime > "+startestTime+" AND endTime < "+endestTime);
    	}
    }
    
    
    
    
    
    public void setClassSynced(long classId, long eventId)
    {
    	SQLiteDatabase db = helper.getWritableDatabase();
    	db.execSQL("update CLASSINFO set eventId=? where id = ?",
                new Object[] { eventId, classId });
    	db.close();
    }
    
    //指示App配置 自动登录  自动提示升级信息
    public void setAutoLogin(Context context, boolean b)
    {
    	context.getSharedPreferences("share", 0).edit().putBoolean("autoLogin", b).commit();    	
    }
    
    public void setAutoUpdateNotify(Context context, boolean b)
    {
    	context.getSharedPreferences("share", 0).edit().putBoolean("autoUpdateNotify", b).commit(); 
    }
    
    public boolean getAutoLogin(Context context)
    {
    	SharedPreferences pref = context.getSharedPreferences("share", 0); 
		return pref.getBoolean("autoLogin", true);   	
    }
    
    public boolean getAutoUpdateNotify(Context context)
    {
    	SharedPreferences pref = context.getSharedPreferences("share", 0); 
		return pref.getBoolean("autoUpdateNotify", true);
    }
}