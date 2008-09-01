package org.xmedia.oms.model.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.Provenance;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;


public class PropertyMember extends Axiom implements IPropertyMember{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int DATA_PROPERTY_MEMBER = 0;

	public static final int OBJECT_PROPERTY_MEMBER = 1;

	private int m_type;  

	private IProperty m_property;

	private IResource m_source;

	private IResource m_target;

	private IOntology m_onto;
	
	private String uri;
	
	private Set<IProvenance> provenances;

	public PropertyMember(IProperty prop, IResource source, IResource target, IOntology onto, int type) {
		
		this(prop, source, target, onto, type, "", new HashSet<IProvenance>());
		
	}
	
	public PropertyMember(IProperty prop, 
			IResource source, 
			IResource target, 
			IOntology onto, 
			int type, 
			String uri,
			IProvenance provenance) {
		
		this(prop, source, target, onto, type, uri, new HashSet<IProvenance>());
		
	}
	
	public PropertyMember(IProperty prop, 
			IResource source, 
			IResource target, 
			IOntology onto, 
			int type, 
			String uri,
			Set<IProvenance> provenances) {
		super(UniqueIdGenerator.getInstance().getNewId(prop.getUri() + " " + source.getOid() + " " + target.getOid() + " " + type));

		Emergency.checkPrecondition(prop != null && source != null && target != null, "prop != null && source != null && target != null");

		if(type == DATA_PROPERTY_MEMBER){
			Emergency.checkPrecondition(source instanceof IIndividual, "source instanceof IIndividual");
			m_type = DATA_PROPERTY_MEMBER;
		}
		if(type == OBJECT_PROPERTY_MEMBER){
			Emergency.checkPrecondition(source instanceof IIndividual && (target instanceof IIndividual || target instanceof IConcept), 
			"source instanceof IIndividual && target instanceof IIndividual");
			m_type = OBJECT_PROPERTY_MEMBER;
		}

		m_property = prop;
		m_source = source;
		m_target = target;
		m_onto = onto;
		
		if (uri == null) uri = "";
		this.uri = uri.trim();

		if (provenances == null)
			this.provenances = new HashSet<IProvenance>();
		else
			this.provenances = provenances;
	}


	public IResource getSource() {
		return m_source;
	}

	public IResource getTarget() {
		return m_target;
	}

	public IProperty getProperty() {
		return m_property;
	}

	public IOntology getOntology(){
		return m_onto;
	}
	
	public int getType(){
		return m_type;
	}

	public String getLabel() {

		return m_source.getLabel() + " " + m_property.getLabel() + " " + m_target.getLabel();


	}
	
	@Override
	public boolean equals(Object res) {
		// TODO Auto-generated method stub
		return super.equals(res);
	}
	
	public String getUri() {
		
		return uri;
		
	}


	public Set<INamedIndividual> getAgents() {

		Set<INamedIndividual> agents = new HashSet<INamedIndividual>();
		
		for (IProvenance provenance : getProvenances())
			if (provenance.getAgent() != null)
				agents.add(provenance.getAgent());
		
		return agents;
		
	}


	public double[] getConfidenceDegree() {

		Set<Double> confidencesTmp = new HashSet<Double>();
		
		for (IProvenance provenance : getProvenances())
			confidencesTmp.add(provenance.getConfidenceDegree());
		
		double[] confidences = new double[confidencesTmp.size()];
		int i = 0;
		for (Double confidence : confidencesTmp)
			confidences[i++] = confidence.doubleValue();
		
		return confidences;
		
	}


	public Set<Date> getCreationDates() {

		Set<Date> dates = new HashSet<Date>();
		
		for (IProvenance provenance : getProvenances())
			if (provenance.getCreationDate() != null)
				dates.add(provenance.getCreationDate());
		
		return dates;
		
	}


	public IResource getElement() {

		return this;
		
	}


	public Set<IEntity> getMetaknowledgeSources() {

		Set<IEntity> sources = new HashSet<IEntity>();
		
		for (IProvenance provenance : getProvenances())
			if (provenance.getSource() != null)
				sources.add(provenance.getSource());
		
		return sources;
		
	}


	public Set<IProvenance> getProvenances() {

		return provenances;
		
	}
}
