package org.geotools.data.grid;

import java.util.List;

import org.geotools.catalog.ServiceInfo;
import org.geotools.data.DataAccess;
import org.geotools.data.Source;
import org.opengis.feature.type.TypeName;

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
