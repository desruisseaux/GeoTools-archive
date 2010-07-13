/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.gce.imagemosaic;

import it.geosolutions.imageio.stream.input.spi.URLImageInputStreamSpi;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.remote.SerializableRenderedImage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.catalogbuilder.CatalogBuilder;
import org.geotools.gce.imagemosaic.catalogbuilder.CatalogBuilderConfiguration;
import org.geotools.gce.imagemosaic.catalogbuilder.CatalogBuilder.ExceptionEvent;
import org.geotools.gce.imagemosaic.catalogbuilder.CatalogBuilder.ProcessingEvent;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Converters;
import org.geotools.util.Utilities;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.datum.PixelInCell;

/**
 * Sparse utilities for the various mosaic classes. I use them to extract
 * complex code from other places.
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * 
 */
public class Utils {
    
    final static BorderExtender BORDER_EXTENDER = BorderExtender.createInstance(BorderExtender.BORDER_COPY);

    final static RenderingHints BORDER_EXTENDER_HINTS = new RenderingHints(JAI.KEY_BORDER_EXTENDER, BORDER_EXTENDER);
    
    static class Prop {
        final static String LOCATION_ATTRIBUTE = "LocationAttribute";
        final static String ENVELOPE2D = "Envelope2D";
        final static String LEVELS_NUM = "LevelsNum";
        final static String LEVELS = "Levels";
        final static String SUGGESTED_SPI = "SuggestedSPI";
        final static String EXP_RGB = "ExpandToRGB";
        final static String ABSOLUTE_PATH = "AbsolutePath";
        final static String NAME = "Name";
        final static String FOOTPRINT_MANAGEMENT = "FootprintManagement";       
    }
    
	/**
	 * Discriminator for the type of queue we should use.
	 * 
	 * @author Simone Giannecchini, GeoSolutions SAS
	 *
	 */
    enum QueueType{
    	UNBOUNDED, DIRECT;

		public static QueueType getDefault() {
			return UNBOUNDED;
		}
    }
	final static String THREADPOOL_CONFIG_FILE = "mosaicthreadpoolconfig.properties";
	static final int DEFAULT_CORE_POOLSIZE = 5;	
	/**
	 * {@link AffineTransform} that can be used to go from an image datum placed
	 * at the center of pixels to one that is placed at ULC.
	 */
	final static AffineTransform CENTER_TO_CORNER = AffineTransform
			.getTranslateInstance(PixelTranslation
					.getPixelTranslation(PixelInCell.CELL_CORNER),
					PixelTranslation
							.getPixelTranslation(PixelInCell.CELL_CORNER));
	/**
	 * {@link AffineTransform} that can be used to go from an image datum placed
	 * at the ULC corner of pixels to one that is placed at center.
	 */
	final static AffineTransform CORNER_TO_CENTER = AffineTransform
			.getTranslateInstance(-PixelTranslation
					.getPixelTranslation(PixelInCell.CELL_CORNER),
					-PixelTranslation
							.getPixelTranslation(PixelInCell.CELL_CORNER));
	/**
	 * Logger.
	 */
	private final static Logger LOGGER = org.geotools.util.logging.Logging
			.getLogger(Utils.class.toString());
	/**
	 * Default wildcard for creating mosaics.
	 */
	public static final String DEFAULT_WILCARD = "*.*";

	/**
	 * Default path behavior with respect to absolute paths.
	 */
	public static final boolean DEFAULT_PATH_BEHAVIOR = false;

	/**
	 * Default path behavior with respect to index caching.
	 */
	private static final boolean DEFAULT_CACHING_BEHAVIOR = false;

	/**
	 * Cached instance of {@link URLImageInputStreamSpi} for creating
	 * {@link ImageInputStream} instances.
	 */
	private static ImageInputStreamSpi cachedStreamSPI = new URLImageInputStreamSpi();

