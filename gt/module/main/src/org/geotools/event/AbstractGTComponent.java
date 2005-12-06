/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.event;

import org.geotools.event.GTComponent;
import org.geotools.event.GTDelta;

import java.util.ArrayList;
import java.util.List;



/**
 * Provides basic StyleEvent notification, may be used in conjuction with
 * StyleList during event handling.
 * 
 * <p>
 * This class has package scope to prevent user code mistaking it for something
 * important. It is only used to assist in the construction of this one
 * implementation of StyleEvents. Basically this is NOT API :-)
 * </p>
 *
 * @since 2.2.M3
 * @author Jody Garnett
 */
public abstract class AbstractGTComponent implements GTComponent {
    GTComponent parent = GTRoot.NO_PARENT;

    /**
     * Simple notification that we changed.
     * 
     * <p>
     * Change will be passed on to parent.changed( delta ).
     * </p>
     */
    protected final void fireChanged() {
        fire(new GTDeltaImpl(this));
    }

    /**
     * Provide notification based on the provided delta.
     * 
     * <p>
     * Delta must come from this StyleComponent.
     * </p>
     *
     * @param delta object containing change information
     */
    protected void fire(GTDelta delta) {
        parent.changed(delta);
    }

    /**
     * Used to pass on "something is about to change" notification from
     * children.
     *
     * @param delta object containing change information
     */
    public void removed(GTDelta delta) {
        parent.removed(new GTDeltaImpl(GTDelta.Kind.NO_CHANGE, this, delta));
    }

    /**
     * Used to pass on "We changed" notification from children.
     *
     * @param delta object containing change information
     */
    public void changed(GTDelta delta) {
        fire(new GTDeltaImpl(GTDelta.Kind.NO_CHANGE, this, delta));
    }

    /**
     * Call this after adding child
     * 
     * <p>
     * Use this for non StyleComponent children - like Color. Can be used to
     * indicate a color has been added to a list.
     * </p>
     *
     * @param child 
     */
    final protected void fireChildAdded(Object child) {
        if (child == null) {
            return;
        }
        if (child instanceof AbstractGTComponent){
        	AbstractGTComponent myChild = (AbstractGTComponent) child;
        	myChild.parent = this;
        }

        GTDelta childDelta = new GTDeltaImpl(GTDelta.Kind.ADDED, child);
        fire(new GTDeltaImpl(GTDelta.Kind.NO_CHANGE, this, childDelta));
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
        if (child instanceof AbstractGTComponent){
        	AbstractGTComponent myChild = (AbstractGTComponent) child;
        	myChild.parent = null;
        }
        removed(new GTDeltaImpl(GTDelta.Kind.REMOVED, child));
    }

    /**
     * Create a child delta and send it off.
     * 
     * <p>
     * Use this for non StyleComponent children - like Color. Can be used to
     * indicate that a color has changed in a List.
     * </p>
     *
     * @param child 
     */
    final protected void fireChildChanged(Object child) {
        if (child == null) {
            fireChanged(); // sounds like something was "removed"!			
        } else {
            changed(new GTDeltaImpl(GTDelta.Kind.CHANGED, child));
        }
    }

    /**
     * Call this after child has been moved in a list.
     * 
     * <p>
     * Use this for non StyleComponent children - like Color. When items are
     * moved in a list, two items are effected, a single delta can be sent out
     * describe this atomic change.
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

        fire(new GTDeltaImpl(GTDelta.Kind.CHANGED, this, list));
    }
    
    public GTComponent getParent() {
    	return parent;
    }
    
    public void setParent(GTComponent newParent) {
    	if( newParent == null ){
    		newParent = GTRoot.NO_PARENT;
    	}
    	parent = newParent;
    }
}
