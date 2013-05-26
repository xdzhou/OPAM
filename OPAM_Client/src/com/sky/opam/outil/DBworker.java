package com.sky.opam.outil;

import java.util.ArrayList;
import java.util.List;

import com.sky.opam.model.Cours;
import com.sky.opam.model.User;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBworker {
	private DBHelper helper;
	private SQLiteDatabase db;
	
	public DBworker (Context context){
		helper = new DBHelper(context);
	}
	
	public void addUser(User user){
		db = helper.getWritableDatabase();
		db.execSQL("insert into user (login,password,name) values (?,?,?)", new Object[]
		{ user.getLogin(), user.getPasswoed(),user.getUsename() });
		db.close();
	}
	
	public User defaultUser(){
		db = helper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select login,password,thisweek,name from user where defaultuser=1", null);		
		User user = new User();
		cursor.moveToFirst();
		if(!cursor.isAfterLast()){
			user.setLogin(cursor.getString(0));
			user.setPasswoed(cursor.getString(1));
			user.setThisweek(cursor.getInt(2));
			user.setUsename(cursor.getString(3));
		}
		cursor.close();
		db.close();
		return user;
	}
	
	public void addCours(Cours c){
		db = helper.getWritableDatabase();
		db.execSQL("insert into cours values (?,?,?,?,?,?,?,?,?,?,?)", 
		new Object[]{ c.login, c.name,c.type,c.position,c.debut,c.fin,c.auteur,c.formateur,c.apprenants,c.groupe,c.salle });
		db.close();
	}
	
	public void updateUser(User user){
		db = helper.getWritableDatabase();
		db.execSQL("update user set password = ?,defaultuser =?, thisweek=? where login = ?", new Object[]
		{ user.getPasswoed(),user.getDefaultUser(),user.getThisweek(), user.getLogin() });
		db.close();
	}
	
	public void delAllCours(){
		db = helper.getWritableDatabase();
		db.execSQL("delete from cours ;");
		db.close();
	}
	
	public void delAllCours(String login){
		db = helper.getWritableDatabase();
		db.execSQL("delete from cours where login ='"+login+"';");
		db.close();
	}
	
	public void delAllUsers(){
		db = helper.getWritableDatabase();
		db.execSQL("delete from user;");
		db.close();
	}
	
	public User findUser(String id){		
		db = helper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from user where login='"+id+"';", null);
		if(cursor.getCount()==1){
			cursor.moveToFirst();
			String login = cursor.getString(0);
			String password = cursor.getString(1);
			String username = cursor.getString(2);
			int defaultuser = cursor.getInt(3);
			int thisweek = cursor.getInt(4);
			User user = new User(login, password,username,defaultuser, thisweek);
			cursor.close();
			db.close();
			return user;
		}else{
			cursor.close();
			db.close();
			return null;
		}
	}
	
	public Cursor findClass(String login, String flag){
		db = helper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select name,type,debut,fin,groupe,salle from cours where login='"+login+"' AND position='"+flag+"';", null);
		db.close();
		return cursor;
	}
	
	public List<Cours> trouverCours(String login, String flag){
		db = helper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select name,type,debut,fin,groupe,salle,formateur,auteur,apprenant from cours where login='"+login+"' AND position='"+flag+"';", null);
		List<Cours> cours = new ArrayList<Cours>();
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
        	Cours c = new Cours();
        	c.name=cursor.getString(0);
        	c.type=cursor.getString(1);
        	c.debut = cursor.getString(2);
        	c.fin=cursor.getString(3);
        	c.groupe=cursor.getString(4);
        	c.salle=cursor.getString(5);
        	c.formateur=cursor.getString(6);
        	c.auteur=cursor.getString(7);
        	c.apprenants=cursor.getString(8);
        	cours.add(c);
        }
		cursor.close();
		db.close();
		return cours;
	}
	
	public List<Cours> findClass(String login, int numweek){
		List<Cours> cours = new ArrayList<Cours>();
		cours.addAll(trouverCours(login, numweek+"_1"));
		cours.addAll(trouverCours(login, numweek+"_2"));
		cours.addAll(trouverCours(login, numweek+"_3"));
		cours.addAll(trouverCours(login, numweek+"_4"));
		cours.addAll(trouverCours(login, numweek+"_5"));
		return cours;
	}
	
	public long nbUser(){
		db = helper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select count(login) from user", null);
		db.close();
		if (cursor.moveToNext()){
			return cursor.getLong(0);
		}
		return 0;
	}
	
	public void updateDefaultUser(String login){
		db = helper.getWritableDatabase();
		db.execSQL("update user set defaultuser = 0 where defaultuser = 1");
		db.execSQL("update user set defaultuser =? where login = ?", new Object[]{ 1, login });
		db.close();
	}

}
