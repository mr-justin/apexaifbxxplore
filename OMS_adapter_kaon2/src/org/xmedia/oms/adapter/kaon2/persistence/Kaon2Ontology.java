package org.xmedia.oms.adapter.kaon2.persistence;


import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.aifb.xxplore.shared.exception.ExploreRuntimeException;
import org.apache.log4j.Logger;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.Request;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.semanticweb.kaon2.api.reasoner.Reasoner;
import org.semanticweb.kaon2.api.reasoner.SubsumptionHierarchy;
import org.xmedia.oms.adapter.kaon2.model.XHierarchicalSchema;
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
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.persistence.AbstractDataSource;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;




public class Kaon2Ontology extends AbstractDataSource implements IOntology {

    public static final String OWL_RDF="OWL/RDF";
    /** The name of the format for OWL XML. */
    public static final String OWL_XML="OWL/XML";
    /** The name of the format for OWL 1.1 XML. */
    public static final String OWL_1_1_XML="OWL1.1/XML";
    
    
	/** the URI of the data source*/
	private String m_uri;

	/** the ontology if opened*/
	private Ontology m_delegate;

	/** the cache subsumption hieararchy **/
	private SubsumptionHierarchy m_hierarchy;
	
	private static Logger s_log = Logger.getLogger(Kaon2Ontology.class);

	public Kaon2Ontology(long id, Ontology delegate) {
		super(id);
		m_delegate = delegate;
		m_uri = delegate.getOntologyURI();
	}

	public String getName() {
		return m_uri;
	}

	public String getUri() {

		return m_uri;
	}

	/**
	 * 
	 * @return the kaon2 delegate of this ontology
	 */
	public Ontology getDelegate (){
		return m_delegate;
	}

	public IHierarchicalSchema getHierarchicalSchema() {

		// TODO Auto-generated method stub
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Reasoner reasoner = (Reasoner)session.getReasoner();

		if (s_log.isDebugEnabled()) s_log.debug(
				"Get hierarchical scheme of following ontology: " + session.getOntology().getUri());
		
		if (m_hierarchy == null){
			try {

				m_hierarchy = reasoner.getSubsumptionHierarchy();
				if (s_log.isDebugEnabled()) s_log.debug(
						"The computed hiararchical schema: " + m_hierarchy.toString());

			}
			catch (Exception e) {

				s_log.error("Exception " + e + " occurred while trying to get hieararchical schema");
				//TODO exception handling
			} 

			finally {session.freeReasoner(reasoner);}
		}

		return new XHierarchicalSchema(m_hierarchy, this);
	}

	public ISchema getSchema() {
		//TODO implement!!
		return null;
	}

	public Object createReasoner() {
		if(m_delegate != null){
			try {
				return m_delegate.createReasoner();
			} catch (KAON2Exception e) {
				s_log.error("Exception " + e + " occurred while trying to get create reasoner");
				// TODO Auto-generated catch block
			}
		}
		
		return null;
	}

	public int getNumberOfConcept() {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false){

			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

			if (s_log.isDebugEnabled()) s_log.debug("find all Named Concept of the ontology: " + session.getOntology().getUri());

			try {
				Request<OWLClass> clazzRequest = onto.getDelegate().createEntityRequest(OWLClass.class);	
				return clazzRequest.sizeAll();	


			} catch (KAON2Exception e) {

				throw new DatasourceException(e);
			}
		}
		return 0;
	}
	
	public int getNumberOfIndividual() {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false){

			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

			if (s_log.isDebugEnabled()) s_log.debug("find all Named Concept of the ontology: " + session.getOntology().getUri());

			try {
				Request<Individual> indRequest = onto.getDelegate().createEntityRequest(Individual.class);	
				return indRequest.sizeAll();	


			} catch (KAON2Exception e) {

				throw new DatasourceException(e);
			}
		}
		return 0;
	}
	
	public int getNumberOfObjectProperty() {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false){

			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

			if (s_log.isDebugEnabled()) s_log.debug("find all Named Concept of the ontology: " + session.getOntology().getUri());

			try {
				Request<ObjectProperty> oPropRequest = onto.getDelegate().createEntityRequest(ObjectProperty.class);
				return oPropRequest.sizeAll();	


			} catch (KAON2Exception e) {

				throw new DatasourceException(e);
			}
		}
		return 0;
	}
	
	public int getNumberOfObjectPropertyMember() {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false){

			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

			if (s_log.isDebugEnabled()) s_log.debug("find all Named Concept of the ontology: " + session.getOntology().getUri());

			try {
				Request<ObjectPropertyMember> oPropMemberRequest = onto.getDelegate().createAxiomRequest(ObjectPropertyMember.class);
				return oPropMemberRequest.sizeAll();	


			} catch (KAON2Exception e) {

				throw new DatasourceException(e);
			}
		}
		return 0;
	}
	
	public int getNumberOfDataProperty() {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false){

			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

			if (s_log.isDebugEnabled()) s_log.debug("find all Named Concept of the ontology: " + session.getOntology().getUri());

			try {
				Request<DataPropertyMember> dPropMemberRequest = onto.getDelegate().createAxiomRequest(DataPropertyMember.class);	
				return dPropMemberRequest.sizeAll();	


			} catch (KAON2Exception e) {

				throw new DatasourceException(e);
			}
		}
		return 0;
	}
	
	public int getNumberOfDataPropertyMember() {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false){

			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

			if (s_log.isDebugEnabled()) s_log.debug("find all Named Concept of the ontology: " + session.getOntology().getUri());

			try {
				Request<DataProperty> dPropRequest = onto.getDelegate().createEntityRequest(DataProperty.class);		
				return dPropRequest.sizeAll();	


			} catch (KAON2Exception e) {

				throw new DatasourceException(e);
			}
		}
		return 0;
	}
	
	public void export(String format, Writer writer) throws OntologyExportException {
		try {
			m_delegate.saveOntology(format, writer , KbEnvironment.DEFAULT_ONTOLOGY_ENCODING);
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void importOntology(String language, String baseUri, Reader reader) throws OntologyImportException {
		// TODO Auto-generated method stub
		
	}

	public IIndividual createIndividual() {
		// TODO Auto-generated method stub
		return null;
	}

	public ILiteral createLiteral(Object itsValue, String itsDatatype, String itsLanguage) {
		// TODO Auto-generated method stub
		return null;
	}

	public INamedIndividual createNamedIndividual(String itsUri) {
		// TODO Auto-generated method stub
		return null;
	}

	public IProperty createProperty(String itsUri) {
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertyMember createPropertyMember(INamedIndividual subject, IProperty property, IResource object) {
		// TODO Auto-generated method stub
		return null;
	}

	public ILiteral createStringLiteral(String itsValue, String itsLanguage) {
		// TODO Auto-generated method stub
		return null;
	}

	public IDataProperty createDataProperty(String itsUri) {
		// TODO Auto-generated method stub
		return null;
	}

	public IIndividual createIndividual(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	public INamedConcept createNamedConcept(String itsUri) {
		// TODO Auto-generated method stub
		return null;
	}

	public IObjectProperty createObjectProperty(String itsUri) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getExpressivity() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setExpressivity(String expressivity) {
		// TODO Auto-generated method stub
		
	}
}
