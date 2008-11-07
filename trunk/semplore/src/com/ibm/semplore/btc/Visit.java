/**
 * 
 */
package com.ibm.semplore.btc;

/**
 * @author xrsun
 *
 */
public interface Visit {
	/**
	 * @see QueryPlanner.startTraverse
	 * @param parent parent of current node
	 * @param o current node in traverse
	 */
	public void visit(Object parent, Object o);
}
