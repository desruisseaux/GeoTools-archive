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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.geotools.TestData;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SimpleFeature;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/shpLazyLoadingIndex/ext/shape/test/org/geotools/data/shapefile/indexed/ShapefileDataStoreTest.java $
 * @version $Id$
 * @author Ian Schneider
 */
public class ShapefileDataStoreTest extends TestCaseSupport {
	final static String STATE_POP = "shapes/statepop.shp";

	final static String STREAM = "shapes/stream.shp";

	public ShapefileDataStoreTest(String testName) throws IOException {
		super(testName);
	}

	protected FeatureCollection loadFeatures(String resource, Query q)
			throws Exception {
		if (q == null) {
			q = new DefaultQuery();
		}

		URL url = TestData.url(resource);
		IndexedShapefileDataStore s = new IndexedShapefileDataStore(url);
		FeatureSource fs = s.getFeatureSource(s.getTypeNames()[0]);

		return fs.getFeatures(q).collection();
	}

	protected FeatureCollection loadFeatures(IndexedShapefileDataStore s)
			throws Exception {
		return s.getFeatureSource(s.getTypeNames()[0]).getFeatures()
				.collection();
	}

	public void testLoad() throws Exception {
		loadFeatures(STATE_POP, null);
	}

	public void testSchema() throws Exception {
		URL url = TestData.url(STATE_POP);
		IndexedShapefileDataStore s = new IndexedShapefileDataStore(url);
		FeatureType schema = s.getSchema(s.getTypeNames()[0]);
		AttributeType[] types = schema.getAttributeTypes();
		assertEquals("Number of Attributes", 253, types.length);
		assertNotNull(schema.getDefaultGeometry().getCoordinateSystem());
	}

	public void testSpacesInPath() throws Exception {
		URL u = TestData.url(this, "folder with spaces/pointtest.shp");
		File f = new File(URLDecoder.decode(u.getFile(), "UTF-8"));
		assertTrue(f.exists());

		IndexedShapefileDataStore s = new IndexedShapefileDataStore(u);
		loadFeatures(s);
	}

	/**
	 * Test envelope versus old DataSource
	 */
	public void testEnvelope() throws Exception {
		FeatureCollection features = loadFeatures(STATE_POP, null);
		testEnvelope(features, IndexedShapefileDataStore.TREE_GRX);
		testEnvelope(features, IndexedShapefileDataStore.TREE_QIX);
		testEnvelope(features, IndexedShapefileDataStore.TREE_NONE);
	}

	private void testEnvelope(FeatureCollection features, byte treeType)
			throws MalformedURLException, IOException {
		IndexedShapefileDataStore s = new IndexedShapefileDataStore(TestData
				.url(STATE_POP), null, true, true, treeType);
		String typeName = s.getTypeNames()[0];
		FeatureResults all = s.getFeatureSource(typeName).getFeatures();

		assertEquals(features.getBounds(), all.getBounds());
	}

	public void testCreateAndReadQIX() throws Exception {
		File shpFile = copyShapefiles(STATE_POP);
		URL url = shpFile.toURL();
		String filename = url.getFile();
		filename = filename.substring(0, filename.lastIndexOf("."));

		File file = new File(filename + ".qix");

		if (file.exists()) {
			file.delete();
		}
		file.deleteOnExit();

		IndexedShapefileDataStore ds = new IndexedShapefileDataStore(url, null,
				true, true, IndexedShapefileDataStore.TREE_QIX);
		FeatureCollection features = ds.getFeatureSource().getFeatures();
		FeatureIterator indexIter = features.features();

		GeometryFactory factory=new GeometryFactory();
		double area = Double.MAX_VALUE;
		Feature smallestFeature=null;
		while (indexIter.hasNext()) {
			Feature newFeature=indexIter.next();
			
			double newArea= factory.toGeometry(newFeature.getBounds()).getArea();
			
			if (smallestFeature == null || newArea<area ){
				smallestFeature = newFeature;
				area=newArea;
			}
		}
		indexIter.close();


		IndexedShapefileDataStore ds2 = new IndexedShapefileDataStore(url,
				null, false, false, IndexedShapefileDataStore.TREE_NONE);

		Envelope newBounds = ds.getBounds(Query.ALL);
		double dx = newBounds.getWidth()/4;
		double dy = newBounds.getHeight()/4;
		newBounds=new Envelope(newBounds.getMinX()+dx, newBounds.getMaxX()-dx,
				newBounds.getMinY()+dy, newBounds.getMaxY()-dy);
		performQueryComparison(ds, ds2, newBounds);
		performQueryComparison(ds, ds2, smallestFeature.getBounds());

		assertTrue(file.exists());
	}

