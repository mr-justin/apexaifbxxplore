package basic;

/**
 * Used to extract the local name from a Freebase URI
 * @author Linyun Fu
 *
 */
public class FreebaseLocalNameExtractor implements ILocalNameExtractor {

	@Override
	public String getLocalName(String uri) {
		String prefix = "<http://www.freebase.com/resource/";
		int pos = uri.indexOf("/", prefix.length());
		return uri.substring(prefix.length(), pos);

	}

}
