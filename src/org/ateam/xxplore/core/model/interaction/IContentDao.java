package org.ateam.xxplore.core.model.interaction;

import java.util.Set;

public interface IContentDao {
	public IContent findContentByUri(String contentUri);
	public Set<IContent> findAllContents();
	public void saveContent(IContent agent);

}
