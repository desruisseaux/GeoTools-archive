package org.geotools.data.wfs;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureTypeBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class WFSFeatureTypeTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test that lenient WFSFeature is lenient and somewhat intelligent
     * @throws Exception 
     */
    public void testCreateObjectArrayString() throws Exception {
        FeatureTypeBuilder builder=FeatureTypeBuilder.newInstance("type");
        builder.addType(AttributeTypeFactory.newAttributeType("geom", Geometry.class, false));
        builder.addType(AttributeTypeFactory.newAttributeType("id", Integer.class, false));
        builder.addType(AttributeTypeFactory.newAttributeType("name", String.class, false));
        String name2 = "name2";
        builder.addType(AttributeTypeFactory.newAttributeType(name2, String.class, false));
        builder.addType(AttributeTypeFactory.newAttributeType("double", Double.class, false));
        WFSFeatureType ft = new WFSFeatureType(builder.getFeatureType(),null);
        ft.setLenient(true);
        GeometryFactory fac=new GeometryFactory();
        
        Point point = fac.createPoint(new Coordinate(10,10));
        Integer id = new Integer(2);
        String name = "name1";
        Double double1 = new Double(3.0);
        // basic case
        Object[] atts=new Object[]{
                point,
                id,
                name,
                double1,
                name2
        };
        
        Feature feature = ft.create(atts);
        
        assertNotNull(feature.getID());
        assertEquals(point, feature.getAttribute(0));
        assertEquals(id, feature.getAttribute(1));
        assertEquals(name, feature.getAttribute(2));
        assertEquals(name2, feature.getAttribute(3));
        assertEquals(double1, feature.getAttribute(4));
        
        atts[1]=null;
        String fid="fid";
        feature = ft.create(atts, fid);
        
        assertEquals(fid, feature.getID());
        assertEquals(point, feature.getAttribute(0));
        assertNull(feature.getAttribute(1));
        assertEquals(name, feature.getAttribute(2));
        assertEquals(name2, feature.getAttribute(3));
        assertEquals(double1, feature.getAttribute(4));
        
        atts=new Object[]{
                point,
                name,
                name2,
                double1
        };
        feature = ft.create(atts, fid);
        
        assertEquals(fid, feature.getID());
        assertEquals(point, feature.getAttribute(0));
        assertNull(feature.getAttribute(1));
        assertEquals(name, feature.getAttribute(2));
        assertEquals(name2, feature.getAttribute(3));
        assertEquals(double1, feature.getAttribute(4));
        
        atts=new Object[]{
                point,
                id,
                name2,
                double1
        };
        feature = ft.create(atts, fid);
        
        assertEquals(fid, feature.getID());
        assertEquals(point, feature.getAttribute(0));
        assertEquals(id, feature.getAttribute(1));
        assertEquals(name2, feature.getAttribute(2));
        assertNull(feature.getAttribute(3));
        assertEquals(double1, feature.getAttribute(4));        
        atts=new Object[]{
                point,
                id,
                null,
                name2,
                double1
        };
        
        feature = ft.create(atts, fid);
        
        assertEquals(fid, feature.getID());
        assertEquals(point, feature.getAttribute(0));
        assertEquals(id, feature.getAttribute(1));
        assertNull(feature.getAttribute(2));
        assertEquals(name2, feature.getAttribute(3));
        assertEquals(double1, feature.getAttribute(4));
    }

}

