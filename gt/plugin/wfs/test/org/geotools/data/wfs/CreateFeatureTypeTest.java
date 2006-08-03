/**
 * 
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
