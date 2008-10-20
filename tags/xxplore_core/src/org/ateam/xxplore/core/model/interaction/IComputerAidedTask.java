package org.ateam.xxplore.core.model.interaction;

import java.util.Date;
import java.util.Set;

import org.xmedia.oms.model.api.INamedIndividual;

public interface IComputerAidedTask{
    
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
	
	public Set<INamedIndividual> getAgents();
	
}
