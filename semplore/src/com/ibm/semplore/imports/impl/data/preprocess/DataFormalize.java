package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DataFormalize {

	public void removeRelationPrefix(String filename, String relFile, String attFile) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(filename));
		BufferedWriter relwr = new BufferedWriter(new FileWriter(relFile));
		BufferedWriter attwr = new BufferedWriter(new FileWriter(attFile));
		String temp;
		int count = 0;

		while ((temp = rd.readLine()) != null) {
			count ++;
			if (count % 5000 == 0) System.out.println(count);
//			System.out.println(temp);
//			if (count > 10) break;
			String[] split = temp.split(">");

			if (split[0].startsWith("<http://dbpedia.org/resource/") && 
					split[1].startsWith(" <http://dbpedia.org/property/")) {
				if (split[2].startsWith(" <http://dbpedia.org/resource/")) {
					relwr.write(split[0].substring(29) + "\t" + split[1].substring(30) + "\t" + 
							split[2].substring(30) + "\n");
				}
				else if (!split[2].startsWith(" <http://")){
					String[] attsp = split[2].split("\"");

					if (attsp.length > 1) {
						attsp[1] = attsp[1].replace("\t", " ");
//						attsp[1] = attsp[1].replace("\n", " ");
//						attsp[1] = attsp[1].replace("\r", " ");
						attwr.write(split[0].substring(29) + "\t" + split[1].substring(30) + "\t" + attsp[1] + "\n");
					}
				}
			}
		}
		rd.close();
		attwr.close();
		relwr.close();
	}
	
	public void removeCategoryPrefix(String filename, String catFile) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(filename));
		BufferedWriter wr = new BufferedWriter(new FileWriter(catFile));
		String temp;
		int count = 0;
		
		while ((temp = rd.readLine()) != null) {
//			System.out.println(temp);
			count ++;
			if (count % 5000 == 0) System.out.println(count);
			
			String[] split = temp.split(">");
			if (split[0].startsWith("<http://dbpedia.org/resource/") && split[2].startsWith(" <http://dbpedia.org/class/yago/")) {
				wr.append(split[0].substring(29) + "\t" + split[2].substring(32) + "\n");
			}
		}
		wr.close();
		rd.close();
	}

	public static void main(String[] args) {
		DataFormalize ds = new DataFormalize();

		try {
//			ds.removeRelationPrefix("data/dbpedia/infoboxes.nt", "data/dbpedia/relations.nt", "data/dbpedia/attrbutes.nt");
			ds.removeCategoryPrefix("data/dbpedia/yago_classes.nt", "data/dbpedia/category.nt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
