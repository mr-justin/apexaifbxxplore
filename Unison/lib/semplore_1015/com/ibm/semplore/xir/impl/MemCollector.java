/**
 * 
 */
package com.ibm.semplore.xir.impl;

import java.util.ArrayList;

import org.apache.lucene.search.HitCollector;

/**
 * @author lql
 *
 */
public class MemCollector extends HitCollector {

	private ArrayList<Integer> id = new ArrayList<Integer>();
	private ArrayList<Float> score = new ArrayList<Float>();
	private float max = -1;

	public int size() {
	return id.size();
	}

	public int getId(int i) {
	return id.get(i);
	}

	public float getScore(int i) {
	return score.get(i);
	}

	public float getMaxScore() {
		return max;
	}
	
	@Override
	public void collect(int arg0, float arg1) {
	id.add(arg0);
	score.add(arg1);
	if (max<arg1)
		max = arg1;
	}
}
