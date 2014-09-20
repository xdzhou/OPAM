package com.sky.opam.core;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.sky.opam.model.User;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends OrmLiteSqliteOpenHelper {
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "opamInfo.db";
    private SQLiteDatabase db;

    public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    	try {
			Log.i(DBHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, User.class);
		} catch (SQLException e) {
			Log.e(DBHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    	try {
			Log.i(DBHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, User.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DBHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}        
    }
    
    @Override
    public void close() {
    	super.close();
    }

}