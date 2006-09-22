/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gce.imagemosaic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureSource;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;
import org.geotools.feature.Feature;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.resources.CRSUtilities;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This reader is repsonsible for providing access to mosaic of georeferenced
 * images. Citing JAI documentation:
 * 
 * The "Mosaic" operation creates a mosaic of two or more source images. This
 * operation could be used for example to assemble a set of overlapping
 * geospatially rectified images into a contiguous image. It could also be used
 * to create a montage of photographs such as a panorama.
 * 
 * All source images are assumed to have been geometrically mapped into a common
 * coordinate space. The origin (minX, minY) of each image is therefore taken to
 * represent the location of the respective image in the common coordinate
 * system of the sour ce images. This coordinate space will also be that of the
 * destination image.
 * 
 * All source images must have the same data type and sample size for all bands
 * and have the same number of bands as color components. The destination will
 * have the same data type, sample size, and number of bands and color
 * components as the sources.
 * 
 * 
 * @author Simone Giannecchini
 * @since 2.3
 * 
 */
public final class ImageMosaicReader extends AbstractGridCoverage2DReader
		implements GridCoverageReader {

	private final static Logger LOGGER = Logger
			.getLogger(ImageMosaicReader.class.toString());

	private final static Interpolation nnInterpolation = new InterpolationNearest();

	private final URL sourceURL;

	private final AbstractDataStore tileIndexStore;

	private SoftReference index;

	private final String typeName;

	private final FeatureSource featureSource;

	private final static RenderingHints NO_CACHE = new RenderingHints(
			JAI.KEY_TILE_CACHE, null);

	private static final int MAX_TILES = 300;

	/**
	 * COnstructor.
	 * 
	 * @param source
	 *            The source object.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * 
	 */
	public ImageMosaicReader(Object source, Hints uHints) throws IOException {
		// /////////////////////////////////////////////////////////////////////
		// 
		// Forcing longitude first since the geotiff specification seems to
		// assume that we have first longitude the latitude.
		//
		// /////////////////////////////////////////////////////////////////////
		if (uHints != null) {
			// prevent the use from reordering axes
			this.hints.add(uHints);
		}
		if (source == null) {

			final IOException ex = new IOException(
					"ImageMosaicReader:No source set to read this coverage.");
			throw new DataSourceException(ex);
		}
		this.source = source;

		// /////////////////////////////////////////////////////////////////////
		//
		// Check source
		//
		// /////////////////////////////////////////////////////////////////////
		if (source instanceof File)
			this.sourceURL = ((File) source).toURL();
		else if (source instanceof URL)
			this.sourceURL = (URL) source;
		else if (source instanceof String) {
			final File tempFile = new File((String) source);
			if (tempFile.exists()) {
				this.sourceURL = tempFile.toURL();
			} else
				try {
					this.sourceURL = new URL(URLDecoder.decode((String) source,
							"UTF8"));
					if (this.sourceURL.getProtocol() != "file") {
						throw new IllegalArgumentException(
								"This plugin accepts only File,  URL and String pointing to a file");

					}
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(
							"This plugin accepts only File,  URL and String pointing to a file");

				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException(
							"This plugin accepts only File,  URL and String pointing to a file");

				}

		} else
			throw new IllegalArgumentException(
					"This plugin accepts only File, URL and String pointing to a file");
		// /////////////////////////////////////////////////////////////////////
		//
		// Load tiles informations, especially the bounds, which will be
		// reused
		//
		// /////////////////////////////////////////////////////////////////////
		tileIndexStore = new ShapefileDataStore(this.sourceURL);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Connected mosaic reader to its data store "
					+ sourceURL.toString());
		final String[] typeNames = tileIndexStore.getTypeNames();
		if (typeNames.length <= 0)
			throw new IllegalArgumentException(
					"Problems when opening the index, no typenames for the schema are defined");

		typeName = typeNames[0];
		featureSource = tileIndexStore.getFeatureSource(typeName);

		// //
		//
		// Load all the features inside the index
		//
		// //
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("About to create index");
		index = new SoftReference(new MemorySpatialIndex(featureSource
				.getFeatures()));
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Created index");
		// //
		//
		// get the crs if able to
		//
		// //
		final CoordinateReferenceSystem tempcrs = featureSource.getSchema()
				.getDefaultGeometry().getCoordinateSystem();
		if (tempcrs == null) {
			// use the default crs
			crs = AbstractGridFormat.getDefaultCRS();
			LOGGER
					.log(
							Level.WARNING,
							new StringBuffer(
									"Unable to find a CRS for this coverage, using a default one: ")
									.append(crs.toWKT()).toString());
		} else
			crs = tempcrs;

		// /////////////////////////////////////////////////////////////////////
		//
		// Load properties file with information about levels and envelope
		//
		// /////////////////////////////////////////////////////////////////////
		// property file
		String temp = URLDecoder.decode(sourceURL.getFile(), "UTF8");
		final int index = temp.lastIndexOf(".");
		if (index != -1)
			temp = temp.substring(0, index);
		final File propertiesFile = new File(new StringBuffer(temp).append(
				".properties").toString());
		assert propertiesFile.exists() && propertiesFile.isFile();
		final Properties properties = new Properties();
		properties.load(new BufferedInputStream(new FileInputStream(
				propertiesFile)));

		// load the envelope
		final String envelope = properties.getProperty("Envelope2D");
		String[] pairs = envelope.split(" ");
		final double cornersV[][] = new double[2][2];
		String pair[];
		for (int i = 0; i < 2; i++) {
			pair = pairs[i].split(",");
			cornersV[i][0] = Double.parseDouble(pair[0]);
			cornersV[i][1] = Double.parseDouble(pair[1]);
		}
		this.originalEnvelope = new GeneralEnvelope(cornersV[0], cornersV[1]);
		this.originalEnvelope.setCoordinateReferenceSystem(crs);

		// resolutions levels
		numOverviews = Integer.parseInt(properties.getProperty("LevelsNum")) - 1;
		final String levels = properties.getProperty("Levels");
		pairs = levels.split(" ");
		overViewResolutions = numOverviews > 1 ? new double[numOverviews][2]
				: null;
		pair = pairs[0].split(",");
		highestRes = new double[2];
		highestRes[0] = Double.parseDouble(pair[0]);
		highestRes[1] = Double.parseDouble(pair[1]);

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Highest res ").append(highestRes[0])
					.append(" ").append(highestRes[1]).toString());

		for (int i = 1; i < numOverviews + 1; i++) {
			pair = pairs[i].split(",");
			overViewResolutions[i - 1][0] = Double.parseDouble(pair[0]);
			overViewResolutions[i - 1][1] = Double.parseDouble(pair[1]);
		}

		// name
		coverageName = properties.getProperty("Name");

		// original gridrange (estimated)
		originalGridRange = new GeneralGridRange(
				new Rectangle((int) Math.round(originalEnvelope.getLength(0)
						/ highestRes[0]), (int) Math.round(originalEnvelope
						.getLength(1)
						/ highestRes[1])));
	}

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            The source object.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * 
	 */
	public ImageMosaicReader(Object source) throws IOException {
		this(source, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
	 */
	public Format getFormat() {
		if (format == null)
			format = new ImageMosaicFormat();
		return format;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataNames()
	 */
	public String[] getMetadataNames() throws IOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
	 */
	public String getMetadataValue(String arg0) throws IOException,
			MetadataNameNotFoundException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#listSubNames()
	 */
	public String[] listSubNames() throws IOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#getCurrentSubname()
	 */
	public String getCurrentSubname() throws IOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
	 */
	public boolean hasMoreGridCoverages() throws IOException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] params) throws IOException {

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Reading mosaic from " + sourceURL.toString());
			LOGGER.fine(new StringBuffer("Highest res ").append(highestRes[0])
					.append(" ").append(highestRes[1]).toString());
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Checking params
		//
		// /////////////////////////////////////////////////////////////////////
		double alphaThreshold = ((Double) ImageMosaicFormat.ALPHA_THRESHOLD
				.getDefaultValue()).doubleValue();
		boolean alpha = ((Boolean) ImageMosaicFormat.FINAL_ALPHA
				.getDefaultValue()).booleanValue();
		boolean singleImageROI = ((Boolean) ImageMosaicFormat.INPUT_IMAGE_ROI
				.getDefaultValue()).booleanValue();
		int singleImageROIThreshold = ((Integer) ImageMosaicFormat.INPUT_IMAGE_ROI_THRESHOLD
				.getDefaultValue()).intValue();
		Parameter param = null;
		GeneralEnvelope requestedEnvelope = null;
		Rectangle dim = null;
		if (params != null) {
			final int length = params.length;
			for (int i = 0; i < length; i++) {
				param = (Parameter) params[i];

				if (param.getDescriptor().getName().getCode().equals(
						ImageMosaicFormat.READ_GRIDGEOMETRY2D.getName()
								.toString())) {
					final GridGeometry2D gg = (GridGeometry2D) param.getValue();
					requestedEnvelope = (GeneralEnvelope) gg.getEnvelope();
					dim = gg.getGridRange2D().getBounds();
				} else if (param.getDescriptor().getName().getCode().equals(
						ImageMosaicFormat.ALPHA_THRESHOLD.getName().toString())) {
					alphaThreshold = ((Double) param.getValue()).doubleValue();
				} else if (param.getDescriptor().getName().getCode().equals(
						ImageMosaicFormat.FINAL_ALPHA.getName().toString())) {
					alpha = ((Boolean) param.getValue()).booleanValue();
				} else if (param.getDescriptor().getName().getCode().equals(
						ImageMosaicFormat.INPUT_IMAGE_ROI.getName().toString())) {
					singleImageROI = ((Boolean) param.getValue())
							.booleanValue();
				} else if (param.getDescriptor().getName().getCode().equals(
						ImageMosaicFormat.INPUT_IMAGE_ROI_THRESHOLD.getName()
								.toString())) {
					singleImageROIThreshold = ((Integer) param.getValue())
							.intValue();
				}
			}
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Loading tiles
		//
		// /////////////////////////////////////////////////////////////////////
		return loadTiles(requestedEnvelope, alpha, alphaThreshold,
				singleImageROI, singleImageROIThreshold, dim);
	}

	/**
	 * Loading the tiles which overlap with the requested envelope.
	 * 
	 * @param envelope
	 * @param alphaThreshold
	 * @param alpha
	 * @param singleImageROIThreshold
	 * @param singleImageROI
	 * @param dim
	 * @return
	 * @throws IOException
	 */
	private GridCoverage loadTiles(GeneralEnvelope requestedEnvelope,
			boolean alpha, double alphaThreshold, boolean singleImageROI,
			int singleImageROIThreshold, Rectangle dim) throws IOException {

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer(
					"Creating mosaic to comply with envelope ").append(
					requestedEnvelope != null ? requestedEnvelope.toString()
							: null).append(" crs ").append(crs.toWKT()).append(
					" dim ").append(dim == null ? " null" : dim.toString())
					.toString());
		// /////////////////////////////////////////////////////////////////////
		//
		// Check if we have something to load by intersecting the requested
		// envelope with the bounds of the data set.
		//
		// If the requested envelope is not in the same crs of the data set crs
		// we have to perform a conversion towards the latter crs before
		// intersecting anything.
		//
		// /////////////////////////////////////////////////////////////////////
		if (requestedEnvelope != null) {
			if (!CRS.equalsIgnoreMetadata(requestedEnvelope
					.getCoordinateReferenceSystem(), this.crs)) {
				try {
					// transforming the envelope back to the data set crs
					final MathTransform transform = operationFactory
							.createOperation(
									requestedEnvelope
											.getCoordinateReferenceSystem(),
									crs).getMathTransform();
					if (!transform.isIdentity()) {
						requestedEnvelope = CRSUtilities.transform(transform,
								requestedEnvelope);
						requestedEnvelope
								.setCoordinateReferenceSystem(this.crs);

						if (LOGGER.isLoggable(Level.FINE))
							LOGGER.fine(new StringBuffer(
									"Reprojected envelope ").append(
									requestedEnvelope.toString()).append(
									" crs ").append(crs.toWKT()).toString());
					}
				} catch (TransformException e) {
					throw new DataSourceException(
							"Unable to create a coverage for this source", e);
				} catch (FactoryException e) {
					throw new DataSourceException(
							"Unable to create a coverage for this source", e);
				}
			}
			if (!requestedEnvelope.intersects(this.originalEnvelope, true))
				return null;

			// intersect the requested area with the bounds of this layer
			requestedEnvelope.intersect(originalEnvelope);

		} else {
			requestedEnvelope = new GeneralEnvelope(originalEnvelope);

		}
		requestedEnvelope.setCoordinateReferenceSystem(this.crs);
		// ok we got something to return, let's load records from the index
		// /////////////////////////////////////////////////////////////////////
		//
		// Prepare the filter for loading th needed layers
		//
		// /////////////////////////////////////////////////////////////////////
		final ReferencedEnvelope requestedJTSEnvelope = new ReferencedEnvelope(
				requestedEnvelope, crs);

		// /////////////////////////////////////////////////////////////////////
		//
		// Load feaures from the index
		//
		// /////////////////////////////////////////////////////////////////////

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("loading tile for envelope "
					+ requestedJTSEnvelope.toString());
		final List features =getFeaturesFromIndex(requestedJTSEnvelope);
		if(features==null)
		{
			return null;
		}
		// do we have any feature to load
		final Iterator it = features.iterator();
		if (!it.hasNext())
			return null;
		final int size=features.size();
		if (size > MAX_TILES)
			return fakeMosaic(requestedEnvelope,size);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("We have " + size + " tiles to load");
		try {
			return loadRequestedTiles(requestedEnvelope, alpha, alphaThreshold,
					requestedJTSEnvelope, features, it, singleImageROI,
					singleImageROIThreshold, dim);
		} catch (DataSourceException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (TransformException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return null;

	}

	private GridCoverage fakeMosaic(GeneralEnvelope requestedEnvelope,
			int features) {
		LOGGER.warning(new StringBuffer("We can load at most ").append(
				MAX_TILES).append(" tiles while there were requested ").append(
				features).append(
				"\nI am going to print out a fake coverage, sorry about it!")
				.toString());
		Rectangle dim = new Rectangle(800, 600);
		final BufferedImage image = new BufferedImage(dim.width, dim.height,
				BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g2D = (Graphics2D) image.getGraphics();
		g2D.setColor(Color.red);
		g2D.draw3DRect(0, 0, dim.width - 1, dim.height - 1, true);
		g2D.drawLine(0, 0, dim.width, dim.height);
		g2D.drawLine(0, dim.height, dim.width, 0);
		g2D.dispose();
		return coverageFactory.create(coverageName, image, requestedEnvelope);

	}

	/**
	 * This method loads the tiles which overlap the requested envelope using
	 * the provided values for alpha and input ROI.
	 * 
	 * @param requestedEnvelope
	 * @param alpha
	 * @param alphaThreshold
	 * @param requestedJTSEnvelope
	 * @param features
	 * @param it
	 * @param singleImageROI
	 * @param singleImageROIThreshold
	 * @param dim
	 * @return
	 * @throws DataSourceException
	 * @throws TransformException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws FactoryRegistryException
	 */
	private GridCoverage loadRequestedTiles(GeneralEnvelope requestedEnvelope,
			boolean alpha, double alphaThreshold,
			final Envelope requestedJTSEnvelope, final List features,
			final Iterator it, boolean singleImageROI,
			int singleImageROIThreshold, Rectangle dim)
			throws DataSourceException, TransformException {

		try {
			// if we get here we have something to load
			// /////////////////////////////////////////////////////////////////////
			//
			// prepare the mosaic params
			//
			// /////////////////////////////////////////////////////////////////////
			final ParameterBlockJAI pbjMosaic = new ParameterBlockJAI("Mosaic");
			pbjMosaic.setParameter("mosaicType",
					MosaicDescriptor.MOSAIC_TYPE_OVERLAY);

			// /////////////////////////////////////////////////////////////////////
			//
			// compute the requested resolution
			//
			// /////////////////////////////////////////////////////////////////////
			final ImageReadParam readP = new ImageReadParam();
			final Integer imageChoice;
			if (dim != null)
				imageChoice = setReadParams(readP, requestedEnvelope, dim);
			else
				imageChoice = new Integer(0);
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Loading level ").append(
						imageChoice.toString()).append(
						" with subsampling factors ").append(
						readP.getSourceXSubsampling()).append(" ").append(
						readP.getSourceYSubsampling()).toString());
			// /////////////////////////////////////////////////////////////////////
			//
			// Resolution.
			// I am implicitly assuming that all the images have the same
			// resolution. In principle this is not required but in practice
			// having
			// different resolution would surely bring to having small
			// displacements
			// in the final mosaic which we do not wnat to happen.
			// /////////////////////////////////////////////////////////////////////
			final double[] res;
			if (imageChoice.intValue() == 0) {
				res = new double[highestRes.length];
				res[0] = highestRes[0];
				res[1] = highestRes[1];
			} else
				res = overViewResolutions[imageChoice.intValue() - 1];
			// adjusting the resolution for the source subsampling
			res[0] *= readP.getSourceXSubsampling();
			res[1] *= readP.getSourceYSubsampling();

			// /////////////////////////////////////////////////////////////////////
			//
			// Loop over the single features and load the images which
			// intersect the requested envelope. Once all of them have been
			// loaded, next step is to create the mosaic and then
			// crop it as requested.
			//
			// /////////////////////////////////////////////////////////////////////
			final File tempFile = new File(this.sourceURL.getFile());
			final String parentLocation = tempFile.getParent();
			Feature feature;
			String location;
			Envelope bound;
			Envelope loadedDataSetBound = new Envelope();
			RenderedOp loadedImage;
			File imageFile;
			final int numImages = features.size();
			final ROI[] rois = new ROI[numImages];
			final PlanarImage[] alphaChannels = new PlanarImage[numImages];
			final Area finalLayout = new Area();
			// /////////////////////////////////////////////////////////////////////
			//
			// envelope of the loaded dataset
			//
			// /////////////////////////////////////////////////////////////////////
			final Point2D ULC = getULC(new Envelope(requestedJTSEnvelope
					.getMinX(), requestedJTSEnvelope.getMaxY(),
					requestedJTSEnvelope.getMinX(), requestedJTSEnvelope
							.getMaxY()));

			// reusable parameters
			boolean alphaIn = false;
			int[] alphaIndex = null;
			int i = 0;
			Boolean readMetadata = Boolean.FALSE;
			Boolean readThumbnails = Boolean.FALSE;
			Boolean verifyInput = Boolean.FALSE;

			do {
				// /////////////////////////////////////////////////////////////////////
				//
				// get location and envelope of the image to load.
				//
				// /////////////////////////////////////////////////////////////////////
				feature = (Feature) it.next();
				location = (String) feature.getAttribute("location");
				bound = feature.getBounds();
				loadedDataSetBound.expandToInclude(bound);

				// /////////////////////////////////////////////////////////////////////
				//
				// load a tile from disk as requested.
				//
				// /////////////////////////////////////////////////////////////////////
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("About to read image number " + i);
				imageFile = new File(new StringBuffer(parentLocation).append(
						File.separatorChar).append(location).toString());

				ParameterBlock pbjImageRead = new ParameterBlock();
				pbjImageRead.add(ImageIO.createImageInputStream(imageFile));
				pbjImageRead.add(imageChoice);
				pbjImageRead.add(readMetadata);
				pbjImageRead.add(readThumbnails);
				pbjImageRead.add(verifyInput);
				pbjImageRead.add(null);
				pbjImageRead.add(null);
				pbjImageRead.add(readP);
				pbjImageRead.add(null);
				loadedImage = JAI.create("ImageRead", pbjImageRead, null);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Just read image number " + i);

				// /////////////////////////////////////////////////////////////
				//
				// Input alpha management.
				//
				//
				//
				//
				// /////////////////////////////////////////////////////////////
				if (i == 0) {

					final ColorModel model = loadedImage.getColorModel();
					alphaIn = model.hasAlpha();
					if (alphaIn)
					// XXX good for RGBA but what about ARGB and others???
					{
						alphaIndex = new int[] { model.getNumComponents() - 1 };

						// we do not need final alpha since the initial images
						// have alpha channel therefore they background for the
						// mosaic will be transparent by default.
						alpha = false;
					}

				}

				// /////////////////////////////////////////////////////////////////////
				//
				// add to the mosaic collection
				//
				// /////////////////////////////////////////////////////////////////////
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Adding to mosaic image number " + i);
				addToMosaic(pbjMosaic, bound, ULC, res, loadedImage,
						singleImageROI, rois, i, singleImageROIThreshold,
						alphaIn, alphaIndex, alphaChannels, finalLayout,
						imageFile);

				i++;
			} while (i < numImages);

			// /////////////////////////////////////////////////////////////////////
			//
			// create the mosaic image
			// 
			// /////////////////////////////////////////////////////////////////////
			if (!alphaIn && singleImageROI) {
				pbjMosaic.setParameter("sourceThreshold",
						new double[][] { { 0 } });
				pbjMosaic.setParameter("sourceROI", rois);

			} else if (alphaIn) {
				pbjMosaic.setParameter("mosaicType",
						MosaicDescriptor.MOSAIC_TYPE_BLEND);
				pbjMosaic.setParameter("sourceAlpha", alphaChannels);

			}

			// prepare the final mosaic
			return prepareMosaic(location, requestedEnvelope,
					requestedJTSEnvelope, res, loadedDataSetBound, pbjMosaic,
					alpha, alphaThreshold, finalLayout);
		} catch (IOException e) {
			throw new DataSourceException("Unable to create this mosaic", e);
		}
	}

	/**
	 * Retrieves the ULC of the BBOX composed by all the tiles we need to load.
	 * 
	 * @param double *
	 * @return
	 * @throws IOException
	 */
	private Point2D getULC(Envelope envelope) throws IOException {
		// /////////////////////////////////////////////////////////////////////
		//
		// Load feaures and evaluate envelope
		//
		// /////////////////////////////////////////////////////////////////////
		List features = getFeaturesFromIndex(envelope);
		final Envelope loadedULC = new Envelope();
		Iterator it = features.iterator();
		while (it.hasNext()) {
			loadedULC.expandToInclude(((Feature) it.next())
					.getDefaultGeometry().getEnvelopeInternal());
		}
		return new Point2D.Double(loadedULC.getMinX(), loadedULC.getMaxY());

	}

	/**
	 * Retrieves the list of features that intersect the provided evelope
	 * loadinf them inside an index in memory where beeded.
	 * 
	 * @param envelope
	 *            Envelope for selectig features that intersect.
	 * @return A list of fetaures.
	 * @throws IOException
	 *             In case loading the needed features failes.
	 */
	private List getFeaturesFromIndex(final Envelope envelope)
			throws IOException {
		List features = null;
		Object o;
		synchronized (index) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Trying to  use the index...");
			o = index.get();
			if (o != null) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Index does not need to be created...");
				;
			} else {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Index needa to be recreated...");
				o = new MemorySpatialIndex(featureSource.getFeatures());
			}
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Index Loaded");
		}
		features = ((MemorySpatialIndex) o).findFeatures(envelope);
		return features;
	}

	/**
	 * Once we reach this method it means that we have loaded all the images
	 * which were intersecting the requested nevelope. Next step is to create
	 * the final mosaic image and cropping it to the exact requested envelope.
	 * 
	 * @param location
	 * 
	 * @param envelope
	 * @param requestedEnvelope
	 * @param res
	 * @param loadedTilesBound
	 * @param pbjMosaic
	 * @param alphaThreshold
	 * @param doAlpha
	 * @param finalLayout
	 * @param singleImageROI
	 * @return
	 * @throws IllegalArgumentException
	 * @throws FactoryRegistryException
	 * @throws DataSourceException
	 */
	private GridCoverage prepareMosaic(String location,
			GeneralEnvelope requestedEnvelope,
			final Envelope requestedJTSEnvelope, double[] res,
			Envelope loadedTilesBound, ParameterBlockJAI pbjMosaic,
			boolean doAlpha, double alphaThreshold, Area finalLayout)
			throws DataSourceException {

		RenderedOp imageBeforeAlpha;
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Loaded bbox ").append(
					loadedTilesBound.toString()).append(
					" while requested bbox ").append(
					requestedJTSEnvelope.toString()).toString());
		if (!requestedJTSEnvelope.equals(loadedTilesBound)) {
			// /////////////////////////////////////////////////////////////////////
			//
			// CROP the mosaic image to the requested BBOX
			//
			// /////////////////////////////////////////////////////////////////////
			final GeneralEnvelope intersection = new GeneralEnvelope(
					new double[] { requestedJTSEnvelope.getMinX(),
							requestedJTSEnvelope.getMinY() }, new double[] {
							requestedJTSEnvelope.getMaxX(),
							requestedJTSEnvelope.getMaxY() });
			intersection.setCoordinateReferenceSystem(crs);

			final GeneralEnvelope loadedTilesBoundEnv = new GeneralEnvelope(
					new double[] { loadedTilesBound.getMinX(),
							loadedTilesBound.getMinY() }, new double[] {
							loadedTilesBound.getMaxX(),
							loadedTilesBound.getMaxY() });
			loadedTilesBoundEnv.setCoordinateReferenceSystem(crs);

			// intersect them
			intersection.intersects(loadedTilesBoundEnv, true);

			// get the transform for going from world to grid
			try {
				final MathTransform transform = GeneralGridGeometry
						.getTransform(
								new GeneralGridRange(finalLayout.getBounds()),
								loadedTilesBoundEnv, false).inverse();
				final GeneralGridRange finalRange = new GeneralGridRange(
						CRSUtilities.transform(transform, intersection));
				// CROP
				finalLayout.intersect(new Area(finalRange.toRectangle()));
				Rectangle tempRect = finalLayout.getBounds();

				imageBeforeAlpha = JAI.create("Mosaic", pbjMosaic,
						new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
								new ImageLayout(tempRect.x, tempRect.y,
										tempRect.width, tempRect.height)));

			} catch (MismatchedDimensionException e) {
				throw new DataSourceException(
						"Problem when creating this mosaic.", e);
			} catch (NoninvertibleTransformException e) {
				throw new DataSourceException(
						"Problem when creating this mosaic.", e);
			} catch (TransformException e) {
				throw new DataSourceException(
						"Problem when creating this mosaic.", e);
			}

		} else
			imageBeforeAlpha = JAI.create("Mosaic", pbjMosaic, null);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Mosaic created ").toString());
		// ///////////////////////////////////////////////////////////////////
		//
		// TRANSLATE in order to have minx and minY equal o zero.
		// It is not required but it could simply things a lot in successive
		// steps.
		//
		// ///////////////////////////////////////////////////////////////////
		if (imageBeforeAlpha.getMinX() != 0 || imageBeforeAlpha.getMinY() != 0) {
			// TRANSLATE in order to have minx and minY equal o zero
			final ParameterBlock pbjTranslate = new ParameterBlock();
			pbjTranslate.addSource(imageBeforeAlpha).add(
					new Float(-imageBeforeAlpha.getMinX())).add(
					new Float(-imageBeforeAlpha.getMinY()))
					.add(nnInterpolation);
			imageBeforeAlpha = JAI.create("Translate", pbjTranslate, NO_CACHE);

		}

		// /////////////////////////////////////////////////////////////////////
		//
		// FINAL ALPHA
		//
		// /////////////////////////////////////////////////////////////////////
		if (doAlpha) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Support for alpha").toString());
			// /////////////////////////////////////////////////////////////////////
			//
			// If requested I can perform the ROI operation on the prepared ROI
			// image for building up the alpha band
			//
			// /////////////////////////////////////////////////////////////////////

			ImageWorker w = new ImageWorker(imageBeforeAlpha);
			w.setRenderingHint(JAI.KEY_TILE_CACHE, null);
			w.intensity();
			w.binarize(alphaThreshold);
			w.forceComponentColorModel();
			w.retainFirstBand();
			final PlanarImage alpha = w.getPlanarImage();

			final ParameterBlock pbjMultiplyConst = new ParameterBlock();
			pbjMultiplyConst.addSource(alpha).add(new double[] { 255 });
			final RenderedOp multipliedImage = JAI.create("MultiplyConst",
					pbjMultiplyConst, NO_CACHE);

			// /////////////////////////////////////////////////////////////////////
			//
			// create the coverage
			//
			// /////////////////////////////////////////////////////////////////////
			final ParameterBlock pbjBandMerge = new ParameterBlock();
			pbjBandMerge.addSource(imageBeforeAlpha).addSource(multipliedImage);
			return coverageFactory.create(coverageName, JAI.create("BandMerge",
					pbjBandMerge, NO_CACHE), requestedEnvelope);
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// create the coverage
		//
		// /////////////////////////////////////////////////////////////////////
		return coverageFactory.create(coverageName, imageBeforeAlpha,
				requestedEnvelope);

	}

	/**
	 * Adding an image which intersect the requested envelope to the final
	 * moisaic. This operation means computing the translation factor keeping
	 * into account the resolution of the actual image, the envelope of the
	 * loaded dataset and the envelope of this image.
	 * 
	 * @param pbjMosaic
	 * @param bound
	 *            Lon-Lat bounds of the loaded image
	 * @param ulc
	 *            Lon-Lat bounds of the loaded dataset
	 * @param res
	 * @param loadedImage
	 * @param singleImageROI
	 * @param rois
	 * @param i
	 * @param singleImageROIThreshold
	 * @param alphaChannels
	 * @param alphaIndex
	 * @param alphaIn
	 * @param finalLayout
	 * @param imageFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void addToMosaic(ParameterBlockJAI pbjMosaic, Envelope bound,
			Point2D ulc, double[] res, RenderedOp loadedImage,
			boolean singleImageROI, ROI[] rois, int i,
			int singleImageROIThreshold, boolean alphaIn, int[] alphaIndex,
			PlanarImage[] alphaChannels, Area finalLayout, File imageFile) {

		// /////////////////////////////////////////////////////////////////////
		//
		// Translation.
		// Using the spatial resolution we compute the translation factors for
		// positioning the actual image correctly in final mosaic.
		// /////////////////////////////////////////////////////////////////////
		// evaluate trans
		int xTrans = (int) Math.round((bound.getMinX() - ulc.getX()) / res[0]);
		int yTrans = (int) Math.round((ulc.getY() - bound.getMaxY()) / res[1]);

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Adding to mosaic image  ").append(
					imageFile.getAbsolutePath()).append(" as number ")
					.append(i).append(" with translations factors ").append(
							xTrans).append(" ").append(yTrans).toString());

		final ParameterBlock pbjTranslate = new ParameterBlock();
		// translation
		pbjTranslate.addSource(loadedImage).add(new Float(xTrans)).add(
				new Float(yTrans)).add(nnInterpolation);
		final RenderedOp readyToMosaicImage = JAI.create("Translate",
				pbjTranslate, NO_CACHE);
		pbjMosaic.addSource(readyToMosaicImage);
		finalLayout.add(new Area(readyToMosaicImage.getBounds()));

		// /////////////////////////////////////////////////////////////////////
		//
		// roi
		// It is ignored in case I have input alpha
		//
		// /////////////////////////////////////////////////////////////////////
		if (!alphaIn && singleImageROI) {
			ImageWorker w = new ImageWorker(readyToMosaicImage);
			w.setRenderingHint(JAI.KEY_TILE_CACHE, null);
			w.intensity();
			w.binarize(singleImageROIThreshold);
			rois[i] = w.getImageAsROI();
		} else if (alphaIn) {
			final ParameterBlock pbjBandSelect = new ParameterBlock();
			// translation
			pbjBandSelect.addSource(readyToMosaicImage).add(alphaIndex);
			alphaChannels[i] = JAI
					.create("BandSelect", pbjBandSelect, NO_CACHE);
		}

	} /*
		 * (non-Javadoc)
		 * 
		 * @see org.opengis.coverage.grid.GridCoverageReader#skip()
		 */

	public void skip() throws IOException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#dispose()
	 */
	public void dispose() throws IOException {

	}

}
