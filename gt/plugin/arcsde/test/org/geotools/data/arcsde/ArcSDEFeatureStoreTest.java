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

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Unit tests for transaction support
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class ArcSDEFeatureStoreTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = Logger.getLogger(ArcSDEFeatureStoreTest.class.getPackage()
                                                                                .getName());

    /** DOCUMENT ME! */
    private TestData testData;

    /** an ArcSDEDataStore created on setUp() to run tests against */
    private DataStore store;

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
        this.store = testData.getDataStore();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        testData = null;
        super.tearDown();
    }

    /**
     * Creates an ArcSDE table, "EXAMPLE", and adds a spatial column, "SHAPE",
     * to it.
     * 
     * <p>
     * This code is directly taken from the createBaseTable mehtod of the
     * arcsdeonline "Working with layers" example, to verify that it works
     * prior to blame the gt implementation.
     * </p>
     *
     * @throws SeException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    public void __testCreateBaseTable()
        throws SeException, IOException, UnavailableConnectionException {
        ArcSDEConnectionPool connPool = ((ArcSDEDataStore) testData
            .getDataStore()).getConnectionPool();
        SeConnection conn = connPool.getConnection();

        SeLayer layer = new SeLayer(conn);
        SeTable table = null;

        try {
            /*
             *   Create a qualified table name with current user's name and
             *   the name of the table to be created, "EXAMPLE".
             */
            String tableName = (conn.getUser() + ".EXAMPLE");
            table = new SeTable(conn, tableName);
            layer.setTableName("EXAMPLE");
            try{
            	table.delete();
            }catch(Exception e){
            	LOGGER.warning(e.getMessage());
            }

            SeColumnDefinition[] colDefs = new SeColumnDefinition[7];

            /*
             *   Define the columns and their attributes for the table to be created.
             *   NOTE: The valid range/values of size and scale parameters vary from
             *   one database to another.
             */
            boolean isNullable = true;
            colDefs[0] = new SeColumnDefinition("INT32_COL",
                    SeColumnDefinition.TYPE_INTEGER, 10, 0, isNullable);
            colDefs[1] = new SeColumnDefinition("INT16_COL",
                    SeColumnDefinition.TYPE_SMALLINT, 4, 0, isNullable);
            colDefs[2] = new SeColumnDefinition("FLOAT32_COL",
                    SeColumnDefinition.TYPE_FLOAT, 5, 2, isNullable);
            colDefs[3] = new SeColumnDefinition("FLOAT64_COL",
                    SeColumnDefinition.TYPE_DOUBLE, 15, 4, isNullable);
            colDefs[4] = new SeColumnDefinition("STRING_COL",
                    SeColumnDefinition.TYPE_STRING, 25, 0, isNullable);
            colDefs[5] = new SeColumnDefinition("DATE_COL",
                    SeColumnDefinition.TYPE_DATE, 1, 0, isNullable);
            colDefs[6] = new SeColumnDefinition("INT64_COL",
                    SeColumnDefinition.TYPE_INTEGER, 10, 0, isNullable);

            /*
             *   Create the table using the DBMS default configuration keyword.
             *   Valid keywords are defined in the dbtune table.
             */
            System.out.println(
                "\n--> Creating a table using DBMS Default Keyword");
            table.create(colDefs, "DEFAULTS");
            System.out.println(" - Done.");

            /*
             *   Define the attributes of the spatial column
             */
            layer.setSpatialColumnName("SHAPE");

            /*
             *   Set the type of shapes that can be inserted into the layer. Shape type can be just one
             *   or many.
             *   NOTE: Layers that contain more than one shape type can only be accessed through
             *   the C and Java APIs and Arc Explorer Java 3.x. They cannot be seen from ArcGIS
             *   desktop applications.
             */
            layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK
                | SeLayer.SE_POINT_TYPE_MASK | SeLayer.SE_LINE_TYPE_MASK
                | SeLayer.SE_SIMPLE_LINE_TYPE_MASK | SeLayer.SE_AREA_TYPE_MASK
                | SeLayer.SE_MULTIPART_TYPE_MASK);
            layer.setGridSizes(1100.0, 0.0, 0.0);
            layer.setDescription("Layer Example");

            SeExtent ext = new SeExtent(0.0, 0.0, 10000.0, 10000.0);
            layer.setExtent(ext);

            /*
             *   Define the layer's Coordinate Reference
             */
            SeCoordinateReference coordref = new SeCoordinateReference();
            coordref.setXY(0, 0, 100);
            layer.setCoordRef(coordref);

            /*
             *   Spatially enable the new table...
             */
            System.out.println("\n--> Adding spatial column \"SHAPE\"...");
            layer.create(3, 4);
            System.out.println(" - Done.");
        } catch (SeException e) {
            System.out.println(e.getSeError().getErrDesc());
            e.printStackTrace();
            throw e;
        } finally {
            connPool.release(conn);
        }
    } // End method createBaseTable

    /**
     * Creates an ArcSDE table, "EXAMPLE", and adds a spatial column, "SHAPE",
     * to it.
     * 
     * <p>
     * This code is directly taken from the createBaseTable mehtod of the
     * arcsdeonline "Working with layers" example, to verify that it works
     * prior to blame the gt implementation.
     * </p>
     *
     * @throws SeException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    public void _testCreateNonStandardSchema()
        throws SeException, IOException, UnavailableConnectionException {
        ArcSDEConnectionPool connPool = ((ArcSDEDataStore) testData
            .getDataStore()).getConnectionPool();
        SeConnection conn = connPool.getConnection();

        SeLayer layer = new SeLayer(conn);
        SeTable table = null;

        try {
            /*
             *   Create a qualified table name with current user's name and
             *   the name of the table to be created, "EXAMPLE".
             */
            String tableName = (conn.getUser() + ".NOTENDSWITHGEOM");
            table = new SeTable(conn, tableName);
            layer.setTableName("NOTENDSWITHGEOM");
            try{
            	table.delete();
            	layer.delete();
            }catch(Exception e){
            	LOGGER.warning(e.getMessage());
            }

            /*
             *   Create the table using the DBMS default configuration keyword.
             *   Valid keywords are defined in the dbtune table.
             */
            System.out.println(
                "\n--> Creating a table using DBMS Default Keyword");
            SeColumnDefinition []tmpCols = new SeColumnDefinition[]{
            		new SeColumnDefinition("tmp", SeColumnDefinition.TYPE_STRING, 5, 0, true)
            		};
            table.create(tmpCols, "DEFAULTS");
            System.out.println(" - Done.");
            
            SeColumnDefinition[] colDefs = new SeColumnDefinition[7];

            /*
             *   Define the columns and their attributes for the table to be created.
             *   NOTE: The valid range/values of size and scale parameters vary from
             *   one database to another.
             */
            boolean isNullable = true;
            colDefs[0] = new SeColumnDefinition("INT32_COL",
                    SeColumnDefinition.TYPE_INTEGER, 10, 0, isNullable);
            colDefs[1] = new SeColumnDefinition("INT16_COL",
                    SeColumnDefinition.TYPE_SMALLINT, 4, 0, isNullable);
            colDefs[2] = new SeColumnDefinition("FLOAT32_COL",
                    SeColumnDefinition.TYPE_FLOAT, 5, 2, isNullable);
            colDefs[3] = new SeColumnDefinition("FLOAT64_COL",
                    SeColumnDefinition.TYPE_DOUBLE, 15, 4, isNullable);
            colDefs[4] = new SeColumnDefinition("STRING_COL",
                    SeColumnDefinition.TYPE_STRING, 25, 0, isNullable);
            colDefs[5] = new SeColumnDefinition("DATE_COL",
                    SeColumnDefinition.TYPE_DATE, 1, 0, isNullable);
            colDefs[6] = new SeColumnDefinition("INT64_COL",
                    SeColumnDefinition.TYPE_INTEGER, 10, 0, isNullable);

            table.addColumn(colDefs[0]);
            table.addColumn(colDefs[1]);
            table.addColumn(colDefs[2]);
            table.addColumn(colDefs[3]);
            table.dropColumn(tmpCols[0].getName());

            /*
             *   Define the attributes of the spatial column
             */
            layer.setSpatialColumnName("SHAPE");

            /*
             *   Set the type of shapes that can be inserted into the layer. Shape type can be just one
             *   or many.
             *   NOTE: Layers that contain more than one shape type can only be accessed through
             *   the C and Java APIs and Arc Explorer Java 3.x. They cannot be seen from ArcGIS
             *   desktop applications.
             */
            layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK
                | SeLayer.SE_POINT_TYPE_MASK | SeLayer.SE_LINE_TYPE_MASK
                | SeLayer.SE_SIMPLE_LINE_TYPE_MASK | SeLayer.SE_AREA_TYPE_MASK
                | SeLayer.SE_MULTIPART_TYPE_MASK);
            layer.setGridSizes(1100.0, 0.0, 0.0);
            layer.setDescription("Layer Example");

            SeExtent ext = new SeExtent(0.0, 0.0, 10000.0, 10000.0);
            layer.setExtent(ext);

            /*
             *   Define the layer's Coordinate Reference
             */
            SeCoordinateReference coordref = new SeCoordinateReference();
            coordref.setXY(0, 0, 100);
            layer.setCoordRef(coordref);

            /*
             *   Spatially enable the new table...
             */
            System.out.println("\n--> Adding spatial column \"SHAPE\"...");
            layer.create(3, 4);
            System.out.println(" - Done.");

        
            table.addColumn(colDefs[4]);
            table.addColumn(colDefs[5]);
            table.addColumn(colDefs[6]);
        
        } catch (SeException e) {
            System.out.println(e.getSeError().getErrDesc());
            e.printStackTrace();
            throw e;
        } finally {
            try{
            	table.delete();
            }catch(Exception e){}
            try{
            	layer.delete();
            }catch(Exception e){
            }
            connPool.release(conn);
        }
    } // End method createBaseTable
    /**
     * Tests the creation of new feature types, wich CRS and all.
     *
     * @throws IOException DOCUMENT ME!
     * @throws SchemaException DOCUMENT ME!
     */
    public void testCreateSchema() throws IOException, SchemaException {
        FeatureType type;
        AttributeType[] atts = new AttributeType[4];
        String typeName = "GT_TEST_TYPE";

        atts[0] = AttributeTypeFactory.newAttributeType("FST_COL", String.class,
                false);
        atts[1] = AttributeTypeFactory.newAttributeType("SECOND_COL", Double.class,
                false);
        atts[2] = AttributeTypeFactory.newAttributeType("GEOM", Point.class,
                false);
        atts[3] = AttributeTypeFactory.newAttributeType("FOURTH_COL", Integer.class,
                false);
        type = FeatureTypeFactory.newFeatureType(atts, typeName);

        DataStore ds = testData.getDataStore();
        
        silentlyDeleteSdeLayer(typeName, ((ArcSDEDataStore)ds).getConnectionPool());
        ds.createSchema(type);
        silentlyDeleteSdeLayer(typeName, ((ArcSDEDataStore)ds).getConnectionPool());
        
    }
    
    private void silentlyDeleteSdeLayer(String typeName, ArcSDEConnectionPool pool){
    		SeConnection conn = null;
    		try {
				conn = pool.getConnection();
				SeTable table = new SeTable(conn, typeName);
				table.delete();
			} catch (Exception e) {
				LOGGER.warning(e.getMessage());
			}finally{
				pool.release(conn);
			}
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
