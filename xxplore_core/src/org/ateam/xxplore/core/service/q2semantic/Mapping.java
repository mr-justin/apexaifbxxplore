package org.ateam.xxplore.core.service.q2semantic;


public abstract class Mapping{
	private String m_source;
	private String m_sourceDS;

	private String m_target;
	private String m_targetDS;

	private double m_confidence;

	public String getSource() {
		return m_source;
	}

	public void setSource(String m_source) {
		this.m_source = m_source;
	}

	public String getSourceDsURI() {
		return m_sourceDS;
	}

	public void setSourceDs(String m_sourcedsURI) {
		m_sourceDS = m_sourcedsURI;
	}

	public String getTarget() {
		return m_target;
	}

	public void setTarget(String m_target) {
		this.m_target = m_target;
	}

	public String getTargetDsURI() {
		return m_targetDS;
	}

	public void setTargetDsURI(String m_targetdsURI) {
		m_targetDS = m_targetdsURI;
	}

	public double getConfidence() {
		return m_confidence;
	}

	public void setConfidence(double m_confidence) {
		this.m_confidence = m_confidence;
	}

}

