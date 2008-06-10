package org.xmedia.accessknow.sesame.model;

import java.util.Date;

import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;

public class Provenance extends org.xmedia.oms.metaknow.Provenance {

	private static final long serialVersionUID = -3842761562030327093L;

	public Provenance(double confidenceDegree, INamedIndividual agent, IEntity source, Date creationDate) {
		super(new Double(confidenceDegree), agent, source, creationDate);
	}
	
	@Override
	public int hashCode() {
		
		int hashcode = (getAgent() != null ? getAgent().hashCode() : 0) +
		(getSource() != null ? getSource().hashCode() : 0) +
		(getCreationDate() != null ? getCreationDate().hashCode() : 0) +
		(new Double(getConfidenceDegree()).hashCode());
		
		return hashcode;
	}

	@Override
	public boolean equals(Object res) {
		
		boolean areEquals = false;

		if (res != null && !(areEquals = (this == res))) {
			if ((areEquals = res instanceof IProvenance)) {
				IProvenance target = (IProvenance)res;
				
				areEquals = (
						( (getAgent() != null ? getAgent().equals(target.getAgent()) : target.getAgent() == null) ) &&
						( (getSource() != null ? getSource().equals(target.getSource()) : target.getSource() == null) ) &&
						( (getCreationDate() != null ? getCreationDate().equals(target.getCreationDate()) : target.getCreationDate() == null) ) &&
						(getConfidenceDegree().equals(target.getConfidenceDegree()))
						);
			}
		}

		return areEquals;
	}

	@Override
	public String toString() {
		return "<" + 
		"Agent: " + (getAgent() != null ? getAgent() : "-" ) + ", " +
		"Source: " + (getSource() != null ? getSource() : "-" ) + ", " +
		"Date: " + (getCreationDate() != null ? getCreationDate() : "-" ) + ", " +
		"Confidence: " + (getConfidenceDegree() > 0 ? getConfidenceDegree() : "-" ) + 
		">";
	}

}
