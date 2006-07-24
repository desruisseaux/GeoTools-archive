/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's
 * Fire Science Lab for internal use.  It is therefore ineligible for
 * copyright under title 17, section 105 of the United States Code.  You
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the
 * authors, for free or for compensation.  You may not claim exclusive
 * ownership of this code because it is already owned by everyone.  Use this
 * software entirely at your own risk.  No warranty of any kind is given.
 *
 * A copy of 17-USC-105 should have accompanied this distribution in the file
 * 17USC105.html.  If not, you may access the law via the US Government's
 * public websites:
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package org.geotools.gce.geotiff;

// JAI ImageIO Tools dependencies
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.GeoTiffIIOMetadataDecoder;
import org.geotools.gce.geotiff.crs_adapters.GeoTiffMetadata2CRSAdapter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

/**
 * <CODE>GeoTiffReader</CODE> is responsible for exposing the data and the
 * Georeferencing metadata available to the Geotools library. This reader is
 * heavily based on the capabilities provided by the ImageIO tools and JAI
 * libraries.
 * 
 * 
 * @author Bryce Nordgren, USDA Forest Service
 * @author Simone Giannecchini
 * @since 2.1
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/coverages_branch/trunk/gt/plugin/geotiff/src/org/geotools/gce/geotiff/GeoTiffReader.java $
 */
