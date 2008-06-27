/**
 * 
 */
package sjtu.apex.q2semantic.index;

import sjtu.apex.q2semantic.index.impl.IndexBuilderImpl;

/**
 * @author whfcarter
 *
 */
public class IndexService {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		IndexBuilder tap_ib = new IndexBuilderImpl();
		tap_ib.init(IndexEnvironment.TAP_FLAG);
		tap_ib.index();
		tap_ib.destroy();
		
//		IndexBuilder lubm_ib = new IndexBuilderImpl();
//		lubm_ib.init(IndexEnvironment.LUBM_FLAG);
//		lubm_ib.index();
//		lubm_ib.destroy();
		
//		IndexBuilder dblp_ib = new IndexBuilderImpl();
//		dblp_ib.init(IndexEnvironment.DBLP_FLAG);
//		dblp_ib.index();
//		dblp_ib.destroy();
		
//		try {
//			IndexReader reader = IndexReader.open("tap_kb_index");
//			System.out.println(reader.maxDoc());
//			reader.close();
//		} catch (CorruptIndexException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
