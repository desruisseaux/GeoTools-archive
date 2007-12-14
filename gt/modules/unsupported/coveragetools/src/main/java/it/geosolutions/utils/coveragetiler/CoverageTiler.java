/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package it.geosolutions.utils.coveragetiler;

import it.geosolutions.utils.progress.ExceptionEvent;
import it.geosolutions.utils.progress.ProcessingEvent;
import it.geosolutions.utils.progress.ProcessingEventListener;
import it.geosolutions.utils.progress.ProgressManager;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.GroupImpl;
import org.apache.commons.cli2.util.HelpFormatter;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * <p>
 * This utility splits rasters into smaller pieces. Having a raster tiled into
 * pieces, and using them on a mosaic, for instance, means big performance
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
 *         HINT: set the tile dimensions in order to obtain smaller pieces which size is between 500Kb and 2Mb
 *               The size of the pieces depends on the raster resolution and the Envelope.                    
 *               Use the CoverageScaler to change your raster resolution.                                     
 *               If you don't know these parameters, however, try first with small values like 20,20 or 40,40 
 *               and calibrate then the tile dimension in order to obtain the desired size.                   
 * </pre>
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 * @version 0.3
 * 
 */
public class CoverageTiler extends ProgressManager implements
		ProcessingEventListener, Runnable {
	/** Default Logger * */
	private final static Logger LOGGER = Logger.getLogger(CoverageTiler.class
			.toString());

	/** Program Version */
	private final static String versionNumber = "0.3";

	private final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder();

	private DefaultOption helpOpt;

	private DefaultOption versionOpt;

	private DefaultOption inputLocationOpt;

	private DefaultOption outputLocationOpt;

	private File inputLocation;

	private File outputLocation;

	private DefaultOption tileDimOpt;

	private int tileWidth;

	private int tileHeight;

	private DefaultOption internalTileDimOpt;

	private int internalTileWidth;

	private int internalTileHeight;

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
		tileDimOpt = optionBuilder.withShortName("t").withLongName(
				"tile_dimension").withArgument(
				arguments.withName("t").withMinimum(1).withMaximum(1).create())
				.withDescription("Width and height of each tile we generate")
				.withRequired(true).create();

		internalTileDimOpt = optionBuilder.withShortName("it").withLongName(
				"internal_tile_dimension").withArgument(
				arguments.withName("it").withMinimum(0).withMaximum(1).create()).withDescription(
				"Internal width and height of each tile we generate")
				.withRequired(false).create();

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
		cmdOpts.add(internalTileDimOpt);
		

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
			final Thread t = new Thread(coverageTiler, "CoverageTiler");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.geosolutions.utils.progress.ProgressManager#run()
	 */
	public void run() {

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// Trying to acquire a reader for the provided source file.
		// 
		// 
		// /////////////////////////////////////////////////////////////////////
		StringBuffer message = new StringBuffer(
				"Acquiring a mosaic reader to mosaic ").append(inputLocation);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
		fireEvent(message.toString(), 0);
		// get the format of this file, if it is recognized!
		final AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(inputLocation);
		if (format == null || format instanceof UnknownFormat) {
			fireException(
					"Unable to decide format for this coverage",
					0,
					new IOException("Could not find a format for this coverage"));
			return;
		}
		// get a reader for this file
		final AbstractGridCoverage2DReader inReader = (AbstractGridCoverage2DReader) format
				.getReader(inputLocation, new Hints(
						Hints.IGNORE_COVERAGE_OVERVIEW, Boolean.TRUE));
		if (inReader == null) {
			message = new StringBuffer(
					"Unable to instantiate a reader for this coverage");
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.fine(message.toString());
			fireEvent(message.toString(), 0);
			return;
		}

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// If everything went fine, let's proceed with tiling this coverage.
		// 
		// 
		// /////////////////////////////////////////////////////////////////////
		if (!outputLocation.exists())
			outputLocation.mkdir();

		// //
		//
		// getting source envelope and crs
		//
		// //
		final GeneralEnvelope envelope = inReader.getOriginalEnvelope();
		message = new StringBuffer("Original envelope is ").append(envelope
				.toString());
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
		fireEvent(message.toString(), 0);

		// //
		//
		// getting source gridrange and checking tile dimensions to be not
		// bigger than the original coverage size
		//
		// //
		final GeneralGridRange range = inReader.getOriginalGridRange();
		final int w = range.getLength(0);
		final int h = range.getLength(1);
		tileWidth = tileWidth > w ? w : tileWidth;
		tileHeight = tileHeight > h ? h : tileHeight;
		message = new StringBuffer("Original range is ").append(range
				.toString());
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
		fireEvent(message.toString(), 0);
		message = new StringBuffer("New matrix dimension is (cols,rows)==(")
				.append(tileWidth).append(",").append(tileHeight).append(")");
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
		fireEvent(message.toString(), 0);

		// //
		//
		// read a coverage with the actual
		// envelope
		//
		// //
		GridCoverage2D gc;
		try {
			gc = (GridCoverage2D) inReader.read(null);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			fireException(e);
			return;

		}

		// ///////////////////////////////////////////////////////////////////
		//
		// MAIN LOOP
		//
		//
		// ///////////////////////////////////////////////////////////////////
		final int numTileX = (int) (w / (tileWidth * 1.0) + 1);
		final int numTileY = (int) (h / (tileHeight * 1.0) + 1);
		for (int i = 0; i < numTileX; i++)
			for (int j = 0; j < numTileY; j++) {

				// //
				//
				// computing the bbox for this tile
				//
				// //
				final Rectangle sourceRegion = new Rectangle(i * tileWidth, j
						* tileHeight, tileWidth, tileHeight);
				message = new StringBuffer("Writing region  ")
						.append(sourceRegion);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(message.toString());
				fireEvent(message.toString(), (i + j)
						/ (numTileX * numTileY * 1.0));

				// //
				//
				// building gridgeometry for the read operation with the actual
				// envelope
				//
				// //
				final File fileOut = new File(outputLocation, new StringBuffer(
						"mosaic").append("_").append(
						Integer.toString(i * tileWidth + j)).append(".")
						.append("tiff").toString());
				// remove an old output file if it exists
				if (fileOut.exists())
					fileOut.delete();

				message = new StringBuffer(
						"Preparing to write tile (col,row)==(").append(j)
						.append(",").append(i).append(") to file ").append(
								fileOut);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(message.toString());
				fireEvent(message.toString(), (i + j)
						/ (numTileX * numTileY * 1.0));

				// //
				//
				// Write this coverage out as a geotiff
				//
				// //
				final AbstractGridFormat outFormat= new GeoTiffFormat();
				try {

					final GeoTiffWriteParams wp = new GeoTiffWriteParams();
					wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
					wp.setTiling(internalTileWidth, internalTileHeight);
					wp.setSourceRegion(sourceRegion);
					final ParameterValueGroup params = outFormat
							.getWriteParameters();
					params.parameter(
							AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName()
									.toString()).setValue(wp);

					final GeoTiffWriter writerWI = new GeoTiffWriter(fileOut);
					writerWI.write(gc, (GeneralParameterValue[]) params
							.values().toArray(new GeneralParameterValue[1]));
					writerWI.dispose();
				} catch (IOException e) {
					fireException(e);
					return;
				}

			}

		message = new StringBuffer("Done...");
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(message.toString());
		fireEvent(message.toString(), 100);
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
			// //
			//
			// tile dim
			//
			// //
			final String tileDim = (String) cmdLine.getValue(tileDimOpt);
			String[] pairs = tileDim.split(",");
			tileWidth = Integer.parseInt(pairs[0]);
			tileHeight = Integer.parseInt(pairs[1]);

			// //
			//
			// Internal Tile dim
			//
			// //
			final String internalTileDim = (String) cmdLine
					.getValue(internalTileDimOpt);
			if (internalTileDim != null && internalTileDim.length() > 0) {
				pairs = internalTileDim.split(",");
				internalTileWidth = Integer.parseInt(pairs[0]);
				internalTileHeight = Integer.parseInt(pairs[1]);
			} else {
				internalTileWidth=tileWidth;
				internalTileHeight=tileHeight;
			}

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

	public int getTileWidth() {
		return tileWidth;
	}

	public void setTileWidth(int numTileX) {
		this.tileWidth = numTileX;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	public void setTileHeight(int numTileY) {
		this.tileHeight = numTileY;
	}

	public File getOutputLocation() {
		return outputLocation;
	}

	public void setOutputLocation(File outputLocation) {
		this.outputLocation = outputLocation;
	}
}
