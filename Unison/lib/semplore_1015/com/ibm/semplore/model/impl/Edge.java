/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: Edge.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Relation;

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
    
//    public boolean equals(Object obj) {
//        if (obj instanceof Edge) {
//            Edge ed2 = (Edge)obj;
//            if (ed2.fromNode == this.fromNode && ed2.toNode == this.toNode && ed2.rel.equals(this.rel))
//                return true;
//            else
//                return false;
//        } else 
//            return false;
//    }
    
    public String toString() {
        return "("+fromNode+"->"+toNode+"#"+rel.toString()+")";
    }
        
}
