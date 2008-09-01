package org.xmedia.accessknow.sesame.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.xmedia.accessknow.sesame.model.PropertyMember;
import org.xmedia.accessknow.sesame.persistence.converter.AK2Ses;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.MetaVocabulary;
import org.xmedia.oms.metaknow.ProvenanceUnknownException;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.dao.BODeletionException;
import org.xmedia.oms.persistence.dao.BOInsertionException;
import org.xmedia.oms.persistence.dao.BOsDeletionException;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.oms.persistence.dao.QueryEvaluatorUnavailableException;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.IQueryWrapper;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryException;
import org.xmedia.oms.query.QueryWrapper;

public class PropertyMemberDao implements IPropertyMemberAxiomDao {

	private enum ProvSparqlVariable {

		AGENT ("agent"),
		SOURCE ("source"),
		CONFIDENCE ("confidence"),
		DATE ("date");

		private final String name;
		private static Map<String,ProvSparqlVariable> tokenMap;

		private ProvSparqlVariable(String name){
			this.name = name.toLowerCase();
			map(name,this);
		}

		private void map(String name, ProvSparqlVariable op){
			if (tokenMap==null) {
				tokenMap = new HashMap<String, ProvSparqlVariable>();
			}
			tokenMap.put(name,op);
		}

		public static ProvSparqlVariable forName(String name){
			return tokenMap.get(name);
		}

		public String getUrn() {
			return name;
		}
	}

	private SesameSession m_session;

	private IQueryEvaluator m_sparqlEngine;

	protected PropertyMemberDao(SesameSession session) {

		this.m_session = session;
	}

	private synchronized IQueryEvaluator getSparqlEngine() throws QueryEvaluatorUnavailableException {

		if (m_sparqlEngine == null) {
			m_sparqlEngine = this.m_session.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);
		}

		return m_sparqlEngine;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public Set<IPropertyMember> findByIndividual(IIndividual individual)
	throws DatasourceException {
		return null;
	}


