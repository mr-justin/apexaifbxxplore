package main;

public class Evaluation {

	static String workFolder = "";
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		KeyIndDealer.evaluate(workFolder+ "r" + args[0] + "sorted.txt",
				Indexer.indexFolder + "sameAsID.txt", workFolder + "result" + args[0] + ".txt");
	}

}
