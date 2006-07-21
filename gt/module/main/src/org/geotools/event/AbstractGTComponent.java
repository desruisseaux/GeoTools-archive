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

import org.geotools.event.GTComponent;
import org.geotools.event.GTDelta;


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
 * @source $URL$
 */
public abstract class AbstractGTComponent implements GTComponent {
    //    GTComponent notificationParent = GTRoot.NO_PARENT;
    //    protected String notificationName = "";
    //    protected int notificationPosition = GTDelta.NO_INDEX;
    protected GTNote notification = new GTNoteImpl(GTRoot.NO_PARENT, "", GTDelta.NO_INDEX);

    protected Object clone() throws CloneNotSupportedException {
        AbstractGTComponent copy = (AbstractGTComponent) super.clone();

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
        notification.getParent().changed(delta);
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
    protected synchronized void fireChildChanged( String name, Object child, Object oldValue) {
    	GTNote here = new GTNoteImpl( this, name, GTDelta.NO_INDEX );
    	
    	if( oldValue instanceof GTComponent ){
    		GTNote note = new GTNoteImpl( GTRoot.NO_PARENT, "", GTDelta.NO_INDEX );
    		
    		GTComponent myDeath = (GTComponent) oldValue ;
    		myDeath.setNote( note );
    	}
    	
    	if( child instanceof GTComponent ){
    		GTComponent myChild = (GTComponent) child;
    		myChild.setNote( here );
    	}
    	
        if (child == null) {
            fireChanged(); // well something changed			
        } else {        	
            GTDelta delta;            
            delta = new GTDeltaImpl( here, GTDelta.Kind.CHANGED, child, oldValue );             // <-- child delta
            changed( delta );            
        }
    }

    public GTComponent getParent() {
        return notification.getParent();
    }

    //	public void setParent(GTComponent newParent) {
    //		if( newParent == null ) {
    //			newParent = GTRoot.NO_PARENT;
    //		}		
    //		if( notificationParent != GTRoot.NO_PARENT ){
    //			// TODO: Freek out if Construct is adopted by a new parent
    //			//       Previous parents need to disown children beforehand
    //			throw new IllegalStateException("Please remove from existing parent first");
    //		}
    //		notificationParent = newParent;
    //	}
    //
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
    public GTNote getNote() {
        return notification;
    }

    public void setNote(GTNote container) {
        notification = container;
    }
}
