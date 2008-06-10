package org.xmedia.oms.adapter.kaon2.ex;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.Cursor;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.Fact;
import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Namespaces;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
import org.semanticweb.kaon2.api.logic.Term;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.semanticweb.kaon2.api.reasoner.Query;
import org.semanticweb.kaon2.api.reasoner.Reasoner;

/**
 * This example shows how to use the metaviews framework for the management of meta-knowledge, i.e. metal-level statements 
 * about statements. In particular, it shows how provenance and uncertainty information can be managaned in a logical way 
 * using a metalevel ontology (metaview) that is automatically generated by KAON2. This example bases on Example10 that shows
 * the management of meta-knowledge through the generated entity metaview. As describe in [reference [9] from KAON2's Web site], 
 * KAON2 can also generate an axiom metaview, which is a "axiom-based metalevel view" of the original ontology. This example also show this axiom metaview
 * and how to combine both view into a more general "total metaview".   
 *  
 * In OWL 1.1, metalevel statements about statements can be  asserted using annotations, which can be constant- or individual-valued. Also, annotations can refer to an axiom 
 * or an entity (class, individual, etc.). In metaknow-ont.xml, concepts and a annotation vocabulary is defined. In metaknow-example,     
 * this vocabulary is imported to assert that some statements are produced by a particular agent (individual-valued), 
 * and that truth value of some statements is subjected to a particular confidence level (constant-valued). 
 * 
 * Note that however, these annotations in  metaknow-example is primarily intended for hunam users and have no 
 * semantic import. In order to give logical interpretation to this metalogical information expressed using annotations,
 * a metaviews is generated from the main ontology---which is the metaknow-example in this case. Through a predefined transformation,
 * entity and axioms in the metaknow-example are translated into instances of a metalevel ontology containing concepts
 * such as class assertions, data property assertions and object property assertions (see reference [9] from KAON2's Web site). 
 * 
 * These logical definitions of meta-information generated in the form of an entity metaview can be enhanced. For this, 
 * we specify in the main ontology that the meta-knowledge-mw-ext shall be imported by the generated metaview. 
 * In particular, the meta-knowledge-mw-ext contains some more axioms which further constraints the interpretation of the 
 * annotations that has been map to data (constant-valued annotation) or object property assertions during the metaview 
 * generation. Any other OWL axioms can be added in the same way to add further logical definitions to this metalevel
 * ontology for a logical management of meta knowledge. 
 */
