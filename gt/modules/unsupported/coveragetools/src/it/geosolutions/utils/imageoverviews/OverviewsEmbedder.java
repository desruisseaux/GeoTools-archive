package it.geosolutions.utils.imageoverviews;

import it.geosolutions.utils.DefaultWriteProgressListener;
import it.geosolutions.utils.progress.ExceptionEvent;
import it.geosolutions.utils.progress.ProcessingEvent;
import it.geosolutions.utils.progress.ProcessingEventListener;
import it.geosolutions.utils.progress.ProgressManager;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RecyclingTileFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.TileCache;

import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.GroupImpl;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;
import org.apache.commons.io.filefilter.WildcardFilter;
import org.geotools.resources.image.ImageUtilities;

import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;

/**
 * <pre>
 *  Example of usage:
 * <code>
 * OverviewsEmbedder -s &quot;/usr/home/tmp&quot; -w *.tiff -t "512,512" -f 32 -n 8 -a nn -c 512
 * </code>
 *  &lt;pre&gt;
 *  
 * <p>
 *  HINT: Take more memory as the 64Mb default by using the following Java Options&lt;BR/&gt;
 * <code>
 * -Xmx1024M - Xms512M
 * </code>
 * </p>
 *  @author Simone Giannecchini (GeoSolutions)
 *  @author Alessio Fabiani (GeoSolutions)
 *  @since 2.3.x
 *  @version 0.2
 * 
 */
