package org.geotools.data.dir;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class DirectoryDataStoreFactoryTest extends TestCase {
    
    public void testStringListParam() throws IOException {
        DirectoryDataStoreFactory factory = new DirectoryDataStoreFactory();
        Map params = new HashMap();
        params.put("data_directory", getClass().getResource("test-data/test1").getFile());
        params.put("suffix_list", "shp mif");
        factory.createDataStore(params);
    }
} 