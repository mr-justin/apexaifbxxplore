package org.aifb.xxplore.core.service.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.model.definition.EntityDefinition;
import org.aifb.xxplore.core.model.definition.IDefinition;
import org.aifb.xxplore.core.model.definition.IEntityDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.model.definition.RelationDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition.DefinitionTuple;
import org.aifb.xxplore.core.service.IService;
import org.aifb.xxplore.core.service.IServiceListener;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.aifb.xxplore.shared.vocabulary.XMLSchema;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Individual;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;
import org.xmedia.oms.query.ConceptMemberPredicate;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.PropertyMemberPredicate;
import org.xmedia.oms.query.QueryException;
import org.xmedia.oms.query.QueryWrapper;
import org.xmedia.oms.query.Variable;

public class QueryTranslationService implements IService {

	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub
		
	}

	public void disposeService() {
		// TODO Auto-generated method stub
		
	}

	public void init(Object... params) {

		
	}
	
	private String getDelimitedIRI(String uri) {
		return "<" + uri + ">";
	}
	
	private IDatatype getDatatype(ILiteral literal) {
		
		if(literal.getDatatypes() == null)
		{
			return ExploreEnvironment.DEFAULT_DATATYPE;
		}
		else
		{
			IDatatype out = null;
			
			for (IDatatype datatype : literal.getDatatypes())
			{
				out = datatype;
				break;
			}
			
			return out;
		}
	}
	
	/**
	 * Translates a collection of predicates and select variables to a corresponding SPARQL query. 
	 * @param predicates
	 * @param variables
	 * @return
	 */
	public QueryWrapper translate2SparqlQuery(Collection<OWLPredicate> predicates, Collection<Variable> variables){
		
		if((predicates == null) || (predicates.size() == 0)) {
			return null;
		}
		String whereClause = "WHERE { " ;
		for(OWLPredicate predicate : predicates){
			
			if(predicate instanceof ConceptMemberPredicate){
				if(!(((ConceptMemberPredicate)predicate).getConcept() instanceof INamedConcept)) {
					continue;
				}
				String con = getDelimitedIRI(((INamedConcept)((ConceptMemberPredicate)predicate).getConcept()).getUri());
				String term = ""; 
				IResource res = ((ConceptMemberPredicate)predicate).getTerm();
				if(res instanceof NamedIndividual) {
					term = getDelimitedIRI(((NamedIndividual)res).getUri());
				} else if (res instanceof Variable) {
					term = "?" + ((Variable)res).getName();
				} else if (res instanceof ILiteral) {
					term = "\"" + ((ILiteral)res).getLiteral() + "\"^^<" + getDatatype((ILiteral)res) + ">";
				}
				
				whereClause += term + " " + getDelimitedIRI(ExploreEnvironment.IS_INSTANCE_OF_URI) + " " + con + " . ";
			}
			else if (predicate instanceof PropertyMemberPredicate){
				IResource res1 = ((PropertyMemberPredicate)predicate).getFirstTerm();
				IResource res2 = ((PropertyMemberPredicate)predicate).getSecondTerm();
				IProperty prop = ((PropertyMemberPredicate)predicate).getProperty();
				String term1 = ""; 
				if(res1 instanceof NamedIndividual) {
					term1 = getDelimitedIRI(((NamedIndividual)res1).getUri());
				} else if (res1 instanceof Variable) {
					term1 = "?" + ((Variable)res1).getName();
				} else if (res1 instanceof ILiteral) {
					
					ILiteral lit = (ILiteral)res1;						
					term1 = "\"" + lit.getLiteral();
					
					Iterator<IDatatype> iter = lit.getDatatypes().iterator();
					
					if(iter.hasNext()){
						
						IDatatype datatype = iter.next();
						
						if(datatype.getLabel().equals(XMLSchema.STRING)){
							
							if(lit.getLanguage()!= null){
								term1 += "\"@"+lit.getLanguage();
							}
							else{
								term1 += "\"@"+ExploreEnvironment.DEFAULT_LITERAL_LANGUAGE;
							}
						}
						else{
							term1 += "\"^^<" + getDatatype((ILiteral)res1) + ">";
						}
					}
				}
				
				String term2 = ""; 
				if(res2 instanceof NamedIndividual) {
					term2 = getDelimitedIRI(((NamedIndividual)res2).getUri());
				} else if (res2 instanceof Variable) {
					term2 = "?" + ((Variable)res2).getName();
				} else if (res2 instanceof ILiteral) {
					
					ILiteral lit = (ILiteral)res2;						
					term2 = "\"" + lit.getLiteral();
					
					Iterator<IDatatype> iter = lit.getDatatypes().iterator();
					
					if(iter.hasNext()){
						
						IDatatype datatype = iter.next();
						
						if(datatype.getLabel().equals(XMLSchema.STRING)){
							
							if(lit.getLanguage()!= null){
								term2 += "\"@"+lit.getLanguage();
							}
							else{
								term2 += "\"@"+ExploreEnvironment.DEFAULT_LITERAL_LANGUAGE;
							}
						}
						else{
							term2 += "\"^^<" + getDatatype((ILiteral)res2) + ">";
						}
					}
				}

				whereClause += term1 + " " + getDelimitedIRI(prop.getUri()) + " " + term2 + " . ";
			}
		}
		whereClause += " }";
		
		String selectClause = "SELECT "; 
		String[] vars = new String[variables.size()];
		int i = 0;
		for (Variable var : variables){
			String varName = var.getName().startsWith("?") ? var.getName().substring(1) : var.getName();
			selectClause +=  ("?" + varName) + " ";
			vars[i] = var.getName();
			i++;
		}
		
		String query = selectClause + whereClause;
//		System.out.println(query);
		
		return new QueryWrapper(query, vars);
		
	}

	/**
	 * Translates a collection of predicates to a corresponding ModelDefinition. 
	 * @param predicates
	 * @return
	 */
	public ModelDefinition translate2ModelDefinition(Collection<OWLPredicate> predicates,ModelDefinition modeldefinition) {
		Map<Variable,List<OWLPredicate>> domainVar2Predicates = new LinkedHashMap<Variable,List<OWLPredicate>>();
		Map<Variable,List<OWLPredicate>> rangeVar2Predicates = new LinkedHashMap<Variable,List<OWLPredicate>>();

		for (OWLPredicate pred : predicates) {
			if (pred instanceof PropertyMemberPredicate) {
				PropertyMemberPredicate pmp = (PropertyMemberPredicate)pred;
				if (pmp.getFirstTerm() instanceof Variable) {
					if (!domainVar2Predicates.containsKey(pmp.getFirstTerm())) {
						domainVar2Predicates.put((Variable)pmp.getFirstTerm(), new ArrayList<OWLPredicate>());
					}
					domainVar2Predicates.get(pmp.getFirstTerm()).add(pmp);
				}

				if (pmp.getSecondTerm() instanceof Variable) {
					if (!rangeVar2Predicates.containsKey(pmp.getSecondTerm())) {
						rangeVar2Predicates.put((Variable)pmp.getSecondTerm(), new ArrayList<OWLPredicate>());
					}
					rangeVar2Predicates.get(pmp.getSecondTerm()).add(pmp);
				}
			}
			if (pred instanceof ConceptMemberPredicate) {
				ConceptMemberPredicate cmp = (ConceptMemberPredicate)pred;
				if (cmp.getTerm() instanceof Variable) {
					if (!domainVar2Predicates.containsKey(cmp.getTerm())) {
						domainVar2Predicates.put((Variable)cmp.getTerm(), new ArrayList<OWLPredicate>());
					}
					domainVar2Predicates.get(cmp.getTerm()).add(cmp);
				}
			}
		}

		Map<Variable,List<DefinitionTuple>> domainVarWithTuples = new LinkedHashMap<Variable,List<DefinitionTuple>>();
		Map<Variable,List<DefinitionTuple>> domainVarWithRangeVars = new LinkedHashMap<Variable,List<DefinitionTuple>>();
		
		for (Variable var : domainVar2Predicates.keySet()) {
			List<DefinitionTuple> definitionTuples = new ArrayList<DefinitionTuple>();
			domainVarWithTuples.put(var, definitionTuples);

			for (OWLPredicate pred : domainVar2Predicates.get(var)) {
				if (pred instanceof PropertyMemberPredicate) {
					PropertyMemberPredicate pmp = (PropertyMemberPredicate)pred;
					if (!(pmp.getSecondTerm() instanceof Variable)) {
						RelationDefinition rd = new RelationDefinition(modeldefinition.getDataSource(), pmp.getProperty());
						EntityDefinition ed = new EntityDefinition(modeldefinition.getDataSource(), IEntityDefinition.OBJECT, pmp.getSecondTerm());
						definitionTuples.add(modeldefinition.new DefinitionTuple(rd, ed, modeldefinition));
					}
					else {
						RelationDefinition rd = new RelationDefinition(modeldefinition.getDataSource(), pmp.getProperty());
						Variable rangeVar = (Variable)pmp.getSecondTerm();
						ModelDefinition submodel = new ModelDefinition(modeldefinition.getDataSource()); 
						submodel.setVariableName(rangeVar.getName());
						submodel.setSuperDefinition(modeldefinition);
						DefinitionTuple tuple = modeldefinition.new DefinitionTuple(rd, submodel, modeldefinition);
						definitionTuples.add(tuple);
						
						if (!domainVarWithRangeVars.containsKey(var)) {
							domainVarWithRangeVars.put(var, new ArrayList<DefinitionTuple>());
						}
						domainVarWithRangeVars.get(var).add(tuple);
					}
				}

				if (pred instanceof ConceptMemberPredicate) {
					ConceptMemberPredicate cmp = (ConceptMemberPredicate)pred;
					RelationDefinition rd = new RelationDefinition(modeldefinition.getDataSource(), Property.IS_INSTANCE_OF);
					EntityDefinition ed = new EntityDefinition(modeldefinition.getDataSource(), IEntityDefinition.OBJECT, cmp.getConcept()); 
					definitionTuples.add(modeldefinition.new DefinitionTuple(rd, ed, modeldefinition));
				}
			}
		}
		
		domainVarWithRangeVars.keySet().removeAll(rangeVar2Predicates.keySet());
		for (Variable domainVar : domainVarWithRangeVars.keySet()) {
			List<DefinitionTuple> tupless= domainVarWithRangeVars.get(domainVar);
			
			if((tupless != null) && (tupless.size() > 0)){
				for (DefinitionTuple tuple : tupless) {
					recursive(domainVar, tuple, domainVarWithTuples);
				}
			}
		}
		
		domainVar2Predicates.keySet().removeAll(rangeVar2Predicates.keySet());
		for (Variable var : domainVar2Predicates.keySet()) {
			modeldefinition.setVariableName(var.getName());
			List<DefinitionTuple> tuples = domainVarWithTuples.get(var);
			for(DefinitionTuple tuple : tuples) {
				modeldefinition.addCompleteDefinitionTuple(tuple.getRelationDefinition(), tuple.getObjectDefinition());
			}
		}
		return modeldefinition;
	}

	protected void recursive(Variable domainVar, DefinitionTuple definitionTuple, Map<Variable,List<DefinitionTuple>> domainVarWithTuples) {
		if(definitionTuple.getObjectDefinition() instanceof ModelDefinition) {
			ModelDefinition modeldefinition = (ModelDefinition)definitionTuple.getObjectDefinition();
			Variable var = new Variable(modeldefinition.getVariableName());
			if(domainVarWithTuples.containsKey(var)) {
				List<DefinitionTuple> tuples = domainVarWithTuples.get(var);
				for(DefinitionTuple tuple : tuples) {
					IDefinition definition = tuple.getObjectDefinition();
					modeldefinition.addCompleteDefinitionTuple(tuple.getRelationDefinition(), definition);
					if((definition instanceof ModelDefinition) && (((ModelDefinition)definition).getVariableName()!=domainVar.getLabel())) {
						recursive(new Variable(((ModelDefinition)definition).getVariableName()), tuple, domainVarWithTuples);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void test() throws QueryException {
//		IConceptDao cDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
//		IDatatypeDao dDao = (IDatatypeDao) PersistenceUtil.getDaoManager().getAvailableDao(IDatatypeDao.class);
//		IPropertyMemberAxiomDao paDao = (IPropertyMemberAxiomDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		IPropertyDao pDao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
		IIndividualDao iDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
//		QueryTranslationService qtservice = new QueryTranslationService();
		
//		qtservice.test();
		
		List<OWLPredicate> predicates = new ArrayList<OWLPredicate>();
		List<Variable> variables = new ArrayList<Variable>();
		
		Variable var1 = new Variable("x"+String.valueOf(UniqueIdGenerator.getInstance().getNewId()));
//		Variable var2 = new Variable("x"+String.valueOf(UniqueIdGenerator.getInstance().getNewId()));
		
//		NamedConcept publication = (NamedConcept)cDao.findByUri("http://swrc.ontoware.org/ontology#Publication");
//		NamedConcept organization = (NamedConcept)cDao.findByUri("http://swrc.ontoware.org/ontology#Organization");
//		Property name = (Property)pDao.findByUri("http://swrc.ontoware.org/ontology#name");
		Property affiliation = (Property)pDao.findByUri("http://swrc.ontoware.org/ontology#affiliation");
		Individual id2096 = (Individual)iDao.findByUri("http://www.aifb.uni-karlsruhe.de/Personen/viewPersonOWL/id2096instance");
		
//		Set<IPropertyMember> propMembers = paDao.findByTargetValue(new Literal("Wallenberg Global Learning Network"));
//		for (IPropertyMember propMember : propMembers) {
//			ILiteral lit = (ILiteral)propMember.getTarget();
//			System.out.println(lit.getDatatypes());
//		}
//		predicates.add(new ConceptMemberPredicate(organization, var1));
//		predicates.add(new PropertyMemberPredicate(name, var1, new Literal("Wallenberg Global Learning Network")));
		predicates.add(new PropertyMemberPredicate(affiliation, id2096, var1));
		
		variables.add(var1);
		
//		QueryWrapper q = qtservice.translate2SparqlQuery(predicates, variables);
		
//		IQueryEvaluator eval;
//		try {
//			eval = PersistenceUtil.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);
			
//			IQueryResult result = eval.evaluate(q);
//			Set<ITuple> results = result.getResult();
//			for (ITuple x : results){
//				System.out.println(((ResourceTuple)x).getElementAt(0).getLabel());
//				System.out.println(x.getLabelAt(0));
//			}

//		} catch (QueryEvaluatorUnavailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public Set<String[]> translate2StringTriples(Collection<OWLPredicate> predicates, Set<Variable> variables) {
		Set<String[]> triples = new HashSet<String[]>();
		
		if((predicates == null) || (predicates.size() == 0)) {
			return null;
		}
		
		for(OWLPredicate predicate : predicates){
			String[] triple = new String [3];
			if(predicate instanceof ConceptMemberPredicate){
				if(!(((ConceptMemberPredicate)predicate).getConcept() instanceof INamedConcept)) {
					continue;
				}
				String con = getDelimitedIRI(((INamedConcept)((ConceptMemberPredicate)predicate).getConcept()).getUri());
				String term = ""; 
				IResource res = ((ConceptMemberPredicate)predicate).getTerm();
				if(res instanceof NamedIndividual) {
					term = getDelimitedIRI(((NamedIndividual)res).getUri());
				} else if (res instanceof Variable) {
					term = "?" + ((Variable)res).getName();
				} else if (res instanceof ILiteral) {
					term = "\"" + ((ILiteral)res).getLiteral() + "\"^^<" + getDatatype((ILiteral)res) + ">";
				}
				
				triple[0] = term;
				triple[1] = getDelimitedIRI(ExploreEnvironment.IS_INSTANCE_OF_URI);
				triple[2] = con;
			}
			else if (predicate instanceof PropertyMemberPredicate){
				IResource res1 = ((PropertyMemberPredicate)predicate).getFirstTerm();
				IResource res2 = ((PropertyMemberPredicate)predicate).getSecondTerm();
				IProperty prop = ((PropertyMemberPredicate)predicate).getProperty();
				String term1 = ""; 
				if(res1 instanceof NamedIndividual) {
					term1 = getDelimitedIRI(((NamedIndividual)res1).getUri());
				} else if (res1 instanceof Variable) {
					term1 = "?" + ((Variable)res1).getName();
				} else if (res1 instanceof ILiteral) {
					
					ILiteral lit = (ILiteral)res1;						
					term1 = "\"" + lit.getLiteral();
					
					Iterator<IDatatype> iter = lit.getDatatypes().iterator();
					
					if(iter.hasNext()){
						
						IDatatype datatype = iter.next();
						
						if(datatype.getLabel().equals(XMLSchema.STRING)){
							
							if(lit.getLanguage()!= null){
								term1 += "\"@"+lit.getLanguage();
							}
							else{
								term1 += "\"@"+ExploreEnvironment.DEFAULT_LITERAL_LANGUAGE;
							}
						}
						else{
							term1 += "\"^^<" + getDatatype((ILiteral)res1) + ">";
						}
					}
				}
				
				String term2 = ""; 
				if(res2 instanceof NamedIndividual) {
					term2 = getDelimitedIRI(((NamedIndividual)res2).getUri());
				} else if (res2 instanceof Variable) {
					term2 = "?" + ((Variable)res2).getName();
				} else if (res2 instanceof ILiteral) {
					
					ILiteral lit = (ILiteral)res2;						
					term2 = "\"" + lit.getLiteral();
					
					Iterator<IDatatype> iter = lit.getDatatypes().iterator();
					
					if(iter.hasNext()){
						
						IDatatype datatype = iter.next();
						
						if(datatype.getLabel().equals(XMLSchema.STRING)){
							
							if(lit.getLanguage()!= null){
								term2 += "\"@"+lit.getLanguage();
							}
							else{
								term2 += "\"@"+ExploreEnvironment.DEFAULT_LITERAL_LANGUAGE;
							}
						}
						else{
							term2 += "\"^^<" + getDatatype((ILiteral)res2) + ">";
						}
					}
				}

				triple[0] = term1;
				triple[1] = getDelimitedIRI(prop.getUri());
				triple[2] = term2;
			}
			triples.add(triple);
		}
		
		return triples;
	}
	
}
