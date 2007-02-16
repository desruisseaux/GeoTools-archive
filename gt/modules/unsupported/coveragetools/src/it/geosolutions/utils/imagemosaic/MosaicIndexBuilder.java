package it.geosolutions.utils.imagemosaic;

import it.geosolutions.utils.progress.ExceptionEvent;
import it.geosolutions.utils.progress.ProcessingEvent;
import it.geosolutions.utils.progress.ProcessingEventListener;
import it.geosolutions.utils.progress.ProgressManager;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.GroupImpl;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFilter;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.feature.type.TextualAttributeType;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.resources.CRSUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**                                                                                                                  
 * This classis in charge for creating the index for a mosaic of images that we                                      
 * want to tie together as a sigle bg coverage.                                                                      
 *                                                                                                                   
 * <p>                                                                                                               
 * To get instructions on how to run the toll just run it without any arguments                                      
 * and nice and clean help will be printed to the command line.                                                      
 *                                                                                                                   
 *                                                                                                                   
 * <p>                                                                                                               
 * It is worth to point out that this tool comes as a command line tool but it                                       
 * has been built with in mind a GUI. It has the capapbility to register                                             
 * {@link ProcessingEventListener} object that receive notifications about what                                      
 * is going on. Moreover it delegates all the computations to an external                                            
 * thread, hence we can stop the tool in the middle of processig with no so many                                     
 * concerns (hopefully :-) ).                                                                                        
 * <p>                                                                                                               
 *                                                                                                                   
 * <p>                                                                                                               
 *                                                                                                                   
 * @author Simone Giannecchini                                                                         
 * @author Alessio Fabiani                                                                             
 * @author Blaz Repnik                                                                                               
 * @version 0.3                                                                                                      
 *                                                                                                                   
 */
