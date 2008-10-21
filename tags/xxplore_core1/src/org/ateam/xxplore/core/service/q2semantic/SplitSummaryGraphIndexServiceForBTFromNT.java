package org.ateam.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.graph.Pseudograph;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;

import com.ibm.semplore.imports.impl.data.load.Util4NT;



public class SplitSummaryGraphIndexServiceForBTFromNT extends SummaryGraphIndexServiceForBTFromNT {

	public static String GRAPHEDGEPOOL = "-graphEdgePool", OBJPROPPOOL = "-objpropPool", conceptCountObj = "-conceptCount.obj", resultFile = "-result.txt";
	public static long MAX_OBJPROP_FILESIZE = 1*1024*1024*1024, MAX_GRAPHEDGE_FILESIZE = 50*1024*1024, MIN_GRAPHEDGE_FILESIZE = 10*1024;
	public static double MIN_OBJPROP_SCORE = 0.000001;
	public TreeMap<String, Integer> predPool;
	public TreeMap<String, SummaryGraphElement> elemPool;
	
	public void buildGraphs(String path) throws Exception
	{	
		conceptCount = new TreeMap<String, Integer>();
		elemPool = new TreeMap<String, SummaryGraphElement>();
		indiv2con = new LuceneMap();
		
		firstScan(path);
		System.gc();
		
		secondScan(path);
		System.gc();
		
		thirdScan(path);
		System.gc();

	}

