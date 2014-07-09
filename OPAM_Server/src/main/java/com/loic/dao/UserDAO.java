package com.loic.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.loic.model.User;

@Repository //marks the specific class as a Data Access Object
public class UserDAO extends GenericDAO<User>{
	
	@Autowired
	public UserDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@SuppressWarnings("unchecked")
	public User findByLogin(String login){
		Transaction tr = session.beginTransaction();	
		List<User> resultaList = session.createQuery("from User where login = ?")
		.setString(0, login).list();
		tr.commit();
		if(resultaList==null || resultaList.size()==0) return null;
		else return resultaList.get(0);
	}
}
