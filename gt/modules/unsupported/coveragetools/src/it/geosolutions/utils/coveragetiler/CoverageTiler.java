/**
 * 
 */
package it.geosolutions.utils.coveragetiler;

import it.geosolutions.utils.progress.ExceptionEvent;
import it.geosolutions.utils.progress.ProcessingEvent;
import it.geosolutions.utils.progress.ProcessingEventListener;
import it.geosolutions.utils.progress.ProgressManager;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.Interpolation;

import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.GroupImpl;
import org.apache.commons.cli2.util.HelpFormatter;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.coverage.grid.UnknownFormat;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * <p>
 * This utility splits rasters into smaller pieces. Having a raster tilized into
 * pieces, and using them on a mosaic, fo instance, means big performance
 * improvements.
 * </p>
 * 
 * <p>
 * Example of usage:<br/>
 * <code>CoverageTiler -t "35,35" -s "/usr/home/tmp/myImage.tiff"</code>
 * </p>
 * 
 * <p>
 * The tiles will be stored on the folder <code>"/usr/home/tmp/tiled"</code>,
 * which will be automatically created.
 * </p>
 * 
 * <pre>
 *                                                                                                  
 *     HINT: set the tile dimensions in order to obtain smaller pieces which size is between 500Kb and 2Mb
 *           The size of the pieces depends on the raster resolution and the Envelope.                    
 *           Use the CoverageScaler to change your raster resolution.                                     
 *           If you don't know these parameters, however, try first with small values like 20,20 or 40,40 
 *           and calibrate then the tile dimension in order to obtain the desired size.                   
 * </pre>
 * 
 * @author Simone Giannecchini
 * @author Alessio Fabiani
 * @version 0.2
 * 
 */
