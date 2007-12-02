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
package org.geotools.image.io.mosaic;

import javax.imageio.ImageReader;
import org.geotools.resources.Utilities;


/**
 * A pair of {@link ImageReader} with its {@link Input}. Only used as keys in hash map.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ReaderInputPair {
    /**
     * The image reader.
     */
    final ImageReader reader;

    /**
     * The input to be given to the image reader.
     */
    private final Object input;

    /**
     * Creates a reader/input pair for the given tile.
     */
    ReaderInputPair(final Tile tile) {
        reader = tile.getReader();
        input  = tile.getInput();
    }

    /**
     * Returns a hash value for this reader/input pair.
     */
    @Override
    public int hashCode() {
        return reader.hashCode() + 37*Utilities.deepHashCode(input);
    }

    /**
     * Compares this reader/input pair with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof ReaderInputPair) {
            final ReaderInputPair that = (ReaderInputPair) object;
            return this.reader == that.reader && // We really want the same instance.
                    Utilities.deepEquals(this.input, that.input);
        }
        return false;
    }

}
