package org.aifb.xxplore.storedquery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StoredQueryListManager {
    	
    private File storedQueryFile;
    private StoredQueryList storedQueryList = new StoredQueryList();
    private int nextQueryId;    
    
    public StoredQueryListManager(File file) {
        this.storedQueryFile = file;
        nextQueryId = 1;
    }    
        
    public StoredQueryList createNewQueryList() {
    	storedQueryList = new StoredQueryList();
        return storedQueryList;
    } 

    public String genUniqueTaskId() {
        return "query-" + nextQueryId++;
    }
    
    public void saveQueryList() {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element queries = doc.createElement("Queries");
			doc.appendChild(queries);
			for (IStoredQueryListElement queryListElement : storedQueryList.getQueryList()) {
				IQuery query = queryListElement.getQuery();
				Element queryElement = doc.createElement("Query");
				queryElement.setAttribute("name", query.getName());
				queryElement.setAttribute("description", query.getDescription());
				queryElement.setAttribute("notes", query.getNotes());
				Stack<Prefix> prefixes = query.getPrefixes();
				for(Prefix prefix : prefixes){
					Element prefixElement = doc.createElement("Prefix");
					prefixElement.setAttribute("prefix", prefix.getPrefix());
					prefixElement.setAttribute("ontology", prefix.getOntology());
					queryElement.appendChild(prefixElement);
				}
				Stack<String[]> predicates = query.getPredicates();
				for(String[] queryPredicate : predicates){
					Element predicateElement = doc.createElement("Predicate");
					predicateElement.setAttribute("subject", queryPredicate[0]);
					predicateElement.setAttribute("predicate", queryPredicate[1]);
					predicateElement.setAttribute("object", queryPredicate[2]);
					queryElement.appendChild(predicateElement);
				}
				Set<String> variables = query.getVariables();
				for(String variable : variables){
					Element variableElement = doc.createElement("Variable");
					variableElement.setAttribute("field", variable);
					queryElement.appendChild(variableElement);
				}
				Set<String> selectedVars = query.getSelectedVariables();
				for(String  selectedVar : selectedVars){
					Element selectedVariableElement = doc.createElement("SelectedVariable");
					selectedVariableElement.setAttribute("field", selectedVar);
					queryElement.appendChild(selectedVariableElement);
				}
				queries.appendChild(queryElement);
			}
			
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT,"yes");
			if(!storedQueryFile.exists()){
				storedQueryFile.createNewFile();
			}
			t.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(storedQueryFile)));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
    }  
    
    public void readQueryList() {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	if(!storedQueryFile.exists())
			return;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(storedQueryFile);
			NodeList nl = doc.getElementsByTagName("Query");
			int len = 0;
			if(nl != null) len = nl.getLength();
			for(int i=0; i<len; i++) {
				Element queryElement = (Element)nl.item(i);
				IQuery query = new Query();
				query.setName(queryElement.getAttribute("name"));
				query.setDescription(queryElement.getAttribute("description"));
				query.setNotes(queryElement.getAttribute("notes"));
				
				NodeList pnl = queryElement.getElementsByTagName("Prefix"); 
				Stack<Prefix> prefixes = new Stack<Prefix>();
				int plen = pnl.getLength();
				for(int j=0; j<plen; j++) {
					Element prefixElement = (Element)pnl.item(j);
					prefixes.push(new Prefix(prefixElement.getAttribute("prefix"), prefixElement.getAttribute("ontology"))); 
				}	
				query.setPrefixes(prefixes);
				
				NodeList vnl = queryElement.getElementsByTagName("Variable"); 
				Set<String> variables = new HashSet<String>();
				int vlen = vnl.getLength();
				for(int j=0; j<vlen; j++) {
					Element varElement = (Element)vnl.item(j);
					variables.add(varElement.getAttribute("field")); 
				}	
				query.setVariables(variables);
				
				NodeList snl = queryElement.getElementsByTagName("SelectedVariable"); 
				Set<String> selectedVars = new HashSet<String>();
				int slen = snl.getLength();
				for(int j=0; j<slen; j++) {
					Element svarElement = (Element)snl.item(j);
					selectedVars.add(svarElement.getAttribute("field")); 
				}
				query.setSelectedVariables(selectedVars);
				
				NodeList prnl = queryElement.getElementsByTagName("Predicate"); 
				Stack<String[]> predicates = new Stack<String[]>();
				int prlen = prnl.getLength();
				for(int j=0; j<prlen; j++) {
					Element predicateElement = (Element)prnl.item(j);
					String[] strs = new String[3];
					strs[0] = predicateElement.getAttribute("subject");
					strs[1] = predicateElement.getAttribute("predicate");
					strs[2] = predicateElement.getAttribute("object");
 					predicates.push(strs); 
				}	
				query.setPredicates(predicates);
				
				IStoredQueryListElement newQueryListElement = new StoredQueryListElement(query, genUniqueTaskId());
				storedQueryList.addQuery(newQueryListElement);
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 
    
    public StoredQueryList getQueryList() {
        return storedQueryList;
    }
    
    public void setQueryList(StoredQueryList storedQueryList) {
        this.storedQueryList = storedQueryList;
    }

    public void addQuery(IStoredQueryListElement task) {
    	storedQueryList.addQuery(task);
    }
        
    public void deleteTask(IStoredQueryListElement task) {
        storedQueryList.deleteQuery(task);
    }
    
    public void setStoredQueryFile(File f) {
    	if (this.storedQueryFile.exists()) {
    		this.storedQueryFile.delete();
    	}
    	this.storedQueryFile = f;
    }
    
    public IStoredQueryListElement getTaskForHandle(String handle) {
    	return storedQueryList.getTaskForHandle(handle);
    }
}
