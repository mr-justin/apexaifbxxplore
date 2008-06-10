package org.xmedia.accessknow.sesame.persistence;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;

public class AbstractDao {
	
	@SuppressWarnings("deprecation")
	public static IEntity checkForDelegate(IEntity res)
	{	
		if (res.getDelegate() == null)
		{
			if(res instanceof NamedConcept)
			{
				IConceptDao dao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
				return dao.findByUri(res.getUri());
			}
			if(res instanceof NamedIndividual)
			{
				IIndividualDao dao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
				return dao.findByUri(res.getUri());
			}
			if(res instanceof IProperty)
			{
				IPropertyDao dao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
				return dao.findByUri(res.getUri());
			}
			else return null;
		}
		else return res;
	}
}
