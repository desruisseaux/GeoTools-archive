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
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.LiteRenderer2;
import org.geotools.resources.TestData;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Envelope;
import com.yourkit.api.Controller;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class Timing {

	private static final FilterFactory filterFactory = FilterFactory
			.createFilterFactory();

	private static boolean ALL_DATA = true;

	private static boolean DISPLAY = true;

	private static boolean ANTI_ALIASING = true;

	private static boolean RUN_SHAPE = true;

	private static boolean RUN_LITE = false;

	private static boolean RUN_TINY = false;

	private static boolean ACCURATE = false;

	private static boolean CACHING = false;

	private static boolean NO_REPROJECTION = false;

	private static boolean FILTER = false;

	private static boolean CPU_PROFILE = false;

	private static boolean LINES = false;

	private String testName;
	{
		testName = "";
		if (LINES) {
			testName += "LINES";
		} else {
			testName += "POLYGONS";
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
	}

	final static FileWriter out;
	static {
		FileWriter tmp;
		try {
			tmp = new FileWriter("/home/jones/Desktop/TimingResults.txt", true);
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
		StyleFactory sFac = StyleFactory.createStyleFactory();
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
		StyleFactory sFac = StyleFactory.createStyleFactory();
		// The following is complex, and should be built from
		Stroke myStroke = sFac.getDefaultStroke();
		myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
		myStroke
				.setWidth(filterFactory.createLiteralExpression(new Integer(2)));
		Fill myFill= sFac.getDefaultFill();
		PolygonSymbolizer lineSym = sFac.createPolygonSymbolizer(myStroke,myFill,"the_geom");

		Rule rule2 = sFac.createRule();
		rule2.setSymbolizers(new Symbolizer[] { lineSym });
		if (FILTER) {
			ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
			ShapefileDataStore store = (ShapefileDataStore) fac
					.createDataStore(new URL(LINES_FILE));
			AttributeExpression exp = filterFactory.createAttributeExpression(
					store.getSchema(), "WSG_CODE");
			CompareFilter filter = filterFactory
					.createCompareFilter(Filter.COMPARE_NOT_EQUALS);
			filter.addLeftValue(exp);
			filter.addRightValue(filterFactory.createLiteralExpression("blah"));
			rule2.setFilter(filter);
		}
		FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
		fts2.setRules(new Rule[] { rule2 });
		fts2.setFeatureTypeName(typeName);

		Style style = sFac.createStyle();
		style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts2 });

		return style;
	}

	public static void main(String[] args) throws Exception {

		Timing t = new Timing();
		if (RUN_SHAPE)
			t.runShapeRendererTest();

		if (RUN_TINY)
			t.runTinyTest();

		if (RUN_LITE)
			t.runLiteRendererTest();

		if (out != null && !DISPLAY && !CPU_PROFILE)
			out.close();
	}

	private void runShapeRendererTest() throws Exception {
		MapContext context = getMapContext();
		ShapeRenderer renderer = new ShapeRenderer(context);

		if (ANTI_ALIASING)
			renderer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		final BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);

		Frame display = null;
		if (DISPLAY)
			display=display("shape", image, w, h);

		Graphics2D g = image.createGraphics();
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

		Controller controller = null;
		if (CPU_PROFILE) {
			controller = new Controller();
			controller.startCPUSampling();
		}

		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			renderer.paint(g, new Rectangle(w, h), bounds);
			renderer.paint(g, new Rectangle(w, h), bounds);
		}
		if (CPU_PROFILE) {
			controller.captureCPUSnapshot("shape_" + testName, false);
		}
		long end = System.currentTimeMillis();
		if (ACCURATE){
			if (out != null)
				out
						.append("shape " + testName + "=" + (end - start) / 3
								+ "\n");
		}else if (out != null)
				out.append("shape " + testName + "=" + (end - start) + "\n");
		if( display!=null )
		display.repaint();
	}

	private MapContext getMapContext() throws Exception {
		ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
		ShapefileDataStore store = (ShapefileDataStore) fac
				.createDataStore(new URL(LINES ? LINES_FILE : POLY_FILE));
		DefaultMapContext context = new DefaultMapContext();
		context.addLayer(store.getFeatureSource(), LINES ? createLineStyle()
				: createPolyStyle());
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
		ShapeRenderer renderer = new ShapeRenderer(context);
		if (ANTI_ALIASING)
			renderer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		final BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);

		Frame display = null;
		if (DISPLAY)
			display=display("shape", image, w, h);
		Graphics2D g = image.createGraphics();
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

		Controller controller = null;
		if (CPU_PROFILE) {
			controller = new Controller();
			controller.startCPUSampling();
		}

		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			renderer.paint(g, new Rectangle(w, h), bounds);
			renderer.paint(g, new Rectangle(w, h), bounds);
		}
		if (CPU_PROFILE) {
			controller.captureCPUSnapshot("shape_" + testName, false);
		}
		long end = System.currentTimeMillis();
		if (ACCURATE){
			out.append("shape " + testName + "=" + (end - start) / 3 + "\n");
		}else
			out.append("shape " + testName + "=" + (end - start) + "\n");
		if( display!=null)
			display.repaint();
	}

	private void runLiteRendererTest() throws Exception {

		MapContext context = getMapContext();
		LiteRenderer2 renderer = new LiteRenderer2(context);
		renderer.setOptimizedDataLoadingEnabled(true);
		if (ANTI_ALIASING)
			renderer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

		if (CACHING)
			renderer.setMemoryPreloadingEnabled(true);

		final BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);

		Frame display = null;
		if (DISPLAY)
			display=display("shape", image, w, h);
		
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		Envelope bounds = context.getLayerBounds();

		if (!ALL_DATA)
			bounds = new Envelope(bounds.getMinX() + bounds.getWidth() / 4,
					bounds.getMaxX() - bounds.getWidth() / 4, bounds.getMinY()
							+ bounds.getHeight() / 4, bounds.getMaxY()
							- bounds.getHeight() / 4);

		if (ACCURATE)
			renderer.paint(g, new Rectangle(w, h), bounds);
		long start = System.currentTimeMillis();
		Controller controller = null;
		if (CPU_PROFILE) {
			controller = new Controller();
			controller.startCPUSampling();
		}
		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			renderer.paint(g, new Rectangle(w, h), bounds);
			renderer.paint(g, new Rectangle(w, h), bounds);
		}
		if (CPU_PROFILE) {
			controller.captureCPUSnapshot("lite_" + testName, false);
		}

		long end = System.currentTimeMillis();
		if (ACCURATE){
			if (out != null)
				out.append("lite " + testName + "=" + (end - start) / 3 + "\n");
		}
			else if (out != null)
				out.append("lite " + testName + "=" + (end - start) + "\n");
			if( display!=null)
				display.repaint();
	}

	public static Frame display(String testName, final BufferedImage image,
			int w, int h) {
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


	private static String NEW_YORK_FILE="file:///home/jones/demo/nyct2000.shp";
	private static String NORTH_AMERICA_FILE="file:///home/jones/demo/north_america.shp";

	private static String NEW_YORK_NAME="nyct2000";
	private static String NORTH_AMERICA_NAME="north_america";
	
	private static String LINES_FILE = "file:///home/jones/aData/bc_roads.shp";

	private static String LINES_TYPE_NAME = "bc_roads";

	private static String POLY_FILE = NORTH_AMERICA_FILE;

	private static String POLY_TYPE_NAME = NORTH_AMERICA_NAME;

	int w = 512, h = 512;
}
