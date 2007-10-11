package org.geotools.data.jdbc;

import java.util.Iterator;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;

public abstract class JDBCFeatureSourceTest extends JDBCTestSupport {

    ContentFeatureSource featureSource;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        featureSource = (JDBCFeatureStore) dataStore.getFeatureSource("ft1");
    }
    
    public void testSchema() throws Exception {
        SimpleFeatureType schema = featureSource.getSchema();
        assertEquals( "ft1", schema.getTypeName() );
        assertEquals( dataStore.getNamespaceURI(), schema.getName().getNamespaceURI() );
        assertEquals( CRS.decode("EPSG:4326"), schema.getCRS());
        
        assertEquals( 4, schema.getAttributeCount() );
        assertNotNull( schema.getAttribute("geometry"));
        assertNotNull( schema.getAttribute("intProperty"));
        assertNotNull( schema.getAttribute("stringProperty"));
        assertNotNull( schema.getAttribute("doubleProperty"));
    }
    
    public void testBounds() throws Exception {
        ReferencedEnvelope bounds = featureSource.getBounds();
        assertEquals( 0d, bounds.getMinX() );
        assertEquals( 0d, bounds.getMinY() );
        assertEquals( 2d, bounds.getMaxX() );
        assertEquals( 2d, bounds.getMaxY() );
        
        assertEquals( CRS.decode("EPSG:4326"), bounds.getCoordinateReferenceSystem() );
    }
    
    public void testBoundsWithQuery() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = 
            ff.equals( ff.property("stringProperty"), ff.literal("one") );
        
        DefaultQuery query = new DefaultQuery();
        query.setFilter( filter );
        
        ReferencedEnvelope bounds = featureSource.getBounds( query );
        assertEquals( 1d, bounds.getMinX() );
        assertEquals( 1d, bounds.getMinY() );
        assertEquals( 1d, bounds.getMaxX() );
        assertEquals( 1d, bounds.getMaxY() );
        
        assertEquals( CRS.decode("EPSG:4326"), bounds.getCoordinateReferenceSystem() );
        
    }
    
    public void testCount() throws Exception {
        assertEquals( 3, featureSource.getCount(Query.ALL) );
    }
    
    public void testCountWithFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = 
            ff.equals( ff.property("stringProperty"), ff.literal("one") );
        
        DefaultQuery query = new DefaultQuery();
        query.setFilter( filter );
        assertEquals( 1, featureSource.getCount(query) );
    }

    public void testGetFeatures() throws Exception {
        FeatureCollection features = featureSource.getFeatures();
        assertEquals( 3, features.size() );
    }
    
    public void testGetFeaturesWithFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = 
            ff.equals( ff.property("stringProperty"), ff.literal("one") );
        
        FeatureCollection features = featureSource.getFeatures( filter );
        assertEquals( 1, features.size() );
        
        Iterator iterator = features.iterator();
        assertTrue( iterator.hasNext() );
        
        SimpleFeature feature = (SimpleFeature) iterator.next();
        assertEquals( new Integer(1), feature.getAttribute("intProperty"));
        assertEquals( "one", feature.getAttribute("stringProperty"));
        
    }
    
    public void testGetFeaturesWithQuery() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = 
            ff.equals( ff.property("stringProperty"), ff.literal("one") );
        
        DefaultQuery query = new DefaultQuery();
        query.setPropertyNames( new String[]{ "doubleProperty", "intProperty" } );
        query.setFilter( filter );
        
        FeatureCollection features = featureSource.getFeatures( query );
        assertEquals( 1, features.size() );
        
        Iterator iterator = features.iterator();
        assertTrue( iterator.hasNext() );
        
        SimpleFeature feature = (SimpleFeature) iterator.next();
        assertEquals( 2, feature.getAttributeCount() );
        
        assertEquals( new Double( 1.1 ), feature.getAttribute(0) );
        assertEquals( new Integer( 1 ), feature.getAttribute(1) );
    }
    
    
}
