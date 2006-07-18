/**
 * 
 */
package org.geotools.coverage.grid;

import java.awt.image.RenderedImage;

import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderCopy;
import javax.media.jai.Interpolation;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.Operations;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author Simone Giannecchini
 * 
 */
public class ScaleTest extends GridCoverageTest {
	private static final boolean SHOW = true;

	/**
	 * The source grid coverage.
	 */
	private GridCoverage2D coverage;

	/**
	 * @param name
	 */
	public ScaleTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Run the suit from the command line.
	 */
	public static void main(final String[] args) {

		org.geotools.util.MonolineFormatter
				.initGeotools(AbstractProcessor.OPERATION);
		junit.textui.TestRunner.run(ScaleTest.class);
	}

	/**
	 * Set up common objects used for all tests.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		coverage = getExample(0);
	}

	public void testScale() {

		final AbstractProcessor processor = AbstractProcessor.getInstance();
		GridCoverage2D source = coverage.geophysics(true);
		final ParameterValueGroup param = processor.getOperation("Scale")
				.getParameters();
		param.parameter("Source").setValue(source);
		param.parameter("xScale").setValue(new Float(0.5));
		param.parameter("yScale").setValue(new Float(0.5));
		param.parameter("xTrans").setValue(new Float(0));
		param.parameter("yTrans").setValue(new Float(0));
		param.parameter("Interpolation").setValue(
				Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
		param.parameter("BorderExtender").setValue(
				BorderExtenderCopy.createInstance(BorderExtender.BORDER_COPY));
		GridCoverage2D scaled = (GridCoverage2D) processor.doOperation(param);
		assertNotNull(scaled.getRenderedImage().getData());
		final RenderedImage image = scaled.getRenderedImage();
		scaled = scaled.geophysics(false);
		String operation = null;
		if (image instanceof RenderedOp) {
			operation = ((RenderedOp) image).getOperationName();
			AbstractProcessor.LOGGER.fine("Applied \"" + operation
					+ "\" JAI operation.");
		}
		if (SHOW) {
			Viewer.show(coverage,coverage.getName().toString());
			Viewer.show(scaled,scaled.getName().toString());
		} else {
			// Force computation
			assertNotNull(coverage.getRenderedImage().getData());
			assertNotNull(scaled.getRenderedImage().getData());
		}

		final GridCoverage2D scaledGridCoverage = (GridCoverage2D) Operations.DEFAULT
				.scale(source, 10, 10, 0.0, 0.0, Interpolation
						.getInstance(Interpolation.INTERP_BILINEAR),
						BorderExtender
								.createInstance(BorderExtender.BORDER_COPY));
		if (SHOW) {
			Viewer.show(scaledGridCoverage,scaledGridCoverage.getName().toString());
		} else {
			// Force computation
			assertNotNull(scaledGridCoverage.getRenderedImage().getData());
		}


	}
}
