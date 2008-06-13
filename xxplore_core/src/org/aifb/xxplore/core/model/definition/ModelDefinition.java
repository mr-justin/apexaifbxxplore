package org.aifb.xxplore.core.model.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.shared.util.IModelChangeListener;
import org.aifb.xxplore.shared.util.ModelChangeEvent;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.IDataSource;
import org.xmedia.oms.query.ConceptMemberPredicate;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.PropertyMemberPredicate;
import org.xmedia.oms.query.QueryWrapper;
import org.xmedia.oms.query.Variable;


public class ModelDefinition extends AbstractDefinition implements IModelDefinition{

	private Collection<IModelChangeListener> m_listeners;

	/**
	 * These are keywords 
	 */
	private String m_query; 
	
	private QueryWrapper m_dlquery;
	
	private List<DefinitionTuple> m_currentCompleteDefinitionTuples;

	private DefinitionTuple m_currentIncompleteTuple;
	
	private String m_currentVarName;
	
	private int m_varSize;
	
	private static int m_varIndex = 0;
	
	private Map<String,List<DefinitionTuple>> m_completeDefinitionTuples;
	
	private Map<String,DefinitionTuple> m_incompleteTuples;

	public class DefinitionTuple {
		
		private ModelDefinition m_modelDefinition;
		private IRelationDefinition m_relation;
		private IDefinition m_object;

		public DefinitionTuple(IRelationDefinition rel, IDefinition def, ModelDefinition md) {
			m_relation = rel;
			m_object = def;
			m_modelDefinition = md;
		}

		public IDefinition getObjectDefinition() {
			return m_object;
		}

		public IRelationDefinition getRelationDefinition() {
			return m_relation;
		}
		
		public IModelDefinition getModelDefinition() {
			return m_modelDefinition;
		}

		public void setObjectDefinition(IDefinition m_object) {
			this.m_object = m_object;
		}

		public void setRelationDefinition(IRelationDefinition m_relation) {
			this.m_relation = m_relation;
		}

		public boolean isIncomplete() {
			if (m_relation.getDefinition() == null) {
				return true;
			}

			if ((m_object instanceof EntityDefinition) && (((EntityDefinition)m_object).getDefinition() == null)) {
				return true;
			}

			return false;
		}
		
		@Override
		public String toString() {
			String out = new String();
			out += "[";
			out += "m_relation = ";
			out += m_relation + ", ";
			out += "m_object = ";
			out += m_object;
			out += "]";
			return out;
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj instanceof DefinitionTuple){
				DefinitionTuple dt = (DefinitionTuple)obj;
//				return (this.m_modelDefinition == dt.m_modelDefinition) && 
				return	(this.m_relation.equals(dt.m_relation))
						&& (this.m_object.equals(dt.m_object));
			}

			return false;
		}
	}

	public ModelDefinition(IDataSource datasource) {
		super(datasource);
		m_listeners = new ArrayList<IModelChangeListener>();
		m_currentVarName = "x";
		m_varSize = 1;
		
		m_completeDefinitionTuples = new LinkedHashMap<String, List<DefinitionTuple>>();
		m_incompleteTuples = new LinkedHashMap<String, DefinitionTuple>();
		
		m_currentCompleteDefinitionTuples = new ArrayList<DefinitionTuple>();
		m_currentIncompleteTuple = new DefinitionTuple(new RelationDefinition(getDataSource()), new EntityDefinition(getDataSource(), IEntityDefinition.OBJECT), this);
		
	}
	
	public int getVarSize() {
		return m_varSize;
	}
	
	public String getVariableName() {
		return m_currentVarName;
	}
	
	public Set<String> getVariableNames() {
		return m_completeDefinitionTuples.keySet();
	}
	
	public String getNextVarName() {
		return "x" + m_varIndex;
	}
	
	public void setVariableName(String varName) {
		if (m_completeDefinitionTuples.containsKey(varName)) {
			m_currentVarName = varName;
			m_currentCompleteDefinitionTuples = m_completeDefinitionTuples.get(varName);
			m_currentIncompleteTuple = m_incompleteTuples.get(varName);
		} else {
			addVariableName(varName);
		}
	}

	public void addVariableName(String varName) {
		if (!(getVariableName().equals("x"))) {
			m_currentVarName = varName;
			m_varSize++;
			m_varIndex++;
			m_currentCompleteDefinitionTuples = new ArrayList<DefinitionTuple>();
			m_currentIncompleteTuple = new DefinitionTuple(new RelationDefinition(getDataSource()), new EntityDefinition(getDataSource(), IEntityDefinition.OBJECT), this);
			
			m_completeDefinitionTuples.put(m_currentVarName, m_currentCompleteDefinitionTuples);
			m_incompleteTuples.put(m_currentVarName, m_currentIncompleteTuple);
		}
		else {
			m_currentVarName = varName;
			m_varIndex++;
			m_completeDefinitionTuples.put(m_currentVarName, m_currentCompleteDefinitionTuples);
			m_incompleteTuples.put(m_currentVarName, m_currentIncompleteTuple);
		}	
	}
	
