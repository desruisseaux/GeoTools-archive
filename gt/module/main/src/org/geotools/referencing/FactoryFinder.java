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
 */
package org.geotools.referencing;

// J2SE direct dependencies
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.Factory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransformFactory;

// Geotools dependencies
import org.geotools.factory.FactoryRegistry;
import org.geotools.io.TableWriter;
import org.geotools.resources.Arguments;
import org.geotools.resources.LazySet;
import org.geotools.resources.Utilities;


/**
 * Defines static methods used to access the application's default {@linkplain Factory
 * factory} implementation.
 *
 * <P>To declare a factory implementation, a services subdirectory is placed within the
 * <code>META-INF</code> directory that is present in every JAR file. This directory
 * contains a file for each factory interface that has one or more implementation classes
 * present in the JAR file. For example, if the JAR file contained a class named
 * <code>com.mycompany.DatumFactoryImpl</code> which implements the {@link DatumFactory}
 * interface, the JAR file would contain a file named:</P>
 *
 * <blockquote><pre>META-INF/services/org.opengis.referencing.datum.DatumFactory</pre></blockquote>
 *
 * <P>containing the line:</P>
 *
 * <blockquote><pre>com.mycompany.DatumFactoryImpl</pre></blockquote>
 *
 * <P>If the factory classes implements {@link RegisterableService}, it will be notified upon
 * registration and deregistration. Note that the factory classes should be lightweight and quick
 * to load. Implementations of these interfaces should avoid complex dependencies on other classes
 * and on native code. The usual pattern for more complex services is to register a lightweight
 * proxy for the heavyweight service.</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Allows the user to set ordering (i.e. preferred implementation).
 */
public final class FactoryFinder {
    /**
     * The service registry for this manager.
     * Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    /**
     * Do not allows any instantiation of this class.
     */
    private FactoryFinder() {
        // singleton
    }

    /**
     * Returns the service registry. The registry will be created the first
     * time this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(FactoryFinder.class);
        if (registry == null) {
            registry = new FactoryRegistry(Arrays.asList(new Class[] {
                    DatumFactory.class,
                    CSFactory.class,
                    CRSFactory.class,
                    CRSAuthorityFactory.class,
                    MathTransformFactory.class,
                    AuthorityFactory.class,
                    CoordinateOperationFactory.class}));
        }
        return registry;
    }

    /**
     * Programmatic management of authority factories.
     * <br><br>
     * Needed for user managed, not plug-in managed, authority factory.
     * Also useful for test cases.
     *
     * @param authority The authority to add.
     */
    public static synchronized void addAuthority(final AuthorityFactory authority) {
        getServiceRegistry().registerServiceProvider(authority);
    }

    /**
     * Programmatic management of authority factories.
     * <br><br>
     * Needed for user managed, not plug-in managed, authority factory.
     * Also useful for test cases.
     *
     * @param authority The authority to remove.
     */
    public static synchronized void removeAuthority(final AuthorityFactory authority) {
        getServiceRegistry().deregisterServiceProvider(authority);
    }

