/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.grid;

/**
 * Constants for neighbor positions.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/unsupported/grid/src/test/java/org/geotools/grid/hexagon/HexagonTest.java $
 * @version $Id: HexagonTest.java 35566 2010-05-24 13:47:59Z mbedward $
 */
public enum Neighbor {
    UPPER,
    UPPER_LEFT,
    UPPER_RIGHT,
    LOWER,
    LOWER_LEFT,
    LOWER_RIGHT,
    LEFT,
    RIGHT
}
