package org.xmedia.oms.model.api;

import java.io.Reader;
import java.io.Writer;

import org.xmedia.oms.persistence.IDataSource;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;



public interface IOntology extends IDataSource{
	
	public static final String TRIX_LANGUAGE = "http://www.x-media-project/rdf/serialization/languages/trix";
	public static final String RDF_XML_LANGUAGE = "http://www.x-media-project/rdf/serialization/languages/rdf_xml";
	public static final String N3_LANGUAGE = "http://www.x-media-project/rdf/serialization/languages/n3";
	
	public static final String RDFS_EXPRESSIVITY = "http://www.x-media-project/rdf/ontology/languages/rdfs";
	public static final String OWL_EXPRESSIVITY = "http://www.x-media-project/rdf/ontology/languages/owl";
	
	public String getUri();
	
	public Object createReasoner();
	
	/**
	 * 
	 * @return all classes (the schema, the T-Box) as a subsumption hierarchy
	 */
	public IHierarchicalSchema getHierarchicalSchema();
	
	public ISchema getSchema();
	
	public void export(String language, Writer writer) throws OntologyExportException;
	
	public void importOntology(String language, String baseUri, Reader reader) throws OntologyImportException;
	
	public INamedConcept createNamedConcept(String itsUri);
	
	public INamedIndividual createNamedIndividual(String itsUri);
	
	public IIndividual createIndividual();
	
	public ILiteral createLiteral(Object itsValue, String itsDatatype, String itsLanguage);
	
	public ILiteral createStringLiteral(String itsValue, String itsLanguage);

	public IProperty createProperty(String itsUri);
	
	public IObjectProperty createObjectProperty(String itsUri);
	
	public IDataProperty createDataProperty(String itsUri);
	
	public IIndividual createIndividual(String label);
	
	public String getExpressivity();
	
	public void setExpressivity(String expressivity);
	
	public int getNumberOfConcept();
	
	public int getNumberOfIndividual();
	
	public int getNumberOfObjectProperty();
	
	public int getNumberOfObjectPropertyMember();
	
	public int getNumberOfDataProperty();
	
	public int getNumberOfDataPropertyMember();

	/**
	 * @deprecated Use instead {@link IPropertyMemberAxiomDao#insert(INamedIndividual, IProperty, IResource)}
	 * 
	 * @param subject
	 * @param property
	 * @param object
	 * @return
	 */
	@Deprecated
	public IPropertyMember createPropertyMember(INamedIndividual subject, IProperty property, IResource object);
}
