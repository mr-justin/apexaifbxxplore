package org.xmedia.oms.query;

import org.xmedia.oms.model.api.IOntology;

public interface IQueryWrapper {
	
	public String getQuery();
	
	public String[] getSelectVariables();
	
	public String getOntologyURI();
	
	public void setOntology(String uri);
	
	public boolean equals(Object otherquerywrapper);
	
}
