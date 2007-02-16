package it.geosolutions.utils.coveragescaler;

import it.geosolutions.utils.progress.ExceptionEvent;
import it.geosolutions.utils.progress.ProcessingEvent;
import it.geosolutions.utils.progress.ProcessingEventListener;
import it.geosolutions.utils.progress.ProgressManager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.GroupImpl;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;
import org.apache.commons.io.filefilter.WildcardFilter;
import org.geotools.coverage.processing.operation.FilteredSubsample;
import org.geotools.coverage.processing.operation.Scale;
import org.geotools.coverage.processing.operation.SubsampleAverage;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * <p>
 * This classis in charge for creating the index for a mosaic of images that we
 * want to tie together as a sigle bg coverage.
 * </p>
 * 
 * <p>
 * To get instructions on how to run the toll just run it without any arguments
 * and nice and clean help will be printed to the command line.
 * </p>
 * 
 * <p>
 * It is worth to point out that this tool comes as a command line tool but it
 * has been built with in mind a GUI. It has the capapbility to register
 * {@link ProcessingEventListener} object that receive notifications about what
 * is going on. Moreover it delegates all the computations to an external
 * thread, hence we can stop the tool in the middle of processig with no so many
 * concerns (hpefully :-) ).
 * </p>
 * 
 * <p>
 * Usage:<br/> <code>CoverageScaler -h -v -s -w -p -a -f -c</code>
 * </p>
 * 
 * <pre>
 *                                                                                                                        
 *     where:                                                                                                                   
 *      -h : Prints a nice command line Help                                                                                    
 *      -v : Prints the tools Version                                                                                           
 *      -s : Is the path where the raster(s) is(are) located                                                                    
 *      -w : Is the wildcard representing just the file we want to process (e.g. *.tiff)                                        
 *      -p : Is the Thread Priority, a number between 1 and 10 -&gt; 1 [LOW] - 5 [MED] - 10 [HIGH]                              
 *      -a : Represents the Scaling algorithm to use. You can choose among one of the following                                 
 *           nn, bil, avg, filt                                                                                                 
 *      -f : Represents the scale factor. If you want a raster which is 1/2 resolution                                          
 *           of the original, f should be 2                                                                                     
 *      -c : Represents the JAI TileCache dimension. This is an optional parameter which allows                                 
 *           you to tune the tool performances.                                                                                 
 * </pre>
 * 
 * <p>
 * Example of usage:<br/>
 * <code>CoverageScaler -s "/usr/home/tmp/tiled" -w *.tiff -a nn -f 2</code>
 * </p>
 * 
 * @author Simone Giannecchini
 * @author Alessio Fabiani
 * @version 0.2
 * 
 */
