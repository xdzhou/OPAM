package com.loic.dao;

import java.lang.reflect.ParameterizedType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository //marks the specific class as a Data Access Object
public abstract class GenericDAO<T> {
	protected Session session;
	private Class<T> persistenceClass;
	@Autowired
	private SessionFactory sessionFactory;

	public GenericDAO(){
		session = sessionFactory.getCurrentSession();
		if(session == null) session = sessionFactory.openSession();
		this.persistenceClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	@Transactional
	public long save(T objet){
		Transaction tr = session.beginTransaction();
		long id = (Long) session.save(objet);
		tr.commit();
		return id;
	}
	@Transactional
	public void update(T objet){
		Transaction tr = session.beginTransaction();
		session.update(objet);
		tr.commit();
	}
	@Transactional
	public void saveOrUpdate(T objet){
		Transaction tr = session.beginTransaction();
		session.saveOrUpdate(objet);
		tr.commit();
	}
	@Transactional
	public void delete(T objet){
		Transaction tr = session.beginTransaction();
		session.delete(objet);
		tr.commit();
	}
	@Transactional
	public T findByID(long id){
		Transaction tr = session.beginTransaction();
		T objet = (T) session.get(persistenceClass, id);
		tr.commit();
		return objet;
	}
	
	public void flush(){
		session.flush();
	}
}
