package org.xmedia.oms.query;


public class QueryWrapper implements IQueryWrapper {
	
	private String m_query;
	
	private String[] m_vars;
	
	private String m_ontologyURI;
	
	public QueryWrapper(String query, String[] variables){
		m_query = query;
		m_vars = variables;
	}
	
	public QueryWrapper(String query){
		m_query = query;
	}
	
	public String getQuery() {
		
		return m_query;
	}

	public void setQuery(String query) {
		
		m_query = query;
	}

	public void setSelectVariables(String[] vars) {
		
		m_vars = vars;
	}
	
	public void setOntology(String ontoURI){
		m_ontologyURI = ontoURI;
	}
	
	public String getOntologyURI(){
		return m_ontologyURI;
	}
	
	public String[] getSelectVariables() {
		
		return m_vars; 
	}
	
	public boolean equals(Object otherquerywrapper){
		
		try{
		
		if(this.getQuery().equals(((QueryWrapper)otherquerywrapper).getQuery())){
			return true;
		}
		
		return false;
		}catch(ClassCastException e){
			return false;
		}
				
	}
	
}
