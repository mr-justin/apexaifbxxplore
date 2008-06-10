/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import name.levering.ryan.sparql.parser.model.ASTSelectQuery;

/**
 * @author bernie_2
 * 
 */

public class QueryFormatter {

	/**
	 * Calling any of 'AST...Query.toString()' and 'AST...Query.toQueryString()' will cause
	 * 'toString()' being called for objects of type ASTQuotedIRIref 'toString()' and not
	 * 'toQueryString()' :(
	 * 
	 * @param sparql
	 * @return
	 */
	public static String addURIBrackets(String sparql) {
		String[] tokens = sparql.split(" ");
		String result = "";
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].indexOf("://") >= 0) {
				// really bad hack
				tokens[i] = "<" + tokens[i] + ">";
			}
		}
		for (String s : Arrays.asList(tokens)) {
			result += s + " ";
		}
		return result;
	}

	public static String format(String sparql) {
		String[] tokens = sparql.split("\n");
		String s = "";
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].indexOf("PREFIX") == 0) {
				;
			} else {
				s += tokens[i] + " ";// + "\n";
			}
		}
		tokens = s.split(" ");
		s = "";
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].indexOf("://") >= 0) {
				// really bad hack
				s += "<" + tokens[i] + ">" + " ";
			} else {
				s += tokens[i] + " ";
			}
		}
		return s;
	}

	/**
	 * Extracted from class 'ASTSelectQuery'. Could serve as starting point to implement own string
	 * output. Maybe later :)
	 * 
	 * @param sq
	 * @return
	 */
	public static String toQueryString(ASTSelectQuery sq) {
		// StringBuffer output = new StringBuffer(super.toString() + "\n");
		StringBuffer output = new StringBuffer();
		// output.append((QueryNode)sq).toString());
		// output.append("\n");
		output.append("SELECT ");

		if (sq.getDistinct()) {
			output.append("DISTINCT ");
		}

		List selectVars = sq.getQueryVariables();
		if (!selectVars.isEmpty()) {
			for (Iterator i = selectVars.iterator(); i.hasNext();) {
				output.append(i.next());
				if (i.hasNext()) {
					output.append(" ");
				}
			}
		} else {
			output.append("*");
		}
		output.append('\n');

		Collection defaults = sq.getDefaultDatasets();
		for (Iterator i = defaults.iterator(); i.hasNext();) {
			output.append("FROM ");
			output.append(i.next());
			output.append("\n");
		}
		Collection named = sq.getNamedDatasets();
		for (Iterator i = named.iterator(); i.hasNext();) {
			output.append("FROM NAMED ");
			output.append(i.next());
			output.append("\n");
		}

		output.append("WHERE\n");
		output.append(sq.getConstraint());
		output.append('\n');

		Collection orderExpressions = sq.getOrderExpressions();
		if (!orderExpressions.isEmpty()) {
			output.append("ORDER BY ");
			for (Iterator i = orderExpressions.iterator(); i.hasNext();) {
				output.append(i.next());
				if (i.hasNext()) {
					output.append(" ");
				}
			}
			output.append('\n');
		}

		if (sq.getLimit() >= 0) {
			output.append("LIMIT ");
			output.append(sq.getLimit());
			output.append('\n');
		}

		if (sq.getOffset() >= 0) {
			output.append("OFFSET ");
			output.append(sq.getOffset());
			output.append('\n');
		}

		return output.toString();
	}

}
