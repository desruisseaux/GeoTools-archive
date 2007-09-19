/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Refractions Reserach Inc.
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
package org.geotools.graph.build.feature;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.structure.Edge;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class FeatureGraphGeneratorTest extends TestCase {

	public void test() throws Exception {
		LineStringGraphGenerator lsgg = new LineStringGraphGenerator();
		FeatureGraphGenerator fgg = new FeatureGraphGenerator( new LineStringGraphGenerator() );
		
		LineString[] lines = lines();
		Feature[] features = features( lines );
		
		List el1 = new ArrayList();
		List el2 = new ArrayList();
		for ( int i = 0; i < lines.length; i++ ) {
			el1.add( lsgg.add( lines[i] ) );
			el2.add( fgg.add( features[i] ) );
		}
		
		for ( int i = 0; i < el1.size(); i++ ) {
			Edge e1 = (Edge) el1.get( i );
			Edge e2 = (Edge) el2.get( i );
			
			assertTrue( e1.getObject() instanceof LineString );
			assertTrue( e2.getObject() instanceof Feature );
			
			LineString line = (LineString) e1.getObject();
			Feature feature = (Feature) e2.getObject();
			
			assertEquals( line, feature.getDefaultGeometry() );
		}
	}
	
	LineString[] lines() {
		GeometryFactory gf = new GeometryFactory();
		LineString[] lines = new LineString[5];
		
		for ( int i = 0; i < lines.length; i++ ) {
			lines[i] = gf.createLineString(
				new Coordinate[]{
					new Coordinate( i, i+1), new Coordinate( i+2, i+3 )
				}
			);
		}
		
		return lines;
	}
	
	Feature[] features( LineString[] lines ) throws SchemaException, IllegalAttributeException {
		
		FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance( "test" );
		typeFactory.addType( AttributeTypeFactory.newAttributeType( "the_geom", LineString.class ) );
		typeFactory.addType( AttributeTypeFactory.newAttributeType( "id", Integer.class ) );
		
		FeatureType schema = typeFactory.getFeatureType();
		Feature[] features = new Feature[ lines.length ];
		for ( int i = 0; i < lines.length; i++) {
			features[i] = schema.create( new Object[] { lines[i], new Integer( i ) } );
		}
		
		return features;
	}
	
	
}
