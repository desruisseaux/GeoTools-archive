/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
import java.util.Collection;

import org.geotools.feature.Feature;


/**
 * A <code>MouseFeatureEvent</code> is created whenever the mouse moves into
 * or out of the area of a {@linkplain Feature feature}, or if the mouse is
 * clicked while the mouse is over a feature. It is passed to all
 * {@link MouseFeatureListener} objects that have been registered with the
 * {@link HighlightManager}.
 *
 * @version $Id$
 * @author Julian Elliott
 */
public interface MouseFeatureEvent {
    /**
     * Returns a collection of {@linkplain Feature feature} objects to which the event refers.
     * Topmost feature is first.
     */
    public Collection getFeatures();
}
