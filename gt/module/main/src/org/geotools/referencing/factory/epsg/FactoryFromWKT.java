/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URL;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.referencing.factory.PropertyAuthorityFactory;


/**
 * Implementation for a coordinate reference system authority factory backed
 * by the EPSG property file. This factory is used as a fallback when no
 * connection to an EPSG database is available. It search for a file named
 * {@value #FILENAME} in the first of the following locations where such a
 * file is found:
 *
 * <ul>
 *   <li>(...todo...)</li>
 *   <li>{@code org/geotools/referencing/factory/espg} directory in the classpath
 *       or in a JAR file.</li>
 * </ul>
 *
 * @version $Id$
 * @author Jody Garnett
 * @author Rueben Schulz
 * @author Martin Desruisseaux
 */
public class FactoryFromWKT extends DeferredAuthorityFactory {
    /**
     * The filename to read. This file will be searched in all locations
     * described in the {@linkplain #FactoryFromWKT class javadoc}.
     */
    public static final String FILENAME = "epsg.properties";

    /**
     * Constructs an authority factory using the default set of
     * {@linkplain org.opengis.referencing.ObjectFactory object factories}.
     */
    public FactoryFromWKT() {
        super(new FactoryGroup(), MINIMUM_PRIORITY+20);
        setTimeout(15*60*1000L); // Closes the connection after at least 15 minutes of inactivity.
    }

    /**
     * Returns the authority, which is {@link CitationImpl#EPSG EPSG}.
     */
    public Citation getAuthority() {
        return CitationImpl.EPSG;
    }

    /**
     * Creates the backing store authority factory. This method search for the {@value #FILENAME}
     * file in all locations described in the {@linkplain #FactoryFromWKT class javadoc}.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the constructor failed to find or read the file.
     *         This exception usually has an {@link IOException} as its cause.
     */
    protected AbstractAuthorityFactory createBackingStore() throws FactoryException {
        try {
            URL url;
            url = FactoryFromWKT.class.getResource(FILENAME);
            if (url == null) {
                throw new FileNotFoundException(FILENAME);
            }
            LOGGER.config("Using \""+url.getPath()+"\" as EPSG factory."); // TODO: localize
            return new PropertyAuthorityFactory(factories, getAuthority(), url);
        } catch (IOException exception) {
            // TODO: localize
            throw new FactoryException("Failed to read \""+FILENAME+"\".", exception);
        }
    }
}
