package com.ibm.semplore.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUHashMap<K,V> extends LinkedHashMap<K,V> {
	private final int capacity;

	public LRUHashMap(int capacity) {
		super(capacity + 1, 1.1f, true);
		this.capacity = capacity;
	}

	protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > capacity;
	}
}