package org.xmedia.oms.metaknow.rewrite;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.parser.ParseException;
import name.levering.ryan.sparql.parser.SPARQLParser;
import name.levering.ryan.sparql.parser.model.Node;

import org.xmedia.oms.metaknow.ComplexProvenance;
import org.xmedia.oms.metaknow.Provenance;
import org.xmedia.oms.model.impl.NamedIndividual;
/**
 * @author bernie_2
 * 
 */
public class Test {

	static String sQueryOptFilter = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/> PREFIX  ns:  <http://example.org/ns#> SELECT  ?title ?price WHERE   { ?x dc:title ?title . OPTIONAL { ?x ns:price ?price . FILTER (?price < 30) }}";
	static String sQueryOptFilter2 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/> PREFIX  ns:  <http://example.org/ns#> SELECT  ?title ?price WHERE   { {?x dc:title ?title} OPTIONAL { ?x ns:price ?price} FILTER (?title = \"asdf\") }";
	static String sAND_query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/> PREFIX  ns:  <http://example.org/ns#> SELECT  ?title ?price WHERE   { ?x dc:title ?title . { ?x ns:price ?price}}";
	static String sQueryANDDot = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/> PREFIX  ns:  <http://example.org/ns#> SELECT  ?title ?price WHERE   { ?x dc:title ?title . ?x ns:price ?price}";
	static String sQueryUNION = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/> PREFIX  ns:  <http://example.org/ns#> SELECT  ?title ?price WHERE   { {?x dc:title ?title} UNION {?x ns:price ?price} }";
	static String sQueryGraph = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/> PREFIX  ns:  <http://example.org/ns#> SELECT  ?title ?price WHERE   {Graph ?gname  {?x dc:title ?title}}";
	// static String sQueryAugmented = "SELECT ?title ?price WHERE{ { GRAPH ?G0 { ?x
	// <http://purl.org/dc/elements/1.1/title> ?title } } { { GRAPH ?G1 { ?x
	// <http://example.org/ns#price> ?price } } } }";

	static String[] FiatQueries =
					{
							"SELECT * WHERE  {?x <http://example.org/Fiat/maxSpeed> ?y}",
							"SELECT  ?x ?y WHERE  {?x <http://example.org/Fiat/maxSpeed> ?y}",
							"SELECT  ?x ?y ?z WHERE  {{?x <http://example.org/Fiat/maxSpeed> ?y} "
											+ "{?x <http://example.org/Fiat/numberOfSeats> ?z} FILTER (?z<5)}",
							"SELECT  ?x ?y ?z WHERE  {{?x <http://example.org/Fiat/maxSpeed> ?y} "
											+ "OPTIONAL {?x <http://example.org/Fiat/hasSafetyRating> ?z}}",
							"SELECT  ?x ?y ?z WHERE  {{?x <http://example.org/Fiat/maxSpeed> ?y} "
											+ "OPTIONAL {?x <http://example.org/Fiat/hasSafetyRating> ?z}" + "FILTER (?y > 160)}",
							"SELECT ?x ?y ?z WHERE { ?x <http://example.org/Fiat/maxSpeed> ?y . ?x <http://example.org/Fiat/hasSafetyRating> ?z . FILTER (?z < 6) }"};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test.runQuery(FiatQueries[0]);
		// Test.runQuery(sQueryOptFilter2);

	}

	static public void runQuery(String sQuery) {
		Query query = null;
		try {
			//import statements above rely on sparql-0.8
			query = SPARQLParser.parse(new StringReader(sQuery));
			System.out.println(query.toString());
			// System.out.println(((ASTSelectQuery) query).toQueryString());//macht keinen
			// Unterschied
			// in ASTQuotedIRIref 'toString()' gets called and not 'toQueryString()' :(
			System.out.println("-----formatted for input:-----------------");
			System.out.println(QueryFormatter.format(query.toString()));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// show trace of parse Tree; depth-first, left to right traversal
		// System.out.println("\n---TRACE:----------------------");
		// TraceVisitor tv = new TraceVisitor();
		// ((Node) query).jjtAccept(tv);

		// create provenance expression
		System.out.println("\n---Provenance:----------------------");
		ProvenanceVisitor pv = new ProvenanceVisitor();
		((Node) query).jjtAccept(pv);
		NodeProvenance np = pv.getProvenance();
		System.out.println(np.provString);
		System.out.println(np.provFormula);

		// create augmented query
		System.out.println("\n---AUGMENTED QUERY:-----------------");
		AugmentVisitor av = new AugmentVisitor();
		((Node) query).jjtAccept(av);
		String formatedAugQueryStr = QueryFormatter.format(query.toString());
		System.out.println(formatedAugQueryStr);
		Query augQuery = null;
		try {
			augQuery = SPARQLParser.parse(new StringReader(formatedAugQueryStr));
			System.out.println(augQuery.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// create augmented query 
		System.out.println("\n---AUGMENTED QUERY (via AugmentVisitor.augmentQuery(...)):-----------------");
		AugmentVisitor av2 = new AugmentVisitor();
		System.out.println(av2.augmentQuery(sQuery));

		// provenance:
		System.out
					.println("\n---EVALUATE PROVENANCE FOR SINGLE BINDING:-----------------");
		System.out.println(np.provFormula);
		// Fake provenance for a single binding for ?G1 and ?G2:
		HashSet<String> sources = new HashSet<String>();
		sources.add("d1.doc");
		ComplexProvenance g1pc = new ComplexProvenance(0.6, new NamedIndividual("Smith"),
					new NamedIndividual("d1.doc"), new Date());// Calendar.set(year + 1900, month,
																// date)
		ComplexProvenance g2pc = new ComplexProvenance(0.3, new NamedIndividual("Miller"),
					new NamedIndividual("d2.doc"), new Date());
		HashMap<String, Provenance> graphProvenances   = new HashMap<String, Provenance>();
		graphProvenances.put("?G0", g1pc);
		graphProvenances.put("?G1", g2pc);
		// evaluate provenance:
		ComplexProvenance prov = np.provFormula.getProvenance(graphProvenances);
	}
}
