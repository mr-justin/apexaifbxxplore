package org.aifb.xxplore.model;

import java.util.List;

import org.aifb.xxplore.StoredQueryView;
import org.aifb.xxplore.storedquery.StoredQueryList;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class StoredQueryViewContentProvider implements ITreeContentProvider {


	private final StoredQueryView m_view;
	
	public StoredQueryViewContentProvider(StoredQueryView view){
		this.m_view = view;
	}
	
	
	
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof StoredQueryList){
			List list = ((StoredQueryList)inputElement).getQueryList();
			return list.toArray();
		} else
			return new Object[0];
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

}
