/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.gce.arcgrid;

import java.util.Collections;
import java.util.Map;

import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;


/**
 * Implementation of the GridCoverageFormat service provider interface for arc
 * grid files.
 *
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public class ArcGridFormatFactory implements GridFormatFactorySpi {
    public boolean isAvailable() {
        boolean available = true;

        // if these classes are here, then the runtine environment has 
        // access to JAI and the JAI ImageI/O toolbox.
        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
            Class.forName("org.geotools.gce.imageio.asciigrid.AsciiGridsImageMetadata");
        } catch (ClassNotFoundException cnf) {
            available = false;
        }

        return available;
    }

    public Format createFormat() {
        return new ArcGridFormat();
    }

    /**
     * Returns the implementation hints. The default implementation returns en
     * empty map.
     *
     * @return DOCUMENT ME!
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
