/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: Edge.java,v 1.2 2008/09/02 16:43:17 xrsun Exp $
 */
package com.ibm.semplore.model;


/**
 * @author lql
 *
 */
public class Edge {
    int fromNode;
    int toNode;
    Relation rel;
    
    public Edge(int fromNode, int toNode, Relation rel) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.rel = rel;
    }
    
    public Relation getRelation() {
        return rel;
    }
    
    public int getFromNode() {
        return fromNode;
    }
    
    public int getToNode() {
        return toNode;
    }
        
    public int hashCode() {
        return toString().hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Edge) {
            Edge ed2 = (Edge)obj;
            if (ed2.fromNode == this.fromNode && ed2.toNode == this.toNode) {
            	if (ed2.rel == null) return this.rel == null;
            	else if (this.rel == null) return false;
            	else return ed2.rel.equals(this.rel);
            }
            else
                return false;
        } else 
            return false;
    }
    
    public String toString() {
    	if (rel != null)
    		return "("+fromNode+"->"+toNode+"#"+rel.toString()+")";
    	return "("+fromNode+"<=>"+toNode+")";
    }
        
    public Edge reverse() {
    	return new Edge(toNode, fromNode, rel);
    }
}