public final class GeoTiffReader extends AbstractGridCoverage2DReader
		implements
			GridCoverageReader {
	private Logger LOGGER = Logger.getLogger(GeoTiffReader.class.toString());

	/** SPI for creating tiff readers in ImageIO tools */
	private final TIFFImageReaderSpi readerSPI = new TIFFImageReaderSpi();

	/**
	 * Number of images read from file. read() increments this counter and
	 * hasMoreGridCoverages() accesses it.
	 */
	private int imagesRead = 0;

	/**
	 * This contains the maximum number of grid coverages in the file/stream.
	 * Until multi-image files are supported, this is going to be 0 or 1.
	 */
	private volatile int maxImages = 0;

	private GeoTiffIIOMetadataDecoder metadata;

	private GeoTiffMetadata2CRSAdapter gtcs;

	private String coverageName;

	/**
	 * Creates a new instance of GeoTiffReader
	 * 
	 * @param source
	 *            the GeoTiff file
	 * @param hints
	 *            user-supplied hints TODO currently are unused
	 * @throws DataSourceException
	 */
	public GeoTiffReader(Object input, Hints hints) throws DataSourceException {
		// /////////////////////////////////////////////////////////////////////
		// 
		// Forcing longitude first since the geotiff specification seems to
		// assume that we have first longitude the latitude.
		//
		// /////////////////////////////////////////////////////////////////////
		this.hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				Boolean.TRUE);
		if (hints != null) {
			// prevent the use from reordering axes
			hints.remove(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
			this.hints.add(hints);
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Seting input
		//
		// /////////////////////////////////////////////////////////////////////
		if (input == null) {

			final IOException ex = new IOException(
					"GeoTiffReader:No source set to read this coverage.");
			throw new DataSourceException(ex);
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Set the source being careful in case it is an URL pointing to a file
		//
		// /////////////////////////////////////////////////////////////////////
		try {
			this.source = input;
			format = new GeoTiffFormat();
			// setting source
			if (input instanceof URL) {
				final URL sourceURL = (URL) input;
				if (sourceURL.getProtocol().equalsIgnoreCase("http")
						|| sourceURL.getProtocol().equalsIgnoreCase("ftp")) {
					try {
						source = sourceURL.openStream();
					} catch (IOException e) {
						new RuntimeException(e);
					}
				} else if (sourceURL.getProtocol().equalsIgnoreCase("file"))
					source = new File(URLDecoder.decode(sourceURL.getFile(),
							"UTF-8"));
			}

			this.hints = hints;
			boolean closeMe = true;
			// /////////////////////////////////////////////////////////////////////
			//
			// Get a stream in order to read from it for getting the basic
			// information for this coverfage
			//
			// /////////////////////////////////////////////////////////////////////
			if ((source instanceof InputStream)
					|| (source instanceof ImageInputStream))
				closeMe = false;
			inStream = ImageIO.createImageInputStream(source);
			if (inStream == null)
				throw new IllegalArgumentException(
						"No input stream for the provided source");

			// /////////////////////////////////////////////////////////////////////
			//
			// Informations about multiple levels and such
			//
			// /////////////////////////////////////////////////////////////////////
			getHRInfo(hints);

			// /////////////////////////////////////////////////////////////////////
			// 
			// Coverage name
			//
			// /////////////////////////////////////////////////////////////////////
			coverageName = source instanceof File
					? ((File) source).getName()
					: "geotiff_coverage";
			final int dotIndex = coverageName.lastIndexOf('.');
			if (dotIndex != -1&&dotIndex!=coverageName.length())
				coverageName = coverageName.substring(0, dotIndex);

			// /////////////////////////////////////////////////////////////////////
			// 
			// Freeing streams
			//
			// /////////////////////////////////////////////////////////////////////
			if (closeMe)// 
				inStream.close();
		} catch (IOException e) {
			throw new DataSourceException(e);
		} catch (TransformException e) {
			throw new DataSourceException(e);
		} catch (FactoryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @param hints
	 * @throws IOException
	 * @throws FactoryException
	 * @throws GeoTiffException
	 * @throws TransformException
	 * @throws MismatchedDimensionException
	 * @throws DataSourceException
	 */
	private void getHRInfo(Hints hints) throws IOException, FactoryException,
			GeoTiffException, TransformException, MismatchedDimensionException,
			DataSourceException {
		// //
		//
		// Get a reader for this formatr
		//
		// //
		final ImageReader reader = readerSPI.createReaderInstance();

		// //
		//
		// get the METADATA
		//
		// //
		reader.setInput(inStream);
		final IIOMetadata iioMetadata = reader.getImageMetadata(0);
		metadata = new GeoTiffIIOMetadataDecoder(iioMetadata);
		gtcs = new GeoTiffMetadata2CRSAdapter(hints);
		gtcs.setMetadata(metadata);

		// //
		//
		// get the CRS INFO
		//
		// //
		// get the coordinate reference system for this coverage
		crs = gtcs.createCoordinateSystem();

		// //
		//
		// get the dimension of the hr image and build the model as well as
		// computing the resolution
		// //
		maxImages = reader.getNumImages(true);
		int hrWidth = reader.getWidth(0);
		int hrHeight = reader.getHeight(0);
		final Rectangle actualDim = new Rectangle(0, 0, hrWidth, hrHeight);
		originalGridRange = new GeneralGridRange(actualDim);

		// //
		//
		// get information for the successive images
		//
		// //
		if (maxImages > 1) {
			overViewDimensions = new int[maxImages - 1][2];
			for (int i = 1; i < maxImages; i++) {
				overViewDimensions[i - 1][0] = reader.getWidth(i);
				overViewDimensions[i - 1][1] = reader.getHeight(i);
			}

		} else
			overViewDimensions = null;

		this.raster2Model = gtcs.getRasterToModel();
		final AffineTransform tempTransform = new AffineTransform(
				(AffineTransform) raster2Model);
		tempTransform.translate(-0.5, -0.5);
		originalEnvelope = CRSUtilities.transform(ProjectiveTransform
				.create(tempTransform), new GeneralEnvelope(actualDim));
		originalEnvelope.setCoordinateReferenceSystem(crs);

		// ///
		// 
		// setting the higher resolution avalaible for this coverage
		//
		// ///
		higherRes = getResolution(originalEnvelope, actualDim, crs);
	}

	/**
	 * TODO use me in a me better way
	 */
	public void dispose() {
		try {
			if (inStream != null)
				inStream.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

	}

	/**
	 * No subnames. Always returns null.
	 * 
	 * @return null
	 */
	public String getCurrentSubname() {
		return null;
	}

	public Format getFormat() {
		if (format == null)
			format = new GeoTiffFormat();
		return format;
	}

	public String[] getMetadataNames() throws IOException {
		throw new UnsupportedOperationException(
				"GeoTIFF reader doesn't support metadata manipulation yet");
	}

	public String getMetadataValue(String name) throws IOException,
			MetadataNameNotFoundException {
		throw new UnsupportedOperationException(
				"GeoTIFF reader doesn't support metadata manipulation yet");
	}

	public Object getSource() {
		return source;
	}

	/**
	 * Returns true if another image remains to be read. This module currently
	 * only supports one image per TIFF file, so the first read will make this
	 * method return false.
	 * 
	 * @return true if another grid coverage remains to be read.
	 */
	public boolean hasMoreGridCoverages() {
		return imagesRead < maxImages;
	}

	/**
	 * Always returns null. No subnames.
	 * 
	 * @return null
	 */
	public String[] listSubNames() {
		return null;
	}

	/**
	 * This method reads in the TIFF image, constructs an appropriate CRS,
	 * determines the math transform from raster to the CRS model, and
	 * constructs a GridCoverage.
	 * 
	 * @param params
	 *            currently ignored, potentially may be used for hints.
	 * 
	 * @return grid coverage represented by the image
	 * 
	 * @throws IOException
	 *             on any IO related troubles
	 */
	public GridCoverage read(GeneralParameterValue[] params) throws IOException {
		GeneralEnvelope requestedEnvelope = null;
		Rectangle dim = null;
		if (params != null) {
			// /////////////////////////////////////////////////////////////////////
			//
			// Checking params
			//
			// /////////////////////////////////////////////////////////////////////
			if (params != null) {
				Parameter param;
				final int length = params.length;
				for (int i = 0; i < length; i++) {
					param = (Parameter) params[i];

					if (param.getDescriptor().getName().getCode().equals(
							AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
									.toString())) {
						final GridGeometry2D gg = (GridGeometry2D) param
								.getValue();
						requestedEnvelope = new GeneralEnvelope((Envelope) gg
								.getEnvelope2D());
						dim = gg.getGridRange2D().getBounds();
					}
				}
			}
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// set params
		//
		// /////////////////////////////////////////////////////////////////////
		Integer imageChoice = new Integer(0);
		final ImageReadParam readP = new ImageReadParam();
		try {
			imageChoice = setReadParams(readP, requestedEnvelope, dim, source);
		} catch (TransformException e) {
			new DataSourceException(e);
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// IMAGE READ OPERATION
		//
		// /////////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjRead = new ParameterBlockJAI("ImageRead");
		pbjRead.setParameter("ImageChoice", imageChoice);
		pbjRead.setParameter("Input", source);
		pbjRead.setParameter("Reader", readerSPI.createReaderInstance());
		pbjRead.setParameter("ReadParam", readP);
		pbjRead.setParameter("ReadMetadata", Boolean.FALSE);
		pbjRead.setParameter("VerifyInput", Boolean.FALSE);
		pbjRead.setParameter("ReadThumbnails", Boolean.FALSE);

		// /////////////////////////////////////////////////////////////////////
		//
		// BUILDING COVERAGE
		//
		// /////////////////////////////////////////////////////////////////////
		// get the raster -> model transformation and
		// create the coverage
		return createImageCoverage(ImageUtilities.tileImage(JAI.create(
				"ImageRead", pbjRead)));

	}

	/**
	 * There are no guts to this function. Only single-image TIFF files are
	 * supported.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	public void skip() {
		// add support for multi image TIFF files later.
		throw new UnsupportedOperationException(
				"No support for multi-image TIFF.");
	}

}