	public void firstScan(String path) throws Exception
	{
		System.out.println("=======firstScan==========");
		indiv2con.openWriter(path, true);
		indivSize = BuildQ2SemanticService.instNumMap.get(BuildQ2SemanticService.datasource)==null?
				-1:BuildQ2SemanticService.instNumMap.get(BuildQ2SemanticService.datasource);
		BufferedReader br = new BufferedReader(new FileReader(BuildQ2SemanticService.source));
		PrintWriter pw;
		new File(path+OBJPROPPOOL).mkdir();
		predPool = new TreeMap<String, Integer>();
		TreeSet<String> indivSet = new TreeSet<String>();
		String line;
		int count = 0, predID = 0;
		while((line = br.readLine())!=null)
		{
			count++;
			if(count%10000==0)
				System.out.println("1st scan\t"+count);

			String[] part = Util4NT.processTripleLine(line);
			if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2 || part[2].length()<2)
				continue;
//			System.out.println(part[0]+"\t"+part[1]+"\t"+part[2]);
			if(!part[0].startsWith("<") || !part[1].startsWith("<"))
				continue;
//			System.out.println(part[0]+"\t"+part[1]+"\t"+part[2]);
			String subj = part[0].substring(1, part[0].length()-1);
			String pred = part[1].substring(1, part[1].length()-1);
			String obj = part[2];
			if(!obj.startsWith("<"))
				obj = "";
			else if(obj.length()>=2) obj = part[2].substring(1, part[2].length()-1);
			if(!subj.startsWith("http") || !pred.startsWith("http"))
					continue;
//			System.out.println(subj+"\t"+pred+"\t"+obj);
			if(indivSize ==-1 && getSubjectType(pred, obj).equals(INDIVIDUAL))
				indivSet.add(subj);
			if(indivSize ==-1 && getObjectType(pred, obj).equals(INDIVIDUAL))
				indivSet.add(obj);
			if(!getPredicateType(pred, obj).equals(RDFSPROP))
				propSize++;
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getObjectType(pred, obj).equals(CONCEPT))
			{
				indiv2con.put(subj, obj);
				Integer i = conceptCount.get(obj);
				if(i==null) i = Integer.valueOf(0);
				conceptCount.put(obj, i+1);
			}
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getObjectType(pred, obj).equals(INDIVIDUAL) && getPredicateType(pred, obj).equals(OBJPROP))
			{
				Integer id = predPool.get(pred);
				if(id == null) 
				{
					id = predID;
					predPool.put(pred, predID++);
				}

				{
					File output = new File(path+OBJPROPPOOL+File.separator+id);
					if(!output.exists()) output.createNewFile();
					pw = new PrintWriter(new FileWriter(output, true));
					pw.println(subj+"\t"+obj);
					pw.close();
				}
			}
		}
		if(indivSize ==-1)
		{
			indivSize = indivSet.size();
			indivSet.clear();
			indivSet = null;
		}
		br.close();
		

		indiv2con.closeWriter();
		pw = new PrintWriter(new FileWriter(path+resultFile));
		pw.println(indivSize);
		pw.println(propSize);
		for(String key: predPool.keySet())
			pw.println(key+"\t"+predPool.get(key));
		pw.close();
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path+conceptCountObj));
		out.writeObject(conceptCount);
		out.close();
		System.out.println("indivSize: "+indivSize+"\tpropSize: "+propSize);
	}
	
	public void secondScan(String path) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("=======secondScan==========");
		
		indiv2con.openSearcher(path);
		BufferedReader root = new BufferedReader(new FileReader(path+resultFile)), br;
		new File(path+GRAPHEDGEPOOL).mkdir();
		PrintWriter pw;
		indivSize = Integer.parseInt(root.readLine());
		propSize = Integer.parseInt(root.readLine());
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(path+conceptCountObj));
		conceptCount = (TreeMap<String, Integer>)in.readObject(); 
		in.close();
		cache = new TreeMap<String, Set<String>>();
		String predid;
		int count = 0, hits = 0;
		while((predid = root.readLine())!=null)
		{
			String pred = predid.substring(0, predid.indexOf('\t'));
			String id = predid.substring(predid.indexOf('\t')+1);
			System.out.println((++count)+" "+pred+"\tcache:"+cache.size()+"\thits:"+hits);
			
			File file = new File(path+OBJPROPPOOL+File.separator+id);
			if(file.length() > MAX_OBJPROP_FILESIZE) continue;
			br = new BufferedReader(new FileReader(file));
			
			TreeMap<String, Integer> triples = new TreeMap<String, Integer>();
			
			String line;
			while((line = br.readLine())!= null)
			{
				String subj = line.substring(0, line.indexOf('\t'));
				String obj = line.substring(line.indexOf('\t')+1);
				Set<String> subjParent = null, objParent = null;
				subjParent = cache.get(subj);
				try 
				{
					if(subjParent == null || subjParent.size()==0)
						subjParent = indiv2con.search(subj);
					else hits++;
				} catch (Exception e) { e.printStackTrace(); continue; }
				
				if(subjParent == null || subjParent.size()==0)
				{
					subjParent = new TreeSet<String>();
					subjParent.add(NamedConcept.TOP.getUri());
				}
				if(!cache.containsKey(subj))
					cache.put(subj, subjParent);
//				System.out.println(obj);
				objParent = cache.get(obj);
				try 
				{
					if(objParent == null || objParent.size()==0)
						objParent = indiv2con.search(obj);
					else hits++;
				} catch (Exception e) { e.printStackTrace(); continue; }
				
				if(objParent == null || objParent.size()==0)
				{
					objParent = new TreeSet<String>();
					objParent.add(NamedConcept.TOP.getUri());
				}
				if(!cache.containsKey(obj))
					cache.put(obj, objParent);
				
				while(cache.size()>=MAX_CACHE_SIZE) cache.clear();
				
				for(String str: subjParent)
				{
					for(String otr: objParent)
					{
						Integer i = triples.get(str+'\t'+otr);
						if(i==null) i = 0;
						triples.put(str+'\t'+otr, i+1);
					}		
				}
				subjParent = null;
				objParent = null;
			}
			br.close();
			pw = new PrintWriter(new FileWriter(path+GRAPHEDGEPOOL+File.separator+id));
			System.out.println("search finished! size:"+triples.size());
			int order = 0;
			for(String so: triples.keySet())
			{
				String str = so.substring(0, so.indexOf('\t'));
				String otr = so.substring(so.indexOf('\t')+1);
				String ptr = pred+"("+order+")";
				double sscore, oscore, pscore;
				if(str.equals(NamedConcept.TOP.getUri()))
					sscore = TOP_ELEMENT_SCORE;
				else
				{
					Integer j = conceptCount.get(str);
					if(j==null) sscore = Double.MAX_VALUE;
					else sscore = j.doubleValue()/indivSize;
				}
				if(otr.equals(NamedConcept.TOP.getUri()))
					oscore = TOP_ELEMENT_SCORE;
				else
				{
					Integer j = conceptCount.get(otr);
					if(j==null) oscore = Double.MAX_VALUE;
					else oscore = j.doubleValue()/indivSize;
				}
				Integer i = triples.get(so);
				if(i==null) pscore = Double.MAX_VALUE;
				else pscore = i.doubleValue()/propSize;
				pw.println(str+"\t"+sscore+"\t"+ptr+"\t"+pscore+"\t"+otr+"\t"+oscore);
				order++;
			}
			pw.close();
			triples.clear();
			triples = null;
			
		}
		root.close();
		indiv2con.closeSearcher();
	}
	
	public void thirdScan(String path) throws Exception
	{
		System.out.println("=======thirdScan==========");
		BufferedReader root = new BufferedReader(new FileReader(path+resultFile)), br;
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph = new Pseudograph<SummaryGraphElement,SummaryGraphEdge>(SummaryGraphEdge.class);
		root.readLine(); root.readLine();
		
		String line;
		int count = 0;
		while((line = root.readLine())!= null)
		{
			String pred = line.substring(0, line.indexOf('\t'));
			System.out.println(++count+" "+pred+" "+elemPool.size());
			String id = line.substring(line.indexOf('\t')+1);
			File file = new File(path+GRAPHEDGEPOOL+File.separator+id);
			if(!file.exists()) continue;
//			if(file.length() > MAX_GRAPHEDGE_FILESIZE || file.length() < MIN_GRAPHEDGE_FILESIZE) continue;
			br = new BufferedReader(new FileReader(file));
			String line2;
			while((line2=br.readLine())!= null)
			{
				String[] part = line2.split("\t");
				
//				if(Double.parseDouble(part[3]) < MIN_OBJPROP_SCORE) continue;
				SummaryGraphElement p = getElem(part[2], SummaryGraphElement.RELATION);
				p.setCost(Double.parseDouble(part[3]));
				SummaryGraphElement s = getElem(part[0], SummaryGraphElement.CONCEPT);
				s.setCost(Double.parseDouble(part[1]));
				SummaryGraphElement o = getElem(part[4], SummaryGraphElement.CONCEPT);
				o.setCost(Double.parseDouble(part[5]));

				summaryGraph.addVertex(s);
				summaryGraph.addVertex(o);
				summaryGraph.addVertex(p);
		
				SummaryGraphEdge edge1 = new SummaryGraphEdge(s, p, SummaryGraphEdge.DOMAIN_EDGE);
				SummaryGraphEdge edge2 = new SummaryGraphEdge(p, o, SummaryGraphEdge.RANGE_EDGE);
				if(!summaryGraph.containsEdge(edge1))
					summaryGraph.addEdge(s, p, edge1);
				if(!summaryGraph.containsEdge(edge2))
					summaryGraph.addEdge(p, o, edge2);
			}
			br.close();
		}
		root.close();
		
		System.out.println("write splitted summary graph");
		writeSummaryGraph(summaryGraph, BuildQ2SemanticService.summaryObj+".split");
		writeSummaryGraphAsRDF(summaryGraph, BuildQ2SemanticService.summaryRDF+".split");
		outputGraphInfo(summaryGraph);
	}
	
	
	public SummaryGraphElement getElem(String uri, int type)
	{
		SummaryGraphElement res = elemPool.get(type+uri);
		if(res == null)
		{
			if(type == SummaryGraphElement.CONCEPT)
				res = new SummaryGraphElement(new NamedConcept(uri), SummaryGraphElement.CONCEPT);
			else if(type == SummaryGraphElement.RELATION)
				res = new SummaryGraphElement(new ObjectProperty(uri), SummaryGraphElement.RELATION);
		}
		elemPool.put(type+uri, res);
		return res;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length!=1)
		{
			System.err.println("java BuildQ2SemanticService configFilePath(String)");
			return;
		}
		long start = System.currentTimeMillis();
//		load configFile
		try {
			BuildQ2SemanticService.getConfiguation(args[0]);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		build graphs
		SplitSummaryGraphIndexServiceForBTFromNT wawa = new SplitSummaryGraphIndexServiceForBTFromNT();
		try {
			wawa.buildGraphs(BuildQ2SemanticService.indexRoot);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//db index location
		
		long end = System.currentTimeMillis();
		System.out.println("Time customing: "+(end-start)+" ms");
	}

}
