package com.ibm.semplore.btc.impl;

import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSources;
import com.ibm.semplore.imports.impl.data.load.Util4NT;

/**
 * @author xrsun
 *
 */
public class SchemaObjectInfoForMultiDataSourcesImpl implements
		SchemaObjectInfoForMultiDataSources {
	public SchemaObjectInfoForMultiDataSourcesImpl(String uri,
			String dataSource, long id, String label, String summary,
			String textDesc) {
		URI = uri;
		this.dataSource = dataSource;
		this.id = id;
		this.label = label;
		this.summary = summary;
		this.textDesc = textDesc;
	}

	private String dataSource;
	private long id;
	private String label;
	private String summary;
	private String textDesc;
	private String URI;
	
	@Override
	public String getDataSource() {
		return dataSource;
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getSummary() {
		return summary;
	}

	@Override
	public String getTextDescription() {
		return textDesc;
	}

	@Override
	public String getURI() {
		return URI;
	}

	@Override
	public String getURILocalName() {
        return Util4NT.getDefaultLabel(URI);
	}

}
