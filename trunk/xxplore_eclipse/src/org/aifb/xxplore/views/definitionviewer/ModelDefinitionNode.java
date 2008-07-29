package org.aifb.xxplore.views.definitionviewer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.ateam.xxplore.core.model.definition.IModelDefinition;
import org.ateam.xxplore.core.model.definition.ModelDefinition;
import org.eclipse.jface.action.IAction;
import org.xmedia.oms.model.api.IResource;

public class ModelDefinitionNode extends AbstractTreeNode {

	private SubjectNode m_subjectNode;
	private RelationNode m_relationNode;
	private ObjectNode m_objectNode;
	
	private List<ITreeNode> m_children;
	
	private Set<String> variables;
	
	private IModelDefinition m_modelDefinition;
	
	public ModelDefinitionNode(ITreeNode parent, ModelDefinitionContentProvider contentProvider, IModelDefinition definition) {
		super(parent, contentProvider);
		m_modelDefinition = definition;
		variables = new HashSet<String>();
		m_children = new ArrayList<ITreeNode>();
		m_subjectNode = new SubjectNode(m_modelDefinition, this, contentProvider, ((ModelDefinition)m_modelDefinition).getVariableName());
		m_relationNode = new RelationNode(m_modelDefinition, this, contentProvider, ((ModelDefinition)m_modelDefinition).getVariableName());
		m_objectNode = new ObjectNode(m_modelDefinition, this, contentProvider, ((ModelDefinition)m_modelDefinition).getVariableName());
		m_children.add(m_subjectNode);
		m_children.add(m_relationNode);
		m_children.add(m_objectNode);
	}
	
	public ModelDefinitionNode(ModelDefinitionContentProvider contentProvider, IModelDefinition definition) {
		this(null, contentProvider, definition);
	}
	
	public IModelDefinition getModelDefinition() {
		return m_modelDefinition;
	}
	
	public void setModelDefinition(IModelDefinition def) {
		m_modelDefinition = def;
	}
	
	public List<ITreeNode> getChildren() {
		if (m_modelDefinition instanceof ModelDefinition && ((ModelDefinition)m_modelDefinition).getVarSize() <= 1) {
			String var = ((ModelDefinition)m_modelDefinition).getVariableName();
			if (!var.equals("x"))
				variables.add(var);
			return m_children;
		}
		else if (m_modelDefinition instanceof ModelDefinition && ((ModelDefinition)m_modelDefinition).getVarSize() > 1) {
			int varSize = ((ModelDefinition)m_modelDefinition).getVarSize();
			
			System.out.println("varSize:"+varSize);
			
			Set<String> varNames = ((ModelDefinition)m_modelDefinition).getVariableNames();
			Iterator<String> itr = varNames.iterator();
			String var1 = itr.next();
			
			System.out.println("var1:"+var1);
			
			m_subjectNode.setVarName(var1);
			m_relationNode.setVarName(var1);
			m_objectNode.setVarName(var1);
			for(int i = varSize -1;i > 0; i--) {
				String var = itr.next();
				if (!variables.contains(var)) {
					System.out.println("var:"+var);
					
					variables.add(var);
					SubjectNode subjectNode = new SubjectNode(m_modelDefinition, this, getContentProvider(), var);
					RelationNode relationNode = new RelationNode(m_modelDefinition, this, getContentProvider(), var);
					ObjectNode objectNode = new ObjectNode(m_modelDefinition, this, getContentProvider(), var);
					m_children.add(subjectNode);
					m_children.add(relationNode);
					m_children.add(objectNode);
				}
			}
			
			System.out.println("m_children.size():"+m_children.size());
			
			return m_children;
		}

		return new ArrayList<ITreeNode>();
	}

	public List<IAction> getContextMenuActions() {
		return new ArrayList<IAction>();
	}

	public String getLabel() {
		return "ModelDefinition";
	}

	public boolean hasChildren() {
		return true;
	}

	public void dropResource(IResource res) {
		// TODO Auto-generated method stub
	}

}
