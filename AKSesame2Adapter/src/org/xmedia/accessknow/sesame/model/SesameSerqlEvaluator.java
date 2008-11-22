package org.xmedia.accessknow.sesame.model;

import java.io.StringReader;
import java.util.ArrayList;
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
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;
import org.xmedia.oms.metaknow.ComplexProvenance;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.MetaVocabulary;
import org.xmedia.oms.metaknow.ProvenanceUnknownException;
import org.xmedia.oms.metaknow.rewrite.AugmentVisitor;
import org.xmedia.oms.metaknow.rewrite.NodeProvenance;
import org.xmedia.oms.metaknow.rewrite.ProvenaceGraphNotSupportedException;
import org.xmedia.oms.metaknow.rewrite.ProvenanceVisitor;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.IQueryResult;
import org.xmedia.oms.query.IQueryWrapper;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryException;
import org.xmedia.oms.query.QueryResult;
import org.xmedia.oms.query.QueryWrapper;
import org.xmedia.oms.query.ResourceTuple;

public class SesameSerqlEvaluator implements IQueryEvaluator {
////////////////////////////////////////

	private long m_time1;

	private long m_time2;
	private long m_time2Tmp;

	private long m_time3;
	private long m_time3Tmp;
	private ArrayList<Long> m_time3Detailed;

	private long m_time4;
	private long m_time4Tmp;
	private ArrayList<Long> m_time4Detailed;

	private long m_timeStart;

	private ArrayList<String> m_bindings;
	private Map<String, IProvenance> m_processedTripleURIs = new HashMap<String, IProvenance>();
	////////////////////////////////////////
	
	private SesameOntology ontology = null;
	private Repository repository = null;
	
	public static String COMBINED_PROVENANCE = "cb";

	protected SesameSerqlEvaluator(Repository repository, SesameOntology ontology) throws RepositoryException {
		this.repository  = repository;
		this.ontology = ontology;
	}

