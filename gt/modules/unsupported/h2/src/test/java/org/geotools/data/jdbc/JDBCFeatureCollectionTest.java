package org.geotools.data.jdbc;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class JDBCFeatureCollectionTest extends JDBCTestSupport {

    JDBCFeatureCollection collection;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        JDBCFeatureSource source = (JDBCFeatureSource) dataStore.getFeatureSource("ft1"); 
        
        collection = new JDBCFeatureCollection( 
            source, (JDBCState) source.getEntry().getState(Transaction.AUTO_COMMIT)
        );
    }
    
    public void testIterator() throws Exception {
        
        Iterator i = collection.iterator();
        assertNotNull( i );
        
        for ( int x = 0; x < 3; x++ ) {
            assertTrue( i.hasNext() );
            
            SimpleFeature feature = (SimpleFeature) i.next();
            assertNotNull( feature );
            
            assertEquals( "" + x, feature.getID() );
            assertEquals( new Integer(x), feature.getAttribute("intProperty" ) );
        }
        
        assertFalse( i.hasNext() );
        collection.close( i );
    }
    
    public void testBounds() throws IOException {
        ReferencedEnvelope bounds = collection.getBounds();
        assertNotNull( bounds );
        
        assertEquals( 0d, bounds.getMinX(), 0.1 );
        assertEquals( 0d, bounds.getMinY(), 0.1 );
        assertEquals( 2d, bounds.getMaxX(), 0.1 );
        assertEquals( 2d, bounds.getMaxY(), 0.1 );
    }
    
    public void testSize() throws IOException {
        assertEquals( 3, collection.size() );
    }
    
    public void testSubCollection() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        Filter f = ff.equals( ff.property("intProperty"), ff.literal(1) );
        
        FeatureCollection sub = collection.subCollection(f);
        assertNotNull(sub);
        
        assertEquals( 1, sub.size() );
        assertEquals( new ReferencedEnvelope(1,1,1,1,CRS.decode("EPSG:4326")), sub.getBounds());
        
        sub.clear();
        assertEquals( 2, collection.size() );
    }
    
    public void testAdd() throws IOException {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(collection.getSchema());
        b.set( "intProperty", new Integer(3) );
        b.set( "doubleProperty", new Double(3.3) );
        b.set( "stringProperty", "three" );
        b.set( "geometry", new GeometryFactory().createPoint( new Coordinate( 3, 3) ) );
        
        SimpleFeature feature = b.buildFeature( null );
        assertEquals( 3, collection.size());
        
        collection.add( feature );
        assertEquals( 4, collection.size() );
        
        Iterator i = collection.iterator();
        boolean found = false;
        while( i.hasNext() ) {
            SimpleFeature f = (SimpleFeature) i.next();
            if ( new Integer(3).equals( f.getAttribute("intProperty") ) ) {
                assertEquals( feature.getAttribute("doubleProperty"), f.getAttribute("doubleProperty") );
                assertEquals( feature.getAttribute("stringProperty"), f.getAttribute("stringProperty") );
                assertTrue( ((Geometry)feature.getAttribute("geometry")).equals((Geometry)f.getAttribute("geometry")) );
                found = true;
            }
        }
        assertTrue( found );
        
        collection.close( i );
    }
    
    public void testClear() throws IOException {
        collection.clear();
        
        Iterator i = collection.iterator();
        assertFalse( i.hasNext() );
        
        collection.close( i );
    }
   
}
