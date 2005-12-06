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
 * @author jgarnett
 */
public interface GTListener {
    /**
     * Notifies this listener that some changes are happening, or have already
     * happened.
     * 
     * <p>
     * The supplied event gives details. This event object (and the delta
     * within it) is valid only for the duration of the invocation of this
     * method.
     * </p>
     * 
     * <p>
     * Note that during style change event notification, further changes to the
     * style may be disallowed.
     * </p>
     * 
     * <p>
     * Note that this method is not guaranteed to execute in an user interface
     * thread:
     * 
     * <ul>
     * <li>
     * SwingUtilities.invokeLater( Runnable );
     * </li>
     * <li>
     * SWT: Display.getDefault().asyncExec( Runnable );
     * </li>
     * </ul>
     * 
     * All you J2EE developers can stop laughing now.
     * </p>
     *
     * @param event the style change event
     */
    public void changed(GTEvent event);
}