	public IQueryResult evaluate(IQueryWrapper query) throws QueryException {

		IQueryResult theResult = null;
		RepositoryConnection conn = null;
		try {
			conn = repository.getConnection();
			TupleQuery compiledQuery = conn.prepareTupleQuery(QueryLanguage.SERQL, query.getQuery());
			TupleQueryResult results = compiledQuery.evaluate();
			try {
				String[] variablesNames = new String[results.getBindingNames().size()];
				variablesNames = results.getBindingNames().toArray(variablesNames);
				Set<ITuple> tuplesSet = new HashSet<ITuple>();

				while (results.hasNext()) {
					BindingSet aBindingSet = results.next();
					List<IResource> tuples = new ArrayList<IResource>();
					List<String> names = new ArrayList<String>();
					for(String aName : variablesNames) {
						Binding aBinding = aBindingSet.getBinding(aName);
						if (aBinding != null) {
							names.add(aName);
							tuples.add(Ses2AK.getObject(aBinding.getValue(), ontology));
						}
					}

					if (tuples.size() > 0) {
						String[] namesAsArray = new String[names.size()];
						namesAsArray = names.toArray(namesAsArray);
						tuplesSet.add(new ResourceTuple(tuples.size(), tuples.toArray(), namesAsArray));
					}
				}

				theResult = new QueryResult(tuplesSet, variablesNames);
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
			if (conn != null)
				try {
					conn.close();
				} catch (RepositoryException e) {
					throw new QueryException(query, e);
				}
		}

		return theResult;
	}

	public IQueryResult evaluateWithProvenance(IQueryWrapper query) throws QueryException {

		/* begin of step 1 */
		m_timeStart = System.currentTimeMillis();

		IQueryResult result = null;
		Query provQuery = null;
		String querystr = query.getQuery(); 
		try {
			provQuery = SPARQLParser.parse(new StringReader(querystr));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if(query == null) return null; 

		//create provenance expression using visitor 
		ProvenanceVisitor pv = new ProvenanceVisitor();
		((Node) provQuery).jjtAccept(pv);
		// create augmented query
		AugmentVisitor av = new AugmentVisitor();
		//((Node) provQuery).jjtAccept(av);
		String provQueryStr = av.augmentQuery(querystr);

		/* end of step 1 */
		m_time1 = System.currentTimeMillis();
		m_time1 = m_time1 - m_timeStart;

		m_timeStart = System.currentTimeMillis();

		RepositoryConnection conn = null;
		try {
			conn = repository.getConnection();

			//String provQueryStr = QueryFormatter.addURIBrackets(provQuery.toString());
			TupleQuery compiledQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, provQueryStr);
			TupleQueryResult results = compiledQuery.evaluate();

			/* end of step 2 */
//			m_time2 = System.currentTimeMillis();
//			m_time2 = m_time2 - m_timeStart;

			//with results, performed query to get provenances 
			Set<ITuple> tuples = new HashSet<ITuple>(); 
			String[] variablesNames = new String[results.getBindingNames().size()];
			variablesNames = results.getBindingNames().toArray(variablesNames);
			String[] varNamesAndProvenance = new String[variablesNames.length + 1];;
			String[] provVars = av.getUsedVars();
			List<Binding> provBindings = new ArrayList<Binding>();


			/* begin of step 3 and 4 */
			m_time2 = 0;
			m_time3 = 0;
			m_time4 = 0;

			m_time3Detailed = new ArrayList<Long>();
			m_time4Detailed = new ArrayList<Long>();		
			m_bindings = new ArrayList<String>();


			//clear the map that is used to store provenances that already have been computed
			m_processedTripleURIs.clear();

			while (results.hasNext()) {

				String help = "";

				BindingSet aBindingSet = results.next();
				for(String aName : variablesNames) 
				{
					if(isProvenanceVariable(provVars, aName))
					{
						provBindings.add(aBindingSet.getBinding(aName));
						help += ", "+aBindingSet.getBinding(aName).toString();
					}
				}

				m_bindings.add(help.substring(2));

				m_time2Tmp = System.currentTimeMillis();
				m_time2 += m_time2Tmp - m_timeStart;

				m_timeStart = System.currentTimeMillis();

				//merge provenances with provExpression
				@SuppressWarnings("unused")
				HashMap<String, org.xmedia.oms.metaknow.Provenance> graphProvenances = getGraphProvenances(provBindings, conn);

				@SuppressWarnings("unused")
				NodeProvenance np = pv.getProvenance();
				ComplexProvenance prov = null;


				/* end of step 3 and begin of stept 4 */
				m_time3Tmp = System.currentTimeMillis();
				m_time3Detailed.add(m_time3Tmp - m_timeStart);
				m_time3 += m_time3Tmp - m_timeStart;

				m_timeStart = System.currentTimeMillis();

				prov = np.getProvenanceFormula().getProvenance(graphProvenances);

				m_time4Tmp = System.currentTimeMillis();
				m_time4Detailed.add(m_time4Tmp - m_timeStart);
				m_time4 += m_time4Tmp - m_timeStart;

				m_timeStart = System.currentTimeMillis();

				Object[] tuple = new Object[variablesNames.length +1];

				for(int i = 0; i < variablesNames.length; i++) {
					Binding aBinding = aBindingSet.getBinding(variablesNames[i]);
					if (aBinding != null) {
						varNamesAndProvenance[i] = variablesNames[i];
						tuple[i] = Ses2AK.getObject(aBinding.getValue(), ontology);
					}
				}

				//add prov to last position 
				tuple[variablesNames.length] = prov; 
				varNamesAndProvenance[variablesNames.length] = COMBINED_PROVENANCE;				
				tuples.add(new ResourceTuple(variablesNames.length + 1, tuple, varNamesAndProvenance));


				m_time2Tmp = System.currentTimeMillis();
				m_time2 += m_time2Tmp - m_timeStart;

			}

			result = new QueryResult(tuples, varNamesAndProvenance);
			/* end of step 3 and 4 */

		} catch (RepositoryException e) {
			throw new QueryException(query, e);
		} catch (MalformedQueryException e) {
			throw new QueryException(query, e);
		} catch (QueryEvaluationException e) {
			throw new QueryException(query, e);
		} catch (Exception e) {
			throw new QueryException(query, e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (RepositoryException e) {
					throw new QueryException(query, e);
				}
		}

		return result;
	}


	private HashMap<String, org.xmedia.oms.metaknow.Provenance> getGraphProvenances(List<Binding> provBindings, RepositoryConnection conn) throws ProvenaceGraphNotSupportedException{
		if (provBindings == null || provBindings.size() == 0) return null;

		HashMap<String, org.xmedia.oms.metaknow.Provenance> graphProvenances = new HashMap<String, org.xmedia.oms.metaknow.Provenance>();
		for(Binding b : provBindings){

			/******compute the provenance graph using the uri of the triple (which is the URI of the graph that contains it) ****/
			String tripleGraphUri = b.getValue().toString();
			String tripleGraphName = b.getName();
			IProvenance provenance = null;

			//try to retrieve provenances from the cache
			provenance = m_processedTripleURIs.get(tripleGraphUri);
			
			if (provenance == null){
				Set<IProvenance> provenances = null;
				try {
					//retrieve provenances 
					provenances = getProvenances(tripleGraphUri);
				} catch (ProvenanceUnknownException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
				
				//assume that there is only one "provenance object" in the provenance graph 
				//TODO fusion of provenances in case there is more than one!
				if (provenances != null && provenances.size() == 1){
					provenance = provenances.iterator().next();
					m_processedTripleURIs.put(tripleGraphUri, provenance);
				}
			}

			graphProvenances.put("?" + tripleGraphName, (org.xmedia.oms.metaknow.Provenance)provenance);
		}
		return graphProvenances;
	}

	public Set<IProvenance> getProvenances(String reification) throws ProvenanceUnknownException {
		Set<IProvenance> provenances = new HashSet<IProvenance>();
		if (reification == null || reification.length() == 0) return null;
		else {

			try {

				IQueryWrapper theQuery = new QueryWrapper("SELECT DISTINCT * FROM "+
						               "{<"+reification+">} <"+MetaVocabulary.HAS_PROVENANCE+"> {provenance}, "+
						               "[{provenance} meta:agent {"+ProvSerqlVariable.AGENT + "}], " +
										"[{provenance} meta:confidence_degree {" +ProvSerqlVariable.CONFIDENCE + "}]," +
										"[{provenance} meta:creation_time {"+ ProvSerqlVariable.DATE + "}]," +
										"[{provenance} <"+MetaVocabulary.SOURCE+"> {"+ProvSerqlVariable.SOURCE + "}] "+
										"using namespace meta=<"+MetaVocabulary.METAKNOW_ONTOLOGY_URI+">",
										null);
					

				Set<ITuple> theResult = evaluate(theQuery).getResult();

				if (theResult.size() == 0)
					return null;
				else
					for(ITuple aTuple : theResult)
						provenances.add(createProvenance(aTuple));

			} catch (QueryException e) {
				e.printStackTrace();
			}

		}

		return provenances;
	}


	private boolean isProvenanceVariable(String[] provnanceVar, String var){
		if(provnanceVar == null || provnanceVar.length == 0) return false;
		for(int i = 0; i < provnanceVar.length; i++){
			if (provnanceVar[i].equals(var)) return true;
		}

		return false; 
	}

	private enum ProvSerqlVariable {

		AGENT ("agent"),
		SOURCE ("source"),
		CONFIDENCE ("confidence"),
		DATE ("date");

		private final String name;
		private static Map<String,ProvSerqlVariable> tokenMap;

		private ProvSerqlVariable(String name){
			this.name = name.toLowerCase();
			map(name,this);
		}

		private void map(String name, ProvSerqlVariable op){
			if (tokenMap==null) tokenMap = new HashMap<String, ProvSerqlVariable>();
			tokenMap.put(name,op);
		}

		public static ProvSerqlVariable forName(String name){
			return tokenMap.get(name);
		}

		public String getUrn() {
			return name;
		}
	}

	private Provenance createProvenance(ITuple queryResult) {

		INamedIndividual agent = null;
		double confidence = 0;
		Date date = null;
		INamedIndividual source = null;

		for (int i = 0; i < queryResult.getArity(); i++) {

			String value = queryResult.getElementAt(i).getLabel();
			ProvSerqlVariable variable = ProvSerqlVariable.forName(queryResult.getLabelAt(i).toLowerCase());
			if (variable != null)
				switch (variable) {
				case AGENT:
					agent = ontology.createNamedIndividual(value);
					break;
				case CONFIDENCE:
					confidence = Double.valueOf(value);
					break;
				case DATE:
					//TODO: need to construct a data from the string value 
					//date = new Date(value.toString());
					break;
				case SOURCE:
					source = ontology.createNamedIndividual(value);
					break;
				default:
					break;
				}

		}

		return new Provenance(confidence, agent, source, date);
	}

	//////////////////////////////////////////

	/**
	 * Used for benchmarking purposes of evaluateWithProvenance().
	 * Returns time (in ms) at the specified step. 
	 * 
	 *  @param step - int 
	 *  
	 */
	public long getTime(int step){

		switch(step)
		{
		case 1: return m_time1;	      	
		case 2: return m_time2;      
		case 3: return m_time3;
		case 4: return m_time4;
		default: return -1;

		}
	}

	/**
	 * Used for benchmarking purposes of evaluateWithProvenance().
	 * Returns time (in ms) at the specified step. 
	 * 
	 *  @param step - int 
	 *  
	 */
	public ArrayList<Long> getTimeDetailed(int step){

		switch(step)
		{
		case 3: return m_time3Detailed;	      	
		case 4: return m_time4Detailed;
		default: return null;                  
		}
	}

	public ArrayList<String> getBindings(){
		return m_bindings;
	}

	@Override
	public boolean hasResults(IQueryWrapper queryWrapper) throws QueryException {
		// TODO Auto-generated method stub
		return false;
	}

	//////////////////////////////////////////
	
}

