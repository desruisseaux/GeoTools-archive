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

import org.geotools.geometry.JTS;
import org.geotools.io.TableWriter;
import org.geotools.referencing.crs.GeographicCRS;
import org.geotools.resources.Arguments;
import org.geotools.resources.LazySet;
import org.geotools.resources.Utilities;
import org.geotools.util.ClassFinder;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.Factory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;


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
	private static ServiceRegistry registry;
	
	/**
	 * This method will initialize the {@link ServiceRegistry} and scan for
	 * plugin the first time it will be invoked.
	 * @return 
	 */
	public static ServiceRegistry getServiceRegistry() {
	    if (registry == null) {
			//TODO: remove the cast when we will be allowed to compile against J2SE 1.5.
			registry = new ServiceRegistry((Iterator) Arrays.asList(new Class[] {
					DatumFactory.class,
                    CSFactory.class,
                    CRSFactory.class,
                    CRSAuthorityFactory.class,
                    MathTransformFactory.class,
                    AuthorityFactory.class,
                    CoordinateOperationFactory.class}).iterator());
	    }
	    return registry;
	}

    /**
     * Programtic managment of AuthorityFactory.
     * <p>
     * Needed for user managed, not plug-in manged, AuthorityFactory. Also
     * useful for testcases.
     * <p>
     * @param authority 
     */
    public static void addAuthority( AuthorityFactory authority ){
        getServiceRegistry().registerServiceProvider( authority );
    }
    /**
     * Programtic managment of AuthorityFactory.
     * <p>
     * Needed for user managed, not plug-in manged, AuthorityFactory. Also
     * useful for testcases.
     * <p>
     * @param authority 
     */
    public static void removeAuthority( AuthorityFactory authority ){
        getServiceRegistry().deregisterServiceProvider( authority );
    }
    /**
     * Do not allows any instantiation of this class.
     */
    private FactoryFinder() {
        // singleton
    }

    /**
     * Returns the default implementation of {@link DatumFactory}. If no implementation is
     * registered, then this method throws an exception. If more than one implementation is
     * registered, an arbitrary one is selected.
     * @return First DatumFactory found, not determinisitc
     *
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link DatumFactory} interface.
     */
    public static synchronized DatumFactory getDatumFactory() throws NoSuchElementException {
        return (DatumFactory) ClassFinder.getProviders(getServiceRegistry(), DatumFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the {@link DatumFactory} interface.
     * @return Set of available DatumFactory
     */
    public static synchronized Set getDatumFactories() {
        return new LazySet(ClassFinder.getProviders(getServiceRegistry(), DatumFactory.class));
    }

    /**
     * Returns the default implementation of {@link CSFactory}. If no implementation is
     * registered, then this method throws an exception. If more than one implementation is
     * registered, an arbitrary one is selected.
     * @return The first CSFactory found - not deterministic 
     *
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link CSFactory} interface.
     */
    public static synchronized CSFactory getCSFactory() throws NoSuchElementException {
        return (CSFactory) ClassFinder.getProviders(getServiceRegistry(), CSFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the {@link CSFactory} interface.
     * @return Set of available CSFactory implementations
     */
    public static synchronized Set getCSFactories() {
        return new LazySet(ClassFinder.getProviders(getServiceRegistry(), CSFactory.class));
    }

    /**
     * Returns the default implementation of {@link CRSFactory}. If no implementation is
     * registered, then this method throws an exception. If more than one implementation is
     * registered, an arbitrary one is selected.
     * @return The first CRSFactory found - not deterministic
     *
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link CRSFactory} interface.
     */
    public static synchronized CRSFactory getCRSFactory() throws NoSuchElementException {
        return (CRSFactory) ClassFinder.getProviders(getServiceRegistry(), CRSFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the {@link CRSFactory} interface.
     * @return Set of available CRSFactory
     */
    public static synchronized Set getCRSFactories() {
        return new LazySet(ClassFinder.getProviders(getServiceRegistry(), CRSFactory.class));
    }
    
    /**
     * Transforms the envelope from its current crs to WGS84 coordinate system. 
     * @param env The envelope to transform
     * @param crs The CRS the envelope is currently in.
     * @return env transformed to be in WGS84 CRS.
     * @throws OperationNotFoundException
     * @throws NoSuchElementException
     * @throws FactoryException
     * @throws TransformException
     */
    public static Envelope toGeographic(Envelope env, CoordinateReferenceSystem crs) throws OperationNotFoundException, NoSuchElementException, FactoryException, TransformException{
    	if( crs.equals(GeographicCRS.WGS84) )
    		return env;
        MathTransform transform=getCoordinateOperationFactory().createOperation(crs, GeographicCRS.WGS84).getMathTransform();
        return JTS.transform(env, transform);
    }
    
    /**
     * Returns a set of all available implementations for the {@link AuthorityFactory} interface.
     * <p>
     * This Set can be used to list the available codes known to all authorities.
     * In the event that the same code is understood by more then one authority
     * you will need to assume both are close enough, or make use of this set directly
     * rather than use the decode method.
     * </p>
     */
    public static synchronized Set getCRSAuthorityFactories() {
        return new LazySet(ClassFinder.getProviders(getServiceRegistry(), CRSAuthorityFactory.class));
    }

    /**
     * Returns the default implementation of {@link MathTransformFactory}. If no implementation
     * is registered, then this method throws an exception. If more than one implementation is
     * registered, an arbitrary one is selected.
     *
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link MathTransformFactory} interface.
     */
    public static synchronized MathTransformFactory getMathTransformFactory()
            throws NoSuchElementException
    {
        return (MathTransformFactory) ClassFinder.getProviders(getServiceRegistry(), MathTransformFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the
     * {@link MathTransformFactory} interface.
     */
    public static synchronized Set getMathTransformFactories() {
        return new LazySet(ClassFinder.getProviders(getServiceRegistry(), MathTransformFactory.class));
    }

    /**
     * Returns the default implementation of {@link CoordinateOperationFactory}. If no
     * implementation is registered, then this method throws an exception. If more than
     * one implementation is registered, an arbitrary one is selected.
     *
     * @throws NoSuchElementException if no implementation was found for the
     *         {@link CoordinateOperationFactory} interface.
     */
    public static synchronized CoordinateOperationFactory getCoordinateOperationFactory()
            throws NoSuchElementException
    {
        return (CoordinateOperationFactory) ClassFinder.getProviders(getServiceRegistry(), CoordinateOperationFactory.class).next();
    }

    /**
     * Returns a set of all available implementations for the
     * {@link CoordinateOperationFactory} interface.
     */
    public static synchronized Set getCoordinateOperationFactories() {
        return new LazySet(ClassFinder.getProviders(getServiceRegistry(), CoordinateOperationFactory.class));
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
        ClassFinder.getProviders(getServiceRegistry(), DatumFactory.class); // Force the initialization of ServiceRegistry
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
            for (final Iterator providers=ClassFinder.getProviders(getServiceRegistry(), category); providers.hasNext();) {
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
