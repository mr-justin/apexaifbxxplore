/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

import org.xmedia.oms.metaknow.ComplexProvenance;
import org.xmedia.oms.metaknow.Provenance;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.impl.NamedIndividual;

/**
 * @author bernie_2
 *
 */
public class OMSProvEvaluator implements IOMSProvEvaluator {

	/* (non-Javadoc)
	 * @see org.xmedia.sparql.prov.eval.IProvEvaluator#conj(org.xmedia.sparql.prov.eval.Provenance, org.xmedia.sparql.prov.eval.Provenance)
	 */
	public ComplexProvenance conj(ComplexProvenance p1, ComplexProvenance p2) {
		//Confidence as fuzzy, use of list in ComplexProvenance is uncertain
		LinkedList<Double> liConfs = new LinkedList<Double>();
		liConfs.add(Math.min(p1.getConfidenceDegree(),p2.getConfidenceDegree()));
		// Date
		LinkedList<Date> liDates = new LinkedList<Date>();
		if (p1.getComplexCreationDate()!= null &&  p1.getComplexCreationDate().size() > 0) {
			liDates.addAll(p1.getComplexCreationDate());
		}
		if (p2.getComplexCreationDate()!= null &&  p2.getComplexCreationDate().size() > 0) {
			liDates.addAll(p2.getComplexCreationDate());
		}
		//agents
		HashSet<INamedIndividual> setAgents = new HashSet<INamedIndividual>();
		setAgents.addAll(p1.getComplexAgent());
		setAgents.addAll(p2.getComplexAgent());
		//sources
		HashSet<IEntity> setSources = new HashSet<IEntity>();
		setSources.addAll(p1.getComplexSource());
		setSources.addAll(p2.getComplexSource());
		ComplexProvenance p = new ComplexProvenance(liConfs, liDates,setAgents, setSources);
		return p;
	}

	/* (non-Javadoc)
	 * @see org.xmedia.sparql.prov.eval.IProvEvaluator#disj(org.xmedia.sparql.prov.eval.Provenance, org.xmedia.sparql.prov.eval.Provenance)
	 */
	public ComplexProvenance disj(ComplexProvenance p1, ComplexProvenance p2) {
		//Confidence as fuzzy, use of list in ComplexProvenance is uncertain
		LinkedList<Double> liConfs = new LinkedList<Double>();
		liConfs.add(Math.max(p1.getConfidenceDegree(),p2.getConfidenceDegree()));
		//Date
		LinkedList<Date> liDates = new LinkedList<Date>();
		if (p1.getComplexCreationDate()!= null &&  p1.getComplexCreationDate().size() > 0) {
			liDates.addAll(p1.getComplexCreationDate());
		}
		if (p2.getComplexCreationDate()!= null &&  p2.getComplexCreationDate().size() > 0) {
			liDates.addAll(p2.getComplexCreationDate());
		}
		//agents
		HashSet<INamedIndividual> setAgents = new HashSet<INamedIndividual>();
		setAgents.addAll(p1.getComplexAgent());
		setAgents.addAll(p2.getComplexAgent());
		//sources
		HashSet<IEntity> setSources = new HashSet<IEntity>();
		setSources.addAll(p1.getComplexSource());
		setSources.addAll(p2.getComplexSource());
		ComplexProvenance p = new ComplexProvenance(liConfs, liDates,setAgents, setSources);
		return p;
	}

	/* (non-Javadoc)
	 * @see org.xmedia.sparql.prov.eval.IProvEvaluator#neg(org.xmedia.sparql.prov.eval.Provenance)
	 */
	public ComplexProvenance neg(ComplexProvenance p1) {
		//Confidence as fuzzy, use of list in ComplexProvenance is uncertain
		LinkedList<Double> liConfs = new LinkedList<Double>();
		liConfs.add(p1.getConfidenceDegree());
		//Date
		LinkedList<Date> liDates = new LinkedList<Date>();
		if (p1.getComplexCreationDate()!= null &&  p1.getComplexCreationDate().size() > 0) {
			liDates.addAll(p1.getComplexCreationDate());
		}
		//agents
		HashSet<INamedIndividual> setAgents = new HashSet<INamedIndividual>();
		setAgents.addAll(p1.getComplexAgent());
		//sources
		HashSet<IEntity> setSources = new HashSet<IEntity>();
		setSources.addAll(p1.getComplexSource());
		ComplexProvenance p = new ComplexProvenance(liConfs, liDates,setAgents, setSources);
		return p;
	}

}
