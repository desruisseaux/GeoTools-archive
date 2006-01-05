/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.geometry;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.FactoryFinder;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A direct position capable to {@linkplain #transform transform} a point between an arbitrary CRS
 * and {@linkplain #getCoordinateReferenceSystem its own CRS}. This class caches the last transform
 * used in order to improve the performances when the {@linkplain CoordinateOperation#getSourceCRS
 * source} and {@linkplain CoordinateOperation#getTargetCRS target} CRS don't change often. Using
 * this class is faster than invoking <code>{@linkplain CoordinateOperationFactory#createOperation
 * CoordinateOperationFactory.createOperation}(sourceCRS, targetCRS)</code> for every points.
 * <p>
 * <strong>Note 1:</strong> This class is advantageous on a performance point of view only if the
 * same instance of {@code TransformedDirectPosition} is used for transforming many points between
 * arbitrary CRS and the {@linkplain #getCoordinateReferenceSystem CRS of this position}.
 * <p>
 * <strong>Note 2:</strong> This convenience class is useful when the source and target CRS are
 * <em>not likely</em> to change often. If you are <em>sure</em> that the source and target CRS are
 * frozen for a given set of positions, then using {@link CoordinateOperation} directly gives much
 * better performances. This is because {@code TransformedDirectPosition} checks if the CRS changed
 * before every transformations, which may be costly.
 * <p>
 * <strong>Note 3:</strong> This class is called <cite>Transformed</cite> Direct Position because
 * its more commonly used for transforming many points from arbitrary CRS to a common CRS (using
 * the {@link #transform(DirectPosition)} method) than the other way around.
 * <p>
 * This class usually don't appears in a public API. It is more typicaly used as a helper private
 * field in some more complex class. For example suppose that {@code MyClass} needs to perform its
 * internal working in some particular CRS, but we want robust API that adjusts itself to whatever
 * CRS the client happen to use. {@code MyClass} could be written as below:
 *
 * <blockquote><pre>
 * public class MyClass {
 *     private static final CoordinateReferenceSystem INTERNAL_CRS = ...
 *     private static final CoordinateReferenceSystem   PUBLIC_CRS = ...
 *
 *     private final TransformedDirectPosition myPosition = 
 *             new TransformedDirectPosition(INTERNAL_CRS, null);
 *
 *     public DirectPosition getPosition() throws TransformException {
 *         return myPosition.transform(PUBLIC_CRS);
 *     }
 *
 *     public void setPosition(DirectPosition position) throws TransformException {
 *         // The position CRS is usually PUBLIC_CRS, but code below will work even if it is not.
 *         myPosition.transform(position);
 *     }
 * }
 * </pre></blockquote>
 * 
 * @since 2.2
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class TransformedDirectPosition extends GeneralDirectPosition {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3988283183934950437L;

    /**
     * The factory to use for creating new coordinate operation.
     */
    private final CoordinateOperationFactory factory;

    /**
     * The last coordinate operation used, or {@code null}. If non-null, then the
     * {@linkplain CoordinateOperation#getTargetCRS target CRS} must always be identical
     * to the {@linkplain #getCoordinateReferenceSystem CRS of this position}. We freeze
     * the target CRS instead of the source CRS because transformations from an arbitrary
     * CRS to a common CRS is more frequent than the other way around.
     */
    private transient CoordinateOperation operation;

    /**
     * Creates a new direct position with the specified coordinate reference system.
     *
     * @param crs The CRS for this direct position.
     * @param hints The set of hints to use for fetching a {@link CoordinateOperationFactory},
     *        or {@code null} if none.
     */
    public TransformedDirectPosition(final CoordinateReferenceSystem crs, final Hints hints) {
        super(crs);
        ensureNonNull("crs", crs);
        factory = FactoryFinder.getCoordinateOperationFactory(hints);
    }

    /**
     * Sets the coordinate reference system in which the coordinate is given.
     * The given CRS will be used as:
     * <p>
     * <ul>
     *   <li>the {@linkplain CoordinateOperation#getSourceCRS source CRS} for every call to
     *       {@link #transform(CoordinateReferenceSystem)}</li>
     *   <li>the {@linkplain CoordinateOperation#getTargetCRS target CRS} for every call to
     *       {@link #transform(DirectPosition)}</li>
     * </ul>
     *
     * @param  crs The new CRS for this direct position.
     * @throws MismatchedDimensionException if the specified CRS doesn't have the expected
     *         number of dimensions.
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs)
            throws MismatchedDimensionException
    {
        ensureNonNull("crs", crs);
        super.setCoordinateReferenceSystem(crs);
        operation = null;
    }

    /**
     * Returns a new point with the same coordinates than this one, but transformed in the given
     * CRS. This method never returns {@code this}, so the returned point usually doesn't need to
     * be cloned.
     *
     * @param  targetCRS the target CRS.
     * @return The same position than {@code this}, but transformed in the specified target CRS.
     * @throws TransformException if a coordinate transformation was required and failed.
     */
    public DirectPosition transform(final CoordinateReferenceSystem targetCRS) throws TransformException {
        if (operation==null || !CRSUtilities.equalsIgnoreMetadata(operation.getSourceCRS(), targetCRS)) {
            final CoordinateReferenceSystem sourceCRS = getCoordinateReferenceSystem();
            try {
                operation = factory.createOperation(targetCRS, sourceCRS);
            } catch (FactoryException exception) {
                throw new TransformException(exception.getLocalizedMessage(), exception);
            }
        }
        assert operation.getTargetCRS().equals(getCoordinateReferenceSystem()) : operation;
        return operation.getMathTransform().inverse().transform(this, null);
    }

    /**
     * Transforms a given position and stores the result in this object. The {@linkplain
     * CoordinateOperation#getSourceCRS source CRS} is the CRS of the given position. The
     * {@linkplain CoordinateOperation#getTargetCRS target CRS} is the {@linkplain
     * #getCoordinateReferenceSystem CRS of this position}.
     *
     * @param  position A position using an arbitrary CRS. This object will not be modified.
     * @throws TransformException if a coordinate transformation was required and failed.
     */
    public void transform(final DirectPosition position) throws TransformException {
        final CoordinateReferenceSystem sourceCRS = position.getCoordinateReferenceSystem();
        if (sourceCRS == null) {
            setLocation(position);
            return;
        }
        /*
         * A projection may be required. Checks if it is the same one than the one used
         * last time this method has been invoked. If the specified position uses a new
         * CRS, then gets the transformation and saves it in case the next call to this
         * method would uses again the same transformation.
         */
        if (operation==null || !CRSUtilities.equalsIgnoreMetadata(operation.getSourceCRS(), sourceCRS)) {
            final CoordinateReferenceSystem targetCRS = getCoordinateReferenceSystem();
            try {
                operation = factory.createOperation(sourceCRS, targetCRS);
            } catch (FactoryException exception) {
                throw new TransformException(exception.getLocalizedMessage(), exception);
            }
        }
        assert operation.getTargetCRS().equals(getCoordinateReferenceSystem()) : operation;
        final MathTransform mt = operation.getMathTransform();
        if (mt.transform(position, this) != this) {
            throw new AssertionError(mt); // Should never occurs.
        }
    }

    /**
     * Makes sure an argument is non-null.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if {@code object} is null.
     */
    private static void ensureNonNull(final String name, final Object object)
        throws IllegalArgumentException
    {
        if (object == null) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NULL_ARGUMENT_$1, name));
        }
    }
}
