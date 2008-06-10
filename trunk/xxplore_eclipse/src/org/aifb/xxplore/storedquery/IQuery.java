package org.aifb.xxplore.storedquery;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.Stack;

import org.xmedia.oms.model.api.INamedIndividual;

public interface IQuery extends Serializable {
    
    public abstract String getPath();

    public abstract void setPath(String path);
        
    public abstract String getNotes();
    
    public abstract void setNotes(String notes);

    public abstract Date getEndDate();
	
	public abstract void setEndDate(String date);
	
	public abstract void setEndDate(Date date);
	
	public abstract String getEndDateString();
		
	public abstract Date getCreationDate();
	
	public abstract void setCreationDate(String date);
	
	public abstract void setCreationDate(Date date);
	
	public abstract String getCreationDateString();
	
	public abstract String getName();
	
	public abstract String getDescription();
	
	public void setDescription(String description);
	
	public abstract String getUri();

	public abstract void setName(String name);
	
//	public String getOntology();
	
	public Stack<Prefix> getPrefixes();
	
//	public void setOntology(String onto); 
	
	public Stack<String[]> getPredicates();
	
	public Set<String> getVariables();
	
	public Set<String> getSelectedVariables();
	
	public void setPredicates(Stack<String[]> predicates);
	
	public void setPrefixes(Stack<Prefix> prefixes);
	
	public void setSelectedVariables(Set<String> vars);
	
	public void setVariables(Set<String> vars);
	
	public void setMetaFilter(IQueryMetaFilter constraints);
	
	public IQueryMetaFilter getMetaFilter();

	public String toSPARQL();
}