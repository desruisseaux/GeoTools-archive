package org.geotools.gce.gtopo30.util;

import java.io.File;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.gce.gtopo30.GTopo30FormatFactory;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.ParameterValueGroup;

/**
 * A simple crop utility that can be used to create subset of the huges GTOPO30
 * files for testing purposes.
 * 
 * @author wolf
 * @author Simone Giannecchini
 * 
 */
public class GTopo30Cropper {

	public static void main(String[] args) throws Exception {
		// COMPUTATION PARAMETERS
		// 1) original file
		File source = new File("c:\\temp\\w020n90\\w020n90.dem");
		// 2) cropping envelope
		GeneralEnvelope cropEnvelope = new GeneralEnvelope(2);
		cropEnvelope.setRange(0, -2, 0);
		cropEnvelope.setRange(1, 52, 54);
		// 3) destination name
		String resultName = "W002N52";

		// read the original coverage
		AbstractGridFormat format = (AbstractGridFormat) new GTopo30FormatFactory()
				.createFormat();
		GridCoverageReader reader = format.getReader(source.toURL());
		GridCoverage gc = ((GridCoverage2D) reader.read(null));
		cropEnvelope.setCoordinateReferenceSystem(gc
				.getCoordinateReferenceSystem());
		System.out.println(cropEnvelope);

		// Crop and rename the coverage
		GridCoverage2D cropped = getCroppedCoverage(gc, cropEnvelope);
		GridCoverageFactory factory = new GridCoverageFactory();
		GridCoverage2D result = factory.create(resultName, cropped
				.getRenderedImage(), cropped.getEnvelope());
		System.out.println(result);

		// Write the resulting coverate to the output folder
		File destDir = new File(source.getParentFile().getParentFile(),
				resultName);
		if (!destDir.exists())
			destDir.mkdir();
		GridCoverageWriter writer = format.getWriter(destDir);
		writer.write(result, null);
	}

	/**
	 * Crops a coverage and returns the result.
	 * <p>
	 * Highly derived from GridCoverageRenderer
	 * 
	 * @param gc
	 * @param envelope
	 * @return
	 */
	private static GridCoverage2D getCroppedCoverage(GridCoverage gc,
			GeneralEnvelope envelope) {
		DefaultProcessor processor = new DefaultProcessor(new Hints(
				Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));
		final ParameterValueGroup param = (ParameterValueGroup) processor
				.getOperation("CoverageCrop").getParameters();
		param.parameter("source").setValue(gc);
		param.parameter("Envelope").setValue(envelope);
		return (GridCoverage2D) processor.doOperation(param);

	}
}
