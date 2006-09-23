/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation;

import java.lang.ref.Reference;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.factory.Hints;
import org.geotools.referencing.operation.matrix.MatrixFactory;
import org.geotools.resources.CRSUtilities;
import org.geotools.util.LRULinkedHashMap;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.OperationNotFoundException;

/**
 * The purpose of this class is to buffer {@link CoordinateOperation} objects
 * built from {@link CoordinateReferenceSystem} objects which can be very
 * expensive to create.
 * <p>
 * During rendering and during data I/O we make use a lof of coordinate
 * transformations, hence caching them might help out a lot.
 * <p>
 * Please note that the deprected method for creaating trasnformation actually
 * throws an {@link UnsupportedOperationException}.
 * 
 * @author Simone Giannecchini
 * @since 2.3
 *
 * @deprecated Replaced by {@link BufferedCoordinateOperationFactory}. User should not need to
 *  create explicitly instance of this class, since buffered coordinate operation factory are
 *  now registered and returned by {@link org.geotools.referencing.FactoryFinder}.
 */
public final class BufferedDefaultCoordinateOperationFactory extends
		DefaultCoordinateOperationFactory {

	/** Logger. */
	private final static Logger LOGGER = Logger
			.getLogger(BufferedDefaultCoordinateOperationFactory.class
					.toString());

	/**
	 * This class is an helper class that can be used in order to build an
	 * hashing for a pair of source-destination
	 * <code>CoordinateReferenceSystem</code> objects.
	 * <p>
	 * This hash is used to cache the transformations that are pretty
	 * time-consuming to build each time.
	 * 
	 * @author simone Giannecchini
	 * @since 2.3
	 */
	private static final class CRSTransformHashUtil {
		private int hash;

		private CoordinateReferenceSystem sourceCRS;

		private CoordinateReferenceSystem destinationCRS;

		/**
		 * Constructor.
		 * 
		 * @param sourceCRS
		 * @param destinationCRS
		 */
		public CRSTransformHashUtil(final CoordinateReferenceSystem sourceCRS,
				final CoordinateReferenceSystem destinationCRS) {
			this.sourceCRS = sourceCRS;
			this.destinationCRS = destinationCRS;
			this.hash = (37 * sourceCRS.hashCode()) + destinationCRS.hashCode();
		}

		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				return true;
			}

			if (obj instanceof CRSTransformHashUtil) {
				final CRSTransformHashUtil obj1 = (CRSTransformHashUtil) obj;

				return CRSUtilities.equalsIgnoreMetadata(obj1.getSourceCRS(),
						sourceCRS)
						&& CRSUtilities.equalsIgnoreMetadata(obj1
								.getDestinationCRS(), destinationCRS);
			}

			return false;
		}

		public int hashCode() {
			return hash;
		}

		public CoordinateReferenceSystem getDestinationCRS() {
			return destinationCRS;
		}

		public CoordinateReferenceSystem getSourceCRS() {
			return sourceCRS;
		}
	}

	/**
	 * The default maximum number of cached transformations.
	 */
	private static final int DEFAULT_MAX = 100;

	/**
	 * The default minimum number of cached transformations.
	 */
	private static final int DEFAULT_MIN = 30;

	/**
	 * The pool of cached objects.
	 */
	private final static Map pool = Collections
			.synchronizedMap(new LRULinkedHashMap(DEFAULT_MIN, 0.75f, true,
					DEFAULT_MAX));

	/**
	 * Returns an object from the pool for the specified key.
	 */
	private Object get(final Object key) {
		if (key == null)
			throw new IllegalArgumentException("Provided key cannot be null!");

		// //
		//
		// Identity is not enough let's check the pool
		//
		// //
		synchronized (pool) {

			Object object = pool.get(key);
			if (object == null) {

				// //
				//
				// Found in the pool
				//
				// //
				try {
					// //
					//
					// Check if we actually have to create and operation or if
					// identity is
					// enough
					//
					// //
					final CRSTransformHashUtil crsHash = (CRSTransformHashUtil) key;
					final CoordinateReferenceSystem sourceCRS = crsHash
							.getSourceCRS();
					final CoordinateReferenceSystem destinationCRS = crsHash
							.getDestinationCRS();

					if (equalsIgnoreMetadata(sourceCRS, destinationCRS)) {
						final int dim = getDimension(sourceCRS);
						assert dim == getDimension(destinationCRS) : dim;
						try {
							object = createFromAffineTransform(IDENTITY,
									sourceCRS, destinationCRS, MatrixFactory
											.create(dim + 1));
						} catch (FactoryException e) {
							LOGGER
									.log(Level.SEVERE, e.getLocalizedMessage(),
											e);
							return null;
						}
					} else
						object = super.createOperation(sourceCRS,
								destinationCRS);
				} catch (OperationNotFoundException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					return null;
				} catch (FactoryException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					return null;
				}
				put(key, object);
				return object;

			} else {
				// //
				//
				// Found in the pool
				//
				// //
				return object;
			}

		}
	}

	/**
	 * Put an element back in the pool.
	 * 
	 * @param key
	 * @param object
	 */
	private void put(final Object key, final Object object) {
		synchronized (pool) {
			pool.put(key, object);

		}
	}

	/**
	 * Default constructor.
	 */
	public BufferedDefaultCoordinateOperationFactory() {
		super(null);

	}

	/**
	 * Constructor that lets the user provide hints for the underlying
	 * {@link DefaultCoordinateOperationFactory}.
	 * 
	 * @param hints
	 */
	public BufferedDefaultCoordinateOperationFactory(Hints hints) {
		super(hints, NORMAL_PRIORITY);

	}

	/**
	 * Constructor that lets the user provide hints and priority for the
	 * underlying {@link DefaultCoordinateOperationFactory}.
	 * 
	 * @param hints
	 * @param priority
	 */
	public BufferedDefaultCoordinateOperationFactory(Hints hints, int priority) {
		super(hints, priority);

	}

	/**
	 * Creates a {@link CoordinateOperation} out of a source and target
	 * {@link CoordinateReferenceSystem}.
	 */
	public CoordinateOperation createOperation(
			CoordinateReferenceSystem sourceCRS,
			CoordinateReferenceSystem targetCRS)
			throws OperationNotFoundException, FactoryException {
		return (CoordinateOperation) get(new CRSTransformHashUtil(sourceCRS,
				targetCRS));
	}

	/**
	 * We are violating the contract stated by
	 * {@link DefaultCoordinateOperationFactory} here but supporting this method
	 * is beyond the scope of this class and by the way this method is
	 * deprecated.
	 */
	public CoordinateOperation createOperation(
			CoordinateReferenceSystem sourceCRS,
			CoordinateReferenceSystem targetCRS, OperationMethod method)
			throws OperationNotFoundException, FactoryException {
		throw new UnsupportedOperationException(
				"This class does not support this deprectaed method.");
	}

}
