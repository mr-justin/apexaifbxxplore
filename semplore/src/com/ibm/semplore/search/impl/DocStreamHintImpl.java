package com.ibm.semplore.search.impl;

import com.ibm.semplore.search.CacheHint;
import com.ibm.semplore.xir.DocStream;

public class DocStreamHintImpl implements CacheHint {
	DocStream ds;
	public DocStreamHintImpl(DocStream d) {
		ds = d;
	}

	@Override
	public DocStream getStream() {
		return ds;
	}

}
