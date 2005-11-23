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
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.OperationNotFoundException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.OptionalFactory;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.CRSUtilities;


/**
 * A {@linkplain CoordinateOperationFactory coordinate operation factory} using the help
 * of an {@linkplain CoordinateOperationAuthorityFactory authority factory}.
 * When <code>{@linkplain #createOperation createOperation}(sourceCRS, targetCRS)</code> is
 * invoked, this class first fetch the authority codes for source and target CRS and invokes
 * the {@link CoordinateOperationAuthorityFactory#createFromCoordinateReferenceSystemCodes
 * createFromCoordinateReferenceSystemCodes} method from the authority factory. If the
 * authority factory doesn't know about the specified CRS, only then the default (standalone)
 * process from the super-class is used as a fallback.
 * <p>
 * Using an authority factory when available gives access to informations not available otherwise.
 * Examples include {@linkplain CoordinateOperation#getValidArea area of validity},
 * {@linkplain CoordinateOperation#getScope scope} and
 * {@linkplain CoordinateOperation#getPositionalAccuracy positional accuracy}.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AuthorityBackedFactory extends DefaultCoordinateOperationFactory
                                 implements OptionalFactory
{
    /**
     * The default authority factory to use.
     */
    private static final String DEFAULT_AUTHORITY = "EPSG";

    /**
     * The authority factory to use for creating new operations.
     * If {@code null}, a default factory will be fetched when first needed.
     */
    private CoordinateOperationAuthorityFactory authorityFactory;

    /**
     * Creates a new factory backed by a default EPSG authority factory.
     * This factory will uses a priority slightly higher than the
     * {@linkplain DefaultCoordinateOperationFactory default (standalone) factory}.
     */
    public AuthorityBackedFactory() {
        this(null);
    }

    /**
     * Creates a new factory backed by an authority factory fetched using the specified hints.
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints. In addition, the {@link FactoryGroup#HINT_KEY} hint may be used as
     * a low-level substitute for all the above.
     *
     * @param hints The hints, or {@code null} if none.
     */
    public AuthorityBackedFactory(final Hints hints) {
        super(hints, NORMAL_PRIORITY + 10);
        if (hints!=null && !hints.isEmpty()) {
            authorityFactory = FactoryFinder
                    .getCoordinateOperationAuthorityFactory(DEFAULT_AUTHORITY, hints);
        }
    }

    /**
     * Returns the underlying coordinate operation authority factory.
     */
    protected CoordinateOperationAuthorityFactory getAuthorityFactory() {
        /*
         * No need to synchronize. This is not a big deal if FactoryFinder is invoked twice.
         * Actually, we should not synchronize at all. All methods from the super-class are
         * thread-safe without synchronized statements, and we should preserve this advantage
         * in order to reduce the risk of thread lock.
         */
        if (authorityFactory == null) {
            /*
             * Factory creation at this stage will happen only if null hints were specified at
             * construction time, which explain why it is correct to use {@link FactoryFinder}
             * with null hints here.
             */
            authorityFactory = FactoryFinder
                    .getCoordinateOperationAuthorityFactory(DEFAULT_AUTHORITY, null);
        }
        return authorityFactory;
    }

    /**
     * Returns an operation for conversion or transformation between two coordinate reference
     * systems. This invokes the {@link #createFromCoordinateReferenceSystemCodes
     * createFromCoordinateReferenceSystemCodes} method with the code of the supplied CRS.
     * If no operation is found for those codes, then this method fallback on the
     * {@linkplain DefaultCoordinateOperationFactory#createOperation(CoordinateReferenceSystem,
     * CoordinateReferenceSystem) generic implementation} provided by the super-class.
     * <p>
     * Note that this method may be invoked recursively by the super-class implementation. For
     * example no operation may be available from the underlying {@linkplain #getAuthorityFactory
     * authority factory} between two {@linkplain org.opengis.referencing.crs.CompoundCRS compound
     * CRS}, but an operation may be available between two components of those compound CRS.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from {@code sourceCRS} to {@code targetCRS}.
     * @throws OperationNotFoundException if no operation path was found from {@code sourceCRS}
     *         to {@code targetCRS}.
     * @throws FactoryException if the operation creation failed for some other reason.
     */
    public CoordinateOperation createOperation(final CoordinateReferenceSystem sourceCRS,
                                               final CoordinateReferenceSystem targetCRS)
            throws OperationNotFoundException, FactoryException
    {
        ensureNonNull("sourceCRS", sourceCRS);
        ensureNonNull("targetCRS", targetCRS);
        final CoordinateOperationAuthorityFactory authorityFactory = getAuthorityFactory();
        final Citation authority = authorityFactory.getAuthority();
        final Identifier sourceCode = AbstractIdentifiedObject.getIdentifier(sourceCRS, authority);
        if (sourceCode != null) {
            final Identifier targetCode = AbstractIdentifiedObject.getIdentifier(targetCRS, authority);
            if (targetCode != null) try {
                final CoordinateOperation candidate;
                candidate = createFromCoordinateReferenceSystemCodes(sourceCode.getCode(),
                                                                     targetCode.getCode());
                if (candidate != null) {
                    /*
                     * It is possible that the Identifier in user's CRS is not quite right.   For
                     * example the user may have created his source and target CRS from WKT using
                     * a different axis order than the official one and still call it "EPSG:xxxx"
                     * as if it were the official CRS.   It is possible also that the user simply
                     * doesn't understand authority codes and just gave bogus identifiers. Checks
                     * if the source and target CRS for the operation just created are really the
                     * same (ignoring metadata) than the one specified by the user.
                     */
                    if (CRSUtilities.equalsIgnoreMetadata(sourceCRS, candidate.getSourceCRS()) &&
                        CRSUtilities.equalsIgnoreMetadata(targetCRS, candidate.getTargetCRS()))
                    {
                        return candidate;
                    }
                }
            } catch (NoSuchAuthorityCodeException exception) {
                /*
                 * sourceCode or targetCode is unknow to the underlying authority factory.
                 * Ignores the exception and fallback on the generic algorithm provided by
                 * the super-class.
                 */
            } catch (FactoryException exception) {
                /*
                 * Other kind of error. It may be more serious, but the super-class is capable
                 * to provides a raisonable default behavior. Log as a warning and lets continue.
                 *
                 * TODO: localize.
                 */
                final LogRecord record = new LogRecord(Level.WARNING,
                      "Failure while creating a coordinate operation from the authority factory.");
                record.setSourceClassName("AuthorityBackedFactory");
                record.setSourceMethodName("createOperation");
                record.setThrown(exception);
                LOGGER.log(record);
            }
        }
        return super.createOperation(sourceCRS, targetCRS);
    }

    /**
     * Creates an operation from coordinate reference system codes. The default implementation
     * delegates the call to the underlying {@linkplain #getAuthorityFactory authority factory}
     * and returns the first operation found. If no operation is found, then this method returns
     * {@code null}. Subclasses can override this method if they wish to select a better operation
     * from the authority codes.
     *
     * @param  sourceCode   Coded value of source coordinate reference system.
     * @param  targetCode   Coded value of target coordinate reference system.
     * @return The operation, or {@code null} if the underlying authority factory doesn't provide
     *         any operation for the specified source and target CRS.
     *
     * @throws NoSuchAuthorityCodeException if a specified code was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    protected CoordinateOperation createFromCoordinateReferenceSystemCodes(final String sourceCode,
                                                                           final String targetCode)
            throws FactoryException
    {
        final Set operations =
            getAuthorityFactory().createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
        if (operations == null || operations.isEmpty()) {
            return null;
        }
        return (CoordinateOperation) operations.iterator().next();
    }

    /**
     * Returns {@code true} if this factory and its underlying
     * {@linkplain #getAuthorityFactory authority factory} are ready for use.
     */
    public boolean isReady() {
        try {
            final CoordinateOperationAuthorityFactory authorityFactory = getAuthorityFactory();
            if (authorityFactory instanceof OptionalFactory) {
                return ((OptionalFactory) authorityFactory).isReady();
            }
            return true;
        } catch (FactoryRegistryException exception) {
            // No factory found. Ignore the exception since it is the
            // purpose of this method to figure out this kind of case.
            return false;
        }
    }
}