public class OverviewsEmbedder extends ProgressManager implements Runnable,
		ProcessingEventListener {

	/**
	 * 
	 * @author Simone Giannecchini
	 * @since 2.3.x
	 * 
	 */
	private class OverviewsEmbedderWriteProgressListener extends
			DefaultWriteProgressListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.geosolutions.pyramids.DefaultWriteProgressListener#imageComplete(javax.imageio.ImageWriter)
		 */
		public void imageComplete(ImageWriter source) {

			OverviewsEmbedder.this.fireEvent(new StringBuffer(
					"Started with writing out overview number ").append(
					overviewInProcess + 1.0).toString(),
					(overviewInProcess + 1 / numSteps) * 100.0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.geosolutions.pyramids.DefaultWriteProgressListener#imageProgress(javax.imageio.ImageWriter,
		 *      float)
		 */
		public void imageProgress(ImageWriter source, float percentageDone) {
			OverviewsEmbedder.this.fireEvent(new StringBuffer(
					"Writing out overview ").append(overviewInProcess + 1)
					.toString(), (overviewInProcess / numSteps + percentageDone
					/ (100 * numSteps)) * 100.0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.geosolutions.pyramids.DefaultWriteProgressListener#imageStarted(javax.imageio.ImageWriter,
		 *      int)
		 */
		public void imageStarted(ImageWriter source, int imageIndex) {
			OverviewsEmbedder.this.fireEvent(new StringBuffer(
					"Completed writing out overview number ").append(
					overviewInProcess + 1).toString(), (overviewInProcess)
					/ numSteps * 100.0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.geosolutions.pyramids.DefaultWriteProgressListener#warningOccurred(javax.imageio.ImageWriter,
		 *      int, java.lang.String)
		 */
		public void warningOccurred(ImageWriter source, int imageIndex,
				String warning) {
			OverviewsEmbedder.this.fireEvent(new StringBuffer(
					"Warning at overview ").append((overviewInProcess + 1))
					.toString(), 0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.geosolutions.pyramids.DefaultWriteProgressListener#writeAborted(javax.imageio.ImageWriter)
		 */
		public void writeAborted(ImageWriter source) {
			OverviewsEmbedder.this.fireEvent(new StringBuffer(
					"Aborted writing process.").toString(), 100.0);
		}
	}

	/**
	 * The default listener for checking the progress of the writing process.
	 */
	private final OverviewsEmbedderWriteProgressListener writeProgressListener = new OverviewsEmbedderWriteProgressListener();

	/**
	 * Default tile cache size.
	 */
	public final static long DEFAULT_TILE_CHACHE_SIZE = 64 * 1024 * 1024;

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

	/** Commons-cli option for the tile dimension. */
	private DefaultOption tileDimOpt;

	/** Commons-cli option for the scale algorithm. */
	private DefaultOption scaleAlgorithmOpt;

	/** Commons-cli option for the wild card to use. */
	private DefaultOption wildcardOpt;

	/** Commons-cli option for the tile cache size to use. */
	private DefaultOption tileCacheSizeOpt;

	/** Commons-cli option for the tile numbe of subsample step to use. */
	private DefaultOption numStepsOpt;

	/** Commons-cli option for the scale factor to use. */
	private DefaultOption scaleFactorOpt;

	/** Tile width. */
	private int tileW = -1;

	/** Tile height. */
	private int tileH = -1;

	/** Scale algorithm. */
	private String scaleAlgorithm;

	/** Logger for this class. */
	private final static Logger LOGGER = Logger
			.getLogger(OverviewsEmbedder.class.toString());

	/** Default number of resolution steps.. */
	public final int DEFAULT_RESOLUTION_STEPS = 5;

	/** ImageIO caching behvaiour controller. */
	private boolean useImageIOCache = DEFAULT_IMAGEIO_CACHING_BEHAVIOUR;

	/** Default border extender. */
	private BorderExtender borderExtender = DEFAULT_BORDER_EXTENDER;

	/** Downsampling step. */
	private int downsampleStep;

	/** Default tile cache size. */
	private long tileCacheSize = DEFAULT_TILE_CHACHE_SIZE;

	/** Low pass filter. */
	private float[] lowPassFilter = DEFAULT_KERNEL_GAUSSIAN;

	/**
	 * The source path. It could point to a single file or to a directory when
	 * we want to embed overwies into a set of files having a certain name.
	 */
	private String sourcePath;

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

	private String wildcardString = "*.*";

	private volatile int fileBeingProcessed;

	private volatile int overviewInProcess;

	/**
	 * Simple constructor for a pyramid generator. Use the input string in order
	 * to read an image.
	 * 
	 * 
	 */
	public OverviewsEmbedder() {
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
													"Source can be a single file or  directory ");
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

		wildcardOpt = optionBuilder.withShortName("w").withLongName(
				"wildcardOpt").withArgument(
				arguments.withName("wildcardOpt").withMinimum(0).withMaximum(1)
						.create()).withDescription(
				"wildcardOpt to use for selecting files").withRequired(false)
				.create();

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
						"name of the scaling algorithm, eeither one of average (a), filtered	 (f), bilinear (bil), nearest neigbhor (nn)")
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

		cmdOpts.add(locationOpt);
		cmdOpts.add(tileDimOpt);
		cmdOpts.add(scaleFactorOpt);
		cmdOpts.add(scaleAlgorithmOpt);
		cmdOpts.add(numStepsOpt);
		cmdOpts.add(wildcardOpt);
		cmdOpts.add(priorityOpt);
		cmdOpts.add(tileCacheSizeOpt);
		cmdOpts.add(versionOpt);
		cmdOpts.add(helpOpt);

		optionsGroup = new GroupImpl(cmdOpts, "Options", "All the options", 0,
				9);

		// /////////////////////////////////////////////////////////////////////
		//
		// Help Formatter
		//
		// /////////////////////////////////////////////////////////////////////
		final HelpFormatter cmdHlp = new HelpFormatter("| ", "  ", " |", 75);
		cmdHlp.setShellCommand("OverviewsEmbedder");
		cmdHlp.setHeader("Help");
		cmdHlp.setFooter(new StringBuffer(
				"OverviewsEmbedder - GeoSolutions S.a.s (C) 2006 - v ").append(
				OverviewsEmbedder.versionNumber).toString());
		cmdHlp
				.setDivider("|-------------------------------------------------------------------------|");

		cmdParser.setGroup(optionsGroup);
		cmdParser.setHelpOption(helpOpt);
		cmdParser.setHelpFormatter(cmdHlp);
	}

	/**
	 * This method retiles the original image using a specified tile wiedth and
	 * height.
	 * 
	 * @param Original
	 *            image to be tiled or retiled.
	 * @param tileWidth
	 *            Tile width.
	 * @param tileHeight
	 *            Tile height.
	 * @param tileGrdiOffseX
	 * @param tileGrdiOffseY
	 * @param interp
	 *            Interpolation method used.
	 * 
	 * @return RenderedOp containing the chain to obtain the tiled image.
	 */
	private ImageLayout tile(final int tileWidth, final int tileHeight,
			final int tileGrdiOffseX, final int tileGrdiOffseY,
			final Interpolation interp) {

		// //
		//
		// creating a new layout for this image
		// using tiling
		//
		// //
		ImageLayout layout = new ImageLayout();

		// //
		//
		// changing parameters related to the tiling
		//
		//
		// //
		layout.setTileGridXOffset(tileGrdiOffseX);
		layout.setTileGridYOffset(tileGrdiOffseY);
		layout.setValid(ImageLayout.TILE_GRID_X_OFFSET_MASK);
		layout.setValid(ImageLayout.TILE_GRID_Y_OFFSET_MASK);
		layout.setTileWidth(tileWidth);
		layout.setTileHeight(tileHeight);
		layout.setValid(ImageLayout.TILE_HEIGHT_MASK);
		layout.setValid(ImageLayout.TILE_WIDTH_MASK);

		return layout;
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
		cache.setMemoryCapacity(tileCacheSize * 1024 * 1024);
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

	/**
	 * This methods built up a RenderedOp for subsampling an image in order to
	 * create various previes. I wanted to use the filtered subsample but It was
	 * giving me problems in the native libraries therefore I am doing a two
	 * steps downsampling:
	 * 
	 * Step 1: low pass filtering.
	 * 
	 * Step 2: Subsampling.
	 * 
	 * @param src
	 *            Image to subsample.
	 * @param scale
	 *            Scale factor.
	 * @param interp
	 *            Interpolation method used.
	 * @param tileHints
	 *            Hints provided.
	 * 
	 * @return The subsampled RenderedOp.
	 */
	private RenderedOp subsample(RenderedOp src) {
		// using filtered subsample operator to do a subsampling
		final ParameterBlockJAI pb = new ParameterBlockJAI("filteredsubsample");
		pb.addSource(src);
		pb.setParameter("scaleX", new Integer(downsampleStep));
		pb.setParameter("scaleY", new Integer(downsampleStep));
		pb.setParameter("qsFilterArray", new float[] { 1.0f });
		pb.setParameter("Interpolation", interp);
		//remember to add the hint to avoid replacement of the original IndexColorModel
		//in future versions we might want to make this parametrix XXX TODO @task
		return JAI.create("filteredsubsample", pb,ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL);
	}

	public int getDownsampleStep() {
		return downsampleStep;
	}

	public void setDownsampleStep(int downsampleWH) {
		this.downsampleStep = downsampleWH;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;

	}

	public int getTileHeight() {
		return tileH;
	}

	public void setTileHeight(int tileHeight) {
		this.tileH = tileHeight;
	}

	public int getTileWidth() {
		return tileW;
	}

	public void setTileWidth(int tileWidth) {
		this.tileW = tileWidth;
	}

	/**
	 * Creating the scale operation using the FilteredSubSample operation with a
	 * null filter, which basically does not do any filtering. This is a hint I
	 * found on the JAI mailing list, a SUN engineer suggested to use this
	 * instead of scale since it uses a integer factor which is easier for the
	 * library to handle than a float scale factor like Scale operation is
	 * using.
	 * 
	 * @param src
	 *            Source image to be scaled.
	 * @param factor
	 *            Scale factor.
	 * @param interpolation
	 *            Interpolation used.
	 * @param hints
	 *            Hints provided to this method.
	 * 
	 * @return The scaled image.
	 */
	private RenderedOp filteredSubsample(RenderedImage src) {
		// using filtered subsample operator to do a subsampling
		final ParameterBlockJAI pb = new ParameterBlockJAI("filteredsubsample");
		pb.addSource(src);
		pb.setParameter("scaleX", new Integer(downsampleStep));
		pb.setParameter("scaleY", new Integer(downsampleStep));
		pb.setParameter("qsFilterArray", lowPassFilter);
		pb.setParameter("Interpolation", interp);
		return JAI.create("filteredsubsample", pb);
	}

	/**
	 * Creating the scale operation using the FilteredSubSample operation with a
	 * null filter, which basically does not do any filtering. This is a hint I
	 * found on the JAI mailing list, a SUN engineer suggested to use this
	 * instead of scale since it uses a integer factor which is easier for the
	 * library to handle than a float scale factor like Scale operation is
	 * using.
	 * 
	 * @param src
	 *            Source image to be scaled.
	 * @param factor
	 *            Scale factor.
	 * @param interpolation
	 *            Interpolation used.
	 * @param hints
	 *            Hints provided to this method.
	 * 
	 * @return The scaled image.
	 */
	private RenderedOp scaleAverage(RenderedImage src) {
		// using filtered subsample operator to do a subsampling
		final ParameterBlockJAI pb = new ParameterBlockJAI("SubsampleAverage");
		pb.addSource(src);
		pb.setParameter("scaleX", new Double(1.0 / downsampleStep));
		pb.setParameter("scaleY", new Double(1.0 / downsampleStep));
		return JAI.create("SubsampleAverage", pb);
	}

	public void setBorderExtender(BorderExtender borderExtender) {
		this.borderExtender = borderExtender;
	}

	public void setInterp(Interpolation interp) {
		this.interp = interp;
	}

	public void setTileCacheSize(long tileCacheSize) {
		this.tileCacheSize = tileCacheSize;
	}

	public void setUseImageIOCache(boolean useImageIOCache) {
		this.useImageIOCache = useImageIOCache;
	}

	public float[] getLowPassFilter() {
		return lowPassFilter;
	}

	public void setLowPassFilter(float[] lowPassFilter) {
		this.lowPassFilter = lowPassFilter;
	}

	public void run() {
		try {

			// //
			//
			// setting JAI wide hints
			//
			// //
			settingJAIHints();
			// final TCTool tool = new TCTool();

			// getting an image input stream to the file
			final File dir = new File(sourcePath);
			final File[] files;
			int numFiles = 1;
			StringBuffer message;
			if (dir.isDirectory()) {
				final FileFilter fileFilter = new WildcardFilter(wildcardString);
				files = dir.listFiles(fileFilter);
				numFiles = files.length;
				if (numFiles <= 0) {
					message = new StringBuffer("No files to process!");
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine(message.toString());
					}
					fireEvent(message.toString(), 100);

				}

			} else
				files = new File[] { dir };

			// /////////////////////////////////////////////////////////////////////
			//
			// Cycling over the features
			//
			// /////////////////////////////////////////////////////////////////////
			RenderedOp currentImage;
			ImageReader reader;
			ImageInputStream stream;
			Iterator it;
			ImageLayout layout;
			ImageOutputStream streamOut;
			RenderingHints newHints;
			ParameterBlock pbjRead;
			for (fileBeingProcessed = 0; fileBeingProcessed < numFiles; fileBeingProcessed++) {

				message = new StringBuffer("Managing file  ").append(
						fileBeingProcessed).append(" of ").append(
						files[fileBeingProcessed]).append(" files");
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine(message.toString());
				}
				fireEvent(message.toString(),
						((fileBeingProcessed * 100.0) / numFiles));

				if (getStopThread()) {
					message = new StringBuffer("Stopping requested at file  ")
							.append(fileBeingProcessed).append(" of ").append(
									numFiles).append(" files");
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine(message.toString());
					}
					fireEvent(message.toString(),
							((fileBeingProcessed * 100.0) / numFiles));
					return;
				}

				// //
				//
				// get a stream
				//
				//
				// //
				stream = ImageIO
						.createImageInputStream(files[fileBeingProcessed]);
				stream.mark();

				// //
				//
				// get a reader
				//
				//
				// //
				it = ImageIO.getImageReaders(stream);
				if (!it.hasNext()) {

					return;
				}
				reader = (ImageReader) it.next();
				stream.reset();
				stream.mark();

				// //
				//
				// set input
				//
				// //
				reader.setInput(stream);
				layout = null;
				// tiling the image if needed
				int actualTileW = reader.getTileWidth(0);
				int actualTileH = reader.getTileHeight(0);
				if (reader.isImageTiled(0) && (actualTileH != tileH)
						&& (actualTileW != tileW) && tileH != -1 && tileW != -1) {

					message = new StringBuffer("Retiling image  ")
							.append(fileBeingProcessed);
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine(message.toString());
					}
					fireEvent(message.toString(),
							((fileBeingProcessed * 100.0) / numFiles));
					layout = tile(tileW, tileH, 0, 0, interp);
				}
				stream.reset();
				reader.dispose();

				// //
				//
				// output image stream
				//
				// //
				streamOut = ImageIO
						.createImageOutputStream(files[fileBeingProcessed]);
				if (streamOut == null) {
					message = new StringBuffer(
							"Unable to acquire an ImageOutputStream for the file ")
							.append(files[fileBeingProcessed].toString());
					if (LOGGER.isLoggable(Level.SEVERE)) {
						LOGGER.severe(message.toString());
					}
					fireEvent(message.toString(),
							((fileBeingProcessed * 100.0) / numFiles));
					return;
				}

				// //
				//
				// Preparing to write the set of images. First of all I write
				// the first image `
				//
				// //
				// getting a writer for this reader
				ImageWriter writer = ImageIO.getImageWriter(reader);
				writer.setOutput(streamOut);
				writer.addIIOWriteProgressListener(writeProgressListener);
				ImageWriteParam param = writer.getDefaultWriteParam();

				// can we tile this image? (TIFF or JPEG2K)
				if (!(param.canWriteTiles())) {
					message = new StringBuffer(
							"This format do not support tiling!");
					if (LOGGER.isLoggable(Level.SEVERE)) {
						LOGGER.severe(message.toString());
					}
					fireEvent(message.toString(),
							((fileBeingProcessed * 100.0) / numFiles));
					return;
				}

				// can we write a sequence for these images?
				if (!(writer.canInsertImage(1))) {
					message = new StringBuffer(
							"This format do not support overviews!");
					if (LOGGER.isLoggable(Level.SEVERE)) {
						LOGGER.severe(message.toString());
					}
					fireEvent(message.toString(),
							((fileBeingProcessed * 100.0) / numFiles));
					return;

				}

				// //
				//
				// setting tiling on the first image using writing parameters
				//
				// //
				if (tileH != -1 & tileW != -1) {
					param.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
					param.setTiling(tileW, tileH, 0, 0);

				} else {
					param.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
					param.setTiling(actualTileW, actualTileH, 0, 0);
				}


				// //
				//
				// creating the image to use for the successive
				// subsampling
				//
				// //

				newHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
				pbjRead = new ParameterBlock();
				pbjRead.add(ImageIO
						.createImageInputStream(files[fileBeingProcessed]));
				pbjRead.add(new Integer(0));
				pbjRead.add(Boolean.FALSE);
				pbjRead.add(Boolean.FALSE);
				pbjRead.add(Boolean.FALSE);
				pbjRead.add(null);
				pbjRead.add(null);
				pbjRead.add(null);
				pbjRead.add(null);
				currentImage = JAI.create("ImageRead", pbjRead, newHints);
				message = new StringBuffer("Reaad original image  ")
						.append(fileBeingProcessed);
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine(message.toString());
				}
				fireEvent(message.toString(),
						((fileBeingProcessed * 100.0) / numFiles));
				for (overviewInProcess = 0; overviewInProcess < numSteps; overviewInProcess++) {

					if (overviewInProcess > 0) {

						// re-instantiate the current image from disk
						stream = ImageIO
								.createImageInputStream(files[fileBeingProcessed]);
						pbjRead = new ParameterBlock();
						pbjRead.add(stream);
						pbjRead.add(new Integer(overviewInProcess));
						pbjRead.add(Boolean.FALSE);
						pbjRead.add(Boolean.FALSE);
						pbjRead.add(Boolean.FALSE);
						pbjRead.add(null);
						pbjRead.add(null);
						pbjRead.add(null);
						pbjRead.add(null);
						currentImage = JAI.create("ImageRead", pbjRead,
								newHints);

						// //
						//
						// output image stream
						//
						// //
						streamOut = ImageIO
								.createImageOutputStream(files[fileBeingProcessed]);

						// //
						//
						// Preparing to write the set of images. First of all I
						// write
						// the first image `
						//
						// //
						// getting a writer for this reader
						writer = ImageIO.getImageWriter(reader);
						writer.setOutput(streamOut);
						writer
								.addIIOWriteProgressListener(writeProgressListener);
						param = writer.getDefaultWriteParam();

					}

					message = new StringBuffer("Subsampling step ").append(
							overviewInProcess).append(" of image  ").append(
							fileBeingProcessed);
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine(message.toString());
					}
					fireEvent(message.toString(),
							((fileBeingProcessed * 100.0) / numFiles));

					// paranoiac check
					if (currentImage.getWidth() / downsampleStep <= 0
							|| currentImage.getHeight() / downsampleStep <= 0)
						break;

					// subsampling the input image using the chosen algorithm
					if (scaleAlgorithm.equalsIgnoreCase("avg"))
						currentImage = scaleAverage(currentImage);
					else if (scaleAlgorithm.equalsIgnoreCase("filt"))
						currentImage = filteredSubsample(currentImage);
					if (scaleAlgorithm.equalsIgnoreCase("bil"))
						currentImage = bilinear(currentImage);
					if (scaleAlgorithm.equalsIgnoreCase("nn"))
						currentImage = subsample(currentImage);
					if (scaleAlgorithm.equalsIgnoreCase("bic"))
						currentImage = bicubic(currentImage);

					// write out
					writer.writeInsert(overviewInProcess + 1, new IIOImage(
							currentImage, null, null), param);

					message = new StringBuffer("Step ").append(
							overviewInProcess).append(" of image  ").append(
							fileBeingProcessed).append(" done!");
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine(message.toString());
					}
					fireEvent(message.toString(),
							((fileBeingProcessed * 100.0) / numFiles));

					// flushing cache
					JAI.getDefaultInstance().getTileCache().flush();

					// free everything
					streamOut.flush();
					streamOut.close();
					writer.dispose();
					currentImage.dispose();
					stream.close();

				}
				message = new StringBuffer("Done with  image  ")
						.append(fileBeingProcessed);
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine(message.toString());
				}
				fireEvent(message.toString(),
						(((fileBeingProcessed + 1) * 100.0) / numFiles));

			}

		} catch (IOException e) {
            fireException(e);
		}

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Done!!!");

	}

	/**
	 * Performs a bilinear interpolation on the provided image.
	 * 
	 * @param src
	 *            The source image.
	 * @return The subsampled image.
	 */
	private RenderedOp bilinear(RenderedOp src) {
		// using filtered subsample operator to do a subsampling
		final ParameterBlockJAI pb = new ParameterBlockJAI("filteredsubsample");
		pb.addSource(src);
		pb.setParameter("scaleX", new Integer(downsampleStep));
		pb.setParameter("scaleY", new Integer(downsampleStep));
		pb.setParameter("qsFilterArray", new float[] { 1.0f });
		pb.setParameter("Interpolation", new InterpolationBilinear());
		return JAI.create("filteredsubsample", pb);
	}

	/**
	 * Performs a bicubic interpolation on the provided image.
	 * 
	 * @param src
	 *            The source image.
	 * @return The subsampled image.
	 */
	private RenderedOp bicubic(RenderedOp src) {
		// using filtered subsample operator to do a subsampling
		final ParameterBlockJAI pb = new ParameterBlockJAI("filteredsubsample");
		pb.addSource(src);
		pb.setParameter("scaleX", new Integer(downsampleStep));
		pb.setParameter("scaleY", new Integer(downsampleStep));
		pb.setParameter("qsFilterArray", new float[] { 1.0f });
		pb.setParameter("Interpolation", new InterpolationBicubic(2));
		return JAI.create("filteredsubsample", pb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.geosolutions.utils.progress.ProcessingEventListener#getNotification(it.geosolutions.utils.progress.ProcessingEvent)
	 */
	public void getNotification(ProcessingEvent event) {
		LOGGER.info(new StringBuffer("Progress is at ").append(
				event.getPercentage()).append("\n").append(
				"attached message is: ").append(event.getMessage()).toString());

	}
    
    public void exceptionOccurred(ExceptionEvent event) {
        LOGGER.log(Level.SEVERE, "An error occurred during processing", event.getException());
    }

	private boolean parseArgs(String[] args) {
		cmdLine = cmdParser.parseAndHelp(args);
		if (cmdLine != null && cmdLine.hasOption(versionOpt)) {
			LOGGER.fine(new StringBuffer(
					"OverviewsEmbedder - GeoSolutions S.a.s (C) 2006 - v")
					.append(OverviewsEmbedder.versionNumber).toString());
			System.exit(1);

		} else if (cmdLine != null) {
			// ////////////////////////////////////////////////////////////////
			//
			// parsing command line parameters and setting up
			// Mosaic Index Builder options
			//
			// ////////////////////////////////////////////////////////////////
			sourcePath = (String) cmdLine.getValue(locationOpt);

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
			downsampleStep = Integer.parseInt(scaleF);

			// //
			//
			// wildcard
			//
			// //
			if (cmdLine.hasOption(wildcardOpt))
				wildcardString = (String) cmdLine.getValue(wildcardOpt);

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
		final OverviewsEmbedder overviewsEmbedder = new OverviewsEmbedder();
		// adding the embedder itself as a listener
		overviewsEmbedder.addProcessingEventListener(overviewsEmbedder);
		// parsing input arguments
		if (overviewsEmbedder.parseArgs(args)) {
			// creating a thread to execute the request process, with the
			// provided priority
			final Thread t = new Thread(overviewsEmbedder, "OverviewsEmbedder");
			t.setPriority(overviewsEmbedder.priority);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}

		} else if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Unable to parse command line arguments, exiting...");

	}

	/**
	 * Sets the wildcar string to use.
	 * 
	 * @param wildcardString
	 *            the wildcardString to set
	 */
	public final void setWildcardString(String wildcardString) {
		this.wildcardString = wildcardString;
	}
}
