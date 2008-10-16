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

	public static String OBJPROPPOOL = "-objpropPool";
	public static int MAX_HASHED_ELEM_SIZE = 5000000;
	public static double rate = 0.000002;
	public int MAX_CACHE_SIZE = 8000000;
	public TreeMap<String, Integer> predPool;
	public int hits1 = 0, hits2 = 0;
	public boolean cleared = false;
	public BloomFilter bf = new BloomFilter();
	public TreeMap<String, SummaryGraphElement> elemPool;
	
	public void buildGraphs(String path) throws Exception
	{	
		propCount = new TreeMap<String, Integer>();
		conceptCount = new TreeMap<String, Integer>();
		elemPool = new TreeMap<String, SummaryGraphElement>();

		/*************using Lucenemap*******************/
		indiv2con = new LuceneMap();
		splitRelPool = new TreeMap<String, Integer>();
		
		//preparation true = re-create false = dont create
//		firstScan(path, false, false);
		System.gc();
		//split summary graph
		secondScan(path);
		System.gc();

	}
	private void secondScan(String path) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("=======secondScan==========");
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph = new Pseudograph<SummaryGraphElement,SummaryGraphEdge>(SummaryGraphEdge.class);
		indiv2con.openSearcher(path);
		BufferedReader br;
		BufferedReader root = new BufferedReader(new FileReader(path+"-result.txt"));
		indivSize = Integer.parseInt(root.readLine());
		propSize = Integer.parseInt(root.readLine());
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(path+"-conceptCount.obj"));
		conceptCount = (TreeMap<String, Integer>)in.readObject(); 
		in.close();
		cache = new TreeMap<String, Set<String>>();
		String predid;
		int count = 0;
		while((predid = root.readLine())!=null)
		{
			String pred = predid.substring(0, predid.indexOf('\t'));
			String id = predid.substring(predid.indexOf('\t')+1);
			System.out.println((++count)+" "+pred+"\tcache1:"+elemPool.size()+"\thits1:"+hits1+"\tcache2:"+cache.size()+"\thits2:"+hits2);
			if(count%50==0)System.gc();
			TreeMap<String, Integer> triples = new TreeMap<String, Integer>();

			br = new BufferedReader(new FileReader(path+OBJPROPPOOL+File.separator+id));
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
					else hits2++;
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
					else hits2++;
				} catch (Exception e) { e.printStackTrace(); continue; }
				
				if(objParent == null || objParent.size()==0)
				{
					objParent = new TreeSet<String>();
					objParent.add(NamedConcept.TOP.getUri());
				}
				if(!cache.containsKey(obj))
					cache.put(obj, objParent);
				
				while(cache.size()>=MAX_CACHE_SIZE)
					cache.clear();
				for(String str: subjParent)
				{
					if(conceptCount.get(str)!=null && conceptCount.get(str)<(int)(indivSize*rate))
						continue;
					for(String otr: objParent)
					{
						if(conceptCount.get(otr)!=null && conceptCount.get(otr)<(int)(indivSize*rate))
							continue;
						Integer i = triples.get(str+'\t'+otr);
						if(i==null) i = 0;
						triples.put(str+'\t'+otr, i+1);
					}		
				}
				subjParent = null;
				objParent = null;
			}
			br.close();
			System.out.println("search finished! size:"+triples.size());
			int order = 0;
			for(String so: triples.keySet())
			{
				Integer i = triples.get(so);
				if(i<(int)(propSize*rate)) continue;
				String str = so.substring(0, so.indexOf('\t'));
				String otr = so.substring(so.indexOf('\t')+1);
				SummaryGraphElement s = getElemFromUri(str, summaryGraph);
				SummaryGraphElement o = getElemFromUri(otr, summaryGraph);
				SummaryGraphElement p = getElem(pred+"("+order+")", SummaryGraphElement.RELATION, summaryGraph);
				if(i==null) p.setCost(Double.MAX_VALUE);
				else p.setCost(i.doubleValue()/propSize);
				summaryGraph.addVertex(s);
				summaryGraph.addVertex(o);
				summaryGraph.addVertex(p);
	
				SummaryGraphEdge edge1 = new SummaryGraphEdge(s, p, SummaryGraphEdge.DOMAIN_EDGE);
				SummaryGraphEdge edge2 = new SummaryGraphEdge(p, o, SummaryGraphEdge.RANGE_EDGE);
				if(!summaryGraph.containsEdge(edge1))
					summaryGraph.addEdge(s, p, edge1);
				if(!summaryGraph.containsEdge(edge2))
					summaryGraph.addEdge(p, o, edge2);
				order++;
			}
			
			triples.clear();
			triples = null;
			
		}
		root.close();
		System.out.println("write splitted summary graph");
		writeSummaryGraph(summaryGraph, BuildQ2SemanticService.summaryObj+".split");
		writeSummaryGraphAsRDF(summaryGraph, BuildQ2SemanticService.summaryRDF+".split");
		indiv2con.closeSearcher();
		outputGraphInfo(summaryGraph);
	}
	public void firstScan(String path, boolean create, boolean writeRel) throws Exception
	{
		if(create)
			indiv2con.openWriter(path, true);
		System.out.println("=======firstScan==========");
		indivSize = BuildQ2SemanticService.instNumMap.get(BuildQ2SemanticService.datasource)==null?
				-1:BuildQ2SemanticService.instNumMap.get(BuildQ2SemanticService.datasource);
		BufferedReader br = new BufferedReader(new FileReader(BuildQ2SemanticService.source));
		PrintWriter pw;
		if(writeRel)
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
			{
				propSize++;
//				Integer n = propCount.get(pred);
//				if(n==null) n = Integer.valueOf(0);
//				propCount.put(pred, n+1);
			}
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getObjectType(pred, obj).equals(CONCEPT))
			{
				if(create)
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
				if(writeRel)
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
		System.out.println("indivSize: "+indivSize+"\tpropSize: "+propSize);
		if(create)
			indiv2con.closeWriter();
		pw = new PrintWriter(new FileWriter(path+"-result.txt"));
		pw.println(indivSize);
		pw.println(propSize);
		for(String key: predPool.keySet())
			pw.println(key+"\t"+predPool.get(key));
		pw.close();
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path+"-conceptCount.obj"));
		out.writeObject(conceptCount);
		out.close();
	}
	
	public SummaryGraphElement getElemFromUri(String uri, Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph)
	{
		if(uri.equals(NamedConcept.TOP.getUri()))
		{
			SummaryGraphElement elem = getElem(NamedConcept.TOP.getUri(), SummaryGraphElement.CONCEPT, graph);
			elem.setCost(TOP_ELEMENT_SCORE);
			return elem;
		}
		else
		{
			double cost;
			Integer i = conceptCount.get(uri);
			if(i==null) cost = 0;
			else cost = i.doubleValue()/indivSize;
			
			SummaryGraphElement elem = getElem(uri, SummaryGraphElement.CONCEPT, graph);
			elem.setCost(cost);
			return elem;
		}
	}
	
	public SummaryGraphElement getElem(String uri, int type, Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph)
	{
//		int key = bf.hashRabin(type+uri);
		SummaryGraphElement res = elemPool.get(type+uri);
//		if(res == null && cleared)
//		{
//			for(SummaryGraphElement elem : graph.vertexSet())
//				if(elem.getType() == type && SummaryGraphUtil.getResourceUri(elem).equals(uri))
//					res = elem;
//		}
//		else hits1++;
		if(res == null)
		{
			if(type == SummaryGraphElement.CONCEPT)
				res = new SummaryGraphElement(new NamedConcept(uri), SummaryGraphElement.CONCEPT);
			else if(type == SummaryGraphElement.RELATION)
				res = new SummaryGraphElement(new ObjectProperty(uri), SummaryGraphElement.RELATION);
		}
		elemPool.put(type+uri, res);
//		if(elemPool.size()>MAX_HASHED_ELEM_SIZE) {elemPool.clear(); cleared = true;}
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
