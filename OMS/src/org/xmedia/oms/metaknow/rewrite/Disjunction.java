/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;
import org.xmedia.oms.metaknow.ComplexProvenance;
import java.util.Map;

/**
 * @author bernie_2
 * 
 */
public class Disjunction extends Formula {

	/**
	 * 
	 */
	public Disjunction() {
	}


	/**
	 * @param child
	 * @param child2
	 */
	public Disjunction(Formula child, Formula child2) {
		super(child, child2);
		// TODO Auto-generated constructor stub
	}


	/* (non-Javadoc)
	 * @see org.xmedia.sparql.prov.eval.Formula#eval()
	 */
	@Override
	public void eval(Map<String, ComplexProvenance> graphProvenances) {
		super.eval(graphProvenances);
		this.provenance = Formula.provEvaluator.disj(this.lChild.provenance, this.rChild.provenance);
	}

	
	public String toString() {
		return "(" + this.lChild.toString() + "v"+this.rChild.toString() + ")";
	}
}
