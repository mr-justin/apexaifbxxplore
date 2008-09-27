/**
 * 
 */
package com.ibm.semplore.xir;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import com.ibm.semplore.search.impl.MEMDocStream;

/**
 * @author xrsun
 *
 */
public class TreeSetDocStream implements DocStream {
	TreeSet<Integer> set;
	int pos;
	Iterator<Integer> itr = null;
	Integer doc = null;

	public TreeSetDocStream(TreeSet<Integer> set) {
		this.set = set;
	}

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#clone()
     */
	public Object clone()
    {
		TreeSetDocStream c1 = new TreeSetDocStream(set);
		c1.pos = 0;
        return c1;
    }

    /* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#count()
	 */
	@Override
	public int count() {
		return pos+1;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#doc()
	 */
	@Override
	public int doc() {
		return doc;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#getLen()
	 */
	@Override
	public int getLen() {
		return set.size();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#init()
	 */
	@Override
	public void init() throws IOException {
		this.pos = 0;
		this.itr = set.iterator();
		if (itr.hasNext()) doc = itr.next();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#next()
	 */
	@Override
	public boolean next() throws IOException {
		if (itr.hasNext()){
			doc = itr.next();
			pos ++;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#score()
	 */
	@Override
	public float score() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#skipTo(int)
	 */
	@Override
	public boolean skipTo(int target) throws IOException {
        while (doc() < target) {
            if (!next())
                return false;
        }
        return true;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#skipToIndex(int)
	 */
	@Override
	public boolean skipToIndex(int idx) throws IOException {
		while (pos < idx) {
			if (!next()) return false;
		}
		return true;
	}

}
