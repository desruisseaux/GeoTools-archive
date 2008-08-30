package org.geotools.jdbc;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public abstract class JDBCPrimaryKeyTest extends JDBCTestSupport {

    @Override
    protected abstract JDBCPrimaryKeyTestSetup createTestSetup();
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        dataStore.setDatabaseSchema(null);
    }
    
    public void testAutoGeneratedPrimaryKey() throws Exception {
        FeatureSource fs = dataStore.getFeatureSource(tname("auto"));
        FeatureCollection features = fs.getFeatures();
        assertPrimaryKeyValues(features, 3);
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder( (SimpleFeatureType) fs.getSchema() );
        b.add("four");
        b.add(new GeometryFactory().createPoint( new Coordinate(4,4) ));
        features.add( b.buildFeature(null) );
        
        assertPrimaryKeyValues(features,4);
    }

    public void testNonIncrementingPrimaryKey() throws Exception {
        FeatureSource fs = dataStore.getFeatureSource(tname("noninc"));
        FeatureCollection features = fs.getFeatures();
        assertPrimaryKeyValues(features, 3);
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder( (SimpleFeatureType) fs.getSchema() );
        b.add("four");
        b.add( new GeometryFactory().createPoint( new Coordinate(4,4) ) );
        features.add( b.buildFeature(null) );
        
        assertPrimaryKeyValues(features,4);
    }
    
    void assertPrimaryKeyValues( FeatureCollection features, int count ) throws Exception {
        FeatureIterator i = features.features();
       
        for ( int j = 1; j <= count; j++ ) {
            assertTrue( i.hasNext() );
            SimpleFeature f = (SimpleFeature) i.next();
            
            assertEquals( tname(features.getSchema().getName().getLocalPart()) + "." + j , f.getID() );
        }
        
        features.close( i );
        
    }
    
    public void testMultiColumnPrimaryKey() throws Exception {
        FeatureSource fs = dataStore.getFeatureSource(tname("multi"));
        FeatureCollection features = fs.getFeatures();
        FeatureIterator i = features.features();
        
        String[] xyz = new String[]{"x","y","z"};
        for ( int j = 1; j <= 3; j++ ) {
            assertTrue( i.hasNext() );
            SimpleFeature f = (SimpleFeature) i.next();
            
            assertEquals( tname("multi") + "." + j + "." + xyz[j-1], f.getID() );
        }
        features.close( i );
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder( (SimpleFeatureType) fs.getSchema() );
        b.add("four");
        b.add(new GeometryFactory().createPoint(new Coordinate(4,4)));
        features.add( b.buildFeature(null) );
        
        i = features.features();
        for ( int j = 0; j < 3; j++ ) {
            i.hasNext();
            i.next();
        }
        
        assertTrue( i.hasNext() );
        SimpleFeature f = (SimpleFeature) i.next();
        assertTrue( f.getID().startsWith( tname("multi") + ".4.") );
        
        features.close( i );
    }
}
