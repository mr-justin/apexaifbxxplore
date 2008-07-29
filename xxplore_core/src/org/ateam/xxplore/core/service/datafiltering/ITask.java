package org.ateam.xxplore.core.service.datafiltering;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.xmedia.oms.model.api.INamedIndividual;

public interface ITask extends Serializable {
    
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
	
	public String getBasePolicyOntology();
	
	public void setBasePolicyOntology();
	
	public Set<INamedIndividual> getAgents();
	
	public Set<INamedIndividual> getInformationProviders(); 

}