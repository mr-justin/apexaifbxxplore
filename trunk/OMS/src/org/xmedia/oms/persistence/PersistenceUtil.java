package org.xmedia.oms.persistence;

import org.xmedia.oms.persistence.dao.IDaoManager;

public class PersistenceUtil {

	private static IDaoManager s_daoManager;
	
	private static ISessionFactory s_sessionFactory;
	
	public static IDaoManager getDaoManager(){
		return s_daoManager;
	}
	
	public static ISessionFactory getSessionFactory(){
		return s_sessionFactory;
	}
	
	public static void setDaoManager(IDaoManager man){
		s_daoManager = man;
	}
	
	public static void setSessionFactory(ISessionFactory factory){
		s_sessionFactory = factory;
	}

}

