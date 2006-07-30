/**
 * 
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
import org.opengis.referencing.operation.OperationNotFoundException;

/**
 * The purpose of this class is to buffer coordinate operations built from
 * <code>CoordinateReferenceSystem</code> objects which can be very expensive
 * to create.
 * 
 * <p>
 * During rendering and during data I/O we make use a lof of coordinate
 * transformations, hence caching them might help out a lot.
 * 
 * @author simone
 * @since 2.3
 * 
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
	 * 
	 * <p>
	 * This hash is used to cache the transformations that are pretty
	 * time-consuming to build each time.
	 * 
	 * @author simone Giannecchini
	 * @since 2.3
	 */
	private final class CRSTransformHashUtil {
		private int hash;

		private CoordinateReferenceSystem sourceCRS;

		private CoordinateReferenceSystem destinationCRS;

		public CRSTransformHashUtil(final CoordinateReferenceSystem sourceCRS,
				final CoordinateReferenceSystem destinationCRS) {
			this.sourceCRS = sourceCRS;
			this.destinationCRS = destinationCRS;
			this.hash = (3 * sourceCRS.hashCode())
					+ (2 * destinationCRS.hashCode());
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
	 * The default value for {@link #maxStrongReferences} .
	 */
	public final int DEFAULT_MAX = 100;

	/**
	 * The pool of cached objects.
	 */
	private final static Map pool = Collections.synchronizedMap(new LRULinkedHashMap(
			50, 0.75f, true, 100));

	/**
	 * Returns an object from the pool for the specified code. If the object was
	 * retained as a {@linkplain Reference weak reference}, the
	 * {@link Reference#get referent} is returned.
	 * 
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
					// Check if we actually have to create and operation or if identity is
					// enough
					//
					// //
					final CRSTransformHashUtil crsHash = (CRSTransformHashUtil) key;
					final CoordinateReferenceSystem sourceCRS = crsHash.getSourceCRS();
					final CoordinateReferenceSystem destinationCRS = crsHash
							.getDestinationCRS();

			        if (equalsIgnoreMetadata(sourceCRS, destinationCRS)) {
			            final int dim  = getDimension(sourceCRS);
			            assert    dim == getDimension(destinationCRS) : dim;
			            try {
							object= createFromAffineTransform(IDENTITY, sourceCRS, destinationCRS,
							                                 MatrixFactory.create(dim+1));
						} catch (FactoryException e) {
							LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
							return null;
						}
			        }
			        else
			        	object = super.createOperation(sourceCRS, destinationCRS);
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
				if (object instanceof Reference) {
					object = ((Reference) object).get();
				}
				return object;
			}

		}
	}

//	/**
//	 * Releases resources immediately instead of waiting for the garbage
//	 * collector.
//	 */
//	private void clear() {
//		synchronized (pool) {
//			pool.clear();
//		}
//	}

	/**
	 * Put an element in the pool
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
		super();

	}

	/**
	 * @param hints
	 */
	public BufferedDefaultCoordinateOperationFactory(Hints hints) {
		super(hints);

	}

	/**
	 * @param hints
	 * @param priority
	 */
	public BufferedDefaultCoordinateOperationFactory(Hints hints, int priority) {
		super(hints, priority);

	}

	public CoordinateOperation createOperation(
			CoordinateReferenceSystem sourceCRS,
			CoordinateReferenceSystem targetCRS)
			throws OperationNotFoundException, FactoryException {
		return (CoordinateOperation) get(new CRSTransformHashUtil(sourceCRS,
				targetCRS));
	}

}
