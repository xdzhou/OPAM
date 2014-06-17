package com.sky.opam.tool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
        private static final int VERSION = 3;
        private static final String BDNAME = "opamInfo.db";
        private SQLiteDatabase db;

        public DBHelper(Context context) {
                super(context, BDNAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	//Table USER
        	db.execSQL("create table if not exists USER ("
        			+ "login varchar(10) primary key,"
                    + "password varchar(64), "
                    + "name varchar(50), "
                    + "numWeekUpdated int);");
        	
        	//Table Room
        	db.execSQL("create table if not exists ROOM ("
        			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name varchar(64));");
        	//Table ClassType
        	db.execSQL("create table if not exists CLASSTYPE ("
        			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name varchar(50));");
        	//Table Config
        	db.execSQL("create table if not exists CONFIG ("
        			+ "login varchar(10) PRIMARY KEY,"
        			+ "startTime int,"
        			+ "endTime int,"
                    + "isDefaultUser BOOLEAN);");
        	//Table ClassInfo
        	db.execSQL("create table if not exists CLASSINFO ("
        			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        			+ "login varchar(10),"
                    + "name varchar(50),"
                    + "typeId int,"
                    + "weekOfYear int,"
                    + "dayOfWeek int,"
                    + "startTime varchar(10),"
                    + "endTime varchar(10),"
                    + "auteur varchar(50),"
                    + "teacher varchar(50),"
                    + "students text,"
                    + "groupe varchar(30),"
                    + "roomId int,"
                    + "bgColor varchar(10),"
                    + "eventId INTEGER,"
                    + "foreign key(typeId) references CLASSTYPE(id),"
                    + "foreign key(roomId) references ROOM(id),"
                    + "foreign key(login) references USER(login));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 3 && newVersion == 3) {
            	db.execSQL("DROP table if exists user;");
            	db.execSQL("DROP table if exists cours;");
            	db.execSQL("DROP table if exists syncevent;");
            }
            onCreate(db);         
        }

        public void close() {
                if (db != null) db.close();
        }

}