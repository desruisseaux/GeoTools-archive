package org.geotools.geometry.jts;

import org.geotools.ct.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Service object that takes a geometry an applies a MathTransform on top
 * of it.
 * @author Andrea Aime
 */
public class GeometryCoordinateSequenceTransformer {
    private MathTransform2D transform;
    private CoordinateSequenceTransformer csTransformer;

    
    public GeometryCoordinateSequenceTransformer() {
        csTransformer = new DefaultCoordinateSequenceTransformer();
    }

    /**
     * Sets the math transform to be used for transformation
     * @param transform
     */
    public void setMathTransform(MathTransform2D transform) {
        this.transform = transform;
    }
    
    
    /**
     * Applies the transform to the provided geometry, given
     * @param g
     * @return
     * @throws TransformException
     */
    public Geometry transform(Geometry g) throws TransformException {
        GeometryFactory factory = g.getFactory();
        if(g instanceof Point) {
            return transformPoint((Point) g, factory);
        } else if(g instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) g;
            Point[] points = new Point[mp.getNumGeometries()];
            for (int i = 0; i < points.length; i++) {
                points[i] = transformPoint((Point) mp.getGeometryN(i), factory);
            }
            return factory.createMultiPoint(points);
        } else if(g instanceof LineString) {
            return transformLineString((LineString) g, factory);
        } else if(g instanceof MultiLineString) {
            MultiLineString mls = (MultiLineString) g;
            LineString[] lines = new LineString[mls.getNumGeometries()];
            for (int i = 0; i < lines.length; i++) {
                lines[i] = transformLineString((LineString) mls.getGeometryN(i), factory);
            }
            return factory.createMultiLineString(lines);
        } else if(g instanceof Polygon) {
            return transformPolygon((Polygon) g, factory);
        } else if(g instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) g;
            Polygon[] polygons = new Polygon[mp.getNumGeometries()];
            for (int i = 0; i < polygons.length; i++) {
                polygons[i] = transformPolygon((Polygon) mp.getGeometryN(i), factory);
            }
            return factory.createMultiPolygon(polygons);
        } else if(g instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) g;
            Geometry[] geoms = new Geometry[gc.getNumGeometries()];
            for (int i = 0; i < geoms.length; i++) {
                geoms[i] = transform(gc.getGeometryN(i));
            }
            return factory.createGeometryCollection(geoms);
        } else {
            throw new IllegalArgumentException("Unsupported geometry type " + g.getClass());
        }
    }


    /**
     * @param string
     * @return
     * @throws TransformException
     */
    protected LineString transformLineString(LineString ls, GeometryFactory gf) throws TransformException {
        CoordinateSequence cs = projectCoordinateSequence(ls.getCoordinateSequence());
        if(ls instanceof LinearRing)
            return gf.createLinearRing(cs);
        else
            return gf.createLineString(cs);
    }


    /**
     * @param point
     * @return
     * @throws TransformException
     */
    protected Point transformPoint(Point point, GeometryFactory gf) throws TransformException {
        CoordinateSequence cs = projectCoordinateSequence(point.getCoordinateSequence());
        return gf.createPoint(cs);
    }


    /**
     * @param coordinateSequence
     * @return
     * @throws TransformException
     */
    protected CoordinateSequence projectCoordinateSequence(CoordinateSequence cs) throws TransformException {
        return csTransformer.transform(cs, transform);
    }


    /**
     * @param polygon
     * @return
     * @throws TransformException
     */
    protected Polygon transformPolygon(Polygon polygon, GeometryFactory gf) throws TransformException {
        LinearRing exterior = (LinearRing) transformLineString(polygon.getExteriorRing(), gf);
        LinearRing[] interiors = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0; i < interiors.length; i++) {
            interiors[i] = (LinearRing) transformLineString(polygon.getInteriorRingN(i), gf);
        }
        return gf.createPolygon(exterior, interiors);
    }
}
