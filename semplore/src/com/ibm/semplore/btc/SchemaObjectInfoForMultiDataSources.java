package com.ibm.semplore.btc;

import com.ibm.semplore.model.SchemaObjectInfo;

public interface SchemaObjectInfoForMultiDataSources extends SchemaObjectInfo {
	
	public String getDataSource();
	
}
