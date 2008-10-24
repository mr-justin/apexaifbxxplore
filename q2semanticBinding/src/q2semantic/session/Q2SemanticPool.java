package q2semantic.session;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import org.ateam.xxplore.core.service.q2semantic.SearchQ2SemanticService;

public class Q2SemanticPool {
	public static int q2semantic_num = 5;
	public static SearchQ2SemanticService pool [] = new SearchQ2SemanticService[q2semantic_num];
	public static ReentrantLock [] locks = new ReentrantLock[q2semantic_num];
	public static Semaphore sem = new Semaphore(q2semantic_num);
	
	static {
		for(int i = 0; i < q2semantic_num; i++) {
			locks[i] = new ReentrantLock();
			try {
				pool[i] = new SearchQ2SemanticService();
				pool[i].loadPara("config/path.prop");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static SearchQ2SemanticService getEvaluator(int i) {
		return pool[i];
	}
	
	public static int acquire() throws InterruptedException {
		sem.acquire();
		for(int i = 0; i < q2semantic_num; i++) {
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
