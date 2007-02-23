package org.geotools.geometry.jts.spatialschema.geometry.primitive;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.geometry.PolyhedralSurface;

/**
 * The {@code PolyhedralSurfaceImpl} class/interface...
 * 
 * @author SYS Technologies
 * @author dillard
 * @version $Revision $
 */
public class PolyhedralSurfaceImpl extends SurfaceImpl implements PolyhedralSurface {
    
    /**
     * Creates a new {@code PolyhedralSurfaceImpl}.
     * @param crs
     */
    public PolyhedralSurfaceImpl(CoordinateReferenceSystem crs) {
        super(crs);
    }
}
