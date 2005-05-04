package org.geotools.util;

public class KeySortedLinkedList {
    KeySortedListNode header;

    public KeySortedLinkedList() {
        header = new KeySortedListNode(null, null);
    }

    /**
     * Is the list empty?
     *
     * @return true if the list is empty flase otherwise.
     */
    public boolean isEmpty() {
        return header.next == null;
    }

    /**
     * Retrieves an iterator that points to the first element or to null for an
     * empty list.
     *
     * @return
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
     * @param x
     *
     * @return
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

    public KeySortedLinkedListIterator find(Comparable x) {
        KeySortedListNode itr = header.next;

        while ((itr != null) && (itr.key.compareTo(x) < 0))
            itr = itr.next;

        return new KeySortedLinkedListIterator(itr);
    }

    public KeySortedLinkedListIterator findPrevious(Comparable x) {
        KeySortedListNode itr = header;

        while ((itr.next != null) && (itr.next.element != null)
                && (itr.next.key.compareTo(x) < 0))
            itr = itr.next;

        return new KeySortedLinkedListIterator(itr);
    }

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
     * @param index
     *
     * @return
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