	/**
	 * Creates a mosaic for the provided input parameters.
	 * 
	 * @param location
	 *            path to the directory where to gather the elements for the
	 *            mosaic.
	 * @param indexName
	 *            name to give to this mosaic
	 * @param wildcard
	 *            wildcard to use for walking through files. We are using
	 *            commonsIO for this task
	 * @param hints hints to control reader instantiations
	 * @return <code>true</code> if everything is right, <code>false</code>if
	 *         something bad happens, in which case the reason should be logged
	 *         to the logger.
	 */
	static boolean createMosaic(final String location, final String indexName,
			final String wildcard, final boolean absolutePath, final Hints hints) {

		// create a mosaic index builder and set the relevant elements
		final CatalogBuilderConfiguration configuration = new CatalogBuilderConfiguration();
		configuration.setAbsolute(absolutePath);
		configuration.setHints(hints);
		configuration.setRootMosaicDirectory(location);
		configuration.setIndexingDirectories(Arrays.asList(location));
		configuration.setIndexName(indexName);

		// look for and indexed.properties file
		final File parent = new File(location);
		final File indexerProperties = new File(parent, "indexer.properties");
		if (Utils.checkFileReadable(indexerProperties)) {
			// load it and parse it
			final Properties props = Utils.loadPropertiesFromURL(DataUtilities
					.fileToURL(indexerProperties));

			// name
			if (props.containsKey("Name"))
				configuration.setIndexName(props.getProperty("Name"));

			// absolute
			if (props.containsKey("Absolute"))
				configuration.setAbsolute(Boolean.getBoolean(props
						.getProperty("Absolute")));

			// recursive
			if (props.containsKey("Recursive"))
				configuration.setRecursive(Boolean.valueOf(props
						.getProperty("Recursive")));

			// wildcard
			if (props.containsKey("Wildcard"))
				configuration.setWildcard(props.getProperty("Wildcard"));

			// schema
			if (props.containsKey("Schema"))
				configuration.setSchema(props.getProperty("Schema"));

			// time attr
			if (props.containsKey("TimeAttribute"))
				configuration.setTimeAttribute(props.getProperty("TimeAttribute"));
			
			// elevation attr
			if (props.containsKey("ElevationAttribute"))
				configuration.setElevationAttribute(props.getProperty("ElevationAttribute"));			
			

			// collectors
			if (props.containsKey("PropertyCollectors"))
				configuration.setPropertyCollectors(props.getProperty("PropertyCollectors"));
		}

		// create the builder
		final CatalogBuilder catalogBuilder = new CatalogBuilder(configuration);
		// this is going to help us with catching exceptions and logging them
		final Queue<Throwable> exceptions = new LinkedList<Throwable>();
		try {

			final CatalogBuilder.ProcessingEventListener listener = new CatalogBuilder.ProcessingEventListener() {

				@Override
				public void exceptionOccurred(ExceptionEvent event) {
					final Throwable t = event.getException();
					exceptions.add(t);
					if (LOGGER.isLoggable(Level.SEVERE))
						LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);

				}

				@Override
				public void getNotification(ProcessingEvent event) {
					if (LOGGER.isLoggable(Level.FINE))
						LOGGER.fine(event.getMessage());

				}

			};
			catalogBuilder.addProcessingEventListener(listener);
			catalogBuilder.run();
		} catch (Throwable e) {
			LOGGER.log(Level.SEVERE, "Unable to build mosaic", e);
			return false;
		} finally {
			catalogBuilder.dispose();
		}