public class MetaknowWithOWL11MV {
    public static void main(String[] args) throws Exception {
        
    	// The main ontology containing statements and meta-level statements in the form of annotations.
    	DefaultOntologyResolver resolver=new DefaultOntologyResolver();

        // The extension containning additional logical definitions to be imported by the metaview. 
        String metaOntURI = resolver.registerOntology("file:../OMS/res/org/xmedia/oms/model/onto/metaknow/metaknow.owl");
        String metaviewExtensionURI = resolver.registerOntology("file:res/metaknowledge-mw-ext.xml");
    	//the meta ontology containing vocabulary to make meta-level statements
//    	String metaOntURI=resolver.registerOntology("file:res/metaknow-ont.xml");
    	
    	//the main ontology containing some example statements and annotations to assert metalevel statements about statements
    	String ontologyURI=resolver.registerOntology("file:res/metaknow-example.xml");

        // Let us now open the entity metaview of the ontology 'metaknow-example'. As described in
        // the paper [9], the entity metaviews have ontology URIs that are obtained from the URI
        // of the ontology by prefixing it with 'ent:'. Note that this 'ent:' is not a namespace
        // prefix; rather, it is just a dumb prefix that has been assigned to entity metaviews.
        // Hence, the following line computes the prefix of the entity metaview:
        String entityMetaviewOntologyURI="ent:"+ontologyURI;
        
        // Do the same to obtain the axiom metaview
        String axiomMetaviewOntologyURI="ax:"+ontologyURI;
        System.out.println(axiomMetaviewOntologyURI);
        // Define a combined axiom and entity metaview
        String metaviewOntologyURI="meta:"+ontologyURI;

        // This connection will contain the original ontology and all metaview-related information.
        KAON2Connection connection=KAON2Manager.newConnection();
        connection.setOntologyResolver(resolver);
        
        
        // We now open the entity metaview by simply asking for the mentioned URI.
//        Ontology entityMetaview=connection.openOntology(entityMetaviewOntologyURI,new HashMap<String,Object>());
        
        // We open the axiom metaview by simply asking for the mentioned URI.
        Ontology metaknow = connection.openOntology(metaOntURI, new HashMap<String,Object>());
        Ontology metaviewExtension = connection.openOntology(metaviewExtensionURI, new HashMap<String,Object>());
        Ontology onto = connection.openOntology(ontologyURI, new HashMap<String,Object>());
        Ontology axiomMetaview=connection.openOntology(axiomMetaviewOntologyURI,new HashMap<String,Object>());
        
        System.out.println(metaviewExtensionURI);
        
        //Define a metaview that imports both the axiom and the entity metaview
//        Ontology metaview = connection.createOntology(metaviewOntologyURI, new HashMap<String,Object>());
//        metaview.addToImports(axiomMetaview);
//        metaview.addToImports(entityMetaview);
        
        // We'll use these namespaces to shorten the display of the results.
        Namespaces namespaces=new Namespaces();
        namespaces.registerPrefix("o",ontologyURI+"#");                                 // o: stands for the original ontology
//        namespaces.registerPrefix("e",metaviewExtensionURI+"#");                        // e: stands for the the metaview extension
        namespaces.registerPrefix("ent","http://www.cs.man.ac.uk/EntityMetaview#");     // read on for the meaning of ent:
        namespaces.registerPrefix("ax","http://www.cs.man.ac.uk/AxiomMetaview#");     // read on for the meaning of ent:
        
//        // the data properties in the ontology correspond to the constant-valued annotations of the original ontology.
//        System.out.println(" Data properties in the entity metaview:");
//        System.out.println("-------------------------------------------");
//        printEntityCursor(namespaces,entityMetaview.createEntityRequest(DataProperty.class).openCursor());
//        
//        //the object properties in the ontology encompasses the individual-valued annotations of the original ontology.
//        System.out.println(" Object properties in the entity metaview:");
//        System.out.println("-------------------------------------------");
//        printEntityCursor(namespaces,entityMetaview.createEntityRequest(ObjectProperty.class).openCursor());
//
//        // The contents of this ontology contains entities representing reified axioms and entities of the original ontology.
//        System.out.println(" Facts in the entity metaview:");
//        System.out.println("-------------------------------------------");
//        printAxiomCursor(namespaces,entityMetaview.createAxiomRequest(Fact.class).openCursor());
//
//        System.out.println(" The axioms in the axiom metaview:");
//        System.out.println("-------------------------------------------");
//        printAxiomCursor(namespaces,axiomMetaview.createAxiomRequest().openCursor());
        
//        Set<Axiom> axioms = onto.createAxiomRequest().getAll();
//        for (Axiom axiom : axioms) {
//        	System.out.println(axiom);
//        }
//    	System.out.println();
        
        Set<Axiom> axioms = axiomMetaview.createAxiomRequest().getAll();
        for (Axiom axiom : axioms) {
        	System.out.println(axiom);
        }
    	System.out.println("-----------------------------");

        axioms = metaviewExtension.createAxiomRequest().getAll();
        for (Axiom axiom : onto.createAxiomRequest(ObjectPropertyMember.class).setCondition("objectProperty", KAON2Manager.factory().objectProperty("http://kaon2.semanticweb.org/example10-ontology#hasRelation")).getAll()) {
        	System.out.println(((ObjectPropertyMember)axiom).getObjectProperty());
        }
    	System.out.println();
//        
//        System.out.println(" The axioms in the combined metaview:");
//        System.out.println("-------------------------------------------");
//        System.out.println("right no axioms will be printed because imported ontologies are not considered in the implementation...");
////        printAxiomCursor(namespaces,metaview.createAxiomRequest().openCursor());
//        
//        // we wants to add logical statements to further defines the reified 'metaknow-example.xml'.
//        // Therefore, we created the ontology 'metaknowledge-mw-ext.xml' that describes further relationships.
//        // Let us open this ontology. The ontology URI of this ontology can be chosen freely (i.e., the metaview
//        // framework does not proscribe it); hence, we use the URI returned by the resolver.
//        Ontology metaviewExtension=connection.openOntology(metaviewExtensionURI,new HashMap<String,Object>());
//
//        // Note that, in the entity extension, annotation properties from the original ontology
//        // are treated as data properties, just like in the entity metaview.
//        System.out.println(" The axioms in the metaview extension:");
//        System.out.println("-------------------------------------------");
//        printAxiomCursor(namespaces,metaviewExtension.createAxiomRequest().openCursor());
        
        
        // Phew, this was a lot of work. Let us now see what we can do using the metaviews.
        // For example, we can ask a query for all textual annotations. Because the entity
        // metaview imports 'metaknowledge-mw-ext.xml', our query will obey the semantics specified
        // in that ontology.
        final String AXIOM_METAVIEW_ONTOLOGY_URI = "http://www.cs.man.ac.uk/AxiomMetaview";
        String query = "SELECT ?e WHERE { \n" +
    		"?e <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#ObjectPropertyAssertion>. \n" +
    		"?e <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#objectProperty> <http://kaon2.semanticweb.org/example10-ontology#hasRelation>.\n" +
    		"?e <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#sourceIndividual> <http://kaon2.semanticweb.org/example10-ontology#Adam>.\n" +
    		"?e <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#targetIndividual> <http://kaon2.semanticweb.org/example10-ontology#Eve>.\n" +
    		"}";
        
        query = "SELECT ?e ?p WHERE { \n" +
        	"?e <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#ObjectPropertyAssertion> . \n" +
        	"?e <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#objectProperty> ?bla .\n" +
        	"?e <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#sourceIndividual> ?blu .\n" +
        	"?e <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#targetIndividual> ?ble .\n" +
        	"?e <http://www.x-media.org/ontologies/metaknow#has_Provenance> ?p . \n" +   
        	"?p <http://www.x-media.org/ontologies/metaknow#confidence_degree> \"0.4\"^^<http://www.w3.org/2001/XMLSchema#float> . \n" +
		"}";

        List<String[]> results = runQuery(null, axiomMetaview, query);
        
        for (String [] result : results) {
        	for (String s : result)
        		System.out.print(s + " ");
        	System.out.println();
        }
        
        String axiomUri = results.get(0)[0];

        System.out.println("axiom uri: " + axiomUri + "\n");
        
        query = "SELECT ?a WHERE { \n" +
        	"<" + axiomUri + "> <http://www.x-media.org/ontologies/metaknow#has_Provenance> ?a . \n" +
	    	"}";
        results = runQuery(null, metaviewExtension, query);
        
        String provenanceUri = results.get(0)[0];
        
        System.out.println("provenance uri: " + provenanceUri);
        

        query = "SELECT ?e WHERE { \n" +
    	"<" + provenanceUri + "> <http://www.x-media.org/ontologies/metaknow#source> ?e . \n" +
    	"}";
        results = runQuery(null, metaviewExtension, query);
        
        System.out.println(results.get(0)[0]);
//        System.out.println(" The list of textual annotations from the original ontology:");
//        System.out.println("-------------------------------------------");
//        executeQuery(namespaces,entityMetaview,"SELECT ?e ?a WHERE { ?e <http://x-media.org/metaknowledge-mw-ext#textAnnotation> ?a }");
//        
//        System.out.println(" The list of axioms that has confidence degree higher than 0.6:");
//        System.out.println("-------------------------------------------");
//        executeQuery(namespaces,axiomMetaview,"SELECT ?e ?a WHERE { ?e <http://x-media.org/metaknowledge#uncertaintyGreaterThan> 0.6 }");
//                
//        System.out.println(" The list of axioms that has been produced by thanh:");
//        System.out.println("-------------------------------------------");
//        executeQuery(namespaces,axiomMetaview,"SELECT ?e ?a WHERE { ?e <http://x-media.org/metaknowledge#agentAnnotation> <http://x-media.org/metaknowledge#Thanh> }");
        
//        axiomMetaview.saveOntology(OntologyFileFormat.OWL_1_1_XML,new File("axiom-mw.xml"),"UTF-8");
//        entityMetaview.saveOntology(OntologyFileFormat.OWL_1_1_XML,new File("C:\\MyFiles\\Projekte\\Workspaces\\current_projects\\xxplore\\trunk\\standard-kaon2\\res\\entity-mw.xml"),"UTF-8"); 
        // Finally, we clean-up before we exit.
        connection.close();
    }
    protected static <T extends OWLEntity> void printEntityCursor(Namespaces namespaces,Cursor<T> cursor) throws KAON2Exception {
        try {
            while (cursor.hasNext()) {
                T element=cursor.next();
                StringBuffer buffer=new StringBuffer();
                element.toString(buffer,namespaces);
                System.out.println(buffer.toString());
            }
            System.out.println("-------------------------------------------");
            System.out.println();
        }
        finally {
            cursor.close();
        }
    }
    protected static <T extends Axiom> void printAxiomCursor(Namespaces namespaces,Cursor<T> cursor) throws KAON2Exception {
        try {
            while (cursor.hasNext()) {
                T element=cursor.next();
                StringBuffer buffer=new StringBuffer();
                element.toString(buffer,namespaces);
                System.out.println(buffer.toString());
            }
            System.out.println("-------------------------------------------");
            System.out.println();
        }
        finally {
            cursor.close();
        }
    }
    
