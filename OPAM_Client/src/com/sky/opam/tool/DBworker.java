package com.sky.opam.tool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.sky.opam.model.ClassColor;
import com.sky.opam.model.ClassInfo;
import com.sky.opam.model.ClassType;
import com.sky.opam.model.Room;
import com.sky.opam.model.User;

import android.content.ContentValues;
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
        db.execSQL("insert into USER (login,password,name,numWeekUpdated) values (?,?,?,?)",
        		new Object[] { user.getLogin(), user.getPasswoed(),user.getName(),user.getNumWeekUpdated()});
        db.execSQL("insert into CONFIG values (?,?,?,?)",
            new Object[] { user.getLogin(), "08:00", "19:00", 0});
        db.close();
    }
    
    public void updateUser(User user) {
        db = helper.getWritableDatabase();
        db.execSQL("update user set password = ?,name =?, numWeekUpdated=? where login = ?",
            new Object[] { user.getPasswoed(), user.getName(),user.getNumWeekUpdated(), user.getLogin() });
        db.close();
    }
    
    public User getUser(String login) {
    	if(login==null) return null;
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from USER where login='" + login + "';", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            String password = cursor.getString(1);
            String name = cursor.getString(2);
            int numWeekUpdated = cursor.getInt(3);
            User user = new User(login, password, name, numWeekUpdated);
            cursor.close();
            db.close();
            return user;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }
    
    public List<User> getAllUser() {
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select login,name from USER", null);
        List<User> users = new ArrayList<User>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            User u = new User();
            u.setLogin(cursor.getString(0));
            u.setName(cursor.getString(1));
            users.add(u);
	    }     
	    cursor.close();
	    db.close();
	    return users;
	}
    
    public void delAllUsers() {
        db = helper.getWritableDatabase();
        db.execSQL("delete from USER;");
        db.close();
    }

    public User getDefaultUser() {
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select login from CONFIG where isDefaultUser=1 ",null);
        String login = null;
        if (cursor.getCount() == 1){
        	cursor.moveToFirst();
        	login = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return getUser(login);
    }
    
    public void setDefaultUser(String login) {
    	db = helper.getWritableDatabase();
    	db.execSQL("update CONFIG set isDefaultUser = 0 where isDefaultUser = 1");
    	db.execSQL("update CONFIG set isDefaultUser = ? where login = ?",new Object[] { 1, login });
    	db.close();
    }
    // Room
    public long addGetRoom(Room room){
    	return addGetRoom(room.name);
    }
    
    public long addGetRoom(String name){
    	if(name==null || name.equals("")) return -1;
    	Room r = getRoom(name);
    	if(r == null){
    		ContentValues cv = new ContentValues();
            cv.put("name", name);
            return insertData("ROOM", cv);
    	}else {
			return r.id;
		}
        
    }
    
    public List<Room> getAllRoom(){
    	db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select id,name from ROOM", null);
        List<Room> rooms = new ArrayList<Room>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Room room = new Room();
            room.id = cursor.getInt(0);
            room.name = cursor.getString(1);
            rooms.add(room);
	    }
	    cursor.close();
	    db.close();
	    return rooms;
    }
    
    private Room getRoom(long id){
    	if(id == -1) return null;
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from ROOM where id='" + id + "';", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            Room room = new Room();
            room.id = id;
            room.name = cursor.getString(1);
            cursor.close();
            db.close();
            return room;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }
    private Room getRoom(String name){
    	if(name == null || name.equals("")) return null;
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from ROOM where name='" + name + "';", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            Room room = new Room();
            room.id = cursor.getLong(0);
            room.name = name;
            cursor.close();
            db.close();
            return room;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }
    
 // ClassType
    public long addGetClassType(ClassType classType){
    	return addGetClassType(classType.name);
    }
    
    public long addGetClassType(String name){
    	if(name==null || name.equals("")) return -1;
    	ClassType classType = getClassType(name);
    	if(classType==null){
    		ContentValues cv = new ContentValues();
            cv.put("name", name);
            return insertData("CLASSTYPE", cv);
    	}else {
			return classType.id;
		}
    	
    }
    
    public List<ClassType> getAllClassType(){
    	db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select id,name from CLASSTYPE", null);
        List<ClassType> list = new ArrayList<ClassType>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        	ClassType classType = new ClassType();
        	classType.id = cursor.getInt(0);
        	classType.name = cursor.getString(1);
            list.add(classType);
	    }
	    cursor.close();
	    db.close();
	    return list;
    }
    private ClassType getClassType(long id){
    	if(id == -1) return null;
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from CLASSTYPE where id='" + id + "';", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            ClassType classType = new ClassType();
            classType.id = id;
            classType.name = cursor.getString(1);
            cursor.close();
            db.close();
            return classType;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }
    private ClassType getClassType(String name){
    	if(name==null || name.equals("")) return null;
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from CLASSTYPE where name='" + name + "';", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            ClassType classType = new ClassType();
            classType.id = cursor.getLong(0);
            classType.name = name;
            cursor.close();
            db.close();
            return classType;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }
    
 // ClassColor
    public long addClassColor(ClassColor classColor){
    	return addClassColor(classColor.color);
    }
    
    public long addClassColor(int color){
    	ContentValues cv = new ContentValues();
        cv.put("color", color);
        return insertData("CLASSCOLOR", cv);
    }

    public List<ClassColor> getAllClassColor(){
    	db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select id,color from CLASSCOLOR", null);
        List<ClassColor> list = new ArrayList<ClassColor>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        	ClassColor classColor = new ClassColor();
        	classColor.id = cursor.getInt(0);
        	classColor.color = cursor.getInt(1);
            list.add(classColor);
	    }
	    cursor.close();
	    db.close();
	    return list;
    }
    private ClassColor getClassColor(long id){
    	if(id == -1) return null;
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from CLASSCOLOR where id='" + id + "';", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            ClassColor classColor = new ClassColor();
            classColor.id = id;
            classColor.color = cursor.getInt(1);
            cursor.close();
            db.close();
            return classColor;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }
    
    //class info
    
    public long addClassInfo(ClassInfo c) {       
        ContentValues cv = new ContentValues();
        cv.put("login", c.login);
        cv.put("name", c.name);
        cv.put("weekOfYear", c.weekOfYear);
        cv.put("dayOfWeek", c.dayOfWeek);
        cv.put("startTime", c.startTime);
        cv.put("endTime", c.endTime);
        cv.put("auteur", c.auteur);
        cv.put("teacher", c.teacher);
        cv.put("students", c.students);
        cv.put("groupe", c.groupe);
        if(c.classType != null) cv.put("typeId", c.classType.id);       
        if(c.room != null) cv.put("roomId", c.room.id);
        if(c.color != null) cv.put("colorId", c.color.id);
        cv.put("isSync", c.isSync);
        return insertData("CLASSINFO", cv);
    }
    
    public void updateClassInfo(ClassInfo c) {
        db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("login", c.login);
        cv.put("name", c.name);
        cv.put("weekOfYear", c.weekOfYear);
        cv.put("dayOfWeek", c.dayOfWeek);
        cv.put("startTime", c.startTime);
        cv.put("endTime", c.endTime);
        cv.put("auteur", c.auteur);
        cv.put("teacher", c.teacher);
        cv.put("students", c.students);
        cv.put("groupe", c.groupe);
        if(c.classType != null) cv.put("typeId", c.classType.id);
        if(c.room != null) cv.put("roomId", c.room.id);
        if(c.color != null) cv.put("colorId", c.color.id);
        db.update("CLASSINFO", cv, "id = ?", new String[]{c.id+""});
        db.close();
    }
    
    public void delClassInfo(long id){
    	db = helper.getWritableDatabase();
    	String sql = "delete from CLASSINFO where id ='" + id + "' ;";
    	db.execSQL(sql);
        db.close();
    }

    public void delAllClassInfo() {
        db = helper.getWritableDatabase();
        db.execSQL("delete from CLASSINFO ;");
        db.close();
    }

    public void delClassInfo(String login) {
        db = helper.getWritableDatabase();
        db.execSQL("delete from CLASSINFO where login ='" + login + "';");
        db.close();
    }

    public List<ClassInfo> getClassInfo(String login, int weekOfYear, int dayOfWeek) {
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from CLASSINFO where login='"
            + login + "' AND weekOfYear=" + weekOfYear +" AND dayOfWeek="+dayOfWeek+ ";", null);
        List<ClassInfo> cours = new ArrayList<ClassInfo>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ClassInfo c = new ClassInfo();
            c.id = cursor.getLong(0);
            c.login = cursor.getString(1);
            c.name = cursor.getString(2);
            c.classType = getClassType(cursor.getLong(3));
            c.weekOfYear = cursor.getInt(4);
            c.dayOfWeek = cursor.getInt(5);
            c.startTime = cursor.getString(6);
            c.endTime = cursor.getString(7);
            c.auteur = cursor.getString(8);
            c.teacher = cursor.getString(9);
            c.students = cursor.getString(10);
            c.groupe = cursor.getString(11);
            c.room = getRoom(cursor.getLong(12));
            c.color = getClassColor(cursor.getLong(13));
            c.isSync = Boolean.parseBoolean(cursor.getString(14));
            cours.add(c);
        }
        cursor.close();
        db.close();
        return cours;
    }

    public List<ClassInfo> getClassInfo(String login, int weekOfYear) {
        List<ClassInfo> cours = new ArrayList<ClassInfo>();
        for(int i=0; i<5; i++){
        	cours.addAll(getClassInfo(login, weekOfYear, i+Calendar.MONDAY));
        }
        return cours;
    }

    public List<ClassInfo> getClassInfo(String login) {
        List<ClassInfo> cours = new ArrayList<ClassInfo>();
        int startNumWeek = getUser(login).getNumWeekUpdated();
        List<ClassInfo> classInfos;
        do{
        	classInfos = getClassInfo(login, startNumWeek);
        	cours.addAll(classInfos);
        	startNumWeek++;
        }while(classInfos.size()!=0);
        
        return cours;
    }

    //calendar sync
    public boolean isCalendarSynced(String login) {
            boolean b = false;
            db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select sync from user where login='"
                            + login + "';", null);
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

    //////////////////////////////////////////////////////////
    private long insertData (String tableName, ContentValues cv){
    	db = helper.getWritableDatabase();
        long id = db.insert(tableName, null, cv);
        db.close();
        return id;
    }

}