public class CoverageTiler extends ProgressManager implements
		ProcessingEventListener, Runnable {
	/** Default Logger * */
	private final static Logger LOGGER = Logger.getLogger(CoverageTiler.class
			.toString());

	/** Program Version */
	private final static String versionNumber = "0.2";

	protected final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder();

	private DefaultOption helpOpt;

	private DefaultOption versionOpt;

	private DefaultOption inputLocationOpt;

	private DefaultOption outputLocationOpt;

	private File inputLocation;

	private File outputLocation;

	private DefaultOption tileDimOpt;

	private int numTileX;

	private int numTileY;

	/**
	 * Default constructor
	 */
	public CoverageTiler() {
		// /////////////////////////////////////////////////////////////////////
		// Options for the command line
		// /////////////////////////////////////////////////////////////////////
		helpOpt = optionBuilder.withShortName("h").withShortName("?")
				.withLongName("helpOpt").withDescription("print this message.")
				.create();
		versionOpt = optionBuilder.withShortName("v")
				.withLongName("versionOpt").withDescription(
						"print the versionOpt.").create();
		inputLocationOpt = optionBuilder.withShortName("s").withLongName(
				"src_coverage").withArgument(
				arguments.withName("source").withMinimum(1).withMaximum(1)
						.create()).withDescription(
				"path where the source code is located").withRequired(true)
				.create();
		outputLocationOpt = optionBuilder
				.withShortName("d")
				.withLongName("dest_directory")
				.withArgument(
						arguments.withName("destination").withMinimum(0)
								.withMaximum(1).create())
				.withDescription(
						"output directory, if none is provided, the \"tiled\" directory will be used")
				.withRequired(false).create();
		tileDimOpt = optionBuilder
				.withShortName("t")
				.withLongName("tiled_dimension")
				.withArgument(
						arguments.withName("t").withMinimum(1).withMaximum(1)
								.create())
				.withDescription(
						"number or rows and columns used to split the image as a couple rows,cols")
				.withRequired(true).create();

		priorityOpt = optionBuilder.withShortName("p").withLongName(
				"thread_priority").withArgument(
				arguments.withName("priority").withMinimum(0).withMaximum(1)
						.create()).withDescription(
				"priority for the underlying thread").withRequired(false)
				.create();

		cmdOpts.add(helpOpt);
		cmdOpts.add(tileDimOpt);
		cmdOpts.add(versionOpt);
		cmdOpts.add(inputLocationOpt);
		cmdOpts.add(outputLocationOpt);
		cmdOpts.add(priorityOpt);

		optionsGroup = new GroupImpl(cmdOpts, "Options", "All the options", 1,
				10);

		// /////////////////////////////////////////////////////////////////////
		//
		// Help Formatter
		//
		// /////////////////////////////////////////////////////////////////////
		final HelpFormatter cmdHlp = new HelpFormatter("| ", "  ", " |", 75);
		cmdHlp.setShellCommand("CoverageTiler");
		cmdHlp.setHeader("Help");
		cmdHlp.setFooter(new StringBuffer(
				"CoverageTiler - GeoSolutions S.a.s (C) 2006 - v ").append(
				CoverageTiler.versionNumber).toString());
		cmdHlp
				.setDivider("|-------------------------------------------------------------------------|");

		cmdParser.setGroup(optionsGroup);
		cmdParser.setHelpOption(helpOpt);
		cmdParser.setHelpFormatter(cmdHlp);
	}

	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws MalformedURLException,
			InterruptedException {
		final CoverageTiler coverageTiler = new CoverageTiler();
		coverageTiler.addProcessingEventListener(coverageTiler);
		if (coverageTiler.parseArgs(args)) {
			final Thread t = new Thread(coverageTiler, "MosaicIndexBuilder");
			t.setPriority(coverageTiler.priority);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}

		} else
			LOGGER.fine("Exiting...");
	}

	/**
	 * This method is responsible for sending the process progress events to the
	 * logger.
	 * 
	 * <p>
	 * It should be used to do normal logging when running this tools as command
	 * line tools but it should be disable when putting the tool behind a GUI.
	 * In such a case the GUI should register itself as a
	 * {@link ProcessingEventListener} and consume the processing events.
	 * 
	 * @param event
	 *            is a {@link ProcessingEvent} that informs the receiver on the
	 *            precetnage of the progress as well as on what is happening.
	 */
	public void getNotification(ProcessingEvent event) {
		LOGGER.info(new StringBuffer("Progress is at ").append(
				event.getPercentage()).append("\n").append(
				"attached message is: ").append(event.getMessage()).toString());

	}

	public void exceptionOccurred(ExceptionEvent event) {
		LOGGER.log(Level.SEVERE, "An error occurred during processing", event
				.getException());
	}

	public void run() {

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// PARSING INPUT PARAMETERS
		// 
		// 
		// /////////////////////////////////////////////////////////////////////

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// Opening the base mosaic
		// 
		// 
		// /////////////////////////////////////////////////////////////////////
		// mosaic reader
		StringBuffer message = new StringBuffer(
				"Acquiring a mosaic reader to mosaic ").append(inputLocation);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());

		AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(inputLocation);
		if (format == null||format instanceof UnknownFormat) {
			fireException(
					"Unable to decide format for this coverage",
					0,
					new IOException("Could not find a format for this coverage"));
			return;
		}
		AbstractGridCoverage2DReader inReader = (AbstractGridCoverage2DReader) format
				.getReader(inputLocation);
		if (inReader == null) {
			message = new StringBuffer(
					"Unable to instantiate a reader for this coverage");
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.fine(message.toString());
			return;
		}

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// Preparing all the params
		// 
		// 
		// /////////////////////////////////////////////////////////////////////
		if (!outputLocation.exists())
			outputLocation.mkdir();

		// getting envelope and other information about dimension
		final GeneralEnvelope envelope = inReader.getOriginalEnvelope();
		message = new StringBuffer("Original envelope is ").append(envelope
				.toString());
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
		// world.200401.3x21600x21600.C1.tif

		final GeneralGridRange range = inReader.getOriginalGridRange();

		message = new StringBuffer("Original range is ").append(range
				.toString());
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
		// world.200401.3x21600x21600.C1.tif

		message = new StringBuffer("New matrix dimension is (cols,rows)==(")
				.append(numTileX).append(",").append(numTileY).append(")");
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
		// world.200401.3x21600x21600.C1.tif

		final int uppers[] = range.getUppers();
		final double newRange[] = new double[] { uppers[0] / numTileX,
				uppers[1] / numTileY };
		final double minx = envelope.getMinimum(0);
		final double miny = envelope.getMinimum(1);
		final double maxx = envelope.getMaximum(0);
		final double maxy = envelope.getMaximum(1);
		// getting resolution
		final double dx = envelope.getLength(0) / numTileX;
		final double dy = envelope.getLength(1) / numTileY;

		double _maxx = 0.0;
		double _maxy = 0.0;
		double _minx = 0.0;
		double _miny = 0.0;
		final AbstractProcessor processor = new DefaultProcessor(null);
		GridCoverage2D gc = null;
		File fileOut;
		GeoTiffWriter writerWI;
		ParameterValue gg;
		GeneralEnvelope cropEnvelope;

		// ///////////////////////////////////////////////////////////////////
		//
		// MAIN LOOP
		//
		//
		// ///////////////////////////////////////////////////////////////////
		for (int i = 0; i < numTileY; i++)
			for (int j = 0; j < numTileX; j++) {

				// //
				//
				// computing the bbox for this tile
				//
				// //
				_maxx = minx + (j + 1) * dx;
				_minx = minx + (j) * dx;
				_maxy = miny + (i + 1) * dy;
				_miny = miny + (i) * dy;
				if (_maxx > maxx)
					_maxx = maxx;
				if (_maxy > maxy)
					_maxy = maxy;

				// //
				//
				// building gridgeometry for the read operation
				//
				// //
				gg = (ParameterValue) ImageMosaicFormat.READ_GRIDGEOMETRY2D
						.createValue();
				cropEnvelope = new GeneralEnvelope(
						new double[] { _minx, _miny }, new double[] { _maxx,
								_maxy });
				cropEnvelope.setCoordinateReferenceSystem(inReader.getCrs());
				gg.setValue(new GridGeometry2D(new GeneralGridRange(
						new Rectangle(0, 0, 800, 800)), cropEnvelope));

				message = new StringBuffer("Reading with grid envelope ")
						.append(cropEnvelope.toString());
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(message.toString());

				try {
					gc = (GridCoverage2D) inReader
							.read(new GeneralParameterValue[] { gg });

				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					fireEvent(e.getLocalizedMessage(), 0);
					return;
				}

				fileOut = new File(outputLocation, new StringBuffer("mosaic")
						.append("_").append(Integer.toString(i * numTileX + j))
						.append(".").append("tiff").toString());
				if (fileOut.exists())
					fileOut.delete();

				message = new StringBuffer("Preparing tile (col,row)==(")
						.append(j).append(",").append(i).append(") to file ")
						.append(fileOut);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(message.toString());

				// //
				//
				// building gridgeometry for the read operation
				//
				// //

				ParameterValueGroup param = processor.getOperation(
						"CoverageCrop").getParameters();
				param.parameter("Source").setValue(gc);
				param.parameter("Envelope").setValue(cropEnvelope);

				GridCoverage2D cropped = (GridCoverage2D) processor
						.doOperation(param);

				final GeneralGridRange newGridrange = new GeneralGridRange(
						new Rectangle2D.Double(0.0, 0.0, newRange[0],
								newRange[1]).getBounds());
				final GridGeometry2D scaledGridGeometry = new GridGeometry2D(
						newGridrange, cropEnvelope);
				param = processor.getOperation("Resample").getParameters();
				param.parameter("Source").setValue(cropped);
				param.parameter("CoordinateReferenceSystem").setValue(
						inReader.getCrs());
				param.parameter("GridGeometry").setValue(scaledGridGeometry);
				param
						.parameter("InterpolationType")
						.setValue(
								Interpolation
										.getInstance(Interpolation.INTERP_NEAREST));

				GridCoverage2D scaled = (GridCoverage2D) processor
						.doOperation(param);

				message = new StringBuffer("Writing out...");
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(message.toString());

				try {
					writerWI = new GeoTiffWriter(fileOut);
					writerWI.write(scaled, null);
				} catch (IOException e) {
					fireException(e);
					return;
				}

			}

		message = new StringBuffer("Done...");
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
	}

	private boolean parseArgs(String[] args) {
		cmdLine = cmdParser.parseAndHelp(args);
		if (cmdLine != null && cmdLine.hasOption(versionOpt)) {
			System.out.print(new StringBuffer(
					"MosaicIndexBuilder - GeoSolutions S.a.s (C) 2006 - v")
					.append(CoverageTiler.versionNumber).toString());
			System.exit(1);

		} else if (cmdLine != null) {
			// ////////////////////////////////////////////////////////////////
			//
			// parsing command line parameters and setting up
			// Mosaic Index Builder options
			//
			// ////////////////////////////////////////////////////////////////
			inputLocation = new File((String) cmdLine
					.getValue(inputLocationOpt));

			// output files' directory
			if (cmdLine.hasOption(outputLocationOpt))
				outputLocation = new File((String) cmdLine
						.getValue(outputLocationOpt));
			else
				outputLocation = new File(inputLocation.getParentFile(),
						"tiled");

			// tile dim
			final String tileDim = (String) cmdLine.getValue(tileDimOpt);
			final String[] pairs = tileDim.split(",");
			numTileX = Integer.parseInt(pairs[0]);
			numTileY = Integer.parseInt(pairs[1]);

			// //
			//
			// Thread priority
			//
			// //
			// index name
			if (cmdLine.hasOption(priorityOpt))
				priority = Integer.parseInt((String) cmdLine
						.getValue(priorityOpt));
			return true;

		}
		return false;

	}

	public File getInputLocation() {
		return inputLocation;
	}

	public void setInputLocation(File inputLocation) {
		this.inputLocation = inputLocation;
	}

	public int getNumTileX() {
		return numTileX;
	}

	public void setNumTileX(int numTileX) {
		this.numTileX = numTileX;
	}

	public int getNumTileY() {
		return numTileY;
	}

	public void setNumTileY(int numTileY) {
		this.numTileY = numTileY;
	}

	public File getOutputLocation() {
		return outputLocation;
	}

	public void setOutputLocation(File outputLocation) {
		this.outputLocation = outputLocation;
	}
}
