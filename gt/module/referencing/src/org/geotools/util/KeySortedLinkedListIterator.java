package org.geotools.util;

/**
 * Iterator for {@link KeySortedLinkedList}.
 *
 * Note: this class may change in the future, or by replaced by
 *       a {@link java.util.TreeMap}-based solution.
 *
 * @author Simone Giannecchini
 */
public class KeySortedLinkedListIterator {
    KeySortedListNode current;

    KeySortedLinkedListIterator(KeySortedListNode theNode) {
        current = theNode;
    }

    public boolean isPastEnd() {
        return (current == null);
    }

    public Object retrieve() {
        return isPastEnd() ? null : current.element;
    }

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
