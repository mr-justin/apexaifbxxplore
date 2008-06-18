package org.xmedia.oms.adapter.kaon2.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import name.levering.ryan.sparql.parser.model.ASTGraph;
import name.levering.ryan.sparql.parser.model.ASTGraphConstraint;
import name.levering.ryan.sparql.parser.model.ASTGroupConstraint;
import name.levering.ryan.sparql.parser.model.ASTNamedGraph;
import name.levering.ryan.sparql.parser.model.ASTQName;
import name.levering.ryan.sparql.parser.model.ASTSelectQuery;
import name.levering.ryan.sparql.parser.model.ASTVar;
import name.levering.ryan.sparql.parser.model.Node;
import name.levering.ryan.sparql.parser.model.SimpleNode;
import name.levering.ryan.sparql.parser.model.SimpleVisitor;

import org.xmedia.oms.metaknow.rewrite.QueryFormatter;
import org.xmedia.oms.query.IQueryWrapper;
import org.xmedia.oms.query.QueryWrapper;

public class MetaknowSplitVisitor extends SimpleVisitor {

	private String m_baseQuery = null;
	private List<String> m_baseVariables = new ArrayList<String>();
	private String m_baseOnto = null;
	private String m_metaQuery = null;
	private List<String> m_metaVariables = new ArrayList<String>();
	private String m_metaOnto = null;
	private boolean m_inBase = false, m_inMeta = false;
	private Set<String> m_tempVars = new HashSet<String>();


	@Override
	public void visit(SimpleNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ASTNamedGraph node) {
//		System.out.println("ASTNamedGraph " + node);
	}

	public void visit(ASTGraph node) {
//		System.out.println("ASTGraph " + node);
//		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//		System.out.println(" " + node.jjtGetChild(i));
//		}
//		System.out.println(" parent: " + node.jjtGetParent() + ", " + node.jjtGetParent().getClass());
//		for (int i = 0; i < node.jjtGetParent().jjtGetNumChildren(); i++) {
//		System.out.println(" " + node.jjtGetParent().jjtGetChild(i));
//		}
	}

	public void visit(ASTVar node) {
		if(!(node.jjtGetParent() instanceof ASTSelectQuery))
			m_tempVars.add(node.toString());
	}

	public void visit(ASTGraphConstraint node) {
//		System.out.println("ASTGraphConstraint: " + node + "<<<<<");
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Node child = node.jjtGetChild(i);
//			System.out.println(node.jjtGetChild(i).getClass() + ": " + node.jjtGetChild(i));
//			System.out.println(node.jjtGetChild(i).getClass());
			if (child instanceof ASTGraph){
				Object graph  = ((ASTGraph)child).jjtGetChild(0);
				if(graph instanceof ASTQName){
					if (m_baseOnto == null){

						m_baseOnto = ((ASTQName)graph).toString();
						if(m_baseOnto.lastIndexOf("#") == m_baseOnto.length()-1)
							m_baseOnto = m_baseOnto.substring(0, m_baseOnto.lastIndexOf("#"));					
					}
					else {
						m_metaOnto = ((ASTQName)graph).toString();
						if(m_metaOnto.lastIndexOf("#") == m_metaOnto.length()-1)
							m_metaOnto = m_metaOnto.substring(0, m_metaOnto.lastIndexOf("#"));
					}
				}

			}


			if (child instanceof ASTGroupConstraint) {
				if (m_baseQuery == null) {
					m_baseQuery = "{";
					int n = child.jjtGetNumChildren();
					for (int j = 0; j < n; j++){
						String triple  = child.jjtGetChild(j).toString();
						m_baseQuery+= triple + " .\n";
					}
					m_baseQuery+="}";
					m_baseVariables.addAll(m_tempVars);
					m_tempVars.clear();
//					System.out.println("baseQuery: " + m_baseQuery);
//					System.out.println("bv: " + m_baseVariables);
				}
				else {
					m_metaQuery = "{";
					int n = child.jjtGetNumChildren();
					for (int j = 0; j < n; j++){
						String triple  = child.jjtGetChild(j).toString();
						m_metaQuery+= triple + " .\n";
					}
					m_metaQuery+="}";
					m_metaVariables.addAll(m_tempVars);
					m_tempVars.clear();
//					System.out.println("metaQuery: " + m_metaQuery);
//					System.out.println("mv: " + m_metaVariables);
				}
			}
		}
//		System.out.println("");
	}

	public void visit(ASTGroupConstraint node) {
	}


	private IQueryWrapper constructQueryWrapper(List<String> vars, String whereClause, String ontoURI) {
		String query = "SELECT ";

		for (String v : vars)
			query += v + " ";

		QueryWrapper wrapper = new QueryWrapper(QueryFormatter.format(query + "WHERE " + whereClause), vars.toArray(new String [] {}));
		wrapper.setOntology(ontoURI);
		return wrapper;
	}

	public IQueryWrapper getMetaQuery() {
		if (m_metaQuery == null) return null;
		return constructQueryWrapper(m_metaVariables, m_metaQuery, m_metaOnto);
	}

	public IQueryWrapper getBaseQuery() {
		return constructQueryWrapper(m_baseVariables, m_baseQuery, m_baseOnto);
	}
}
