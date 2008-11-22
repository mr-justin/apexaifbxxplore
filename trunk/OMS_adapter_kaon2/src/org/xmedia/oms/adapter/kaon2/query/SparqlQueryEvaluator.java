package org.xmedia.oms.adapter.kaon2.query;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import name.levering.ryan.sparql.parser.ParseException;
import name.levering.ryan.sparql.parser.SPARQLParser;
import name.levering.ryan.sparql.parser.model.ASTVar;
import name.levering.ryan.sparql.parser.model.Node;

import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.Join;
import org.aifb.xxplore.shared.util.Join.DataSet;
import org.aifb.xxplore.shared.util.Join.Row;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.Namespaces;
import org.semanticweb.kaon2.api.reasoner.Query;
import org.semanticweb.kaon2.api.reasoner.Reasoner;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider.Kaon2Connection;
import org.xmedia.oms.adapter.kaon2.util.Kaon2OMSModelConverter;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.IQueryResult;
import org.xmedia.oms.query.IQueryWrapper;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryException;
import org.xmedia.oms.query.QueryResult;
import org.xmedia.oms.query.ResourceTuple;

public class SparqlQueryEvaluator implements IQueryEvaluator{

	public IQueryResult evaluate(IQueryWrapper query) {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Emergency.checkPrecondition(session.getReasoner() != null && session.getReasoner()  instanceof Reasoner, 
		"session.getReasoner() != null && session.getReasoner()  instanceof Reasoner");

		Reasoner reasoner = (Reasoner)session.getReasoner();
		Set<ITuple> results = new HashSet<ITuple>();

		try {

			Query kaon2query = reasoner.createQuery(new Namespaces(Namespaces.INSTANCE), query.getQuery());
			String[] labels = query.getSelectVariables();		
			kaon2query.open();
			while (!kaon2query.afterLast()) {

				Object[] tupleBuffer= kaon2query.tupleBuffer();
				IResource[] ress = new IResource[tupleBuffer.length];
				for (int i = 0; i < tupleBuffer.length; i++){
					ress[i] = Kaon2OMSModelConverter.convert(tupleBuffer[i], session.getOntology());
				}

				results.add(new ResourceTuple(tupleBuffer.length, ress, labels));

				kaon2query.next();
			}
			kaon2query.close();
			kaon2query.dispose();

		} 

		catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new QueryResult(results,query.getSelectVariables());
	}

	public IQueryResult evaluateWithProvenance(IQueryWrapper query) throws QueryException {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();

		name.levering.ryan.sparql.model.Query provQuery = null;
		try {
			provQuery = SPARQLParser.parse(new StringReader(query.getQuery()));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Collection<ASTVar> varset = provQuery.getVariables();
		String[] vars = new String[varset.size()];
		if (varset != null && vars.length > 0){
			Iterator<ASTVar> iter = varset.iterator();
			for(int i = 0; i < vars.length; i ++){
				vars[i] = iter.next().getName();
			}
		}
		
		MetaknowSplitVisitor msv = new MetaknowSplitVisitor();
		((Node)provQuery).jjtAccept(msv);

		IQueryWrapper metaQuery = msv.getMetaQuery();
		IQueryWrapper baseQuery = msv.getBaseQuery();

		System.out.println();
		System.out.println(baseQuery.getQuery());

		DataSet baseResults = runQuery(baseQuery, session, baseQuery.getOntologyURI());
		System.out.println("result length of domain query: " +baseResults.size());
		
		if(metaQuery == null || metaQuery.getQuery().length() == 0) 
			return new QueryResult();

		System.out.println(metaQuery.getQuery());
		DataSet metaResults = runQuery(metaQuery, session, metaQuery.getOntologyURI());
		System.out.println("result length of meta query: " +metaResults.size());
		
		Set<String> joinVarsSet = new HashSet<String>(Arrays.asList(baseResults.getVars()));
		joinVarsSet.retainAll(Arrays.asList(metaResults.getVars()));
		String[] joinVars = joinVarsSet.toArray(new String[] {});

		DataSet result = Join.hashJoin(joinVars, baseResults, metaResults);
		if(result != null){
			Set<ITuple> resultSet = new HashSet<ITuple>();
			for (Iterator<Row> i = result.iterator(); i.hasNext(); ) {
				//TODO compute real arity 
				ResourceTuple rt = new ResourceTuple(-1, i.next().getData(), query.getSelectVariables());
				resultSet.add(rt);
			}
			return new QueryResult(resultSet, query.getSelectVariables());
		}
		else return null;

	}

	private DataSet runQuery(IQueryWrapper query, StatelessSession session, String ontoURI) {
		DataSet result = null;

		try {
			IOntology onto = ((Kaon2Connection)session.getConnection()).findOntologyByUri(ontoURI);
			Reasoner reasoner = ((Kaon2Ontology)onto).getDelegate().createReasoner();
			Query kaon2query = reasoner.createQuery(new Namespaces(Namespaces.INSTANCE), query.getQuery());
			String[] labels = query.getSelectVariables();
			result = new DataSet(labels);
			kaon2query.open();
			while (!kaon2query.afterLast()) {

				Object[] tupleBuffer = kaon2query.tupleBuffer();
				IResource[] ress = new IResource[tupleBuffer.length];
				for (int i = 0; i < tupleBuffer.length; i++){
					ress[i] = Kaon2OMSModelConverter.convert(tupleBuffer[i], session.getOntology());
				}

				result.addRow(new Row(result, ress));

				kaon2query.next();
			}
			kaon2query.close();
			kaon2query.dispose();

		} 
		catch (KAON2Exception e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean hasResults(IQueryWrapper queryWrapper) throws QueryException {
		// TODO Auto-generated method stub
		return false;
	}

}
