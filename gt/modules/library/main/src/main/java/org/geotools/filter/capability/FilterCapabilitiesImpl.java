package org.geotools.filter.capability;

import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.capability.IdCapabilities;
import org.opengis.filter.capability.ScalarCapabilities;
import org.opengis.filter.capability.SpatialCapabilities;

/**
 * Implementation of the FilterCapabilities interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FilterCapabilitiesImpl implements FilterCapabilities {

    String version;
    IdCapabilities id;
    ScalarCapabilities scalar;
    SpatialCapabilities spatial;
    
    public FilterCapabilitiesImpl( String version, ScalarCapabilities scalar, 
        SpatialCapabilities spatial, IdCapabilities id ) {
        this.version = version;
        this.id = id;
        this.scalar = scalar;
        this.spatial = spatial;
    }
    
    public String getVersion() {
        return version;
    }
    
    public IdCapabilities getIdCapabilities() {
        return id;
    }

    public ScalarCapabilities getScalarCapabilities() {
        return scalar;
    }

    public SpatialCapabilities getSpatialCapabilities() {
        return spatial;
    }
}
