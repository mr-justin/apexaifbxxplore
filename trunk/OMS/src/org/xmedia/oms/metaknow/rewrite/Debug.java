package org.xmedia.oms.metaknow.rewrite;

/**
 * Guerilla tactics for few special cases.
 * 
 * 
 * @author bernie_2
 *
 */
class Debug {
        public static boolean on = true;

	public static void print( Object o ) {
		if (on) System.out.println(o);
	}
}