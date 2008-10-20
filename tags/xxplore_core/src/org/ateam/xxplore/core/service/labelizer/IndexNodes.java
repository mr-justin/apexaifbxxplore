package org.ateam.xxplore.core.service.labelizer;

import org.xmedia.oms.model.api.INamedConcept;

public class IndexNodes {
	
	public class IndexGraphNode {
		
		private int m_costValue;
		
		public IndexGraphNode(int costValue) {
			m_costValue = costValue;
		}
		
		public int getCostValue() {
			return m_costValue;
		}
		
		public void setCostValue(int costValue) {
			m_costValue = costValue;
		}
	}
	
	public class CNode extends IndexGraphNode {
		
		private INamedConcept _conceptLabel;
		
		public CNode (INamedConcept conceptLabel, int costValue) {
			super(costValue);
			_conceptLabel = conceptLabel;
		}
		
		public INamedConcept getConceptLabel() {
			return _conceptLabel;
		}
		
		public void setConceptLabel(INamedConcept conceptLabel) {
			_conceptLabel = conceptLabel;
		}
	}
	
	public class KNode extends IndexGraphNode {
		
		private String _literal;
		
		public KNode (String literal, int costValue) {
			super(costValue);
			_literal = literal;
		}
		
		public String getLiteral() {
			return _literal;
		}
		
		public void setLiteral(String literal) {
			_literal = literal;
		}
	}
}
