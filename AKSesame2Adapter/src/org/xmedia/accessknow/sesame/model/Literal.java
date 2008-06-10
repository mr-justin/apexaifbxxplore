package org.xmedia.accessknow.sesame.model;

import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IOntology;

public class Literal extends org.xmedia.oms.model.impl.Literal {

	private static final long serialVersionUID = 329011310478154272L;
	
	private Set<IDatatype> datatypes = new HashSet<IDatatype>();
	
	protected Literal(Object value, IOntology onto) {
		super(value, onto);
	}

	@Override
	public Set<IDatatype> getDatatypes() {
		return datatypes;
	}
	
	public void addDatatype(IDatatype itsDatatype) {
		datatypes.add(itsDatatype);
	}

	@Override
	public String toString() {
		return getLiteral();
	}

	@Override
	public boolean equals(Object res) {
		
		boolean areEquals = false;
		if (res instanceof ILiteral) {
			areEquals = (getLiteral().equals(((ILiteral)res).getLiteral()));
		}
		
		return areEquals;
	}
	
}
