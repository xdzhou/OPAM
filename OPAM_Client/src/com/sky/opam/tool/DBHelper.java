package com.sky.opam.tool;

import com.loic.common.sqliteTool.SqliteHelper;
import com.loic.common.sqliteTool.SqliteManager;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.model.ClassUpdateInfo;
import com.sky.opam.model.User;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SqliteHelper 
{
    private static final int VERSION = 5;
    private static final String BDNAME = "opamInfo.db";

    public DBHelper(Context context)
    {
    	super(context, BDNAME, null, VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) 
    {
    	db.execSQL("DROP table if exists ClassEvent;");
    	String sql = SqliteManager.generateTableSql(ClassEvent.class);
    	System.out.println(sql);
    	db.execSQL(sql);
    	
    	db.execSQL("DROP table if exists ClassUpdateInfo;");
    	sql = SqliteManager.generateTableSql(ClassUpdateInfo.class);
    	System.out.println(sql);
    	db.execSQL(sql);
    	
    	db.execSQL("DROP table if exists User;");
    	sql = SqliteManager.generateTableSql(User.class);
    	System.out.println(sql);
    	db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {    	
    	Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
    	while(c.moveToNext())
    	{
    	   String s = c.getString(0);
    	   if(s.equals("android_metadata"))
    	   {
    	     continue;
    	   }
    	   else
    	   {
    		   db.execSQL("DROP table if exists "+s+";");
    	   }
    	 }
        onCreate(db);         
    }

	@Override
	protected String getModelPackage() 
	{
		return "com.sky.opam.model";
	}

}