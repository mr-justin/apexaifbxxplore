package org.aifb.xxplore.views.definitionviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aifb.xxplore.action.CreateModelDefinitionAction;
import org.aifb.xxplore.action.DeleteDefinitionViewNodeAction;
import org.aifb.xxplore.model.ImageRegistry;
import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.model.definition.EntityDefinition;
import org.ateam.xxplore.core.model.definition.IDefinition;
import org.ateam.xxplore.core.model.definition.IEntityDefinition;
import org.ateam.xxplore.core.model.definition.IModelDefinition;
import org.ateam.xxplore.core.model.definition.ModelDefinition;
import org.ateam.xxplore.core.model.definition.RelationDefinition;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Image;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Property;

public class ObjectNode extends DefinitionNode {

	private SubjectNode m_subjectNode = null;
	private RelationNode m_relationNode = null;
	private ObjectNode m_objectNode = null;
	
	public ObjectNode(IModelDefinition def, ITreeNode parent, ModelDefinitionContentProvider contentProvider, String varName) {
		super(parent, contentProvider, def, varName);
	}
	
	public ObjectNode(IModelDefinition def, ModelDefinitionContentProvider contentProvider, String varName) {
		this(def, null, contentProvider, varName);
	}
	
	public List<ITreeNode> getChildren() {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		IDefinition def = md.getObjectDefinition();
		
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
	
	public void resetChildren(){
		m_subjectNode = null;
		m_relationNode = null;
		m_objectNode = null;
	}
	
	public boolean hasChildren() {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		IDefinition def = md.getObjectDefinition();
		
		if (def instanceof EntityDefinition)
			return false;
		else if (def instanceof ModelDefinition)
			return true;
		
		return false;
	}
	
	public String getLabel() {
		String label = ExploreEnvironment.OBJECT_LABEL;
		
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		if (md.getObjectDefinition() instanceof EntityDefinition) {
			EntityDefinition def = (EntityDefinition)md.getObjectDefinition();
			if (def.getDefinition() != null) {
				IResource object = def.getDefinition();
				label += ": " + truncateUri(object.getLabel());
			}
		}
		
		return label;
	}
	
	public void dropResource(IResource res) {
		
//		if (res instanceof INamedConcept) {
//			EntityDefinition newDef = new EntityDefinition(md.getDataSource(), IEntityDefinition.OBJECT, (INamedConcept)res);
//			md.setObjectDefinition(newDef);
//		}
		if (res instanceof INamedConcept) {
			ModelDefinition md = (ModelDefinition)this.getModelDefinition();
			md.setVariableName(varName);
			
			
			ModelDefinition newDef = new ModelDefinition(md.getDataSource());
			newDef.setVariableName(newDef.getNextVarName());
			newDef.addCompleteDefinitionTuple(new RelationDefinition(newDef.getDataSource(), Property.IS_INSTANCE_OF), 
				new EntityDefinition(newDef.getDataSource(), EntityDefinition.OBJECT, (INamedConcept)res));
			md.setObjectDefinition(newDef);
			resetChildren();
			
		}
		
		if (res instanceof IIndividual) {
			ModelDefinition md = (ModelDefinition)getModelDefinition();
			md.setVariableName(varName);
			EntityDefinition newDef = new EntityDefinition(md.getDataSource(), IEntityDefinition.OBJECT, res);
			md.setObjectDefinition(newDef);
		}
		this.getContentProvider().refreshViewers(this);
	}

	public List<IAction> getContextMenuActions() {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		IDefinition def = md.getObjectDefinition();

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
	
	public Image getImage() {
		ModelDefinition md = (ModelDefinition)getModelDefinition();
		md.setVariableName(varName);
		if (md.getObjectDefinition() instanceof EntityDefinition) {
			return (new ImageRegistry()).getConceptImage();
		}
		
		return null;
	}
}