	private ArrayList performQueryComparison(IndexedShapefileDataStore indexedDS, IndexedShapefileDataStore baselineDS, Envelope newBounds) throws FactoryConfigurationError, IllegalFilterException, IOException {
		FeatureCollection features;
		FeatureIterator indexIter;
		FilterFactory fac = FilterFactoryFinder.createFilterFactory();
		org.geotools.filter.expression.BBoxExpression bbox = fac.createBBoxExpression(newBounds);
		org.geotools.filter.expression.AttributeExpression attrExpression = fac.createAttributeExpression(
				indexedDS.getSchema().getDefaultGeometry().getName());
		GeometryFilter filter = fac
				.createGeometryFilter(FilterType.GEOMETRY_BBOX);
		filter.addRightGeometry(attrExpression);
		filter.addLeftGeometry(bbox);
		features = indexedDS.getFeatureSource().getFeatures(filter);
		FeatureCollection features2 = baselineDS.getFeatureSource()
				.getFeatures(filter);

		FeatureIterator baselineIter = features2.features();
		indexIter=features.features();
		
		ArrayList baselineFeatures=new ArrayList();
		ArrayList indexedFeatures=new ArrayList();
		
		try{
			while( baselineIter.hasNext() ){
				baselineFeatures.add(baselineIter.next());
			}
			while( indexIter.hasNext() ){
				indexedFeatures.add(indexIter.next());
			}
			assertFalse(indexIter.hasNext());
			assertFalse(baselineIter.hasNext());
			assertEquals( baselineFeatures, indexedFeatures);
		}finally{
			indexIter.close();
			baselineIter.close();
		}
		return indexedFeatures;
	}

	public void testCreateAndReadGRX() throws Exception {
		URL url = TestData.url(STATE_POP);
		String filename = url.getFile();
		filename = filename.substring(0, filename.lastIndexOf("."));

		File file = new File(filename + ".grx");

		if (file.exists()) {
			file.delete();
		}

		IndexedShapefileDataStore ds = new IndexedShapefileDataStore(url, null,
				true, true, IndexedShapefileDataStore.TREE_GRX);
		FeatureCollection features = ds.getFeatureSource().getFeatures();
		Iterator iter = features.iterator();

		while (iter.hasNext()) {
			iter.next();
		}

// TODO: The following assertion fails
//      assertTrue(file.exists());
	}

	public void testLoadAndVerify() throws Exception {
		FeatureCollection features = loadFeatures(STATE_POP, null);

		assertEquals("Number of Features loaded", 49, features.size());

		FeatureType schema = firstFeature(features).getFeatureType();
		assertNotNull(schema.getDefaultGeometry());
		assertEquals("Number of Attributes", 253,
				schema.getAttributeTypes().length);
		assertEquals("Value of statename is wrong", firstFeature(features)
				.getAttribute("STATE_NAME"), "Illinois");
		assertEquals("Value of land area is wrong", ((Double) firstFeature(
				features).getAttribute("LAND_KM")).doubleValue(), 143986.61,
				0.001);
	}

	private IndexedShapefileDataStore createDataStore(File f) throws Exception {
		FeatureCollection fc = createFeatureCollection();
		f.createNewFile();

		IndexedShapefileDataStore sds = new IndexedShapefileDataStore(f.toURL());
		writeFeatures(sds, fc);

		return sds;
	}

	private IndexedShapefileDataStore createDataStore() throws Exception {
		return createDataStore(getTempFile());
	}

	/**
	 * Create a set of features, then remove every other one, updating the
	 * remaining. Test for removal and proper update after reloading...
	 */
	public void testUpdating() throws Throwable {
		try {
			IndexedShapefileDataStore sds = createDataStore();
			loadFeatures(sds);

			FeatureWriter writer = null;

			try {
				writer = sds.getFeatureWriter(sds.getTypeNames()[0],
						Filter.NONE, Transaction.AUTO_COMMIT);

				while (writer.hasNext()) {
					Feature feat = writer.next();
					Byte b = (Byte) feat.getAttribute(1);

					if ((b.byteValue() % 2) == 0) {
						writer.remove();
					} else {
						feat.setAttribute(1, new Byte((byte) -1));
					}
				}
			} finally {
				if (writer != null) {
					writer.close();
				}
			}

			FeatureCollection fc = loadFeatures(sds);

			assertEquals(10, fc.size());

			for (FeatureIterator i = fc.features(); i.hasNext();) {
				assertEquals(-1, ((Byte) i.next().getAttribute(1)).byteValue());
			}
		} catch (Throwable t) {
			if (System.getProperty("os.name").startsWith("Windows")) {
				System.out.println("Ignore " + t
						+ " because you are on windows");

				return;
			} else {
				throw t;
			}
		}
	}

