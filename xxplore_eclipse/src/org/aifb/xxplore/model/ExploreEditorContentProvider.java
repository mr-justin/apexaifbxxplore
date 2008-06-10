package org.aifb.xxplore.model;

import java.util.ArrayList;
import java.util.List;
import org.aifb.xxplore.core.model.definition.IModelDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.model.navigation.GraphModel;
import org.aifb.xxplore.core.model.navigation.IGraphModel;
import org.aifb.xxplore.core.service.IContentProviderService;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IResource;
import prefuse.data.Node;
//import sun.rmi.runtime.GetThreadPoolAction;


/**
 * The content provider class is responsible for providing objects to the
 * view. This provider wraps and provides access methods to a graph model. This provider 
 * is sensitive to the states of the viewer. For instance, when the viewer's input change, the graph model
 * adapted by this provider change accordingly. 
 */
public class ExploreEditorContentProvider implements IExploreEditorContentProvider{

	/**
	 * the definition of the model provided by this class
	 */
	private ModelDefinition m_input;

	/**
	 * the model provided by this class
	 */
	private IGraphModel m_model;

	
	/** the cached schema ****/
	private IHierarchicalSchema m_schema;

	/**
	 * this class provide the model for these viewers 
	 */ 
	private List<Viewer> m_viewers;

	/**
	 * the services 
	 */
	private IContentProviderService m_content_service;
	
//	private static Logger s_log = Logger.getLogger(ExploreEditorContentProvider.class);
	
	
	/**
	 * 
	 * @param delegate the service this class make use of to request the data
	 */
	public ExploreEditorContentProvider(IContentProviderService content_service) {

		super();
		m_viewers = new ArrayList<Viewer>();
		m_content_service = content_service;		
	}


	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		

		//add viewers when not already in list.
		if(!m_viewers.contains(viewer)) {
			m_viewers.add(viewer);
		}			

		//new input is null 
		if (newInput == null){
			m_model = null;
		}
		//if not the same input
		if(oldInput != newInput){
			if((newInput != null) && (newInput != m_input)){
				//input must be a model defintion
				Assert.isTrue(newInput instanceof IModelDefinition);

				//update input and model
				m_input = (ModelDefinition)newInput;
				createModel();
			}

		}

		viewer.refresh();

	}

	public IGraphModel getGraphModel(){
		if (m_model == null) {
			createModel();
		}
		return m_model;
	}

	/**
	 * Obtain the schema from the m_input and compute the graph model with this schema.
	 * @return 
	 */
	private void createModel() {
		if (m_schema == null) {
			if((m_input != null) && (m_input.getDataSource() instanceof IOntology)){

				IOntology onto = (IOntology) m_input.getDataSource();
				if(m_model == null) {
					m_model = new GraphModel();
				}
				m_schema = m_content_service.getHierarchicalSchema(onto);
				m_model.addData(m_schema);
			}
		}
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement == m_input) {

			if (m_model == null){
				createModel();
			}
			return m_model.getElements().toArray(new Node[0]);
		} else {
			return new Object[0];
		}
	}

	public Object[] getChildren(Object parentElement) {
		if (m_model == null){
			createModel();
		}
		return m_model.getChildren((Node)parentElement);
	}

	public Object getParent(Object element) {
		if (m_model == null){
			createModel();
		}
		return m_model.getParent((Node)element);
	}

	public boolean hasChildren(Object element) {
		if (m_model == null){
			createModel();
		}
		return m_model.hasChildren((Node)element);
	}
	
	
	public void dispose() {
		m_input = null;
		m_model = null;
		
	}
	
	public class ConceptHierachyViewContentProvider implements ITreeContentProvider{

		public Object[] getChildren(Object elem) {
			
			if (m_model == null) {
				createModel();
			}
			
			return m_model.getChildrenConceptHierachyView((IResource)elem);
			
		}

		public Object getParent(Object elem) {
			
			if (m_model == null) {
				createModel();
			}
			
			return m_model.getParentConceptHierachyView((IResource)elem);
			
		}

		public boolean hasChildren(Object element) {
			
			return  getChildren(element).length == 0 ? false : true;
			
		}

		public Object[] getElements(Object element) {
			
			if (m_model == null) {
				createModel();
			}
			
			return m_model.getElementsConceptHierachyView();
		}

		public void dispose() {
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}
	}
}
