package org.team.xxplore.core.service.q2semantic.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import com.hp.hpl.jena.vocabulary.RDF;

public class RemoveBlankNode {
	
	private String fn;
	private String blankNode;
	
	public RemoveBlankNode(String fn, String blankNode)
	{
		this.fn = fn;
		this.blankNode = blankNode;
	}

	public void removeBlankNode() throws Exception
	{
		Parameters para = Parameters.getParameters();
		/* Output File Name */
		HashMap<String, String> blankNodeMap = new HashMap<String, String>();
		HashSet<String> bnMeansCollection = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(fn));
		PrintWriter pw1 = new PrintWriter(new FileWriter(para.blankNodeFile));
		PrintWriter pw2 = new PrintWriter(new FileWriter(para.noBlankNodeFile));
		
		/* First Scan Record BlankNode */
		String line;
		while((line = br.readLine())!=null)
		{
			String[] parts = line.replaceAll("<", "").replaceAll(">", "").split(" ");
			if(parts[0].startsWith(blankNode) && !parts[2].startsWith(blankNode))
			{
				if(bnMeansCollection.contains(parts[0])){
					pw1.println(line);
				}
				else if(parts[1].equals(RDF.type.getURI()) && para.conEdgeSet.contains(parts[2])){
					bnMeansCollection.add(parts[0]);
				}
			}
			else if(!parts[0].startsWith(blankNode) && parts[2].startsWith(blankNode)){
				blankNodeMap.put(parts[2], "<"+parts[0]+"> <"+parts[1]+">");
			}
			else if(!parts[0].startsWith(blankNode) && !parts[2].startsWith(blankNode)){
				pw2.println(line);
			}
		}
		br.close();
		pw1.close();
		
		/* Second Scan Connect BlankNode */
		br = new BufferedReader(new FileReader(para.blankNodeFile));
		while((line = br.readLine())!=null)
		{
			String[] parts = line.split(" ");
			if(blankNodeMap.containsKey(parts[0])){
				pw2.println(blankNodeMap.get(parts[0])+" "+parts[2]+" .");
			}
		}
		pw2.close();
		br.close();

		/* Delete Temp File */
		System.gc();
		if(!new File(fn).delete()){
			new File(fn).deleteOnExit();
		}
		if(!new File(para.blankNodeFile).delete()){
			new File(para.blankNodeFile).deleteOnExit();
		}
		new File(para.noBlankNodeFile).renameTo(new File(fn));//cover the original nt file
	}
}
