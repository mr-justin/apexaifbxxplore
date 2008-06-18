package org.aifb.xxplore.core.service.labelizer;

import org.aifb.xxplore.core.service.labelizer.IndexNodes.CNode;
import org.aifb.xxplore.core.service.labelizer.IndexNodes.KNode;
//import org.aifb.xxplore.core.service.query.QueryInterpretationServiceExtent.KbEdge;
import org.aifb.xxplore.shared.exception.Emergency;
import org.jgrapht.graph.DefaultEdge;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;

public class IndexEdges {

	public class REdge extends DefaultEdge {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5584485745673658957L;

		private INamedConcept m_vertex1;

		private INamedConcept m_vertex2;

		private IProperty m_property;
		
		private int m_costValue;
		
		public REdge(INamedConcept vertex1, INamedConcept vertex2, IProperty prop, int costValue){
			m_vertex1 = vertex1;
			m_vertex2 = vertex2;
			m_property = prop;
			m_costValue = costValue;
			Emergency.checkPostcondition(m_vertex1 != null && m_vertex2 != null && m_property != null, "m_vertex1 != null && m_vertex2 != null && m_property != null"); 
		}

		public void setVertex1(INamedConcept vertex1){
			m_vertex1 = vertex1;
		}

		public void setVertex2(INamedConcept vertex2){
			m_vertex2 = vertex2;
		}

		public void setProperty(IProperty propertyName){
			m_property = propertyName;
		}
		
		public void setCostValue(int costValue) {
			m_costValue = costValue;
		}

		public INamedConcept getVertex1(){
			return m_vertex1;
		}

		public INamedConcept getVertex2(){
			return m_vertex2;
		}

		public IProperty getProperty(){
			return m_property;
		}
		
		public int getCostValue() {
			return m_costValue;
		}

//		public boolean equals(KbEdge edge){
//
//			if (!m_property.equals(edge.getProperty()))  return false;
//			if (!m_vertex1.equals(edge.getVertex1())) return false;
//			if (!m_vertex2.equals(edge.getVertex2())) return false;
//			return true;
//		}

		public String toString(){
			if(m_vertex1 != null && m_vertex2 != null && m_property != null) return m_vertex1.toString() + " " + m_property + " "  + m_vertex2.toString() + " (" + m_costValue;
			else return super.toString();
		}
	}
	
	public class AEdge extends DefaultEdge {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8910612443622111731L;

		private CNode m_vertex1;

		private KNode m_vertex2;

		private IProperty m_property;


		public AEdge(CNode vertex1, KNode vertex2, IProperty prop){
			m_vertex1 = vertex1;
			m_vertex2 = vertex2;
			m_property = prop;
			Emergency.checkPostcondition(m_vertex1 != null && m_vertex2 != null && m_property != null, "m_vertex1 != null && m_vertex2 != null && m_property != null"); 
		}

		public void setVertex1(CNode vertex1){
			m_vertex1 = vertex1;
		}

		public void setVertex2(KNode vertex2){
			m_vertex2 = vertex2;
		}

		public void setProperty(IProperty propertyName){
			m_property = propertyName;
		}


		public CNode getVertex1(){
			return m_vertex1;
		}

		public KNode getVertex2(){
			return m_vertex2;
		}

		public IProperty getProperty(){
			return m_property;
		}

//		public boolean equals(KbEdge edge){
//
//			if (!m_property.equals(edge.getProperty()))  return false;
//			if (!m_vertex1.equals(edge.getVertex1())) return false;
//			if (!m_vertex2.equals(edge.getVertex2())) return false;
//			return true;
//		}

		public String toString(){
			if(m_vertex1 != null && m_vertex2 != null && m_property != null) return m_vertex1.toString() + " " + m_property + " "  + m_vertex2.toString();
			else return super.toString();
		}
		
	}
}
