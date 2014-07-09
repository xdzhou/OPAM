package com.loic.dao;

import java.lang.reflect.ParameterizedType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository //marks the specific class as a Data Access Object
public abstract class GenericDAO<T> {
	protected Session session;
	private Class<T> persistenceClass;

	public GenericDAO(SessionFactory sessionFactory){
		session = sessionFactory.openSession();
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

/*

CREATE TABLE IF NOT EXISTS `User` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `login` char(20) NOT NULL,
  `name` char(50) NOT NULL,
  `numWeekUpdated` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `Message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender` int(11) NOT NULL,
  `recevier` int(11) NOT NULL,
  `content` text NOT NULL,
  `time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`sender`) REFERENCES User(`id`),
  FOREIGN KEY (`recevier`) REFERENCES User(`id`)  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 */
