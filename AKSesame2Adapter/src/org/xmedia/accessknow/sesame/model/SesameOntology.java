package org.xmedia.accessknow.sesame.model;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import org.aifb.xxplore.shared.vocabulary.XMLSchema;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.xmedia.accessknow.sesame.persistence.ConceptDao;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.IndividualDao;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRemoteRepositoryHandle;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.api.ISchema;
import org.xmedia.oms.model.api.OntologyExportException;
import org.xmedia.oms.model.api.OntologyImportException;
import org.xmedia.oms.model.impl.Datatype;
import org.xmedia.oms.model.impl.Individual;
import org.xmedia.oms.persistence.IMessageListener;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.oms.persistence.dao.QueryEvaluatorUnavailableException;
import org.xmedia.oms.query.IQueryEvaluator;


public class SesameOntology implements IOntology {

	public static final boolean REIFICATION_ENABLED_DEFAULT = true;
	
	private URI uri = null;
	private Repository repository = null;
	private SesameConnection connection = null;
	
	private SesameSparqlEvaluator theSparqlEvaluator = null;
	private SesameSparqlEvaluator2perf sparqlPerfEvaluator = null;
	private SesameSerqlEvaluator theSerqlEvaluator = null;
	
	private boolean reificationEnabled = true;
	private Boolean reificationMutex = new Boolean(true);
	private String expressivity = IOntology.RDFS_EXPRESSIVITY;;
	
	private int m_noConcept = -1;
	private int m_noOp = -1;
	private int m_noOpm = -1;
	private int m_noInd = -1;
	
	public SesameOntology(
			Repository itsRepository, 
			URI itsUri, 
			SesameConnection connection,
			boolean reificationEnabled) throws RepositoryException {
		
		this.repository = itsRepository;
		this.uri = itsUri;
		this.connection = connection;
		
		theSparqlEvaluator = new SesameSparqlEvaluator(itsRepository, this);
		sparqlPerfEvaluator = new SesameSparqlEvaluator2perf(itsRepository, this);
		theSerqlEvaluator = new SesameSerqlEvaluator(itsRepository, this);
		
		this.reificationEnabled = reificationEnabled;

	}

	public void shutdown() throws RepositoryException {
		
		if (repository != null) {
			repository.shutDown();
		}
		
	}
	
	protected SesameConnection getConnection(){
		return this.connection;
	}
	
	public Boolean getReificationMutex() {
		return reificationMutex;
	}
	
	/**
	 * @deprecated
	 */
	@Deprecated
	public Object createReasoner()  {
		return null;
	}

