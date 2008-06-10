package org.xmedia.oms.query;

import org.xmedia.oms.metaknow.ComplexProvenance;
import org.xmedia.oms.model.api.IResource;


public interface ITuple {

	public int getArity();

	public IResource getElementAt(int position);

	public String getLabelAt(int position);
	
	public boolean hasProvenance();
	
	public ComplexProvenance getProvenance();
}
