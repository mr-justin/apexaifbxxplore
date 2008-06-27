package sjtu.apex.q2semantic.index;

public class IndexEnvironment {
	//summary graph location for each dataset
	public static String TAP_OBJ_PATH = "graph/TAP.inf";
	public static String LUBM_OBJ_PATH = "graph/LUBM.inf";
	public static String DBLP_OBJ_PATH = "graph/DBLP.inf";
	public static String TAP_FREQ = "freq/tap.txt";
	public static String LUBM_FREQ = "freq/lubm.txt";
	public static String DBLP_FREQ = "freq/dblp.txt";
	
	//lucene indices
	public static String SYN_INDEX = "syn_index";
	public static String TAP_KB_INDEX = "tap_kb_index";
	public static String LUBM_KB_INDEX = "lubm_kb_index";
	public static String DBLP_KB_INDEX = "dblp_kb_index";
	
	//sesame native stores
	public static String TAP_REPO = "repository/tap";
	public static String LUBM_REPO = "repository/lubm";
	public static String DBLP_REPO = "repository/dblp";
	public static String INDICES = "spoc,posc";
	
	public static String CONCEPT = "concept";
	public static String OBJECTPROPERTY = "objectproperty";
	public static String DATAPROPERTY = "dataproperty";
	public static String INDIVIDUAL = "indiviudal";
	public static String LITERAL = "literal";
	
	public static int TAP_FLAG = 0;
	public static int LUBM_FLAG = 1;
	public static int DBLP_FLAG = 2;
	
	public static String TAP_BASEURI = "http://tap.stanford.edu/kb/";
	public static String TAP_NS = "http://tap.stanford.edu/tap#";
	public static String LUBM_NS = "http://www.example.com/#";
	public static String LUBM_BASEURI = "http://www.example.com/#";
}
