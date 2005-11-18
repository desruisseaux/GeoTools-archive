package org.geotools.util;

/**
 * List of elements sorted by a key which is not the element itself.
 *
 * @version $Id$
 * @author Simone Giannecchini
 *
 * @deprecated Replaced by {@link KeySortedList}. The new implementation fits better in the Java
 *             Collection framework, and is also built on top of {@link java.util.TreeMap}, which
 *             should provides O(log(n)) performance instead of O(n) for most operations.
 */
public class KeySortedLinkedList {
    KeySortedListNode header;

    /**
     * @deprecated Replaced by {@link KeySortedList}.
     */
    public KeySortedLinkedList() {
        header = new KeySortedListNode(null, null);
    }

    /**
     * Is the list empty?
     *
     * @return true if the list is empty flase otherwise.
     *
     * @deprecated Replaced by {@link KeySortedList#isEmpty}.
     */
    public boolean isEmpty() {
        return header.next == null;
    }

    /**
     * Retrieves an iterator that points to the first element or to null for an
     * empty list.
     *
     * @deprecated Replaced by {@link KeySortedList#iterator}.
     */
    public KeySortedLinkedListIterator first() {
        return new KeySortedLinkedListIterator(header.next);
    }

    /**
     * Inserting an element in the list by using a key.  In case we already
     * have such a key WE DO NOT replace the old value but we add a  new one
     * with the same key!
     *
     * @param x Key to be used to find the right location.
     * @param o Object to be inserted.
     *
     * @deprecated Replaced by {@link KeySortedList#add}. The new method name is {@code add}
     *             instead of {@code insert} for consistency with other {@code add} methods
     *             in the {@link List} interface.
     */
    public void insert(Comparable x, Object o) {
        KeySortedLinkedListIterator location = this.findPrevious(x);
        boolean success;

        //insert at the end of all the elements which might be equals to this one
        while (!location.isPastEnd()) {
            if ((location.current == header) || (location.current.next == null)
                    || (location.current.next.element == null)
                    || (location.current.next.key.compareTo(x) != 0)) {
                location.current.next = new KeySortedListNode(x, o,location.current.next);
                break;
            }
            location.advance();
        }
    }

    /**
     * Checks if the list contains a certain element, or better its key.
     *
     * @deprecated Replaced by {@link KeySortedList#containsKey}.
     */
    public boolean contains(Comparable x) {
        KeySortedListNode itr = header.next;
        boolean found = false;

        while ((itr != null) && (found == false)) {
            found = (itr.key.compareTo(x) == 0);
            itr = itr.next;
        }

        return found;
    }

    /**
     * @deprecated Replaced by {@link KeySortedList#toString}.
     */
    public String toString() {
        KeySortedListNode itr = header.next;
        String s = new String();
        s += "(";

        while (itr.next != null) {
            s += (itr.element.toString() + ",");
            itr = itr.next;
        }

        s += (itr.element.toString() + ")");

        return s;
    }

    /**
     * @deprecated Replaced by {@link KeySortedList#listIterator(Comparable)}, or
     *             {@link KeySortedList#first} if only a single value is wanted.
     */
    public KeySortedLinkedListIterator find(Comparable x) {
        KeySortedListNode itr = header.next;

        while ((itr != null) && (itr.key.compareTo(x) < 0))
            itr = itr.next;

        return new KeySortedLinkedListIterator(itr);
    }

    /**
     * @deprecated Replaced by {@link KeySortedList#listIterator(Comparable)} folllowed by
     *             calls to {@link java.util.ListIterator#previous}.
     */
    public KeySortedLinkedListIterator findPrevious(Comparable x) {
        KeySortedListNode itr = header;

        while ((itr.next != null) && (itr.next.element != null)
                && (itr.next.key.compareTo(x) < 0))
            itr = itr.next;

        return new KeySortedLinkedListIterator(itr);
    }

    /**
     * @deprecated Replaced by {@link KeySortedList#removeAll}.
     */
    public void remove(Comparable x) {
        if (this.isEmpty()) {
            return;
        }

        KeySortedLinkedListIterator p = findPrevious(x);

        if (p.current.next != null) {
            p.current.next = p.current.next.next;
        }
    }

    /**
     * Returns the object stored at a determined zero-based index in case it
     * exists, null otherwise.
     *
     * @deprecated Replaced by {@link KeySortedList#get}.
     */
    public KeySortedLinkedListIterator getAt(int index) {
        //index check
        if (index < 0) {
            return null;
        }

        //getting an iterator
        KeySortedLinkedListIterator it = this.first();
        int i = 0;

        while (!it.isPastEnd()) {
            //did we find it?
            if (i++ == index) {
                break;
            }

            //NO
            it.advance();
        }

        return it;
    }
}
