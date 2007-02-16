package it.geosolutions.utils.imagepyramid;

import it.geosolutions.utils.coveragetiler.CoverageTiler;
import it.geosolutions.utils.imagemosaic.MosaicIndexBuilder;
import it.geosolutions.utils.progress.ExceptionEvent;
import it.geosolutions.utils.progress.ProcessingEvent;
import it.geosolutions.utils.progress.ProcessingEventListener;
import it.geosolutions.utils.progress.ProgressManager;

import java.awt.RenderingHints;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RecyclingTileFactory;
import javax.media.jai.TileCache;

import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.GroupImpl;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;
import org.apache.commons.io.FileUtils;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.grid.GridRange;

/**
 * Given an original image, builds an image pyramid out of it by combining the
 * various tiler, mosaic and pyramid layer builder tools.
 * 
 * <pre>
 * Example of use:
 *   PyramidBuilder -s "/usr/home/data/home.tif" -f 2 -n 4 -t "25,25" -w
 * </pre>
 * 
 * @author Andrea Aime
 * @since 2.3.x
 * 
 */
public class PyramidBuilder extends ProgressManager implements Runnable,
		ProcessingEventListener {

	/**
	 * Default tile cache size.
	 */
	public final static long DEFAULT_TILE_CHACHE_SIZE = 32 * 1024 * 1024;

	/**
	 * Default imageio caching behaviour.
	 */
	public final boolean DEFAULT_IMAGEIO_CACHING_BEHAVIOUR = false;

	/**
	 * Default thread priority.
	 */
	public final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

	/**
	 * Default interpolation.
	 */
	public final static Interpolation DEFAULT_INTERPOLATION = Interpolation
			.getInstance(Interpolation.INTERP_NEAREST);

	/** Default filter for subsampling averaged. */
	public final static float[] DEFAULT_KERNEL_GAUSSIAN = new float[] { 0.5F,
			1.0F / 3.0F, 0.0F, -1.0F / 12.0F };

	/**
	 * Default border extender.
	 */
	public final static BorderExtender DEFAULT_BORDER_EXTENDER = BorderExtender
			.createInstance(BorderExtender.BORDER_COPY);

	/** Static immutable ap for scaling algorithms. */
	private static List scalingAlgorithms;
	static {
		scalingAlgorithms = new ArrayList(4);
		scalingAlgorithms.add("nn");
		scalingAlgorithms.add("bil");
		scalingAlgorithms.add("bic");
		scalingAlgorithms.add("avg");
		scalingAlgorithms.add("filt");
	}

	/** Program Version */
	private final static String versionNumber = "0.2";

	/** Commons-cli option for the input location. */
	private DefaultOption locationOpt;

	/** Commons-cli option for the output location. */
	private DefaultOption outputLocationOpt;

	/** Output folder, defaults to the "pyramid" subfolder */
	private File outputLocation;

	/** Commons-cli option for the tile dimension. */
	private DefaultOption tileDimOpt;

	/** Commons-cli option for the scale algorithm. */
	private DefaultOption scaleAlgorithmOpt;

	/** Commons-cli option for the tile cache size to use. */
	private DefaultOption tileCacheSizeOpt;

	/** Commons-cli option for the tile numbe of subsample step to use. */
	private DefaultOption numStepsOpt;

	/** Commons-cli option for the scale factor to use. */
	private DefaultOption scaleFactorOpt;

	/**
	 * Commons-cli options for overwriting the output layer dirs if already
	 * available
	 */
	private DefaultOption overwriteOpt;

	/** Tile width. */
	private int tileW = -1;

	/** Tile height. */
	private int tileH = -1;

	/** Scale algorithm. */
	private String scaleAlgorithm;

	/** Logger for this class. */
	private final static Logger LOGGER = Logger.getLogger(PyramidBuilder.class
			.toString());

	/** ImageIO caching behvaiour controller. */
	private boolean useImageIOCache = DEFAULT_IMAGEIO_CACHING_BEHAVIOUR;

	/** Default border extender. */
	private BorderExtender borderExtender = DEFAULT_BORDER_EXTENDER;

	/** Downsampling step. */
	private int scaleFactor;

	/** Default tile cache size. */
	private long tileCacheSize = DEFAULT_TILE_CHACHE_SIZE;

	/**
	 * The source path.
	 */
	private File inputLocation;

	/**
	 * The name of the output pyramid, will simply be "pyramid" if not set
	 */
	private String name;

	/**
	 * Commons-cli option for the pyramid name
	 */
	private DefaultOption nameOpt;

	/**
	 * 
	 * Interpolation method used througout all the program.
	 * 
	 * @TODO make the interpolation method customizable from the user
	 *       perpsective.
	 * 
	 */
	private Interpolation interp = DEFAULT_INTERPOLATION;

	private int numSteps;

	private boolean exceptionOccurred = false;

	private boolean overwriteOutputDirs = false;

	private double currStep = 0;

	private double totalSteps = 0;

	/**
	 * Relaunches slave tools progress with the appropriate percentage
	 * corrections
	 */
	private ProcessingEventListener slaveToolsListener = new ProcessingEventListener() {

		public void getNotification(ProcessingEvent event) {
			fireEvent(event.getMessage(), (currStep / totalSteps) * 100
					+ event.getPercentage() / totalSteps);
		}

		public void exceptionOccurred(ExceptionEvent event) {
			fireException(event.getMessage(), event.getPercentage(), event
					.getException());
			exceptionOccurred = true;
		}

	};

	private GeneralEnvelope envelope;

	private double[][] resolutions;

	/**
	 * Simple constructor for a pyramid generator. Use the input string in order
	 * to read an image.
	 * 
	 * 
	 */
	public PyramidBuilder() {
		// /////////////////////////////////////////////////////////////////////
		// Options for the command line
		// /////////////////////////////////////////////////////////////////////
		helpOpt = optionBuilder.withShortName("h").withShortName("?")
				.withLongName("helpOpt").withDescription("print this message.")
				.withRequired(false).create();

		versionOpt = optionBuilder.withShortName("v")
				.withLongName("versionOpt").withDescription(
						"print the versionOpt.").withRequired(false).create();

		locationOpt = optionBuilder
				.withShortName("s")
				.withLongName("source")
				.withArgument(
						arguments.withName("source").withMinimum(1)
								.withMaximum(1).withValidator(new Validator() {

									public void validate(List args)
											throws InvalidArgumentException {
										final int size = args.size();
										if (size > 1)
											throw new InvalidArgumentException(
													"Source can be a single file or directory ");
										final File source = new File(
												(String) args.get(0));
										if (!source.exists())
											throw new InvalidArgumentException(
													new StringBuffer(
															"The provided source is invalid! ")

													.toString());
									}

								}).create()).withDescription(
						"path where files are located").withRequired(true)
				.create();

		nameOpt = optionBuilder.withShortName("name").withLongName(
				"pyramid_name").withArgument(
				arguments.withName("name").withMinimum(0).withMaximum(1)
						.create()).withDescription(
				"name for the pyramid property file").withRequired(false)
				.create();

		tileDimOpt = optionBuilder.withShortName("t").withLongName(
				"tiled_dimension").withArgument(
				arguments.withName("t").withMinimum(0).withMaximum(1).create())
				.withDescription(
						"tile dimensions as a couple width,height in pixels")
				.withRequired(false).create();

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
											LOGGER
													.warning("The scale factor is 1, program will exit!");
											System.exit(0);
										}
									}

								}).create()).withDescription(
						"integer scale factor")
				.withRequired(true).create();

		numStepsOpt = optionBuilder.withShortName("n")
				.withLongName("num_steps").withArgument(
						arguments.withName("n").withMinimum(1).withMaximum(1)
								.withValidator(new Validator() {

									public void validate(List args)
											throws InvalidArgumentException {
										final int size = args.size();
										if (size > 1)
											throw new InvalidArgumentException(
													"Only one scaling algorithm at a time can be chosen");
										int steps = Integer
												.parseInt((String) args.get(0));
										if (steps <= 0)
											throw new InvalidArgumentException(
													new StringBuffer(
															"The provided scale factor is negative! ")

													.toString());

									}

								}).create()).withDescription(
						"integer scale factor").withRequired(true).create();

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
						"name of the scaling algorithm, eeither one of average (a), filtered (f), bilinear (bil), nearest neigbhor (nn)")
				.withRequired(false).create();

		priorityOpt = optionBuilder.withShortName("p").withLongName(
				"thread_priority").withArgument(
				arguments.withName("thread_priority").withMinimum(0)
						.withMaximum(1).create()).withDescription(
				"priority for the underlying thread").withRequired(false)
				.create();

		tileCacheSizeOpt = optionBuilder.withShortName("c").withLongName(
				"cache_size").withArgument(
				arguments.withName("c").withMinimum(0).withMaximum(1).create())
				.withDescription("tile cache sized").withRequired(false)
				.create();

		overwriteOpt = optionBuilder.withShortName("w").withLongName(
				"overwrite").withDescription(
				"completely wipe out existing layer dirs before proceeding.")
				.withRequired(false).create();

		cmdOpts.add(locationOpt);
		cmdOpts.add(tileDimOpt);
		cmdOpts.add(scaleFactorOpt);
		cmdOpts.add(scaleAlgorithmOpt);
		cmdOpts.add(numStepsOpt);
		cmdOpts.add(priorityOpt);
		cmdOpts.add(tileCacheSizeOpt);
		cmdOpts.add(versionOpt);
		cmdOpts.add(helpOpt);
		cmdOpts.add(overwriteOpt);

		optionsGroup = new GroupImpl(cmdOpts, "Options", "All the options", 0,
				9);

		// /////////////////////////////////////////////////////////////////////
		//
		// Help Formatter
		//
		// /////////////////////////////////////////////////////////////////////
		final HelpFormatter cmdHlp = new HelpFormatter("| ", "  ", " |", 75);
		cmdHlp.setShellCommand("PyramidBuilder");
		cmdHlp.setHeader("Help");
		cmdHlp.setFooter(new StringBuffer(
				"PyramidBuilder - GeoSolutions S.a.s (C) 2006 - v ").append(
				PyramidBuilder.versionNumber).toString());
		cmdHlp
				.setDivider("|-------------------------------------------------------------------------|");

		cmdParser.setGroup(optionsGroup);
		cmdParser.setHelpOption(helpOpt);
		cmdParser.setHelpFormatter(cmdHlp);
	}

	/**
	 * 
	 * This method is a utlity method for setting various JAi wide hints we will
	 * use here and afterwards.
	 * 
	 * 
	 */
	private void settingJAIHints() {

		// //
		// Imageio caching behaviour in case it is ever needed.
		// //
		ImageIO.setUseCache(useImageIOCache);

		// //
		//
		// JAI cache fine tuning
		//
		// //
		final JAI jaiDef = JAI.getDefaultInstance();
		// setting the tile cache
		final TileCache cache = jaiDef.getTileCache();
		cache.setMemoryCapacity(tileCacheSize);
		// setting JAI wide hints
		jaiDef.setRenderingHint(JAI.KEY_INTERPOLATION, this.interp);
		jaiDef.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		jaiDef.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
		jaiDef.setRenderingHint(JAI.KEY_CACHED_TILE_RECYCLING_ENABLED,
				Boolean.TRUE);
		// //
		//
		// tile factory and recycler
		//
		// //
		RecyclingTileFactory recyclingFactory = new RecyclingTileFactory();
		jaiDef.setRenderingHint(JAI.KEY_TILE_FACTORY, recyclingFactory);
		jaiDef.setRenderingHint(JAI.KEY_TILE_RECYCLER, recyclingFactory);

		// //
		//
		// border extender
		//
		// //
		jaiDef.setRenderingHint(JAI.KEY_BORDER_EXTENDER, borderExtender);

	}

	public void run() {
		// //
		//
		// setting JAI wide hints
		//
		// //
		settingJAIHints();

		// /////////////////////////////////////////////////////////////////////
		//
		// Gather reader to compute tile x and y from tile size
		//
		// /////////////////////////////////////////////////////////////////////

		AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(inputLocation);
		if (format == null) {
			String message = "Could not find a format for this coverage";
			fireException(message, 0, new IOException(message));
			return;
		}
		AbstractGridCoverage2DReader inReader = (AbstractGridCoverage2DReader) format
				.getReader(inputLocation);
		if (inReader == null) {
			String message = "Unable to instantiate a reader for this coverage";
			fireException(message, 0, new IOException(message));
			return;
		}

		envelope = inReader.getOriginalEnvelope();
		final GridRange range = inReader.getOriginalGridRange();
		final int numTileX = (int) Math.ceil(range.getLength(0) / tileW);
		final int numtileY = (int) Math.ceil(range.getLength(1) / tileH);
		inReader.dispose();


		// /////////////////////////////////////////////////////////////////////
		//
		// Create output directory
		//
		// /////////////////////////////////////////////////////////////////////

		if (!outputLocation.exists())
			if (!outputLocation.mkdir()) {
				String message = "Could not create output directory: "
						+ outputLocation;
				fireException(message, 0, new IOException(message));
				return;
			}

		// /////////////////////////////////////////////////////////////////////
		// 
		// Compute total steps and set current one so that slave tools progress
		// event percentages can be corrected to represent the global progress
		//
		// //////////////////////////////////////////////////////////////////////

		totalSteps = (numSteps + 1) * 2;
		currStep = 1;

		// /////////////////////////////////////////////////////////////////////
		//
		// Set up initial level using the coverage tiler
		//
		// /////////////////////////////////////////////////////////////////////

		File outputDir = new File(outputLocation, "0");
		if (!checkLayerDir(outputDir))
			return;

		// create first tiled set
		resolutions = new double[2][numSteps+1];
		tileInput(numTileX, numtileY, outputDir);
		if (exceptionOccurred)
			return;
		currStep++;

		// mosaic it
		double[] resolution = mosaicLevel(0);
		resolutions[0][0] = resolution[0];
		resolutions[1][0] = resolution[1];
		if (exceptionOccurred)
			return;
		currStep++;

		// /////////////////////////////////////////////////////////////////////
		//
		// Now do create a new level, and mosaic it, up to the final level
		//
		// /////////////////////////////////////////////////////////////////////
		int currLevel = scaleFactor;
		int prevLevel = 0;
		for (int step = 0; step < numSteps; step++) {
			// check output dir
			File prevLevelDirectory = new File(outputLocation, String
					.valueOf(prevLevel));
			File currLevelDirectory = new File(outputLocation, String
					.valueOf(currLevel));
			if (!checkLayerDir(currLevelDirectory))
				return;

			// create next tiled set
			buildNewLayer(prevLevelDirectory, currLevelDirectory);
			if (exceptionOccurred)
				return;
			currStep++;

			// mosaic it
			resolution = mosaicLevel(currLevel);
			resolutions[0][step+1] = resolution[0];
			resolutions[1][step+1] = resolution[1];
			if (exceptionOccurred)
				return;
			currStep++;

			// switch to next resolution level
			prevLevel = currLevel;
			currLevel *= scaleFactor;
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Finally, build the property file
		//
		// /////////////////////////////////////////////////////////////////////

		fireEvent("Creating final properties file ", 99.9);
		createPropertiesFiles();
		if (!exceptionOccurred)
			fireEvent("Done!!!", 100);
	}

	private boolean checkLayerDir(File outputDir) {
		if (!outputDir.exists())
			return true;
		if (!overwriteOutputDirs) {
			fireException(new IOException("Layer directory " + outputDir
					+ " already exist. Use -w to force its deletion"));
			return false;
		}
		try {
			FileUtils.deleteDirectory(outputDir);
		} catch (IOException e) {
			fireException(e);
			return false;
		}
		return true;
	}

	private void tileInput(final int numTileX, final int numtileY,
			File outputDir) {
		CoverageTiler tiler = new CoverageTiler();
		tiler.addProcessingEventListener(slaveToolsListener);
		tiler.setInputLocation(inputLocation);
		tiler.setOutputLocation(outputDir);
		tiler.setNumTileX(numTileX);
		tiler.setNumTileY(numtileY);
		tiler.run();
		tiler.removeAllProcessingEventListeners();
	}

	private void buildNewLayer(File prevLevelDirectory, File currLevelDirectory) {
		PyramidLayerBuilder layerBuilder = new PyramidLayerBuilder();
		layerBuilder.addProcessingEventListener(slaveToolsListener);
		layerBuilder
				.setInputLocation(new File(prevLevelDirectory, name+".shp"));
		layerBuilder.setOutputLocation(currLevelDirectory);
		layerBuilder.setScaleAlgorithm(scaleAlgorithm);
		layerBuilder.setScaleFactor(scaleFactor);
		layerBuilder.setTileH(tileH);
		layerBuilder.setTileW(tileW);
		layerBuilder.run();
		layerBuilder.removeAllProcessingEventListeners();
	}

	private double[] mosaicLevel(int level) {
		MosaicIndexBuilder builder = new MosaicIndexBuilder();
		builder.addProcessingEventListener(slaveToolsListener);
		builder.setLocationPath(new File(outputLocation, String.valueOf(level))
				.getAbsolutePath());
		builder.setIndexName(name);
		builder.run();
		builder.removeAllProcessingEventListeners();
		return new double[] { builder.getResolutionX(),
				builder.getResolutionY() };
	}

	/**
	 * @param envelope
	 * @param doneSomething
	 */
	private void createPropertiesFiles() {
		// envelope
		final Properties properties = new Properties();
		properties.setProperty("Envelope2D", new StringBuffer(Double
				.toString(envelope.getMinimum(0))).append(",").append(
				Double.toString(envelope.getMinimum(1))).append(" ").append(
				Double.toString(envelope.getMaximum(0))).append(",").append(
				Double.toString(envelope.getMaximum(1))).toString());
		properties.setProperty("LevelsNum", Integer.toString(numSteps+1));
		final StringBuffer levels = new StringBuffer();
		final StringBuffer levelDirs = new StringBuffer();
		for (int i = 0; i < numSteps+1; i++) {
			levels.append(Double.toString(resolutions[0][i])).append(",")
					.append(Double.toString(resolutions[1][i]));
			levelDirs.append(i == 0 ? "0" : Integer.toString((int) Math.pow(
					scaleFactor, i)));
			if (i < numSteps) {
				levels.append(" ");
				levelDirs.append(" ");
			}
		}
		properties.setProperty("Levels", levels.toString());
		properties.setProperty("LevelsDirs", levelDirs.toString());
		properties.setProperty("Name", name);
		try {
			properties.store(new BufferedOutputStream(new FileOutputStream(
					new File(outputLocation, name + ".properties"))), "");

			// //
			// Creating PRJ file
			// //
			File prjFile = new File(outputLocation, name + ".prj");
			BufferedWriter out = new BufferedWriter(new FileWriter(prjFile));
			out.write(envelope.getCoordinateReferenceSystem().toWKT());
			out.close();
		} catch (FileNotFoundException e) {
			fireException(e);
		} catch (IOException e) {
			fireException(e);
		}
	}

	public void getNotification(ProcessingEvent event) {
		LOGGER.info(new StringBuffer("Progress is at ").append(
				event.getPercentage()).append("\n").append(
				"attached message is: ").append(event.getMessage()).toString());
	}

	public void exceptionOccurred(ExceptionEvent event) {
		LOGGER.log(Level.SEVERE, "An error occurred during processing", event
				.getException());
	}

	private boolean parseArgs(String[] args) {
		cmdLine = cmdParser.parseAndHelp(args);
		if (cmdLine != null && cmdLine.hasOption(versionOpt)) {
			LOGGER.fine(new StringBuffer(
					"OverviewsEmbedder - GeoSolutions S.a.s (C) 2006 - v")
					.append(PyramidBuilder.versionNumber).toString());
			System.exit(1);

		} else if (cmdLine != null) {
			// ////////////////////////////////////////////////////////////////
			//
			// parsing command line parameters and setting up
			// Pyramid Builder options
			//
			// ////////////////////////////////////////////////////////////////
			inputLocation = new File((String) cmdLine.getValue(locationOpt));

			// output files' directory
			if (cmdLine.hasOption(outputLocationOpt))
				outputLocation = new File((String) cmdLine
						.getValue(outputLocationOpt));
			else
				outputLocation = new File(inputLocation.getParentFile(),
						"pyramid");

			// output file name
			if (cmdLine.hasOption(nameOpt))
				name = (String) cmdLine.getValue(nameOpt);
			else
				name = "pyramid";

			// shall we overwrite the output dirs?
			overwriteOutputDirs = cmdLine.hasOption(overwriteOpt);

			// tile dim
			if (cmdLine.hasOption(tileDimOpt)) {
				final String tileDim = (String) cmdLine.getValue(tileDimOpt);
				final String[] pairs = tileDim.split(",");
				tileW = Integer.parseInt(pairs[0]);
				tileH = Integer.parseInt(pairs[1]);
			}
			// //
			//
			// scale factor
			//
			// //
			final String scaleF = (String) cmdLine.getValue(scaleFactorOpt);
			scaleFactor = Integer.parseInt(scaleF);

			// //
			//
			// scaling algorithm (default to nearest neighbour)
			//
			// //
			scaleAlgorithm = (String) cmdLine.getValue(scaleAlgorithmOpt);
			if (scaleAlgorithm == null)
				scaleAlgorithm = "nn";

			// //
			//
			// number of steps
			//
			// //
			numSteps = Integer.parseInt((String) cmdLine.getValue(numStepsOpt));

			// //
			//
			// Thread priority
			//
			// //
			// index name
			if (cmdLine.hasOption(priorityOpt))
				priority = Integer.parseInt((String) cmdLine
						.getValue(priorityOpt));

			// //
			//
			// Tile cache size
			//
			// //
			// index name
			if (cmdLine.hasOption(tileCacheSizeOpt)) {
				tileCacheSize = Integer.parseInt((String) cmdLine
						.getValue(tileCacheSizeOpt));

			}
			return true;

		}
		return false;

	}

	/**
	 * This tool is designed to be used by the command line using this main
	 * class but it can also be used from an GUI by using the setters and
	 * getters.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IllegalArgumentException,
			IOException, InterruptedException {

		// creating an overviews embedder
		final PyramidBuilder builder = new PyramidBuilder();
		// adding the embedder itself as a listener
		builder.addProcessingEventListener(builder);
		// parsing input arguments
		if (builder.parseArgs(args)) {
			// creating a thread to execute the request process, with the
			// provided priority
			final Thread t = new Thread(builder, "PyramidBuilder");
			t.setPriority(builder.priority);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}

		} else if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Unable to parse command line arguments, exiting...");

	}
}
