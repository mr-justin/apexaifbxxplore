package org.aifb.xxplore.storedquery;

import org.eclipse.swt.graphics.Image;

public class StoredQueryListElement implements IStoredQueryListElement {

	private String m_handle;
	private IQuery m_query;
	
	public StoredQueryListElement(IQuery query, String handle) {
		m_query = query;
		m_handle = handle;
	}
	
	public String getHandle() {
		return m_handle;
	}

	public Image getIcon() {
		return null;
	}

	public String getStringForSortingDescription() {
		return null;
	}

	public String getToolTipText() {
		return m_query.getDescription();
	}

	public boolean isDirectlyModifiable() {
		return true;
	}

	public void setHandle(String id) {
		m_handle = id;
	}

	public IQuery getQuery() {
		return m_query;
	}

	public String getLabel() {
		return m_query.getName();
	}

}
