import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.ibm.semplore.imports.impl.data.load.Util4NT;


/**
 * @author xrsun
 *
 */
public class RDF2Triple {
	
	public static void main(String[] args) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(args[0]));
		Parser parser = new Parser(r);
		
		String part;
		String line; 
		while ((part = parser.nextPart())!=null) {
			if (part.equals("Instances"))
				while (true) {
					line = parser.nextLine(".");
					if (line.equals("")) break;
					int pos = line.indexOf(" : ");
					String i = line.substring(0, pos);
					String t = line.substring(pos + 3);
					if (t.endsWith(".")) t = t.substring(0, t.length()-1);

					System.out.println(String.format("<%s>\t%s\t<%s> .", parser.parseURI(i), Util4NT.TYPE, parser.parseURI(t)));
				}
			
			if (part.equals("Attribute/Values"))
				while (true) {
					line = parser.nextLine("].");
					if (line.equals("")) break;
					int pos  = line.indexOf("[");
					String i = line.substring(0, pos);
					int pos1 = line.indexOf("->>", pos+1);
					String p = line.substring(pos+1, pos1);
					int pos2 = line.indexOf(']', pos1+1);
					String o = line.substring(pos1+3, pos2);
					o = parser.parseURI(o).trim();
					if (o.startsWith("http://")) o = '<' + o + '>';
					else o = '"' + o + '"';
					
					if (o!="\"\"")System.out.println(String.format("<%s>\t<%s>\t%s .", parser.parseURI(i), parser.parseURI(p),o));
				}
		}
	}

}

class Parser {
	BufferedReader reader;
	
	Parser(BufferedReader r) {
		reader = r;
	}
	
	String nextPart() throws IOException {
		String line;
		while ((line = reader.readLine())!=null) {
			if (!line.startsWith("//")) continue;
			try {
				line = line.substring(3, line.indexOf('-')).trim();
			} catch (Exception e) {
				continue;
			}
			if (!line.equals("")) return line; 
		}
		return line;
	}
	
	String nextLine(String end) throws IOException {
		String line = reader.readLine();
		if (line.equals("")) return "";
		while  (!line.endsWith(end)) line += reader.readLine();
		return line;
	}
	
	String parseURI(String s) {
		String uri = "";
		if (s.length() == 0) return uri;
		boolean inString = false;
		for (int i=0; i<s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '"') inString = !inString;
			else if (ch == '\\') {
				i++; 
				uri += ch + s.charAt(i);
			} else uri += ch;
		}
		return uri;
	}
}