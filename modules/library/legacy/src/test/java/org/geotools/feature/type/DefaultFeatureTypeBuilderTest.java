/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.feature.type;

import junit.framework.TestCase;

import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureType;

import com.vividsolutions.jts.geom.Point;

public class DefaultFeatureTypeBuilderTest extends TestCase {

	static final String URI = "gopher://localhost/test";
	
	DefaultFeatureTypeBuilder builder;
	
	protected void setUp() throws Exception {
		builder = new DefaultFeatureTypeBuilder();
	}
	
	public void testSanity() {
		builder.setName( "testName" );
		builder.setNamespaceURI( "testNamespaceURI" );
		builder.add( "point", Point.class );
		builder.add( "integer", Integer.class );
		
		DefaultFeatureType type = (DefaultFeatureType) builder.buildFeatureType();
		assertNotNull( type );
		
		assertEquals( 2, type.getAttributeCount() );
		
		AttributeType t = type.getAttributeType( "point" );
		assertNotNull( t );
		assertEquals( Point.class, t.getBinding() );
		
		t = type.getAttributeType( "integer" );
		assertNotNull( t );
		assertEquals( Integer.class, t.getBinding() );
		
		t = type.getDefaultGeometry();
		assertNotNull( t );
		assertEquals( Point.class, t.getBinding() );
	}
}
