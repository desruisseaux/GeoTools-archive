package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.feature.Name;
import org.geotools.util.Logging;

public class ComplexDataStoreFactoryTest extends TestCase {

    ComplexDataStoreFactory factory;

    Map params;

    private static final String NSURI = "http://online.socialchange.net.au";

    static final Name mappedTypeName = new Name(NSURI, "RoadSegment");

    protected void setUp() throws Exception {
        super.setUp();
        Logging.GEOTOOLS.forceMonolineConsoleOutput(Level.INFO);
        factory = new ComplexDataStoreFactory();
        params = new HashMap();
        params.put("dbtype", "complex");
        params.put("config", getClass().getResource("test-data/roadsegments.xml"));
        Properties env = System.getProperties();
        // env.save(System.out, "");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        factory = null;
        params = null;
    }

    /**
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStoreFactory.createDataStore(Map)'
     */
    public void testCreateDataStorePreconditions() {
        Map badParams = new HashMap();
        try {
            factory.createDataStore(badParams);
            fail("allowed bad params");
        } catch (IOException e) {
            // OK
        }
        badParams.put("dbtype", "complex");
        try {
            factory.createDataStore(badParams);
            fail("allowed bad params");
        } catch (IOException e) {
            // OK
        }
        badParams.put("config", "file://_inexistentConfigFile123456.xml");
        try {
            factory.createDataStore(badParams);
            fail("allowed bad params");
        } catch (IOException e) {
            // OK
        }
    }

    /*
     * public void test2()throws Exception{ String configFile =
     * "file:/home/gabriel/workspaces/complex_sco/GEOS/conf/data/featureTypes/complexWQ_Plus/wq_plus_mappings.xml";
     * Map params = new HashMap(); params.put("dbtype", "complex");
     * params.put("config", configFile);
     * 
     * DataStore ds = DataStoreFinder.getDataStore(params); assertNotNull(ds);
     * assertTrue(ds instanceof ComplexDataStore);
     * 
     * org.opengis.feature.type.FeatureType ft = ds.getSchema("wq_plus");
     * assertNotNull(ft);
     * 
     * FeatureSource fs = ds.getFeatureSource("wq_plus"); assertNotNull(fs);
     * FeatureIterator fi = fs.getFeatures().features(); while(fi.hasNext()){
     * Feature f = fi.next(); assertNotNull(f); Object result = XPath.get(f,
     * "measurement/result"); assertNotNull(result); } fi.close();
     * 
     * Envelope bounds = fs.getBounds(); assertNotNull(bounds); }
     */

    /**
     * 
     * @throws IOException
     */
    public void testCreateDataStore() throws IOException {
        FeatureAccess ds = (FeatureAccess) factory.createDataStore(params);
        assertNotNull(ds);
        FeatureSource2 mappedSource = (FeatureSource2) ds.access(mappedTypeName);
        assertNotNull(mappedSource);
        assertSame(ds, mappedSource.getDataStore());
    }

    /**
     * 
     * @throws IOException
     */
    public void testFactoryLookup() throws IOException {
        DataAccess ds = DataAccessFinder.createAccess((Object) params);
        assertNotNull(ds);
        assertTrue(ds instanceof ComplexDataStore);

        FeatureSource2 mappedSource = (FeatureSource2) ds.access(mappedTypeName);
        assertNotNull(mappedSource);
    }

    /**
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStoreFactory.createNewDataStore(Map)'
     */
    public void testCreateNewDataStore() throws IOException {
        try {
            factory.createNewDataStore(Collections.EMPTY_MAP);
            fail("unsupported?");
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }

    /**
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStoreFactory.getParametersInfo()'
     */
    public void testGetParametersInfo() {
        DataStoreFactorySpi.Param[] params = factory.getParametersInfo();
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].type);
        assertEquals(URL.class, params[1].type);
    }

    /**
     * 
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStoreFactory.canProcess(Map)'
     */
    public void testCanProcess() {
        Map params = new HashMap();
        assertFalse(factory.canProcess(params));
        params.put("dbtype", "arcsde");
        params.put("config", "http://somesite.net/config.xml");
        assertFalse(factory.canProcess(params));
        params.remove("config");
        params.put("dbtype", "complex");
        assertFalse(factory.canProcess(params));

        params.put("config", "http://somesite.net/config.xml");
        assertTrue(factory.canProcess(params));
    }

    /**
     * 
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStoreFactory.isAvailable()'
     */
    public void testIsAvailable() {
        assertTrue(factory.isAvailable());
    }

    /**
     * 
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStoreFactory.getImplementationHints()'
     */
    public void testGetImplementationHints() {
        assertNotNull(factory.getImplementationHints());
        assertEquals(0, factory.getImplementationHints().size());
    }

}
