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
 * A sample converter that replaces a pad value by 0, and applies an offset on all other values.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class IntegerConverter implements SampleConverter {
    /**
     * The pad value to replace by 0.
     */
    private final int padValue;

    /**
     * An offset to add to the values to be read, before to store them in the raster.
     * This is used primarily for transforming <em>signed</em> short into <em>unsigned</em>
     * short.
     */
    private final int offset;

    /**
     * Constructs a converter with the specified pad value and offset.
     */
    public IntegerConverter(final int padValue, final int offset) {
        this.padValue = padValue;
        this.offset   = offset;
    }

    /**
     * Converts a double-precision value before to store it in the raster.
     */
    public double convert(double value) {
        return (value == padValue) ? 0 : value + offset;
    }

    /**
     * Converts a float-precision value before to store it in the raster.
     */
    public float convert(float value) {
        return (value == padValue) ? 0 : value + offset;
    }

    /**
     * Converts a float-precision value before to store it in the raster.
     */
    public int convert(int value) {
        return (value == padValue) ? 0 : value + offset;
    }
}
