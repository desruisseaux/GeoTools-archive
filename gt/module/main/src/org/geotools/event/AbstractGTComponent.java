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

import org.geotools.event.GTComponent;
import org.geotools.event.GTDelta;
import java.util.ArrayList;
import java.util.Iterator;
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
 */
public abstract class AbstractGTComponent implements GTComponent {
    GTComponent notificationParent = GTRoot.NO_PARENT;
    protected String notificationName = "";
    protected int notificationPosition = GTDelta.NO_INDEX;

    protected Object clone() throws CloneNotSupportedException {
    	AbstractGTComponent copy = (AbstractGTComponent) super.clone();
    	copy.notificationParent = GTRoot.NO_PARENT;
    	copy.notificationName = "";
    	copy.notificationPosition = GTDelta.NO_INDEX;
    	
    	return copy;
    }
    
    /**
     * Provide notification based on the provided delta.
     * 
     * <p>
     * Delta must come from this StyleComponent.
     * </p>
     *
     * @param childDelta object containing change information protected void
     *        fire(GTDelta delta){ parent.changed(delta); }
     */
    /**
     * Used to pass on "something is about to change" notification from
     * children.
     *
     * @param childDelta object containing change information
     */
    public void removed(GTDelta childDelta) {
        GTDelta delta = new GTDeltaImpl(notificationName, notificationPosition,
                GTDelta.Kind.NO_CHANGE, this, null, childDelta);
        notificationParent.removed(delta);
    }

    /**
     * Used to pass on "We changed" notification from children.
     *
     * @param childDelta object containing change information
     */
    public void changed(GTDelta childDelta) {
        GTDelta delta = new GTDeltaImpl(notificationName, notificationPosition,
                GTDelta.Kind.NO_CHANGE, this, null, childDelta);
        notificationParent.removed(delta);
    }

    /**
     * Simple notification that we changed.
     * 
     * <p>
     * Change will be passed on to parent.changed( delta ).
     * </p>
     */
    protected void fireChanged() {
        GTDelta delta = new GTDeltaImpl(notificationName, notificationPosition,
                GTDelta.Kind.CHANGED, this, null);
        notificationParent.changed(delta);
    }

    /**
     * Create a child delta and send it off.
     * 
     * <p>
     * Use this for changes to simple types like int and Color.
     * </p>
     *
     * @param childName used to the child (often bean propertyName or map key)
     * @param child
     */
    protected void fireChildChanged(String childName, Object child, Object oldValue) {
        if (child == null) {
            fireChanged(); // well something changed			
        } else {
            GTDelta delta;
            delta = new GTDeltaImpl(childName, GTDelta.NO_INDEX,
                    GTDelta.Kind.CHANGED, child);
            delta = new GTDeltaImpl(notificationName, notificationPosition,
                    GTDelta.Kind.NO_CHANGE, this);
            notificationParent.changed(delta);
        }
    }

    /**
     * Call this after adding child, called by GTList - although will be
     * helpful for those using arrays.
     *
     * @param list name of list being modified
     * @param position in list where child was added
     * @param child
     *
    final protected void fireChildAdded(String list, int position, Object child) {
        if (child == null) {
            return;
        }

        if (child instanceof GTComponent) {
            GTComponent myChild = (GTComponent) child;
            myChild.setParent(this);
            myChild.setNotificationName(list);
            myChild.setNotificationPosition(position);
        }

        GTDelta delta;
        delta = new GTDeltaImpl(notificationName, notificationPosition,
                GTDelta.Kind.ADDED, child);
        delta = new GTDeltaImpl(notificationName, notificationPosition,
                GTDelta.Kind.CHANGED, this);
        notificationParent.changed(delta);
    }*/

    /**
     * Call this when removing a Child.
     *
     * @param list name of list being modified
     * @param index in list where child was added
     * @param child
     * @param rest List of children that have moved up one
     *
    final protected void fireChildRemoved(String list, int index, Object child,
        List rest) {
        if (child == null) {
            return;
        }

        List changed = new ArrayList(1);

        if (child instanceof GTComponent) {
            GTComponent myChild = (GTComponent) child;
            myChild.setParent(GTRoot.NO_PARENT);
            myChild.setNotificationName("");
            myChild.setNotificationPosition(GTDelta.NO_INDEX);
        }

        changed.add(new GTDeltaImpl(notificationName, notificationPosition,
                GTDelta.Kind.REMOVED, null, child));

        int position = index;

        for (Iterator i = rest.iterator(); i.hasNext(); position++) {
            Object element = i.next();

            if (element instanceof GTComponent) {
                GTComponent aChild = (GTComponent) element;
                aChild.setNotificationPosition(position);
            }

            changed.add(new GTDeltaImpl(notificationName, position,
                    GTDelta.Kind.NO_CHANGE, element, null));
        }

        GTDelta delta;
        delta = delta = new GTDeltaImpl(notificationName, notificationPosition,
                    GTDelta.Kind.CHANGED, this);
        notificationParent.changed(delta);
    }*/

    public GTComponent getParent() {
        return notificationParent;
    }

	public void setParent(GTComponent newParent) {
		if( newParent == null ) {
			newParent = GTRoot.NO_PARENT;
		}		
		if( notificationParent != GTRoot.NO_PARENT ){
			// TODO: Freek out if Construct is adopted by a new parent
			//       Previous parents need to disown children beforehand
			// throw new IllegalStateException("Please remove from existing parent first");
		}
		notificationParent = newParent;
	}

	public void setNotificationName(String name) {
		if( name == null ) name = "";
		notificationName = name;
	}

	public String getNotificationName() {
		return notificationName;
	}

	public void setNotificationPosition(int index) {
		notificationPosition = index;
	}

	public int getNotificationPosition() {
		return notificationPosition;
	}
}
