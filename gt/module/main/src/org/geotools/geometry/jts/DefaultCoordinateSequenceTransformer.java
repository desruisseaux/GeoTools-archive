package org.geotools.geometry.jts;

import org.geotools.ct.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;

/**
 * @author Andrea Aime
 *
 */
public class DefaultCoordinateSequenceTransformer implements
        CoordinateSequenceTransformer {
    double[] coords;
    CoordinateSequenceFactory csFactory;
    
    public DefaultCoordinateSequenceTransformer() {
        coords = new double[100];
        csFactory = DefaultCoordinateSequenceFactory.instance();
    }

    /**
     * @throws TransformException
     * @see org.geotools.geometry.jts.CoordinateSequenceTransformer#transform(com.vividsolutions.jts.geom.CoordinateSequence, org.geotools.ct.MathTransform2D)
     */
    public CoordinateSequence transform(CoordinateSequence cs,
            MathTransform2D transform) throws TransformException {
        Coordinate[] scs = cs.toCoordinateArray();
        Coordinate[] tcs = new Coordinate[scs.length];
        if(coords.length < (scs.length * 2))
            coords = new double[scs.length * 2];
        for (int i = 0; i < scs.length; i++) {
            coords[i * 2] = scs[i].x; 
            coords[i * 2 + 1] = scs[i].y;
        }
        transform.transform(coords, 0, coords, 0, scs.length);
        for (int i = 0; i < tcs.length; i++) {
            tcs[i] = new Coordinate(coords[i * 2], coords[i * 2 + 1]);
        }        
        return csFactory.create(tcs);
    }
}
