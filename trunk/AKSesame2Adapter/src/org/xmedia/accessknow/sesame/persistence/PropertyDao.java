package org.xmedia.accessknow.sesame.persistence;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.aifb.xxplore.shared.util.Pair;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.IPropertyDao;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.accessknow.sesame.persistence.converter.AK2Ses;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;

/**
 * @author an2548
 * 
 */
public class PropertyDao extends AbstractDao implements IPropertyDao {

	private SesameSession m_session;

	public PropertyDao(SesameSession session) {
		this.m_session = session;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public Set<IProperty> findProperties(INamedConcept concept)
			throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IProperty> findProperties(INamedIndividual individual)
			throws DatasourceException {
		Set<IProperty> results = new HashSet<IProperty>();
		try {
			results.addAll(findPropertiesFrom(individual));
			results.addAll(findPropertiesTo(individual));
			return results;
		} catch (Exception e) {
			throw new DatasourceException(
					"Error occurred while retrieving all properties of an individual with a uri "
							+ individual.getUri(), e);
		}
	}

	public Set<Pair> findPropertiesAndRangesFrom(INamedConcept concept)
			throws DatasourceException {

		Set<Pair> props = new HashSet<Pair>();

		try {

			RepositoryConnection con = m_session.getRepositoryConnection();
			SesameOntology onto = ((SesameOntology) m_session.getOntology());
			ValueFactory factory = onto.getRepository().getValueFactory();

			try {

				List<Statement> properties = con.getStatements(null,
						RDFS.DOMAIN, AK2Ses.getResource(concept, factory),
						m_session.isReasoningOn()).asList();

				// find properties from superconcepts
				IDaoManager daoManager = m_session.getDaoManager();
				IConceptDao conceptDao = daoManager.getConceptDao();

				Set<IConcept> super_concepts = conceptDao
						.findSuperconcepts(concept);
				Iterator<IConcept> iter = super_concepts.iterator();

				while (iter.hasNext()) {

					IConcept super_concept = iter.next();
					properties.addAll(con.getStatements(null, RDFS.DOMAIN,
							AK2Ses.getResource(super_concept, factory),
							m_session.isReasoningOn()).asList());

				}

				for (Statement stmt : properties) {

					Resource subject = stmt.getSubject();

					if (subject instanceof URI) {

						RepositoryResult<Statement> property_range = con
								.getStatements(subject, RDFS.RANGE, null,
										m_session.isReasoningOn());

						// property range has been specified
						if (property_range.hasNext()) {

							Statement property_range_stmt = property_range
									.next();
							Value range = property_range_stmt.getObject();

							if (Ses2AK.isObjectProperty((URI) subject, range)) {

								if (range instanceof URI) {

									INamedConcept namedConcept = Ses2AK
											.getNamedConcept((URI) range, onto);
									IObjectProperty objectProperty = Ses2AK
											.getObjectProperty((URI) subject,
													m_session.getOntology());

									props.add(new Pair(objectProperty,
											namedConcept));
								}
							} else {

								if (range instanceof org.openrdf.model.Literal) {

									IDatatype datatype = Ses2AK.getDatatype(
											((org.openrdf.model.Literal) range)
													.getDatatype(), onto);
									IDataProperty dataProperty = Ses2AK
											.getDataProperty((URI) subject,
													m_session.getOntology());

									props.add(new Pair(dataProperty, datatype));
								} else if (range instanceof URI) {

									IDatatype datatype = Ses2AK.getDatatype(
											(URI) range, onto);
									IDataProperty dataProperty = Ses2AK
											.getDataProperty((URI) subject,
													m_session.getOntology());

									props.add(new Pair(dataProperty, datatype));
								}
							}
						}
					}
				}
			} catch (Exception s) {
				s.printStackTrace();
			} finally {
				con.close();
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		return props;
	}

	public Set<IProperty> findPropertiesFrom(INamedConcept concept)
			throws DatasourceException {

		Set<IProperty> result = new HashSet<IProperty>();

		try {

			RepositoryConnection con = m_session.getRepositoryConnection();
			ValueFactory factory = ((SesameOntology) m_session.getOntology())
					.getRepository().getValueFactory();

			List<Statement> properties = con.getStatements(null, RDFS.DOMAIN,
					AK2Ses.getResource(concept, m_session.getValueFactory()),
					m_session.isReasoningOn()).asList();

			IDaoManager daoManager = m_session.getDaoManager();
			IConceptDao conceptDao = daoManager.getConceptDao();

			// also add properties from superConcepts ...
			Set<IConcept> super_concepts = conceptDao
					.findSuperconcepts(concept);
			Iterator<IConcept> iter = super_concepts.iterator();

			while (iter.hasNext()) {

				IConcept super_concept = iter.next();
				properties.addAll(con.getStatements(null, RDFS.DOMAIN,
						AK2Ses.getResource(super_concept, factory),
						m_session.isReasoningOn()).asList());

			}

			for (Statement stmt : properties) {

				Resource subject = stmt.getSubject();

				if (subject instanceof URI) {

					RepositoryResult<Statement> property_range = con
							.getStatements(subject, RDFS.RANGE, null, m_session
									.isReasoningOn());

					if (property_range.hasNext()) {

						Statement property_range_stmt = property_range.next();
						Value range = property_range_stmt.getObject();

						if (Ses2AK.isObjectProperty((URI) subject, range)) {
							result.add(Ses2AK.getObjectProperty((URI) subject,
									m_session.getOntology()));
						} else {
							result.add(Ses2AK.getDataProperty((URI) subject,
									m_session.getOntology()));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public Set<IProperty> findPropertiesFrom(INamedIndividual individual)
			throws DatasourceException {

		Set<IProperty> propertiesFrom = new HashSet<IProperty>();

		try {

			RepositoryConnection con = m_session.getRepositoryConnection();

			RepositoryResult<Statement> results = con.getStatements(AK2Ses
					.getResource(individual, m_session.getValueFactory()),
					null, null, m_session.isReasoningOn());

			while (results.hasNext()) {

				Statement stmt = results.next();
				Resource subject = stmt.getSubject();

				if (subject instanceof URI) {

					RepositoryResult<Statement> property_range = con
							.getStatements(subject, RDFS.RANGE, null, m_session
									.isReasoningOn());

					if (property_range.hasNext()) {

						Statement property_range_stmt = property_range.next();
						Value range = property_range_stmt.getObject();

						if (Ses2AK.isObjectProperty((URI) subject, range)) {
							propertiesFrom.add(Ses2AK.getObjectProperty(
									(URI) subject, m_session.getOntology()));
						} else {
							propertiesFrom.add(Ses2AK.getDataProperty(
									(URI) subject, m_session.getOntology()));
						}
					}
				}
			}
		} catch (Exception e) {
			throw new DatasourceException(
					"Error occurred while retrieving all properties from an individual with a uri "
							+ individual.getUri(), e);
		}

		return propertiesFrom;
	}

	public Set<IProperty> findPropertiesTo(INamedConcept concept)
			throws DatasourceException {

		Set<IProperty> result = new HashSet<IProperty>();

		try {

			RepositoryConnection con = m_session.getRepositoryConnection();
			RepositoryResult<Statement> results = con.getStatements(null, null,
					AK2Ses.getResource(concept, m_session.getValueFactory()),
					m_session.isReasoningOn());

			while (results.hasNext()) {

				Statement stmt = results.next();
				URI predicate = stmt.getPredicate();
				result.add(Ses2AK.getObjectProperty(predicate, m_session
						.getOntology()));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public Set<IProperty> findPropertiesTo(INamedIndividual individual)
			throws DatasourceException {
		Set<IProperty> results = new HashSet<IProperty>();
		IProperty prop;
		try {
			Resource resource = AK2Ses.getResource(individual, m_session
					.getValueFactory());
			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> sesResult = conn.getStatements(null,
					null, resource, m_session.isReasoningOn());
			Statement stmt;
			try {
				while (sesResult.hasNext()) {
					stmt = sesResult.next();
					prop = this.findByUri(stmt.getPredicate().toString());
					if (!results.contains(prop)) {
						results.add(prop);
					}
				}
			} finally {
				sesResult.close();
			}
			return results;
		} catch (Exception e) {
			throw new DatasourceException(
					"Error occurred while retrieving all properties to an individual with a uri "
							+ individual.getUri(), e);
		}
	}

	public IProperty findByUri(String uri) throws DatasourceException {
		try {
			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> sesResult = conn.getStatements(
					m_session.getValueFactory().createURI(uri), RDF.TYPE,
					RDF.PROPERTY, true);
			IProperty res = null;
			URI sesUri;
			try {
				if (sesResult.hasNext()) {

					sesUri = (URI) sesResult.next().getSubject();
					if (Ses2AK.isObjectProperty(sesUri,
							m_session.getOntology(), m_session
									.getRepositoryConnection())) {
						res = Ses2AK.getObjectProperty(sesUri, m_session
								.getOntology());
					} else {
						res = Ses2AK.getDataProperty(sesUri, m_session
								.getOntology());
					}
				}
			} finally {
				sesResult.close();
			}
			return res;
		} catch (RepositoryException e) {
			throw new DatasourceException(
					"Error occurred while retrieving a property with a uri "
							+ uri, e);
		}
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void delete(IBusinessObject existingBo) throws DatasourceException {
		// TODO Auto-generated method stub

	}

	public List<? extends IBusinessObject> findAll() throws DatasourceException {
		List<IProperty> results = new ArrayList<IProperty>();

		try {

			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> properties = null;

			properties = conn.getStatements(null, RDF.TYPE, RDF.PROPERTY,
					m_session.isReasoningOn());

			while (properties.hasNext()) {

				Statement stmt = properties.next();
				Resource property = stmt.getSubject();

				if (property instanceof URI) {

					if (Ses2AK.isObjectProperty((URI) property, m_session
							.getOntology(), conn)) {

						results.add(Ses2AK.getObjectProperty((URI) stmt
								.getSubject(), m_session.getOntology()));
					} else {

						results.add(Ses2AK.getDataProperty((URI) stmt
								.getSubject(), m_session.getOntology()));
					}
				}
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		return results;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public IBusinessObject findById(String id) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class getBoClass() {
		return Property.class;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void insert(IBusinessObject newBo) throws DatasourceException {
		// TODO Auto-generated method stub

	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void update(IBusinessObject existingBo) throws DatasourceException {
		// TODO Auto-generated method stub

	}

	public String findLabel(IProperty property) throws DatasourceException {

		try {
			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> stmts = conn.getStatements(AK2Ses
					.getResource(property, m_session.getValueFactory()),
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
					"Error occurred while finding a label by property" + property,
					e);
		}
	}

}
