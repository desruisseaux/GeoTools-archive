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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.operation.AuthorityBackedFactory;


/**
 * Helper class for the creation of concatenated operation by {@link FactoryUsingSQL}.
 * This class is used by {@link FactoryUsingSQL#createCoordinateOperation}. It contains
 * to source and target CRS in order to avoid never-ending loops.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
final class ConcatenatedOperationFactory extends AuthorityBackedFactory {
    /**
     * The source and target CRS for which a coordinate operation has been requested.
     */
    private transient String source, target;

    /**
     * The properties to give to the operation to create.
     */
    private transient Map properties;

    /**
     * Creates a new instance using the specified factories.
     *
     * @param factories The low-level factories to use.
     * @param owner The {@link FactoryUsingSQL} instance for which this helper class is created.
     */
    ConcatenatedOperationFactory(final FactoryGroup factories,
                                 final CoordinateOperationAuthorityFactory owner)
    {
        super(getHints(factories, owner));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Hints getHints(final FactoryGroup factories,
                                  final CoordinateOperationAuthorityFactory owner)
    {
        final Hints hints = new Hints(FactoryGroup.HINT_KEY, factories);
        hints.put(Hints.COORDINATE_OPERATION_AUTHORITY_FACTORY, owner);
        return hints;
    }

    /**
     * Initialize this factory. This method <strong>must</strong> be invoked before any call
     * to {@link #createCoordinateOperation} in order to avoid never-ending loop.
     *
     * @param properties The properties to give to the operation to create.
     * @param sourceCode the code of the source CRS.
     * @param targetCode the code of the target CRS.
     */
    final void init(final Map properties, final String sourceCode, final String targetCode) {
        this.properties = properties;
        this.source     = sourceCode;
        this.target     = targetCode;
    }

    /**
     * Creates a coordinate operation from the following codes. The super-class implementation
     * delegates the work to the {@link FactoryUsingSQL} instance specified at construction time.
     * We will get a never-ending loop if do not intercept the source and target CRS for which
     * the {@link FactoryUsingSQL} want a coordinate operation.
     */
    protected CoordinateOperation createFromCoordinateReferenceSystemCodes(final String sourceCode,
                                                                           final String targetCode)
            throws FactoryException
    {
        if (sourceCode.equals(source) && targetCode.equals(target)) {
            return null;
        }
        return super.createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
    }

    /**
     * Creates a concatenated operation from a sequence of operations.
     *
     * @param  properties Set of properties. Should contains at least {@code "name"}.
     * @param  operations The sequence of operations.
     * @return The concatenated operation.
     * @throws FactoryException if the object creation failed.
     */
    public CoordinateOperation createConcatenatedOperation(Map properties,
                                                           CoordinateOperation[] operations)
            throws FactoryException
    {
        return super.createConcatenatedOperation(properties, operations);
    }
}
