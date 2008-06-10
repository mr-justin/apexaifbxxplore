package org.xmedia.accessknow.sesame.model;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.parser.ParseException;
import name.levering.ryan.sparql.parser.SPARQLParser;
import name.levering.ryan.sparql.parser.model.Node;

import org.openrdf.model.Statement;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.model.NumericMemLiteral;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;
import org.xmedia.oms.metaknow.ComplexProvenance;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.MetaVocabulary;
import org.xmedia.oms.metaknow.ProvenanceUnknownException;
import org.xmedia.oms.metaknow.rewrite.AugmentVisitor;
import org.xmedia.oms.metaknow.rewrite.Formula;
import org.xmedia.oms.metaknow.rewrite.ProvenanceVisitor;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.IQueryResult;
import org.xmedia.oms.query.IQueryWrapper;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryException;
import org.xmedia.oms.query.QueryResult;
import org.xmedia.oms.query.ResourceTuple;

/**
 * @author bernie_2
 *
 */
/**
 * @author bernie_2
 *
 */
public class SesameSparqlEvaluator2perf implements IQueryEvaluator {

	/*
	 * Needed for benchmarking purposes. Please see MetaknowQueryTest for details.
	 */

	// //////////////////////////////////////
	public long m_timeStart;
	public long m_time1;
	public long m_time2;
	public long m_time2a;
	public long m_time2b;
	public long m_time3;
	public long m_time4;
	public long m_time5;
	
	public int m_resultSize;

	public ArrayList<String> m_bindings;

	// //////////////////////////////////////
	
	//In order to have these options -- but not as (mandatory) part of the interface:
	public static enum ProvRepresentations {OLD, NEW, FOR_EVALUATION, PROV_ONLY};
	public ProvRepresentations useProvRepr = ProvRepresentations.OLD;
	
	public static String COMBINED_PROVENANCE = "combinedProv";
	
	private SesameOntology ontology = null;
	private Repository repository = null;

	
	protected SesameSparqlEvaluator2perf(Repository repository, SesameOntology ontology) throws RepositoryException {

		this.repository = repository;
		this.ontology = ontology;

	}

	public IQueryResult evaluate(IQueryWrapper query) throws QueryException {
				
		IQueryResult theResult = null;
		RepositoryConnection conn = null;
		try {
			conn = repository.getConnection();
			TupleQuery compiledQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.getQuery());
			TupleQueryResult results = compiledQuery.evaluate();
			try {
//				String[] variablesNames = new String[results.getBindingNames().size()];
//				variablesNames = results.getBindingNames().toArray(variablesNames);
//				Set<ITuple> tuplesSet = new HashSet<ITuple>();

				m_resultSize = 0;
				while (results.hasNext()) {
//					BindingSet aBindingSet = results.next();
					m_resultSize++;
//					List<IResource> tuples = new ArrayList<IResource>();
//					List<String> names = new ArrayList<String>();
//					for (String aName : variablesNames) {
//						Binding aBinding = aBindingSet.getBinding(aName);
//						if (aBinding != null) {
//							names.add(aName);
//							tuples.add(Ses2AK.getObject(aBinding.getValue(), ontology));
//						}
//					}
//
//					if (tuples.size() > 0) {
//						String[] namesAsArray = new String[names.size()];
//						namesAsArray = names.toArray(namesAsArray);
//						tuplesSet.add(new ResourceTuple(tuples.size(), tuples.toArray(), namesAsArray));
//					}
				}

//				theResult = new QueryResult(tuplesSet, variablesNames);
			} catch (Exception e) {
				throw e;
			} finally {
				results.close();
			}
		} catch (RepositoryException e) {
			throw new QueryException(query, e);
		} catch (MalformedQueryException e) {
			throw new QueryException(query, e);
		} catch (QueryEvaluationException e) {
			throw new QueryException(query, e);
		} catch (Exception e) {
			throw new QueryException(query, e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (RepositoryException e) {
					throw new QueryException(query, e);
				}
			}
		}

