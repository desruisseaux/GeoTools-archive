/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.referencing;

// J2SE direct dependencies
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.Locale;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashSet;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.spi.RegisterableService;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.Factory;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.MathTransformFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.Arguments;
import org.geotools.resources.LazySet;


/**
 * Defines static methods used to access the ReferencingFactoryFinder.
 * 
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @deprecated Please use ReferencingFactoryFinder directly
 */
public final class FactoryFinder {
    /**
    /**
     * Do not allows any instantiation of this class.
     */
    private FactoryFinder() {
        // singleton
    }

    /**
     * Programmatic management of authority factories.
     * @param authority The authority factory to add.
     */
    public static synchronized void addAuthorityFactory(final AuthorityFactory authority) {
        ReferencingFactoryFinder.addAuthorityFactory(authority);
    }

    /**
     * Programmatic management of authority factories.
     * Needed for user managed, not plug-in managed, authority factory.
     * Also useful for test cases.
     *
     * @param authority The authority factory to remove.
     */
    public static synchronized void removeAuthorityFactory(final AuthorityFactory authority) {
        ReferencingFactoryFinder.removeAuthorityFactory(authority); 
    }

    /**
     * Returns the names of all currently registered authorities.
     */
    public static synchronized Set/*<String>*/ getAuthorityNames() {
        return ReferencingFactoryFinder.getAuthorityNames();
    }


    /**
     * Returns the first implementation of {@link DatumFactory} matching the specified hints.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first datum factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link DatumFactory} interface.
     */
    public static DatumFactory getDatumFactory(final Hints hints) throws FactoryRegistryException {
        return ReferencingFactoryFinder.getDatumFactory(hints);
    }

    /**
     * Returns a set of all available implementations for the {@link DatumFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available datum factory implementations.
     *
     * @since 2.3
     */
    public static Set getDatumFactories(final Hints hints) {
        return ReferencingFactoryFinder.getDatumFactories(hints);
    }

    /**
     * Returns the first implementation of {@link CSFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise. If more than one implementation is registered and an
     * {@linkplain #setVendorOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate system factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CSFactory} interface.
     */
    public static CSFactory getCSFactory(final Hints hints) throws FactoryRegistryException {
        return ReferencingFactoryFinder.getCSFactory(hints);
    }

    /**
     * Returns a set of all available implementations for the {@link CSFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available coordinate system factory implementations.
     *
     * @since 2.3
     */
    public static Set getCSFactories(final Hints hints) {
        return ReferencingFactoryFinder.getCSFactories(hints);
    }

    /**
     * Returns the first implementation of {@link CRSFactory} matching the specified hints.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate reference system factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CRSFactory} interface.
     */
    public static CRSFactory getCRSFactory(final Hints hints) throws FactoryRegistryException {
        return ReferencingFactoryFinder.getCRSFactory(hints);
    }

    /**
     * Returns a set of all available implementations for the {@link CRSFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available coordinate reference system factory implementations.
     *
     * @since 2.3
     */
    public static Set getCRSFactories(final Hints hints) {
        return ReferencingFactoryFinder.getCRSFactories(hints);
    }

    /**
     * Returns the first implementation of {@link CoordinateOperationFactory} matching the specified
     * hints.
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate operation factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CoordinateOperationFactory} interface.
     */
    public static CoordinateOperationFactory getCoordinateOperationFactory(final Hints hints)
            throws FactoryRegistryException
    {
        return ReferencingFactoryFinder.getCoordinateOperationFactory(hints);
    }

    /**
     * Returns a set of all available implementations for the
     * {@link CoordinateOperationFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available coordinate operation factory implementations.
     *
     * @since 2.3
     */
    public static Set getCoordinateOperationFactories(final Hints hints) {
        return ReferencingFactoryFinder.getCoordinateOperationFactories(hints);
    }

    /**
     * Returns the first implementation of {@link DatumAuthorityFactory} matching the specified
     * hints.
     * @param  authority The desired authority (e.g. "EPSG").
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first datum authority factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link DatumAuthorityFactory} interface.
     */
    public static DatumAuthorityFactory getDatumAuthorityFactory(final String authority,
                                                                 final Hints  hints)
            throws FactoryRegistryException
    {
        return ReferencingFactoryFinder.getDatumAuthorityFactory(authority, hints);
    }

