package org.geotools.data;

import org.geotools.data.memory.MemoryDataStore;

public class FeatureTypeMetadataEnityTest extends DataTestCase {
    MemoryDataStore store;
    
    public FeatureTypeMetadataEnityTest(String test) {
        super(test);
    }
    protected void setUp() throws Exception {
        super.setUp();
        store = new MemoryDataStore();
        store.addFeatures( roadFeatures );
        store.addFeatures( riverFeatures );
        store.addFeatures( lakeFeatures );                               
    }
    public void testRoadMeta(){
        FeatureTypeMetadataEntity roadMeta =
            new FeatureTypeMetadataEntity( store, null, "road" );
        
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
