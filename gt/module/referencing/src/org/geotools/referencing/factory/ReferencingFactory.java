/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.logging.Logger;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.Factory;
import org.opengis.referencing.ObjectFactory;

// Geotools dependencies
import org.geotools.factory.AbstractFactory;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.FactoryFinder;  // For javadoc
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Base class for all factories in the referencing module.
 * Factories can be grouped in two categories:
 * <P>
 * <UL>
 *   <LI>{@linkplain AuthorityFactory Authority factories} creates objects from
 *       a compact string defined by an authority.</LI>
 *   <LI>{@linkplain ObjectFactory Object factories} allows applications
 *       to make objects that cannot be created by an authority factory.
 *       This factory is very flexible, whereas the authority factory is
 *       easier to use.</LI>
 * </UL>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ReferencingFactory extends AbstractFactory implements Factory {
    /**
     * The logger for event related to Geotools's factories.
     */
    public static final Logger LOGGER = Logger.getLogger("org.geotools.referencing.factory");

    /**
     * Constructs a factory with the default priority.
     */
    protected ReferencingFactory() {
        super();
    }

    /**
     * Constructs a factory with the specified priority.
     *
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     */
    protected ReferencingFactory(final int priority) {
        super(priority);
    }

    /**
     * Returns the vendor responsible for creating this factory implementation. Many implementations
     * may be available for the same factory interface. The default implementation returns
     * {@linkplain Citations#GEOTOOLS Geotools}.
     *
     * @return The vendor for this factory implementation.
     */
    public Citation getVendor() {
        return Citations.GEOTOOLS;
    }

    /**
     * Makes sure that an argument is non-null. This is a
     * convenience method for subclass methods.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if {@code object} is null.
     */
    protected static void ensureNonNull(final String name, final Object object)
        throws InvalidParameterValueException
    {
        if (object == null) {
            throw new InvalidParameterValueException(Errors.format(
                        ErrorKeys.NULL_ARGUMENT_$1, name), name, object);
        }
    }
}
