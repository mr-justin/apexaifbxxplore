package org.xmedia.oms.model.impl;

import org.aifb.xxplore.shared.exception.EmergencyException;
import org.xmedia.businessobject.AbstractBo;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IResource;

public class Resource extends AbstractBo implements IResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6119504672483585595L;

	private String m_label;

	private IOntology m_onto; 
	
	private Object m_delegate;

	public Resource() {
		super();
	}
	
	public Resource(Long id) {
		super(id);
	}
	
	public Resource(Long id, IOntology onto) {
		super(id);
		m_onto = onto;
	}

	public Resource(IOntology onto){
		super();
		m_onto = onto;
	};

	public Resource(String label){
		super();
		m_label = label;

	}
	
	public Resource(String label, IOntology onto){
		super();
		m_label = label;
		m_onto = onto;
	}

	public String toString() {
		return getLabel();
	}

	public String getLabel() {
		if (m_label != null) return m_label; 
		else return getOid().toString();
	}

	public void setLabel(String label) {
		m_label = label;
	}

	public IOntology getOntology() {
		return m_onto;
	}
	
	public void setDelegate(Object delegate){
		m_delegate = delegate;
	}
	
	public Object getDelegate(){
		
		return m_delegate;
	}
	
	public boolean equals(Object res){
		
		if(res instanceof Resource) 
		{
			if (((Resource)res).getOid() == getOid()) 
				return true;
			else 
				if (((Resource)res).getLabel().equals(getLabel())) 
					return true;
		
				else 
					return false;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		if (getLabel() != null) {
			return getLabel().hashCode();
		} else {
			throw new EmergencyException("Resource has no label!");
		}
	}
}
