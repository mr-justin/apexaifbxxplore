package org.ateam.xxplore.core.model.interaction;

import java.util.Date;

import org.xmedia.businessobject.AbstractBo;

public class ApplicationInteraction extends AbstractBo {
	
    private String mTitle;
    
    private Date mStartTime;

    private Date mEndTime;
    
    private long mDurationInMillis; 

    public Date getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Date startTime) {
        mStartTime = startTime;
    }

    public Date getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Date endTime) {
        mEndTime = endTime;
    }
    
    /**
     * duration of the interaction in milliseconds. 
     * @return long value of the duration in milliseconds or -1.
     */
    public long getDuration() {
        if (mEndTime != null && mStartTime != null){
        	if (mStartTime.before(mEndTime)){
        		return mDurationInMillis = mEndTime.getTime()-mStartTime.getTime();
        	}
        	
        }
        return -1; 
    }

    

}
