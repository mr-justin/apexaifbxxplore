/**
 * 
 */
package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.ibm.semplore.imports.impl.data.load.Util4NT;

/**
 * @author xrsun
 *
 */
public class EliminateBlanknode {
	void process() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//		BufferedReader reader = new BufferedReader(new FileReader("d:\\sxr\\temp\\swetodblp_april_2008-mod.nt"));
		Writer writer = new OutputStreamWriter(System.out);
		String line;
		line = reader.readLine();
		while (line!=null) {
			String[] triple = Util4NT.line2Triple(line);
			if (triple!=null) {
				if (triple[2].startsWith("_")) {
					//expand BlankNode
					//assume BlankNode are directly adjacent
					String line1;
					while ((line1 = reader.readLine())!=null) {
						String[] triple1 = Util4NT.line2Triple(line1);
						if (triple1==null) continue;
						if (triple1[0].equals(triple[2])) {
							if (triple1[1].equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")) continue;
							writer.write(triple[0] + " " + triple[1] + " " + triple1[2] + "\n");
						} else break;
					}
					line = line1;
					continue;
				} else
					writer.write(line+"\n");
			}
			line = reader.readLine();
		}
		reader.close();
		writer.close();
		
	}

	//pipe input triples through stdin
	public static void main(String[] args) throws IOException {
		(new EliminateBlanknode()).process();
	}

}
