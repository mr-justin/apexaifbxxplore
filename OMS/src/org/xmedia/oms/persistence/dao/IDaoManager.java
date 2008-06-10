package org.xmedia.oms.persistence.dao;

import org.xmedia.oms.query.IQueryEvaluator;

public interface IDaoManager {
	
	public static int SPARQL_QUERYTYPE = 0;
	
	public static int DLSAFE_QUERYTYPE = 1;
	
	public static int SERQL_QUERYTYPE = 2;
	
	public void freeDao(IDao dao);
	
	/**
	 * @deprecated Use instead the get*Dao() methods.
	 * 
	 * @param daotype
	 * @return
	 */
	public IDao getAvailableDao(Class daotype);
	
	public IQueryEvaluator getAvailableEvaluator(int querytype) throws QueryEvaluatorUnavailableException;
	
	public IQueryEvaluator getAvailableEvaluatorPerf(int querytype) throws QueryEvaluatorUnavailableException;
	
	public IPropertyMemberAxiomDao getPropertyMemberDao() throws DaoUnavailableException;
	
	public IDatatypeDao getDatatypeDao() throws DaoUnavailableException;
	
	public IConceptDao getConceptDao() throws DaoUnavailableException;
	
	public IEntityDao getEntityDao() throws DaoUnavailableException;
	
	public IIndividualDao getIndividualDao() throws DaoUnavailableException;
	
	public ILiteralDao getLiteralDao() throws DaoUnavailableException;
	
	public IPropertyDao getPropertyDao() throws DaoUnavailableException;
	
	public ISchemaDao getSchemaDao() throws DaoUnavailableException;
}
