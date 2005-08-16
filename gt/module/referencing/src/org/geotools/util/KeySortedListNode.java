package org.geotools.util;


class KeySortedListNode implements Comparable {
    Object element;
    Comparable key;
    KeySortedListNode next;

    KeySortedListNode(Comparable theKey, Object theElement) {
        this(theKey, theElement, null);
    }

    KeySortedListNode(Comparable theKey, Object theElement, KeySortedListNode n) {
        element = theElement;
        key = theKey; //this should implement comparable		
        next = n;
    }

    // required by Comparable interface
    public int compareTo(Object theKey) {
        return key.compareTo(theKey);
    }

    // handy, but storage object must have toString (pretty common)
    public String toString() {
        return element.toString();
    }
}
