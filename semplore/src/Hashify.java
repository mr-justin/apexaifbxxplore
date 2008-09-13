import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import com.ibm.semplore.util.Md5_BloomFilter_64bit;

/**
 * @author xrsun
 *
 */
public class Hashify {

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		BufferedReader fin  = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line=fin.readLine())!=null) {
			StringTokenizer tok = new StringTokenizer(line);
			boolean first=true;
			while (tok.hasMoreTokens()) {
				if (first) first=false;
				else System.out.print("\t");
				System.out.print(Md5_BloomFilter_64bit.URItoID(tok.nextToken()));
			}
			System.out.println();
		}
	}

}
