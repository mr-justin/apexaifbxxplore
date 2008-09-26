/**
 * 
 */
package com.ibm.semplore.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author xrsun
 * 
 */
public class BufferedRandomAccessFile {
	static final int defaultBufSize = 1024;
	private RandomAccessFile file;
	private int bufLen;
	private int bufSize;
	private byte[] buf;
	private long bufStart;
	private long filePos;

	public BufferedRandomAccessFile(File file, String mode)
			throws FileNotFoundException {
		this(file,mode,defaultBufSize);
	}

	public BufferedRandomAccessFile(File file, String mode, int bufsize)
			throws FileNotFoundException {
		this.file = new RandomAccessFile(file, mode);
		bufLen = bufsize;
		buf = new byte[bufLen];
		bufSize = 0;
		bufStart = Integer.MIN_VALUE;
		filePos = 0;
	}

	public BufferedRandomAccessFile(String string, String mode) throws FileNotFoundException {
		this(new File(string), mode);
	}

	public int readInt() throws IOException {
		if (filePos >=bufStart && filePos+4 <= bufStart + bufSize) {
		} else {
			file.seek(filePos);
			bufStart = filePos;
			bufSize = file.read(buf);
			if (bufSize<4) throw new EOFException();
		}
		int s = (int)(filePos - bufStart);
		filePos += 4;
//		System.out.print(String.format("%d %d %d %d: ", buf[s],buf[s+1],buf[s+2],(128+buf[s+3])+128));
		return (((buf[s]>=0?buf[s]:256+buf[s]) << 24) + ((buf[s+1]>=0?buf[s+1]:256+buf[s+1]) << 16) + ((buf[s+2]>=0?buf[s+2]:256+buf[s+2]) << 8) + ((buf[s+3]>=0?buf[s+3]:256+buf[s+3]) << 0));
	}

	public void seek(long pos) throws IOException {
		filePos = pos;
	}
	
	public void close() throws IOException {
		file.close();
	}

}
