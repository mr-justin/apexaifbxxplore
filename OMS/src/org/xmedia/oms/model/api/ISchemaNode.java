package org.xmedia.oms.model.api;

import java.util.Map;
import java.util.Set;

public interface ISchemaNode {
	
	/**    
	 * Returns the set of mutually equivalent classes in this node
	 */
	public Set<INamedConcept> getConcepts();
	
	
	/**    
	 * Returns the set of nodes related with this nodes via the given property 
	 */
	public Map<String, ISchemaNode>getRelatedNodes();
	
	/**    
	 * Returns the set of nodes related with this nodes via the returned property.
	 * This node is the range of the returned property.
	 */
	public Map<String, ISchemaNode>getRelatedFromNodes();
	
	/**    
	 * Returns the set of nodes related with this nodes via the returned property.
	 * This node is the domain of the returned property.
	 */
	public Map<String, ISchemaNode>getRelatedToNodes();
	
	public Map<String, ISchemaNode>getAllRelatedNodes();	
	
	public Map<String, ISchemaNode>getAllRelatedFromNodes();	
	
	public Map<String, ISchemaNode>getAllRelatedToNodes();	
}
