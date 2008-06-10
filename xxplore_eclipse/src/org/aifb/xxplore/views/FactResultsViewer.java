package org.aifb.xxplore.views;

import org.aifb.xxplore.model.ResultViewContentProvider;
import org.apache.log4j.Logger;
//import org.apache.lucene.search.Hits;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
//import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class FactResultsViewer extends ContentViewer{

	private static Logger s_log = Logger.getLogger(DocumentResultsViewer.class);

	private Form m_form = null;

	private Composite m_parent;

	private FormToolkit m_toolkit;

	private Object m_input; 

	private Table m_table;
	
	public FactResultsViewer(Composite parent)
	{
		m_parent = parent;
//		m_toolkit = new FormToolkit(m_parent.getDisplay());
//		m_form = m_toolkit.createForm(m_parent);
//		m_form.setVisible(true);
//		m_form.setText("test");

		m_table = new Table(m_parent, SWT.SINGLE);
		TableColumn tc1 = new TableColumn(m_table, SWT.LEFT);
		tc1.setText("test");
		m_table.setVisible(true);
		m_table.setHeaderVisible(true);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.grabExcessVerticalSpace = true;
		m_table.setLayoutData(gridData2);
	}
	

	@Override
	public void refresh() {

		if  (m_form !=null){
			String query = ((ResultViewContentProvider)getContentProvider()).getQuery();
//			TableWrapLayout layout = new TableWrapLayout();
//			m_form.getBody().setLayout(layout);
			m_form.setText("Searching Result for " + query);

//			Hits results = ((ResultViewContentProvider)getContentProvider()).getQueryResults();

//			if (s_log.isDebugEnabled()) {
//				s_log.debug("Perform search with query " + query);
//				s_log.debug(results.length()+ " Total Matching");
//			}
//
//			if (results == null || results.length() == 0) {
//
//				Control[] children = m_form.getBody().getChildren();			
//				for (int j = 0; j < children.length; j++){
//					children[j].dispose();
//				}
//
//				m_form.setText("No result was found");
//				m_form.reflow(true);
//				//return the form with msg that no results could be found for query
//				return;
//			}
//
//
//			Control[] children = m_form.getBody().getChildren();			
//			for (int j = 0; j < children.length; j++){
//				children[j].dispose();
//			}
//
//
//			for (int i=0; i<results.length(); i++){
//				try {
//					Document doc=results.doc(i);
//					String path = doc.get("path");
//					if (path.toUpperCase().endsWith(".PDF"))
//						doDisplayPDFfiles(doc);
//
//					else
//						doDisplayHTMLTextfiles(doc);
//
//
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			m_toolkit.paintBordersFor(m_form.getBody());
//			m_form.reflow(true);



		}

		if (s_log.isDebugEnabled()) {
			s_log.debug("Refreshing Result Form finished!");
		}

	}

//	private void doDisplayHTMLTextfiles(Document doc)
//	{
//		String title = doc.get("title");
//		String summary = doc.get("summary");
//		String infos = "";
//		final String path =  doc.get("path");
//		final File file = new File(path);
//
//		ExpandableComposite ec = m_toolkit.createExpandableComposite(m_form.getBody(), ExpandableComposite.TREE_NODE|ExpandableComposite.CLIENT_INDENT);
//
//
//		ImageHyperlink eci = m_toolkit.createImageHyperlink(ec, SWT.LEFT);
//
//		ec.setTextClient(eci);
//
//		ec.setText(file.getName());
//
//		eci.setText("Open");
//		
//
//		eci.setToolTipText(path);
//
//
//		eci.addHyperlinkListener(new HyperlinkAdapter()
//		{
//			public void linkActivated(HyperlinkEvent e) {
//				if (path.toUpperCase().endsWith(".HTM") || path.toUpperCase().endsWith(".HTML"))
//					
//				{
//					IWorkbenchBrowserSupport support= PlatformUI.getWorkbench().getBrowserSupport();
//					
//					try {
//						IWebBrowser browser = support.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR|IWorkbenchBrowserSupport.NAVIGATION_BAR,null,file.getName(),path);
//						
//						browser.openURL(file.toURL());						} 
//					catch (MalformedURLException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					catch (PartInitException e2) {
//						// TODO Auto-generated catch block
//						e2.printStackTrace();
//					}
//				}
//					else
//						Program.launch(path);
//			}
//		}
//		);
//
//		if (title != null)
//			infos = infos +"Title : "+title+"\n";
//		if (summary !=null)
//			infos = infos +"Summary : "+summary;
//		if (infos !="")
//		{
//			Label client = m_toolkit.createLabel(ec , infos , SWT.WRAP);
//			ec.setClient(client);
//		}
//		else
//		{
//			Label client = m_toolkit.createLabel(ec , "No infos", SWT.WRAP);
//			ec.setClient(client);
//		}
//
//		ec.addExpansionListener(new ExpansionAdapter() {
//			public void expansionStateChanged(ExpansionEvent e) {
//				m_form.reflow(true);
//			}
//		});
//
//
//
//	}
//
//	private void doDisplayPDFfiles(Document doc) {
//		String title = doc.get("Title");
//		String keywords =  doc.get("Keywords");
//		String author = doc.get("Author");
//		final String path =  doc.get("path");
//		File file = new File(path);
//
//		String summary = doc.get("summary");
//
//
//		ExpandableComposite ec = m_toolkit.createExpandableComposite(m_form.getBody(), ExpandableComposite.TREE_NODE|ExpandableComposite.CLIENT_INDENT);
//
//
//		ImageHyperlink eci = m_toolkit.createImageHyperlink(ec, SWT.LEFT);
//
//		ec.setTextClient(eci);
//
//		eci.setText("Open");
//
//		eci.setToolTipText(path);
//
//
//		eci.addHyperlinkListener(new HyperlinkAdapter()
//		{
//			public void linkActivated(HyperlinkEvent e) {
//				Program.launch(path);
//			}
//		}
//		);
//		if (title !=null)
//			ec.setText(title);
//		else
//			ec.setText(file.getName());
//		String ctext = "";
//		if (keywords !=null)
//			ctext=ctext+"Keywords : "+keywords.trim();
//		if (author !=null)
//			ctext=ctext+"Author : "+author.trim()+"\n";
//		if (summary !=null)
//			ctext=ctext+"Summary : "+summary.trim()+"\n";
//
//		Label client = m_toolkit.createLabel(ec, ctext, SWT.WRAP);
//		ec.setClient(client);
//
//		ec.addExpansionListener(new ExpansionAdapter() {
//			public void expansionStateChanged(ExpansionEvent e) {
//				m_form.reflow(true);
//			}
//		});
//
//
//
//	}


	@Override
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInput(Object input){
		Assert.isTrue(getContentProvider() != null,
		"ContentViewer must have a content provider when input is set."); 

		Object oldInput = m_input;
		getContentProvider().inputChanged(this, oldInput, input);
		m_input = input;

	}

	@Override
	protected void handleDispose(DisposeEvent e){
		m_toolkit.dispose();
		m_form.dispose();
		super.handleDispose(e);
	}


	@Override
	public Control getControl() {

		return m_form;
	}

}
