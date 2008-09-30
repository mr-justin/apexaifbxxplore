package org.ateam.xxplore.core.service.mapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.kaon2.api.logic.Constant;
import org.semanticweb.kaon2.api.owl.elements.DataPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectPropertyExpression;
import org.xmedia.accessknow.sesame.model.NamedIndividual;
import org.xmedia.accessknow.sesame.model.PropertyMember;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;


public class ExtractInstancesService {

	private static Logger s_log = Logger.getLogger(ExtractInstancesService.class);

	private static final String DUMMY_INSTANCE_ONTOLOGY_URI = "file:src/instance/";
	public ExtractInstancesService() {}

	public void extractInstances(String schemaMappingFile, String extractedEntitiesFile, IDaoManager man) {
		IConceptDao conceptDao = (IConceptDao) man.getAvailableDao(IConceptDao.class);
		IIndividualDao individualDao = (IIndividualDao) man.getAvailableDao(IIndividualDao.class);
		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)man.getAvailableDao(IPropertyMemberAxiomDao.class);
		String line;


		new File(extractedEntitiesFile).getParentFile().mkdirs();
		try {
			BufferedReader br = new BufferedReader(new FileReader(schemaMappingFile));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(extractedEntitiesFile),"UTF-8"));
			RDFXMLWriter writer = new RDFXMLWriter(bw);
			writer.startRDF();
			while ((line = br.readLine()) != null) {
				String[] mapping = line.split(";");
				if (mapping.length == 3) {
					INamedConcept concept = conceptDao.findByUri(mapping[0]);
					if(concept != null){
						URI conceptUri = new URIImpl(concept.getUri());
						Set<IIndividual> individuals = individualDao.findMemberIndividuals(concept); 
						for(IIndividual individual : individuals){
							if(individual instanceof INamedIndividual) { 
								URI subjectUri = new URIImpl(((INamedIndividual)individual).getUri());
								writer.handleStatement(new StatementImpl(subjectUri, RDF.TYPE, conceptUri));

								Set<IPropertyMember> propMems = propertyMemberDao.findBySourceIndividual(individual);
								for(IPropertyMember propMem : propMems) {
									if(propMem.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
										IProperty dataProperty = propMem.getProperty();
										URI propertyUri = new URIImpl(dataProperty.getUri());
										IResource object = propMem.getTarget();
										if(object instanceof ILiteral) {
											LiteralImpl literal = new LiteralImpl(((ILiteral)object).getLiteral());
											writer.handleStatement(new StatementImpl(subjectUri, propertyUri, literal));
										}
									} else {
										IProperty objectProperty = propMem.getProperty();
										URI propertyUri = new URIImpl(objectProperty.getUri());
										IResource object = propMem.getTarget();
										if(object instanceof IIndividual) {
											if(object instanceof INamedIndividual) { 
												URI objectUri = new URIImpl(((INamedIndividual)object).getUri());
												writer.handleStatement(new StatementImpl(subjectUri, propertyUri, objectUri));
											} else {
												String objectNodeId = object.getLabel();
												if(objectNodeId.startsWith("_:")) {
													objectNodeId = objectNodeId.substring(2);
												}
												BNodeImpl objectBNode = new BNodeImpl(objectNodeId); 
												writer.handleStatement(new StatementImpl(subjectUri, propertyUri, objectBNode));
											}		
										}
									}
								}
							} else {
								String subjectNodeId = individual.getLabel();
								if(subjectNodeId.startsWith("_:")) {
									subjectNodeId = subjectNodeId.substring(2);
								}
								BNodeImpl subjectBNode = new BNodeImpl(subjectNodeId); 
								writer.handleStatement(new StatementImpl(subjectBNode, RDF.TYPE, conceptUri));

								Set<IPropertyMember> propMems = propertyMemberDao.findBySourceIndividual(individual);
								for(IPropertyMember propMem : propMems) {
									if(propMem.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
										IProperty dataProperty = propMem.getProperty();
										URI propertyUri = new URIImpl(dataProperty.getUri());
										IResource object = propMem.getTarget();
										if(object instanceof ILiteral) {
											LiteralImpl literal = new LiteralImpl(((ILiteral)object).getLiteral());
											writer.handleStatement(new StatementImpl(subjectBNode, propertyUri, literal));
										}
									} else {
										IProperty objectProperty = propMem.getProperty();
										URI propertyUri = new URIImpl(objectProperty.getUri());
										IResource object = propMem.getTarget();
										if(object instanceof IIndividual) {
											if(object instanceof INamedIndividual) { 
												URI objectUri = new URIImpl(((INamedIndividual)object).getUri());
												writer.handleStatement(new StatementImpl(subjectBNode, propertyUri, objectUri));
											} else {
												String objectNodeId = object.getLabel();
												if(objectNodeId.startsWith("_:")) {
													objectNodeId = objectNodeId.substring(2);
												}
												BNodeImpl objectBNode = new BNodeImpl(objectNodeId); 
												writer.handleStatement(new StatementImpl(subjectBNode, propertyUri, objectBNode));
											}		
										}
									}
								}	
							}	
						}
					}
				}
			}
			writer.endRDF();
			if(bw != null)
				bw.close();
			if(br != null)
				br.close();
		} catch (DatasourceException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (RDFHandlerException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}


	public Ontology extractInstances(String namedconcept, IDaoManager man) {
		IConceptDao conceptDao = (IConceptDao) man.getAvailableDao(IConceptDao.class);
		IIndividualDao individualDao = (IIndividualDao) man.getAvailableDao(IIndividualDao.class);
		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)man.getAvailableDao(IPropertyMemberAxiomDao.class);
		OntologyManager ontoMan = null;
		Ontology ontology = null;
		try {
			ontoMan = KAON2Manager.newOntologyManager();
			ontology=ontoMan.createOntology(DUMMY_INSTANCE_ONTOLOGY_URI+namedconcept,new HashMap<String,Object>());

			INamedConcept concept = conceptDao.findByUri(namedconcept);
			//is not a named concept
			if (concept == null) return null;
			
			OWLClass kaon2Con =KAON2Manager.factory().owlClass(concept.getUri());
			List<OntologyChangeEvent> changes=new ArrayList<OntologyChangeEvent>();

			if(concept != null){
				Set<IIndividual> individuals = individualDao.findMemberIndividuals(concept); 
				for(IIndividual individual : individuals){
					if(individual instanceof INamedIndividual) { 
						Individual kaon2In =KAON2Manager.factory().individual(((NamedIndividual)individual).getUri());
						changes.add(new OntologyChangeEvent(KAON2Manager.factory().classMember(
								kaon2Con,  kaon2In), OntologyChangeEvent.ChangeType.ADD));

						Set<IPropertyMember> propMems = propertyMemberDao.findBySourceIndividual(individual);
						for(IPropertyMember propMem : propMems) {
							if(propMem.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
								DataPropertyExpression kaon2_dp = KAON2Manager.factory().dataProperty(propMem.getProperty().getUri());
								Constant kaon2_ob = KAON2Manager.factory().constant(propMem.getTarget().getDelegate());
								changes.add(new OntologyChangeEvent(KAON2Manager.factory().dataPropertyMember(
										kaon2_dp,  kaon2In, kaon2_ob), OntologyChangeEvent.ChangeType.ADD));

							} 
							else {
								IResource object = propMem.getTarget();
								if (object instanceof INamedIndividual){
									ObjectPropertyExpression kaon2_dp = KAON2Manager.factory().objectProperty(propMem.getProperty().getUri());
									Individual kaon2_ob = KAON2Manager.factory().individual(((INamedIndividual)object).getUri());
									changes.add(new OntologyChangeEvent(KAON2Manager.factory().objectPropertyMember(
											kaon2_dp,  kaon2In, kaon2_ob), OntologyChangeEvent.ChangeType.ADD));								
								}
							}
						}
					} 
				}
			}
			ontology.applyChanges(changes);
			ontoMan.close();

		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ontology;

	}
}
