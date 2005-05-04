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
package org.geotools.renderer.shape;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.renderer.lite.LiteRenderer2;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
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
	
	private static boolean DISPLAY = false;

	private static boolean RUN_SHAPE = true;

	private static boolean RUN_LITE = false;

	private static boolean ACCURATE = false;

	private static boolean CACHING = false;

	private static boolean NO_REPROJECTION = true;
	
	private static boolean FILTER = false;
	
	private static boolean CPU_PROFILE= false;

	private String testName="all_accurate_no_reproject_no_copy";

	static Style createTestStyle() throws Exception {
		StyleFactory sFac = StyleFactory.createStyleFactory();
		// The following is complex, and should be built from

		LineSymbolizer linesym = sFac.createLineSymbolizer();
		Stroke myStroke = sFac.getDefaultStroke();
		myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
		myStroke
				.setWidth(filterFactory.createLiteralExpression(new Integer(5)));
		linesym.setStroke(myStroke);

		Rule rule2 = sFac.createRule();
		rule2.setSymbolizers(new Symbolizer[] { linesym });
		if( FILTER ){
			ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
			ShapefileDataStore store = (ShapefileDataStore) fac
			.createDataStore(new URL(
					"file:///home/jones/aData/bc_roads.shp"));
			AttributeExpression exp=filterFactory.createAttributeExpression(store.getSchema(), "STREET");
			CompareFilter filter=filterFactory.createCompareFilter(Filter.COMPARE_NOT_EQUALS);
			filter.addLeftValue(exp);
			filter.addRightValue(filterFactory.createLiteralExpression("blah"));
			rule2.setFilter(filter);
		}
		FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
		fts2.setRules(new Rule[] { rule2 });
		fts2.setFeatureTypeName("bc_roads");

		Style style = sFac.createStyle();
		style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts2 });

		return style;
	}

	public static void main(String[] args) throws Exception {
		
		Timing t=new Timing();
		if (RUN_SHAPE)
			t.runShapeRendererTest();

		if (DISPLAY)
			Thread.sleep(3000);

		if (RUN_LITE)
			t.runLiteRendererTest();
		if (DISPLAY)
			Thread.sleep(3000);
	}

	private void runShapeRendererTest() throws Exception {
		ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
		ShapefileDataStore store = (ShapefileDataStore) fac
				.createDataStore(new URL(
						"file:///home/jones/aData/bc_roads.shp"));
		DefaultMapContext context = new DefaultMapContext();
		context.addLayer(store.getFeatureSource(), createTestStyle());
		if( NO_REPROJECTION )
			context.setAreaOfInterest(new Envelope(), store.getSchema().getDefaultGeometry().getCoordinateSystem());
		ShapeRenderer renderer = new ShapeRenderer(context);
		int w = 1000, h = 1000;
		final BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);

		if (CACHING)
			renderer.setCaching(true);

		Envelope bounds = context.getLayerBounds();
		if( !ALL_DATA)
		bounds = new Envelope(bounds.getMinX() + bounds.getWidth() / 4, bounds
				.getMaxX()
				- bounds.getWidth() / 4, bounds.getMinY() + bounds.getHeight()
				/ 4, bounds.getMaxY() - bounds.getHeight() / 4);
		
			
		if (ACCURATE)
			renderer.paint(g, new Rectangle(w, h), bounds);
		long start = System.currentTimeMillis();

		Controller controller=null;
		if( CPU_PROFILE ){
			controller=new Controller();
			controller.startCPUSampling();
		}

		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			renderer.paint(g, new Rectangle(w, h), bounds);
			renderer.paint(g, new Rectangle(w, h), bounds);
		}
		if( CPU_PROFILE ){
			controller.captureCPUSnapshot("shape_"+testName, false);
		}
		long end = System.currentTimeMillis();
		if (ACCURATE) 
			System.out.println("shape time=" + (end - start) / 3);
		else 
			System.out.println("shape time=" + (end - start));
		if (DISPLAY)
			display("shape", image, w, h);

	}

	private void runLiteRendererTest() throws Exception {
		ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
		ShapefileDataStore store = (ShapefileDataStore) fac
				.createDataStore(new URL(
						"file:///home/jones/aData/bc_roads.shp"));
		DefaultMapContext context = new DefaultMapContext();
		context.addLayer(store.getFeatureSource(), createTestStyle());
		
		if( NO_REPROJECTION )
			context.setAreaOfInterest(new Envelope(), store.getSchema().getDefaultGeometry().getCoordinateSystem());
		
		LiteRenderer2 renderer = new LiteRenderer2(context);
		renderer.setOptimizedDataLoadingEnabled(true);

		if (CACHING)
			renderer.setMemoryPreloadingEnabled(true);

		int w = 1000, h = 1000;
		final BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		Envelope bounds = context.getLayerBounds();

		if( !ALL_DATA)
			bounds = new Envelope(bounds.getMinX() + bounds.getWidth() / 4, bounds
				.getMaxX()
				- bounds.getWidth() / 4, bounds.getMinY() + bounds.getHeight()
				/ 4, bounds.getMaxY() - bounds.getHeight() / 4);

		if (ACCURATE)
			renderer.paint(g, new Rectangle(w, h), bounds);
		long start = System.currentTimeMillis();
		Controller controller=null;
		if( CPU_PROFILE ){
			controller=new Controller();
			controller.startCPUSampling();
		}
		renderer.paint(g, new Rectangle(w, h), bounds);
		if (ACCURATE) {
			renderer.paint(g, new Rectangle(w, h), bounds);
			renderer.paint(g, new Rectangle(w, h), bounds);
		}		
		if( CPU_PROFILE ){
			controller.captureCPUSnapshot("lite_"+testName, false);
		}

		long end = System.currentTimeMillis();
		if (ACCURATE) 
			System.out.println("lite time=" + (end - start) / 3);
		else 
			System.out.println("lite time=" + (end - start));
		if (DISPLAY)
			display("lite", image, w, h);
	}

	public static void display(String testName, final BufferedImage image,
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
	}
}
