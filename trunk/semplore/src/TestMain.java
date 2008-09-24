import java.util.HashMap;



public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HashMap<Integer, Integer> map = new HashMap<Integer,Integer>();
		for (int i=0; i<1200000; i++)
			map.put(i,-i);
		Runtime r = Runtime.getRuntime();
		System.out.println(r.totalMemory() - r.freeMemory());
	}

}
