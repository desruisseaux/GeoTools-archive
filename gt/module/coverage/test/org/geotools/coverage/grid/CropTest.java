/**
 * 
 */
package org.geotools.coverage.grid;

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.util.logging.Logger;

import javax.media.jai.RenderedOp;

import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Giannecchini
 * 
 */
public class CropTest extends GridCoverageTest {
	private final static Logger LOGGER = Logger.getLogger(CropTest.class
			.toString());

	private static final boolean SHOW = true;

	private GridCoverage2D coverage;

	/**
	 * @param name
	 */
	public CropTest(String name) {
		super(name);

	}

	/**
	 * Set up common objects used for all tests.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		coverage = getExample(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		org.geotools.util.MonolineFormatter
				.initGeotools(AbstractProcessor.OPERATION);
		junit.textui.TestRunner.run(CropTest.class);

	}

	public void testCrop() throws InvalidGridGeometryException,
			TransformException {

		final AbstractProcessor processor = AbstractProcessor.getInstance();
		GridCoverage2D source = coverage.geophysics(true);
		final ParameterValueGroup param = processor
				.getOperation("CoverageCrop").getParameters();
		param.parameter("Source").setValue(source);
		param.parameter("Envelope").setValue(source.getEnvelope());

		GridCoverage2D cropped = (GridCoverage2D) processor.doOperation(param);
		assertNotNull(cropped.getRenderedImage().getData());
		final RenderedImage image = cropped.getRenderedImage();
		cropped = cropped.geophysics(false);
		String operation = null;
		if (image instanceof RenderedOp) {
			operation = ((RenderedOp) image).getOperationName();
			AbstractProcessor.LOGGER.fine("Applied \"" + operation
					+ "\" JAI operation.");
		}
		if (SHOW) {
			Viewer.show(coverage, coverage.getName().toString());
			Viewer.show(cropped, cropped.getName().toString());
		} else {
			// Force computation
			assertNotNull(coverage.getRenderedImage().getData());
		}

		final AffineTransform gridToWorld = new AffineTransform(
				(AffineTransform) coverage.gridGeometry
						.getGridToCoordinateSystem());
		gridToWorld.translate(-0.5, -0.5);
		final MathTransform worldToGrid = ProjectiveTransform.create(
				gridToWorld).inverse();
		final GeneralEnvelope sourceRange = CRSUtilities.transform(worldToGrid,
				source.getEnvelope2D());
		LOGGER.info(source.getEnvelope2D().toString());

		final GridGeometry2D gridG = new GridGeometry2D(new GeneralGridRange(
				sourceRange),
				coverage.gridGeometry.getGridToCoordinateSystem(), source
						.getCoordinateReferenceSystem2D());
		LOGGER.info(gridG.getGridRange().toString());
		LOGGER.info(gridG.getEnvelope2D().toString());
	}
}
