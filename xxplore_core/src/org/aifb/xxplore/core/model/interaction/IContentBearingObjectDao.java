package org.aifb.xxplore.core.model.interaction;

import java.util.Set;

public interface IContentBearingObjectDao {
	public IContentBearingObject findContentBearingObjectByUri(String cboUri);
	public Set<IContentBearingObject> findAllContentBearingObjects();
	public void saveContentBearingObject(IContentBearingObject cbo);
}
