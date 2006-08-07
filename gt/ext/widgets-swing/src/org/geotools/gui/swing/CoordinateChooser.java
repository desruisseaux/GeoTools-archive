/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gui.swing;

// J2SE dependencies
import java.util.Date;


/**
 * A pane of controls designed to allow a user to select spatio-temporal coordinates.
 * Current implementation uses geographic coordinates (longitudes/latitudes) and dates
 * according some locale calendar. Future version may allow the use of user-specified
 * coordinate system. Latitudes are constrained in the range 90°S to 90°N inclusive.
 * Longitudes are constrained in the range 180°W to 180°E inclusive. By default, dates
 * are constrained in the range January 1st, 1970 up to the date at the time the widget
 * was created.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the {@link org.geotools.gui.swing.referencing} package.
 */
public class CoordinateChooser extends org.geotools.gui.swing.referencing.CoordinateChooser {
    /**
     * Constructs a default coordinate chooser. Date will be constrained in the range from
     * January 1st, 1970 00:00 UTC up to the {@linkplain System#currentTimeMillis current time}.
     */
    public CoordinateChooser() {
        super();
    }

    /**
     * Constructs a coordinate chooser with date constrained in the specified range.
     * Note that the {@code [minTime..maxTime]} range is not the same than the
     * range given to {@link #setTimeRange}. The later set only the time range shown
     * in the widget, while this constructor set also the minimum and maximum dates
     * allowed.
     *
     * @param minTime The minimal date allowed.
     * @param maxTime the maximal date allowed.
     */
    public CoordinateChooser(final Date minTime, final Date maxTime) {
        super(minTime, maxTime);
    }
}
