package org.xmedia.accessknow.sesame.persistence.converter;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;

public class AK2Ses {
	
	public static Resource getResource(IResource aResource, ValueFactory theValueFactory) throws Exception {
		
		Resource sesResource = null;
		
		Object delegate = aResource.getDelegate();
		if (delegate != null && delegate instanceof Resource)
			sesResource = (Resource)delegate;
		else {
			if (aResource instanceof IEntity) {
				sesResource = theValueFactory.createURI(((IEntity)aResource).getUri());
				DelegatesManager.setDelegate(aResource, sesResource);
			} else if(aResource instanceof IIndividual) {
				String nodeId = ((IIndividual)aResource).getLabel();
				if(nodeId.startsWith("_:")) {
					nodeId = nodeId.substring(2);
				}
				sesResource = theValueFactory.createBNode(nodeId); 
			} else
				throw new Exception("Unsupported resource type as statement subject: " + aResource.getClass().getName());
		}
		
		return sesResource;
	}
	
	public static URI getProperty(IResource aPredicate, ValueFactory theValueFactory) throws Exception {
		
		URI sesProperty = null;
		
		Object delegate = aPredicate.getDelegate();
		if (delegate != null && delegate instanceof URI)
			sesProperty = (URI) delegate;
		else {
			if (aPredicate instanceof IProperty) {
				sesProperty = theValueFactory.createURI(((IProperty)aPredicate).getUri());
				DelegatesManager.setDelegate(aPredicate, sesProperty);
			} else
				throw new Exception("Unsupported resource type as statement property: " + aPredicate.getClass().getName());
		}
		
		return sesProperty;
	}
	
	private static Literal getLiteral(ILiteral aLiteral, ValueFactory theValueFactory) {
		
		Literal sesLiteral = null;
		
		Object delegate = aLiteral.getDelegate();
		if (delegate != null && delegate instanceof Literal)
			sesLiteral = (Literal) delegate;
		else {
			sesLiteral = theValueFactory.createLiteral(aLiteral.getLiteral(), aLiteral.getLanguage());
			DelegatesManager.setDelegate(aLiteral, sesLiteral);
		}
		
		return sesLiteral;
	}
	
	public static Value getObject(IResource anObject, ValueFactory theValueFactory) throws Exception {
		
		Value sesObject = null;
		
		if (anObject instanceof IIndividual)
			sesObject = getResource(anObject, theValueFactory);
		else {
			if (anObject instanceof ILiteral)
				sesObject = getLiteral((ILiteral)anObject, theValueFactory);
			else
				throw new Exception("Unsupported resource type as statement object: " + anObject.getClass().getName());
		}
		
		return sesObject;
	}
	
	public static Statement getStatement(IPropertyMember aPropertyMember, ValueFactory theValueFactory) throws Exception {
		
		Statement sesStatement = null;
		
		Object delegate = aPropertyMember.getDelegate();
		if (delegate != null && delegate instanceof Statement)
			sesStatement = (Statement) delegate;
		else {
			Resource sesSubject = AK2Ses.getResource(aPropertyMember.getSource(), theValueFactory);
			URI sesPredicate = AK2Ses.getProperty(aPropertyMember.getProperty(), theValueFactory);
			Value sesObject = AK2Ses.getObject(aPropertyMember.getTarget(), theValueFactory);

			sesStatement = theValueFactory.createStatement(sesSubject, sesPredicate, sesObject);
			DelegatesManager.setDelegate(aPropertyMember, delegate);
		}

		return sesStatement;
		
	}

}
