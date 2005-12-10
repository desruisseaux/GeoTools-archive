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

//            public void setParent(GTComponent newParent) {
//            }
//
//            public void setNotificationName(String name) {
//            }
//
//            public String getNotificationName() {
//                return "";
//            }
//
//            public void setNotificationPosition(int index) {
//            }
//
//            public int getNotificationPosition() {
//                return GTDelta.NO_INDEX;
//            }

			public GTNote getNote() {
				return GTNote.EMPTY;
			}

			public void setNote(GTNote container) {
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
