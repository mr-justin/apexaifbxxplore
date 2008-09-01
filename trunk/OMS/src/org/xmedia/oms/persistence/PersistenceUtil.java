package org.xmedia.oms.persistence;

import org.xmedia.oms.persistence.dao.IDaoManager;

public class PersistenceUtil {

	private static IDaoManager s_daoManager;
	
	public static IDaoManager getDaoManager(){
		return s_daoManager;
	}
	
	public static ISessionFactory getSessionFactory(){
		return SessionFactory.getInstance();
	}
	
	public static void setDaoManager(IDaoManager man){
		s_daoManager = man;
	}
	
	public static void setSession(ISession session){
		SessionFactory.getInstance().setSession(session);
	}

}

