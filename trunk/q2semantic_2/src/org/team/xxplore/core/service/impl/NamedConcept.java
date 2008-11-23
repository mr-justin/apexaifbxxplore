package org.team.xxplore.core.service.impl;

import org.team.xxplore.core.service.api.INamedConcept;
import org.team.xxplore.core.service.q2semantic.SummaryGraphUtil;

public class NamedConcept implements INamedConcept {

	private String uri;
	public static NamedConcept TOP = new NamedConcept("http://www.w3.org/2002/07/owl#Thing");
	
	public NamedConcept(String uri)
	{
		this.uri = uri;
	}
	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return SummaryGraphUtil.getLocalName(uri);
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return uri;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedConcept other = (NamedConcept) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	
}
