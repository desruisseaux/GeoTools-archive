package org.geotools.geometry.jts.spatialschema.geometry.geometry;

import java.util.List;

import org.opengis.spatialschema.geometry.geometry.Polygon;
import org.opengis.spatialschema.geometry.primitive.Ring;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;
import org.opengis.spatialschema.geometry.primitive.SurfaceInterpolation;

import org.geotools.geometry.jts.spatialschema.geometry.primitive.SurfacePatchImpl;
import org.geotools.geometry.jts.JTSGeometry;
import org.geotools.geometry.jts.JTSUtils;

public class PolygonImpl extends SurfacePatchImpl implements Polygon {
    
    //*************************************************************************
    //  Fields
    //*************************************************************************
    
    // Why the hell is this a list???
    private List spanningSurface;

    //*************************************************************************
    //  Constructors
    //*************************************************************************
    
    public PolygonImpl(SurfaceBoundary boundary) {
        // We only support planar polygons
        this(boundary, null);
    }

    public PolygonImpl(SurfaceBoundary boundary, List spanningSurface) {
        super(SurfaceInterpolation.PLANAR, boundary);
        this.spanningSurface = spanningSurface;
    }

    //*************************************************************************
    //  implement the *** interface
    //*************************************************************************
    
    public int getNumDerivativesOnBoundary() {
        return 0;
    }

    /**
     * @return
     * @see com.polexis.lite.spatialschema.geometry.primitive.SurfacePatchImpl#calculateJTSPeer()
     */
    public com.vividsolutions.jts.geom.Geometry calculateJTSPeer() {
        SurfaceBoundary boundary = getBoundary();
        Ring exterior = boundary.getExterior();
        Ring [] interiors = boundary.getInteriors();
        com.vividsolutions.jts.geom.Geometry g = ((JTSGeometry) exterior).getJTSGeometry();
        int numHoles = (interiors != null) ? interiors.length : 0;
        com.vividsolutions.jts.geom.LinearRing jtsExterior =
            JTSUtils.GEOMETRY_FACTORY.createLinearRing(g.getCoordinates());
        com.vividsolutions.jts.geom.LinearRing [] jtsInterior =
            new com.vividsolutions.jts.geom.LinearRing[numHoles];
        for (int i=0; i<numHoles; i++) {
            com.vividsolutions.jts.geom.Geometry g2 =
                ((JTSGeometry) interiors[i]).getJTSGeometry();
            jtsInterior[i] = JTSUtils.GEOMETRY_FACTORY.createLinearRing(g2.getCoordinates());
        }
        com.vividsolutions.jts.geom.Polygon result =
            JTSUtils.GEOMETRY_FACTORY.createPolygon(jtsExterior, jtsInterior);
        return result;
    }

    /**
     * @return
     * @see org.opengis.spatialschema.geometry.geometry.Polygon#getSpanningSurface()
     */
    public List getSpanningSurface() {
        // Why the hell is this a list???
        return spanningSurface;
    }
    
    public boolean isValid() {
    	com.vividsolutions.jts.geom.Polygon poly = (com.vividsolutions.jts.geom.Polygon)
			this.getJTSGeometry();
    	return poly.isValid();
    }
}
