package org.xmedia.businessobject;

import java.io.Serializable;
import java.util.Date;

import org.aifb.xxplore.shared.exception.EmergencyException;
import org.aifb.xxplore.shared.util.PropertyUtils;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;


public abstract class AbstractBo implements Serializable, IBusinessObject {

    public static String PROP_OID = "oid";
    public static String PROP_IS_ACTIVE = "isActive";
    public static String PROP_MODIFIED = "modified";
    public static String PROP_TRACE = "trace";
    public static String PROP_UPDATE_USER = "updateUser";

    // ~ Instance fields -------------------------------------------------------------------------------------------------------

    /**
     * The unique ID of this BO
     */
    private Long mOid;

    /**
     * The version counter
     */
    private Long mTrace;

    /**
     * <code>true</code> if the BO is active, <code>false</code> if the BO is logically deleted
     */
    private Boolean mIsActive;

    /**
     * The date this BO was mModified the last time
     */
    private Date mModified;

    /**
     * The login-ID of the user who mModified this BO the last time
     */
    private String mUpdateUser;

    // ~ Constructors ----------------------------------------------------------------------------------------------------------

    /**
     * Creates a new AbstractBo.
     */
    public AbstractBo() {
    	mOid = new Long(UniqueIdGenerator.getInstance().getNewId());
    }

    public AbstractBo(Long id) {
        mOid = id;
        mIsActive = Boolean.TRUE;
    }

    // ~ Methods ---------------------------------------------------------------------------------------------------------------

    // ***** Standard methods *****

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        // Performance optimization
        if (other == this) {
            return true;
        }

        // Compare objects
        if (other != null) {
            if (other.getClass() == getClass()) {
                Long otherId = ((AbstractBo) other).mOid;
                if (mOid != null) {
                    return mOid.equals(otherId);
                }
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (mOid != null) {
            return mOid.hashCode();
        } else {
            throw new EmergencyException("BO has no ID!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return PropertyUtils.getUnqualifiedClassName(getClass()) + "(id: " + mOid + ", mIsActive: " + mIsActive + ", modifed: " + mModified
                + ", mTrace: " + mTrace + ", mUpdateUser: " + mUpdateUser + ")";
    }

    // ***** Getter and Setter methods *****

    /**
     * Returns the unique Id of this BO.
     *
     * @return The unique Id of this BO
     */
    public Long getOid() {
        return mOid;
    }


    /**
     * Sets the unique ID of this BO.
     *
     * @param id The unique ID of this BO
     */
    public void setOid(long id) {
        this.mOid = new Long(id);
    }

    /**
     * Returns the version counter.
     *
     * @return The version counter
     */
    public Long getTrace() {
        return mTrace;
    }

    /**
     * Sets the version counter.
     *
     * @param trace The version counter
     */
    public void setTrace(Long trace) {
        this.mTrace = trace;
    }

    /**
     * Sets the version counter.
     *
     * @param trace The version counter
     */
    public void setTrace(long trace) {
        this.mTrace = new Long(trace);
    }

    /**
     * Returns <code>true</code> if the BO is active, <code>false</code> if the BO is logically deleted.
     *
     * @return <code>true</code> if the BO is active, <code>false</code> if the BO is logically deleted
     */
    public Boolean getIsActive() {
        return mIsActive;
    }

    /**
     * Sets the active flage of the BO.
     *
     * @param isActive <code>true</code> if the BO is active, <code>false</code> if the BO is logically deleted
     */
    public void setIsActive(Boolean isActive) {
        this.mIsActive = isActive;
    }

    /**
     * Sets the active flage of the BO.
     *
     * @param isActive <code>true</code> if the BO is active, <code>false</code> if the BO is logically deleted
     */
    public void setIsActive(boolean isActive) {
        this.mIsActive = Boolean.valueOf(isActive);
    }

    /**
     * Returns the date this BO was mModified the last time.
     *
     * @return The date this BO was mModified the last time
     */
    public Date getModified() {
        return mModified;
    }

    /**
     * Sets the date this BO was mModified the last time.
     *
     * @param modified The date this BO was mModified the last time
     */
    public void setModified(Date modified) {
        this.mModified = modified;
    }

    /**
     * Returns the login-ID of the user who mModified this BO the last time.
     *
     * @return The login-ID of the user who mModified this BO the last time
     */
    public String getUpdateUser() {
        return mUpdateUser;
    }

    /**
     * Sets the login-ID of the user who mModified this BO the last time.
     *
     * @param updateUser The login-ID of the user who mModified this BO the last time
     */
    public void setUpdateUser(String updateUser) {
        this.mUpdateUser = updateUser;
    }
}