	public IHierarchicalSchema getHierarchicalSchema() {
		return new HierachicalSchema(this);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public ISchema getSchema() {
		return null;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public long getID() {
		return 0;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void addMessageListener(IMessageListener listener) {}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void removeMessageListener(IMessageListener listener) {}
	
	public String getUri() {
		return uri.toString();
	}
	
	public String getName() {
		return getUri();
	}

	public Repository getRepository() {
		return repository;
	}
	
	public Resource findResourceByURI(String uri){
		
		Resource res = null;
		
		try {
			RepositoryConnection con = getRepository().getConnection();
			
			try {
				
				String queryString = "SELECT R "+
									 "FROM {R} rdf:type {rdfs:Resource} "+
									 "WHERE R LIKE \""+uri+"\"";
					
				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SERQL, queryString);
				TupleQueryResult result = tupleQuery.evaluate();
				
				try {
					if (result.hasNext()) {
						res = (Resource)result.next().getValue("R");
					}
				}
				finally {
					result.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (OpenRDFException e) {
			e.printStackTrace();
		}	
		
		return res;
	}
	
	public boolean containsResource(String uri){
		
		return findResourceByURI(uri) != null ? true : false;
	}


	public IIndividual createIndividual() {
		return new Individual(this);
	}
	
	public IIndividual createIndividual(String label) {
		return new Individual(label, this);
	}

	public ILiteral createStringLiteral(String itsValue, String itsLanguage) {
		return createLiteral(itsValue, XMLSchema.STRING, itsLanguage);
	}
	
	public ILiteral createLiteral(Object itsValue, String itsDatatype, String itsLanguage) {
		Literal theLiteral = new Literal(itsValue, this);
		
		if (itsLanguage != null) {
			theLiteral.setLanguage(itsLanguage);
		}
		
		if (itsDatatype != null) {
			theLiteral.addDatatype(new Datatype(itsDatatype, this));
		}
		
		return theLiteral;
	}

	public INamedIndividual createNamedIndividual(String itsUri) {
		return new NamedIndividual(itsUri, this);
	}
	
	public IProperty createProperty(String itsUri) {
		return new Property(itsUri, this);
	}
	
	public INamedConcept createNamedConcept(String itsUri) {
		return new NamedConcept(itsUri, this);
	}
	
	public IObjectProperty createObjectProperty(String itsUri) {
		return new ObjectProperty(itsUri, this);
	}
	
	public IDataProperty createDataProperty(String itsUri) {
		return new DataProperty(itsUri, this);
	}
	
	@Deprecated
	public IPropertyMember createPropertyMember(INamedIndividual subject, IProperty property, IResource object) {
		
		return PropertyMember.createPropertyMember(subject, property, object, this);
		
	}

	public IQueryEvaluator getAvailableEvaluator(int querytype) throws QueryEvaluatorUnavailableException {
		
		if (IDaoManager.SPARQL_QUERYTYPE == querytype) {
			return theSparqlEvaluator;
		}
		if (IDaoManager.SERQL_QUERYTYPE == querytype) {
			return theSerqlEvaluator;
		} else {
			throw new QueryEvaluatorUnavailableException(querytype);
		}
	}

	public IQueryEvaluator getAvailableEvaluatorPerf(int querytype) throws QueryEvaluatorUnavailableException {
		
		if (IDaoManager.SPARQL_QUERYTYPE == querytype) {
			return sparqlPerfEvaluator;
		} else {
			throw new QueryEvaluatorUnavailableException(querytype);
		}
	}
	
	public void export(String language, Writer writer) throws OntologyExportException {
		
		RepositoryConnection sesConnection = null;
		try {
			sesConnection = repository.getConnection();
			sesConnection.export(Rio.createWriter(RDFFormat.forMIMEType(
					getMimeType(language)), 
					writer));
		} catch (RepositoryException e) {
			throw new OntologyExportException(getUri(), language, e);
		} catch (RDFHandlerException e) {
			throw new OntologyExportException(getUri(), language, e);
		} catch (UnsupportedRDFormatException e) {
			throw new OntologyExportException(getUri(), language, e);
		} catch (Exception e) {
			throw new OntologyExportException(getUri(), language, e);
		} finally {
			if (sesConnection != null) {
				try {
					sesConnection.close();
				} catch (RepositoryException e) {
					throw new OntologyExportException(getUri(), language, e);
				}
			}
		}
		
	}
	
public void export(RDFFormat format, Writer writer) throws OntologyExportException {
		
		RepositoryConnection sesConnection = null;
		try {
			sesConnection = repository.getConnection();
			sesConnection.export(Rio.createWriter(format, writer));
		} catch (RepositoryException e) {
			throw new OntologyExportException(getUri(), format.toString(), e);
		} catch (RDFHandlerException e) {
			throw new OntologyExportException(getUri(), format.toString(), e);
		} catch (UnsupportedRDFormatException e) {
			throw new OntologyExportException(getUri(), format.toString(), e);
		} catch (Exception e) {
			throw new OntologyExportException(getUri(), format.toString(), e);
		} finally {
			if (sesConnection != null) {
				try {
					sesConnection.close();
				} catch (RepositoryException e) {
					throw new OntologyExportException(getUri(), format.toString(), e);
				}
			}
		}
		
	}
	

	private String getMimeType(String serializationLanguage) throws Exception {
		
		String itsMimeType = "";

		if (IOntology.RDF_XML_LANGUAGE.equalsIgnoreCase(serializationLanguage)) {
			itsMimeType = "application/rdf+xml";
		} else
			if (IOntology.TRIX_LANGUAGE.equalsIgnoreCase(serializationLanguage)) {
				itsMimeType = "application/trix";
			} else
				if (IOntology.N3_LANGUAGE.equalsIgnoreCase(serializationLanguage)) {
					itsMimeType = "text/rdf+n3";
				} else {
					throw new Exception("Serialization language '" + serializationLanguage + "' not supported.");
				}

		return itsMimeType;
	}

	public void importOntology(String language, String baseUri, Reader reader) throws OntologyImportException {
		
		RepositoryConnection sesConnection = null;
		try {
			sesConnection = repository.getConnection(); 
			sesConnection.add(reader, baseUri, RDFFormat.forMIMEType(getMimeType(language)));
		} catch (RDFParseException e) {
			throw new OntologyImportException(getUri(), language);
		} catch (RepositoryException e) {
			throw new OntologyImportException(getUri(), language);
		} catch (IOException e) {
			throw new OntologyImportException(getUri(), language);
		} catch (Exception e) {
			throw new OntologyImportException(getUri(), language);
		} finally {
			if (sesConnection != null) {
				try {
					sesConnection.close();
				} catch (RepositoryException e) {
					throw new OntologyImportException(getUri(), language);
				}
			}
		}
		
	}

	public boolean isReificationEnabled() {
		return reificationEnabled;
	}
	
	public String getExpressivity() {		
		return this.expressivity;
	}

	public void setExpressivity(String expressivity) {
		this.expressivity = expressivity;		
	}

	public int getNumberOfConcept() {
		if (m_noConcept != -1) return m_noConcept;
		
		int count = 0;
		IDaoManager dao_manager = PersistenceUtil.getDaoManager();
		
		if(dao_manager instanceof ExtendedSesameDaoManager){
			
			ConceptDao conceptDao = (ConceptDao)((ExtendedSesameDaoManager)dao_manager).getAvailableDao(ConceptDao.class);
			count = conceptDao.findAll().size();
			m_noConcept = count;
			return count;
		}
		else{
			return count;
		}
	}

//	not used
	public int getNumberOfDataProperty() {
		// TODO Auto-generated method stub
		return 0;
	}

//	not used
	public int getNumberOfDataPropertyMember() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberOfIndividual() {
		if (m_noInd != -1) return m_noInd;
		
		int count = 0;
		IDaoManager dao_manager = PersistenceUtil.getDaoManager();
		
		if(dao_manager instanceof ExtendedSesameDaoManager){
			
			IndividualDao individualDao = (IndividualDao)((ExtendedSesameDaoManager)dao_manager).getAvailableDao(IndividualDao.class);
			count = individualDao.findAll().size();
			m_noInd = count;
			return count;
		}
		else{
			return count;
		}
	}

	public int getNumberOfObjectProperty() {
		if (m_noOp != -1) return m_noOp;		
		int count = 0;
		
		try {
			
			RepositoryConnection con = repository.getConnection();
			RepositoryResult<Statement> results;

			results = con.getStatements(null, RDF.TYPE, RDF.PROPERTY, false);

			while(results.hasNext()){

				Statement stmt = results.next();
				Resource subject = stmt.getSubject();

				if(subject instanceof org.openrdf.model.URI){
					count++;
				}
			}
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		m_noOp = count;
		return count;
	}

	public int getNumberOfObjectPropertyMember() {
		if (m_noOpm!= -1) return m_noOpm;
		
		int count = 0;
		IDaoManager dao_manager = PersistenceUtil.getDaoManager();
		
		if(dao_manager instanceof ExtendedSesameDaoManager){
			
			IPropertyMemberAxiomDao propertyMemberDao = ((ExtendedSesameDaoManager)dao_manager).getPropertyMemberDao();
			count = propertyMemberDao.findAll().size();
//			count = 10000;
			m_noOpm = count;
			return count;
		}
		else{
			return count;
		}
	}

}
