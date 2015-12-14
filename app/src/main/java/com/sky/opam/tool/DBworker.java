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
import com.sky.opam.service.IntHttpService;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.functions.Action1;

public class DBworker extends SqliteWorker
{
    private static final String TAG = DBworker.class.getSimpleName();
    private static DBworker singleton;

    public static synchronized DBworker getInstance()
    {
        if(singleton == null)
        {
            synchronized (DBworker.class)
            {
                if(singleton == null)
                {
                    singleton = new DBworker(LibApplication.getContext());
                }
            }
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
        Object object = retrieveAData(User.class, "login = '" + login + "'");
        if(object != null && object instanceof User)
        {
            return (User) object;
        }
        return null;
    }

    public void delUser(String login)
    {
        if(login != null && !login.isEmpty())
        {
            deleteData(User.class, "login = '" + login + "'");
        }
    }
    
    
    /******************************************************
     ************** ClassUpdateInfo Operation *************
     ******************************************************/
    public Observable<List<ClassEvent>> inserClassEvents(final @NonNull String login, final int year, final int month, @NonNull Observable<List<ClassEvent>> rxClassEvents)
    {
        return rxClassEvents.doOnNext(new Action1<List<ClassEvent>>()
        {
            @Override
            public void call(List<ClassEvent> classEvents)
            {
                ClassUpdateInfo updateInfo = getUpdateInfo(login, year, month);

                boolean needSave = false;
                if(updateInfo == null)
                {
                    updateInfo = new ClassUpdateInfo(login);
                    updateInfo.year = year;
                    updateInfo.month = month;
                    needSave = true;
                }

                deleteClassEvents(login, year, month);
                int totalTime = 0; //in second
                for(ClassEvent classEvent : classEvents)
                {
                    insertData(classEvent);
                    totalTime += (classEvent.endTime.getTime() - classEvent.startTime.getTime()) / 1000;
                }
                updateInfo.lastSuccessUpdateDate = new Date();
                updateInfo.classNumber = classEvents.size();
                updateInfo.totalTime = totalTime;
                updateInfo.errorEnum = IntHttpService.HttpServiceErrorEnum.OkError;

                if(needSave)
                {
                    insertData(updateInfo);
                    Log.d(TAG, "insert updateInfo : "+updateInfo);
                }
                else
                {
                    updateClassUpdateInfro(updateInfo);
                    Log.d(TAG, "update updateInfo : "+updateInfo);
                }
            }
        });
    }

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
            {
                return (ClassUpdateInfo) object;
            }
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

}