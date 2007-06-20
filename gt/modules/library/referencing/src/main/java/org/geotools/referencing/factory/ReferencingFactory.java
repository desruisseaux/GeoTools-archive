/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le D�veloppement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Collection;
import java.util.Collections;
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
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * Base class for all factories in the referencing module.
 * Factories can be grouped in two categories:
 *
 * <ul>
 *   <li><p>{@linkplain AuthorityFactory Authority factories} creates objects from
 *       a compact string defined by an authority.
 *   <br><em>These classes are working as "builders": they hold the definition or recipie
 *       used to construct an objet.</em></p></li>
 *
 *   <li><p>{@linkplain ObjectFactory Object factories} allows applications
 *       to make objects that cannot be created by an authority factory.
 *       This factory is very flexible, whereas the authority factory is
 *       easier to use.
 *   <br><em>These classes are working as "Factories": they provide a series of
 *       {@code create} methods that can be used like a constructor.</em></p></li>
 * </ul>
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
     * A citation which contains only the title "All" in localized language. Used
     * as a pseudo�authority name for {@link AllAuthoritiesFactory}. Declared here
     * because processed specially by {@link IdentifiedObjectFinder}, since it is
     * not a valid authority name (not declared in {@link AllAuthoritiesFactory}
     * because we want to avoid this dependency in {@link IdentifiedObjectFinder}).
     */
    static final Citation ALL;
    static {
        final CitationImpl citation = new CitationImpl(Vocabulary.format(VocabularyKeys.ALL));
        citation.freeze();
        ALL = citation;
    }

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

    /**
     * Returns the direct {@linkplain Factory factory} dependencies, which may be {@code null}.
     * This method should not returns indirect dependencies. Elements should be instance of
     * {@link Factory} or {@link FactoryException} if a particular dependency can't be obtained.
     * <p>
     * The default implementation always returns an empty set.
     */
    Collection/*<?>*/ dependencies() {
        return Collections.EMPTY_SET;
    }
}