		return theResult;
	}

	public IQueryResult evaluateWithProvenance(IQueryWrapper query) throws QueryException {

		/* Step 1 -- parse query, create augmented query, create provenance expression = formula pattern*/
		m_timeStart = System.currentTimeMillis();

		IQueryResult result = null;
		Query provQuery = null;
		String querystr = query.getQuery();
		System.out.println(querystr);
		try {
			provQuery = SPARQLParser.parse(new StringReader(querystr));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (query == null) {
			return null;
		}

		// create provenance expression using visitor
		ProvenanceVisitor pv = new ProvenanceVisitor();
		((Node) provQuery).jjtAccept(pv);
		// create augmented query
		AugmentVisitor av = new AugmentVisitor();
		// ((Node) provQuery).jjtAccept(av);
		String provQueryStr = av.augmentQuery(querystr);
		List<String> liProvVars = Arrays.asList(av.getUsedVars());

		/* Step 2 -- execute augmented query and read results into a Map*/
		m_time1 = System.currentTimeMillis();
		/* Step 2a -- run augmented query */

		RepositoryConnection conn = null;
		try {
			conn = repository.getConnection();

			TupleQuery compiledQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, provQueryStr);
			TupleQueryResult results = compiledQuery.evaluate();

			m_bindings = new ArrayList<String>();

			/* Step 2b -- TupleQueryResult -> Map; load all bindings into Map for multiple iterations, free TupleQueryResult */
			m_time2a = System.currentTimeMillis();
			// Currently, all results have to fit into main memory anyway (sometimes even twice for AccessNow) - thus:
			HashMap<BindingSet, ComplexProvenance> BindingsPlusProvenance = new HashMap<BindingSet, ComplexProvenance>();
			BindingSet tuple;
			m_resultSize = 0;
			while (results.hasNext()) {
				tuple = results.next();
				m_resultSize++;
				if (!BindingsPlusProvenance.containsKey(tuple)) {
					BindingsPlusProvenance.put(tuple, null);
				} else {
					System.out.println("INTERESTING: tuple not a unique object?");
				}
			}
			// TupleQueryResult implements info.aduna.iteration.CloseableIteration - resources are freed after exhaustion? Not during debug? Thus:
			results.close();

			
			/* Step 3 -- acquire all metadata for all bindings */
			m_time2b = System.currentTimeMillis();
			m_time2 = m_time2b;
			Map<String, org.xmedia.oms.metaknow.IProvenance> graphUris2Prov = new HashMap<String, IProvenance>();
			// collect all graph uris
			Set<String> boundVars = new HashSet<String>();
			String uri;
			for (BindingSet aTuple: BindingsPlusProvenance.keySet()) {
				boundVars.clear();
				boundVars.addAll(aTuple.getBindingNames());
				boundVars.retainAll(liProvVars);
				for (String provVar : boundVars) {
					uri = aTuple.getBinding(provVar).getValue().toString(); // since "provVarNames" are variables matched to graph names this value must be a URI
					graphUris2Prov.put(uri, null);
				}	
			}
			// collect metadata
			switch (this.useProvRepr) {
				case OLD: this.acquireMapGraphUri2Provenance(graphUris2Prov, conn); break;
				case FOR_EVALUATION: this.acquireMapGraphUri2Prov_Perf(graphUris2Prov, conn); break;
				case PROV_ONLY: break; //to evaluate only the how-provenance
				case NEW: System.out.println("New provenance representation does not exists yet!"); break;
			}
			
			
			/* Step 4 -- calculate complex provenance objects */
			m_time3 = System.currentTimeMillis();
			HashMap<String, org.xmedia.oms.metaknow.Provenance> provVars2ProvPerBinding = new HashMap<String, org.xmedia.oms.metaknow.Provenance>();
			Formula f1;
			for (BindingSet aTuple : BindingsPlusProvenance.keySet()) {
				boundVars.clear();
				boundVars.addAll(aTuple.getBindingNames());
				boundVars.retainAll(liProvVars);
				provVars2ProvPerBinding.clear();
				for (String provVar : boundVars) {
					uri = aTuple.getBinding(provVar).getValue().toString();
					// since "provVarNames" are variables matched to graph names value must be a URI
					if (this.useProvRepr == ProvRepresentations.PROV_ONLY) {
						provVars2ProvPerBinding.put(provVar, null);// to evaluated only the how-provenance
					} else {
						provVars2ProvPerBinding.put(provVar, (ComplexProvenance) graphUris2Prov.get(uri));
						// since Formula.getProvenance() does some checks it is better not to use graphUris2Prov directly
					}
				}
				f1 = pv.getProvenance().getProvenanceFormula();
				if (this.useProvRepr == ProvRepresentations.PROV_ONLY) {
					BindingsPlusProvenance.put(aTuple, new ComplexProvenance(new ArrayList<Double>(), new ArrayList<Date>(), new HashSet<INamedIndividual>(), new HashSet<IEntity>()));
				} else {
					BindingsPlusProvenance.put(aTuple, f1.getProvenance(provVars2ProvPerBinding));
					// to evaluate only the how-provenance; 'f1' contains the formula including graph variable names and 'BindingsPlusProvenance' contains graph
					// variables -> graph URIs
				}
			}
			
			
			/* Step 5 -- Sesame2 -> AccessKnow */
			m_time4 = System.currentTimeMillis();
			//List<Binding> provBindings = new ArrayList<Binding>();
			Set<ITuple> iTuples = new HashSet<ITuple>();
			String[] variableNames = new String[results.getBindingNames().size()];
			variableNames = results.getBindingNames().toArray(variableNames);
			String[] varNamesPlusProvenance = new String[variableNames.length + 1];
			Object[] oTuple;
			Binding aBinding;			
			for (Map.Entry<BindingSet, ComplexProvenance> tuple2Prov : BindingsPlusProvenance.entrySet()) {
				oTuple = new Object[variableNames.length + 1];
				for (int i = 0; i < variableNames.length; i++) {
					aBinding = tuple2Prov.getKey().getBinding(variableNames[i]);
					if (aBinding != null) {
						varNamesPlusProvenance[i] = variableNames[i];
						oTuple[i] = Ses2AK.getObject(aBinding.getValue(), ontology);
					}
				}
				// add provenance to last position
				varNamesPlusProvenance[variableNames.length] = COMBINED_PROVENANCE;
				oTuple[variableNames.length] = tuple2Prov.getValue();
				iTuples.add(new ResourceTuple(variableNames.length + 1, oTuple, varNamesPlusProvenance));
			}
	
			result = new QueryResult(iTuples, varNamesPlusProvenance);
			
			results = null;
			
			
		} catch (RepositoryException e) {
			throw new QueryException(query, e);
		} catch (MalformedQueryException e) {
			throw new QueryException(query, e);
		} catch (QueryEvaluationException e) {
			throw new QueryException(query, e);
		} catch (Exception e) {
			throw new QueryException(query, e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (RepositoryException e) {
					throw new QueryException(query, e);
				}
			}
		}
		m_time5 = System.currentTimeMillis();
		return result;
	}

	private void acquireMapGraphUri2Provenance(Map<String, IProvenance> graphUris2Prov, RepositoryConnection conn)
					throws QueryException, ProvenanceUnknownException {

		// get provenance URIs corresponding to graph URIs
		Map<String, String> provUri2graphUri = new HashMap<String, String>();
		// 2 alternatives: which one is faster?
		
		try {
			List<Statement> statements;
			for (String graphUri : graphUris2Prov.keySet()) {
				statements =
								conn.getStatements(repository.getValueFactory().createURI(graphUri),
												repository.getValueFactory().createURI("http://www.x-media.org/ontologies/metaknow#has_Provenance"), null,
												false).asList();
				if (statements.size() != 1) {
					throw new ProvenanceUnknownException(null, new Exception("Provenance not set (correctly)."));
				}
				provUri2graphUri.put(statements.get(0).getObject().toString(), graphUri);
			}
		} catch (RepositoryException e1) {
			e1.printStackTrace();
		}
	
		
//		int urisPerQuery = 3;
//		String query;
//		String select = "SELECT DISTINCT *  WHERE ";
//		String graphPattern = "";
//		int i = 0;
//		for (String uri : graphURIs) {
//			i++;
//			// assemble query string
//			if (i % urisPerQuery == 1) {
//				graphPattern += "{ " + uri + " http://www.x-media.org/ontologies/metaknow#has_Provenance ?provUri" + " }";
//			} else {
//				graphPattern += "UNION { " + uri + " http://www.x-media.org/ontologies/metaknow#has_Provenance ?provUri"  + "}";
//			}
//			// sent query, add results to provURIs
//			if (i % urisPerQuery == 0 || i == graphURIs.size()) {
//				query = select +  "{ " + graphPattern + " }";
//				this.readURIsInto(provURIs, query, conn);
//				graphPattern = "";
//			}
//		}	

		
		// get provenance data associated with provURIs, using UNION only
		//2 alternatives: which one is faster?
		
		try {
			List<Statement> statements;
			ComplexProvenance prov;
			for (String provUri : provUri2graphUri.keySet()) {
				statements = conn.getStatements(repository.getValueFactory().createURI(provUri), null, null, false).asList();
				for (Statement triple : statements) {
					if (!graphUris2Prov.containsKey(provUri2graphUri.get(provUri))) {
						prov = new ComplexProvenance(new ArrayList<Double>(),new ArrayList<Date>(), new HashSet<INamedIndividual>(), new HashSet<IEntity>());
						//prov = new ComplexProvenance(Double.valueOf(0), null, null, null);
						graphUris2Prov.put(provUri2graphUri.get(provUri), prov);
					}
					prov = (ComplexProvenance) graphUris2Prov.get(provUri2graphUri.get(provUri));
					String s = triple.getPredicate().toString().toLowerCase();
					if (s.equalsIgnoreCase(MetaVocabulary.AGENT)) {
						prov.getComplexAgent().add(ontology.createNamedIndividual(triple.getObject().toString()));
					} else if (s.equalsIgnoreCase(MetaVocabulary.CONFIDENCE_DEGREE)) {
						NumericMemLiteral num = (NumericMemLiteral)triple.getObject();
						prov.getComplexConfidenceDegree().add(num.doubleValue());
					} else if (s.equalsIgnoreCase(MetaVocabulary.CREATION_TIME)) {
						;
					} else if (s.equalsIgnoreCase(MetaVocabulary.SOURCE)) {
						prov.getComplexSource().add(ontology.createNamedIndividual(triple.getObject().toString()));
					}
				}
			}
		} catch (RepositoryException e1) {
			e1.printStackTrace();
		}
		
// 		urisPerQuery = 3;
//		graphPattern = "";
//		i = 0;
//		for (String uri : provURIs) {
//			i++;
//			// assemble query string
//			if (i % urisPerQuery == 1) {
//				graphPattern += "{ " + uri + " ?provProperty" + " ?provValue" + " }";
//			} else {
//				graphPattern += "UNION " + "{ " + uri + " ?provProperty" + " ?provValue" + " }";
//			}
//			// sent query, add results to provURIs
//			if (i % urisPerQuery == 0 || i == provURIs.size()) {
//				query = select +  "{ " + graphPattern + " }";
//				this.readProvInto(mapProv, query, conn);
//				graphPattern = "";
//			}
//		}
				
	}


	
	private void acquireMapGraphUri2Prov_Perf(Map<String, IProvenance> graphUris2Prov, RepositoryConnection conn)
					throws QueryException {

		try {
			List<Statement> statements;
			for (String graphUri : graphUris2Prov.keySet()) {
				statements = conn.getStatements(repository.getValueFactory().createURI(graphUri), null, null, false).asList();
				//fill meta knowledge about graph into ComplexProvenance object
				ComplexProvenance prov;
				for (Statement quadruple : statements) {
					if (graphUris2Prov.get(graphUri) == null) {
						prov = new ComplexProvenance(new ArrayList<Double>(), new ArrayList<Date>(), new HashSet<INamedIndividual>(), new HashSet<IEntity>());
						// prov = new ComplexProvenance(Double.valueOf(0), null, null, null);
						graphUris2Prov.put(graphUri, prov);
					}
					prov = (ComplexProvenance) graphUris2Prov.get(graphUri);
					String s = quadruple.getPredicate().toString().toLowerCase();
					if (s.equalsIgnoreCase(MetaVocabulary.AGENT)) {
						prov.getComplexAgent().add(ontology.createNamedIndividual(quadruple.getObject().toString()));
					} else if (s.equalsIgnoreCase(MetaVocabulary.CONFIDENCE_DEGREE)) {
						NumericMemLiteral num = (NumericMemLiteral) quadruple.getObject();
						prov.getComplexConfidenceDegree().add(num.doubleValue());
					} else if (s.equalsIgnoreCase(MetaVocabulary.CREATION_TIME)) {
						;
					} else if (s.equalsIgnoreCase(MetaVocabulary.SOURCE)) {
						prov.getComplexSource().add(ontology.createNamedIndividual(quadruple.getObject().toString()));
					}
				}
			}
		} catch (RepositoryException e1) {
			e1.printStackTrace();
		}
	}
	
	
	// ////////////////////////////////////////



	public ArrayList<String> getBindings() {

		return m_bindings;
	}

	// ////////////////////////////////////////
}
