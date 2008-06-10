package org.xmedia.accessknow.sesame.model;

import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;

public class PropertyMember extends org.xmedia.oms.model.impl.PropertyMember {

	private static final long serialVersionUID = 4528591305226424760L;

	private static int getPropertyType(IResource anObject) {
		
		if (anObject instanceof ILiteral)
			return PropertyMember.DATA_PROPERTY_MEMBER;
		else
			return PropertyMember.OBJECT_PROPERTY_MEMBER;
		
	}
	
	public static PropertyMember createPropertyMember(
			IIndividual subject, 
			IProperty property, 
			IResource object, 
			IOntology itsOntology) {
		
		return createPropertyMember( 
				subject, 
				property,
				object, 
				itsOntology, "", null);
		
	}
	
	public static PropertyMember createPropertyMember(
			IIndividual subject, 
			IProperty property, 
			IResource object, 
			IOntology itsOntology,
			String uri) {
		
		return createPropertyMember( 
				subject, 
				property,
				object, 
				itsOntology, uri, null);
		
	}
	
	public static PropertyMember createPropertyMember(
			IIndividual subject, 
			IProperty property, 
			IResource object, 
			IOntology itsOntology,
			String uri,
			IProvenance provenance) {
		
		return new PropertyMember(
				property, 
				subject, 
				object, 
				itsOntology, 
				getPropertyType(object), 
				uri, 
				provenance);
		
	}
	
	protected PropertyMember(
			IProperty prop, 
			IResource source, 
			IResource target, 
			IOntology onto, 
			int type, 
			String uri,
			IProvenance provenance) {
		super(prop, source, target, onto, type, uri, provenance);
	}

//	/**
//	 * @deprecated
//	 */
//	@Override
//	public String getLabel() {
//		return toString();
//	}
	
	@Override
	public String toString() {
		return "<" + getSource() + "," + getProperty() + "," + getTarget() + ">";
	}
}
