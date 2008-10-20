package org.ateam.xxplore.core.service.datafiltering;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.xmedia.oms.model.api.INamedIndividual;

public class Task implements ITask {

    private static final long serialVersionUID = 3545518391537382197L;
    private String description;
    private String notes = "";
    private Date endDate = null;
    private Date creationDate = null;
	private String m_name = "";
	private String m_uri;

    public Task() {
    } 
    
    public Task(String uri) {
    	m_uri = uri;
    }
    
	public String getNotes() {
		if (notes == null) {
			notes = "";
		}
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
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
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return m_name;
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPath(String path) {
		// TODO Auto-generated method stub
		
	}

	public String getUri() {
		// TODO Auto-generated method stub
		return m_uri;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String toString() {
		return "[" + m_name + ", " + m_uri + ", " + description + ", " + creationDate + ", " + endDate + "]";
	}

	public String getBasePolicyOntology() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBasePolicyOntology() {
		// TODO Auto-generated method stub
		
	}

	public Set<INamedIndividual> getAgents() {
		return TaskPolicyDao.getInstance().findAgentsForTask(this);
	}

	public Set<INamedIndividual> getInformationProviders() {
		return TaskPolicyDao.getInstance().findInformationProviderForTask(this);
	}
}
