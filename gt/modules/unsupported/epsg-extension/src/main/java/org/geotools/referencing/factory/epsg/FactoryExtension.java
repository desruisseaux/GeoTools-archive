/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
import java.net.URL;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.factory.Hints;


/**
 * Provides common {@linkplain CoordinateReferenceSystem Coordinate Reference Systems}
 * not found in the standard EPSG database. Those CRS will be registered in 
 * {@code "EPSG"} name space.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Andrea Aime
 */
public class FactoryExtension extends FactoryUsingWKT {
    /**
     * The default filename to read. This file will be searched in the
     * {@code org/geotools/referencing/factory/espg} directory in the
     * classpath or in a JAR file.
     *
     * @see #getDefinitionsURL
     */
    public static final String FILENAME = "extension.properties";

    /**
     * Constructs an authority factory using the default set of factories.
     */
    public FactoryExtension() {
        this(null);
    }

    /**
     * Constructs an authority factory using a set of factories created from the specified hints.
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints.
     */
    public FactoryExtension(final Hints hints) {
        super(hints, DEFAULT_PRIORITY - 2);
    }

    /**
     * Returns the URL to the property file that contains CRS definitions.
     * The default implementation returns the URL to the {@value #FILENAME} file.
     *
     * @return The URL, or {@code null} if none.
     */
    protected URL getDefinitionsURL() {
        return FactoryExtension.class.getResource(FILENAME);
    }

    /**
     * Prints a list of codes that duplicate the ones provided in the {@link DefaultFactory}.
     * The factory tested is the one registered in {@link ReferencingFactoryFinder}.  By default, this
     * is this {@code FactoryExtension} class backed by the {@value #FILENAME} property file.
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
     */
    public static void main(final String[] args) throws FactoryException {
        main(args, FactoryExtension.class);
    }
}
