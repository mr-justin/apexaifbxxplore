package org.xmedia.oms.model.impl;

import java.util.Set;

import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;

public class Datatype extends Resource implements IDatatype {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1264373329042478542L;

	private String m_uri;
		
	public Datatype(String uri){
		m_uri = uri;
	}
	
	public Datatype(IOntology onto){
		super(onto);
	}
	
	public Datatype(String uri, IOntology onto){
		super(onto);
		m_uri = uri;
	}
	
	public Datatype(Long oid, String uri, IOntology onto){
		super(oid,onto);
		m_uri = uri;
	}
	
	public Class getJavaClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IProperty> getDataPropertiesTo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getUri() {
		return m_uri;
	}
	
	public String getLabel(){
		return m_uri;
	}
	
	@Override
	public boolean equals(Object res) {
		if (res instanceof Datatype && m_uri != null){
			if(((Datatype)res).getUri().equals(getUri())) return true;
		}
		
		return super.equals(res);
	}
}
