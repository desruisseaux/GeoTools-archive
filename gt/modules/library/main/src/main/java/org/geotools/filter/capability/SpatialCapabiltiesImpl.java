package org.geotools.filter.capability;

import org.opengis.filter.capability.GeometryOperand;
import org.opengis.filter.capability.SpatialCapabilities;
import org.opengis.filter.capability.SpatialOperators;

public class SpatialCapabiltiesImpl implements SpatialCapabilities {

    GeometryOperand[] geometryOperands;
    SpatialOperators spatialOperators;
    
    public SpatialCapabiltiesImpl( GeometryOperand[] geometryOperands, 
            SpatialOperators spatialOperators) {
        this.geometryOperands = geometryOperands;
        this.spatialOperators = spatialOperators;
    }

    public GeometryOperand[] getGeometryOperands() {
        return geometryOperands;
    }
    
    public SpatialOperators getSpatialOperators() {
        return spatialOperators;
    }

    

}
