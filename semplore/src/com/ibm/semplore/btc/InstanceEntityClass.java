package com.ibm.semplore.btc;

import com.ibm.semplore.util.TestUnicode;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


/**
 * EntityClass used in SnippetDB
 * 
 * @author xrsun
 *
 */
@Entity
public class InstanceEntityClass {
	// Primary key is pKey
	@PrimaryKey
	private String uri;
	private String data = "";

	public void setPKey(String data) {
		uri = data;
	}

	public String getPKey() {
		return uri;
	}
	
	public void addSnippet(String s) {
		data += s + "\n";
	}
	
	public String getData() {
		return TestUnicode.parse(data);
	}
}