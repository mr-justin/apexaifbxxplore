package org.aifb.xxplore.views.definitionviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aifb.xxplore.action.CreateModelDefinitionAction;
import org.aifb.xxplore.action.DeleteDefinitionViewNodeAction;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.model.definition.EntityDefinition;
import org.aifb.xxplore.core.model.definition.IDefinition;
import org.aifb.xxplore.core.model.definition.IEntityDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition.DefinitionTuple;
import org.aifb.xxplore.model.ImageRegistry;
import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Image;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IResource;

public class DefinitionTupleObjectNode extends AbstractTreeNode {

	private DefinitionTuple m_definitionTuple;
	private SubjectNode m_subjectNode = null;
	private RelationNode m_relationNode = null;
	private ObjectNode m_objectNode = null;
	
	public DefinitionTupleObjectNode(DefinitionTuple dt, ITreeNode parent, ModelDefinitionContentProvider contentProvider) {
		super(parent, contentProvider);
		m_definitionTuple = dt;
	}
	
	public DefinitionTuple getDefinitionTuple() {
		return m_definitionTuple;
	}

	public List<ITreeNode> getChildren() {
		IDefinition def = m_definitionTuple.getObjectDefinition();
		
		if (def instanceof ModelDefinition) {
			if (m_subjectNode == null)
				m_subjectNode = new SubjectNode((ModelDefinition)def, this, getContentProvider(),((ModelDefinition)def).getVariableName());
			if (m_relationNode == null)
				m_relationNode = new RelationNode((ModelDefinition)def, this, getContentProvider(),((ModelDefinition)def).getVariableName());
			if (m_objectNode == null)
				m_objectNode = new ObjectNode((ModelDefinition)def, this, getContentProvider(),((ModelDefinition)def).getVariableName());
			
			return Arrays.asList(new ITreeNode[] { m_subjectNode, m_relationNode, m_objectNode });
		}

		return new ArrayList<ITreeNode>();
	}

	public boolean hasChildren() {
		IDefinition def = m_definitionTuple.getObjectDefinition();
		
		if (def instanceof EntityDefinition)
			return false;
		else if (def instanceof ModelDefinition)
			return true;
		
		return false;
	}

	public List<IAction> getContextMenuActions() {
		IDefinition def = m_definitionTuple.getObjectDefinition();

		DeleteDefinitionViewNodeAction deleteAction = new DeleteDefinitionViewNodeAction(this);
		if (def instanceof ModelDefinition || 
				(def instanceof EntityDefinition && ((EntityDefinition)def).getDefinition() != null))
			deleteAction.setEnabled(true);
		else
			deleteAction.setEnabled(false);
		
		CreateModelDefinitionAction createAction = new CreateModelDefinitionAction(this);
		if (def instanceof ModelDefinition)
			createAction.setEnabled(false);
		
		return Arrays.asList(new IAction[] { createAction, deleteAction });
	}

	public String getLabel() {
		String label = ExploreEnvironment.OBJECT_LABEL;
		
		if (m_definitionTuple.getObjectDefinition() instanceof EntityDefinition) {
			EntityDefinition def = (EntityDefinition)m_definitionTuple.getObjectDefinition();
			if (def.getDefinition() != null) {
				IResource object = def.getDefinition();
				label += ": " + truncateUri(object.getLabel());
			}
		}
		
		return label;
	}

	public void dropResource(IResource res) {
		if (res instanceof INamedConcept) {
			EntityDefinition newDef = new EntityDefinition(m_definitionTuple.getModelDefinition().getDataSource(), IEntityDefinition.OBJECT, (INamedConcept)res);
			m_definitionTuple.setObjectDefinition(newDef);
		}
		
//		if (res instanceof IIndividual) {
//			EntityDefinition newDef = new EntityDefinition(md.getDataSource(), IEntityDefinition.OBJECT, res);
//			md.setObjectDefinition(newDef);
//		}
		this.getContentProvider().refreshViewers(this);
	}

	public Image getImage() {
		if (m_definitionTuple.getObjectDefinition() instanceof EntityDefinition) {
			return (new ImageRegistry()).getConceptImage();
		}
		
		return null;
	}
}
