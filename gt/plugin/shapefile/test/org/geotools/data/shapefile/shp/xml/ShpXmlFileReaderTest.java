package org.geotools.data.shapefile.shp.xml;

import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.TypeEntry;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.resources.TestData;

import com.vividsolutions.jts.geom.Envelope;

import junit.framework.TestCase;

public class ShpXmlFileReaderTest extends TestCase {
     ShpXmlFileReader reader;
     
     protected void setUp() throws Exception {
        super.setUp();
        URL example = TestData.getResource( this, "example.shp.xml" );
        reader = new ShpXmlFileReader( example );
    }
    public void testBBox() {
         Metadata meta = reader.parse();
         assertNotNull( "meta", meta );
         IdInfo idInfo = meta.getIdinfo();
         assertNotNull( "idInfo", idInfo );
         Envelope bounding = idInfo.getBounding();
         assertNotNull( bounding );
         assertEquals( -180.0, bounding.getMinX(), 0.00001 );
         assertEquals( 180.0, bounding.getMaxX(), 0.00001 );
         assertEquals( -90.0, bounding.getMinY(), 0.00001 );
         assertEquals( 90.0, bounding.getMaxY(), 0.00001 );
    }
    public void testOptimization() throws Exception {
        Envelope expected = reader.parse().getIdinfo().getLbounding();
        assertNotNull( "lbounding", expected );
        
        URL example = TestData.getResource( this, "example.shp" );
        ShapefileDataStore shape = new ShapefileDataStore( example );
        
        TypeEntry entry = (TypeEntry) shape.entries().get(0);
        Envelope actual;
        
        // optimization # 1
        actual = entry.getBounds();
        assertNotNull( "entry works", actual );
        assertTrue( "expected contains actual", expected.contains( actual ));
        assertEquals( "entry optimized", expected, actual );
        
        // optimization # 2
        FeatureSource source = entry.getFeatureSource();
        actual = source.getBounds();
        //assertNotNull( "source works", actual );
        //assertTrue( "expected contains actual", expected.contains( actual ));
        //assertEquals( "featuresource optimized", expected, actual );
        int i=source.getCount(Query.ALL);
        // accurate
        actual = source.getFeatures().getBounds();
        assertNotNull( "contents calculates", actual );        
        assertTrue( "expected contains data", expected.contains( actual ));
    }
     
}
