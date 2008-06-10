package org.aifb.xxplore.shared.util;

import java.util.HashMap;
import java.util.Map;

public class UniqueIdGenerator {
	private long m_lastid = 0;
	private long m_lastVarId = 0;

	private Map<String,Long> m_uri2Id;

	//Private constructor suppresses generation of a (public) default constructor
	private UniqueIdGenerator() {
		m_uri2Id = new HashMap<String,Long>();
	}

	private static class SingletonHolder {
		private static UniqueIdGenerator s_instance = new UniqueIdGenerator();
	} 

	public static UniqueIdGenerator getInstance() {
		return SingletonHolder.s_instance;
	}

	/**
	 * 
	 * @return the id of the ontology, which is the same like the indey of m_ontos
	 */
	public long getNewId(String uri){
		synchronized(m_uri2Id) {
			if (!m_uri2Id.containsKey(uri))
				m_uri2Id.put(uri, m_lastid++);

			return m_uri2Id.get(uri);
		}
	}

	public long getNewId(){
		synchronized(m_uri2Id) {
			return m_lastid ++;
		}
	}

	public long getNewVarId(){
		return m_lastVarId ++;
	}
	
	public void resetVarIds(){
		m_lastVarId = 0;
	}

}
