package org.xmedia.accessknow.sesame.persistence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.xmedia.accessknow.sesame.persistence.converter.AK2Ses;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.dao.IDatatypeDao;

public class DatatypeDao implements IDatatypeDao {

	SesameSession m_session;

	public DatatypeDao(SesameSession session) {
		this.m_session = session;
	}

	public Set<IDatatype> findDatatypeRanges(IProperty property)
			throws DatasourceException {
		Set<IDatatype> res = new HashSet<IDatatype>();
		try {
			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> stmts = conn.getStatements(AK2Ses
					.getResource(property, m_session.getValueFactory()),
					RDFS.RANGE, null, m_session.isReasoningOn());
			Statement stmt;
			try {
				while (stmts.hasNext()) {
					stmt = stmts.next();
					if (stmt.getObject() instanceof URI) {
						res.add(Ses2AK.getDatatype((URI) stmt.getObject(),
								m_session.getOntology()));
					}
				}
			} finally {
				stmts.close();
			}
			if (res.isEmpty()) {
				res.add(Ses2AK.getDatatype(XMLSchema.STRING, m_session
						.getOntology()));
			}
		} catch (Exception e) {
			throw new DatasourceException(
					"Error occurred while retrieving datatype ranges of a properties from an individual with a uri "
							+ property.getUri(), e);
		}
		return res;
	}

	public Set<IDatatype> findDatatype(IProperty property) throws DatasourceException {
		Set<IDatatype> res = new HashSet<IDatatype>();
		try {
			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> stmts = conn.getStatements(AK2Ses
					.getResource(property, m_session.getValueFactory()),
					RDFS.DATATYPE, null, m_session.isReasoningOn());
			Statement stmt;
			try {
				while (stmts.hasNext()) {
					stmt = stmts.next();
					if (stmt.getObject() instanceof URI) {
						res.add(Ses2AK.getDatatype((URI) stmt.getObject(),
								m_session.getOntology()));
					}
				}
			} finally {
				stmts.close();
			}

		} catch (Exception e) {
			throw new DatasourceException(
					"Error occurred while retrieving datatype of a properties from an individual with a uri "
							+ property.getUri(), e);
		}
		return res;
	}

	public Set<IDatatype> findDatatypes(ILiteral literal)
			throws DatasourceException {

		Set<IDatatype> datatypes = new HashSet<IDatatype>();

		RepositoryConnection con = null;

		try {

			con = m_session.getRepositoryConnection();

			String queryString = "SELECT L " + "FROM {R} Z {L}"
					+ "WHERE isLiteral(L) AND L LIKE \"" + literal.getLiteral()
					+ "\"";

			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SERQL,
					queryString);
			TupleQueryResult results = tupleQuery.evaluate();

			org.openrdf.model.Literal ses_lit;

			try {

				if (results.hasNext()) {

					Object result = results.next().getValue("L");

					if (result instanceof org.openrdf.model.Literal) {
						ses_lit = (org.openrdf.model.Literal) result;

						if (ses_lit.getLanguage() != null) {
							datatypes.add(Ses2AK.getDatatype(XMLSchema.STRING,
									m_session.getOntology()));
						} else if (ses_lit.getDatatype() != null) {
							datatypes.add(Ses2AK.getDatatype(ses_lit
									.getDatatype(), m_session.getOntology()));
						} else {
							// TODO: what to do .. ?!
							datatypes.add(Ses2AK.getDatatype(XMLSchema.STRING,
									m_session.getOntology()));
						}
					}
				}
			} finally {
				results.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}

		return datatypes;
	}

	public void delete(IBusinessObject existingBo) throws DatasourceException {
		// TODO Auto-generated method stub
	}

	public List<? extends IBusinessObject> findAll() throws DatasourceException {

		List<IDatatype> datatypes = new ArrayList<IDatatype>();

		try {
			RepositoryConnection conn = m_session.getRepositoryConnection();
			RepositoryResult<Statement> stmts = conn.getStatements(null,
					RDFS.RANGE, null, m_session.isReasoningOn());

			Statement stmt;
			try {
				while (stmts.hasNext()) {
					stmt = stmts.next();
					if (stmt.getObject() instanceof URI) {
						IDatatype thisDatatype = Ses2AK.getDatatype((URI) stmt
								.getObject(), m_session.getOntology());
						if (!datatypes.contains(thisDatatype))
							datatypes.add(thisDatatype);
					}
				}
			} finally {
				stmts.close();
			}
			if (datatypes.isEmpty()) {
				datatypes.add(Ses2AK.getDatatype(XMLSchema.STRING, m_session
						.getOntology()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datatypes;
	}

	public IBusinessObject findById(String id) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class getBoClass() {
		return IDatatype.class;
	}

	public void insert(IBusinessObject newBo) throws DatasourceException {
		// throw new
		//UnsupportedOperationException("Insert/update unsupported for entities."
		// );
	}

	public void update(IBusinessObject existingBo) throws DatasourceException {
		// throw new
		//UnsupportedOperationException("Insert/update unsupported for entities."
		// );
	}

}
