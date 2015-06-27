package com.sky.opam.tool;

import com.loic.common.sqliteTool.SqliteHelper;
import com.loic.common.sqliteTool.SqliteManager;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.model.ClassUpdateInfo;
import com.sky.opam.model.User;

import android.content.Context;
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
        //System.out.println(sql);
        db.execSQL(sql);
        
        db.execSQL("DROP table if exists ClassUpdateInfo;");
        sql = SqliteManager.generateTableSql(ClassUpdateInfo.class);
        //System.out.println(sql);
        db.execSQL(sql);
        
        db.execSQL("DROP table if exists User;");
        sql = SqliteManager.generateTableSql(User.class);
        //System.out.println(sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {   
        //old version
        db.execSQL("DROP table if exists user;");
        db.execSQL("DROP table if exists cours;");
        db.execSQL("DROP table if exists syncevent;");
        
        //newer version
        db.execSQL("DROP table if exists USER;");
        db.execSQL("DROP table if exists CLASSINFO;");
        db.execSQL("DROP table if exists CLASSTYPE;");
        db.execSQL("DROP table if exists CONFIG;");
        db.execSQL("DROP table if exists ROOM;");
        
        onCreate(db);         
    }

    @Override
    protected String getModelPackage() 
    {
        return "com.sky.opam.model";
    }

}