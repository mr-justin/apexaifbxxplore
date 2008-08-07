import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.Facet;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.XFacetedSearchable;
import com.ibm.semplore.search.impl.SearchFactoryImpl;

public class ForServlet {
    DOMParser parser = new DOMParser();
	XFacetedSearchable searcher = null;
	SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
	SearchFactory searchFactory = SearchFactoryImpl.getInstance();
	Hashtable<String,XFacetedResultSet> resultCache = new Hashtable<String,XFacetedResultSet>();
	
	private GeneralCategory createGeneralCategory(Node node) {
		NamedNodeMap map = node.getAttributes();
		CompoundCategory cat = schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND);
		Node n = map.getNamedItem("conceptConstraint"); 
		if (n!=null && !n.getNodeValue().equals("") ) {
			cat.addComponentCategory(schemaFactory.createCategory(n.getNodeValue()));
		}
		n = map.getNamedItem("keyword");
		if (n!=null && !n.getNodeValue().equals("") ) {
			cat.addComponentCategory(schemaFactory.createKeywordCategory(n.getNodeValue()));
		}
		return cat;
	}
	
	private void addRelations(CatRelGraph graph, int fromNodeIndex, NodeList nodes) {
		if (nodes==null)
			return;
		for (int i=0; i<nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals("#text"))
				continue;
//			System.out.println("name="+node.getNodeName());
			graph.add(createGeneralCategory(node));
			int toNodeIndex = graph.numOfNodes()-1;
			NamedNodeMap map = node.getAttributes();
			if (map.getNamedItem("inverse").getNodeValue().equals("0")) {
				graph.add(schemaFactory.createRelation(map.getNamedItem("relation").getNodeValue()), fromNodeIndex, toNodeIndex);
			} else {
				graph.add(schemaFactory.createRelation(map.getNamedItem("relation").getNodeValue()), toNodeIndex, fromNodeIndex);
			}
			addRelations(graph, toNodeIndex, node.getChildNodes());
		}
	}
	
	private XFacetedQuery XML2Query(Document doc) {
        try {
            Node node = null;
            NamedNodeMap map = null;
            
            node = doc.getElementsByTagName("action").item(0);
        	map = node.getAttributes();
        	if (!map.getNamedItem("type").getNodeValue().equals("all")) {
        		return null;
        	}
        	
        	//create XFacetedQuery
            node = doc.getElementsByTagName("targetNode").item(0);
        	map = node.getAttributes();
        	int target = new Integer(map.getNamedItem("index").getNodeValue()).intValue();
        	
        	CatRelGraph graph = schemaFactory.createCatRelGraph();
        	node = doc.getElementsByTagName("queryNode").item(0);
        	graph.add(createGeneralCategory(node));
//			System.out.println("name="+node.getNodeName());
        	addRelations(graph, 0, node.getChildNodes());
        	System.out.println(graph);
        	System.out.println("target="+target);
        	
            XFacetedQuery query = searchFactory.createXFacetedQuery();
            query.setSearchTarget(target);
            query.setQueryConstraint(graph);
            query.setResultSpec(searchFactory.createXFacetedResultSpec());            
            return query;
        } catch (Exception ex) {
            System.out.println(ex);
        }		
		return null;
	}
	
	private String nomalizeLabel(String label) {
		return label.replaceAll("[0-9]*", "");
	}
	private String result2XML(XFacetedResultSet resultSet, Document doc2) {
        try {        	         
        } catch (Exception ex) {
            System.out.println(ex);
        }		
        try {
            Node node = null;
            NamedNodeMap map = null;            
            node = doc2.getElementsByTagName("action").item(0);
        	map = node.getAttributes();
        	
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();            
            Element root = doc.createElement("result");     // Create Root Element
        	if (map.getNamedItem("type").getNodeValue().equals("all") || map.getNamedItem("type").getNodeValue().equals("instanceList") ) {
                Element item = doc.createElement("instanceList");       // Create element
                item.setAttribute("count", Integer.toString(resultSet.getLength()));
                int start = new Integer(map.getNamedItem("start").getNodeValue()).intValue();
                int count = new Integer(map.getNamedItem("count").getNodeValue()).intValue();
                int end = start+count;
                if (end > resultSet.getLength()) end = resultSet.getLength();
                for (int i=start; i<end; i++) {
                	SchemaObjectInfo result = resultSet.getResult(i);
                	Element subitem = doc.createElement("instance");
                	subitem.setAttribute("name", result.getLabel());
                	subitem.setAttribute("uri", result.getURI());
                	subitem.setAttribute("snippet", result.getTextDescription());
                	item.appendChild(subitem);
                }
                root.appendChild( item );                       // atach element to Root element
        	}
            
        	if (map.getNamedItem("type").getNodeValue().equals("all") || map.getNamedItem("type").getNodeValue().equals("conceptList") ) {
                Element item = doc.createElement("conceptList");       // Create element
                int start = new Integer(map.getNamedItem("start").getNodeValue()).intValue();
                int count = new Integer(map.getNamedItem("count").getNodeValue()).intValue();
                int end = start+count;
                Facet[] facets = resultSet.getCategoryFacets(); 
                if (end>facets.length)
                	end = facets.length;
                item.setAttribute("count", Integer.toString(facets.length));
                for (int i=start; i<end; i++) {
                	Facet result = facets[i];
                	Element subitem = doc.createElement("concept");
                	subitem.setAttribute("name", nomalizeLabel(result.getInfo().getLabel()));
                	subitem.setAttribute("uri", result.getInfo().getURI());
                	subitem.setAttribute("count", String.valueOf(result.getCount()));
                	item.appendChild(subitem);
                }
                root.appendChild( item );                       // atach element to Root element
        	}

        	if (map.getNamedItem("type").getNodeValue().equals("all") || map.getNamedItem("type").getNodeValue().equals("relationList") ) {
                Element item = doc.createElement("relationList");       // Create element
                int start = new Integer(map.getNamedItem("start").getNodeValue()).intValue();
                int count = new Integer(map.getNamedItem("count").getNodeValue()).intValue();
                int end = start+count;
                Facet[] facets = resultSet.getRelationFacets(); 
                if (end>facets.length)
                	end = facets.length;
                item.setAttribute("count", Integer.toString(facets.length));
                for (int i=start; i<end; i++) {
                	Facet result = facets[i];
                	Element subitem = doc.createElement("relation");
                	subitem.setAttribute("name", result.getInfo().getLabel());
                	subitem.setAttribute("uri", result.getInfo().getURI());
                	subitem.setAttribute("count", String.valueOf(result.getCount()));
                	if (result.isInverseRelation())
                		subitem.setAttribute("inverse", "1");
                	else
                		subitem.setAttribute("inverse", "0");
                	item.appendChild(subitem);
                }
                root.appendChild( item );                       // atach element to Root element
        	}
        	            
            doc.appendChild( root );                        // Add Root to Document
            OutputFormat    format  = new OutputFormat( doc );   //Serialize DOM
            StringWriter  stringOut = new StringWriter();        //Writer will be a String
            XMLSerializer    serial = new XMLSerializer( stringOut, format );
            serial.asDOMSerializer();                            // As a DOM Serializer
            serial.serialize( doc.getDocumentElement() );
//            System.out.println( "STRXML = " + stringOut.toString() ); //Spit out DOM as a String
            return stringOut.toString();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        
		return null;
	}
	
	public String query(String queryXML, String uid) {
        try {
        	XFacetedResultSet resultSet = null;
        	
            parser.parse(new InputSource(new StringReader(queryXML)));
            Document doc = parser.getDocument();
            Node node = null;
            NamedNodeMap map = null;
            
            //create result set
            node = doc.getElementsByTagName("action").item(0);
        	map = node.getAttributes();
        	if (map.getNamedItem("type").getNodeValue().equals("all")) {//a new query
        		XFacetedQuery query = XML2Query(doc);
        		resultSet = searcher.search(query);
        	} else {//"more_instance","more_concept","more_relation"
        		resultSet = resultCache.get(uid);
        		if (resultSet==null) {
            		XFacetedQuery query = XML2Query(doc);
            		resultSet = searcher.search(query);
        		}
        	}
        	
        	//result set to xml
        	return result2XML(resultSet, doc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }		
		return null;
	}
	
	public void init(String configFile) throws Exception {
		Properties config = Config.readConfigFile(configFile);
        XFacetedSearchService searchService = searchFactory.getXFacetedSearchService(config);
        searcher = searchService.getXFacetedSearchable();		
	}

	public static void main(String[] args) {
		try {
			ForServlet forServ = new ForServlet();
			String configFile = args[0];//"E:\\User\\AllisQM\\Semplore\\SSModel\\config\\semplore-dbpedia-yago.cfg";
			forServ.init(configFile);//config file
			String queryXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<query>" +
					"	<action type=\"all\" start=\"0\" count=\"10\"/>" +
					"	<targetNode index=\"0\"/>" +
					"	<graph>" +
					"		<queryNode relation=\"\" conceptConstraint=\"&lt;http://dbpedia.org/class/yago/City108524735>\" keyword=\"earthquake\">" +
					"		</queryNode>" +
					"	</graph>" +
					"</query>";
			String uid = "127.0.0.1";
			String resultXML = forServ.query(queryXML,uid);
			System.out.println("uid:"+uid);
			System.out.println("queryXML:"+queryXML);
			System.out.println("resultXML:"+resultXML);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
