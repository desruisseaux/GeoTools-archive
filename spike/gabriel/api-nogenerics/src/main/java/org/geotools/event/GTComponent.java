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
 * Provides support for Parent/Child relationships for the event system.
 * <p>
 * Several of the GeoTools objects are produced in reference to
 * specifications, in particular XML based specifications. Often
 * we try and match the same abstractions present in a specification
 * like SLD or Filter. But rather then make use of pure Java Beans, and
 * make user interface code responsible for managing a host of listeners
 * we are providing a single set of listeners located at the object
 * matching the document base.
 * <p>
 * For more Details:
 * <ul>
 * <li>This design is similar to EMF, or JFace use (aka borrow code examples)
 * <li>Not specific to Documents, the Catalog api will use these events
 * <li>We do try and match the document structure perfectly for Feature/FeatureCollection/GML
 * (so that the same XPath expressions can be respected). FeatureCollection and
 * Feature have their own well explored structure and issues and will not be using
 * this event system. Given the size of FeatureCollections it is not practicle for each
 * child to "know" its parent.
 * </ul>
 * <p>
 * @author Jody Garnett
 *
 * @source $URL$
 */
public interface GTComponent {
    /**
     * Used to locate our parent.
     * <p>
     * This method will return a "NULLObject", called GTRoot.NO_PARENT when
     * no parent is present, client code should never have to be concerned
     * this method return <code>null</code>.
     * @deprecated use getNote().getParent()
     * @return Parent GTComponent or GTRoot.NO_PARENT if none
     */
    GTComponent getParent();

    //	/**
    //	 * Used to set the parent, and associated placement information.
    //	 * 
    //	 * @param newParent GTComponent or NULLGTRoot if none
    //	 */
    //	void setParent(GTComponent newParent );		
    //	
    //	/** Indicate name used during notification */
    //	public void setNotificationName( String name );
    //	/** Indicate name used during notification */	
    //	public String getNotificationName();
    //	/** Indicate name position used during notification */	
    //	public void setNotificationPosition( int index );
    //	/** Indicate position used during notification */	
    //	public int getNotificationPosition();

    /**
     * Small stratagy object passed in by our parent so we can call home.
     * Used to pass change information "up" to our parent, to root parent
     * will broadcast the events out to listeners.
     */
    public GTNote getNote();

    /**
     * Small stratagy object passed in by our parent so we can call home.
     * Used to pass change information "up" to our parent, to root parent
     * will broadcast the events out to listeners.
     * @param container
     */
    public void setNote(GTNote container);

    /**
     * A child has been removed, issued before change.
     * <p>
     * This method is for use by children <b>only</b> it is implementor
     * API and should not be called by client code.
     * </p>
     * Q:Why does it exist then?<br>
     * So you can implement your own Symbolizer,
     * and still allow the GeoTools Stroke implementation to pass change
     * notification onto you.
     * </p>
     * <p>
     * Q:GeoAPI does not support this?
     * No they don't, their interface are set up to match specification
     * for interoptability between toolkit implementations. By the time
     * you pass a GeoAPI object around it should stop wiggling and just be
     * viewed as stable data. But yes we should ask them about this...
     * </p>
     */
    void removed(GTDelta delta);

    /**
     * A child has been changed (maybe added), issued after change.
     * <p>
     * This method is for use by children <b>only</b> it is implementor
     * API and should not be called by client code.
     * </p>
     * Q:Why does it exist then?<br>
     * So you can implement your own Symbolizer,
     * and still allow the GeoTools Stroke implementation to pass change
     * notification onto you.
     * </p>
     * <p>
     * Q:GeoAPI does not support this?
     * No they don't, their interface are set up to match specification
     * for interoptability between toolkit implementations. By the time
     * you pass a GeoAPI object around it should stop wiggling and just be
     * viewed as stable data. But yes we should ask them about this...
     * </p>
     */
    void changed(GTDelta delta);
}
