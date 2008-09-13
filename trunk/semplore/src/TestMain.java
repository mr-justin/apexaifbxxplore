import java.io.StringReader;
import java.util.Scanner;


public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String text = "-933737343608282964\tURI\t3069097663303534468\t<Britney_Spears>\n-933737343608282964\tTYPE\t-95230424018221932\t-7302756725272101313\n";
		Scanner dataReader = new Scanner(new StringReader(text));
		long s, p;
		String o;
		String itype;

		while (dataReader.hasNext()) {
			s = dataReader.nextLong(); 
			itype = dataReader.next();
			p = dataReader.nextLong(); 
			o = dataReader.next();
			System.out.println(s+","+itype+","+p+","+o);
		}
	}

}
