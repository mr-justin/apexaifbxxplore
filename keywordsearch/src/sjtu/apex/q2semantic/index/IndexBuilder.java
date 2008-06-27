/**
 * 
 */
package sjtu.apex.q2semantic.index;


/**
 * @author whfcarter
 *
 */
public interface IndexBuilder {
	public void init(int flag);
	public void destroy();
	public void index();
}
