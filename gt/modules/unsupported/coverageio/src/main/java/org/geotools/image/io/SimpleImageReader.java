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
package org.geotools.image.io;

import java.io.*; // Many imports, including some for javadoc only.
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;


/**
 * Base class for simple image decoders. The main simplification provided by this class is to
 * assume that only one {@linkplain ImageTypeSpecifier image type} is supported (as opposed to
 * the arbitrary number allowed by the standard {@link ImageReader}) and to provide a default
 * image type built automatically from a color palette and a range of valid values.
 * <p>
 * More specifically, this class provides the following conveniences to implementors:
 *
 * <ul>
 *   <li><p>Provides a {@link #getInputStream} method, which returns the {@linkplain #input input}
 *       as an {@link InputStream} for convenience. Different kinds of input like {@linkplain File}
 *       or {@linkplain URL} are automatically handled.</p></li>
 *
 *   <li><p>Provides default {@link #getNumImages} and {@link #getNumBands} implementations,
 *       which return 1. This default behavior matches simple image formats like flat binary
 *       files or ASCII files. Those methods need to be overrided for more complex image
 *       formats.</p></li>
 *
 *   <li><p>Provides {@link #checkImageIndex} and {@link #checkBandIndex} convenience methods.
 *       Those methods are invoked by most implementation of public methods. They perform their
 *       checks based on the informations provided by the above-cited {@link #getNumImages} and
 *       {@link #getNumBands} methods.</p></li>
 *
 *   <li><p>Provides default implementations of {@link #getImageTypes} and {@link #getRawImageType},
 *       which assume that only one {@linkplain ImageTypeSpecifier image type} is supported. The
 *       default image type is created from the informations provided by {@link #getRawDataType}
 *       and {@link #getImageMetadata}.</p></li>
 *
 *   <li><p>Provides {@link #getStreamMetadata} and {@link #getImageMetadata} default
 *       implementations, which return {@code null} as authorized by the specification.
 *       Note that subclasses should consider returning
 *       {@link org.geotools.image.io.metadata.GeographicMetadata}.</p></li>
 * </ul>
 * 
 * Images may be flat binary or ASCII files with no meta-data and no color information.
 * Their pixel values may be floating point values instead of integers. The default
 * implementation assumes floating point values and uses a grayscale color space scaled
 * to fit the range of values. Displaying such an image may be very slow. Consequently,
 * users who want to display image are encouraged to change data type and color space with
 * <a href="http://java.sun.com/products/java-media/jai/">Java Advanced Imaging</a>
 * operators after reading.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link StreamImageReader}.
 */
public abstract class SimpleImageReader extends StreamImageReader {
    /**
     * Constructs a new image reader.
     *
     * @param provider The {@link ImageReaderSpi} that is invoking this constructor,
     *        or {@code null} if none.
     */
    protected SimpleImageReader(final ImageReaderSpi provider) {
        super(provider);
    }
}