	public Set<IPropertyMember> findByProperty(IProperty property) throws DatasourceException {

		Set<IPropertyMember> result = new HashSet<IPropertyMember>();

		try {

			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> stmts =  conn.getStatements(null,AK2Ses.getProperty(property, m_session.getValueFactory()),null, m_session.isReasoningOn());

			Statement stmt;

			try {

				while(stmts.hasNext()) {
					stmt = stmts.next();
					result.add(Ses2AK.getPropertyMember(stmt, m_session.getOntology()));
				}

			} 
			finally {
				stmts.close();
			}
		} 
		catch(Exception e) {
			e.printStackTrace();
		}	

		return result;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public Set<IPropertyMember> findObjectPropertyMemberByIndividual(
			IIndividual individual) throws DatasourceException {
		return null;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public Set<IPropertyMember> findObjectPropertyMemberBySource(
			IIndividual individual) throws DatasourceException {
		return null;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public IBusinessObject findById(String id) throws DatasourceException {
		return null;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public Class getBoClass() {
		return null;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void insert(IBusinessObject newBo) throws DatasourceException {}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void delete(IBusinessObject existingBo) throws DatasourceException {
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void update(IBusinessObject existingBo) throws DatasourceException {
	}


	public Set<IPropertyMember> findByTargetIndividual(IIndividual individual) throws DatasourceException {
		return findByTargetIndividual(individual, m_session.isReasoningOn());
	}

	public Set<IPropertyMember> findByTargetValue(ILiteral literal) throws DatasourceException {

		Set<IPropertyMember> result = new HashSet<IPropertyMember>();

		try {

			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> stmts =  conn.getStatements(null,null,AK2Ses.getObject(literal, m_session.getValueFactory()), m_session.isReasoningOn());

			Statement stmt;

			try {

				while(stmts.hasNext()) {
					stmt = stmts.next();
					result.add(Ses2AK.getPropertyMember(stmt, m_session.getOntology()));
				}

			} 
			finally {
				stmts.close();
			}
		} 
		catch(Exception e) {
			e.printStackTrace();
		}	

		return result;
	}

	public Set<IPropertyMember> findByAgent(IEntity agent) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findByAgent(String agentUri) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findByConfidenceDegree(double degree, int degreeType) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findByConfidenceDegreeBetween(double lowerbound, int degreeTypeLower, long upperbound, int degreeTypeUpper) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findByCreationDate(Date creationDate, int type) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findByCreationDateBetween(Date before, int beforeType, Date after, int aftertype) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findBySource(IEntity source) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findBySource(String sourceUri) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public IEntity getAgent(IResource res) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getConfidenceDegree(IResource res) {
		// TODO Auto-generated method stub
		return 0;
	}

	public IProvenance getCreationDate(IResource res) {
		// TODO Auto-generated method stub
		return null;
	}

	public IEntity getSource(IResource res) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IEntity> getSources(IPropertyMember res)
	throws ProvenanceUnknownException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findByIndividual(IIndividual individual,
			boolean includeInferred) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findByProperty(IProperty property,
			boolean includeInferred) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findByTargetIndividual(
			IIndividual target, boolean includeInferred)
			throws DatasourceException {

		Set<IPropertyMember> results = new HashSet<IPropertyMember>();

		try {
			Resource sesObject = AK2Ses.getResource(target, m_session.getValueFactory());
			List<Statement> itsStatements = m_session.getStatements(null, null, sesObject, includeInferred);

			for (Statement aStatement : itsStatements) {
				results.add(Ses2AK.getPropertyMember(aStatement, m_session.getOntology()));
			}

		} catch (Exception e) {
			throw new DatasourceException("Error occurred while retrieving statements for '" + target.toString() + "'.", e);
		}

		return results;

	}

	public Set<IPropertyMember> findByTargetValue(ILiteral literal,
			boolean includeInferred) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findObjectPropertyMemberByIndividual(
			IIndividual individual, boolean includeInferred)
			throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IPropertyMember> findObjectPropertyMemberBySource(
			IIndividual individual, boolean includeInferred)
			throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}


	public IPropertyMember find(
			IIndividual subject, 
			IProperty property,
			IResource object) throws DatasourceException {

		return find(subject, property, object, m_session.isReasoningOn());

	}

	public IPropertyMember find(
			IIndividual subject, 
			IProperty property,
			IResource object,
			boolean includeInferred) throws DatasourceException {

		IPropertyMember found =  null; 

		try {
			List<Statement> matching = m_session.getStatements(
					AK2Ses.getResource(subject, m_session.getValueFactory()),
					AK2Ses.getProperty(property, m_session.getValueFactory()),
					AK2Ses.getObject(object, m_session.getValueFactory()),
					includeInferred);

			if (matching.size() > 0) {

				for (Statement statement : matching) {
					if (SesameSession.isReficationContext(statement.getContext())) {
						found = PropertyMember.createPropertyMember(
								subject, 
								property, 
								object, 
								m_session.getOntology(), 
								statement.getContext().toString());
						break;
					}
				}

				if (found == null) {
					found = PropertyMember.createPropertyMember(subject, property, object, m_session.getOntology());
				}
			}

		} catch (Exception e) {
			throw new DatasourceException("Cannot find property member " + 
					PropertyMember.createPropertyMember(subject, property, object, m_session.getOntology()), e);
		}

		return found;

	}

	public List<IPropertyMember> findAll() throws DatasourceException {

		return findAll(m_session.isReasoningOn(), false);

	}

	private boolean isProvenanceStatement(Statement statement) {

		boolean isProvenanceStatement = false;

		String subjectUri = statement.getSubject().toString();
		String objectUri = statement.getObject().toString();

		isProvenanceStatement = 
			subjectUri.startsWith(SesameSession.PROVENANCE_NS) ||
			objectUri.startsWith(SesameSession.PROVENANCE_NS);

		return isProvenanceStatement;
	}

	public List<IPropertyMember> findAll(boolean includeInferred, boolean includeProvenanceStatements) throws DatasourceException {

		List<IPropertyMember> results = new ArrayList<IPropertyMember>();

		try {

			// Note: in case includeProvenanceStatements is false, it seems to me anyway impossible to
			// perform a filtered SPARQL query because in sparql (to my knowledge (piercarlo)) 
			// there is no way to handle the includeInferred parameter.
			List<Statement> itsStatements = m_session.getAllStatements(includeInferred);

			for (Statement statement : itsStatements) {
				if (includeProvenanceStatements || !isProvenanceStatement(statement)) {
					if(statement.getObject() != null && statement.getPredicate() != null & statement.getSubject() != null){
						results.add(Ses2AK.getPropertyMember(statement, m_session.getOntology()));
					}
				}
			}

		} catch (Exception e) {
			throw new DatasourceException("Error occurred while retrieving all statements.", e);
		}

		return results;

	}

	public Set<IPropertyMember> findBySourceIndividual(IIndividual aSubject) throws DatasourceException {

		return findBySourceIndividual(aSubject, m_session.isReasoningOn());

	}

	public Set<IPropertyMember> findBySourceIndividual(IIndividual aSubject, boolean includeInferred) throws DatasourceException {

		Set<IPropertyMember> results = new HashSet<IPropertyMember>();

		try {
			Resource sesSubject = AK2Ses.getResource(aSubject, m_session.getValueFactory());
			List<Statement> itsStatements = m_session.getStatementsWithSubject(sesSubject, includeInferred);

			for (Statement aStatement : itsStatements) {
				results.add(Ses2AK.getPropertyMember(aStatement, m_session.getOntology()));
			}

		} catch (Exception e) {
			throw new DatasourceException("Error occurred while retrieving statements for '" + aSubject.toString() + "'.", e);
		}

		return results;
	}

	public IProvenance createProvenance(
			INamedIndividual agent,
			Double confidenceDegree, 
			Date creationDate, 
			IEntity source) {

		return new org.xmedia.accessknow.sesame.model.Provenance(confidenceDegree, agent, source, creationDate);
	}

	private IProvenance createProvenance(ITuple queryResult) {

		INamedIndividual agent = null;
		double confidence = 0;
		Date date = null;
		INamedIndividual source = null;

		for (int i = 0; i < queryResult.getArity(); i++) {

			String value = queryResult.getElementAt(i).getLabel();
			ProvSparqlVariable variable = ProvSparqlVariable.forName(queryResult.getLabelAt(i).toLowerCase());
			if (variable != null) {
				switch (variable) {
				case AGENT:
					agent = m_session.getOntology().createNamedIndividual(value);
					break;
				case CONFIDENCE:
					confidence = Double.valueOf(value);
					break;
				case DATE:
					date = new Date(Long.valueOf(value));
					break;
				case SOURCE:
					source = m_session.getOntology().createNamedIndividual(value);
					break;
				default:
					break;
				}
			}

		}

		return createProvenance(agent, confidence, date, source);
	}

	public Set<IProvenance> getProvenances(IPropertyMember res) throws ProvenanceUnknownException {

		Set<IProvenance> provenances = new HashSet<IProvenance>();
		String reification = res.getUri(); 

		if ((reification == null) || (reification.length() == 0)) {
			throw new ProvenanceUnknownException(res, new Exception("Property member not reified."));
		} else {

			try {
				IQueryEvaluator sparqlEngine = getSparqlEngine();

				IQueryWrapper theQuery = new QueryWrapper(
						"\nSELECT DISTINCT *" +
						"\nWHERE {" +
						"\n<" + reification + "> <" + MetaVocabulary.HAS_PROVENANCE + "> ?provenance ." +
						"\nOPTIONAL {?provenance <" + MetaVocabulary.AGENT + "> ?" + ProvSparqlVariable.AGENT + " } ." +
						"\nOPTIONAL {?provenance <" + MetaVocabulary.CONFIDENCE_DEGREE + "> ?" + ProvSparqlVariable.CONFIDENCE + " } ." +
						"\nOPTIONAL {?provenance <" + MetaVocabulary.CREATION_TIME + "> ?" + ProvSparqlVariable.DATE + " } ." +
						"\nOPTIONAL {?provenance <" + MetaVocabulary.SOURCE + "> ?" + ProvSparqlVariable.SOURCE + " } ." +
						"}",
						null);

				Set<ITuple> theResult = sparqlEngine.evaluate(theQuery).getResult();

				if (theResult.size() == 0) {
					throw new ProvenanceUnknownException(res, new Exception("Provenance not set."));
				} else {
					for(ITuple aTuple : theResult) {
						provenances.add(createProvenance(aTuple));
					}
				}

			} catch (QueryEvaluatorUnavailableException e) {
				throw new ProvenanceUnknownException(res, e);
			} catch (QueryException e) {
				throw new ProvenanceUnknownException(res, e);
			}

		}

		return provenances;
	}

	public IPropertyMember insert(IIndividual subject, IProperty property,
			IResource object, IProvenance provenance)
	throws BOInsertionException {

		IPropertyMember inserted = insert(subject, property, object);
		String itsReification = inserted.getUri();

		try {
			if ((itsReification == null) || (itsReification.length() == 0)) {
				if (!m_session.getSesameOntology().isReificationEnabled()) {
					throw new ReificationUnsupported(m_session.getOntology());
				} else {
					throw new Exception("Reification not availabe for statement: " + inserted.toString());
				}
			} else {
				m_session.addProvenance(itsReification, provenance);
			}
		} catch (Exception e) {
			throw new BOInsertionException(inserted, e);
		}

		return inserted;
	}

	public IPropertyMember insert(List<IIndividual> subjects, List<IProperty> properties, List<IResource> objects, IProvenance provenance)
	throws BOInsertionException {

		List<String> insertedStatements = new ArrayList<String>();
		for (int i = 0; i < subjects.size(); i++) {
			IPropertyMember inserted = insert(subjects.get(i), properties.get(i), objects.get(i));
			insertedStatements.add(inserted.getUri());
		}

		try {
			if ((insertedStatements == null) || (insertedStatements.size() == 0)) {
				if (!m_session.getSesameOntology().isReificationEnabled()) {
					throw new ReificationUnsupported(m_session.getOntology());
				} else {
					throw new Exception("Reification not availabe for statement");//: " + inserted.toString());
				}
			} else {
				m_session.addProvenance(insertedStatements, provenance);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public IPropertyMember insert(IIndividual subject, IProperty property, IResource object) throws BOInsertionException {

		IPropertyMember tmpPropMember = PropertyMember.createPropertyMember(subject, property, object, m_session.getOntology()); 

		try {
			String reification = m_session.insert(AK2Ses.getStatement(tmpPropMember, m_session.getValueFactory()));

			tmpPropMember = 
				PropertyMember.createPropertyMember(subject, property, object, m_session.getOntology(), reification);

		} catch (Exception e) {
			throw new BOInsertionException(tmpPropMember, e);
		}

		return tmpPropMember;
	}

	public void delete(IIndividual subject, IProperty property, IResource object) throws BODeletionException {

		delete(find(subject, property, object));

	}

	public void delete(Set<IPropertyMember> propertyMembers) throws BODeletionException, BOsDeletionException {

		List<Statement> statements = new ArrayList<Statement>();

		for (IPropertyMember aPropertyMember : propertyMembers) {
			try { 

				deleteProvenance(aPropertyMember);
				statements.add(AK2Ses.getStatement(aPropertyMember, m_session.getValueFactory()));

			} catch (Exception e) {
				throw new BODeletionException(aPropertyMember, e);
			}
		}

		try {
			m_session.delete(statements);
		} catch (RepositoryException e) {
			throw new BOsDeletionException(propertyMembers, e);
		}

	}

	public void delete(IPropertyMember aPropertyMember) throws BODeletionException {

		try {

			deleteProvenance(aPropertyMember);

			m_session.delete(AK2Ses.getStatement(aPropertyMember, m_session.getValueFactory()));

		} catch (Exception e) {
			throw new BODeletionException(aPropertyMember, e);
		}

	}

	private void deleteProvenance(IPropertyMember aPropertyMember) throws RepositoryException, Exception {

		String reification = aPropertyMember.getUri();
		if ((reification != null) && (reification.length() > 0)) {

			List<Statement> provenanceRefs = m_session.getStatementsWithSubject(
					m_session.getValueFactory().createURI(reification), 
					false);

			List<Statement> provenances = new ArrayList<Statement>();
			for (Statement statement : provenanceRefs) {
				provenances.addAll(m_session.getStatementsWithSubject(
						(Resource)statement.getObject(), 
						false));
			} 

			List<Statement> toBeDeleted = new ArrayList<Statement>();
			toBeDeleted.addAll(provenanceRefs);
			toBeDeleted.addAll(provenances);

			m_session.delete(toBeDeleted);
		}

	}

	public IPropertyMember findByUri(String uri) throws DatasourceException {

		IPropertyMember reified = null;

		try {
			List<Statement> reifiedStatements = m_session.getStatementsInContext(uri);

			if ((reifiedStatements != null) && (reifiedStatements.size() > 0)) {
				reified = Ses2AK.getPropertyMember(reifiedStatements.get(0), m_session.getOntology());
			}

		} catch (RepositoryException e) {
			throw new DatasourceException(e);
		}

		return reified;
	}

	public Set<INamedIndividual> getAgents(IPropertyMember res) throws ProvenanceUnknownException {
		// TODO Auto-generated method stub
		return null;
	}

	public Double[] getConfidenceDegrees(IPropertyMember res) throws ProvenanceUnknownException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Date> getCreationDates(IPropertyMember res) throws ProvenanceUnknownException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<IPropertyMember> findAllDataPropertyMember() throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<IPropertyMember> findAllObjectPropertyMember() throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfPropertyMember(IProperty property) {
		return findByProperty(property).size();
	}

}
