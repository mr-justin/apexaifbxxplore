package org.ateam.xxplore.core.service;

public interface IService {
	
	public void init(Object... params);

	public void callService(IServiceListener listener, Object... params);
	
	public void disposeService();
	
}
