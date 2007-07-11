/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.shapefile.indexed;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.TestData;
import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;


/**
 * @source $URL$
 */
public class IndexedShapefileDataStoreFactoryTest extends TestCaseSupport {
    private IndexedShapefileDataStoreFactory factory;

    public IndexedShapefileDataStoreFactoryTest() throws IOException {
        super("IndexedShapefileDataStoreFactoryTest");
    }

    protected void setUp() throws Exception {
        factory = new IndexedShapefileDataStoreFactory();
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.canProcess(Map)'
     */
    public void testCanProcessMap() throws Exception {
        Map map = new HashMap();
        map.put(IndexedShapefileDataStoreFactory.URLP.key,
            TestData.url(ShapefileDataStoreTest.STATE_POP));
        assertTrue(factory.canProcess(map));
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.createDataStore(Map)'
     */
    public void testCreateDataStoreMap() throws Exception {
        testCreateDataStore(true);

        ShapefileDataStore ds1 = testCreateDataStore(true,true);
        ShapefileDataStore ds2 = testCreateDataStore(true,true);

        assertSame(ds1, ds2);

        ds2 = testCreateDataStore(true,false);
        assertNotSame(ds1, ds2);
    }

    private ShapefileDataStore testCreateDataStore(boolean createIndex) throws Exception {
        return testCreateDataStore( true, createIndex);
    }

    public void testNamespace() throws Exception {
        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        Map map = new HashMap();
        URI namespace = new URI("http://jesse.com");
        map.put(ShapefileDataStoreFactory.NAMESPACEP.key, namespace);
        map.put(ShapefileDataStoreFactory.URLP.key,
            TestData.url(ShapefileDataStoreTest.STATE_POP));

        DataStore store = factory.createDataStore(map);
        assertEquals(namespace,
            store.getSchema(ShapefileDataStoreTest.STATE_POP.substring(
                    ShapefileDataStoreTest.STATE_POP.indexOf('/')+1,
                    ShapefileDataStoreTest.STATE_POP.lastIndexOf('.')))
                 .getNamespace());
    }

    private ShapefileDataStore testCreateDataStore(boolean newDS,boolean createIndex)
        throws Exception {
        copyShapefiles(ShapefileDataStoreTest.STATE_POP);
        Map map = new HashMap();
        map.put(ShapefileDataStoreFactory.URLP.key,
            TestData.url(this, ShapefileDataStoreTest.STATE_POP));
        map.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
                createIndex ? Boolean.TRUE : Boolean.FALSE );

        ShapefileDataStore ds;

        if (newDS) {
            // This may provided a warning if the file already is created
            ds = (ShapefileDataStore) factory.createNewDataStore(map);
        } else {
            ds = (ShapefileDataStore) factory.createDataStore(map);
        }

        if( ds instanceof IndexedShapefileDataStore){
            IndexedShapefileDataStore indexed = (IndexedShapefileDataStore) ds;
            testDataStore(IndexedShapefileDataStore.TREE_QIX, createIndex, indexed);            
        }
        return ds;
    }

    private void testDataStore(byte treeType, 
        boolean createIndex, IndexedShapefileDataStore ds) {
        assertNotNull(ds);
        assertEquals(treeType, ds.treeType);
        assertEquals(treeType != IndexedShapefileDataStore.TREE_NONE,
            ds.useIndex);
        assertEquals(createIndex && (treeType != IndexedShapefileDataStore.TREE_NONE),
            ds.createIndex);
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.createNewDataStore(Map)'
     */
    public void testCreateNewDataStore() throws Exception {
        ShapefileDataStore ds1 = testCreateDataStore(true, false );
        ShapefileDataStore ds2 = testCreateDataStore(true, true);

        assertNotSame(ds1, ds2);
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.isAvailable()'
     */
    public void testIsAvailable() {
        assertTrue(factory.isAvailable());
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.getParametersInfo()'
     */
    public void testGetParametersInfo() {
        List infos = Arrays.asList(factory.getParametersInfo());
        assertTrue(infos.contains(
                IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX));
        assertTrue(infos.contains(IndexedShapefileDataStoreFactory.URLP));
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.getFileExtensions()'
     */
    public void testGetFileExtensions() {
        List ext = Arrays.asList(factory.getFileExtensions());
        assertTrue(ext.contains(".shp"));
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.canProcess(URL)'
     */
    public void testCanProcessURL() throws FileNotFoundException {
        factory.canProcess(TestData.url(ShapefileDataStoreTest.STATE_POP));
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.createDataStore(URL)'
     */
    public void testCreateDataStoreURL() throws IOException {
        copyShapefiles(ShapefileDataStoreTest.STATE_POP);
        DataStore ds = factory.createDataStore(TestData.url(
                    this, ShapefileDataStoreTest.STATE_POP));
        testDataStore(IndexedShapefileDataStore.TREE_QIX, true,
            (IndexedShapefileDataStore) ds);
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.getTypeName(URL)'
     */
    public void testGetTypeName() throws IOException {
        factory.getTypeName(TestData.url(ShapefileDataStoreTest.STATE_POP));
    }
}
