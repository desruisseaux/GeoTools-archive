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

import java.util.List;

import org.geotools.catalog.ServiceInfo;
import org.geotools.data.DataAccess;
import org.geotools.data.Source;
import org.opengis.feature.type.TypeName;

/**
 * This is an example of using DataAccess proposal to access raster content.
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface GridAccess extends DataAccess/*<GridCoverage,GridCoverageDescription>*/  {
    
    /** Names of avaiable content. */    
    List/*<TypeName*/ getTypeNames();
    
    /**
     * Metadata about this access point
     * 
     * @return GridServiceInfo ?
     */
    ServiceInfo getInfo();
    
    /**
     * Description of GridCoverageDescription.
     * 
     * @return GridCoverageDescription
     */
    public Object /*GridCoverageDescription*/ describe( TypeName typeName );
       
    /**
     * 
     *@return GridSource
     */
    public Source access( TypeName typeName );
    
    /**
     * This will free any cached info object or header information.
     * <p>
     * Often a GridAccess will keep a file channel open, this will clean that
     * sort of thing up.
     * </p>
     */
    public void dispose();
}
