package org.xmedia.oms.adapter.kaon2.persistence;

import java.util.ArrayList;
import java.util.List;

import org.xmedia.oms.adapter.kaon2.query.SparqlQueryEvaluator;
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



public class Kaon2DaoManager implements IDaoManager{

	private List<IConceptDao> m_conceptDaos;

	private List<Boolean> m_conceptDaoAvailability;

	private List<IIndividualDao> m_indvidualDaos;

	private List<Boolean> m_indvidualDaoAvailability;

	private List<ILiteralDao> m_literalDaos;

	private List<Boolean> m_literalDaoAvailability;

	private List<IPropertyDao> m_propertyDaos;

	private List<Boolean> m_propertyDaoAvailability;

	private List<IPropertyMemberAxiomDao> m_propertyMemberDaos;

	private List<Boolean> m_propertyMemberDaoAvailability;
	
	private List<IDatatypeDao> m_datatypeDaos;

	private List<Boolean> m_datatypeDaoAvailability;

	private List<IQueryEvaluator> m_sparqlEngines;

	private List<Boolean> m_sparqlEnAvailability;

	//Private constructor suppresses generation of a (public) default constructor
	private Kaon2DaoManager() {
		m_conceptDaoAvailability = new ArrayList<Boolean>();
		m_conceptDaos = new ArrayList<IConceptDao>();
		m_indvidualDaoAvailability = new ArrayList<Boolean>();
		m_indvidualDaos = new ArrayList<IIndividualDao>();
		m_literalDaoAvailability = new ArrayList<Boolean>();
		m_literalDaos = new ArrayList<ILiteralDao>();
		m_propertyDaoAvailability = new ArrayList<Boolean>();
		m_propertyDaos = new ArrayList<IPropertyDao>();
		m_propertyMemberDaoAvailability = new ArrayList<Boolean>();
		m_propertyMemberDaos = new ArrayList<IPropertyMemberAxiomDao>();
		m_datatypeDaoAvailability = new ArrayList<Boolean>();
		m_datatypeDaos = new ArrayList<IDatatypeDao>();
		m_sparqlEnAvailability = new ArrayList<Boolean>();
		m_sparqlEngines = new ArrayList<IQueryEvaluator>();
	}

	private static class SingletonHolder {
		private static Kaon2DaoManager s_instance = new Kaon2DaoManager();
	} 

	public static Kaon2DaoManager getInstance() {
		return SingletonHolder.s_instance;
	}

	private IDao createDao(Class daoInterface) {

		IDao dao = null; 

		if (daoInterface.getCanonicalName().contains("ConceptDao")){
			dao = new Kaon2ConceptDao();
			m_conceptDaoAvailability.add(Boolean.TRUE);
			m_conceptDaos.add((IConceptDao)dao);
		}
		if (daoInterface.getCanonicalName().contains("IndividualDao")){
			dao = new Kaon2IndividualDao();
			m_indvidualDaoAvailability.add(Boolean.TRUE);
			m_indvidualDaos.add((IIndividualDao)dao);
		}
		if (daoInterface.getCanonicalName().contains("LiteralDao")){
			dao = new Kaon2LiteralDao();
			m_literalDaoAvailability.add(Boolean.TRUE);
			m_literalDaos.add((ILiteralDao)dao);
		}
		if (daoInterface.getCanonicalName().contains("PropertyDao")){
			dao = new Kaon2PropertyDao();
			m_propertyDaoAvailability.add(Boolean.TRUE);
			m_propertyDaos.add((IPropertyDao)dao);

		}
		if (daoInterface.getCanonicalName().contains("PropertyMemberAxiomDao")){
			dao = new Kaon2PropertyMemberDao();
			m_propertyMemberDaoAvailability.add(Boolean.TRUE);
			m_propertyMemberDaos.add((IPropertyMemberAxiomDao)dao);

		}

		if (daoInterface.getCanonicalName().contains("DatatypeDao")){
			dao = new Kaon2DatatypeDao();
			m_datatypeDaoAvailability.add(Boolean.TRUE);
			m_datatypeDaos.add((IDatatypeDao)dao);

		}

		return dao;
	}
	
	private IQueryEvaluator createEngine(int querytype) {

		IQueryEvaluator eval = null; 

		if (querytype == IDaoManager.SPARQL_QUERYTYPE){
			eval = new SparqlQueryEvaluator();
			m_sparqlEnAvailability.add(Boolean.TRUE);
			m_sparqlEngines.add(eval);
		}

		return eval;
	}
	
	
	public void freeDao(IDao dao) {
		List current = null; 
		if (dao instanceof IConceptDao) current = m_conceptDaos;
		else if(dao instanceof IPropertyDao) current = m_propertyDaos;
		else if(dao instanceof IIndividualDao) current = m_indvidualDaos;
		else if(dao instanceof ILiteralDao) current = m_literalDaos;
		else if(dao instanceof IPropertyMemberAxiomDao) current = m_propertyMemberDaos;
		else if(dao instanceof IDatatypeDao) current = m_datatypeDaos;

		int i = current.indexOf(dao);
		getAvailabilityList(current).set(i, true);

	}

