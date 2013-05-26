package com.sky.opam.outil;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;
	private static final String BDNAME = "opamInfo.db";
	private SQLiteDatabase db;

	public DBHelper(Context context) {
		super(context, BDNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//db.execSQL("create table config (id int,lastlogin varchar(10), showinfo int);");
		db.execSQL("create table user (login varchar(10) primary key," +
										"password varchar(64), " +
										"name varchar(50), " +
										"defaultuser int," +
										"thisweek int);");
		db.execSQL("create table cours (login varchar(10)," +
										"name varchar(50)," +
										"type varchar(20)," +
										"position varchar(10)," +
										"debut varchar(10)," +
										"fin varchar(10)," +
										"auteur varchar(50)," +
										"formateur varchar(50)," +
										"apprenant text," +
										"groupe varchar(30)," +
										"salle varchar(50),foreign key(login) references user(login));");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		//db.execSQL("DROP TABLE IF EXISTS diary"); 
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
        if (db != null)  
            db.close();  
    }  

}