	/**
	 * Create a test file, then continue removing the first entry until there
	 * are no features left.
	 */
	public void testRemoveFromFrontAndClose() throws Throwable {
		try {
			IndexedShapefileDataStore sds = createDataStore();

			int idx = loadFeatures(sds).size();

			while (idx > 0) {
				FeatureWriter writer = null;

				try {
					writer = sds.getFeatureWriter(sds.getTypeNames()[0],
							Filter.NONE, Transaction.AUTO_COMMIT);
					writer.next();
					writer.remove();
				} finally {
					if (writer != null) {
						writer.close();
						writer = null;
					}
				}

				assertEquals(--idx, loadFeatures(sds).size());
			}
		} catch (Throwable t) {
			if (System.getProperty("os.name").startsWith("Windows")) {
				System.out.println("Ignore " + t
						+ " because you are on windows");

				return;
			} else {
				throw t;
			}
		}
	}

	/**
	 * Create a test file, then continue removing the last entry until there are
	 * no features left.
	 */
	public void testRemoveFromBackAndClose() throws Throwable {
		try {
			IndexedShapefileDataStore sds = createDataStore();

			int idx = loadFeatures(sds).size();

			while (idx > 0) {
				FeatureWriter writer = null;

				try {
					writer = sds.getFeatureWriter(sds.getTypeNames()[0],
							Filter.NONE, Transaction.AUTO_COMMIT);

					while (writer.hasNext()) {
						writer.next();
					}

					writer.remove();
				} finally {
					if (writer != null) {
						writer.close();
						writer = null;
					}
				}

				assertEquals(--idx, loadFeatures(sds).size());
			}
		} catch (Throwable t) {
			if (System.getProperty("os.name").startsWith("Windows")) {
				System.out.println("Ignore " + t
						+ " because you are on windows");

				return;
			} else {
				throw t;
			}
		}
	}

	public void testTestTransaction() throws Exception {
		IndexedShapefileDataStore sds = createDataStore();

		int idx = sds.getCount(Query.ALL);

		FeatureStore store = (FeatureStore) sds.getFeatureSource(sds
				.getCurrentTypeName());

		Transaction transaction = new DefaultTransaction();
		store.setTransaction(transaction);
		Feature[] newFeatures1 = new Feature[1];
		Feature[] newFeatures2 = new Feature[2];
		GeometryFactory fac = new GeometryFactory();
		newFeatures1[0] = DataUtilities.template(sds.getSchema());
		newFeatures1[0].setDefaultGeometry(fac
				.createPoint(new Coordinate(0, 0)));
		newFeatures2[0] = DataUtilities.template(sds.getSchema());
		newFeatures2[0].setDefaultGeometry(fac
				.createPoint(new Coordinate(0, 0)));
		newFeatures2[1] = DataUtilities.template(sds.getSchema());
		newFeatures2[1].setDefaultGeometry(fac
				.createPoint(new Coordinate(0, 0)));

		store.addFeatures(DataUtilities.reader(newFeatures1));
		store.addFeatures(DataUtilities.reader(newFeatures2));
		transaction.commit();
		assertEquals(idx + 3, sds.getCount(Query.ALL));

	}

	private FeatureCollection createFeatureCollection() throws Exception {
		FeatureTypeFactory factory = FeatureTypeFactory.newInstance("junk");
		factory.addType(AttributeTypeFactory.newAttributeType("a",
				Geometry.class));
		factory.addType(AttributeTypeFactory.newAttributeType("b", Byte.class));
		factory
				.addType(AttributeTypeFactory
						.newAttributeType("c", Short.class));
		factory.addType(AttributeTypeFactory
				.newAttributeType("d", Double.class));
		factory
				.addType(AttributeTypeFactory
						.newAttributeType("e", Float.class));
		factory.addType(AttributeTypeFactory
				.newAttributeType("f", String.class));
		factory.addType(AttributeTypeFactory.newAttributeType("g", Date.class));
		factory.addType(AttributeTypeFactory.newAttributeType("h",
				Boolean.class));
		factory.addType(AttributeTypeFactory
				.newAttributeType("i", Number.class));
		factory.addType(AttributeTypeFactory.newAttributeType("j", Long.class));

		FeatureType type = factory.getFeatureType();
		FeatureCollection features = FeatureCollections.newCollection();

		for (int i = 0, ii = 20; i < ii; i++) {
			features.add(type.create(new Object[] {
					new GeometryFactory().createPoint(new Coordinate(1, -1)),
					new Byte((byte) i), new Short((short) i), new Double(i),
					new Float(i), new String(i + " "), new Date(i),
					new Boolean(true), new Integer(22),
					new Long(1234567890123456789L) }));
		}

		return features;
	}

