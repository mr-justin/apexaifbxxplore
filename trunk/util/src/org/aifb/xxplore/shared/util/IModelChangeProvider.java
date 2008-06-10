package org.aifb.xxplore.shared.util;


/**
 * Notifies listener when internal state has changed. 
 * @author Administrator
 *
 */
public interface IModelChangeProvider {
	
	void removeModelChangeListener(IModelChangeListener listener);				

	void addModelChangeListener(IModelChangeListener listener);


}
