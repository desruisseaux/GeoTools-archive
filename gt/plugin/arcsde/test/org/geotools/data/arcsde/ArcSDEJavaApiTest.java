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

import java.io.IOException;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;

/**
 * Exersices the ArcSDE Java API to ensure our assumptions are correct.
 * 
 * <p>
 * Some of this tests asserts the information from the documentation found on <a
 * href="http://arcsdeonline.esri.com">arcsdeonline </a>, and others are needed
 * to validate our assumptions in the API behavoir due to the very little
 * documentation ESRI provides about the less obvious things.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class ArcSDEJavaApiTest extends TestCase {
	/** package logger */
	private static Logger LOGGER = Logger.getLogger(ArcSDEJavaApiTest.class
			.getPackage().getName());

	/** utility to load test parameters and build a datastore with them */
	private TestData testData;

	/**
	 * Constructor for ArcSDEJavaApiTest.
	 * 
	 * @param arg0
	 */
	public ArcSDEJavaApiTest(String arg0) {
		super(arg0);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param args
	 *            DOCUMENT ME!
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ArcSDEJavaApiTest.class);
	}

	/**
	 * loads /testData/testparams.properties into a Properties object, wich is
	 * used to obtain test tables names and is used as parameter to find the
	 * DataStore
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.testData = new TestData();
		this.testData.setUp();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	protected void tearDown() throws Exception {
		this.testData.tearDown();
		this.testData = null;
		super.tearDown();
	}

	public void testNullSQLConstruct() throws Exception {
		ArcSDEConnectionPool pool = this.testData.getDataStore().getConnectionPool();
		String typeName = this.testData.getPolygon_table();

		String[] columns = { "POP_ADMIN" };
		SeSqlConstruct sql = null;
		SeConnection conn = pool.getConnection();

		try {
			SeQuery rowQuery = new SeQuery(conn, columns, sql);
			rowQuery.prepareQuery();
			rowQuery.execute();
			fail("A null SeSqlConstruct should have thrown an exception!");
		} catch (SeException e) {
			LOGGER.info("Null SqlConstruct throwed exception, it's OK");
		}
	}

	public void testEmptySQLConstruct() throws Exception {
		ArcSDEConnectionPool pool = this.testData.getDataStore().getConnectionPool();
		String typeName = this.testData.getPolygon_table();

		String[] columns = { "POP_ADMIN" };
		SeSqlConstruct sql = new SeSqlConstruct(typeName);
		SeConnection conn = pool.getConnection();

		SeQuery rowQuery = new SeQuery(conn, columns, sql);
		rowQuery.prepareQuery();
		rowQuery.execute();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public void testGetBoundsWhileFetchingRows() throws Exception {
		try {
			ArcSDEConnectionPool pool = this.testData.getDataStore()
					.getConnectionPool();
			String typeName = this.testData.getPolygon_table();
			String where = "POP_ADMIN < 270000";

			String[] columns = { "POP_ADMIN" };
			SeSqlConstruct sql = new SeSqlConstruct(typeName, where);

			SeQueryInfo qInfo = new SeQueryInfo();
			qInfo.setConstruct(sql);

			// add a bounding box filter and verify both spatial and non spatial
			// constraints affects the COUNT statistics
			SeExtent extent = new SeExtent(-68, -55, -63, -52);

			SeLayer layer = pool.getSdeLayer(typeName);
			SeShape filterShape = new SeShape(layer.getCoordRef());
			filterShape.generateRectangle(extent);

			SeShapeFilter bboxFilter = new SeShapeFilter(typeName, layer
					.getSpatialColumn(), filterShape, SeFilter.METHOD_ENVP,
					true);
			SeFilter[] spatFilters = { bboxFilter };

			SeConnection conn = pool.getConnection();

			for (int i = 0; i < 26; i++) {
				LOGGER.info("Running iteration #" + i);

				SeQuery rowQuery = new SeQuery(conn, columns, sql);
				rowQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE, true,
						spatFilters);
				rowQuery.prepareQuery();
				rowQuery.execute();

				// fetch some rows
				rowQuery.fetch();
				rowQuery.fetch();
				rowQuery.fetch();

				SeQuery countQuery = new SeQuery(conn, columns, sql);
				countQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE, true,
						spatFilters);

				final int expCount = 2;

				SeTable.SeTableStats tableStats = countQuery
						.calculateTableStatistics("POP_ADMIN",
								SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

				rowQuery.fetch();
				rowQuery.fetch();

				int resultCount = tableStats.getCount();

				assertEquals(expCount, resultCount);

				rowQuery.close();
				countQuery.close();
			}
			LOGGER.info("TEST PASSED");
		} catch (SeException e) {
			LOGGER.warning(e.getSeError().getErrDesc());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public void testCalculateCount() throws Exception {
		try {
			ArcSDEConnectionPool pool = this.testData.getDataStore()
					.getConnectionPool();
			SeConnection conn = pool.getConnection();

			String typeName = this.testData.getPolygon_table();
			String where = "POP_ADMIN < 270000";
			int expCount = 4;

			String[] columns = { "POP_ADMIN" };
			SeSqlConstruct sql = new SeSqlConstruct(typeName, where);
			SeQuery query = new SeQuery(conn, columns, sql);
			SeQueryInfo qInfo = new SeQueryInfo();
			qInfo.setConstruct(sql);

			SeTable.SeTableStats tableStats = query.calculateTableStatistics(
					"POP_ADMIN", SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

			assertEquals(expCount, tableStats.getCount());
			query.close();

			// add a bounding box filter and verify both spatial and non spatial
			// constraints affects the COUNT statistics
			SeExtent extent = new SeExtent(-68, -55, -63, -52);

			SeLayer layer = pool.getSdeLayer(typeName);
			SeShape filterShape = new SeShape(layer.getCoordRef());
			filterShape.generateRectangle(extent);

			query = new SeQuery(conn, columns, sql);

			SeShapeFilter bboxFilter = new SeShapeFilter(typeName, layer
					.getSpatialColumn(), filterShape, SeFilter.METHOD_ENVP,
					true);
			SeFilter[] spatFilters = { bboxFilter };

			query.setSpatialConstraints(SeQuery.SE_OPTIMIZE, true, spatFilters);

			expCount = 2;
			tableStats = query.calculateTableStatistics("POP_ADMIN",
					SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

			int resultCount = tableStats.getCount();

			assertEquals(expCount, resultCount);
		} catch (SeException e) {
			LOGGER.warning(e.getSeError().getErrDesc());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws SeException
	 *             DOCUMENT ME!
	 */
	public void testGenericSeCoordinateReferenceLimits() throws SeException {
		SeCoordinateReference crs = this.testData.getGenericCoordRef();
		LOGGER.info("CRS constraints: " + crs.getXYEnvelope() + ", presision: "
				+ crs.getXYUnits());

		SDEPoint[] ptArray = new SDEPoint[2];
		SeShape shape = new SeShape(crs);
		double[][][] coords;
		ptArray[0] = new SDEPoint(0.0, 0.0);

		// find the lower limit of separation between coordinates
		double shift = 1.0;

		try {
			while (true) {
				ptArray[1] = new SDEPoint(shift, shift);
				shape.generateLine(2, 1, new int[] { 0 }, ptArray);
				shift /= 10;
			}
		} catch (SeException e) {
			LOGGER.info("Lower limit: " + String.valueOf(10 * shift));
		}

		int numPts = 5;
		ptArray = new SDEPoint[numPts];
		ptArray[0] = new SDEPoint(-1, 0);
		ptArray[1] = new SDEPoint(0, 1);
		ptArray[2] = new SDEPoint(1, 0);
		ptArray[3] = new SDEPoint(0, -1);
		ptArray[4] = new SDEPoint(-1, 0);

		SeShape polygon = new SeShape(crs);
		polygon.generatePolygon(numPts, 1, new int[] { 0 }, ptArray);
	}

	/**
	 * Ensures a point SeShape behaves as expected.
	 * 
	 * @throws SeException
	 *             if it is thrown while constructing the SeShape
	 */
	public void testPointFormat() throws SeException {
		int numPts = 1;
		SDEPoint[] ptArray = new SDEPoint[numPts];
		ptArray[0] = new SDEPoint(3000, 100);

		SeShape point = new SeShape();
		point.generatePoint(numPts, ptArray);

		int numParts = 0;
		double[][][] coords = point.getAllCoords();

		assertEquals("Num of parts invalid", numPts, coords.length);

		for (; numParts < numPts; numParts++) {
			assertEquals("Num subparts invalid", 1, coords[numParts].length);
		}

		for (; numParts < numPts; numParts++) {
			int numSubParts = 0;

			for (; numSubParts < coords[numParts].length; numParts++) {
				assertEquals("Num of points invalid", 2,
						coords[numParts][numSubParts].length);
			}
		}
	}

	/**
	 * Ensures a multipoint SeShape behaves as expected.
	 * 
	 * @throws SeException
	 *             if it is thrown while constructing the SeShape
	 */
	public void testMultiPointFormat() throws SeException {
		int numPts = 4;
		SDEPoint[] ptArray = new SDEPoint[numPts];
		ptArray[0] = new SDEPoint(3000, 100);
		ptArray[1] = new SDEPoint(3000, 300);
		ptArray[2] = new SDEPoint(4000, 300);
		ptArray[3] = new SDEPoint(4000, 100);

		SeShape point = new SeShape();
		point.generatePoint(numPts, ptArray);

		double[][][] coords = point.getAllCoords();
		assertEquals("Num of parts invalid", numPts, coords.length);

		int numParts = 0;

		for (; numParts < numPts; numParts++) {
			assertEquals("Num subparts invalid", 1, coords[numParts].length);
		}

		for (; numParts < numPts; numParts++) {
			int numSubParts = 0;

			for (; numSubParts < coords[numParts].length; numParts++) {
				assertEquals("Num of points invalid", 2,
						coords[numParts][numSubParts].length);
			}
		}
	}

	/**
	 * Ensures a linestring SeShape behaves as expected.
	 * 
	 * @throws SeException
	 *             if it is thrown while constructing the SeShape
	 */
	public void testLineStringFormat() throws SeException {
		int numPts = 4;
		SDEPoint[] ptArray = new SDEPoint[numPts];
		ptArray[0] = new SDEPoint(3000, 100);
		ptArray[1] = new SDEPoint(3000, 300);
		ptArray[2] = new SDEPoint(4000, 300);
		ptArray[3] = new SDEPoint(4000, 100);

		SeShape point = new SeShape();
		int numParts = 1;
		int[] partOffsets = { 0 }; // index of each part's start in the gobal
		// coordinate array
		point.generateLine(numPts, numParts, partOffsets, ptArray);

		double[][][] coords = point.getAllCoords();

		assertEquals("Num of parts invalid", 1, coords.length);

		assertEquals("Num subparts invalid", 1, coords[0].length);

		assertEquals("Num of points invalid", 2 * numPts, coords[0][0].length);
	}

	/**
	 * Ensures a multilinestring SeShape behaves as expected.
	 * 
	 * @throws SeException
	 *             if it is thrown while constructing the SeShape
	 */
	public void testMultiLineStringFormat() throws SeException {
		int numPts = 4;
		SDEPoint[] ptArray = new SDEPoint[numPts];
		ptArray[0] = new SDEPoint(3000, 100);
		ptArray[1] = new SDEPoint(3000, 300);
		ptArray[2] = new SDEPoint(4000, 300);
		ptArray[3] = new SDEPoint(4000, 100);

		SeShape point = new SeShape();
		int numParts = 2;
		int[] partOffsets = { 0, 2 }; // index of each part's start in the
		// gobal coordinate array
		point.generateLine(numPts, numParts, partOffsets, ptArray);

		double[][][] coords = point.getAllCoords();

		assertEquals("Num of parts invalid", numParts, coords.length);

		assertEquals("Num subparts invalid", 1, coords[0].length);
		assertEquals("Num subparts invalid", 1, coords[1].length);

		assertEquals("Num of points invalid", numPts, coords[0][0].length);
		assertEquals("Num of points invalid", numPts, coords[1][0].length);
	}

	/**
	 * Ensures a polygon SeShape behaves as expected, building a simple polygon
	 * and a polygon with a hole.
	 * 
	 * @throws SeException
	 *             if it is thrown while constructing the SeShape
	 */
	public void testPolygonFormat() throws SeException {
		/*
		 * Generate an area shape composed of two polygons, the first with a
		 * hole
		 */
		int numPts = 4;
		int numParts = 1;
		int[] partOffsets = new int[numParts];
		partOffsets[0] = 0;

		SDEPoint[] ptArray = new SDEPoint[numPts];

		// simple polygon
		ptArray[0] = new SDEPoint(1600, 1200);
		ptArray[1] = new SDEPoint(2800, 1650);
		ptArray[2] = new SDEPoint(1800, 2000);
		ptArray[3] = new SDEPoint(1600, 1200);

		SeShape polygon = new SeShape();
		polygon.generatePolygon(numPts, numParts, partOffsets, ptArray);

		double[][][] coords = polygon.getAllCoords();

		assertEquals("Num of parts invalid", numParts, coords.length);
		assertEquals("Num subparts invalid", 1, coords[0].length);
		assertEquals("Num of points invalid", 2 * 4, coords[0][0].length);

		numPts = 14;
		numParts = 1;
		ptArray = new SDEPoint[numPts];
		partOffsets = new int[numParts];
		partOffsets[0] = 0;

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

		polygon = new SeShape();
		polygon.generatePolygon(numPts, numParts, partOffsets, ptArray);

		coords = polygon.getAllCoords();

		assertEquals("Num of parts invalid", numParts, coords.length);
		assertEquals("Num subparts invalid", 2, coords[0].length);

		// first part of first polygon (shell) has 5 points
		assertEquals("Num of points invalid", 2 * 5, coords[0][0].length);

		// second part of first polygon (hole) has 9 points
		assertEquals("Num of points invalid", 2 * 9, coords[0][1].length);
	}

	/**
	 * Ensures a multipolygon SeShape behaves as expected.
	 * 
	 * @throws SeException
	 *             if it is thrown while constructing the SeShape
	 */
	public void testMultiPolygonFormat() throws SeException {
		/*
		 * Generate an area shape composed of two polygons, the first with a
		 * hole
		 */
		int numPts = 18;
		int numParts = 2;
		int[] partOffsets = new int[numParts];
		partOffsets[0] = 0;
		partOffsets[1] = 14;

		SDEPoint[] ptArray = new SDEPoint[numPts];

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

		SeShape multipolygon = new SeShape();
		multipolygon.generatePolygon(numPts, numParts, partOffsets, ptArray);

		double[][][] coords = multipolygon.getAllCoords();

		assertEquals("Num of parts invalid", numParts, coords.length);

		// the first polygon has 2 parts
		assertEquals("Num subparts invalid", 2, coords[0].length);

		// the second polygon has only 1 part
		assertEquals("Num subparts invalid", 1, coords[1].length);

		// first part of first polygon (shell) has 5 points
		assertEquals("Num of points invalid", 2 * 5, coords[0][0].length);

		// second part of first polygon (hole) has 9 points
		assertEquals("Num of points invalid", 2 * 9, coords[0][1].length);

		// second polygon (shell with no holes) has 4 points
		assertEquals("Num of points invalid", 2 * 4, coords[1][0].length);
	}

	/**
	 * Creates an ArcSDE table, "EXAMPLE", and adds a spatial column, "SHAPE",
	 * to it.
	 * 
	 * <p>
	 * This code is directly taken from the createBaseTable mehtod of the
	 * arcsdeonline "Working with layers" example, to verify that it works prior
	 * to blame the gt implementation.
	 * </p>
	 * 
	 * @throws SeException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @throws UnavailableConnectionException
	 *             DOCUMENT ME!
	 */
	public void testCreateBaseTable() throws SeException, IOException,
			UnavailableConnectionException {
		ArcSDEConnectionPool connPool = this.testData
				.getDataStore().getConnectionPool();
		SeConnection conn = connPool.getConnection();

		SeLayer layer = new SeLayer(conn);
		SeTable table = null;

		try {
			/*
			 * Create a qualified table name with current user's name and the
			 * name of the table to be created, "EXAMPLE".
			 */
			String tableName = (conn.getUser() + ".EXAMPLE");
			table = new SeTable(conn, tableName);
			layer.setTableName("EXAMPLE");

			try {
				table.delete();
			} catch (Exception e) {
				LOGGER.warning(e.getMessage());
			}

			SeColumnDefinition[] colDefs = new SeColumnDefinition[7];

			/*
			 * Define the columns and their attributes for the table to be
			 * created. NOTE: The valid range/values of size and scale
			 * parameters vary from one database to another.
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
			 * Create the table using the DBMS default configuration keyword.
			 * Valid keywords are defined in the dbtune table.
			 */
			System.out
					.println("\n--> Creating a table using DBMS Default Keyword");
			table.create(colDefs, "DEFAULTS");
			System.out.println(" - Done.");

			/*
			 * Define the attributes of the spatial column
			 */
			layer.setSpatialColumnName("SHAPE");

			/*
			 * Set the type of shapes that can be inserted into the layer. Shape
			 * type can be just one or many. NOTE: Layers that contain more than
			 * one shape type can only be accessed through the C and Java APIs
			 * and Arc Explorer Java 3.x. They cannot be seen from ArcGIS
			 * desktop applications.
			 */
			layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK
					| SeLayer.SE_POINT_TYPE_MASK | SeLayer.SE_LINE_TYPE_MASK
					| SeLayer.SE_SIMPLE_LINE_TYPE_MASK
					| SeLayer.SE_AREA_TYPE_MASK
					| SeLayer.SE_MULTIPART_TYPE_MASK);
			layer.setGridSizes(1100.0, 0.0, 0.0);
			layer.setDescription("Layer Example");

			SeExtent ext = new SeExtent(0.0, 0.0, 10000.0, 10000.0);
			layer.setExtent(ext);

			/*
			 * Define the layer's Coordinate Reference
			 */
			SeCoordinateReference coordref = this.testData.getGenericCoordRef();
			layer.setCoordRef(coordref);

			/*
			 * Spatially enable the new table...
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
	 * arcsdeonline "Working with layers" example, to verify that it works prior
	 * to blame the gt implementation.
	 * </p>
	 * 
	 * @throws SeException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @throws UnavailableConnectionException
	 *             DOCUMENT ME!
	 */
	public void testCreateNonStandardSchema() throws SeException, IOException,
			UnavailableConnectionException {
		ArcSDEConnectionPool connPool = this.testData
				.getDataStore().getConnectionPool();
		SeConnection conn = connPool.getConnection();

		SeLayer layer = new SeLayer(conn);
		SeTable table = null;

		try {
			/*
			 * Create a qualified table name with current user's name and the
			 * name of the table to be created, "EXAMPLE".
			 */
			String tableName = (conn.getUser() + ".NOTENDSWITHGEOM");
			table = new SeTable(conn, tableName);
			layer.setTableName("NOTENDSWITHGEOM");

			try {
				table.delete();
			} catch (Exception e) {
				// intentionally blank
			}

			/*
			 * Create the table using the DBMS default configuration keyword.
			 * Valid keywords are defined in the dbtune table.
			 */
			System.out
					.println("\n--> Creating a table using DBMS Default Keyword");

			SeColumnDefinition[] tmpCols = new SeColumnDefinition[] { new SeColumnDefinition(
					"tmp", SeColumnDefinition.TYPE_STRING, 5, 0, true) };
			table.create(tmpCols, "DEFAULTS");
			System.out.println(" - Done.");

			SeColumnDefinition[] colDefs = new SeColumnDefinition[7];

			/*
			 * Define the columns and their attributes for the table to be
			 * created. NOTE: The valid range/values of size and scale
			 * parameters vary from one database to another.
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
			 * Define the attributes of the spatial column
			 */
			layer.setSpatialColumnName("SHAPE");

			/*
			 * Set the type of shapes that can be inserted into the layer. Shape
			 * type can be just one or many. NOTE: Layers that contain more than
			 * one shape type can only be accessed through the C and Java APIs
			 * and Arc Explorer Java 3.x. They cannot be seen from ArcGIS
			 * desktop applications.
			 */
			layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK
					| SeLayer.SE_POINT_TYPE_MASK | SeLayer.SE_LINE_TYPE_MASK
					| SeLayer.SE_SIMPLE_LINE_TYPE_MASK
					| SeLayer.SE_AREA_TYPE_MASK
					| SeLayer.SE_MULTIPART_TYPE_MASK);
			layer.setGridSizes(1100.0, 0.0, 0.0);
			layer.setDescription("Layer Example");

			SeExtent ext = new SeExtent(0.0, 0.0, 10000.0, 10000.0);
			layer.setExtent(ext);

			/*
			 * Define the layer's Coordinate Reference
			 */
			SeCoordinateReference coordref = new SeCoordinateReference();
			coordref.setXY(0, 0, 100);
			layer.setCoordRef(coordref);

			/*
			 * Spatially enable the new table...
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
			try {
				table.delete();
			} catch (Exception e) {
				// intentionally blank
			}

			try {
				layer.delete();
			} catch (Exception e) {
				// intentionally blank
			}

			connPool.release(conn);
		}
	} // End method createBaseTable
}
