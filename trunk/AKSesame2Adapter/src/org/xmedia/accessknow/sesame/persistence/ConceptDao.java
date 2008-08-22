package org.xmedia.accessknow.sesame.persistence;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;
import org.xmedia.accessknow.sesame.persistence.converter.AK2Ses;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.dao.IConceptDao;


public class ConceptDao implements IConceptDao {

	SesameSession session;
	
	public ConceptDao(SesameSession session) {
		this.session = session;
	}

	public Set<INamedConcept> findConceptRanges(IObjectProperty property)
			throws DatasourceException {
		try {
			
			Set<INamedConcept> ranges = new HashSet<INamedConcept>();

			RepositoryResult<Statement> stmts = session.getRepositoryConnection().getStatements(
					AK2Ses.getProperty(property, 
							session.getValueFactory()), 
					RDFS.RANGE, null, true);
			INamedConcept cur;
			Statement stmt;
			try {
				while(stmts.hasNext()) {
					stmt = stmts.next();
					if(stmt.getObject() instanceof URI) {
						cur = Ses2AK.getNamedConcept((URI)stmt.getObject(), session.getOntology());
						ranges.add(cur);
					}
				}
			} finally {
				stmts.close();
			}
			return ranges;
		} catch(Exception e) {
			throw new DatasourceException("Error occurred while retrieving all concept ranges of the property "+property.getUri(), e);
		}
	}

	public Set<IConcept> findDisjointConcepts(INamedConcept concept)
			throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<INamedConcept> findDomains(IProperty property)
			throws DatasourceException {
		try {
			Set<INamedConcept> domains = new HashSet<INamedConcept>();
			RepositoryConnection conn = session.getRepositoryConnection();
			RepositoryResult<Statement> stmts = conn.getStatements(
					AK2Ses.getProperty(property, 
							session.getValueFactory()), 
					RDFS.DOMAIN, null, session.isReasoningOn());
			INamedConcept cur;
			Statement stmt;
			try {
				while(stmts.hasNext()) {
					stmt = stmts.next();
					if(stmt.getObject() instanceof URI) {
						cur = Ses2AK.getNamedConcept((URI)stmt.getObject(), session.getOntology());
						domains.add(cur);
					}
				}
			} finally {
				stmts.close();
			}
			return domains;
		} catch(Exception e) {
			throw new DatasourceException("Error occurred while retrieving all domains of the property "+property.getUri(), e);
		}
	}
	
	public Set<IConcept> findEquivalentConcepts(INamedConcept concept)
			throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IConcept> findSubconcepts(INamedConcept concept)
			throws DatasourceException {
		
		return findSubconcepts(concept, session.isReasoningOn());
	}

	public Set<IConcept> findSuperconcepts(INamedConcept concept)
			throws DatasourceException {
		return findSuperconcepts(concept, session.isReasoningOn());
	}

	public Set<IConcept> findTypes(IIndividual individual)
			throws DatasourceException {
		try {
			RepositoryConnection conn = session.getRepositoryConnection();
			Resource sesInd = AK2Ses.getResource(individual, session.getValueFactory());
			INamedConcept concept;
			Set<IConcept> types = new HashSet<IConcept>();
			RepositoryResult<Statement> stmts = conn.getStatements(sesInd, RDF.TYPE, null, session.isReasoningOn());
			Statement stmt;
			
			try {
				while(stmts.hasNext()) {
					
					stmt = stmts.next();
					
					if(stmt.getObject() instanceof URI) {

						URI object = (URI)stmt.getObject();

						if(!object.equals(RDFS.CLASS) && !object.equals(new URIImpl("http://www.w3.org/2002/07/owl#Class"))){

							concept = Ses2AK.getNamedConcept((Resource)stmt.getObject(), session.getOntology());
							if(concept != null) types.add(concept);
							
						}
					}
				}
			} finally {
				stmts.close();
			}
			return types;
		} 
		catch(Exception e) {
			throw new DatasourceException("Error occurred while retrieving all types of an individual with a uri "+individual.getLabel(), e);
		}
	}

	
	public INamedConcept findByUri(String uri) throws DatasourceException {
		try {
			RepositoryConnection conn = session.getRepositoryConnection();
			
			
			//TODO: should not use create but search for entity that has this URI?
			RepositoryResult<Statement> sesResult = conn.getStatements(
					session.getValueFactory().createURI(uri), 
					RDF.TYPE, null, session.isReasoningOn());
			INamedConcept concept = null;
			try {
				if(sesResult.hasNext()) {
					concept = Ses2AK.getNamedConcept(sesResult.next().getSubject(), session.getOntology()); 
				} 
			} finally {
				sesResult.close();
			}
			return concept;
		} catch (RepositoryException e) {
			throw new DatasourceException("Error occurred while retrieving a concept with a uri "+uri, e);
		}
	}
	
