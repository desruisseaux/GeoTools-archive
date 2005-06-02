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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.geotools.data.shape.ShapeFileIndexer;
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
import org.geotools.styling.Font;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Envelope;

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

	private static boolean DISPLAY = false;

	private static boolean ANTI_ALIASING = true;

	private static boolean RUN_SHAPE = true;

	private static boolean RUN_LITE = false;

	private static boolean RUN_TINY = false;

	private static boolean ACCURATE = true;

	private static boolean CACHING = false;

	private static boolean NO_REPROJECTION = true;

	private static boolean FILTER = false;

	private static boolean CPU_PROFILE = false;

	private static boolean LINES = true;
	
	private static boolean LABELING=false;
	
	private static boolean RTREE=false;
	
	private static final boolean QUADTREE = false;

	private String testName;
	{
		testName = "";
		if (LINES) {
			testName += LINES_TYPE_NAME;
		} else {
			testName += POLY_TYPE_NAME;
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
		if( QUADTREE ){
			testName += "_QUADTREE";
		}
		if( RTREE ){
			testName += "_RTREE";
		}
	}

	public final static FileWriter out;

    
	static {
		FileWriter tmp;
		try {
            String homePath = System.getProperty("user.home");
            File home = new File( homePath );
            File results = new File( homePath, "TimingResults.txt"  );
			tmp = new FileWriter( results , true);
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
		if (LABELING) {
			TextSymbolizer textsym=sFac.createTextSymbolizer();
			textsym.setFill(sFac.getDefaultFill());
			textsym.setGeometryPropertyName("the_geom");
			textsym.setLabel(filterFactory.createLiteralExpression(LINES_LABEL));
			textsym.setFonts(new Font[]{sFac.getDefaultFont()});
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
			TextSymbolizer textsym=sFac.createTextSymbolizer();
			textsym.setFill(sFac.createFill(filterFactory.createLiteralExpression("#000000")));
			textsym.setGeometryPropertyName("the_geom");
			ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
			ShapefileDataStore store = (ShapefileDataStore) fac
					.createDataStore(new URL(POLY_FILE));
			textsym.setLabel(filterFactory.createAttributeExpression(
					store.getSchema(), POLY_LABEL));
			textsym.setFonts(new Font[]{sFac.getDefaultFont()});
			rule2.setSymbolizers(new Symbolizer[] { lineSym, textsym });
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
		ShapefileRenderer renderer = new ShapefileRenderer(context);

		if (ANTI_ALIASING)
			renderer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		final BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);

		Frame display = null;

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


		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			renderer.paint(g, new Rectangle(w, h), bounds);
			renderer.paint(g, new Rectangle(w, h), bounds);
		}

		long end = System.currentTimeMillis();
		if (ACCURATE){
			if (out != null) {
                out.write("shape " + testName + "=" + (end - start) / 3 + "\n");
            }
		}
        else if (out != null) {
            out.write("shape " + testName + "=" + (end - start) + "\n");
        }

		if (DISPLAY)
			display=display("shape", image, w, h);
	}

	private MapContext getMapContext() throws Exception {
		URL url=new URL(LINES ? LINES_FILE : POLY_FILE);
		ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
		ShapefileDataStore store = (ShapefileDataStore) fac
				.createDataStore(url);
		
		if( QUADTREE && RTREE ){
			throw new Exception( "QUADTREE and RTREE are both true");
		}
		if( !QUADTREE ){			
			String s=url.getPath();
			s=s.substring(0, s.lastIndexOf( "."));
			File file = new File( s+".gix");
			if( file.exists() ){
				file.delete();
			}
		}		
		if( !RTREE ){
			String s=url.getPath();
			s=s.substring(0, s.lastIndexOf("."));
			File file = new File( s+".grx");
			if( file.exists() ){
				file.delete();
			}
		}
		if( RTREE ){
			String s=url.getPath();
			s=s.substring(0, s.lastIndexOf("."));
			File file=new File(s+".shx");
			if( !file.exists() )
				throw new IOException( "No shx file" );

			
			ShapeFileIndexer indexer=new ShapeFileIndexer();
			indexer.setIdxType(ShapeFileIndexer.RTREE);
			indexer.setShapeFileName(url.getPath());
			indexer.index(true);
		}		
		if( QUADTREE ){
			String s=url.getPath();
			s=s.substring(0, s.lastIndexOf("."));
			File file=new File(s+".shx");
			if( !file.exists() )
				throw new IOException( "No shx file" );

			
			ShapeFileIndexer indexer=new ShapeFileIndexer();
			indexer.setIdxType(ShapeFileIndexer.QUADTREE);
			indexer.setShapeFileName(url.getPath());
			indexer.index(true);
		}
		
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
		ShapefileRenderer renderer = new ShapefileRenderer(context);
		if (ANTI_ALIASING)
			renderer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		final BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);

		Frame display = null;
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


		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			renderer.paint(g, new Rectangle(w, h), bounds);
			renderer.paint(g, new Rectangle(w, h), bounds);
		}

		long end = System.currentTimeMillis();
		if (ACCURATE){
			out.write("tiny " + testName + "=" + (end - start) / 3 + "\n");
		}else
			out.write("tiny " + testName + "=" + (end - start) + "\n");
		if (DISPLAY)
			display=display("tiny", image, w, h);
		

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

		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			renderer.paint(g, new Rectangle(w, h), bounds);
			renderer.paint(g, new Rectangle(w, h), bounds);
		}
		long end = System.currentTimeMillis();
		if (ACCURATE){
			if (out != null)
				out.write("lite " + testName + "=" + (end - start) / 3 + "\n");
		}
			else if (out != null)
				out.write("lite " + testName + "=" + (end - start) + "\n");
		if (DISPLAY)
			display=display("lite", image, w, h);
		
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


	private static String NEW_YORK_HOME_FILE="file:///home/jones/demo/nyct2000.shp";

	private static String NEW_YORK_WORK_FILE="file:///home/jones/aData/nyct2000.shp";
	private static String NEW_YORK_WORK_NAME="nyct2000";
	private static String NEW_YORK_WORK_LABEL="CT2000";

	private static String WORLD_HOME_FILE="file:///home/jones/allShapefiles/cntry00.shp";
	private static String WORLD_HOME_NAME="cntry00";
	private static String WORLD_HOME_LABEL="CNTRY_NAME";

	private static String BC_FILE="file:///home/jones/aData/lwsg_prov.shp";
	private static String BC_NAME="lwsg_prov";
	
	private static String CIRCLES_FILE="file:///home/jones/aData/pt_circles2.shp";
	private static String CIRCLES_NAME="pt_circles2";
	
	private static String LINES_WORK_FILE = "file:///home/jones/aData/bc_roads.shp";
	private static String LINES_WORK_TYPE_NAME = "bc_roads";
	private static String LINES_WORK_LABEL = "STREET";	
	
	private static String LINES_HOME_FILE = "file:///home/jones/allShapefiles/tcn-roads.shp";
	private static String LINES_HOME_TYPE_NAME = "tcn-roads";
	private static String LINES_HOME_LABEL = "STREET";

	private static String LAKES_FILE="file:/home/jones/dev/geotools/ext/shaperenderer/test/org/geotools/renderer/shape/test-data/lakes.shp";
	private static String LAKES_NAME = "lakes";
	
	private static String LINES_FILE = LINES_WORK_FILE;
	private static String LINES_TYPE_NAME = LINES_WORK_TYPE_NAME;
	private static String LINES_LABEL = LINES_HOME_LABEL;	
	
	private static String POLY_FILE = BC_FILE;
	private static String POLY_TYPE_NAME = BC_NAME;
	private static String POLY_LABEL = NEW_YORK_WORK_LABEL;

	int w = 512, h = 512;
}
