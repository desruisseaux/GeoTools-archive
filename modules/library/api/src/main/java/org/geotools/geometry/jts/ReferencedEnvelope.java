/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.awt.geom.Rectangle2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.resources.Classes;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;

/**
 * A JTS envelope associated with a
 * {@linkplain CoordinateReferenceSystem coordinate reference system}. In
 * addition, this JTS envelope also implements the GeoAPI
 * {@linkplain org.opengis.geometry.coordinate.Envelope envelope} interface
 * for interoperability with GeoAPI.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 * @author Simone Giannecchini
 *
 * @see org.geotools.geometry.Envelope2D
 * @see org.geotools.geometry.GeneralEnvelope
 * @see org.opengis.metadata.extent.GeographicBoundingBox
 */
public class ReferencedEnvelope extends Envelope implements org.opengis.geometry.Envelope,
    BoundingBox {
	/** A ReferencedEnvelope containing "everything" */
	public static ReferencedEnvelope EVERYTHING = new ReferencedEnvelope(
			Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,null){
	    private static final long serialVersionUID = -3188702602373537164L;		
		public boolean contains(BoundingBox bbox) {
			return true;
		}
		public boolean contains(Coordinate p) {
			return true;			
		}
		public boolean contains(DirectPosition pos) {
			return true;
		}
		public boolean contains(double x, double y) {
			return true;
		}
		public boolean contains(Envelope other) {
			return true;
		}	
		public boolean isEmpty() {
			return false;
		}
		public boolean isNull() {
			return true;
		}		
	};
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -3188702602373537163L;

    /**
     * The coordinate reference system, or {@code null}.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Creates a null envelope with a null coordinate reference system.
     */
    public ReferencedEnvelope() {
        this((CoordinateReferenceSystem) null);
    }

    /**
     * Creates a null envelope with the specified coordinate reference system.
     *
     * @param crs The coordinate reference system.
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     */
    public ReferencedEnvelope(CoordinateReferenceSystem crs)
        throws MismatchedDimensionException {
        this.crs = crs;
        checkCoordinateReferenceSystemDimension();
    }

    /**
     * Creates an envelope for a region defined by maximum and minimum values.
     *
     * @param x1  The first x-value.
     * @param x2  The second x-value.
     * @param y1  The first y-value.
     * @param y2  The second y-value.
     * @param crs The coordinate reference system.
     *
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     */
    public ReferencedEnvelope(final double x1, final double x2, final double y1, final double y2,
        final CoordinateReferenceSystem crs) throws MismatchedDimensionException {
        super(x1, x2, y1, y2);
        this.crs = crs;
        checkCoordinateReferenceSystemDimension();
    }

    /**
     * Creates an envelope for a Java2D rectangle.
     *
     * @param rectangle The rectangle.
     * @param crs The coordinate reference system.
     *
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     *
     * @since 2.4
     */
    public ReferencedEnvelope(final Rectangle2D rectangle, final CoordinateReferenceSystem crs)
        throws MismatchedDimensionException {
        this(rectangle.getMinX(), rectangle.getMaxX(), rectangle.getMinY(), rectangle.getMaxY(), crs);
    }

    /**
     * Creates a new envelope from an existing envelope.
     *
     * @param envelope The envelope to initialize from
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     *
     * @since 2.3
     */
    public ReferencedEnvelope(final ReferencedEnvelope envelope)
        throws MismatchedDimensionException {
        super(envelope);
        crs = envelope.getCoordinateReferenceSystem();
        checkCoordinateReferenceSystemDimension();
    }

    /**
     * Creates a new envelope from an existing bounding box.
     *
     * @param bbox The bounding box to initialize from.
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     *
     * @since 2.4
     */
    public ReferencedEnvelope(final BoundingBox bbox) throws MismatchedDimensionException {
        this(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY(),
            bbox.getCoordinateReferenceSystem());
    }

    /**
     * Creates a new envelope from an existing OGC envelope.
     *
     * @param envelope The envelope to initialize from.
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     *
     * @since 2.4
     */
    public ReferencedEnvelope(final org.opengis.geometry.Envelope envelope)
        throws MismatchedDimensionException {
        super(envelope.getMinimum(0), envelope.getMaximum(0), envelope.getMinimum(1),
            envelope.getMaximum(1));
        this.crs = envelope.getCoordinateReferenceSystem();
        checkCoordinateReferenceSystemDimension();
    }

    /**
     * Creates a new envelope from an existing JTS envelope.
     *
     * @param envelope The envelope to initialize from.
     * @param crs The coordinate reference system.
     * @throws MismatchedDimensionExceptionif the CRS dimension is not valid.
     */
    public ReferencedEnvelope(final Envelope envelope, final CoordinateReferenceSystem crs)
        throws MismatchedDimensionException {
        super(envelope);
        this.crs = crs;
        checkCoordinateReferenceSystemDimension();
    }

    /**
     * Sets this envelope to the specified bounding box.
     */
    public void init(BoundingBox bounds) {
        super.init(bounds.getMinimum(0), bounds.getMaximum(0), bounds.getMinimum(1),
            bounds.getMaximum(1));
        this.crs = bounds.getCoordinateReferenceSystem();
    }

    /**
     * Returns the specified bounding box as a JTS envelope.
     */
    private static Envelope getJTSEnvelope(final BoundingBox bbox) {
        if (bbox instanceof Envelope) {
            return (Envelope) bbox;
        }
        return new ReferencedEnvelope(bbox);
    }

    /**
     * Convenience method for checking coordinate reference system validity.
     *
     * @throws IllegalArgumentException if the CRS dimension is not valid.
     */
    private void checkCoordinateReferenceSystemDimension()
        throws MismatchedDimensionException {
        if (crs != null) {
            final int expected = getDimension();
            final int dimension = crs.getCoordinateSystem().getDimension();
            if (dimension != expected) {
                throw new MismatchedDimensionException(Errors.format(
                        ErrorKeys.MISMATCHED_DIMENSION_$3, crs.getName().getCode(),
                        new Integer(dimension), new Integer(expected)));
            }
        }
    }

    /**
     * Make sure that the specified bounding box uses the same CRS than this one.
     *
     * @param  bbox The other bounding box to test for compatibility.
     * @throws MismatchedReferenceSystemException if the CRS are incompatibles.
     */
    private void ensureCompatibleReferenceSystem(final BoundingBox bbox)
        throws MismatchedReferenceSystemException {
        if (crs != null) {
            final CoordinateReferenceSystem other = bbox.getCoordinateReferenceSystem();
            if (other != null) {
                if (!CRS.equalsIgnoreMetadata(crs, other)) {
                    throw new MismatchedReferenceSystemException(Errors.format(
                            ErrorKeys.MISMATCHED_COORDINATE_REFERENCE_SYSTEM));
                }
            }
        }
    }

    /**
     * Returns the coordinate reference system associated with this envelope.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * @deprecated Use {@link #getCoordinateReferenceSystem}, since it is the
     * method inherited from the interface.
     */
    public CoordinateReferenceSystem crs() {
        return getCoordinateReferenceSystem();
    }

    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        return 2;
    }

    /**
     * Returns the minimal ordinate along the specified dimension.
     */
    public double getMinimum(final int dimension) {
        switch (dimension) {
        case 0:
            return getMinX();

        case 1:
            return getMinY();

        default:
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * @deprecated Use {@link #getMinX}.
     */
    public double minX() {
        return getMinX();
    }

    /**
     * @deprecated Use {@link #getMinY}.
     */
    public double minY() {
        return getMinY();
    }

    /**
     * Returns the maximal ordinate along the specified dimension.
     */
    public double getMaximum(final int dimension) {
        switch (dimension) {
        case 0:
            return getMaxX();

        case 1:
            return getMaxY();

        default:
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * @deprecated Use {@link #getMaxX}.
     */
    public double maxX() {
        return getMaxX();
    }

    /**
     * @deprecated Use {@link #getMaxY}.
     */
    public double maxY() {
        return getMaxY();
    }

    /**
     * @deprecated Use {@link #getMedian}.
     */
    public double getCenter(final int dimension) {
        return getMedian(dimension);
    }

    /**
     * Returns the center ordinate along the specified dimension.
     */
    public double getMedian(final int dimension) {
        switch (dimension) {
        case 0:
            return 0.5 * (getMinX() + getMaxX());

        case 1:
            return 0.5 * (getMinY() + getMaxY());

        default:
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * @deprecated Use {@link #getSpan}.
     */
    public double getLength(final int dimension) {
        return getSpan(dimension);
    }

    /**
     * Returns the envelope length along the specified dimension. This length is
     * equals to the maximum ordinate minus the minimal ordinate.
     */
    public double getSpan(final int dimension) {
        switch (dimension) {
        case 0:
            return getWidth();

        case 1:
            return getHeight();

        default:
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * A coordinate position consisting of all the minimal ordinates for each
     * dimension for all points within the {@code Envelope}.
     */
    public DirectPosition getLowerCorner() {
        return new DirectPosition2D(crs, getMinX(), getMinY());
    }

    /**
     * A coordinate position consisting of all the maximal ordinates for each
     * dimension for all points within the {@code Envelope}.
     */
    public DirectPosition getUpperCorner() {
        return new DirectPosition2D(crs, getMaxX(), getMaxY());
    }

    /**
     * Returns {@code true} if lengths along all dimension are zero.
     *
     * @since 2.4
     */
    public boolean isEmpty() {
        return super.isNull();
    }

    /**
     * Returns {@code true} if the provided location is contained by this bounding box.
     *
     * @since 2.4
     */
    public boolean contains(DirectPosition pos) {
        return super.contains(pos.getOrdinate(0), pos.getOrdinate(1));
    }

    /**
     * Returns {@code true} if the provided bounds are contained by this bounding box.
     *
     * @since 2.4
     */
    public boolean contains(final BoundingBox bbox) {
        ensureCompatibleReferenceSystem(bbox);

        return super.contains(getJTSEnvelope(bbox));
    }

    /**
     * Check if this bounding box intersects the provided bounds.
     *
     * @since 2.4
     */
    public boolean intersects(final BoundingBox bbox) {
        ensureCompatibleReferenceSystem(bbox);

        return super.intersects(getJTSEnvelope(bbox));
    }

    /**
     * Include the provided bounding box, expanding as necessary.
     *
     * @since 2.4
     */
    public void include(final BoundingBox bbox) {
        ensureCompatibleReferenceSystem(bbox);
        super.expandToInclude(getJTSEnvelope(bbox));
    }

    /**
     * Include the provided coordinates, expanding as necessary.
     *
     * @since 2.4
     */
    public void include(double x, double y) {
        super.expandToInclude(x, y);
    }

    /**
     * Initialize the bounding box with another bounding box.
     *
     * @since 2.4
     */
    public void setBounds(final BoundingBox bbox) {
        ensureCompatibleReferenceSystem(bbox);
        super.init(getJTSEnvelope(bbox));
    }

    /**
     * Returns a new bounding box which contains the transformed shape of this bounding box.
     * This is a convenience method that delegate its work to the {@link #transform transform}
     * method.
     *
     * @since 2.4
     */
    public BoundingBox toBounds(final CoordinateReferenceSystem targetCRS)
        throws TransformException {
        try {
            return transform(targetCRS, true);
        } catch (FactoryException e) {
            throw new TransformException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Transforms the referenced envelope to the specified coordinate reference system.
     * <p>
     * This method can handle the case where the envelope contains the North or South pole,
     * or when it cross the &plusmn;180ï¿½ longitude.
     *
     * @param targetCRS The target coordinate reference system.
     * @param lenient   {@code true} if datum shift should be applied even if there is
     *                  insuffisient information. Otherwise (if {@code false}), an
     *                  exception is thrown in such case.
     * @return The transformed envelope.
     * @throws FactoryException if the math transform can't be determined.
     * @throws TransformException if at least one coordinate can't be transformed.
     *
     * @see CRS#transform(CoordinateOperation, org.opengis.geometry.Envelope)
     */
    public ReferencedEnvelope transform(CoordinateReferenceSystem targetCRS, boolean lenient)
        throws TransformException, FactoryException {
        return transform(targetCRS, lenient, 5);
    }

    /**
     * Transforms the referenced envelope to the specified coordinate reference system
     * using the specified amount of points.
     * <p>
     * This method can handle the case where the envelope contains the North or South pole,
     * or when it cross the &plusmn;180ï¿½ longitude.
     *
     * @param targetCRS The target coordinate reference system.
     * @param lenient   {@code true} if datum shift should be applied even if there is
     *                  insuffisient information. Otherwise (if {@code false}), an
     *                  exception is thrown in such case.
     * @param numPointsForTransformation The number of points to use for sampling the envelope.
     * @return The transformed envelope.
     * @throws FactoryException if the math transform can't be determined.
     * @throws TransformException if at least one coordinate can't be transformed.
     *
     * @see CRS#transform(CoordinateOperation, org.opengis.geometry.Envelope)
     *
     * @since 2.3
     */
    public ReferencedEnvelope transform(final CoordinateReferenceSystem targetCRS,
        final boolean lenient, final int numPointsForTransformation)
        throws TransformException, FactoryException {
        if( crs == null ){
            if( isEmpty() ){
                // We don't have a CRS yet because we are still empty, being empty is
                // something we can represent in the targetCRS
                return new ReferencedEnvelope(targetCRS);
            }
            else {
                // really this is a the code that created this ReferencedEnvelope
                throw new NullPointerException("Unable to transform referenced envelope, crs has not yet been provided.");
            }
        }

        /*
         * Gets a first estimation using an algorithm capable to take singularity in account
         * (North pole, South pole, 180ï¿½ longitude). We will expand this initial box later.
         */
        CoordinateOperationFactory coordinateOperationFactory = CRS.getCoordinateOperationFactory(lenient);

        final CoordinateOperation operation = coordinateOperationFactory.createOperation(crs, targetCRS);
        final GeneralEnvelope transformed = CRS.transform(operation, this);
        transformed.setCoordinateReferenceSystem(targetCRS);

        /*
         * Now expands the box using the usual utility methods.
         */
        final ReferencedEnvelope target = new ReferencedEnvelope(transformed);
        final MathTransform transform = operation.getMathTransform();
        JTS.transform(this, target, transform, numPointsForTransformation);

        return target;
    }

    /**
     * Returns a hash value for this envelope. This value need not remain
     * consistent between different implementations of the same class.
     */
    @Override
    public int hashCode() {
        int code = super.hashCode() ^ (int) serialVersionUID;
        if (crs != null) {
            code ^= crs.hashCode();
        }
        return code;
    }

    /**
     * Compares the specified object with this envelope for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final CoordinateReferenceSystem otherCRS = (object instanceof ReferencedEnvelope)
                ? ((ReferencedEnvelope) object).crs : null;

            return Utilities.equals(crs, otherCRS);
        }
        return false;
    }

    /**
     * Returns a string representation of this envelope. The default implementation
     * is okay for occasional formatting (for example for debugging purpose).
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(this)).append('[');
        final int dimension = getDimension();

        for (int i = 0; i < dimension; i++) {
            if (i != 0) {
                buffer.append(", ");
            }

            buffer.append(getMinimum(i)).append(" : ").append(getMaximum(i));
        }

        return buffer.append(']').toString();
    }
    
    /**
     * Utility method to ensure that an Envelope if a ReferencedEnvelope.
     * <p>
     * This method first checks if <tt>e</tt> is an instanceof {@link ReferencedEnvelope},
     * if it is, itself is returned. If not <code>new ReferencedEnvelpe(e,null)</code>
     * is returned.
     * </p>
     * <p>
     * If e is null, null is returned.
     * </p>
     * @param e The envelope.  Can be null.
     * @return A ReferencedEnvelope using the specified envelope, or null if the envelope was null.
     */
    public static ReferencedEnvelope reference(Envelope e) {
        if (e == null) {
            return null;
        } else {
            if (e instanceof ReferencedEnvelope) {
                return (ReferencedEnvelope) e;
            }

            return new ReferencedEnvelope(e, null);
        }
    }

    /**
     * Utility method to ensure that an BoundingBox in a ReferencedEnvelope.
     * <p>
     * This method first checks if <tt>e</tt> is an instanceof {@link ReferencedEnvelope},
     * if it is, itself is returned. If not <code>new ReferencedEnvelpe(e)</code>
     * is returned.
     * </p>
     * @param e The envelope.
     * @return
     */
    public static ReferencedEnvelope reference(BoundingBox e) {
        if (e == null) {
            return null;
        }

        if (e instanceof ReferencedEnvelope) {
            return (ReferencedEnvelope) e;
        }

        return new ReferencedEnvelope(e);
    }
}
