package org.aifb.xxplore.storedquery;

import org.aifb.xxplore.core.service.datafiltering.ITask;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public interface IStoredQueryListElement {
	
    public static final Font BOLD = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
    public static final Font ITALIC = JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);

    public Color GRAY_VERY_LIGHT  = new Color(Display.getDefault(), 200, 200, 200); // TODO: use theme?
    	
	public abstract Image getIcon();
    
    public abstract String getHandle();
    
    public abstract void setHandle(String id);
	
    public abstract boolean isDirectlyModifiable();
    
	public abstract String getToolTipText();
	
	public abstract String getStringForSortingDescription();
	
	public IQuery getQuery();
	
	public String getLabel();
}
