package org.team.xxplore.core.service.mapping;

import java.util.*;
import java.io.*;

/**
 * change the count to percent of property mapping
 * @author jqchen
 *
 */
public class CountPropertyPercent {
	/**
	 * change the type to a single integer.
	 * @param t
	 * @return
	 */
	private int getInt(String t) {
		if(t.equals("attribute")) {
			return 0;
		}
		else if(t.equals("relation_subject")) {
			return 1;
		}
		else if(t.equals("relation_object")) {
			return 2;
		}
		else return -1;
	}
	
	/**
	 * get the count of instance of property.
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, int[]> getCount(String filename) throws IOException {
		HashMap<String,int []> hm = new HashMap();
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while((line = br.readLine())!=null) {
			String [] tokens = line.split("\t");
			if(tokens.length != 3) {
				System.err.println("getcount " + line);
				continue;
			}
			
			int [] t = hm.get(tokens[1]);
			
			if(t == null) t = new int[3];
			t[getInt(tokens[2])] ++;
			hm.put(tokens[1], t);
		}
		return hm;
	}
	
	/**
	 * 
	 * @param filename1
	 * @param filename2
	 * @param hm1
	 * @param hm2
	 * @throws IOException
	 */
	public void readFile(String filename1,String filename2,HashMap<String,int[]> hm1,HashMap<String,int[]> hm2) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename1));
		PrintWriter pw = new PrintWriter(filename2);
		String line;
		while((line = br.readLine()) != null) {
			String [] tokens = line.split("\t");
			int t1[] = hm1.get(tokens[0]);
			int t2[] = hm2.get(tokens[1]);
			int pos = this.getInt(tokens[3]);
			double rate = Integer.parseInt(tokens[2])/(double)(t1[pos] + t2[pos]);
			String out = tokens[0] + "\t" + tokens[1] + "\t" + rate + "\t" + tokens[3];
			pw.println(out);
		}
		pw.close();
	}
	
	/**
	 * Find the file whoes name contain the string part.
	 * @param list
	 * @param part
	 * @return
	 */
	private File findFile(File[] list,String part) {
		for(File file : list) {
			if( file.getName().indexOf(part) != -1 )
				return file;
		}
		return null;
	}
	
	/**
	 * handle one fold.
	 * @param file
	 * @throws IOException
	 */
	public void runone(File file) throws IOException {
		File f1 = this.findFile(file.listFiles(), "temp1");
		File f2 = this.findFile(file.listFiles(), "temp2");
		
		File f3 = new File(file.getAbsolutePath() + "/output");
		
		if(f1 == null) return;
		
		HashMap<String, int[]> hm1 = this.getCount(f1.getAbsolutePath());
		HashMap<String, int[]> hm2 = this.getCount(f2.getAbsolutePath());
		
		String output = file.getAbsolutePath() + "/output2";
		this.readFile(f3.getAbsolutePath(),output,hm1,hm2);
		
	}
	
	/**
	 * scan the root fold and handle all the subfold.
	 * @throws IOException
	 */
	public void run() throws IOException {
		File t = new File("//hera/user/jqchen/mapping_count/");
		for(File file : t.listFiles()) {
			System.out.println(file.getAbsolutePath());
			runone(file);
		}
	}
	
	public static void main(String[] args) throws IOException {
		new CountPropertyPercent().run();
	}
}