    /**
     * Returns a set of all available implementations for the {@link DatumAuthorityFactory}
     * interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available datum authority factory implementations.
     *
     * @since 2.3
     */    
    public static Set getDatumAuthorityFactories(final Hints hints) {
        return ReferencingFactoryFinder.getDatumFactories(hints);
    }

    /**
     * Returns the first implementation of {@link CSAuthorityFactory} matching the specified
     * hints.
     * @param  authority The desired authority (e.g. "EPSG").
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate system authority factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CSAuthorityFactory} interface.
     */
    public static CSAuthorityFactory getCSAuthorityFactory(final String authority,
                                                           final Hints  hints)
            throws FactoryRegistryException
    {
        return ReferencingFactoryFinder.getCSAuthorityFactory(authority, hints);
    }

    /**
     * Returns a set of all available implementations for the {@link CSAuthorityFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available coordinate system authority factory implementations.
     *
     * @since 2.3
     */
    public static Set getCSAuthorityFactories(final Hints hints) {
        return ReferencingFactoryFinder.getCSAuthorityFactories(hints);
    }

    /**
     * Returns the first implementation of {@link CRSAuthorityFactory} matching the specified
     * hints.
     * @param  authority The desired authority (e.g. "EPSG").
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate reference system authority factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CRSAuthorityFactory} interface.
     */
    public static CRSAuthorityFactory getCRSAuthorityFactory(final String authority,
                                                             final Hints  hints)
            throws FactoryRegistryException
    {
        return ReferencingFactoryFinder.getCRSAuthorityFactory(authority, hints);
    }

    /**
     * Returns a set of all available implementations for the {@link CRSAuthorityFactory} interface.
     * 
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available coordinate reference system authority factory implementations.
     *
     * @since 2.3
     */
    public static Set getCRSAuthorityFactories(final Hints hints) {
        return ReferencingFactoryFinder.getCRSAuthorityFactories(hints);
    }

    /**
     * Returns the first implementation of {@link CoordinateOperationAuthorityFactory} matching
     * the specified hints.
     * 
     * @param  authority The desired authority (e.g. "EPSG").
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate operation authority factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CoordinateOperationAuthorityFactory} interface.
     */
    public static CoordinateOperationAuthorityFactory getCoordinateOperationAuthorityFactory(
            final String authority, final Hints hints)
            throws FactoryRegistryException
    {
        return ReferencingFactoryFinder.getCoordinateOperationAuthorityFactory(authority, hints);
    }

    /**
     * Returns a set of all available implementations for the
     * {@link CoordinateOperationAuthorityFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available coordinate operation authority factory implementations.
     *
     * @since 2.3
     */
    public static Set getCoordinateOperationAuthorityFactories(final Hints hints) {
        return ReferencingFactoryFinder.getCoordinateOperationAuthorityFactories(hints);
    }

    /**
     * Returns the first implementation of {@link MathTransformFactory} matching the specified
     * hints.
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first math transform factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link MathTransformFactory} interface.
     */
    public static MathTransformFactory getMathTransformFactory(final Hints hints)
            throws FactoryRegistryException
    {
        return ReferencingFactoryFinder.getMathTransformFactory(hints);
    }

    /**
     * Returns a set of all available implementations for the
     * {@link MathTransformFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available math transform factory implementations.
     *
     * @since 2.3
     */
    public static Set getMathTransformFactories(final Hints hints) {
        return ReferencingFactoryFinder.getMathTransformFactories(hints);
    }

    /**
     * Sets a pairwise ordering between two vendors. If one or both vendors are not
     * currently registered, or if the desired ordering is already set, nothing happens
     * and {@code false} is returned.
     * <p>
     * The example below said that an ESRI implementation (if available) is
     * preferred over the Geotools one:
     *
     * <blockquote><code>FactoryFinder.setVendorOrdering("ESRI", "Geotools");</code></blockquote>
     *
     * @param  vendor1 The preferred vendor.
     * @param  vendor2 The vendor to which {@code vendor1} is preferred.
     * @return {@code true} if the ordering was set for at least one category.
     */
    public static synchronized boolean setVendorOrdering(final String vendor1,
                                                         final String vendor2)
    {
        return ReferencingFactoryFinder.setVendorOrdering(vendor1, vendor2);
    }

