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
 *
 */
package org.geotools.data.arcsde;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Point;
import junit.framework.TestCase;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Unit tests for transaction support
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class ArcSDEFeatureStoreTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = Logger.getLogger(ArcSDEFeatureStoreTest.class.getPackage()
                                                                                .getName());

    /** DOCUMENT ME! */
    private TestData testData;

    /**
     * loads /testData/testparams.properties into a Properties object, wich is
     * used to obtain test tables names and is used as parameter to find the
     * DataStore
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.testData = new TestData();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        testData.deleteTempTable();
        testData = null;
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testDeleteByFID() throws Exception {
        testData.createTemptTable();

        DataStore ds = testData.getDataStore();
        String typeName = testData.getTemp_table();

        //get a fid
        FeatureReader reader = ds.getFeatureReader(new DefaultQuery(typeName),
                Transaction.AUTO_COMMIT);
        String fid = reader.next().getID();
        reader.close();
        
        FilterFactory ff = FilterFactory.createFilterFactory();
        Filter fidFilter = ff.createFidFilter(fid);

        FeatureWriter writer = ds.getFeatureWriter(typeName, fidFilter,
                Transaction.AUTO_COMMIT);

        assertTrue(writer.hasNext());

        Feature feature = writer.next();
        assertEquals(fid, feature.getID());
        writer.remove();
        assertFalse(writer.hasNext());
        writer.close();

        //was it really removed?
        reader = ds.getFeatureReader(new DefaultQuery(typeName, fidFilter),
                Transaction.AUTO_COMMIT);
        assertFalse(reader.hasNext());
        reader.close();
    }

    /**
     * Tests the creation of new feature types, wich CRS and all.
     *
     * @throws IOException DOCUMENT ME!
     * @throws SchemaException DOCUMENT ME!
     */
    public void testCreateSchema() throws IOException, SchemaException {
        FeatureType type;
        AttributeType[] atts = new AttributeType[4];
        String typeName = testData.getTemp_table();

        atts[0] = AttributeTypeFactory.newAttributeType("FST_COL",
                String.class, false);
        atts[1] = AttributeTypeFactory.newAttributeType("SECOND_COL",
                Double.class, false);
        atts[2] = AttributeTypeFactory.newAttributeType("GEOM", Point.class,
                false);
        atts[3] = AttributeTypeFactory.newAttributeType("FOURTH_COL",
                Integer.class, false);
        type = FeatureTypeFactory.newFeatureType(atts, typeName);

        DataStore ds = testData.getDataStore();

        testData.deleteTempTable(((ArcSDEDataStore) ds).getConnectionPool());
        ds.createSchema(type);
        testData.deleteTempTable(((ArcSDEDataStore) ds).getConnectionPool());
    }

    /**
     * Tests the writing of features with autocommit transaction
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void testFeatureWriterAutoCommit() {
        throw new UnsupportedOperationException("Don't forget to implement");
    }

    /**
     * Tests the writing of features with real transactions
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void testFeatureWriterTransaction() {
        throw new UnsupportedOperationException("Don't forget to implement");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void testFeatureWriterAppend() {
        throw new UnsupportedOperationException("Don't forget to implement");
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArcSDEFeatureStoreTest.class);
    }
}
