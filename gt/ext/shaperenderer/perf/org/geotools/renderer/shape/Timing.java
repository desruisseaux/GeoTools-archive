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
 *    This library is distributed in the hryope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.renderer.shape;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.resources.TestData;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 * @source $URL$
 */
public class Timing {

	private static final FilterFactory filterFactory = new FilterFactoryImpl();

	private static final int POINTS = 0;

	private static final int LINES = 1;

	private static final int POLYGONS = 2;

	private static boolean ALL_DATA = true;

	private static boolean HW_ACCEL = true;

	private static boolean DISPLAY = false;

	private static boolean ANTI_ALIASING = false;

	private static boolean RUN_SHAPE = true;

	private static boolean RUN_LITE = true;

	private static boolean RUN_TINY = false;

	private static boolean ACCURATE = true;

	private static boolean CACHING = false;

	private static boolean NO_REPROJECTION = true;

	private static boolean FILTER = false;

	private static boolean CPU_PROFILE = false;

	private static int SHAPE_TYPE = POLYGONS;

	private static boolean LABELING = false;

	private static boolean RTREE = false;

	private static final boolean QUADTREE = false;

	private static final int CYCLES = 4;

	private String testName;
	{
		testName = "";
		if (SHAPE_TYPE == LINES) {
			testName += LINES_TYPE_NAME;
		} else if (SHAPE_TYPE == POLYGONS) {
			testName += POLY_TYPE_NAME;
		} else if (SHAPE_TYPE == POINTS) {
			testName += POINT_TYPE_NAME;
		}
		if (ALL_DATA) {
			testName += "_ALL";
		} else {
			testName += "_ZOOM";
		}
		if (ACCURATE) {
			testName += "_ACCURATE";
		} else {
			testName += "_INACCURATE";
		}
		if (CACHING) {
			testName += "_CACHING";
		}
		if (NO_REPROJECTION) {
			testName += "_NO_REPROJECTION";
		} else {
			testName += "_REPROJECTED";
		}
		if (FILTER) {
			testName += "_FILTER";
		} else {
			testName += "_NO_FILTER";
		}
		if (CPU_PROFILE) {
			testName += "_PROFILE";
		}
		if (QUADTREE) {
			testName += "_QUADTREE";
		}
		if (RTREE) {
			testName += "_RTREE";
		}

		if (HW_ACCEL)
			testName += "_HW_ACCEL";
		else
			testName += "_NO_HW_ACCEL";
	}

	public final static FileWriter out;

	static {
		FileWriter tmp;
		try {
			String homePath = System.getProperty("user.home");
			File results = new File(homePath, "TimingResults.txt");
			tmp = new FileWriter(results, true);
		} catch (IOException e) {
			tmp = null;
			e.printStackTrace();
		}
		out = tmp;
	}

	static Style createLineStyle() throws Exception {
		return createLineStyle(null);
	}

