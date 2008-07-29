package org.aifb.xxplore.views.definitionviewer;

import java.util.ArrayList;
import java.util.List;

import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.ateam.xxplore.core.model.definition.EntityDefinition;
import org.ateam.xxplore.core.model.definition.IModelDefinition;
import org.ateam.xxplore.core.model.definition.ModelDefinition;
import org.ateam.xxplore.core.model.definition.RelationDefinition;
import org.ateam.xxplore.core.model.definition.ModelDefinition.DefinitionTuple;
import org.eclipse.jface.action.IAction;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Property;

public class SubjectNode extends DefinitionNode {
	
	private List<ITreeNode> m_children = null;
	
	public SubjectNode(IModelDefinition def, ITreeNode parent, ModelDefinitionContentProvider contentProvider, String varName) {
		super(parent, contentProvider, def, varName);
	}
	
	public SubjectNode(IModelDefinition def, ModelDefinitionContentProvider contentProvider, String varName) {
		this(def, null, contentProvider, varName);
	}

	public List<ITreeNode> getChildren() {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);

		List<DefinitionTuple> definitions = md.getCompleteDefinitionTuples();
		
		if (m_children == null) {
			m_children = new ArrayList<ITreeNode>();
			for (DefinitionTuple dt : definitions) {
				m_children.add(new CriterionNode(this, getContentProvider(), dt)); //, dt.getRelationDefinition(), dt.getObjectDefinition()));
			}
		}
		else {
			List<ITreeNode> newChildren = new ArrayList<ITreeNode>();
			for (DefinitionTuple dt : definitions) {
				boolean found = false;
				for (ITreeNode child : m_children) {
					if (child instanceof CriterionNode) {
						CriterionNode cn = (CriterionNode)child;
						if (cn.getDefinitionTuple() == dt) {
							found = true;
							newChildren.add(cn);
							break;
						}
					}
				}
				
				if (!found) {
					newChildren.add(new CriterionNode(this, getContentProvider(), dt));
				}
			}
			
			m_children = newChildren;
		}
		
		return m_children;
	}
	
	public boolean hasChildren() {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		return md.getCompleteDefinitionTuples().size() > 0;
	}
	
	public String getLabel() {
		return getVarName() + ":Subject";
	}
	
	public void dropResource(IResource res) {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		if (res instanceof IProperty) {
			md.setRelationDefinition(new RelationDefinition(md.getDataSource(), (IProperty)res));
		}
		else if (res instanceof INamedConcept) {
			md.addCompleteDefinitionTuple(new RelationDefinition(md.getDataSource(), Property.IS_INSTANCE_OF), 
				new EntityDefinition(md.getDataSource(), EntityDefinition.OBJECT, (INamedConcept)res));
		}
		this.getContentProvider().refreshViewers(this);
	}

	public List<IAction> getContextMenuActions() {
		return new ArrayList<IAction>();
	}

	public void removeCriterion(CriterionNode crit) {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		md.removeCompleteDefinitionTuple(crit.getDefinitionTuple());
	}
}
