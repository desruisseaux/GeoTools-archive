package org.geotools.feature.type;

import java.util.Collections;

import junit.framework.TestCase;

import org.geotools.feature.Types;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;

public class GeometryTypeImplTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.geotools.feature.type.GeometryTypeImpl.getCRS()'
	 */
	public void testGetCRS() throws FactoryException{
		
		GeometryType type = new GeometryTypeImpl(
			Types.typeName("testType"), LineString.class, null, false, false, 
			Collections.EMPTY_SET, null, null
		);
		
		assertNull(type.getCRS());
		
		CoordinateReferenceSystem crs = CRS.parseWKT("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");
		type = new GeometryTypeImpl(
			Types.typeName("testType"), LineString.class, crs, false, false, 
			Collections.EMPTY_SET, null, null
		);
		assertSame(crs, type.getCRS());
	}

}