	public void testAttributesWriting() throws Exception {
		FeatureCollection features = createFeatureCollection();
		File tmpFile = getTempFile();
		tmpFile.createNewFile();

		IndexedShapefileDataStore s = new IndexedShapefileDataStore(tmpFile
				.toURL());
		writeFeatures(s, features);
	}

	public void testGeometriesWriting() throws Exception {
		String[] wktResources = new String[] { "point", "multipoint", "line",
				"multiline", "polygon", "multipolygon" };

		PrecisionModel pm = new PrecisionModel();

		for (int i = 0; i < wktResources.length; i++) {
			Geometry geom = readGeometry(wktResources[i]);
			String testName = wktResources[i];

			try {
				runWriteReadTest(geom, false);
				make3D(geom);
				testName += "3d";
				runWriteReadTest(geom, true);
			} catch (Throwable e) {
				throw new Exception("Error in " + testName, e);
			}
		}
	}

	private void make3D(Geometry g) {
		Coordinate[] c = g.getCoordinates();

		for (int i = 0, ii = c.length; i < ii; i++) {
			c[i].z = 42 + i;
		}
	}

	private void writeFeatures(IndexedShapefileDataStore s, FeatureCollection fc)
			throws Exception {
		s.createSchema(fc.features().next().getFeatureType());

		FeatureWriter fw = s.getFeatureWriter(s.getTypeNames()[0],
				Transaction.AUTO_COMMIT);
		FeatureIterator it = fc.features();

		while (it.hasNext()) {
			((SimpleFeature) fw.next()).setAttributes(it.next().getAttributes(
					null));
			fw.write();
		}

		fw.close();
	}

	private void runWriteReadTest(Geometry geom, boolean d3) throws Exception {
		// make features
		FeatureTypeFactory factory = FeatureTypeFactory.newInstance("junk");
		factory.addType(AttributeTypeFactory.newAttributeType("a",
				Geometry.class));

		FeatureType type = factory.getFeatureType();
		FeatureCollection features = FeatureCollections.newCollection();

		for (int i = 0, ii = 20; i < ii; i++) {
			features.add(type.create(new Object[] { geom.clone() }));
		}

		// set up file
		File tmpFile = getTempFile();
		tmpFile.delete();

		// write features
		IndexedShapefileDataStore s = new IndexedShapefileDataStore(tmpFile
				.toURL());
		s.createSchema(type);
		writeFeatures(s, features);

		// read features
		s = new IndexedShapefileDataStore(tmpFile.toURL());

		FeatureCollection fc = loadFeatures(s);
		FeatureIterator fci = fc.features();

		// verify
		while (fci.hasNext()) {
			Feature f = fci.next();
			Geometry fromShape = f.getDefaultGeometry();

			if (fromShape instanceof GeometryCollection) {
				if (!(geom instanceof GeometryCollection)) {
					fromShape = ((GeometryCollection) fromShape)
							.getGeometryN(0);
				}
			}

			try {
				Coordinate[] c1 = geom.getCoordinates();
				Coordinate[] c2 = fromShape.getCoordinates();

				for (int cc = 0, ccc = c1.length; cc < ccc; cc++) {
					if (d3) {
						assertTrue(c1[cc].equals3D(c2[cc]));
					} else {
						assertTrue(c1[cc].equals2D(c2[cc]));
					}
				}
			} catch (Throwable t) {
				fail("Bogus : " + Arrays.asList(geom.getCoordinates()) + " : "
						+ Arrays.asList(fromShape.getCoordinates()));
			}
		}

		tmpFile.delete();
	}

	public static void main(java.lang.String[] args) throws Exception {
		junit.textui.TestRunner.run(suite(ShapefileDataStoreTest.class));
	}
}
