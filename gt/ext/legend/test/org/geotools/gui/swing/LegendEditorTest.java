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
package org.geotools.gui.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.File;

import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.gui.swing.sldeditor.style.StyleEditorChooser;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.map.DefaultMapLayer;
import org.geotools.renderer.LegendIconMaker;
import org.geotools.resources.TestData;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


/**
 *
 * @source $URL$
 * @version $Id$
 * @author wolf
 */
public class LegendEditorTest extends TestCase {
	StyleBuilder sb;

	protected void setUp() throws Exception {
		super.setUp();

		sb = new StyleBuilder(); // defaultness!
	}
    /**
     * {@code true} for enabling {@code println} statements. By default {@code true}
     * when running from the command line, and {@code false} when running by Maven.
     */
    private static boolean verbose;

    /**
     * The context which contains this maps data
     */
    public LegendEditorTest(String testName) {
        super(testName);
    }
    
    public void testLegend() throws Exception {
        SLDParser sld = null;
        
        File sldFile = TestData.file(this, "color.sld");
        sld = new SLDParser(StyleFactoryFinder.createStyleFactory(), sldFile);
        
        Style[] styles = sld.readXML();
        if (verbose) {
            System.out.println("Style loaded");
        }
        long start = System.currentTimeMillis();
        StyleEditorChooser sec = new StyleEditorChooser(null, styles[0]);
        
        if (verbose) {
            System.out.println("Style editor created in " + (System.currentTimeMillis() - start));
        }    
        // Create frame
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        frame.setContentPane(sec);

        frame.pack();
        frame.setVisible(true);
        Thread.currentThread().sleep(500);
        frame.dispose();
    }
    
    public static void main(java.lang.String[] args) {
        verbose = true;
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new junit.framework.TestSuite(LegendEditorTest.class);
    }


	/**
	 * Bug report on geotools-user list...apparently using any style with
	 * attribute expression will kill the legend.
	 * <p>
	 * TODO: Jira number
	 * TODO: try this style with legend (apparently it will break)
	 */
	public void testInteraction() throws Exception  {
		Style style = buildStyle();
		assertNotNull( style );
	}

	protected void ajoutePointsLayer() throws Exception {
		StyleBuilder builder = sb;

		AttributeType[] typepoint = new AttributeType[4];
		typepoint[0] = AttributeTypeFactory.newAttributeType("the_geomln",
				Point.class);
		typepoint[1] = AttributeTypeFactory.newAttributeType("nompt",
				String.class);
		typepoint[2] = AttributeTypeFactory.newAttributeType("size",
				Float.class);
		typepoint[3] = AttributeTypeFactory.newAttributeType("rotation",
				Float.class);
		FeatureType ftPoint = FeatureTypeFactory.newFeatureType(typepoint,
				"points");
		GeometryFactory geomf = new GeometryFactory();
		Point geometry = geomf.createPoint(new Coordinate(35.0, -12.0));
		String nom = ("Label");
		Float size = new Float(20.);
		Float rotat = new Float(1.);
		Feature ftpt1 = ftPoint.create(new Object[] { geometry, nom, size,
				rotat }, "pt1");
		FeatureCollection fc = FeatureCollections.newCollection();
		fc.add(ftpt1);
		final DataStore store = new MemoryDataStore(fc);
		final FeatureSource features = store.getFeatureSource(store
				.getTypeNames()[0]);

		final Style style = buildStyle();
		final DefaultMapLayer layer = new DefaultMapLayer(features, style);
		layer.setTitle("Points");

		System.out.println("ajoute layer");
	}

	private Expression attributeExpression(String attributeName,
			FilterFactory ff) throws org.geotools.filter.IllegalFilterException {
		         AttributeExpression attribute = ff
				.createAttributeExpression(null, attributeName);
		return attribute;
	}

	private Style buildStyle() throws Exception {
		FilterFactory ff = sb.getFilterFactory();
		Style style = sb.createStyle();
		style.setName("MyStyle");
		Mark testMark = sb.createMark(StyleBuilder.MARK_STAR, // sb.attributeExpression("name"),
				sb.createFill(Color.BLUE, 0.5), null);
		Graphic graph = sb.createGraphic(null, new Mark[] { testMark }, null,
				sb.literalExpression(1), sb.attributeExpression("size"), // sb.literalExpression(10.),
																			// attributeExpression("size",ff),
				sb.attributeExpression("rotation")); // attributeExpression("rotation",ff));
														// sb.literalExpression(0.));
		/*
		 * Graphic graph = builder.createGraphic(null //extn graphic , testMark ,
		 * null //Symbol , 0.5 // opacity 0.0 transparent 1.0 completement
		 * opaque , 10. //size , 0 // rotation );
		 */style.addFeatureTypeStyle(sb.createFeatureTypeStyle(
				"testPoint",
				new Symbolizer[] { sb.createPointSymbolizer(graph) }));
		AnchorPoint anchorPoint = sb.createAnchorPoint(0., 0.);
		PointPlacement pointPlacement = sb.createPointPlacement(anchorPoint,
				null, sb.literalExpression(0));
		TextSymbolizer textSymbolizer = sb.createTextSymbolizer(sb
				.createFill(Color.BLACK), new Font[] {
				sb.createFont("Lucida Sans", 10), sb.createFont("Arial", 10) },
				sb.createHalo(), sb.attributeExpression("nompt"), // attributeExpression("nompt",ff),
				pointPlacement, null);
		Mark circle = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.RED);
		Graphic graph2 = sb.createGraphic(null, circle, null, 1, 4, 0);
		PointSymbolizer pointSymbolizer = sb.createPointSymbolizer(graph2);
		style.addFeatureTypeStyle(sb.createFeatureTypeStyle("labelPoint",
				new Symbolizer[] { pointSymbolizer })); // textSymbolizer,
		return style;
	}
}
