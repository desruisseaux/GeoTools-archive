/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
        map.put(IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key,
            IndexedShapefileDataStoreFactory.TREE_NONE);
        assertTrue(factory.canProcess(map));
        map.put(IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key,
            new Byte((byte) 30));
        assertFalse(factory.canProcess(map));
        map.put(IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key,
            new Byte(IndexedShapefileDataStore.TREE_NONE));
        assertTrue(factory.canProcess(map));
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.createDataStore(Map)'
     */
    public void testCreateDataStoreMap() throws Exception {
        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_NONE, true,
            true);
        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_NONE, false,
            true);
        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_NONE, true,
            false);

        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_QIX, true,
            true);
        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_QIX, false,
            true);
        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_QIX, true,
            false);

        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_GRX, true,
            true);
        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_GRX, false,
            true);
        testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_GRX, true,
            false);

        IndexedShapefileDataStore ds1 = testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_GRX,
                true, true);
        IndexedShapefileDataStore ds2 = testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_GRX,
                true, true);

        assertEquals(ds1, ds2);

        ds2 = testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_GRX,
                true, false);
        assertNotSame(ds1, ds2);
        ds2 = testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_GRX,
                false, true);
        assertNotSame(ds1, ds2);
        ds2 = testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_NONE,
                true, true);
        assertNotSame(ds1, ds2);
    }

    private IndexedShapefileDataStore testCreateDataStore(Byte treeType,
        boolean memorymapped, boolean createIndex) throws Exception {
        return testCreateDataStore(false, treeType, memorymapped, createIndex);
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

    private IndexedShapefileDataStore testCreateDataStore(boolean newDS,
        Byte treeType, boolean memorymapped, boolean createIndex)
        throws Exception {
        copyShapefiles(ShapefileDataStoreTest.STATE_POP);
        Map map = new HashMap();
        map.put(IndexedShapefileDataStoreFactory.URLP.key,
            TestData.url(this, ShapefileDataStoreTest.STATE_POP));
        map.put(IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key,
            treeType);
        map.put(IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
            new Boolean(createIndex));
        map.put(IndexedShapefileDataStoreFactory.MEMORY_MAPPED.key,
            new Boolean(memorymapped));

        IndexedShapefileDataStore ds;

        if (newDS) {
            ds = (IndexedShapefileDataStore) factory.createNewDataStore(map);
        } else {
            ds = (IndexedShapefileDataStore) factory.createDataStore(map);
        }

        testDataStore(treeType, memorymapped, createIndex, ds);

        return ds;
    }

    private void testDataStore(Byte treeType, boolean memorymapped,
        boolean createIndex, IndexedShapefileDataStore ds) {
        assertNotNull(ds);
        assertEquals(treeType.byteValue(), ds.treeType);
        assertEquals(treeType.byteValue() != IndexedShapefileDataStore.TREE_NONE,
            ds.useIndex);
        assertEquals(createIndex
            && (treeType.byteValue() != IndexedShapefileDataStore.TREE_NONE),
            ds.createIndex);
        assertEquals(memorymapped, ds.isMemoryMapped());
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.createNewDataStore(Map)'
     */
    public void testCreateNewDataStore() throws Exception {
        IndexedShapefileDataStore ds1 = testCreateDataStore(IndexedShapefileDataStoreFactory.TREE_GRX,
                true, true);
        IndexedShapefileDataStore ds2 = testCreateDataStore(true,
                IndexedShapefileDataStoreFactory.TREE_GRX, true, true);

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
        assertTrue(infos.contains(
                IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE));
        assertTrue(infos.contains(
                IndexedShapefileDataStoreFactory.MEMORY_MAPPED));
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
        testDataStore(IndexedShapefileDataStoreFactory.TREE_QIX, false, true,
            (IndexedShapefileDataStore) ds);
    }

    /*
     * Test method for 'org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory.getTypeName(URL)'
     */
    public void testGetTypeName() throws IOException {
        factory.getTypeName(TestData.url(ShapefileDataStoreTest.STATE_POP));
    }
}