	static Style createLineStyle(String typeName) throws Exception {
		if (typeName == null)
			typeName = LINES_TYPE_NAME;
		StyleFactory sFac = new StyleFactoryImpl();
		// The following is complex, and should be built from

		LineSymbolizer linesym = sFac.createLineSymbolizer();
		Stroke myStroke = sFac.getDefaultStroke();
		myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
		myStroke
				.setWidth(filterFactory.createLiteralExpression(new Integer(2)));
		linesym.setStroke(myStroke);

		Rule rule2 = sFac.createRule();
		rule2.setSymbolizers(new Symbolizer[] { linesym });
		if (FILTER) {
			ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
			ShapefileDataStore store = (ShapefileDataStore) fac
					.createDataStore(new URL(LINES_FILE));
			AttributeExpression exp = filterFactory.createAttributeExpression(
					store.getSchema(), "STREETS");
			CompareFilter filter = filterFactory
					.createCompareFilter(Filter.COMPARE_NOT_EQUALS);
			filter.addLeftValue(exp);
			filter.addRightValue(filterFactory.createLiteralExpression("blah"));
			rule2.setFilter(filter);
		}
		if (LABELING) {
			StyleBuilder builder = new StyleBuilder();
			TextSymbolizer textsym = sFac.createTextSymbolizer();
			textsym.setFill(sFac.getDefaultFill());
			textsym.setGeometryPropertyName("the_geom");
			textsym
					.setLabel(filterFactory
							.createLiteralExpression(LINES_LABEL));
			textsym.setFonts(new Font[] { builder.createFont(new java.awt.Font(
					"Arial", java.awt.Font.PLAIN, 10)) });
			rule2.setSymbolizers(new Symbolizer[] { linesym, textsym });
		}
		FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
		fts2.setRules(new Rule[] { rule2 });
		fts2.setFeatureTypeName(typeName);

		Style style = sFac.createStyle();
		style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts2 });

		return style;
	}

	static Style createPolyStyle() throws Exception {
		return createPolyStyle(null);
	}

	static Style createPolyStyle(String typeName) throws Exception {
		if (typeName == null)
			typeName = POLY_TYPE_NAME;
		StyleFactory sFac =new StyleFactoryImpl();
		// The following is complex, and should be built from
		Stroke myStroke = sFac.getDefaultStroke();
		myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
		myStroke
				.setWidth(filterFactory.createLiteralExpression(new Integer(2)));
		Fill myFill = sFac.getDefaultFill();
		PolygonSymbolizer lineSym = sFac.createPolygonSymbolizer(myStroke,
				myFill, "the_geom");

		Rule rule2 = sFac.createRule();
		rule2.setSymbolizers(new Symbolizer[] { lineSym });
		if (FILTER) {
			ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
			ShapefileDataStore store = (ShapefileDataStore) fac
					.createDataStore(new URL(POLY_FILE));
			AttributeExpression exp = filterFactory.createAttributeExpression(
					store.getSchema(), POLY_LABEL);
			CompareFilter filter = filterFactory
					.createCompareFilter(Filter.COMPARE_NOT_EQUALS);
			filter.addLeftValue(exp);
			filter.addRightValue(filterFactory.createLiteralExpression("blah"));
			rule2.setFilter(filter);
		}
		if (LABELING) {
			StyleBuilder builder = new StyleBuilder();

			TextSymbolizer textsym = sFac.createTextSymbolizer();
			textsym.setFill(sFac.createFill(filterFactory
					.createLiteralExpression("#000000")));
			textsym.setGeometryPropertyName("the_geom");
			ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
			ShapefileDataStore store = (ShapefileDataStore) fac
					.createDataStore(new URL(POLY_FILE));
			textsym.setLabel(filterFactory.createAttributeExpression(store
					.getSchema(), POLY_LABEL));
			textsym.setFonts(new Font[] { builder.createFont(new java.awt.Font(
					"Arial", java.awt.Font.PLAIN, 10)) });
			rule2.setSymbolizers(new Symbolizer[] { lineSym, textsym });
		}

		FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
		fts2.setRules(new Rule[] { rule2 });
		fts2.setFeatureTypeName(typeName);

		Style style = sFac.createStyle();
		style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts2 });

		return style;
	}

	static Style createPointStyle() throws Exception {
		return createPointStyle(null);
	}

	static Style createPointStyle(String typeName) throws Exception {
		if (typeName == null)
			typeName = POINT_TYPE_NAME;
		StyleFactory sFac =  new StyleFactoryImpl();
		StyleBuilder builder = new StyleBuilder(sFac);
		// The following is complex, and should be built from
		Stroke myStroke = sFac.getDefaultStroke();
		myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
		myStroke
				.setWidth(filterFactory.createLiteralExpression(new Integer(2)));
		Fill myFill = sFac.getDefaultFill();
		PointSymbolizer point = sFac.createPointSymbolizer(builder
				.createGraphic(), "the_geom");

		Rule rule2 = sFac.createRule();
		rule2.setSymbolizers(new Symbolizer[] { point });
		if (FILTER) {
			ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
			ShapefileDataStore store = (ShapefileDataStore) fac
					.createDataStore(new URL(POINT_FILE));
			AttributeExpression exp = filterFactory.createAttributeExpression(
					store.getSchema(), POINT_LABEL);
			CompareFilter filter = filterFactory
					.createCompareFilter(Filter.COMPARE_NOT_EQUALS);
			filter.addLeftValue(exp);
			filter.addRightValue(filterFactory.createLiteralExpression("blah"));
			rule2.setFilter(filter);
		}
		if (LABELING) {

			TextSymbolizer textsym = sFac.createTextSymbolizer();
			textsym.setFill(sFac.createFill(filterFactory
					.createLiteralExpression("#000000")));
			textsym.setGeometryPropertyName("the_geom");
			ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
			ShapefileDataStore store = (ShapefileDataStore) fac
					.createDataStore(new URL(POINT_FILE));
			textsym.setLabel(filterFactory.createAttributeExpression(store
					.getSchema(), POINT_LABEL));
			textsym.setFonts(new Font[] { builder.createFont(new java.awt.Font(
					"Arial", java.awt.Font.PLAIN, 10)) });
			rule2.setSymbolizers(new Symbolizer[] { point, textsym });
		}

		FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
		fts2.setRules(new Rule[] { rule2 });
		fts2.setFeatureTypeName(typeName);

		Style style = sFac.createStyle();
		style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts2 });

		return style;
	}

	static Frame volatileImageSource;

	public static void main(String[] args) throws Exception {

		Timing t = new Timing();
		if (RUN_SHAPE)
			t.runShapeRendererTest();

		if (RUN_TINY)
			t.runTinyTest();



		if (out != null && !DISPLAY && !CPU_PROFILE)
			out.close();
		if (volatileImageSource != null)
			volatileImageSource.setVisible(false);
		System.exit(0);
	}

	private void runShapeRendererTest() throws Exception {
		MapContext context = getMapContext();
		ShapefileRenderer renderer = new ShapefileRenderer(context);

		if (ANTI_ALIASING)
			renderer.setJava2DHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON));

		Image image;
		Graphics2D g;

		image = createImage();
		g = createGraphics(image);

		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);

		if (CACHING)
			renderer.setCaching(true);

		Envelope bounds = context.getLayerBounds();
		if (!ALL_DATA)
			bounds = new Envelope(bounds.getMinX() + bounds.getWidth() / 4,
					bounds.getMaxX() - bounds.getWidth() / 4, bounds.getMinY()
							+ bounds.getHeight() / 4, bounds.getMaxY()
							- bounds.getHeight() / 4);

		if (ACCURATE)
			renderer.paint(g, new Rectangle(w, h), bounds);
		long start = System.currentTimeMillis();

		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			for (int i = 0; i < CYCLES; i++)
				renderer.paint(g, new Rectangle(w, h), bounds);
		}

		long end = System.currentTimeMillis();
		if (ACCURATE) {
			if (out != null) {
				out.write("shape " + testName + "=" + (end - start) / 3 + "\n");
			}
		} else if (out != null) {
			out.write("shape " + testName + "=" + (end - start) + "\n");
		}

		if (DISPLAY) {
			display("shape", image, w, h);
		}
	}

	private Graphics2D createGraphics(Image image) {
		Graphics2D g;
		if (image instanceof VolatileImage) {
			g = ((VolatileImage) image).createGraphics();
		} else {
			g = ((BufferedImage) image).createGraphics();
		}
		return g;
	}

	private Image createImage() {
		Image image;
		if (HW_ACCEL) {
			volatileImageSource = new Frame("");
			volatileImageSource.setUndecorated(true);
			volatileImageSource.setSize(10, 10);
			volatileImageSource.setVisible(true);
			image = volatileImageSource.getGraphicsConfiguration()
					.createCompatibleVolatileImage(w, h, 2);
			image.setAccelerationPriority(1.0f);
		} else {
			image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		}
		return image;
	}

	private MapContext getMapContext() throws Exception {
		URL url;
		if (SHAPE_TYPE == LINES) {
			url = new URL(LINES_FILE);
		} else if (SHAPE_TYPE == POLYGONS) {
			url = new URL(POLY_FILE);
		} else {
			url = new URL(POINT_FILE);
		}

		if (QUADTREE && RTREE) {
			throw new Exception("QUADTREE and RTREE are both true");
		}
		if (!QUADTREE) {
			String s = url.getPath();
			s = s.substring(0, s.lastIndexOf("."));
			File file = new File(s + ".qix");
			if (file.exists()) {
				file.delete();
			}
		}
		if (!RTREE) {
			String s = url.getPath();
			s = s.substring(0, s.lastIndexOf("."));
			File file = new File(s + ".grx");
			if (file.exists()) {
				file.delete();
			}
		}

		IndexedShapefileDataStoreFactory fac = new IndexedShapefileDataStoreFactory();
		IndexedShapefileDataStore store;
		Map params = new HashMap();
		params.put(IndexedShapefileDataStoreFactory.URLP.key, url);
		params.put(IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
				new Boolean(false));
		params.put(IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key,
				IndexedShapefileDataStoreFactory.TREE_NONE);
		if (QUADTREE) {
			params.put(
					IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
					new Boolean(true));
			params.put(IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key,
					IndexedShapefileDataStoreFactory.TREE_QIX);
			store = (IndexedShapefileDataStore) fac.createDataStore(params);
		}
		if (RTREE) {
			params.put(
					IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
					new Boolean(true));
			params.put(IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key,
					IndexedShapefileDataStoreFactory.TREE_GRX);
			store = (IndexedShapefileDataStore) fac.createDataStore(params);
		} else {
			store = (IndexedShapefileDataStore) fac.createDataStore(params);
		}

		DefaultMapContext context = new DefaultMapContext();
		if (SHAPE_TYPE == LINES)
			context.addLayer(store.getFeatureSource(), createLineStyle());
		else if (SHAPE_TYPE == POLYGONS)
			context.addLayer(store.getFeatureSource(), createPolyStyle());
		else
			context.addLayer(store.getFeatureSource(), createPointStyle());

		if (NO_REPROJECTION)
			context.setAreaOfInterest(new Envelope(), store.getSchema()
					.getDefaultGeometry().getCoordinateSystem());
		return context;
	}

	private void runTinyTest() throws Exception {
		ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
		ShapefileDataStore store = (ShapefileDataStore) fac
				.createDataStore(TestData.getResource(Timing.class,
						"theme1.shp"));
		DefaultMapContext context = new DefaultMapContext();
		context.addLayer(store.getFeatureSource(), createLineStyle("theme1"));
		if (NO_REPROJECTION)
			context.setAreaOfInterest(new Envelope(), store.getSchema()
					.getDefaultGeometry().getCoordinateSystem());
		ShapefileRenderer renderer = new ShapefileRenderer(context);
		if (ANTI_ALIASING)
			renderer.setJava2DHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON));
		Image image;
		Graphics2D g;

		image = createImage();
		g = createGraphics(image);

		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);

		if (CACHING)
			renderer.setCaching(true);

		Envelope bounds = new Envelope(-7.105552354197932, 8.20555235419793,
				-3.239388966356115, 4.191388966388683);

		if (!ALL_DATA)
			bounds = new Envelope(bounds.getMinX() + bounds.getWidth() / 4,
					bounds.getMaxX() - bounds.getWidth() / 4, bounds.getMinY()
							+ bounds.getHeight() / 4, bounds.getMaxY()
							- bounds.getHeight() / 4);

		if (ACCURATE)
			renderer.paint(g, new Rectangle(w, h), bounds);
		long start = System.currentTimeMillis();

		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			for (int i = 0; i < CYCLES; i++)
				renderer.paint(g, new Rectangle(w, h), bounds);
		}

		long end = System.currentTimeMillis();
		if (ACCURATE) {
			out.write("tiny " + testName + "=" + (end - start) / 3 + "\n");
		} else
			out.write("tiny " + testName + "=" + (end - start) + "\n");
		if (DISPLAY)
			display("tiny", image, w, h);

	}

	public static Frame display(String testName, final Image image, int w, int h) {
		Frame frame = new Frame(testName);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				e.getWindow().dispose();
			}
		});

		Panel p = new Panel() {
			/** <code>serialVersionUID</code> field */
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				g.drawImage(image, 0, 0, this);
			}
		};
		frame.add(p);
		frame.setSize(w, h);
		frame.setVisible(true);
		return frame;
	}

	private static String NEW_YORK_HOME_FILE = "file:///home/jones/demo/nyct2000.shp";

	private static String NEW_YORK_WORK_FILE = "file:///home/jones/aData/nyct2000.shp";

	private static String NEW_YORK_WORK_NAME = "nyct2000";

	private static String NEW_YORK_WORK_LABEL = "CT2000";

	private static String WORLD_HOME_FILE = "file:///home/jones/allShapefiles/cntry00.shp";

	private static String WORLD_HOME_NAME = "cntry00";

	private static String WORLD_HOME_LABEL = "CNTRY_NAME";

	private static String BC_FILE = "file:///home/jones/aData/lwsg_prov.shp";

	private static String BC_NAME = "lwsg_prov";

	private static String BC_WIN_FILE = "file:/L:\\uDigData\\countries.shp";

	private static String BC_WIN_NAME = "countries";

	private static String BC_WIN_LABEL = "CNTRY_NAME";

	private static String CIRCLES_FILE = "file:///home/jones/aData/pt_circles2.shp";

	private static String CIRCLES_NAME = "pt_circles2";

	private static String LINES_WORK_FILE = "file:///home/jones/data/uDigBundle/bc_roads.shp";

	private static String LINES_WORK_TYPE_NAME = "bc_roads";

	private static String LINES_WORK_LABEL = "STREET";

	private static String LINES_WIN_FILE = "file:/c:\\java\\uDigBundle\\bc_roads.shp";

	private static String LINES_HOME_FILE = "file:///home/jones/allShapefiles/tcn-roads.shp";

	private static String LINES_HOME_TYPE_NAME = "tcn-roads";

	private static String LINES_HOME_LABEL = "STREET";

	private static String LAKES_FILE = "file:/home/jones/dev/geotools/ext/shaperenderer/test/org/geotools/renderer/shape/test-data/lakes.shp";

	private static String LAKES_NAME = "lakes";

	private static String LINES_FILE = "file:/home/jones/data/other/hydrogl020.shp";

	private static String LINES_TYPE_NAME = "hydrogl020";

	private static String LINES_LABEL = LINES_WORK_LABEL;

	private static String POLY_FILE = "file:/home/jones/data/other/wwf_eco.shp";

	private static String POLY_TYPE_NAME = "wwf_eco";

	private static String POLY_LABEL = BC_WIN_LABEL;

	private static String POINT_FILE = "file:///home/jones/data/other/Join_0115.shp";

	private static String POINT_TYPE_NAME = "Join_0115";

	private static String POINT_LABEL = "test-points";

	int w = 512, h = 512;
}
