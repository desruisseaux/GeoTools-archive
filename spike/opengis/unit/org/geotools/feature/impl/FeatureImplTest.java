package org.geotools.feature.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.feature.Descriptors;
import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.ComplexTestData;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class FeatureImplTest extends TestCase {

	/**
	 * contents:
	 * 
	 * <pre>
	 *     [
	 *     	measurement[
	 *     		determinand_description: d1
	 *      	result: 1
	 *     	]
	 *     	measurement[
	 *     		determinand_description: d2;
	 *      	result: 2
	 *     	]
	 *        location[ Point(-1, -1)]
	 *        nearestSlimePit[Point(10, 10)]
	 *        sitename[ site1 ]
	 *        sitename[ site2 ]
	 *        sitename[ site3 ]
	 *     ]
	 * </pre>
	 */
	private Feature sampleFeature;
	
	private FeatureType featureType;

	private Geometry location, nearestSlimePit; 
	
	protected void setUp() throws Exception {
		super.setUp();
		
		GeometryFactory gf = new GeometryFactory();
		nearestSlimePit = gf.createPoint(new Coordinate(10, 10));
		location = gf.createPoint(new Coordinate(-1, -1));
		
		TypeFactory tf = new TypeFactoryImpl();
		DescriptorFactory df = new DescriptorFactoryImpl();
		featureType = ComplexTestData.createExample03MultipleGeometries(tf, df);
		AttributeFactoryImpl af = new AttributeFactoryImpl();
		sampleFeature = af.create(featureType, null);
		
		AttributeType type;
		Attribute att;
		Descriptor schema = featureType.getDescriptor();

		List<Attribute>contents = new ArrayList<Attribute>();
		
		ComplexType measurementType = (ComplexType)Descriptors.type(schema, "measurement");
		
		att = af.create(measurementType, null);
		List<Attribute>measurementContent = new ArrayList<Attribute>();
		
		Attribute att2;
		att2 = af.create(measurementType.type("determinand_description"), null, "d1");
		measurementContent.add(att2);
		att2 = af.create(measurementType.type("result"), null, "1");
		measurementContent.add(att2);
		att.set(measurementContent);
		
		measurementContent.clear();
		contents.add(att);

		att2 = af.create(measurementType.type("determinand_description"), null, "d2");
		measurementContent.add(att2);
		att2 = af.create(measurementType.type("result"), null, "2");
		measurementContent.add(att2);
		att.set(measurementContent);
		
		contents.add(att);
		
		att = af.create(featureType.type("location"), null, location);
		contents.add(att);

		att = af.create(featureType.type("nearestSlimePit"), null, nearestSlimePit);
		contents.add(att);

		att = af.create(featureType.type("sitename"), null, "site1");
		contents.add(att);
		att = af.create(featureType.type("sitename"), null, "site2");
		contents.add(att);
		att = af.create(featureType.type("sitename"), null, "site3");
		contents.add(att);
		
		sampleFeature = new FeatureImpl("test-id", featureType);
		sampleFeature.set(contents);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for
	 * 'org.geotools.feature.impl.FeatureImpl.FeatureImpl(String, FeatureType)'
	 */
	public void testConstruct() {
		try{
			new FeatureImpl(null, featureType);
			fail("null id");
		}catch(NullPointerException e){
			//OK
		}
		
		try{
			new FeatureImpl("test-id", null);
			fail("null type");
		}catch(NullPointerException e){
			//OK
		}

		FeatureImpl f = new FeatureImpl("test-id", featureType);
		assertEquals("test-id", f.ID);
		assertEquals(featureType, f.TYPE);
		assertNotNull(f.attribtues);
		assertEquals(0, f.attribtues.size());
	}

	/*
	 * Test method for 'org.geotools.feature.impl.FeatureImpl.getType()'
	 */
	public void testGetType() {
		assertNotNull(sampleFeature.getType());
		assertSame(featureType, sampleFeature.getType());
	}

	/*
	 * Test method for 'org.geotools.feature.impl.FeatureImpl.getCRS()'
	 */
	public void testGetCRS() {
		GeometryType defaultGeometryType = sampleFeature.getType().getDefaultGeometry(); 
		CoordinateReferenceSystem crs = defaultGeometryType.getCRS();
		assertNotNull(sampleFeature.getCRS());
	}

	/*
	 * Test method for 'org.geotools.feature.impl.FeatureImpl.getBounds()'
	 */
	public void testGetBounds() {
		Envelope expected = new Envelope();
		expected.expandToInclude(location.getEnvelopeInternal());
		expected.expandToInclude(nearestSlimePit.getEnvelopeInternal());
		
		Envelope bounds = sampleFeature.getBounds();
		assertNotNull(bounds);
		assertTrue(expected.equals(bounds));
	}

	/*
	 * Test method for
	 * 'org.geotools.feature.impl.FeatureImpl.getDefaultGeometry()'
	 */
	public void testGetDefaultGeometry() {
		Geometry geom = sampleFeature.getDefaultGeometry();
		assertNotNull(geom);
		assertTrue(geom instanceof Point);
		assertTrue(nearestSlimePit.equals(geom));
	}

	/*
	 * Test method for
	 * 'org.geotools.feature.impl.FeatureImpl.setDefault(Geometry)'
	 */
	public void testSetDefaultGeometry() {
		sampleFeature.setDefaultGeometry(null);
		assertNull(sampleFeature.getDefaultGeometry());
		GeometryFactory gf = new GeometryFactory();
		Geometry badType = gf.createGeometryCollection(new Geometry[]{});
		try{
			sampleFeature.setDefaultGeometry(badType);
			fail("allowed bogus geometry");
		}catch(IllegalArgumentException e){
			//OK
		}
		Point good = gf.createPoint(new Coordinate(100, 100));
		sampleFeature.setDefaultGeometry(good);
		assertTrue(good.equals(sampleFeature.getDefaultGeometry()));
	}

}
