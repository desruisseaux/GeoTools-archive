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

import org.geotools.catalog.GeoResource;
import org.geotools.catalog.Resolve;
import org.geotools.catalog.Service;
import org.geotools.util.ProgressListener;
import org.opengis.coverage.grid.GridCoverage;

/**
 * A handle to a grid coverage for use in a catalog.
 * <p>
 * The handle allows "lazy" access to grid coverage information, that is descriptive information
 * (GeoResourceInfo) may be available based on header information, prior to slurping up the
 * entire content.
 * </p>
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface GridGeoResource extends GeoResource {
    
    /**
     * Note this is the direct parent, may be another GridGeoResource
     * in the case of tiled fun.
     */
    Resolve parent( ProgressListener monitor ) throws IOException;
    
    /**
     * This method is a helper, allows you to loop through parent and return 
     * the first instanceof Service.
     */
    Service service( ProgressListener monitor ) throws IOException;
    
    /**
     * Describes the connection status of the "preferred" resource.
     * <p>
     * This list provides details on any troubles, assumptions or errors
     * when obtaining source().
     */
    List/**ConnectionStatus*/ getConnectionStatus(); // TODO: Consider this as an extention to Serice that let's us hack at warnings in a user friendly fashion
    
    /**
     * Provide access to our preferred resourc (ie GridSource)
     * <p>
     * This method is called by our implementation of resolve( GridSource.class, PorgressListener )
     * </p>
     * @return GridAccess
     */
    GridSource source();
    
    /**
     * Optional method, providing quick access to GridCoverage?
     * <p>
     * This is simliar to wrapping up this collection <code>source().content()</code>
     * as a single GridCoverage.
     * 
     * @return GridCoverage
     */
    GridCoverage coverage(); // TODO: simboss feedback needed as I don't understand grid coverage
    
    /**
     * You could consider making this nested for tiled GridCoverage stuff.
     * 
     * @return List<GridResource>
     */
    List members( ProgressListener monitor ) throws IOException;
}
