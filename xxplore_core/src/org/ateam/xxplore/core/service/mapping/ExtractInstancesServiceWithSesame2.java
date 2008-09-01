package org.ateam.xxplore.core.service.mapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
import org.xmedia.accessknow.sesame.model.PropertyMember;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class ExtractInstancesServiceWithSesame2 {

	private static Logger s_log = Logger.getLogger(ExtractInstancesServiceWithSesame2.class);
			
	private BufferedReader br;
	
	private BufferedWriter bw;
	private RDFXMLWriter writer;
	
	
	public ExtractInstancesServiceWithSesame2(String schemaMappingFile, String extractedEntities) {
		new File(extractedEntities).getParentFile().mkdirs();
		try {
			br = new BufferedReader(new FileReader(schemaMappingFile));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(extractedEntities),"UTF-8"));
			writer = new RDFXMLWriter(bw);
			writer.startRDF();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void extractInstances() {
		IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
		IIndividualDao individualDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		
		String line;
			try {
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
}
