/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wfs;

import java.io.InputStream;

import org.geotools.TestData;
import org.geotools.feature.FeatureType;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.schema.Schema;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

/**
 * @author Jesse
 *
 */
public class CreateFeatureTypeTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSimple() throws Exception {
		InputStream in = TestData.openStream("xml/feature-type-simple.xsd");
		try{
	        Schema schema = SchemaFactory.getInstance(null, in);
	        FeatureType ft = WFSDataStore.parseDescribeFeatureTypeResponse("WATER", schema);
	        assertNotNull(ft);
	        assertEquals(Integer.class, ft.getAttributeType("ID").getType());
	        assertEquals(String.class, ft.getAttributeType("CODE").getType());
	        assertEquals(Float.class, ft.getAttributeType("KM").getType());
	        assertEquals(Polygon.class, ft.getAttributeType("GEOM").getType());
		}finally{
			in.close();
		}
	}

	public void testChoiceGeom() throws Exception {
		InputStream in = TestData.openStream("xml/feature-type-choice.xsd");
		try{
	        Schema schema = SchemaFactory.getInstance(null, in);
	        FeatureType ft = WFSDataStore.parseDescribeFeatureTypeResponse("WATER", schema);
	        assertNotNull(ft);
	        assertEquals(Integer.class, ft.getAttributeType("ID").getType());
	        assertEquals(String.class, ft.getAttributeType("CODE").getType());
	        assertEquals(Float.class, ft.getAttributeType("KM").getType());
	        assertEquals(MultiPolygon.class, ft.getAttributeType("GEOM").getType());
		}finally{
			in.close();
		}
	}
	
}
