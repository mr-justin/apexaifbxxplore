package org.aifb.xxplore.views;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.aifb.xxplore.model.ResultViewContentProvider;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class DocumentResultsViewer extends ContentViewer{

	private static Logger s_log = Logger.getLogger(DocumentResultsViewer.class);

	private ScrolledForm m_form = null;
	private Composite m_parent;
	private FormToolkit m_toolkit;
	private Object m_input; 
	private boolean m_isCleared;
	
	
	
	public DocumentResultsViewer(Composite parent){
		
		m_isCleared = false;
		m_parent = parent;
		m_toolkit = new FormToolkit(m_parent.getDisplay());
		m_form = m_toolkit.createScrolledForm(m_parent);
		
	}
	
	public void clear(){
		
		clearForm();
		m_isCleared = true;
		m_form.setText("Results cleared. Enter new query.");
		m_form.reflow(true);	
		s_log.debug("Results cleared.");
			
	}
	
	public void refresh() {

		if(m_form != null){
			
			ResultViewContentProvider provider = ((ResultViewContentProvider)getContentProvider());
			String query = provider.getDocumentResultViewQuery();
			
			TableWrapLayout layout = new TableWrapLayout();
			m_form.getBody().setLayout(layout);
						
			if(m_isCleared)
			{
				clearForm();				
				m_isCleared = false;
				return;
			}
			
			Hits results = provider.getQueryResults();

			if (s_log.isDebugEnabled() && results != null) {
				s_log.debug("Perform search with query " + query);
				s_log.debug(results.length()+ " Total Matching");
			}
			
			if(results == null)
			{
				m_form.setText("");
				m_form.reflow(true);
				return;
			}

			if(results.length() == 0){

				clearForm();

				m_form.setText("No result was found for query: " + query);
				m_form.reflow(true);
				
				//return the form with msg that no results could be found for query
				s_log.debug("No result was found.");
				return;
			}
			
			m_form.setText("Result for '" + query+"'");
			
			clearForm();

			for (int i=0; i<results.length(); i++){
				try {
					Document doc=results.doc(i);
					String path = doc.get("path");
					if (path.toUpperCase().endsWith(".PDF"))
						doDisplayPDFfiles(doc);

					else
						doDisplayHTMLTextfiles(doc);


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			m_toolkit.paintBordersFor(m_form.getBody());
			m_form.reflow(true);


		}

		if (s_log.isDebugEnabled()) {
			s_log.debug("Refreshing Result Form finished!");
		}
	}
	
	private void clearForm(){
		
		Control[] children = m_form.getBody().getChildren();			
		for (Control child : children)
		{
			child.dispose();
		}
	}

	private void doDisplayHTMLTextfiles(Document doc)
	{
		String title = doc.get("title");
		String summary = doc.get("summary");
		String infos = "";
		final String path =  doc.get("path");
		final File file = new File(path);

		ExpandableComposite ec = m_toolkit.createExpandableComposite(m_form.getBody(), ExpandableComposite.TREE_NODE|ExpandableComposite.CLIENT_INDENT);


		ImageHyperlink eci = m_toolkit.createImageHyperlink(ec, SWT.LEFT);

		ec.setTextClient(eci);

		ec.setText(file.getName());

		eci.setText("Open");
		

		eci.setToolTipText(path);


		eci.addHyperlinkListener(new HyperlinkAdapter()
		{
			public void linkActivated(HyperlinkEvent e) {
				if (path.toUpperCase().endsWith(".HTM") || path.toUpperCase().endsWith(".HTML"))
					
				{
					IWorkbenchBrowserSupport support= PlatformUI.getWorkbench().getBrowserSupport();
					
					try {
						IWebBrowser browser = support.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR|IWorkbenchBrowserSupport.NAVIGATION_BAR,null,file.getName(),path);
						
						browser.openURL(file.toURL());						} 
					catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					catch (PartInitException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
					else
						Program.launch(path);
			}
		}
		);

		if (title != null)
			infos = infos +"Title : "+title+"\n";
		if (summary !=null)
			infos = infos +"Summary : "+summary;
		if (infos !="")
		{
			Label client = m_toolkit.createLabel(ec , infos , SWT.WRAP);
			ec.setClient(client);
		}
		else
		{
			Label client = m_toolkit.createLabel(ec , "No infos", SWT.WRAP);
			ec.setClient(client);
		}

		ec.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				m_form.reflow(true);
			}
		});



	}

	private void doDisplayPDFfiles(Document doc) {
		//String title = doc.get("Title");
		String keywords =  doc.get("Keywords");
		String author = doc.get("Author");
		final String path =  doc.get("path");
		File file = new File(path);

		String summary = doc.get("summary");


		ExpandableComposite ec = m_toolkit.createExpandableComposite(m_form.getBody(), ExpandableComposite.TREE_NODE|ExpandableComposite.CLIENT_INDENT);


		ImageHyperlink eci = m_toolkit.createImageHyperlink(ec, SWT.LEFT);

		ec.setTextClient(eci);

		eci.setText("Open");

		eci.setToolTipText(path);


		eci.addHyperlinkListener(new HyperlinkAdapter()
		{
			public void linkActivated(HyperlinkEvent e) {
				Program.launch(path);
			}
		}
		);
		//if (title !=null)
			//ec.setText(title);
		//else
		ec.setText(file.getName());
		String ctext = "";
		if (keywords !=null)
			ctext=ctext+"Keywords : "+keywords.trim();
		if (author !=null)
			ctext=ctext+"Author : "+author.trim()+"\n";
		if (summary !=null)
			ctext=ctext+"Summary : "+summary.trim()+"\n";

		Label client = m_toolkit.createLabel(ec, ctext, SWT.WRAP);
		ec.setClient(client);

		ec.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				m_form.reflow(true);
			}
		});



	}

	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	
	public void setSelection(ISelection selection, boolean reveal) {
		// TODO Auto-generated method stub

	}

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
