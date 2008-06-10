/**
 * 
 */
package org.xmedia.oms.metaknow.rewrite;

import java.util.HashMap;
import java.util.Map;

import name.levering.ryan.sparql.parser.model.ASTFilterConstraint;
import name.levering.ryan.sparql.parser.model.ASTGraphConstraint;
import name.levering.ryan.sparql.parser.model.ASTGroupConstraint;
import name.levering.ryan.sparql.parser.model.ASTOptionalConstraint;
import name.levering.ryan.sparql.parser.model.ASTTripleSet;
import name.levering.ryan.sparql.parser.model.ASTUnionConstraint;
import name.levering.ryan.sparql.parser.model.Node;
import name.levering.ryan.sparql.parser.model.QueryNode;
import name.levering.ryan.sparql.parser.model.SimpleNode;
import name.levering.ryan.sparql.parser.model.SimpleVisitor;

/**
 * @author bernie_2
 * 
 * jjAccept() in class SimpleNode always start traversal of subtree :(
 */
/**
 * @author bernie_2
 * 
 */
/**
 * @author bernie_2
 * 
 */
public class ProvenanceVisitor extends SimpleVisitor {

	private Integer varID = 0;
	private Map<SimpleNode, NodeProvenance> visitedNodes = new HashMap<SimpleNode, NodeProvenance>();
	private NodeProvenance rootProvenance;

	private Integer getVarIDInteger() {
		varID++;
		return varID - 1;
	}

	private String getVarID() {
		String s = "?G" + this.varID.toString();
		this.varID++;
		return s;
	}

	public NodeProvenance getProvenance(SimpleNode node) {
		return this.visitedNodes.get(node);
	}

	public NodeProvenance getProvenance() {
		return rootProvenance;
	}

	@Override
	public void visit(SimpleNode node) {
		;
	}

	public void visit(ASTGroupConstraint node) {
		String parseTree = "";
		String provString = "";
		String tmp;
		Formula f = null;
		Conjunction conj = null;
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Node child = node.jjtGetChild(i);
			// constructing provString and provFormula
			if (child instanceof ASTOptionalConstraint) {
				// (a^b)v(a^!b), 
				//!! assume one OPT clause occuring after mandatory patterns in
				// the same GroupConstr for now
				tmp = "(" + provString + "^" + this.visitedNodes.get(child).provString
							+ ")v";
				tmp += "(" + provString + "^!" + this.visitedNodes.get(child).provString
							+ ")";
				provString = tmp;
				// provFormula:
				Formula b = this.visitedNodes.get(child).provFormula;
				Conjunction conj1 = new Conjunction(f, b);
				Negation nb = new Negation(b);
				Conjunction conj2 = new Conjunction(f, nb);
				f  = new Disjunction(conj1,conj2);
			} else {
				if (i == 0) {
					provString = this.visitedNodes.get(child).provString;
					f = this.visitedNodes.get(child).provFormula;
				}
				if (i > 0 && this.visitedNodes.get(child).provString.length() > 0) {
					// except e.g. FILTER, which has provString == ""
					provString += "^" + this.visitedNodes.get(child).provString;
					// provFormula, left bracketing
					conj = new Conjunction(f,this.visitedNodes.get(child).provFormula);
					f = conj;
				}
			}
			// constructing parseTree
			if (i > 0)
				parseTree += "^";
			parseTree += this.visitedNodes.get(child).parseTree;
		}
		parseTree = "(" + parseTree + ")";
		provString = "(" + provString + ")";
		NodeProvenance np = new NodeProvenance(parseTree, provString, node, f);
		this.visitedNodes.put(node, np);
		if (node.jjtGetParent() instanceof QueryNode) {
			this.rootProvenance = np;
		}
	}

	public void visit(ASTFilterConstraint node) {
		// NodeProvenance np = new NodeProvenance("FILTER", "FILT", node);
		// We don't care about bindings which are not going to be in the result
		// for now.
		NodeProvenance np = new NodeProvenance("FILTER", "", node, null);
		this.visitedNodes.put(node, np);
	}

	public void visit(ASTOptionalConstraint node) {
		NodeProvenance childNp = this.visitedNodes.get(node.jjtGetChild(0));
		NodeProvenance np = new NodeProvenance("(OPT " + childNp.parseTree + ")",
					childNp.provString, node, childNp.provFormula);
		this.visitedNodes.put(node, np);
	}

	public void visit(ASTUnionConstraint node) {
		NodeProvenance child0Np = this.visitedNodes.get(node.jjtGetChild(0));
		NodeProvenance child1Np = this.visitedNodes.get(node.jjtGetChild(1));
		Disjunction disj = new Disjunction(child0Np.provFormula, child1Np.provFormula);
		NodeProvenance np = new NodeProvenance("(" + child0Np.parseTree + " UNION "
					+ child1Np.parseTree + ")", "(" + child0Np.provString + " v "
					+ child1Np.provString + ")", node, disj);
		this.visitedNodes.put(node, np);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see name.levering.ryan.sparql.parser.model.SimpleVisitor#visit(name.levering.ryan.sparql.parser.model.ASTGraphConstraint)
	 */
	/**
	 * Graph variable has been created at TripleSet level. Class 'AugmentVisitor' independently
	 * inserts the same graph variables into SPARQL.
	 */
	public void visit(ASTGraphConstraint node) {
		NodeProvenance groupConstrNp = this.visitedNodes.get(node.jjtGetChild(1));
		NodeProvenance np = new NodeProvenance(groupConstrNp.parseTree,
					groupConstrNp.provString, node, groupConstrNp.provFormula);
		this.visitedNodes.put(node, np);
	}

	public void visit(ASTTripleSet node) {
		String var = this.getVarID();
		// Open issue: If the query was already augmented using AugmentVisitor then the
		// corresponding graph variable should be used
		Leaf leaf = new Leaf(var);
		NodeProvenance np = new NodeProvenance(var, var, node, leaf);
		this.visitedNodes.put(node, np);
	}
}
