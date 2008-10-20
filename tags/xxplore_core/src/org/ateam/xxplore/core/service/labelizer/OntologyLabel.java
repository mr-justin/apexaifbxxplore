package org.ateam.xxplore.core.service.labelizer;

import java.util.LinkedHashSet;
import java.util.Set;

public class OntologyLabel {

	private String _label;
	private Set<OntologySubLabel> _sublabels;
	
	public OntologyLabel(String label) {
		_label = label;
		_sublabels = new LinkedHashSet<OntologySubLabel>();
	}
	
	public String getLabel() {
		return _label;
	}
	
	public Set<OntologySubLabel> getSubLabels() {
		return _sublabels;
	}
	
	public void addSublabel(String sublabel, String type) {
		_sublabels.add(new OntologySubLabel(sublabel, type));
	}
	
	public void addSublabel(String sublabel, String type, String property) {
		_sublabels.add(new OntologySubLabel(sublabel, type, property));
	}
	
	public boolean equals(Object obj) {
		return getLabel().equals(obj.toString());
	}
	
	public void printOntologyLabel() {
		System.out.print("Label: " + getLabel() + " has sub-labels: ");
		Set<OntologySubLabel> sublabels = (LinkedHashSet<OntologySubLabel>) getSubLabels();
		for (OntologySubLabel sublabel : sublabels)
			System.out.print(sublabel.toString() + "; ");
		System.out.println();
	}
}
