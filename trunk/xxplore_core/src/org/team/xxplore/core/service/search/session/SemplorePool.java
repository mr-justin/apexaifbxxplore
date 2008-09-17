package org.team.xxplore.core.service.search.session;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import com.ibm.semplore.btc.QueryEvaluator;

public class SemplorePool {
	public static int semplore_num = 5;
	public static QueryEvaluator pool [] = new QueryEvaluator[semplore_num];
	public static ReentrantLock [] locks = new ReentrantLock[semplore_num];
	public static Semaphore sem = new Semaphore(semplore_num);
	
	static {
		for(int i=0;i<semplore_num;i++) {
			locks[i] = new ReentrantLock();
		}
	}
	
	public static QueryEvaluator getEvaluator(int i) {
		return pool[i];
	}
	
	public static int acquire() throws InterruptedException {
		sem.acquire();
		for(int i=0;i<semplore_num;i++) {
			if( locks[i].tryLock() ) {
				return i;
			}
		}
		System.err.println("acquire: All the semplore is being used!");
		return -1;
	}
	
	public static void release(int i) {
		locks[i].unlock();
		sem.release();
	}
	
}
