package org.geotools.data;

import org.geotools.data.memory.MemoryDataStore;

public class FeatureSourceMetadataEnityTest extends DataTestCase {
    MemoryDataStore store;
    
    public FeatureSourceMetadataEnityTest(String test) {
        super(test);
    }
    protected void setUp() throws Exception {
        super.setUp();
        store = new MemoryDataStore();
        store.addFeatures( roadFeatures );
        store.addFeatures( riverFeatures );
        store.addFeatures( lakeFeatures );                               
    }
//    public void testRoadMeta(){
//        FeatureSourceMetadataEnity roadMeta =
//            new FeatureSourceMetadataEnity( store, "road" );
//        
//    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
