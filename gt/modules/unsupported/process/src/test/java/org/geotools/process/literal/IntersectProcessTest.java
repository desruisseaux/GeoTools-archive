package org.geotools.process.literal;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.factory.FactoryFinder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Test case to watch this thing turn over.
 * 
 * @author Jody
 */
public class IntersectProcessTest extends TestCase {

    public void testIntersectProcess() throws Exception {
        WKTReader reader = new WKTReader( new GeometryFactory() );
        
        Geometry geom1 = (Polygon) reader.read("POLYGON((20 10, 30 0, 40 10, 30 20, 20 10))");
        Geometry geom2 = (Polygon) reader.read("POLYGON((20 10, 30 0, 40 10, 20 10))");
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( IntersectsFactory.GEOM1.key, geom1 );
        map.put( IntersectsFactory.GEOM2.key, geom2 );
        IntersectProcess process = new IntersectProcess(null, map );
        
        Map<String, Object> resultMap = process.process( null );
        assertNotNull( resultMap );
        Object result = resultMap.get(IntersectsFactory.RESULT.key);
        assertNotNull( result );
        assertTrue( "expected geometry", result instanceof Geometry );
        Geometry intersection = geom1.intersection(geom2);
        assertTrue( intersection.equals( (Geometry) result ) );
    }
}
