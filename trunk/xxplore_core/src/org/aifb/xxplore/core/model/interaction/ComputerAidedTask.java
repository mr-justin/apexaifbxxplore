package org.aifb.xxplore.core.model.interaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.xmedia.oms.model.api.INamedIndividual;

public class ComputerAidedTask implements IComputerAidedTask{
	
	private String m_uri;
	
	private String m_des;
	
	private String m_name;
	
	 private Date endDate = null;
	 
	 private Date creationDate = null;
	
	public ComputerAidedTask(String m_uri){
		this.m_uri = m_uri;
	}
	
	public String getUri(){
		return this.m_uri;
	}

	public Set<INamedIndividual> getAgents() {
		// TODO Auto-generated method stub
		return ComputerAidedTaskDao.getInstance().findAgentsForTask(this);
	}

	public Date getCreationDate() {
		// TODO Auto-generated method stub
		return this.creationDate;
	}

	public String getCreationDateString() {
		// TODO Auto-generated method stub
		if (creationDate != null) {
			String f = "yyyy-MM-dd HH:mm:ss.S z";
	    	SimpleDateFormat format = new SimpleDateFormat(f, Locale.ENGLISH);
	    	return format.format(creationDate);
		} else {
			return "";
		}
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return this.m_des;
	}

	public Date getEndDate() {
		// TODO Auto-generated method stub
		return this.endDate;
	}

	public String getEndDateString() {
		// TODO Auto-generated method stub
		if (endDate != null) {
			String f = "yyyy-MM-dd HH:mm:ss.S z";
	    	SimpleDateFormat format = new SimpleDateFormat(f, Locale.ENGLISH);
	    	return format.format(endDate);
		} else {
			return "";
		}	
	}

	public String getName() {
		// TODO Auto-generated method stub
		return this.m_name;
	}

	public void setCreationDate(String date) {
		// TODO Auto-generated method stub
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

	public void setCreationDate(Date date) {
		// TODO Auto-generated method stub
		this.creationDate = date;
	}

	public void setDescription(String description) {
		// TODO Auto-generated method stub
		this.m_des = description;
	}

	public void setEndDate(String date) {
		// TODO Auto-generated method stub
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

	public void setEndDate(Date date) {
		// TODO Auto-generated method stub
		this.endDate = date;
	}

	public void setName(String name) {
		// TODO Auto-generated method stub
		this.m_name = name;
	}
}