public class MosaicIndexBuilder extends ProgressManager implements Runnable,
		ProcessingEventListener {

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
	private final static Logger LOG = Logger.getLogger(MosaicIndexBuilder.class
			.toString());

	/** Program Version */
	private final static String versionNumber = "0.2";

	private static final double EPS = 1E-4;

	private final DefaultOption locationOpt;

	private String locationPath;

	private final DefaultOption wildcardOpt;

	private String wildcardString = "*.*";

	private DefaultOption nameOpt;

	/**
	 * Index file name. Default is index.
	 */
	private String indexName = "index";

	/**
	 * This field will tell the plugin if it must do a conversion of color from
	 * the original index color model to an RGB color model. This happens f the
	 * original images uses different color maps between each other making for
	 * us impossible to reuse it for the mosaic.
	 */
	private boolean mustConvertToRGB = false;

	private ColorModel actualCM = null;

	private ColorModel defaultCM = null;

	private SampleModel defaultSM = null;

	private SampleModel actualSM = null;

	private GeneralEnvelope globEnvelope = null;

	private GeneralEnvelope envelope = null;

	private byte[][] defaultPalette = null;

	private CoordinateReferenceSystem defaultCRS = null;

	private CoordinateReferenceSystem actualCRS = null;

	/**
	 * Recurses the directory tree and returns valid files.
	 */
	private void recurse(List allFiles, String locationPath) {
		File dir = new File(locationPath);
		FileFilter fileFilter = new WildcardFilter(wildcardString);
		File[] files = dir.listFiles(fileFilter);
		File[] dirs = dir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		final int filesLength = files.length;
		for (int i = 0; i < filesLength; i++) {
			allFiles.add(files[i]);
		}
		final int dirsLength = dirs.length;
		for (int i = 0; i < dirsLength; i++) {
			recurse(allFiles, locationPath + '/' + dirs[i].getName());
		}
	}

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
            fireException(el);
            return;
		} catch (IOException el) {
            fireException(el);
            return;
		}
        
		// /////////////////////////////////////////////////////////////////////
        //
        // Create a set of file names that have to be skipped since these are
        // our metadata files
        //
        // /////////////////////////////////////////////////////////////////////
        Set skipFiles = new HashSet(Arrays.asList(new String[] {indexName + ".shp", 
                indexName + ".dbf", indexName + ".shx", indexName + ".prj", "error.txt", 
                "error.txt.lck", indexName + ".properties"      
        }));

		// /////////////////////////////////////////////////////////////////////
		//
		// Creating temp vars
		//
		// /////////////////////////////////////////////////////////////////////
		ShapefileDataStore index = null;
		FeatureWriter fw = null;
		Feature feature = null;
		Transaction t = new DefaultTransaction();
		// declaring a preciosion model to adhere the java double type
		// precision
		PrecisionModel precMod = new PrecisionModel(PrecisionModel.FLOATING);
		GeometryFactory geomFactory = new GeometryFactory(precMod);
		try {
			index = new ShapefileDataStore(new File(locationPath + "/"
					+ indexName + ".shp").toURL());
		} catch (MalformedURLException ex) {
			if (LOG.isLoggable(Level.SEVERE))
				LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            fireException(ex);
			return;
		}

		/** Fixed local variables * */
		AbstractGridCoverage2DReader reader;
		ImageInputStream inStream;
		ImageTypeSpecifier its;
		Iterator it;
		ImageReader r;
		double[] res;
		boolean skipFeature = false;
		double resX = 0, resY = 0;
		boolean doneSomething = false;

		// final File dir = new File(locationPath);
		List files = new ArrayList(25);
		recurse(files, locationPath);

		StringBuffer message;
		File fileBeingProcessed;
		// /////////////////////////////////////////////////////////////////////
		//
		// Cycling over the features
		//
		// /////////////////////////////////////////////////////////////////////
		numFiles = files.size();
		String validFileName = null;
		final Iterator filesIt = files.iterator();
		for (int i = 0; i < numFiles; i++) {
			fileBeingProcessed = ((File) filesIt.next());
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
				fireEvent(message.toString(), ((i * 100.0) / numFiles));
				return;
			} // replacing chars on input path
			try {
				validFileName = fileBeingProcessed.getCanonicalPath();
			} catch (IOException e1) {
                fireException(e1);
				return;
			}
			validFileName = validFileName.replace('\\', '/');
			validFileName = validFileName.substring(locationPath.length() + 1,
					fileBeingProcessed.getAbsolutePath().length());
            if(skipFiles.contains(validFileName))
                continue;
			message = new StringBuffer("Now indexing file ")
					.append(validFileName);

			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(message.toString());
			}
			fireEvent(message.toString(), ((i * 100.0) / numFiles));
			try {
				// ////////////////////////////////////////////////////////
				//
				//
				// STEP 1
				// Getting an ImageIO reader for this coverage.
				//
				//
				// ////////////////////////////////////////////////////////
				inStream = ImageIO.createImageInputStream(fileBeingProcessed);
				inStream.mark();
				it = ImageIO.getImageReaders(inStream);

				if (it.hasNext()) {
					r = (ImageReader) it.next();
					r.setInput(inStream);
				} else {
					message = new StringBuffer("Skipped file ").append(
							files.get(i)).append(
							":No ImageIO readeres avalaible.");
					if (LOG.isLoggable(Level.INFO))
						LOG.info(message.toString());
					fireEvent(message.toString(), ((i * 99.0) / numFiles));
					continue;
				}

				// ////////////////////////////////////////////////////////
				//
				//
				// STEP 2
				// Getting a coverage reader for this coverage. Right now we can
				// index
				// Geotiff or WorldImage
				//
				//
				// ////////////////////////////////////////////////////////
				if (LOG.isLoggable(Level.FINE))
					LOG.fine(new StringBuffer("Getting a reader").toString());
				AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
						.findFormat(files.get(i));
				if (format == null) {
					message = new StringBuffer("Skipped file ").append(
							files.get(i)).append(
							": File format is not supported.");
					if (LOG.isLoggable(Level.INFO))
						LOG.info(message.toString());
					fireEvent(message.toString(), ((i * 99.0) / numFiles));
					continue;
				}
				reader = (AbstractGridCoverage2DReader) format.getReader(files
						.get(i));

				envelope = (GeneralEnvelope) reader.getOriginalEnvelope();
				actualCRS = reader.getCrs();

				// /////////////////////////////////////////////////////////////////////
				//
				// STEP 3
				// Get the type specifier for this image and the check that the
				// image has the correct sample model and color model.
				// If this is the first cycle of the loop we initialize
				// eveything.
				//
				// /////////////////////////////////////////////////////////////////////
				its = ((ImageTypeSpecifier) r.getImageTypes(0).next());
				if (globEnvelope == null) {
					// /////////////////////////////////////////////////////////////////////
					//
					// at the first step we initialize everything that we will
					// reuse afterwards starting with color models, sample
					// models, crs, etc....
					//
					// /////////////////////////////////////////////////////////////////////

					defaultCM = its.getColorModel();
					if (defaultCM instanceof IndexColorModel) {
						IndexColorModel icm = (IndexColorModel) defaultCM;
						int numBands = defaultCM.getNumColorComponents();
						defaultPalette = new byte[3][icm.getMapSize()];
						icm.getReds(defaultPalette[0]);
						icm.getGreens(defaultPalette[0]);
						icm.getBlues(defaultPalette[0]);
						if (numBands == 4)
							icm.getAlphas(defaultPalette[0]);

					}
					defaultSM = its.getSampleModel();
					defaultCRS = actualCRS;
					globEnvelope = new GeneralEnvelope(envelope);
					// /////////////////////////////////////////////////////////////////////
					//
					// getting information about resolution
					//
					// /////////////////////////////////////////////////////////////////////

					// //
					//
					// get the dimension of the hr image and build the model
					// as well as
					// computing the resolution
					// //
					// resetting reader and recreating stream, turnarounf for a
					// strange iamgeio bug
					r.reset();
					inStream.reset();
					r.setInput(inStream);
					numberOfLevels = r.getNumImages(true);
					resolutionLevels = new double[2][numberOfLevels];
					res = getResolution(envelope, new Rectangle(r.getWidth(0),
							r.getHeight(0)), defaultCRS);
					resX = res[0];
					resY = res[1];
					resolutionLevels[0][0] = res[0];
					resolutionLevels[1][0] = res[1];

					// resolutions levels
					if (numberOfLevels > 1) {

						for (int k = 0; k < numberOfLevels; k++) {
							res = getResolution(envelope, new Rectangle(r
									.getWidth(k), r.getHeight(k)), defaultCRS);
							resolutionLevels[0][k] = res[0];
							resolutionLevels[1][k] = res[1];
						}
					}

					// /////////////////////////////////////////////////////////////////////
					//
					// creating the schema
					//
					// /////////////////////////////////////////////////////////////////////
					final GeometricAttributeType refGeom = new GeometricAttributeType(
							"the_geom", Polygon.class, true, null, defaultCRS,
							null);
					final TextualAttributeType locationAttribute = new TextualAttributeType(
							"location", true, 1, 1, "none", null);
					final FeatureTypeBuilder builder = FeatureTypeBuilder
							.newInstance("index");
					builder.setDefaultGeometry(refGeom);
					builder.addType(locationAttribute);

					FeatureType ftType = null;
					try {
						ftType = builder.getFeatureType();
					} catch (SchemaException e) {
						if (LOG.isLoggable(Level.SEVERE))
							LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
						fireEvent(e.getLocalizedMessage(), 0);
						return;
					}
					// create the schema for the new shape file
					index.createSchema(ftType);

					// get a feature writer
					fw = index.getFeatureWriter(index.getTypeNames()[0], t);
				} else {
					// ////////////////////////////////////////////////////////
					// 
					// comparing ColorModel
					// comparing SampeModel
					// comparing CRSs
					// ////////////////////////////////////////////////////////
					globEnvelope.add(envelope);
					actualCM = its.getColorModel();
					actualSM = its.getSampleModel();
					skipFeature = (i > 0 ? !(CRSUtilities.equalsIgnoreMetadata(
							defaultCRS, actualCRS)) : false);
					if (skipFeature)
						LOG.warning(new StringBuffer("Skipping image ").append(
								files.get(i)).append(
								" because CRSs do not match.").toString());
					skipFeature = checkColorModels(defaultCM, defaultPalette,
							actualCM);
					if (skipFeature)
						LOG.warning(new StringBuffer("Skipping image ").append(
								files.get(i)).append(
								" because color models do not match.")
								.toString());
					// defaultCM.getNumComponents()==actualCM.getNumComponents()&&
					// defaultCM.getClass().equals(actualCM.getClass())
					// && defaultSM.getNumBands() == actualSM
					// .getNumBands()
					// && defaultSM.getDataType() == actualSM
					// .getDataType() &&
					//
					// if (skipFeature)
					// LOG
					// .warning(new StringBuffer("Skipping image ")
					// .append(files.get(i))
					// .append(
					// " because cm or sm does not match.")
					// .toString());
					res = getResolution(envelope, new Rectangle(r.getWidth(0),
							r.getHeight(0)), defaultCRS);
					if (Math.abs((resX - res[0]) / resX) > EPS
							|| Math.abs(resY - res[1]) > EPS) {
						LOG.warning(new StringBuffer("Skipping image ").append(
								files.get(i)).append(
								" because resolutions does not match.")
								.toString());
						skipFeature = true;
					}
				}

				// ////////////////////////////////////////////////////////
				//
				// STEP 4
				//
				// create and store features
				//
				// ////////////////////////////////////////////////////////
				if (!skipFeature) {

					feature = fw.next();
					feature.setAttribute(0, geomFactory
							.toGeometry(new ReferencedEnvelope(envelope,
									actualCRS)));

					feature.setAttribute(1, validFileName);
					fw.write();

					message = new StringBuffer("Done with file ").append(files
							.get(i));
					if (LOG.isLoggable(Level.FINE)) {
						LOG.fine(message.toString());
					}
					message.append('\n');
					fireEvent(message.toString(), (((i + 1) * 99.0) / numFiles));
					doneSomething = true;
				} else
					skipFeature = false;

			} catch (IOException e) {
				fireException(e);
                break;
			} catch (ArrayIndexOutOfBoundsException e) {
                fireException(e);
                break;
			} catch (IllegalAttributeException e) {
                fireException(e);
                break;
			}

		}
		try {
			if (fw != null)
				fw.close();
			t.commit();
			t.close();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		createPropertiesFiles(globEnvelope, doneSomething);

	}

	/**
	 * @param globEnvelope
	 * @param doneSomething
	 */
	private void createPropertiesFiles(GeneralEnvelope globEnvelope,
			boolean doneSomething) {
		StringBuffer message;
		if (numFiles > 0 && doneSomething) {
			// /////////////////////////////////////////////////////////////////////
			//
			// FINAL STEP
			//
			// CREATING GENERAL INFO FILE
			//
			// /////////////////////////////////////////////////////////////////////
			message = new StringBuffer("Creating final properties file ");
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(message.toString());
			}
			fireEvent(message.toString(), 99.9);

			// envelope
			final Properties properties = new Properties();
			properties.setProperty("NumFiles", Integer.toString(numFiles));
			properties.setProperty("Envelope2D", new StringBuffer(Double
					.toString(globEnvelope.getMinimum(0))).append(",").append(
					Double.toString(globEnvelope.getMinimum(1))).append(" ")
					.append(Double.toString(globEnvelope.getMaximum(0)))
					.append(",").append(
							Double.toString(globEnvelope.getMaximum(1)))
					.toString());
			properties.setProperty("LevelsNum", Integer
					.toString(numberOfLevels));
			final StringBuffer levels = new StringBuffer();
			for (int k = 0; k < numberOfLevels; k++) {
				levels.append(Double.toString(resolutionLevels[0][k])).append(
						",").append(Double.toString(resolutionLevels[1][k]));
				if (k < numberOfLevels - 1)
					levels.append(" ");
			}
			properties.setProperty("Levels", levels.toString());
			properties.setProperty("Name", indexName);
			properties.setProperty("ExpandToRGB", Boolean
					.toString(mustConvertToRGB));
			try {
				properties.store(new BufferedOutputStream(new FileOutputStream(
						locationPath + "/" + indexName + ".properties")), "");
			} catch (FileNotFoundException e) {
				if (LOG.isLoggable(Level.SEVERE))
					LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
				fireEvent(e.getLocalizedMessage(), 0);
			} catch (IOException e) {
				if (LOG.isLoggable(Level.SEVERE))
					LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
				fireEvent(e.getLocalizedMessage(), 0);
			}

			// processing information
			message = new StringBuffer("Done!!!");
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(message.toString());
			}
			fireEvent(message.toString(), 100);
		} else {
			// processing information
			message = new StringBuffer("No file to process!!!");
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(message.toString());
			}
			fireEvent(message.toString(), 100);
		}
	}

	/**
	 * This method checks the {@link ColorModel} of the current image with the
	 * one of the first image in order to check if they are compatible or not in
	 * order to perform a mosaic operation.
	 * 
	 * <p>
	 * It is worth to point out that we also check if, in case we have two index
	 * color model image, we also try to suggest whether or not we should do a
	 * color expansion.
	 * 
	 * @param defaultCM
	 * @param defaultPalette
	 * @param actualCM
	 * @return a boolean asking to skip this feature.
	 */
	private boolean checkColorModels(ColorModel defaultCM,
			byte[][] defaultPalette, ColorModel actualCM) {

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// ComponentColorModel
		//
		//
		// /////////////////////////////////////////////////////////////////////
		if (defaultCM instanceof ComponentColorModel
				&& actualCM instanceof ComponentColorModel) {
			final ComponentColorModel defCCM = (ComponentColorModel) defaultCM, actualCCM = (ComponentColorModel) actualCM;
			return !(defCCM.getNumColorComponents() == actualCCM
					.getNumColorComponents()
					&& defCCM.hasAlpha() == actualCCM.hasAlpha()
					&& defCCM.getColorSpace().equals(actualCCM.getColorSpace())
					&& defCCM.getTransparency() == actualCCM.getTransparency() && defCCM
					.getTransferType() == actualCCM.getTransferType());
		}
		// /////////////////////////////////////////////////////////////////////
		//
		//
		// IndexColorModel
		//
		//
		// /////////////////////////////////////////////////////////////////////
		if (defaultCM instanceof IndexColorModel
				&& actualCM instanceof IndexColorModel) {
			final IndexColorModel defICM = (IndexColorModel) defaultCM, actualICM = (IndexColorModel) actualCM;
			if (defICM.getNumColorComponents() != actualICM
					.getNumColorComponents()
					|| defICM.hasAlpha() != actualICM.hasAlpha()
					|| !defICM.getColorSpace()
							.equals(actualICM.getColorSpace())
					|| defICM.getTransferType() != actualICM.getTransferType())
				return true;
			// ///
			//
			// Suggesting expansion in the simplest case
			//
			// ///
			if (defICM.getMapSize() != actualICM.getMapSize()
					|| defICM.getTransparency() != actualICM.getTransparency()
					|| defICM.getTransferType() != actualICM.getTransferType()
					|| defICM.getTransparentPixel() != actualICM
							.getTransparentPixel()) {
				mustConvertToRGB = true;
				return false;
			}
			// //
			//
			// Now checking palettes to see if we need to do a color convert
			//
			// //
			// get the palette for this color model
			int numBands = actualICM.getNumColorComponents();
			byte[][] actualPalette = new byte[3][actualICM.getMapSize()];
			actualICM.getReds(defaultPalette[0]);
			actualICM.getGreens(defaultPalette[0]);
			actualICM.getBlues(defaultPalette[0]);
			if (numBands == 4)
				actualICM.getAlphas(defaultPalette[0]);
			// compare them
			for (int i = 0; i < defICM.getMapSize(); i++)
				for (int j = 0; j < numBands; j++)
					if (actualPalette[j][i] != defaultPalette[j][i]) {
						mustConvertToRGB = true;
						break;
					}
			return false;

		}
		// //
		//
		// if we get here this means that the two color models where completely
		// different, hence skip this feature.
		//
		// //
		return true;
	}

	/**
	 * Default constructor
	 */
	public MosaicIndexBuilder() {
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

		nameOpt = optionBuilder.withShortName("name")
				.withLongName("index_name").withArgument(
						arguments.withName("name").withMinimum(0)
								.withMaximum(1).create()).withDescription(
						"name for the index file").withRequired(false).create();

		priorityOpt = optionBuilder.withShortName("p").withLongName(
				"thread_priority").withArgument(
				arguments.withName("priority").withMinimum(0).withMaximum(1)
						.create()).withDescription(
				"priority for the underlying thread").withRequired(false)
				.create();

		cmdOpts.add(helpOpt);
		cmdOpts.add(versionOpt);
		cmdOpts.add(locationOpt);
		cmdOpts.add(wildcardOpt);
		cmdOpts.add(nameOpt);
		cmdOpts.add(priorityOpt);

		optionsGroup = new GroupImpl(cmdOpts, "Options", "All the options", 1,
				10);

		// /////////////////////////////////////////////////////////////////////
		//
		// Help Formatter
		//
		// /////////////////////////////////////////////////////////////////////
		final HelpFormatter cmdHlp = new HelpFormatter("| ", "  ", " |", 75);
		cmdHlp.setShellCommand("MosaicIndexBuilder");
		cmdHlp.setHeader("Help");
		cmdHlp.setFooter(new StringBuffer(
				"MosaicIndexBuilder - GeoSolutions S.a.s (C) 2006 - v ")
				.append(MosaicIndexBuilder.versionNumber).toString());
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

		final MosaicIndexBuilder mosaicIndexBuilder = new MosaicIndexBuilder();
		mosaicIndexBuilder.addProcessingEventListener(mosaicIndexBuilder);
		if (mosaicIndexBuilder.parseArgs(args)) {
			final Thread t = new Thread(mosaicIndexBuilder,
					"MosaicIndexBuilder");
			t.setPriority(mosaicIndexBuilder.priority);
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
					"MosaicIndexBuilder - GeoSolutions S.a.s (C) 2006 - v")
					.append(MosaicIndexBuilder.versionNumber).toString());
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
			try {
				locationPath = inDir.getCanonicalPath();
				locationPath = locationPath.replace('\\', '/');
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return false;
			}
			// wildcard
			if (cmdLine.hasOption(wildcardOpt))
				wildcardString = (String) cmdLine.getValue(wildcardOpt);

			// index name
			if (cmdLine.hasOption(nameOpt))
				indexName = (String) cmdLine.getValue(nameOpt);

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
	 * This method is responsible for computing the resolutions in for the
	 * provided grid geometry in the provided crs.
	 * 
	 * <P>
	 * It is worth to note that the returned resolution array is of length of 2
	 * and it always is lon, lat for the moment.<br>
	 * It might be worth to remove the axes reordering code when we are
	 * confident enough with the code to handle the north-up crs.
	 * <p>
	 * TODO use orthodromic distance?
	 * 
	 * @param requestedEnvelope
	 * @param dim
	 * @param requestedRes
	 * @throws DataSourceException
	 */
	private double[] getResolution(GeneralEnvelope envelope, Rectangle2D dim,
			CoordinateReferenceSystem crs) throws DataSourceException {
		double[] requestedRes = null;
		try {
			if (dim != null && envelope != null) {
				// do we need to transform the originalEnvelope?
				final CoordinateReferenceSystem crs2D = CRSUtilities
						.getCRS2D(envelope.getCoordinateReferenceSystem());

				final boolean longitudeFirst = !GridGeometry2D.swapXY(crs2D
						.getCoordinateSystem());

				requestedRes = new double[2];
				requestedRes[0] = envelope.getLength(longitudeFirst ? 0 : 1)
						/ dim.getWidth();
				requestedRes[1] = envelope.getLength(longitudeFirst ? 1 : 0)
						/ dim.getHeight();
			}
			return requestedRes;
		} catch (TransformException e) {
			throw new DataSourceException("Unable to get the resolution", e);
		}

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
        LOG.log(Level.SEVERE, "An error occurred during processing", event.getException());
    }

	/**
	 * @param locationPath
	 *            the locationPath to set
	 */
	public final void setLocationPath(String locationPath) {
		this.locationPath = locationPath;
		final File inDir = new File(locationPath);
		if (!inDir.isDirectory()) {
			LOG.severe("Provided input dir does not exist or is not a dir!");
			throw new IllegalArgumentException(
					"Provided input dir does not exist or is not a dir!");
		}
		try {
			locationPath = inDir.getCanonicalPath();
			locationPath = locationPath.replace('\\', '/');
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			final IllegalArgumentException ex = new IllegalArgumentException();
			ex.initCause(e);
            throw ex;
		}
	}

	/**
	 * @param wildcardString
	 *            the wildcardString to set
	 */
	public final void setWildcardString(String wildcardString) {
		this.wildcardString = wildcardString;
	}

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    public double getResolutionX() {
        return this.resolutionLevels[0][0];
    }
    
    public double getResolutionY() {
        return this.resolutionLevels[1][0];
    }
}
