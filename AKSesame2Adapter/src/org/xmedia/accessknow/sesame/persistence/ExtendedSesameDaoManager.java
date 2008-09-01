package org.xmedia.accessknow.sesame.persistence;

import org.xmedia.accessknow.sesame.model.SesameSparqlEvaluator;
import org.xmedia.accessknow.sesame.persistence.SesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameSession;

import java.util.ArrayList;
import java.util.List;

import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IDao;
import org.xmedia.oms.persistence.dao.IDatatypeDao;
import org.xmedia.oms.persistence.dao.IEntityDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.oms.persistence.dao.ISchemaDao;
import org.xmedia.oms.query.IQueryEvaluator;

public class ExtendedSesameDaoManager extends SesameDaoManager{

	private SesameSession m_session;
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

	private static final String CONCEPT_DAO = "ConceptDao";
	private static final String INDIVIDUAL_DAO = "IndividualDao";
	private static final String LITERAL_DAO = "LiteralDao";
	private static final String PROPERTY_DAO = "PropertyDao";
	private static final String PROPERTY_MEMBER_DAO = "PropertyMemberAxiomDao";
	private static final String DATATYPE_DAO = "DatatypeDao";

	private static ExtendedSesameDaoManager s_instance;

	//Private constructor suppresses generation of a (public) default constructor
	private ExtendedSesameDaoManager(SesameSession session) 
	{
		super(session);

		m_session = session;

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

	public static ExtendedSesameDaoManager getInstance(SesameSession session)
	{
		ExtendedSesameDaoManager.s_instance = new ExtendedSesameDaoManager(session);
		return ExtendedSesameDaoManager.s_instance;
	}

	@SuppressWarnings("unchecked")
	private IDao createDao(Class daoInterface)
	{
		IDao dao = null; 

		if (daoInterface.getCanonicalName().contains(CONCEPT_DAO))
		{
			dao = new ConceptDao(m_session);
			m_conceptDaoAvailability.add(Boolean.TRUE);
			m_conceptDaos.add((IConceptDao)dao);
		}
		if (daoInterface.getCanonicalName().contains(INDIVIDUAL_DAO))
		{
			dao = new IndividualDao(m_session);
			m_indvidualDaoAvailability.add(Boolean.TRUE);
			m_indvidualDaos.add((IIndividualDao)dao);
		}
		if (daoInterface.getCanonicalName().contains(LITERAL_DAO))
		{
			dao = new LiteralDao();
			m_literalDaoAvailability.add(Boolean.TRUE);
			m_literalDaos.add((ILiteralDao)dao);
		}
		if (daoInterface.getCanonicalName().contains(PROPERTY_DAO))
		{
			dao = new PropertyDao(m_session);
			m_propertyDaoAvailability.add(Boolean.TRUE);
			m_propertyDaos.add((IPropertyDao)dao);
		}
		if (daoInterface.getCanonicalName().contains(PROPERTY_MEMBER_DAO))
		{
			dao = new PropertyMemberDao(m_session);
			m_propertyMemberDaoAvailability.add(Boolean.TRUE);
			m_propertyMemberDaos.add((IPropertyMemberAxiomDao)dao);
		}

		if (daoInterface.getCanonicalName().contains(DATATYPE_DAO))
		{
			dao = new DatatypeDao(m_session);
			m_datatypeDaoAvailability.add(Boolean.TRUE);
			m_datatypeDaos.add((IDatatypeDao)dao);
		}

		return dao;
	}

//	private IQueryEvaluator createEngine(int querytype,Repository repository,SesameOntology ontology) 
//	{
//	IQueryEvaluator eval = null; 
	//
//	if (querytype == IDaoManager.SPARQL_QUERYTYPE)
//	{
//	eval = new SesameSparqlEvaluator(repository,ontology);
//	m_sparqlEnAvailability.add(Boolean.TRUE);
//	m_sparqlEngines.add(eval);
//	}
	//
//	return eval;
//	}


	@SuppressWarnings("unchecked")
	public void freeDao(IDao dao)
	{
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

	@SuppressWarnings("unchecked")
	public void freeEvaluator(IQueryEvaluator evaluator)
	{
		List current = null; 

		if (evaluator instanceof SesameSparqlEvaluator) current = m_sparqlEngines;

		int i = current.indexOf(evaluator);
		getAvailabilityList(current).set(i, true);
	}

	@SuppressWarnings("unchecked")
	public IDao getAvailableDao(Class daotype)
	{
		List current = null; 

		if (daotype.getCanonicalName().contains(PROPERTY_DAO)) current = m_propertyDaos;
		else if (daotype.getCanonicalName().contains(CONCEPT_DAO)) current = m_conceptDaos;
		else if (daotype.getCanonicalName().contains(INDIVIDUAL_DAO)) current = m_indvidualDaos;
		else if (daotype.getCanonicalName().contains(LITERAL_DAO)) current = m_literalDaos;
		else if (daotype.getCanonicalName().contains(PROPERTY_MEMBER_DAO)) current = m_propertyMemberDaos;
		else if (daotype.getCanonicalName().contains(DATATYPE_DAO)) current = m_datatypeDaos;

		List<Boolean> availability = getAvailabilityList(current);

		if(getCurrentSize(current) > -1)
		{	    		
			int index =-1;
			if((index = availability.indexOf(true)) > -1)
			{
				availability.set(index, Boolean.FALSE);
				return (IDao)current.get(index);
			}
		}

		return createDao(daotype);
	}

	@SuppressWarnings("unchecked")
	private List<Boolean> getAvailabilityList(List daoList)
	{

		if (daoList == m_conceptDaos) return m_conceptDaoAvailability;
		else if (daoList == m_propertyDaos) return m_propertyDaoAvailability;
		else if (daoList == m_literalDaos) return m_literalDaoAvailability;
		else if (daoList == m_propertyMemberDaos) return m_propertyMemberDaoAvailability;
		else if (daoList == m_indvidualDaos) return m_indvidualDaoAvailability;
		else if (daoList == m_datatypeDaos) return m_datatypeDaoAvailability;
		else if (daoList == m_sparqlEngines) return m_sparqlEnAvailability;
		else return null;

	}

	@SuppressWarnings("unchecked")
	private int getCurrentSize(List daoList)
	{
		if (daoList!=null) return daoList.size()-1;
		else return -1;
	}

	public IConceptDao getConceptDao() throws DaoUnavailableException
	{
		return (IConceptDao)getAvailableDao(IConceptDao.class);
	}

	public IDatatypeDao getDatatypeDao() throws DaoUnavailableException
	{
		return (IDatatypeDao)getAvailableDao(IDatatypeDao.class);
	}

	@SuppressWarnings("unchecked")
	public IEntityDao getEntityDao() throws DaoUnavailableException
	{
		return (IEntityDao)getAvailableDao(IEntityDao.class);
	}

	public IIndividualDao getIndividualDao() throws DaoUnavailableException
	{
		return (IIndividualDao)getAvailableDao(IIndividualDao.class);
	}

	public ILiteralDao getLiteralDao() throws DaoUnavailableException
	{
		return (ILiteralDao)getAvailableDao(ILiteralDao.class);
	}

	public IPropertyDao getPropertyDao() throws DaoUnavailableException 
	{
		return (IPropertyDao)getAvailableDao(IPropertyDao.class);
	}

	public IPropertyMemberAxiomDao getPropertyMemberDao() 
	{
		return (IPropertyMemberAxiomDao)getAvailableDao(IPropertyMemberAxiomDao.class);
	}

	public ISchemaDao getSchemaDao() throws DaoUnavailableException 
	{
		return (ISchemaDao)getAvailableDao(ISchemaDao.class);
	}
}
