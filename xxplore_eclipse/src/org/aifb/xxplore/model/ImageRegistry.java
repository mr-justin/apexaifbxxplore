/**
 * 
 */
package org.aifb.xxplore.model;

import org.aifb.xxplore.ExplorePlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * simple image registry for images of concept, instances, ...
 * TODO: It is probably better to use the ImageRegistry of JFace...
 * @author jta
 *
 */

public class ImageRegistry {

	private static ImageDescriptor s_conceptimage;
	private static ImageDescriptor s_propertyimage;
	private static ImageDescriptor s_instanceimage;
	private static ImageDescriptor s_propertyinstanceimage;
	private static ImageDescriptor s_removeimage;
	private static ImageDescriptor s_taskimage;
	private static ImageDescriptor s_objectpropertyimage;
	private static ImageDescriptor s_datapropertyimage;
	private static ImageDescriptor s_queryimage;
	static {
		s_conceptimage =  ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/concept.png");
		s_instanceimage =  ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/instance.png");
		s_propertyimage =  ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/property.png");
		s_propertyinstanceimage =  ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/propertyinstance.png");
		s_removeimage = ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/remove.gif");
		s_taskimage = ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/task.gif");
		s_objectpropertyimage = ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/objectproperty.png");
		s_datapropertyimage = ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/dataproperty.png");
		s_queryimage = ExplorePlugin.imageDescriptorFromPlugin(ExplorePlugin.PLUGIN_ID, "icons/query.png");
	}
	
	private Image m_conceptimage;
	private Image m_instanceimage;
	private Image m_propertyimage;
	private Image m_propertyinstanceimage;
	private Image m_removeimage;
	private Image m_taskimage;
	private Image m_objectpropertyimage;
	private Image m_datapropertyimage;
	private Image m_queryimage;

	public Image getTaskImage() {
		if (m_taskimage==null){
			m_taskimage = s_taskimage.createImage();
		}
		return m_taskimage;
	}
	
	public Image getQueryImage() {
		if (m_queryimage==null){
			m_queryimage = s_queryimage.createImage();
		}
		return m_queryimage;
	}
	
	public Image getRemoveImage() {
		if (m_removeimage==null){
			m_removeimage = s_removeimage.createImage();
		}
		return m_removeimage;
	}
	
	public Image getPropertyInstanceImage() {
		if (m_propertyinstanceimage==null){
			m_propertyinstanceimage = s_propertyinstanceimage.createImage();
		}
		return m_propertyinstanceimage;
	}

	public Image getPropertyImage() {
		if (m_propertyimage==null){
			m_propertyimage = s_propertyimage.createImage();
		}
		return m_propertyimage;
	}
	
	public Image getObjectPropertyImage() {
		if (m_objectpropertyimage==null){
			m_objectpropertyimage = s_objectpropertyimage.createImage();
		}
		return m_objectpropertyimage;
	}
	
	public Image getDataPropertyImage() {
		if (m_datapropertyimage==null){
			m_datapropertyimage = s_datapropertyimage.createImage();
		}
		return m_datapropertyimage;
	}

	public Image getIndividualImage() {
		if (m_instanceimage==null){
			m_instanceimage = s_instanceimage.createImage();
		}
		return m_instanceimage;
	}

	public Image getConceptImage() {
		if (m_conceptimage==null){
			m_conceptimage = s_conceptimage.createImage();
		}
		return m_conceptimage;
	}
	
	public void dispose(){
		if (m_conceptimage != null)
			m_conceptimage.dispose();
		if (m_instanceimage != null)
			m_instanceimage.dispose();
		if (m_propertyimage != null)
			m_propertyimage.dispose();
		if (m_propertyinstanceimage != null)
			m_propertyinstanceimage.dispose();
		if (m_removeimage != null)
			m_removeimage.dispose();
	}
}