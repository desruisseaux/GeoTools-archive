package org.geotools.feature;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.impl.AttributeImpl;
import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class XPathTest extends TestCase {

	private SimpleFeature simpleFeature;
	private Feature complexFeature;

	
	protected void setUp() throws Exception {
		super.setUp();
		simpleFeature = createSimpleFeature();
		//complexFeature = createComplexFeature();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		simpleFeature = null;
		complexFeature = null;
	}

	public void testSelf()throws Exception{
		Object val;
		
		val = XPath.get(simpleFeature, ".");
		assertEquals(simpleFeature, val);
		
		TypeFactory typef = new TypeFactoryImpl();
		AttributeType simpleType = typef.createType("SimpleType", String.class);
		AttributeImpl simpleAtt = new AttributeImpl(simpleType);
		simpleAtt.set("test value");
		
		val = XPath.get(simpleAtt, ".");
		assertEquals(simpleAtt, val);

	}
	
	
	public void testSimpleFeatureGetValue()throws Exception{
		Object val;
		
		val = XPath.get(simpleFeature, "oid");
		assertEquals("testOid", val);

		val = XPath.get(simpleFeature, "number");
		assertEquals(new Integer(1),val);
		
		val = XPath.get(simpleFeature, "the_geom");
		assertTrue(val instanceof Point);
		assertEquals(1D, ((Point)val).getX(), 0);
		assertEquals(-1D, ((Point)val).getY(), 0);
		
		val = XPath.get(simpleFeature, "the_geom/envelopeInternal/minX");
		assertEquals(new Double(1), val);
		val = XPath.get(simpleFeature, "the_geom/envelopeInternal/minY");
		assertEquals(new Double(-1), val);

		/*
		 * val = XPath.get(simpleFeature, "the_geom/coordinates[1]");
		 * assertTrue( val instanceof Coordinate);
		 */
	}
	
	
	/*
	public void testSimpleFeatureSetValue(){

		FeatureXPath.setValue(simpleFeature, "oid", "newId");
		assertEquals("newId", simpleFeature.getAttribute("oid"));

		FeatureXPath.setValue(simpleFeature, "number", new Integer(2));
		assertEquals(new Integer(2), simpleFeature.getAttribute("number"));
		
		Point newPoint = new GeometryFactory().createPoint(new Coordinate(2, -2));
		FeatureXPath.setValue(simpleFeature, "the_geom", newPoint);
		assertEquals(2D, ((Point)simpleFeature.getAttribute("the_geom")).getX(), 0);
		assertEquals(-2D, ((Point)simpleFeature.getAttribute("the_geom")).getY(), 0);

	}
	*/

	/**
	 * Creates a simple feature with the following characteristics:
	 * <p>
	 * <ul>
	 * <li>schema: "oid:String,name:String,number:Integer,the_geom:Point"
	 * <ul>Values:
	 * <li>oid: "testOid"
	 * <li>name: "testName"
	 * <li>number: new Integer(1)
	 * <li>the_geom: POINT(1, -1)
	 * </ul>
	 * </ul>
	 * </p>
	 * @return
	 * @throws Exception
	 */
	private static SimpleFeature createSimpleFeature()throws Exception{
		final TypeFactory tf = new TypeFactoryImpl();
		final DescriptorFactory df = new DescriptorFactoryImpl();
		final AttributeFactory af = new AttributeFactoryImpl();
		
		List<AttributeType> types = new ArrayList<AttributeType>();
		types.add(tf.createType("oid", String.class));
		types.add(tf.createType("name", String.class));
		types.add(tf.createType("number", Integer.class));
		types.add(tf.createType("the_geom", Point.class));
		
		SimpleFeatureType type = tf.createFeatureType("testType", types,(GeometryType) types.get(3));

		SimpleFeature f = af.create(type, null);
		f.set("oid", "testOid");
		f.set("name", "testName");
		f.set("number", new Integer(1));
		f.set("the_geom", new GeometryFactory().createPoint(new Coordinate(1, -1)));
		return f;
	}
	
	/*
	private static Feature createComplexFeature(){
		
	}*/
	
}