    /**
     * Unsets a pairwise ordering between two vendors. If one or both vendors are not
     * currently registered, or if the desired ordering is already unset, nothing happens
     * and {@code false} is returned.
     *
     * @param  vendor1 The preferred vendor.
     * @param  vendor2 The vendor to which {@code vendor1} is preferred.
     * @return {@code true} if the ordering was unset for at least one category.
     */
    public static synchronized boolean unsetVendorOrdering(final String vendor1,
                                                           final String vendor2)
    {
        return ReferencingFactoryFinder.unsetVendorOrdering(vendor1, vendor2);
    }

    /**
     * Sets a pairwise ordering between two authorities. If one or both authorities are not
     * currently registered, or if the desired ordering is already set, nothing happens
     * and {@code false} is returned.
     * <p>
     * The example below said that EPSG {@linkplain AuthorityFactory authority factories}
     * are preferred over ESRI ones:
     *
     * <blockquote><code>FactoryFinder.setAuthorityOrdering("EPSG", "ESRI");</code></blockquote>
     *
     * @param  authority1 The preferred authority.
     * @param  authority2 The authority to which {@code authority1} is preferred.
     * @return {@code true} if the ordering was set for at least one category.
     */
    public static synchronized boolean setAuthorityOrdering(final String authority1,
                                                            final String authority2)
    {
        return ReferencingFactoryFinder.setAuthorityOrdering(authority1, authority2);
    }

    /**
     * Unsets a pairwise ordering between two authorities. If one or both authorities are not
     * currently registered, or if the desired ordering is already unset, nothing happens
     * and {@code false} is returned.
     *
     * @param  authority1 The preferred authority.
     * @param  authority2 The vendor to which {@code authority1} is preferred.
     * @return {@code true} if the ordering was unset for at least one category.
     */
    public static synchronized boolean unsetAuthorityOrdering(final String authority1,
                                                              final String authority2)
    {
        return ReferencingFactoryFinder.unsetAuthorityOrdering(authority1, authority2);
    }

    /**
     * Scans for factory plug-ins on the application class path.
     */
    public static synchronized void scanForPlugins() {
        ReferencingFactoryFinder.scanForPlugins();
    }

    /**
     * List all available factory implementations in a tabular format. For each factory interface,
     * the first implementation listed is the default one.
     *
     * @param  out The output stream where to format the list.
     * @param  locale The locale for the list, or {@code null}.
     * @throws IOException if an error occurs while writting to {@code out}.
     */
    public static synchronized void listProviders(final Writer out, final Locale locale)
            throws IOException
    {
        ReferencingFactoryFinder.listProviders(out, locale);
    }

    /**
     * Dump to the standard output stream a list of available factory implementations.
     * This method can be invoked from the command line. It provides a mean to verify
     * if some implementations were found in the classpath. The syntax is:
     * <BR>
     * <BLOCKQUOTE><CODE>
     * java org.geotools.referencing.FactoryFinder <VAR>&lt;options&gt;</VAR>
     * </CODE></BLOCKQUOTE>
     *
     * <P>where options are:</P>
     *
     * <TABLE CELLPADDING='0' CELLSPACING='0'>
     *   <TR><TD NOWRAP><CODE>-encoding</CODE> <VAR>&lt;code&gt;</VAR></TD>
     *       <TD NOWRAP>&nbsp;Set the character encoding</TD></TR>
     *   <TR><TD NOWRAP><CODE>-locale</CODE> <VAR>&lt;language&gt;</VAR></TD>
     *       <TD NOWRAP>&nbsp;Set the language for the output (e.g. "fr" for French)</TD></TR>
     * </TABLE>
     *
     * <P><strong>Note for Windows users:</strong> If the output contains strange
     * symbols, try to supply an "{@code -encoding}" argument. Example:</P>
     *
     * <blockquote><code>
     * java org.geotools.referencing.FactoryFinder -encoding Cp850
     * </code></blockquote>
     *
     * <P>The codepage number (850 in the previous example) can be obtained from the DOS
     * commande line using the "{@code chcp}" command with no arguments.
     * This {@code -encoding} argument need to be supplied only once.</P>
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(0);
        try {
            listProviders(arguments.out, arguments.locale);
        } catch (Exception exception) {
            exception.printStackTrace(arguments.err);
        }
    }
}
