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
package org.geotools.gce.gtopo30;

import java.util.Collections;
import java.util.Map;

import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;

/**
 * The GTopo30FormatFactory will be discovered by the
 * GridFormatFinder. Use the standard Geotools method of discovering
 * a factory in order to create a format.
 *
 * @author giannecchini
 * @author mkraemer
 * @source $URL$
 */
public class GTopo30FormatFactory implements GridFormatFactorySpi {
    /**
     * Creates a new instance of GTopo30Format
     * 
     * @return an instance of GTopo30Format
     */
    public Format createFormat() {
        return new GTopo30Format();
    }

    /**
     * Checks for the JAI library which is needed by the
     * GTopo30DataSource
     * 
     * @return true if all libraries are available
     */
    public boolean isAvailable() {
    	boolean available = true;

        // if these classes are here, then the runtine environment has 
        // access to JAI and the JAI ImageI/O toolbox.
        try {
        	Class.forName("javax.media.jai.ImageLayout");
        	Class.forName("javax.media.jai.JAI");
        	Class.forName("javax.media.jai.ParameterBlockJAI");
        	Class.forName("javax.media.jai.RenderedOp");
        	Class.forName("com.sun.media.imageio.stream.FileChannelImageInputStream");
        	Class.forName("com.sun.media.imageio.stream.RawImageInputStream");
        } catch (ClassNotFoundException cnf) {
            available = false;
        }

        return available;
    }

    /**
     * Returns the implementation hints
     * 
     * @return the implementation hints (an empty map, actually)
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
