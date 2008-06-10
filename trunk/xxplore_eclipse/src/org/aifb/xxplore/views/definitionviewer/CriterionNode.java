package org.aifb.xxplore.views.definitionviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aifb.xxplore.action.DeleteDefinitionViewNodeAction;
import org.aifb.xxplore.action.EditDefinitionNodeAction;
import org.aifb.xxplore.action.RefineObjectDefinitionNodeAction;
import org.aifb.xxplore.core.model.definition.EntityDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.model.definition.RelationDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition.DefinitionTuple;
import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.eclipse.jface.action.IAction;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;

public class CriterionNode extends AbstractTreeNode {

	private DefinitionTuple m_definitionTuple;
	
	private SubjectNode m_subjectNode = null;
	private RelationNode m_relationNode = null;
	private ObjectNode m_objectNode = null;
	
	private boolean m_editActive = false;
	
	private DefinitionTupleObjectNode m_definitionTupleObjectNode;
	
	public static int EDIT_OFF = 0, EDIT_ALL = 1, EDIT_REFINE_OBJECT = 2;
	private int m_editState = EDIT_OFF;
	
	public CriterionNode(ITreeNode parent, ModelDefinitionContentProvider contentProvider, DefinitionTuple dt) {
		super(parent, contentProvider);
		m_definitionTuple = dt;
	}
	
	public List<ITreeNode> getChildren() {
		if (m_definitionTuple.getObjectDefinition() instanceof ModelDefinition) {
			if ((getEditState() == EDIT_OFF) && ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getCompleteDefinitionTuples().size() != 0){
				if (m_subjectNode == null)
					m_subjectNode = new SubjectNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				if (m_relationNode == null)
					m_relationNode = new RelationNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				if (m_objectNode == null)
					m_objectNode = new ObjectNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				
				return Arrays.asList(new ITreeNode[] { m_subjectNode, m_relationNode, m_objectNode });
			}
			else if (getEditState() == EDIT_REFINE_OBJECT){
				if (m_subjectNode == null)
					m_subjectNode = new SubjectNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				if (m_relationNode == null)
					m_relationNode = new RelationNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				if (m_objectNode == null)
					m_objectNode = new ObjectNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				
				return Arrays.asList(new ITreeNode[] { m_subjectNode, m_relationNode, m_objectNode });
			}
			else if (getEditState() == EDIT_ALL){

					if (m_definitionTupleObjectNode == null)
						m_definitionTupleObjectNode = new DefinitionTupleObjectNode(m_definitionTuple, this, getContentProvider());
					
					return Arrays.asList(new ITreeNode[] { new DefinitionTupleRelationNode(m_definitionTuple, this, getContentProvider()),
							m_definitionTupleObjectNode });
				}
			else
				return new ArrayList<ITreeNode>();
		}
		
		else if (m_definitionTuple.getObjectDefinition() instanceof EntityDefinition) {
			if (getEditState() == EDIT_ALL){

				if (m_definitionTupleObjectNode == null)
					m_definitionTupleObjectNode = new DefinitionTupleObjectNode(m_definitionTuple, this, getContentProvider());
				
				return Arrays.asList(new ITreeNode[] { new DefinitionTupleRelationNode(m_definitionTuple, this, getContentProvider()),
						m_definitionTupleObjectNode });
			}
			else if (getEditState() == EDIT_REFINE_OBJECT) {
				
				if (m_subjectNode == null)
					m_subjectNode = new SubjectNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				if (m_relationNode == null)
					m_relationNode = new RelationNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				if (m_objectNode == null)
					m_objectNode = new ObjectNode((ModelDefinition)m_definitionTuple.getObjectDefinition(), this, getContentProvider(), ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName());
				
				return Arrays.asList(new ITreeNode[] { m_subjectNode, m_relationNode, m_objectNode });
	//			return Arrays.asList(new ITreeNode[] { new DefinitionTupleObjectNode(m_definitionTuple, this, getContentProvider()) });
			}
			else
				return new ArrayList<ITreeNode>();
		}
		
		else 
			return new ArrayList<ITreeNode>();

	}

	public boolean hasChildren() {
//		return getEditState() != EDIT_OFF; //(m_definitionTuple.getObjectDefinition() instanceof ModelDefinition);
		return (getEditState() != EDIT_OFF) || ((m_definitionTuple.getObjectDefinition() instanceof ModelDefinition) 
				&& ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getCompleteDefinitionTuples().size() != 0);
	}

	public String getLabel() {
		String relLabel = "rel", objectLabel = "obj";
		
		if (m_definitionTuple.getRelationDefinition() != null && m_definitionTuple.getRelationDefinition().getDefinition() != null)
			relLabel = truncateUri(m_definitionTuple.getRelationDefinition().getDefinition().getLabel());
		
		if (m_definitionTuple.getObjectDefinition() != null && m_definitionTuple.getObjectDefinition() instanceof EntityDefinition) {
			EntityDefinition ed = (EntityDefinition)m_definitionTuple.getObjectDefinition();
			IResource object = ed.getDefinition();
			if (object != null)
				objectLabel = truncateUri(object.getLabel());
		}
		else if (m_definitionTuple.getObjectDefinition() != null && m_definitionTuple.getObjectDefinition() instanceof ModelDefinition) {
			objectLabel = ((ModelDefinition)m_definitionTuple.getObjectDefinition()).getVariableName();
		}
		
		return ((SubjectNode)getParent()).getVarName() + " " + relLabel + " " + objectLabel;
	}

	public void dropResource(IResource res) {
		ModelDefinition md = (ModelDefinition)((SubjectNode)getParent()).getModelDefinition();
		if (res instanceof IProperty) {
			m_definitionTuple.setRelationDefinition(new RelationDefinition(md.getDataSource(), (IProperty)res));
		}
		else if (res instanceof INamedConcept) {
			md.setObjectDefinition(new EntityDefinition(md.getDataSource(), EntityDefinition.OBJECT, (INamedConcept)res));
		}
		this.getContentProvider().refreshViewers(this);
	}

	public List<IAction> getContextMenuActions() {
		EditDefinitionNodeAction editAction = new EditDefinitionNodeAction(this);
		if (getEditState() == EDIT_ALL)
			editAction.setChecked(true);
		
		RefineObjectDefinitionNodeAction refineAction = new RefineObjectDefinitionNodeAction(this);
		if (getEditState() == EDIT_REFINE_OBJECT)
			refineAction.setChecked(true);
		
		if (m_definitionTuple.getObjectDefinition() instanceof ModelDefinition) 
			return Arrays.asList(new IAction[] { editAction, refineAction, new DeleteDefinitionViewNodeAction(this) });
		else if (m_definitionTuple.getObjectDefinition() instanceof EntityDefinition)
			return Arrays.asList(new IAction[] { editAction, new DeleteDefinitionViewNodeAction(this) });
		else 
			return new ArrayList<IAction>();
	}

	public DefinitionTuple getDefinitionTuple() {
		return m_definitionTuple;
	}
	
	public void setEditState(int state) {
		m_editState = state;
	}
	
	public int getEditState() {
		return m_editState;
	}
}