//	 TODO
	public void clear() {
		m_currentVarName = "x";
		m_varSize = 1;
		m_varIndex = 0;
		m_completeDefinitionTuples.clear();
		m_incompleteTuples.clear();
		
		m_currentCompleteDefinitionTuples = new ArrayList<DefinitionTuple>();
		m_currentIncompleteTuple = new DefinitionTuple(new RelationDefinition(getDataSource()), new EntityDefinition(getDataSource(), IEntityDefinition.OBJECT), this);
		
		m_query = null;
		m_dlquery = null;
		
	}
	
	public Map<String,List<DefinitionTuple>> getAllCompleteDefinitionTuples() {
		return m_completeDefinitionTuples;
	}
	
	public Map<String,DefinitionTuple> getAllIncompleteDefinitionTuples() {
		return m_incompleteTuples;
	}
	
	public List<DefinitionTuple> getCompleteDefinitionTuples() {
		return m_currentCompleteDefinitionTuples;
	}

	public IRelationDefinition getRelationDefinition() {
		return m_currentIncompleteTuple.getRelationDefinition();
	}

	public IDefinition getObjectDefinition() {
		return m_currentIncompleteTuple.getObjectDefinition();
	}

	public void addModelChangeListener(IModelChangeListener listener) {

		if (!m_listeners.contains(listener)){
			m_listeners.add(listener);
		}		
	}

	public void removeModelChangeListener(IModelChangeListener listener) {
		if(m_listeners.contains(listener)){
			m_listeners.remove(listener);
		}
	}

	/**
	 * Helper method for subclasses to fire IModelChangeEvent.
	 */
	protected void fireStateChanged(ModelChangeEvent event) {
		for (IModelChangeListener listener : m_listeners) {
			listener.modelChanged(event);
		}

//		if (getSuperDefinition() != null) {
//			if (getSuperDefinition() instanceof ModelDefinition) {
//				((ModelDefinition)getSuperDefinition()).fireStateChanged(event);
//			}
//		}
	}
	
	public boolean hasIncompleteTuple(){
		return m_currentIncompleteTuple.isIncomplete();
	}

	public void setObjectDefinition(IDefinition definition) {
		m_currentIncompleteTuple.setObjectDefinition(definition);
		definition.setSuperDefinition(this);

		if (!m_currentIncompleteTuple.isIncomplete()) {
			m_currentCompleteDefinitionTuples.add(m_currentIncompleteTuple);
			m_currentIncompleteTuple = new DefinitionTuple(new RelationDefinition(getDataSource()), new EntityDefinition(getDataSource(), IEntityDefinition.OBJECT), this);
			m_incompleteTuples.put(m_currentVarName, m_currentIncompleteTuple);
		} else {
			m_incompleteTuples.put(m_currentVarName, m_currentIncompleteTuple);
		}
		fireStateChanged(new ModelChangeEvent(this, ModelChangeEvent.OBJECT_CHANGE));

	}

	public void setRelationDefinition(IRelationDefinition definition) {
		m_currentIncompleteTuple.setRelationDefinition(definition);
		definition.setSuperDefinition(this);

		if (!m_currentIncompleteTuple.isIncomplete()) {
			m_currentCompleteDefinitionTuples.add(m_currentIncompleteTuple);
			m_currentIncompleteTuple = new DefinitionTuple(new RelationDefinition(getDataSource()), new EntityDefinition(getDataSource(), IEntityDefinition.OBJECT), this);
			m_incompleteTuples.put(m_currentVarName, m_currentIncompleteTuple);
		} else {
			m_incompleteTuples.put(m_currentVarName, m_currentIncompleteTuple);
		}
		fireStateChanged(new ModelChangeEvent(this, ModelChangeEvent.PREDICATE_CHANGE));
	}
	

	public void addCompleteDefinitionTuple(IRelationDefinition rd, IDefinition ed) {
		m_currentCompleteDefinitionTuples.add(new DefinitionTuple(rd, ed, this));
		fireStateChanged(new ModelChangeEvent(this, ModelChangeEvent.SUBJECT_CHANGE));
	}
	
	//TODO
	public boolean containsCompleteDefinitionTuple(RelationDefinition rd, IDefinition ed){
		
		return m_currentCompleteDefinitionTuples.contains(new DefinitionTuple(rd, ed, this));
		
	}
	
	
	public void removeCompleteDefinitionTuple(DefinitionTuple remove) {
		if (m_currentCompleteDefinitionTuples.remove(remove)) {
			fireStateChanged(new ModelChangeEvent(this, ModelChangeEvent.SUBJECT_CHANGE));
		}
	}
	
	public void setQuery(String query){

		m_query = query;
		fireStateChanged(new ModelChangeEvent(this, ModelChangeEvent.QUERY_CHANGE));
	}

	public String getQuery(){
		return m_query;
	}
	
	public void setDLQuery(QueryWrapper dlquery){
		m_dlquery = dlquery;
	}

	public QueryWrapper getDLQuery(){
		return m_dlquery;
	}

	public void getSelectedQuery(ModelDefinition md, Collection<OWLPredicate> selectedQuery){
		Map<String,List<DefinitionTuple>> completeDefinitionTuples = md.getAllCompleteDefinitionTuples();
		if ((completeDefinitionTuples != null) && (completeDefinitionTuples.size() != 0)) {
			for (String key : completeDefinitionTuples.keySet()) {
				Variable sub = new Variable(key);
				List<DefinitionTuple> tuples = completeDefinitionTuples.get(key);
				for (DefinitionTuple tuple : tuples) {
					IProperty prop = tuple.getRelationDefinition().getDefinition();
					IResource obj = null;
					IDefinition objectDefinition = tuple.getObjectDefinition();
//					System.out.println(sub);
//					System.out.println(prop);
//					System.out.println(objectDefinition);
					if (objectDefinition instanceof EntityDefinition) {
						obj = ((EntityDefinition) objectDefinition).getDefinition();
					} else if (objectDefinition instanceof ModelDefinition) {
						String varName = ((ModelDefinition) objectDefinition).getVariableName();
						obj = new Variable(varName);
						getSelectedQuery((ModelDefinition) objectDefinition,selectedQuery);
					}
					if (prop.equals(Property.IS_INSTANCE_OF) && (obj instanceof INamedConcept)) {
						selectedQuery.add(new ConceptMemberPredicate(obj, sub));
					} else if (!prop.equals(Property.IS_INSTANCE_OF) && !(obj instanceof INamedConcept)) {
						selectedQuery.add(new PropertyMemberPredicate(prop,sub, obj));
					}
				}
			}
		}
	}
	
	public boolean isComplex() {
		// TODO Auto-generated method stub
		return false;
	}
	
	//TODO
	@Override
	public String toString() {
		
		String out = new String();
		out += "[complete tuples: " ;
		out += m_completeDefinitionTuples;
		out += "| incomplete tuples: ";
		out += m_incompleteTuples;
		out += "]";
		return out;
	}
	
	//TODO
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof ModelDefinition) 
		{
			ModelDefinition modeldef = (ModelDefinition) obj;
			return (m_incompleteTuples.equals(modeldef.m_incompleteTuples))
					&& (m_completeDefinitionTuples.equals(modeldef.m_completeDefinitionTuples));
		}
		else
		{
			return Boolean.FALSE;
		}
	}
	
	/**
	 * A simple hash function from Robert Sedgwicks Algorithms in C book. Please see:
	 * <a href="http://www.partow.net/programming/hashfunctions/">http://www.partow.net/programming/hashfunctions/</a>.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	
	@Override
	public int hashCode(){
		
		String moddef = this.toString();
		
		int b     = 378551;
	    int a     = 63689;
	    long hash = 0;

	    for(int i = 0; i < moddef.length(); i++)
	    {
	       hash = hash * a + moddef.charAt(i);
	       a    = a * b;
	    }

	    return (int)hash;
	}
}
