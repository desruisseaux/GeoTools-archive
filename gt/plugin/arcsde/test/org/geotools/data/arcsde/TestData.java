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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Logger;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Provides access to the ArcSDEDataStore test data configuration.
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class TestData {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(TestData.class.getPackage()
                                                                        .getName());

    /** DOCUMENT ME! */
    static final String COORD_SYS = "GEOGCS[\"WGS 84\","
        + "DATUM[\"WGS_1984\","
        + "  SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]],"
        + "  AUTHORITY[\"EPSG\",\"6326\"]],"
        + "PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],"
        + "UNIT[\"degree\", 0.017453292519943295]," + "AXIS[\"Lon\", EAST],"
        + "AXIS[\"Lat\", NORTH]," + "AUTHORITY[\"EPSG\",\"4326\"]]";

    /** the set of test parameters loaded from {@code test-data/testparams.properties} */
    private Properties conProps = null;

    /** the name of the table holding the point test features */
    private String point_table;

    /** the name of the table holding the linestring test features */
    private String line_table;

    /** the name of the table holding the polygon test features */
    private String polygon_table;

    /**
     * the name of a table that can be manipulated without risk of loosing
     * important data
     */
    private String temp_table;

    /** DOCUMENT ME! */
    private ArcSDEDataStore dataStore = null;

    /**
     * Creates a new TestData object.
     *
     * @throws IOException DOCUMENT ME!
     */
    public TestData() throws IOException {
//    	intentionally blank
    }

    /**
     * Must be called from inside the test's setUp() method.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void setUp() throws IOException {
        this.conProps = new Properties();

        String propsFile = "testparams.properties";
        InputStream in = org.geotools.resources.TestData.openStream(this, propsFile);
        // The line above should never returns null. It should thow a
        // FileNotFoundException instead if the resource is not available.

        this.conProps.load(in);
        in.close();

        this.point_table = this.conProps.getProperty("point_table");
        this.line_table = this.conProps.getProperty("line_table");
        this.polygon_table = this.conProps.getProperty("polygon_table");
        this.temp_table = this.conProps.getProperty("temp_table");

        if (this.point_table == null) {
            throw new IOException("point_table not defined in " + propsFile);
        }

        if (this.line_table == null) {
            throw new IOException("line_table not defined in " + propsFile);
        }

        if (this.polygon_table == null) {
            throw new IOException("polygon_table not defined in " + propsFile);
        }

        if (this.temp_table == null) {
            throw new IOException("temp_table not defined in " + propsFile);
        }
    }

    /**
     * Must be called from inside the test's tearDown() method.
     */
    public void tearDown(boolean cleanTestTable, boolean cleanPool) {
    	if(cleanTestTable){
    		deleteTempTable();
    	}
        if(cleanPool){
	        ConnectionPoolFactory pfac = ConnectionPoolFactory.getInstance();
	        pfac.clear();
        }
        this.dataStore = null;
    }

    /**
     * creates an ArcSDEDataStore using {@code test-data/testparams.properties} as
     * holder of datastore parameters
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public ArcSDEDataStore getDataStore() throws IOException {
        if (this.dataStore == null) {
            ConnectionPoolFactory pfac = ConnectionPoolFactory.getInstance();
            ConnectionConfig config = new ConnectionConfig(this.conProps);
            ArcSDEConnectionPool pool = pfac.createPool(config);
            this.dataStore = new ArcSDEDataStore(pool);
        }

        return this.dataStore;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the conProps.
     */
    public Properties getConProps() {
        return this.conProps;
    }

    /**
     * DOCUMENT ME!
     *
     * @param conProps The conProps to set.
     */
    public void setConProps(Properties conProps) {
        this.conProps = conProps;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the line_table.
     */
    public String getLine_table() {
        return this.line_table;
    }

    /**
     * DOCUMENT ME!
     *
     * @param line_table The line_table to set.
     */
    public void setLine_table(String line_table) {
        this.line_table = line_table;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the point_table.
     */
    public String getPoint_table() {
        return this.point_table;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the temp_table.
     */
    public String getTemp_table() {
        return this.temp_table;
    }

    /**
     * DOCUMENT ME!
     *
     * @param point_table The point_table to set.
     */
    public void setPoint_table(String point_table) {
        this.point_table = point_table;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the polygon_table.
     */
    public String getPolygon_table() {
        return this.polygon_table;
    }

    /**
     * DOCUMENT ME!
     *
     * @param polygon_table The polygon_table to set.
     */
    public void setPolygon_table(String polygon_table) {
        this.polygon_table = polygon_table;
    }

    /**
     * DOCUMENT ME!
     */
    public void deleteTempTable() {
        //only if the datastore was used
        if (this.dataStore != null) {
            ArcSDEConnectionPool pool = null;

            try {
                pool = getDataStore().getConnectionPool();
                deleteTempTable(pool);
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param pool DOCUMENT ME!
     */
    public void deleteTempTable(ArcSDEConnectionPool pool) {
        SeConnection conn = null;

        try {
            conn = pool.getConnection();

            SeTable table = new SeTable(conn, getTemp_table());
            table.delete();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        } finally {
            try {
                pool.release(conn);
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    /**
     * Creates an ArcSDE feature type names as <code>getTemp_table()</code> on
     * the underlying database and if <code>insertTestData == true</code> also
     * inserts some sample values.
     *
     * @param insertTestData wether to insert some sample rows or not
     *
     * @throws SeException for any error
     * @throws IOException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    public void createTemptTable(boolean insertTestData)
        throws SeException, IOException, UnavailableConnectionException {
        ArcSDEConnectionPool connPool = getDataStore()
            .getConnectionPool();

        deleteTempTable(connPool);

        SeConnection conn = connPool.getConnection();

        try {
            SeColumnDefinition[] coldefs;

            /*
             *   Create a qualified table name with current user's name and
             *   the name of the table to be created, "EXAMPLE".
             */
            SeLayer layer = new SeLayer(conn);
            String tableName = getTemp_table();
            SeTable table = new SeTable(conn, tableName);
            layer.setTableName(tableName);

            coldefs = createBaseTable(conn, table, layer);

            if (insertTestData) {
                insertData(layer, conn, coldefs);
            }
        } catch (SeException e) {
            e.printStackTrace();
            throw e;
        } finally {
            connPool.release(conn);
        }
    }

    /**
     *
     *
     */
    private static SeColumnDefinition[] createBaseTable(SeConnection conn,
        SeTable table, SeLayer layer) throws SeException {
        SeColumnDefinition[] colDefs = new SeColumnDefinition[6];

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

        /*
         *   Create the table using the DBMS default configuration keyword.
         *   Valid keywords are defined in the dbtune table.
         */
        table.create(colDefs, "DEFAULTS");

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

        /*
         *   Define the layer's Coordinate Reference
         */
        SeCoordinateReference coordref = getGenericCoordRef();

        //SeExtent ext = new SeExtent(-1000000.0, -1000000.0, 1000000.0, 1000000.0);
        SeExtent ext = coordref.getXYEnvelope();
        layer.setExtent(ext);
        layer.setCoordRef(coordref);

        /*
         *   Spatially enable the new table...
         */
        layer.create(3, 4);

        return colDefs;
    }

    /*
     *   Inserts 8 rows of data into the layer
     *
     *   Columns Inserted
     *   1. Integer  - values: 1 -> 8
     *   2. Short    - values: 1, 2 or 3
     *   3. Float    - values: Random values
     *   4. Double   - values: Random values
     *   5. String   - values: Describes the Shape Type
     *   6. Date     - values: July 28 2004 -> July 2 2004
     *   7. Shape    - 2 Rectangles, 1 point shape, 1 multi-point shape,
     *   1 simple line, 1 line, 1 single part polygon, 1 multipart polygon.
     */
    private static void insertData(SeLayer layer, SeConnection conn,
        SeColumnDefinition[] colDefs) throws SeException {

        /*
         *   Define the names of the columns that data is to be inserted into.
         */
        String[] columns = new String[7];

        columns[0] = new String(colDefs[0].getName()); // INT32 column
        columns[1] = new String(colDefs[1].getName()); // INT16 column
        columns[2] = new String(colDefs[2].getName()); // FLOAT32 column
        columns[3] = new String(colDefs[3].getName()); // FLOAT64 column
        columns[4] = new String(colDefs[4].getName()); // String column
        columns[5] = new String(colDefs[5].getName()); // Date column
        columns[6] = new String("SHAPE"); // Shape column

        SeInsert insert = null;

        try {
            insert = new SeInsert(conn);
            insert.intoTable(layer.getName(), columns);
            insert.setWriteMode(true);

            SeRow row = insert.getRowToSet();

            SeCoordinateReference coordref = layer.getCoordRef();
            LOGGER.fine("CRS constraints: " + coordref.getXYEnvelope()
                + ", presision: " + coordref.getXYUnits());

            SeShape shape = new SeShape(coordref);
            Calendar cal = Calendar.getInstance();

            // Year, month, date, hour, minute, second.
            cal.set(2004, 06, 28, 12, 0, 0);

            /*
             *   Insert 2 Rectangles into the layer
             */
            int numRectangles = 2;
            SeExtent rectangle = new SeExtent();
            int rowId = 1;

            for (rowId = 1; rowId <= numRectangles; rowId++) {
                rectangle.setMinX(-1);
                rectangle.setMinY(-1);
                rectangle.setMaxX(1);
                rectangle.setMaxY(1);
                shape.generateRectangle(rectangle);

                // set the values in the row
                row.setInteger(0, new Integer(rowId));
                row.setShort(1, new Short((short) ((rowId % 3) + 1)));
                row.setFloat(2, new Float(-1000.2 + rowId));
                row.setDouble(3, new Double(0.02 + (rowId / 1000.0)));
                row.setString(4, "RECTANGLE");
                row.setTime(5, cal);
                row.setShape(6, shape);

                // Insert row
                insert.execute();
            } // End for

            /*
             *   Insert a simple line
             */
            int points = 2;
            int numParts = 1;
            int[] partOffSets = new int[numParts];
            partOffSets[0] = 0;

            SDEPoint[] ptArray = new SDEPoint[points];

            for (int i = 0; i < points; i++) {
                ptArray[i] = new SDEPoint(-5000.0 + (i * 10),
                        -5000.0 + (i * 100));
            }

            SeShape line1 = new SeShape(coordref);
            line1.generateSimpleLine(points, numParts, partOffSets, ptArray);

            // set the col values
            row.setInteger(0, new Integer(rowId));
            row.setShort(1, new Short((short) ((rowId % 3) + 1)));
            row.setFloat(2, new Float(10.45));
            row.setDouble(3, new Double(-120.0232));
            row.setString(4, "SIMPLE LINE");
            row.setTime(5, cal);
            cal.roll(Calendar.DATE, true);
            row.setShape(6, line1);

            // insert row
            insert.execute();

            /*
             *   Insert a multipart line
             */
            numParts = 3;

            int[] partOffsets = new int[numParts];
            partOffsets[0] = 0;
            partOffsets[1] = 3;
            partOffsets[2] = 7;

            int numPts = 12;
            ptArray = new SDEPoint[numPts];

            // line 1
            ptArray[0] = new SDEPoint(100, 100);
            ptArray[1] = new SDEPoint(200, 200);
            ptArray[2] = new SDEPoint(300, 100);

            // line 2 - Self intersecting line
            ptArray[3] = new SDEPoint(200, 300);
            ptArray[4] = new SDEPoint(300, 400);
            ptArray[5] = new SDEPoint(300, 300);
            ptArray[6] = new SDEPoint(200, 400);

            // line 3
            ptArray[7] = new SDEPoint(100, 700);
            ptArray[8] = new SDEPoint(300, 500);
            ptArray[9] = new SDEPoint(500, 500);
            ptArray[10] = new SDEPoint(600, 600);
            ptArray[11] = new SDEPoint(700, 800);

            SeShape multiLine = new SeShape(coordref);
            multiLine.generateLine(numPts, numParts, partOffsets, ptArray);

            rowId++;

            // set the col values
            row.setInteger(0, new Integer(rowId));
            row.setShort(1, new Short((short) ((rowId % 3) + 1)));
            row.setFloat(2, new Float(40.05));
            row.setDouble(3, new Double(-120.000232));
            row.setString(4, "MULTI-PART LINE");
            row.setTime(5, cal);
            cal.roll(Calendar.DATE, true);
            row.setShape(6, multiLine);

            // insert row
            insert.execute();

            /*
             *   Insert simple area shape
             */
            numParts = 1;
            partOffsets = new int[numParts];
            partOffsets[0] = 0;
            numPts = 5;
            ptArray = new SDEPoint[numPts];
            ptArray[0] = new SDEPoint(-1, 0);
            ptArray[1] = new SDEPoint(0, 1);
            ptArray[2] = new SDEPoint(1, 0);
            ptArray[3] = new SDEPoint(0, -1);
            ptArray[4] = new SDEPoint(-1, 0);

            SeShape polygon = new SeShape(coordref);
            polygon.generatePolygon(numPts, numParts, partOffsets, ptArray);

            rowId++;

            // set the col values
            row.setInteger(0, new Integer(rowId));
            row.setShort(1, new Short((short) ((rowId % 3) + 1)));
            row.setFloat(2, new Float(1.456));
            row.setDouble(3, new Double(30.177));
            row.setString(4, "SINGLE PART POLYGON");
            row.setTime(5, cal);
            cal.roll(Calendar.DATE, true);
            row.setShape(6, polygon);

            // insert row
            insert.execute();

            /*
             *   Insert single point shape
             */
            SeShape point = null;
            point = new SeShape(coordref);
            numPts = 1;
            ptArray = new SDEPoint[numPts];
            ptArray[0] = new SDEPoint(8000, 8000);
            point.generatePoint(numPts, ptArray);

            rowId++;

            // set the col values
            row.setInteger(0, new Integer(rowId));
            row.setShort(1, new Short((short) ((rowId % 3) + 1)));
            row.setFloat(2, new Float(0.78782));
            row.setDouble(3, new Double(4332.3414233));
            row.setString(4, "SINGLE POINT SHAPE");
            row.setTime(5, cal);
            cal.roll(Calendar.DATE, true);
            row.setShape(6, point);

            // insert row
            insert.execute();

            /*
             *   Insert a multi part point
             */
            numPts = 4;
            ptArray = new SDEPoint[numPts];
            ptArray[0] = new SDEPoint(3000, 100);
            ptArray[1] = new SDEPoint(3000, 300);
            ptArray[2] = new SDEPoint(4000, 300);
            ptArray[3] = new SDEPoint(4000, 100);

            point.generatePoint(numPts, ptArray);

            rowId++;

            // set the col values
            row.setInteger(0, new Integer(rowId));
            row.setShort(1, new Short((short) ((rowId % 3) + 1)));
            row.setFloat(2, new Float(0.786456));
            row.setDouble(3, new Double(42342.177));
            row.setString(4, "MULTI POINT SHAPE");
            row.setTime(5, cal);
            cal.roll(Calendar.DATE, true);
            row.setShape(6, point);

            // insert row
            insert.execute();

            /*
             *   Generate complex area shape
             */
            numParts = 2;
            partOffsets = new int[numParts];
            partOffsets[0] = 0;
            partOffsets[1] = 14;
            numPts = 18;
            ptArray = new SDEPoint[numPts];

            // part one
            ptArray[0] = new SDEPoint(100, 1100);
            ptArray[1] = new SDEPoint(1500, 1100);
            ptArray[2] = new SDEPoint(1500, 1900);
            ptArray[3] = new SDEPoint(100, 1900);
            ptArray[4] = new SDEPoint(100, 1100);

            // Hole - sub part of part one
            ptArray[5] = new SDEPoint(200, 1200);
            ptArray[6] = new SDEPoint(200, 1500);
            ptArray[7] = new SDEPoint(500, 1500);
            ptArray[8] = new SDEPoint(500, 1700);
            ptArray[9] = new SDEPoint(800, 1700);
            ptArray[10] = new SDEPoint(800, 1500);
            ptArray[11] = new SDEPoint(500, 1500);
            ptArray[12] = new SDEPoint(500, 1200);
            ptArray[13] = new SDEPoint(200, 1200);

            // part two
            ptArray[14] = new SDEPoint(1600, 1200);
            ptArray[15] = new SDEPoint(2800, 1650);
            ptArray[16] = new SDEPoint(1800, 2000);
            ptArray[17] = new SDEPoint(1600, 1200);

            polygon.generatePolygon(numPts, numParts, partOffsets, ptArray);

            rowId++;

            // set the col values
            row.setInteger(0, new Integer(rowId));
            row.setShort(1, new Short((short) ((rowId % 3) + 1)));
            row.setFloat(2, new Float(230.7862));
            row.setDouble(3, new Double(4234.33177));
            row.setString(4, "MULTI PART POLYGON");
            row.setTime(5, cal);
            cal.roll(Calendar.DATE, true);
            row.setShape(6, polygon);

            // insert row
            insert.execute();

            insert.close();
        } catch (SeException e) {
            /*
             *   Making sure the insert stream was closed. If the stream isn't closed,
             *   the resources used by the stream will be held/locked by the stream
             *   until the associated connection is closed.
             */
            try {
                insert.close();
            } catch (SeException se) {
                System.out.println(se.getSeError().getErrDesc());
            }

            System.out.println(e.getSeError().getSdeError());
            System.out.println(e.getSeError().getExtError());
            throw e;
        }
    } // End method insertData    

    /**
     * Creates a FeatureCollection with features whose schema adheres to the
     * one created in <code>createTestData()</code> and returns it.
     * 
     * <p>
     * This schema is something like:
     * <pre>
     *  colDefs[0] "INT32_COL", SeColumnDefinition.TYPE_INTEGER, 10, 0, true
     *  colDefs[1] = "INT16_COL", SeColumnDefinition.TYPE_SMALLINT, 4, 0, true
     *  colDefs[2] = "FLOAT32_COL", SeColumnDefinition.TYPE_FLOAT, 5, 2, true
     *  colDefs[3] = "FLOAT64_COL", SeColumnDefinition.TYPE_DOUBLE, 15, 4, true
     *  colDefs[4] = "STRING_COL", SeColumnDefinition.TYPE_STRING, 25, 0, true
     *  colDefs[5] = "DATE_COL", SeColumnDefinition.TYPE_DATE, 1, 0, true
     *  colDefs[6] = "SHAPE", Geometry, 1, 0, true
     *  </pre>
     * </p>
     *
     * @param jtsGeomType class of JTS geometry to create
     * @param numFeatures number of features to create.
     *
     * @return
     *
     * @throws IOException if the schema for te test table cannot be fetched
     *         from the database.
     * @throws IllegalAttributeException if the feature type created from the
     *         test table cannot build a feature with the given attribute
     *         values.
     */
    public FeatureCollection createTestFeatures(Class jtsGeomType,
        int numFeatures) throws IOException, IllegalAttributeException {
        FeatureCollection col = FeatureCollections.newCollection();
        FeatureType type = getDataStore().getSchema(getTemp_table());
        Object[] values = new Object[type.getAttributeCount()];

        for (int i = 0; i < numFeatures; i++) {
            values[0] = new Integer(i);

            //put some nulls
            values[1] = ((i % 2) == 0) ? null : new Integer(2 * i);
            values[2] = new Float(0.1 * i);
            values[3] = new Double(1000 * i);
            values[4] = "String value #" + i;

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, i);
            values[5] = cal.getTime();
            values[6] = createTestGeometry(jtsGeomType, i);

            Feature f = type.create(values);
            col.add(f);
        }

        return col;
    }

    /**
     * DOCUMENT ME!
     *
     * @param geomType DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    private static Geometry createTestGeometry(Class geomType, int index) {
        Geometry geom = null;
        GeometryFactory gf = new GeometryFactory();

        if (geomType == Geometry.class) {
            geom = createTestGenericGeometry(gf, index);
        } else if (geomType == Point.class) {
            geom = createTestPoint(gf, index);
        } else if (geomType == MultiPoint.class) {
            geom = createTestMultiPoint(gf, index);
        } else if (geomType == LineString.class) {
            geom = createTestLineString(gf, index);
        } else if (geomType == MultiLineString.class) {
            geom = createTestMultiLineString(gf, index);
        } else if (geomType == Polygon.class) {
            geom = createTestPolygon(gf, index);
        } else if (geomType == MultiPolygon.class) {
            geom = createTestMultiPolygon(gf, index);
        } else {
            throw new UnsupportedOperationException("finish implementing this!");
        }

        return geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static Geometry createTestGenericGeometry(GeometryFactory gf,
        int index) {
        if ((index % 6) == 0) {
            return createTestPoint(gf, index);
        } else if ((index % 4) == 0) {
            return createTestMultiPoint(gf, index);
        } else if ((index % 3) == 0) {
            return createTestLineString(gf, index);
        } else if ((index % 2) == 0) {
            return createTestMultiLineString(gf, index);
        } else if ((index % 1) == 0) {
            return createTestPolygon(gf, index);
        } else {
            return createTestMultiPolygon(gf, index);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static Point createTestPoint(GeometryFactory gf, int index) {
        return gf.createPoint(new Coordinate(index, index));
    }

    /**
     * DOCUMENT ME!
     *
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static MultiPoint createTestMultiPoint(GeometryFactory gf, int index) {
        Coordinate[] coords = {
                new Coordinate(index, index), new Coordinate(-index, -index)
            };

        return gf.createMultiPoint(coords);
    }

    /**
     * DOCUMENT ME!
     *
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static LineString createTestLineString(GeometryFactory gf, int index) {
        Coordinate[] coords = {
                new Coordinate(0, 0), new Coordinate(++index, -index)
            };

        return gf.createLineString(coords);
    }

    /**
     * DOCUMENT ME!
     *
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static MultiLineString createTestMultiLineString(
        GeometryFactory gf, int index) {
        Coordinate[] coords1 = {
                new Coordinate(0, 0), new Coordinate(++index, ++index)
            };
        Coordinate[] coords2 = {
                new Coordinate(0, index), new Coordinate(index, 0)
            };
        LineString[] lines = {
                gf.createLineString(coords1), gf.createLineString(coords2)
            };

        return gf.createMultiLineString(lines);
    }

    /**
     * DOCUMENT ME!
     *
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static Polygon createTestPolygon(GeometryFactory gf, int index) {
        Coordinate[] coords = {
                new Coordinate(index, index), new Coordinate(index, index + 1),
                new Coordinate(index + 1, index + 1),
                new Coordinate(index + 1, index), new Coordinate(index, index)
            };
        LinearRing shell = gf.createLinearRing(coords);

        return gf.createPolygon(shell, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static MultiPolygon createTestMultiPolygon(GeometryFactory gf,
        int index) {
        Polygon[] polys = {
                createTestPolygon(gf, index), createTestPolygon(gf, 1 + index)
            };

        MultiPolygon mp = gf.createMultiPolygon(polys);
        //System.out.println(mp);

        return mp;
    }

    /**
     * Creates and returns a <code>SeCoordinateReference</code> CRS, though
     * based on WGS84, is inclusive enough (in terms of valid coordinate range
     * and presicion) to deal with most coordintates.
     * 
     * <p>
     * Actually tested to deal with coordinates with 0.0002 units of separation
     * as well as with large coordinates such as UTM (values greater than
     * 500,000.00)
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public static SeCoordinateReference getGenericCoordRef()
        throws SeException {
        //create a sde CRS with a huge value range and 5 digits of presission
        SeCoordinateReference seCRS = new SeCoordinateReference();
        int shift = 100000;
        SeExtent validRange = new SeExtent(-shift, -shift, shift, shift);
        seCRS.setXYByEnvelope(validRange);
        GeometryBuilderTest.LOGGER.fine("CRS: " + seCRS.getXYEnvelope());

        return seCRS;
    }
}
