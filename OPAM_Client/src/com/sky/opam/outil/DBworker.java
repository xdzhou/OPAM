package com.sky.opam.outil;

import java.util.ArrayList;
import java.util.List;

import com.sky.opam.entity.Cours;
import com.sky.opam.entity.User;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBworker {
	private DBHelper helper;
	private SQLiteDatabase db;

	public DBworker(Context context) {
		helper = new DBHelper(context);
	}

	public void addUser(User user) {
		db = helper.getWritableDatabase();
		db.execSQL("insert into user (login,password,name,sync) values (?,?,?,?)",new Object[] { user.getLogin(), user.getPasswoed(),user.getUsename(), 1 });
		db.close();
	}

	public User defaultUser() {
		db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select login,password,thisweek,name from user where defaultuser=1",null);
		User user = new User();
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			user.setLogin(cursor.getString(0));
			user.setPasswoed(cursor.getString(1));
			user.setThisweek(cursor.getInt(2));
			user.setUsename(cursor.getString(3));
		}
		cursor.close();
		db.close();
		return user;
	}

	public void addCours(Cours c) {
		db = helper.getWritableDatabase();
		db.execSQL("insert into cours values (?,?,?,?,?,?,?,?,?,?,?,?)",
				new Object[] { c.login, c.name, c.type, c.position, c.debut,
						c.fin, c.auteur, c.formateur, c.apprenants, c.groupe,
						c.salle,c.eventid });
		db.close();
	}

	public void updateUser(User user) {
		db = helper.getWritableDatabase();
		db.execSQL("update user set password = ?,defaultuser =?, thisweek=? where login = ?",
				new Object[] { user.getPasswoed(), user.getDefaultUser(),
						user.getThisweek(), user.getLogin() });
		db.close();
	}

	public void delAllCours() {
		db = helper.getWritableDatabase();
		db.execSQL("delete from cours ;");
		db.close();
	}
	
	//del all coures and it's calender (this week and next week)
	public void delAllCours(String login, Context context) {
		int thisweek = 0;
		db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select thisweek from user where login='" + login+ "';", null);	
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			thisweek = cursor.getInt(cursor.getColumnIndex("thisweek"));
		}
		cursor.close();
		db.close();
		GoogleCalendarAPI calendarAPI = new GoogleCalendarAPI(context);
		for(Cours c : findClass(login)){
			int numWeekofClass = Integer.parseInt(c.position.split("_")[0]);
			if(numWeekofClass == thisweek || numWeekofClass == thisweek+1){
				if(c.eventid!=0) calendarAPI.delEvent(c.eventid);
			}
		}		
		db = helper.getWritableDatabase();
		db.execSQL("delete from cours where login ='" + login + "';");
		db.close();
	}

	public void delAllUsers() {
		db = helper.getWritableDatabase();
		db.execSQL("delete from user;");
		db.close();
	}

	public User findUser(String id) {
		db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user where login='" + id
				+ "';", null);
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			String login = cursor.getString(0);
			String password = cursor.getString(1);
			String username = cursor.getString(2);
			int defaultuser = cursor.getInt(3);
			int thisweek = cursor.getInt(4);
			User user = new User(login, password, username, defaultuser,
					thisweek);
			cursor.close();
			db.close();
			return user;
		} else {
			cursor.close();
			db.close();
			return null;
		}
	}

	public List<Cours> findClass(String login, String flag) {
		db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
						"select name,type,debut,fin,groupe,salle,formateur,auteur,apprenant,position,eventid from cours where login='"
						+ login + "' AND position='" + flag + "';",null);
		List<Cours> cours = new ArrayList<Cours>();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Cours c = new Cours();
			c.name = cursor.getString(0);
			c.type = cursor.getString(1);
			c.debut = cursor.getString(2);
			c.fin = cursor.getString(3);
			c.groupe = cursor.getString(4);
			c.salle = cursor.getString(5);
			c.formateur = cursor.getString(6);
			c.auteur = cursor.getString(7);
			c.apprenants = cursor.getString(8);
			c.position = cursor.getString(9);
			c.eventid = cursor.getLong(cursor.getColumnIndex("eventid"));
			cours.add(c);
		}
		cursor.close();
		db.close();
		return cours;
	}

	public List<Cours> findClass(String login, int numweek) {
		List<Cours> cours = new ArrayList<Cours>();
		cours.addAll(findClass(login, numweek + "_1"));
		cours.addAll(findClass(login, numweek + "_2"));
		cours.addAll(findClass(login, numweek + "_3"));
		cours.addAll(findClass(login, numweek + "_4"));
		cours.addAll(findClass(login, numweek + "_5"));
		return cours;
	}

	public List<Cours> findClass(String login) {
		db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
						"select name,type,debut,fin,groupe,salle,formateur,auteur,apprenant,position,eventid from cours where login='"
								+ login + "';", null);
		List<Cours> cours = new ArrayList<Cours>();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Cours c = new Cours();
			c.name = cursor.getString(0);
			c.type = cursor.getString(1);
			c.debut = cursor.getString(2);
			c.fin = cursor.getString(3);
			c.groupe = cursor.getString(4);
			c.salle = cursor.getString(5);
			c.formateur = cursor.getString(6);
			c.auteur = cursor.getString(7);
			c.apprenants = cursor.getString(8);
			c.position = cursor.getString(9);
			c.eventid = cursor.getLong(cursor.getColumnIndex("eventid"));
			cours.add(c);
		}
		cursor.close();
		db.close();
		return cours;
	}

	public long nbUser() {
		db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select count(login) from user", null);
		db.close();
		if (cursor.moveToNext()) {
			return cursor.getLong(0);
		}
		return 0;
	}

	public void updateDefaultUser(String login) {
		db = helper.getWritableDatabase();
		db.execSQL("update user set defaultuser = 0 where defaultuser = 1");
		db.execSQL("update user set defaultuser =? where login = ?",
				new Object[] { 1, login });
		db.close();
	}

	public boolean isCalendarSynced(String login) {
		boolean b = false;
		db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select sync from user where login='"+ login + "';", null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			b = cursor.getInt(cursor.getColumnIndex("sync")) == 1;
		}
		cursor.close();
		db.close();
		return b;
	}

	public void setCalendarSynced(String login, boolean synced) {
		int flag = synced ? 1 : 0;
		db = helper.getWritableDatabase();
		db.execSQL("update user set sync =? where login = ?", new Object[] {
				flag, login });
		db.close();
	}

	public int syncCalendar(Context context, String login) {
		int nbClassSync = 0;
		GoogleCalendarAPI calendarAPI = new GoogleCalendarAPI(context);
		for(Cours c : findClass(login)){
			if(c.eventid==0){
				nbClassSync++;
				long eventid = calendarAPI.addCourse2Calendar(c);
				c.eventid = eventid;
				updateClass(c, login);
			}
		}
		return nbClassSync;
	}
	
	private void updateClass(Cours c, String login){
		db = helper.getWritableDatabase();
		db.execSQL("update cours set eventid = ? where debut = ? AND login = ?",
				new Object[] { c.eventid, c.debut,login });
		db.close();
	}

}
