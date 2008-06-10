package org.aifb.xxplore.views.definitionviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aifb.xxplore.action.DeleteDefinitionViewNodeAction;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.model.definition.IRelationDefinition;
import org.aifb.xxplore.core.model.definition.RelationDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition.DefinitionTuple;
import org.aifb.xxplore.model.ImageRegistry;
import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Image;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;

public class DefinitionTupleRelationNode extends AbstractTreeNode {

	private DefinitionTuple m_definitionTuple;
	
	public DefinitionTupleRelationNode(DefinitionTuple dt, ITreeNode parent, ModelDefinitionContentProvider contentProvider) {
		super(parent, contentProvider);
		
		m_definitionTuple = dt;
	}

	public DefinitionTuple getDefinitionTuple() {
		return m_definitionTuple;
	}

	public List<ITreeNode> getChildren() {
		return new ArrayList<ITreeNode>();
	}

	public boolean hasChildren() {
		return false;
	}

	public List<IAction> getContextMenuActions() {
		DeleteDefinitionViewNodeAction deleteAction = new DeleteDefinitionViewNodeAction(this);
		deleteAction.setEnabled(m_definitionTuple.getRelationDefinition().getDefinition() != null);
		return Arrays.asList(new IAction[] { deleteAction });
	}

	public String getLabel() {
		String label = ExploreEnvironment.PREDICATE_LABEL;
		IRelationDefinition rd = m_definitionTuple.getRelationDefinition();
		if (rd.getDefinition() != null)
			label += ": " + truncateUri(rd.getDefinition().getLabel());
		
		return label;
	}

	public void dropResource(IResource res) {
		if (res instanceof IProperty) {
			IRelationDefinition newDef = new RelationDefinition(m_definitionTuple.getModelDefinition().getDataSource(),  (IProperty)res);
			m_definitionTuple.setRelationDefinition(newDef);
		}
		this.getContentProvider().refreshViewers(this);
	}
	
	public Image getImage() {
		if (m_definitionTuple.getRelationDefinition() instanceof RelationDefinition) {
			return (new ImageRegistry()).getPropertyImage();
		}
		
		return null;
	}
}
