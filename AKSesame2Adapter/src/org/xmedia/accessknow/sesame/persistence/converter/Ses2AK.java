package org.xmedia.accessknow.sesame.persistence.converter;

import org.aifb.xxplore.shared.vocabulary.XMLSchema;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.xmedia.accessknow.sesame.model.PropertyMember;
import org.xmedia.accessknow.sesame.persistence.SesameSession;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Datatype;
import org.xmedia.oms.persistence.DatasourceException;

public class Ses2AK {

	public static INamedIndividual getNamedIndividual(Resource aResource, IOntology itsOntology) {
		
		INamedIndividual akResource = itsOntology.createNamedIndividual(aResource.toString());
		DelegatesManager.setDelegate(akResource, aResource);
		
		return akResource;
	}
	
	public static IResource getObject(Value object, IOntology itsOntology) {
		
		IResource akObject = null;
		if (object instanceof Literal) {
			
			Literal literal = (Literal)object;
			akObject =
				itsOntology.createLiteral(literal.getLabel(), getDatatype(literal), literal.getLanguage());
			DelegatesManager.setDelegate(akObject, object);
			
		} else {
			if (object instanceof URI) {
				akObject = getNamedIndividual((URI)object, itsOntology);
			} else if(object instanceof BNode) {
				akObject  = getBlankNode((BNode)object, itsOntology);
			}
		}

		return akObject;
	}
	
	private static String getDatatype(Literal aLiteral) {
		
		if (aLiteral.getDatatype() != null) {
			return aLiteral.getDatatype().toString();
		} else {
			return XMLSchema.STRING;
		}
		
	}
	
	public static IDatatype getDatatype(URI datatype, IOntology itsOntology) {
		
		Long oid = new Long(datatype.hashCode());
		IDatatype akDatatype = new Datatype(oid, datatype.toString(), itsOntology);
		DelegatesManager.setDelegate(akDatatype, datatype);
		
		return akDatatype;
	}

	public static IProperty getProperty(URI predicate, IOntology itsOntology) {
		
		IProperty akProperty = itsOntology.createProperty(predicate.toString());
		DelegatesManager.setDelegate(akProperty, predicate);
		
		return akProperty;
	}
	
	public static IPropertyMember getPropertyMember(Statement aStatement, IOntology itsOntology) {
		
		Value sesObject = aStatement.getObject();
		IIndividual subject;
		if(aStatement.getSubject() instanceof URI) {
			subject = Ses2AK.getNamedIndividual(aStatement.getSubject(), itsOntology);
		} else {
			subject = Ses2AK.getBlankNode(aStatement.getSubject(), itsOntology);
		}
		IProperty property;
		if(isObjectPropertyPredicate(aStatement)) {
			property = getObjectProperty(aStatement.getPredicate(), itsOntology);
		} else {
			property = getDataProperty(aStatement.getPredicate(), itsOntology);
		}
		IResource object = Ses2AK.getObject(sesObject, itsOntology);
		
		return PropertyMember.createPropertyMember(subject, 
				property, 
				object, 
				itsOntology,
				(SesameSession.isReficationContext(aStatement.getContext()) ? aStatement.getContext().toString() : ""));
	}
	
	public static INamedConcept getNamedConcept(Resource aResource, IOntology itsOntology) {
		INamedConcept akConcept = itsOntology.createNamedConcept(((URI)aResource).toString());
		DelegatesManager.setDelegate(akConcept, aResource);
		return akConcept;
	}
	
	public static IIndividual getBlankNode(Resource aResource, IOntology itsOntology) {
		IIndividual akResource = itsOntology.createIndividual(aResource.toString());
		DelegatesManager.setDelegate(akResource, aResource);
		
		return akResource;
	}
	
	
	public static IDataProperty getDataProperty(URI predicate, IOntology itsOntology) {
		IDataProperty akProperty =itsOntology.createDataProperty(predicate.toString());
		DelegatesManager.setDelegate(akProperty,predicate);
		return akProperty;
	}
	
	public static IObjectProperty getObjectProperty(URI predicate, IOntology itsOntology) {
		IObjectProperty akProperty = itsOntology.createObjectProperty(predicate.toString());
		DelegatesManager.setDelegate(akProperty, predicate);
		return akProperty;
	}
	
	public static boolean isObjectPropertyPredicate(Statement stmt) {
		if(stmt.getObject() instanceof Literal) {
			return false;
		}
		return true;
	}
	
	public static boolean isObjectProperty(URI predicate, IOntology onto, RepositoryConnection con) throws DatasourceException {
		
		try {			
			RepositoryResult<Statement> stmts = con.getStatements(predicate, RDFS.RANGE, null, true);
			
			if(stmts.hasNext()){				
				Value range = stmts.next().getObject();
				return isObjectProperty(predicate,range);
			}
		} 
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static boolean isObjectProperty(URI predicate, Value range) throws DatasourceException {
		
		if(predicate.equals(RDFS.COMMENT)|| predicate.equals(RDFS.LABEL)) {
			return false;
		}

		if(range instanceof Literal) {
			return false;
		}					
		else if(range instanceof URI) {

			URI uri = (URI)range;

			if(uri.getNamespace().equals(org.openrdf.model.vocabulary.XMLSchema.NAMESPACE)){
				return false;
			}
			else{	
				return true;
			}
		}

		return true;
	}
}
