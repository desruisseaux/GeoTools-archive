/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.event;

/**
 * A List to hold objects being tracked by the GTEvent system, note this list
 * is event aware, and will fire off events to a getParent.
 * 
 * <p>
 * Special attention is paid to children that are GTComponents (they will have
 * setParent called). This list is not limited to GTComponent children, a
 * native Java type like Color will do just fine.
 * </p>
 * 
 * <p>
 * This list is used to maintain list or array properties in implementations
 * such as FeatureTypeStyle.
 * </p>
 * @source $URL$
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class GTList extends ArrayList implements List {
    private static final long serialVersionUID = -4849245752797538846L;
    // private List delegate; // TODO use a list delegate
    private GTComponent host;
    private String notificationName;

    /**
     * Package visiable constructor for test purposes
     */
    GTList() {
        this(GTRoot.NO_PARENT, "");
    }

    /**
     * Client code must construct with a GTComponent parent (in order to fire
     * events).
     *
     * @param host Host for this list
     * @param listName DOCUMENT ME!
     */
    public GTList(GTComponent host, String listName) {
        this.host = host;
        this.notificationName = listName;
    }

    /**
     * Indicate that the range has been added.
     * 
     * <p>
     * Performs all the book keeping but does not actually add, or fire
     * notifications.
     * </p>
     *
     * @param fromIndex DOCUMENT ME!
     * @param toIndex DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private List deltaAdded(int fromIndex, int toIndex) {
        int position = fromIndex;
        List range = subList(fromIndex, toIndex);
        List added = new ArrayList(range.size());

        for (Iterator i = range.iterator(); i.hasNext(); position++) {
            Object item = i.next();
            added.add(deltaAdded(position, item));
        }

        return added;
    }

    private GTDelta deltaAdded(int position, Object item) {
        if (item instanceof GTComponent) {
            GTComponent myChild = (GTComponent) item;
            myChild.setNote( note( position) );
        }

        GTDelta delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    position), GTDelta.Kind.ADDED, item, null);

        return delta;
    }

    protected GTNote note( int position ){
    	return new GTNoteImpl( host, notificationName, position );
    }
    /**
     * Indicate that the range has been moved
     * 
     * <p>
     * Performs all the book keeping but does not actually move, or fire
     * notifications.
     * </p>
     *
     * @param fromIndex DOCUMENT ME!
     * @param toIndex DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private List deltaMove(int fromIndex, int toIndex) {
        int position = fromIndex;
        List rest = subList(fromIndex, toIndex);
        List moved = new ArrayList(rest.size());

        for (Iterator i = rest.iterator(); i.hasNext(); position++) {
            Object item = i.next();
            moved.add(deltaSync(position, item));
        }

        return moved;
    }

    private GTDelta deltaSync(int position, Object item) {
        if (item instanceof GTComponent) {
            GTComponent myChild = (GTComponent) item;            
            myChild.setNote( GTNote.EMPTY );            
            myChild.setNote( note( position ) );            
        }
        GTDelta delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    position), GTDelta.Kind.NO_CHANGE, item, null);

        return delta;
    }

    /**
     * Indicate that the range has been removed.
     * 
     * <p>
     * Performs all the book keeping but does not actually remove, or fire
     * notifications.
     * </p>
     *
     * @param fromIndex DOCUMENT ME!
     * @param toIndex DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private List deltaRemoved(int fromIndex, int toIndex) {
        int position = fromIndex;
        List rest = subList(fromIndex, toIndex);
        List removed = new ArrayList(rest.size());

        for (Iterator i = rest.iterator(); i.hasNext(); position++) {
            Object item = i.next();
            removed.add(deltaRemoved(position, item));
        }

        return removed;
    }

    private List deltaRemoved(Collection collection) {
        List removed = new ArrayList(collection.size());

        for (Iterator i = collection.iterator(); i.hasNext();) {
            Object item = i.next();
            int position = indexOf(item);
            removed.add(deltaRemoved(position, item));
        }

        return removed;
    }

    private GTDelta deltaRemoved(int position, Object item) {
        if (item instanceof GTComponent) {
            GTComponent myChild = (GTComponent) item;
            myChild.setNote( GTNote.EMPTY );            
        }
        GTDelta delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    position), GTDelta.Kind.REMOVED, null, item);

        return delta;
    }

    public boolean add(Object item) {
        boolean added = super.add(item);

        GTDelta delta;
        delta = deltaAdded(size() - 1, item);
        delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    GTDelta.NO_INDEX), GTDelta.Kind.CHANGED, host, delta);
        host.getNote().getParent().changed(delta);

        return added;
    }

    /**
     * Fire even when added
     *
     * @param index
     * @param item
     */
    public void add(int index, Object item) {
        super.add(index, item);

        GTDelta delta;
        delta = deltaAdded(index, item);
        delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    GTDelta.NO_INDEX), GTDelta.Kind.CHANGED, host, delta);
        host.getNote().getParent().changed(delta);
    }

    public boolean addAll(Collection list) {
        int position = isEmpty() ? 0 : (size() - 1);
        boolean added = super.addAll(list);

        List changed = deltaAdded(position, size());
        GTDelta delta;
        delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    GTDelta.NO_INDEX), GTDelta.Kind.CHANGED, host, changed);
        host.getNote().getParent().changed(delta);

        return added;
    }

    public boolean addAll(int index, Collection list) {
        int start = index;
        int end = start + list.size();

        boolean added = super.addAll(index, list);
        List changed = deltaAdded(start, end);
        changed.addAll(deltaMove(end, size()));

        GTDelta delta;
        delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    GTDelta.NO_INDEX), GTDelta.Kind.CHANGED, host, changed);
        host.getNote().getParent().changed(delta);

        return added;
    }

    public void clear() {
        super.clear();

        GTDelta delta;
        delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    GTDelta.NO_INDEX), GTDelta.Kind.CHANGED, host);
        host.getNote().getParent().changed(delta);
    }

    public Object remove(int index) {
        List changed = deltaRemoved(index, index + 1);
        Object item = super.remove(index);

        changed.addAll(deltaMove(index, size()));

        GTDelta delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    GTDelta.NO_INDEX), GTDelta.Kind.CHANGED, host, changed);
        host.getNote().getParent().changed(delta);

        return item;
    }

    public boolean remove(Object item) {
        int index = indexOf(item);

        if (index == -1) {
            return false;
        }

        remove(index);

        return true;
    }

    public boolean removeAll(Collection collection) {
        List changed = deltaRemoved(collection);
        boolean removed = super.removeAll(collection);
        changed.addAll(deltaMove(0, size()));

        GTDelta delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    GTDelta.NO_INDEX), GTDelta.Kind.CHANGED, host, changed);
        host.getNote().getParent().changed(delta);

        return removed;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        List changed = deltaRemoved(fromIndex, toIndex);

        super.removeRange(fromIndex, toIndex);

        changed.addAll(deltaMove(0, size()));

        GTDelta delta = new GTDeltaImpl(new GTNoteImpl(notificationName,
                    GTDelta.NO_INDEX), GTDelta.Kind.CHANGED, host, changed);
        host.getNote().getParent().changed(delta);
    }
}