		// check that nothing bad happened
		if (exceptions.size() > 0)
			return false;
		return true;
	}

	public static String getMessageFromException(Exception exception) {
		if (exception.getLocalizedMessage() != null)
			return exception.getLocalizedMessage();
		else
			return exception.getMessage();
	}

	static URL checkSource(Object source) throws MalformedURLException,
			DataSourceException {
		return checkSource(source, null);
	}

	/**
	 * Checks the provided {@link URL} in order to see if it is a a query to
	 * build a mosaic or not.
	 * 
	 * @param sourceURL
	 * @param hints 
	 * @return a modified version of the provided {@link URL} which points to a
	 *         shapefile in case we created a mosaic, or to the original
	 *         {@link URL}otherwise.
	 */
	static URL checkURLForMosaicQuery(final URL sourceURL,final  Hints hints) {
		// //
		//
		// Query with parameters, it might be that the user is
		// trying to build the mosaic specifying the params as
		// well
		//
		// //
		if (sourceURL.getProtocol().equalsIgnoreCase("file")) {
			final String query = sourceURL.getQuery();
			if (query != null) {
				final String[] tokens = query.split("\\&");
				final String locationPath = sourceURL.getPath();// remove
																// 'file:'
																// prefix
				String indexName = null;
				final File sourceDir = new File(locationPath);
				if (!(sourceDir.isDirectory() && sourceDir.exists() && sourceDir
						.canRead()))
					return null;
				String wildcardString = null;
				boolean absolutePath = DEFAULT_PATH_BEHAVIOR;
				for (String token : tokens) {
					// splitting token
					final String[] values = token.split("\\=");
					if (values[0].equalsIgnoreCase("name"))
						indexName = values[1];
					else if (values[0].equalsIgnoreCase("w")
							|| values[0].equalsIgnoreCase("wildcard"))
						wildcardString = values[1];
					else if (values[0].equalsIgnoreCase("p")
							|| values[0].equalsIgnoreCase("path"))
						absolutePath = Boolean.parseBoolean(values[1]);

				}

				// now check if the shapefle is already there
				final File shapeFile = new File(locationPath, indexName
						+ ".shp");
				File propertiesFile = new File(locationPath, indexName
						+ ".properties");
				if (!shapeFile.exists() || !shapeFile.canRead()
						|| !shapeFile.isFile() || !propertiesFile.exists()
						|| !propertiesFile.canRead()
						|| !propertiesFile.isFile()) {
					// try to build it
					createMosaic(locationPath, indexName != null ? indexName
							: FilenameUtils.getBaseName(locationPath),
							wildcardString != null ? wildcardString
									: DEFAULT_WILCARD, absolutePath,hints);

				}

				// check URL again
				if (!shapeFile.exists() || !shapeFile.canRead()
						|| !shapeFile.isFile() || !propertiesFile.exists()
						|| !propertiesFile.canRead()
						|| !propertiesFile.isFile())
					return null;
				else
					try {
						return shapeFile.toURI().toURL();
					} catch (MalformedURLException e) {
						if (LOGGER.isLoggable(Level.FINE))
							LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
					}

			}

		}

		return sourceURL;
	}

	static MosaicConfigurationBean loadMosaicProperties(final URL sourceURL,
                final String defaultLocationAttribute) {
	    return loadMosaicProperties(sourceURL, defaultLocationAttribute, null);
	}
	
	static MosaicConfigurationBean loadMosaicProperties(
	        final URL sourceURL,
		final String defaultLocationAttribute, 
		final Set<String> ignorePropertiesSet) {
		// ret value
		final MosaicConfigurationBean retValue = new MosaicConfigurationBean();
		final boolean ignoreSome = ignorePropertiesSet != null && !ignorePropertiesSet.isEmpty();

		//
		// load the properties file
		//
		URL propsURL = sourceURL;
		if (!sourceURL.toExternalForm().endsWith(".properties"))
			propsURL = DataUtilities.changeUrlExt(sourceURL, "properties");
		final Properties properties = loadPropertiesFromURL(propsURL);
		if (properties == null) {
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER.info("Unable to load mosaic properties file");
			return null;
		}

		String[] pairs = null;
		String pair[] = null;
		
		//
                // resolutions levels
                //              
                if (!ignoreSome || !ignorePropertiesSet.contains(Prop.LEVELS)){
                        int levelsNumber = Integer.parseInt(properties.getProperty(Prop.LEVELS_NUM,"1").trim()) ;
                        retValue.setLevelsNum(levelsNumber);
                        if(!properties.containsKey(Prop.LEVELS)) {
                                if(LOGGER.isLoggable(Level.INFO))
                                        LOGGER.info("Required key Levels not found.");                
                                return  null;
                        }                       
                        final String levels = properties.getProperty(Prop.LEVELS).trim();
                        pairs = levels.split(" ");
                        if (pairs == null || pairs.length != levelsNumber) {
                            if (LOGGER.isLoggable(Level.INFO))
                                LOGGER.info("Levels number is different from the provided number of levels resoltion.");
                                return null;
                        }
                        final double[][] resolutions = new double[levelsNumber][2];
                        for (int i = 0; i < levelsNumber; i++) {
                                pair = pairs[i].split(",");
                                if (pair == null || pair.length != 2) {
                                    if (LOGGER.isLoggable(Level.INFO))
                                        LOGGER.info("OverviewLevel number is different from the provided number of levels resoltion.");
                                        return null;
                                }                               
                                resolutions[i][0] = Double.parseDouble(pair[0]);
                                resolutions[i][1] = Double.parseDouble(pair[1]);
                        }
                        retValue.setLevels(resolutions);
                }

		//
		// suggested spi is optional
		//
                if (!ignoreSome || !ignorePropertiesSet.contains(Prop.SUGGESTED_SPI)) {
                    if (properties.containsKey(Prop.SUGGESTED_SPI)) {
                            final String suggestedSPI = properties.getProperty(Prop.SUGGESTED_SPI).trim();
                            retValue.setSuggestedSPI(suggestedSPI);
                    }
                }

		//
		// time attribute is optional
		//
		if (properties.containsKey("TimeAttribute")) {
		        final String timeAttribute = properties.getProperty("TimeAttribute").trim();
			retValue.setTimeAttribute(timeAttribute);
		}

		//
		// elevation attribute is optional
		//
		if (properties.containsKey("ElevationAttribute")) {
		        final String elevationAttribute = properties.getProperty("ElevationAttribute").trim();
			retValue.setElevationAttribute(elevationAttribute);
		}

		//
		// caching
		//
		if (properties.containsKey("Caching")) {
			String caching = properties.getProperty("Caching").trim();
			try {
				retValue.setCaching(Boolean.valueOf(caching));
			} catch (Throwable e) {
				retValue.setCaching(Boolean.valueOf(Utils.DEFAULT_CACHING_BEHAVIOR));
			}
		}

		//
		// name is not optional
		//
		if (!ignoreSome || !ignorePropertiesSet.contains(Prop.NAME)){
                    if(!properties.containsKey(Prop.NAME)) {
                            if(LOGGER.isLoggable(Level.SEVERE))
                                    LOGGER.severe("Required key Name not found.");          
                            return  null;
                    }                       
                    String coverageName = properties.getProperty(Prop.NAME).trim();
                    retValue.setName(coverageName);
                }

		// need a color expansion?
                // this is a newly added property we have to be ready to the case where
                // we do not find it.
                if (!ignoreSome || !ignorePropertiesSet.contains(Prop.EXP_RGB)) {
                        final boolean expandMe = Boolean.valueOf(properties.getProperty(Prop.EXP_RGB,"false").trim());  
                        retValue.setExpandToRGB(expandMe);
                }

                //
                // Absolute or relative path
                //
                if (!ignoreSome || !ignorePropertiesSet.contains(Prop.ABSOLUTE_PATH)) {
                        final boolean absolutePath = Boolean.parseBoolean(properties.getProperty(Prop.ABSOLUTE_PATH, Boolean.toString(Utils.DEFAULT_PATH_BEHAVIOR)).trim());
                        retValue.setAbsolutePath(absolutePath);
                }
                
                //
                // Footprint management
                //
                if (!ignoreSome || !ignorePropertiesSet.contains(Prop.FOOTPRINT_MANAGEMENT)) {
                        final boolean footprintManagement=Boolean.valueOf(properties.getProperty(Prop.FOOTPRINT_MANAGEMENT, "false").trim());   
                        retValue.setFootprintManagement(footprintManagement);
                }
                
                //
                //  location
                //  
                if (!ignoreSome || !ignorePropertiesSet.contains(Prop.LOCATION_ATTRIBUTE)) {
                        retValue.setLocationAttribute(properties.getProperty(Prop.LOCATION_ATTRIBUTE, Utils.DEFAULT_LOCATION_ATTRIBUTE).trim());
                }

		// retrn value
		return retValue;
	}

	public static Properties loadPropertiesFromURL(URL propsURL) {
		final Properties properties = new Properties();
		InputStream stream = null;
		InputStream openStream = null;
		try {
			openStream = propsURL.openStream();
			stream = new BufferedInputStream(openStream);
			properties.load(stream);
		} catch (FileNotFoundException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		} finally {

			if (stream != null)
				IOUtils.closeQuietly(stream);

			if (openStream != null)
				IOUtils.closeQuietly(openStream);

		}
		return properties;
	}

	/**
	 * Returns a suitable threshold depending on the {@link DataBuffer} type.
	 * 
	 * <p>
	 * Remember that the threshold works with >=.
	 * 
	 * @param dataType
	 *            to create a low threshold for.
	 * @return a minimum threshold value suitable for this data type.
	 */
	static double getThreshold(int dataType) {
		switch (dataType) {
		case DataBuffer.TYPE_BYTE:
		case DataBuffer.TYPE_USHORT:
			// this may cause problems and truncations when the native mosaic
			// operations is enabled
			return 0.0;
		case DataBuffer.TYPE_INT:
			return Integer.MIN_VALUE;
		case DataBuffer.TYPE_SHORT:
			return Short.MIN_VALUE;
		case DataBuffer.TYPE_DOUBLE:
			return -Double.MAX_VALUE;
		case DataBuffer.TYPE_FLOAT:
			return -Float.MAX_VALUE;
		}
		return 0;
	}

	/**
	 * Builds a {@link ReferencedEnvelope} in WGS84 from a {@link GeneralEnvelope}.
	 * 
	 * @param coverageEnvelope
	 *            the {@link GeneralEnvelope} to convert.
	 * @return an instance of {@link ReferencedEnvelope} in WGS84 or <code>null</code> in case a problem during the conversion occurs.
	 */
	static ReferencedEnvelope getWGS84ReferencedEnvelope(
			final GeneralEnvelope coverageEnvelope) {
		Utilities.ensureNonNull("coverageEnvelope", coverageEnvelope);
		final ReferencedEnvelope refEnv= new ReferencedEnvelope(coverageEnvelope);
		try{
		    return refEnv.transform(DefaultGeographicCRS.WGS84, true);
		}catch (Exception e) {
                    return null;
                }
	}

	/**
	 * @param transparentColor
	 * @param image
	 * @return
	 * @throws IllegalStateException
	 */
	static RenderedImage makeColorTransparent(final Color transparentColor,
			final RenderedImage image) throws IllegalStateException {
		final ImageWorker w = new ImageWorker(image);
		if (image.getSampleModel() instanceof MultiPixelPackedSampleModel)
			w.forceComponentColorModel();
		return w.makeColorTransparent(transparentColor).getRenderedImage();
	}

	static ImageReadParam cloneImageReadParam(ImageReadParam param) {

		// The ImageReadParam passed in is non-null. As the
		// ImageReadParam class is not Cloneable, if the param
		// class is simply ImageReadParam, then create a new
		// ImageReadParam instance and set all its fields
		// which were set in param. This will eliminate problems
		// with concurrent modification of param for the cases
		// in which there is not a special ImageReadparam used.

		// Create a new ImageReadParam instance.
		ImageReadParam newParam = new ImageReadParam();

		// Set all fields which need to be set.

		// IIOParamController field.
		if (param.hasController()) {
			newParam.setController(param.getController());
		}

		// Destination fields.
		newParam.setDestination(param.getDestination());
		if (param.getDestinationType() != null) {
			// Set the destination type only if non-null as the
			// setDestinationType() clears the destination field.
			newParam.setDestinationType(param.getDestinationType());
		}
		newParam.setDestinationBands(param.getDestinationBands());
		newParam.setDestinationOffset(param.getDestinationOffset());

		// Source fields.
		newParam.setSourceBands(param.getSourceBands());
		newParam.setSourceRegion(param.getSourceRegion());
		if (param.getSourceMaxProgressivePass() != Integer.MAX_VALUE) {
			newParam.setSourceProgressivePasses(param
					.getSourceMinProgressivePass(), param
					.getSourceNumProgressivePasses());
		}
		if (param.canSetSourceRenderSize()) {
			newParam.setSourceRenderSize(param.getSourceRenderSize());
		}
		newParam.setSourceSubsampling(param.getSourceXSubsampling(), param
				.getSourceYSubsampling(), param.getSubsamplingXOffset(), param
				.getSubsamplingYOffset());

		// Replace the local variable with the new ImageReadParam.
		return newParam;

	}

	public static IOFileFilter excludeFilters(final IOFileFilter inputFilter,
			IOFileFilter... filters) {
		IOFileFilter retFilter = inputFilter;
		for (IOFileFilter filter : filters) {
			retFilter = FileFilterUtils.andFileFilter(retFilter,
					FileFilterUtils.notFileFilter(filter));
		}
		return retFilter;
	}

	/**
	 * Look for an {@link ImageReader} instance that is able to read the
	 * provided {@link ImageInputStream}, which must be non null.
	 * 
	 * <p>
	 * In case no reader is found, <code>null</code> is returned.
	 * 
	 * @param inStream
	 *            an instance of {@link ImageInputStream} for which we need to
	 *            find a suitable {@link ImageReader}.
	 * @return a suitable instance of {@link ImageReader} or <code>null</code>
	 *         if one cannot be found.
	 */
	static ImageReader getReader(final ImageInputStream inStream) {
		Utilities.ensureNonNull("inStream", inStream);
		// get a reader
		inStream.mark();
		final Iterator<ImageReader> readersIt = ImageIO
				.getImageReaders(inStream);
		if (!readersIt.hasNext()) {
			return null;
		}
		return readersIt.next();
	}

	/**
	 * Retrieves the dimensions of the {@link RenderedImage} at index
	 * <code>imageIndex</code> for the provided {@link ImageReader} and
	 * {@link ImageInputStream}.
	 * 
	 * <p>
	 * Notice that none of the input parameters can be <code>null</code> or a
	 * {@link NullPointerException} will be thrown. Morevoer the
	 * <code>imageIndex</code> cannot be negative or an
	 * {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param imageIndex
	 *            the index of the image to get the dimensions for.
	 * @param inStream
	 *            the {@link ImageInputStream} to use as an input
	 * @param reader
	 *            the {@link ImageReader} to decode the image dimensions.
	 * @return a {@link Rectangle} that contains the dimensions for the image at
	 *         index <code>imageIndex</code>
	 * @throws IOException
	 *             in case the {@link ImageReader} or the
	 *             {@link ImageInputStream} fail.
	 */
	static Rectangle getDimension(final int imageIndex,
			final ImageInputStream inStream, final ImageReader reader)
			throws IOException {
		Utilities.ensureNonNull("inStream", inStream);
		Utilities.ensureNonNull("reader", reader);
		if (imageIndex < 0)
			throw new IllegalArgumentException(Errors.format(
					ErrorKeys.INDEX_OUT_OF_BOUNDS_$1, imageIndex));
		inStream.reset();
		reader.setInput(inStream);
		return new Rectangle(0, 0, reader.getWidth(imageIndex), reader
				.getHeight(imageIndex));
	}

	/**
	 * Retrieves an {@link ImageInputStream} for the provided input {@link File}
	 * .
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	static ImageInputStream getInputStream(final File file) throws IOException {
		final ImageInputStream inStream = ImageIO.createImageInputStream(file);
		if (inStream == null)
			return null;
		return inStream;
	}

	/**
	 * Retrieves an {@link ImageInputStream} for the provided input {@link URL}.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	static ImageInputStream getInputStream(final URL url) throws IOException {
		final ImageInputStream inStream = cachedStreamSPI
				.createInputStreamInstance(url, ImageIO.getUseCache(), ImageIO
						.getCacheDirectory());
		if (inStream == null)
			return null;
		return inStream;
	}

	/**
	 * Default priority for the underlying {@link Thread}.
	 */
	public static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;
	/**
	 * Default location attribute name.
	 */
	public static final String DEFAULT_LOCATION_ATTRIBUTE = "location";

	public static final String DEFAULT_INDEX_NAME = "index";

	/**
	 * Checks that a {@link File} is a real file, exists and is readable.
	 * 
	 * @param file
	 *            the {@link File} instance to check. Must not be null.
	 * 
	 * @return <code>true</code> in case the file is a real file, exists and is
	 *         readable; <code>false </code> otherwise.
	 */
	public static boolean checkFileReadable(final File file) {
		if (LOGGER.isLoggable(Level.FINE)) {
			final StringBuilder builder = new StringBuilder();
			builder.append("Checking file:").append(
					FilenameUtils.getFullPath(file.getAbsolutePath())).append(
					"\n");
			builder.append("canRead:").append(file.canRead()).append("\n");
			builder.append("isHidden:").append(file.isHidden()).append("\n");
			builder.append("isFile").append(file.isFile()).append("\n");
			builder.append("canWrite").append(file.canWrite()).append("\n");
			LOGGER.fine(builder.toString());
		}
		if (!file.exists() || !file.canRead() || !file.isFile())
			return false;
		return true;
	}

	/**
	 * @param testingDirectory
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public static String checkDirectory(String testingDirectory)
			throws IllegalArgumentException {
		File inDir = new File(testingDirectory);
		if (!inDir.isDirectory() || !inDir.canRead()) {
			LOGGER.severe("Provided input dir does not exist or is not a dir!");
			throw new IllegalArgumentException(
					"Provided input dir does not exist or is not a dir!");
		}
		try {
			testingDirectory = inDir.getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		testingDirectory = FilenameUtils.normalize(testingDirectory);
		if (!testingDirectory.endsWith(File.separator))
			testingDirectory = testingDirectory + File.separator;
		// test to see if things are still good
		inDir = new File(testingDirectory);
		if (!inDir.isDirectory() || !inDir.canRead()) {
			LOGGER.severe("Provided input dir does not exist or is not a dir!");
			throw new IllegalArgumentException(
					"Provided input dir does not exist or is not a dir!");
		}
		return testingDirectory;
	}

	static boolean checkURLReadable(URL url) {
		try {
			url.openStream().close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static final DataStoreFactorySpi SHAPE_SPI = new ShapefileDataStoreFactory();

	public static final DataStoreFactorySpi INDEXED_SHAPE_SPI = new ShapefileDataStoreFactory();

	static final String DIRECT_KAKADU_PLUGIN = "it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReader";

	public static final boolean DEFAULT_RECURSION_BEHAVIOR = true;

	/**
	 * 
	 * @param datastoreProperties
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Serializable> createDataStoreParamsFromPropertiesFile(
			final URL datastoreProperties)
			throws IOException {
		// read the properties file
		Properties properties = loadPropertiesFromURL(datastoreProperties);
		if (properties == null)
			return null;

		// SPI
		final String SPIClass = properties.getProperty("SPI");
		try {
			// create a datastore as instructed
			final DataStoreFactorySpi spi = (DataStoreFactorySpi) Class.forName(SPIClass).newInstance();
			return createDataStoreParamsFromPropertiesFile(properties, spi);
		} catch (ClassNotFoundException e) {
			final IOException ioe = new IOException();
			throw (IOException) ioe.initCause(e);
		} catch (InstantiationException e) {
			final IOException ioe = new IOException();
			throw (IOException) ioe.initCause(e);
		} catch (IllegalAccessException e) {
			final IOException ioe = new IOException();
			throw (IOException) ioe.initCause(e);
		}
	}

	/**
	 * Store a sample image from which we can derive the default SM and CM
	 * 
	 * @param sampleImageFile
	 *            where we should store the image
	 * @param defaultSM
	 *            the {@link SampleModel} for the sample image.
	 * @param defaultCM
	 *            the {@link ColorModel} for the sample image.
	 * @throws IOException
	 *             in case something bad occurs during writing.
	 */
	public static void storeSampleImage(final File sampleImageFile,
			final SampleModel defaultSM, final ColorModel defaultCM)
			throws IOException {
		// create 1X1 image
		final SampleModel sm = defaultSM.createCompatibleSampleModel(1, 1);
		final WritableRaster raster = RasterFactory.createWritableRaster(sm,
				null);
		final BufferedImage sampleImage = new BufferedImage(defaultCM, raster,
				false, null);

		// serialize it
		OutputStream outStream = null;
		ObjectOutputStream ooStream = null;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(
					sampleImageFile));
			ooStream = new ObjectOutputStream(outStream);
			ooStream.writeObject(new SerializableRenderedImage(sampleImage));
		} finally {
			try {
				if (ooStream != null)
					ooStream.close();
			} catch (Throwable e) {
				IOUtils.closeQuietly(ooStream);
			}
			try {
				if (outStream != null)
					outStream.close();
			} catch (Throwable e) {
				IOUtils.closeQuietly(outStream);
			}
		}
	}

	/**
	 * Load a sample image from which we can take the sample model and color
	 * model to be used to fill holes in responses.
	 * 
	 * @param sampleImageFile
	 *            the path to sample image.
	 * @return a sample image from which we can take the sample model and color
	 *         model to be used to fill holes in responses.
	 */
	public static RenderedImage loadSampleImage(final File sampleImageFile) {
		// serialize it
		InputStream inStream = null;
		ObjectInputStream oiStream = null;
		try {

			// do we have the sample image??
			if (Utils.checkFileReadable(sampleImageFile)) {
				inStream = new BufferedInputStream(new FileInputStream(
						sampleImageFile));
				oiStream = new ObjectInputStream(inStream);

				// load the image
				return (RenderedImage) oiStream.readObject();

			} else {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.warning("Unable to find sample image for path "
							+ sampleImageFile);
				return null;
			}
		} catch (FileNotFoundException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		} catch (ClassNotFoundException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		} finally {
			try {
				if (inStream != null)
					inStream.close();
			} catch (Throwable e) {

				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			}
			try {
				if (oiStream != null)
					oiStream.close();
			} catch (Throwable e) {

				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			}
		}
	}

	static final FilterFactory2 FILTER_FACTORY = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
	/**
	 * A transparent color for missing data.
	 */
	static final Color TRANSPARENT = new Color(0,0,0,0);
        
	final static Boolean IGNORE_FOOTPRINT = Boolean.getBoolean("org.geotools.footprint.ignore");
	
        public static final boolean DEFAULT_FOOTPRINT_MANAGEMENT = true;
	
	static final int DEFAULT_MAX_POOLSIZE = 15;
	public static final int DEFAULT_KEEP_ALIVE = 30;
	static final QueueType DEFAULT_QUEUE_TYPE = QueueType.getDefault();
	/** 
	     * Build a background values array using the same dataType of the input {@link SampleModel} (if available). 
	     * 
	     * @param sampleModel
	     * @param backgroundValues
	     * @return
	     */
	    static Number[] getBackgroundValues(final SampleModel sampleModel, final double[] backgroundValues) {
	        Number[] values = null;
	        final int dataType = sampleModel != null ? sampleModel.getDataType() : DataBuffer.TYPE_DOUBLE;
	        final int numBands=sampleModel.getNumBands();
	        switch (dataType){
	            case DataBuffer.TYPE_BYTE:
	                values = new Byte[numBands];
	                 if (backgroundValues == null){                          
	                         Arrays.fill(values, Byte.valueOf((byte)0));
	                 }
	                 else{
	                        //we have background values available
	                     for (int i = 0; i < values.length; i++)
	                         values[i] = i>=backgroundValues.length?Byte.valueOf((byte)backgroundValues[0]):Byte.valueOf((byte)backgroundValues[i]);
	                 }
	                 break;
	            case DataBuffer.TYPE_SHORT:
	            case DataBuffer.TYPE_USHORT:
	                 values = new Short[numBands] ;
	                 if (backgroundValues == null)
	                         Arrays.fill(values, Short.valueOf((short)0));
	                 else {
	                        //we have background values available
        	                 for (int i = 0; i < values.length; i++)
        	                     values[i] = i>=backgroundValues.length?Short.valueOf((short)backgroundValues[0]):Short.valueOf((short)backgroundValues[i]);
	                 }
	                 break;
	            case DataBuffer.TYPE_INT:
	                values = new Integer[numBands] ;
                        if (backgroundValues == null)
                            Arrays.fill(values, Integer.valueOf((int) 0));
                        else {
                            // we have background values available
                            for (int i = 0; i < values.length; i++)
                                values[i] = i >= backgroundValues.length ? Integer.valueOf((int) backgroundValues[0]) : Integer.valueOf((int) backgroundValues[i]);
            
                        }
	                 break;
	            case DataBuffer.TYPE_FLOAT:
	                values = new Float[numBands] ;
	                 if (backgroundValues == null)
	                        Arrays.fill(values, Float.valueOf(0.f));
	                 else{
	                        //we have background values available
	                     for (int i = 0; i < values.length; i++)
	                         values[i] = i>=backgroundValues.length?Float.valueOf((float)backgroundValues[0]):Float.valueOf((float)backgroundValues[i]);
	                 }
	                 break;
	            case DataBuffer.TYPE_DOUBLE:
	                values = new Double[numBands] ;
	                 if (backgroundValues == null)
	                        Arrays.fill(values, Double.valueOf(0.d));
	                 else {
	                        //we have background values available
	                     for (int i = 0; i < values.length; i++)
	                         values[i] = i>=backgroundValues.length?Double.valueOf((Double)backgroundValues[0]):Double.valueOf((Double)backgroundValues[i]);
	                 }
	                 break;
	            }
	        return values;
	    }

	public static Map<String, Serializable> createDataStoreParamsFromPropertiesFile(
			Properties properties, DataStoreFactorySpi spi) throws IOException {
		// get the params
		final Map<String, Serializable> params = new HashMap<String, Serializable>();
		final Param[] paramsInfo = spi.getParametersInfo();
		for (Param p : paramsInfo) {
			// search for this param and set the value if found
			if (properties.containsKey(p.key))
				params.put(p.key, (Serializable) Converters.convert(properties.getProperty(p.key), p.type));
			else if (p.required && p.sample == null)
				throw new IOException("Required parameter missing: "+ p.toString());
		}
		
		return params;
	}

    static URL checkSource(Object source, Hints hints) {
        URL sourceURL = null;
        File sourceFile = null;
        // /////////////////////////////////////////////////////////////////////
        //
        // Check source
        //
        // /////////////////////////////////////////////////////////////////////
        // if it is a URL or a String let's try to see if we can get a file to
        // check if we have to build the index
        if (source instanceof File) {
                sourceFile = (File) source;
                sourceURL = DataUtilities.fileToURL(sourceFile);
                sourceURL = checkURLForMosaicQuery((URL) sourceURL,hints);
        } else if (source instanceof URL) {
                sourceURL = checkURLForMosaicQuery((URL) source,hints);
                if (sourceURL.getProtocol().equals("file")) {
                        sourceFile = DataUtilities.urlToFile(sourceURL);
                }
        } else if (source instanceof String) {
                // is it a File?
                final String tempSource = (String) source;
                File tempFile = new File(tempSource);
                if (!tempFile.exists()) {
                        // is it a URL
                        try {
                                sourceURL = new URL(tempSource);
                                sourceURL = checkURLForMosaicQuery(sourceURL,hints);
                                source = DataUtilities.urlToFile(sourceURL);
                        } catch (MalformedURLException e) {
                                sourceURL = null;
                                source = null;
                        }
                } else {
                        sourceURL = DataUtilities.fileToURL(tempFile);

                        // so that we can do our magic here below
                        sourceFile = tempFile;
                }
        }

        // //
        //
        // at this point we have tried to convert the thing to a File as hard as
        // we could, let's see what we can do
        //
        // //
        if (sourceFile != null) {
                if (!sourceFile.isDirectory())
                        // real file, can only be a shapefile at this stage or a
                        // datastore.properties file
                        sourceURL = DataUtilities.fileToURL((File) sourceFile);
                else {
                        // it's a DIRECTORY, let's look for a possible properties files
                        // that we want to load
                        final String locationPath = sourceFile.getAbsolutePath();
                        final String defaultIndexName = FilenameUtils.getName(locationPath);
                        boolean datastoreFound = false;
                        boolean buildMosaic = false;

                        //
                        // do we have a datastore properties file? It will preempt on
                        // the shapefile
                        //
                        File dataStoreProperties = new File(locationPath,"datastore.properties");

                        // this can be used to look for properties files that do NOT
                        // define a datastore
                        final File[] properties = sourceFile
                                        .listFiles((FilenameFilter) FileFilterUtils
                                                        .andFileFilter(
                                                                        FileFilterUtils
                                                                                        .notFileFilter(FileFilterUtils
                                                                                                        .nameFileFilter("datastore.properties")),
                                                                        FileFilterUtils
                                                                                        .makeFileOnly(FileFilterUtils
                                                                                                        .suffixFileFilter(".properties"))));

                        // do we have a valid datastore + mosaic properties pair?
                        if (Utils.checkFileReadable(dataStoreProperties)) {
                                // we have a datastore.properties file
                                datastoreFound = true;

                                // check the first valid mosaic properties
                                boolean found = false;
                                for (File propFile : properties)
                                        if (Utils.checkFileReadable(propFile)) {
                                                // load it
                                                if (null != Utils.loadMosaicProperties(DataUtilities.fileToURL(propFile),"location")) {
                                                        found = true;
                                                        break;
                                                }
                                        }

                                // we did not find any good candidate for mosaic.properties
                                // file, this will signal it
                                if (!found)
                                        buildMosaic = true;

                        } else
                        {
                                // we did not find any good candidate for mosaic.properties
                                // file, this will signal it
                                buildMosaic = true;
                                datastoreFound = false;
                        }

                        //
                        // now let's try with shapefile and properties couple
                        //
                        File shapeFile = null;
                        if (!datastoreFound) {
                                for (File propFile : properties) {

                                        // load properties
                                        if (null == Utils.loadMosaicProperties(DataUtilities.fileToURL(propFile), Utils.DEFAULT_LOCATION_ATTRIBUTE))
                                                continue;

                                        // look for a couple shapefile, mosaic properties file
                                        shapeFile = new File(locationPath, FilenameUtils.getBaseName(propFile.getName())+ ".shp");
                                        if (!Utils.checkFileReadable(shapeFile)&& Utils.checkFileReadable(propFile))
                                                buildMosaic = true;
                                        else {
                                                buildMosaic = false;
                                                break;
                                        }
                                }

                        }

                        // did we find anything?
                        if (buildMosaic) {
                                // try to build a mosaic inside this directory and see what
                                // happens
                                createMosaic(locationPath, defaultIndexName,DEFAULT_WILCARD, DEFAULT_PATH_BEHAVIOR,hints);

                                // check that the mosaic properties file was created
                                final File propertiesFile = new File(locationPath,
                                                defaultIndexName + ".properties");
                                if (!Utils.checkFileReadable(propertiesFile)) {
                                        sourceURL = null;
                                        return sourceURL;
                                }

                                // check that the shapefile was correctly created in case it
                                // was needed
                                if (!datastoreFound) {
                                        shapeFile = new File(locationPath, defaultIndexName+ ".shp");

                                        if (!Utils.checkFileReadable(shapeFile))
                                                sourceURL = null;
                                        else
                                                // now set the new source and proceed
                                                sourceURL = DataUtilities.fileToURL(shapeFile);
                                } else {
                                        dataStoreProperties = new File(locationPath,"datastore.properties");

                                        // datastore.properties as the source
                                        if (!Utils.checkFileReadable(dataStoreProperties))
                                                sourceURL = null;
                                        else
                                                sourceURL = DataUtilities.fileToURL(dataStoreProperties);
                                }

                        } else
                                // now set the new source and proceed
                                sourceURL = datastoreFound ? DataUtilities.fileToURL(dataStoreProperties) : DataUtilities.fileToURL(shapeFile); 

                }
        } else {
                // SK: We don't set SourceURL to null now, just because it doesn't
                // point to a file
                // sourceURL=null;
        }
        return sourceURL;
    }
}
