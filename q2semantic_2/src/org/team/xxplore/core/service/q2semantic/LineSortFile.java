package org.team.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class LineSortFile extends File {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final boolean DEBUG = true;
	
	private String fileName;
	
	private int maxSingleFileSizeInKB = 256 * 1024;
	private int maxMergeNumberOfFile = 20;
	
	private boolean deleteTempFileAfterFinish = true;
	private boolean deleteWhenStringRepeated = false;
	
	private static int bufSize = 256*1024;
	private static byte [] buf = new byte[bufSize];
	
	public LineSortFile(String fileName) {
		super(fileName);
		this.fileName = fileName;
	}
	
	/**
	 * Get the file name of current file.
	 * @return current file full name.
	 */
	public String getFileFullName() {
		return fileName;
	}
	
	/**
	 * Set maxSingleFileSizeInKB value.
	 * 
	 * @param maxSingleFileSizeInKB
	 *            Which used in departFile method. It decide the departed files'
	 *            size.
	 */
	public void setMaxSingleFileSizeInKB(int maxSingleFileSizeInKB) {
		this.maxSingleFileSizeInKB = maxSingleFileSizeInKB;
	}
	
	/**
	 * Set maxMergeNumberOfFile value.
	 * 
	 * @param maxMergeNumberOfFile
	 *            Which used in mergeSort method. It decide how many files merge
	 *            together one time.
	 */
	public void setMaxMergeNumberOfFile(int maxMergeNumberOfFile) {
		this.maxMergeNumberOfFile = maxMergeNumberOfFile;
	}
	
	/**
	 * Set deleteTempFileAfterFinish.
	 * 
	 * @param deleteTempFileAfterFinish
	 *            Which decide whether delete all the temperate files generated
	 *            by methods.
	 */
	public void setDeleteTempFileAfterFinish(boolean deleteTempFileAfterFinish) {
		this.deleteTempFileAfterFinish = deleteTempFileAfterFinish;
	}
	
	/**
	 * Set deleteWhenStringRepeated.
	 * 
	 * @param deleteWhenStringRepeated
	 *            Which decide whether delete the same string copies in the
	 *            result file.
	 */
	public void setDeleteWhenStringRepeated(boolean deleteWhenStringRepeated) {
		this.deleteWhenStringRepeated = deleteWhenStringRepeated;
	}
	
	/**
	 * It's a private method, which sort a small file fully in the memory, so it
	 * cann't sort too big file.
	 * 
	 * @param sortFileName
	 *            The file name which will be sorted.
	 * @throws FileNotFoundException
	 *             When the file doesn't exists, throw this exception.
	 * @throws IOException
	 *             When the file cann't read or some other wrong, throw this
	 *             exception.
	 */
	private void sortSingleFile(String sortFileName) throws FileNotFoundException, IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(sortFileName)));
		for (String line = null; (line = reader.readLine()) != null;)
			lines.add(line);
		reader.close();
		Collections.sort(lines);
		PrintStream pout = new PrintStream(sortFileName);
		String lst = null;
		for (String line:lines) {
			if (!deleteWhenStringRepeated || lst==null || !lst.equals(line))
				pout.println(line);
			lst = line;
		}
		pout.close();
	}
	
	/**
	 * Sort current file. If deleteTempFileAfterFinish is true, the delete all
	 * the temperate files. If deleteWhenStringRepeated is true, the remove all
	 * the same string, only left one copy.
	 * 
	 * @throws IOException
	 */
	public void sortFile() throws IOException {
		{
			if (!this.exists())
				throw new IOException("File not exists.");
		}
		
		boolean fileIsBig = new FileInputStream(fileName).available() > maxSingleFileSizeInKB * 1024;

		if (fileIsBig) {
			List<String> subFiles = departFile();
			for (String file : subFiles)
				sortSingleFile(file);
			mergeSort(subFiles, fileName);
			if (deleteTempFileAfterFinish)
				deleteFiles(subFiles);
		} else {
			sortSingleFile(fileName);
		}
	}
	
	/**
	 * Split a file with satisfied char set. And one part should not exceed 1024KB.  
	 * @param splitSet
	 * The set of split the file. Often is "\n".
	 * @return
	 * A iterable<String>, which could get a iterator to get all the split string.
	 * @throws IOException
	 * Which happened when start read the file.
	 */
	public Iterable<String> splitWith(String splitSet) throws IOException {
	    final InputStream fin = new FileInputStream(this);
	    final String splitString = splitSet;
	    
	    final Iterator<String> lineIter = new Iterator<String>() {
			String line = null;
			byte buffer[] = new byte[1024*1024];
			int off = 0, len = 0;

			public boolean hasNext() {
				if (line == null && off <= len) {
					int nxtOff = off;
					while (nxtOff<len) {
						if (splitString.indexOf(buffer[nxtOff]) != -1)
							break;
						nxtOff++;
					}
					if (nxtOff >= len) {
						if (off!=0) {
							for (int i = off; i<len; i++)
								buffer[i-off] = buffer[i];
							len -= off;
							off = 0;
						}
						int tlen = 0;
						try {
							tlen = fin.read(buffer, len, buffer.length-len);
						} catch (IOException e) {
						}
						if (tlen<0) tlen = 0;
						len += tlen;
						nxtOff = off;
						while (nxtOff<len) {
							if (splitString.indexOf(buffer[nxtOff]) != -1)
								break;
							nxtOff++;
						}
					}
					if (off < len) {
						line = new String(buffer, off, nxtOff-off);
						off = nxtOff + 1;
					}
				}
				return line != null;
			}

			public String next() {
				String result = line;
				line = null;
				return result;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return lineIter;
			}
		};
	}
	
	/**
	 * A static method is used to delete a list of files.
	 * @param files The files to be deleted.
	 */
	public static void deleteFiles(List<String> files) {
		System.gc();
		for (String file:files) {
			File f = new File(file);
			if(!f.delete())
				f.deleteOnExit();
		}
	}
	
	/**
	 * This method depart one big file to several small files, which size don't exceed maxSingleFileSizeInKB KB.
	 * @return the list of the departed file names.
	 * @throws IOException
	 */
	public List<String> departFile() throws IOException {
		{
			if (!this.exists()) throw new IOException("File not exists.");
		}
		
		List<String> result = new ArrayList<String>();
		
		String currentOutputFile = fileName + ".p1.part";
		FileInputStream fin = new FileInputStream(this);
		FileOutputStream fout = new FileOutputStream(currentOutputFile);
		result.add(currentOutputFile);
		
		long startTime = new Date().getTime();
		long finishTime = 0;
		int totalSize = fin.available();
		
		int len = 0;
		while (true) {
			if (len < bufSize) {
				int tlen = fin.read(buf, len, bufSize - len);
				if (tlen>0) len += tlen;
			}
			if (len == 0) break;
			
			boolean fileFull = new FileInputStream(currentOutputFile).available() + len*2>maxSingleFileSizeInKB * 1024;
			
			if (fileFull) {
				int lst = len-1;
				while (lst>=0 && buf[lst] != 10) lst --;
				lst++;
				if (lst<=0) lst = len;
				fout.write(buf, 0, lst);
				fout.flush();
				for (int i = lst; i<len; i++)
					buf[i-lst] = buf[i];
				len -= lst;
				fout.close();
				currentOutputFile = fileName + ".p" + (result.size()+1) + ".part";
				result.add(currentOutputFile);
				fout = new FileOutputStream(currentOutputFile);
			} else {
				fout.write(buf, 0, len);
				fout.flush();
				len = 0;
			}
		}
		finishTime = new Date().getTime();
		if (DEBUG) {
			System.out.println("time used: " + (finishTime-startTime) + " ms.");
			System.out.println("average speed: " + (1.0*totalSize/(finishTime-startTime)/1024) + " MB/s.");
		}
		return result;
	}
	
	/**
	 * Merge files to a single file.
	 * @param files the file list which will be merged.
	 * @param outputFile The output file name.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void mergeSort(List<String> files, String outputFile) throws FileNotFoundException, IOException{
		
		if (files.size() > maxMergeNumberOfFile) {
			for (int processStart = 0; processStart <files.size(); processStart += maxMergeNumberOfFile) {
				String tmpOutputFile = null;
				if (files.size()-processStart > maxMergeNumberOfFile)
					tmpOutputFile = fileName + "." + (files.size()+1) + ".part";
				else
					tmpOutputFile = outputFile;

				List<String> processFiles = new ArrayList<String>();
				for (int i = 0; i < maxMergeNumberOfFile && processStart + i < files.size(); i++)
					processFiles.add(files.get(processStart + i));
				mergeSort(processFiles, tmpOutputFile);
				if (tmpOutputFile.equals(outputFile))
					break;
				else
					files.add(tmpOutputFile);
			}
		} else {
			BufferedReader readers[] = new BufferedReader[files.size()];
			for (int i = 0; i<files.size(); i++)
				readers[i] = new BufferedReader(new InputStreamReader(new FileInputStream(files.get(i))));
			String buf[] = new String[files.size()];
			for (int i = 0; i<files.size(); i++)
				buf[i] = readers[i].readLine();
			PrintStream pout = new PrintStream(outputFile);
			long size = 0, outTime = 0;
			while (true) {
				String currentWord = null;
				for (int i = 0; i<files.size(); i++) {
					if (currentWord == null || buf[i]!=null && buf[i].compareTo(currentWord)<0)
						currentWord = buf[i];
				}
				if (currentWord == null) break;
				if (deleteWhenStringRepeated)  {
					pout.println(currentWord);
					size += currentWord.length()+2;
					if (++outTime%100==0)
						System.out.println(size/1024/1024 + "MB");
				}
				for (int i = 0; i<files.size(); i++) {
					while (buf[i]!=null && currentWord.equals(buf[i])) {
						if (!deleteWhenStringRepeated) pout.println(currentWord);
						buf[i] = readers[i].readLine();
					}
				}
			}
		}
	}

	/**
	 * Copy current file to another place. 
	 * @param toCopyFile The file name. 
	 * @param cover If the file already exists, whether we will cover it.
	 * @throws IOException
	 */
	public void copyTo(String toCopyFile, boolean cover) throws IOException {
		if (!this.exists()) {
			throw new IOException("File not exists.");
		}

		File output = new File(toCopyFile);
		
		if (output.exists() && !cover) {
			throw new IOException("File already exists.");
		}
		
		long startCopyTime = new Date().getTime();
		long finishCopyTime = new Date().getTime();
		long totalBytes = 0;
//		long partTime1 = 0;
		long partTime2 = 0;
		
		FileInputStream fin = new FileInputStream(this);
		FileOutputStream fout = new FileOutputStream(output);
		int len;
		int outTime = 0;
		while (true) {
//			partTime1 = new Date().getTime();
			
			len = fin.read(buf);
			if (len<=0) break;
			fout.write(buf, 0, len);
			fout.flush();
			
			partTime2 = new Date().getTime();
			totalBytes += len;
			if (DEBUG) {
				if (++outTime%100==0) {
					System.out.println("already copy " + totalBytes + " bytes.");
					System.out.println("time used " + (partTime2 - startCopyTime) + " ms.");
					double averageSpeed = ((1.0)*totalBytes)/(partTime2-startCopyTime)/1024;
					System.out.println("average speed " + averageSpeed + " M/s");
				}
			}
		}
		fin.close();
		fout.close();
		finishCopyTime = new Date().getTime();
		if (DEBUG) {
			System.out.println("total use " + (finishCopyTime - startCopyTime) + " ms.");
			System.out.println("average speed " + ((1.0)*totalBytes)/(finishCopyTime - startCopyTime)/1024 + " M/s");
		}
	}

	/**
	 * This method could save a copy of current file with name filename.bak .
	 * @throws IOException
	 */
	public void saveCopy() throws IOException {
		copyTo(getFileFullName() + ".bak", true);
	}











}