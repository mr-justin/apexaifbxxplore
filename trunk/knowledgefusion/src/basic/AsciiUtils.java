package basic;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.Normalizer;

import main.Blocker;

public class AsciiUtils {
	private static final String PLAIN_ASCII = "AaEeIiOoUu" // grave`
			+ "AaEeIiOoUuYy" // acute'
			+ "AaEeIiOoUuYy" // circumflex-
			+ "AaOoNn" // tilde~
			+ "AaEeIiOoUuYy" // umlaut"
			+ "Aa" // ring \circ
			+ "Cc" // cedilla tail below
			+ "OoUu" // double acute 
			+ "Nn" // 
	;

	private static final String UNICODE = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
			+ "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
			+ "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
			+ "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
			+ "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
			+ "\u00C5\u00E5" + "\u00C7\u00E7" + "\u0150\u0151\u0170\u0171" + "\u0124\u0144";

	private static final String[] UNICODE_STRING = { "\\u00C0", "\\u00E0",
			"\\u00C8", "\\u00E8", "\\u00CC", "\\u00EC", "\\u00D2", "\\u00F2",
			"\\u00D9", "\\u00F9", "\\u00C1", "\\u00E1", "\\u00C9", "\\u00E9",
			"\\u00CD", "\\u00ED", "\\u00D3", "\\u00F3", "\\u00DA", "\\u00FA",
			"\\u00DD", "\\u00FD", "\\u00C2", "\\u00E2", "\\u00CA", "\\u00EA",
			"\\u00CE", "\\u00EE", "\\u00D4", "\\u00F4", "\\u00DB", "\\u00FB",
			"\\u0176", "\\u0177", "\\u00C3", "\\u00E3", "\\u00D5", "\\u00F5",
			"\\u00D1", "\\u00F1", "\\u00C4", "\\u00E4", "\\u00CB", "\\u00EB",
			"\\u00CF", "\\u00EF", "\\u00D6", "\\u00F6", "\\u00DC", "\\u00FC",
			"\\u0178", "\\u00FF", "\\u00C5", "\\u00E5", "\\u00C7", "\\u00E7",
			"\\u0150", "\\u0151", "\\u0170", "\\u0171", "\\u0124", "\\u0144" };

	// private constructor, can't be instanciated!
	private AsciiUtils() {
	}

	public static void main(String args[]) throws Exception {
//		String s = "Bad\u00EDa";
//		System.out.println(s);
//		System.out.println(AsciiUtils.convertNonAscii(s));
//		unaccentFile(Blocker.workFolder+"keyIndBasicFeatureCaned.txt", 
//				Blocker.workFolder+"keyIndBasicFeatureUnacced.txt");
//		unaccentFile(Blocker.workFolder+"keyIndExtendedFeatureCaned.txt", 
//				Blocker.workFolder+"keyIndExtendedFeatureUnacced.txt");
//		for (int i = 0; i < 255; i++) {
//			System.out.printf("%c \\u%04X\n", (char)i, i);
//		}
		System.out.println(unicodeEncode("Nowogr\\u00F3d Bobrza\\u0144ski"));
	}

	// remove accentued from a string and replace with ascii equivalent
	public static String convertNonAscii(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			if (i > n-6) {
				sb.append(s.charAt(i));
				continue;
			}
			String c = s.substring(i, i+6);
			int pos;
			for (pos = 0; pos < UNICODE_STRING.length && !UNICODE_STRING[pos].equals(c); pos++) ;
			if (pos < UNICODE_STRING.length) {
				sb.append(PLAIN_ASCII.charAt(pos));
				i += 5;
			} else {
				sb.append(c.charAt(0));
			}
		}
		return sb.toString();
	}

	public static void unaccentFile(String input, String output) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(input);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			pw.println(convertNonAscii(line));
			lineCount++;
			if (lineCount % 10000 == 0) System.out.println(lineCount);
		}
		pw.close();
		br.close();
	}
	
	public static String unicodeEncode(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			if (i > n-6 || s.charAt(i) != '\\' || i < n-1 && s.charAt(i+1) != 'u') {
				sb.append(s.charAt(i));
				continue;
			}
			String c = s.substring(i+2, i+6);
			int ch = Integer.parseInt(c, 16);
			sb.append((char)ch);
			i += 5;
		}
		return sb.toString();
		
	}
	
}
