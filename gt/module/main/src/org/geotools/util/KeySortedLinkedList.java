package org.geotools.util;

public class KeySortedLinkedList {
    KeySortedListNode header;

    public KeySortedLinkedList() {
        header = new KeySortedListNode(null, null);
    }

    public boolean isEmpty() {
        return header.next == null;
    }

    public KeySortedLinkedListIterator first() {
        return new KeySortedLinkedListIterator(header.next);
    }

    public void insert(Comparable x, Object o) {
        KeySortedLinkedListIterator location = this.findPrevious(x);
        boolean success;

        if ((location.current.next == null) ||
                (location.current.next.element == null) ||
                (location.current.next.key.compareTo(x) != 0)) {
            location.current.next = new KeySortedListNode(x, o,
                    location.current.next);
        }
    }

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

        while ((itr.next != null) && (itr.next.element != null) &&
                (itr.next.key.compareTo(x) < 0))
            itr = itr.next;

        return new KeySortedLinkedListIterator(itr);
    }

    public void remove(Comparable x) {
        KeySortedLinkedListIterator p = findPrevious(x);

        if (p.current.next != null) {
            p.current.next = p.current.next.next;
        }
    }
}