    /**
     * Returns the default implementation of {@link DatumFactory}. If no implementation is
     * registered, then this method throws an exception. If more than one implementation is
     * registered and an {@linkplain #setOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @return First datum factory found.
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link DatumFactory} interface.
     */
    public static synchronized DatumFactory getDatumFactory() throws NoSuchElementException {
        return (DatumFactory) getServiceRegistry().getServiceProviders(DatumFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the {@link DatumFactory} interface.
     *
     * @return Set of available datum factory implementations.
     */
    public static synchronized Set getDatumFactories() {
        return new LazySet(getServiceRegistry().getServiceProviders(DatumFactory.class));
    }

    /**
     * Returns the default implementation of {@link CSFactory}. If no implementation is
     * registered, then this method throws an exception. If more than one implementation is
     * registered and an {@linkplain #setOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @return The first coordinate system factory found.
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link CSFactory} interface.
     */
    public static synchronized CSFactory getCSFactory() throws NoSuchElementException {
        return (CSFactory) getServiceRegistry().getServiceProviders(CSFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the {@link CSFactory} interface.
     *
     * @return Set of available coordinate system factory implementations.
     */
    public static synchronized Set getCSFactories() {
        return new LazySet(getServiceRegistry().getServiceProviders(CSFactory.class));
    }

    /**
     * Returns the default implementation of {@link CRSFactory}. If no implementation is
     * registered, then this method throws an exception. If more than one implementation is
     * registered and an {@linkplain #setOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @return The first coordinate reference system factory found.
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link CRSFactory} interface.
     */
    public static synchronized CRSFactory getCRSFactory() throws NoSuchElementException {
        return (CRSFactory) getServiceRegistry().getServiceProviders(CRSFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the {@link CRSFactory} interface.
     *
     * @return Set of available coordinate reference system factory implementations.
     */
    public static synchronized Set getCRSFactories() {
        return new LazySet(getServiceRegistry().getServiceProviders(CRSFactory.class));
    }
    
    /**
     * Returns a set of all available implementations for the {@link AuthorityFactory} interface.
     * <p>
     * This set can be used to list the available codes known to all authorities.
     * In the event that the same code is understood by more then one authority
     * you will need to assume both are close enough, or make use of this set directly
     * rather than use the decode method.
     * </p>
     */
    public static synchronized Set getCRSAuthorityFactories() {
        return new LazySet(getServiceRegistry().getServiceProviders(CRSAuthorityFactory.class));
    }

    /**
     * Returns the default implementation of {@link MathTransformFactory}. If no implementation
     * is registered, then this method throws an exception. If more than one implementation is
     * registered and an {@linkplain #setOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link MathTransformFactory} interface.
     */
    public static synchronized MathTransformFactory getMathTransformFactory() throws NoSuchElementException {
        return (MathTransformFactory) getServiceRegistry().getServiceProviders(MathTransformFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the
     * {@link MathTransformFactory} interface.
     */
    public static synchronized Set getMathTransformFactories() {
        return new LazySet(getServiceRegistry().getServiceProviders(MathTransformFactory.class));
    }

    /**
     * Returns the default implementation of {@link CoordinateOperationFactory}. If no
     * implementation is registered, then this method throws an exception. If more than
     * one implementation is registered and an {@linkplain #setOrdering ordering is set},
     * then the preferred implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link CoordinateOperationFactory} interface.
     */
    public static synchronized CoordinateOperationFactory getCoordinateOperationFactory() throws NoSuchElementException {
        return (CoordinateOperationFactory) getServiceRegistry().getServiceProviders(CoordinateOperationFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the
     * {@link CoordinateOperationFactory} interface.
     */
    public static synchronized Set getCoordinateOperationFactories() {
        return new LazySet(getServiceRegistry().getServiceProviders(CoordinateOperationFactory.class));
    }

    /**
     * Sets a pairwise ordering between two vendors. If one or both vendors are not
     * currently registered, or if the desired ordering is already set, nothing happens
     * and <code>false</code> is returned.
     * <br><br>
     * The example below said that an ESRI implementation (if available) is
     * preferred over the Geotools one:
     *
     * <blockquote><code>FactoryFinder.setVendorOrdering("ESRI", "Geotools");</code></blockquote>
     *
     * @param  vendor1 The preferred vendor.
     * @param  vendor2 The vendor to which <code>vendor1</code> is preferred.
     * @return <code>true</code> if the ordering was set for at least one category.
     */
    public static boolean setVendorOrdering(final String vendor1, final String vendor2) {
        return getServiceRegistry().setOrdering(Factory.class, true,
                                                new VendorFilter(vendor1),
                                                new VendorFilter(vendor2));
    }

    /**
     * Unsets a pairwise ordering between two vendors. If one or both vendors are not
     * currently registered, or if the desired ordering is already unset, nothing happens
     * and <code>false</code> is returned.
     *
     * @param  vendor1 The preferred vendor.
     * @param  vendor2 The vendor to which <code>vendor1</code> is preferred.
     * @return <code>true</code> if the ordering was unset for at least one category.
     */
    public static boolean unsetVendorOrdering(final String vendor1, final String vendor2) {
        return getServiceRegistry().setOrdering(Factory.class, false,
                                                new VendorFilter(vendor1),
                                                new VendorFilter(vendor2));
    }

    /**
     * A filter for factories provided by a given vendor.
     */
    private static final class VendorFilter implements ServiceRegistry.Filter {
        /** The vendor to filter. */
        private final String vendor;

        /** Constructs a filter for the given vendor. */
        public VendorFilter(final String vendor) {
            this.vendor = vendor;
        }

        /** Returns <code>true</code> if the specified provider is built by the vendor. */
        public boolean filter(final Object provider) {
            return org.geotools.metadata.citation.Citation.titleMatches(
                    ((Factory)provider).getVendor(), vendor);
        }
    }

    /**
     * Sets a pairwise ordering between two authorities. If one or both authorities are not
     * currently registered, or if the desired ordering is already set, nothing happens
     * and <code>false</code> is returned.
     * <br><br>
     * The example below said that EPSG {@linkplain AuthorityFactory authority factories}
     * are preferred over ESRI ones:
     *
     * <blockquote><code>FactoryFinder.setAuthorityOrdering("EPSG", "ESRI");</code></blockquote>
     *
     * @param  authority1 The preferred authority.
     * @param  authority2 The authority to which <code>authority1</code> is preferred.
     * @return <code>true</code> if the ordering was set for at least one category.
     */
    public static boolean setAuthorityOrdering(final String authority1, final String authority2) {
        return getServiceRegistry().setOrdering(AuthorityFactory.class, true,
                                                new AuthorityFilter(authority1),
                                                new AuthorityFilter(authority2));
    }

    /**
     * Unsets a pairwise ordering between two authorities. If one or both authorities are not
     * currently registered, or if the desired ordering is already unset, nothing happens
     * and <code>false</code> is returned.
     *
     * @param  authority1 The preferred authority.
     * @param  authority2 The vendor to which <code>authority1</code> is preferred.
     * @return <code>true</code> if the ordering was unset for at least one category.
     */
    public static boolean unsetAuthorityOrdering(final String authority1, final String authority2) {
        return getServiceRegistry().setOrdering(AuthorityFactory.class, false,
                                                new AuthorityFilter(authority1),
                                                new AuthorityFilter(authority2));
    }

    /**
     * A filter for factories provided for a given authority.
     */
    private static final class AuthorityFilter implements ServiceRegistry.Filter {
        /** The authority to filter. */
        private final String authority;

        /** Constructs a filter for the given authority. */
        public AuthorityFilter(final String authority) {
            this.authority = authority;
        }

        /** Returns <code>true</code> if the specified provider is for the authority. */
        public boolean filter(final Object provider) {
            return org.geotools.metadata.citation.Citation.titleMatches(
                    ((AuthorityFactory)provider).getAuthority(), authority);
        }
    }

    /**
     * Scans for factory plug-ins on the application class path. This method is
     * needed because the application class path can theoretically change, or
     * additional plug-ins may become available. Rather than re-scanning the
     * classpath on every invocation of the API, the class path is scanned
     * automatically only on the first invocation. Clients can call this
     * method to prompt a re-scan. Thus this method need only be invoked by
     * sophisticated applications which dynamically make new plug-ins
     * available at runtime.
     */
    public static void scanForPlugins() {
        if (registry != null) {
            registry.scanForPlugins();
        }
    }

    /**
     * List all available factory implementations in a tabular format. For each factory interface,
     * the first implementation listed is the default one. This method provides a way to check the
     * state of a system, usually for debugging purpose.
     *
     * @param  out The output stream where to format the list.
     * @param  locale The locale for the list, or <code>null</code>.
     * @throws IOException if an error occurs while writting to <code>out</code>.
     *
     * @todo Localize the title line.
     */
    public static synchronized void listProviders(final Writer out, final Locale locale)
            throws IOException
    {
        getServiceRegistry().getServiceProviders(DatumFactory.class); // Force the initialization of ServiceRegistry
        final TableWriter table  = new TableWriter(out, " \u2502 ");
        table.setMultiLinesCells(true);
        table.writeHorizontalSeparator();
        table.write("Factory");
        table.nextColumn();
        table.write("Implementation(s)");
        table.writeHorizontalSeparator();
        for (final Iterator categories=getServiceRegistry().getCategories(); categories.hasNext();) {
            final Class category = (Class)categories.next();
            table.write(Utilities.getShortName(category));
            table.nextColumn();
            boolean first = true;
            for (final Iterator providers=getServiceRegistry().getServiceProviders(category); providers.hasNext();) {
                if (!first) {
                    table.write('\n');
                }
                first = false;
                final Factory provider = (Factory)providers.next();
                final Citation vendor = provider.getVendor();
                table.write(vendor.getTitle().toString(locale));
            }
            table.nextLine();
        }
        table.writeHorizontalSeparator();
        table.flush();
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
     * symbols, try to supply an "<code>-encoding</code>" argument. Example:</P>
     *
     * <blockquote><code>
     * java org.geotools.referencing.FactoryFinder -encoding Cp850
     * </code></blockquote>
     *
     * <P>The codepage number (850 in the previous example) can be obtained from the DOS
     * commande line using the "<code>chcp</code>" command with no arguments.
     * This <code>-encoding</code> argument need to be supplied only once.</P>
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
