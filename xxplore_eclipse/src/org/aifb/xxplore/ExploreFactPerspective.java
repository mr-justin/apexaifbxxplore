package org.aifb.xxplore;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;



public class ExploreFactPerspective implements IPerspectiveFactory{

	public static final String ID = "org.aifb.xxplore.explorefactperspective";
	
	public void createInitialLayout(IPageLayout layout) {
		
		defineActions(layout);
	    defineLayout(layout);
	    
	}
	
	private void defineActions(IPageLayout layout) {
		
        // Add "show views" shortcuts
        layout.addShowViewShortcut(ConceptHierarchyView.ID);
        layout.addShowViewShortcut(DefinitionView.ID);
        layout.addShowViewShortcut(DocumentResultView.ID);
        layout.addShowViewShortcut(FactResultView.ID);
        layout.addShowViewShortcut(SparqlQueryView.ID);
        layout.addShowViewShortcut(TaskListView.ID);
        
	}
	
	private void defineLayout(IPageLayout layout) {
       
		String editorArea = layout.getEditorArea();
		
		layout.setEditorAreaVisible(true);		
		layout.setFixed(false);		
		
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,editorArea);
		topLeft.addView(ConceptHierarchyView.ID);
		topLeft.addView(IPageLayout.ID_RES_NAV);
		
		IFolderLayout bottomLeft = layout.createFolder("bottomLeft",IPageLayout.BOTTOM, 0.50f,"topLeft");
		bottomLeft.addView(DefinitionView.ID);
		bottomLeft.addView(SparqlQueryView.ID);
		
		IFolderLayout topRight = layout.createFolder("topRight",IPageLayout.RIGHT, 0.75f,editorArea);
		topRight.addView(IPageLayout.ID_OUTLINE);
		
		IFolderLayout bottom = layout.createFolder("bottom",IPageLayout.BOTTOM, 0.50f,editorArea);
		bottom.addView(FactResultView.ID);
		bottom.addView(TaskListView.ID);

	}
}
