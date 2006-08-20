package org.geotools.renderer.lite;

import junit.framework.TestCase;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class RenderUtilitiesTest extends TestCase {

	public void testScaleOutsideCrsDefinition() throws Exception {
		CoordinateReferenceSystem utm1N = CRS.decode("EPSG:32601");
		ReferencedEnvelope re = new ReferencedEnvelope(new Envelope(0, 0, 100,
				100), utm1N);
		try {
			RendererUtilities.calculateScale(re, 100, 100, 75);
			fail("Should have failed, envelope outside of the source crs validity area");
		} catch (IllegalArgumentException e) {
			// ok
		}
	}

	public void testScaleProjected() throws Exception {
		CoordinateReferenceSystem utm1N = CRS.decode("EPSG:32601");
		// valid coords for utm nord 1 start from (200000, 0)
		ReferencedEnvelope re = new ReferencedEnvelope(new Envelope(200000, 200100,
				0, 100), utm1N);
		double scale = RendererUtilities.calculateScale(re, 100, 100, 2.54);
		assertEquals(100.0, scale, 0.1); // account for projection deformation
	}
	
	public void testScaleCartesian() throws Exception {
		ReferencedEnvelope re = new ReferencedEnvelope(new Envelope(0, 10,
				0, 10), DefaultEngineeringCRS.CARTESIAN_2D);
		double scale = RendererUtilities.calculateScale(re, 10 * 100, 10 * 100, 2.54);
		assertEquals(1.0, scale, 0.00001); // no projection deformation here!
	}
	
	public void testScaleGeneric() throws Exception {
		ReferencedEnvelope re = new ReferencedEnvelope(new Envelope(0, 10,
				0, 10), DefaultEngineeringCRS.GENERIC_2D);
		double scale = RendererUtilities.calculateScale(re, 10 * 100, 10 * 100, 2.54);
		assertEquals(1.0, scale, 0.00001); // no projection deformation here!
	}
}
