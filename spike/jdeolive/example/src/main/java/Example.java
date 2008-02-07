import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


public class Example {

    DataStore dataStore;
    
    /**
     * Simple features, feature reader, casting.
     */
    public void simpleReader1() throws IOException {
        FeatureReader reader = dataStore.getFeatureReader(Query.ALL, Transaction.AUTO_COMMIT );
    
        while( reader.hasNext() ) {
            SimpleFeature feature = (SimpleFeature) reader.next();
            feature.getAttribute(0);
        }
    }
  
    /**
     * Simple features, feature reader, parameter declaration
     */
    public void simpleReader2() throws IOException {
        FeatureReader<SimpleFeatureType,SimpleFeature> reader = 
            dataStore.getFeatureReader(Query.ALL, Transaction.AUTO_COMMIT );
    
        while( reader.hasNext() ) {
            SimpleFeature feature = reader.next();
            feature.getAttribute( 0 );
        }
    }
    
    
    /**
     * Complex features, feature reader
     */
    public void complexReader() throws IOException {
        FeatureReader reader = dataStore.getFeatureReader(Query.ALL, Transaction.AUTO_COMMIT );
        
        while( reader.hasNext() ) {
            Feature feature = reader.next();
            feature.getProperty( "foo" );
        }
    }
    
    /**
     * Simple features, feature collection, casting.
     */
    public void simpleCollection1() throws IOException {
        FeatureSource featureSource = dataStore.getFeatureSource("foo");
        FeatureCollection collection = featureSource.getFeatures();
        FeatureIterator features = collection.features();
        
        while ( features.hasNext() ) {
            SimpleFeature feature = (SimpleFeature) features.next();
            feature.getAttribute(0);
        }
    }
    
    /**
     * Simple features, feature collection, parameter declaration.
     */
    public void simpleCollection2() throws IOException {
        FeatureSource<SimpleFeatureType,SimpleFeature> featureSource = dataStore.getFeatureSource("foo");
        FeatureCollection<SimpleFeatureType,SimpleFeature> collection = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> features = collection.features();
        
        while ( features.hasNext() ) {
            SimpleFeature feature = features.next();
            feature.getAttribute(0);
        }
    }
    
    /**
     * Simple features, feature collection, parameter declaration with warning.
     */
    public void simpleCollectionWithWarnings() throws IOException {
        FeatureSource featureSource = dataStore.getFeatureSource("foo");
        FeatureCollection collection = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> features = collection.features();
    
        while( features.hasNext() ) {
            SimpleFeature feature = features.next();
            feature.getProperty("zero");
        }
    }
    
    /**
     * Complex features, feature collection.
     */
    public void complexCollection() throws IOException {
        FeatureSource featureSource = dataStore.getFeatureSource("foo");
        FeatureCollection collection = featureSource.getFeatures();
        FeatureIterator features = collection.features();
        
        while( features.hasNext() ) {
            Feature feature = features.next();
            feature.getProperty( "foo" );
        }
    }
}
