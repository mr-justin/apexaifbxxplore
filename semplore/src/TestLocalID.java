import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;


public class TestLocalID {


	static int findInFile(String filename, long rel) {
		RandomAccessFile file = null; 
		try {
			// System.out.println(filename);
			file = new RandomAccessFile(filename, "r");
			int a = 0;
			int b = (int) (file.length() >> 3) - 1;
			int c;
			long cc;
			while (a <= b) {
				c = (a + b) / 2;
				file.seek(c << 3); // each long 8 bytes. 64bit.
				cc = file.readLong();
				if (rel < cc) {
					b = c - 1;
					continue;
				}
				if (rel > cc) {
					a = c + 1;
					continue;
				}
				return c;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                }
            }
        }
		return -1;
	}

	
	public static void main(String[] args) throws IOException {
		String ftext = "D:\\User\\liuqiaoling\\semplore\\data\\dbpedia\\DataForIndex\\relationTemp";
		String fbin = "D:\\User\\liuqiaoling\\semplore\\data\\dbpedia\\DataForIndex\\relation";
		BufferedReader fin = new BufferedReader(new FileReader(ftext));
		String line;
		while ((line=fin.readLine())!=null) {
			System.out.println(findInFile(fbin, Long.valueOf(line)));
		}
	}

}
