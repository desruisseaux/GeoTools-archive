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

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


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
public abstract class AbstractGTRoot extends AbstractGTComponent
    implements GTRoot {
    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.event");

    //    GTComponent notificationParent = GTRoot.NO_PARENT;
    //    protected String notificationName = "";
    //    protected int notificationPosition = GTDelta.NO_INDEX;
    GTNote notification = new GTNoteImpl(GTRoot.NO_PARENT, "", GTDelta.NO_INDEX);
    private HashSet listeners;

    protected Object clone() throws CloneNotSupportedException {
        AbstractGTRoot copy = (AbstractGTRoot) super.clone();

        //    	copy.notificationParent = GTRoot.NO_PARENT;
        //    	copy.notificationName = "";
        //    	copy.notificationPosition = GTDelta.NO_INDEX;
        copy.notification = new GTNoteImpl(GTRoot.NO_PARENT, "",
                GTDelta.NO_INDEX);

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
        GTDelta delta = new GTDeltaImpl(notification, GTDelta.Kind.NO_CHANGE,
                this, null, childDelta);
        notification.getParent().removed(delta);
    }

    /**
     * Used to pass on "We changed" notification from children.
     *
     * @param childDelta object containing change information
     */
    public void changed(GTDelta childDelta) {
        GTDelta delta = new GTDeltaImpl(notification, GTDelta.Kind.NO_CHANGE,
                this, null, childDelta);
        notification.getParent().removed(delta);
    }

    /**
     * Simple notification that we changed.
     * 
     * <p>
     * Change will be passed on to parent.changed( delta ).
     * </p>
     */
    protected void fireChanged() {
        GTDelta delta = new GTDeltaImpl(notification, GTDelta.Kind.CHANGED,
                this, null);
        notification.getParent().changed(delta);
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
     * @param oldValue DOCUMENT ME!
     */
    final protected void fireChildChanged(String childName, Object child,
        Object oldValue) {
        if (child == null) {
            fireChanged(); // well something changed			
        } else {
            GTDelta delta;
            delta = new GTDeltaImpl(new GTNoteImpl(childName, GTDelta.NO_INDEX),
                    GTDelta.Kind.CHANGED, child);
            delta = new GTDeltaImpl(notification, GTDelta.Kind.NO_CHANGE, this);
            notification.getParent().changed(delta);
        }
    }

    //    public GTComponent getParent() {
    //        return notification.getParent();
    //    }
    //	public void setParent(GTComponent newParent) {
    //		notification.setParent(newParent);
    //	}
    //	public void setNotificationName(String name) {
    //		if( name == null ) name = "";
    //		notificationName = name;
    //	}
    //
    //	public String getNotificationName() {
    //		return notificationName;
    //	}
    //
    //	public void setNotificationPosition(int index) {
    //		notificationPosition = index;
    //	}
    //
    //	public int getNotificationPosition() {
    //		return notificationPosition;
    //	}

    /**
     * Listens to changes in the Style content.
     * 
     * <p>
     * Changes are provided:
     * 
     * <ul>
     * <li>
     * Before: deletion
     * </li>
     * <li>
     * After: modification
     * </li>
     * </ul>
     * </p>
     * 
     * <p>
     * Since the Style data structure can be vast and complicated a trail of
     * breadcrumbs (a delta) is provided to help find your way to the change.
     * </p>
     *
     * @param listener
     */
    public synchronized void addListener(GTListener listener) {
        if (listeners == null) {
            listeners = new HashSet();
        }

        listeners.add(listener);
    }

    /**
     * Remove a style listener
     *
     * @param listener Listen to notifications
     */
    public synchronized void removeListener(GTListener listener) {
        listeners.remove(listener);
    }

    /**
     * Provides notification of daring do (and undo) in style space.
     *
     * @param event Event describing notification
     */
    protected synchronized void fire(GTEvent event) {
        assert event != null;

        for (Iterator i = listeners.iterator(); i.hasNext();) {
            GTListener listener = (GTListener) i.next();

            try {
                listener.changed(event);
            } catch (Throwable t) {
                //LOGGER.log( Level.SEVERE, listener.getClass().getSimpleName() + " encountered a serious problem", t );
                LOGGER.log(Level.SEVERE,
                    "GTListener encountered a serious problem", t);
            }
        }
    }

    /**
     * Issue a change event w/ POST_CHANGE
     *
     * @param delta Used to quickly fire off a child delta
     */
    protected synchronized void fire(GTDelta delta) {
        if (!hasListeners()) {
            return;
        }

        GTEventImpl event = new GTEventImpl(this, GTEvent.Type.POST_CHANGE,
                delta);
        fire(event);
    }

    protected boolean hasListeners() {
        return (listeners != null) && !listeners.isEmpty();
    }
}
