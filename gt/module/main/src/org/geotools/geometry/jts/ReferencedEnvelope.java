/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

// JTS dependencies
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A JTS envelope associated with a
 * {@linkplain CoordinateReferenceSystem coordinate reference system}. In
 * addition, this JTS envelope also implements the GeoAPI
 * {@linkplain org.opengis.spatialschema.geometry.Envelope envelope} interface
 * for interoperability with GeoAPI.
 * 
 * @since 2.2
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/main/src/org/geotools/geometry/jts/ReferencedEnvelope.java $
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 * 
 * @see org.geotools.geometry.Envelope2D
 * @see org.geotools.geometry.GeneralEnvelope
 * @see org.opengis.metadata.extent.GeographicBoundingBox
 */
public class ReferencedEnvelope extends Envelope implements
		org.opengis.spatialschema.geometry.Envelope {
	/**
	 * Serial number for compatibility with different versions.
	 */
	private static final long serialVersionUID = -3188702602373537163L;

	/**
	 * The coordinate reference system, or {@code null}.
	 */
	private final CoordinateReferenceSystem crs;

	/**
	 * Creates a null envelope with the specified coordinate reference system.
	 * 
	 * @param crs
	 *            The coordinate reference system.
	 * @throws IllegalArgumentException
	 *             if the CRS dimension is not valid.
	 */
	public ReferencedEnvelope(final CoordinateReferenceSystem crs) {
		this.crs = crs;
		checkCoordinateReferenceSystemDimension();
	}

	/**
	 * Creates an envelope for a region defined by maximum and minimum values.
	 * 
	 * @param x1
	 *            the first x-value.
	 * @param x2
	 *            the second x-value.
	 * @param y1
	 *            the first y-value.
	 * @param y2
	 *            the second y-value.
	 * @param crs
	 *            The coordinate reference system.
	 * 
	 * @throws IllegalArgumentException
	 *             if the CRS dimension is not valid.
	 */
	public ReferencedEnvelope(final double x1, final double x2,
			final double y1, final double y2,
			final CoordinateReferenceSystem crs) {
		super(x1, x2, y1, y2);
		this.crs = crs;
		checkCoordinateReferenceSystemDimension();
	}

	/**
	 * Creates a new envelope from an existing envelope.
	 * 
	 * @param envelope
	 *            The Envelope to initialize from
	 * @param crs
	 *            The coordinate reference system.
	 * @throws IllegalArgumentException
	 *             if the CRS dimension is not valid.
	 */
	public ReferencedEnvelope(final Envelope envelope,
			final CoordinateReferenceSystem crs) {
		super(envelope);
		this.crs = crs;
		checkCoordinateReferenceSystemDimension();
	}

	/**
	 * Creates a new envelope from an existing envelope.
	 * 
	 * @param envelope
	 *            The Envelope to initialize from
	 * @param crs
	 *            The coordinate reference system.
	 * @throws IllegalArgumentException
	 *             if the CRS dimension is not valid.
	 */
	public ReferencedEnvelope(
			final org.opengis.spatialschema.geometry.Envelope envelope,
			final CoordinateReferenceSystem crs) {
		super(getJTSEnvelope(envelope, crs));
		this.crs = crs;
		checkCoordinateReferenceSystemDimension();
	}

	/**
	 * Creates a new envelope from an existing envelope.
	 */
	public ReferencedEnvelope(ReferencedEnvelope env) {
		super(env);
		this.crs = env.getCoordinateReferenceSystem();
		checkCoordinateReferenceSystemDimension();
	}

	/**
	 * Get a JTS envelope out of an OGC Envelope
	 * 
	 * @param envelope
	 * @param crs
	 * @return
	 */
	private static Envelope getJTSEnvelope(
			org.opengis.spatialschema.geometry.Envelope envelope,
			CoordinateReferenceSystem crs) {
		try {
			final Envelope2D envelope2D = new Envelope2D(envelope);
			envelope2D.setCoordinateReferenceSystem(crs);
			return new Envelope(envelope2D.getMinX(), envelope2D.getMaxX(),
					envelope2D.getMinY(), envelope2D.getMaxY());

		} catch (IllegalStateException e) {
			final IllegalArgumentException ex = new IllegalArgumentException();
			ex.initCause(e);
			throw ex;
		}

	}

	/**
	 * Convenience method for checking coordinate reference system validity.
	 * 
	 * @throws IllegalArgumentException
	 *             if the CRS dimension is not valid.
	 */
	private void checkCoordinateReferenceSystemDimension()
			throws IllegalArgumentException {
		if (crs != null) {
			final int expected = getDimension();
			final int dimension = crs.getCoordinateSystem().getDimension();
			if (dimension != expected) {
				throw new IllegalArgumentException(Errors.format(
						ErrorKeys.MISMATCHED_DIMENSION_$3, crs.getName()
								.getCode(), new Integer(dimension),
						new Integer(expected)));
			}
		}
	}

	/**
	 * Returns the coordinate reference system associated with this envelope.
	 * 
	 * @deprecated Replaced by {@link #getCoordinateReferenceSystem} for
	 *             consistency with other envelope implementations, and also
	 *             because a future GeoAPI release may provides a
	 *             {@code getCoordinateReferenceSystem()} method in their
	 *             interface.
	 */
	public CoordinateReferenceSystem getCRS() {
		return crs;
	}

	/**
	 * Returns the coordinate reference system associated with this envelope.
	 */
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return crs;
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
			throw new ArrayIndexOutOfBoundsException(dimension);
		}
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
			throw new ArrayIndexOutOfBoundsException(dimension);
		}
	}

	/**
	 * Returns the center ordinate along the specified dimension.
	 */
	public double getCenter(final int dimension) {
		switch (dimension) {
		case 0:
			return 0.5 * (getMinX() + getMaxX());
		case 1:
			return 0.5 * (getMinY() + getMaxY());
		default:
			throw new ArrayIndexOutOfBoundsException(dimension);
		}
	}

	/**
	 * Returns the envelope length along the specified dimension. This length is
	 * equals to the maximum ordinate minus the minimal ordinate.
	 */
	public double getLength(final int dimension) {
		switch (dimension) {
		case 0:
			return getWidth();
		case 1:
			return getHeight();
		default:
			throw new ArrayIndexOutOfBoundsException(dimension);
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
	 * Transforms the referenced envelope to the specified coordinate reference
	 * system.
	 * 
	 * @param targetCRS
	 *            The target coordinate reference system.
	 * @param lenient
	 *            {@code true} if datum shift should be applied even if there is
	 *            insuffisient information. Otherwise (if {@code false}), an
	 *            exception is thrown in such case.
	 * @return The transformed envelope.
	 * @throws FactoryException
	 *             if the math transform can't be determined.
	 * @throws TransformException
	 *             if at least one coordinate can't be transformed.
	 */
	public ReferencedEnvelope transform(
			final CoordinateReferenceSystem targetCRS, final boolean lenient)
			throws TransformException, FactoryException {

		return transform(targetCRS, lenient, 5);

	}

	/**
	 * Transforms the referenced envelope to the specified coordinate reference
	 * system.
	 * 
	 * @param targetCRS
	 *            The target coordinate reference system.
	 * @param lenient
	 *            {@code true} if datum shift should be applied even if there is
	 *            insuffisient information. Otherwise (if {@code false}), an
	 *            exception is thrown in such case.
	 * @return The transformed envelope.
	 * @throws FactoryException
	 *             if the math transform can't be determined.
	 * @throws TransformException
	 *             if at least one coordinate can't be transformed.
	 */
	public ReferencedEnvelope transform(
			final CoordinateReferenceSystem targetCRS, final boolean lenient,
			final int numPointsForTransformation) throws TransformException,
			FactoryException {

		// COMMENTED OUT NEW CODE, CRS.transform does provide the flipping
		// if needed, adding it again introduces errors

		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the transform
		//
		// /////////////////////////////////////////////////////////////////////
		MathTransform transform = CRS.transform(crs, targetCRS, lenient);

		// /////////////////////////////////////////////////////////////////////
		//
		// Transforming
		//
		// /////////////////////////////////////////////////////////////////////
		final ReferencedEnvelope target = new ReferencedEnvelope(targetCRS);
		JTS.transform(this, target, transform, numPointsForTransformation);

		return target;
	}

	/**
	 * Returns a hash value for this envelope. This value need not remain
	 * consistent between different implementations of the same class.
	 */
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
	public boolean equals(final Object object) {
		if (super.equals(object)) {
			final CoordinateReferenceSystem otherCRS = (object instanceof ReferencedEnvelope) ? ((ReferencedEnvelope) object).crs
					: null;
			return Utilities.equals(crs, otherCRS);
		}
		return false;
	}

	/**
	 * Returns a string representation of this envelope. The default
	 * implementation formats the {@linkplain #getLowerCorner lower} and
	 * {@linkplain #getUpperCorner upper} corners using a shared instance of
	 * {@link org.geotools.measure.CoordinateFormat}. This is okay for
	 * occasional formatting (for example for debugging purpose). But if there
	 * is a lot of positions to format, users will get better performance and
	 * more control by using their own instance of
	 * {@link org.geotools.measure.CoordinateFormat}.
	 */
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append('[');
		buffer.append(GeneralDirectPosition.toString(getLowerCorner()));
		buffer.append(" , ");
		buffer.append(GeneralDirectPosition.toString(getUpperCorner()));
		buffer.append(']');
		return buffer.toString();
	}
}
