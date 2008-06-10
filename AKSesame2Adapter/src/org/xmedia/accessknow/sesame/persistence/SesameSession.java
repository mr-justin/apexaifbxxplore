package org.xmedia.accessknow.sesame.persistence;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.accessknow.sesame.persistence.converter.AK2Ses;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.MetaVocabulary;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ITransaction;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.uris.XMURIFactory;

/**
 * Reasoning is turned off by default.
 * 
 * @author slp
 *
 */
public class SesameSession implements ISession {

	private static final long serialVersionUID = -8677031095384638150L;
	
	private static final String REIFICATION_NS = "http://www.x-media-project.org/ontologies/reification#";
	private static final String REIFICATION_FRAGMET_PREFIX = "statement_";
	protected static final String PROVENANCE_NS = "http://http://www.x-media-project.org/ontologies/provenance#";
	
	private RepositoryConnection repositoryConnection = null;
	
	private Boolean transactionMutex = new Boolean(true);
	private SesameTransaction currentTransaction;
	private SesameConnection connection;

	private SesameOntology ontology;
	
	private SesameDaoManager daoManager;
	
	private XMURIFactory uriFactory;
	
	private boolean reasoningOn = false;
	
	public SesameSession(
			SesameOntology itsOntology, 
			SesameConnection connection,
			XMURIFactory uriFactory) throws RepositoryException {
		this.ontology = itsOntology;
		this.connection = connection;
		
		daoManager = SesameDaoManager.getInstance(this);
		
		this.uriFactory = uriFactory;
		
		repositoryConnection = this.ontology.getRepository().getConnection();
	}
	
	/**
	 * @deprecated
	 */
	public void reconnect(IKbConnection connection) throws DatasourceException {}
	
	@Override
	protected void finalize() throws Throwable {
		
		close();
		
		super.finalize();
	}
	
	public ITransaction beginTransaction() throws Exception {
		
		synchronized (transactionMutex) {
			if (currentTransaction != null)
				throw new Exception("A transaction is already active.");
			else {
				currentTransaction = new SesameTransaction(repositoryConnection, this);
				currentTransaction.begin();
			}
		} 
		
		return currentTransaction;
	}

	protected void closeTransaction() {
		
		synchronized (transactionMutex) {
			currentTransaction = null;
		}
	}
	
	public void close() throws DatasourceException {
		
		closeTransaction();
		
		try {
			if (repositoryConnection != null && repositoryConnection.isOpen())
				repositoryConnection.close();
		} catch (RepositoryException e) {
			throw new DatasourceException("Cannot close underlying sesame connection.", e);
		}
	}

	
	public void disconnect() throws DatasourceException {}

	public IKbConnection getConnection() {
		return connection;
	}

	protected SesameOntology getSesameOntology() {
		return ontology;
	}
	
	public IOntology getOntology() {
		return ontology;
	}

	public ITransaction getTransaction() {
		synchronized (transactionMutex) {
			return currentTransaction;
		}
	}

	public boolean isConnected() {
		return true;
	}

	public boolean isOpen() {
		return true;
	}
	
	protected ValueFactory getValueFactory() {
		
		return ontology.getRepository().getValueFactory();
		
	}
	
	public static boolean isReficationContext(Resource aContext) {
		
		return (aContext != null && aContext.toString().startsWith(REIFICATION_NS));
		
	}
	
	/**
	 * 
	 * @param aStatement
	 * @return the reified context
	 * @throws RepositoryException
	 */
	protected String insert(Statement aStatement) throws RepositoryException {
		
		String reifiedStatement = "";
					
		if (!ontology.isReificationEnabled()) {
			repositoryConnection.add(aStatement);
		} else {
			boolean statementExist = false;
			reifiedStatement = uriFactory.getUri(REIFICATION_NS, REIFICATION_FRAGMET_PREFIX);
			
			synchronized (ontology.getReificationMutex()) {
				if (repositoryConnection.hasStatement(aStatement, false)) {
					// The statements already exists: get its reification uri (if any)
					List<Statement> existings = getStatements(
							aStatement.getSubject(), 
							aStatement.getPredicate(), 
							aStatement.getObject(), true);

					for (Statement statementTmp : existings) {
						Resource itsContext = statementTmp.getContext();
						if (isReficationContext(itsContext)) {
							reifiedStatement = itsContext.toString();
							statementExist = true;
							break;
						}
					}
				}
				
				if (!statementExist)
					repositoryConnection.add(aStatement, getValueFactory().createURI(reifiedStatement));
			}
		}

		return reifiedStatement;
	}
	
	protected void delete(Statement aStatement) throws RepositoryException {
		
		repositoryConnection.remove(aStatement);
		
	}
	
	protected List<Statement> getStatementsInContext(String aContext) throws RepositoryException {
		
		return repositoryConnection.getStatements(null, null, null, false, getValueFactory().createURI(aContext)).asList();
	}
	
