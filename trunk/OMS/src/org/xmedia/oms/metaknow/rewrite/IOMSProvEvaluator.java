/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;

import org.xmedia.oms.metaknow.ComplexProvenance;

/**
 * @author bernie_2
 *
 */
public interface IOMSProvEvaluator {
	public  ComplexProvenance conj(ComplexProvenance p1, ComplexProvenance p2);
	public  ComplexProvenance disj(ComplexProvenance p1, ComplexProvenance p2);
	public  ComplexProvenance neg(ComplexProvenance p1);
}
