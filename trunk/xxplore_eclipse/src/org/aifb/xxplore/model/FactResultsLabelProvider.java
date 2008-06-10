package org.aifb.xxplore.model;

import org.aifb.xxplore.shared.util.URIHelper;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.xmedia.oms.query.ITuple;

public class FactResultsLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ITuple) {
			ITuple tuple = (ITuple)element;
			if (columnIndex >= tuple.getArity()) {
				if (tuple.hasProvenance()) {
					columnIndex -= tuple.getArity();
					switch (columnIndex) {
					case 0:
						return tuple.getProvenance().getComplexConfidenceDegree().toString();
					case 1:
						return tuple.getProvenance().getComplexCreationDate().toString();
					case 2:
						return tuple.getProvenance().getComplexAgent().toString();
					case 3:
						return tuple.getProvenance().getComplexSource().toString();
					default:
						return null;
					}
				}
				else
					return null;
			}
			else
				return URIHelper.truncateUri(tuple.getElementAt(columnIndex).toString());
		}
		
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}
}
