/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.referencing.operation.transform;


/**
 * @deprecated Moved to {@link org.geotools.referencing.operation.builder} package.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 * @author Alessio Fabiani
 */
public class LocalizationGrid extends org.geotools.referencing.operation.builder.LocalizationGrid {
    /**
     * Constructs an initially empty localization grid. All "real worlds"
     * coordinates are initially set to {@code (NaN,NaN)}.
     *
     * @param width  Number of grid's columns.
     * @param height Number of grid's rows.
     */
    public LocalizationGrid(final int width, final int height) {
        super(width, height);
    }
}
