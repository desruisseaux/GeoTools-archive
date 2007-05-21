/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.image.io;


/**
 * A sample converter that do not performs any conversion.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class IdentityConverter implements SampleConverter {
    /**
     * Converts a double-precision value before to store it in the raster.
     */
    public double convert(double value) {
        return value;
    }

    /**
     * Converts a float-precision value before to store it in the raster.
     */
    public float convert(float value) {
        return value;
    }

    /**
     * Converts a float-precision value before to store it in the raster.
     */
    public int convert(int value) {
        return value;
    }
}
