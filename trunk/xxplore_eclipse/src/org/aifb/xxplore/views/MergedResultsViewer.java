package org.aifb.xxplore.views;

import java.awt.Scrollbar;
import java.io.File;
import java.net.MalformedURLException;

import org.aifb.xxplore.misc.ScoredDocumentITupleContainer;
import org.aifb.xxplore.model.MergedViewContentProvider;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
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
import org.xmedia.oms.query.ITuple;

public class MergedResultsViewer extends ContentViewer{

	private static Logger s_log = Logger.getLogger(MergedResultsViewer.class);

	private Composite parent;
	
	private ScrolledForm m_form = null;
	private FormToolkit m_toolkit;
	private Object m_input; 
	private Scrollbar scrolbar;

	public MergedResultsViewer(Composite parent) {
		this.parent = parent;

		m_toolkit = new FormToolkit(parent.getDisplay());
		
		m_form = m_toolkit.createScrolledForm( parent );
		TableWrapLayout layout = new TableWrapLayout();
		m_form.getBody().setLayout(layout);
	}
	
	@Override
	public void refresh() {
		if(m_form != null){
			MergedViewContentProvider contentprovider = ((MergedViewContentProvider)getContentProvider());
			Object[] elements = contentprovider.getElements(null);			

			clearForm();
			//TODO implement ratioBar here
			
			if (elements == null) {
				m_form.setText("No result was found.");
				m_form.reflow(true);
				
				s_log.debug("result elements was null.");
				return;
			}
			
			for (int i=0; i<elements.length; i++){
				Object element = elements[i];
				if (element instanceof ScoredDocumentITupleContainer) {
					ScoredDocumentITupleContainer container = (ScoredDocumentITupleContainer) element;
					doDisplayContainer(container);
				} else if (element instanceof String) {
					/*this can be an seperator implement s.t. like "inBoth, inXOnly"*/
					doDisplayHeadline((String) element);
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
		for (Control child : children) { //0 is scrolbar
			child.dispose();
		}
	}

	private void doDisplayContainer( ScoredDocumentITupleContainer container) {
		Document doc = container.getDocument();
		ITuple tup = container.getItuple();

		String titleText; //displaied as title
		String bodyText = "";  //displaied as body
		
		String summary = null;
		String keywords = null;
		String author = null;
		String path = null;
		String title = null;
		
		String tupinfos = null;

		float score = container.getScore();
		
		if (doc != null) {
			title = doc.get("title");
			keywords =  doc.get("Keywords");
			author = doc.get("Author");
			summary = doc.get("summary");
			path =  doc.get("path");
		} else if (tup != null) {
			title = "fact"; //TODO depending on tuplestructure
			//TODO path = "";  depending on tuplestructure
		}
		
		if (tup != null) {
			tupinfos = "fakten fakten fakten"; //TODO depending on tuplestructure
		}
		
		//consturct titleText
		if (title == null) {
			if (path != null) {
				title = path;
			} else {
				title = "No Title";
			}
		}
		
		String scorePerCent = String.valueOf(score*100).substring(0,4) + ":  ";
		titleText = scorePerCent + title;
		
		
		//construct bodyText
		if (keywords !=null) {
			bodyText=bodyText+"Keywords : "+keywords.trim();
		}
		if (author !=null) {
			bodyText=bodyText+"Author : "+author.trim()+"\n";
		}
		if (tupinfos !=null) {
			bodyText=bodyText+"FactInfo : "+tupinfos.trim()+"\n";
		}
		if (summary !=null) {
			bodyText=bodyText+"Summary : "+summary.trim()+"\n";
		}
		
		
		//add stuff to View
		ExpandableComposite ec = m_toolkit.createExpandableComposite(m_form.getBody(), ExpandableComposite.TREE_NODE|ExpandableComposite.CLIENT_INDENT);
		ec.setText(titleText);
		
		Label bodyTextLabel = m_toolkit.createLabel(ec, bodyText, SWT.WRAP);
		ec.setClient(bodyTextLabel);
		ec.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				m_form.reflow(true);
			}
		});
		
		//add a link to open referenced document
		if (path != null) {
			final String finalPath = path;
			ImageHyperlink eci = m_toolkit.createImageHyperlink(ec, SWT.LEFT);
			ec.setTextClient(eci);
			eci.setText("Open");
			eci.setToolTipText(path);

			if (path.toLowerCase().endsWith(".htm") || path.toLowerCase().endsWith(".html")) {
				IWorkbenchBrowserSupport support= PlatformUI.getWorkbench().getBrowserSupport();
				try {
					final File file = new File(path);
					IWebBrowser browser = support.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR|IWorkbenchBrowserSupport.NAVIGATION_BAR,null,file.getName(),path);
					browser.openURL(file.toURL());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (PartInitException e2) {
					e2.printStackTrace();
				}
				
			}else {
				eci.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						Program.launch(finalPath);
					}
				});
			}
		}
	}
	
	private void doDisplayHeadline(String headline) {
		Label l = m_toolkit.createLabel(m_form.getBody(), headline, SWT.BOLD);
		l.setForeground(m_form.getForeground());
		l.setFont(m_form.getFont());
	}
	
	@Override
	public ISelection getSelection() {
		return null;
	}
	
	@Override
	public void setSelection(ISelection selection, boolean reveal) {
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
