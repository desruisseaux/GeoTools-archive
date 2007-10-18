package org.geotools.filter.capability;

import org.opengis.filter.capability.GeometryOperand;
import org.opengis.filter.capability.SpatialOperator;

/**
 * Implementation of the SpatialOperator interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SpatialOperatorImpl extends OperatorImpl 
    implements SpatialOperator {

    GeometryOperand[] geometryOperands;
    
    public SpatialOperatorImpl( String name, GeometryOperand[] geometryOperands ) {
        super( name );
        this.geometryOperands = geometryOperands;
    }
    
    public GeometryOperand[] getGeometryOperands() {
        return geometryOperands;
    }

}
