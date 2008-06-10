/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;

import java.io.StringReader;
import java.util.ArrayList;

import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.parser.ParseException;
import name.levering.ryan.sparql.parser.SPARQLParser;
import name.levering.ryan.sparql.parser.model.ASTGraph;
import name.levering.ryan.sparql.parser.model.ASTGraphConstraint;
import name.levering.ryan.sparql.parser.model.ASTGroupConstraint;
import name.levering.ryan.sparql.parser.model.ASTSelectQuery;
import name.levering.ryan.sparql.parser.model.ASTTripleSet;
import name.levering.ryan.sparql.parser.model.ASTVar;
import name.levering.ryan.sparql.parser.model.Node;
import name.levering.ryan.sparql.parser.model.SPARQLParserTreeConstants;
import name.levering.ryan.sparql.parser.model.SimpleNode;
import name.levering.ryan.sparql.parser.model.SimpleVisitor;

/**
 * @author bernie_2
 * 
 */
public class AugmentVisitor extends SimpleVisitor {

	private Integer varID = 0;

	private String getVarID() {
		String s = "G" + this.varID.toString();
		this.varID++;
		return s;
	}
	public String[] getUsedVars() {
		String[] addedVars = new String[this.varID];
		for (int i = 0; i < this.varID; i++) {
			addedVars[i] = "G" + i;
		}
		return addedVars;
	}

	public String augmentQuery(String sparql) {
		String s = null;
		try {
			Query query = SPARQLParser.parse(new StringReader(sparql));
			((Node) query).jjtAccept(this);
			s = QueryFormatter.format(query.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return s;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see name.levering.ryan.sparql.parser.model.SimpleVisitor#visit(name.levering.ryan.sparql.parser.model.SimpleNode)
	 */
	@Override
	public void visit(SimpleNode node) {
		;
	}

	public void visit(ASTTripleSet node) {
		Node parent = node.jjtGetParent();
		// get position of node in parent
		int pos = 0;
		for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
			if (parent.jjtGetChild(i).equals(node))
				pos = i;
		}
		// create replacement for node
		ASTGroupConstraint groupConstr = new ASTGroupConstraint(
					SPARQLParserTreeConstants.JJTGROUPCONSTRAINT);
		ASTGraphConstraint graphConstr = new ASTGraphConstraint(
					SPARQLParserTreeConstants.JJTGRAPHCONSTRAINT);
		ASTVar var = new ASTVar(SPARQLParserTreeConstants.JJTVAR);
		var.setName(this.getVarID());
		ASTGraph graph = new ASTGraph(SPARQLParserTreeConstants.JJTGRAPH);
		ASTGroupConstraint groupConstr2 = new ASTGroupConstraint(
					SPARQLParserTreeConstants.JJTGROUPCONSTRAINT);
		groupConstr.jjtAddChild(graphConstr, 0);
		graphConstr.jjtAddChild(graph, 0);
		graphConstr.jjtAddChild(groupConstr2, 1);
		graph.jjtAddChild(var, 0);
		groupConstr2.jjtAddChild(node, 0);
		// replace node
		parent.jjtAddChild(groupConstr, pos);
	}

	public void visit(ASTSelectQuery node) {
		if (!(node.jjtGetChild(0) instanceof ASTVar)) {
			//SELECT * ...
			return;
		}
		//insert variables, if there are already some -- else "*" has been specified
		ArrayList<Node> nodeList = new ArrayList<Node>();
		String[] usedVars = this.getUsedVars();
		for (String s : usedVars) {
			// create new ASTVar nodes
			ASTVar var = new ASTVar(SPARQLParserTreeConstants.JJTVAR);
			var.setName(s);
			nodeList.add(var);
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			nodeList.add(node.jjtGetChild(i));
		}
		for (int i = 0; i < nodeList.size(); i++) {
			node.jjtAddChild(nodeList.get(i), i);
		}

	}

}
