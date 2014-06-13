package com.sky.opam.tool;

import com.sky.opam.R.integer;

import android.content.Context;
import android.database.Cursor;
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
        	//Table Color
        	db.execSQL("create table if not exists CLASSCOLOR ("
        			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "color int);");
        	//Table Config
        	db.execSQL("create table if not exists CONFIG ("
        			+ "login varchar(10) PRIMARY KEY,"
        			+ "startTime varchar(10),"
        			+ "endTime varchar(10),"
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
                    + "colorId int,"
                    + "isSync BOOLEAN,"
                    + "foreign key(typeId) references CLASSTYPE(id),"
                    + "foreign key(roomId) references ROOM(id),"
                    + "foreign key(colorId) references COLOR(id),"
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
            // set a default color
            db.execSQL("insert into CLASSCOLOR values (?,?)",new Object[] { 0, 0xff888888});
        }

        public void addConfigTable() {

        }

        public Cursor queryUser() {
                SQLiteDatabase db = getWritableDatabase();
                Cursor c = db.query("user", null, null, null, null, null, null);
                return c;
        }

        public Cursor queryCours() {
                SQLiteDatabase db = getWritableDatabase();
                Cursor c = db.query("cours", null, null, null, null, null, null);
                return c;
        }

        public void delUser(int id) {
                if (db == null)
                        db = getWritableDatabase();
                db.delete("user", "login=?", new String[] { String.valueOf(id) });
        }

        public void delCours(int id) {
                if (db == null)
                        db = getWritableDatabase();
                db.delete("cours", "id=?", new String[] { String.valueOf(id) });
        }

        public void close() {
                if (db != null) db.close();
        }

}