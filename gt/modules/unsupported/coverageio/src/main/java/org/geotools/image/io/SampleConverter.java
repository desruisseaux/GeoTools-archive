/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Geomatys
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

// J2SE dependencies
import java.awt.image.Raster;  // For javadoc


/**
 * Converts samples from the values stored in the image file to the values stored in the
 * {@link Raster}. This interface provides a hook for allowing users to convert "nodata"
 * values (typically a fixed value like 9999 or 32767) to {@link Float#NaN NaN} if the
 * target type is {@code float} or {@code double}, or 0 if the target type is an integer.
 * Users can also implement this interface in order to convert <em>signed</em> integers
 * into <em>unsigned</em> integers, by applying an offset to the values.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface SampleConverter {
    /**
     * Converts a double-precision value before to store it in the raster.
     * Subclasses should override this method if some fixed values need to
     * be converted into {@link Double#NaN} value.
     *
     * @param value The value read from the image file.
     * @return The value to store in the {@linkplain Raster raster}.
     */
    double convert(double value);

    /**
     * Converts a float-precision value before to store it in the raster.
     * Subclasses should override this method if some fixed values need to
     * be converted into {@link Float#NaN} value.
     *
     * @param value The value read from the image file.
     * @return The value to store in the {@linkplain Raster raster}.
     */
    float convert(float value);

    /**
     * Converts a float-precision value before to store it in the raster.
     *
     * @param value The value read from the image file.
     * @return The value to store in the {@linkplain Raster raster}.
     */
    int convert(int value);
}
