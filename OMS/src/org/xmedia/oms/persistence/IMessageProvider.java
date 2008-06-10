package org.xmedia.oms.persistence;

public interface IMessageProvider {

	void addMessageListener(IMessageListener listener);
	
	void removeMessageListener(IMessageListener listener);
	
}
