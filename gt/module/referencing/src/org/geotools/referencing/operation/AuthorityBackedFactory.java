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
import java.util.Iterator;
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

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.OptionalFactory;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.CRSUtilities;


/**
 * A {@linkplain CoordinateOperationFactory coordinate operation factory} extended with the extra
 * informations provided by an {@linkplain CoordinateOperationAuthorityFactory authority factory}.
 * Such authority factory may help to find transformation paths not available otherwise (often
 * determined from empirical parameters). Authority factories can also provide additional
 * informations like the
 * {@linkplain CoordinateOperation#getValidArea area of validity},
 * {@linkplain CoordinateOperation#getScope scope} and
 * {@linkplain CoordinateOperation#getPositionalAccuracy positional accuracy}.
 * <p>
 * When <code>{@linkplain #createOperation createOperation}(sourceCRS, targetCRS)</code> is invoked,
 * {@code AuthorityBackedFactory} fetch the authority codes for source and target CRS and submits
 * them to the {@linkplain #getAuthorityFactory underlying authority factory} through a call to its
 * <code>{@linkplain CoordinateOperationAuthorityFactory#createFromCoordinateReferenceSystemCodes
 * createFromCoordinateReferenceSystemCodes}(sourceCode, targetCode)</code> method. If the
 * authority factory doesn't know about the specified CRS, then the default (standalone)
 * process from the super-class is used as a fallback.
 *
 * @since 2.2
 * @source $URL$
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
     * systems. The default implementation extracts the authority code from the supplied
     * {@code sourceCRS} and {@code targetCRS}, and submit them to the
     * <code>{@linkplain CoordinateOperationAuthorityFactory#createFromCoordinateReferenceSystemCodes
     * createFromCoordinateReferenceSystemCodes}(sourceCode, targetCode)</code> methods.
     * If no operation is found for those codes, then this method returns {@code null}.
     * <p>
     * Note that this method may be invoked recursively. For example no operation may be available
     * from the {@linkplain #getAuthorityFactory underlying authority factory} between two
     * {@linkplain org.opengis.referencing.crs.CompoundCRS compound CRS}, but an operation
     * may be available between two components of those compound CRS.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from {@code sourceCRS} to {@code targetCRS}, or {@code null}
     *         if no such operation is explicitly defined in the underlying database.
     *
     * @since 2.3
     */
    // @Override
    protected CoordinateOperation createFromDatabase(final CoordinateReferenceSystem sourceCRS,
                                                     final CoordinateReferenceSystem targetCRS)
    {
        final CoordinateOperationAuthorityFactory authorityFactory = getAuthorityFactory();
        final Citation  authority = authorityFactory.getAuthority();
        final Identifier sourceID = AbstractIdentifiedObject.getIdentifier(sourceCRS, authority);
        if (sourceID == null) {
            return null;
        }
        final Identifier targetID = AbstractIdentifiedObject.getIdentifier(targetCRS, authority);
        if (targetID == null) {
            return null;
        }
        final String sourceCode = sourceID.getCode();
        final String targetCode = targetID.getCode();
        final Set operations;
        try {
            operations = authorityFactory.createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
        } catch (NoSuchAuthorityCodeException exception) {
            /*
             * sourceCode or targetCode is unknow to the underlying authority factory.
             * Ignores the exception and fallback on the generic algorithm provided by
             * the super-class.
             */
            return null;
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
            record.setSourceMethodName("createFromDatabase");
            record.setThrown(exception);
            LOGGER.log(record);
            return null;
        }
        if (operations != null) {
            for (final Iterator it=operations.iterator(); it.hasNext();) {
                final CoordinateOperation candidate = (CoordinateOperation) it.next();
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
                        if (accept(candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if the specified operation is acceptable. This method is invoked
     * automatically by <code>{@linkplain #createFromDatabase createFromDatabase}(...)</code>
     * for every operation candidates found. The default implementation returns always {@code
     * true}. Subclasses should override this method if they wish to filter the coordinate
     * operations to be returned.
     *
     * @since 2.3
     */
    protected boolean accept(final CoordinateOperation operation) {
        return true;
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
