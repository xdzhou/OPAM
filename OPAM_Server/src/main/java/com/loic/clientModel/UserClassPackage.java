package com.loic.clientModel;

import java.util.ArrayList;
import java.util.List;

public class UserClassPackage {
	private UserClient user = new UserClient();
	private List<ClassInfoClient> classInfos = new ArrayList<ClassInfoClient>();
	
	public UserClient getUser() {
		return user;
	}
	public void setUser(UserClient user) {
		this.user = user;
	}
	public List<ClassInfoClient> getClassInfos() {
		return classInfos;
	}
	public void setClassInfos(List<ClassInfoClient> classInfos) {
		this.classInfos = classInfos;
	}
	
	public void addClassInfo(ClassInfoClient c){
		classInfos.add(c);
	}
}
