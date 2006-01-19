package org.geotools.util;

/**
 * Iterator for {@link KeySortedLinkedList}.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 *
 * @deprecated Replaced by the standard J2SE {@link java.util.Iterator}.
 */
public class KeySortedLinkedListIterator {
    KeySortedListNode current;

    /**
     * @deprecated
     */
    KeySortedLinkedListIterator(KeySortedListNode theNode) {
        current = theNode;
    }

    /**
     * @deprecated Replaced by <code>!{@linkplain java.util.Iterator#hasNext}</code>.
     */
    public boolean isPastEnd() {
        return (current == null);
    }

    /**
     * @deprecated Replaced by {@link java.util.Iterator#next}.
     */
    public Object retrieve() {
        return isPastEnd() ? null : current.element;
    }

    /**
     * @deprecated Replaced by {@link java.util.Iterator#next}.
     */
    public void advance() {
        if (!isPastEnd()) {
            current = current.next;
        }
    }

    public String toString() {
        if (!isPastEnd()) {
            return "&" + current.toString();
        } else {
            return "&null";
        }
    }
}
