/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// J2SE dependencies
import java.util.Locale;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

// OpenGIS dependencies
import org.opengis.metadata.extent.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.measure.AngleFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.util.UnsupportedImplementationException;


/**
 * A set of static methods working on OpenGIS&reg;
 * {@linkplain CoordinateReferenceSystem coordinate reference system} objects.
 * Some of those methods are useful, but not really rigorous. This is why they
 * do not appear in the "official" package, but instead in this private one.
 * <strong>Do not rely on this API!</strong> It may change in incompatible way
 * in any future release.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CRSUtilities {
    /**
     * Do not allow creation of instances of this class.
     */
    private CRSUtilities() {
    }

    /**
     * Compare the specified objects for equality. If both objects are Geotools
     * implementations of {@linkplain org.geotools.referencing.IdentifiedObject},
     * then this method will ignore the metadata during the comparaison.
     *
     * @param  object1 The first object to compare (may be null).
     * @param  object2 The second object to compare (may be null).
     * @return <code>true</code> if both objects are equals.
     */
    public static boolean equalsIgnoreMetadata(final Object object1, final Object object2) {
        if (object1 instanceof org.geotools.referencing.IdentifiedObject &&
            object2 instanceof org.geotools.referencing.IdentifiedObject)
        {
            return ((org.geotools.referencing.IdentifiedObject) object1).equals(
                   ((org.geotools.referencing.IdentifiedObject) object2), false);
        }
        return Utilities.equals(object1, object2);
    }
    
    /**
     * Returns the dimension within the coordinate system of the first occurrence of an axis
     * colinear with the specified axis. If an axis with the same
     * {@linkplain CoordinateSystemAxis#getDirection direction} or an
     * {@linkplain AxisDirection#inverse opposite} direction than <code>axis</code>
     * ocurs in the coordinate system, then the dimension of the first such occurrence
     * is returned. That is, the a value <i>k</i> such that:
     *
     * <blockquote><pre>
     * cs.getAxis(<i>k</i>).getDirection().absolute() == axis.getDirection().absolute()
     * </pre></blockquote>
     *
     * is <code>true</code>. If no such axis occurs in this coordinate system,
     * then <code>-1</code> is returned.
     * <br><br>
     * For example, <code>dimensionColinearWith(CoordinateSystemAxis.TIME)</code>
     * returns the dimension number of time axis.
     *
     * @param  cs   The coordinate system to examine.
     * @param  axis The axis to look for.
     * @return The dimension number of the specified axis, or <code>-1</code> if none.
     */
    public static int dimensionColinearWith(final CoordinateSystem     cs,
                                            final CoordinateSystemAxis axis)
    {
        int candidate = -1;
        final int dimension = cs.getDimension();
        final AxisDirection direction = axis.getDirection().absolute();
        for (int i=0; i<dimension; i++) {
            final CoordinateSystemAxis xi = cs.getAxis(i);
            if (direction.equals(xi.getDirection().absolute())) {
                candidate = i;
                if (axis.equals(xi)) {
                    break;
                }
            }
        }
        return candidate;
    }
    
    /**
     * Returns the dimension of the first coordinate reference system of the given type. The
     * <code>type</code> argument must be a subinterface of {@link CoordinateReferenceSystem}.
     * If no such dimension is found, then this method returns <code>-1</code>.
     *
     * @param  crs  The coordinate reference system (CRS) to examine.
     * @param  type The CRS type to look for.
     *         Must be a subclass of {@link CoordinateReferenceSystem}.
     * @return The dimension range of the specified CRS type, or <code>-1</code> if none.
     * @throws IllegalArgumentException if the <code>type</code> is not legal.
     */
    public static int getDimensionOf(final CoordinateReferenceSystem crs, final Class type)
            throws IllegalArgumentException
    {
        if (!CoordinateReferenceSystem.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName());
        }
        if (type.isAssignableFrom(crs.getClass())) {
            return 0;
        }
        if (crs instanceof CompoundCRS) {
            final CoordinateReferenceSystem[] c= ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (int offset=0,i=0; i<c.length; i++) {
                final CoordinateReferenceSystem ci = c[i];
                final int index = getDimensionOf(ci, type);
                if (index >= 0) {
                    return index + offset;
                }
                offset += ci.getCoordinateSystem().getDimension();
            }
        }
        return -1;
    }

    /**
     * Returns a sub-coordinate reference system for the specified dimension range.
     *
     * @param  crs   The coordinate system to decompose.
     * @param  lower The first dimension to keep, inclusive.
     * @param  upper The last  dimension to keep, exclusive.
     * @return The sub-coordinate system, or <code>null</code> if <code>crs</code> can't
     *         be decomposed for dimensions in the range <code>[lower..upper]</code>.
     */
    public static CoordinateReferenceSystem getSubCRS(CoordinateReferenceSystem crs,
                                                      int lower, int upper)
    {
        int dimension = crs.getCoordinateSystem().getDimension();
        if (lower<0 || lower>upper || upper>dimension) {
            throw new IndexOutOfBoundsException(Resources.format(
                            ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1,
                            new Integer(lower<0 ? lower : upper)));
        }
        while (lower!=0 || upper!=dimension) {
            if (!(crs instanceof CompoundCRS)) {
                return null;
            }
            final CoordinateReferenceSystem[] c= ((CompoundCRS)crs).getCoordinateReferenceSystems();
            if (c.length == 0) {
                return null;
            }
            for (int i=0; i<c.length; i++) {
                crs = c[i];
                dimension = crs.getCoordinateSystem().getDimension();
                if (lower < dimension) {
                    break;
                }
                lower -= dimension;
                upper -= dimension;
            }
        }
        return crs;
    }
    
    /**
     * Returns a two-dimensional coordinate reference system representing the two first dimensions
     * of the specified coordinate reference system. If <code>crs</code> is already a
     * two-dimensional CRS, then it is returned unchanged. Otherwise, if it is a
     * {@link CompoundCRS}, then the head coordinate system is examined.
     *
     * @param  crs The coordinate system, or <code>null</code>.
     * @return A two-dimensional coordinate reference system that represents the two first
     *         dimensions of <code>crs</code>, or <code>null</code> if <code>crs</code> was
     *         <code>null</code>.
     * @throws TransformException if <code>crs</code> can't be reduced to a two-coordinate system.
     *         We use this exception class since this method is usually invoked in the context of
     *         a transformation process.
     */
    public static CoordinateReferenceSystem getCRS2D(CoordinateReferenceSystem crs)
            throws TransformException
    {
        if (crs != null) {
            while (crs.getCoordinateSystem().getDimension() != 2) {
                if (!(crs instanceof CompoundCRS)) {
                    throw new TransformException(Resources.format(
                            ResourceKeys.ERROR_CANT_REDUCE_TO_TWO_DIMENSIONS_$1,
                            crs.getName().toString()));
                }
                final CoordinateReferenceSystem[] c= ((CompoundCRS)crs).getCoordinateReferenceSystems();
                if (c.length == 0) {
                    return null;
                }
                crs = c[0];
            }
        }
        return crs;
    }
    
    /**
     * Returns the first horizontal coordinate reference system found in the given CRS,
     * or <code>null</code> if there is none.
     */
    public static CoordinateReferenceSystem getHorizontalCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof GeographicCRS || crs instanceof ProjectedCRS) {
            if (crs.getCoordinateSystem().getDimension() == 2) {
                return crs;
            }
        }
        if (crs instanceof CompoundCRS) {
            final CoordinateReferenceSystem[] c= ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (int i=0; i<c.length; i++) {
                final CoordinateReferenceSystem candidate = getHorizontalCRS(c[i]);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the first projected coordinate reference system found in a the given CRS,
     * or <code>null</code> if there is none.
     */
    public static ProjectedCRS getProjectedCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof ProjectedCRS) {
            return (ProjectedCRS) crs;
        }
        if (crs instanceof CompoundCRS) {
            final CoordinateReferenceSystem[] c= ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (int i=0; i<c.length; i++) {
                final ProjectedCRS candidate = getProjectedCRS(c[i]);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the first vertical coordinate reference system found in a the given CRS,
     * or <code>null</code> if there is none.
     */
    public static VerticalCRS getVerticalCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof VerticalCRS) {
            return (VerticalCRS) crs;
        }
        if (crs instanceof CompoundCRS) {
            final CoordinateReferenceSystem[] c= ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (int i=0; i<c.length; i++) {
                final VerticalCRS candidate = getVerticalCRS(c[i]);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the first temporal coordinate reference system found in the given CRS,
     * or <code>null</code> if there is none.
     */
    public static TemporalCRS getTemporalCRS(final CoordinateReferenceSystem crs) {
        if (crs instanceof TemporalCRS) {
            return (TemporalCRS) crs;
        }
        if (crs instanceof CompoundCRS) {
            final CoordinateReferenceSystem[] c= ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (int i=0; i<c.length; i++) {
                final TemporalCRS candidate = getTemporalCRS(c[i]);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Returns the datum of the specified CRS, or <code>null</code> if none.
     */
    public static Datum getDatum(final CoordinateReferenceSystem crs) {
        return (crs instanceof SingleCRS) ? ((SingleCRS) crs).getDatum() : null;
    }

    /**
     * Returns the first ellipsoid found in a coordinate reference system,
     * or <code>null</code> if there is none.
     */
    public static Ellipsoid getEllipsoid(final CoordinateReferenceSystem crs) {
        final Datum datum = getDatum(crs);
        if (datum instanceof GeodeticDatum) {
            return ((GeodeticDatum) datum).getEllipsoid();
        }
        if (crs instanceof CompoundCRS) {
            final CoordinateReferenceSystem[] c= ((CompoundCRS)crs).getCoordinateReferenceSystems();
            for (int i=0; i<c.length; i++) {
                final Ellipsoid candidate = getEllipsoid(c[i]);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Returns the ellipsoid used by the specified coordinate reference system, providing that
     * the two first dimensions use an instance of {@link GeographicCRS}. Otherwise (i.e. if the
     * two first dimensions are not geographic), returns <code>null</code>.
     */
    public static Ellipsoid getHeadGeoEllipsoid(CoordinateReferenceSystem crs) {
        while (!(crs instanceof GeographicCRS)) {
            if (crs instanceof CompoundCRS) {
                CoordinateReferenceSystem[] c = ((CompoundCRS)crs).getCoordinateReferenceSystems();
                if (c.length != 0) {
                    crs = c[0];
                    continue;
                }
            }
            return null;
        }
        // Remove first cast when covariance will be allowed.
        return ((GeodeticDatum) ((GeographicCRS) crs).getDatum()).getEllipsoid();
    }

    /**
     * Returns the bounding box of the specified coordinate reference system, or <code>null</code>
     * if none. This method search in the metadata informations.
     *
     * @param  crs The coordinate reference system, or <code>null</code>.
     * @return The envelope, or <code>null</code> if none.
     */
    public static Envelope getEnvelope(final CoordinateReferenceSystem crs) {
        if (crs != null) {
            final Datum datum = getDatum(crs);
            if (datum != null) {
                Extent validArea = datum.getValidArea();
                if (validArea != null) {
                    GeographicExtent geo = validArea.getGeographicElement();
                    if (geo instanceof GeographicBoundingBox) {
                        final GeographicBoundingBox bounds = (GeographicBoundingBox) geo;
                        return new GeneralEnvelope(new double[] {bounds.getEastBoundLongitude(),
                                                                 bounds.getWestBoundLongitude()},
                                                   new double[] {bounds.getSouthBoundLatitude(),
                                                                 bounds.getNorthBoundLatitude()});
                    }
                    if (geo instanceof BoundingPolygon) {
                        return ((BoundingPolygon) geo).getPolygon().getEnvelope();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Transform an envelope. The transformation is only approximative.
     *
     * @param  transform The transform to use.
     * @param  envelope Envelope to transform. This envelope will not be modified.
     * @return The transformed envelope. It may not have the same number of dimensions
     *         than the original envelope.
     * @throws TransformException if a transform failed.
     */
    public static Envelope transform(final MathTransform transform, final GeneralEnvelope envelope)
            throws TransformException
    {
        final int sourceDim = transform.getDimSource();
        final int targetDim = transform.getDimTarget();
        if (envelope.getDimension() != sourceDim) {
            throw new MismatchedDimensionException(Resources.format(
                      ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                      new Integer(sourceDim), new Integer(envelope.getDimension())));
        }
        int          coordinateNumber = 0;
        GeneralEnvelope   transformed = null;
        final GeneralDirectPosition sourcePt = new GeneralDirectPosition(sourceDim);
        final GeneralDirectPosition targetPt = new GeneralDirectPosition(targetDim);
        for (int i=sourceDim; --i>=0;) {
            sourcePt.setOrdinate(i, envelope.getMinimum(i));
        }
  loop: while (true) {
            // Transform a point and add the transformed point to the destination envelope.
            if (targetPt != transform.transform(sourcePt, targetPt)) {
                throw new UnsupportedImplementationException(transform.getClass());
            }
            if (transformed != null) {
                transformed.add(targetPt);
            } else {
                transformed = new GeneralEnvelope(targetPt, targetPt);
            }
            // Get the next point's coordinate.   The 'coordinateNumber' variable should
            // be seen as a number in base 3 where the number of digits is equals to the
            // number of dimensions. For example, a 4-D space would have numbers ranging
            // from "0000" to "2222". The digits are then translated into minimal, central
            // or maximal ordinates.
            int n = ++coordinateNumber;
            for (int i=sourceDim; --i>=0;) {
                switch (n % 3) {
                    case 0:  sourcePt.setOrdinate(i, envelope.getMinimum(i)); n/=3; break;
                    case 1:  sourcePt.setOrdinate(i, envelope.getCenter (i)); continue loop;
                    case 2:  sourcePt.setOrdinate(i, envelope.getMaximum(i)); continue loop;
                    default: throw new AssertionError(); // Should never happen
                }
            }
            break;
        }
        return transformed;
    }
    
    /**
     * Transform an envelope. The transformation is only approximative.
     * Invoking this method is equivalent to invoking the following:
     * <br>
     * <pre>transform(transform, new GeneralEnvelope(source)).toRectangle2D()</pre>
     *
     * @param  transform The transform to use. Source and target dimension must be 2.
     * @param  source The rectangle to transform (may be <code>null</code>).
     * @param  dest  The destination rectangle (may be <code>source</code>).
     *         If <code>null</code>, a new rectangle will be created and returned.
     * @return <code>dest</code>, or a new rectangle if <code>dest</code> was non-null
     *         and <code>source</code> was null.
     * @throws TransformException if a transform failed.
     */
    public static Rectangle2D transform(final MathTransform2D transform,
                                        final Rectangle2D     source,
                                        final Rectangle2D     dest)
            throws TransformException
    {
        if (source == null) {
            return null;
        }
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        final Point2D.Double point = new Point2D.Double();
        for (int i=0; i<8; i++) {
            /*
             *   (0)----(5)----(1)
             *    |             |
             *   (4)           (7)
             *    |             |
             *   (2)----(6)----(3)
             */
            point.x = (i&1)==0 ? source.getMinX() : source.getMaxX();
            point.y = (i&2)==0 ? source.getMinY() : source.getMaxY();
            switch (i) {
                case 5: // fallthrough
                case 6: point.x=source.getCenterX(); break;
                case 7: // fallthrough
                case 4: point.y=source.getCenterY(); break;
            }
            transform.transform(point, point);
            if (point.x<xmin) xmin=point.x;
            if (point.x>xmax) xmax=point.x;
            if (point.y<ymin) ymin=point.y;
            if (point.y>ymax) ymax=point.y;
        }
        if (dest != null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return XRectangle2D.createFromExtremums(xmin, ymin, xmax, ymax);
    }

    /**
     * Transforms the relative distance vector specified by <code>source</code> and stores
     * the result in <code>dest</code>.  A relative distance vector is transformed without
     * applying the translation components.
     *
     * @param transform The transform to apply.
     * @param origin The position where to compute the delta transform in the source CS.
     * @param source The distance vector to be delta transformed
     * @param dest   The resulting transformed distance vector, or <code>null</code>
     * @return       The result of the transformation.
     * @throws TransformException if the transformation failed.
     *
     * @see AffineTransform#deltaTransform(Point2D,Point2D)
     */
    public static Point2D deltaTransform(final MathTransform2D transform,
                                         final Point2D         origin,
                                         final Point2D         source,
                                               Point2D         dest)
            throws TransformException
    {
        if (transform instanceof AffineTransform) {
            return ((AffineTransform) transform).deltaTransform(source, dest);
        }
        final double ox = origin.getX();
        final double oy = origin.getY();
        final double dx = source.getX()*0.5;
        final double dy = source.getY()*0.5;
        Point2D P1 = new Point2D.Double(ox-dx, oy-dy);
        Point2D P2 = new Point2D.Double(ox+dx, oy+dy);
        P1 = transform.transform(P1, P1);
        P2 = transform.transform(P2, P2);
        if (dest == null) {
            dest = P2;
        }
        dest.setLocation(P2.getX()-P1.getX(), P2.getY()-P1.getY());
        return dest;
    }
    
    /**
     * Returns a character string for the specified geographic area. The string will have the
     * form "45°00.00'N-50°00.00'N 30°00.00'E-40°00.00'E". If a map projection is required in
     * order to obtain this representation, it will be automatically applied.  This string is
     * mostly used for debugging purpose.
     *
     * @todo Uncomment the transformation block once CoordinateTransformationFactory is implemented.
     */
    public static String toWGS84String(CoordinateReferenceSystem crs, Rectangle2D bounds) {
        StringBuffer buffer = new StringBuffer();
        try {
            crs = getCRS2D(crs);
            if (!equalsIgnoreMetadata(org.geotools.referencing.crs.GeographicCRS.WGS84, crs)) {
                throw new UnsupportedOperationException("Not yet implemented"); // TODO: to remove
//                final CoordinateTransformation tr = CoordinateTransformationFactory.getDefault().
//                               createFromCoordinateSystems(cs, GeographicCoordinateSystem.WGS84);
//                bounds = transform((MathTransform2D) tr.getMathTransform(), bounds, null);
            }
            final AngleFormat fmt = new AngleFormat("DD°MM.m'");
            buffer = fmt.format(new  Latitude(bounds.getMinY()), buffer, null); buffer.append('-');
            buffer = fmt.format(new  Latitude(bounds.getMaxY()), buffer, null); buffer.append(' ');
            buffer = fmt.format(new Longitude(bounds.getMinX()), buffer, null); buffer.append('-');
            buffer = fmt.format(new Longitude(bounds.getMaxX()), buffer, null);
        } catch (TransformException exception) {
            buffer.append(Utilities.getShortClassName(exception));
            final String message = exception.getLocalizedMessage();
            if (message != null) {
                buffer.append(": ");
                buffer.append(message);
            }
        }
        return buffer.toString();
    }
}
