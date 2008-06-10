package org.xmedia.accessknow.sesame.persistence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class LiteralDao implements ILiteralDao{

//	private static Logger s_log = Logger.getLogger(LiteralDao.class);

	public Set<ILiteral> findMemberIndividuals(IDatatype arg0) throws DatasourceException
	{
		StatelessSession session =  (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		SesameOntology onto = (SesameOntology)session.getOntology();
		Set<ILiteral> literals = new HashSet<ILiteral>();

		try 
		{
			RepositoryConnection con = (onto).getRepository().getConnection();

			RepositoryResult<Statement> statements = con.getStatements(null, null, null, false);
			Value val;
			org.openrdf.model.Literal ses_lit;

			try 
			{
				while (statements.hasNext()) 
				{
					val = statements.next().getObject();

					if(val instanceof org.openrdf.model.Literal){

						ses_lit = (org.openrdf.model.Literal)val;

						String ses_datatype_uri = ses_lit.getDatatype().getNamespace() + ses_lit.getDatatype().getLocalName();
						String ak_datatype_uri = arg0.getLabel();

						if(ses_datatype_uri.equals(ak_datatype_uri)) {
							literals.add((ILiteral)Ses2AK.getObject(ses_lit, onto));
						}						
					}
				}
			}
			finally
			{
				statements.close();
				con.close();
			}
		} 
		catch (RepositoryException e) 
		{
			e.printStackTrace();
		}

		return literals;
	}

	public void delete(IBusinessObject businessObject) throws DatasourceException
	{
		Emergency.checkPrecondition(businessObject instanceof ILiteral,"existingBo instanceof ILiteral");
		ILiteral oms_literal = (ILiteral) businessObject;

		StatelessSession session = (StatelessSession)PersistenceUtil.getSessionFactory().getCurrentSession();

		Emergency.checkPrecondition(session.getOntology() instanceof SesameOntology,"session.getOntology() instanceof Xmedia2Ontology");
		SesameOntology onto = (SesameOntology)session.getOntology();

		try 
		{
			RepositoryConnection con = (onto).getRepository().getConnection();
			ValueFactory factory = (onto).getRepository().getValueFactory();

			org.openrdf.model.Literal sesame_literal = factory.createLiteral(oms_literal.getLiteral());		
			RepositoryResult<Statement> statements = con.getStatements(null, null, sesame_literal, false);

			try 
			{
				while (statements.hasNext()) 
				{
					con.remove(statements.next());
				}
			}
			finally
			{
				statements.close();
				con.close();
			}
		} 
		catch (RepositoryException e) 
		{
			e.printStackTrace();
		}
	}

	public List<? extends IBusinessObject> findAll() throws DatasourceException
	{
		StatelessSession session = (StatelessSession) PersistenceUtil.getSessionFactory().getCurrentSession();
		List<ILiteral> literals = new ArrayList<ILiteral>();

		Emergency.checkPrecondition(session.getOntology() instanceof SesameOntology,"session.getOntology() instanceof Xmedia2Ontology");

		try {

			IPropertyMemberAxiomDao propertyMDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();			
			List<IPropertyMember> pMembers = propertyMDao.findAll();

			for(IPropertyMember pm : pMembers){
				IResource obj = pm.getTarget();

				if((obj instanceof ILiteral) && !literals.contains(obj)){
					literals.add((ILiteral)obj);

				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}


		return literals;
	}


	public IBusinessObject findById(String strg) throws DatasourceException
	{
//		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
//		List<ILiteral> literals = new ArrayList<ILiteral>();
		//
//		Set<DataPropertyMember> propmembers = new HashSet<DataPropertyMember>();
//		try {
		//
//		propmembers = onto.getDelegate().createAxiomRequest(
//		DataPropertyMember.class).getAll();
//		// no such axioms
//		if (propmembers == null || propmembers.size() == 0)
//		return null;
		//
//		literals = new ArrayList<ILiteral>();
//		for (DataPropertyMember propmember : propmembers) {
//		if (propmember.getAxiomID().equals(id))
//		return (ILiteral) Kaon2OMSModelConverter.convert(propmember
//		.getTargetValue(), onto);
//		}
//		} catch (KAON2Exception e) {
//		throw new DatasourceException(e);
//		}
		//
//		return literals;

//		Emergency.checkPrecondition(businessObject instanceof ILiteral,"existingBo instanceof ILiteral");
//		ILiteral oms_literal = (ILiteral) businessObject;


		return null;
	}

	public Class<Literal> getBoClass()
	{
		return Literal.class;
	}

	public void insert(IBusinessObject arg0) throws DatasourceException
	{
		throw new UnsupportedOperationException("Insert/update unsupported for literals.");
	}

	public void update(IBusinessObject arg0) throws DatasourceException
	{
		throw new UnsupportedOperationException("Insert/update unsupported for literals.");
	}
}
