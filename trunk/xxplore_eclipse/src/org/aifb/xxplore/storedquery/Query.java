package org.aifb.xxplore.storedquery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

public class Query implements IQuery{
	private static final long serialVersionUID = 3545518391537382197L;
	
	private String m_name;
    private String m_description;
    private String m_notes;
	private Stack<Prefix> m_prefixes = new Stack<Prefix>();
	private Stack<String[]> m_predicates = new Stack<String[]>();
	private Set<String> m_variables = new HashSet<String>();
	private Set<String> m_selectedVars = new HashSet<String>();
	private boolean m_requireProvenances = false;
	 
	private String m_uri;
	private Date endDate = null;
	private Date creationDate = null;
	
	private IQueryMetaFilter m_meta;

    public Query() {
    	m_meta = new QueryMetaFilter();
    } 
    
    public Query(String uri) {
    	this();
    	m_uri = uri;
    }
    
    public void setName(String name) {
		m_name = name;
	}
    
    public String getName() {
    	if(m_name == null)
    		m_name = "New Query";
		return m_name;
	}
    
	public String getNotes() {
		if (m_notes == null)
			m_notes = "";
		return m_notes;
	}

//	public String getOntology() {
//		if(m_onto == null)
//			m_onto = "";
//		return m_onto; 
//	}
	
	public Set<String> getVariables(){
		return m_variables;
	}
	
	public void setVariables(Set<String> vars) {
		m_variables = vars;
	}
	
	public Set<String> getSelectedVariables(){
		return m_selectedVars;
	}
	
	public void setSelectedVariables(Set<String> vars) {
		m_selectedVars = vars;
	}
	
//	public void setOntology(String onto) {
//		m_onto = onto;
//	}
	
	public Stack<Prefix> getPrefixes() {
		return m_prefixes;
	}
	
	public void setPrefixes(Stack<Prefix> prefixes) {
		m_prefixes = prefixes;
	}
	
	public Stack<String[]> getPredicates() {
		return m_predicates;
	}
	
	public void setPredicates(Stack<String[]> predicates) {
		m_predicates = predicates;
	}
	
	public void setNotes(String notes) {
		this.m_notes = notes;
	}

	public Date getEndDate() {
		return endDate;
	}	
	
	public void setEndDate(Date date) {
		this.endDate = date;
	}
	
	public String getEndDateString() {
		if (endDate != null) {
			String f = "yyyy-MM-dd HH:mm:ss.S z";
	    	SimpleDateFormat format = new SimpleDateFormat(f, Locale.ENGLISH);
	    	return format.format(endDate);
		} else {
			return "";
		}		
	}
	
	public void setEndDate(String date) {
		if (!date.equals("")) {
			String formatString = "yyyy-MM-dd HH:mm:ss.S z";
			SimpleDateFormat format = new SimpleDateFormat(formatString, Locale.ENGLISH);
			try {
				endDate = format.parse(date);
			} catch (ParseException e) {
				endDate = null;
			}
		} else {
				endDate = new Date(0);
		}
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date date) {
		this.creationDate = date;
	}
	
	public void setCreationDate(String date) {
		if (!date.equals("")) {
			String formatString = "yyyy-MM-dd HH:mm:ss.S z";
			SimpleDateFormat format = new SimpleDateFormat(formatString, Locale.ENGLISH);
			try {
				creationDate = format.parse(date);
			} catch (ParseException e) {
				creationDate = null;
			}
		} else {
				creationDate = new Date(0);
		}
	}
	
	public String getCreationDateString() {
		if (creationDate != null) {
			String f = "yyyy-MM-dd HH:mm:ss.S z";
	    	SimpleDateFormat format = new SimpleDateFormat(f, Locale.ENGLISH);
	    	return format.format(creationDate);
		} else {
			return "";
		}
	}

	public void setDescription(String description) {
		this.m_description = description;
	}

	public String getDescription() {
		if(m_description == null)
			m_description = "";
		return m_description;
	}

	public String getPath() {
		return null;
	}

	public void setPath(String path) {
		
	}

	public String getUri() {
		return m_uri;
	}

	public String toString() {
		return "[" + m_name + ", " + m_uri + ", " + m_description + ", " + creationDate + ", " + endDate + "]";
	}

	public IQueryMetaFilter getMetaFilter() {
		return m_meta;
	}

	public void setMetaFilter(IQueryMetaFilter constraints) {
		m_meta = constraints;
	}
	
	public String toSPARQL() {
		String s = "";
		for (Prefix prefix : m_prefixes)
			if (prefix.getPrefix() != null && prefix.getPrefix().length() > 0 && prefix.getOntology() != null && prefix.getOntology().length() > 0)
				s += "PREFIX " + prefix.getPrefix() + ": <" + prefix.getOntology() + ">\n"; 
		s += "SELECT ";
		for (String var : m_selectedVars) {
			if (!var.startsWith("?"))
				var = "?" + var;
			s += var + " ";
		}
		s += "WHERE { \n";
		for (String[] pred : m_predicates)
			if (pred[0].length() > 0 && pred[1].length() > 0 && pred[2].length() > 0)
				s += pred[0] + " " + pred[1] + " " + pred[2] + " . \n";
		s += "}";
		return s;
	}
}
