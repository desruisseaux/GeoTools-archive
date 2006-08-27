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
package org.geotools.referencing.factory.esri;

// J2SE dependencies
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.net.URL;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Authority factory for ESRI {@linkplain CoordinateReferenceSystem Coordinate reference system}
 * backed by <cite>Well Known Text</cite>.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Rueben Schulz
 * @author Martin Desruisseaux
 */
public class FactoryUsingWKT extends DeferredAuthorityFactory implements CRSAuthorityFactory {
    /**
     * The filename to read.
     */
    private static final String FILENAME = "esri.properties";

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
        super(hints, MINIMUM_PRIORITY+20);
        factories = FactoryGroup.createInstance(hints);
        setTimeout(15*60*1000L); // Closes the connection after at least 15 minutes of inactivity.
    }

    /**
     * Returns the authority, which is {@link Citations#ESRI ESRI}.
     */
    public Citation getAuthority() {
        return Citations.ESRI;
    }

    /**
     * Creates the backing store authority factory. This method search for the {@value #FILENAME}
     * file in all locations described in the {@linkplain FactoryUsingWKT class javadoc}.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the constructor failed to find or read the file.
     *         This exception usually has an {@link IOException} as its cause.
     */
    protected AbstractAuthorityFactory createBackingStore() throws FactoryException {
        try {
            URL url = FactoryUsingWKT.class.getResource(FILENAME);
            if (url == null) {
                throw new FileNotFoundException(FILENAME);
            }
            LOGGER.log(Logging.format(Level.CONFIG, LoggingKeys.USING_FILE_AS_FACTORY_$2,
                                      url.getPath(), "ESRI"));
            return new PropertyAuthorityFactory(factories, getAuthority(), url);
        } catch (IOException exception) {
            throw new FactoryException(Errors.format(ErrorKeys.CANT_READ_$1, FILENAME), exception);
        }
    }
}
