package org.geotools.data.crs.geometry;

import org.geotools.ct.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;

/**
 * This coordinate sequence transformer will take a Geometry and transform in a set
 * of curved lines that will be "flattened" in order to get back a set of straight segments.
 * The error in the transform is linked to the "flattening", the higher the flattening,
 * the bigger the error, but also, the lesser the number of points that will be used
 * to represent the resulting coordinate sequence. 
 *
 */
public class PreciseCoordinateSequenceTransformer implements CoordinateSequenceTransformer {
    CoordinateSequenceFactory csFactory;
    double flatness;
    
    public PreciseCoordinateSequenceTransformer() {
        csFactory = DefaultCoordinateSequenceFactory.instance();
    }
    
    /**
     * @see org.geotools.data.crs.geometry.CoordinateSequenceTransformer#transform(com.vividsolutions.jts.geom.CoordinateSequence, org.geotools.ct.MathTransform2D)
     */
    public CoordinateSequence transform(CoordinateSequence cs, MathTransform2D transform)
            throws TransformException {
//        Coordinate[] scs = cs.toCoordinateArray();
//        GeneralPath path = new GeneralPath();
//        path.moveTo(scs[0].x, scs[0].y);
//        for (int i = 0; i < scs.length; i++) {
//            path.moveTo(scs[0].x, scs[0].y);
//        }
//        Shape transformed = transform.createTransformedShape(path);
//        PathIterator iterator = transformed.getPathIterator(new AffineTransform(), flatness);
//        ArrayList coords = new ArrayList(scs.length);
//        double[] point = new double[6];
//        while(!iterator.isDone()) {
//            iterator.next();
//            iterator.currentSegment(point);
//            coords.add(new Coordinate(point[0], point[1]));
//        }
//        return csFactory.create(coords);
    	return null;
    }

    public double getFlatness() {
        return flatness;
    }
    public void setFlatness(double flatness) {
        this.flatness = flatness;
    }
}
