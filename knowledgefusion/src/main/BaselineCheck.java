package main;

public class BaselineCheck {
	
	static String workFolder = ""; 
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		KeyIndDealer.translateDocNum(Indexer.indexFolder+"keyInd.txt",
				Analyzer.countLines(Indexer.indexFolder+"keyInd.txt"),
				workFolder + "r" + args[0] + ".txt", workFolder + "r" + args[0] + "translated.txt");
		
	}
}
