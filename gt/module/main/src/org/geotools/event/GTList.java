/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class GTList extends ArrayList implements List {
    private static final long serialVersionUID = -4849245752797538846L;
    GTComponent host;

    /**
     * Package visiable constructor for test purposes
     */
    GTList() {
        this(GTRoot.NO_PARENT);
    }

    /**
     * Client code must construct with a GTComponent parent (in order to fire
     * events).
     *
     * @param host Host for this list
     */
    public GTList(GTComponent host) {
        this.host = host;
    }

    /**
     * Notify parent that a new child has been added.
     * 
     * <p>
     * Cigars optional
     * </p>
     *
     * @param child
     */
    final protected void fireChildAdded(Object child) {
        if (child == null) {
            return;
        }

        if (child instanceof GTComponent) {
            GTComponent myChild = (GTComponent) child;
            myChild.setParent(host);
        }

        GTDelta delta;
        delta = new GTDeltaImpl(GTDelta.Kind.ADDED, child);
        delta = new GTDeltaImpl(GTDelta.Kind.CHANGED, host, delta);
        host.getParent().changed(delta);
    }

    /**
     * Call this before removing Child
     * 
     * <p>
     * Use this for non StyleComponent children - like Color. Can be used to
     * indicate a color has been removed from a list.
     * </p>
     *
     * @param child
     */
    final protected void fireChildRemoved(Object child) {
        if (child == null) {
            return;
        }

        if (child instanceof GTComponent) {
            GTComponent myChild = (GTComponent) child;
            myChild.setParent(GTRoot.NO_PARENT);
        }

        GTDelta delta;
        delta = new GTDeltaImpl(GTDelta.Kind.REMOVED, child);
        delta = new GTDeltaImpl(GTDelta.Kind.CHANGED, host, delta);
        host.getParent().changed(delta);
    }

    /**
     * Call this after child has been moved in a list.
     * 
     * <p>
     * Used mostly for for non StyleComponent children - like Color. When items
     * are moved in a list, two items are effected, a single delta can be sent
     * out describe this atomic change.
     * </p>
     *
     * @param child1
     * @param child2
     */
    final protected void fireChildChanged(Object child1, Object child2) {
        GTDelta delta1 = new GTDeltaImpl(GTDelta.Kind.NO_CHANGE, child1);
        GTDelta delta2 = new GTDeltaImpl(GTDelta.Kind.NO_CHANGE, child2);
        List list = new ArrayList(2);
        list.add(delta1);
        list.add(delta2);

        GTDelta delta = new GTDeltaImpl(GTDelta.Kind.CHANGED, host, list);
        host.getParent().changed(delta);
    }

    public boolean add(Object child) {
        boolean added = super.add(child);
        fireChildAdded(child);
        return added;
    }

    /**
     * Fire even when added
     *
     * @param index
     * @param child
     */
    public void add(int index, Object child) {
        super.add(index, child);
    }

    public boolean addAll(Collection list) {
        boolean added = super.addAll(list);
        fireChildrenAdded(list);
        return added;
    }
    public boolean addAll(int index, Collection list) {
		boolean added = super.addAll(index, list);
		fireChildrenAdded(list);
		return added;
	}
    public void clear() {
    	fireChildRemoved( this );
    	super.clear();
    }
    public Object remove(int index) {
    	fireChildRemoved( get( index ) );    	
    	return super.remove(index);
    }
    public boolean remove(Object child) {
        fireChildRemoved(child);
        return super.remove(child);
    }
    public boolean removeAll(Collection collection) {
    	fireChildrenRemoved( collection );
    	return super.removeAll( collection );
    }
    protected void removeRange(int fromIndex, int toIndex) {
    	fireChildRemoved( subList( fromIndex, toIndex ));
    	super.removeRange(fromIndex, toIndex);
    }
    
    //
    // Utility Methods (event notification)
    //
	private void fireChildrenAdded(Collection list) {
		List delta = new ArrayList(list.size());

        for (Iterator i = list.iterator(); i.hasNext();) {
            Object child = i.next();

            if (child instanceof GTComponent) {
                GTComponent myChild = (GTComponent) child;
                myChild.setParent(host);
            }

            delta.add(new GTDeltaImpl(GTDelta.Kind.ADDED, child));
        }

        host.getParent()
            .changed(new GTDeltaImpl(GTDelta.Kind.CHANGED, host, delta));
	}
	private void fireChildrenRemoved(Collection list) {
		List delta = new ArrayList(list.size());

        for (Iterator i = list.iterator(); i.hasNext();) {
            Object child = i.next();

            if (child instanceof GTComponent) {
                GTComponent myChild = (GTComponent) child;
                myChild.setParent(host);
            }

            delta.add(new GTDeltaImpl(GTDelta.Kind.REMOVED, child));
        }

        host.getParent()
            .removed(new GTDeltaImpl(GTDelta.Kind.CHANGED, host, delta));
	}
}
