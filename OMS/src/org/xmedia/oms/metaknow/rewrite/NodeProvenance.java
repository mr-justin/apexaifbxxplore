/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;

import name.levering.ryan.sparql.parser.model.SimpleNode;

/**
 * @author bernie_2
 * For the representation of abstract provenance of a node in the parse tree.
 */
public class NodeProvenance {
	
	/**For debug: expression mirroring parse tree of relevant parts of query
	 */
	protected String parseTree = "";
	/**Provenance expression as String
	 */
	protected String provString = "";
	protected Formula provFormula = null;
	protected String literals = "";
	protected String parseNodeClass = "";
	
/*	
	public NodeProvenance(String s){
		provString=s;
	}
*/

	public NodeProvenance(String parseTree, String provString, SimpleNode n, Formula f){
		this.parseTree = parseTree;
		this.provString = provString;
		this.literals = n.toString();
		this.parseNodeClass = n.getClass().toString();
		this.provFormula = f;
	}

	public String toString(){
		return "(" + this.provString + ", \"" + this.literals+"\", " + this.parseNodeClass + ")";
	}
	
	public Formula getProvenanceFormula(){
		return provFormula;
	}
}
