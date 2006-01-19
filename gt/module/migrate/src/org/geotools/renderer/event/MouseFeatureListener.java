/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC))
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
package org.geotools.renderer.event;

// J2SE dependencies
import java.util.EventListener;

import org.geotools.feature.Feature;


/**
 * The listener interface for receiving "interesting" mouse events (click, enter, and exit)
 * on a {@linkplain Feature feature}.
 *
 * @source $URL$
 * @version $Id$
 * @author Julian Elliott
 */
public interface MouseFeatureListener extends EventListener {
    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a
     * {@linkplain Feature feature}.
     */
    public void mouseClicked(MouseFeatureEvent e);

    /**
     * Invoked when the mouse enters a {@linkplain Feature feature}.
     */
    public void mouseEntered(MouseFeatureEvent e);

    /**
     * Invoked when the mouse exits a {@linkplain Feature feature}.
     */
    public void mouseExited(MouseFeatureEvent e);
}
