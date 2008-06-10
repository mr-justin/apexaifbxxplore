package org.xmedia.accessknow.sesame.persistence;

import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IDao;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.IDatatypeDao;
import org.xmedia.oms.persistence.dao.IEntityDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.oms.persistence.dao.ISchemaDao;
import org.xmedia.oms.persistence.dao.QueryEvaluatorUnavailableException;
import org.xmedia.oms.query.IQueryEvaluator;

public class SesameDaoManager implements IDaoManager {
	
	private SesameSession session;
	private static SesameDaoManager s_instance;
	
	protected SesameDaoManager(SesameSession session) {
		this.session = session;
	}

	public static SesameDaoManager getInstance(SesameSession session)
	{
		if(SesameDaoManager.s_instance == null) SesameDaoManager.s_instance = new SesameDaoManager(session);
		return SesameDaoManager.s_instance;
	}
	
	/**
	 * @deprecated
	 */
	@SuppressWarnings("unchecked")
	public IDao getAvailableDao(Class daotype) {
		try {
			if(daotype.equals(IPropertyMemberAxiomDao.class)) {
				return getPropertyMemberDao();
			}
			if(daotype.equals(IIndividualDao.class)) {
				return getIndividualDao();
			}
			if(daotype.equals(IConceptDao.class)) {
				return getConceptDao();
			}
			if(daotype.equals(IPropertyDao.class)) {
				return getPropertyDao();
			}
			if(daotype.equals(IDatatypeDao.class)) {
				return getDatatypeDao();
			}
		} catch(DaoUnavailableException e) {
			return null;
		}
		return null;
	}
	
	public void freeDao(IDao dao) {
	}

	public IQueryEvaluator getAvailableEvaluator(int querytype) throws QueryEvaluatorUnavailableException {
		return session.getSesameOntology().getAvailableEvaluator(querytype);
	}

	public IQueryEvaluator getAvailableEvaluatorPerf(int querytype) throws QueryEvaluatorUnavailableException {
		return session.getSesameOntology().getAvailableEvaluatorPerf(querytype);
	}
	
	public IPropertyMemberAxiomDao getPropertyMemberDao() {
		return new PropertyMemberDao(session);
	}

	public IConceptDao getConceptDao() throws DaoUnavailableException {
		return new ConceptDao(session);
	}

	public IDatatypeDao getDatatypeDao() throws DaoUnavailableException {
		return new DatatypeDao(session);
	}

	@SuppressWarnings("unchecked")
	public IEntityDao getEntityDao() throws DaoUnavailableException {
		throw new DaoUnavailableException();
	}

	public IIndividualDao getIndividualDao() throws DaoUnavailableException {
		return new IndividualDao(session);
	}

	public ILiteralDao getLiteralDao() throws DaoUnavailableException {
		throw new DaoUnavailableException();
	}

	public IPropertyDao getPropertyDao() throws DaoUnavailableException {
		return new PropertyDao(session);
	}

	public ISchemaDao getSchemaDao() throws DaoUnavailableException {
		throw new DaoUnavailableException();
	}

}
