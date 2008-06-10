package org.aifb.xxplore;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import org.aifb.xxplore.model.MergedViewContentProvider;
import org.aifb.xxplore.views.MergedResultsViewer;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class MergedResultView extends ViewPart {
	public static final String ID = "org.aifb.xxplore.mergedresultview";
	private static Logger s_log = Logger.getLogger(MergedResultView.class);

	private MergedResultsViewer m_viewer;
	private MergedViewContentProvider m_contentProvider;
	private Label factsearchLabel,docsearchLable;
	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		/* add DefinitionViewListener to the commonSearchResultObject.
		 * if modeldefinition changes, it will notify the contentprovider to refresh its data and refresh the view
		 */

		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new FormLayout());

		
		//add ScrolBar and Ratio info
		FormData data1 = new FormData();
		data1 .top 	   = new FormAttachment(0,2);	
		data1 .left    = new FormAttachment(0,2);
		data1 .right   = new FormAttachment(100,-2);
		data1 .bottom  = new FormAttachment(15,-2);
		Composite comp = new Composite(container,SWT.EMBEDDED);
		comp.setLayoutData(data1);
		Frame frame = SWT_AWT.new_Frame(comp);
		Scrollbar scrollbar = new Scrollbar(Scrollbar.HORIZONTAL,0,5,0,25);
		scrollbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent event) {
				int perCentage = event.getValue()*5;
				float ratio = perCentage/100f;
				factsearchLabel.setText((perCentage)+"% DocSearch");
				docsearchLable.setText("FactSearch "+ (100-perCentage) +"%");
				m_contentProvider.setSearchRelationRatio(ratio);
			}
		});
		frame.setLayout(new BorderLayout());
		factsearchLabel = new Label("0% DocSearch ");
		docsearchLable = new Label("FactSearch 100%");
		frame.add(docsearchLable,BorderLayout.WEST);
		frame.add(scrollbar,BorderLayout.CENTER);
		frame.add(factsearchLabel,BorderLayout.EAST);
		
		//Button refreshButton = new Button("Refresh");
		//refreshButton.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		m_contentProvider.update(null, 0);
		//	}
		//});
		//frame.add(refreshButton,BorderLayout.EAST);
		
		//add viewer
		FormData data2 = new FormData();		
		data2 .top    = new FormAttachment(comp,2);
		data2 .left    = new FormAttachment(0,2);
		data2 .right   = new FormAttachment(100,-2);		
		data2 .bottom    = new FormAttachment(92,-2);
		
		m_viewer = new MergedResultsViewer(container);
		m_viewer.getControl().setLayoutData(data2);
		
		m_contentProvider = new MergedViewContentProvider(m_viewer);
		
		m_viewer.setContentProvider(m_contentProvider);
		
		s_log.debug("MergedResultView created.");
	}
	
	/**
	 * Passing the focus request to the form.
	 */
	public void setFocus() {
		m_viewer.getControl().setFocus();
	}
	
	public Viewer getViewer(){
		return m_viewer;
	}
}