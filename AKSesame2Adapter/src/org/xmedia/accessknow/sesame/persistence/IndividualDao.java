package org.xmedia.accessknow.sesame.persistence;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.accessknow.sesame.persistence.converter.AK2Ses;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

/**
 * @author an2548
 *
 */
public class IndividualDao implements IIndividualDao {

	SesameSession session;
	/**
	 * 
	 */
	public IndividualDao(SesameSession session) {
		this.session = session;
	}

	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IIndividualDao#findMemberIndividuals(org.xmedia.oms.model.api.INamedConcept)
	 */
	public Set<IIndividual> findMemberIndividuals(INamedConcept concept)
			throws DatasourceException {
		Set<IIndividual> resultSet = new HashSet<IIndividual>();
		try {
			RepositoryConnection conn = session.getRepositoryConnection();
			
			RepositoryResult<Statement> sesResult = conn.getStatements(null, 
					RDF.TYPE, 
					AK2Ses.getResource(concept, session.getValueFactory()), 
					session.isReasoningOn());
			Statement stmt;
			try {
				while(sesResult.hasNext()) {
					stmt = sesResult.next();
					resultSet.add(
							Ses2AK.getNamedIndividual(
									stmt.getSubject(), 
									concept.getOntology()));
				}
			} finally {
				sesResult.close();
			}
			return resultSet;
		} catch(Exception e) {
			throw new DatasourceException("Error occurred while retrieving all member individuals of the concept "+concept.getUri(), e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IIndividualDao#findIndividualsByPropery(org.xmedia.oms.model.api.IProperty)
	 */
	public Set<IIndividual> findIndividualsByPropery(IProperty property)
			throws DatasourceException {
		
		Set<IIndividual> resultSet = new HashSet<IIndividual>();
		
		RepositoryConnection conn = session.getRepositoryConnection();
		
		try {
			
			RepositoryResult<Statement> sesResult = conn.getStatements(null, 
					AK2Ses.getProperty(property, session.getValueFactory()), 
					null, 
					session.isReasoningOn());
			
			while(sesResult.hasNext()){
				
				Value object = sesResult.next().getObject();
				
				if(object instanceof Resource){
					resultSet.add(
						Ses2AK.getNamedIndividual(
							(Resource)object,
							session.getOntology()));
				}
				
			}
			
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IEntityDao#findByUri(java.lang.String)
	 */
	public INamedIndividual findByUri(String uri) throws DatasourceException {
		try {
			RepositoryConnection conn = session.getRepositoryConnection();
			RepositoryResult<Statement> sesResult = conn.getStatements(
					session.getValueFactory().createURI(uri), 
					RDF.TYPE, null, false);
			INamedIndividual response = null;
			try {
				if(sesResult.hasNext()) {
					response = Ses2AK.getNamedIndividual(sesResult.next().getSubject(), session.getOntology());
				}
			} finally {
				sesResult.close();
			}
			return response;
		} catch (RepositoryException e) {
			throw new DatasourceException("Error occurred while retrieving an individual with a uri "+uri, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IDao#delete(org.xmedia.businessobject.IBusinessObject)
	 */
	public void delete(IBusinessObject existingBo) throws DatasourceException {
		try {
			IIndividual ind = (IIndividual)existingBo;
			Resource sesInd = AK2Ses.getResource(ind, session.getValueFactory());
			Resource[] contexts = new Resource[0];
			RepositoryConnection conn = session.getRepositoryConnection();
			conn.remove(
					sesInd, 
					null, 
					null);
			conn.remove(
					null, 
					null, 
					sesInd, 
					contexts);
		} catch(Exception e) {
			throw new DatasourceException("Error occurred while deleting the individual "+((INamedIndividual)existingBo).getUri(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IDao#findAll()
	 * TODO Warning: here the individuals are interpreted as 
	 * "anything with an explicitly defined type AND the type should not belong to either 
	 * RDFS or OWL namespace". This is not always correct and should be revised.
	 */
	public List<? extends IBusinessObject> findAll() throws DatasourceException {
		try {
			List<IIndividual> result = new ArrayList<IIndividual>();
			RepositoryConnection conn = session.getRepositoryConnection();
			RepositoryResult<Statement> sesResult = conn.getStatements(null, RDF.TYPE, null, false);
			IIndividual ind;
			Statement stmt;
			try {
				while(sesResult.hasNext()) {
					stmt = sesResult.next();
					if(stmt.getObject() instanceof URI) {
						if(((URI)stmt.getObject()).getNamespace().equals(RDFS.NAMESPACE)||
								((URI)stmt.getObject()).getNamespace().equals(RDF.NAMESPACE)||
								((URI)stmt.getObject()).getNamespace().startsWith("http://www.w3.org/2002/07/owl#")) {
							continue;
						}
						ind = Ses2AK.getNamedIndividual(stmt.getSubject(), session.getOntology());
						result.add(ind);
					}
				}
			} finally {
				sesResult.close();
			}
			return result;
		} catch(Exception e) {
			throw new DatasourceException("Error occurred while retrieving all individuals", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IDao#findById(java.lang.String)
	 */
	public IBusinessObject findById(String id) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IDao#getBoClass()
	 */
	@SuppressWarnings("unchecked")
	public Class getBoClass() {
		return NamedIndividual.class;
	}

	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IDao#insert(org.xmedia.businessobject.IBusinessObject)
	 */
	public void insert(IBusinessObject newBo) throws DatasourceException {
		try {
			AK2Ses.getResource((IIndividual)newBo, session.getValueFactory());
		} catch(Exception e) {
			throw new DatasourceException("Error occurred while inserting an individual "+((IIndividual)newBo).toString(), e);
		}

	}

	/* (non-Javadoc)
	 * @see org.xmedia.oms.persistence.dao.IDao#update(org.xmedia.businessobject.IBusinessObject)
	 */
	public void update(IBusinessObject existingBo) throws DatasourceException {
		// TODO Auto-generated method stub

	}

	/** 
	 * Note: findMemberIndividuals(INamedConcept concept, boolean includeInferred) is currently just calling
	 * findMemberIndividuals(INamedConcept concept)!
	 * @see org.xmedia.oms.persistence.dao.IIndividualDao#findMemberIndividuals(org.xmedia.oms.model.api.INamedConcept, boolean)
	 */
	public Set<IIndividual> findMemberIndividuals(INamedConcept concept, boolean includeInferred) throws DatasourceException {
		return findMemberIndividuals(concept);
	}

	public int getNumberOfIndividual(INamedConcept concept) throws DatasourceException {

		Set<IIndividual> result = new HashSet<IIndividual>();
		
		try {
			
			RepositoryConnection conn = session.getRepositoryConnection();
			
			org.openrdf.model.Resource sesResource = AK2Ses.getResource(concept, session.getValueFactory());
			RepositoryResult<Statement> sesResult = conn.getStatements(null, RDF.TYPE, sesResource, session.isReasoningOn());
			
			IIndividual ses_ind;
			Statement stmt;
			
			try {
				
				while(sesResult.hasNext()) {
					
					stmt = sesResult.next();
					
					if(stmt.getObject() instanceof URI) {
						ses_ind = Ses2AK.getNamedIndividual(stmt.getSubject(), session.getOntology());
						result.add(ses_ind);
					}
				}
			} 
			finally {
				sesResult.close();
				conn.close();
			}
		} catch(Exception e) {
			throw new DatasourceException("Error occurred while retrieving all individuals", e);
		}
		return result.size();
	}
	
	public int getNumberOfIndividual(INamedConcept concept, boolean includeInferred) throws DatasourceException {
		return getNumberOfIndividual(concept);
	}
	
	public String findLabel(INamedIndividual individual) throws DatasourceException {

		try {
			RepositoryConnection conn = session.getRepositoryConnection();
			RepositoryResult<Statement> stmts = conn.getStatements(AK2Ses
					.getResource(individual, session.getValueFactory()),
					RDFS.LABEL, null, false);

			Statement stmt;
			String label = null;
			try {
				while (stmts.hasNext()) {
					stmt = stmts.next();
					label = stmt.getObject().stringValue();
				}
			} finally {
				stmts.close();
			}
			return label;

		} catch (Exception e) {
			throw new DatasourceException(
					"Error occurred while finding a label by concept" + individual,
					e);
		}
	}


}
