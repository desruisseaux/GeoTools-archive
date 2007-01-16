/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.coverage.io;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

// OpenGIS dependencies
import org.opengis.coverage.grid.Format;

// Geotools dependencies
import org.geotools.data.coverage.grid.GridFormatFactorySpi;


/**
 * A factory to create the format for a netCDF file. It verifies if JAI and JAI-IO are reachable.
 *
 * @author Cédric Briançon
 */
public class NetcdfFormatFactorySpi implements GridFormatFactorySpi {
    /**
     * The depth for the raster.
     */
    private static final int DEPTH = 0;

    /**
     *
     */
    public NetcdfFormatFactorySpi() {}

    /**
     * The format is created if the needed classes in JAI and JAI Image IO are found.
     */
    public Format createFormat() {
        if (!isAvailable()) {
            throw new UnsupportedOperationException(
                    "The netCDF plugin requires the JAI and JAI ImageI/O libraries.");
        }

        //System.out.println("depth :" + DEPTH);
        return new NetcdfFormat(DEPTH);
    }

    /**
     * Verifies if the JAI and JAI-IO package are installed on your machine and reachables.
     *
     * @return True if the needed classes are found, false otherwise.
     */
    public boolean isAvailable() {
        boolean available = true;
        // verifies if these classes are found.
        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
        } catch (ClassNotFoundException cnf) {
            available = false;
        }
        return available;
    }

    /**
     * Returns the implementation hints. The default implementation returns en
     * empty map.
     *
     * @return Empty Map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
