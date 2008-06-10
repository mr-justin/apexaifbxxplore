package org.aifb.xxplore.model;

import org.aifb.xxplore.core.model.navigation.IGraphModel;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 *  Adapter interface to a graph model with different access functionalities. 
 * @author Julien Tane, Thanh Tran Duc
 *
 */

public interface IExploreEditorContentProvider extends ITreeContentProvider{
	
	/**
	 * @return the graph that fits with the initialized input
	 */
	public IGraphModel getGraphModel();
	
}