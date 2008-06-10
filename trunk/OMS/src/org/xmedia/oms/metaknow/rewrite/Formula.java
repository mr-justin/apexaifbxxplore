/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;
import org.xmedia.oms.metaknow.ComplexProvenance;
import org.xmedia.oms.metaknow.Provenance;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bernie_2
 * 
 */
public abstract class Formula {
	/**
	 * Defines algebra (see 'IOMSProvEvaluator') applied to provenances. Default: 'OMSProvEvaluator'. Set
	 * before calling 'eval()'! Provenance objects can be adjusted accordingly, but must extend
	 * class 'Provenance'.
	 */
	public static IOMSProvEvaluator provEvaluator = new OMSProvEvaluator();

	protected Formula lChild;
	protected Formula rChild;
	protected ComplexProvenance provenance;

	
	
	
	
	public Formula() {
	}

	/**
	 * @param child
	 * @param child2
	 */
	public Formula(Formula child, Formula child2) {
		this.lChild = child;
		this.rChild = child2;
	}

	/**
	 * @param graphProvenances TODO
	 * @return the provenance of the Formula.
	 * 
	 * Accepts 'Provenance' instead of 'ComplexProvenance' to make SesameSparqlEvaluator compile.
	 * It's not really compatible. Simple Provenance makes no sense here.
	 */
	public ComplexProvenance getProvenance(Map<String, Provenance> graphProvenances) {
		HashMap<String, ComplexProvenance> map = new HashMap<String, ComplexProvenance>();
		boolean foundProvenance = false;
		for (Map.Entry<String, Provenance> me : graphProvenances.entrySet()) {
			ComplexProvenance prov = null;
			if (me.getValue() != null) {
				foundProvenance = true;
				if (!(me.getValue() instanceof ComplexProvenance)) {
					prov = new ComplexProvenance(me.getValue().getConfidenceDegree(), me.getValue().getAgent(), me.getValue().getSource(), me.getValue().getCreationDate());
				}
				else {
					prov = (ComplexProvenance)me.getValue();	
				}
				map.put(me.getKey(), prov);
			}
		}
		if (!foundProvenance)
			return null;
		this.eval(map);
		return this.provenance;
	}

	/**
	 * @param graphProvenances  Mapping from graph names to a binding.
	 *            Mapping from graph names contained in the augmented query (see
	 *            org.xmedia.sparql.prov) to provenance of a specific binding obtained by execution
	 *            of the augmented query.
	 * 
	 */
	public void eval(Map<String, ComplexProvenance> graphProvenances) {
		this.lChild.eval(graphProvenances);
		this.rChild.eval(graphProvenances);
	}

	public abstract String toString();

}
