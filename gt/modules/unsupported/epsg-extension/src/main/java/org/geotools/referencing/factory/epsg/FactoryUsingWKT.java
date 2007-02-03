/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.net.URL;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.io.TableWriter;
import org.geotools.io.IndentedLineWriter;
import org.geotools.resources.Arguments;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * Authority factory for {@linkplain CoordinateReferenceSystem Coordinate Reference Systems}
 * beyong the one defined in the EPSG database. This factory is used as a fallback when a
 * requested code is not found in the EPSG database, or when there is no connection at all
 * to the EPSG database. The additional CRS are defined as <cite>Well Known Text</cite> in
 * a property file (by default the {@value #FILENAME} file).
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 * @author Rueben Schulz
 */
public class FactoryUsingWKT extends DeferredAuthorityFactory implements CRSAuthorityFactory {
    /**
     * The default filename to read. This file will be searched in the
     * {@code org/geotools/referencing/factory/espg} directory in the
     * classpath or in a JAR file.
     *
     * @see #getDefinitionsURL
     */
    public static final String FILENAME = "epsg.properties";

    /**
     * The factories to be given to the backing store.
     */
    private final FactoryGroup factories;

    /**
     * Constructs an authority factory using the default set of factories.
     */
    public FactoryUsingWKT() {
        this(null);
    }

    /**
     * Constructs an authority factory using a set of factories created from the specified hints.
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints.
     */
    public FactoryUsingWKT(final Hints hints) {
        this(hints, DefaultFactory.PRIORITY - 10);
    }

    /**
     * Constructs an authority factory using the specified hints and priority.
     */
    FactoryUsingWKT(final Hints hints, final int priority) {
        super(hints, priority);
        factories = FactoryGroup.createInstance(hints);
        // Disposes the cached property file after at least 15 minutes of inactivity.
        setTimeout(15 * 60 * 1000L);
    }

    /**
     * Returns the authority. The default implementation returns {@link Citations#EPSG EPSG}
     * in order to register the extra CRS in the "EPSG" namespace, even if the code defined
     * in the property file may not be official EPSG codes.
     */
    public Citation getAuthority() {
        return Citations.EPSG;
    }

    /**
     * Returns the set of authorities to give to {@link PropertyAuthorityFactory} constructor.
     * To be overriden by {@link FactoryESRI} only.
     */
    Citation[] getAuthorities() {
        return new Citation[] {
            getAuthority()
        };
    }

    /**
     * Returns the URL to the property file that contains CRS definitions.
     * The default implementation returns the URL to the {@value #FILENAME} file.
     *
     * @return The URL, or {@code null} if none.
     */
    protected URL getDefinitionsURL() {
        return FactoryUsingWKT.class.getResource(FILENAME);
    }

    /**
     * Creates the backing store authority factory.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the constructor failed to find or read the file.
     *         This exception usually has an {@link IOException} as its cause.
     */
    protected AbstractAuthorityFactory createBackingStore() throws FactoryException {
        try {
            URL url = getDefinitionsURL();
            if (url == null) {
                throw new FileNotFoundException(FILENAME);
            }
            final Collection ids = getAuthority().getIdentifiers();
            final String authority = ids.isEmpty() ? "EPSG" : (String) ids.iterator().next();
            LOGGER.log(Logging.format(Level.CONFIG, LoggingKeys.USING_FILE_AS_FACTORY_$2,
                                      url.getPath(), authority));
            return new PropertyAuthorityFactory(factories, getAuthorities(), url);
        } catch (IOException exception) {
            throw new FactoryException(Errors.format(ErrorKeys.CANT_READ_$1, FILENAME), exception);
        }
    }

    /**
     * Returns a factory of the given type.
     */
    private static final AbstractAuthorityFactory /*T*/ getFactory(
            final Class/*<T extends AbstractAuthorityFactory>*/ type)
    {
        // TODO: use type.cast(...) when we will be allowed to compile for J2SE 1.5.
        return (AbstractAuthorityFactory) FactoryFinder.getCRSAuthorityFactory("EPSG",
                new Hints(Hints.CRS_AUTHORITY_FACTORY, type));
    }

    /**
     * Prints a list of codes that duplicate the ones provided by {@link DefaultFactory}.
     * This is used for implementation of {@linkplain #main main method} in order to check
     * the content of the {@value #FILENAME} file (or whatever property file used as backing
     * store for this factory) from the command line.
     *
     * @param  out The writer where to print the report.
     * @return The set of duplicated codes.
     * @throws FactoryException if an error occured.
     *
     * @since 2.4
     */
    protected Set reportDuplicatedCodes(final PrintWriter out) throws FactoryException {
        final AbstractAuthorityFactory sqlFactory = getFactory(DefaultFactory.class);
        final Vocabulary resources = Vocabulary.getResources(null);
        out.println(resources.getLabel(VocabularyKeys.COMPARE_WITH));
        try {
            final IndentedLineWriter w = new IndentedLineWriter(out);
            w.setIndentation(4);
            w.write(sqlFactory.getBackingStoreDescription());
            w.flush();
        } catch (IOException e) {
            // Should never happen, since we are writting to a PrintWriter.
            throw new AssertionError(e);
        }
        out.println();
        final Set wktCodes   = this.      getAuthorityCodes(CoordinateReferenceSystem.class);
        final Set sqlCodes   = sqlFactory.getAuthorityCodes(CoordinateReferenceSystem.class);
        final Set duplicated = new TreeSet();
        for (final Iterator it=wktCodes.iterator(); it.hasNext();) {
            final String code = ((String) it.next()).trim();
            if (sqlCodes.contains(code)) {
                duplicated.add(code);
                /*
                 * Note: we don't use wktCodes.retainsAll(sqlCode) because the Set implementations
                 *       are usually not the standard ones, but rather some implementations backed
                 *       by a connection to the resources of the underlying factory. We also close
                 *       the connection after this loop for the same reason.  In addition, we take
                 *       this opportunity for sorting the codes.
                 */
            }
        }
        if (duplicated.isEmpty()) {
            out.println(resources.getString(VocabularyKeys.NO_DUPLICATION_FOUND));
        } else {
            for (final Iterator it=duplicated.iterator(); it.hasNext();) {
                final String code = (String) it.next();
                out.print(resources.getLabel(VocabularyKeys.DUPLICATED_VALUE));
                out.println(code);
            }
        }
        return duplicated;
    }

    /**
     * Prints a list of CRS that can't be instantiated. This is used for implementation of
     * {@linkplain #main main method} in order to check the content of the {@value #FILENAME}
     * file (or whatever property file used as backing store for this factory) from the command
     * line.
     *
     * @param  out The writer where to print the report.
     * @return The set of codes that can't be instantiated.
     * @throws FactoryException if an error occured while
     *         {@linkplain #getAuthorityCodes fetching authority codes}.
     *
     * @since 2.4
     */
    protected Set reportInstantiationFailures(final PrintWriter out) throws FactoryException {
        final Set codes = getAuthorityCodes(CoordinateReferenceSystem.class);
        final Map failures = new TreeMap();
        for (final Iterator it=codes.iterator(); it.hasNext();) {
            final String code = (String) it.next();
            try {
                createCoordinateReferenceSystem(code);
            } catch (FactoryException exception) {
                failures.put(code, exception.getLocalizedMessage());
            }
        }
        if (!failures.isEmpty()) {
            final TableWriter writer = new TableWriter(out, " ");
            for (final Iterator it=failures.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry) it.next();
                writer.write((String) entry.getKey());
                writer.write(':');
                writer.nextColumn();
                writer.write((String) entry.getValue());
                writer.nextLine();
            }
            try {
                writer.flush();
            } catch (IOException e) {
                // Should not happen, since we are writting to a PrintWriter
                throw new AssertionError(e);
            }
        }
        return failures.keySet();
    }

    /**
     * Prints a list of codes that duplicate the ones provided in the {@link DefaultFactory}.
     * The factory tested is the one registered in {@link FactoryFinder}.  By default, this
     * is this {@code FactoryUsingWKT} class backed by the {@value #FILENAME} property file.
     * This method can be invoked from the command line in order to check the content of the
     * property file. Valid arguments are:
     * <p>
     * <table>
     *   <tr><td>{@code -test}</td><td>Try to instantiate all CRS and reports any failure
     *       to do so.</td></tr>
     *   <tr><td>{@code -duplicated}</td><td>List all codes from the WKT factory that are
     *       duplicating a code from the SQL factory.</td></tr>
     * </table>
     *
     * @param  args Command line arguments.
     * @throws FactoryException if an error occured.
     *
     * @since 2.4
     */
    public static void main(final String[] args) throws FactoryException {
        main(args, FactoryUsingWKT.class);
    }

    /**
     * Implementation of the {@link #main} method, shared by subclasses.
     */
    static void main(String[] args, final Class/*<? extends FactoryUsingWKT>*/ type)
            throws FactoryException
    {
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        final boolean duplicated  = arguments.getFlag("-duplicated");
        final boolean instantiate = arguments.getFlag("-test");
        args = arguments.getRemainingArguments(0);
        // TODO: remove the cast when we will be allowed to compile for J2SE 1.5.
        final FactoryUsingWKT factory = (FactoryUsingWKT) getFactory(type);
        if (duplicated) {
            factory.reportDuplicatedCodes(arguments.out);
        }
        if (instantiate) {
            factory.reportInstantiationFailures(arguments.out);
        }
        factory.dispose();
    }
}
