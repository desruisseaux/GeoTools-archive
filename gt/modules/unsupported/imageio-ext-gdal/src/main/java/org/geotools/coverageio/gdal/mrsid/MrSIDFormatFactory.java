/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Management Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.coverageio.gdal.mrsid;

import it.geosolutions.imageio.plugins.mrsid.MrSIDImageReaderSpi;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;


/**
 * Implementation of the {@link Format} service provider interface for MrSID
 * files.
 *
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.5.x
 */
public final class MrSIDFormatFactory implements GridFormatFactorySpi {
    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.coverageio.gdal.mrsid");

    /**
     * Tells me if the coverage plugin to access MrSID is available or not.
     *
     * @return <code>true</code> if the plugin is available, <code>false</code> otherwise.
     */
    public boolean isAvailable() {
        boolean available = true;

        // if these classes are here, then the runtime environment has
        // access to JAI and the JAI ImageI/O toolbox.
        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");

            Class.forName("it.geosolutions.imageio.plugins.mrsid.MrSIDImageReaderSpi");
            available = new MrSIDImageReaderSpi().isAvailable();

            if (LOGGER.isLoggable(Level.FINE)) {
                if (available) {
                    LOGGER.fine("MrSIDFormatFactory is availaible.");
                } else {
                    LOGGER.fine("MrSIDFormatFactory is not availaible.");
                }
            }
        } catch (ClassNotFoundException cnf) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("MrSIDFormatFactory is not availaible.");
            }

            available = false;
        }

        return available;
    }

    /**
     * Creating a {@link MrSIDFormat}
     *
     * @return A {@link MrSIDFormat}
     */
    public Format createFormat() {
        return new MrSIDFormat();
    }

    /**
     * Returns the implementation hints. The default implementation returns en
     * empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
