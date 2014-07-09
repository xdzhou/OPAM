package com.loic.dao;

import java.util.List;

import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import com.loic.model.Message;
import com.loic.model.User;

@Repository //marks the specific class as a Data Access Object
public class MessageDAO extends GenericDAO<Message>{
	
	@SuppressWarnings("unchecked")
	public List<User> findByName(String name){
		Transaction tr = session.beginTransaction();	
		List<User> resultaList = session.createQuery("from User where name = ?")
		.setString(0, name).list();
		tr.commit();
		return resultaList;
	}
}
