package org.ateam.xxplore.core.service.labelizer;

public class OntologySubLabel {
	
	private String _sublabel;
	private String _type;
	private String _property;
	
	public OntologySubLabel(String sublabel, String type) {
		_sublabel = sublabel;
		_type = type;
		_property = null;
	}
	
	public OntologySubLabel(String sublabel, String type, String property) {
		_sublabel = sublabel;
		_type = type;
		_property = property;
	}
	
	public String getSublabel() {
		return _sublabel;
	}
	
	public String getType() {
		return _type;
	}
	
	public String getProperty() {
		return _property;
	}
	
	public boolean equals(Object obj) {
		OntologySubLabel sublabel = (OntologySubLabel) obj;
		
		if (getProperty() != null && sublabel.getProperty() != null) {
			if (getSublabel().equals(sublabel.getSublabel()) && getType().equals(sublabel.getType()) && getProperty().equals(sublabel.getProperty()))
				return true;
			return false;
		}
		else if (getProperty() == null && sublabel.getProperty() == null) {
			if (getSublabel().equals(sublabel.getSublabel()) && getType().equals(sublabel.getType()))
				return true;
		}
		return false;
	}
	
	public String toString() {
		String returnString = new String(getSublabel() + " (type: " + getType());
		if (getProperty() != null)
			returnString = returnString + " --> via property: " + getProperty();
		returnString = returnString + ")";
		return returnString;
	}
}