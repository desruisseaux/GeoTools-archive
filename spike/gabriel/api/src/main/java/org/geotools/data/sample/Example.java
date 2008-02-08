package org.geotools.data.sample;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.geotools.data.DataRepository;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Reader;
import org.geotools.data.SimpleFeatureCollection;
import org.geotools.data.Source;
import org.geotools.data.Transaction;
import org.geotools.data.Writer;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

public class Example {

    public void sampleDataStoreUsage() throws IOException {
        // finds only Shapefile, PostGIS, etc. (i.e. simple feature capable data
        // stores)
        Iterator dataStores = DataStoreFinder.getAllDataStores();
        
        DataStore ds = DataStoreFinder.getDataStore(new HashMap());
        
        FeatureReader featureReader = ds.getFeatureReader(Query.ALL, Transaction.AUTO_COMMIT);
        SimpleFeature next = featureReader.next();
        
        FeatureSource featureSource = ds.getFeatureSource("typeName");
        SimpleFeatureType schema = featureSource.getSchema();
        SimpleFeatureCollection features = featureSource.getFeatures();        

        FeatureWriter featureWriter = ds.getFeatureWriter("typeName", Transaction.AUTO_COMMIT);
        SimpleFeature next2 = featureWriter.next();
        SimpleFeatureType featureType = featureWriter.getFeatureType();
    }

    public void sampleDataRepositoryUsage() throws IOException {
        // finds ShapefileDataStore, PostGisDataStore, ComplexDataStore,
        // WFSDataStore, etc
        Iterator repositories = DataStoreFinder.getAvailableRepositories();
        
        DataRepository<FeatureType, Feature> ds = DataStoreFinder.getDataRepository(new HashMap());
        
        Reader<FeatureType, Feature> featureReader = ds.getFeatureReader(Query.ALL, Transaction.AUTO_COMMIT);
        Feature next = featureReader.next();
        
        Source<FeatureType, Feature> featureSource = ds.getFeatureSource("typeName");
        FeatureType schema = featureSource.getSchema();
        FeatureCollection<FeatureType, Feature> features = featureSource.getFeatures();        

        Writer<FeatureType, Feature> featureWriter = ds.getFeatureWriter("typeName", Transaction.AUTO_COMMIT);
        Feature next2 = featureWriter.next();
        FeatureType featureType = featureWriter.getFeatureType();
    }
}
