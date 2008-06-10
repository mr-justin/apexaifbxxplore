package org.aifb.xxplore.storedquery;

import org.aifb.xxplore.model.StoredQueryViewContentProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class StoredQueryEditorInput implements IEditorInput {
	
	private IStoredQueryListElement queryListElement;
	
	private StoredQueryViewContentProvider m_contentProvider;
	
	private String id;
	
	private boolean isNewQuery = false;

	public StoredQueryEditorInput(IStoredQueryListElement query, StoredQueryViewContentProvider contentProvider, boolean isNewQuery) {
		this.queryListElement = query;
		this.m_contentProvider = contentProvider;
		this.id = query.getHandle();
		this.isNewQuery = isNewQuery;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return queryListElement.getLabel();
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public IStoredQueryListElement getQuery() {
		return queryListElement;
	}
	
	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return queryListElement.getLabel();
	}
	
	public StoredQueryViewContentProvider getContentProvider(){
		return m_contentProvider;
	}

	public boolean isNewQuery() {
		return isNewQuery;
	}
	
	public boolean equals(Object o) {
		if (o instanceof StoredQueryEditorInput) {
			StoredQueryEditorInput input = (StoredQueryEditorInput) o;
			return getId() == input.getId();
		}
		return false;
	}
	
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		final StoredQueryEditorInput other = (StoredQueryEditorInput) obj;
//		if (queryListElement == null) {
//			if (other.queryListElement != null)
//				return false;
//		} else if (!queryListElement.equals(other.queryListElement))
//			return false;
//		return true;
//	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((queryListElement == null) ? 0 : queryListElement.hashCode());
		return result;
	}
}
