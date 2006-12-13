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
package org.geotools.data.grid;

import java.io.IOException;
import java.util.List;

import org.geotools.catalog.Service;
import org.geotools.util.ProgressListener;

/**
 * Service handle describing a source of raster data.
 * <p>
 * A handle may point to an external service or database but is often a simple
 * file. Even in the case of a simple file some formats allow multiple coverages
 * be described - each will be made available as a GeoResource via the members method.
 * </p>
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface GridService extends Service {

    /**
     * Describes the connection status of the "preferred" resource,
     * ie the same one described by the Service.getMessage().
     * 
     * TODO steal description of this concept from uDig javadocs 
     */
    List/**ConnectionStatus*/ getConnectionStatus(); // TODO: Consider this as an extention to Serice that let's us hack at warnings in a user friendly fashion
    
    /**
     * Provide access to our preferred resourc (ie GridAccess)
     * <p>
     * This method is called by our implementation of resolve( GridAccess.class, PorgressListener )
     * </p>
     * @return GridAccess
     */
    GridAccess access();
    
    /**
     * @return List<GridGeoResource>
     */
    List members( ProgressListener monitor ) throws IOException;
}