	public void freeEvaluator(IQueryEvaluator evaluator){
		List current = null; 
		if (evaluator instanceof SparqlQueryEvaluator) current = m_sparqlEngines;
		
		int i = current.indexOf(evaluator);
		getAvailabilityList(current).set(i, true);
	}



	public IDao getAvailableDao(Class daotype) {
		List current = null; 
		if (daotype.getCanonicalName().contains("PropertyDao")) current = m_propertyDaos;
		else if (daotype.getCanonicalName().contains("ConceptDao")) current = m_conceptDaos;
		else if (daotype.getCanonicalName().contains("IndividualDao")) current = m_indvidualDaos;
		else if (daotype.getCanonicalName().contains("LiteralDao")) current = m_literalDaos;
		else if (daotype.getCanonicalName().contains("PropertyMemberAxiomDao")) current = m_propertyMemberDaos;
		else if (daotype.getCanonicalName().contains("DatatypeDao")) current = m_datatypeDaos;
		
		List<Boolean> availability = getAvailabilityList(current);

		if (getCurrentSize(current)>-1){	    		
			int index =-1;
			if ((index = availability.indexOf(true))>-1){
				availability.set(index, Boolean.FALSE);
				return (IDao)current.get(index);

			}

		}
		//else:
		return createDao(daotype);
	}

	public IQueryEvaluator getAvailableEvaluator(int querytype){

		List<IQueryEvaluator> current = null; 
		if (querytype == IDaoManager.SPARQL_QUERYTYPE) current = m_sparqlEngines;
		List<Boolean> availability = getAvailabilityList(current);
		
		if (getCurrentSize(current) > -1){	    		
			int index = -1;
			if ((index = availability.indexOf(true)) > -1){
				availability.set(index, Boolean.FALSE);
				return current.get(index);

			}

		}
		//else
		return createEngine(querytype);
	}

	private List<Boolean> getAvailabilityList(List daoList){
		if (daoList == m_conceptDaos) return m_conceptDaoAvailability;
		else if (daoList == m_propertyDaos) return m_propertyDaoAvailability;
		else if (daoList == m_literalDaos) return m_literalDaoAvailability;
		else if (daoList == m_propertyMemberDaos) return m_propertyMemberDaoAvailability;
		else if (daoList == m_indvidualDaos) return m_indvidualDaoAvailability;
		else if (daoList == m_datatypeDaos) return m_datatypeDaoAvailability;
		else if (daoList == m_sparqlEngines) return m_sparqlEnAvailability;
		else return null;

	}

	private int getCurrentSize(List daoList){
		if (daoList!=null) return daoList.size()-1;
		else return -1;
	}

	public IConceptDao getConceptDao() throws DaoUnavailableException {
		return (IConceptDao)getAvailableDao(IConceptDao.class);
	}

	public IDatatypeDao getDatatypeDao() throws DaoUnavailableException {
		return (IDatatypeDao)getAvailableDao(IDatatypeDao.class);
	}

	public IEntityDao getEntityDao() throws DaoUnavailableException {
		return (IEntityDao)getAvailableDao(IEntityDao.class);
	}

	public IIndividualDao getIndividualDao() throws DaoUnavailableException {
		return (IIndividualDao)getAvailableDao(IIndividualDao.class);
	}

	public ILiteralDao getLiteralDao() throws DaoUnavailableException {
		return (ILiteralDao)getAvailableDao(ILiteralDao.class);
	}

	public IPropertyDao getPropertyDao() throws DaoUnavailableException {
		return (IPropertyDao)getAvailableDao(IPropertyDao.class);
	}

	public IPropertyMemberAxiomDao getPropertyMemberDao() throws DaoUnavailableException {
		return (IPropertyMemberAxiomDao)getAvailableDao(IPropertyMemberAxiomDao.class);
	}

	public ISchemaDao getSchemaDao() throws DaoUnavailableException {
		return (ISchemaDao)getAvailableDao(ISchemaDao.class);
	}

	public IQueryEvaluator getAvailableEvaluatorPerf(int querytype) throws QueryEvaluatorUnavailableException {
		throw new QueryEvaluatorUnavailableException(-1);
	}
}
