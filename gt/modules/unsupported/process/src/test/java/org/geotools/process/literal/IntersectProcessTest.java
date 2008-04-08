/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.process.literal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        
        IntersectionProcess process = new IntersectionProcess( null );        
        Map<String, Object> resultMap = process.process( map, null );
        
        assertNotNull( resultMap );
        Object result = resultMap.get(IntersectsFactory.RESULT.key);
        assertNotNull( result );
        assertTrue( "expected geometry", result instanceof Geometry );
        Geometry intersection = geom1.intersection(geom2);
        assertTrue( intersection.equals( (Geometry) result ) );
    }
    
    public void testUnionProcess() throws Exception {
        WKTReader reader = new WKTReader( new GeometryFactory() );
        List<Geometry> list = new ArrayList<Geometry>();
        list.add( reader.read("POLYGON((20 10, 30 0, 40 10, 30 20, 20 10))") );
        list.add( reader.read("POLYGON((20 10, 30 0, 40 10, 20 10))") );
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( UnionFactory.GEOM1.key, list );
        
        UnionProcess process = new UnionProcess( null );        
        Map<String, Object> resultMap = process.process( map, null );
        
        assertNotNull( resultMap );
        Object result = resultMap.get(IntersectsFactory.RESULT.key);
        assertNotNull( result );
        assertTrue( "expected geometry", result instanceof Geometry );        
    }    
}
