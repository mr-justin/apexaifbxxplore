/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;
import org.xmedia.oms.metaknow.ComplexProvenance;
import java.util.Map;

/**
 * @author bernie_2
 * 
 * Only child 'lChild' is used.
 */
public class Negation extends Formula {

	
	
	
	/**
	 * 
	 */
	public Negation() {
	}

	/**
	 * @param child
	 * @param child2
	 */
	public Negation(Formula child) {
		super(child, null);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmedia.sparql.prov.eval.Formula#eval()
	 */
	@Override
	public void eval(Map<String, ComplexProvenance> graphProvenances) {
		this.lChild.eval(graphProvenances);
		this.provenance = Formula.provEvaluator.neg(this.lChild.provenance);
	}

	public String toString() {
		return "!" + this.lChild.toString();
	}
}
