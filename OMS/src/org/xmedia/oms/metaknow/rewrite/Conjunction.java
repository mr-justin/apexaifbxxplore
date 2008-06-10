/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;

import java.util.Map;

import org.xmedia.oms.metaknow.ComplexProvenance;

/**
 * @author bernie_2
 *
 */
public class Conjunction extends Formula {

		
	
	/**
	 * 
	 */
	public Conjunction() {
	}

	/**
	 * @param child
	 * @param child2
	 */
	public Conjunction(Formula child, Formula child2) {
		super(child, child2);
		// TODO Auto-generated constructor stub
	}

	public void eval(Map<String, ComplexProvenance> graphProvenances) {
		super.eval(graphProvenances);
		this.provenance = Formula.provEvaluator.conj(this.lChild.provenance, this.rChild.provenance);
	}

	public String toString() {
		return "("+this.lChild.toString() + "^"+this.rChild.toString() + ")";
	}
	
}
