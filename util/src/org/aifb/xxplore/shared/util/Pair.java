package org.aifb.xxplore.shared.util;

import java.io.Serializable;

/**
 * <code>Pair</code> is a helper class for the frequent case, when two objects must
 * be combined for a collection entry or even a key. The concept of a pair is fundamental
 * for all languages derived from LISP and is even useful in a language like Java.
 *
 * <p>Note: <code>Pair</code>s may be used as keys for a hash collection, when both
 * elements, named head and tail, implements the hashing protocol.
 *
 */
public final class Pair implements Serializable {
    private Object head;
    private Object tail;

    /**
     * Constructs a <code>Pair</code> using the two given objects as head and tail.
     *
     * @param   head    the object to be used as the head of the pair
     * @param   tail    the object to be used as the tail of the pair
     */
    public Pair(Object head, Object tail) {
        this.head = head;
        this.tail = tail;
    }

    /**
     * Returns the head object of the pair.
     *
     * @return  the head object of the pair
     * @see     #setHead
     * @see     #getTail
     */
    public final Object getHead() {
        return head;
    }

    /**
     * Modifies the head object of the pair.
     *
     * @param   newHead the new head object of the pair
     * @see     #getHead
     */
    public final void setHead(Object newHead) {
        head = newHead;
    }

    /**
     * Returns the tail object of the pair.
     *
     * @return  the tail object of the pair
     * @see     #setTail
     * @see     #getHead
     */
    public final Object getTail() {
        return tail;
    }

    /**
     * Modifies the tail object of the pair.
     *
     * @param   newTail new tail object of the pair
     * @see     #getTail
     */
    public final void setTail(Object newTail) {
        tail = newTail;
    }

    public final boolean equals(Object object) {
        if (object == this)
            return true;
        else if ((object != null) && (object.getClass() == getClass())) {
            Pair pair = (Pair)object;

            if (head == null) {
                if (pair.head != null)
                    return false;
            }
            else if (!head.equals(pair.head))
                return false;

            if (tail == null) {
                if (pair.tail != null)
                    return false;
            }
            else if (!tail.equals(pair.tail))
                return false;

            return true;
        }
        else
            return false;
    }

    public final int hashCode() {
        return ((head == null) ? 0 : head.hashCode()) + ((tail == null) ? 0 : tail.hashCode());
    }
}
