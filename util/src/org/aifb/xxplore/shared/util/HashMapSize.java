package org.aifb.xxplore.shared.util;


/**
 * This interface defines several constants for initially sizing hash tables for different
 * purposes.
 *
 */
public interface HashMapSize {
    /**
     * <code>TINY</code> defines the size for a hash map that will hold one or two elements.
     * This small hash tables may be usefull for algorithmic purposes.
     */
    public static final int TINY = 3;

    /**
     * <code>SMALL</code> defines the size for a hash map that will hold up to 5 elements.
     * This small hash tables may be usefull for algorithmic purposes.
     */
    public static final int SMALL = 7;

    /**
     * <code>COMPACT</code> defines the size for a hash map that will hold up to 15 elements.
     */
    public static final int COMPACT = 19;

    /**
     * <code>BIG</code> defines the size for a hash map that will hold up to 40 elements.
     */
    public static final int BIG = 47;

    /**
     * <code>LARGE</code> defines the size for a hash map that will hold up to 75 elements.
     */
    public static final int LARGE = 89;

}
