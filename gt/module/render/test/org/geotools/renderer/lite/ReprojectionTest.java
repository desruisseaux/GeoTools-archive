package org.geotools.renderer.lite;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import junit.framework.TestCase;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.RenderListener;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Tests for rendering and reprojection
 * 
 * @author wolf
 * 
 */
public class ReprojectionTest extends TestCase {

	private FeatureType pointFeautureType;

	private GeometryFactory gf = new GeometryFactory();

	protected int errors;

	protected void setUp() throws Exception {
		super.setUp();
		AttributeType[] attributes = new AttributeType[] { AttributeTypeFactory
				.newAttributeType("geom", LineString.class, false, 0, null,
						DefaultGeographicCRS.WGS84) };
		pointFeautureType = FeatureTypes.newFeatureType(attributes, "Lines");
	}

	public FeatureCollection createLineCollection() throws Exception {
		FeatureCollection fc = FeatureCollections.newCollection();
		fc.add(createLine(-177, 0, -177, 10));
		fc.add(createLine(-177, 0, -200, 0));
		fc.add(createLine(-177, 0, -177, 100));

		return fc;
	}

	private Feature createLine(double x1, double y1, double x2, double y2)
			throws IllegalAttributeException {
		Coordinate[] coords = new Coordinate[] { new Coordinate(x1, y1),
				new Coordinate(x2, y2) };
		return pointFeautureType.create(new Object[] { gf
				.createLineString(coords) });
	}

	private Style createLineStyle() {
		StyleBuilder sb = new StyleBuilder();
		return sb.createStyle(sb.createLineSymbolizer());
	}

	public void testSkipProjectionErrors() throws Exception {
		// build map context
		MapContext mapContext = new DefaultMapContext(
				DefaultGeographicCRS.WGS84);
		mapContext.addLayer(createLineCollection(), createLineStyle());

		// build projected envelope to work with (small one around the area of
		// validity of utm zone 1, which being a Gauss projection is a vertical 
		// slice parallel to the central meridian, -177°)
		ReferencedEnvelope reWgs = new ReferencedEnvelope(new Envelope(-180,
				-170, 20, 40), DefaultGeographicCRS.WGS84);
		System.out.println(reWgs);
		CoordinateReferenceSystem utm1N = CRS.decode("EPSG:32601");
		System.out.println(utm1N);
		ReferencedEnvelope reUtm = reWgs.transform(utm1N, true);

		BufferedImage image = new BufferedImage(200, 200,
				BufferedImage.TYPE_4BYTE_ABGR);

		// setup the renderer and listen for errors
		StreamingRenderer sr = new StreamingRenderer();
		sr.setContext(mapContext);
		sr.addRenderListener(new RenderListener() {

			public void featureRenderer(Feature feature) {
			}

			public void errorOccurred(Exception e) {
				errors++;
			}

		});
		errors = 0;
		sr.paint((Graphics2D) image.getGraphics(), new Rectangle(200, 200),
				reUtm);
		// we should get two errors since there are two features that cannot be
		// projected
		// but the renderer itself should not throw exceptions
		assertEquals(2, errors);
	}
}