    protected static List<String[]> runQuery(Namespaces namespaces, Ontology ontology, String queryText)throws KAON2Exception, InterruptedException {
    	
    	System.out.println("query: " + queryText);
    	
        Reasoner reasoner=ontology.createReasoner();
        List<String[]> terms = new ArrayList<String[]>();
        try {
            Query query=reasoner.createQuery(namespaces,queryText);
            try {
                query.open();
                Term[] tupleBuffer = query.tupleBuffer();
                while (!query.afterLast()) {
                	String[] results = new String [tupleBuffer.length];
                	for (int i = 0; i < results.length; i++)
                		results[i] = tupleBuffer[i].toString();
                	terms.add(results);
                    query.next();
                }
                query.close();
            }
            finally {
                query.dispose();
            }
        }
        finally {
            reasoner.dispose();
        }
        
        return terms;
    }
    
    protected static void executeQuery(Namespaces namespaces,Ontology ontology,String queryText) throws KAON2Exception,InterruptedException {
        Reasoner reasoner=ontology.createReasoner();
        try {
            Query query=reasoner.createQuery(namespaces,queryText);
            try {
                query.open();
                Term[] tupleBuffer=query.tupleBuffer();
                while (!query.afterLast()) {
                    System.out.print("[ ");
                    for (int i=0;i<tupleBuffer.length;i++) {
                        if (i>0)
                            System.out.print(", ");
                        StringBuffer buffer=new StringBuffer();
                        tupleBuffer[i].toString(buffer,namespaces);
                        System.out.print(buffer.toString());
                    }
                    System.out.println(" ]");
                    query.next();
                }
                query.close();
                System.out.println("-------------------------------------------");
                System.out.println();
            }
            finally {
                query.dispose();
            }
        }
        finally {
            reasoner.dispose();
        }
    }
}

