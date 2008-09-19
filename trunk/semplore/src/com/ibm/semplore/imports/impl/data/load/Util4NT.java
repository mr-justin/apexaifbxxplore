/**
 * 
 */
package com.ibm.semplore.imports.impl.data.load;

import com.ibm.semplore.util.TestUnicode;

/**
 * @author lql
 * 
 */
public class Util4NT {
	public static final String RELATION = "3r:";

	public static final String INVRELATION = "5i:";

	public static final String CATEGORY = "2c:";

	public static final String ATTRIBUTE = "4a:";

	public static final String INSTANCE = "1i:";

	protected static String ns_cat = "<";

	protected static String ns_rel = "<";

	protected static String ns_ins = "<";

	protected static String ns_att = "";

	protected static String ns_blanknode = "_:"; // e.g. _:node1356lc5c7x26

	public static String TYPE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

	public static String TOP = "<TOP_Category>";

	public static String DOMAIN = "<DOMAIN_Category>";

	public static String SHALLOW = "<SHALLOW_Category>";

	public static String[] line2Triple(String line) {
		// check nt gramma
		String[] triple = line.replaceAll("\t", " ").split(" ");
		int i = 3;
		while (triple.length > i) {
			if (triple[i].equals("."))
				break;
			triple[2] = triple[2] + " " + triple[i];
			i++;
		}
		if (triple.length < 3)
			return null;
		line = triple[0] + "\t" + triple[1] + "\t" + triple[2] + "\t.";
		triple = line.split("\t");
		// check nt gramma

		return triple;
	}

	public static String checkTripleType(String[] triple) {
		if (triple == null || triple.length < 3 || triple[0] == null
				|| triple[1] == null || triple[2] == null)// not 3 string args
			return null;
		if (triple[0].indexOf(ns_ins) != 0
				&& triple[0].indexOf(ns_blanknode) != 0)// arg1 is not ins
			return null;
		if (triple[1].equals(TYPE) && triple[2].indexOf(ns_cat) == 0)
			// valid category
			return Util4NT.CATEGORY;
		if (triple[1].indexOf(ns_rel) == 0
				&& (triple[2].indexOf(ns_ins) == 0 || triple[2]
						.indexOf(ns_blanknode) == 0))
			// valid relation
			return Util4NT.RELATION;
		if (triple[1].indexOf(ns_att) == 0)// valid attribute
			return Util4NT.ATTRIBUTE;
		return null;
	}

	public static void setTYPE(String TYPE) {
		if (TYPE != null)
			Util4NT.TYPE = TYPE;
	}

	public static void setNameSpace(String ns_cat, String ns_rel,
			String ns_ins, String ns_att) {
		Util4NT.ns_cat = ns_cat;
		Util4NT.ns_rel = ns_rel;
		Util4NT.ns_ins = ns_ins;
		Util4NT.ns_att = ns_att;
	}

	public static String[] processTripleLine(String line) {
		// Split..java
		String[] triple = line.replaceAll("\t", " ").split(" ");
		int i = 3;
		while (triple.length > i) {
			if (triple[i].equals(".")) // TODO sxr:what if attribute contains a
										// " . " in the middle?
				break;
			triple[2] = triple[2] + " " + triple[i];
			i++;
		}
		if (triple.length < 3)
			return null;
		line = triple[0] + "\t" + triple[1] + "\t" + triple[2] + "\t.";
		triple = line.split("\t");

		String tripletype = Util4NT.checkTripleType(triple);
		// InstanceDocumentIter..java
		if (tripletype == Util4NT.ATTRIBUTE) {
			triple[2] = TestUnicode.parse(triple[2]);

			// InstanceDocument..java
			// ignore language, e.g. "A sense of an adjective word."@en-us --> A
			// sense of an adjective word.
			if (triple[2].matches(".*\".*\"@.*")) {
				triple[2] = triple[2].substring(triple[2].indexOf("\"") + 1,
						triple[2].lastIndexOf("@") - 1);
			}

			// ignore datatype, e.g.
			// "1"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger> --> 1
			if (triple[2].matches(".*\".*\"\\^\\^<.*")) {
				triple[2] = triple[2].substring(triple[2].indexOf("\"") + 1,
						triple[2].lastIndexOf("^^") - 1);
			}

			// ignore "", e.g. "1" --> 1
			if (triple[2].matches("\".*\"")) {
				triple[2] = triple[2].substring(1, triple[2].length() - 1);
			}
		}
		return triple;
	}

}
