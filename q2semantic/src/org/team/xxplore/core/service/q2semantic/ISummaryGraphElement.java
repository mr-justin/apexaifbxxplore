package org.team.xxplore.core.service.q2semantic;

import java.io.Serializable;

public interface ISummaryGraphElement extends Serializable {
	
	public int getType();
	
	public String getDatasource();
	
	public double getEF();
	
	public double getMatchingScore();
	
	public double getTotalCost();
}