public class CoverageScaler extends ProgressManager implements Runnable,
		ProcessingEventListener {
	private final static Hints LENIENT_HINT = new Hints(
			Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

	private final static FilteredSubsample filteredSubsampleFactory = new FilteredSubsample();

	private static final SubsampleAverage subsampleAvgFactory = new SubsampleAverage();

	private static final Scale scaleFactory = new Scale();

	/** Static immutable ap for scaling algorithms. */
	private static List scalingAlgorithms;
	static {
		scalingAlgorithms = new ArrayList(4);
		scalingAlgorithms.add("nn");
		scalingAlgorithms.add("bil");
		scalingAlgorithms.add("avg");
		scalingAlgorithms.add("filt");
	}

	/** Static immutable ap for scaling algorithms. */
	private static List outputFormats;
	static {
		outputFormats = new ArrayList(6);
		outputFormats.add("tiff");
		outputFormats.add("tif");
		outputFormats.add("gtiff");
		outputFormats.add("gtif");
		outputFormats.add("png");
		outputFormats.add("jpeg");

	}

	/**
	 * Number of resolution levels for the coverages.
	 */
	private int numberOfLevels;

	/**
	 * Resolutions levels.
	 */
	private double[][] resolutionLevels;

	/** Number of files to process. */
	private int numFiles;

	/** Default Logger * */
	private final static Logger LOG = Logger.getLogger(CoverageScaler.class
			.toString());

	/** Program Version */
	private final static String versionNumber = "0.2";

	private static final double EPS = 1E-6;

	private final DefaultOption locationOpt;

	private String locationPath;

	private final DefaultOption wildcardOpt;

	private String wildcardString = "*.*";

	/**
	 * Index file name. Default is index.
	 */
	private String indexName = "index";

	private DefaultOption scaleAlgorithmOpt;

	private DefaultOption tileCacheSizeOpt;

	private DefaultOption scaleFactorOpt;

	private int scaleFactor;

	private String scaleAlgorithm;

	/**
	 * Main thread for the mosaic index builder.
	 */
	public void run() {

		// /////////////////////////////////////////////////////////////////////
		//
		// CREATING INDEX FILE
		//
		// /////////////////////////////////////////////////////////////////////

		// /////////////////////////////////////////////////////////////////////
		//
		// Create a file handler that write log record to a file called
		// my.log
		//
		// /////////////////////////////////////////////////////////////////////
		FileHandler handler;
		try {
			boolean append = true;
			handler = new FileHandler(new StringBuffer(locationPath).append(
					"/error.txt").toString(), append);
			handler.setLevel(Level.SEVERE);
			// Add to the desired logger
			LOG.addHandler(handler);
		} catch (SecurityException el) {
			LOG.severe(el.getLocalizedMessage());
		} catch (IOException el) {
			LOG.severe(el.getLocalizedMessage());
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Creating temp vars
		//
		// /////////////////////////////////////////////////////////////////////

		/** Fixed local variables * */
		AbstractGridCoverage2DReader reader;

		CoordinateReferenceSystem defaultCRS = null, actualCRS = null;

		GeneralEnvelope globEnvelope = null;
		GeneralEnvelope envelope;

		ImageInputStream inStream;
		ImageTypeSpecifier its;
		Iterator it;
		ImageReader r;
		double[] res;
		boolean skipFeature = false;
		double resX = 0, resY = 0;

		final File dir = new File(locationPath);
		final FileFilter fileFilter = new WildcardFilter(wildcardString);
		final File[] files = dir.listFiles(fileFilter);
		StringBuffer message;
		// /////////////////////////////////////////////////////////////////////
		//
		// Cycling over the features
		//
		// /////////////////////////////////////////////////////////////////////
		numFiles = files.length;
		for (int i = 0; i < numFiles; i++) {
			// //
			//
			// Anyone has asked us to stop?
			//
			// //
			if (getStopThread()) {
				message = new StringBuffer("Stopping requested at file  ")
						.append(i).append(" of ").append(numFiles).append(
								" files");
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine(message.toString());
				}
				fireEvent(message.toString(), ((i * 100.0) / numFiles) - 1);
				return;
			}
			message = new StringBuffer("Now scaling file ").append(files[i]);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(message.toString());
			}
			fireEvent(message.toString(), ((i * 100.0) / numFiles));

		}

		if (numFiles <= 0) {
			// processing information
			message = new StringBuffer("No file to process!!!");
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(message.toString());
			}
			fireEvent(message.toString(), 100);
		}

	}

	/**
	 * Default constructor
	 */
	public CoverageScaler() {

		// /////////////////////////////////////////////////////////////////////
		// Options for the command line
		// /////////////////////////////////////////////////////////////////////
		helpOpt = optionBuilder.withShortName("h").withShortName("?")
				.withLongName("helpOpt").withDescription("print this message.")
				.create();
		versionOpt = optionBuilder.withShortName("v")
				.withLongName("versionOpt").withDescription(
						"print the versionOpt.").create();
		locationOpt = optionBuilder.withShortName("s").withLongName(
				"source_directory").withArgument(
				arguments.withName("source").withMinimum(1).withMaximum(1)
						.create()).withDescription(
				"path where files are located").withRequired(true).create();
		wildcardOpt = optionBuilder.withShortName("w").withLongName(
				"wildcardOpt").withArgument(
				arguments.withName("wildcardOpt").withMinimum(0).withMaximum(1)
						.create()).withDescription(
				"wildcardOpt to use for selecting files").withRequired(false)
				.create();

		scaleFactorOpt = optionBuilder
				.withShortName("f")
				.withLongName("scale_factor")
				.withArgument(
						arguments.withName("f").withMinimum(1).withMaximum(1)
								.withValidator(new Validator() {

									public void validate(List args)
											throws InvalidArgumentException {
										final int size = args.size();
										if (size > 1)
											throw new InvalidArgumentException(
													"Only one scaling algorithm at a time can be chosen");
										int factor = Integer
												.parseInt((String) args.get(0));
										if (factor <= 0)
											throw new InvalidArgumentException(
													new StringBuffer(
															"The provided scale factor is negative! ")

													.toString());
										if (factor == 1) {
											LOG
													.warning("The scale factor is 1, program will exit!");
											System.exit(0);
										}
									}

								}).create()).withDescription(
						"integer scale factor")
				.withRequired(true).create();

		scaleAlgorithmOpt = optionBuilder
				.withShortName("a")
				.withLongName("scaling_algorithm")
				.withArgument(
						arguments.withName("a").withMinimum(0).withMaximum(1)
								.withValidator(new Validator() {

									public void validate(List args)
											throws InvalidArgumentException {
										final int size = args.size();
										if (size > 1)
											throw new InvalidArgumentException(
													"Only one scaling algorithm at a time can be chosen");
										if (!scalingAlgorithms.contains(args
												.get(0)))
											throw new InvalidArgumentException(
													new StringBuffer(
															"The output format ")
															.append(args.get(0))
															.append(
																	" is not permitted")
															.toString());

									}
								}).create())
				.withDescription(
						"name of the scaling algorithm, eeither one of average (a), filtered	 (f), bilinear (bil), nearest neigbhor (nn)")
				.withRequired(false).create();

		priorityOpt = optionBuilder.withShortName("p").withLongName(
				"thread_priority").withArgument(
				arguments.withName("priority").withMinimum(0).withMaximum(1)
						.create()).withDescription(
				"priority for the underlying thread").withRequired(false)
				.create();
		tileCacheSizeOpt = optionBuilder.withShortName("c").withLongName(
				"cache_size").withArgument(
				arguments.withName("c").withMinimum(0).withMaximum(1).create())
				.withDescription("tile cache sized").withRequired(false)
				.create();

		cmdOpts.add(helpOpt);
		cmdOpts.add(versionOpt);
		cmdOpts.add(locationOpt);
		cmdOpts.add(wildcardOpt);
		cmdOpts.add(priorityOpt);
		cmdOpts.add(scaleAlgorithmOpt);
		cmdOpts.add(scaleFactorOpt);
		cmdOpts.add(tileCacheSizeOpt);

		optionsGroup = new GroupImpl(cmdOpts, "Options", "All the options", 1,
				10);

		// /////////////////////////////////////////////////////////////////////
		//
		// Help Formatter
		//
		// /////////////////////////////////////////////////////////////////////
		final HelpFormatter cmdHlp = new HelpFormatter("| ", "  ", " |", 75);
		cmdHlp.setShellCommand("CoverageScaler");
		cmdHlp.setHeader("Help");
		cmdHlp.setFooter(new StringBuffer(
				"CoverageScaler - GeoSolutions S.a.s (C) 2006 - v ").append(
				CoverageScaler.versionNumber).toString());
		cmdHlp
				.setDivider("|-------------------------------------------------------------------------|");

		cmdParser.setGroup(optionsGroup);
		cmdParser.setHelpOption(helpOpt);
		cmdParser.setHelpFormatter(cmdHlp);
	}

	/**
	 * Entry point for the index builder.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		final CoverageScaler coverageScaler = new CoverageScaler();
		coverageScaler.addProcessingEventListener(coverageScaler);
		if (coverageScaler.parseArgs(args)) {
			final Thread t = new Thread(coverageScaler, "CoverageScaler");
			t.setPriority(coverageScaler.priority);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}

		} else
			LOG.fine("Exiting...");

	}

	private boolean parseArgs(String[] args) {
		cmdLine = cmdParser.parseAndHelp(args);
		if (cmdLine != null && cmdLine.hasOption(versionOpt)) {
			System.out.print(new StringBuffer(
					"CoverageScaler - GeoSolutions S.a.s (C) 2006 - v").append(
					CoverageScaler.versionNumber).toString());
			System.exit(1);

		} else if (cmdLine != null) {
			// ////////////////////////////////////////////////////////////////
			//
			// parsing command line parameters and setting up
			// Mosaic Index Builder options
			//
			// ////////////////////////////////////////////////////////////////
			locationPath = (String) cmdLine.getValue(locationOpt);
			final File inDir = new File(locationPath);
			if (!inDir.isDirectory()) {
				LOG
						.severe("Provided input dir does not exist or is not a dir!");
				return false;
			}
			// wildcard
			if (cmdLine.hasOption(wildcardOpt))
				wildcardString = (String) cmdLine.getValue(wildcardOpt);

			// //
			//
			// scale factor
			//
			// //
			final String scaleF = (String) cmdLine.getValue(scaleFactorOpt);
			scaleFactor = Integer.parseInt(scaleF);

			// //
			//
			// scaling algorithm
			//
			// //
			scaleAlgorithm = (String) cmdLine.getValue(scaleAlgorithmOpt);

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

	/**
	 * This method is repsonbile for sending the process progress events to the
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
		LOG.info(new StringBuffer("Progress is at ").append(
				event.getPercentage()).append("\n").append(
				"attached message is: ").append(event.getMessage()).toString());

	}

	public void exceptionOccurred(ExceptionEvent event) {
		LOG.log(Level.SEVERE, "An error occurred during processing", event
				.getException());
	}
}
