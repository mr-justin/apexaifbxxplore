package org.aifb.xxplore.views.definitionviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aifb.xxplore.action.DeleteDefinitionViewNodeAction;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.model.definition.IModelDefinition;
import org.aifb.xxplore.core.model.definition.IRelationDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.model.definition.RelationDefinition;
import org.aifb.xxplore.model.ImageRegistry;
import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Image;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;

public class RelationNode extends DefinitionNode {
	
	public RelationNode(IModelDefinition def, ITreeNode parent, ModelDefinitionContentProvider contentProvider, String varName) {
		super(parent, contentProvider, def, varName);
	}

	public RelationNode(IModelDefinition def, ModelDefinitionContentProvider contentProvider, String varName) {
		this(def, null, contentProvider, varName);
	}

	public List<ITreeNode> getChildren() {
		return new ArrayList<ITreeNode>();
	}
	
	public boolean hasChildren() {
		return false;
	}
	
	public String getLabel() {
		String label = ExploreEnvironment.PREDICATE_LABEL;
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		IRelationDefinition rd = md.getRelationDefinition();
		if (rd.getDefinition() != null)
			label += ": " + truncateUri(rd.getDefinition().getLabel());
		return label;
	}
	
	public void dropResource(IResource res) {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		if (res instanceof IProperty) {
			IRelationDefinition newDef = new RelationDefinition(md.getDataSource(),  (IProperty)res);
			md.setRelationDefinition(newDef);
		}
		this.getContentProvider().refreshViewers(this);
	}
	
	public List<IAction> getContextMenuActions() {
		DeleteDefinitionViewNodeAction deleteAction = new DeleteDefinitionViewNodeAction(this);
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		deleteAction.setEnabled(md.getRelationDefinition().getDefinition() != null);
		return Arrays.asList(new IAction[] { deleteAction });
	}
	
	public Image getImage() {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		if (md.getRelationDefinition() instanceof RelationDefinition) {
			return (new ImageRegistry()).getPropertyImage();
		}
		
		return null;
	}
}
