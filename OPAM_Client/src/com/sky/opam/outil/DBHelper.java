package com.sky.opam.outil;

import android.R.integer;
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
		// db.execSQL("create table config (id int,lastlogin varchar(10), showinfo int);");
		db.execSQL("create table if not exists user (login varchar(10) primary key,"
				+ "password varchar(64), "
				+ "name varchar(50), "
				+ "defaultuser int,"
				+ "thisweek int,"
				+ "sync int, "
				+ "weeksync int);");
		db.execSQL("create table if not exists cours (login varchar(10),"
				+ "name varchar(50),"
				+ "type varchar(20),"
				+ "position varchar(10),"
				+ "debut varchar(10),"
				+ "fin varchar(10),"
				+ "auteur varchar(50),"
				+ "formateur varchar(50),"
				+ "apprenant text,"
				+ "groupe varchar(30),"
				+ "salle varchar(50),foreign key(login) references user(login));");
		db.execSQL("create table if not exists syncevent (login varchar(10), "
				+ "numweek int,"
				+ "eventid long);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1) {
			db.execSQL("ALTER TABLE user ADD COLUMN sync int;"); // 0:no 1:yes
			db.execSQL("ALTER TABLE user ADD COLUMN weeksync int;"); // 0:no 1:yes
			db.execSQL("update user set sync = 1");
		}
		if(oldVersion ==2){
			db.execSQL("ALTER TABLE syncevent ADD COLUMN numweek int;"); // 0:no 1:yes
		}
		onCreate(db);
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
		if (db != null)
			db.close();
	}

}
