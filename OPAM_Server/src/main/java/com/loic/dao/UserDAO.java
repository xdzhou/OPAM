package com.loic.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.loic.model.User;

@Repository //marks the specific class as a Data Access Object
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserDAO {
	private Session session;
	
	@Autowired  //tells the DI container to inject a dependency automaticly
	public UserDAO(SessionFactory sessionFactory){
		session = sessionFactory.openSession();
	}
	
	@Transactional
	public long save(User user){
		Transaction tr = session.beginTransaction();
		long id = (Long) session.save(user);
		tr.commit();
		return id;
	}
	@Transactional
	public void update(User user){
		Transaction tr = session.beginTransaction();
		session.update(user);
		tr.commit();
	}
	@Transactional
	public void delete(User user){
		Transaction tr = session.beginTransaction();
		session.delete(user);
		tr.commit();
	}
	@Transactional
	public User findByID(long id){
		Transaction tr = session.beginTransaction();
		User u = (User) session.get(User.class, id);
		tr.commit();
		return u;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> findByName(String name){
		Transaction tr = session.beginTransaction();	
		List<User> resultaList = session.createQuery("from User where name = ?")
		.setString(0, name).list();
		tr.commit();
		return resultaList;
	}
}
