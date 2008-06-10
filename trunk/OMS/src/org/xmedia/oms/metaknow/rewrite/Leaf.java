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
public class Leaf extends Formula {
	protected Formula lChild = null;
	protected Formula rChild = null;
	protected String graphVar;

	
	public Leaf(String graphVar) {
		this.graphVar = graphVar;
	}
	
	/* (non-Javadoc)
	 * @see org.xmedia.sparql.prov.eval.Formula#eval(org.xmedia.sparql.prov.eval.Provenance)
	 */
	@Override
	public void eval(Map<String, ComplexProvenance> graphProvenances) {
		//get provenance of binding using 'graphVar'
		this.provenance = graphProvenances.get(graphVar);
		//variable might not start with "?"
		if (this.provenance == null) {
			this.provenance = graphProvenances.get(graphVar.substring(1));
		}
	}

	public String toString() {
		return graphVar;
	}
}
