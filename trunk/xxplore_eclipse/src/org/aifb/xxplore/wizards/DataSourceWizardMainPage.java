/**
 * 
 */
package org.aifb.xxplore.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class DataSourceWizardMainPage extends WizardNewFileCreationPage {
	
	public static final String DATASOURCE_URI_KEY = "datasourceURI";
	
	public final static String EXTENSION = ".ods"; // Ontology Data Source
	
	private Text m_datasourceURI;

	public DataSourceWizardMainPage(IStructuredSelection selection) {
		super("New Data Source", selection);
	}
	
	
	protected String getDataSourceURI(){
		return m_datasourceURI.getText().trim();
	}
	
	@Override
	protected IFile createFileHandle(IPath filePath) {
		return super.createFileHandle(filePath);
	}
	
	@Override
	protected InputStream getInitialContents() {
		StringBuffer buff = new StringBuffer();
		buff.append(DATASOURCE_URI_KEY+"=");
		buff.append(getDataSourceURI());
		buff.append("\n");
		return new ByteArrayInputStream(buff.toString().getBytes());
	}
}