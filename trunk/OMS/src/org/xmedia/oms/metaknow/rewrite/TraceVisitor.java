/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;

import name.levering.ryan.sparql.parser.model.ASTGraph;
import name.levering.ryan.sparql.parser.model.SimpleNode;
import name.levering.ryan.sparql.parser.model.SimpleVisitor;

/**
 * @author bernie_2
 *
 */
public class TraceVisitor extends SimpleVisitor {

	/* (non-Javadoc)
	 * @see name.levering.ryan.sparql.parser.model.SimpleVisitor#visit(name.levering.ryan.sparql.parser.model.SimpleNode)
	 */

	@Override
	public void visit(SimpleNode node) {
		System.out.println(node.getClass());
		System.out.println(node.toString(" "));
		return;
	}
	
    /* (non-Javadoc)
     * @see name.levering.ryan.sparql.parser.model.SimpleVisitor#visit(name.levering.ryan.sparql.parser.model.ASTGraph)
     */
    public void visit(ASTGraph node) {
        this.visit((SimpleNode) node);
    }

}
