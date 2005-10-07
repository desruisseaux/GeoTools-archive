/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.geometry.jts;

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.IllegalPathStateException;

// JTS dependencies
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.geometry.ShapeUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;


/**
 * JTS Geometry utility methods, bringing Geotools to JTS.
 * <p>
 * Offers geotools based services such as reprojection.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>transformation</li>
 *   <li>coordinate sequence editing</li>
 *   <li>common coordinate sequence implementations for specific uses</li>
 * </ul>
 *
 * @since 2.2
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class JTS {
    /**
     * @deprecated Do not create subclasses of this class.
     *             This constructor will be private in a future release.
     */
    protected JTS() {
    }

    /**
     * Transforms the envelope using the specified math transform.
     *
     * @param  envelope  The envelope to transform.
     * @param  transform The transform to use.
     * @return The transformed Envelope
     * @throws TransformException if at least one coordinate can't be transformed.
     */
    public static Envelope transform(final Envelope envelope, final MathTransform transform)
            throws TransformException
    {
        return transform(envelope, null, transform, 5);
    }

    /**
     * Transforms the densified envelope using the specified math transform.
     * The envelope is densified (extra points put around the outside edge)
     * to provide a better new envelope for high deformed situations.
     * <p>
     * If an optional target envelope is provided, this envelope will be
     * {@linkplain Envelope#expandToInclude expanded} with the transformation result. It will
     * <strong>not</strong> be {@linkplain Envelope#setToNull nullified} before the expansion.
     *
     * @param  sourceEnvelope  The envelope to transform.
     * @param  targetEnvelope  An envelope to expand with the transformation result, or {@code null}
     *                         for returning an new envelope.
     * @param  transform       The transform to use.
     * @param  npoints         Densification of each side of the rectange.
     * @return {@code targetEnvelope} if it was non-null, or a new envelope otherwise.
     *         In all case, the returned envelope fully contains the transformed envelope.
     * @throws TransformException if a coordinate can't be transformed.
     */
    public static Envelope transform(final Envelope sourceEnvelope, Envelope targetEnvelope,
                                     final MathTransform transform, int npoints)
            throws TransformException 
    {
        if (transform.getSourceDimensions()!=2 || transform.getTargetDimensions()!=2) {
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.BAD_TRANSFORM_$1,
                                                   Utilities.getShortClassName(transform)));
        }
        npoints++; // for the starting point.
        final double[] coordinates = new double[ (4*npoints)*2 ];
        final double xmin   = sourceEnvelope.getMinX();
        final double xmax   = sourceEnvelope.getMaxX();
        final double ymin   = sourceEnvelope.getMinY();
        final double ymax   = sourceEnvelope.getMaxY();
        final double scaleX = (xmax - xmin) / npoints;
        final double scaleY = (ymax - ymin) / npoints;

        int offset = 0;
        for (int t=0; t<npoints; t++) {
            final double dx = scaleX*t;
            final double dy = scaleY*t;
            coordinates[offset++] = xmin;       // Left side, increasing toward top.
            coordinates[offset++] = ymin + dy;
            coordinates[offset++] = xmin + dx;  // Top side, increasing toward right.
            coordinates[offset++] = ymax;
            coordinates[offset++] = xmax;       // Right side, increasing toward bottom.
            coordinates[offset++] = ymax - dy;
            coordinates[offset++] = xmax - dx;  // Bottom side, increasing toward left.
            coordinates[offset++] = ymin;
        }
        assert offset == coordinates.length;
        xform(transform, coordinates, coordinates);

        // Now find the min/max of the result
        if (targetEnvelope == null) {
            targetEnvelope = new Envelope();
        }
        for (int t=0; t<offset;) {
            targetEnvelope.expandToInclude(coordinates[t++], coordinates[t++]);
        }
        return targetEnvelope;
    }

    /**
     * Transforms the geometry using the default transformer.
     * 
     * @param  geom The geom to transform
     * @param  transform the transform to use during the transformation.
     * @return the transformed geometry.  It will be a new geometry.
     * @throws MismatchedDimensionException if the geometry doesn't have the expected dimension
     *         for the specified transform.
     * @throws TransformException if a point can't be transformed.
     */
    public static Geometry transform(final Geometry geom, final MathTransform transform)
            throws MismatchedDimensionException, TransformException
    {
        final GeometryCoordinateSequenceTransformer transformer = new GeometryCoordinateSequenceTransformer();
        transformer.setMathTransform(transform);
        return transformer.transform(geom);
    }

    /**
     * Transforms the coordinate using the provided math transform.
     *
     * @param source the source coordinate that will be transformed
     * @param dest the coordinate that will be set.  May be null or the source coordinate
     *        (or new coordinate of course). 
     * @return the destination coordinate if not null or a new Coordinate.
     * @throws TransformException if the coordinate can't be transformed.
     */     
    public static Coordinate transform(Coordinate source, Coordinate dest, MathTransform transform)
            throws TransformException
    {
        if (dest == null) {
            dest = new Coordinate();
        }
        final double[] array = new double[transform.getSourceDimensions()];
        switch (array.length) {
            case 3: array[2] = source.z;  // Fall through
            case 2: array[1] = source.y;  // Fall through
            case 1: array[0] = source.x;  // Fall through
            case 0: break;
        }
        transform.transform(array, 0, array, 0, 1);
        switch (transform.getTargetDimensions()) {
            case 3: dest.z = array[2];  // Fall through
            case 2: dest.y = array[1];  // Fall through
            case 1: dest.x = array[0];  // Fall through
            case 0: break;
        }
        return dest;
    }

    /**
     * Transforms the envelope from its current crs to WGS84 coordinate reference system.
     * If the specified envelope is already in WGS84, then it is returned unchanged.
     *
     * @param envelope The envelope to transform.
     * @param crs The CRS the envelope is currently in.
     * @return The envelope transformed to be in WGS84 CRS.
     * @throws FactoryException If no transform is available from {@code crs} to WGS84.
     * @throws TransformException If at least one coordinate can't be transformed.
     */
    public static Envelope toGeographic(final Envelope envelope, final CoordinateReferenceSystem crs)
            throws FactoryException, TransformException
    {
        if (CRSUtilities.equalsIgnoreMetadata(crs, DefaultGeographicCRS.WGS84)) {
            return envelope;
        }
        final MathTransform transform = CRS.transform(crs, DefaultGeographicCRS.WGS84, true);
        return transform(envelope, transform);
    }

    /**
     * Like a transform but eXtreme!
     *
     * Transforms an array of coordinates using the provided math transform.
     * Each coordinate is transformed separately. In case of a transform exception then
     * the new value of the coordinate is the last coordinate correctly transformed.
     *
     * @param mt    The math transform to apply.
     * @param src   The source coordinates.
     * @param dest  The destination array for transformed coordinates.
     * @throws TransformException if this method failed to transform any of the points.
     */
    public static void xform(final MathTransform mt, final double[] src, final double[] dest)
            throws TransformException
    {
        final int sourceDim = mt.getSourceDimensions();
        final int targetDim = mt.getTargetDimensions();
        if (targetDim != sourceDim) {
            throw new MismatchedDimensionException();
        }
        boolean startPointTransformed = true;
        for (int i=0; i<src.length; i+=sourceDim) {
            try {
                mt.transform(src, i, dest, i, 1);
                if (!startPointTransformed) {
                    startPointTransformed = true;
                    for (int j=0; j<i; j++) {
                        System.arraycopy(dest, j, dest, i, targetDim);
                    }
                }
            } catch (TransformException e) {
                if (i==0) {
                    startPointTransformed = false;
                } else if (startPointTransformed) {
                    System.arraycopy(dest, i-targetDim, dest, i, targetDim);
                }
            }
        }
        if (!startPointTransformed) {
            // TODO: localize
            throw new TransformException("Unable to transform any of the points in the shape");
        }
    }

    /**
     * Converts an arbitrary Java2D shape into a JTS geometry. The created JTS geometry
     * may be any of {@link LineString}, {@link LinearRing} or {@link MultiLineString}.
     *
     * @param  shape    The Java2D shape to create.
     * @param  factory  The JTS factory to use for creating geometry.
     * @return The JTS geometry.
     */
    public static Geometry shapeToGeometry(final Shape shape, final GeometryFactory factory) {
        final PathIterator iterator = shape.getPathIterator(null, ShapeUtilities.getFlatness(shape));
        final double[] buffer = new double[6];
        final List     coords = new ArrayList();
        final List     lines  = new ArrayList();
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(buffer)) {
                /*
                 * Close the polygon: the last point is equal to
                 * the first point, and a LinearRing is created.
                 */
                case PathIterator.SEG_CLOSE: {
                    if (!coords.isEmpty()) {
                        coords.add((Coordinate[]) coords.get(0));
                        lines.add(factory.createLinearRing((Coordinate[]) coords.toArray(
                                                        new Coordinate[coords.size()])));
                        coords.clear();
                    }
                    break;
                }
                /*
                 * General case: A LineString is created from previous
                 * points, and a new LineString begin for next points.
                 */
                case PathIterator.SEG_MOVETO: {
                    if (!coords.isEmpty()) {
                        lines.add(factory.createLineString((Coordinate[]) coords.toArray(
                                                        new Coordinate[coords.size()])));
                        coords.clear();
                    }
                    // Fall through
                }
                case PathIterator.SEG_LINETO: {
                    coords.add(new Coordinate(buffer[0], buffer[1]));
                    break;
                }
                default: {
                    throw new IllegalPathStateException();
                }
            }
            iterator.next();
        }
        /*
         * End of loops: create the last LineString if any, then create the MultiLineString.
         */
        if (!coords.isEmpty()) {
            lines.add(factory.createLineString((Coordinate[]) coords.toArray(
                                            new Coordinate[coords.size()])));
        }
        switch (lines.size()) {
            case 0: return null;
            case 1: return (LineString) lines.get(0);
            default: {
                return factory.createMultiLineString(GeometryFactory.toLineStringArray(lines));
            }
        }
    }
}
