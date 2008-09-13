import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import com.ibm.semplore.util.Md5_BloomFilter_64bit;


public class GenHash {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		System.out.println("Initializing Dictionary");
		BufferedReader fin = new BufferedReader(new FileReader(args[0]));
		String line;
		while ((line = fin.readLine())!=null) {
			System.out.println(line+"\t"+Md5_BloomFilter_64bit.URItoID(line));
		}
	}

}
