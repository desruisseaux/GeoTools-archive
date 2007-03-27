/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.geometry.jts;

// OpenGIS dependencies
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.geometry.MismatchedDimensionException;

// JTS dependencies
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;


/**
 * A default implementation of {@linkplain CoordinateSequenceTransformer coordinate sequence
 * transformer}. This transformer applies the coordinate transformations immediately (which
 * means that caller are immediately notified if a transformation fails).
 * <p>
 * This transformer support {@linkplain MathTransform math transform} with up to 3 source
 * or target dimensions. This transformer is not thread-safe.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Andrea Aime
 * @author Martin Desruisseaux
 */
public class DefaultCoordinateSequenceTransformer implements CoordinateSequenceTransformer {
    /**
     * A buffer for coordinate transformations. We choose a length which is divisible by
     * both 2 and 3, since JTS coordinates may be up to three-dimensional. If the number
     * of coordinates point to transform is greater than the buffer capacity, then the
     * buffer will be flushed to the destination array before to continue. We avoid to
     * create a buffer as large than the number of point to transforms, because it would
     * consume a large amount of memory for big geometries.
     */
    private final transient double[] buffer = new double[96];

    /**
     * The coordinate sequence factory to use.
     */
    private final CoordinateSequenceFactory csFactory;

    /**
     * Constructs a default coordinate sequence transformer.
     */
    public DefaultCoordinateSequenceTransformer() {
        csFactory = DefaultCoordinateSequenceFactory.instance();
    }

    /**
     * {@inheritDoc}
     */
    public CoordinateSequence transform(final CoordinateSequence sequence, final MathTransform transform)
            throws TransformException
    {
        final int sourceDim      = transform.getSourceDimensions();
        final int targetDim      = transform.getTargetDimensions();
        final int size           = sequence.size();
        final Coordinate[] tcs   = new Coordinate[size];
        final int bufferCapacity = buffer.length / Math.max(sourceDim, targetDim);
        int remainingBeforeFlush = Math.min(bufferCapacity, size);
        int ib = 0; // Index in the buffer array.
        int it = 0; // Index in the target array.
        for (int i=0; i<size; i++) {
            final Coordinate c = sequence.getCoordinate(i);
            switch (sourceDim) {
                default: throw new MismatchedDimensionException();
                case 3:  buffer[ib+2] = c.z; // Fall through
                case 2:  buffer[ib+1] = c.y; // Fall through
                case 1:  buffer[ib  ] = c.x; // Fall through
                case 0:  break;
            }
            ib += sourceDim;
            if (--remainingBeforeFlush == 0) {
                /*
                 * The buffer is full, or we just copied the last coordinates.
                 * Transform the coordinates and flush to the destination array.
                 */
                assert ib % sourceDim == 0;
                final int n = ib/sourceDim;
                transform.transform(buffer, 0, buffer, 0, n);
                ib = 0;
                for (int j=0; j<n; j++) {
                    final Coordinate t;
                    switch (targetDim) {
                        default: throw new MismatchedDimensionException();
                        case 3: t = new Coordinate(buffer[ib++], buffer[ib++], buffer[ib++]); break;
                        case 2: t = new Coordinate(buffer[ib++], buffer[ib++]              ); break;
                        case 1: t = new Coordinate(buffer[ib++], Double.NaN                ); break;
                        case 0: t = new Coordinate(Double.NaN,   Double.NaN                ); break;
                    }
                    tcs[it++] = t;
                }
                assert ib == n*targetDim;
                ib = 0;
                remainingBeforeFlush = Math.min(bufferCapacity, size-(i+1));
            }
        }
        assert it == tcs.length : tcs.length-it;
        return csFactory.create(tcs);
    }
}
