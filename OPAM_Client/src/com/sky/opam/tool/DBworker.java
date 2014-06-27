package com.sky.opam.tool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.sky.opam.model.ClassInfo;
import com.sky.opam.model.ClassType;
import com.sky.opam.model.Config;
import com.sky.opam.model.Room;
import com.sky.opam.model.User;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
        db.execSQL("insert into CONFIG values (?,?,?,?,?)",
            new Object[] { user.getLogin(), 8, 19, 1, 0});
        db.close();
    }
    
    public void updateUser(User user) {
        db = helper.getWritableDatabase();
        db.execSQL("update USER set password = ?,name =?, numWeekUpdated=? where login = ?",
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
    
    public void delUser(String login){
    	db = helper.getWritableDatabase();
    	db.execSQL("delete from USER where login ='" + login + "' ;");
    	db.execSQL("delete from CONFIG where login ='" + login + "' ;");
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
        if(login==null){
        	List<User> list = getAllUser();
        	if(list.size()>1) return list.get(0);
        	else return null; 
        }else {
        	return getUser(login);
		}      
    }
    
    public void setDefaultUser(String login) {
    	db = helper.getWritableDatabase();
    	db.execSQL("update CONFIG set isDefaultUser = 0 where isDefaultUser = 1");
    	db.execSQL("update CONFIG set isDefaultUser = ? where login = ?",new Object[] { 1, login });
    	db.close();
    }
    
    public boolean isLoginExist(String login){
    	if(login==null) return false;
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from USER where login='" + login + "';", null);
        return cursor.getCount() == 1;
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
	    if(rooms.size()==0) {
	    	Room r = new Room();
	    	r.name = "E001";
	    	r.id = addGetRoom(r);
	    	rooms.add(r);
	    }
	    return rooms;
    }
    
    public Room getRoom(long id){
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
    public Room getRoom(String name){
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
	    if(list.size()==0) {
	    	ClassType ct = new ClassType();
	    	ct.name = "Examen";
	    	ct.id = addGetClassType(ct);
	    	list.add(ct);
	    }
	    return list;
    }
    public ClassType getClassType(long id){
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
    public ClassType getClassType(String name){
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
        cv.put("bgColor", c.bgColor);
        cv.put("eventId", c.eventId);
        return insertData("CLASSINFO", cv);
    }
    
    public void updateClassInfo(ClassInfo c, GoogleCalendarAPI googleCalendarAPI) {
    	googleCalendarAPI.delEvent(c.eventId);
    	c.eventId = 0;
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
        cv.put("bgColor", c.bgColor);
        cv.put("eventId", c.eventId);
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

    public void delDownloadClassInfo(String login, GoogleCalendarAPI calendarAPI) {
    	int currentWeekOfYear = Tool.getNumWeek();
    	String sql = "select * from CLASSINFO where login='"+ login + 
    			"' AND weekOfYear>=" + currentWeekOfYear +" AND eventId>0" +" AND auteur!='"+login+"';";
    	List<ClassInfo> list = getClassInfoViaSql(sql);
    	for(ClassInfo c : list){
    		calendarAPI.delEvent(c.eventId);
    	}
        db = helper.getWritableDatabase();
        db.execSQL("delete from CLASSINFO where login ='" + login +"' AND auteur!='"+login+"';");
        db.execSQL("delete from CLASSINFO where login ='" + login +"' AND auteur='"+login+"' AND weekOfYear<"+currentWeekOfYear+" ;");
        db.close();
    }

    public List<ClassInfo> getClassInfo(String login, int weekOfYear, int dayOfWeek) {
    	String sql = "select * from CLASSINFO where login='"
                + login + "' AND weekOfYear=" + weekOfYear +" AND dayOfWeek="+dayOfWeek+ ";";
    	return getClassInfoViaSql(sql);
    }
    
    public List<ClassInfo> getUnsyncClassInfo(String login) {
        return getClassInfoViaSql("select * from CLASSINFO where login='"+ login + "' AND eventId = 0 ;");
    }
    
    public void setClassSynced(long classId, long eventId){
    	db = helper.getWritableDatabase();
    	db.execSQL("update CLASSINFO set eventId=? where id = ?",
                new Object[] { eventId, classId });
    	db.close();
    }
    
    private List<ClassInfo> getClassInfoViaSql(String sql) {
    	db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
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
            c.bgColor = cursor.getString(13);
            c.eventId = cursor.getLong(14);
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

    public ClassInfo getClassInfo(long id) {
    	String sql = "select * from CLASSINFO where id = "+id+" ;";
        List<ClassInfo> cours = getClassInfoViaSql(sql);
        return cours.get(0);
    }
    //get config   
    public Config getConfig(String login){
    	if(login == null || login.equals("")) return null;
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from CONFIG where login='" + login + "';", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            Config config = new Config();
            config.login = login;
            config.startTime = cursor.getInt(1);
            config.endTime = cursor.getInt(2);
            config.isAutoSync = cursor.getInt(3)==1;
            config.isDefaultUser = cursor.getInt(4)==1;
            cursor.close();
            db.close();
            return config;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }
    
    public void updateConfig(Config config) {
        db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("startTime", config.startTime);
        cv.put("endTime", config.endTime);
        cv.put("isAutoSync", config.isAutoSync);
        cv.put("isDefaultUser", config.isDefaultUser);       
        db.update("CONFIG", cv, "login = ?", new String[]{config.login});
        db.close();
    }
    
    //指示App配置 自动登录  自动提示升级信息
    public void setAutoLogin(Context context, boolean b){
    	context.getSharedPreferences("share", 0).edit().putBoolean("autoLogin", b).commit();    	
    }
    
    public void setAutoUpdateNotify(Context context, boolean b){
    	context.getSharedPreferences("share", 0).edit().putBoolean("autoUpdateNotify", b).commit(); 
    }
    
    public boolean getAutoLogin(Context context){
    	SharedPreferences pref = context.getSharedPreferences("share", 0); 
		return pref.getBoolean("autoLogin", true);   	
    }
    
    public boolean getAutoUpdateNotify(Context context){
    	SharedPreferences pref = context.getSharedPreferences("share", 0); 
		return pref.getBoolean("autoUpdateNotify", true);
    }   

    //////////////////////////////////////////////////////////
    private long insertData (String tableName, ContentValues cv){
    	db = helper.getWritableDatabase();
        long id = db.insert(tableName, null, cv);
        db.close();
        return id;
    }

}