package org.ateam.xxplore.core.model.navigation;

import java.util.Collection;
import java.util.List;

import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.api.ISchema;

import prefuse.data.Edge;
import prefuse.data.Node;

public interface IGraphModel{

	
	/**
	 * @return the complete graph which is used to store the model
	 */
	public prefuse.data.Graph getGraph();
	
	/**
	 * 
	 * @param data the kb schema that shall be added to the model
	 */
	public void addData(ISchema data);
	
	public Node addNode(IResource res);
	
	public Edge addEdge(IResource from, IResource to, IProperty property, String label);
				
	/**
	 * @author lei
	 * @see IStructuredContentProvider,ITreeContentProvider
	 * 
	 */
	public List<Node> getElements();

	public Node[] getChildren(Node parentNode);

	public boolean hasChildren(Node parentNode);
	
	public Node getParent(Node node);
		
	/**
	 * Methods are used by ConceptHierachyViewContentProvider
	 * @see ITreeContentProvider
	 */
	
	public IResource[] getChildrenConceptHierachyView(IResource res);
	
	public IResource getParentConceptHierachyView(IResource res);
	
	public IResource[] getElementsConceptHierachyView();
		
}