	public String findLabel(INamedConcept concept) throws DatasourceException{
		
		try {
		RepositoryConnection conn = session.getRepositoryConnection();
		RepositoryResult<Statement> stmts = conn.getStatements(
				AK2Ses.getResource(concept,session.getValueFactory()), 
				RDFS.LABEL,
				null,false);
		
		Statement stmt;
		String label = null;
		try {
			while(stmts.hasNext()) {
				stmt = stmts.next();
				label = stmt.getObject().stringValue();
			}
		} finally {
			stmts.close();
		}		
		return label;	
		
	} catch (Exception e) {
		throw new DatasourceException("Error occurred while finding a label by concept"+concept, e);
	}
}
	

	/**
	 * @deprecated
	 */
	public void delete(IBusinessObject existingBo) throws DatasourceException {
		// TODO Auto-generated method stub

	}

	
	public List<? extends IBusinessObject> findAll() throws DatasourceException {
		List<INamedConcept> results = new ArrayList<INamedConcept>();
		try {
			
			RepositoryConnection conn = session.getRepositoryConnection();
			
			List<Statement> sesResult = conn.getStatements(null, RDF.TYPE, RDFS.CLASS, session.isReasoningOn()).asList();
			sesResult.addAll(conn.getStatements(null, RDF.TYPE, new URIImpl("http://www.w3.org/2002/07/owl#Class"), session.isReasoningOn()).asList());
						
			
				for(Statement stmt : sesResult) {
					if(stmt.getSubject() instanceof URI) {
						if((((URI)stmt.getSubject()).getNamespace().equals(RDFS.NAMESPACE))
								||(((URI)stmt.getSubject()).getNamespace().equals(RDF.NAMESPACE))||
								(((URI)stmt.getSubject()).getNamespace().equals("http://www.w3.org/2002/07/owl#"))) {
							continue;
						}
						results.add(Ses2AK.getNamedConcept(stmt.getSubject(), session.getOntology()));
					}
				}
		} catch(RepositoryException e) {
			throw new DatasourceException("Error occurred while retrieving all concepts", e);
		}
		
		return results;
	}

	/**
	 * @deprecated
	 */
	public IBusinessObject findById(String id) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @deprecated
	 */
	@SuppressWarnings("unchecked")
	public Class getBoClass() {
		return NamedConcept.class;
	}

	/**
	 * @deprecated
	 */
	public void insert(IBusinessObject newBo) throws DatasourceException {
//		throw new UnsupportedOperationException("Insert/update unsupported for entities.");
	}

	/**
	 * @deprecated
	 */
	public void update(IBusinessObject existingBo) throws DatasourceException {
//		throw new UnsupportedOperationException("Insert/update unsupported for entities.");

	}

	public Set<IConcept> findSubconcepts(INamedConcept concept, boolean includeInferred) throws DatasourceException {
		Set<IConcept> types = new HashSet<IConcept>();
		try {
			RepositoryConnection conn = session.getRepositoryConnection();
			URI sesConcept = (URI)AK2Ses.getResource(concept, session.getValueFactory());
			RepositoryResult<Statement> stmts = conn.getStatements(null, RDFS.SUBCLASSOF, sesConcept, includeInferred);
			Statement stmt;
			INamedConcept type;
			try {
				while(stmts.hasNext()) {
					stmt = stmts.next();
					
					if(((URI)stmt.getSubject()).toString().equals(sesConcept.toString())) 
						continue;
				
				    type = Ses2AK.getNamedConcept((URI)stmt.getSubject(), session.getOntology());
				    if(type!=null) 
					  types.add(type);			       
			   }
			} finally {
				stmts.close();
			}
		} catch (Exception e) {
			throw new DatasourceException("Error occurred while retrieving all subconcepts of the concept "+concept.getUri(), e);
		}
		return types;
	}

	public Set<IConcept> findSuperconcepts(INamedConcept concept, boolean includeInferred) throws DatasourceException {
		Set<IConcept> types = new HashSet<IConcept>();
		try {
			RepositoryConnection conn = session.getRepositoryConnection();
			URI sesConcept = (URI)AK2Ses.getResource(concept, session.getValueFactory());
			RepositoryResult<Statement> stmts = conn.getStatements(sesConcept, RDFS.SUBCLASSOF, null, includeInferred);
			Statement stmt;
			INamedConcept type;
			try {
				while(stmts.hasNext()) {
					stmt = stmts.next();
					if(stmt.getObject() instanceof URI) {
						if(((URI)stmt.getObject()).toString().equals(sesConcept.toString())) {
							continue;
						}
						type = Ses2AK.getNamedConcept((URI)stmt.getObject(), session.getOntology());
						if(type!=null) {
							types.add(type);
						}
					}
				}
			} finally {
				stmts.close();
			}
		} catch (Exception e) {
			throw new DatasourceException("Error occurred while retrieving all superconcepts of the concept "+concept.getUri(), e);
		}
		return types;
	}

}
