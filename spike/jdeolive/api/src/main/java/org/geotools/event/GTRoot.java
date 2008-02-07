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
 * Indicates the "root" of a Geotools data structure with event notification.
 *
 * <p>
 * Several of the GeoTools objects are produced in reference to specifications,
 * in particular XML based specifications. Often we try and match the same
 * abstractions present in a specification like SLD or Filter. But rather then
 * make use of pure Java Beans, and make user interface code responsible for
 * managing a host of listeners we are providing a single set of listeners
 * located at the object matching the document base.
 * </p>
 *
 * <p></p>
 *
 * @author Jody Garnett, Refractions Research
 * @source $URL$
 */
public interface GTRoot extends GTComponent {
    public static GTRoot NO_PARENT = new GTRoot() {
            public GTComponent getParent() {
                throw new IllegalStateException("Invalid root");
            }

            public void changed(GTDelta delta) {
            }

            public void removed(GTDelta delta) {
            }

            public void addListener(GTListener listener) {
            }

            public void removeListener(GTListener listener) {
            }

            public GTNote getNote() {
                return GTNote.EMPTY;
            }

            public void setNote(GTNote container) {
            }

            public String toString() {
                return "NO_PARENT";
            }
        };

    /**
     * Should not be called, will return a "NullObject" - NO_PARENT.
     *
     * @return NO_PARENT
     */
    public GTComponent getParent();

    /**
     * Since this is the "root" of the tree, please fire event off to the
     * listeners.
     *
     * @param delta Delta describing change
     */
    public void changed(GTDelta delta);

    /**
     * Since this is the "root" of the tree, please fire event off to the
     * listeners.
     *
     * @param delta Delta describing change
     */
    public void removed(GTDelta delta);

    /**
     * Adds a listener for GTEvents
     *
     * @param listener Listener to change events
     */
    public void addListener(GTListener listener);

    /**
     * Removes a previously installed GTListener
     *
     * @param listener Listener to change events
     */
    public void removeListener(GTListener listener);
}
