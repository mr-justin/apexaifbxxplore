/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CatRelGraphImpl.java,v 1.3 2008/09/02 06:14:02 lql Exp $
 */
package com.ibm.semplore.model.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Relation;

/**
 * @author lql
 *
 */
public class CatRelGraphImpl implements CatRelGraph
{
    public Hashtable<Integer,HashSet<Edge>> edgeHash = new Hashtable<Integer,HashSet<Edge>>();
    public ArrayList<GeneralCategory> catArray = new ArrayList<GeneralCategory>();
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CatRelGraph#add(com.ibm.semplore.model.GeneralCategory)
     */
    public CatRelGraph add(GeneralCategory cat)
    {
        catArray.add(cat);
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CatRelGraph#add(com.ibm.semplore.model.Relation, int, com.ibm.semplore.model.GeneralCategory)
     */
    public CatRelGraph add(Relation rel, int fromNodeIndex, int toNodeIndex)
    {
        Edge ed = new Edge(fromNodeIndex,toNodeIndex,rel);
        HashSet<Edge> set = edgeHash.get(fromNodeIndex);
        if (set==null) {
            set = new HashSet<Edge>();
            edgeHash.put(fromNodeIndex, set);
        }
        set.add(ed);
        
        set = edgeHash.get(toNodeIndex);
        if (set==null) {
            set = new HashSet<Edge>();
            edgeHash.put(toNodeIndex, set);            
        }
        set.add(ed);
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CatRelGraph#getEdges(int)
     */
    public Iterator<Edge> getEdges(int nodeIndex)
    {
        if (edgeHash.get(nodeIndex) == null)
            return new HashSet<Edge>().iterator();
        return edgeHash.get(nodeIndex).iterator();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CatRelGraph#getNode(int)
     */
    public GeneralCategory getNode(int nodeIndex)
    {
        return (GeneralCategory)catArray.get(nodeIndex);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CatRelGraph#numOfNodes()
     */
    public int numOfNodes()
    {
        return catArray.size();
    }

    public String toString() {
        StringBuffer res = new StringBuffer("\n");
        for (int i=0; i<catArray.size(); i++) {
            GeneralCategory cat = catArray.get(i);
            res.append(String.format("Node %d: %s\n", i, cat.toString()));
        }
        for (int i=0; i<catArray.size(); i++) {
            Iterator it = getEdges(i);
            while (it.hasNext()) {
                Edge ed = (Edge)it.next();
                if (Math.min(ed.getFromNode(), ed.getToNode())<i) continue;
                res.append(String.format("Edge %d->%d: %s\n", ed.getFromNode(), ed.getToNode(), ed.toString()));
            }
        }
        return res.toString();
    }

	@Override
	public int removeRelation(Relation rel, int nodeIndex) {
		HashSet<Edge> rels = this.edgeHash.get(nodeIndex);
		Edge edge = rels.iterator().next();//only one edge
		int nodeIndex2;
		if (edge.getFromNode()==nodeIndex) 
			nodeIndex2 = edge.getToNode();
		else  
			nodeIndex2 = edge.getFromNode();		
		rels.remove(edge);
		this.edgeHash.get(nodeIndex2).remove(edge);
		// TODO remove the node of nodeIndex
		return nodeIndex2;
	}
}
