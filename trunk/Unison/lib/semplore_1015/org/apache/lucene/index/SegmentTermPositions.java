package org.apache.lucene.index;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */

import java.io.IOException;

import org.apache.lucene.store.IndexInput;

import com.ibm.semplore.xir.DocPositionStream;

final class SegmentTermPositions
extends SegmentTermDocs implements TermPositions,DocPositionStream {
  private IndexInput proxStream;
  private int proxCount;
  private int position;
  
  SegmentTermPositions(SegmentReader p) {
    super(p);
    this.proxStream = (IndexInput)parent.proxStream.clone();
  }

  public void init() throws IOException {//added by lql
	  proxCount = 0;
      next();
  }
  
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {//added by lql
      SegmentTermPositions cl = null;       
      try {
          cl = new SegmentTermPositions(parent);
          cl.seek(termInfo);
      } catch (IOException e) {
          e.printStackTrace();
      }
      return cl;
    }

  final void seek(TermInfo ti) throws IOException {
    super.seek(ti);
    if (ti != null)
      proxStream.seek(ti.proxPointer);
    proxCount = 0;
  }

  public final void close() throws IOException {
    super.close();
    proxStream.close();
  }

  public final int nextPosition() throws IOException {
    proxCount--;
    return position += proxStream.readVInt();
  }

  protected final void skippingDoc() throws IOException {
    for (int f = freq; f > 0; f--)		  // skip all positions
      proxStream.readVInt();
  }

  public final boolean next() throws IOException {
    for (int f = proxCount; f > 0; f--)		  // skip unread positions
      proxStream.readVInt();

    if (super.next()) {				  // run super
      proxCount = freq;				  // note frequency
      position = 0;				  // reset position
      return true;
    }
    return false;
  }

  public final int read(final int[] docs, final int[] freqs) {
    throw new UnsupportedOperationException("TermPositions does not support processing multiple documents in one call. Use TermDocs instead.");
  }


  /** Called by super.skipTo(). */
  protected void skipProx(long proxPointer) throws IOException {
    proxStream.seek(proxPointer);
    proxCount = 0;
  }

  /* My Code Start */
  
  public int genPositionLen() {
		return freq;
  }
  
  public boolean skipPositionTo(int target) throws IOException{
	  while(position<target){ 		  
		  if(proxCount==0)
			  return false;
		  nextPosition();
	  }
	  return true;
  }

	public boolean hasNextPosition() {
		return proxCount>0;
	}

	public int getPosition() {
		return position;
	}

	public byte[] getPayload(byte[] data, int offset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPayloadLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isPayloadAvailable() {
		// TODO Auto-generated method stub
		return false;
	}
  
  /* My Code End */
}