	protected List<Statement> getStatements(Resource subj, URI pred, Value obj, boolean includeInferred) throws RepositoryException {
		
		List<Statement> statements = new ArrayList<Statement>();
		List<Statement> unsortedStatements = repositoryConnection.getStatements(subj, pred, obj, includeInferred).asList();
		
		/**
		 * It seems (2 February 2008) that Sesame 2 (final) is affected by a bug:
		 * http://www.openrdf.org/forum/mvnforum/viewthread?thread=1584
		 * 
		 * Basically, when statements whose predicate belongs to RDF or RDFS spec and which
		 * are added to a context, are retrieved by means of getStatements, they appear
		 * both with the expected context and with the NULL context.
		 * 
		 * The following workaround will remove statements with NULL context if they also appear
		 * with a non-NULL context.
		 * 
		 */
		List<Statement> statementsInDefaultContext = new ArrayList<Statement>();
		for (Statement statement : unsortedStatements) {
			if (statement.getContext() == null)
				statementsInDefaultContext.add(statement);
			else
				statements.add(statement);
		}
		
		for (Statement statement : statementsInDefaultContext) {
			if (!statements.contains(statement))
				statements.add(statement);
		}
		
		return statements;
	}
	
	protected List<Statement> getStatementsWithSubject(Resource aSubject, boolean includeInferred) throws RepositoryException {
		
		return getStatements(aSubject, null, null, includeInferred);
		
	}
	
	protected List<Statement> getAllStatements(boolean includeInferred) throws RepositoryException {
		
		return getStatements(null, null, null, includeInferred);
		
	}

	public IDaoManager getDaoManager() {
		return daoManager;
	}

	public boolean isReasoningOn() {
		return reasoningOn;
	}

	public void setReasoningOn(boolean on) {
		this.reasoningOn = on;
	}

	protected void addProvenance(String reification, IProvenance itsProvenance) throws Exception {
		
		URI theProvenance = getValueFactory().createURI(uriFactory.getUriWithNamespace(PROVENANCE_NS)); 
		
		repositoryConnection.add(
				getValueFactory().createStatement(
						getValueFactory().createURI(reification), 
						getValueFactory().createURI(MetaVocabulary.HAS_PROVENANCE), 
						theProvenance));
		
		if (itsProvenance.getAgent() != null) {
			repositoryConnection.add(
					getValueFactory().createStatement(
							theProvenance, 
							getValueFactory().createURI(MetaVocabulary.AGENT), 
							AK2Ses.getResource(itsProvenance.getAgent(), getValueFactory())));
		}
		
		if (itsProvenance.getSource() != null) {
			repositoryConnection.add(
					getValueFactory().createStatement(
							theProvenance, 
							getValueFactory().createURI(MetaVocabulary.SOURCE), 
							AK2Ses.getResource(itsProvenance.getSource(), getValueFactory())));
		}
		
		if (itsProvenance.getConfidenceDegree() > 0) {
			repositoryConnection.add(
					getValueFactory().createStatement(
							theProvenance, 
							getValueFactory().createURI(MetaVocabulary.CONFIDENCE_DEGREE), 
							getValueFactory().createLiteral(itsProvenance.getConfidenceDegree())));
		}
		
		if (itsProvenance.getCreationDate() != null) {
			repositoryConnection.add(
					getValueFactory().createStatement(
							theProvenance, 
							getValueFactory().createURI(MetaVocabulary.CREATION_TIME), 
							getValueFactory().createLiteral(Long.toString(itsProvenance.getCreationDate().getTime()))));
		}
		
	}
	
	protected void addProvenance(List<String> reifications, IProvenance itsProvenance) throws Exception {
		
		URI theProvenance = getValueFactory().createURI(uriFactory.getUriWithNamespace(PROVENANCE_NS)); 
	
		for (String reification : reifications) {
			repositoryConnection.add(
							getValueFactory().createStatement(
									getValueFactory().createURI(reification), 
									getValueFactory().createURI(MetaVocabulary.HAS_PROVENANCE), 
									theProvenance));
		}
		
		if (itsProvenance.getAgent() != null) {
			repositoryConnection.add(
					getValueFactory().createStatement(
							theProvenance, 
							getValueFactory().createURI(MetaVocabulary.AGENT), 
							AK2Ses.getResource(itsProvenance.getAgent(), getValueFactory())));
		}
		
		if (itsProvenance.getSource() != null) {
			repositoryConnection.add(
					getValueFactory().createStatement(
							theProvenance, 
							getValueFactory().createURI(MetaVocabulary.SOURCE), 
							AK2Ses.getResource(itsProvenance.getSource(), getValueFactory())));
		}
		
		if (itsProvenance.getConfidenceDegree() > 0) {
			repositoryConnection.add(
					getValueFactory().createStatement(
							theProvenance, 
							getValueFactory().createURI(MetaVocabulary.CONFIDENCE_DEGREE), 
							getValueFactory().createLiteral(itsProvenance.getConfidenceDegree())));
		}
		
		if (itsProvenance.getCreationDate() != null) {
			repositoryConnection.add(
					getValueFactory().createStatement(
							theProvenance, 
							getValueFactory().createURI(MetaVocabulary.CREATION_TIME), 
							getValueFactory().createLiteral(Long.toString(itsProvenance.getCreationDate().getTime()))));
		}
		
	}
	
	protected void delete(List<Statement> statements) throws RepositoryException {
		
		repositoryConnection.remove(statements);
		
	}

	public RepositoryConnection getRepositoryConnection() {
		
		try {
			if(repositoryConnection == null ||!repositoryConnection.isOpen()){
				repositoryConnection = ontology.getRepository().getConnection();
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		return repositoryConnection;
	}

}
